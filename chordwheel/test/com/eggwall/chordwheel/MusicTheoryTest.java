package com.eggwall.chordwheel;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link MusicTheory}: Circle of Fifths sector intervals, diatonic & chromatic
 * chord resolution, scale-degree number composition (e.g. C = 1-3-5, F = 4-6-8 in Key of C),
 * CAGED / Barre guitar voicing pitch-class validity across all 252 voicings, and progression presets.
 */
public class MusicTheoryTest {

    @Test
    public void testCircleOfFifthsSectorIntervals() {
        assertEquals(12, MusicTheory.WHEEL.length);
        for (int s = 0; s < 12; s++) {
            MusicTheory.WheelSector cur = MusicTheory.WHEEL[s];
            MusicTheory.WheelSector next = MusicTheory.WHEEL[(s + 1) % 12];

            /* Each clockwise step advances by a perfect fifth (+7 semitones mod 12) */
            assertEquals("Sector " + s + " -> " + ((s + 1) % 12) + " must advance +7 semitones",
                    (cur.majPc + 7) % 12, next.majPc);

            /* Relative minor is 3 semitones below major tonic (+9 mod 12) */
            assertEquals("Relative minor of " + cur.majName,
                    (cur.majPc + 9) % 12, cur.minPc);

            /* Leading-tone diminished root is 1 semitone below major tonic (+11 mod 12) */
            assertEquals("Leading-tone dim of " + cur.majName,
                    (cur.majPc + 11) % 12, cur.dimPc);
        }
    }

    @Test
    public void testScaleDegreeNumbersInKeyOfC() {
        MusicTheory.ResolvedChord r = new MusicTheory.ResolvedChord();

        /* 1. C Major (I in Key of C) -> C - E - G, 1 - 3 - 5 */
        MusicTheory.resolveChordAt(0, 0, 0, false, r);
        assertEquals("C", r.chordName);
        assertEquals("I", r.romanNum);
        assertEquals("Tonic Major", r.funcDesc);
        assertEquals("C - E - G", r.notesStr);
        assertEquals("1 - 3 - 5", r.degreesStr);

        /* 2. F Major (IV in Key of C) -> F - A - C, 4 - 6 - 8 */
        MusicTheory.resolveChordAt(0, 11, 0, false, r);
        assertEquals("F", r.chordName);
        assertEquals("IV", r.romanNum);
        assertEquals("Subdominant", r.funcDesc);
        assertEquals("F - A - C", r.notesStr);
        assertEquals("4 - 6 - 8", r.degreesStr);

        /* 3. G Major (V in Key of C) -> G - B - D, 5 - 7 - 9 */
        MusicTheory.resolveChordAt(0, 1, 0, false, r);
        assertEquals("G", r.chordName);
        assertEquals("V", r.romanNum);
        assertEquals("Dominant", r.funcDesc);
        assertEquals("G - B - D", r.notesStr);
        assertEquals("5 - 7 - 9", r.degreesStr);

        /* 4. Dm (ii in Key of C) -> D - F - A, 2 - 4 - 6 */
        MusicTheory.resolveChordAt(0, 11, 1, false, r);
        assertEquals("Dm", r.chordName);
        assertEquals("ii", r.romanNum);
        assertEquals("D - F - A", r.notesStr);
        assertEquals("2 - 4 - 6", r.degreesStr);

        /* 5. Em (iii in Key of C) -> E - G - B, 3 - 5 - 7 */
        MusicTheory.resolveChordAt(0, 1, 1, false, r);
        assertEquals("Em", r.chordName);
        assertEquals("iii", r.romanNum);
        assertEquals("E - G - B", r.notesStr);
        assertEquals("3 - 5 - 7", r.degreesStr);

        /* 6. Am (vi in Key of C) -> A - C - E, 6 - 8 - 10 */
        MusicTheory.resolveChordAt(0, 0, 1, false, r);
        assertEquals("Am", r.chordName);
        assertEquals("vi", r.romanNum);
        assertEquals("A - C - E", r.notesStr);
        assertEquals("6 - 8 - 10", r.degreesStr);

        /* 7. Bdim (vii° in Key of C) -> B - D - F, 7 - 9 - 11 */
        MusicTheory.resolveChordAt(0, 0, 2, false, r);
        assertEquals("Bdim", r.chordName);
        assertEquals("viidim", r.romanNum);
        assertEquals("B - D - F", r.notesStr);
        assertEquals("7 - 9 - 11", r.degreesStr);
    }

