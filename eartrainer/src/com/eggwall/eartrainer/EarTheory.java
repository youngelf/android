package com.eggwall.eartrainer;

import java.util.Random;

/**
 * Pure, stateless ear-training & music-theory engine ported and extended from the
 * PowKiddy V90 {@code eartrainer} application ({@code eartrainer.c}).
 *
 * <p>Supports four comprehensive ear-training drill modes across a 12-item (4x3) keypad:
 * <ul>
 *   <li><b>Mode 0 ({@link #MODE_INTERVALS}):</b> 12 Chromatic Intervals ({@code m2..P8})
 *       with Ascending ({@code ASC}), Descending ({@code DESC}), Harmonic ({@code HARM}),
 *       and Mixed ({@code MIX}) playback styles.</li>
 *   <li><b>Mode 1 ({@link #MODE_CHORDS}):</b> 12 Guitar & Piano Chord Qualities
 *       ({@code Major, Minor, dim, aug, sus2, sus4, Dom7, Maj7, m7, m7b5, dim7, 7sus4})
 *       with {@code ARP+STRUM}, {@code STRUM}, and {@code ARPEGGIO} playback styles.</li>
 *   <li><b>Mode 2 ({@link #MODE_DEGREES}):</b> 12 Chromatic Scale Degrees & Solfege
 *       ({@code b2 Ra..1/8 Do'}) played after a tonic cadence reference.</li>
 *   <li><b>Mode 3 ({@link #MODE_PROGRESSIONS}):</b> 12Melodic Dictations & Harmonic
 *       Progressions (3-note melodic cells and 3-chord cadential progressions).</li>
 * </ul>
 *
 * <p><b>Engineering Extension Points:</b>
 * <ul>
 *   <li><b>Tunings / Instruments:</b> Modify {@link #OPEN_STRING_MIDI} and {@link #STRING_NAMES}
 *       to adjust the 6-string guitar neck visualizer's open string pitches.</li>
 *   <li><b>Intervals & Song Mnemonics:</b> Edit {@link #INTERVALS} to customize song references
 *       or difficulty tiers ({@code minTier = 0..2}).</li>
 *   <li><b>Chord Voicings & Formulas:</b> Edit {@link #CHORDS} to customize chord formulas,
 *       semitone stacks, and movable 4-string fretboard shapes.</li>
 *   <li><b>Question & Guess Rules:</b> {@link TrainerSession} encapsulates the 3-guess rule,
 *       streak tracking, and per-item accuracy statistics with zero Android UI dependencies.</li>
 * </ul>
 */
public final class EarTheory {

    private EarTheory() {}

    /** Number of answer items in the 4x3 keypad grid for each drill mode. */
    public static final int NUM_ITEMS = 12;
    /** Number of guesses allowed per question before the answer is revealed. */
    public static final int MAX_GUESSES = 3;

    /* Drill Mode Identifiers */
    public static final int MODE_INTERVALS    = 0;
    public static final int MODE_CHORDS       = 1;
    public static final int MODE_DEGREES      = 2;
    public static final int MODE_PROGRESSIONS = 3;
    public static final int NUM_MODES         = 4;

    public static final String[] MODE_NAMES = {
        "INTERVALS", "CHORDS", "SOLFEGE", "MELODIC"
    };

    public static final String[] MODE_FULL_TITLES = {
        "Intervals (m2..P8)",
        "Chord Qualities",
        "Scale Degrees / Solfege",
        "Melodic & Cadence Dictation"
    };

    /* Difficulty Tier Identifiers */
    public static final int TIER_EASY   = 0;
    public static final int TIER_MEDIUM = 1;
    public static final int TIER_ALL_12 = 2;

    public static final String[] TIER_NAMES = { "EASY", "MEDIUM", "ALL 12" };

    /* Interval Playback Styles (L1) */
    public static final int INT_STYLE_ASC  = 0;
    public static final int INT_STYLE_DESC = 1;
    public static final int INT_STYLE_HARM = 2;
    public static final int INT_STYLE_MIX  = 3;

    public static final String[] INT_STYLE_NAMES = { "ASC", "DESC", "HARM", "MIX" };

