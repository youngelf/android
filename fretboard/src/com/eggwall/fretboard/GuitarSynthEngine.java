package com.eggwall.fretboard;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Real-time 44.1 kHz 16-bit mono 6-voice Karplus-Strong plucked guitar synthesizer.
 *
 * <p>Supports:
 * <ul>
 *   <li>Ascending scale-box auditioning ({@link #triggerScaleAudition}) with real-time
 *       vibrating string/fret highlight feedback ({@link #getActivePlayString},
 *       {@link #getActivePlayFret}).</li>
 *   <li>6-string guitar chord downstroke strumming ({@link #triggerChordStrum}) with a natural
 *       32 ms pick stagger across unmuted strings.</li>
 *   <li>Immediate single-string plucking ({@link #pluckNote}) when tapping or dragging across
 *       the guitar fretboard.</li>
 * </ul>
 *
 * <p><b>Engineering Customization Guide:</b>
 * <ul>
 *   <li><b>String Damping / Sustain:</b> Adjust the Q15 loop-filter feedback multiplier
 *       ({@code 32590} out of {@code 32768} = {@code 0.9945}) in {@link #audioLoop} to make
 *       strings ring longer (closer to {@code 32700}) or mute faster (closer to {@code 32300}).</li>
 *   <li><b>Pick Attack & Body Warmth:</b> In {@link #exciteStringLocked}, adjust the blend
 *       between white-noise pick transient ({@code r / 3}) and fundamental/2nd-harmonic
 *       body resonance ({@code mSinLut}), or change the number of initial low-pass smoothing passes.</li>
 * </ul>
 */
public final class GuitarSynthEngine {

    /** Output sample rate in Hz (44.1 kHz CD-quality mono PCM). */
    public static final int SAMPLE_RATE = 44100;
    /** Maximum waveguide delay-line length in samples (supports down to ~60 Hz, well below Drop D = 73.4 Hz). */
    private static final int KS_MAX_DELAY = 750;
    /** Audio frames mixed per AudioTrack write call (~5.8 ms per chunk at 44.1 kHz). */
    private static final int CHUNK_FRAMES = 256;

    private static final class KsString {
        final short[] buf = new short[KS_MAX_DELAY];
        int length = 24;
        int pos = 0;
        int activeSamplesLeft = 0;
        boolean active = false;
    }

    private static final class PluckEvent {
        int stringIdx;
        int fret;
        int midiNote;
        int delaySamples;
    }

    private static final int MAX_PLUCK_QUEUE = 64;

    private final KsString[] mStrings = new KsString[FretboardTheory.NUM_STRINGS];
    private final PluckEvent[] mQueue = new PluckEvent[MAX_PLUCK_QUEUE];
    private int mQueueHead = 0;
    private int mQueueCount = 0;
    private final Object mLock = new Object();

    private final short[] mSinLut = new short[1024];
    private final AtomicInteger mReqGen = new AtomicInteger(0);

    private volatile boolean mRunning = false;
    private Thread mAudioThread;
    private int mRngState = 0x9E3779B9;

    /* Real-time glowing note indicator updated by the audio thread as plucks fire */
    private volatile int mActivePlayStr  = -1;
    private volatile int mActivePlayFret = -1;
    private volatile int mActivePlayTtl  = 0;

    public GuitarSynthEngine() {
        for (int s = 0; s < FretboardTheory.NUM_STRINGS; s++) {
            mStrings[s] = new KsString();
        }
        for (int i = 0; i < MAX_PLUCK_QUEUE; i++) {
            mQueue[i] = new PluckEvent();
        }
        for (int i = 0; i < 1024; i++) {
            mSinLut[i] = (short) (Math.sin((2.0 * Math.PI * (double) i) / 1024.0) * 16384.0);
        }
    }

    public synchronized void start() {
        if (mRunning) return;
        mRunning = true;
        mAudioThread = new Thread(this::audioLoop, "Fretboard-KarplusStrong");
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

    /** Returns the currently ringing string index (0..5) or {@code -1} if idle. */
    public int getActivePlayString() {
        return (mActivePlayTtl > 0) ? mActivePlayStr : -1;
    }

    /** Returns the currently ringing fret (0..15) or {@code -1} if idle. */
    public int getActivePlayFret() {
        return (mActivePlayTtl > 0) ? mActivePlayFret : -1;
    }

    /** Returns {@code true} if a note glow or queued sequence is currently active. */
    public boolean isHighlightActive() {
        synchronized (mLock) {
            return mActivePlayTtl > 0 || mQueueHead < mQueueCount;
        }
    }

    /** Immediately plucks a single string and fret in the given guitar tuning. */
    public void pluckNote(int tuningIdx, int stringIdx, int fret) {
        if (stringIdx < 0 || stringIdx >= FretboardTheory.NUM_STRINGS || fret < 0) return;
        mReqGen.incrementAndGet();
        int midi = FretboardTheory.getMidiNote(tuningIdx, stringIdx, fret);
        synchronized (mLock) {
            mQueueHead = 0;
            mQueueCount = 0;
            exciteStringLocked(stringIdx, fret, midi);
        }
    }

    /**
     * Strums a 6-string guitar chord (or Explorer locked chord) with a 32 ms string-to-string
     * pick stagger across all unmuted strings ({@code frets[s] >= 0}).
     */
    public void triggerChordStrum(int tuningIdx, int[] frets) {
        if (frets == null) return;
        mReqGen.incrementAndGet();
        synchronized (mLock) {
            mQueueHead = 0;
            mQueueCount = 0;
            int step = 0;
            for (int s = 0; s < FretboardTheory.NUM_STRINGS && s < frets.length; s++) {
                int f = frets[s];
                if (f >= 0) {
                    int midi = FretboardTheory.getMidiNote(tuningIdx, s, f);
                    enqueueLocked(s, f, midi, (step == 0) ? 0 : 32);
                    step++;
                }
            }
        }
    }

    /**
     * Auditions the active scale CAGED box ascending across all 6 strings (145 ms per note),
     * illuminating each fretted note on the neck as it is plucked.
     */
    public void triggerScaleAudition(int tuningIdx, int rootPc, int scaleIdx, int cagedPos) {
        mReqGen.incrementAndGet();
        FretboardTheory.ScaleDef sc = FretboardTheory.SCALES[
                FretboardTheory.clampIndex(scaleIdx, FretboardTheory.SCALES.length)];
        int effectivePos = (cagedPos <= 0) ? 1 : cagedPos;
        int[] range = FretboardTheory.computeCagedBoxRange(tuningIdx, rootPc, effectivePos);
        int lowF = range[0];
        int highF = range[1];

        synchronized (mLock) {
            mQueueHead = 0;
            mQueueCount = 0;
            int step = 0;
            int lastMidi = -1;
            for (int s = 0; s < FretboardTheory.NUM_STRINGS; s++) {
                for (int f = lowF; f <= highF; f++) {
                    int midi = FretboardTheory.getMidiNote(tuningIdx, s, f);
                    int pc = midi % 12;
                    int rel = (pc - rootPc + 12) % 12;
                    if (sc.mask[rel] > 0 && midi > lastMidi) {
                        enqueueLocked(s, f, midi, (step == 0) ? 0 : 145);
                        lastMidi = midi;
                        step++;
                    }
                }
            }
        }
    }

    /** Silences all strings and clears the pluck queue. */
    public void silenceAll() {
        mReqGen.incrementAndGet();
        synchronized (mLock) {
            mQueueHead = 0;
            mQueueCount = 0;
            mActivePlayTtl = 0;
            mActivePlayStr = -1;
            mActivePlayFret = -1;
            for (int s = 0; s < FretboardTheory.NUM_STRINGS; s++) {
                mStrings[s].active = false;
                mStrings[s].activeSamplesLeft = 0;
            }
        }
    }

    private void enqueueLocked(int stringIdx, int fret, int midiNote, int delayMs) {
        if (mQueueCount >= MAX_PLUCK_QUEUE) return;
        PluckEvent ev = mQueue[mQueueCount++];
        ev.stringIdx = stringIdx;
        ev.fret = fret;
        ev.midiNote = midiNote;
        ev.delaySamples = (delayMs * SAMPLE_RATE) / 1000;
    }

    private void exciteStringLocked(int stringIdx, int fret, int midiNote) {
        if (stringIdx < 0 || stringIdx >= FretboardTheory.NUM_STRINGS) return;
        KsString ks = mStrings[stringIdx];
        double freq = FretboardTheory.midiToFreq(midiNote);
        int period = (int) (SAMPLE_RATE / freq + 0.5);
        if (period < 24) period = 24;
        if (period >= KS_MAX_DELAY) period = KS_MAX_DELAY - 1;

        ks.length = period;
        ks.pos = 0;
        ks.activeSamplesLeft = SAMPLE_RATE * 2; /* 2.0 seconds decay */

        int rng = mRngState ^ (midiNote * 0x85EBCA6B) ^ (int) System.nanoTime();
        short[] raw = ks.buf;
        for (int i = 0; i < period; i++) {
            rng = rng * 1664525 + 1013904223;
            short r = (short) ((rng >> 16) - 32768);
            int idx1 = ((i * 1024) / period) & 1023;
            int idx2 = (idx1 * 2) & 1023;
            int warm = ((int) mSinLut[idx1] * 14000 + (int) mSinLut[idx2] * 6000) >> 14;
            int mixed = (r / 3) + warm;
            if (mixed > 28000) mixed = 28000;
            if (mixed < -28000) mixed = -28000;
            raw[i] = (short) mixed;
        }
        mRngState = rng;

        for (int pass = 0; pass < 2; pass++) {
            short prev = raw[period - 1];
            for (int i = 0; i < period; i++) {
                short cur = raw[i];
                raw[i] = (short) (((int) cur + (int) prev) >> 1);
                prev = cur;
            }
        }
        ks.active = true;

        mActivePlayStr = stringIdx;
        mActivePlayFret = fret;
        mActivePlayTtl = SAMPLE_RATE / 4; /* 250 ms glow per plucked note */
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
                    while (mQueueHead < mQueueCount) {
                        PluckEvent ev = mQueue[mQueueHead];
                        if (ev.delaySamples <= 0) {
                            mQueueHead++;
                            exciteStringLocked(ev.stringIdx, ev.fret, ev.midiNote);
                        } else {
                            ev.delaySamples--;
                            anyActive = true;
                            break;
                        }
                    }

                    if (mActivePlayTtl > 0) {
                        mActivePlayTtl--;
                        if (mActivePlayTtl == 0) {
                            mActivePlayStr = -1;
                            mActivePlayFret = -1;
                        }
                    }

                    int mix = 0;
                    for (int s = 0; s < FretboardTheory.NUM_STRINGS; s++) {
                        KsString ks = mStrings[s];
                        if (!ks.active || ks.activeSamplesLeft <= 0) continue;
                        anyActive = true;

                        int p = ks.pos;
                        int nextP = (p + 1 < ks.length) ? (p + 1) : 0;
                        int cur = ks.buf[p];
                        int nxt = ks.buf[nextP];

                        /* Karplus-Strong 1-zero loop filter with Q15 damping (32590 / 32768 = 0.9945) */
                        int avg = (cur + nxt) >> 1;
                        int damped = (avg * 32590) >> 15;
                        ks.buf[p] = (short) damped;
                        ks.pos = nextP;
                        ks.activeSamplesLeft--;
                        if (ks.activeSamplesLeft == 0) {
                            ks.active = false;
                        }
                        mix += cur;
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
