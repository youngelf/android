package com.eggwall.chordwheel;

/**
 * Pure music theory, Circle of Fifths harmonic resolution, scale-degree number
 * calculator, CAGED/Barre guitar voicing generator, and 4-bar chord progression
 * preset tables.
 *
 * <p><b>Engineering Extension Points:</b>
 * <ul>
 *   <li><b>Tunings / Instruments:</b> Modify {@link #STRING_MIDI} and {@link #STRING_NAMES}
 *       (e.g. Drop D, DADGAD, half-step down, or baritone guitar) and update the open
 *       shape lookup tables ({@link #OPEN_MAJ}..{@link #OPEN_DOM7}) if needed.</li>
 *   <li><b>Chord Qualities:</b> Add new {@code QUAL_*} constants and extend
 *       {@link #resolveChordAt} and {@link #buildGuitarVoicing}.</li>
 *   <li><b>Progression Presets:</b> Append new {@link ProgPreset} entries to
 *       {@link #PRESETS} using clockwise Circle-of-Fifths offsets ({@code 0 = I/vi},
 *       {@code 1 = V/iii}, {@code 11 = IV/ii}).</li>
 * </ul>
 */
public final class MusicTheory {

    private MusicTheory() {}

    /* Chord quality identifiers used by ResolvedChord and buildGuitarVoicing() */
    public static final int QUAL_MAJ  = 0;
    public static final int QUAL_MIN  = 1;
    public static final int QUAL_DIM  = 2;
    public static final int QUAL_MAJ7 = 3;
    public static final int QUAL_MIN7 = 4;
    public static final int QUAL_DOM7 = 5;
    public static final int QUAL_M7B5 = 6;

    /**
     * Standard 6-string guitar open-string MIDI pitches from 6th string (low E2 = 40)
     * to 1st string (high E4 = 64): E2(40), A2(45), D3(50), G3(55), B3(59), E4(64).
     * Used by both {@link GuitarSynthEngine} for pitch synthesis and {@link ChordWheelView}
     * for fretboard note-name labeling.
     */
    public static final int[] STRING_MIDI = { 40, 45, 50, 55, 59, 64 };
    public static final String[] STRING_NAMES = { "E", "A", "D", "G", "B", "e" };

    /** Chromatic pitch-class names (0 = C .. 11 = B) for sharp-key and flat-key spelling. */
    public static final String[] NOTE_SHARP = {
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };
    public static final String[] NOTE_FLAT = {
        "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"
    };

    /**
     * Metadata for each of the 12 sectors around the Circle of Fifths.
     * Sector 0 sits at 12 o'clock (C Major / A Minor / B dim) and each clockwise step
     * advances by a perfect fifth (+7 semitones modulo 12).
     */
    public static final class WheelSector {
        public final String majName;
        public final String minName;
        public final String dimRootName;
        public final String keySig;
        public final int majPc;
        public final int minPc;
        public final int dimPc;
        public final boolean useFlats;

        public WheelSector(String majName, String minName, String dimRootName, String keySig,
                           int majPc, int minPc, int dimPc, boolean useFlats) {
            this.majName = majName;
            this.minName = minName;
            this.dimRootName = dimRootName;
            this.keySig = keySig;
            this.majPc = majPc;
            this.minPc = minPc;
            this.dimPc = dimPc;
            this.useFlats = useFlats;
        }
    }

    public static final WheelSector[] WHEEL = {
        new WheelSector("C",  "Am",  "B",  "0 #/b",  0,  9, 11, false), /* 0  (12:00) */
        new WheelSector("G",  "Em",  "F#", "1 #",    7,  4,  6, false), /* 1  ( 1:00) */
        new WheelSector("D",  "Bm",  "C#", "2 #",    2, 11,  1, false), /* 2  ( 2:00) */
        new WheelSector("A",  "F#m", "G#", "3 #",    9,  6,  8, false), /* 3  ( 3:00) */
        new WheelSector("E",  "C#m", "D#", "4 #",    4,  1,  3, false), /* 4  ( 4:00) */
        new WheelSector("B",  "G#m", "A#", "5 #",   11,  8, 10, false), /* 5  ( 5:00) */
        new WheelSector("F#", "D#m", "F",  "6 #",    6,  3,  5, false), /* 6  ( 6:00) */
        new WheelSector("Db", "Bbm", "C",  "5 b",    1, 10,  0, true ), /* 7  ( 7:00) */
        new WheelSector("Ab", "Fm",  "G",  "4 b",    8,  5,  7, true ), /* 8  ( 8:00) */
        new WheelSector("Eb", "Cm",  "D",  "3 b",    3,  0,  2, true ), /* 9  ( 9:00) */
        new WheelSector("Bb", "Gm",  "A",  "2 b",   10,  7,  9, true ), /* 10 (10:00) */
        new WheelSector("F",  "Dm",  "E",  "1 b",    5,  2,  4, true )  /* 11 (11:00) */
    };

