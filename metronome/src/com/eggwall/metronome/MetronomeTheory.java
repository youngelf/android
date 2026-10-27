package com.eggwall.metronome;

/**
 * Pure, stateless rhythm, tempo, time-signature, subdivision, click-drop, and
 * tap-tempo math for the Precision Metronome.
 *
 * <p>Designed with zero Android UI dependencies so 100% of the musical timing
 * and rhythm rules can be tested directly on a host JVM via JUnit 4
 * ({@code MetronomeTheoryTest}).
 */
public final class MetronomeTheory {

    private MetronomeTheory() {}

    /** Minimum supported tempo in Beats Per Minute. */
    public static final int MIN_BPM = 20;
    /** Maximum supported tempo in Beats Per Minute. */
    public static final int MAX_BPM = 300;
    /** Default startup tempo in Beats Per Minute. */
    public static final int DEFAULT_BPM = 120;

    /** Number of initial measures that always play 100% of clicks before Drop% activates. */
    public static final int LOCK_IN_MEASURES = 4;

    /** Beat state: silent / muted beat. */
    public static final int BEAT_MUTE = 0;
    /** Beat state: standard unaccented pulse. */
    public static final int BEAT_NORMAL = 1;
    /** Beat state: secondary/medium accent (e.g. beat 3 in 4/4 or beat 4 in 6/8). */
    public static final int BEAT_SUB_ACCENT = 2;
    /** Beat state: primary downbeat accent (Beat 1). */
    public static final int BEAT_ACCENT = 3;

    /** Subdivision pulse level used for off-beat subdivision ticks. */
    public static final int BEAT_SUBDIVISION = 4;

    /** Maximum gap in milliseconds between consecutive taps before Tap Tempo resets. */
    public static final long TAP_RESET_THRESHOLD_MS = 3000L;

    /** Immutable descriptor for a musical time signature. */
    public static final class TimeSignature {
        public final String name;
        public final int numerator;
        public final int denominator;
        public final String feelName;
        public final int[] defaultBeatStates;

        public TimeSignature(String name, int numerator, int denominator,
                             String feelName, int[] defaultBeatStates) {
            this.name = name;
            this.numerator = numerator;
            this.denominator = denominator;
            this.feelName = feelName;
            this.defaultBeatStates = defaultBeatStates.clone();
        }
    }

    /** Supported time signatures covering simple, compound, and odd meters. */
    public static final TimeSignature[] TIME_SIGNATURES = new TimeSignature[] {
        new TimeSignature("4/4", 4, 4, "Common Time",
                new int[] { BEAT_ACCENT, BEAT_NORMAL, BEAT_SUB_ACCENT, BEAT_NORMAL }),
        new TimeSignature("3/4", 3, 4, "WaltzTriple",
                new int[] { BEAT_ACCENT, BEAT_NORMAL, BEAT_NORMAL }),
        new TimeSignature("2/4", 2, 4, "March Duple",
                new int[] { BEAT_ACCENT, BEAT_NORMAL }),
        new TimeSignature("5/4", 5, 4, "Quintuple (3+2)",
                new int[] { BEAT_ACCENT, BEAT_NORMAL, BEAT_NORMAL, BEAT_SUB_ACCENT, BEAT_NORMAL }),
        new TimeSignature("6/8", 6, 8, "Compound Duple",
                new int[] { BEAT_ACCENT, BEAT_NORMAL, BEAT_NORMAL, BEAT_SUB_ACCENT, BEAT_NORMAL, BEAT_NORMAL }),
        new TimeSignature("7/8", 7, 8, "Odd Balkan (2+2+3)",
                new int[] { BEAT_ACCENT, BEAT_NORMAL, BEAT_SUB_ACCENT, BEAT_NORMAL, BEAT_SUB_ACCENT, BEAT_NORMAL, BEAT_NORMAL }),
        new TimeSignature("7/4", 7, 4, "Septuple (4+3)",
                new int[] { BEAT_ACCENT, BEAT_NORMAL, BEAT_NORMAL, BEAT_NORMAL, BEAT_SUB_ACCENT, BEAT_NORMAL, BEAT_NORMAL }),
        new TimeSignature("9/8", 9, 8, "Compound Triple",
                new int[] { BEAT_ACCENT, BEAT_NORMAL, BEAT_NORMAL, BEAT_SUB_ACCENT, BEAT_NORMAL, BEAT_NORMAL, BEAT_SUB_ACCENT, BEAT_NORMAL, BEAT_NORMAL }),
        new TimeSignature("12/8", 12, 8, "Slow Blues Shuffle",
                new int[] { BEAT_ACCENT, BEAT_NORMAL, BEAT_NORMAL, BEAT_SUB_ACCENT, BEAT_NORMAL, BEAT_NORMAL, BEAT_SUB_ACCENT, BEAT_NORMAL, BEAT_NORMAL, BEAT_SUB_ACCENT, BEAT_NORMAL, BEAT_NORMAL })
    };

