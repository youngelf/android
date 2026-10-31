package com.eggwall.sightread;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure, stateless music-theory and sight-reading generator engine for SightRead.
 *
 * <p>Supports:
 * <ul>
 *   <li>3 Clef modes: Treble Clef (8vb guitar / standard), Bass Clef, and Grand Staff</li>
 *   <li>7 Key Signatures: C Major, G (1#), D (2#), A (3#), E (4#), F (1b), Bb (2b)</li>
 *   <li>5 Guitar Neck Positions: Open (0-4fr), Pos 2 (2-5fr), Pos 5 (5-8fr), Pos 7 (7-10fr), Pos 9 (9-12fr)</li>
 *   <li>4 Progressive Complexity Levels (Exercises #01..#99) with quarter notes and beamed eighth pairs</li>
 *   <li>Dual-input pitch matching for both the 6-string Guitar Fretboard and 7-note Piano keyboard</li>
 * </ul>
 */
public final class SightReadTheory {

    private SightReadTheory() {}

    /** Standard guitar open-string MIDI pitches: E2(40), A2(45), D3(50), G3(55), B3(59), E4(64). */
    public static final int[] OPEN_MIDI = { 40, 45, 50, 55, 59, 64 };

    /** Guitar string labels from 6th string (index 0) to 1st string (index 5). */
    public static final char[] STRING_LABELS = { 'E', 'A', 'D', 'G', 'B', 'e' };

    /** Natural note letter names for diatonic indices 0..6 (C, D, E, F, G, A, B). */
    public static final String[] NATURAL_NAMES = { "C", "D", "E", "F", "G", "A", "B" };

    /** Chromatic note names (sharp spelling). */
    public static final String[] CHROMATIC_SHARP = {
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };

    /** Chromatic note names (flat spelling). */
    public static final String[] CHROMATIC_FLAT = {
        "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"
    };

    public static final int CLEF_TREBLE = 0;
    public static final int CLEF_BASS   = 1;
    public static final int CLEF_GRAND  = 2;
    public static final int NUM_CLEFS   = 3;

    public static final String[] CLEF_NAMES = {
        "TREBLE", "BASS", "GRAND"
    };

    public static final int MODE_ETUDE  = 0;
    public static final int MODE_QUIZ   = 1;
    public static final int MODE_SCROLL = 2;
    public static final int NUM_MODES   = 3;

    public static final String[] MODE_NAMES = {
        "ETUDE", "QUIZ", "FLOW"
    };

    public static final int MIN_BPM = 40;
    public static final int MAX_BPM = 180;
    public static final int DEFAULT_BPM = 84;

    public static final int MAX_PIECE_NOTES = 16;
    public static final int MAX_SCALE_POOL  = 36;
    public static final int NUM_EXERCISES   = 24;

    /** Concise musical titles for the 24 progressive sight-reading exercises. */
    public static final String[] EXERCISE_NAMES = {
        "Open Stepwise",
        "Scale Walk",
        "Neighbor Tone",
        "Running Eighths",
        "Arch Melody",
        "Scale Cascade",
        "Rhythmic Steps",
        "Cadence Study",
        "Broken Thirds",
        "Triad Outline",
        "Skipping Stone",
        "Arpeggio Etude",
        "Interval Weave",
        "String Crossing",
        "Position Builder",
        "Classical Etude",
        "Lyric Prelude",
        "Sonata Motif",
        "Baroque Study",
        "Fretboard Leap",
        "Perpetual Motion",
        "Chamber Theme",
        "Virtuoso Study",
        "Concert Caprice"
    };

    /** Returns the formatted exercise label (e.g., {@code "Ex 1: Open Stepwise"}). */
    public static String exerciseLabel(int exerciseNum) {
        int safe = Math.max(1, exerciseNum);
        String title = EXERCISE_NAMES[(safe - 1) % EXERCISE_NAMES.length];
        return "Ex " + safe + ": " + title;
    }

    /** Treble staff lines (bottom to top) in diatonic steps relative to Middle C (step 0): E=2, G=4, B=6, D=8, F=10. */
    public static final int[] TREBLE_LINE_STEPS = { 2, 4, 6, 8, 10 };

    /** Bass staff lines (bottom to top) in diatonic steps relative to Middle C (step 0): G=-10, B=-8, D=-6, F=-4, A=-2. */
    public static final int[] BASS_LINE_STEPS = { -10, -8, -6, -4, -2 };

    /** Treble clef sharp steps: F(10), C(7), G(11), D(8). */
    public static final int[] TREBLE_SHARP_STEPS = { 10, 7, 11, 8 };

    /** Treble clef flat steps: B(6), E(9). */
    public static final int[] TREBLE_FLAT_STEPS = { 6, 9 };

    /** Bass clef sharp steps: F(-4), C(-7), G(-3), D(-6). */
    public static final int[] BASS_SHARP_STEPS = { -4, -7, -3, -6 };

    /** Bass clef flat steps: B(-8), E(-5). */
    public static final int[] BASS_FLAT_STEPS = { -8, -5 };

    /** Defines a 4-fret guitar neck position window. */
    public static final class NeckPosition {
        public final String shortName;
        public final String fullName;
        public final int minFret;
        public final int maxFret;
        public final int baseFret;

        public NeckPosition(String shortName, String fullName, int minFret, int maxFret, int baseFret) {
            this.shortName = shortName;
            this.fullName = fullName;
            this.minFret = minFret;
            this.maxFret = maxFret;
            this.baseFret = baseFret;
        }
    }

    public static final NeckPosition[] POSITIONS = {
        new NeckPosition("OPEN (0-4fr)",  "Open Position (Frets 0-4)",  0,  4, 1),
        new NeckPosition("POS 2 (2-5fr)", "2nd Position (Frets 2-5)",   2,  5, 2),
        new NeckPosition("POS 5 (5-8fr)", "5th Position (Frets 5-8)",   5,  8, 5),
        new NeckPosition("POS 7 (7-10fr)","7th Position (Frets 7-10)",  7, 10, 7),
        new NeckPosition("POS 9 (9-12fr)","9th Position (Frets 9-12)",  9, 12, 9)
    };
    public static final int NUM_POSITIONS = POSITIONS.length;

    /** Defines one of the 7 supported key signatures and its diatonic scale spellings. */
    public static final class KeySigDef {
        public final String name;
        public final String badge;
        public final int tonicPc;
        public final int sharpsOrFlats;
        public final int[] degPc;
        public final String[] degName;

        public KeySigDef(String name, String badge, int tonicPc, int sharpsOrFlats,
                         int[] degPc, String[] degName) {
            this.name = name;
            this.badge = badge;
            this.tonicPc = tonicPc;
            this.sharpsOrFlats = sharpsOrFlats;
            this.degPc = degPc;
            this.degName = degName;
        }
    }

    public static final KeySigDef[] KEYS = {
        new KeySigDef("C Major",  "C Maj",   0,  0,
                new int[]{ 0, 2, 4, 5, 7, 9, 11 },
                new String[]{ "C", "D", "E", "F", "G", "A", "B" }),
        new KeySigDef("G Major",  "G (1#)",  7,  1,
                new int[]{ 0, 2, 4, 6, 7, 9, 11 },
                new String[]{ "C", "D", "E", "F#", "G", "A", "B" }),
        new KeySigDef("D Major",  "D (2#)",  2,  2,
                new int[]{ 1, 2, 4, 6, 7, 9, 11 },
                new String[]{ "C#", "D", "E", "F#", "G", "A", "B" }),
        new KeySigDef("A Major",  "A (3#)",  9,  3,
                new int[]{ 1, 2, 4, 6, 8, 9, 11 },
                new String[]{ "C#", "D", "E", "F#", "G#", "A", "B" }),
        new KeySigDef("E Major",  "E (4#)",  4,  4,
                new int[]{ 1, 3, 4, 6, 8, 9, 11 },
                new String[]{ "C#", "D#", "E", "F#", "G#", "A", "B" }),
        new KeySigDef("F Major",  "F (1b)",  5, -1,
                new int[]{ 0, 2, 4, 5, 7, 9, 10 },
                new String[]{ "C", "D", "E", "F", "G", "A", "Bb" }),
        new KeySigDef("Bb Major", "Bb (2b)", 10, -2,
                new int[]{ 0, 2, 3, 5, 7, 9, 10 },
                new String[]{ "C", "D", "Eb", "F", "G", "A", "Bb" })
    };
    public static final int NUM_KEYS = KEYS.length;

    /** Represents a single musical note on the staff and guitar neck. */
    public static final class StaffNote {
        public int str;            // Guitar string 0 (6th E) .. 5 (1st e)
        public int fret;           // Fret number within active NeckPosition
        public int finger;         // 0=Open, 1=Index, 2=Middle, 3=Ring, 4=Pinky
        public int midi;           // Sounding MIDI pitch (40=E2 .. 76=E5)
        public int diatonicStep;   // Step relative to C3=0 (-5=E2 .. +16=E5)
        public int letterIndex;    // Natural letter index 0=C .. 6=B
        public int durEighths;     // 2 = Quarter Note, 1 = Eighth Note
        public int barIdx;         // Measure index (0 or 1)
        public int eighthInBar;    // Eighth-note offset within the measure (0..7)
        public boolean beamWithNext;
        public boolean beamFromPrev;
        public String label;       // e.g. "C4", "F#3"
        public String pitchName;   // e.g. "C", "F#", "Bb"

        public StaffNote() {
            this.label = "C3";
            this.pitchName = "C";
        }

        public StaffNote copy() {
            StaffNote c = new StaffNote();
            c.str = this.str;
            c.fret = this.fret;
            c.finger = this.finger;
            c.midi = this.midi;
            c.diatonicStep = this.diatonicStep;
            c.letterIndex = this.letterIndex;
            c.durEighths = this.durEighths;
            c.barIdx = this.barIdx;
            c.eighthInBar = this.eighthInBar;
            c.beamWithNext = this.beamWithNext;
            c.beamFromPrev = this.beamFromPrev;
            c.label = this.label;
            c.pitchName = this.pitchName;
            return c;
        }
    }

    /** Represents a complete 2-measure generated sight-reading exercise. */
    public static final class SightPiece {
        public int exerciseNum;
        public int level;
        public int numBars;
        public int beatsPerBar;
        public int numNotes;
        public final StaffNote[] notes = new StaffNote[MAX_PIECE_NOTES];

        public SightPiece() {
            for (int i = 0; i < MAX_PIECE_NOTES; i++) {
                notes[i] = new StaffNote();
            }
        }
    }

    /** Returns the sounding MIDI pitch for a guitar string (0=6th..5=1st) and fret (0..12). */
    public static int midiForStringFret(int str, int fret) {
        if (str < 0) str = 0;
        if (str > 5) str = 5;
        if (fret < 0) fret = 0;
        return OPEN_MIDI[str] + fret;
    }

    /** Returns the progressive difficulty level (1..4) for an exercise number (1..99). */
    public static int levelForExercise(int exerciseNum) {
        if (exerciseNum <= 3) return 1;
        if (exerciseNum <= 8) return 2;
        if (exerciseNum <= 15) return 3;
        return 4;
    }

    /** Returns a concise human-readable description of a progressive level (1..4). */
    public static String levelDescription(int level) {
        switch (level) {
            case 1: return "Stepwise Quarters";
            case 2: return "Quarters & Eighths";
            case 3: return "Eighths & 3rd Skips";
            default: return "Melodic Etudes";
        }
    }

    /**
     * Maps a note's diatonic step (relative to guitar C3 = 0) into the visual staff step
     * for the active clef mode.
     *
     * <ul>
     *   <li>In {@link #CLEF_TREBLE}: returns {@code diatonicStep} directly (step 0 = 1st ledger line below Treble staff).</li>
     *   <li>In {@link #CLEF_BASS}: shifts into Bass clef register so lower guitar notes sit cleanly on Bass lines (-10..-2).</li>
     *   <li>In {@link #CLEF_GRAND}: returns {@code diatonicStep} (positive/zero on upper Treble, negative on lower Bass).</li>
     * </ul>
     */
    public static int visualStepForClef(int diatonicStep, int clefMode) {
        if (clefMode == CLEF_BASS) {
            return diatonicStep - 7;
        }
        return diatonicStep;
    }

    /**
     * Computes all ledger-line steps required to render a note at {@code visualStep}
     * on either the Treble staff ({@code isBassStaff == false}) or Bass staff ({@code isBassStaff == true}).
     */
    public static List<Integer> computeLedgerSteps(int visualStep, boolean isBassStaff) {
        List<Integer> ledgers = new ArrayList<>();
        if (!isBassStaff) {
            // Treble staff lines are 2, 4, 6, 8, 10.
            // Below staff: 0, -2, -4, ...
            if (visualStep <= 0) {
                for (int st = 0; st >= visualStep; st -= 2) {
                    ledgers.add(st);
                }
            } else if (visualStep >= 12) {
                // Above staff: 12, 14, 16, ...
                for (int st = 12; st <= visualStep; st += 2) {
                    ledgers.add(st);
                }
            }
        } else {
            // Bass staff lines are -10, -8, -6, -4, -2.
            // Above Bass staff: 0 (Middle C), 2, ...
            if (visualStep >= 0) {
                for (int st = 0; st <= visualStep; st += 2) {
                    ledgers.add(st);
                }
            } else if (visualStep <= -12) {
                // Below Bass staff: -12, -14, ...
                for (int st = -12; st >= visualStep; st -= 2) {
                    ledgers.add(st);
                }
            }
        }
        return ledgers;
    }

    /**
     * Checks whether a tapped guitar (string, fret) matches the target {@link StaffNote}.
     * Accepts either the exact same MIDI pitch anywhere on the neck or the exact target position.
     */
    public static boolean matchesNoteByFretboard(StaffNote target, int tappedStr, int tappedFret, boolean strictPosition) {
        if (target == null) return false;
        if (strictPosition) {
            return target.str == tappedStr && target.fret == tappedFret;
        }
        int tappedMidi = midiForStringFret(tappedStr, tappedFret);
        return tappedMidi == target.midi;
    }

    /**
     * Checks whether a tapped piano/note button (diatonic degree 0=C .. 6=B in the active key,
     * or pitch class 0..11) matches the target {@link StaffNote}.
     */
    public static boolean matchesNoteByLetterIndex(StaffNote target, int letterIdx) {
        if (target == null) return false;
        return target.letterIndex == letterIdx;
    }

    public static boolean matchesNoteByPitchClass(StaffNote target, int pitchClass) {
        if (target == null) return false;
        int normPc = ((pitchClass % 12) + 12) % 12;
        return (target.midi % 12) == normPc;
    }

    /**
     * Builds the ascending pool of diatonic notes strictly inside {@code posIdx} and {@code keyIdx}.
     */
    public static int buildPositionScalePool(int posIdx, int keyIdx, StaffNote[] pool) {
        int safePos = Math.max(0, Math.min(NUM_POSITIONS - 1, posIdx));
        int safeKey = Math.max(0, Math.min(NUM_KEYS - 1, keyIdx));
        NeckPosition pos = POSITIONS[safePos];
        KeySigDef key = KEYS[safeKey];

        int count = 0;
        int lastMidi = -1;

        for (int s = 0; s < 6; s++) {
            int nextStrMinMidi = (s < 5) ? (OPEN_MIDI[s + 1] + pos.minFret) : 999;
            for (int f = pos.minFret; f <= pos.maxFret; f++) {
                int midi = OPEN_MIDI[s] + f;
                if (midi >= nextStrMinMidi && s < 5) break;
                if (midi <= lastMidi) continue;

                int pc = midi % 12;
                int foundDeg = -1;
                for (int d = 0; d < 7; d++) {
                    if (key.degPc[d] == pc) {
                        foundDeg = d;
                        break;
                    }
                }
                if (foundDeg < 0) continue;

                int cBaseMidi = midi - key.degPc[foundDeg];
                int octaveFromC3 = (cBaseMidi - 48) / 12;
                if (cBaseMidi < 48) {
                    octaveFromC3 = -((48 - cBaseMidi + 11) / 12);
                }
                int diatonicStep = octaveFromC3 * 7 + foundDeg;

                int finger = 0;
                if (f > 0) {
                    finger = f - pos.baseFret + 1;
                    if (finger < 1) finger = 1;
                    if (finger > 4) finger = 4;
                }

                StaffNote n = pool[count];
                if (n == null) {
                    n = new StaffNote();
                    pool[count] = n;
                }
                n.str = s;
                n.fret = f;
                n.finger = finger;
                n.midi = midi;
                n.diatonicStep = diatonicStep;
                n.letterIndex = foundDeg;
                n.durEighths = 2;
                n.barIdx = 0;
                n.eighthInBar = 0;
                n.beamWithNext = false;
                n.beamFromPrev = false;
                n.pitchName = key.degName[foundDeg];
                int octNum = (cBaseMidi / 12) - 1;
                n.label = key.degName[foundDeg] + octNum;

                count++;
                lastMidi = midi;
                if (count >= pool.length) {
                    return count;
                }
            }
        }
        return count;
    }

    /**
     * Programmatically generates a deterministic 2-measure (8-beat, 16-eighth-note) sight-reading piece
     * strictly constrained to {@code posIdx} and {@code keyIdx}.
     */
    public static void generateSightPiece(SightPiece piece, int exerciseNum, int posIdx, int keyIdx) {
        if (exerciseNum < 1) exerciseNum = 1;
        if (exerciseNum > 99) exerciseNum = 99;
        int safePos = Math.max(0, Math.min(NUM_POSITIONS - 1, posIdx));
        int safeKey = Math.max(0, Math.min(NUM_KEYS - 1, keyIdx));

        piece.exerciseNum = exerciseNum;
        piece.numBars = 2;
        piece.beatsPerBar = 4;
        piece.level = levelForExercise(exerciseNum);

        StaffNote[] pool = new StaffNote[MAX_SCALE_POOL];
        for (int i = 0; i < MAX_SCALE_POOL; i++) {
            pool[i] = new StaffNote();
        }
        int poolLen = buildPositionScalePool(safePos, safeKey, pool);
        if (poolLen <= 0) {
            piece.numNotes = 0;
            return;
        }

        KeySigDef key = KEYS[safeKey];

        // Find tonic note index closest to the middle of the Treble Staff (diatonicStep ~ 6)
        int minPoolIdx = 0;
        if (piece.level <= 2) {
            for (int i = 0; i < poolLen - 5; i++) {
                if (pool[i].diatonicStep >= 0) {
                    minPoolIdx = i;
                    break;
                }
            }
        }
        int tonicIdx = Math.max(minPoolIdx, poolLen * 2 / 3);
        int bestDist = 999;
        for (int i = minPoolIdx; i < poolLen; i++) {
            if ((pool[i].midi % 12) == key.tonicPc) {
                int d = Math.abs(pool[i].diatonicStep - 6);
                if (d < bestDist) {
                    bestDist = d;
                    tonicIdx = i;
                }
            }
        }

        // Deterministic unsigned 32-bit LCG PRNG matching sightread.c
        long rng = ((long) exerciseNum * 747796405L
                  + (long) safePos * 2891336453L
                  + (long) safeKey * 1013904223L
                  + 12345L) & 0xFFFFFFFFL;

        boolean[] beatIsEighth = new boolean[8];
        int eighthPairTarget;
        if (piece.level == 1) {
            eighthPairTarget = (exerciseNum == 1) ? 1 : 2;
        } else if (piece.level == 2) {
            eighthPairTarget = 3;
        } else if (piece.level == 3) {
            eighthPairTarget = 4;
        } else {
            eighthPairTarget = 5;
        }

        int pairsPlaced = 0;
        for (int attempt = 0; attempt < 24 && pairsPlaced < eighthPairTarget; attempt++) {
            rng = (rng * 1664525L + 1013904223L) & 0xFFFFFFFFL;
            int b = (int) (((rng >>> 16) & 0xFFFFL) % 7L); // Beats 0..6 (beat 7 is quarter cadence)
            if (!beatIsEighth[b]) {
                beatIsEighth[b] = true;
                pairsPlaced++;
            }
        }

        int curPoolIdx = tonicIdx;
        int dir = ((exerciseNum & 1) == 0) ? 1 : -1;
        int nIdx = 0;

        for (int beat = 0; beat < 8 && nIdx < MAX_PIECE_NOTES; beat++) {
            int bar = beat / 4;
            int beatInBar = beat % 4;
            boolean isPair = beatIsEighth[beat] && (nIdx + 1 < MAX_PIECE_NOTES);
            int subNotes = isPair ? 2 : 1;

            for (int sub = 0; sub < subNotes; sub++) {
                if (beat == 0 && sub == 0) {
                    curPoolIdx = tonicIdx;
                } else if (beat == 7 && sub == 0) {
                    int nearestT = tonicIdx;
                    int minD = 999;
                    for (int i = 0; i < poolLen; i++) {
                        if ((pool[i].midi % 12) == key.tonicPc) {
                            int d = Math.abs(i - curPoolIdx);
                            if (d < minD) {
                                minD = d;
                                nearestT = i;
                            }
                        }
                    }
                    curPoolIdx = nearestT;
                } else {
                    rng = (rng * 1664525L + 1013904223L) & 0xFFFFFFFFL;
                    int r = (int) (((rng >>> 16) & 0xFFFFL) % 100L);
                    int stepDelta;
                    if (piece.level == 1) {
                        if (r < 25) dir = -dir;
                        stepDelta = dir;
                    } else if (piece.level == 2) {
                        if (r < 30) dir = -dir;
                        stepDelta = (r > 80 && sub == 0) ? (dir * 2) : dir;
                    } else {
                        if (r < 35) dir = -dir;
                        if (sub == 1) stepDelta = dir;
                        else if (r > 85) stepDelta = dir * 3;
                        else if (r > 65) stepDelta = dir * 2;
                        else stepDelta = dir;
                    }

                    curPoolIdx += stepDelta;
                    if (curPoolIdx >= poolLen - 1) {
                        curPoolIdx = poolLen - 1;
                        dir = -1;
                    } else if (curPoolIdx <= minPoolIdx) {
                        curPoolIdx = minPoolIdx;
                        dir = 1;
                    }
                }

                StaffNote src = pool[curPoolIdx];
                StaffNote dst = piece.notes[nIdx];
                dst.str = src.str;
                dst.fret = src.fret;
                dst.finger = src.finger;
                dst.midi = src.midi;
                dst.diatonicStep = src.diatonicStep;
                dst.letterIndex = src.letterIndex;
                dst.label = src.label;
                dst.pitchName = src.pitchName;
                dst.durEighths = isPair ? 1 : 2;
                dst.barIdx = bar;
                dst.eighthInBar = beatInBar * 2 + sub;
                dst.beamWithNext = (isPair && sub == 0);
                dst.beamFromPrev = (isPair && sub == 1);
                nIdx++;
            }
        }
        piece.numNotes = nIdx;
    }
}
