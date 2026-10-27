package com.eggwall.chordwheel;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Background-thread 6-string fixed-point Karplus-Strong plucked guitar synthesizer.
 * Supports both full-chord downstroke strumming (with a 22 ms pick stagger) and
 * individual string plucking when the user taps or drags across strings on the
 * guitar fretboard diagram.
 *
 * <p><b>Engineering Customization Guide:</b>
 * <ul>
 *   <li><b>String Damping / Sustain:</b> Adjust the Q15 loop-filter feedback multiplier
 *       ({@code 32590} out of {@code 32768} = {@code 0.9945}) in {@link #audioLoop} to make
 *       strings ring longer (closer to {@code 32700}) or mute faster (closer to {@code 32300}).</li>
 *   <li><b>Pick Attack & Body Warmth:</b> In {@link #exciteStringLocked}, adjust the blend
 *       between white-noise pick transient ({@code r / 3}) and fundamental/2nd-harmonic
 *       body resonance ({@code mSinLut}), or change the number of initial low-pass smoothing passes.</li>
 *   <li><b>Strum Speed:</b> In {@link #triggerStrum}, change the {@code 22} ms stagger between
 *       adjacent unmuted strings for faster or slower arpeggiated strums.</li>
 * </ul>
 */
public final class GuitarSynthEngine {

    /** Output sample rate in Hz (44.1 kHz CD-quality mono PCM). */
    private static final int SAMPLE_RATE = 44100;
    /** Maximum waveguide delay-line length in samples (supports down to ~73.5 Hz, below E2 = 82.4 Hz). */
    private static final int KS_MAX_DELAY = 600;
    /** Audio frames mixed per AudioTrack write call (~5.8 ms per chunk at 44.1 kHz). */
    private static final int CHUNK_FRAMES = 256;

    private static final class KsString {
        final short[] buf = new short[KS_MAX_DELAY];
        int length = 24;
        int pos = 0;
        int startDelaySamples = 0;
        int activeSamplesLeft = 0;
        boolean active = false;
    }

    private final KsString[] mStrings = new KsString[6];
    private final Object mStringLock = new Object();

    private final short[] mSinLut = new short[1024];
    private final AtomicInteger mReqGen = new AtomicInteger(0);

    private volatile boolean mRunning = false;
    private Thread mAudioThread;
    private int mRngState = 0x9E3779B9;

    public GuitarSynthEngine() {
        for (int s = 0; s < 6; s++) {
            mStrings[s] = new KsString();
        }
        for (int i = 0; i < 1024; i++) {
            mSinLut[i] = (short) (Math.sin((2.0 * Math.PI * (double) i) / 1024.0) * 16384.0);
        }
    }

    public synchronized void start() {
        if (mRunning) return;
        mRunning = true;
        mAudioThread = new Thread(this::audioLoop, "ChordWheel-KarplusStrong");
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
     * Triggers a full 6-string guitar downstroke strum for the given voicing,
     * with a 22 ms pick stagger between consecutive unmuted strings.
     */
    public void triggerStrum(final MusicTheory.GuitarVoicing voicing) {
        if (voicing == null) return;
        final int gen = mReqGen.incrementAndGet();
        final int[] frets = new int[6];
        System.arraycopy(voicing.frets, 0, frets, 0, 6);

        synchronized (mStringLock) {
            int activeOrder = 0;
            for (int s = 0; s < 6; s++) {
                if (mReqGen.get() != gen) return;
                KsString ks = mStrings[s];
                if (frets[s] < 0) {
                    ks.active = false;
                    ks.activeSamplesLeft = 0;
                    continue;
                }
                int delaySamples = activeOrder * ((SAMPLE_RATE * 22) / 1000);
                exciteStringLocked(ks, s, frets[s], delaySamples);
                activeOrder++;
            }
        }
    }

    /**
     * Plucks a single guitar string (0 = 6th string E .. 5 = 1st string e) immediately,
     * allowing the user to tap individual strings or drag their finger across the strings.
     */
    public void pluckSingleString(int stringIdx, int fret) {
        if (stringIdx < 0 || stringIdx >= 6 || fret < 0) return;
        synchronized (mStringLock) {
            exciteStringLocked(mStrings[stringIdx], stringIdx, fret, 0);
        }
    }

    /** Silences all strings immediately. */
    public void silenceAll() {
        mReqGen.incrementAndGet();
        synchronized (mStringLock) {
            for (int s = 0; s < 6; s++) {
                mStrings[s].active = false;
                mStrings[s].activeSamplesLeft = 0;
            }
        }
    }

    private void exciteStringLocked(KsString ks, int stringIdx, int fret, int startDelaySamples) {
        int midiNote = MusicTheory.STRING_MIDI[stringIdx] + fret;
        double freq = 440.0 * Math.pow(2.0, (double) (midiNote - 69) / 12.0);
        int period = (int) (SAMPLE_RATE / freq + 0.5);
        if (period < 24) period = 24;
        if (period >= KS_MAX_DELAY) period = KS_MAX_DELAY - 1;

        ks.length = period;
        ks.pos = 0;
        ks.startDelaySamples = startDelaySamples;
        ks.activeSamplesLeft = SAMPLE_RATE * 2; /* 2.0s acoustic ring-out */

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

        /* Two-pass smoothing filter so higher strings sound warm & acoustic */
        for (int pass = 0; pass < 2; pass++) {
            short prev = raw[period - 1];
            for (int i = 0; i < period; i++) {
                short cur = raw[i];
                raw[i] = (short) (((int) cur + (int) prev) >> 1);
                prev = cur;
            }
        }
        ks.active = true;
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
            synchronized (mStringLock) {
                for (int f = 0; f < CHUNK_FRAMES; f++) {
                    int mix = 0;
                    for (int s = 0; s < 6; s++) {
                        KsString ks = mStrings[s];
                        if (!ks.active || ks.activeSamplesLeft <= 0) continue;
                        anyActive = true;

                        if (ks.startDelaySamples > 0) {
                            ks.startDelaySamples--;
                            continue;
                        }

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
