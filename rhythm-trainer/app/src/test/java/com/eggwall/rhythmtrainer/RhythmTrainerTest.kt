package com.eggwall.rhythmtrainer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class RhythmTrainerTest {

    @Test
    fun timeSignaturesMatchSpecification() {
        val labels = TimeSignature.entries.map { it.label }
        assertEquals(listOf("4/4", "3/4", "2/2", "5/4", "7/4"), labels)
        assertEquals(16, TimeSignature.FOUR_FOUR.sixteenthUnitsPerBar)
        assertEquals(12, TimeSignature.THREE_FOUR.sixteenthUnitsPerBar)
        assertEquals(16, TimeSignature.TWO_TWO.sixteenthUnitsPerBar)
        assertEquals(20, TimeSignature.FIVE_FOUR.sixteenthUnitsPerBar)
        assertEquals(28, TimeSignature.SEVEN_FOUR.sixteenthUnitsPerBar)
    }

    @Test
    fun defaultAndRandomMeasuresRespectTimeSignatureLength() {
        for (sig in TimeSignature.entries) {
            val defaults = RhythmBuilder.defaultMeasuresFor(sig)
            assertTrue(defaults.isNotEmpty())
            for (measure in defaults) {
                assertEquals(sig.sixteenthUnitsPerBar, measure.totalSixteenthUnits)
            }

            repeat(10) {
                val randomMeasure = RhythmBuilder.randomMeasure(sig)
                assertEquals(sig.sixteenthUnitsPerBar, randomMeasure.totalSixteenthUnits)
            }
        }
    }

    @Test
    fun editModeSubdividesAndAbsorbsWhilePreservingBarLength() {
        for (sig in TimeSignature.entries) {
            var measure = RhythmBuilder.quarterNoteBar(sig)
            assertEquals(sig.sixteenthUnitsPerBar, measure.totalSixteenthUnits)

            for (duration in NoteDuration.entries) {
                for (isRest in listOf(false, true)) {
                    for (idx in measure.elements.indices) {
                        val (updated, newIdx) = RhythmBuilder.updateElementInMeasure(
                            measure = measure,
                            elementIndex = idx,
                            newDuration = duration,
                            newIsRest = isRest,
                            barUnits = sig.sixteenthUnitsPerBar
                        )
                        assertEquals(
                            "Bar units mismatch for ${sig.label} after editing index $idx to $duration",
                            sig.sixteenthUnitsPerBar,
                            updated.totalSixteenthUnits
                        )
                        assertTrue(newIdx in updated.elements.indices)
                        val expectedDuration = if (duration.sixteenthUnits > sig.sixteenthUnitsPerBar) {
                            NoteDuration.entries.first { it.sixteenthUnits <= sig.sixteenthUnitsPerBar }
                        } else {
                            duration
                        }
                        assertEquals(expectedDuration, updated.elements[newIdx].duration)
                        assertEquals(isRest, updated.elements[newIdx].isRest)
                        measure = updated
                    }
                }
            }
        }
    }

    @Test
    fun audioEngineSynthesizesCountdownClicksNotesAndRests() {
        val sampleRate = RhythmAudioEngine.SAMPLE_RATE
        val quarterSamples = sampleRate / 2 // 500ms at 120 BPM

        // Rests produce complete silence
        val restPcm = RhythmAudioEngine.synthesizeEventPcm(
            totalSamples = quarterSamples,
            isRest = true,
            isCountdownClick = false
        )
        assertEquals(quarterSamples, restPcm.size)
        assertTrue(restPcm.all { it == 0.toShort() })

        // Countdown clicks have non-zero onset and silent tail
        val clickPcm = RhythmAudioEngine.synthesizeEventPcm(
            totalSamples = quarterSamples,
            isRest = false,
            isCountdownClick = true
        )
        assertEquals(quarterSamples, clickPcm.size)
        assertTrue(clickPcm.take(1000).any { abs(it.toInt()) > 5000 })
        assertTrue(clickPcm.takeLast(quarterSamples / 2).all { it == 0.toShort() })

        // Longer note durations have proportionally more active non-zero samples
        val sixteenthPcm = RhythmAudioEngine.synthesizeEventPcm(
            totalSamples = quarterSamples / 4,
            isRest = false,
            isCountdownClick = false
        )
        val halfNotePcm = RhythmAudioEngine.synthesizeEventPcm(
            totalSamples = quarterSamples * 2,
            isRest = false,
            isCountdownClick = false
        )
        val activeSixteenth = sixteenthPcm.count { it != 0.toShort() }
        val activeHalf = halfNotePcm.count { it != 0.toShort() }
        assertFalse(activeSixteenth == 0)
        assertTrue(activeHalf > activeSixteenth * 4)
    }
}
