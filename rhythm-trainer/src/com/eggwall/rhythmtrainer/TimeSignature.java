package com.eggwall.rhythmtrainer;

/**
 * Supported time signatures as specified in requirements:
 * 4/4, 3/4, 2/2, 5/4, and 7/4.
 */
public enum TimeSignature {
    FOUR_FOUR("4/4", 4, 4, 16),
    THREE_FOUR("3/4", 3, 4, 12),
    TWO_TWO("2/2", 2, 2, 16),
    FIVE_FOUR("5/4", 5, 4, 20),
    SEVEN_FOUR("7/4", 7, 4, 28);

    public final String label;
    public final int numerator;
    public final int denominator;
    public final int sixteenthUnitsPerBar;

    TimeSignature(String label, int numerator, int denominator, int sixteenthUnitsPerBar) {
        this.label = label;
        this.numerator = numerator;
        this.denominator = denominator;
        this.sixteenthUnitsPerBar = sixteenthUnitsPerBar;
    }

    /** Number of quarter-note beats in one bar. */
    public int getQuarterBeatsPerBar() {
        return sixteenthUnitsPerBar / 4;
    }
}
