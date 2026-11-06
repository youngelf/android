package com.eggwall.rhythmtrainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Pure-Java rhythm builder and bar-length invariant engine.
 */
public final class RhythmBuilder {

    public static final class UpdateResult {
        public final Measure measure;
        public final int selectedIndex;

        public UpdateResult(Measure measure, int selectedIndex) {
            this.measure = measure;
            this.selectedIndex = selectedIndex;
        }
    }

    private static final Random DEFAULT_RANDOM = new Random();

    private RhythmBuilder() {}

    /**
     * Decomposes an arbitrary number of sixteenth-note units into valid NoteDuration elements.
     */
    public static List<RhythmElement> decomposeUnits(int units, boolean isRest, boolean smallestFirst) {
        if (units <= 0) return Collections.emptyList();
        List<RhythmElement> result = new ArrayList<RhythmElement>();
        int remaining = units;
        NoteDuration[] ordered = NoteDuration.values(); // WHOLE(16) down to SIXTEENTH(1)
        for (int i = 0; i < ordered.length; i++) {
            NoteDuration dur = ordered[i];
            while (remaining >= dur.sixteenthUnits) {
                result.add(new RhythmElement(dur, isRest));
                remaining -= dur.sixteenthUnits;
            }
        }
        if (smallestFirst) {
            Collections.reverse(result);
        }
        return result;
    }

    /**
     * Creates a default two-bar rhythm pattern for the given TimeSignature that showcases
     * standard note durations and rests while respecting the exact bar length.
     */
    public static List<Measure> defaultMeasuresFor(TimeSignature timeSignature) {
        List<Measure> list = new ArrayList<Measure>(2);
        switch (timeSignature) {
            case FOUR_FOUR:
                list.add(new Measure(Arrays.asList(
                        new RhythmElement(NoteDuration.QUARTER, false),
                        new RhythmElement(NoteDuration.EIGHTH, false),
                        new RhythmElement(NoteDuration.EIGHTH, false),
                        new RhythmElement(NoteDuration.QUARTER, true),
                        new RhythmElement(NoteDuration.QUARTER, false)
                )));
                list.add(new Measure(Arrays.asList(
                        new RhythmElement(NoteDuration.HALF, false),
                        new RhythmElement(NoteDuration.SIXTEENTH, false),
                        new RhythmElement(NoteDuration.SIXTEENTH, false),
                        new RhythmElement(NoteDuration.SIXTEENTH, false),
                        new RhythmElement(NoteDuration.SIXTEENTH, false),
                        new RhythmElement(NoteDuration.QUARTER, false)
                )));
                break;

            case THREE_FOUR:
                list.add(new Measure(Arrays.asList(
                        new RhythmElement(NoteDuration.QUARTER, false),
                        new RhythmElement(NoteDuration.EIGHTH, false),
                        new RhythmElement(NoteDuration.EIGHTH, false),
                        new RhythmElement(NoteDuration.QUARTER, false)
                )));
                list.add(new Measure(Arrays.asList(
                        new RhythmElement(NoteDuration.HALF, false),
                        new RhythmElement(NoteDuration.QUARTER, true)
                )));
                break;

            case TWO_TWO:
                list.add(new Measure(Arrays.asList(
                        new RhythmElement(NoteDuration.HALF, false),
                        new RhythmElement(NoteDuration.QUARTER, false),
                        new RhythmElement(NoteDuration.QUARTER, false)
                )));
                list.add(new Measure(Collections.singletonList(
                        new RhythmElement(NoteDuration.WHOLE, false)
                )));
                break;

            case FIVE_FOUR:
                list.add(new Measure(Arrays.asList(
                        new RhythmElement(NoteDuration.HALF, false),
                        new RhythmElement(NoteDuration.QUARTER, false),
                        new RhythmElement(NoteDuration.EIGHTH, false),
                        new RhythmElement(NoteDuration.EIGHTH, false),
                        new RhythmElement(NoteDuration.QUARTER, true)
                )));
                list.add(new Measure(Arrays.asList(
                        new RhythmElement(NoteDuration.QUARTER, false),
                        new RhythmElement(NoteDuration.QUARTER, false),
                        new RhythmElement(NoteDuration.QUARTER, false),
                        new RhythmElement(NoteDuration.SIXTEENTH, false),
                        new RhythmElement(NoteDuration.SIXTEENTH, false),
                        new RhythmElement(NoteDuration.EIGHTH, false),
                        new RhythmElement(NoteDuration.QUARTER, false)
                )));
                break;

            case SEVEN_FOUR:
            default:
                list.add(new Measure(Arrays.asList(
                        new RhythmElement(NoteDuration.HALF, false),
                        new RhythmElement(NoteDuration.QUARTER, false),
                        new RhythmElement(NoteDuration.QUARTER, false),
                        new RhythmElement(NoteDuration.EIGHTH, false),
                        new RhythmElement(NoteDuration.EIGHTH, false),
                        new RhythmElement(NoteDuration.QUARTER, true),
                        new RhythmElement(NoteDuration.QUARTER, false)
                )));
                list.add(new Measure(Arrays.asList(
                        new RhythmElement(NoteDuration.WHOLE, false),
                        new RhythmElement(NoteDuration.HALF, false),
                        new RhythmElement(NoteDuration.QUARTER, false)
                )));
                break;
        }
        return list;
    }

