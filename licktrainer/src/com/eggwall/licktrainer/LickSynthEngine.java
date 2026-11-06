package com.eggwall.licktrainer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Real-time 44.1 kHz 16-bit mono Karplus-Strong physical-modeling guitar lead,
 * 4-voice backing chord vamp, and precision woodblock metronome synthesizer.
 *
 * <p><b>Engineering Customization Guide:</b>
 * <ul>
 *   <li><b>Lead Guitar Sustain &amp; Articulation:</b> In {@link #exciteVoiceLocked},
 *       adjust {@code dampingQ15} ({@code 32600} for legato hammer/pull/slide vs.
 *       {@code 32550} for picked notes) to lengthen or shorten note sustain.</li>
 *   <li><b>Backing Chord Warmth:</b> In {@link #triggerBackingChord}, adjust the
 *       backing voice amplitude ({@code 9500}) or 16 ms strum stagger.</li>
 *   <li><b>Metronome Click Pitch:</b> In {@link #initClickWaveforms()}, change
 *       {@code 1760.0} Hz (downbeat accent) and {@code 1000.0} Hz (subsequent beats).</li>
 * </ul>
 */
public final class LickSynthEngine {

    /** Output sample rate in Hz (44.1 kHz CD-quality mono PCM). */
    public static final int SAMPLE_RATE = 44100;
    /** Maximum Karplus-Strong delay-line length in samples. */
    private static final int KS_MAX_DELAY = 1200;
    /** Audio frames mixed per AudioTrack write call (~5.8 ms latency chunk). */
    private static final int CHUNK_FRAMES = 256;
    /** Metronome click waveform length in samples (~65 ms at 44.1 kHz). */
    private static final int CLICK_SAMPLES = 2866;

    /** Total polyphonic Karplus-Strong string voices (2 lead + 4 backing + 6 manual pluck). */
    private static final int TOTAL_VOICES = 12;
    private static final int VOICE_LEAD_PRIMARY = 0;
    private static final int VOICE_LEAD_SECONDARY = 1;
    private static final int VOICE_BACKING_BASE = 2; // voices 2..5
    private static final int VOICE_PLUCK_BASE = 6;   // voices 6..11 (strings 0..5)

    /** Callback invoked on the audio thread only when a bar/loop wraps around. */
    public interface BarListener {
        void onBarAdvanced(int barsPlayed, int newBpm);
    }

    private static final class KsVoice {
        final short[] buf = new short[KS_MAX_DELAY];
        int length = 32;
        int pos = 0;
        int startDelaySamples = 0;
        int activeSamplesLeft = 0;
        int dampingQ15 = 32560;
        boolean active = false;

        boolean needsExcite = false;
        int pendingMidi = 60;
        int pendingAmp = 25000;
        int pendingDampingQ15 = 32560;
        boolean pendingWarmLegato = false;
        int pendingStartDelaySamples = 0;
        int pendingDurationSamples = 0;
    }

    private final KsVoice[] mVoices = new KsVoice[TOTAL_VOICES];
    private final Object mLock = new Object();

    private final short[] mClickAccentPcm = new short[CLICK_SAMPLES];
    private final short[] mClickNormalPcm = new short[CLICK_SAMPLES];
    private final short[] mSinLut = new short[1024];

    private int mActiveClickType = 0; // 0 = none, 1 = normal, 2 = accent
    private int mActiveClickPos = 0;
    private int mLeadFlip = 0;

    /* Background Sequencer State */
    private boolean mPlaying = false;
    private LickTheory.LickDef mLick = LickTheory.LICKS[0];
    private int mKeyIdx = 5;
    private int mBpm = 100;
    private int mBaseBpm = 100;
    private int mLadderIdx = 0;
    private int mMixMode = LickTheory.MIX_TAB_BACK_CLICK;
    private boolean mLoopSubRange = false;
    private int mLoopStartStep = 0;
    private int mLoopEndStep = 3;
    private int mCurStep = 0;
    private int mBarsPlayed = 0;
    private int mStepSamplePos = 0;
    private int mStepSampleLen = 11025;
    private BarListener mBarListener;

    private final AtomicInteger mReqGen = new AtomicInteger(0);
    private volatile boolean mRunning = false;
    private Thread mAudioThread;
    private int mRngState = 0xA53C9E17;

    public LickSynthEngine() {
        for (int i = 0; i < TOTAL_VOICES; i++) {
            mVoices[i] = new KsVoice();
        }
        for (int i = 0; i < 1024; i++) {
            mSinLut[i] = (short) (Math.sin((2.0 * Math.PI * (double) i) / 1024.0) * 16384.0);
        }
        initClickWaveforms();
    }

    private void initClickWaveforms() {
        for (int i = 0; i < CLICK_SAMPLES; i++) {
            double progress = (double) i / (double) CLICK_SAMPLES;
            double env = (progress < 0.65) ? 1.0 : ((1.0 - progress) / 0.35);
            double freqAcc = (i < 220) ? (1760.0 * 1.30) : 1760.0;
            double freqNrm = (i < 220) ? (1000.0 * 1.30) : 1000.0;

            double phA = (2.0 * Math.PI * freqAcc * i) / SAMPLE_RATE;
            double phN = (2.0 * Math.PI * freqNrm * i) / SAMPLE_RATE;

            double rawA = (Math.sin(phA) + 0.55 * Math.sin(phA * 2.0)) * 2.4 * env;
            double rawN = (Math.sin(phN) + 0.50 * Math.sin(phN * 2.0)) * 2.2 * env;
            if (rawA > 1.0) rawA = 1.0;
            else if (rawA < -1.0) rawA = -1.0;
            if (rawN > 1.0) rawN = 1.0;
            else if (rawN < -1.0) rawN = -1.0;

            mClickAccentPcm[i] = (short) (rawA * 19500.0);
            mClickNormalPcm[i] = (short) (rawN * 15500.0);
        }
    }

    public synchronized void start() {
        if (mRunning) return;
        mRunning = true;
        mAudioThread = new Thread(this::audioLoop, "LickTrainer-KarplusStrong");
        mAudioThread.setPriority(Thread.MAX_PRIORITY - 1);
        mAudioThread.start();
    }

    public synchronized void stop() {
        if (!mRunning) return;
        mRunning = false;
        mReqGen.incrementAndGet();
        if (mAudioThread != null) {
            try {
                mAudioThread.join(600);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            mAudioThread = null;
        }
    }

    public void setBarListener(BarListener listener) {
        mBarListener = listener;
    }

    private static int computeStepSamples(int bpm, int stepsPerBeat) {
        int safeBpm = Math.max(40, Math.min(240, bpm));
        int spb = Math.max(1, stepsPerBeat);
        return Math.max(256, (int) Math.round((SAMPLE_RATE * 60.0) / ((double) safeBpm * spb)));
    }

    /**
     * Synchronizes the musical exercise, key, tempo, Speed-Builder ladder, audio mix, and loop range
     * with the background audio sequencer thread.
     */
    public void syncSequencerState(
            LickTheory.LickDef lick,
            int keyIdx,
            int bpm,
            int baseBpm,
            int ladderIdx,
            int mixMode,
            boolean loopSubRange,
            int loopStartStep,
            int loopEndStep,
            int barsPlayed) {
        if (lick == null) return;
        synchronized (mLock) {
            mLick = lick;
            mKeyIdx = LickTheory.clamp(keyIdx, 0, LickTheory.NUM_KEYS - 1);
            mBpm = LickTheory.clamp(bpm, LickTheory.MIN_BPM, LickTheory.MAX_BPM);
            mBaseBpm = LickTheory.clamp(baseBpm, LickTheory.MIN_BPM, LickTheory.MAX_BPM);
            mLadderIdx = LickTheory.clamp(ladderIdx, 0, LickTheory.NUM_LADDERS - 1);
            mMixMode = LickTheory.clamp(mixMode, 0, LickTheory.MIX_COUNT - 1);
            mLoopSubRange = loopSubRange;
            mLoopStartStep = LickTheory.clamp(loopStartStep, 0, lick.numSteps - 1);
            mLoopEndStep = LickTheory.clamp(loopEndStep, mLoopStartStep, lick.numSteps - 1);
            mBarsPlayed = Math.max(0, barsPlayed);
            mStepSampleLen = computeStepSamples(mBpm, mLick.stepsPerBeat);
            if (mCurStep >= mLick.numSteps) {
                mCurStep = 0;
            }
        }
    }

    /**
     * Starts or stops the sample-accurate background sequencer clock.
     */
    public void setPlaying(boolean playing, int startStep) {
        synchronized (mLock) {
            mPlaying = playing;
            if (playing) {
                int minStep = mLoopSubRange ? mLoopStartStep : 0;
                int maxStep = mLoopSubRange ? mLoopEndStep : (mLick.numSteps - 1);
                mCurStep = LickTheory.clamp(startStep, minStep, maxStep);
                mStepSamplePos = 0;
                mStepSampleLen = computeStepSamples(mBpm, mLick.stepsPerBeat);
            } else {
                for (int i = 0; i < TOTAL_VOICES; i++) {
                    mVoices[i].active = false;
                    mVoices[i].activeSamplesLeft = 0;
                    mVoices[i].needsExcite = false;
                }
                mActiveClickType = 0;
            }
        }
    }

    private void scheduleVoiceExciteLocked(
            KsVoice v,
            int midiNote,
            int amplitude,
            int dampingQ15,
            boolean warmLegato,
            int startDelaySamples,
            int durationSamples) {
        v.pendingMidi = midiNote;
        v.pendingAmp = amplitude;
        v.pendingDampingQ15 = dampingQ15;
        v.pendingWarmLegato = warmLegato;
        v.pendingStartDelaySamples = startDelaySamples;
        v.pendingDurationSamples = durationSamples;
        v.needsExcite = true;
    }

    /**
     * Triggers a lead guitar tablature note with articulation-sensitive excitation
     * ('D'/'U' crisp pick attack vs. 'H'/'P'/'/' warm legato excitation).
     */
    public void triggerLeadNote(int midiNote, char pickType) {
        if (midiNote < 34 || midiNote > 92) return;
        boolean isLegato = (pickType == 'H' || pickType == 'P' || pickType == '/');
        synchronized (mLock) {
            int prevVoiceIdx = (mLeadFlip == 0) ? VOICE_LEAD_PRIMARY : VOICE_LEAD_SECONDARY;
            // Gently damp previous lead voice so fast 16th runs stay articulate
            if (mVoices[prevVoiceIdx].active) {
                mVoices[prevVoiceIdx].dampingQ15 = 31800;
            }
            mLeadFlip ^= 1;
            int voiceIdx = (mLeadFlip == 0) ? VOICE_LEAD_PRIMARY : VOICE_LEAD_SECONDARY;
            int amp = isLegato ? 21500 : 26500;
            int damping = isLegato ? 32605 : 32565;
            scheduleVoiceExciteLocked(mVoices[voiceIdx], midiNote, amp, damping, isLegato, 0, (int) (SAMPLE_RATE * 0.85));
        }
    }

    /**
     * Triggers a 4-note warm backing chord vamp on beat boundaries when backing mode is active.
     */
    public void triggerBackingChord(int[] chordMidi) {
        if (chordMidi == null) return;
        synchronized (mLock) {
            int count = Math.min(4, chordMidi.length);
            for (int i = 0; i < count; i++) {
                int midi = chordMidi[i];
                if (midi < 34 || midi > 88) continue;
                int delaySamples = i * ((SAMPLE_RATE * 16) / 1000);
                scheduleVoiceExciteLocked(
                        mVoices[VOICE_BACKING_BASE + i],
                        midi,
                        10500,
                        32595,
                        true,
                        delaySamples,
                        (int) (SAMPLE_RATE * 1.15));
            }
        }
    }

    /**
     * Triggers the woodblock metronome click (accented on Beat 1, normal on Beats 2..4).
     */
    public void triggerMetronomeClick(boolean isAccent) {
        synchronized (mLock) {
            mActiveClickPos = 0;
            mActiveClickType = isAccent ? 2 : 1;
        }
    }

    /**
     * Plucks a specific guitar string (0..5) and fret (0..24) when the user taps or
     * drags directly on the interactive fretboard or tablature grid.
     */
    public void pluckFretboardNote(int stringIdx, int fret) {
        if (stringIdx < 0 || stringIdx >= 6 || fret < 0 || fret > 24) return;
        int midi = LickTheory.OPEN_MIDI[stringIdx] + fret;
        synchronized (mLock) {
            scheduleVoiceExciteLocked(
                    mVoices[VOICE_PLUCK_BASE + stringIdx],
                    midi,
                    25000,
                    32585,
                    false,
                    0,
                    (int) (SAMPLE_RATE * 1.4));
        }
    }

    /** Silences all active guitar voices and clicks immediately. */
    public void silenceAll() {
        mReqGen.incrementAndGet();
        synchronized (mLock) {
            mPlaying = false;
            for (int i = 0; i < TOTAL_VOICES; i++) {
                mVoices[i].active = false;
                mVoices[i].activeSamplesLeft = 0;
                mVoices[i].needsExcite = false;
            }
            mActiveClickType = 0;
        }
    }

    private void triggerSequencerStepLocked(int stepIdx) {
        LickTheory.LickDef lick = mLick;
        if (lick == null || stepIdx < 0 || stepIdx >= lick.numSteps) return;
        LickTheory.LickStep st = lick.steps[stepIdx];
        boolean isBeatStart = (stepIdx % lick.stepsPerBeat) == 0;
        int beatIdx = stepIdx / lick.stepsPerBeat;

        if (isBeatStart && mMixMode != LickTheory.MIX_TAB_ONLY) {
            mActiveClickPos = 0;
            mActiveClickType = (beatIdx == 0) ? 2 : 1;
        }
        if (isBeatStart && mMixMode == LickTheory.MIX_TAB_BACK_CLICK) {
            int[] backing = LickTheory.getBackingChordMidi(lick, beatIdx, mKeyIdx);
            if (backing != null) {
                int count = Math.min(4, backing.length);
                for (int i = 0; i < count; i++) {
                    int midi = backing[i];
                    if (midi < 34 || midi > 88) continue;
                    int delaySamples = i * ((SAMPLE_RATE * 16) / 1000);
                    exciteVoiceLocked(
                            mVoices[VOICE_BACKING_BASE + i],
                            midi,
                            10500,
                            32595,
                            true,
                            delaySamples,
                            (int) (SAMPLE_RATE * 1.15));
                }
            }
        }
        if (st.str >= 0 && mMixMode != LickTheory.MIX_CLICK_ONLY) {
            int midi = LickTheory.getStepMidi(st, mKeyIdx);
            if (midi >= 34 && midi <= 92) {
                boolean isLegato = (st.pick == 'H' || st.pick == 'P' || st.pick == '/');
                int prevVoiceIdx = (mLeadFlip == 0) ? VOICE_LEAD_PRIMARY : VOICE_LEAD_SECONDARY;
                if (mVoices[prevVoiceIdx].active) {
                    mVoices[prevVoiceIdx].dampingQ15 = 31800;
                }
                mLeadFlip ^= 1;
                int voiceIdx = (mLeadFlip == 0) ? VOICE_LEAD_PRIMARY : VOICE_LEAD_SECONDARY;
                int amp = isLegato ? 21500 : 26500;
                int damping = isLegato ? 32605 : 32565;
                exciteVoiceLocked(
                        mVoices[voiceIdx],
                        midi,
                        amp,
                        damping,
                        isLegato,
                        0,
                        (int) (SAMPLE_RATE * 0.85));
            }
        }
    }

    private void exciteVoiceLocked(
            KsVoice v,
            int midiNote,
            int amplitude,
            int dampingQ15,
            boolean warmLegato,
            int startDelaySamples,
            int durationSamples) {
        int clampedMidi = Math.max(36, Math.min(88, midiNote));
        double freq = 440.0 * Math.pow(2.0, (clampedMidi - 69) / 12.0);
        int period = (int) (SAMPLE_RATE / freq + 0.5);
        if (period < 16) period = 16;
        if (period >= KS_MAX_DELAY) period = KS_MAX_DELAY - 1;

        v.length = period;
        v.pos = 0;
        v.startDelaySamples = startDelaySamples;
        v.activeSamplesLeft = durationSamples;
        v.dampingQ15 = dampingQ15;

        int rng = mRngState ^ (clampedMidi * 0x9E3779B9) ^ (int) System.nanoTime();
        short[] buf = v.buf;
        int prev = 0;
        for (int i = 0; i < period; i++) {
            rng = rng * 1664525 + 1013904223;
            int noise = (short) (rng >> 16);
            noise = (noise * amplitude) >> 15;

            int idx1 = ((i * 1024) / period) & 1023;
            int idx2 = (idx1 * 2) & 1023;
            int body = ((int) mSinLut[idx1] * (amplitude / 2) + (int) mSinLut[idx2] * (amplitude / 4)) >> 14;

            int sample = (noise / 2) + body;
            if (warmLegato) {
                prev = (prev * 3 + sample) >> 2;
            } else {
                prev = (prev + sample) >> 1;
            }
            if (prev > 29000) prev = 29000;
            if (prev < -29000) prev = -29000;
            buf[i] = (short) prev;
        }
        mRngState = rng;
        v.active = true;
    }

    @SuppressWarnings("deprecation")
    private static AudioTrack createAudioTrack(int trackBufBytes) {
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
                    trackBufBytes,
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE);
        }
        return new AudioTrack(
                AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                trackBufBytes,
                AudioTrack.MODE_STREAM);
    }

    private void audioLoop() {
        int minBufBytes = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        int trackBufBytes = Math.max(minBufBytes, CHUNK_FRAMES * 2 * 4);

        AudioTrack track = null;
        try {
            track = createAudioTrack(trackBufBytes);
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
            boolean anyActive = false;
            int notifyBarsPlayed = -1;
            int notifyNewBpm = -1;

            synchronized (mLock) {
                // Process any pending manual/preview excitations on the background audio thread
                for (int i = 0; i < TOTAL_VOICES; i++) {
                    KsVoice v = mVoices[i];
                    if (v.needsExcite) {
                        v.needsExcite = false;
                        exciteVoiceLocked(
                                v,
                                v.pendingMidi,
                                v.pendingAmp,
                                v.pendingDampingQ15,
                                v.pendingWarmLegato,
                                v.pendingStartDelaySamples,
                                v.pendingDurationSamples);
                    }
                }

                for (int f = 0; f < CHUNK_FRAMES; f++) {
                    if (mPlaying) {
                        anyActive = true;
                        if (mStepSamplePos == 0) {
                            triggerSequencerStepLocked(mCurStep);
                        }
                        mStepSamplePos++;
                        if (mStepSamplePos >= mStepSampleLen) {
                            mStepSamplePos = 0;
                            int minStep = mLoopSubRange ? mLoopStartStep : 0;
                            int maxStep = mLoopSubRange ? mLoopEndStep : (mLick.numSteps - 1);
                            mCurStep++;
                            if (mCurStep > maxStep) {
                                mCurStep = minStep;
                                mBarsPlayed++;
                                LickTheory.LadderPreset lp = LickTheory.LADDERS[mLadderIdx];
                                if (lp.barsPerStep > 0 && (mBarsPlayed % lp.barsPerStep) == 0) {
                                    int goal = Math.min(LickTheory.MAX_BPM, mBaseBpm + lp.goalOffset);
                                    mBpm = Math.min(goal, mBpm + lp.bpmStep);
                                }
                                notifyBarsPlayed = mBarsPlayed;
                                notifyNewBpm = mBpm;
                            }
                            mStepSampleLen = computeStepSamples(mBpm, mLick.stepsPerBeat);
                        }
                    }

                    int mix = 0;

                    for (int i = 0; i < TOTAL_VOICES; i++) {
                        KsVoice v = mVoices[i];
                        if (!v.active || v.activeSamplesLeft <= 0) continue;
                        anyActive = true;

                        if (v.startDelaySamples > 0) {
                            v.startDelaySamples--;
                            continue;
                        }

                        int p = v.pos;
                        int nextP = (p + 1 < v.length) ? (p + 1) : 0;
                        int cur = v.buf[p];
                        int nxt = v.buf[nextP];

                        int avg = (cur + nxt) >> 1;
                        int damped = (avg * v.dampingQ15) >> 15;
                        v.buf[p] = (short) damped;
                        v.pos = nextP;
                        v.activeSamplesLeft--;
                        if (v.activeSamplesLeft == 0) {
                            v.active = false;
                        }
                        mix += cur;
                    }

                    if (mActiveClickType > 0) {
                        anyActive = true;
                        int cpos = mActiveClickPos;
                        if (cpos < CLICK_SAMPLES) {
                            mix += (mActiveClickType == 2) ? mClickAccentPcm[cpos] : mClickNormalPcm[cpos];
                            mActiveClickPos = cpos + 1;
                        } else {
                            mActiveClickType = 0;
                        }
                    }

                    mix = (mix * 3) >> 2;
                    if (mix > 32767) mix = 32767;
                    if (mix < -32767) mix = -32767;
                    outBuf[f] = (short) mix;
                }
            }

            if (notifyBarsPlayed >= 0 && mBarListener != null) {
                mBarListener.onBarAdvanced(notifyBarsPlayed, notifyNewBpm);
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
