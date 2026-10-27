package com.eggwall.pomodoro;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Real-time 44.1 kHz 16-bit mono PCM synthesizer for the Pomodoro Focus Timer.
 *
 * <p>Generates all audio mathematically in memory with zero external sound assets:
 * <ul>
 *   <li><b>Phase-Completion Harmonic Bell Chime ({@link #triggerCompletionChime}):</b>
 *       Synthesizes a 3-note ascending singing-bowl chime ({@code C5 -> G5 -> C6}) when a
 *       Focus session completes, or a warm 2-note resolution chime ({@code G5 -> E5}) when
 *       a break finishes, using fundamental + 2nd/3rd harmonic overtones with exponential decay.</li>
 *   <li><b>Tactile UI Click ({@link #triggerUiClick}):</b>
 *       Short 8 ms damped woodblock impulse for responsive button feedback.</li>
 *   <li><b>Ambient Focus Soundscapes ({@link #setAmbientState}):</b>
 *       Optional background soundscapes during active Focus sessions:
 *       <ul>
 *         <li>{@link PomodoroModel#AMBIENT_SOFT_TICK}: Crisp mechanical pendulum tick once per second.</li>
 *         <li>{@link PomodoroModel#AMBIENT_RAIN}: Pink-filtered raindrop patter with gentle micro-droplets.</li>
 *         <li>{@link PomodoroModel#AMBIENT_BROWN_NOISE}: Deep {@code 1/f^2} leaky-integrator brownian noise.</li>
 *       </ul>
 *   </li>
 * </ul>
 */
public final class PomodoroSynthEngine {

    /** Output sample rate in Hz (44.1 kHz CD-quality mono PCM). */
    public static final int SAMPLE_RATE = 44100;
    /** Audio frames mixed per AudioTrack write call (~5.8 ms latency per chunk). */
    private static final int CHUNK_FRAMES = 256;

    private final short[] mSinLut = new short[1024];
    private final Object mLock = new Object();

    private volatile boolean mRunning = false;
    private Thread mAudioThread;

    /* Chime synthesis state */
    private int mChimeSamplesRemaining = 0;
    private int mChimeTotalSamples = 0;
    private boolean mChimeIsWorkDone = true;
    private int mChimePhase1 = 0;
    private int mChimePhase2 = 0;

    /* Tactile click / pendulum tick state */
    private int mTickSamplesRemaining = 0;
    private int mTickFreqQ10 = (800 << 10) / SAMPLE_RATE;
    private int mTickPhase = 0;

    /* Ambient soundscape state */
    private volatile int mAmbientMode = PomodoroModel.AMBIENT_SILENCE;
    private volatile boolean mAmbientActive = false;
    private int mAmbientTickCounter = 0;
    private int mRngState = 0x6C8E9CF5;
    private int mBrownIntegrator = 0;
    private int mPinkLP1 = 0;
    private int mPinkLP2 = 0;
    private int mDropletSamplesLeft = 0;
    private int mDropletPhase = 0;
    private int mDropletStepQ10 = 0;

    public PomodoroSynthEngine() {
        for (int i = 0; i < 1024; i++) {
            mSinLut[i] = (short) Math.round(Math.sin((2.0 * Math.PI * i) / 1024.0) * 16384.0);
        }
    }

    /** Starts the high-priority background PCM synthesis thread. */
    public synchronized void start() {
        if (mRunning) return;
        mRunning = true;
        mAudioThread = new Thread(this::audioLoop, "Pomodoro-SynthEngine");
        mAudioThread.setPriority(Thread.MAX_PRIORITY - 1);
        mAudioThread.start();
    }

    /** Stops the background PCM synthesis thread and releases the Android {@link AudioTrack}. */
    public synchronized void stop() {
        if (!mRunning) return;
        mRunning = false;
        if (mAudioThread != null) {
            try {
                mAudioThread.join(600);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            mAudioThread = null;
        }
    }

    /**
     * Updates the current ambient soundscape mode and whether the timer is actively running.
     */
    public void setAmbientState(int ambientMode, boolean timerRunning) {
        mAmbientMode = ambientMode;
        mAmbientActive = timerRunning && (ambientMode != PomodoroModel.AMBIENT_SILENCE);
    }

    /**
     * Triggers a brief preview burst or click when the user changes controls or soundscapes.
     */
    public void triggerUiClick() {
        synchronized (mLock) {
            mTickSamplesRemaining = (SAMPLE_RATE * 12) / 1000; // 12 ms wooden tap
            mTickFreqQ10 = (920 << 10) * 1024 / SAMPLE_RATE;
            mTickPhase = 0;
        }
    }

    /**
     * Triggers the multi-note harmonic singing-bowl chime when a Focus or Break session completes.
     *
     * @param isWorkDone {@code true} for the 3-note ascending Focus-complete chime;
     *                   {@code false} for the 2-note Break-complete chime.
     */
    public void triggerCompletionChime(boolean isWorkDone) {
        synchronized (mLock) {
            mChimeIsWorkDone = isWorkDone;
            mChimeTotalSamples = (int) (SAMPLE_RATE * 1.35); // 1.35 second resonant bell sequence
            mChimeSamplesRemaining = mChimeTotalSamples;
            mChimePhase1 = 0;
            mChimePhase2 = 0;
        }
    }

    @SuppressWarnings("deprecation")
    private void audioLoop() {
        int minBufBytes = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        int trackBufBytes = Math.max(minBufBytes, CHUNK_FRAMES * 2 * 4);

        AudioTrack track = null;
        try {
            track = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    trackBufBytes,
                    AudioTrack.MODE_STREAM);
            if (track.getState() == AudioTrack.STATE_INITIALIZED) {
                track.play();
            } else {
                track.release();
                track = null;
            }
        } catch (Throwable t) {
            track = null;
        }

        final short[] outBuf = new short[CHUNK_FRAMES];

        while (mRunning) {
            boolean anySound = false;
            final int mode = mAmbientMode;
            final boolean ambActive = mAmbientActive;

            synchronized (mLock) {
                for (int f = 0; f < CHUNK_FRAMES; f++) {
                    int mix = 0;

                    /* 1. Phase-completion harmonic bell chime */
                    if (mChimeSamplesRemaining > 0) {
                        anySound = true;
                        int elapsed = mChimeTotalSamples - mChimeSamplesRemaining;
                        int third = mChimeTotalSamples / 3;
                        int noteIdx = Math.min(2, elapsed / third);
                        int noteElapsed = elapsed - (noteIdx * third);

                        int freqHz;
                        if (mChimeIsWorkDone) {
                            /* Ascending major triad + octave: C5 (523Hz) -> G5 (784Hz) -> C6 (1046Hz) */
                            freqHz = (noteIdx == 0) ? 523 : (noteIdx == 1) ? 784 : 1046;
                        } else {
                            /* Warm break-over chime: G5 (784Hz) -> E5 (659Hz) -> C6 (1046Hz) */
                            freqHz = (noteIdx == 0) ? 784 : (noteIdx == 1) ? 659 : 1046;
                        }

                        int step1 = (freqHz * 1024) / SAMPLE_RATE;
                        int step2 = (freqHz * 2 * 1024) / SAMPLE_RATE;
                        mChimePhase1 = (mChimePhase1 + step1) & 1023;
                        mChimePhase2 = (mChimePhase2 + step2) & 1023;

                        /* Exponential-like linear decay envelope per note */
                        int env = Math.max(0, third - noteElapsed);
                        int envQ12 = (env << 12) / Math.max(1, third);

                        int fundamental = mSinLut[mChimePhase1];
                        int overtone = mSinLut[mChimePhase2] >> 2;
                        int bellSample = ((fundamental + overtone) * envQ12) >> 13;
                        mix += bellSample;
                        mChimeSamplesRemaining--;
                    }

                    /* 2. Tactile UI click or pendulum tick impulse */
                    if (mTickSamplesRemaining > 0) {
                        anySound = true;
                        mTickPhase += mTickFreqQ10;
                        int lutIdx = (mTickPhase >> 10) & 1023;
                        int env = (mTickSamplesRemaining * 2800) / ((SAMPLE_RATE * 12) / 1000);
                        mix += (mSinLut[lutIdx] * env) >> 14;
                        mTickSamplesRemaining--;
                    }

                    /* 3. Optional Ambient Focus Soundscape */
                    if (ambActive) {
                        anySound = true;
                        mRngState = mRngState * 1664525 + 1013904223;
                        int white = (mRngState >> 16); // -32768 .. 32767

                        if (mode == PomodoroModel.AMBIENT_SOFT_TICK) {
                            mAmbientTickCounter++;
                            if (mAmbientTickCounter >= SAMPLE_RATE) {
                                mAmbientTickCounter = 0;
                                mTickSamplesRemaining = (SAMPLE_RATE * 10) / 1000;
                                mTickFreqQ10 = (680 << 10) * 1024 / SAMPLE_RATE;
                                mTickPhase = 0;
                            }
                        } else if (mode == PomodoroModel.AMBIENT_RAIN) {
                            /* Two-stage low-pass / bandpass filtered rain wash + random micro-droplets */
                            mPinkLP1 += (white - mPinkLP1) >> 3;
                            mPinkLP2 += (mPinkLP1 - mPinkLP2) >> 4;
                            int rainWash = (mPinkLP1 - mPinkLP2) >> 3;

                            if (mDropletSamplesLeft <= 0 && ((mRngState & 0x1FFF) == 0)) {
                                mDropletSamplesLeft = 180;
                                int dropFreq = 1100 + ((mRngState >>> 13) & 0x3FF);
                                mDropletStepQ10 = (dropFreq << 10) * 1024 / SAMPLE_RATE;
                                mDropletPhase = 0;
                            }
                            int drop = 0;
                            if (mDropletSamplesLeft > 0) {
                                mDropletPhase += mDropletStepQ10;
                                int idx = (mDropletPhase >> 10) & 1023;
                                drop = (mSinLut[idx] * mDropletSamplesLeft) >> 12;
                                mDropletSamplesLeft--;
                            }
                            mix += rainWash + drop;
                        } else if (mode == PomodoroModel.AMBIENT_BROWN_NOISE) {
                            /* Leaky integrator 1/f^2 brown noise for deep distraction masking */
                            mBrownIntegrator = ((mBrownIntegrator * 251) >> 8) + (white >> 5);
                            if (mBrownIntegrator > 16000) mBrownIntegrator = 16000;
                            if (mBrownIntegrator < -16000) mBrownIntegrator = -16000;
                            mix += mBrownIntegrator >> 2;
                        }
                    }

                    if (mix > 32767) mix = 32767;
                    if (mix < -32767) mix = -32767;
                    outBuf[f] = (short) mix;
                }
            }

            if (track != null) {
                track.write(outBuf, 0, CHUNK_FRAMES);
            } else {
                try {
                    Thread.sleep(anySound ? 6 : 15);
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
