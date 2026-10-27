package com.eggwall.fretboard;

import java.util.Locale;

/**
 * Pure, stateless guitar fretboard, CAGED scale, chord/arpeggio voicing, reverse chord
 * identifier, and note/interval quiz theory engine.
 *
 * <p>Has zero Android UI dependencies so 100% of the music theory, CAGED position ranges,
 * chord voicings across all 6 tunings, reverse chord identification, and quiz validation
 * can be unit-tested on a standard JVM via JUnit 4 ({@code FretboardTheoryTest}).
 *
 * <p><b>Engineering Extension Points:</b>
 * <ul>
 *   <li><b>Guitar Tunings:</b> Add or modify entries in {@link #TUNINGS} (supports Standard E,
 *       Drop D, Eb Standard, D Standard, DADGAD, and Open G out of the box).</li>
 *   <li><b>Scales & Modes:</b> Add or modify entries in {@link #SCALES} using the 12-semitone
 *       degree role mask ({@code 4=Root}, {@code 3=Color/Blue note}, {@code 2=Chord tone},
 *       {@code 1=Scale tone}, {@code 0=Out of scale}).</li>
 *   <li><b>Chord & Arpeggio Qualities:</b> Add or modify entries in {@link #CHORDS}.</li>
 *   <li><b>Reverse Chord Templates:</b> Extend {@link #CHORD_TEMPLATES} to recognize additional
 *       extended or altered voicings in Explorer mode.</li>
 * </ul>
 */
public final class FretboardTheory {

    private FretboardTheory() {}

    public static final int NUM_STRINGS = 6;
    public static final int MAX_FRET    = 15;
    public static final int NUM_FRETS   = 16; /* Frets 0 .. 15 */

    /* Application operating modes */
    public static final int MODE_SCALES   = 0;
    public static final int MODE_CHORDS   = 1;
    public static final int MODE_EXPLORER = 2;

    /* Fretboard dot label modes */
    public static final int LABEL_INTERVALS = 0;
    public static final int LABEL_NOTES     = 1;
    public static final int LABEL_DEGREES   = 2;

    /* Interactive Fretboard Quiz modes */
    public static final int QUIZ_OFF       = 0;
    public static final int QUIZ_NOTES     = 1;
    public static final int QUIZ_INTERVALS = 2;

    /** Chromatic pitch-class names (0 = C .. 11 = B). */
    public static final String[] NOTE_NAMES = {
        "C", "Db", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B"
    };

    /** Semitone interval labels relative to the active root (0 = R .. 11 = 7). */
    public static final String[] INTERVAL_LABELS = {
        "R", "b2", "2", "b3", "3", "4", "b5", "5", "b6", "6", "b7", "7"
    };

    /** Scale degree labels relative to the active root (0 = 1 .. 11 = 7). */
    public static final String[] DEGREE_LABELS = {
        "1", "b2", "2", "b3", "3", "4", "#4", "5", "b6", "6", "b7", "7"
    };

    /** CAGED position names (0 = ALL NECK, 1..5 = POS 1..5). */
    public static final String[] CAGED_POS_NAMES = {
        "ALL NECK",
        "POS 1 (E)",
        "POS 2 (D)",
        "POS 3 (C)",
        "POS 4 (A)",
        "POS 5 (G)"
    };

    /** CAGED box start offsets relative to the 6th-string root fret. */
    public static final int[] CAGED_REL_START = { 0, 2, 4, 7, 9 };

    /**
     * Definition of a 6-string guitar tuning.
     * {@code midi[0]} is the lowest-pitched 6th string; {@code midi[5]} is the highest 1st string.
     * {@code rowLabels[0]} is the top display row (1st string) down to {@code rowLabels[5]} (6th string).
     */
    public static final class GuitarTuning {
        public final String name;
        public final String shortName;
        public final int[] midi;
        public final String[] rowLabels;

        public GuitarTuning(String name, String shortName, int[] midi, String[] rowLabels) {
            this.name = name;
            this.shortName = shortName;
            this.midi = midi;
            this.rowLabels = rowLabels;
        }
    }