    /* Chord Playback Styles (L1) */
    public static final int CHD_STYLE_ARP_STRUM = 0;
    public static final int CHD_STYLE_STRUM     = 1;
    public static final int CHD_STYLE_ARP       = 2;

    public static final String[] CHD_STYLE_NAMES = { "ARP+STRUM", "STRUM", "ARPEGGIO" };

    /* Scale Degree / Solfege Playback Styles (L1) */
    public static final int DEG_STYLE_CADENCE_NOTE = 0;
    public static final int DEG_STYLE_ROOT_NOTE    = 1;

    public static final String[] DEG_STYLE_NAMES = { "I-IV-V-I+DEG", "ROOT+DEG" };

    /* Melodic / Progression Playback Styles (L1) */
    public static final int PROG_STYLE_MELODIC = 0;
    public static final int PROG_STYLE_CHORDS  = 1;

    public static final String[] PROG_STYLE_NAMES = { "MELODY", "CADENCE" };

    /**
     * Standard 6-string guitar open-string MIDI pitches:
     * E2(40), A2(45), D3(50), G3(55), B3(59), E4(64).
     */
    public static final int[] OPEN_STRING_MIDI = { 40, 45, 50, 55, 59, 64 };
    public static final String[] STRING_NAMES  = { "E", "A", "D", "G", "B", "e" };

    /** Chromatic note names (0 = C .. 11 = B). */
    public static final String[] NOTE_NAMES = {
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };

    /**
     * Metadata for each of the 12 intervals in {@link #MODE_INTERVALS}.
     */
    public static final class IntervalDef {
        public final String shortName;
        public final String subLabel;
        public final String fullName;
        public final String mnemonic;
        public final int semitones;
        public final int minTier;

        public IntervalDef(String shortName, String subLabel, String fullName,
                           String mnemonic, int semitones, int minTier) {
            this.shortName = shortName;
            this.subLabel = subLabel;
            this.fullName = fullName;
            this.mnemonic = mnemonic;
            this.semitones = semitones;
            this.minTier = minTier;
        }
    }

    /** Exact 12 intervals from PowKiddy V90 {@code eartrainer.c}. */
    public static final IntervalDef[] INTERVALS = {
        new IntervalDef("m2", "1 st",  "Minor 2nd",   "\"Jaws\" Theme",        1, TIER_ALL_12),
        new IntervalDef("M2", "2 st",  "Major 2nd",   "\"Happy Birthday\"",    2, TIER_MEDIUM),
        new IntervalDef("m3", "3 st",  "Minor 3rd",   "\"Smoke on Water\"",    3, TIER_EASY),
        new IntervalDef("M3", "4 st",  "Major 3rd",   "\"When the Saints\"",   4, TIER_EASY),
        new IntervalDef("P4", "5 st",  "Perfect 4th", "\"Here Comes Bride\"",  5, TIER_EASY),
        new IntervalDef("TT", "b5/6",  "Tritone",     "\"The Simpsons\"",      6, TIER_ALL_12),
        new IntervalDef("P5", "7 st",  "Perfect 5th", "\"Star Wars\" (5th)",   7, TIER_EASY),
        new IntervalDef("m6", "8 st",  "Minor 6th",   "\"The Entertainer\"",   8, TIER_ALL_12),
        new IntervalDef("M6", "9 st",  "Major 6th",   "\"My Bonnie\"",         9, TIER_MEDIUM),
        new IntervalDef("m7", "10 st", "Minor 7th",   "\"Star Trek\" Theme",  10, TIER_ALL_12),
        new IntervalDef("M7", "11 st", "Major 7th",   "\"Take On Me\"",       11, TIER_MEDIUM),
        new IntervalDef("P8", "Oct",   "Octave",      "\"Over the Rainbow\"", 12, TIER_EASY)
    };

    /**
     * Metadata for each of the 12 chord qualities in {@link #MODE_CHORDS}.
     */
    public static final class ChordDef {
        public final String shortName;
        public final String formula;
        public final String fullName;
        public final int numNotes;
        public final int[] semitones;
        public final int[] strOff;
        public final int[] fretOff;
        public final int minTier;

