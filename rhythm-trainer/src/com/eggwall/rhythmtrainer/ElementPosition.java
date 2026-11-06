package com.eggwall.rhythmtrainer;

/**
 * Identifies a specific note/rest position by measure (bar) index and element index within that measure.
 */
public final class ElementPosition {
    public final int measureIndex;
    public final int elementIndex;

    public ElementPosition(int measureIndex, int elementIndex) {
        this.measureIndex = measureIndex;
        this.elementIndex = elementIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ElementPosition)) return false;
        ElementPosition that = (ElementPosition) o;
        return measureIndex == that.measureIndex && elementIndex == that.elementIndex;
    }

    @Override
    public int hashCode() {
        return 31 * measureIndex + elementIndex;
    }
}