    /** 6 built-in guitar tunings. */
    public static final GuitarTuning[] TUNINGS = {
        new GuitarTuning(
            "Standard (E A D G B e)", "Std E",
            new int[]{ 40, 45, 50, 55, 59, 64 },
            new String[]{ "e", "B", "G", "D", "A", "E" }
        ),
        new GuitarTuning(
            "Drop D (D A D G B e)", "Drop D",
            new int[]{ 38, 45, 50, 55, 59, 64 },
            new String[]{ "e", "B", "G", "D", "A", "D" }
        ),
        new GuitarTuning(
            "Eb Standard (Eb Ab Db Gb Bb eb)", "Eb Std",
            new int[]{ 39, 44, 49, 54, 58, 63 },
            new String[]{ "eb", "Bb", "Gb", "Db", "Ab", "Eb" }
        ),
        new GuitarTuning(
            "D Standard (D G C F A d)", "D Std",
            new int[]{ 38, 43, 48, 53, 57, 62 },
            new String[]{ "d", "A", "F", "C", "G", "D" }
        ),
        new GuitarTuning(
            "DADGAD (D A D G A d)", "DADGAD",
            new int[]{ 38, 45, 50, 55, 57, 62 },
            new String[]{ "d", "A", "G", "D", "A", "D" }
        ),
        new GuitarTuning(
            "Open G (D G D G B d)", "Open G",
            new int[]{ 38, 43, 50, 55, 59, 62 },
            new String[]{ "d", "B", "G", "D", "G", "D" }
        )
    };

    /**
     * Definition of a guitar scale or mode.
     * {@code mask[0..11]} encodes the role of each semitone above the root:
     * <ul>
     *   <li>{@code 0}: Not in scale</li>
     *   <li>{@code 1}: Regular scale tone (2, 4, 6)</li>
     *   <li>{@code 2}: Chord-skeleton tone (3, b3, 5, 7, b7)</li>
     *   <li>{@code 3}: Characteristic color / blue note (b5, Dorian 6, Lydian #4, Phrygian b2, etc.)</li>
     *   <li>{@code 4}: Root note (interval 0)</li>
     * </ul>
     */
    public static final class ScaleDef {
        public final String name;
        public final String shortName;
        public final String formula;
        public final String skeletonChord;
        public final int[] mask;

        public ScaleDef(String name, String shortName, String formula, String skeletonChord, int[] mask) {
            this.name = name;
            this.shortName = shortName;
            this.formula = formula;
            this.skeletonChord = skeletonChord;
            this.mask = mask;
        }
    }