        public ChordDef(String shortName, String formula, String fullName,
                        int numNotes, int[] semitones, int[] strOff, int[] fretOff, int minTier) {
            this.shortName = shortName;
            this.formula = formula;
            this.fullName = fullName;
            this.numNotes = numNotes;
            this.semitones = semitones;
            this.strOff = strOff;
            this.fretOff = fretOff;
            this.minTier = minTier;
        }
    }

    /** Exact 12 guitar chord qualities from PowKiddy V90 {@code eartrainer.c}. */
    public static final ChordDef[] CHORDS = {
        new ChordDef("Major", "1-3-5",      "Major Triad",       4, new int[]{0, 4, 7, 12},  new int[]{0, 1, 2, 3}, new int[]{0,  2,  2, 2}, TIER_EASY),
        new ChordDef("Minor", "1-b3-5",     "Minor Triad",       4, new int[]{0, 3, 7, 12},  new int[]{0, 1, 2, 3}, new int[]{0,  2,  2, 1}, TIER_EASY),
        new ChordDef("dim",   "1-b3-b5",    "Diminished Triad",  3, new int[]{0, 3, 6,  0},  new int[]{0, 1, 2, 0}, new int[]{0,  1,  2, 0}, TIER_MEDIUM),
        new ChordDef("aug",   "1-3-#5",     "Augmented Triad",   3, new int[]{0, 4, 8,  0},  new int[]{0, 1, 3, 0}, new int[]{0,  3,  2, 0}, TIER_MEDIUM),
        new ChordDef("sus2",  "1-2-5",      "Suspended 2nd",     4, new int[]{0, 2, 7, 12},  new int[]{0, 1, 2, 3}, new int[]{0,  2,  2, 0}, TIER_EASY),
        new ChordDef("sus4",  "1-4-5",      "Suspended 4th",     4, new int[]{0, 5, 7, 12},  new int[]{0, 1, 2, 3}, new int[]{0,  2,  2, 3}, TIER_EASY),
        new ChordDef("Dom7",  "1-3-5-b7",   "Dominant 7th (7)",  4, new int[]{0, 4, 7, 10},  new int[]{0, 1, 2, 3}, new int[]{0,  2,  0, 2}, TIER_MEDIUM),
        new ChordDef("Maj7",  "1-3-5-7",    "Major 7th",         4, new int[]{0, 4, 7, 11},  new int[]{0, 1, 2, 3}, new int[]{0,  2,  1, 2}, TIER_MEDIUM),
        new ChordDef("m7",    "1-b3-5-b7",  "Minor 7th",         4, new int[]{0, 3, 7, 10},  new int[]{0, 1, 2, 3}, new int[]{0,  2,  0, 1}, TIER_MEDIUM),
        new ChordDef("m7b5",  "1-b3-b5-b7", "Half-Diminished",   4, new int[]{0, 3, 6, 10},  new int[]{0, 1, 2, 3}, new int[]{0,  1,  0, 1}, TIER_ALL_12),
        new ChordDef("dim7",  "1-b3-b5-6",  "Full Diminished 7", 4, new int[]{0, 3, 6,  9},  new int[]{0, 1, 2, 3}, new int[]{0,  1, -1, 1}, TIER_ALL_12),
        new ChordDef("7sus4", "1-4-5-b7",   "Dominant 7sus4",    4, new int[]{0, 5, 7, 10},  new int[]{0, 1, 2, 3}, new int[]{0,  2,  0, 3}, TIER_ALL_12)
    };

    /**
     * Metadata for each of the 12 Scale Degrees / Solfege syllables in {@link #MODE_DEGREES}.
     */
    public static final class ScaleDegreeDef {
        public final String shortName;
        public final String solfege;
        public final String fullName;
        public final String resolutionTendency;
        public final int semitones;
        public final int minTier;

        public ScaleDegreeDef(String shortName, String solfege, String fullName,
                              String resolutionTendency, int semitones, int minTier) {
            this.shortName = shortName;
            this.solfege = solfege;
            this.fullName = fullName;
            this.resolutionTendency = resolutionTendency;
            this.semitones = semitones;
            this.minTier = minTier;
        }
    }

