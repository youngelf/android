package com.eggwall.sightread;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Real-time 44.1 kHz 16-bit mono PCM synthesizer for SightRead.
 *
 * <p>Generates warm acoustic nylon-guitar / grand-piano tones with harmonic overtones,
 * exponential pluck/hammer envelope, and wood-block metronome clicks via {@link AudioTrack}.
 */
public final class SightReadSynthEngine {

    private static final int SAMPLE_RATE = 44100;
    private final ExecutorService mAudioExec = Executors.newSingleThreadExecutor();
    private volatile boolean mMuted = false;
    private volatile boolean mShutdown = false;

    public void setMuted(boolean muted) {
        mMuted = muted;
    }

    public boolean isMuted() {
        return mMuted;
    }

    public void shutdown() {
        mShutdown = true;
        mAudioExec.shutdownNow();
    }

    /** Converts a MIDI note number (e.g., 60 = C4, 69 = A4) to frequency in Hz. */
    public static double midiToFreq(int midi) {
        return 440.0 * Math.pow(2.0, (midi - 69) / 12.0);
    }

    /** Plays a plucked guitar / warm piano note for {@code durationMs}. */
    public void playNote(final int midi, final int durationMs) {
        if (mMuted || mShutdown) return;
        final double freq = midiToFreq(midi);
        mAudioExec.submit(new Runnable() {
            @Override
            public void run() {
                if (mMuted || mShutdown) return;
                playToneInternal(freq, Math.max(120, Math.min(900, durationMs)), false);
            }
        });
    }

    /** Plays a short low-pitched error buzz when an incorrect note is tapped. */
    public void playErrorBuzz() {
        if (mMuted || mShutdown) return;
        mAudioExec.submit(new Runnable() {
            @Override
            public void run() {
                if (mMuted || mShutdown) return;
                playToneInternal(110.0, 140, true);
            }
        });
    }

    /** Plays a crisp woodblock metronome click (high on downbeat, normal on other beats). */
    public void playMetronomeClick(final boolean accent) {
        if (mMuted || mShutdown) return;
        mAudioExec.submit(new Runnable() {
            @Override
            public void run() {
                if (mMuted || mShutdown) return;
                int numSamples = (SAMPLE_RATE * 32) / 1000;
                short[] pcm = new short[numSamples];
                double f0 = accent ? 1320.0 : 920.0;
                for (int i = 0; i < numSamples; i++) {
                    double t = (double) i / SAMPLE_RATE;
                    double env = Math.exp(-t * 140.0);
                    double sig = Math.sin(2.0 * Math.PI * f0 * t)
                               + 0.35 * Math.sin(2.0 * Math.PI * (f0 * 1.52) * t);
                    int v = (int) (sig * env * 12000.0);
                    if (v > 32767) v = 32767;
                    if (v < -32768) v = -32768;
                    pcm[i] = (short) v;
                }
                writePcm(pcm);
            }
        });
    }

    private void playToneInternal(double freq, int durationMs, boolean harsh) {
        int numSamples = (SAMPLE_RATE * durationMs) / 1000;
        short[] pcm = new short[numSamples];
        int attackSamples = Math.max(1, (SAMPLE_RATE * 6) / 1000);
        for (int i = 0; i < numSamples; i++) {
            double t = (double) i / SAMPLE_RATE;
            double attack = (i < attackSamples) ? ((double) i / attackSamples) : 1.0;
            double decay = Math.exp(-t * (harsh ? 12.0 : 5.2));
            double env = attack * decay;

            double w = 2.0 * Math.PI * freq * t;
            double sig;
            if (harsh) {
                sig = 0.60 * Math.sin(w)
                    + 0.45 * Math.sin(w * 1.414)
                    + 0.35 * Math.sin(w * 2.0);
            } else {
                // Warm acoustic string / piano harmonic profile
                double overtoneEnv = Math.exp(-t * 11.0);
                sig = 0.68 * Math.sin(w)
                    + 0.26 * Math.sin(2.0 * w) * overtoneEnv
                    + 0.11 * Math.sin(3.0 * w) * overtoneEnv
                    + 0.05 * Math.sin(4.0 * w) * overtoneEnv * overtoneEnv;
            }
            int sample = (int) (sig * env * 21000.0);
            if (sample > 32767) sample = 32767;
            if (sample < -32768) sample = -32768;
            pcm[i] = (short) sample;
        }
        writePcm(pcm);
    }

    private void writePcm(short[] pcm) {
        AudioTrack track = null;
        try {
            int byteCount = pcm.length * 2;
            int minBuf = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            int bufSize = Math.max(minBuf, byteCount);
            track = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufSize,
                    AudioTrack.MODE_STATIC);
            track.write(pcm, 0, pcm.length);
            track.play();
            long sleepMs = (pcm.length * 1000L) / SAMPLE_RATE + 15L;
            Thread.sleep(sleepMs);
        } catch (Throwable ignored) {
            // Audio device unavailable on headless CI or interrupted
        } finally {
            if (track != null) {
                try {
                    track.stop();
                } catch (Throwable ignored) {}
                try {
                    track.release();
                } catch (Throwable ignored) {}
            }
        }
    }
}