    /** 14 guitar scales and modes. */
    public static final ScaleDef[] SCALES = {
        new ScaleDef(
            "Minor Pentatonic", "Min Pent",
            "1 - b3 - 4 - 5 - b7", "m7 / 7",
            new int[]{ 4, 0, 0, 2, 0, 1, 0, 2, 0, 0, 2, 0 }
        ),
        new ScaleDef(
            "Blues Scale", "Blues",
            "1 - b3 - 4 - b5 - 5 - b7", "7 / m7 (b5 Blue)",
            new int[]{ 4, 0, 0, 2, 0, 1, 3, 2, 0, 0, 2, 0 }
        ),
        new ScaleDef(
            "Major Pentatonic", "Maj Pent",
            "1 - 2 - 3 - 5 - 6", "Maj / 6 / Maj7",
            new int[]{ 4, 0, 1, 0, 2, 0, 0, 2, 0, 3, 0, 0 }
        ),
        new ScaleDef(
            "Natural Major (Ionian)", "Major",
            "1 - 2 - 3 - 4 - 5 - 6 - 7", "Maj7 (I - IV - V)",
            new int[]{ 4, 0, 1, 0, 2, 1, 0, 2, 0, 1, 0, 2 }
        ),
        new ScaleDef(
            "Natural Minor (Aeolian)", "Nat Minor",
            "1 - 2 - b3 - 4 - 5 - b6 - b7", "m7 (i - iv - v)",
            new int[]{ 4, 0, 1, 2, 0, 1, 0, 2, 3, 0, 2, 0 }
        ),
        new ScaleDef(
            "Dorian Mode", "Dorian",
            "1 - 2 - b3 - 4 - 5 - 6 - b7", "m7 / m6 (Nat 6)",
            new int[]{ 4, 0, 1, 2, 0, 1, 0, 2, 0, 3, 2, 0 }
        ),
        new ScaleDef(
            "Mixolydian Mode", "Mixolydian",
            "1 - 2 - 3 - 4 - 5 - 6 - b7", "Dom 7 (Flat 7)",
            new int[]{ 4, 0, 1, 0, 2, 1, 0, 2, 0, 1, 3, 0 }
        ),
        new ScaleDef(
            "Lydian Mode", "Lydian",
            "1 - 2 - 3 - #4 - 5 - 6 - 7", "Maj7#11 (#4)",
            new int[]{ 4, 0, 1, 0, 2, 0, 3, 2, 0, 1, 0, 2 }
        ),
        new ScaleDef(
            "Phrygian Mode", "Phrygian",
            "1 - b2 - b3 - 4 - 5 - b6 - b7", "m7 / susb9 (b2)",
            new int[]{ 4, 3, 0, 2, 0, 1, 0, 2, 1, 0, 2, 0 }
        ),
        new ScaleDef(
            "Harmonic Minor", "Harm Minor",
            "1 - 2 - b3 - 4 - 5 - b6 - 7", "m(maj7) (Lead 7)",
            new int[]{ 4, 0, 1, 2, 0, 1, 0, 2, 1, 0, 0, 3 }
        ),
        new ScaleDef(
            "Melodic Minor (Jazz)", "Mel Minor",
            "1 - 2 - b3 - 4 - 5 - 6 - 7", "m(maj7) / m6",
            new int[]{ 4, 0, 1, 2, 0, 1, 0, 2, 0, 3, 0, 3 }
        ),
        new ScaleDef(
            "Phrygian Dominant", "Phryg Dom",
            "1 - b2 - 3 - 4 - 5 - b6 - b7", "7b9 (Flamenco/Metal)",
            new int[]{ 4, 3, 0, 0, 2, 1, 0, 2, 1, 0, 2, 0 }
        ),
        new ScaleDef(
            "Locrian Mode", "Locrian",
            "1 - b2 - b3 - 4 - b5 - b6 - b7", "m7b5 (Half-Dim)",
            new int[]{ 4, 1, 0, 2, 0, 1, 3, 0, 1, 0, 2, 0 }
        ),
        new ScaleDef(
            "Whole-Half Diminished", "Diminished",
            "1 - 2 - b3 - 4 - b5 - b6 - 6 - 7", "dim7 (Symmetrical)",
            new int[]{ 4, 0, 1, 2, 0, 1, 3, 0, 1, 2, 0, 3 }
        )
    };

    /** Definition of a guitar chord / arpeggio quality. */
    public static final class ChordQualityDef {
        public final String name;
        public final String suffix;
        public final String formula;
        public final int[] mask;
        public final int prefBassStr;

        public ChordQualityDef(String name, String suffix, String formula, int[] mask, int prefBassStr) {
            this.name = name;
            this.suffix = suffix;
            this.formula = formula;
            this.mask = mask;
            this.prefBassStr = prefBassStr;
        }
    }

    /** 12 guitar chord and arpeggio qualities. */
    public static final ChordQualityDef[] CHORDS = {
        new ChordQualityDef("Major Triad",     "",     "1 - 3 - 5",        new int[]{ 4,0,0,0,2,0,0,2,0,0,0,0 }, 0),
        new ChordQualityDef("Minor Triad",     "m",    "1 - b3 - 5",       new int[]{ 4,0,0,2,0,0,0,2,0,0,0,0 }, 0),
        new ChordQualityDef("Dominant 7th",    "7",    "1 - 3 - 5 - b7",   new int[]{ 4,0,0,0,2,0,0,2,0,0,3,0 }, 0),
        new ChordQualityDef("Major 7th",       "maj7", "1 - 3 - 5 - 7",    new int[]{ 4,0,0,0,2,0,0,2,0,0,0,3 }, 0),
        new ChordQualityDef("Minor 7th",       "m7",   "1 - b3 - 5 - b7",  new int[]{ 4,0,0,2,0,0,0,2,0,0,3,0 }, 0),
        new ChordQualityDef("Half-Diminished", "m7b5", "1 - b3 - b5 - b7", new int[]{ 4,0,0,2,0,0,3,0,0,0,2,0 }, 1),
        new ChordQualityDef("Diminished 7th",  "dim7", "1 - b3 - b5 - 6",  new int[]{ 4,0,0,2,0,0,3,0,0,2,0,0 }, 1),
        new ChordQualityDef("Suspended 2nd",   "sus2", "1 - 2 - 5",        new int[]{ 4,0,3,0,0,0,0,2,0,0,0,0 }, 1),
        new ChordQualityDef("Suspended 4th",   "sus4", "1 - 4 - 5",        new int[]{ 4,0,0,0,0,3,0,2,0,0,0,0 }, 0),
        new ChordQualityDef("Major Add 9",     "add9", "1 - 2 - 3 - 5",    new int[]{ 4,0,3,0,2,0,0,2,0,0,0,0 }, 0),
        new ChordQualityDef("Major 6th",       "6",    "1 - 3 - 5 - 6",    new int[]{ 4,0,0,0,2,0,0,2,0,3,0,0 }, 0),
        new ChordQualityDef("Minor 6th",       "m6",   "1 - b3 - 5 - 6",   new int[]{ 4,0,0,2,0,0,0,2,0,3,0,0 }, 0)
    };