    public static final ScaleDegreeDef[] SCALE_DEGREES = {
        new ScaleDegreeDef("b2", "Ra",  "Flat 2nd (Phrygian)", "Pulls down to 1 (Do)",      1, TIER_ALL_12),
        new ScaleDegreeDef("2",  "Re",  "Supertonic (2nd)",    "Steps to 1 (Do) or 3 (Mi)", 2, TIER_EASY),
        new ScaleDegreeDef("b3", "Me",  "Minor 3rd (b3)",      "Dark minor color tone",     3, TIER_ALL_12),
        new ScaleDegreeDef("3",  "Mi",  "Mediant (Major 3rd)", "Warm major stability",      4, TIER_EASY),
        new ScaleDegreeDef("4",  "Fa",  "Subdominant (4th)",   "Leans down to 3 (Mi)",      5, TIER_EASY),
        new ScaleDegreeDef("#4", "Fi",  "Lydian #4 / b5",      "Pulls up to 5 (Sol)",       6, TIER_ALL_12),
        new ScaleDegreeDef("5",  "Sol", "Dominant (5th)",      "Strong harmonic pillar",    7, TIER_EASY),
        new ScaleDegreeDef("b6", "Le",  "Minor 6th (b6)",      "Pulls down to 5 (Sol)",     8, TIER_ALL_12),
        new ScaleDegreeDef("6",  "La",  "Submediant (6th)",    "Pastoral / relative minor", 9, TIER_MEDIUM),
        new ScaleDegreeDef("b7", "Te",  "Subtonic (Mixolydian)","Bluesy b7 -> 5 or 1",     10, TIER_ALL_12),
        new ScaleDegreeDef("7",  "Ti",  "Leading Tone (7th)",  "Strong pull up to 8 (Do')",11, TIER_MEDIUM),
        new ScaleDegreeDef("8",  "Do'", "Octave Tonic (8th)",  "Complete home resolution", 12, TIER_EASY)
    };

    /**
     * Metadata for each of the 12 Melodic Dictation & Cadence Patterns in {@link #MODE_PROGRESSIONS}.
     */
    public static final class MelodicPatternDef {
        public final String shortName;
        public final String subLabel;
        public final String fullName;
        public final String description;
        /** Semitone offsets of the 3 melodic notes relative to root. */
        public final int[] melodySemitones;
        /** Semitone offsets of the 3 chord roots for cadence playback mode. */
        public final int[] chordRootSemitones;
        /** Chord quality index in {@link #CHORDS} (0 = Major, 1 = Minor, 6 = Dom7) for each step. */
        public final int[] chordQualities;
        public final int minTier;

        public MelodicPatternDef(String shortName, String subLabel, String fullName,
                                 String description, int[] melodySemitones,
                                 int[] chordRootSemitones, int[] chordQualities, int minTier) {
            this.shortName = shortName;
            this.subLabel = subLabel;
            this.fullName = fullName;
            this.description = description;
            this.melodySemitones = melodySemitones;
            this.chordRootSemitones = chordRootSemitones;
            this.chordQualities = chordQualities;
            this.minTier = minTier;
        }
    }

