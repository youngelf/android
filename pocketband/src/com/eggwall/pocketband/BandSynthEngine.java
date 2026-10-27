package com.eggwall.pocketband;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Real-time 44.1 kHz 16-bit mono fixed-point backing-band synthesizer for PocketBand.
 *
 * <p>Synthesizes five independent procedural instruments in real time with zero sample
 * assets or network dependencies:
 * <ol>
 *   <li><b>Kick Drum ({@code TRK_KICK}):</b> Pitch-swept sine wave (215 Hz punch transient
 *       down to 44 Hz fundamental sub) with linear amplitude decay.</li>
 *   <li><b>Snare Drum ({@code TRK_SNARE}):</b> Dual body resonators (185 Hz &amp; 330 Hz)
 *       mixed with Xorshift32 wire noise.</li>
 *   <li><b>Closed &amp; Open Hi-Hat / Ride ({@code TRK_CLHAT}, {@code TRK_OPHAT}):</b>
 *       6-oscillator TR-808 inharmonic metallic square-wave bank + high-pass filter
 *       (Closed Hat automatically chokes Open Hat).</li>
 *   <li><b>Electric Bass ({@code TRK_BASS}):</b> Plucked low-pass-filtered sawtooth wave
 *       blended with a fundamental sub-octave sine wave.</li>
 *   <li><b>Rhythm Chord Comping:</b> 3-voice warm organ/guitar stab (fundamental + 2nd
 *       harmonic) triggered on stylistic comping beats ({@link BandTheory#shouldTriggerComping}).</li>
 * </ol>
 */
public final class BandSynthEngine {

    /** Output sample rate in Hz (44.1 kHz mono PCM). */
    public static final int SAMPLE_RATE = BandTheory.SAMPLE_RATE;

    /** Frames mixed per AudioTrack write call (~5.8 ms per chunk at 44.1 kHz). */
    private static final int CHUNK_FRAMES = 256;

    /** 6 TR-808 metallic square-wave phase increments. */
    private static final int[] HAT_INCS = {
        20000000, 29600000, 36000000, 50800000, 52600000, 77900000
    };

    /** Callback notified whenever the sequencer advances to a new eighth-note step or bar. */
    public interface StepListener {
        void onStepAdvanced(int barIdx, int stepIdx);
    }

    private final short[] mSineLut = new short[256];
    private final Object mStateLock = new Object();

    private volatile boolean mRunning = false;
    private volatile boolean mPlaying = true;
    private Thread mAudioThread;
    private StepListener mStepListener;

    /* Musical & Sequencer Transport State */
    private int mStyleIdx = 0;
    private int mKeyPc = 9; // Default Key: A (9)
    private int mBpm = 110;
    private int mSwingPct = 65;
    private boolean mAutoFill = true;
    private boolean mFillQueued = false;

    /* Per-Channel Mute & Volume (0..100%) */
    private boolean mMuteDrums = false;
    private boolean mMuteBass = false;
    private boolean mMuteChords = false;
    private int mVolDrums = 90;
    private int mVolBass = 90;
    private int mVolChords = 80;

    /* Editable Pattern Grid [styleIdx][trackIdx][stepIdx] */
    private final int[][][] mPatterns =
            new int[BandTheory.STYLES.length][BandTheory.NUM_TRACKS][BandTheory.MAX_STEPS];

    /* Active Bar Chord Progression (4 to 12 bars) */
    private BandTheory.BarChord[] mActiveBars;

    /* Transport Counters */
    private volatile int mCurBar = 0;
    private volatile int mCurStep = 0;
    private int mStepSamplePos = 0;
    private int mStepSampleLen = 10000;

    /* 1. Kick Drum Voice State */
    private int mKickRem = 0;
    private int mKickTotal = 1;
    private int mKickPhase = 0;
    private int mKickAmp = 0;

    /* 2. Snare Drum Voice State */
    private int mSnareRem = 0;
    private int mSnareTotal = 1;
    private int mSnarePhase1 = 0;
    private int mSnarePhase2 = 0;
    private int mSnareAmp = 0;

    /* 3. Hi-Hat / Ride Voice State */
    private int mHatRem = 0;
    private int mHatTotal = 1;
    private int mHatAmp = 0;
    private final int[] mHatPhases = new int[6];
    private int mHatPrevIn = 0;

    /* 4. Electric Bass Voice State */
    private int mBassRem = 0;
    private int mBassTotal = 1;
    private int mBassPhase = 0;
    private int mBassInc = 0;
    private int mBassLpState = 0;
    private int mBassAmp = 0;

    /* 5. Rhythm Chord Comping Voice State (3 voices) */
    private int mChordRem = 0;
    private int mChordTotal = 1;
    private final int[] mChordPhases = new int[3];
    private final int[] mChordIncs = new int[3];
    private int mChordAmp = 0;

    private int mLfsr = 0x12345678;

    public BandSynthEngine() {
        for (int i = 0; i < 256; i++) {
            mSineLut[i] = (short) Math.round(Math.sin((2.0 * Math.PI * i) / 256.0) * 32767.0);
        }
        resetAllPatternsToDefault();
        BandTheory.BarChord[] def = BandTheory.STYLES[0].getDefaultBars();
        mActiveBars = new BandTheory.BarChord[def.length];
        System.arraycopy(def, 0, mActiveBars, 0, def.length);
        mStepSampleLen = BandTheory.computeStepSamples(mBpm, mSwingPct, 0);
    }

    public void setStepListener(StepListener listener) {
        mStepListener = listener;
    }

    public synchronized void start() {
        if (mRunning) return;
        mRunning = true;
        mAudioThread = new Thread(this::audioLoop, "PocketBand-Synth");
        mAudioThread.setPriority(Thread.MAX_PRIORITY - 1);
        mAudioThread.start();
    }

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

    public void resetAllPatternsToDefault() {
        synchronized (mStateLock) {
            for (int s = 0; s < BandTheory.STYLES.length; s++) {
                int[][] src = BandTheory.STYLES[s].defaultSteps;
                for (int t = 0; t < BandTheory.NUM_TRACKS; t++) {
                    System.arraycopy(src[t], 0, mPatterns[s][t], 0, BandTheory.MAX_STEPS);
                }
            }
        }
    }

    public void resetStylePatternToDefault(int styleIdx) {
        synchronized (mStateLock) {
            if (styleIdx < 0 || styleIdx >= BandTheory.STYLES.length) return;
            int[][] src = BandTheory.STYLES[styleIdx].defaultSteps;
            for (int t = 0; t < BandTheory.NUM_TRACKS; t++) {
                System.arraycopy(src[t], 0, mPatterns[styleIdx][t], 0, BandTheory.MAX_STEPS);
            }
        }
    }

    public void syncState(
            int styleIdx,
            int keyPc,
            int bpm,
            int swingPct,
            boolean autoFill,
            boolean muteDrums,
            boolean muteBass,
            boolean muteChords,
            int volDrums,
            int volBass,
            int volChords,
            BandTheory.BarChord[] bars) {
        synchronized (mStateLock) {
            mStyleIdx = ((styleIdx % BandTheory.STYLES.length) + BandTheory.STYLES.length)
                    % BandTheory.STYLES.length;
            mKeyPc = ((keyPc % 12) + 12) % 12;
            mBpm = Math.max(40, Math.min(240, bpm));
            mSwingPct = Math.max(50, Math.min(68, swingPct));
            mAutoFill = autoFill;
            mMuteDrums = muteDrums;
            mMuteBass = muteBass;
            mMuteChords = muteChords;
            mVolDrums = Math.max(0, Math.min(100, volDrums));
            mVolBass = Math.max(0, Math.min(100, volBass));
            mVolChords = Math.max(0, Math.min(100, volChords));
            if (bars != null && bars.length > 0) {
                mActiveBars = new BandTheory.BarChord[bars.length];
                System.arraycopy(bars, 0, mActiveBars, 0, bars.length);
            }
            mStepSampleLen = BandTheory.computeStepSamples(mBpm, mSwingPct, mCurStep);
        }
    }

    public void setPlaying(boolean playing, boolean resetPosition) {
        synchronized (mStateLock) {
            mPlaying = playing;
            if (resetPosition) {
                mCurBar = 0;
                mCurStep = 0;
                mStepSamplePos = 0;
                mStepSampleLen = BandTheory.computeStepSamples(mBpm, mSwingPct, 0);
            }
            if (!playing) {
                mKickRem = 0;
                mSnareRem = 0;
                mHatRem = 0;
                mBassRem = 0;
                mChordRem = 0;
            }
        }
    }

    public boolean isPlaying() {
        return mPlaying;
    }

    public void setPosition(int barIdx, int stepIdx) {
        synchronized (mStateLock) {
            int numBars = (mActiveBars != null && mActiveBars.length > 0) ? mActiveBars.length : 4;
            int spb = BandTheory.STYLES[mStyleIdx].stepsPerBar;
            mCurBar = ((barIdx % numBars) + numBars) % numBars;
            mCurStep = ((stepIdx % spb) + spb) % spb;
            mStepSamplePos = 0;
            mStepSampleLen = BandTheory.computeStepSamples(mBpm, mSwingPct, mCurStep);
        }
    }

    public int getCurBar() {
        return mCurBar;
    }

    public int getCurStep() {
        return mCurStep;
    }

    public void setFillQueued(boolean queued) {
        synchronized (mStateLock) {
            mFillQueued = queued;
        }
    }

    public boolean isFillQueued() {
        synchronized (mStateLock) {
            return mFillQueued;
        }
    }

    public int getStepValue(int styleIdx, int trackIdx, int stepIdx) {
        synchronized (mStateLock) {
            return mPatterns[styleIdx][trackIdx][stepIdx];
        }
    }

    public void setStepValue(int styleIdx, int trackIdx, int stepIdx, int value) {
        synchronized (mStateLock) {
            mPatterns[styleIdx][trackIdx][stepIdx] = value;
        }
    }

    /** Auditions a single chord immediately when tapped in the UI while stopped. */
    public void triggerChordPreview(int keyPc, BandTheory.BarChord bc) {
        if (bc == null) return;
        synchronized (mStateLock) {
            int[] notes = BandTheory.computeChordMidiNotes(keyPc, bc);
            for (int v = 0; v < 3; v++) {
                mChordIncs[v] = BandTheory.midiToPhaseInc(notes[v]);
            }
            mChordTotal = (SAMPLE_RATE * 320) / 1000;
            mChordRem = mChordTotal;
            mChordAmp = 4200;

            int bassMidi = BandTheory.computeBassMidi(keyPc, bc, 1);
            if (bassMidi > 0) {
                mBassTotal = (SAMPLE_RATE * 340) / 1000;
                mBassRem = mBassTotal;
                mBassInc = BandTheory.midiToPhaseInc(bassMidi);
                mBassAmp = 9600;
            }
        }
    }

    private void triggerStepVoicesLocked(int styleIdx, int barIdx, int stepIdx) {
        BandTheory.StylePreset st = BandTheory.STYLES[styleIdx];
        int numBars = (mActiveBars != null && mActiveBars.length > 0) ? mActiveBars.length : 4;
        BandTheory.BarChord bc = mActiveBars[barIdx % numBars];

        boolean isFillBar = (((barIdx + 1) % 4 == 0) && mAutoFill) || mFillQueued;
        int lastTwo = st.stepsPerBar - 2;

        int kickHit  = mPatterns[styleIdx][BandTheory.TRK_KICK][stepIdx];
        int snareHit = mPatterns[styleIdx][BandTheory.TRK_SNARE][stepIdx];
        int clhatHit = mPatterns[styleIdx][BandTheory.TRK_CLHAT][stepIdx];
        int ophatHit = mPatterns[styleIdx][BandTheory.TRK_OPHAT][stepIdx];
        int bassDeg  = mPatterns[styleIdx][BandTheory.TRK_BASS][stepIdx];

        if (isFillBar && stepIdx >= lastTwo) {
            snareHit = (stepIdx == st.stepsPerBar - 1) ? 2 : 1;
            if (stepIdx == lastTwo) {
                kickHit = 1;
            }
        }
        if (stepIdx == st.stepsPerBar - 1 && mFillQueued) {
            mFillQueued = false;
        }

        if (!mMuteDrums && mVolDrums > 0) {
            if (kickHit > 0) {
                mKickTotal = (SAMPLE_RATE * 130) / 1000;
                mKickRem   = mKickTotal;
                mKickPhase = 0;
                mKickAmp   = (((kickHit == 2) ? 15000 : 11500) * mVolDrums) / 100;
            }
            if (snareHit > 0) {
                mSnareTotal  = (SAMPLE_RATE * 145) / 1000;
                mSnareRem    = mSnareTotal;
                mSnarePhase1 = 0;
                mSnarePhase2 = 0;
                mSnareAmp    = (((snareHit == 2) ? 12500 : 9000) * mVolDrums) / 100;
            }
            if (ophatHit > 0) {
                mHatTotal = (SAMPLE_RATE * 230) / 1000;
                mHatRem   = mHatTotal;
                mHatAmp   = (((ophatHit == 2) ? 6200 : 4600) * mVolDrums) / 100;
            } else if (clhatHit > 0) {
                mHatTotal = (SAMPLE_RATE * 48) / 1000;
                mHatRem   = mHatTotal;
                mHatAmp   = (((clhatHit == 2) ? 5800 : 4000) * mVolDrums) / 100;
            }
        }

        if (!mMuteBass && mVolBass > 0 && bassDeg > 0) {
            int midiNote = BandTheory.computeBassMidi(mKeyPc, bc, bassDeg);
            if (midiNote > 0) {
                mBassTotal = (SAMPLE_RATE * 320) / 1000;
                mBassRem   = mBassTotal;
                mBassInc   = BandTheory.midiToPhaseInc(midiNote);
                mBassAmp   = (((stepIdx == 0) ? 10500 : 8800) * mVolBass) / 100;
            }
        }

        if (!mMuteChords && mVolChords > 0
                && BandTheory.shouldTriggerComping(styleIdx, st.beatsPerBar, stepIdx)) {
            int[] chordMidi = BandTheory.computeChordMidiNotes(mKeyPc, bc);
            mChordIncs[0] = BandTheory.midiToPhaseInc(chordMidi[0]);
            mChordIncs[1] = BandTheory.midiToPhaseInc(chordMidi[1]);
            mChordIncs[2] = BandTheory.midiToPhaseInc(chordMidi[2]);
            mChordTotal   = (SAMPLE_RATE * ((stepIdx == 0) ? 280 : 160)) / 1000;
            mChordRem     = mChordTotal;
            mChordAmp     = (((stepIdx == 0) ? 3800 : 3000) * mVolChords) / 100;
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
            int notifyBar = -1;
            int notifyStep = -1;

            synchronized (mStateLock) {
                for (int f = 0; f < CHUNK_FRAMES; f++) {
                    if (mPlaying) {
                        if (mStepSamplePos == 0) {
                            triggerStepVoicesLocked(mStyleIdx, mCurBar, mCurStep);
                            notifyBar = mCurBar;
                            notifyStep = mCurStep;
                        }
                        mStepSamplePos++;
                        if (mStepSamplePos >= mStepSampleLen) {
                            mStepSamplePos = 0;
                            int spb = BandTheory.STYLES[mStyleIdx].stepsPerBar;
                            int nbars = (mActiveBars != null && mActiveBars.length > 0)
                                    ? mActiveBars.length : 4;
                            int nextStep = mCurStep + 1;
                            if (nextStep >= spb) {
                                nextStep = 0;
                                mCurBar = (mCurBar + 1) % nbars;
                            }
                            mCurStep = nextStep;
                            mStepSampleLen = BandTheory.computeStepSamples(mBpm, mSwingPct, mCurStep);
                        }
                    }

                    int mix = 0;

                    /* 1. Kick Drum: Pitch-swept sine (155 Hz -> 44 Hz) */
                    if (mKickRem > 0) {
                        int elapsed = mKickTotal - mKickRem;
                        int freqHz = 44 + ((115 * mKickRem) / mKickTotal);
                        if (elapsed < (SAMPLE_RATE * 18) / 1000) {
                            freqHz += 60;
                        }
                        mKickPhase += freqHz * 97391;
                        int s = mSineLut[(mKickPhase >>> 24) & 0xFF];
                        int env = (mKickRem * mKickAmp) / mKickTotal;
                        mix += (s * env) >> 15;
                        mKickRem--;
                    }

                    /* 2. Snare Drum: Dual body tone + Xorshift32 wire noise */
                    if (mSnareRem > 0) {
                        mSnarePhase1 += 185 * 97391;
                        mSnarePhase2 += 330 * 97391;
                        int body = (mSineLut[(mSnarePhase1 >>> 24) & 0xFF]
                                  + mSineLut[(mSnarePhase2 >>> 24) & 0xFF]) >> 1;

                        mLfsr ^= mLfsr << 13;
                        mLfsr ^= mLfsr >>> 17;
                        mLfsr ^= mLfsr << 5;
                        short noise = (short) (mLfsr >> 16);

                        int env = (mSnareRem * mSnareAmp) / mSnareTotal;
                        int bodyEnv = (mSnareRem > (mSnareTotal >> 1)) ? env : (env >> 1);
                        mix += ((noise * env) >> 15) + ((body * bodyEnv) >> 16);
                        mSnareRem--;
                    }

                    /* 3. Closed / Open Hi-Hat: 6-Oscillator Metallic Bank + HPF */
                    if (mHatRem > 0) {
                        int sqSum = 0;
                        for (int o = 0; o < 6; o++) {
                            mHatPhases[o] += HAT_INCS[o];
                            sqSum += ((mHatPhases[o] & 0x80000000) != 0) ? 4096 : -4096;
                        }
                        mLfsr ^= mLfsr << 13;
                        mLfsr ^= mLfsr >>> 17;
                        mLfsr ^= mLfsr << 5;
                        int rawIn = sqSum + (((short) (mLfsr >> 16)) >> 2);
                        int hpf = rawIn - mHatPrevIn;
                        mHatPrevIn = rawIn;

                        int env = (mHatRem * mHatAmp) / mHatTotal;
                        mix += (hpf * env) >> 15;
                        mHatRem--;
                    }

                    /* 4. Electric Bass: Plucked Low-Pass Filtered Saw + Fundamental Sub */
                    if (mBassRem > 0) {
                        mBassPhase += mBassInc;
                        int saw = (short) ((mBassPhase >>> 16) - 32768);
                        int sub = mSineLut[(mBassPhase >>> 24) & 0xFF];

                        int alpha = 32 + ((mBassRem * 96) / mBassTotal);
                        mBassLpState += ((saw - mBassLpState) * alpha) >> 8;

                        int voice = (mBassLpState + sub) >> 1;
                        int env = (mBassRem * mBassAmp) / mBassTotal;
                        mix += (voice * env) >> 15;
                        mBassRem--;
                    }

                    /* 5. Rhythm Chord Comping: Warm 3-Voice Organ/Guitar Stab */
                    if (mChordRem > 0) {
                        int csum = 0;
                        for (int v = 0; v < 3; v++) {
                            mChordPhases[v] += mChordIncs[v];
                            int idx = (mChordPhases[v] >>> 24) & 0xFF;
                            int idx2 = (idx << 1) & 0xFF;
                            csum += mSineLut[idx] + (mSineLut[idx2] >> 1);
                        }
                        int env = (mChordRem * mChordAmp) / mChordTotal;
                        mix += ((csum / 3) * env) >> 15;
                        mChordRem--;
                    }

                    if (mix > 32767) mix = 32767;
                    if (mix < -32767) mix = -32767;
                    outBuf[f] = (short) mix;
                }
            }

            if (notifyStep >= 0 && mStepListener != null) {
                mStepListener.onStepAdvanced(notifyBar, notifyStep);
            }

            if (track != null) {
                track.write(outBuf, 0, CHUNK_FRAMES);
            } else {
                try {
                    Thread.sleep(6);
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
