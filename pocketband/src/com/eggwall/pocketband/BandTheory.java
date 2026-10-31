package com.eggwall.pocketband;

/**
 * Pure, stateless music-theory, backing-style, drum/bass step-sequencer, and
 * 12-fret guitar scale-map engine for PocketBand.
 *
 * <p>Translated from {@code pocketband.c} on the PowKiddy V90 handheld and expanded
 * with additional backing genres (Bossa Nova, Funk, Country, Reggae) and customizable
 * progression presets. Has zero Android UI dependencies so 100% of the music theory,
 * transposition, MIDI note generation, and step timing math can be unit-tested on a
 * standard JVM via JUnit 4.</p>
 */
public final class BandTheory {

    private BandTheory() {}

    /** Sample rate used by {@link BandSynthEngine} (44,100 Hz). */
    public static final int SAMPLE_RATE = 44100;

    /** Number of sequencer tracks (Kick, Snare, Closed Hat, Open Hat/Ride, Bass). */
    public static final int NUM_TRACKS = 5;

    /** Maximum eighth-note steps per bar (8 steps for 4/4: 1 & 2 & 3 & 4 &; 6 steps for 3/4: 1 & 2 & 3 &). */
    public static final int MAX_STEPS = 8;

    public static final int TRK_KICK  = 0;
    public static final int TRK_SNARE = 1;
    public static final int TRK_CLHAT = 2;
    public static final int TRK_OPHAT = 3;
    public static final int TRK_BASS  = 4;

    public static final String[] TRACK_NAMES = {
        "KICK", "SNARE", "CL HAT", "OP HAT", "BASS"
    };

    /** Bass step degree labels: 0=Rest, 1=Root, 2=3rd, 3=5th, 4=6th, 5=b7th, 6=Octave. */
    public static final String[] BASS_DEG_LABELS = {
        ".", "R", "3", "5", "6", "b7", "8"
    };

    /** Drum step state labels: 0=Off, 1=Normal Hit, 2=Accent Hit. */
    public static final String[] DRUM_STEP_LABELS = {
        ".", "ON", "ACC"
    };

    /** Pitch-class names (0 = C .. 11 = B). */
    public static final String[] NOTE_NAMES = {
        "C", "Db", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B"
    };

    /** Guitar string labels from top row (1st string high e) to bottom row (6th string low E). */
    public static final String[] GTR_STR_LABELS = { "e", "B", "G", "D", "A", "E" };

    /** Open guitar string pitch classes from top row (high e = 4) to bottom row (low E = 4). */
    public static final int[] GTR_STR_OPEN_PC = { 4, 11, 7, 2, 9, 4 };

    /* =========================================================================
     * Chord Qualities & Bar Chord Representation
     * ========================================================================= */
    public static final int CHQ_MAJ  = 0;
    public static final int CHQ_MIN  = 1;
    public static final int CHQ_DOM7 = 2;
    public static final int CHQ_MAJ7 = 3;
    public static final int CHQ_MIN7 = 4;

    /** Represents a single bar's chord relative to the active musical key. */
    public static final class BarChord {
        public final int rootOffset; // Semitones above active key root (0..11)
        public final int qual;       // CHQ_MAJ .. CHQ_MIN7
        public final String roman;   // Roman numeral label, e.g. "I7", "IV7", "ii7"

        public BarChord(int rootOffset, int qual, String roman) {
            this.rootOffset = ((rootOffset % 12) + 12) % 12;
            this.qual = qual;
            this.roman = roman;
        }
    }