    /**
     * Creates a single bar filled with quarter notes for the given TimeSignature.
     */
    public static Measure quarterNoteBar(TimeSignature timeSignature) {
        int count = timeSignature.getQuarterBeatsPerBar();
        List<RhythmElement> elems = new ArrayList<RhythmElement>(count);
        for (int i = 0; i < count; i++) {
            elems.add(new RhythmElement(NoteDuration.QUARTER, false));
        }
        return new Measure(elems);
    }

    /**
     * Adapts an existing list of measures when the time signature changes so every bar has
     * exactly TimeSignature.sixteenthUnitsPerBar units.
     */
    public static List<Measure> adaptMeasuresToTimeSignature(
            List<Measure> measures,
            TimeSignature newTimeSignature) {
        if (measures == null || measures.isEmpty()) {
            return defaultMeasuresFor(newTimeSignature);
        }
        int targetUnits = newTimeSignature.sixteenthUnitsPerBar;
        List<Measure> result = new ArrayList<Measure>(measures.size());
        for (int m = 0; m < measures.size(); m++) {
            Measure measure = measures.get(m);
            List<RhythmElement> updated = new ArrayList<RhythmElement>();
            int accumulated = 0;
            for (int i = 0; i < measure.elements.size(); i++) {
                if (accumulated >= targetUnits) break;
                RhythmElement el = measure.elements.get(i);
                int remaining = targetUnits - accumulated;
                if (el.duration.sixteenthUnits <= remaining) {
                    updated.add(el);
                    accumulated += el.duration.sixteenthUnits;
                } else {
                    updated.addAll(decomposeUnits(remaining, el.isRest, false));
                    accumulated = targetUnits;
                }
            }
            if (accumulated < targetUnits) {
                updated.addAll(decomposeUnits(targetUnits - accumulated, false, false));
            }
            result.add(new Measure(updated));
        }
        return result;
    }