    public static final MelodicPatternDef[] PROGRESSIONS = {
        new MelodicPatternDef("1-2-3",  "I-V-I",    "Major Step Ascent",     "Do-Re-Mi / Authentic I-V-I",      new int[]{0, 2, 4},  new int[]{0, 7, 0},  new int[]{0, 0, 0}, TIER_EASY),
        new MelodicPatternDef("3-2-1",  "I-IV-I",   "Major Step Descent",    "Mi-Re-Do / Plagal I-IV-I",        new int[]{4, 2, 0},  new int[]{0, 5, 0},  new int[]{0, 0, 0}, TIER_EASY),
        new MelodicPatternDef("1-3-5",  "I-IV-V",   "Major Triad Arpeggio",  "Do-Mi-Sol / Open I-IV-V",         new int[]{0, 4, 7},  new int[]{0, 5, 7},  new int[]{0, 0, 0}, TIER_EASY),
        new MelodicPatternDef("5-3-1",  "IV-V-I",   "Major Triad Descent",   "Sol-Mi-Do / Full Cadence IV-V-I", new int[]{7, 4, 0},  new int[]{5, 7, 0},  new int[]{0, 0, 0}, TIER_EASY),
        new MelodicPatternDef("1-b3-5", "i-iv-i",   "Minor Triad Arpeggio",  "Do-Me-Sol / Minor Plagal i-iv-i", new int[]{0, 3, 7},  new int[]{0, 5, 0},  new int[]{1, 1, 1}, TIER_EASY),
        new MelodicPatternDef("5-b3-1", "i-V7-i",   "Minor Triad Descent",   "Sol-Me-Do / Harmonic Min i-V7-i", new int[]{7, 3, 0},  new int[]{0, 7, 0},  new int[]{1, 6, 1}, TIER_MEDIUM),
        new MelodicPatternDef("1-4-5",  "ii-V-I",   "Fourth-Fifth Call",     "Do-Fa-Sol / Jazz Turnaround",     new int[]{0, 5, 7},  new int[]{2, 7, 0},  new int[]{1, 6, 0}, TIER_MEDIUM),
        new MelodicPatternDef("5-4-3",  "I-vi-IV",  "Upper Tetrachord",      "Sol-Fa-Mi / 50s Progression",     new int[]{7, 5, 4},  new int[]{0, 9, 5},  new int[]{0, 1, 0}, TIER_MEDIUM),
        new MelodicPatternDef("5-7-8",  "V-IV-I",   "Leading-Tone Resolution","Sol-Ti-Do' / Blues Rock Cadence",new int[]{7, 11, 12},new int[]{7, 5, 0},  new int[]{0, 0, 0}, TIER_MEDIUM),
        new MelodicPatternDef("1-b3-4", "i-bVII-i", "Minor Pentatonic Cell", "Do-Me-Fa / Aeolian i-bVII-i",     new int[]{0, 3, 5},  new int[]{0, 10, 0}, new int[]{1, 0, 1}, TIER_ALL_12),
        new MelodicPatternDef("1-5-8",  "bVI-bVII-I","Power Fifth + Octave", "Do-Sol-Do' / Epic Mario Cadence", new int[]{0, 7, 12}, new int[]{8, 10, 0}, new int[]{0, 0, 0}, TIER_ALL_12),
        new MelodicPatternDef("8-b7-5", "I-bVII-IV","Mixolydian Rock Line",  "Do'-Te-Sol / Classic Rock Backdoor",new int[]{12, 10, 7},new int[]{0, 10, 5}, new int[]{0, 0, 0}, TIER_ALL_12)
    };

    /**
     * Formats a MIDI note number (e.g. {@code 45} -> {@code "A2"}, {@code 60} -> {@code "C4"}).
     */
    public static String formatMidiNote(int midi) {
        int noteIdx = ((midi % 12) + 12) % 12;
        int octave = (midi / 12) - 1;
        return NOTE_NAMES[noteIdx] + octave;
    }

    /**
     * Returns whether item {@code idx} ({@code 0..11}) is enabled in the given {@code mode} and {@code tier}.
     */
    public static boolean isItemEnabled(int mode, int tier, int idx) {
        if (idx < 0 || idx >= NUM_ITEMS) return false;
        tier = Math.max(TIER_EASY, Math.min(TIER_ALL_12, tier));
        switch (mode) {
            case MODE_INTERVALS:
                return INTERVALS[idx].minTier <= tier;
            case MODE_CHORDS:
                return CHORDS[idx].minTier <= tier;
            case MODE_DEGREES:
                return SCALE_DEGREES[idx].minTier <= tier;
            case MODE_PROGRESSIONS:
                return PROGRESSIONS[idx].minTier <= tier;
            default:
                return INTERVALS[idx].minTier <= tier;
        }
    }