    /** Palette of common diatonic, blues, and secondary-dominant chords selectable per bar slot. */
    public static final BarChord[] CHORD_CANDIDATES = {
        new BarChord(0,  CHQ_MAJ,  "I"),
        new BarChord(0,  CHQ_DOM7, "I7"),
        new BarChord(0,  CHQ_MAJ7, "Imaj7"),
        new BarChord(0,  CHQ_MIN,  "i"),
        new BarChord(0,  CHQ_MIN7, "im7"),
        new BarChord(2,  CHQ_MIN,  "ii"),
        new BarChord(2,  CHQ_MIN7, "ii7"),
        new BarChord(2,  CHQ_DOM7, "II7"),
        new BarChord(4,  CHQ_MIN,  "iii"),
        new BarChord(4,  CHQ_MIN7, "iii7"),
        new BarChord(4,  CHQ_DOM7, "III7"),
        new BarChord(5,  CHQ_MAJ,  "IV"),
        new BarChord(5,  CHQ_DOM7, "IV7"),
        new BarChord(5,  CHQ_MAJ7, "IVmaj7"),
        new BarChord(5,  CHQ_MIN,  "iv"),
        new BarChord(7,  CHQ_MAJ,  "V"),
        new BarChord(7,  CHQ_DOM7, "V7"),
        new BarChord(7,  CHQ_MIN,  "v"),
        new BarChord(8,  CHQ_MAJ,  "bVI"),
        new BarChord(8,  CHQ_MAJ7, "bVImaj7"),
        new BarChord(9,  CHQ_MIN,  "vi"),
        new BarChord(9,  CHQ_MIN7, "vi7"),
        new BarChord(9,  CHQ_DOM7, "VI7"),
        new BarChord(10, CHQ_MAJ,  "bVII"),
        new BarChord(10, CHQ_DOM7, "bVII7")
    };

    /* =========================================================================
     * Named Chord Progression Presets
     * ========================================================================= */
    public static final class ProgressionPreset {
        public final String name;
        public final String shortLabel;
        public final BarChord[] bars;

        public ProgressionPreset(String name, String shortLabel, BarChord[] bars) {
            this.name = name;
            this.shortLabel = shortLabel;
            this.bars = bars;
        }
    }

    public static final ProgressionPreset[] PROGRESSION_PRESETS = {
        // 0: 12-Bar Blues (I7 - IV7 - V7)
        new ProgressionPreset(
            "12-Bar Blues (I7-IV7-V7)", "12-Bar Blues",
            new BarChord[] {
                new BarChord(0, CHQ_DOM7, "I7"),  new BarChord(0, CHQ_DOM7, "I7"),
                new BarChord(0, CHQ_DOM7, "I7"),  new BarChord(0, CHQ_DOM7, "I7"),
                new BarChord(5, CHQ_DOM7, "IV7"), new BarChord(5, CHQ_DOM7, "IV7"),
                new BarChord(0, CHQ_DOM7, "I7"),  new BarChord(0, CHQ_DOM7, "I7"),
                new BarChord(7, CHQ_DOM7, "V7"),  new BarChord(5, CHQ_DOM7, "IV7"),
                new BarChord(0, CHQ_DOM7, "I7"),  new BarChord(7, CHQ_DOM7, "V7")
            }
        ),
        // 1: Classic Rock (I - bVII - IV - I)
        new ProgressionPreset(
            "Classic Rock (I-bVII-IV-I)", "I-bVII-IV-I",
            new BarChord[] {
                new BarChord(0,  CHQ_MAJ, "I"),
                new BarChord(10, CHQ_MAJ, "bVII"),
                new BarChord(5,  CHQ_MAJ, "IV"),
                new BarChord(0,  CHQ_MAJ, "I")
            }
        ),
        // 2: Minor Ballad (i - bVI - bVII - V7)
        new ProgressionPreset(
            "Minor Ballad (i-bVI-bVII-V7)", "i-bVI-bVII-V7",
            new BarChord[] {
                new BarChord(0,  CHQ_MIN,  "i"),
                new BarChord(8,  CHQ_MAJ,  "bVI"),
                new BarChord(10, CHQ_MAJ,  "bVII"),
                new BarChord(7,  CHQ_DOM7, "V7")
            }
        ),
        // 3: Jazz Swing Turnaround (ii7 - V7 - Imaj7 - VI7)
        new ProgressionPreset(
            "Jazz Turnaround (ii7-V7-Imaj7-VI7)", "ii7-V7-I-VI7",
            new BarChord[] {
                new BarChord(2, CHQ_MIN7, "ii7"),
                new BarChord(7, CHQ_DOM7, "V7"),
                new BarChord(0, CHQ_MAJ7, "Imaj7"),
                new BarChord(9, CHQ_DOM7, "VI7")
            }
        ),
        // 4: Jazz / Bossa Cycle (Imaj7 - vi7 - ii7 - V7)
        new ProgressionPreset(
            "Jazz Cycle (Imaj7-vi7-ii7-V7)", "I-vi7-ii7-V7",
            new BarChord[] {
                new BarChord(0, CHQ_MAJ7, "Imaj7"),
                new BarChord(9, CHQ_MIN7, "vi7"),
                new BarChord(2, CHQ_MIN7, "ii7"),
                new BarChord(7, CHQ_DOM7, "V7")
            }
        ),
        // 5: Pop/Country Anthem (I - V - vi - IV)
        new ProgressionPreset(
            "Pop / Country (I-V-vi-IV)", "I-V-vi-IV",
            new BarChord[] {
                new BarChord(0, CHQ_MAJ, "I"),
                new BarChord(7, CHQ_MAJ, "V"),
                new BarChord(9, CHQ_MIN, "vi"),
                new BarChord(5, CHQ_MAJ, "IV")
            }
        ),
        // 6: Funk / Soul Groove (I7 - IV7 - I7 - V7)
        new ProgressionPreset(
            "Funk Groove (I7-IV7-I7-V7)", "I7-IV7-I7-V7",
            new BarChord[] {
                new BarChord(0, CHQ_DOM7, "I7"),
                new BarChord(5, CHQ_DOM7, "IV7"),
                new BarChord(0, CHQ_DOM7, "I7"),
                new BarChord(7, CHQ_DOM7, "V7")
            }
        ),
        // 7: Reggae Roots (I - IV - vi - V)
        new ProgressionPreset(
            "Reggae Roots (I-IV-vi-V)", "I-IV-vi-V",
            new BarChord[] {
                new BarChord(0, CHQ_MAJ, "I"),
                new BarChord(5, CHQ_MAJ, "IV"),
                new BarChord(9, CHQ_MIN, "vi"),
                new BarChord(7, CHQ_MAJ, "V")
            }
        ),
        // 8: Heavy Metal Riff (i - bVI - bVII - i)
        new ProgressionPreset(
            "Heavy Metal (i-bVI-bVII-i)", "i-bVI-bVII-i",
            new BarChord[] {
                new BarChord(0,  CHQ_MIN, "i"),
                new BarChord(8,  CHQ_MAJ, "bVI"),
                new BarChord(10, CHQ_MAJ, "bVII"),
                new BarChord(0,  CHQ_MIN, "i")
            }
        )
    };