    /** Computed 6-string guitar chord voicing for a given root, quality, CAGED position, and tuning. */
    public static final class ComputedVoicing {
        public final int[] frets = new int[NUM_STRINGS];   /* -1 = muted ('X'), 0..15 = fretted/open */
        public final int[] fingers = new int[NUM_STRINGS]; /* 0 = open/muted, 1..4 = finger number */
        public int barreFret;
        public int barreLowStr;
        public int baseFret;
        public int maxFret;
    }

    /** Result of reverse chord identification in Explorer mode. */
    public static final class IdentifiedChord {
        public String chordName = "";
        public String notesStr = "";
    }

    /** Converts a MIDI note number to frequency in Hz (A4 = MIDI 69 = 440.0 Hz). */
    public static double midiToFreq(int midiNote) {
        return 440.0 * Math.pow(2.0, (double) (midiNote - 69) / 12.0);
    }

    /** Returns the MIDI pitch of {@code (stringIdx, fret)} in the given tuning. */
    public static int getMidiNote(int tuningIdx, int stringIdx, int fret) {
        GuitarTuning tn = TUNINGS[clampIndex(tuningIdx, TUNINGS.length)];
        int s = clampIndex(stringIdx, NUM_STRINGS);
        return tn.midi[s] + Math.max(0, fret);
    }

    /** Returns the pitch class (0..11) of {@code (stringIdx, fret)} in the given tuning. */
    public static int getPitchClass(int tuningIdx, int stringIdx, int fret) {
        return ((getMidiNote(tuningIdx, stringIdx, fret) % 12) + 12) % 12;
    }

    /**
     * Computes the inclusive fret range {@code [lowFret, highFret]} for a CAGED box
     * ({@code cagedPos = 0} for ALL NECK, or {@code 1..5} for POS 1..5) in the given tuning.
     */
    public static int[] computeCagedBoxRange(int tuningIdx, int rootPc, int cagedPos) {
        rootPc = ((rootPc % 12) + 12) % 12;
        if (cagedPos <= 0) {
            return new int[]{ 0, MAX_FRET };
        }
        GuitarTuning tn = TUNINGS[clampIndex(tuningIdx, TUNINGS.length)];
        int openLowPc = ((tn.midi[0] % 12) + 12) % 12;
        int rootFret6 = (rootPc - openLowPc + 12) % 12;
        int posIdx = clampIndex(cagedPos - 1, CAGED_REL_START.length);
        int start = rootFret6 + CAGED_REL_START[posIdx];
        while (start > 11) {
            start -= 12;
        }
        if (start < 0) start = 0;
        int low = (start > 0) ? (start - 1) : 0;
        int high = low + 4;
        if (high > MAX_FRET) high = MAX_FRET;
        return new int[]{ low, high };
    }