    /**
     * Returns the short button label for item {@code idx} in the given {@code mode}.
     */
    public static String getItemShortName(int mode, int idx) {
        if (idx < 0 || idx >= NUM_ITEMS) return "";
        switch (mode) {
            case MODE_INTERVALS:    return INTERVALS[idx].shortName;
            case MODE_CHORDS:       return CHORDS[idx].shortName;
            case MODE_DEGREES:      return SCALE_DEGREES[idx].shortName + " " + SCALE_DEGREES[idx].solfege;
            case MODE_PROGRESSIONS: return PROGRESSIONS[idx].shortName;
            default:                return INTERVALS[idx].shortName;
        }
    }

    /**
     * Returns the corner badge / sub-label for item {@code idx} in the given {@code mode}.
     */
    public static String getItemSubLabel(int mode, int idx, int style) {
        if (idx < 0 || idx >= NUM_ITEMS) return "";
        switch (mode) {
            case MODE_INTERVALS:    return String.valueOf(INTERVALS[idx].semitones);
            case MODE_CHORDS:       return CHORDS[idx].formula;
            case MODE_DEGREES:      return SCALE_DEGREES[idx].semitones + "st";
            case MODE_PROGRESSIONS: return (style == PROG_STYLE_CHORDS) ? PROGRESSIONS[idx].subLabel : "3-note";
            default:                return "";
        }
    }

    /**
     * Fretboard position output structure for {@link #findGuitarIntervalPos}.
     */
    public static final class FretPos {
        public int stringIdx;
        public int fret;

        public FretPos() {}

        public FretPos(int stringIdx, int fret) {
            this.stringIdx = stringIdx;
            this.fret = fret;
        }
    }

    /**
     * Given a guitar root string ({@code 0..3}), root fret ({@code 1..9}), and interval semitones ({@code 1..12}),
     * finds the most natural playable guitar position ({@code stringIdx, fret}) on frets {@code 0..12}.
     * Exact translation of {@code find_guitar_interval_pos()} from {@code eartrainer.c}.
     */
    public static void findGuitarIntervalPos(int rootStr, int rootFret, int semitones, FretPos out) {
        rootStr = Math.max(0, Math.min(5, rootStr));
        int targetMidi = OPEN_STRING_MIDI[rootStr] + rootFret + semitones;
        int bestStr = rootStr;
        int bestFret = rootFret + semitones;
        int bestCost = 9999;

        for (int s = rootStr; s < 6; s++) {
            int f = targetMidi - OPEN_STRING_MIDI[s];
            if (f < 0 || f > 12) continue;
            int fretDist = Math.abs(f - rootFret);
            int strDist = s - rootStr;
            /* Prefer notes within a 3-fret box on adjacent strings (classic CAGED shapes) */
            int cost = fretDist * 3 + strDist * 2;
            if (s == rootStr && semitones > 4) cost += 10;
            if (cost < bestCost) {
                bestCost = cost;
                bestStr = s;
                bestFret = f;
            }
        }
        out.stringIdx = bestStr;
        out.fret = bestFret;
    }

    /**
     * Returns the MIDI pitches highlighted by item {@code itemIdx} in {@code mode} starting at {@code rootMidi},
     * used to illuminate both the 6-string guitar fretboard and the interactive piano keyboard.
     */
    public static int[] getItemMidiNotes(int mode, int itemIdx, int rootMidi) {
        itemIdx = Math.max(0, Math.min(NUM_ITEMS - 1, itemIdx));
        switch (mode) {
            case MODE_INTERVALS: {
                int semi = INTERVALS[itemIdx].semitones;
                return new int[]{ rootMidi, rootMidi + semi };
            }
            case MODE_CHORDS: {
                ChordDef cd = CHORDS[itemIdx];
                int[] notes = new int[cd.numNotes];
                for (int n = 0; n < cd.numNotes; n++) {
                    notes[n] = rootMidi + cd.semitones[n];
                }
                return notes;
            }
            case MODE_DEGREES: {
                int semi = SCALE_DEGREES[itemIdx].semitones;
                return new int[]{ rootMidi, rootMidi + semi };
            }
            case MODE_PROGRESSIONS: {
                MelodicPatternDef mp = PROGRESSIONS[itemIdx];
                int[] notes = new int[mp.melodySemitones.length];
                for (int n = 0; n < mp.melodySemitones.length; n++) {
                    notes[n] = rootMidi + mp.melodySemitones[n];
                }
                return notes;
            }
            default:
                return new int[]{ rootMidi };
        }
    }

