package com.eggwall.rhythmtrainer

import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random

/**
 * Supported time signatures as specified in requirements:
 * 4/4, 3/4, 2/2, 5/4, and 7/4.
 */
enum class TimeSignature(
    val label: String,
    val numerator: Int,
    val denominator: Int,
    val sixteenthUnitsPerBar: Int
) {
    FOUR_FOUR("4/4", 4, 4, 16),
    THREE_FOUR("3/4", 3, 4, 12),
    TWO_TWO("2/2", 2, 2, 16),
    FIVE_FOUR("5/4", 5, 4, 20),
    SEVEN_FOUR("7/4", 7, 4, 28);

    /** Number of quarter-note beats in one bar. */
    val quarterBeatsPerBar: Int
        get() = sixteenthUnitsPerBar / 4
}

/**
 * Standard musical note/rest durations measured in sixteenth-note subdivisions.
 */
enum class NoteDuration(
    val displayName: String,
    val fractionText: String,
    val sixteenthUnits: Int
) {
    WHOLE("Whole", "1", 16),
    HALF("Half", "1/2", 8),
    QUARTER("Quarter", "1/4", 4),
    EIGHTH("Eighth", "1/8", 2),
    SIXTEENTH("Sixteenth", "1/16", 1);

    fun durationLabel(isRest: Boolean): String =
        if (isRest) "$displayName Rest ($fractionText)" else "$displayName Note ($fractionText)"
}

private val idGenerator = AtomicLong(1L)

/**
 * A single rhythmic event (either a pitched click/tone of fixed frequency or a silent rest).
 */
data class RhythmElement(
    val id: Long = idGenerator.getAndIncrement(),
    val duration: NoteDuration,
    val isRest: Boolean = false
)

/**
 * A single bar (measure) separated by bar lines.
 * The sum of [elements]' sixteenthUnits always equals [TimeSignature.sixteenthUnitsPerBar].
 */
data class Measure(
    val elements: List<RhythmElement>
) {
    val totalSixteenthUnits: Int
        get() = elements.sumOf { it.duration.sixteenthUnits }
}

/**
 * Identifies a specific note/rest position by measure (bar) index and element index within that measure.
 */
data class ElementPosition(
    val measureIndex: Int,
    val elementIndex: Int
)

object RhythmBuilder {

    /**
     * Decomposes an arbitrary number of sixteenth-note units into valid [NoteDuration] elements.
     */
    fun decomposeUnits(
        units: Int,
        isRest: Boolean = false,
        smallestFirst: Boolean = false
    ): List<RhythmElement> {
        if (units <= 0) return emptyList()
        val result = mutableListOf<RhythmElement>()
        var remaining = units
        val ordered = NoteDuration.entries.sortedByDescending { it.sixteenthUnits }
        for (dur in ordered) {
            while (remaining >= dur.sixteenthUnits) {
                result.add(RhythmElement(duration = dur, isRest = isRest))
                remaining -= dur.sixteenthUnits
            }
        }
        return if (smallestFirst) result.reversed() else result
    }

