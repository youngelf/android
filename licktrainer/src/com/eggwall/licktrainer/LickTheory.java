package com.eggwall.licktrainer;

/**
 * Pure, stateless music-theory, guitar tablature, fretboard position mapping,
 * diatonic 7th arpeggio harmony, and biomechanics verification engine for
 * {@code LickTrainer}, translated from the PowKiddy V90 {@code licktrainer.c}.
 *
 * <p>This class has zero Android UI dependencies so that 100% of the 33 exercises
 * (including Pentatonic, Legato/Arpeggios, Alternate Picking, Blues/Rock,
 * Warmup/Spider, and the 8 Jazz 7th diatonic arpeggio lessons), 12-key transposition,
 * scale-box fretboard mapping, backing chord voicings, and picking/left-hand
 * biomechanics can be unit-tested directly on a host JVM via JUnit 4.
 *
 * <p><b>Engineering Customization Guide:</b>
 * <ul>
 *   <li><b>Adding or Modifying Exercises:</b> Edit {@link #LICKS}, {@link #CATEGORY_NAMES},
 *       {@link #CAT_START}, and {@link #CAT_COUNT}. Run {@code ./gradlew testDebugUnitTest}
 *       to execute {@link #verifyLickLibrary()} against every step's scale mask and
 *       left-hand finger biomechanics.</li>
 *   <li><b>Changing Guitar Tuning:</b> Modify {@link #OPEN_MIDI} and {@link #STR_LABELS}
 *       (default standard tuning {@code E2 A2 D3 G3 B3 E4}).</li>
 *   <li><b>Customizing Speed-Builder BPM Ladders:</b> Edit {@link #LADDERS} to change
 *       automatic tempo ramp steps, bar intervals, and target BPM ceilings.</li>
 * </ul>
 */
public final class LickTheory {

    private LickTheory() {}

    /** Number of exercise categories. */
    public static final int NUM_CATEGORIES = 6;
    /** Total number of built-in guitar exercises & Jazz 7th arpeggio studies. */
    public static final int TOTAL_LICKS = 33;
    /** Maximum number of rhythmic steps per measure (16 for 16th-note 4/4, 12 for 12/8 triplets). */
    public static final int MAX_STEPS = 16;
    /** Number of transposable musical keys (E through Eb). */
    public static final int NUM_KEYS = 12;
    /** Number of automated Speed-Builder BPM ladder presets. */
    public static final int NUM_LADDERS = 5;

    /** Minimum allowed tempo in BPM. */
    public static final int MIN_BPM = 40;
    /** Maximum allowed tempo in BPM. */
    public static final int MAX_BPM = 240;

    /* Audio Mix Modes */
    public static final int MIX_TAB_AND_CLICK  = 0;
    public static final int MIX_TAB_BACK_CLICK = 1;
    public static final int MIX_CLICK_ONLY     = 2;
    public static final int MIX_TAB_ONLY       = 3;
    public static final int MIX_COUNT          = 4;

    public static final String[] MIX_NAMES = {
        "TAB+CLK",
        "TAB+BCK",
        "CLK ONLY",
        "TAB ONLY"
    };

    /* Scale / Arpeggio Pitch-Class Bitmasks relative to Root (bit i = semitone i) */
    public static final int PC_MINOR_PENTA    = (1 << 0) | (1 << 3) | (1 << 5) | (1 << 7) | (1 << 10);
    public static final int PC_NATURAL_MINOR  = (1 << 0) | (1 << 2) | (1 << 3) | (1 << 5) | (1 << 7) | (1 << 8) | (1 << 10);
    public static final int PC_HARMONIC_MINOR = (1 << 0) | (1 << 2) | (1 << 3) | (1 << 5) | (1 << 7) | (1 << 8) | (1 << 11);
    public static final int PC_MAJOR_SCALE    = (1 << 0) | (1 << 2) | (1 << 4) | (1 << 5) | (1 << 7) | (1 << 9) | (1 << 11);
    public static final int PC_MINOR_TRIAD    = (1 << 0) | (1 << 3) | (1 << 7);
    public static final int PC_MIN7_ARP       = (1 << 0) | (1 << 3) | (1 << 7) | (1 << 10);
    public static final int PC_DIM7_ARP       = (1 << 0) | (1 << 3) | (1 << 6) | (1 << 9);
    public static final int PC_BLUES          = (1 << 0) | (1 << 3) | (1 << 5) | (1 << 6) | (1 << 7) | (1 << 10);
    public static final int PC_HYBRID_BLUES   = (1 << 0) | (1 << 2) | (1 << 3) | (1 << 4) | (1 << 5) | (1 << 6) | (1 << 7) | (1 << 9) | (1 << 10);
    public static final int PC_CHROMATIC      = 0x0FFF;

    public static final String[] CATEGORY_NAMES = {
        "PENTATONIC",
        "LEGATO/ARP",
        "ALT PICK",
        "BLUES/ROCK",
        "SPIDER",
        "JAZZ 7THS"
    };

    public static final int[] CAT_START = { 0, 5, 10, 15, 20, 25 };
    public static final int[] CAT_COUNT = { 5, 5,  5,  5,  5,  8 };

    /** Standard tuning open-string MIDI pitches: E2(40), A2(45), D3(50), G3(55), B3(59), E4(64). */
    public static final int[] OPEN_MIDI = { 40, 45, 50, 55, 59, 64 };
    /** Standard 6-string guitar labels from 6th string ('E') to 1st string ('e'). */
    public static final char[] STR_LABELS = { 'E', 'A', 'D', 'G', 'B', 'e' };

    /** 12 chromatic pitch-class names (0 = C .. 11 = B). */
    public static final String[] NOTE_NAMES = {
        "C", "C#", "D", "Eb", "E", "F", "F#", "G", "G#", "A", "Bb", "B"
    };

    /** Left-hand finger descriptions (1-indexed: 1=Index .. 4=Pinky). */
    public static final String[] FINGER_NAMES = {
        "", "1:Index", "2:Middle", "3:Ring", "4:Pinky"
    };

    /** Represents one of the 12 transposable musical keys and its guitar neck anchor fret. */
    public static final class KeyAnchor {
        public final String name;
        public final int anchorFret;