    /**
     * Stateful ear-training session & question manager that implements the 3-guess rule,
     * question generation, hint elimination, streak tracking, and lifetime per-item accuracy.
     * Pure Java with zero Android dependencies so it can be unit-tested on the host JVM.
     */
    public static final class TrainerSession {
        public int mode = MODE_INTERVALS;
        public int tier = TIER_ALL_12;
        public int intStyle = INT_STYLE_ASC;
        public int chdStyle = CHD_STYLE_ARP_STRUM;
        public int degStyle = DEG_STYLE_CADENCE_NOTE;
        public int progStyle = PROG_STYLE_MELODIC;

        /* Current question state */
        public int rootStr = 0;       /* 0..2 (E, A, D strings) */
        public int rootFret = 5;      /* 1..7 */
        public int rootMidi = 45;     /* A2 */
        public int correctIdx = 6;    /* 0..11 */
        public boolean curIsDesc = false;
        public int guessesLeft = MAX_GUESSES;
        public final boolean[] wrongGuessed = new boolean[NUM_ITEMS];
        public boolean revealed = false;
        public boolean solved = false;
        public int auditionIdx = 6;
        public int cursor = 0;

        /* Session & Lifetime Stats */
        public int streak = 0;
        public int bestStreak = 0;
        public int sessionOk = 0;
        public int sessionTot = 0;

        public final int[][] itemOk  = new int[NUM_MODES][NUM_ITEMS];
        public final int[][] itemTot = new int[NUM_MODES][NUM_ITEMS];

        private final Random mRandom;

        public TrainerSession() {
            this(new Random());
        }

        public TrainerSession(Random random) {
            this.mRandom = random;
        }

        public boolean isEnabled(int idx) {
            return isItemEnabled(mode, tier, idx);
        }

        public String getCurrentStyleLabel() {
            switch (mode) {
                case MODE_INTERVALS:    return INT_STYLE_NAMES[intStyle % INT_STYLE_NAMES.length];
                case MODE_CHORDS:       return CHD_STYLE_NAMES[chdStyle % CHD_STYLE_NAMES.length];
                case MODE_DEGREES:      return DEG_STYLE_NAMES[degStyle % DEG_STYLE_NAMES.length];
                case MODE_PROGRESSIONS: return PROG_STYLE_NAMES[progStyle % PROG_STYLE_NAMES.length];
                default:                return INT_STYLE_NAMES[0];
            }
        }

        public void cycleStyle() {
            switch (mode) {
                case MODE_INTERVALS:
                    intStyle = (intStyle + 1) % INT_STYLE_NAMES.length;
                    if (intStyle == INT_STYLE_DESC) curIsDesc = true;
                    else if (intStyle == INT_STYLE_ASC) curIsDesc = false;
                    break;
                case MODE_CHORDS:
                    chdStyle = (chdStyle + 1) % CHD_STYLE_NAMES.length;
                    break;
                case MODE_DEGREES:
                    degStyle = (degStyle + 1) % DEG_STYLE_NAMES.length;
                    break;
                case MODE_PROGRESSIONS:
                    progStyle = (progStyle + 1) % PROG_STYLE_NAMES.length;
                    break;
            }
        }

        public void cycleTier() {
            tier = (tier + 1) % 3;
            generateNewQuestion();
        }

        public void cycleMode(int delta) {
            mode = ((mode + delta) % NUM_MODES + NUM_MODES) % NUM_MODES;
            generateNewQuestion();
        }