    /**
     * Computes a playable 6-string guitar chord voicing for any root key (0..11),
     * chord quality (0..11), CAGED voicing position (0..4), and guitar tuning (0..5).
     */
    public static void computeChordVoicing(
            int tuningIdx, int rootPc, int chordIdx, int voicingIdx, ComputedVoicing out) {
        rootPc = ((rootPc % 12) + 12) % 12;
        ChordQualityDef cd = CHORDS[clampIndex(chordIdx, CHORDS.length)];
        GuitarTuning tn = TUNINGS[clampIndex(tuningIdx, TUNINGS.length)];
        int vIdx = ((voicingIdx % 5) + 5) % 5;

        int openS0Pc = tn.midi[0] % 12;
        int openS1Pc = tn.midi[1] % 12;
        int rootF6 = (rootPc - openS0Pc + 12) % 12;
        int rootF5 = (rootPc - openS1Pc + 12) % 12;

        int[] anchors = new int[5];
        int[] bassStr = new int[5];
        anchors[0] = rootF6;                           bassStr[0] = 0;
        anchors[1] = (rootF6 + 2) % 12;                bassStr[1] = 2;
        anchors[2] = rootF5;                           bassStr[2] = 1;
        anchors[3] = (rootF5 + 3) % 12;                bassStr[3] = 0;
        anchors[4] = rootF6 + ((rootF6 <= 3) ? 12 : 7); bassStr[4] = 0;

        /* Sort the 5 anchors by fret so cycling walks smoothly up the neck from nut to fret 15 */
        for (int i = 0; i < 5; i++) {
            for (int j = i + 1; j < 5; j++) {
                if (anchors[j] < anchors[i]) {
                    int tmpA = anchors[i]; anchors[i] = anchors[j]; anchors[j] = tmpA;
                    int tmpB = bassStr[i]; bassStr[i] = bassStr[j]; bassStr[j] = tmpB;
                }
            }
        }
        for (int i = 1; i < 5; i++) {
            if (anchors[i] <= anchors[i - 1]) {
                anchors[i] = anchors[i - 1] + 2;
            }
            if (anchors[i] > 12) anchors[i] = 12;
        }

        int winLow = anchors[vIdx];
        int minStr = bassStr[vIdx];
        if (cd.prefBassStr > minStr && winLow > 0) {
            minStr = cd.prefBassStr;
        }
        int winHigh = winLow + 3;
        if (winLow == 0) winHigh = 3;
        if (winHigh > MAX_FRET) winHigh = MAX_FRET;

        for (int s = 0; s < NUM_STRINGS; s++) {
            out.frets[s] = -1;
            out.fingers[s] = 0;
        }

        /* Place the bass root on the lowest active string if possible */
        boolean bassPlaced = false;
        for (int s = minStr; s <= 2; s++) {
            for (int f = winLow; f <= winHigh + 1 && f <= MAX_FRET; f++) {
                int pc = (tn.midi[s] + f) % 12;
                if (pc == rootPc) {
                    out.frets[s] = f;
                    minStr = s;
                    bassPlaced = true;
                    break;
                }
            }
            if (bassPlaced) break;
        }

        /* Fill remaining strings from minStr..5 with the closest chord tone in [winLow-1 .. winHigh+1] */
        int searchLow = Math.max(0, winLow - 1);
        for (int s = minStr; s < NUM_STRINGS; s++) {
            if (out.frets[s] >= 0) continue;
            int bestF = -1;
            int bestScore = -999;
            for (int f = searchLow; f <= winHigh + 1 && f <= MAX_FRET; f++) {
                int pc = (tn.midi[s] + f) % 12;
                int rel = (pc - rootPc + 12) % 12;
                int m = cd.mask[rel];
                if (m > 0) {
                    int score = m * 10 - Math.abs(f - (winLow + 1)) * 3;
                    if (s >= 2 && (m == 2 || m == 3)) score += 12;
                    if (score > bestScore) {
                        bestScore = score;
                        bestF = f;
                    }
                }
            }
            out.frets[s] = bestF;
        }

        /* Compute min/max fretted (>0) and finger numbers 1..4 */
        int minF = 99, maxF = 0, minFCount = 0, minFLowStr = 6;
        for (int s = 0; s < NUM_STRINGS; s++) {
            int f = out.frets[s];
            if (f > 0) {
                if (f < minF) {
                    minF = f;
                    minFCount = 1;
                    minFLowStr = s;
                } else if (f == minF) {
                    minFCount++;
                    if (s < minFLowStr) minFLowStr = s;
                }
                if (f > maxF) maxF = f;
            }
        }
        if (minF == 99) {
            minF = 0;
            maxF = 3;
        }

        out.baseFret = (minF > 0) ? minF : 1;
        out.maxFret = (maxF > out.baseFret) ? maxF : (out.baseFret + 3);
        out.barreFret = (minF > 0 && minFCount >= 2) ? minF : 0;
        out.barreLowStr = minFLowStr;

        int nextFinger = (out.barreFret > 0) ? 2 : 1;
        for (int f = minF; f <= maxF && f > 0; f++) {
            for (int s = 0; s < NUM_STRINGS; s++) {
                if (out.frets[s] == f) {
                    if (f == out.barreFret) {
                        out.fingers[s] = 1;
                    } else {
                        out.fingers[s] = (nextFinger <= 4) ? nextFinger : 4;
                        if (nextFinger < 4) nextFinger++;
                    }
                }
            }
        }
    }

