package com.eggwall.eartrainer;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Host-JVM JUnit 4 unit tests for {@link EarTheory} and {@link EarTheory.TrainerSession}.
 */
public class EarTheoryTest {

    @Test
    public void testAll12IntervalsHaveMonotonicSemitonesAndValidMetadata() {
        assertEquals(12, EarTheory.INTERVALS.length);
        for (int i = 0; i < EarTheory.NUM_ITEMS; i++) {
            EarTheory.IntervalDef def = EarTheory.INTERVALS[i];
            assertEquals("Interval index " + i + " should equal i+1 semitones", i + 1, def.semitones);
            assertNotNull(def.shortName);
            assertFalse(def.shortName.isEmpty());
            assertNotNull(def.fullName);
            assertFalse(def.fullName.isEmpty());
            assertNotNull(def.mnemonic);
            assertFalse(def.mnemonic.isEmpty());
            assertTrue(def.minTier >= EarTheory.TIER_EASY && def.minTier <= EarTheory.TIER_ALL_12);
        }
    }

    @Test
    public void testAll12ChordQualitiesAndMovableFretboardShapesMatchSemitones() {
        assertEquals(12, EarTheory.CHORDS.length);
        int rootStr = 1; // 5th string (A2 = MIDI 45)
        int rootFret = 5; // D3 = MIDI 50
        int rootMidi = EarTheory.OPEN_STRING_MIDI[rootStr] + rootFret;

        for (int c = 0; c < EarTheory.NUM_ITEMS; c++) {
            EarTheory.ChordDef cd = EarTheory.CHORDS[c];
            assertTrue(cd.numNotes >= 3 && cd.numNotes <= 4);
            assertEquals(0, cd.semitones[0]);
            for (int n = 0; n < cd.numNotes; n++) {
                int s = rootStr + cd.strOff[n];
                int f = rootFret + cd.fretOff[n];
                assertTrue("Chord " + cd.shortName + " string in bounds", s >= 0 && s < 6);
                assertTrue("Chord " + cd.shortName + " fret in bounds", f >= 0 && f <= 12);
                int frettedPc = (EarTheory.OPEN_STRING_MIDI[s] + f) % 12;
                boolean matchesChordTone = false;
                for (int k = 0; k < cd.numNotes; k++) {
                    if (((rootMidi + cd.semitones[k]) % 12) == frettedPc) {
                        matchesChordTone = true;
                        break;
                    }
                }
                assertTrue(
                        "Chord " + cd.shortName + " voice " + n + " must belong to chord formula pitch classes",
                        matchesChordTone);
            }
        }
    }

    @Test
    public void testScaleDegreesAndProgressionsTablesAreComplete() {
        assertEquals(12, EarTheory.SCALE_DEGREES.length);
        for (int i = 0; i < EarTheory.NUM_ITEMS; i++) {
            EarTheory.ScaleDegreeDef sd = EarTheory.SCALE_DEGREES[i];
            assertEquals(i + 1, sd.semitones);
            assertFalse(sd.solfege.isEmpty());
            assertFalse(sd.resolutionTendency.isEmpty());
        }

        assertEquals(12, EarTheory.PROGRESSIONS.length);
        for (int i = 0; i < EarTheory.NUM_ITEMS; i++) {
            EarTheory.MelodicPatternDef mp = EarTheory.PROGRESSIONS[i];
            assertEquals(3, mp.melodySemitones.length);
            assertEquals(3, mp.chordRootSemitones.length);
            assertEquals(3, mp.chordQualities.length);
            for (int q : mp.chordQualities) {
                assertTrue(q >= 0 && q < EarTheory.CHORDS.length);
            }
        }
    }

    @Test
    public void testFindGuitarIntervalPosProducesExactTargetMidiAcrossFretboard() {
        EarTheory.FretPos pos = new EarTheory.FretPos();
        for (int rootStr = 0; rootStr <= 3; rootStr++) {
            for (int rootFret = 1; rootFret <= 7; rootFret++) {
                int rootMidi = EarTheory.OPEN_STRING_MIDI[rootStr] + rootFret;
                for (int semi = 1; semi <= 12; semi++) {
                    EarTheory.findGuitarIntervalPos(rootStr, rootFret, semi, pos);
                    assertTrue(pos.stringIdx >= rootStr && pos.stringIdx < 6);
                    assertTrue(pos.fret >= 0 && pos.fret <= 12);
                    int actualMidi = EarTheory.OPEN_STRING_MIDI[pos.stringIdx] + pos.fret;
                    assertEquals("Exact MIDI pitch required for rootStr=" + rootStr
                            + " rootFret=" + rootFret + " semi=" + semi,
                            rootMidi + semi, actualMidi);
                }
            }
        }
    }