        /**
         * Generates a new ear-training question for the active {@link #mode} and {@link #tier}.
         */
        public void generateNewQuestion() {
            if (mode == MODE_CHORDS) {
                rootStr = 1; /* 5th string (A string) root for clean 4-string chord shapes */
                rootFret = 2 + mRandom.nextInt(6);
            } else {
                rootStr = mRandom.nextInt(3);
                rootFret = 1 + mRandom.nextInt(7);
            }
            rootMidi = OPEN_STRING_MIDI[rootStr] + rootFret;

            int[] candidates = new int[NUM_ITEMS];
            int count = 0;
            for (int i = 0; i < NUM_ITEMS; i++) {
                if (isEnabled(i)) {
                    candidates[count++] = i;
                }
            }
            if (count == 0) {
                correctIdx = 0;
            } else {
                int pick = candidates[mRandom.nextInt(count)];
                if (count > 1 && pick == correctIdx) {
                    pick = candidates[mRandom.nextInt(count)];
                }
                correctIdx = pick;
            }

            if (mode == MODE_INTERVALS) {
                if (intStyle == INT_STYLE_DESC) {
                    curIsDesc = true;
                } else if (intStyle == INT_STYLE_MIX) {
                    curIsDesc = mRandom.nextBoolean();
                } else {
                    curIsDesc = false;
                }
            } else {
                curIsDesc = false;
            }

            guessesLeft = MAX_GUESSES;
            for (int i = 0; i < NUM_ITEMS; i++) {
                wrongGuessed[i] = false;
            }
            revealed = false;
            solved = false;
            auditionIdx = correctIdx;

            if (!isEnabled(cursor)) {
                for (int i = 0; i < NUM_ITEMS; i++) {
                    if (isEnabled(i)) {
                        cursor = i;
                        break;
                    }
                }
            }
        }

        /**
         * Eliminates up to two remaining wrong answers as a hint without costing a guess try.
         * Returns the number of wrong choices eliminated.
         */
        public int useHintEliminateWrong() {
            if (revealed) return 0;
            int[] availWrong = new int[NUM_ITEMS];
            int count = 0;
            for (int i = 0; i < NUM_ITEMS; i++) {
                if (isEnabled(i) && i != correctIdx && !wrongGuessed[i]) {
                    availWrong[count++] = i;
                }
            }
            int toElim = Math.min(2, Math.max(0, count - 1));
            int eliminated = 0;
            for (int k = 0; k < toElim && count > 0; k++) {
                int pickIdx = mRandom.nextInt(count);
                int item = availWrong[pickIdx];
                wrongGuessed[item] = true;
                availWrong[pickIdx] = availWrong[--count];
                eliminated++;
            }
            return eliminated;
        }

        /** Outcome codes returned by {@link #submitGuess(int)}. */
        public static final int GUESS_IGNORED        = 0;
        public static final int GUESS_CORRECT        = 1;
        public static final int GUESS_WRONG_RETRY    = 2;
        public static final int GUESS_WRONG_REVEALED = 3;
        public static final int GUESS_NEXT_QUESTION  = 4;

        /**
         * Submits a guess for grid item {@code idx} ({@code 0..11}), or advances to the
         * next question if {@link #revealed} is already {@code true}.
         */
        public int submitGuess(int idx) {
            if (revealed) {
                generateNewQuestion();
                return GUESS_NEXT_QUESTION;
            }
            if (!isEnabled(idx) || wrongGuessed[idx]) {
                return GUESS_IGNORED;
            }

            cursor = idx;
            int m = Math.max(0, Math.min(NUM_MODES - 1, mode));

            if (idx == correctIdx) {
                solved = true;
                revealed = true;
                auditionIdx = correctIdx;
                streak++;
                if (streak > bestStreak) {
                    bestStreak = streak;
                }
                sessionOk++;
                sessionTot++;
                itemOk[m][correctIdx]++;
                itemTot[m][correctIdx]++;
                return GUESS_CORRECT;
            } else {
                wrongGuessed[idx] = true;
                guessesLeft--;
                if (guessesLeft <= 0) {
                    solved = false;
                    revealed = true;
                    auditionIdx = correctIdx;
                    streak = 0;
                    sessionTot++;
                    itemTot[m][correctIdx]++;
                    return GUESS_WRONG_REVEALED;
                } else {
                    return GUESS_WRONG_RETRY;
                }
            }
        }

        public int getSessionAccuracyPercent() {
            return (sessionTot > 0) ? ((sessionOk * 100) / sessionTot) : 100;
        }
    }
}