    /**
     * Creates a default two-bar rhythm pattern for the given [TimeSignature] that showcases
     * standard note durations and rests while respecting the exact bar length.
     */
    fun defaultMeasuresFor(timeSignature: TimeSignature): List<Measure> {
        return when (timeSignature) {
            TimeSignature.FOUR_FOUR -> listOf(
                Measure(
                    listOf(
                        RhythmElement(duration = NoteDuration.QUARTER, isRest = false),
                        RhythmElement(duration = NoteDuration.EIGHTH, isRest = false),
                        RhythmElement(duration = NoteDuration.EIGHTH, isRest = false),
                        RhythmElement(duration = NoteDuration.QUARTER, isRest = true),
                        RhythmElement(duration = NoteDuration.QUARTER, isRest = false)
                    )
                ),
                Measure(
                    listOf(
                        RhythmElement(duration = NoteDuration.HALF, isRest = false),
                        RhythmElement(duration = NoteDuration.SIXTEENTH, isRest = false),
                        RhythmElement(duration = NoteDuration.SIXTEENTH, isRest = false),
                        RhythmElement(duration = NoteDuration.SIXTEENTH, isRest = false),
                        RhythmElement(duration = NoteDuration.SIXTEENTH, isRest = false),
                        RhythmElement(duration = NoteDuration.QUARTER, isRest = false)
                    )
                )
            )

            TimeSignature.THREE_FOUR -> listOf(
                Measure(
                    listOf(
                        RhythmElement(duration = NoteDuration.QUARTER, isRest = false),
                        RhythmElement(duration = NoteDuration.EIGHTH, isRest = false),
                        RhythmElement(duration = NoteDuration.EIGHTH, isRest = false),
                        RhythmElement(duration = NoteDuration.QUARTER, isRest = false)
                    )
                ),
                Measure(
                    listOf(
                        RhythmElement(duration = NoteDuration.HALF, isRest = false),
                        RhythmElement(duration = NoteDuration.QUARTER, isRest = true)
                    )
                )
            )

            TimeSignature.TWO_TWO -> listOf(
                Measure(
                    listOf(
                        RhythmElement(duration = NoteDuration.HALF, isRest = false),
                        RhythmElement(duration = NoteDuration.QUARTER, isRest = false),
                        RhythmElement(duration = NoteDuration.QUARTER, isRest = false)
                    )
                ),
                Measure(
                    listOf(
                        RhythmElement(duration = NoteDuration.WHOLE, isRest = false)
                    )
                )
            )

            TimeSignature.FIVE_FOUR -> listOf(
                Measure(
                    listOf(
                        RhythmElement(duration = NoteDuration.HALF, isRest = false),
                        RhythmElement(duration = NoteDuration.QUARTER, isRest = false),
                        RhythmElement(duration = NoteDuration.EIGHTH, isRest = false),
                        RhythmElement(duration = NoteDuration.EIGHTH, isRest = false),
                        RhythmElement(duration = NoteDuration.QUARTER, isRest = true)
                    )
                ),
                Measure(
                    listOf(
                        RhythmElement(duration = NoteDuration.QUARTER, isRest = false),
                        RhythmElement(duration = NoteDuration.QUARTER, isRest = false),
                        RhythmElement(duration = NoteDuration.QUARTER, isRest = false),
                        RhythmElement(duration = NoteDuration.SIXTEENTH, isRest = false),
                        RhythmElement(duration = NoteDuration.SIXTEENTH, isRest = false),
                        RhythmElement(duration = NoteDuration.EIGHTH, isRest = false),
                        RhythmElement(duration = NoteDuration.QUARTER, isRest = false)
                    )
                )
            )

            TimeSignature.SEVEN_FOUR -> listOf(
                Measure(
                    listOf(
                        RhythmElement(duration = NoteDuration.HALF, isRest = false),
                        RhythmElement(duration = NoteDuration.QUARTER, isRest = false),
                        RhythmElement(duration = NoteDuration.QUARTER, isRest = false),
                        RhythmElement(duration = NoteDuration.EIGHTH, isRest = false),
                        RhythmElement(duration = NoteDuration.EIGHTH, isRest = false),
                        RhythmElement(duration = NoteDuration.QUARTER, isRest = true),
                        RhythmElement(duration = NoteDuration.QUARTER, isRest = false)
                    )
                ),
                Measure(
                    listOf(
                        RhythmElement(duration = NoteDuration.WHOLE, isRest = false),
                        RhythmElement(duration = NoteDuration.HALF, isRest = false),
                        RhythmElement(duration = NoteDuration.QUARTER, isRest = false)
                    )
                )
            )
        }
    }

    /**
     * Creates a single bar filled with quarter notes for the given [TimeSignature].
     */
    fun quarterNoteBar(timeSignature: TimeSignature): Measure {
        val count = timeSignature.quarterBeatsPerBar
        return Measure(
            List(count) { RhythmElement(duration = NoteDuration.QUARTER, isRest = false) }
        )
    }