        public KeyAnchor(String name, int anchorFret) {
            this.name = name;
            this.anchorFret = anchorFret;
        }
    }

    /** 12 transposable keys from E (open/0fr) to Eb (11fr), defaulting to Key of A (index 5, 5th fret). */
    public static final KeyAnchor[] KEYS = {
        new KeyAnchor("E",  0),
        new KeyAnchor("F",  1),
        new KeyAnchor("F#", 2),
        new KeyAnchor("G",  3),
        new KeyAnchor("G#", 4),
        new KeyAnchor("A",  5),
        new KeyAnchor("Bb", 6),
        new KeyAnchor("B",  7),
        new KeyAnchor("C",  8),
        new KeyAnchor("C#", 9),
        new KeyAnchor("D",  10),
        new KeyAnchor("Eb", 11)
    };

    /** Diatonic 7th Chord Metadata in the Major Scale (1-indexed: degrees 1..7). */
    public static final class Diatonic7thInfo {
        public final String roman;
        public final String romanShort;
        public final String qualSuffix;
        public final int[] tonesPc;
        public final String[] toneLbl;

        public Diatonic7thInfo(String roman, String romanShort, String qualSuffix,
                               int[] tonesPc, String[] toneLbl) {
            this.roman = roman;
            this.romanShort = romanShort;
            this.qualSuffix = qualSuffix;
            this.tonesPc = tonesPc;
            this.toneLbl = toneLbl;
        }
    }

    public static final Diatonic7thInfo[] DIATONIC_7THS = {
        new Diatonic7thInfo("",        "",    "",     new int[]{0,  0, 0,  0}, new String[]{"",  "",   "",   ""  }),
        new Diatonic7thInfo("Imaj7",   "I",   "maj7", new int[]{0,  4, 7, 11}, new String[]{"1", "3",  "5",  "7" }),
        new Diatonic7thInfo("iim7",    "ii",  "m7",   new int[]{2,  5, 9,  0}, new String[]{"1", "b3", "5",  "b7"}),
        new Diatonic7thInfo("iiim7",   "iii", "m7",   new int[]{4,  7, 11, 2}, new String[]{"1", "b3", "5",  "b7"}),
        new Diatonic7thInfo("IVmaj7",  "IV",  "maj7", new int[]{5,  9, 0,  4}, new String[]{"1", "3",  "5",  "7" }),
        new Diatonic7thInfo("V7",      "V7",  "7",    new int[]{7, 11, 2,  5}, new String[]{"1", "3",  "5",  "b7"}),
        new Diatonic7thInfo("vim7",    "vi",  "m7",   new int[]{9,  0, 4,  7}, new String[]{"1", "b3", "5",  "b7"}),
        new Diatonic7thInfo("viim7b5", "vii", "m7b5", new int[]{11, 2, 5,  9}, new String[]{"1", "b3", "b5", "b7"})
    };

    /** Automated Speed-Builder BPM Ladder preset definition. */
    public static final class LadderPreset {
        public final String label;
        public final String shortLabel;
        public final int bpmStep;
        public final int barsPerStep;
        public final int goalOffset;

        public LadderPreset(String label, String shortLabel, int bpmStep, int barsPerStep, int goalOffset) {
            this.label = label;
            this.shortLabel = shortLabel;
            this.bpmStep = bpmStep;
            this.barsPerStep = barsPerStep;
            this.goalOffset = goalOffset;
        }
    }

    public static final LadderPreset[] LADDERS = {
        new LadderPreset("OFF (STATIC BPM)",  "STATIC",   0, 0,  0),
        new LadderPreset("+2 BPM / 2 BARS",   "+2B/2bar", 2, 2, 40),
        new LadderPreset("+5 BPM / 4 BARS",   "+5B/4bar", 5, 4, 50),
        new LadderPreset("+5 BPM / 8 BARS",   "+5B/8bar", 5, 8, 50),
        new LadderPreset("+10 BPM / 4 BARS",  "+10B/4b", 10, 4, 60)
    };

    /** A single tablature step inside a guitar lick or arpeggio exercise. */
    public static final class LickStep {
        /** Guitar string index: 0 = 6th (Low E) .. 5 = 1st (High e), -1 = rest. */
        public final int str;
        /** Fret offset relative to the selected key's anchor fret. */
        public final int fretOff;
        /** Left-hand finger number: 1 = Index, 2 = Middle, 3 = Ring, 4 = Pinky. */
        public final int finger;
        /** Picking / articulation symbol: 'D'=Down, 'U'=Up, 'H'=Hammer-on, 'P'=Pull-off, '/'=Slide. */
        public final char pick;

        public LickStep(int str, int fretOff, int finger, char pick) {
            this.str = str;
            this.fretOff = fretOff;
            this.finger = finger;
            this.pick = pick;
        }
    }

    /** Complete definition of a single 12-step or 16-step guitar exercise. */
    public static final class LickDef {
        public final int catIdx;
        public final String title;
        public final String subtitle;
        public final int numSteps;
        public final int stepsPerBeat;
        public final int defaultBpm;
        public final int allowedPcMask;
        public final int[] beatDeg;
        public final LickStep[] steps;

        public LickDef(int catIdx, String title, String subtitle,
                       int numSteps, int stepsPerBeat, int defaultBpm,
                       int allowedPcMask, int[] beatDeg, LickStep[] steps) {
            this.catIdx = catIdx;
            this.title = title;
            this.subtitle = subtitle;
            this.numSteps = numSteps;
            this.stepsPerBeat = stepsPerBeat;
            this.defaultBpm = defaultBpm;
            this.allowedPcMask = allowedPcMask;
            this.beatDeg = beatDeg;
            this.steps = steps;
        }

        public boolean isJazz7ths() {
            return beatDeg != null && beatDeg.length >= 4 && beatDeg[0] >= 1 && beatDeg[0] <= 7;
        }
    }

    private static LickStep s(int str, int fretOff, int finger, char pick) {
        return new LickStep(str, fretOff, finger, pick);
    }