    /* =========================================================================
     * Built-In Backing Band Styles (Blues, Rock, Jazz, Bossa, Funk, Country, Reggae)
     * ========================================================================= */
    public static final class StylePreset {
        public final String name;
        public final String shortGenre;
        public final int beatsPerBar;        // 4 for 4/4, 3 for 3/4
        public final int stepsPerBar;        // 8 for 4/4, 6 for 3/4
        public final int defaultBpm;
        public final int swingPct;           // 50 = Straight 8ths, 60..66 = Swing/Shuffle
        public final int defaultProgIdx;     // Index into PROGRESSION_PRESETS
        public final String scaleName;
        public final int[] scaleMask;        // Length 12: 0=off, 1=scale tone, 2=blue/color note
        public final int[][] defaultSteps;   // [NUM_TRACKS][MAX_STEPS]

        public StylePreset(
                String name,
                String shortGenre,
                int beatsPerBar,
                int stepsPerBar,
                int defaultBpm,
                int swingPct,
                int defaultProgIdx,
                String scaleName,
                int[] scaleMask,
                int[][] defaultSteps) {
            this.name = name;
            this.shortGenre = shortGenre;
            this.beatsPerBar = beatsPerBar;
            this.stepsPerBar = stepsPerBar;
            this.defaultBpm = defaultBpm;
            this.swingPct = swingPct;
            this.defaultProgIdx = defaultProgIdx;
            this.scaleName = scaleName;
            this.scaleMask = scaleMask;
            this.defaultSteps = defaultSteps;
        }

        public BarChord[] getDefaultBars() {
            return PROGRESSION_PRESETS[defaultProgIdx].bars;
        }
    }

