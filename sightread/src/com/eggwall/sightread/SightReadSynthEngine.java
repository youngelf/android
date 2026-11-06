package com.eggwall.sightread;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Real-time 44.1 kHz 16-bit mono streaming PCM synthesizer for SightRead.
 *
 * <p>Runs a dedicated high-priority background audio thread ({@code SightRead-Synth})
 * that mixes warm acoustic nylon-guitar / grand-piano tones, error buzzes, and
 * woodblock metronome clicks in real time via a single streaming {@link AudioTrack}.
 */
public final class SightReadSynthEngine {

    private static final int SAMPLE_RATE = 44100;
    private static final int CHUNK_FRAMES = 256;

    private final Object mLock = new Object();
    private volatile boolean mMuted = false;
    private volatile boolean mRunning = false;
    private Thread mAudioThread;

    // Active Note Voice State
    private double mNoteFreq = 440.0;
    private int mNotePos = 0;
    private int mNoteTotalSamples = 0;
    private int mNoteSamplesLeft = 0;

    // Active Error Buzz Voice State
    private int mBuzzPos = 0;
    private int mBuzzTotalSamples = 0;
    private int mBuzzSamplesLeft = 0;

    // Active Metronome Woodblock Click Voice State
    private boolean mClickAccent = false;
    private int mClickPos = 0;
    private int mClickTotalSamples = 0;
    private int mClickSamplesLeft = 0;

    public SightReadSynthEngine() {
        startThread();
    }

    private synchronized void startThread() {
        if (mRunning) return;
        mRunning = true;
        mAudioThread = new Thread(this::audioLoop, "SightRead-Synth");
        mAudioThread.setPriority(Thread.MAX_PRIORITY - 1);
        mAudioThread.start();
    }

    public void setMuted(boolean muted) {
        mMuted = muted;
        if (muted) {
            synchronized (mLock) {
                mNoteSamplesLeft = 0;
                mBuzzSamplesLeft = 0;
                mClickSamplesLeft = 0;
            }
        }
    }

    public boolean isMuted() {
        return mMuted;
    }

    public synchronized void shutdown() {
        mRunning = false;
        synchronized (mLock) {
            mNoteSamplesLeft = 0;
            mBuzzSamplesLeft = 0;
            mClickSamplesLeft = 0;
        }
        if (mAudioThread != null) {
            try {
                mAudioThread.join(400);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            mAudioThread = null;
        }
    }

    /** Converts a MIDI note number (e.g., 60 = C4, 69 = A4) to frequency in Hz. */
    public static double midiToFreq(int midi) {
        return 440.0 * Math.pow(2.0, (midi - 69) / 12.0);
    }

    /** Plays a plucked guitar / warm piano note for {@code durationMs}. */
    public void playNote(final int midi, final int durationMs) {
        if (mMuted || !mRunning) return;
        int clampedMs = Math.max(120, Math.min(900, durationMs));
        int numSamples = (SAMPLE_RATE * clampedMs) / 1000;
        synchronized (mLock) {
            mNoteFreq = midiToFreq(midi);
            mNotePos = 0;
            mNoteTotalSamples = numSamples;
            mNoteSamplesLeft = numSamples;
        }
    }

    /** Plays a short low-pitched error buzz when an incorrect note is tapped. */
    public void playErrorBuzz() {
        if (mMuted || !mRunning) return;
        int numSamples = (SAMPLE_RATE * 140) / 1000;
        synchronized (mLock) {
            mBuzzPos = 0;
            mBuzzTotalSamples = numSamples;
            mBuzzSamplesLeft = numSamples;
        }
    }

    /** Plays a crisp woodblock metronome click (high on downbeat, normal on other beats). */
    public void playMetronomeClick(final boolean accent) {
        if (mMuted || !mRunning) return;
        int numSamples = (SAMPLE_RATE * 32) / 1000;
        synchronized (mLock) {
            mClickAccent = accent;
            mClickPos = 0;
            mClickTotalSamples = numSamples;
            mClickSamplesLeft = numSamples;
        }
    }

    @SuppressWarnings("deprecation")
    private static AudioTrack createAudioTrack(int bufSize) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            android.media.AudioAttributes attrs = new android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
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

    private void audioLoop() {
        int minBuf = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        int bufSize = Math.max(minBuf, CHUNK_FRAMES * 2 * 4);

        AudioTrack track = null;
        try {
            track = createAudioTrack(bufSize);
            if (track.getState() == AudioTrack.STATE_INITIALIZED) {
                track.play();
            } else {
                track.release();
                track = null;
            }
        } catch (Throwable ignored) {
            track = null;
        }

        final short[] outBuf = new short[CHUNK_FRAMES];
        final int attackSamples = Math.max(1, (SAMPLE_RATE * 6) / 1000);

        while (mRunning) {
            boolean anyActive = false;
            synchronized (mLock) {
                for (int f = 0; f < CHUNK_FRAMES; f++) {
                    if (mMuted) {
                        outBuf[f] = 0;
                        continue;
                    }
                    int mix = 0;

                    if (mNoteSamplesLeft > 0) {
                        anyActive = true;
                        int i = mNotePos++;
                        mNoteSamplesLeft--;
                        double t = (double) i / SAMPLE_RATE;
                        double attack = (i < attackSamples) ? ((double) i / attackSamples) : 1.0;
                        double decay = Math.exp(-t * 5.2);
                        double env = attack * decay;
                        double w = 2.0 * Math.PI * mNoteFreq * t;
                        double overtoneEnv = Math.exp(-t * 11.0);
                        double sig = 0.68 * Math.sin(w)
                                + 0.26 * Math.sin(2.0 * w) * overtoneEnv
                                + 0.11 * Math.sin(3.0 * w) * overtoneEnv
                                + 0.05 * Math.sin(4.0 * w) * overtoneEnv * overtoneEnv;
                        mix += (int) (sig * env * 21000.0);
                    }

                    if (mBuzzSamplesLeft > 0) {
                        anyActive = true;
                        int i = mBuzzPos++;
                        mBuzzSamplesLeft--;
                        double t = (double) i / SAMPLE_RATE;
                        double attack = (i < attackSamples) ? ((double) i / attackSamples) : 1.0;
                        double decay = Math.exp(-t * 12.0);
                        double env = attack * decay;
                        double w = 2.0 * Math.PI * 110.0 * t;
                        double sig = 0.60 * Math.sin(w)
                                + 0.45 * Math.sin(w * 1.414)
                                + 0.35 * Math.sin(w * 2.0);
                        mix += (int) (sig * env * 21000.0);
                    }

                    if (mClickSamplesLeft > 0) {
                        anyActive = true;
                        int i = mClickPos++;
                        mClickSamplesLeft--;
                        double t = (double) i / SAMPLE_RATE;
                        double f0 = mClickAccent ? 1320.0 : 920.0;
                        double env = Math.exp(-t * 140.0);
                        double sig = Math.sin(2.0 * Math.PI * f0 * t)
                                + 0.35 * Math.sin(2.0 * Math.PI * (f0 * 1.52) * t);
                        mix += (int) (sig * env * 12000.0);
                    }

                    if (mix > 32767) mix = 32767;
                    if (mix < -32768) mix = -32768;
                    outBuf[f] = (short) mix;
                }
            }

            if (track != null) {
                track.write(outBuf, 0, CHUNK_FRAMES);
            } else {
                try {
                    Thread.sleep(anyActive ? 6 : 15);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

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