    /** 6-string guitar chord voicing (-1 = muted X, 0 = open O, 1..15 = fret). */
    public static final class GuitarVoicing {
        public final int[] frets = new int[6];
        public String label = "";
    }

    /** Resolved harmonic identity of a chord at (keySector, sector, ring, seventhMode). */
    public static final class ResolvedChord {
        public int rootPc;
        public int qual;
        public String chordName = "";
        public String romanNum = "";
        public String funcDesc = "";
        public String notesStr = "";
        public String degreesStr = "";
        /** 0 = Non-diatonic, 1 = Tonic (I/vi), 2 = Subdominant (IV/ii), 3 = Dominant (V/iii/vii°). */
        public int diatonicFamily;
    }

    /** 4-bar chord progression preset definition. */
    public static final class ProgPreset {
        public final String name;
        public final int[] sectorOffsets;
        public final int[] rings;

        public ProgPreset(String name, int[] sectorOffsets, int[] rings) {
            this.name = name;
            this.sectorOffsets = sectorOffsets;
            this.rings = rings;
        }
    }

    public static final ProgPreset[] PRESETS = {
        new ProgPreset("I - V - vi - IV (Pop/Rock)",     new int[]{  0,  1,  0, 11 }, new int[]{ 0, 0, 1, 0 }),
        new ProgPreset("I - IV - V - I (Blues/Folk)",    new int[]{  0, 11,  1,  0 }, new int[]{ 0, 0, 0, 0 }),
        new ProgPreset("ii - V - I - vi (Jazz Turn)",    new int[]{ 11,  1,  0,  0 }, new int[]{ 1, 0, 0, 1 }),
        new ProgPreset("I - vi - IV - V (50s Classic)",  new int[]{  0,  0, 11,  1 }, new int[]{ 0, 1, 0, 0 }),
        new ProgPreset("vi - IV - I - V (Minor Anthem)", new int[]{  0, 11,  0,  1 }, new int[]{ 1, 0, 0, 0 })
    };

    /** 7 Diatonic Scale Degrees (I, ii, iii, IV, V, vi, vii°): sector offsets & ring indices. */
    public static final int[] DIATONIC_DEG_OFFSETS = { 0, 11, 1, 11, 1, 0, 0 };
    public static final int[] DIATONIC_DEG_RINGS   = { 0,  1, 1,  0, 0, 1, 2 };

    private static final int[][] OPEN_MAJ = {
        { -1,  3,  2,  0,  1,  0 }, /* C  */
        { -1,  4,  3,  1,  2,  1 }, /* Db */
        { -1, -1,  0,  2,  3,  2 }, /* D  */
        { -1,  6,  5,  3,  4,  3 }, /* Eb */
        {  0,  2,  2,  1,  0,  0 }, /* E  */
        {  1,  3,  3,  2,  1,  1 }, /* F  */
        {  2,  4,  4,  3,  2,  2 }, /* F# */
        {  3,  2,  0,  0,  0,  3 }, /* G  */
        {  4,  6,  6,  5,  4,  4 }, /* Ab */
        { -1,  0,  2,  2,  2,  0 }, /* A  */
        { -1,  1,  3,  3,  3,  1 }, /* Bb */
        { -1,  2,  4,  4,  4,  2 }  /* B  */
    };

    private static final int[][] OPEN_MIN = {
        { -1,  3,  5,  5,  4,  3 }, /* Cm  */
        { -1,  4,  6,  6,  5,  4 }, /* C#m */
        { -1, -1,  0,  2,  3,  1 }, /* Dm  */
        { -1,  6,  8,  8,  7,  6 }, /* D#m */
        {  0,  2,  2,  0,  0,  0 }, /* Em  */
        {  1,  3,  3,  1,  1,  1 }, /* Fm  */
        {  2,  4,  4,  2,  2,  2 }, /* F#m */
        {  3,  5,  5,  3,  3,  3 }, /* Gm  */
        {  4,  6,  6,  4,  4,  4 }, /* G#m */
        { -1,  0,  2,  2,  1,  0 }, /* Am  */
        { -1,  1,  3,  3,  2,  1 }, /* Bbm */
        { -1,  2,  4,  4,  3,  2 }  /* Bm  */
    };