    public static final StylePreset[] STYLES = {
        // 0: Blues Shuffle 4/4 (12-Bar Blues, I7 - IV7 - V7, 65% Triplet Shuffle)
        new StylePreset(
            "Blues Shuffle 4/4", "BLUES 4/4", 4, 8, 110, 65, 0,
            "Minor Pentatonic + Blues (1 b3 4 b5 5 b7)",
            new int[] { 1, 0, 0, 1, 0, 1, 2, 1, 0, 0, 1, 0 },
            new int[][] {
                /* steps:   1  &  2  &  3  &  4  &  */
                /* KICK */ { 2, 0, 0, 0, 1, 0, 0, 1 },
                /* SNARE*/ { 0, 0, 2, 0, 0, 0, 2, 0 },
                /* CLHAT*/ { 2, 1, 2, 1, 2, 1, 2, 1 },
                /* OPHAT*/ { 0, 0, 0, 0, 0, 0, 0, 0 },
                /* BASS */ { 1, 3, 4, 3, 1, 3, 4, 5 } // R-5-6-5 R-5-6-b7
            }
        ),

        // 1: Slow Blues 3/4 (12-Bar Waltz Blues in 3/4, 1 & 2 & 3 &)
        new StylePreset(
            "Slow Blues 3/4", "BLUES 3/4", 3, 6, 86, 62, 0,
            "Minor Pentatonic + Blues (1 b3 4 b5 5 b7)",
            new int[] { 1, 0, 0, 1, 0, 1, 2, 1, 0, 0, 1, 0 },
            new int[][] {
                /* steps:   1  &  2  &  3  &       */
                /* KICK */ { 2, 0, 0, 1, 0, 0, 0, 0 },
                /* SNARE*/ { 0, 0, 0, 0, 2, 0, 0, 0 },
                /* CLHAT*/ { 2, 1, 1, 1, 1, 0, 0, 0 },
                /* OPHAT*/ { 0, 0, 0, 0, 0, 1, 0, 0 },
                /* BASS */ { 1, 2, 3, 4, 3, 2, 0, 0 } // R-3-5-6-5-3
            }
        ),

        // 2: Classic Rock 4/4 (Straight 8ths, I - bVII - IV - I)
        new StylePreset(
            "Classic Rock 4/4", "ROCK 4/4", 4, 8, 120, 50, 1,
            "Mixolydian / Rock Pentatonic (1 2 3 4 5 6 b7)",
            new int[] { 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 2, 0 },
            new int[][] {
                /* steps:   1  &  2  &  3  &  4  &  */
                /* KICK */ { 2, 0, 0, 1, 2, 0, 0, 0 },
                /* SNARE*/ { 0, 0, 2, 0, 0, 0, 2, 0 },
                /* CLHAT*/ { 2, 1, 2, 1, 2, 1, 2, 0 },
                /* OPHAT*/ { 0, 0, 0, 0, 0, 0, 0, 1 },
                /* BASS */ { 1, 1, 1, 3, 1, 1, 6, 3 } // R-R-R-5 R-R-8-5
            }
        ),

        // 3: Rock Ballad 3/4 (3/4 Time, i - bVI - bVII - V7)
        new StylePreset(
            "Rock Ballad 3/4", "ROCK 3/4", 3, 6, 98, 50, 2,
            "Natural Minor / Aeolian (1 2 b3 4 5 b6 b7)",
            new int[] { 1, 0, 1, 1, 0, 1, 0, 1, 1, 0, 1, 0 },
            new int[][] {
                /* steps:   1  &  2  &  3  &       */
                /* KICK */ { 2, 0, 0, 1, 0, 0, 0, 0 },
                /* SNARE*/ { 0, 0, 0, 0, 2, 0, 0, 0 },
                /* CLHAT*/ { 2, 1, 1, 1, 2, 0, 0, 0 },
                /* OPHAT*/ { 0, 0, 0, 0, 0, 1, 0, 0 },
                /* BASS */ { 1, 1, 3, 3, 6, 3, 0, 0 } // R-R-5-5-8-5
            }
        ),

        // 4: Jazz Swing 4/4 (ii7 - V7 - Imaj7 - VI7, Walking Quarter-Note Bass)
        new StylePreset(
            "Jazz Swing 4/4", "JAZZ 4/4", 4, 8, 126, 63, 3,
            "Major / Jazz Bebop Modes (1 2 3 4 5 6 7)",
            new int[] { 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1 },
            new int[][] {
                /* steps:   1  &  2  &  3  &  4  &  */
                /* KICK */ { 1, 0, 0, 0, 1, 0, 0, 0 },
                /* SNARE*/ { 0, 0, 0, 1, 0, 0, 0, 1 },
                /* CLHAT*/ { 0, 0, 2, 0, 0, 0, 2, 0 },
                /* OPHAT*/ { 2, 0, 1, 1, 2, 0, 1, 1 },
                /* BASS */ { 1, 0, 2, 0, 3, 0, 5, 0 } // Walking R - 3 - 5 - b7
            }
        ),

        // 5: Jazz Waltz 3/4 (Imaj7 - vi7 - ii7 - V7 in 3/4 Swing)
        new StylePreset(
            "Jazz Waltz 3/4", "JAZZ 3/4", 3, 6, 132, 60, 4,
            "Major / Lydian Waltz (1 2 3 4 5 6 7)",
            new int[] { 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1 },
            new int[][] {
                /* steps:   1  &  2  &  3  &       */
                /* KICK */ { 1, 0, 0, 0, 0, 0, 0, 0 },
                /* SNARE*/ { 0, 0, 0, 1, 0, 0, 0, 0 },
                /* CLHAT*/ { 0, 0, 2, 0, 0, 0, 0, 0 },
                /* OPHAT*/ { 2, 0, 1, 1, 1, 0, 0, 0 },
                /* BASS */ { 1, 0, 2, 0, 3, 4, 0, 0 } // R . 3 . 5 6
            }
        ),

        // 6: Bossa Nova 4/4 (Straight Latin groove, Root-Fifth bass on 1 & 3)
        new StylePreset(
            "Bossa Nova 4/4", "BOSSA 4/4", 4, 8, 128, 50, 4,
            "Major / Dorian Bossa (1 2 3 4 5 6 7)",
            new int[] { 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1 },
            new int[][] {
                /* steps:   1  &  2  &  3  &  4  &  */
                /* KICK */ { 2, 0, 0, 1, 2, 0, 0, 1 },
                /* SNARE*/ { 0, 0, 1, 0, 0, 1, 0, 1 }, // Cross-stick partido alto
                /* CLHAT*/ { 1, 1, 1, 1, 1, 1, 1, 1 },
                /* OPHAT*/ { 0, 0, 0, 0, 0, 0, 0, 0 },
                /* BASS */ { 1, 0, 0, 3, 3, 0, 0, 1 } // Classic Bossa R . . 5 5 . . R
            }
        ),

        // 7: Funk Groove 4/4 (Syncopated Pocket, Dorian / Mixolydian Funk)
        new StylePreset(
            "Funk Groove 4/4", "FUNK 4/4", 4, 8, 104, 54, 6,
            "Dorian / Funk Minor Pentatonic (1 2 b3 4 5 6 b7)",
            new int[] { 1, 0, 1, 1, 0, 1, 2, 1, 0, 2, 1, 0 },
            new int[][] {
                /* steps:   1  &  2  &  3  &  4  &  */
                /* KICK */ { 2, 1, 0, 0, 0, 2, 0, 1 },
                /* SNARE*/ { 0, 0, 2, 0, 0, 0, 2, 0 },
                /* CLHAT*/ { 2, 1, 2, 1, 2, 1, 2, 0 },
                /* OPHAT*/ { 0, 0, 0, 0, 0, 0, 0, 1 },
                /* BASS */ { 1, 6, 0, 5, 3, 0, 4, 5 } // Syncopated octave/b7 funk bass
            }
        ),

        // 8: Country Train 4/4 (Alternating Root-Fifth Boom-Chicka)
        new StylePreset(
            "Country Train 4/4", "COUNTRY", 4, 8, 136, 56, 5,
            "Major Pentatonic + Country Blue (1 2 b3 3 5 6)",
            new int[] { 1, 0, 1, 2, 1, 0, 0, 1, 0, 1, 0, 0 },
            new int[][] {
                /* steps:   1  &  2  &  3  &  4  &  */
                /* KICK */ { 2, 0, 0, 0, 2, 0, 0, 0 },
                /* SNARE*/ { 0, 1, 2, 1, 0, 1, 2, 1 }, // Train snare brush
                /* CLHAT*/ { 2, 1, 2, 1, 2, 1, 2, 1 },
                /* OPHAT*/ { 0, 0, 0, 0, 0, 0, 0, 0 },
                /* BASS */ { 1, 0, 3, 0, 1, 0, 3, 0 } // Alternating Root - Fifth
            }
        ),

        // 9: Reggae One-Drop 4/4 (Kick + Rim on Beat 3, Skank on '&')
        new StylePreset(
            "Reggae One-Drop", "REGGAE", 4, 8, 82, 58, 7,
            "Major / Roots Pentatonic (1 2 3 4 5 6 7)",
            new int[] { 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1 },
            new int[][] {
                /* steps:   1  &  2  &  3  &  4  &  */
                /* KICK */ { 0, 0, 0, 0, 2, 0, 0, 1 }, // One-drop on beat 3
                /* SNARE*/ { 0, 0, 0, 0, 2, 0, 0, 0 },
                /* CLHAT*/ { 1, 2, 1, 2, 1, 2, 1, 2 }, // Accent offbeat '&'
                /* OPHAT*/ { 0, 0, 0, 0, 0, 0, 0, 0 },
                /* BASS */ { 1, 0, 3, 5, 1, 0, 3, 0 } // Deep dub bassline
            }
        ),

        // 10: Heavy Metal 4/4 (Driving double-kick & palm-muted eighth-note pedal bass)
        new StylePreset(
            "Heavy Metal 4/4", "METAL 4/4", 4, 8, 148, 50, 8,
            "Aeolian / Phrygian Metal (1 b2 b3 4 b5 5 b6 b7)",
            new int[] { 1, 2, 1, 1, 0, 1, 2, 1, 1, 0, 1, 0 },
            new int[][] {
                /* steps:   1  &  2  &  3  &  4  &  */
                /* KICK */ { 2, 1, 0, 1, 2, 1, 0, 2 },
                /* SNARE*/ { 0, 0, 2, 0, 0, 0, 2, 0 },
                /* CLHAT*/ { 2, 1, 2, 1, 2, 1, 2, 0 },
                /* OPHAT*/ { 0, 0, 0, 0, 0, 0, 0, 1 },
                /* BASS */ { 1, 1, 1, 1, 1, 1, 5, 6 } // Chugging R-R-R-R R-R-b7-8
            }
        ),

        // 11: Pop 4/4 (Upbeat 4-on-the-floor Pop groove, I - V - vi - IV)
        new StylePreset(
            "Pop 4/4", "POP 4/4", 4, 8, 122, 50, 5,
            "Major / Ionian Pop (1 2 3 4 5 6 7)",
            new int[] { 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1 },
            new int[][] {
                /* steps:   1  &  2  &  3  &  4  &  */
                /* KICK */ { 2, 0, 0, 1, 2, 0, 1, 0 },
                /* SNARE*/ { 0, 0, 2, 0, 0, 0, 2, 0 },
                /* CLHAT*/ { 2, 1, 2, 1, 2, 1, 2, 1 },
                /* OPHAT*/ { 0, 0, 0, 1, 0, 0, 0, 1 },
                /* BASS */ { 1, 6, 1, 3, 1, 6, 3, 4 } // Octave-bouncing Pop bass
            }
        )
    };

