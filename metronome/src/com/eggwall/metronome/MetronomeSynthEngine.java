package com.eggwall.metronome;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.SystemClock;

/**
 * Sample-accurate 44.1 kHz 16-bit mono PCM metronome audio clock and percussion
 * synthesizer.
 *
 * <p>Unlike UI-timer metronomes that suffer from 10–25 ms main-thread scheduling
 * jitter, {@code MetronomeSynthEngine} counts exact audio sample frames at 44,100 Hz
 * inside a dedicated high-priority audio thread ({@code Thread.MAX_PRIORITY - 1}),
 * achieving sub-millisecond ({@code < 0.03 ms}) beat-to-beat accuracy.
 *
 * <p><b>Engineering Customization Guide:</b>
 * <ul>
 *   <li><b>Click Pitch & Harmonics:</b> See {@link #synthesizeSample} to customize the
 *       fundamental frequencies, pitch-drop transients, and harmonic overtones for each
 *       of the 4 sound kits ({@code V90 Classic}, {@code Woodblock}, {@code Studio Rim},
 *       {@code Soft Sine}).</li>
 *   <li><b>Click Envelope Duration:</b> Adjust {@link #ACCENT_CLICK_MS},
 *       {@link #NORMAL_CLICK_MS}, and {@link #SUBDIV_CLICK_MS}.</li>
 *   <li><b>Audio Buffer Latency:</b> Adjust {@link #CHUNK_FRAMES} (default {@code 256}
 *       frames = {@code 5.8 ms} at 44.1 kHz).</li>
 * </ul>
 */
public final class MetronomeSynthEngine {

    /** Output sample rate in Hz (44.1 kHz CD-quality mono PCM). */
    public static final int SAMPLE_RATE = 44100;
    /** Audio frames mixed per AudioTrack write call (~5.8 ms per chunk at 44.1 kHz). */
    private static final int CHUNK_FRAMES = 256;

    /** Duration of a primary downbeat accent click in milliseconds (matches V90 C source). */
    private static final int ACCENT_CLICK_MS = 75;
    /** Duration of a normal or sub-accent beat click in milliseconds (matches V90 C source). */
    private static final int NORMAL_CLICK_MS = 55;
    /** Duration of an off-beat subdivision tick in milliseconds. */
    private static final int SUBDIV_CLICK_MS = 36;

    private final Object mStateLock = new Object();

    private volatile boolean mRunning = false;
    private volatile boolean mPlaying = false;
    private Thread mAudioThread;

    /* Musical Configuration */
    private volatile int mBaseBpm = MetronomeTheory.DEFAULT_BPM;
    private volatile int mEffectiveBpm = MetronomeTheory.DEFAULT_BPM;
    private volatile int mTimeSigIdx = 0;
    private volatile int mSubdivIdx = 0;
    private volatile int mSoundKitIdx = 0;
    private volatile int mDropPct = 0;
    private volatile int mTrainerIdx = 0;
    private volatile int mTrainerTargetBpm = 200;

    private final int[] mBeatStates = new int[16];
    private int mBeatsPerBar = 4;

    /* Live Playback Telemetry (Read by MetronomeView at 60 FPS) */
    private volatile int mCurrentBeat = 0;
    private volatile int mCurrentSubdiv = 0;
    private volatile int mMeasureCount = 0;
    private volatile double mBeatPhase0to1 = 0.0;
    private volatile boolean mLastBeatDropped = false;
    private volatile long mLastBeatUptimeMs = 0L;

    /* Sample-Accurate Clock & Active Voice State */
    private double mSampleInBeat = 0.0;
    private int mNextSubdivIndex = 1;
    private int mClickSamplesLeft = 0;
    private int mClickTotalSamples = 1;
    private int mClickSamplePos = 0;
    private int mClickLevel = MetronomeTheory.BEAT_NORMAL;
    private int mRngState = 0x6C8E9CF5;

    public MetronomeSynthEngine() {
        applyTimeSignatureDefaultsLocked(0);
    }

    public synchronized void start() {
        if (mRunning) return;
        mRunning = true;
        mAudioThread = new Thread(this::audioLoop, "Metronome-SampleClock");
        mAudioThread.setPriority(Thread.MAX_PRIORITY - 1);
        mAudioThread.start();
    }