    @Test
    public void testFormatMidiNoteAndGetItemMidiNotes() {
        assertEquals("E2", EarTheory.formatMidiNote(40));
        assertEquals("A2", EarTheory.formatMidiNote(45));
        assertEquals("C4", EarTheory.formatMidiNote(60));
        assertEquals("A4", EarTheory.formatMidiNote(69));

        assertArrayEquals(new int[]{45, 52},
                EarTheory.getItemMidiNotes(EarTheory.MODE_INTERVALS, 6, 45)); // P5 (+7)
        assertArrayEquals(new int[]{50, 54, 57, 62},
                EarTheory.getItemMidiNotes(EarTheory.MODE_CHORDS, 0, 50)); // Major triad
        assertArrayEquals(new int[]{48, 50, 52},
                EarTheory.getItemMidiNotes(EarTheory.MODE_PROGRESSIONS, 0, 48)); // 1-2-3
    }

    @Test
    public void testTrainerSessionThreeGuessRuleHintsAndStreakTracking() {
        EarTheory.TrainerSession session = new EarTheory.TrainerSession(new Random(42L));
        session.mode = EarTheory.MODE_INTERVALS;
        session.tier = EarTheory.TIER_ALL_12;
        session.generateNewQuestion();

        int correct = session.correctIdx;
        int wrong1 = (correct + 1) % 12;
        int wrong2 = (correct + 2) % 12;
        int wrong3 = (correct + 3) % 12;

        // First wrong guess -> 2 tries left
        assertEquals(EarTheory.TrainerSession.GUESS_WRONG_RETRY, session.submitGuess(wrong1));
        assertEquals(2, session.guessesLeft);
        assertFalse(session.revealed);
        assertTrue(session.wrongGuessed[wrong1]);

        // Duplicate wrong guess is ignored
        assertEquals(EarTheory.TrainerSession.GUESS_IGNORED, session.submitGuess(wrong1));
        assertEquals(2, session.guessesLeft);

        // Correct guess on 2nd try -> solved & revealed, streak increments
        assertEquals(EarTheory.TrainerSession.GUESS_CORRECT, session.submitGuess(correct));
        assertTrue(session.revealed);
        assertTrue(session.solved);
        assertEquals(1, session.streak);
        assertEquals(1, session.bestStreak);
        assertEquals(100, session.getSessionAccuracyPercent());

        // Submitting while revealed advances to next question
        assertEquals(EarTheory.TrainerSession.GUESS_NEXT_QUESTION, session.submitGuess(correct));
        assertFalse(session.revealed);
        assertEquals(3, session.guessesLeft);

        // Test hint elimination (-2 wrong answers)
        int eliminated = session.useHintEliminateWrong();
        assertEquals(2, eliminated);
        assertFalse("Hint must never eliminate the correct answer", session.wrongGuessed[session.correctIdx]);

        // Test exhausting all 3 guesses -> revealed as unsolved, streak resets to 0
        int c2 = session.correctIdx;
        int[] wrongs = new int[3];
        int found = 0;
        for (int i = 0; i < 12 && found < 3; i++) {
            if (i != c2 && !session.wrongGuessed[i]) {
                wrongs[found++] = i;
            }
        }
        assertEquals(EarTheory.TrainerSession.GUESS_WRONG_RETRY, session.submitGuess(wrongs[0]));
        assertEquals(EarTheory.TrainerSession.GUESS_WRONG_RETRY, session.submitGuess(wrongs[1]));
        assertEquals(EarTheory.TrainerSession.GUESS_WRONG_REVEALED, session.submitGuess(wrongs[2]));
        assertTrue(session.revealed);
        assertFalse(session.solved);
        assertEquals(0, session.streak);
        assertEquals(1, session.bestStreak);
        assertEquals(50, session.getSessionAccuracyPercent());
    }
}