    /**
     * Adapts an existing list of measures when the time signature changes so every bar has
     * exactly [TimeSignature.sixteenthUnitsPerBar] units.
     */
    fun adaptMeasuresToTimeSignature(
        measures: List<Measure>,
        newTimeSignature: TimeSignature
    ): List<Measure> {
        val targetUnits = newTimeSignature.sixteenthUnitsPerBar
        if (measures.isEmpty()) return defaultMeasuresFor(newTimeSignature)

        return measures.map { measure ->
            val updated = mutableListOf<RhythmElement>()
            var accumulated = 0
            for (el in measure.elements) {
                if (accumulated >= targetUnits) break
                val remaining = targetUnits - accumulated
                if (el.duration.sixteenthUnits <= remaining) {
                    updated.add(el)
                    accumulated += el.duration.sixteenthUnits
                } else {
                    updated.addAll(decomposeUnits(remaining, isRest = el.isRest))
                    accumulated = targetUnits
                }
            }
            if (accumulated < targetUnits) {
                updated.addAll(decomposeUnits(targetUnits - accumulated, isRest = false))
            }
            Measure(updated)
        }
    }

    /**
     * Updates the element at [elementIndex] within [measure] to have [newDuration] and [newIsRest],
     * automatically splitting or absorbing adjacent elements in the same measure so the bar total
     * remains exactly [barUnits] sixteenth-note units.
     */
    fun updateElementInMeasure(
        measure: Measure,
        elementIndex: Int,
        newDuration: NoteDuration,
        newIsRest: Boolean,
        barUnits: Int
    ): Pair<Measure, Int> {
        if (measure.elements.isEmpty()) {
            return Measure(decomposeUnits(barUnits, newIsRest)) to 0
        }
        val safeIndex = elementIndex.coerceIn(0, measure.elements.lastIndex)
        val clampedDuration = if (newDuration.sixteenthUnits > barUnits) {
            NoteDuration.entries.first { it.sixteenthUnits <= barUnits }
        } else {
            newDuration
        }
        val oldElement = measure.elements[safeIndex]
        val oldUnits = oldElement.duration.sixteenthUnits
        val newUnits = clampedDuration.sixteenthUnits

        // Case 1: Same duration, only toggling note vs rest
        if (oldUnits == newUnits) {
            val copy = measure.elements.toMutableList()
            copy[safeIndex] = oldElement.copy(duration = clampedDuration, isRest = newIsRest)
            return Measure(copy) to safeIndex
        }

        // Case 2: Shortening duration (e.g., Quarter -> Eighth or Sixteenth)
        if (newUnits < oldUnits) {
            val copy = measure.elements.toMutableList()
            val replacement = mutableListOf<RhythmElement>()
            if (oldUnits % newUnits == 0 && oldUnits / newUnits <= 4) {
                // Clean musical subdivision (e.g. 1 Quarter -> 2 Eighths or 4 Sixteenths)
                val count = oldUnits / newUnits
                replacement.add(RhythmElement(duration = clampedDuration, isRest = newIsRest))
                repeat(count - 1) {
                    replacement.add(RhythmElement(duration = clampedDuration, isRest = oldElement.isRest))
                }
            } else {
                replacement.add(RhythmElement(duration = clampedDuration, isRest = newIsRest))
                replacement.addAll(
                    decomposeUnits(
                        units = oldUnits - newUnits,
                        isRest = oldElement.isRest,
                        smallestFirst = true
                    )
                )
            }
            copy.removeAt(safeIndex)
            copy.addAll(safeIndex, replacement)
            return Measure(copy) to safeIndex
        }

        // Case 3: Lengthening duration (e.g., Eighth -> Quarter / Half / Whole)
        // Calculate start offset in sixteenth units
        val prefix = measure.elements.subList(0, safeIndex).toMutableList()
        val prefixUnits = prefix.sumOf { it.duration.sixteenthUnits }

        return if (prefixUnits + newUnits <= barUnits) {
            // Consume forward from safeIndex
            var neededFromSuffix = newUnits
            var idx = safeIndex
            val result = mutableListOf<RhythmElement>()
            result.addAll(prefix)
            result.add(RhythmElement(duration = clampedDuration, isRest = newIsRest))

            while (idx < measure.elements.size && neededFromSuffix > 0) {
                val curr = measure.elements[idx]
                val u = curr.duration.sixteenthUnits
                if (u <= neededFromSuffix) {
                    neededFromSuffix -= u
                } else {
                    val leftover = u - neededFromSuffix
                    neededFromSuffix = 0
                    result.addAll(decomposeUnits(leftover, isRest = curr.isRest, smallestFirst = true))
                }
                idx++
            }
            while (idx < measure.elements.size) {
                result.add(measure.elements[idx])
                idx++
            }
            Measure(result) to prefix.size
        } else {
            // New duration overflows the end of the bar if started at prefixUnits;
            // shift start earlier in the bar so the user's chosen note duration fits at the end of the bar.
            val targetPrefixUnits = barUnits - newUnits
            val newPrefix = mutableListOf<RhythmElement>()
            var acc = 0
            for (el in prefix) {
                if (acc >= targetPrefixUnits) break
                val rem = targetPrefixUnits - acc
                if (el.duration.sixteenthUnits <= rem) {
                    newPrefix.add(el)
                    acc += el.duration.sixteenthUnits
                } else {
                    newPrefix.addAll(decomposeUnits(rem, isRest = el.isRest))
                    acc = targetPrefixUnits
                }
            }
            if (acc < targetPrefixUnits) {
                newPrefix.addAll(decomposeUnits(targetPrefixUnits - acc, isRest = false))
            }
            val newIndex = newPrefix.size
            newPrefix.add(RhythmElement(duration = clampedDuration, isRest = newIsRest))
            Measure(newPrefix) to newIndex
        }
    }