    private static final int[][] OPEN_MAJ7 = {
        { -1,  3,  2,  0,  0,  0 }, /* Cmaj7  */
        { -1,  4,  3,  1,  1,  1 }, /* Dbmaj7 */
        { -1, -1,  0,  2,  2,  2 }, /* Dmaj7  */
        { -1,  6,  5,  3,  3,  3 }, /* Ebmaj7 */
        {  0,  2,  1,  1,  0,  0 }, /* Emaj7  */
        { -1, -1,  3,  2,  1,  0 }, /* Fmaj7  */
        {  2, -1,  3,  3,  2, -1 }, /* F#maj7 */
        {  3,  2,  0,  0,  0,  2 }, /* Gmaj7  */
        {  4, -1,  5,  5,  4, -1 }, /* Abmaj7 */
        { -1,  0,  2,  1,  2,  0 }, /* Amaj7  */
        { -1,  1,  3,  2,  3,  1 }, /* Bbmaj7 */
        { -1,  2,  4,  3,  4,  2 }  /* Bmaj7  */
    };

    private static final int[][] OPEN_MIN7 = {
        { -1,  3,  5,  3,  4,  3 }, /* Cm7  */
        { -1,  4,  6,  4,  5,  4 }, /* C#m7 */
        { -1, -1,  0,  2,  1,  1 }, /* Dm7  */
        { -1,  6,  8,  6,  7,  6 }, /* D#m7 */
        {  0,  2,  0,  0,  0,  0 }, /* Em7  */
        {  1,  3,  1,  1,  1,  1 }, /* Fm7  */
        {  2,  4,  2,  2,  2,  2 }, /* F#m7 */
        {  3,  5,  3,  3,  3,  3 }, /* Gm7  */
        {  4,  6,  4,  4,  4,  4 }, /* G#m7 */
        { -1,  0,  2,  0,  1,  0 }, /* Am7  */
        { -1,  1,  3,  1,  2,  1 }, /* Bbm7 */
        { -1,  2,  0,  2,  0,  2 }  /* Bm7  */
    };

    private static final int[][] OPEN_DOM7 = {
        { -1,  3,  2,  3,  1,  0 }, /* C7  */
        { -1,  4,  3,  4,  2, -1 }, /* Db7 */
        { -1, -1,  0,  2,  1,  2 }, /* D7  */
        { -1,  6,  5,  6,  4, -1 }, /* Eb7 */
        {  0,  2,  0,  1,  0,  0 }, /* E7  */
        {  1,  3,  1,  2,  1,  1 }, /* F7  */
        {  2,  4,  2,  3,  2,  2 }, /* F#7 */
        {  3,  2,  0,  0,  0,  1 }, /* G7  */
        {  4,  6,  4,  5,  4,  4 }, /* Ab7 */
        { -1,  0,  2,  0,  2,  0 }, /* A7  */
        { -1,  1,  3,  1,  3,  1 }, /* Bb7 */
        { -1,  2,  1,  2,  0,  2 }  /* B7  */
    };

    private static final String[] MAJ_ROMANS = {
        "I", "V", "V/V", "V/vi", "V/ii", "V/iii", "bV", "bII", "bVI", "bIII", "bVII", "IV"
    };
    private static final String[] MAJ_FUNCS = {
        "Tonic", "Dominant", "Sec Dom (II)", "Sec Dom (VI)", "Sec Dom (III)",
        "Sec Dom (VII)", "Tritone Sub", "Neapolitan bII", "Borrowed bVI",
        "Borrowed bIII", "Borrowed bVII", "Subdominant"
    };

    private static final String[] MIN_ROMANS = {
        "vi", "iii", "vii", "#iv", "#i", "#v", "biii", "bvii", "iv", "i", "v", "ii"
    };
    private static final String[] MIN_FUNCS = {
        "Relative Min", "Mediant Min", "Min v/III", "Chromatic Min", "Chromatic Min",
        "Chromatic Min", "Chromatic Min", "Borrowed bvii", "Borrowed iv",
        "Parallel Min (i)", "Modal Min (v)", "Supertonic"
    };