    /** Immutable descriptor for intra-beat rhythm subdivisions. */
    public static final class Subdivision {
        public final String label;
        public final String shortBadge;
        /** Fractional offsets within a single beat [0.0, 1.0). Index 0 is always 0.0 (on-beat). */
        public final double[] pulseOffsets;

        public Subdivision(String label, String shortBadge, double[] pulseOffsets) {
            this.label = label;
            this.shortBadge = shortBadge;
            this.pulseOffsets = pulseOffsets.clone();
        }
    }

    /** Supported intra-beat subdivision patterns. */
    public static final Subdivision[] SUBDIVISIONS = new Subdivision[] {
        new Subdivision("Quarter (1x)",   "1x QTR",   new double[] { 0.0 }),
        new Subdivision("Eighth (2x)",    "2x 8TH",   new double[] { 0.0, 0.5 }),
        new Subdivision("Triplet (3x)",   "3x TRIP",  new double[] { 0.0, 1.0 / 3.0, 2.0 / 3.0 }),
        new Subdivision("Sixteenth (4x)", "4x 16TH",  new double[] { 0.0, 0.25, 0.5, 0.75 }),
        new Subdivision("Swing 8ths",     "SWING",    new double[] { 0.0, 2.0 / 3.0 })
    };

    /** Names of the 4 synthesized percussion/click sound kits. */
    public static final String[] SOUND_KIT_NAMES = new String[] {
        "V90 Classic",
        "Woodblock",
        "Studio Rim",
        "Soft Sine"
    };

    /** Short badge labels for the 4 synthesized sound kits. */
    public static final String[] SOUND_KIT_BADGES = new String[] {
        "V90 CLICK",
        "WOODBLOCK",
        "STUDIO RIM",
        "SOFT SINE"
    };

    /** Speed-trainer progression presets (BPM increment per N measures). */
    public static final int[] TRAINER_STEP_BPM = new int[] { 0, 2, 4, 5 };
    public static final int TRAINER_MEASURES_PER_STEP = 4;
    public static final String[] TRAINER_LABELS = new String[] {
        "OFF",
        "+2 / 4 BARS",
        "+4 / 4 BARS",
        "+5 / 4 BARS"
    };

    /** Clamps a BPM value into the valid {@link #MIN_BPM}..{@link #MAX_BPM} range. */
    public static int clampBpm(int bpm) {
        if (bpm < MIN_BPM) return MIN_BPM;
        if (bpm > MAX_BPM) return MAX_BPM;
        return bpm;
    }

    /** Clamps a click drop-rate percentage to 0..100% in 5% increments. */
    public static int clampDropPct(int dropPct) {
        if (dropPct < 0) return 0;
        if (dropPct > 100) return 100;
        return (dropPct / 5) * 5;
    }

    /**
     * Returns the traditional Italian classical tempo marking for the given BPM.
     */
    public static String getTempoMarking(int bpm) {
        if (bpm < 40)  return "GRAVE";
        if (bpm < 45)  return "LARGO";
        if (bpm < 55)  return "LENTO";
        if (bpm < 66)  return "LARGHETTO";
        if (bpm < 76)  return "ADAGIO";
        if (bpm < 92)  return "ANDANTE";
        if (bpm < 108) return "ANDANTINO";
        if (bpm < 120) return "MODERATO";
        if (bpm < 156) return "ALLEGRO";
        if (bpm < 176) return "VIVACE";
        if (bpm < 200) return "PRESTO";
        return "PRESTISSIMO";
    }

    /**
     * Returns a concise English musical character description for the given BPM.
     */
    public static String getTempoDescription(int bpm) {
        if (bpm < 40)  return "Solemn & very slow";
        if (bpm < 45)  return "Broad & stately";
        if (bpm < 55)  return "Slow & spacious";
        if (bpm < 66)  return "Rather broadly";
        if (bpm < 76)  return "Slow & expressive";
        if (bpm < 92)  return "Relaxed walking pace";
        if (bpm < 108) return "Light walking pace";
        if (bpm < 120) return "Moderate & steady";
        if (bpm < 156) return "Fast, bright & lively";
        if (bpm < 176) return "Vivid & brisk";
        if (bpm < 200) return "Very fast & driving";
        return "Virtuosic maximum speed";
    }

    /**
     * Cycles a beat's accent state when tapped by the user:
     * {@code ACCENT -> NORMAL -> SUB_ACCENT -> MUTE -> ACCENT}.
     */
    public static int cycleBeatState(int currentState) {
        switch (currentState) {
            case BEAT_ACCENT:
                return BEAT_NORMAL;
            case BEAT_NORMAL:
                return BEAT_SUB_ACCENT;
            case BEAT_SUB_ACCENT:
                return BEAT_MUTE;
            case BEAT_MUTE:
            default:
                return BEAT_ACCENT;
        }
    }