    /**
     * Updates the element at elementIndex within measure to have newDuration and newIsRest,
     * automatically splitting or absorbing adjacent elements in the same measure so the bar total
     * remains exactly barUnits sixteenth-note units.
     */
    public static UpdateResult updateElementInMeasure(
            Measure measure,
            int elementIndex,
            NoteDuration newDuration,
            boolean newIsRest,
            int barUnits) {
        if (measure == null || measure.elements.isEmpty()) {
            return new UpdateResult(new Measure(decomposeUnits(barUnits, newIsRest, false)), 0);
        }
        int safeIndex = Math.max(0, Math.min(elementIndex, measure.elements.size() - 1));
        NoteDuration clampedDuration = newDuration;
        if (newDuration.sixteenthUnits > barUnits) {
            NoteDuration[] all = NoteDuration.values();
            for (int i = 0; i < all.length; i++) {
                if (all[i].sixteenthUnits <= barUnits) {
                    clampedDuration = all[i];
                    break;
                }
            }
        }
        RhythmElement oldElement = measure.elements.get(safeIndex);
        int oldUnits = oldElement.duration.sixteenthUnits;
        int newUnits = clampedDuration.sixteenthUnits;

        // Case 1: Same duration, only toggling note vs rest
        if (oldUnits == newUnits) {
            List<RhythmElement> copy = new ArrayList<RhythmElement>(measure.elements);
            copy.set(safeIndex, oldElement.withDurationAndRest(clampedDuration, newIsRest));
            return new UpdateResult(new Measure(copy), safeIndex);
        }

        // Case 2: Shortening duration (e.g., Quarter -> Eighth or Sixteenth)
        if (newUnits < oldUnits) {
            List<RhythmElement> copy = new ArrayList<RhythmElement>(measure.elements);
            List<RhythmElement> replacement = new ArrayList<RhythmElement>();
            if (oldUnits % newUnits == 0 && (oldUnits / newUnits) <= 4) {
                int count = oldUnits / newUnits;
                replacement.add(new RhythmElement(clampedDuration, newIsRest));
                for (int i = 0; i < count - 1; i++) {
                    replacement.add(new RhythmElement(clampedDuration, oldElement.isRest));
                }
            } else {
                replacement.add(new RhythmElement(clampedDuration, newIsRest));
                replacement.addAll(decomposeUnits(oldUnits - newUnits, oldElement.isRest, true));
            }
            copy.remove(safeIndex);
            copy.addAll(safeIndex, replacement);
            return new UpdateResult(new Measure(copy), safeIndex);
        }

        // Case 3: Lengthening duration (e.g., Eighth -> Quarter / Half / Whole)
        List<RhythmElement> prefix = new ArrayList<RhythmElement>(measure.elements.subList(0, safeIndex));
        int prefixUnits = 0;
        for (int i = 0; i < prefix.size(); i++) {
            prefixUnits += prefix.get(i).duration.sixteenthUnits;
        }

        if (prefixUnits + newUnits <= barUnits) {
            int neededFromSuffix = newUnits;
            int idx = safeIndex;
            List<RhythmElement> result = new ArrayList<RhythmElement>(prefix);
            result.add(new RhythmElement(clampedDuration, newIsRest));

            while (idx < measure.elements.size() && neededFromSuffix > 0) {
                RhythmElement curr = measure.elements.get(idx);
                int u = curr.duration.sixteenthUnits;
                if (u <= neededFromSuffix) {
                    neededFromSuffix -= u;
                } else {
                    int leftover = u - neededFromSuffix;
                    neededFromSuffix = 0;
                    result.addAll(decomposeUnits(leftover, curr.isRest, true));
                }
                idx++;
            }
            while (idx < measure.elements.size()) {
                result.add(measure.elements.get(idx));
                idx++;
            }
            return new UpdateResult(new Measure(result), prefix.size());
        } else {
            int targetPrefixUnits = barUnits - newUnits;
            List<RhythmElement> newPrefix = new ArrayList<RhythmElement>();
            int acc = 0;
            for (int i = 0; i < prefix.size(); i++) {
                if (acc >= targetPrefixUnits) break;
                RhythmElement el = prefix.get(i);
                int rem = targetPrefixUnits - acc;
                if (el.duration.sixteenthUnits <= rem) {
                    newPrefix.add(el);
                    acc += el.duration.sixteenthUnits;
                } else {
                    newPrefix.addAll(decomposeUnits(rem, el.isRest, false));
                    acc = targetPrefixUnits;
                }
            }
            if (acc < targetPrefixUnits) {
                newPrefix.addAll(decomposeUnits(targetPrefixUnits - acc, false, false));
            }
            int newIndex = newPrefix.size();
            newPrefix.add(new RhythmElement(clampedDuration, newIsRest));
            return new UpdateResult(new Measure(newPrefix), newIndex);
        }
    }

    public static Measure randomMeasure(TimeSignature timeSignature) {
        return randomMeasure(timeSignature, DEFAULT_RANDOM);
    }

    public static Measure randomMeasure(TimeSignature timeSignature, Random random) {
        int barUnits = timeSignature.sixteenthUnitsPerBar;
        List<RhythmElement> elements = new ArrayList<RhythmElement>();
        int remaining = barUnits;
        NoteDuration[] all = NoteDuration.values();
        while (remaining > 0) {
            List<NoteDuration> weighted = new ArrayList<NoteDuration>();
            for (int i = 0; i < all.length; i++) {
                NoteDuration dur = all[i];
                if (dur.sixteenthUnits <= remaining) {
                    int w = 1;
                    switch (dur) {
                        case WHOLE: w = 1; break;
                        case HALF: w = 2; break;
                        case QUARTER: w = 4; break;
                        case EIGHTH: w = 3; break;
                        case SIXTEENTH: w = 2; break;
                    }
                    for (int k = 0; k < w; k++) {
                        weighted.add(dur);
                    }
                }
            }
            NoteDuration chosen = weighted.get(random.nextInt(weighted.size()));
            boolean isRest = random.nextFloat() < 0.22f;

            if (chosen == NoteDuration.SIXTEENTH && remaining >= 4 && random.nextBoolean()) {
                int groupCount = (remaining >= 4 && random.nextBoolean()) ? 4 : 2;
                for (int g = 0; g < groupCount; g++) {
                    elements.add(new RhythmElement(NoteDuration.SIXTEENTH, false));
                }
                remaining -= groupCount;
            } else if (chosen == NoteDuration.EIGHTH && remaining >= 4 && random.nextBoolean()) {
                elements.add(new RhythmElement(NoteDuration.EIGHTH, false));
                elements.add(new RhythmElement(NoteDuration.EIGHTH, false));
                remaining -= 4;
            } else {
                elements.add(new RhythmElement(chosen, isRest));
                remaining -= chosen.sixteenthUnits;
            }
        }
        return new Measure(elements);
    }
}