    /**
     * Base scale degree (1..7) of each sector's chord root relative to the active key
     * in the Outer Major ({@link #MAJ_ROOT_DEG}) and Inner Minor ({@link #MIN_ROOT_DEG}) rings,
     * indexed by {@code rel = (sector - keySector + 12) % 12}.
     */
    private static final int[] MAJ_ROOT_DEG = { 1, 5, 2, 6, 3, 7, 5, 2, 6, 3, 7, 4 };
    private static final int[] MIN_ROOT_DEG = { 6, 3, 7, 4, 1, 5, 3, 7, 4, 1, 5, 2 };
    /** Semitone offsets of the 7 natural Major scale degrees (1..7) above the key root. */
    private static final int[] MAJ_SCALE_SEMI = { 0, 2, 4, 5, 7, 9, 11 };

    /**
     * Formats a chord tone as a tertian scale-degree number relative to the active Major key.
     * For example, in Key of C ({@code keyRootPc = 0}):
     * <ul>
     *   <li>C Major ({@code baseDeg = 1}): {@code 1 - 3 - 5}</li>
     *   <li>F Major ({@code baseDeg = 4}): {@code 4 - 6 - 8}</li>
     *   <li>D Major Secondary Dominant ({@code baseDeg = 2}): {@code 2 - #4 - 6}</li>
     * </ul>
     */
    private static String formatScaleDegreeTone(int keyRootPc, int degNum, int notePc) {
        int natPc = (keyRootPc + MAJ_SCALE_SEMI[(degNum - 1) % 7]) % 12;
        int diff = (notePc - natPc + 12) % 12;
        if (diff == 1)  return "#" + degNum;
        if (diff == 11) return "b" + degNum;
        if (diff == 2)  return "##" + degNum;
        if (diff == 10) return "bb" + degNum;
        return String.valueOf(degNum);
    }

