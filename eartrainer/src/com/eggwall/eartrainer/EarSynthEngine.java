package com.eggwall.eartrainer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Real-time 44.1 kHz 16-bit mono polyphonic Karplus-Strong acoustic guitar & piano
 * tone synthesizer ported from the PowKiddy V90 {@code eartrainer.c} audio engine.
 *
 * <p>Supports scheduled note sequences on the audio thread so intervals (ascending,
 * descending, harmonic), chords ({@code ARP+STRUM}, {@code STRUM}, {@code ARPEGGIO}),
 * I-IV-V-I tonic cadences, and melodic dictations play with sample-accurate timing.
 *
 * <p><b>Engineering Customization Guide:</b>
 * <ul>
 *   <li><b>Voice Polyphony & Schedule Capacity:</b> Increase {@link #MAX_VOICES} (default 8)
 *       or {@link #MAX_SCHEDULED} (default 24) to support longer multi-bar dictations.</li>
 *   <li><b>Acoustic Sustain & Damping:</b> In {@link #audioLoop}, adjust {@code v.dampingNum}
 *       (default {@code 127} or {@code 126} out of {@code 128} in the two-point average)
 *       to lengthen or shorten string decay.</li>
 *   <li><b>Warmth vs. Brightness:</b> In {@link #pluckVoiceImmediateLocked}, adjust the blend
 *       between fundamental/2nd-harmonic sine body ({@code body}) and low-pass-filtered
 *       pick noise ({@code prev}).</li>
 * </ul>
 */
public final class EarSynthEngine {

    /** Output sample rate in Hz (44.1 kHz CD-quality 16-bit mono PCM). */
    public static final int SAMPLE_RATE = 44100;
    /** Number of concurrent polyphonic waveguide voices. */
    private static final int MAX_VOICES = 8;
    /** Maximum waveguide delay-line length in samples (supports down to ~43 Hz). */
    private static final int MAX_DELAY_LEN = 1024;
    /** Maximum number of sample-accurate scheduled note plucks in the queue. */
    private static final int MAX_SCHEDULED = 24;
    /** Audio frames mixed per AudioTrack write call (~5.8 ms per chunk at 44.1 kHz). */
    private static final int CHUNK_FRAMES = 256;

    private static final class Voice {
        boolean active = false;
        final short[] delayLine = new short[MAX_DELAY_LEN];
        int delayLen = 64;
        int pos = 0;
        int samplesLeft = 0;
        int dampingNum = 126; /* Out of 256 for (s0 + s1) * dampingNum >> 8 */
    }

    private static final class ScheduledPluck {
        boolean pending = false;
        int delaySamples = 0;
        int midiNote = 60;
        int durationMs = 1000;
    }

    private final Voice[] mVoices = new Voice[MAX_VOICES];
    private final ScheduledPluck[] mSched = new ScheduledPluck[MAX_SCHEDULED];
    private final Object mLock = new Object();

    private int mNextVoice = 0;
    private int mPrngState = 0x13579BDF;
    private volatile boolean mRunning = false;
    private Thread mAudioThread;

    public EarSynthEngine() {
        for (int i = 0; i < MAX_VOICES; i++) {
            mVoices[i] = new Voice();
        }
        for (int i = 0; i < MAX_SCHEDULED; i++) {
            mSched[i] = new ScheduledPluck();
        }
    }

    public synchronized void start() {
        if (mRunning) return;
        mRunning = true;
        mAudioThread = new Thread(this::audioLoop, "EarTrainer-KarplusStrong");
        mAudioThread.setPriority(Thread.MAX_PRIORITY - 1);
        mAudioThread.start();
    }

    public synchronized void stop() {
        if (!mRunning) return;
        mRunning = false;
        silenceAll();
        if (mAudioThread != null) {
            try {
                mAudioThread.join(600);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            mAudioThread = null;
        }
    }

    /** Clears all scheduled note events and silences all active waveguide voices. */
    public void silenceAll() {
        synchronized (mLock) {
            for (int i = 0; i < MAX_SCHEDULED; i++) {
                mSched[i].pending = false;
            }
            for (int i = 0; i < MAX_VOICES; i++) {
                mVoices[i].active = false;
                mVoices[i].samplesLeft = 0;
            }
        }
    }

    /** Plucks a single MIDI note immediately (e.g. when tapping the piano keyboard or fretboard). */
    public void playNoteImmediate(int midiNote, int durationMs) {
        synchronized (mLock) {
            pluckVoiceImmediateLocked(midiNote, durationMs);
        }
    }

    /** Plays the Root reference note alone (`X` button while guessing). */
    public void playRootReference(int rootMidi) {
        synchronized (mLock) {
            clearScheduleLocked();
            scheduleNoteLocked(rootMidi, 0, 1250);
        }
    }

    /**
     * Plays an item ({@code 0..11}) from the current {@link EarTheory.TrainerSession} state,
     * supporting all 4 drill modes (Intervals, Chords, Scale Degrees / Solfege, and Melodic/Cadences).
     */
    public void playSessionItem(EarTheory.TrainerSession session, int itemIdx) {
        if (session == null || itemIdx < 0 || itemIdx >= EarTheory.NUM_ITEMS) return;
        synchronized (mLock) {
            clearScheduleLocked();
            int root = session.rootMidi;

            switch (session.mode) {
                case EarTheory.MODE_INTERVALS: {
                    int semitones = EarTheory.INTERVALS[itemIdx].semitones;
                    int note1 = root;
                    int note2 = root + semitones;
                    if (session.intStyle == EarTheory.INT_STYLE_HARM) {
                        scheduleNoteLocked(note1, 0, 1400);
                        scheduleNoteLocked(note2, 22, 1400);
                    } else if (session.curIsDesc) {
                        scheduleNoteLocked(note2, 0, 1100);
                        scheduleNoteLocked(note1, 580, 1300);
                    } else {
                        scheduleNoteLocked(note1, 0, 1100);
                        scheduleNoteLocked(note2, 580, 1300);
                    }
                    break;
                }

                case EarTheory.MODE_CHORDS: {
                    EarTheory.ChordDef cd = EarTheory.CHORDS[itemIdx];
                    if (session.chdStyle == EarTheory.CHD_STYLE_STRUM) {
                        for (int n = 0; n < cd.numNotes; n++) {
                            scheduleNoteLocked(root + cd.semitones[n], n * 35, 1600);
                        }
                    } else if (session.chdStyle == EarTheory.CHD_STYLE_ARP) {
                        for (int n = 0; n < cd.numNotes; n++) {
                            scheduleNoteLocked(root + cd.semitones[n], n * 230, 1400);
                        }
                    } else {
                        /* ARP + STRUM: Arpeggiate individual notes, then strum chord together */
                        for (int n = 0; n < cd.numNotes; n++) {
                            scheduleNoteLocked(root + cd.semitones[n], n * 200, 1100);
                        }
                        int strumStart = cd.numNotes * 200 + 220;
                        for (int n = 0; n < cd.numNotes; n++) {
                            scheduleNoteLocked(root + cd.semitones[n], strumStart + n * 32, 1500);
                        }
                    }
                    break;
                }

                case EarTheory.MODE_DEGREES: {
                    int semi = EarTheory.SCALE_DEGREES[itemIdx].semitones;
                    if (session.degStyle == EarTheory.DEG_STYLE_CADENCE_NOTE) {
                        /* Play tonic triad strum (I) followed by the scale degree note */
                        scheduleNoteLocked(root, 0, 900);
                        scheduleNoteLocked(root + 4, 24, 900);
                        scheduleNoteLocked(root + 7, 48, 900);
                        scheduleNoteLocked(root + semi, 620, 1400);
                    } else {
                        scheduleNoteLocked(root, 0, 950);
                        scheduleNoteLocked(root + semi, 540, 1350);
                    }
                    break;
                }

                case EarTheory.MODE_PROGRESSIONS: {
                    EarTheory.MelodicPatternDef mp = EarTheory.PROGRESSIONS[itemIdx];
                    if (session.progStyle == EarTheory.PROG_STYLE_CHORDS) {
                        /* Play 3-chord cadence progression */
                        for (int c = 0; c < 3; c++) {
                            int cRoot = root + mp.chordRootSemitones[c];
                            EarTheory.ChordDef cd = EarTheory.CHORDS[mp.chordQualities[c]];
                            int baseDelay = c * 540;
                            for (int n = 0; n < cd.numNotes; n++) {
                                scheduleNoteLocked(cRoot + cd.semitones[n], baseDelay + n * 24, 950);
                            }
                        }
                    } else {
                        /* Play 3-note melodic dictation cell */
                        for (int n = 0; n < mp.melodySemitones.length; n++) {
                            scheduleNoteLocked(root + mp.melodySemitones[n], n * 380, 1050);
                        }
                    }
                    break;
                }
            }
        }
    }

    private void clearScheduleLocked() {
        for (int i = 0; i < MAX_SCHEDULED; i++) {
            mSched[i].pending = false;
        }
        for (int i = 0; i < MAX_VOICES; i++) {
            mVoices[i].active = false;
            mVoices[i].samplesLeft = 0;
        }
    }

    private void scheduleNoteLocked(int midiNote, int delayMs, int durationMs) {
        for (int i = 0; i < MAX_SCHEDULED; i++) {
            if (!mSched[i].pending) {
                mSched[i].midiNote = midiNote;
                mSched[i].delaySamples = (SAMPLE_RATE * delayMs) / 1000;
                mSched[i].durationMs = durationMs;
                mSched[i].pending = true;
                return;
            }
        }
    }

    private short fastRandS16() {
        mPrngState = mPrngState * 1664525 + 1013904223;
        return (short) (mPrngState >> 16);
    }

    private void pluckVoiceImmediateLocked(int midiNote, int durationMs) {
        double freq = 440.0 * Math.pow(2.0, (double) (midiNote - 69) / 12.0);
        int dlen = (int) ((double) SAMPLE_RATE / freq + 0.5);
        if (dlen < 16) dlen = 16;
        if (dlen >= MAX_DELAY_LEN) dlen = MAX_DELAY_LEN - 1;

        Voice v = mVoices[mNextVoice];
        mNextVoice = (mNextVoice + 1) % MAX_VOICES;

        v.active = false;
        v.delayLen = dlen;
        v.pos = 0;
        v.samplesLeft = (SAMPLE_RATE * durationMs) / 1000;
        v.dampingNum = (midiNote < 50) ? 127 : 126;

        /* Excite waveguide with warm pick-position filtered noise + fundamental & 2nd harmonic body */
        int prev = 0;
        for (int i = 0; i < dlen; i++) {
            double phase = (2.0 * Math.PI * (double) i) / (double) dlen;
            int body = (int) (Math.sin(phase) * 14000.0 + Math.sin(phase * 2.0) * 6500.0);
            int noise = (int) fastRandS16() / 2;
            prev = (prev * 3 + noise) / 4;
            int sample = body + prev;
            if (sample > 28000) sample = 28000;
            if (sample < -28000) sample = -28000;
            v.delayLine[i] = (short) sample;
        }
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
                for (int i = 0; i < CHUNK_FRAMES; i++) {
                    for (int s = 0; s < MAX_SCHEDULED; s++) {
                        ScheduledPluck sp = mSched[s];
                        if (sp.pending) {
                            anyActive = true;
                            if (sp.delaySamples <= 0) {
                                sp.pending = false;
                                pluckVoiceImmediateLocked(sp.midiNote, sp.durationMs);
                            } else {
                                sp.delaySamples--;
                            }
                        }
                    }

                    int mix = 0;
                    for (int v = 0; v < MAX_VOICES; v++) {
                        Voice gv = mVoices[v];
                        if (!gv.active) continue;
                        if (gv.samplesLeft <= 0) {
                            gv.active = false;
                            continue;
                        }
                        anyActive = true;
                        gv.samplesLeft--;

                        int p0 = gv.pos;
                        int p1 = (p0 + 1 < gv.delayLen) ? (p0 + 1) : 0;
                        int s0 = gv.delayLine[p0];
                        int s1 = gv.delayLine[p1];

                        mix += s0;

                        int avg = ((s0 + s1) * gv.dampingNum) >> 8;
                        gv.delayLine[p0] = (short) avg;
                        gv.pos = p1;
                    }

                    mix = (mix * 3) / 4;
                    if (mix > 32767) mix = 32767;
                    if (mix < -32767) mix = -32767;
                    outBuf[i] = (short) mix;
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