    /**
     * All 33 Curated Guitar Exercises across 6 Categories:
     * <ul>
     *   <li>Set 1 (#01..#05): PENTATONIC LICKS</li>
     *   <li>Set 2 (#06..#10): LEGATO &amp; ARPEGGIOS</li>
     *   <li>Set 3 (#11..#15): ALTERNATE PICKING</li>
     *   <li>Set 4 (#16..#20): BLUES &amp; ROCK</li>
     *   <li>Set 5 (#21..#25): WARMUP &amp; SPIDER</li>
     *   <li>Set 6 (#26..#33): JAZZ 7TH CHORD ARPEGGIOS (I, ii, iii, IV, V7, vi, viim7b5 -&gt; 1-3-5-7)</li>
     * </ul>
     */
    public static final LickDef[] LICKS = {
        /* ==================== 1. PENTATONIC LICKS (#01..#05) ==================== */
        new LickDef(
            0, "GILBERT 4-NOTE SEQ", "Paul Gilbert Box 1 Pentatonic Burner", 16, 4, 100, PC_MINOR_PENTA, new int[]{0,0,0,0},
            new LickStep[]{
                s(4,0,1,'D'), s(4,3,4,'U'), s(5,0,1,'D'), s(4,3,4,'U'),
                s(4,0,1,'D'), s(3,2,3,'U'), s(4,0,1,'D'), s(4,3,4,'U'),
                s(5,0,1,'D'), s(5,3,4,'U'), s(5,0,1,'P'), s(4,3,4,'D'),
                s(5,0,1,'U'), s(4,3,4,'D'), s(4,0,1,'P'), s(3,2,3,'D')
            }
        ),
        new LickDef(
            0, "ERIC JOHNSON 5S", "Cascading Pentatonic Waterfall", 16, 4, 96, PC_MINOR_PENTA, new int[]{0,0,0,0},
            new LickStep[]{
                s(5,3,4,'D'), s(5,0,1,'P'), s(4,3,4,'D'), s(4,0,1,'U'),
                s(3,2,3,'D'), s(4,3,4,'U'), s(4,0,1,'P'), s(3,2,3,'D'),
                s(3,0,1,'U'), s(2,2,3,'D'), s(3,2,3,'U'), s(3,0,1,'P'),
                s(2,2,3,'D'), s(2,0,1,'U'), s(1,2,3,'D'), s(1,0,1,'U')
            }
        ),
        new LickDef(
            0, "PENTATONIC THREES", "Ascending 12/8 Triplet Box 1 Sequence", 12, 3, 95, PC_MINOR_PENTA, new int[]{0,0,0,0},
            new LickStep[]{
                s(0,0,1,'D'), s(0,3,4,'U'), s(1,0,1,'D'),
                s(0,3,4,'U'), s(1,0,1,'D'), s(1,2,3,'U'),
                s(1,0,1,'D'), s(1,2,3,'U'), s(2,0,1,'D'),
                s(1,2,3,'U'), s(2,0,1,'D'), s(2,2,3,'U')
            }
        ),
        new LickDef(
            0, "WIDE STRING SKIP", "String-Skipping Minor Pentatonic Etude", 16, 4, 90, PC_MINOR_PENTA, new int[]{0,0,0,0},
            new LickStep[]{
                s(0,0,1,'D'), s(0,3,4,'U'), s(2,0,1,'D'), s(2,2,3,'U'),
                s(1,0,1,'D'), s(1,2,3,'U'), s(3,0,1,'D'), s(3,2,3,'U'),
                s(2,0,1,'D'), s(2,2,3,'U'), s(4,0,1,'D'), s(4,3,4,'U'),
                s(3,0,1,'D'), s(3,2,3,'U'), s(5,0,1,'D'), s(5,3,4,'U')
            }
        ),
        new LickDef(
            0, "BOX 1 TO 2 SLIDE", "Diagonal Pentatonic Position Shift", 16, 4, 100, PC_MINOR_PENTA, new int[]{0,0,0,0},
            new LickStep[]{
                s(0,0,1,'D'), s(0,3,3,'H'), s(1,0,1,'D'), s(1,2,3,'H'),
                s(2,0,1,'D'), s(2,2,3,'H'), s(3,0,1,'D'), s(3,2,3,'H'),
                s(3,4,3,'/'), s(4,3,1,'D'), s(4,5,3,'H'), s(5,3,1,'D'),
                s(5,5,3,'H'), s(5,7,3,'/'), s(5,5,1,'D'), s(4,5,3,'U')
            }
        ),

        /* ==================== 2. LEGATO & ARPEGGIOS (#06..#10) ==================== */
        new LickDef(
            1, "LEGATO 3-NPS ROLL", "Hammer-On & Pull-Off Stamina Builder", 16, 4, 95, PC_NATURAL_MINOR, new int[]{0,0,0,0},
            new LickStep[]{
                s(2,0,1,'D'), s(2,2,2,'H'), s(2,4,4,'H'), s(2,2,2,'P'),
                s(3,0,1,'D'), s(3,2,2,'H'), s(3,4,4,'H'), s(3,2,2,'P'),
                s(4,1,1,'D'), s(4,3,2,'H'), s(4,5,4,'H'), s(4,3,2,'P'),
                s(5,2,1,'D'), s(5,3,2,'H'), s(5,5,4,'H'), s(5,3,2,'P')
            }
        ),
        new LickDef(
            1, "MINOR TRIAD ARP", "3-String Minor Arpeggio Etude", 12, 3, 100, PC_MINOR_TRIAD, new int[]{0,0,0,0},
            new LickStep[]{
                s(2,2,3,'D'), s(3,0,1,'D'), s(4,0,1,'D'),
                s(5,0,1,'D'), s(5,3,4,'H'), s(5,0,1,'P'),
                s(4,0,1,'U'), s(3,0,1,'U'), s(2,2,3,'D'),
                s(3,0,1,'D'), s(4,0,1,'D'), s(5,0,1,'U')
            }
        ),
        new LickDef(
            1, "MIN7 SKIPPED ARP", "String-Skipped Minor 7th Arpeggio", 16, 4, 92, PC_MIN7_ARP, new int[]{0,0,0,0},
            new LickStep[]{
                s(0,0,1,'D'), s(0,3,4,'H'), s(2,0,1,'D'), s(2,2,3,'H'),
                s(4,0,1,'D'), s(4,3,4,'H'), s(5,0,1,'D'), s(5,3,4,'H'),
                s(5,0,1,'P'), s(4,3,4,'D'), s(4,0,1,'P'), s(2,2,3,'D'),
                s(2,0,1,'P'), s(0,3,4,'D'), s(0,0,1,'P'), s(2,2,3,'D')
            }
        ),
        new LickDef(
            1, "DIM7 SYMMETRY ARP", "Symmetrical Diminished 7th Sequence", 16, 4, 96, PC_DIM7_ARP, new int[]{0,0,0,0},
            new LickStep[]{
                s(3,0,1,'D'), s(3,3,4,'H'), s(4,2,2,'D'), s(5,0,1,'D'),
                s(5,3,4,'H'), s(5,6,4,'/'), s(5,3,1,'P'), s(4,5,2,'D'),
                s(3,6,3,'U'), s(3,3,1,'P'), s(4,5,2,'D'), s(5,3,1,'D'),
                s(5,6,4,'H'), s(5,3,1,'P'), s(4,2,2,'D'), s(5,0,1,'U')
            }
        ),
        new LickDef(
            1, "5-STR SWEEP SHAPE", "5th-Pos 5-String Minor Sweep Arpeggio", 16, 4, 88, PC_MINOR_TRIAD, new int[]{0,0,0,0},
            new LickStep[]{
                s(1,2,3,'D'), s(2,2,3,'D'), s(3,0,1,'D'), s(4,0,1,'D'),
                s(5,0,1,'D'), s(5,3,4,'H'), s(5,0,1,'P'), s(4,0,1,'U'),
                s(3,0,1,'U'), s(2,2,3,'U'), s(1,2,3,'U'), s(2,2,3,'D'),
                s(3,0,1,'D'), s(4,0,1,'D'), s(5,0,1,'D'), s(5,3,4,'U')
            }
        ),

        /* ==================== 3. ALTERNATE PICKING (#11..#15) ==================== */
        new LickDef(
            2, "GILBERT 3-NPS CELL", "Inside/Outside 6-Note Picking Etude", 12, 3, 105, PC_NATURAL_MINOR, new int[]{0,0,0,0},
            new LickStep[]{
                s(4,0,1,'D'), s(4,1,2,'U'), s(4,3,4,'D'),
                s(5,0,1,'U'), s(4,3,4,'D'), s(4,1,2,'U'),
                s(4,0,1,'D'), s(4,1,2,'U'), s(4,3,4,'D'),
                s(5,0,1,'U'), s(5,2,3,'D'), s(5,3,4,'U')
            }
        ),
        new LickDef(
            2, "NEOCLASSIC PEDAL", "Harmonic Minor High-e Pedal Point", 16, 4, 104, PC_HARMONIC_MINOR, new int[]{0,0,0,0},
            new LickStep[]{
                s(5,0,1,'D'), s(5,3,3,'U'), s(5,2,2,'D'), s(5,3,3,'U'),
                s(4,4,4,'D'), s(5,3,3,'U'), s(5,0,1,'D'), s(5,3,3,'U'),
                s(4,1,1,'D'), s(5,3,3,'U'), s(4,4,4,'D'), s(5,3,3,'U'),
                s(5,0,1,'D'), s(5,3,3,'U'), s(5,2,2,'D'), s(5,0,1,'U')
            }
        ),
        new LickDef(
            2, "DIATONIC 4-NOTE", "Strict Alternate Picking 3-NPS Climb", 16, 4, 100, PC_NATURAL_MINOR, new int[]{0,0,0,0},
            new LickStep[]{
                s(0,0,1,'D'), s(0,2,2,'U'), s(0,3,3,'D'), s(1,0,1,'U'),
                s(0,2,2,'D'), s(0,3,3,'U'), s(1,0,1,'D'), s(1,2,2,'U'),
                s(0,3,3,'D'), s(1,0,1,'U'), s(1,2,2,'D'), s(1,3,3,'U'),
                s(1,0,1,'D'), s(1,2,2,'U'), s(1,3,3,'D'), s(2,0,1,'U')
            }
        ),
        new LickDef(
            2, "DI MEOLA SYNCOPA", "Syncopated Cross-String Picking Etude", 16, 4, 98, PC_NATURAL_MINOR, new int[]{0,0,0,0},
            new LickStep[]{
                s(2,2,3,'D'), s(3,0,1,'U'), s(3,2,3,'D'), s(4,1,2,'U'),
                s(3,2,3,'D'), s(3,0,1,'U'), s(2,2,3,'D'), s(3,0,1,'U'),
                s(3,2,3,'D'), s(4,1,2,'U'), s(4,3,4,'D'), s(5,0,1,'U'),
                s(4,3,4,'D'), s(4,1,2,'U'), s(3,2,3,'D'), s(3,0,1,'U')
            }
        ),
        new LickDef(
            2, "CROSS-STRING JUMP", "Alternate Picking String-Crossing Drill", 16, 4, 94, PC_MINOR_PENTA, new int[]{0,0,0,0},
            new LickStep[]{
                s(2,0,1,'D'), s(3,2,3,'U'), s(2,2,3,'D'), s(4,0,1,'U'),
                s(2,0,1,'D'), s(4,3,4,'U'), s(3,0,1,'D'), s(5,0,1,'U'),
                s(3,2,3,'D'), s(5,3,4,'U'), s(4,0,1,'D'), s(5,0,1,'U'),
                s(4,3,4,'D'), s(3,2,3,'U'), s(4,0,1,'D'), s(3,0,1,'U')
            }
        ),

        /* ==================== 4. BLUES & ROCK (#16..#20) ==================== */
        new LickDef(
            3, "BB & ALBERT TURN", "Classic Major/Minor Blues Turnaround", 16, 4, 92, PC_HYBRID_BLUES, new int[]{0,0,0,0},
            new LickStep[]{
                s(3,2,2,'D'), s(3,4,2,'/'), s(4,3,1,'U'), s(5,3,1,'D'),
                s(5,5,3,'U'), s(5,3,1,'P'), s(4,5,3,'D'), s(4,3,1,'U'),
                s(3,2,3,'D'), s(3,0,1,'P'), s(3,1,2,'H'), s(2,2,3,'D'),
                s(3,0,1,'U'), s(4,0,1,'D'), s(4,3,4,'U'), s(5,0,1,'D')
            }
        ),
        new LickDef(
            3, "SRV TEXAS SHUFFLE", "Driving 12/8 Blues Box Groove", 12, 3, 108, PC_HYBRID_BLUES, new int[]{0,0,0,0},
            new LickStep[]{
                s(0,0,1,'D'), s(0,3,4,'U'), s(1,0,1,'D'),
                s(1,1,2,'H'), s(1,2,3,'H'), s(2,0,1,'D'),
                s(2,2,3,'U'), s(3,0,1,'D'), s(3,1,2,'H'),
                s(4,0,1,'D'), s(4,3,4,'U'), s(2,2,3,'D')
            }
        ),
        new LickDef(
            3, "DORIAN MIXO RUN", "Hybrid Minor/Major 3rd & 6th Lick", 16, 4, 98, PC_HYBRID_BLUES, new int[]{0,0,0,0},
            new LickStep[]{
                s(1,0,1,'D'), s(1,2,3,'H'), s(2,0,1,'D'), s(2,2,3,'H'),
                s(3,0,1,'D'), s(3,1,2,'H'), s(4,0,1,'D'), s(4,2,3,'H'),
                s(5,0,1,'D'), s(5,2,3,'U'), s(5,3,4,'D'), s(5,0,1,'P'),
                s(4,3,4,'D'), s(4,2,3,'U'), s(3,0,1,'D'), s(3,1,2,'H')
            }
        ),
        new LickDef(
            3, "HENDRIX CHORD EMBL", "Double-Stop Style Melodic Fill", 16, 4, 88, PC_MINOR_PENTA, new int[]{0,0,0,0},
            new LickStep[]{
                s(2,0,1,'D'), s(2,2,3,'H'), s(3,0,1,'D'), s(2,2,3,'U'),
                s(3,0,1,'D'), s(3,2,3,'H'), s(3,0,1,'P'), s(2,2,3,'D'),
                s(4,0,1,'D'), s(4,3,4,'H'), s(4,0,1,'P'), s(3,2,3,'D'),
                s(3,0,1,'U'), s(2,2,3,'D'), s(2,0,1,'P'), s(1,2,3,'D')
            }
        ),
        new LickDef(
            3, "BLUES b5 CASCADE", "Descending Blues Scale with Blue Note", 16, 4, 96, PC_BLUES, new int[]{0,0,0,0},
            new LickStep[]{
                s(5,3,4,'D'), s(5,0,1,'P'), s(4,3,4,'D'), s(4,0,1,'P'),
                s(3,3,4,'D'), s(3,2,3,'P'), s(3,0,1,'P'), s(2,2,3,'D'),
                s(3,2,3,'U'), s(3,0,1,'P'), s(2,2,3,'D'), s(2,0,1,'P'),
                s(1,2,3,'D'), s(1,1,2,'P'), s(1,0,1,'P'), s(0,3,4,'D')
            }
        ),

        /* ==================== 5. WARMUP & SPIDER (#21..#25) ==================== */
        new LickDef(
            4, "1-2-3-4 SPIDER", "Classic Chromatic 4-Finger Crawl", 16, 4, 90, PC_CHROMATIC, new int[]{0,0,0,0},
            new LickStep[]{
                s(0,0,1,'D'), s(0,1,2,'U'), s(0,2,3,'D'), s(0,3,4,'U'),
                s(1,0,1,'D'), s(1,1,2,'U'), s(1,2,3,'D'), s(1,3,4,'U'),
                s(2,0,1,'D'), s(2,1,2,'U'), s(2,2,3,'D'), s(2,3,4,'U'),
                s(3,0,1,'D'), s(3,1,2,'U'), s(3,2,3,'D'), s(3,3,4,'U')
            }
        ),
        new LickDef(
            4, "1-3-2-4 PERMUTE", "Alternating Finger Independence Drill", 16, 4, 88, PC_CHROMATIC, new int[]{0,0,0,0},
            new LickStep[]{
                s(2,0,1,'D'), s(2,2,3,'U'), s(2,1,2,'D'), s(2,3,4,'U'),
                s(3,0,1,'D'), s(3,2,3,'U'), s(3,1,2,'D'), s(3,3,4,'U'),
                s(4,0,1,'D'), s(4,2,3,'U'), s(4,1,2,'D'), s(4,3,4,'U'),
                s(5,0,1,'D'), s(5,2,3,'U'), s(5,1,2,'D'), s(5,3,4,'U')
            }
        ),
        new LickDef(
            4, "SKIPPING SPIDER", "String-Skipping Chromatic Spider (6-4-5-3)", 16, 4, 85, PC_CHROMATIC, new int[]{0,0,0,0},
            new LickStep[]{
                s(0,0,1,'D'), s(0,1,2,'U'), s(0,2,3,'D'), s(0,3,4,'U'),
                s(2,0,1,'D'), s(2,1,2,'U'), s(2,2,3,'D'), s(2,3,4,'U'),
                s(1,0,1,'D'), s(1,1,2,'U'), s(1,2,3,'D'), s(1,3,4,'U'),
                s(3,0,1,'D'), s(3,1,2,'U'), s(3,2,3,'D'), s(3,3,4,'U')
            }
        ),
        new LickDef(
            4, "DIAGONAL SPIDER", "Diagonal Chromatic Fretboard Shift", 16, 4, 90, PC_CHROMATIC, new int[]{0,0,0,0},
            new LickStep[]{
                s(0,0,1,'D'), s(1,1,2,'U'), s(2,2,3,'D'), s(3,3,4,'U'),
                s(1,0,1,'D'), s(2,1,2,'U'), s(3,2,3,'D'), s(4,3,4,'U'),
                s(2,0,1,'D'), s(3,1,2,'U'), s(4,2,3,'D'), s(5,3,4,'U'),
                s(5,2,3,'D'), s(4,1,2,'U'), s(3,0,1,'D'), s(2,1,2,'U')
            }
        ),
        new LickDef(
            4, "4-3-2-1 PINKY WORK", "Descending Pinky-First Finger Builder", 16, 4, 88, PC_CHROMATIC, new int[]{0,0,0,0},
            new LickStep[]{
                s(5,3,4,'D'), s(5,2,3,'U'), s(5,1,2,'D'), s(5,0,1,'U'),
                s(4,3,4,'D'), s(4,2,3,'U'), s(4,1,2,'D'), s(4,0,1,'U'),
                s(3,3,4,'D'), s(3,2,3,'U'), s(3,1,2,'D'), s(3,0,1,'U'),
                s(2,3,4,'D'), s(2,2,3,'U'), s(2,1,2,'D'), s(2,0,1,'U')
            }
        ),

        /* ==================== 6. JAZZ 7TH ARPEGGIOS (#26..#33) ==================== */
        new LickDef(
            5, "6TH-STR ASC I-IV", "Diatonic 1-3-5-7 Arps: Imaj7 iim7 iiim7 IVmaj7", 16, 4, 84, PC_MAJOR_SCALE, new int[]{1, 2, 3, 4},
            new LickStep[]{
                s(0,0,1,'D'),  s(1,-1,1,'U'), s(1,2,4,'D'), s(2,1,2,'U'),
                s(0,2,2,'D'),  s(1,0,1,'U'),  s(1,4,4,'D'), s(2,2,3,'U'),
                s(1,-1,1,'D'), s(1,2,4,'U'),  s(2,1,2,'D'), s(2,4,4,'U'),
                s(1,0,1,'D'),  s(1,4,4,'U'),  s(2,2,2,'D'), s(3,1,1,'U')
            }
        ),
        new LickDef(
            5, "6TH-STR ASC V-I", "Diatonic 1-3-5-7 Arps: V7 vim7 viim7b5 Imaj7", 16, 4, 84, PC_MAJOR_SCALE, new int[]{5, 6, 7, 1},
            new LickStep[]{
                s(1,2,2,'D'), s(2,1,1,'U'), s(2,4,4,'D'), s(3,2,2,'U'),
                s(1,4,4,'D'), s(2,2,2,'U'), s(3,1,1,'D'), s(3,4,4,'U'),
                s(2,1,1,'D'), s(2,4,4,'U'), s(3,2,2,'D'), s(4,2,3,'U'),
                s(2,2,2,'D'), s(3,1,1,'U'), s(4,0,1,'D'), s(4,4,4,'U')
            }
        ),
        new LickDef(
            5, "VOICE-LED ALT I-IV", "Up 1-3-5-7 / Down 7-5-3-1: I ii iii IV", 16, 4, 84, PC_MAJOR_SCALE, new int[]{1, 2, 3, 4},
            new LickStep[]{
                s(0,0,1,'D'),  s(1,-1,1,'U'), s(1,2,4,'D'), s(2,1,2,'U'),
                s(2,2,3,'D'),  s(1,4,4,'U'),  s(1,0,1,'D'), s(0,2,2,'U'),
                s(1,-1,1,'D'), s(1,2,4,'U'),  s(2,1,2,'D'), s(2,4,4,'U'),
                s(3,1,1,'D'),  s(2,2,2,'U'),  s(1,4,4,'D'), s(1,0,1,'U')
            }
        ),
        new LickDef(
            5, "VOICE-LED ALT V-I", "Up 1-3-5-7 / Down 7-5-3-1: V7 vi vii I", 16, 4, 84, PC_MAJOR_SCALE, new int[]{5, 6, 7, 1},
            new LickStep[]{
                s(1,2,2,'D'), s(2,1,1,'U'), s(2,4,4,'D'), s(3,2,2,'U'),
                s(3,4,4,'D'), s(3,1,1,'U'), s(2,2,2,'D'), s(1,4,4,'U'),
                s(2,1,1,'D'), s(2,4,4,'U'), s(3,2,2,'D'), s(4,2,3,'U'),
                s(4,4,4,'D'), s(4,0,1,'U'), s(3,1,1,'D'), s(2,2,2,'U')
            }
        ),
        new LickDef(
            5, "UPPER-REG 7THS I-IV", "Upper Register 1-3-5-7: Imaj7 iim7 iiim7 IVmaj7", 16, 4, 82, PC_MAJOR_SCALE, new int[]{1, 2, 3, 4},
            new LickStep[]{
                s(2,2,2,'D'), s(3,1,1,'U'), s(4,0,1,'D'), s(4,4,4,'U'),
                s(2,4,4,'D'), s(3,2,2,'U'), s(4,2,3,'D'), s(5,0,1,'U'),
                s(3,1,1,'D'), s(4,0,1,'U'), s(4,4,4,'D'), s(5,2,2,'U'),
                s(3,2,2,'D'), s(4,2,3,'U'), s(5,0,1,'D'), s(5,4,4,'U')
            }
        ),
        new LickDef(
            5, "UPPER-REG 7THS V-I", "Upper Register 1-3-5-7: V7 vim7 viim7b5 Imaj7", 16, 4, 82, PC_MAJOR_SCALE, new int[]{5, 6, 7, 1},
            new LickStep[]{
                s(3,4,3,'D'), s(4,4,4,'U'), s(5,2,1,'D'), s(5,5,4,'U'),
                s(3,1,1,'D'), s(3,4,4,'U'), s(4,2,2,'D'), s(5,0,1,'U'),
                s(2,1,1,'D'), s(2,4,4,'U'), s(3,2,2,'D'), s(4,2,3,'U'),
                s(2,2,2,'D'), s(3,1,1,'U'), s(4,0,1,'D'), s(5,-1,1,'U')
            }
        ),
        new LickDef(
            5, "CYCLE OF 4THS PT.1", "Jazz Cycle 1-3-5-7: Imaj7 IVmaj7 viim7b5 iiim7", 16, 4, 86, PC_MAJOR_SCALE, new int[]{1, 4, 7, 3},
            new LickStep[]{
                s(0,0,1,'D'),  s(1,-1,1,'U'), s(1,2,4,'D'), s(2,1,2,'U'),
                s(1,0,1,'D'),  s(1,4,4,'U'),  s(2,2,2,'D'), s(3,1,1,'U'),
                s(2,1,1,'D'),  s(2,4,4,'U'),  s(3,2,2,'D'), s(4,2,3,'U'),
                s(1,-1,1,'D'), s(1,2,4,'U'),  s(2,1,2,'D'), s(2,4,4,'U')
            }
        ),
        new LickDef(
            5, "CYCLE OF 4THS PT.2", "Jazz Turnaround 1-3-5-7: vim7 iim7 V7 Imaj7", 16, 4, 86, PC_MAJOR_SCALE, new int[]{6, 2, 5, 1},
            new LickStep[]{
                s(1,4,4,'D'), s(2,2,2,'U'), s(3,1,1,'D'), s(3,4,4,'U'),
                s(0,2,2,'D'), s(1,0,1,'U'), s(1,4,4,'D'), s(2,2,3,'U'),
                s(1,2,2,'D'), s(2,1,1,'U'), s(2,4,4,'D'), s(3,2,2,'U'),
                s(2,2,2,'D'), s(3,1,1,'U'), s(4,0,1,'D'), s(4,4,4,'U')
            }
        )
    };

