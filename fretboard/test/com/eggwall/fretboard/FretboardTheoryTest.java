/*
 * FretboardTheoryTest.java - JVM Unit Tests for FretboardTheory
 *
 * Copyright 2026 Vikram Aggarwal
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.eggwall.fretboard;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests verifying guitar tunings, MIDI/frequency conversions, CAGED box calculations,
 * chord voicing generation, reverse chord identification, and quiz answer validation.
 */
public class FretboardTheoryTest {

    @Test
    public void testMidiToFreqStandardReference() {
        assertEquals(440.0, FretboardTheory.midiToFreq(69), 1e-4);
        assertEquals(82.4069, FretboardTheory.midiToFreq(40), 1e-3);
        assertEquals(329.6276, FretboardTheory.midiToFreq(64), 1e-3);
    }

    @Test
    public void testStandardTuningPitchClasses() {
        /* Standard tuning: E2(40)=4, A2(45)=9, D3(50)=2, G3(55)=7, B3(59)=11, E4(64)=4 */
        assertEquals(4, FretboardTheory.getPitchClass(0, 0, 0));
        assertEquals(9, FretboardTheory.getPitchClass(0, 1, 0));
        assertEquals(2, FretboardTheory.getPitchClass(0, 2, 0));
        assertEquals(7, FretboardTheory.getPitchClass(0, 3, 0));
        assertEquals(11, FretboardTheory.getPitchClass(0, 4, 0));
        assertEquals(4, FretboardTheory.getPitchClass(0, 5, 0));
        /* 5th fret of low E string is A (pitch class 9) */
        assertEquals(9, FretboardTheory.getPitchClass(0, 0, 5));
    }

    @Test
    public void testAllScalesHaveTwelveSemitoneMasks() {
        assertEquals(14, FretboardTheory.SCALES.length);
        for (FretboardTheory.ScaleDef sd : FretboardTheory.SCALES) {
            assertNotNull(sd.name);
            assertEquals(12, sd.mask.length);
            assertEquals("Root must be active in " + sd.name, 4, sd.mask[0]);
        }
    }

    @Test
    public void testCagedBoxRangeBounds() {
        for (int pos = 1; pos <= 5; pos++) {
            int[] range = FretboardTheory.computeCagedBoxRange(0, 9 /* A */, pos);
            assertEquals(2, range.length);
            assertTrue(range[0] >= 0 && range[0] <= FretboardTheory.MAX_FRET);
            assertTrue(range[1] >= range[0] && range[1] <= FretboardTheory.MAX_FRET);
        }
    }

    @Test
    public void testComputeChordVoicingPlayableAcrossKeysAndShapes() {
        FretboardTheory.ComputedVoicing cv = new FretboardTheory.ComputedVoicing();
        for (int root = 0; root < 12; root++) {
            for (int q = 0; q < FretboardTheory.CHORDS.length; q++) {
                for (int v = 0; v < 5; v++) {
                    FretboardTheory.computeChordVoicing(0, root, q, v, cv);
                    int sounded = 0;
                    for (int s = 0; s < FretboardTheory.NUM_STRINGS; s++) {
                        int f = cv.frets[s];
                        assertTrue(f >= -1 && f <= FretboardTheory.MAX_FRET);
                        if (f >= 0) {
                            sounded++;
                            int pc = FretboardTheory.getPitchClass(0, s, f);
                            int relIv = (pc - root + 12) % 12;
                            assertTrue(FretboardTheory.CHORDS[q].mask[relIv] > 0);
                        }
                    }
                    assertTrue("Voicing should sound at least 3 strings", sounded >= 3);
                }
            }
        }
    }

    @Test
    public void testIdentifyLockedChordStandardOpenCMajorAndGMajor() {
        FretboardTheory.IdentifiedChord out = new FretboardTheory.IdentifiedChord();
        int[] cMajorOpen = { -1, 3, 2, 0, 1, 0 };
        FretboardTheory.identifyLockedChord(0, cMajorOpen, out);
        assertEquals("C", out.chordName);

        int[] aMinorOpen = { -1, 0, 2, 2, 1, 0 };
        FretboardTheory.identifyLockedChord(0, aMinorOpen, out);
        assertEquals("Am", out.chordName);
    }

    @Test
    public void testQuizAnswerValidation() {
        /* Note quiz: target C (pc=0). String 1 (A), fret 3 is C. */
        assertTrue(FretboardTheory.isQuizAnswerCorrect(
                FretboardTheory.QUIZ_NOTES, 0, 0, 0, 1, 3));
        assertFalse(FretboardTheory.isQuizAnswerCorrect(
                FretboardTheory.QUIZ_NOTES, 0, 0, 0, 1, 2));

        /* Interval quiz: root A (pc=9), target Perfect 5th (7 semitones -> E, pc=4). Open low E is pc=4. */
        assertTrue(FretboardTheory.isQuizAnswerCorrect(
                FretboardTheory.QUIZ_INTERVALS, 0, 9, 7, 0, 0));
    }
}
