package com.eggwall.licktrainer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Host-JVM JUnit 4 unit tests for {@link LickTheory}.
 */
public class LickTheoryTest {

    @Test
    public void testVerifyAll33LicksPassMusicTheoryAndBiomechanics() {
        String error = LickTheory.verifyLickLibrary();
        assertNull("All 33 exercises must pass scale-mask and finger-biomechanics checks: " + error, error);
    }

    @Test
    public void testCategoryIndexingCoversAll33Licks() {
        int count = 0;
        for (int c = 0; c < LickTheory.NUM_CATEGORIES; c++) {
            for (int l = 0; l < LickTheory.CAT_COUNT[c]; l++) {
                int idx = LickTheory.getGlobalLickIndex(c, l);
                assertEquals(count, idx);
                assertEquals(c, LickTheory.LICKS[idx].catIdx);
                count++;
            }
        }
        assertEquals(LickTheory.TOTAL_LICKS, count);
    }

    @Test
    public void testTranspositionAcrossAll12KeysProducesValidMidiAndFrets() {
        LickTheory.LickDef first = LickTheory.LICKS[0];
        for (int k = 0; k < LickTheory.NUM_KEYS; k++) {
            int startFret = LickTheory.computeNeckWindowStartFret(first, k);
            assertTrue(startFret >= 0 && startFret <= 15);
            for (int s = 0; s < first.numSteps; s++) {
                LickTheory.LickStep step = first.steps[s];
                int actualFret = LickTheory.getActualFret(step, k);
                assertEquals(LickTheory.KEYS[k].anchorFret + step.fretOff, actualFret);
                int midi = LickTheory.getStepMidi(step, k);
                assertEquals(LickTheory.OPEN_MIDI[step.str] + actualFret, midi);
                assertFalseOrNonEmpty(LickTheory.getNoteNameForMidi(midi));
            }
        }
    }

    @Test
    public void testJazz7thArpeggioDegreesAndBackingChords() {
        for (int i = 25; i < LickTheory.TOTAL_LICKS; i++) {
            LickTheory.LickDef jazzLick = LickTheory.LICKS[i];
            assertTrue(jazzLick.isJazz7ths());
            for (int beat = 0; beat < 4; beat++) {
                int[] chordMidi = LickTheory.getBackingChordMidi(jazzLick, beat, 5); // Key of A
                assertNotNull(chordMidi);
                assertEquals(4, chordMidi.length);
                for (int n = 0; n < 4; n++) {
                    int stepIdx = beat * jazzLick.stepsPerBeat + n;
                    String deg = LickTheory.getJazzStepDegreeNum(jazzLick, stepIdx, 5);
                    assertTrue(deg.equals("1") || deg.equals("3") || deg.equals("5") || deg.equals("7"));
                    String iv = LickTheory.getJazzStepIntervalLabel(jazzLick, stepIdx, 5);
                    assertNotNull(iv);
                }
            }
        }
    }

    private static void assertFalseOrNonEmpty(String s) {
        assertNotNull(s);
        assertTrue(!s.isEmpty());
    }
}