    /**
     * Populates {@code out} with a 6-string guitar chord voicing for {@code (rootPc, qual)}
     * and one of the 3 movable voicing families:
     * <ul>
     *   <li>{@code voicingIdx = 0}: Open / CAGED shape (or compact 4-string rootless/shell shape for dim/m7b5)</li>
     *   <li>{@code voicingIdx = 1}: E-shape 6th-string root barre chord</li>
     *   <li>{@code voicingIdx = 2}: A-shape 5th-string root barre chord</li>
     * </ul>
     */
    public static void buildGuitarVoicing(int rootPc, int qual, int voicingIdx, GuitarVoicing out) {
        rootPc = ((rootPc % 12) + 12) % 12;
        voicingIdx = ((voicingIdx % 3) + 3) % 3;

        int eFret = (rootPc - 4 + 12) % 12;
        if (eFret == 0) eFret = 12;
        int aFret = (rootPc - 9 + 12) % 12;
        if (aFret == 0) aFret = 12;

        if (voicingIdx == 0) {
            out.label = "1/3 Open/CAGED";
            if (qual == QUAL_MAJ) {
                System.arraycopy(OPEN_MAJ[rootPc], 0, out.frets, 0, 6);
            } else if (qual == QUAL_MIN) {
                System.arraycopy(OPEN_MIN[rootPc], 0, out.frets, 0, 6);
            } else if (qual == QUAL_MAJ7) {
                System.arraycopy(OPEN_MAJ7[rootPc], 0, out.frets, 0, 6);
            } else if (qual == QUAL_MIN7) {
                System.arraycopy(OPEN_MIN7[rootPc], 0, out.frets, 0, 6);
            } else if (qual == QUAL_DOM7) {
                System.arraycopy(OPEN_DOM7[rootPc], 0, out.frets, 0, 6);
            } else if (qual == QUAL_M7B5) {
                int dFret = (rootPc - 2 + 12) % 12;
                out.frets[0] = -1;
                out.frets[1] = -1;
                out.frets[2] = dFret;
                out.frets[3] = dFret + 1;
                out.frets[4] = dFret + 1;
                out.frets[5] = dFret + 1;
            } else {
                out.frets[0] = -1;
                out.frets[1] = aFret;
                out.frets[2] = aFret + 1;
                out.frets[3] = aFret + 2;
                out.frets[4] = aFret + 1;
                out.frets[5] = -1;
            }
        } else if (voicingIdx == 1) {
            out.label = "2/3 E-Barre (6str)";
            int f = eFret;
            switch (qual) {
                case QUAL_MAJ:
                    out.frets[0] = f; out.frets[1] = f + 2; out.frets[2] = f + 2;
                    out.frets[3] = f + 1; out.frets[4] = f; out.frets[5] = f;
                    break;
                case QUAL_MIN:
                    out.frets[0] = f; out.frets[1] = f + 2; out.frets[2] = f + 2;
                    out.frets[3] = f; out.frets[4] = f; out.frets[5] = f;
                    break;
                case QUAL_MAJ7:
                    out.frets[0] = f; out.frets[1] = -1; out.frets[2] = f + 1;
                    out.frets[3] = f + 1; out.frets[4] = f; out.frets[5] = -1;
                    break;
                case QUAL_MIN7:
                    out.frets[0] = f; out.frets[1] = f + 2; out.frets[2] = f;
                    out.frets[3] = f; out.frets[4] = f; out.frets[5] = f;
                    break;
                case QUAL_DOM7:
                    out.frets[0] = f; out.frets[1] = f + 2; out.frets[2] = f;
                    out.frets[3] = f + 1; out.frets[4] = f; out.frets[5] = f;
                    break;
                default:
                    out.frets[0] = eFret; out.frets[1] = eFret + 1;
                    out.frets[2] = (qual == QUAL_M7B5) ? eFret : (eFret + 2);
                    out.frets[3] = eFret; out.frets[4] = eFret - 1; out.frets[5] = -1;
                    break;
            }
        } else {
            out.label = "3/3 A-Barre (5str)";
            int f = aFret;
            switch (qual) {
                case QUAL_MAJ:
                    out.frets[0] = -1; out.frets[1] = f; out.frets[2] = f + 2;
                    out.frets[3] = f + 2; out.frets[4] = f + 2; out.frets[5] = f;
                    break;
                case QUAL_MIN:
                    out.frets[0] = -1; out.frets[1] = f; out.frets[2] = f + 2;
                    out.frets[3] = f + 2; out.frets[4] = f + 1; out.frets[5] = f;
                    break;
                case QUAL_MAJ7:
                    out.frets[0] = -1; out.frets[1] = f; out.frets[2] = f + 2;
                    out.frets[3] = f + 1; out.frets[4] = f + 2; out.frets[5] = f;
                    break;
                case QUAL_MIN7:
                    out.frets[0] = -1; out.frets[1] = f; out.frets[2] = f + 2;
                    out.frets[3] = f; out.frets[4] = f + 1; out.frets[5] = f;
                    break;
                case QUAL_DOM7:
                    out.frets[0] = -1; out.frets[1] = f; out.frets[2] = f + 2;
                    out.frets[3] = f; out.frets[4] = f + 2; out.frets[5] = f;
                    break;
                default:
                    out.frets[0] = -1; out.frets[1] = aFret; out.frets[2] = aFret + 1;
                    out.frets[3] = (qual == QUAL_M7B5) ? aFret : (aFret + 2);
                    out.frets[4] = aFret + 1; out.frets[5] = -1;
                    break;
            }
        }
    }