    /** Returns the global exercise index (0..32) for a category index and index within category. */
    public static int getGlobalLickIndex(int catIdx, int lickInCat) {
        int c = clamp(catIdx, 0, NUM_CATEGORIES - 1);
        int l = clamp(lickInCat, 0, CAT_COUNT[c] - 1);
        return CAT_START[c] + l;
    }

    /** Clamps an integer value between min and max inclusive. */
    public static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    /** Returns the actual guitar fret (0..24) for a step in the specified key index (0..11). */
    public static int getActualFret(LickStep step, int keyIdx) {
        int k = clamp(keyIdx, 0, NUM_KEYS - 1);
        int fret = KEYS[k].anchorFret + step.fretOff;
        return Math.max(0, fret);
    }

    /** Returns the MIDI note number for a step in the specified key index (0..11). */
    public static int getStepMidi(LickStep step, int keyIdx) {
        if (step.str < 0 || step.str >= 6) return -1;
        return OPEN_MIDI[step.str] + getActualFret(step, keyIdx);
    }

    /** Returns the note name ("C" .. "B") for a MIDI pitch. */
    public static String getNoteNameForMidi(int midi) {
        if (midi < 0) return "";
        return NOTE_NAMES[((midi % 12) + 12) % 12];
    }

    /**
     * Computes the starting fret of the zoomed 8-fret neck window for a given exercise and key.
     */
    public static int computeNeckWindowStartFret(LickDef lick, int keyIdx) {
        int anchorFret = KEYS[clamp(keyIdx, 0, NUM_KEYS - 1)].anchorFret;
        int minOff = 99;
        for (int s = 0; s < lick.numSteps; s++) {
            LickStep st = lick.steps[s];
            if (st.str >= 0 && st.fretOff < minOff) {
                minOff = st.fretOff;
            }
        }
        if (minOff == 99) minOff = 0;
        int startFret = anchorFret + minOff - 1;
        return Math.max(0, startFret);
    }