    /**
     * Generates a random musical rhythm for a single measure of [timeSignature].
     */
    fun randomMeasure(timeSignature: TimeSignature, random: Random = Random.Default): Measure {
        val barUnits = timeSignature.sixteenthUnitsPerBar
        val elements = mutableListOf<RhythmElement>()
        var remaining = barUnits
        while (remaining > 0) {
            val candidates = NoteDuration.entries.filter { it.sixteenthUnits <= remaining }
            // Weight quarter and eighth notes slightly higher for musicality
            val weighted = candidates.flatMap { dur ->
                when (dur) {
                    NoteDuration.WHOLE -> listOf(dur)
                    NoteDuration.HALF -> listOf(dur, dur)
                    NoteDuration.QUARTER -> listOf(dur, dur, dur, dur)
                    NoteDuration.EIGHTH -> listOf(dur, dur, dur)
                    NoteDuration.SIXTEENTH -> listOf(dur, dur)
                }
            }
            val chosen = weighted[random.nextInt(weighted.size)]
            val isRest = random.nextFloat() < 0.22f

            if (chosen == NoteDuration.SIXTEENTH && remaining >= 4 && random.nextBoolean()) {
                // Often group sixteenths in pairs or groups of 4
                val groupCount = if (remaining >= 4 && random.nextBoolean()) 4 else 2
                repeat(groupCount) {
                    elements.add(RhythmElement(duration = NoteDuration.SIXTEENTH, isRest = false))
                }
                remaining -= groupCount
            } else if (chosen == NoteDuration.EIGHTH && remaining >= 4 && random.nextBoolean()) {
                repeat(2) {
                    elements.add(RhythmElement(duration = NoteDuration.EIGHTH, isRest = false))
                }
                remaining -= 4
            } else {
                elements.add(RhythmElement(duration = chosen, isRest = isRest))
                remaining -= chosen.sixteenthUnits
            }
        }
        return Measure(elements)
    }
}