    /* =========================================================================
     * Pure Music-Theory & Sequencer Helper Functions
     * ========================================================================= */

    /** Returns the transposed chord symbol (e.g. {@code "A7"}, {@code "Dm7"}, {@code "F#m"}) for a bar chord. */
    public static String formatChordLabel(int keyPc, BarChord bc) {
        if (bc == null) return "C";
        int pc = ((keyPc + bc.rootOffset) % 12 + 12) % 12;
        String suf;
        switch (bc.qual) {
            case CHQ_MIN:  suf = "m";    break;
            case CHQ_DOM7: suf = "7";    break;
            case CHQ_MAJ7: suf = "maj7"; break;
            case CHQ_MIN7: suf = "m7";   break;
            case CHQ_MAJ:
            default:       suf = "";     break;
        }
        return NOTE_NAMES[pc] + suf;
    }

    /** Returns the spelled chord tones (e.g. {@code "A - C# - E - G"}) for a bar chord in {@code keyPc}. */
    public static String formatChordNotes(int keyPc, BarChord bc) {
        if (bc == null) return "";
        int rootPc = ((keyPc + bc.rootOffset) % 12 + 12) % 12;
        int thirdSemi = (bc.qual == CHQ_MIN || bc.qual == CHQ_MIN7) ? 3 : 4;
        int fifthSemi = 7;
        int n1 = rootPc;
        int n2 = (rootPc + thirdSemi) % 12;
        int n3 = (rootPc + fifthSemi) % 12;
        if (bc.qual == CHQ_MAJ || bc.qual == CHQ_MIN) {
            return NOTE_NAMES[n1] + " - " + NOTE_NAMES[n2] + " - " + NOTE_NAMES[n3];
        }
        int sevSemi = (bc.qual == CHQ_MAJ7) ? 11 : 10;
        int n4 = (rootPc + sevSemi) % 12;
        return NOTE_NAMES[n1] + " - " + NOTE_NAMES[n2] + " - " + NOTE_NAMES[n3] + " - " + NOTE_NAMES[n4];
    }