    /**
     * Resolves the chord-tone degree label ("1", "3", "5", "7") for a step inside a Jazz 7ths lesson.
     */
    public static String getJazzStepDegreeNum(LickDef lick, int stepIdx, int keyIdx) {
        if (!lick.isJazz7ths() || stepIdx < 0 || stepIdx >= lick.numSteps) return "1";
        int beatIdx = (stepIdx / lick.stepsPerBeat) & 3;
        int deg = lick.beatDeg[beatIdx];
        Diatonic7thInfo ch = DIATONIC_7THS[deg];
        int rootMidi = OPEN_MIDI[0] + KEYS[clamp(keyIdx, 0, NUM_KEYS - 1)].anchorFret;
        int sMidi = getStepMidi(lick.steps[stepIdx], keyIdx);
        int sPc = ((sMidi - rootMidi) % 12 + 12) % 12;
        if (sPc == ch.tonesPc[0]) return "1";
        if (sPc == ch.tonesPc[1]) return "3";
        if (sPc == ch.tonesPc[2]) return "5";
        if (sPc == ch.tonesPc[3]) return "7";
        return "1";
    }

    /**
     * Resolves the exact interval label ("1", "3", "b3", "5", "b5", "7", "b7") for a step in Jazz 7ths mode.
     */
    public static String getJazzStepIntervalLabel(LickDef lick, int stepIdx, int keyIdx) {
        if (!lick.isJazz7ths() || stepIdx < 0 || stepIdx >= lick.numSteps) return "1";
        int beatIdx = (stepIdx / lick.stepsPerBeat) & 3;
        int deg = lick.beatDeg[beatIdx];
        Diatonic7thInfo ch = DIATONIC_7THS[deg];
        int rootMidi = OPEN_MIDI[0] + KEYS[clamp(keyIdx, 0, NUM_KEYS - 1)].anchorFret;
        int sMidi = getStepMidi(lick.steps[stepIdx], keyIdx);
        int sPc = ((sMidi - rootMidi) % 12 + 12) % 12;
        for (int t = 0; t < 4; t++) {
            if (sPc == ch.tonesPc[t]) {
                return ch.toneLbl[t];
            }
        }
        return "1";
    }

