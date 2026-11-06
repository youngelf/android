package com.eggwall.rhythmtrainer;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure-Java background-thread audio synthesizer for Rhythm Trainer (API 19+ compatible).
 * Synthesizes and streams the 4..3..2..1 countdown clicks followed by the single-frequency
 * (440 Hz Concert A4) rhythm notes and rests based on their musical duration.
 * Notifies the UI once per countdown beat and once per measure (bar) to keep UI rendering calm.
 */
public final class RhythmAudioEngine {
    public static final int SAMPLE_RATE = 44100;
    /** Single fixed frequency (Concert A4 = 440 Hz) for all rhythm notes and clicks. */
    public static final double FIXED_FREQUENCY_HZ = 440.0;
    public static final int COUNTDOWN_START = 4;

    public interface PlaybackListener {
        void onCountdownTick(Integer count);
        void onBarChanged(ElementPosition activeBarPosition);
        void onPlaybackCompleted();
    }

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private volatile boolean running = false;
    private Thread audioThread;

    /**
     * Computes the duration in milliseconds of one sixteenth-note unit at bpm
     * (where bpm is quarter-note beats per minute).
     */
    public static double sixteenthDurationMs(int bpm) {
        int safeBpm = Math.max(30, Math.min(240, bpm));
        double quarterDurationMs = 60000.0 / safeBpm;
        return quarterDurationMs / 4.0;
    }

    public static short[] synthesizeEventPcm(int totalSamples, boolean isRest, boolean isCountdownClick) {
        return synthesizeEventPcm(totalSamples, isRest, isCountdownClick, SAMPLE_RATE, FIXED_FREQUENCY_HZ);
    }

    /**
     * Synthesizes a 16-bit mono PCM buffer for either a countdown click or a rhythm note/rest.
     */
    public static short[] synthesizeEventPcm(
            int totalSamples,
            boolean isRest,
            boolean isCountdownClick,
            int sampleRate,
            double frequencyHz) {
        int n = Math.max(1, totalSamples);
        short[] buffer = new short[n];
        if (isRest || totalSamples <= 0) {
            return buffer;
        }

        if (isCountdownClick) {
            int clickSamples = Math.min(totalSamples, (int) (sampleRate * 0.050));
            int attackSamples = Math.max(1, (int) (sampleRate * 0.003));
            for (int i = 0; i < clickSamples; i++) {
                double t = (double) i / sampleRate;
                double attackEnv = (i < attackSamples) ? ((double) i / attackSamples) : 1.0;
                double decayEnv = Math.exp(-t * 55.0);
                double sample = Math.sin(2.0 * Math.PI * frequencyHz * t) * attackEnv * decayEnv;
                int val = (int) (sample * Short.MAX_VALUE * 0.90);
                if (val > Short.MAX_VALUE) val = Short.MAX_VALUE;
                if (val < Short.MIN_VALUE) val = Short.MIN_VALUE;
                buffer[i] = (short) val;
            }
            return buffer;
        }

        // Rhythm note: crisp click onset + sustained body proportional to note duration + articulation gap
        int articulationGapSamples = Math.max(
                (int) (sampleRate * 0.012),
                Math.min((int) (totalSamples * 0.14), (int) (sampleRate * 0.045))
        );
        int activeSamples = Math.max(1, totalSamples - articulationGapSamples);
        int attackSamples = Math.max(1, (int) (sampleRate * 0.003));
        int releaseSamples = Math.max(1, Math.min((int) (activeSamples * 0.15), (int) (sampleRate * 0.025)));

        for (int i = 0; i < activeSamples; i++) {
            double t = (double) i / sampleRate;
            double attackEnv = (i < attackSamples) ? ((double) i / attackSamples) : 1.0;
            double releaseEnv = (i > activeSamples - releaseSamples)
                    ? ((double) (activeSamples - i) / releaseSamples)
                    : 1.0;
            double clickBoost = 0.45 * Math.exp(-t * 65.0);
            double sustainLevel = 0.50;
            double envelope = attackEnv * releaseEnv * (sustainLevel + clickBoost);
            double wave = Math.sin(2.0 * Math.PI * frequencyHz * t);
            int val = (int) (wave * envelope * Short.MAX_VALUE);
            if (val > Short.MAX_VALUE) val = Short.MAX_VALUE;
            if (val < Short.MIN_VALUE) val = Short.MIN_VALUE;
            buffer[i] = (short) val;
        }
        return buffer;
    }

