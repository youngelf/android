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

    private static final class KsVoice {
        final short[] buf = new short[KS_MAX_DELAY];
        int length = 32;
        int pos = 0;
        int startDelaySamples = 0;
        int activeSamplesLeft = 0;
        int dampingQ15 = 32560;
        boolean active = false;
    }

    private final KsVoice[] mVoices = new KsVoice[TOTAL_VOICES];
    private final Object mLock = new Object();

    private final short[] mClickAccentPcm = new short[CLICK_SAMPLES];
    private final short[] mClickNormalPcm = new short[CLICK_SAMPLES];
    private final short[] mSinLut = new short[1024];

    private int mActiveClickType = 0; // 0 = none, 1 = normal, 2 = accent
    private int mActiveClickPos = 0;
    private int mLeadFlip = 0;

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
            exciteVoiceLocked(mVoices[voiceIdx], midiNote, amp, damping, isLegato, 0, (int) (SAMPLE_RATE * 0.85));
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
            exciteVoiceLocked(
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
            for (int i = 0; i < TOTAL_VOICES; i++) {
                mVoices[i].active = false;
                mVoices[i].activeSamplesLeft = 0;
            }
            mActiveClickType = 0;
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
            boolean anyActive = false;
            synchronized (mLock) {
                for (int f = 0; f < CHUNK_FRAMES; f++) {
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