    /** Returns the chord formula intervals (e.g. {@code "1 - 3 - 5 - b7"}). */
    public static String formatChordFormula(BarChord bc) {
        if (bc == null) return "1 - 3 - 5";
        switch (bc.qual) {
            case CHQ_MIN:  return "1 - b3 - 5";
            case CHQ_DOM7: return "1 - 3 - 5 - b7";
            case CHQ_MAJ7: return "1 - 3 - 5 - 7";
            case CHQ_MIN7: return "1 - b3 - 5 - b7";
            case CHQ_MAJ:
            default:       return "1 - 3 - 5";
        }
    }

    /**
     * Computes the exact MIDI note number for an electric bass step degree (`1..6`)
     * over {@code bc} in {@code keyPc}, keeping the bass root anchored in E1 (MIDI 28) .. Eb2 (MIDI 39).
     *
     * @return MIDI note number (28..51), or -1 if {@code bassDeg <= 0}.
     */
    public static int computeBassMidi(int keyPc, BarChord bc, int bassDeg) {
        if (bassDeg <= 0 || bassDeg > 6 || bc == null) {
            return -1;
        }
        int chordRootPc = ((keyPc + bc.rootOffset) % 12 + 12) % 12;
        int thirdSemi = (bc.qual == CHQ_MIN || bc.qual == CHQ_MIN7) ? 3 : 4;
        int semitoneOff;
        switch (bassDeg) {
            case 1: semitoneOff = 0;         break; // Root (R)
            case 2: semitoneOff = thirdSemi; break; // 3rd (3 or b3)
            case 3: semitoneOff = 7;         break; // 5th (5)
            case 4: semitoneOff = 9;         break; // 6th (6)
            case 5: semitoneOff = 10;        break; // Flat 7th (b7)
            case 6: semitoneOff = 12;        break; // Octave (8)
            default: semitoneOff = 0;        break;
        }
        int baseMidi = 28 + ((chordRootPc - 4 + 12) % 12);
        return baseMidi + semitoneOff;
    }