    /** Returns the short badge label for a beat's accent state. */
    public static String getBeatStateLabel(int state) {
        switch (state) {
            case BEAT_ACCENT:
                return "ACC";
            case BEAT_SUB_ACCENT:
                return "SUB";
            case BEAT_NORMAL:
                return "BEAT";
            case BEAT_MUTE:
            default:
                return "MUTE";
        }
    }

    /**
     * Computes the duration of a single beat in milliseconds for the given BPM.
     */
    public static double getBeatIntervalMs(int bpm) {
        int clamped = clampBpm(bpm);
        return 60000.0 / (double) clamped;
    }

    /**
     * Computes the exact number of audio frames per beat at the given sample rate.
     */
    public static double getSamplesPerBeat(int bpm, int sampleRate) {
        int clamped = clampBpm(bpm);
        return ((double) sampleRate * 60.0) / (double) clamped;
    }

    /**
     * Determines whether an audio click should be sounded for a beat given the
     * PowKiddy V90 4-measure lock-in rule and random drop percentage.
     *
     * <ul>
     *   <li>If {@code beatState == BEAT_MUTE}, never plays.</li>
     *   <li>For the first {@link #LOCK_IN_MEASURES} measures ({@code measureCount < 4}),
     *       always plays 100% of non-muted clicks so the musician locks into the groove.</li>
     *   <li>From measure 5 onward ({@code measureCount >= 4}), drops the click with
     *       probability {@code dropPct}% (using {@code random0to99} in {@code [0, 99]}).</li>
     * </ul>
     */
    public static boolean shouldPlayClick(int beatState, int measureCount, int dropPct, int random0to99) {
        if (beatState == BEAT_MUTE) {
            return false;
        }
        if (measureCount < LOCK_IN_MEASURES || dropPct <= 0) {
            return true;
        }
        if (dropPct >= 100) {
            return false;
        }
        int r = ((random0to99 % 100) + 100) % 100;
        return r >= dropPct;
    }

    /**
     * Computes the effective BPM when Speed Trainer mode is active.
     *
     * @param baseBpm        Starting BPM configured by the user
     * @param measureCount   Zero-based measures elapsed since playback started
     * @param stepBpm        BPM increase per step (0 = disabled)
     * @param everyNBars     Number of bars per tempo increment (e.g. 4)
     * @param maxTargetBpm   Ceiling BPM where the trainer stops accelerating
     * @return Clamped effective BPM for the current measure
     */
    public static int computeTrainerBpm(int baseBpm, int measureCount, int stepBpm, int everyNBars, int maxTargetBpm) {
        int start = clampBpm(baseBpm);
        if (stepBpm <= 0 || everyNBars <= 0 || measureCount <= 0) {
            return start;
        }
        int cap = clampBpm(Math.max(start, maxTargetBpm));
        int steps = measureCount / everyNBars;
        int candidateBpm = start + steps * stepBpm;
        return Math.min(cap, clampBpm(candidateBpm));
    }

    /**
     * Computes an averaged BPM from an array of tap timestamps in milliseconds.
     * Only consecutive taps separated by at most {@link #TAP_RESET_THRESHOLD_MS} are averaged.
     *
     * @param tapTimestampsMs Chronological tap timestamps in milliseconds
     * @param count           Number of valid entries in {@code tapTimestampsMs}
     * @return Averaged and clamped BPM, or {@code -1} if fewer than 2 valid consecutive taps exist
     */
    public static int computeTapTempoBpm(long[] tapTimestampsMs, int count) {
        if (tapTimestampsMs == null || count < 2) {
            return -1;
        }
        int n = Math.min(count, tapTimestampsMs.length);
        long totalDeltaMs = 0L;
        int validIntervals = 0;

        for (int i = n - 1; i >= 1; i--) {
            long dt = tapTimestampsMs[i] - tapTimestampsMs[i - 1];
            if (dt <= 0L || dt > TAP_RESET_THRESHOLD_MS) {
                break;
            }
            totalDeltaMs += dt;
            validIntervals++;
        }

        if (validIntervals == 0) {
            return -1;
        }
        double avgIntervalMs = (double) totalDeltaMs / (double) validIntervals;
        int bpm = (int) Math.round(60000.0 / avgIntervalMs);
        return clampBpm(bpm);
    }

    /**
     * Computes the visual pendulum swing angle in degrees {@code [-maxSwingDeg, +maxSwingDeg]}
     * for a given beat index and intra-beat phase {@code [0.0, 1.0]}.
     *
     * <p>Even-indexed beats swing smoothly from left ({@code -maxSwingDeg}) to right
     * ({@code +maxSwingDeg}); odd-indexed beats swing from right back to left using
     * cosine-eased harmonic motion.
     */
    public static float computePendulumAngleDeg(int beatIndex, double beatPhase0to1, float maxSwingDeg) {
        double p = Math.max(0.0, Math.min(1.0, beatPhase0to1));
        /* Cosine-eased normalized position in [-1.0, +1.0] */
        double eased = -Math.cos(p * Math.PI);
        if ((beatIndex & 1) == 1) {
            eased = -eased;
        }
        return (float) (eased * maxSwingDeg);
    }
}