    /**
     * Resolves the harmonic identity of a chord on the Circle of Fifths given:
     * <ul>
     *   <li>{@code keySec}: Active key sector (0..11, where 0 = C Major)</li>
     *   <li>{@code sec}: Selected wheel sector (0..11)</li>
     *   <li>{@code ring}: Selected ring (0 = Outer Major, 1 = Inner Minor, 2 = Center vii° hub)</li>
     *   <li>{@code seventh}: {@code false} for 3-note triads, {@code true} for 4-note 7th chords</li>
     * </ul>
     * Writes chord symbol, Roman numeral, harmonic function description, spelled chord notes,
     * and active-key scale-degree numbers into {@code out}.
     */
    public static void resolveChordAt(int keySec, int sec, int ring, boolean seventh, ResolvedChord out) {
        keySec = ((keySec % 12) + 12) % 12;
        sec = ((sec % 12) + 12) % 12;
        int rel = (sec - keySec + 12) % 12;
        boolean flats = WHEEL[keySec].useFlats;
        String[] noteTbl = flats ? NOTE_FLAT : NOTE_SHARP;

        if (ring == 2) {
            out.rootPc = WHEEL[keySec].dimPc;
            out.qual = seventh ? QUAL_M7B5 : QUAL_DIM;
            out.chordName = WHEEL[keySec].dimRootName + (seventh ? "m7b5" : "dim");
            out.romanNum = seventh ? "viim7b5" : "viidim";
            out.funcDesc = "Leading-Tone Dim";
            out.diatonicFamily = 3;
        } else if (ring == 0) {
            out.rootPc = WHEEL[sec].majPc;
            if (rel == 0) {
                out.qual = seventh ? QUAL_MAJ7 : QUAL_MAJ;
                out.romanNum = seventh ? "Imaj7" : "I";
                out.funcDesc = "Tonic Major";
                out.diatonicFamily = 1;
            } else if (rel == 11) {
                out.qual = seventh ? QUAL_MAJ7 : QUAL_MAJ;
                out.romanNum = seventh ? "IVmaj7" : "IV";
                out.funcDesc = "Subdominant";
                out.diatonicFamily = 2;
            } else if (rel == 1) {
                out.qual = seventh ? QUAL_DOM7 : QUAL_MAJ;
                out.romanNum = seventh ? "V7" : "V";
                out.funcDesc = "Dominant";
                out.diatonicFamily = 3;
            } else {
                out.qual = seventh ? QUAL_DOM7 : QUAL_MAJ;
                out.diatonicFamily = 0;
                out.romanNum = MAJ_ROMANS[rel] + (seventh ? "7" : "");
                out.funcDesc = MAJ_FUNCS[rel];
            }
            String suffix = (out.qual == QUAL_MAJ7) ? "maj7" :
                            (out.qual == QUAL_DOM7) ? "7" : "";
            out.chordName = WHEEL[sec].majName + suffix;
        } else {
            out.rootPc = WHEEL[sec].minPc;
            out.qual = seventh ? QUAL_MIN7 : QUAL_MIN;
            if (rel == 0) {
                out.romanNum = seventh ? "vi7" : "vi";
                out.funcDesc = "Relative Minor";
                out.diatonicFamily = 1;
            } else if (rel == 11) {
                out.romanNum = seventh ? "ii7" : "ii";
                out.funcDesc = "Supertonic Min";
                out.diatonicFamily = 2;
            } else if (rel == 1) {
                out.romanNum = seventh ? "iii7" : "iii";
                out.funcDesc = "Mediant Minor";
                out.diatonicFamily = 3;
            } else {
                out.diatonicFamily = 0;
                out.romanNum = MIN_ROMANS[rel] + (seventh ? "7" : "");
                out.funcDesc = MIN_FUNCS[rel];
            }
            out.chordName = WHEEL[sec].minName + (seventh ? "7" : "");
        }

        int i3 = (out.qual == QUAL_MAJ || out.qual == QUAL_MAJ7 || out.qual == QUAL_DOM7) ? 4 : 3;
        int i5 = (out.qual == QUAL_DIM || out.qual == QUAL_M7B5) ? 6 : 7;
        int n1 = out.rootPc;
        int n2 = (out.rootPc + i3) % 12;
        int n3 = (out.rootPc + i5) % 12;

        int keyRootPc = WHEEL[keySec].majPc;
        int baseDeg = (ring == 2) ? 7 : (ring == 0) ? MAJ_ROOT_DEG[rel] : MIN_ROOT_DEG[rel];
        String d1 = formatScaleDegreeTone(keyRootPc, baseDeg, n1);
        String d2 = formatScaleDegreeTone(keyRootPc, baseDeg + 2, n2);
        String d3 = formatScaleDegreeTone(keyRootPc, baseDeg + 4, n3);

        if (out.qual == QUAL_MAJ7 || out.qual == QUAL_MIN7 || out.qual == QUAL_DOM7 || out.qual == QUAL_M7B5) {
            int i7 = (out.qual == QUAL_MAJ7) ? 11 : 10;
            int n4 = (out.rootPc + i7) % 12;
            String d4 = formatScaleDegreeTone(keyRootPc, baseDeg + 6, n4);
            out.notesStr = noteTbl[n1] + " - " + noteTbl[n2] + " - " + noteTbl[n3] + " - " + noteTbl[n4];
            out.degreesStr = d1 + " - " + d2 + " - " + d3 + " - " + d4;
        } else {
            out.notesStr = noteTbl[n1] + " - " + noteTbl[n2] + " - " + noteTbl[n3];
            out.degreesStr = d1 + " - " + d2 + " - " + d3;
        }
    }
}
