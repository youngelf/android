package com.eggwall.rhythmtrainer;

import org.junit.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RhythmTrainerTest {

    @Test
    public void timeSignaturesMatchSpecification() {
        TimeSignature[] sigs = TimeSignature.values();
        assertEquals(5, sigs.length);
        assertEquals("4/4", sigs[0].label);
        assertEquals("3/4", sigs[1].label);
        assertEquals("2/2", sigs[2].label);
        assertEquals("5/4", sigs[3].label);
        assertEquals("7/4", sigs[4].label);

        assertEquals(16, TimeSignature.FOUR_FOUR.sixteenthUnitsPerBar);
        assertEquals(12, TimeSignature.THREE_FOUR.sixteenthUnitsPerBar);
        assertEquals(16, TimeSignature.TWO_TWO.sixteenthUnitsPerBar);
        assertEquals(20, TimeSignature.FIVE_FOUR.sixteenthUnitsPerBar);
        assertEquals(28, TimeSignature.SEVEN_FOUR.sixteenthUnitsPerBar);
    }

    @Test
    public void defaultAndRandomMeasuresRespectTimeSignatureLength() {
        for (TimeSignature sig : TimeSignature.values()) {
            List<Measure> defaults = RhythmBuilder.defaultMeasuresFor(sig);
            assertFalse(defaults.isEmpty());
            for (Measure measure : defaults) {
                assertEquals(sig.sixteenthUnitsPerBar, measure.totalSixteenthUnits);
            }

            for (int i = 0; i < 10; i++) {
                Measure randomMeasure = RhythmBuilder.randomMeasure(sig);
                assertEquals(sig.sixteenthUnitsPerBar, randomMeasure.totalSixteenthUnits);
            }
        }
    }

    @Test
    public void editModeSubdividesAndAbsorbsWhilePreservingBarLength() {
        for (TimeSignature sig : TimeSignature.values()) {
            Measure measure = RhythmBuilder.quarterNoteBar(sig);
            assertEquals(sig.sixteenthUnitsPerBar, measure.totalSixteenthUnits);

            for (NoteDuration duration : NoteDuration.values()) {
                for (boolean isRest : Arrays.asList(false, true)) {
                    for (int idx = 0; idx < measure.elements.size(); idx++) {
                        RhythmBuilder.UpdateResult res = RhythmBuilder.updateElementInMeasure(
                                measure,
                                idx,
                                duration,
                                isRest,
                                sig.sixteenthUnitsPerBar
                        );
                        Measure updated = res.measure;
                        int newIdx = res.selectedIndex;
                        assertEquals(
                                "Bar units mismatch for " + sig.label + " after editing index " + idx + " to " + duration,
                                sig.sixteenthUnitsPerBar,
                                updated.totalSixteenthUnits
                        );
                        assertTrue(newIdx >= 0 && newIdx < updated.elements.size());

                        NoteDuration expectedDuration = duration;
                        if (duration.sixteenthUnits > sig.sixteenthUnitsPerBar) {
                            for (NoteDuration d : NoteDuration.values()) {
                                if (d.sixteenthUnits <= sig.sixteenthUnitsPerBar) {
                                    expectedDuration = d;
                                    break;
                                }
                            }
                        }
                        assertEquals(expectedDuration, updated.elements.get(newIdx).duration);
                        assertEquals(isRest, updated.elements.get(newIdx).isRest);
                        measure = updated;
                    }
                }
            }
        }
    }

    @Test
    public void audioEngineSynthesizesCountdownClicksNotesAndRests() {
        int sampleRate = RhythmAudioEngine.SAMPLE_RATE;
        int quarterSamples = sampleRate / 2; // 500ms at 120 BPM

        // Rests produce complete silence
        short[] restPcm = RhythmAudioEngine.synthesizeEventPcm(quarterSamples, true, false);
        assertEquals(quarterSamples, restPcm.length);
        for (short s : restPcm) {
            assertEquals(0, s);
        }

        // Countdown clicks have non-zero onset and silent tail
        short[] clickPcm = RhythmAudioEngine.synthesizeEventPcm(quarterSamples, false, true);
        assertEquals(quarterSamples, clickPcm.length);
        boolean hasLoudOnset = false;
        for (int i = 0; i < Math.min(1000, clickPcm.length); i++) {
            if (Math.abs((int) clickPcm[i]) > 5000) {
                hasLoudOnset = true;
                break;
            }
        }
        assertTrue(hasLoudOnset);
        for (int i = quarterSamples / 2; i < quarterSamples; i++) {
            assertEquals(0, clickPcm[i]);
        }

        // Longer note durations have proportionally more active non-zero samples
        short[] sixteenthPcm = RhythmAudioEngine.synthesizeEventPcm(quarterSamples / 4, false, false);
        short[] halfNotePcm = RhythmAudioEngine.synthesizeEventPcm(quarterSamples * 2, false, false);
        int activeSixteenth = 0;
        for (short s : sixteenthPcm) {
            if (s != 0) activeSixteenth++;
        }
        int activeHalf = 0;
        for (short s : halfNotePcm) {
            if (s != 0) activeHalf++;
        }
        assertTrue(activeSixteenth > 0);
        assertTrue(activeHalf > activeSixteenth * 4);
    }
}