    private static final class ChordTemplate {
        final String suffix;
        final int reqMask;
        final int allowMask;
        final int bonus;

        ChordTemplate(String suffix, int reqMask, int allowMask, int bonus) {
            this.suffix = suffix;
            this.reqMask = reqMask;
            this.allowMask = allowMask;
            this.bonus = bonus;
        }
    }

    private static final ChordTemplate[] CHORD_TEMPLATES = {
        new ChordTemplate("",        (1<<0)|(1<<4)|(1<<7),                (1<<0)|(1<<4)|(1<<7),                30),
        new ChordTemplate("m",       (1<<0)|(1<<3)|(1<<7),                (1<<0)|(1<<3)|(1<<7),                30),
        new ChordTemplate("5",       (1<<0)|(1<<7),                       (1<<0)|(1<<7),                       24),
        new ChordTemplate("7",       (1<<0)|(1<<4)|(1<<10),               (1<<0)|(1<<4)|(1<<7)|(1<<10),        34),
        new ChordTemplate("maj7",    (1<<0)|(1<<4)|(1<<11),               (1<<0)|(1<<4)|(1<<7)|(1<<11),        34),
        new ChordTemplate("m7",      (1<<0)|(1<<3)|(1<<10),               (1<<0)|(1<<3)|(1<<7)|(1<<10),        34),
        new ChordTemplate("m(maj7)", (1<<0)|(1<<3)|(1<<11),               (1<<0)|(1<<3)|(1<<7)|(1<<11),        32),
        new ChordTemplate("sus2",    (1<<0)|(1<<2)|(1<<7),                (1<<0)|(1<<2)|(1<<7),                28),
        new ChordTemplate("sus4",    (1<<0)|(1<<5)|(1<<7),                (1<<0)|(1<<5)|(1<<7),                28),
        new ChordTemplate("7sus4",   (1<<0)|(1<<5)|(1<<10),               (1<<0)|(1<<5)|(1<<7)|(1<<10),        32),
        new ChordTemplate("add9",    (1<<0)|(1<<2)|(1<<4),                (1<<0)|(1<<2)|(1<<4)|(1<<7),         33),
        new ChordTemplate("m(add9)", (1<<0)|(1<<2)|(1<<3),                (1<<0)|(1<<2)|(1<<3)|(1<<7),         35),
        new ChordTemplate("6",       (1<<0)|(1<<4)|(1<<9),                (1<<0)|(1<<4)|(1<<7)|(1<<9),         31),
        new ChordTemplate("m6",      (1<<0)|(1<<3)|(1<<9),                (1<<0)|(1<<3)|(1<<7)|(1<<9),         31),
        new ChordTemplate("m7b5",    (1<<0)|(1<<3)|(1<<6)|(1<<10),        (1<<0)|(1<<3)|(1<<6)|(1<<10),        36),
        new ChordTemplate("dim7",    (1<<0)|(1<<3)|(1<<6)|(1<<9),         (1<<0)|(1<<3)|(1<<6)|(1<<9),         36),
        new ChordTemplate("dim",     (1<<0)|(1<<3)|(1<<6),                (1<<0)|(1<<3)|(1<<6),                28),
        new ChordTemplate("aug",     (1<<0)|(1<<4)|(1<<8),                (1<<0)|(1<<4)|(1<<8),                28),
        new ChordTemplate("9",       (1<<0)|(1<<2)|(1<<4)|(1<<10),        (1<<0)|(1<<2)|(1<<4)|(1<<7)|(1<<10), 36),
        new ChordTemplate("m9",      (1<<0)|(1<<2)|(1<<3)|(1<<10),        (1<<0)|(1<<2)|(1<<3)|(1<<7)|(1<<10), 36),
        new ChordTemplate("maj9",    (1<<0)|(1<<2)|(1<<4)|(1<<11),        (1<<0)|(1<<2)|(1<<4)|(1<<7)|(1<<11), 36)
    };

