package com.eggwall.rhythmtrainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A single bar (measure) separated by bar lines.
 * The sum of elements' sixteenthUnits always equals TimeSignature.sixteenthUnitsPerBar.
 */
public final class Measure {
    public final List<RhythmElement> elements;
    public final int totalSixteenthUnits;

    public Measure(List<RhythmElement> elements) {
        List<RhythmElement> copy = new ArrayList<RhythmElement>(elements);
        this.elements = Collections.unmodifiableList(copy);
        int sum = 0;
        for (int i = 0; i < copy.size(); i++) {
            sum += copy.get(i).duration.sixteenthUnits;
        }
        this.totalSixteenthUnits = sum;
    }
}