    @Test
    public void testSeventhChordScaleDegreeNumbersAndTransposition() {
        MusicTheory.ResolvedChord r = new MusicTheory.ResolvedChord();

        /* 7th chords in Key of C */
        MusicTheory.resolveChordAt(0, 0, 0, true, r);
        assertEquals("Cmaj7", r.chordName);
        assertEquals("C - E - G - B", r.notesStr);
        assertEquals("1 - 3 - 5 - 7", r.degreesStr);

        MusicTheory.resolveChordAt(0, 11, 0, true, r);
        assertEquals("Fmaj7", r.chordName);
        assertEquals("F - A - C - E", r.notesStr);
        assertEquals("4 - 6 - 8 - 10", r.degreesStr);

        MusicTheory.resolveChordAt(0, 1, 0, true, r);
        assertEquals("G7", r.chordName);
        assertEquals("G - B - D - F", r.notesStr);
        assertEquals("5 - 7 - 9 - 11", r.degreesStr);

        MusicTheory.resolveChordAt(0, 0, 2, true, r);
        assertEquals("Bm7b5", r.chordName);
        assertEquals("B - D - F - A", r.notesStr);
        assertEquals("7 - 9 - 11 - 13", r.degreesStr);

        /* Key of G (sector 1): G is 1-3-5, C is 4-6-8, D is 5-7-9 */
        MusicTheory.resolveChordAt(1, 1, 0, false, r);
        assertEquals("G", r.chordName);
        assertEquals("1 - 3 - 5", r.degreesStr);

        MusicTheory.resolveChordAt(1, 0, 0, false, r);
        assertEquals("C", r.chordName);
        assertEquals("4 - 6 - 8", r.degreesStr);

        MusicTheory.resolveChordAt(1, 2, 0, false, r);
        assertEquals("D", r.chordName);
        assertEquals("5 - 7 - 9", r.degreesStr);

        /* Chromatic / Secondary Dominant in Key of C: D Major (V/V) = 2 - #4 - 6 */
        MusicTheory.resolveChordAt(0, 2, 0, false, r);
        assertEquals("D", r.chordName);
        assertEquals("V/V", r.romanNum);
        assertEquals("2 - #4 - 6", r.degreesStr);

        /* Borrowed Minor iv in Key of C: Fm = 4 - b6 - 8 */
        MusicTheory.resolveChordAt(0, 8, 1, false, r);
        assertEquals("Fm", r.chordName);
        assertEquals("iv", r.romanNum);
        assertEquals("4 - b6 - 8", r.degreesStr);
    }

    @Test
    public void testAll252GuitarVoicingsProduceValidChordTones() {
        int[] qualities = {
            MusicTheory.QUAL_MAJ,
            MusicTheory.QUAL_MIN,
            MusicTheory.QUAL_DIM,
            MusicTheory.QUAL_MAJ7,
            MusicTheory.QUAL_MIN7,
            MusicTheory.QUAL_DOM7,
            MusicTheory.QUAL_M7B5
        };

        MusicTheory.GuitarVoicing voicing = new MusicTheory.GuitarVoicing();

        for (int rootPc = 0; rootPc < 12; rootPc++) {
            for (int qual : qualities) {
                Set<Integer> expectedTones = getExpectedChordPitchClasses(rootPc, qual);

                for (int vIdx = 0; vIdx < 3; vIdx++) {
                    MusicTheory.buildGuitarVoicing(rootPc, qual, vIdx, voicing);
                    assertNotNull(voicing.label);
                    assertFalse(voicing.label.isEmpty());

                    int soundedCount = 0;
                    for (int s = 0; s < 6; s++) {
                        int fret = voicing.frets[s];
                        assertTrue("Fret out of bounds: " + fret, fret >= -1 && fret <= 17);
                        if (fret >= 0) {
                            soundedCount++;
                            int soundedPc = (MusicTheory.STRING_MIDI[s] + fret) % 12;
                            assertTrue(
                                    "Root=" + rootPc + " qual=" + qual + " voicing=" + vIdx
                                            + " string=" + s + " fret=" + fret
                                            + " produced non-chord pitch class " + soundedPc,
                                    expectedTones.contains(soundedPc));
                        }
                    }
                    assertTrue("Voicing must sound at least 3 strings", soundedCount >= 3);
                }
            }
        }
    }

    @Test
    public void testProgressionPresetsAreValid() {
        assertEquals(5, MusicTheory.PRESETS.length);
        MusicTheory.ResolvedChord r = new MusicTheory.ResolvedChord();

        for (MusicTheory.ProgPreset preset : MusicTheory.PRESETS) {
            assertNotNull(preset.name);
            assertEquals(4, preset.sectorOffsets.length);
            assertEquals(4, preset.rings.length);

            for (int keySec = 0; keySec < 12; keySec++) {
                for (int step = 0; step < 4; step++) {
                    int sec = (keySec + preset.sectorOffsets[step]) % 12;
                    int ring = preset.rings[step];
                    MusicTheory.resolveChordAt(keySec, sec, ring, false, r);
                    assertFalse(r.chordName.isEmpty());
                    assertTrue("All preset chords are diatonic to active key", r.diatonicFamily >= 1 && r.diatonicFamily <= 3);
                }
            }
        }
    }

    private static Set<Integer> getExpectedChordPitchClasses(int rootPc, int qual) {
        Set<Integer> pcs = new HashSet<>();
        int i3 = (qual == MusicTheory.QUAL_MAJ || qual == MusicTheory.QUAL_MAJ7 || qual == MusicTheory.QUAL_DOM7) ? 4 : 3;
        int i5 = (qual == MusicTheory.QUAL_DIM || qual == MusicTheory.QUAL_M7B5) ? 6 : 7;
        pcs.add(rootPc % 12);
        pcs.add((rootPc + i3) % 12);
        pcs.add((rootPc + i5) % 12);

        if (qual == MusicTheory.QUAL_MAJ7 || qual == MusicTheory.QUAL_MIN7
                || qual == MusicTheory.QUAL_DOM7 || qual == MusicTheory.QUAL_M7B5) {
            int i7 = (qual == MusicTheory.QUAL_MAJ7) ? 11 : 10;
            pcs.add((rootPc + i7) % 12);
        }
        return pcs;
    }
}
