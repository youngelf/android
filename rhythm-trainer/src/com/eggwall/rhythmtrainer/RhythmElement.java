package com.eggwall.rhythmtrainer;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A single rhythmic event (either a pitched click/tone of fixed 440 Hz frequency or a silent rest).
 */
public final class RhythmElement {
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1L);

    public final long id;
    public final NoteDuration duration;
    public final boolean isRest;

    public RhythmElement(NoteDuration duration, boolean isRest) {
        this(ID_GENERATOR.getAndIncrement(), duration, isRest);
    }

    public RhythmElement(long id, NoteDuration duration, boolean isRest) {
        this.id = id;
        this.duration = duration;
        this.isRest = isRest;
    }

    public RhythmElement withDurationAndRest(NoteDuration newDuration, boolean newIsRest) {
        return new RhythmElement(this.id, newDuration, newIsRest);
    }
}