    public synchronized void stop() {
        if (!mRunning) return;
        mRunning = false;
        mPlaying = false;
        if (mAudioThread != null) {
            try {
                mAudioThread.join(600);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            mAudioThread = null;
        }
    }

    /** Starts or pauses sample-accurate metronome playback. */
    public void setPlaying(boolean playing) {
        synchronized (mStateLock) {
            if (mPlaying == playing) return;
            mPlaying = playing;
            if (playing) {
                mCurrentBeat = 0;
                mCurrentSubdiv = 0;
                mMeasureCount = 0;
                mSampleInBeat = 0.0;
                mNextSubdivIndex = 1;
                mBeatPhase0to1 = 0.0;
                updateEffectiveBpmLocked();
                mLastBeatUptimeMs = SystemClock.uptimeMillis();
                triggerBeatClickLocked(mBeatStates[0]);
            } else {
                mBeatPhase0to1 = 0.0;
                mLastBeatDropped = false;
            }
        }
    }

    public boolean isPlaying() {
        return mPlaying;
    }

    public void setBpm(int bpm) {
        synchronized (mStateLock) {
            mBaseBpm = MetronomeTheory.clampBpm(bpm);
            updateEffectiveBpmLocked();
        }
    }

    public int getBaseBpm() {
        return mBaseBpm;
    }

    public int getEffectiveBpm() {
        return mEffectiveBpm;
    }

    public void setTimeSignatureIndex(int idx, boolean resetBeatStates) {
        synchronized (mStateLock) {
            int n = MetronomeTheory.TIME_SIGNATURES.length;
            mTimeSigIdx = ((idx % n) + n) % n;
            if (resetBeatStates) {
                applyTimeSignatureDefaultsLocked(mTimeSigIdx);
            } else {
                mBeatsPerBar = MetronomeTheory.TIME_SIGNATURES[mTimeSigIdx].numerator;
            }
            mCurrentBeat = 0;
            mCurrentSubdiv = 0;
            mMeasureCount = 0;
            mSampleInBeat = 0.0;
            mNextSubdivIndex = 1;
            updateEffectiveBpmLocked();
            if (mPlaying) {
                mLastBeatUptimeMs = SystemClock.uptimeMillis();
                triggerBeatClickLocked(mBeatStates[0]);
            }
        }
    }

    public int getTimeSignatureIndex() {
        return mTimeSigIdx;
    }

    public int getBeatsPerBar() {
        synchronized (mStateLock) {
            return mBeatsPerBar;
        }
    }

    public int getBeatState(int beatIdx) {
        synchronized (mStateLock) {
            if (beatIdx < 0 || beatIdx >= mBeatsPerBar) return MetronomeTheory.BEAT_NORMAL;
            return mBeatStates[beatIdx];
        }
    }

    public void setBeatState(int beatIdx, int state) {
        synchronized (mStateLock) {
            if (beatIdx >= 0 && beatIdx < mBeatStates.length) {
                mBeatStates[beatIdx] = state;
            }
        }
    }

    public void cycleBeatState(int beatIdx) {
        synchronized (mStateLock) {
            if (beatIdx >= 0 && beatIdx < mBeatsPerBar) {
                int next = MetronomeTheory.cycleBeatState(mBeatStates[beatIdx]);
                mBeatStates[beatIdx] = next;
                if (!mPlaying && next != MetronomeTheory.BEAT_MUTE) {
                    armClickVoiceLocked(next);
                }
            }
        }
    }

    public void setSubdivisionIndex(int idx) {
        synchronized (mStateLock) {
            int n = MetronomeTheory.SUBDIVISIONS.length;
            mSubdivIdx = ((idx % n) + n) % n;
            mNextSubdivIndex = 1;
        }
    }

    public int getSubdivisionIndex() {
        return mSubdivIdx;
    }

    public void setSoundKitIndex(int idx) {
        synchronized (mStateLock) {
            int n = MetronomeTheory.SOUND_KIT_NAMES.length;
            mSoundKitIdx = ((idx % n) + n) % n;
            if (!mPlaying) {
                armClickVoiceLocked(MetronomeTheory.BEAT_ACCENT);
            }
        }
    }

    public int getSoundKitIndex() {
        return mSoundKitIdx;
    }

    public void setDropPct(int dropPct) {
        synchronized (mStateLock) {
            mDropPct = MetronomeTheory.clampDropPct(dropPct);
        }
    }

    public int getDropPct() {
        return mDropPct;
    }

    public void setTrainerIndex(int idx) {
        synchronized (mStateLock) {
            int n = MetronomeTheory.TRAINER_STEP_BPM.length;
            mTrainerIdx = ((idx % n) + n) % n;
            mMeasureCount = 0;
            updateEffectiveBpmLocked();
        }
    }

    public int getTrainerIndex() {
        return mTrainerIdx;
    }

    /** Triggers a single preview click immediately (e.g. when tapping Tap Tempo or testing sound). */
    public void triggerPreviewClick(int level) {
        synchronized (mStateLock) {
            if (!mPlaying) {
                armClickVoiceLocked(level);
            }
        }
    }

    /** Configures deterministic playback state for automated demo/screenshot verification. */
    public void configureDemoTelemetry(int beatIdx, int measureCount, double phase0to1, boolean dropped) {
        synchronized (mStateLock) {
            mCurrentBeat = Math.max(0, Math.min(mBeatsPerBar - 1, beatIdx));
            mMeasureCount = Math.max(0, measureCount);
            mBeatPhase0to1 = Math.max(0.0, Math.min(1.0, phase0to1));
            mLastBeatDropped = dropped;
            mLastBeatUptimeMs = SystemClock.uptimeMillis();
            updateEffectiveBpmLocked();
        }
    }

    public int getCurrentBeat() {
        return mCurrentBeat;
    }

    public int getCurrentSubdiv() {
        return mCurrentSubdiv;
    }

    public int getMeasureCount() {
        return mMeasureCount;
    }

    public double getBeatPhase0to1() {
        return mBeatPhase0to1;
    }

    public boolean isLastBeatDropped() {
        return mLastBeatDropped;
    }

    public long getLastBeatUptimeMs() {
        return mLastBeatUptimeMs;
    }

    private void applyTimeSignatureDefaultsLocked(int timeSigIdx) {
        MetronomeTheory.TimeSignature ts = MetronomeTheory.TIME_SIGNATURES[timeSigIdx];
        mBeatsPerBar = ts.numerator;
        for (int i = 0; i < mBeatStates.length; i++) {
            if (i < ts.defaultBeatStates.length) {
                mBeatStates[i] = ts.defaultBeatStates[i];
            } else {
                mBeatStates[i] = MetronomeTheory.BEAT_NORMAL;
            }
        }
    }

    private void updateEffectiveBpmLocked() {
        int step = MetronomeTheory.TRAINER_STEP_BPM[mTrainerIdx];
        int cap = Math.max(mBaseBpm, mTrainerTargetBpm);
        mEffectiveBpm = MetronomeTheory.computeTrainerBpm(
                mBaseBpm, mMeasureCount, step, MetronomeTheory.TRAINER_MEASURES_PER_STEP, cap);
    }

    private void triggerBeatClickLocked(int beatState) {
        mRngState = mRngState * 1664525 + 1013904223;
        int rand0to99 = ((mRngState >>> 16) & 0x7FFF) % 100;
        boolean play = MetronomeTheory.shouldPlayClick(beatState, mMeasureCount, mDropPct, rand0to99);
        mLastBeatDropped = (beatState != MetronomeTheory.BEAT_MUTE) && !play;
        if (play) {
            armClickVoiceLocked(beatState);
        }
    }

    private void armClickVoiceLocked(int level) {
        if (level == MetronomeTheory.BEAT_MUTE) return;
        mClickLevel = level;
        mClickSamplePos = 0;
        int durationMs;
        if (level == MetronomeTheory.BEAT_ACCENT) {
            durationMs = ACCENT_CLICK_MS;
        } else if (level == MetronomeTheory.BEAT_SUBDIVISION) {
            durationMs = SUBDIV_CLICK_MS;
        } else {
            durationMs = NORMAL_CLICK_MS;
        }
        mClickTotalSamples = (SAMPLE_RATE * durationMs) / 1000;
        mClickSamplesLeft = mClickTotalSamples;
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
            boolean activeSound = false;
            synchronized (mStateLock) {
                double samplesPerBeat = MetronomeTheory.getSamplesPerBeat(mEffectiveBpm, SAMPLE_RATE);
                double[] subdivOffsets = MetronomeTheory.SUBDIVISIONS[mSubdivIdx].pulseOffsets;
                int soundKit = mSoundKitIdx;

                for (int f = 0; f < CHUNK_FRAMES; f++) {
                    if (mPlaying) {
                        mSampleInBeat += 1.0;

                        /* Check intra-beat subdivisions (Eighths, Triplets, Sixteenths, Swing) */
                        if (mNextSubdivIndex < subdivOffsets.length) {
                            double subdivSampleTarget = subdivOffsets[mNextSubdivIndex] * samplesPerBeat;
                            if (mSampleInBeat >= subdivSampleTarget) {
                                mCurrentSubdiv = mNextSubdivIndex;
                                mNextSubdivIndex++;
                                if (!mLastBeatDropped && mBeatStates[mCurrentBeat] != MetronomeTheory.BEAT_MUTE) {
                                    armClickVoiceLocked(MetronomeTheory.BEAT_SUBDIVISION);
                                }
                            }
                        }

                        /* Check main beat boundary */
                        if (mSampleInBeat >= samplesPerBeat) {
                            mSampleInBeat -= samplesPerBeat;
                            mCurrentSubdiv = 0;
                            mNextSubdivIndex = 1;
                            mCurrentBeat = (mCurrentBeat + 1) % mBeatsPerBar;
                            if (mCurrentBeat == 0) {
                                mMeasureCount++;
                                updateEffectiveBpmLocked();
                                samplesPerBeat = MetronomeTheory.getSamplesPerBeat(mEffectiveBpm, SAMPLE_RATE);
                            }
                            mLastBeatUptimeMs = SystemClock.uptimeMillis();
                            triggerBeatClickLocked(mBeatStates[mCurrentBeat]);
                        }
                    }

                    if (mClickSamplesLeft > 0) {
                        activeSound = true;
                        int pos = mClickSamplePos++;
                        mClickSamplesLeft--;
                        outBuf[f] = synthesizeSample(soundKit, mClickLevel, pos, mClickTotalSamples);
                    } else {
                        outBuf[f] = 0;
                    }
                }

                if (mPlaying && samplesPerBeat > 0.0) {
                    double p = mSampleInBeat / samplesPerBeat;
                    if (p < 0.0) p = 0.0;
                    if (p > 1.0) p = 1.0;
                    mBeatPhase0to1 = p;
                }
            }

            if (track != null) {
                track.write(outBuf, 0, CHUNK_FRAMES);
            } else {
                try {
                    Thread.sleep((mPlaying || activeSound) ? 5 : 15);
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

    /**
     * Synthesizes a single 16-bit PCM sample for the active click voice.
     *
     * <ul>
     *   <li><b>Kit 0 (V90 Classic):</b> Exact port of PowKiddy V90 {@code metronome.c}
     *       {@code audio_callback()} — 1760 Hz (A6) accent / 1000 Hz normal with 5 ms
     *       1.35x pitch-drop transient and 3.5x saturated square-harmonic overdrive.</li>
     *   <li><b>Kit 1 (Woodblock):</b> Warm hollow dual-mode resonant woodblock (920 Hz /
     *       640 Hz) with exponential decay and crisp mallet strike transient.</li>
     *   <li><b>Kit 2 (Studio Rim):</b> Punchy studio rimshot + high-hat transient blend
     *       with body resonance and crisp metallic edge.</li>
     *   <li><b>Kit 3 (Soft Sine):</b> Pure harmonic sine chime (1320 Hz / 880 Hz / 660 Hz)
     *       with smooth cosine-tapered envelope for fatigue-free headphone sessions.</li>
     * </ul>
     */
    private short synthesizeSample(int kitIdx, int level, int pos, int totalLen) {
        double progress = (double) pos / (double) Math.max(1, totalLen);
        boolean isAccent = (level == MetronomeTheory.BEAT_ACCENT);
        boolean isSubAccent = (level == MetronomeTheory.BEAT_SUB_ACCENT);
        boolean isSubdiv = (level == MetronomeTheory.BEAT_SUBDIVISION);

        double levelGain = isAccent ? 1.0 : (isSubAccent ? 0.82 : (isSubdiv ? 0.45 : 0.70));

        if (kitIdx == 0) {
            /* Kit 0: PowKiddy V90 Classic Saturated Harmonic Click */
            double sustainFrac = isAccent ? 0.70 : 0.65;
            double env = 1.0;
            if (progress > sustainFrac) {
                env = (1.0 - progress) / (1.0 - sustainFrac);
            }
            double freq = isAccent ? 1760.0 : (isSubAccent ? 1320.0 : (isSubdiv ? 800.0 : 1000.0));
            if (pos < (SAMPLE_RATE * 5) / 1000) {
                freq *= 1.35;
            }
            double phase = (2.0 * Math.PI * freq * (double) pos) / (double) SAMPLE_RATE;
            double raw = Math.sin(phase) + 0.65 * Math.sin(phase * 2.0) + 0.45 * Math.sin(phase * 3.0);
            if (isAccent) {
                raw += 0.50 * Math.sin(phase * 0.5);
            }
            double driven = raw * 3.5;
            if (driven > 1.0) driven = 1.0;
            if (driven < -1.0) driven = -1.0;
            int val = (int) (driven * 32767.0 * env * levelGain);
            return clampPcm16(val);
        } else if (kitIdx == 1) {
            /* Kit 1: Acoustic Hollow Woodblock */
            double env = Math.exp(-5.2 * progress);
            double baseFreq = isAccent ? 980.0 : (isSubAccent ? 780.0 : (isSubdiv ? 540.0 : 660.0));
            double pitchDrop = 1.0 + 0.45 * Math.exp(-32.0 * progress);
            double phase = (2.0 * Math.PI * baseFreq * pitchDrop * (double) pos) / (double) SAMPLE_RATE;
            double body = Math.sin(phase) + 0.42 * Math.sin(phase * 2.41) * Math.exp(-9.0 * progress);
            int val = (int) (body * 26000.0 * env * levelGain);
            return clampPcm16(val);
        } else if (kitIdx == 2) {
            /* Kit 2: Studio Rimshot & Crisp Hat */
            double env = Math.exp(-6.5 * progress);
            double toneFreq = isAccent ? 1480.0 : (isSubAccent ? 1120.0 : (isSubdiv ? 2400.0 : 900.0));
            double phase = (2.0 * Math.PI * toneFreq * (double) pos) / (double) SAMPLE_RATE;
            int hash = (pos * 1103515245 + 12345);
            double noise = ((double) ((hash >> 16) & 0x7FFF) / 16384.0) - 1.0;
            double snapEnv = Math.exp(-22.0 * progress);
            double mix = 0.72 * Math.sin(phase) + 0.55 * noise * snapEnv;
            int val = (int) (mix * 28000.0 * env * levelGain);
            return clampPcm16(val);
        } else {
            /* Kit 3: Soft Pure Sine Bell */
            double attack = Math.min(1.0, (double) pos / ((SAMPLE_RATE * 3.0) / 1000.0));
            double decay = 0.5 * (1.0 + Math.cos(Math.PI * progress));
            double freq = isAccent ? 1320.0 : (isSubAccent ? 1046.5 : (isSubdiv ? 659.25 : 880.0));
            double phase = (2.0 * Math.PI * freq * (double) pos) / (double) SAMPLE_RATE;
            double pure = Math.sin(phase) + 0.20 * Math.sin(phase * 2.0);
            int val = (int) (pure * 24000.0 * attack * decay * levelGain);
            return clampPcm16(val);
        }
    }

    private static short clampPcm16(int sample) {
        if (sample > 32767) return 32767;
        if (sample < -32768) return -32768;
        return (short) sample;
    }
}
