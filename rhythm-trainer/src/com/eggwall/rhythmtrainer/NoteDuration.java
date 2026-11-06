package com.eggwall.rhythmtrainer;

/**
 * Standard musical note/rest durations measured in sixteenth-note subdivisions.
 */
public enum NoteDuration {
    WHOLE("Whole", "1", 16),
    HALF("Half", "1/2", 8),
    QUARTER("Quarter", "1/4", 4),
    EIGHTH("Eighth", "1/8", 2),
    SIXTEENTH("Sixteenth", "1/16", 1);

    public final String displayName;
    public final String fractionText;
    public final int sixteenthUnits;

    NoteDuration(String displayName, String fractionText, int sixteenthUnits) {
        this.displayName = displayName;
        this.fractionText = fractionText;
        this.sixteenthUnits = sixteenthUnits;
    }

    public String durationLabel(boolean isRest) {
        return isRest
                ? displayName + " Rest (" + fractionText + ")"
                : displayName + " Note (" + fractionText + ")";
    }
}