    public synchronized void startPlayback(
            List<Measure> measures,
            final int bpm,
            final boolean loop,
            final PlaybackListener listener) {
        stopPlayback();
        final List<Measure> measuresCopy = new ArrayList<Measure>(measures);
        running = true;

        final Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                runLoop(measuresCopy, bpm, loop, listener);
            }
        }, "RhythmTrainer-Audio");
        worker.setPriority(Thread.MAX_PRIORITY - 1);
        audioThread = worker;
        worker.start();
    }

    public synchronized void stopPlayback() {
        running = false;
        if (audioThread != null) {
            audioThread.interrupt();
            audioThread = null;
        }
    }

    @SuppressWarnings("deprecation")
    private static AudioTrack createAudioTrack(int bufSize) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            AudioFormat format = new AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build();
            return new AudioTrack(
                    attrs,
                    format,
                    bufSize,
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE);
        }
        return new AudioTrack(
                AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufSize,
                AudioTrack.MODE_STREAM);
    }

    private void runLoop(
            List<Measure> measures,
            int bpm,
            boolean loop,
            final PlaybackListener listener) {
        int minBufferBytes = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );
        int bufSize = Math.max(4096, minBufferBytes);

        AudioTrack track = null;
        try {
            track = createAudioTrack(bufSize);
            if (track.getState() != AudioTrack.STATE_INITIALIZED) {
                return;
            }
            track.play();

            double sixteenthMs = sixteenthDurationMs(bpm);
            int quarterSamples = Math.max(1, (int) ((sixteenthMs * 4.0 / 1000.0) * SAMPLE_RATE));

            // Phase 1: Countdown 4 .. 3 .. 2 .. 1 using clicks
            for (int count = COUNTDOWN_START; count >= 1; count--) {
                if (!running || Thread.currentThread().isInterrupted()) break;
                final int tickVal = count;
                if (listener != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (running) {
                                listener.onBarChanged(null);
                                listener.onCountdownTick(Integer.valueOf(tickVal));
                            }
                        }
                    });
                }
                short[] clickPcm = synthesizeEventPcm(quarterSamples, false, true);
                if (!writePcmChunked(track, clickPcm)) break;
            }

            if (running && listener != null) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (running) {
                            listener.onCountdownTick(null);
                        }
                    }
                });
            }

            // Phase 2: Play rhythm measures according to each note/rest duration (notify UI once per bar)
            do {
                for (int mIdx = 0; mIdx < measures.size(); mIdx++) {
                    if (!running || Thread.currentThread().isInterrupted()) break;
                    Measure measure = measures.get(mIdx);
                    final ElementPosition barPos = new ElementPosition(mIdx, 0);
                    if (listener != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (running) {
                                    listener.onBarChanged(barPos);
                                }
                            }
                        });
                    }
                    for (int eIdx = 0; eIdx < measure.elements.size(); eIdx++) {
                        if (!running || Thread.currentThread().isInterrupted()) break;
                        RhythmElement element = measure.elements.get(eIdx);
                        double durationMs = element.duration.sixteenthUnits * sixteenthMs;
                        int totalSamples = Math.max(1, (int) ((durationMs / 1000.0) * SAMPLE_RATE));
                        short[] pcm = synthesizeEventPcm(totalSamples, element.isRest, false);
                        if (!writePcmChunked(track, pcm)) break;
                    }
                }
            } while (running && loop && !Thread.currentThread().isInterrupted());

            if (running && listener != null) {
                running = false;
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onCountdownTick(null);
                        listener.onBarChanged(null);
                        listener.onPlaybackCompleted();
                    }
                });
            }
        } catch (Exception ignored) {
        } finally {
            if (track != null) {
                try {
                    track.pause();
                    track.flush();
                    track.release();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private boolean writePcmChunked(AudioTrack track, short[] pcm) {
        int chunkSize = 882; // ~20ms at 44.1kHz for instant pause/stop response
        int offset = 0;
        while (offset < pcm.length && running && !Thread.currentThread().isInterrupted()) {
            int count = Math.min(chunkSize, pcm.length - offset);
            int written = track.write(pcm, offset, count);
            if (written <= 0) return false;
            offset += written;
        }
        return running && !Thread.currentThread().isInterrupted();
    }
}