    /**
     * Builds a 4-note backing chord MIDI array for a given exercise, beat index (0..3), and key index (0..11),
     * used when the user enables backing chord synthesis ({@link #MIX_TAB_BACK_CLICK}).
     */
    public static int[] getBackingChordMidi(LickDef lick, int beatIdx, int keyIdx) {
        int anchorFret = KEYS[clamp(keyIdx, 0, NUM_KEYS - 1)].anchorFret;
        int rootMidi = OPEN_MIDI[0] + anchorFret; // Between E2 (40) and Eb3 (51)

        if (lick.isJazz7ths()) {
            int b = clamp(beatIdx, 0, 3);
            int deg = lick.beatDeg[b];
            Diatonic7thInfo ch = DIATONIC_7THS[deg];
            int r0 = rootMidi + ch.tonesPc[0];
            int r1 = rootMidi + ch.tonesPc[1] + (ch.tonesPc[1] < ch.tonesPc[0] ? 12 : 0);
            int r2 = rootMidi + ch.tonesPc[2] + (ch.tonesPc[2] < ch.tonesPc[0] ? 12 : 0);
            int r3 = rootMidi + ch.tonesPc[3] + (ch.tonesPc[3] < ch.tonesPc[0] ? 12 : 0);
            return new int[]{ r0, r1, r2, r3 };
        }

        if (lick.allowedPcMask == PC_DIM7_ARP) {
            return new int[]{ rootMidi, rootMidi + 3, rootMidi + 6, rootMidi + 9 };
        } else if (lick.allowedPcMask == PC_HARMONIC_MINOR || lick.allowedPcMask == PC_MINOR_TRIAD) {
            return new int[]{ rootMidi, rootMidi + 7, rootMidi + 12, rootMidi + 15 };
        } else if (lick.allowedPcMask == PC_HYBRID_BLUES) {
            // Dominant 7th blues vamp
            return new int[]{ rootMidi, rootMidi + 4, rootMidi + 7, rootMidi + 10 };
        } else {
            // Minor 7th / power backing vamp
            return new int[]{ rootMidi, rootMidi + 3, rootMidi + 7, rootMidi + 10 };
        }
    }

