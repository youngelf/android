package com.eggwall.pocketband;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for {@link BandTheory} covering chord formatting, bass MIDI generation,
 * chord voicings, swing/straight step sample timing, comping triggers, and 12-fret
 * guitar scale classification.
 */
public class BandTheoryTest {

    @Test
    public void testStylesAndProgressionsPopulated() {
        assertTrue(BandTheory.STYLES.length >= 8);
        assertTrue(BandTheory.PROGRESSION_PRESETS.length >= 8);
        for (BandTheory.StylePreset st : BandTheory.STYLES) {
            assertNotNull(st.name);
            assertNotNull(st.shortGenre);
            assertTrue(st.beatsPerBar == 3 || st.beatsPerBar == 4);
            assertEquals(st.beatsPerBar * 2, st.stepsPerBar);
            assertEquals(12, st.scaleMask.length);
            assertEquals(BandTheory.NUM_TRACKS, st.defaultSteps.length);
            for (int t = 0; t < BandTheory.NUM_TRACKS; t++) {
                assertEquals(BandTheory.MAX_STEPS, st.defaultSteps[t].length);
            }
            assertNotNull(st.getDefaultBars());
            assertTrue(st.getDefaultBars().length >= 4);
        }
    }

    @Test
    public void testChordFormattingAndNotes() {
        BandTheory.BarChord dom7 = new BandTheory.BarChord(0, BandTheory.CHQ_DOM7, "I7");
        assertEquals("A7", BandTheory.formatChordLabel(9, dom7));
        assertEquals("A - Db - E - G", BandTheory.formatChordNotes(9, dom7));
        assertEquals("1 - 3 - 5 - b7", BandTheory.formatChordFormula(dom7));

        BandTheory.BarChord min7 = new BandTheory.BarChord(2, BandTheory.CHQ_MIN7, "ii7");
        assertEquals("Dm7", BandTheory.formatChordLabel(0, min7));
        assertEquals("D - F - A - C", BandTheory.formatChordNotes(0, min7));
        assertEquals("1 - b3 - 5 - b7", BandTheory.formatChordFormula(min7));
    }

    @Test
    public void testBassAndChordMidiNotes() {
        BandTheory.BarChord cMaj7 = new BandTheory.BarChord(0, BandTheory.CHQ_MAJ7, "Imaj7");
        assertEquals(-1, BandTheory.computeBassMidi(0, cMaj7, 0)); // Rest
        assertEquals(36, BandTheory.computeBassMidi(0, cMaj7, 1)); // Root C2
        assertEquals(40, BandTheory.computeBassMidi(0, cMaj7, 2)); // Major 3rd E2
        assertEquals(43, BandTheory.computeBassMidi(0, cMaj7, 3)); // 5th G2
        assertEquals(45, BandTheory.computeBassMidi(0, cMaj7, 4)); // 6th A2
        assertEquals(46, BandTheory.computeBassMidi(0, cMaj7, 5)); // b7 Bb2
        assertEquals(48, BandTheory.computeBassMidi(0, cMaj7, 6)); // Octave C3

        int[] chordNotes = BandTheory.computeChordMidiNotes(0, cMaj7);
        assertArrayEquals(new int[] { 48, 52, 59 }, chordNotes);
    }

    @Test
    public void testStepSamplesAndPhaseInc() {
        // At 120 BPM, 1 beat = 0.5s = 22050 samples; straight 8ths (50% swing) = 11025 each
        int evenStraight = BandTheory.computeStepSamples(120, 50, 0);
        int oddStraight = BandTheory.computeStepSamples(120, 50, 1);
        assertEquals(11025, evenStraight);
        assertEquals(11025, oddStraight);

        // With 65% triplet shuffle, even step > odd step, and even + odd == 22050
        int evenShuffle = BandTheory.computeStepSamples(120, 65, 0);
        int oddShuffle = BandTheory.computeStepSamples(120, 65, 1);
        assertTrue(evenShuffle > oddShuffle);
        assertEquals(22050, evenShuffle + oddShuffle);

        // A4 (MIDI 69) = 440 Hz -> phase inc > A3 (MIDI 57) = 220 Hz
        int incA3 = BandTheory.midiToPhaseInc(57);
        int incA4 = BandTheory.midiToPhaseInc(69);
        assertTrue(incA4 > incA3);
        assertEquals(incA3 * 2, incA4, 4.0);
    }

    @Test
    public void testCompingAndScaleFretClassification() {
        assertTrue(BandTheory.shouldTriggerComping(0, 4, 0));
        // Reggae (index 9) triggers skank on offbeats (odd steps)
        assertFalse(BandTheory.shouldTriggerComping(9, 4, 0));
        assertTrue(BandTheory.shouldTriggerComping(9, 4, 1));

        // Blues in A (keyPc = 9), active chord D7 (rootOffset = 5 -> D = pc 2)
        BandTheory.StylePreset blues = BandTheory.STYLES[0];
        BandTheory.BarChord iv7 = new BandTheory.BarChord(5, BandTheory.CHQ_DOM7, "IV7");
        // String 5 (6th string low E, open pc = 4): fret 5 is A (pc 9) -> Key Root (4)
        assertEquals(4, BandTheory.classifyScaleFret(blues, 9, iv7, 5, 5));
        // String 4 (5th string A, open pc = 9): fret 5 is D (pc 2) -> Active Chord Root (3)
        assertEquals(3, BandTheory.classifyScaleFret(blues, 9, iv7, 4, 5));
        // String 4 (5th string A, open pc = 9): fret 6 is Eb (pc 3, b5 blue note in A) -> Blue Note (2)
        assertEquals(2, BandTheory.classifyScaleFret(blues, 9, iv7, 4, 6));
    }

    @Test
    public void testNextChordCandidateCycles() {
        BandTheory.BarChord first = BandTheory.CHORD_CANDIDATES[0];
        BandTheory.BarChord second = BandTheory.nextChordCandidate(first, 1);
        assertEquals(BandTheory.CHORD_CANDIDATES[1].roman, second.roman);
        BandTheory.BarChord wrapped = BandTheory.nextChordCandidate(first, -1);
        assertEquals(
                BandTheory.CHORD_CANDIDATES[BandTheory.CHORD_CANDIDATES.length - 1].roman,
                wrapped.roman);
    }
}