    /**
     * Computes the 3-voice rhythm comping MIDI notes (Root, 3rd, and 5th/7th in C3..B4 range)
     * for {@code bc} in {@code keyPc}.
     */
    public static int[] computeChordMidiNotes(int keyPc, BarChord bc) {
        int chordRootPc = ((keyPc + bc.rootOffset) % 12 + 12) % 12;
        int rootMidi = 48 + chordRootPc; // C3..B3 range
        int thirdSemi = (bc.qual == CHQ_MIN || bc.qual == CHQ_MIN7) ? 3 : 4;
        int topSemi = (bc.qual == CHQ_MAJ7) ? 11 :
                      (bc.qual == CHQ_DOM7 || bc.qual == CHQ_MIN7) ? 10 : 7;
        return new int[] {
            rootMidi,
            rootMidi + thirdSemi,
            rootMidi + topSemi
        };
    }

    /**
     * Returns whether the rhythm guitar/organ comping track should fire on {@code stepIdx}
     * for a given style and meter.
     */
    public static boolean shouldTriggerComping(int styleIdx, int beatsPerBar, int stepIdx) {
        // Reggae (style 9) skanks on the '&' offbeats (odd steps: 1, 3, 5, 7)
        if (styleIdx == 9) {
            return (stepIdx % 2 == 1);
        }
        // Bossa Nova (style 6) syncopates on 1, 2&, 4
        if (styleIdx == 6) {
            return (stepIdx == 0 || stepIdx == 3 || stepIdx == 6);
        }
        // Standard 4/4 styles stab on Beat 1 (step 0) and the '&' of Beat 2 (step 3)
        if (beatsPerBar == 4) {
            return (stepIdx == 0 || stepIdx == 3);
        }
        // 3/4 styles stab on Beat 1 (step 0) and Beat 2 (step 2)
        return (stepIdx == 0 || stepIdx == 2);
    }