    /**
     * Built-in music-theory &amp; left-hand biomechanics verifier across all 33 exercises.
     * Verifies:
     * <ol>
     *   <li>Every step's pitch class matches the exercise's allowed scale/arpeggio bitmask.</li>
     *   <li>In Jazz 7ths lessons, every beat's 4 notes cover all 4 chord tones {@code {1, 3, 5, 7}}
     *       of that beat's diatonic 7th chord.</li>
     *   <li>Slides ({@code '/'}) stay on the same string, move to a different fret, and keep the SAME left-hand finger.</li>
     *   <li>Hammer-ons ({@code 'H'}) stay on the same string, move to a higher fret, and use a higher finger number.</li>
     *   <li>Pull-offs ({@code 'P'}) stay on the same string, move to a lower fret, and use a lower finger number.</li>
     *   <li>No accidental unison duplicate pitches across adjacent strings in non-chromatic exercises.</li>
     * </ol>
     *
     * @return {@code null} if all 33 exercises pass verification, or a descriptive failure message if any rule fails.
     */
    public static String verifyLickLibrary() {
        final int anchor = 5; // Key of A: root MIDI = 45 (A2)
        final int rootMidi = OPEN_MIDI[0] + anchor;

        if (LICKS.length != TOTAL_LICKS) {
            return "Expected " + TOTAL_LICKS + " licks, found " + LICKS.length;
        }

        for (int i = 0; i < TOTAL_LICKS; i++) {
            LickDef l = LICKS[i];
            if (l.steps.length != l.numSteps) {
                return "Lick #" + (i + 1) + " (" + l.title + "): steps.length=" + l.steps.length + " != numSteps=" + l.numSteps;
            }
            for (int s = 0; s < l.numSteps; s++) {
                LickStep st = l.steps[s];
                if (st.str < 0 || st.str > 5) {
                    return "Lick #" + (i + 1) + " (" + l.title + ") step " + s + ": invalid string " + st.str;
                }
                if (st.finger < 1 || st.finger > 4) {
                    return "Lick #" + (i + 1) + " (" + l.title + ") step " + s + ": invalid finger " + st.finger;
                }
                int midi = OPEN_MIDI[st.str] + anchor + st.fretOff;
                int pc = ((midi - rootMidi) % 12 + 12) % 12;
                if ((l.allowedPcMask & (1 << pc)) == 0) {
                    return "Lick #" + (i + 1) + " (" + l.title + ") step " + s + ": pitch class " + pc
                            + " not in scale mask 0x" + Integer.toHexString(l.allowedPcMask);
                }

                int beatIdx = s / l.stepsPerBeat;
                if (beatIdx >= 0 && beatIdx < 4 && l.beatDeg[beatIdx] >= 1 && l.beatDeg[beatIdx] <= 7) {
                    int deg = l.beatDeg[beatIdx];
                    Diatonic7thInfo ch = DIATONIC_7THS[deg];
                    boolean inChord = false;
                    for (int t = 0; t < 4; t++) {
                        if (pc == ch.tonesPc[t]) {
                            inChord = true;
                            break;
                        }
                    }
                    if (!inChord) {
                        return "Lick #" + (i + 1) + " (" + l.title + ") step " + s + " (beat " + (beatIdx + 1)
                                + ", " + ch.roman + "): pc=" + pc + " not in chord {1,3,5,7}";
                    }
                    if ((s % 4) == 3 && l.stepsPerBeat == 4) {
                        int seenMask = 0;
                        for (int k = s - 3; k <= s; k++) {
                            int mk = OPEN_MIDI[l.steps[k].str] + anchor + l.steps[k].fretOff;
                            int pck = ((mk - rootMidi) % 12 + 12) % 12;
                            for (int t = 0; t < 4; t++) {
                                if (pck == ch.tonesPc[t]) seenMask |= (1 << t);
                            }
                        }
                        if (seenMask != 0x0F) {
                            return "Lick #" + (i + 1) + " (" + l.title + ") beat " + (beatIdx + 1)
                                    + " (" + ch.roman + "): did not cover all 4 chord tones {1,3,5,7} (mask=" + seenMask + ")";
                        }
                    }
                }

                if (s > 0) {
                    LickStep prev = l.steps[s - 1];
                    int prevMidi = OPEN_MIDI[prev.str] + anchor + prev.fretOff;

                    if (st.pick == '/') {
                        if (st.str != prev.str || st.fretOff == prev.fretOff) {
                            return "Lick #" + (i + 1) + " (" + l.title + ") step " + s
                                    + ": slide '/' must stay on same string and change fret";
                        }
                        if (st.finger != prev.finger) {
                            return "Lick #" + (i + 1) + " (" + l.title + ") step " + s
                                    + ": slide '/' changed finger from " + prev.finger + " to " + st.finger;
                        }
                    } else if (st.pick == 'H') {
                        if (st.str != prev.str || st.fretOff <= prev.fretOff || st.finger <= prev.finger) {
                            return "Lick #" + (i + 1) + " (" + l.title + ") step " + s + ": hammer-on 'H' invalid";
                        }
                    } else if (st.pick == 'P') {
                        if (st.str != prev.str || st.fretOff >= prev.fretOff || st.finger >= prev.finger) {
                            return "Lick #" + (i + 1) + " (" + l.title + ") step " + s + ": pull-off 'P' invalid";
                        }
                    }

                    if (l.allowedPcMask != PC_CHROMATIC && st.str != prev.str && midi == prevMidi) {
                        return "Lick #" + (i + 1) + " (" + l.title + ") step " + s + ": accidental unison pitch across strings";
                    }
                }
            }
        }
        return null;
    }
}