    /**
     * Reverse Chord Identifier for Explorer mode: given 6 locked frets ({@code -1} if unfretted,
     * {@code 0..15} if locked), determines the chord symbol (including slash inversions like
     * {@code G/B} or extended chords like {@code Em(add9)}) and spelled note list.
     */
    public static void identifyLockedChord(int tuningIdx, int[] lockedFrets, IdentifiedChord out) {
        GuitarTuning tn = TUNINGS[clampIndex(tuningIdx, TUNINGS.length)];
        int pcMask = 0;
        int bassPc = -1;
        int bassMidi = 999;
        int noteCnt = 0;

        StringBuilder notesSb = new StringBuilder();
        for (int s = 0; s < NUM_STRINGS && s < lockedFrets.length; s++) {
            int f = lockedFrets[s];
            if (f >= 0) {
                int midi = tn.midi[s] + f;
                int pc = midi % 12;
                pcMask |= (1 << pc);
                if (midi < bassMidi) {
                    bassMidi = midi;
                    bassPc = pc;
                }
                if (noteCnt > 0) {
                    notesSb.append(" - ");
                }
                notesSb.append(NOTE_NAMES[pc]);
                noteCnt++;
            }
        }

        if (noteCnt == 0) {
            out.chordName = "No Locked Notes (Tap [LOCK] or Fret)";
            out.notesStr = "Tap any fret to inspect & pluck";
            return;
        }
        out.notesStr = notesSb.toString();
        if (noteCnt == 1) {
            out.chordName = NOTE_NAMES[bassPc] + " (Single Note)";
            return;
        }

        int bestScore = -999;
        int bestRoot = bassPc;
        String bestSuf = "";

        for (int r = 0; r < 12; r++) {
            if ((pcMask & (1 << r)) == 0) continue;
            int relMask = 0;
            for (int p = 0; p < 12; p++) {
                if ((pcMask & (1 << p)) != 0) {
                    relMask |= (1 << ((p - r + 12) % 12));
                }
            }
            for (ChordTemplate tpl : CHORD_TEMPLATES) {
                if ((relMask & tpl.reqMask) == tpl.reqMask && (relMask & ~tpl.allowMask) == 0) {
                    int score = tpl.bonus + ((r == bassPc) ? 10 : 0);
                    if (score > bestScore) {
                        bestScore = score;
                        bestRoot = r;
                        bestSuf = tpl.suffix;
                    }
                }
            }
        }

        if (bestScore < 0) {
            int relIv = 0;
            for (int p = 1; p < 12; p++) {
                if ((pcMask & (1 << ((bassPc + p) % 12))) != 0) {
                    relIv = p;
                    break;
                }
            }
            out.chordName = NOTE_NAMES[bassPc] + " + " + INTERVAL_LABELS[relIv] + " Cluster";
            return;
        }

        if (bestRoot != bassPc) {
            out.chordName = NOTE_NAMES[bestRoot] + bestSuf + "/" + NOTE_NAMES[bassPc];
        } else {
            out.chordName = NOTE_NAMES[bestRoot] + bestSuf;
        }
    }

    /** Formats the interior label of a fretboard dot according to {@code labelMode}. */
    public static String formatDotLabel(int mode, int labelMode, int pc, int relIv, int fingerNum) {
        pc = ((pc % 12) + 12) % 12;
        relIv = ((relIv % 12) + 12) % 12;
        if (labelMode == LABEL_NOTES) {
            return NOTE_NAMES[pc];
        } else if (labelMode == LABEL_DEGREES) {
            if (mode == MODE_CHORDS && fingerNum > 0 && fingerNum <= 4) {
                return "F" + fingerNum;
            }
            return DEGREE_LABELS[relIv];
        } else {
            return INTERVAL_LABELS[relIv];
        }
    }