    /**
     * Computes the duration in audio samples (at 44.1 kHz) for a given eighth-note step
     * given {@code bpm} (40..240) and {@code swingPct} (50..68).
     *
     * <p>Even steps (on the beat: 0, 2, 4, 6) are lengthened when {@code swingPct > 50},
     * while odd steps (on the {@code '&'}: 1, 3, 5, 7) are shortened by the exact complement
     * so every beat pair always sums to one quarter-note beat.</p>
     */
    public static int computeStepSamples(int bpm, int swingPct, int stepIdx) {
        int clampedBpm = Math.max(40, Math.min(240, bpm));
        int clampedSwing = Math.max(50, Math.min(68, swingPct));
        double beatSamples = ((double) SAMPLE_RATE * 60.0) / (double) clampedBpm;
        double ratio = (stepIdx % 2 == 0)
                ? ((double) clampedSwing / 50.0)
                : ((double) (100 - clampedSwing) / 50.0);
        int len = (int) Math.round(beatSamples * 0.5 * ratio);
        return Math.max(1000, len);
    }

    /**
     * Converts a MIDI note number to a 32-bit unsigned fixed-point phase increment at 44.1 kHz.
     */
    public static int midiToPhaseInc(int midiNote) {
        double freq = 440.0 * Math.pow(2.0, (midiNote - 69) / 12.0);
        long inc = (long) ((freq * 4294967296.0) / (double) SAMPLE_RATE);
        return (int) (inc & 0xFFFFFFFFL);
    }

    /**
     * Classifies a guitar fretboard coordinate {@code (stringRow, fret)} for the 12-fret
     * Soloing Scale Map:
     * <ul>
     *   <li>{@code 0} = Not in scale</li>
     *   <li>{@code 1} = Regular scale tone (Cyan)</li>
     *   <li>{@code 2} = Characteristic blue/color note (Pink)</li>
     *   <li>{@code 3} = Active bar chord root (Emerald Green)</li>
     *   <li>{@code 4} = Key signature root (Gold)</li>
     * </ul>
     */
    public static int classifyScaleFret(
            StylePreset style, int keyPc, BarChord curBarChord, int stringRow, int fret) {
        if (style == null || stringRow < 0 || stringRow >= 6 || fret < 0 || fret > 12) {
            return 0;
        }
        int openPc = GTR_STR_OPEN_PC[stringRow];
        int pc = (openPc + fret) % 12;
        int rel = (pc - keyPc + 12) % 12;
        int mask = style.scaleMask[rel];
        if (mask <= 0) {
            return 0;
        }
        int curChordPc = (curBarChord != null) ? ((keyPc + curBarChord.rootOffset) % 12) : keyPc;
        if (pc == keyPc) {
            return 4; // Key root (highest priority)
        }
        if (pc == curChordPc) {
            return 3; // Active chord root
        }
        if (mask == 2) {
            return 2; // Blue / color note
        }
        return 1; // Normal scale tone
    }

    /**
     * Cycles a bar chord to the next candidate in {@link #CHORD_CANDIDATES}.
     */
    public static BarChord nextChordCandidate(BarChord current, int delta) {
        int idx = 0;
        if (current != null) {
            for (int i = 0; i < CHORD_CANDIDATES.length; i++) {
                BarChord c = CHORD_CANDIDATES[i];
                if (c.rootOffset == current.rootOffset && c.qual == current.qual) {
                    idx = i;
                    break;
                }
            }
        }
        int next = ((idx + delta) % CHORD_CANDIDATES.length + CHORD_CANDIDATES.length)
                % CHORD_CANDIDATES.length;
        return CHORD_CANDIDATES[next];
    }
}
