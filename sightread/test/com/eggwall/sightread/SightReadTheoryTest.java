package com.eggwall.sightread;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Host-JVM JUnit 4 unit tests for {@link SightReadTheory}.
 */
public class SightReadTheoryTest {

    @Test
    public void testMidiForStringFretStandardTuning() {
        assertEquals(40, SightReadTheory.midiForStringFret(0, 0));
        assertEquals(45, SightReadTheory.midiForStringFret(1, 0));
        assertEquals(50, SightReadTheory.midiForStringFret(2, 0));
        assertEquals(55, SightReadTheory.midiForStringFret(3, 0));
        assertEquals(59, SightReadTheory.midiForStringFret(4, 0));
        assertEquals(64, SightReadTheory.midiForStringFret(5, 0));
        assertEquals(60, SightReadTheory.midiForStringFret(4, 1)); // B3 + 1 = C4
    }

    @Test
    public void testLevelForExerciseProgression() {
        assertEquals(1, SightReadTheory.levelForExercise(1));
        assertEquals(1, SightReadTheory.levelForExercise(3));
        assertEquals(2, SightReadTheory.levelForExercise(4));
        assertEquals(2, SightReadTheory.levelForExercise(8));
        assertEquals(3, SightReadTheory.levelForExercise(9));
        assertEquals(3, SightReadTheory.levelForExercise(15));
        assertEquals(4, SightReadTheory.levelForExercise(16));
        assertEquals(4, SightReadTheory.levelForExercise(99));
    }

    @Test
    public void testBuildPositionScalePoolStrictlyWithinPositionAndKey() {
        SightReadTheory.StaffNote[] pool = new SightReadTheory.StaffNote[SightReadTheory.MAX_SCALE_POOL];
        for (int posIdx = 0; posIdx < SightReadTheory.NUM_POSITIONS; posIdx++) {
            SightReadTheory.NeckPosition pos = SightReadTheory.POSITIONS[posIdx];
            for (int keyIdx = 0; keyIdx < SightReadTheory.NUM_KEYS; keyIdx++) {
                SightReadTheory.KeySigDef key = SightReadTheory.KEYS[keyIdx];
                int count = SightReadTheory.buildPositionScalePool(posIdx, keyIdx, pool);
                assertTrue("Pool must not be empty", count >= 8);
                int prevMidi = -1;
                for (int i = 0; i < count; i++) {
                    SightReadTheory.StaffNote n = pool[i];
                    assertNotNull(n);
                    assertTrue(n.str >= 0 && n.str < 6);
                    assertTrue(n.fret >= pos.minFret && n.fret <= pos.maxFret);
                    assertTrue("Pool notes must be strictly ascending in MIDI", n.midi > prevMidi);
                    prevMidi = n.midi;
                    assertEquals(key.degPc[n.letterIndex], n.midi % 12);
                }
            }
        }
    }

    @Test
    public void testGenerateSightPieceDeterministicTwoMeasuresSixteenEighths() {
        SightReadTheory.SightPiece piece = new SightReadTheory.SightPiece();
        for (int pos = 0; pos < SightReadTheory.NUM_POSITIONS; pos++) {
            for (int key = 0; key < SightReadTheory.NUM_KEYS; key++) {
                for (int ex : new int[]{1, 5, 12, 24, 99}) {
                    SightReadTheory.generateSightPiece(piece, ex, pos, key);
                    assertTrue(piece.numNotes >= 8 && piece.numNotes <= SightReadTheory.MAX_PIECE_NOTES);
                    int totalEighths = 0;
                    for (int i = 0; i < piece.numNotes; i++) {
                        SightReadTheory.StaffNote n = piece.notes[i];
                        totalEighths += n.durEighths;
                        assertTrue(n.barIdx == 0 || n.barIdx == 1);
                        assertTrue(n.eighthInBar >= 0 && n.eighthInBar < 8);
                    }
                    assertEquals("2 bars of 4/4 must sum to 16 eighth notes", 16, totalEighths);
                }
            }
        }
    }

    @Test
    public void testLedgerLineStepsAndDualInputMatching() {
        List<Integer> trebleMiddleC = SightReadTheory.computeLedgerSteps(0, false);
        assertTrue(trebleMiddleC.contains(0));

        SightReadTheory.StaffNote target = new SightReadTheory.StaffNote();
        target.str = 4;
        target.fret = 1;
        target.midi = 60; // C4
        target.letterIndex = 0; // C

        assertTrue(SightReadTheory.matchesNoteByFretboard(target, 4, 1, true));
        assertTrue(SightReadTheory.matchesNoteByFretboard(target, 3, 5, false)); // G3 + 5 = C4
        assertFalse(SightReadTheory.matchesNoteByFretboard(target, 3, 5, true));
        assertTrue(SightReadTheory.matchesNoteByLetterIndex(target, 0));
        assertFalse(SightReadTheory.matchesNoteByLetterIndex(target, 1));
        assertTrue(SightReadTheory.matchesNoteByPitchClass(target, 0));
    }
}