    /** Returns the spelled scale notes string (e.g. {@code "A C D Eb E G"}) for a given root and scale. */
    public static String formatScaleNotes(int rootPc, int scaleIdx) {
        rootPc = ((rootPc % 12) + 12) % 12;
        ScaleDef sc = SCALES[clampIndex(scaleIdx, SCALES.length)];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            if (sc.mask[i] > 0) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(NOTE_NAMES[(rootPc + i) % 12]);
            }
        }
        return sb.toString();
    }

    /** Returns the spelled chord tones string (e.g. {@code "C E G B"}) for a given root and chord quality. */
    public static String formatChordNotes(int rootPc, int chordIdx) {
        rootPc = ((rootPc % 12) + 12) % 12;
        ChordQualityDef cd = CHORDS[clampIndex(chordIdx, CHORDS.length)];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            if (cd.mask[i] > 0) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(NOTE_NAMES[(rootPc + i) % 12]);
            }
        }
        return sb.toString();
    }

    /** Formats a 6-string fret tab string such as {@code "X-3-5-4-5-3"}. */
    public static String formatVoicingTab(ComputedVoicing cv) {
        StringBuilder sb = new StringBuilder();
        for (int s = 0; s < NUM_STRINGS; s++) {
            if (s > 0) sb.append("-");
            sb.append(cv.frets[s] < 0 ? "X" : String.valueOf(cv.frets[s]));
        }
        return sb.toString();
    }

    /**
     * Evaluates whether a tapped {@code (stringIdx, fret)} satisfies an active quiz question.
     *
     * @param quizMode       {@link #QUIZ_NOTES} or {@link #QUIZ_INTERVALS}
     * @param tuningIdx      active guitar tuning (0..5)
     * @param rootPc         active key root pitch class (0..11)
     * @param targetValue    target pitch class (0..11) for {@code QUIZ_NOTES}, or target semitone
     *                       interval (0..11) above {@code rootPc} for {@code QUIZ_INTERVALS}
     * @param tappedStr      tapped string index (0 = low 6th string .. 5 = high 1st string)
     * @param tappedFret     tapped fret (0..15)
     * @return {@code true} if the tapped position matches the quiz target
     */
    public static boolean isQuizAnswerCorrect(
            int quizMode, int tuningIdx, int rootPc, int targetValue, int tappedStr, int tappedFret) {
        int tappedPc = getPitchClass(tuningIdx, tappedStr, tappedFret);
        targetValue = ((targetValue % 12) + 12) % 12;
        rootPc = ((rootPc % 12) + 12) % 12;
        if (quizMode == QUIZ_NOTES) {
            return tappedPc == targetValue;
        } else if (quizMode == QUIZ_INTERVALS) {
            int relIv = (tappedPc - rootPc + 12) % 12;
            return relIv == targetValue;
        }
        return false;
    }

    /** Formats the prompt banner text for the active fretboard quiz question. */
    public static String formatQuizPrompt(int quizMode, int rootPc, int targetValue, int score, int total) {
        rootPc = ((rootPc % 12) + 12) % 12;
        targetValue = ((targetValue % 12) + 12) % 12;
        if (quizMode == QUIZ_NOTES) {
            return String.format(Locale.US, "NOTE QUIZ (%d/%d): Tap any '%s' on the neck!",
                    score, total, NOTE_NAMES[targetValue]);
        } else if (quizMode == QUIZ_INTERVALS) {
            int targetPc = (rootPc + targetValue) % 12;
            return String.format(Locale.US, "INTERVAL QUIZ (%d/%d): Tap '%s' (%s) in Key of %s!",
                    score, total, INTERVAL_LABELS[targetValue], NOTE_NAMES[targetPc], NOTE_NAMES[rootPc]);
        }
        return "";
    }

    public static int clampIndex(int idx, int length) {
        if (length <= 0) return 0;
        int m = idx % length;
        return (m < 0) ? (m + length) : m;
    }
}
