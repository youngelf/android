package com.eggwall.pomodoro;

import java.util.Locale;

/**
 * Pure, stateless-friendly Pomodoro timer state machine, preset catalog, and work-hours
 * statistics tracker translated from the PowKiddy V90 / Anbernic RG35XX SP {@code pomodoro.c}
 * application.
 *
 * <p>This class has zero Android UI dependencies so that 100% of the timer transitions,
 * 4-session long-break cycle math, duration clamping, preset loading, and daily/lifetime
 * work-hour calculations can be unit-tested directly on a host JVM via JUnit 4.
 */
public final class PomodoroModel {

    /** Focus / Work session phase. */
    public static final int MODE_WORK = 0;
    /** Short Break phase (taken after Pomodoros 1, 2, and 3 in a 4-session cycle). */
    public static final int MODE_SHORT_BREAK = 1;
    /** Long Break phase (taken after every 4th completed Pomodoro). */
    public static final int MODE_LONG_BREAK = 2;
    /** Total number of timer phases. */
    public static final int MODE_COUNT = 3;

    /** Timer is reset to the full duration of the active phase and waiting to start. */
    public static final int STATUS_READY = 0;
    /** Timer is actively counting down once per second. */
    public static final int STATUS_RUNNING = 1;
    /** Timer countdown is paused mid-phase. */
    public static final int STATUS_PAUSED = 2;
    /** Timer reached 00:00 and is waiting to advance to the next phase. */
    public static final int STATUS_DONE = 3;

    /** Number of completed Focus sessions before triggering a Long Break. */
    public static final int POMODOROS_PER_LONG_BREAK = 4;

    /** Default Focus duration in minutes (Classic Pomodoro). */
    public static final int DEFAULT_WORK_MINS = 25;
    /** Default Short Break duration in minutes. */
    public static final int DEFAULT_SHORT_BREAK_MINS = 5;
    /** Default Long Break duration in minutes. */
    public static final int DEFAULT_LONG_BREAK_MINS = 15;

    /** Minimum allowed duration in minutes for any phase. */
    public static final int MIN_DURATION_MINS = 1;
    /** Maximum allowed Focus duration in minutes. */
    public static final int MAX_WORK_MINS = 120;
    /** Maximum allowed Short Break duration in minutes. */
    public static final int MAX_SHORT_BREAK_MINS = 60;
    /** Maximum allowed Long Break duration in minutes. */
    public static final int MAX_LONG_BREAK_MINS = 90;

    /** Ambient focus soundscape: Silent during countdown (chime only on completion). */
    public static final int AMBIENT_SILENCE = 0;
    /** Ambient focus soundscape: Soft studio pendulum tick once per second. */
    public static final int AMBIENT_SOFT_TICK = 1;
    /** Ambient focus soundscape: Gentle pink-filtered rain patter. */
    public static final int AMBIENT_RAIN = 2;
    /** Ambient focus soundscape: Deep 1/f^2 brown noise for masking distractions. */
    public static final int AMBIENT_BROWN_NOISE = 3;
    /** Total number of ambient soundscape modes. */
    public static final int AMBIENT_COUNT = 4;

    /** Display labels for each ambient soundscape mode. */
    public static final String[] AMBIENT_NAMES = {
        "OFF",
        "SOFT TICK",
        "RAIN",
        "BROWN NOISE"
    };

    /**
     * Immutable definition of a Pomodoro duration preset (Focus / Short Break / Long Break).
     */
    public static final class DurationPreset {
        public final String name;
        public final String badge;
        public final int workMins;
        public final int shortBreakMins;
        public final int longBreakMins;

        public DurationPreset(String name, String badge, int workMins, int shortBreakMins, int longBreakMins) {
            this.name = name;
            this.badge = badge;
            this.workMins = workMins;
            this.shortBreakMins = shortBreakMins;
            this.longBreakMins = longBreakMins;
        }
    }

    /** Built-in Pomodoro duration presets. */
    public static final DurationPreset[] PRESETS = {
        new DurationPreset("Classic",   "25 / 5 / 15",  25,  5, 15),
        new DurationPreset("Deep Work", "50 / 10 / 20", 50, 10, 20),
        new DurationPreset("Studio",    "45 / 15 / 30", 45, 15, 30),
        new DurationPreset("Sprint",    "15 / 3 / 10",  15,  3, 10)
    };

    private int mMode = MODE_WORK;
    private int mStatus = STATUS_READY;
    private int mPresetIndex = 0;
    private int mAmbientMode = AMBIENT_SILENCE;

    private int mWorkMins = DEFAULT_WORK_MINS;
    private int mShortBreakMins = DEFAULT_SHORT_BREAK_MINS;
    private int mLongBreakMins = DEFAULT_LONG_BREAK_MINS;

    private int mTotalPhaseSeconds = DEFAULT_WORK_MINS * 60;
    private int mRemainingSeconds = DEFAULT_WORK_MINS * 60;

    private long mTotalWorkSeconds = 0L;
    private long mTodayWorkSeconds = 0L;
    private long mCurrentSessionWorkSeconds = 0L;
    private int mTotalPomodoros = 0;
    private int mTodayPomodoros = 0;
    private String mLastDate = "SESSION";

    public PomodoroModel() {
        resetToDefaults();
    }

    /** Resets all configuration and statistics to their initial default values. */
    public void resetToDefaults() {
        mMode = MODE_WORK;
        mStatus = STATUS_READY;
        mPresetIndex = 0;
        mAmbientMode = AMBIENT_SILENCE;
        mWorkMins = DEFAULT_WORK_MINS;
        mShortBreakMins = DEFAULT_SHORT_BREAK_MINS;
        mLongBreakMins = DEFAULT_LONG_BREAK_MINS;
        mTotalPhaseSeconds = mWorkMins * 60;
        mRemainingSeconds = mTotalPhaseSeconds;
        mTotalWorkSeconds = 0L;
        mTodayWorkSeconds = 0L;
        mCurrentSessionWorkSeconds = 0L;
        mTotalPomodoros = 0;
        mTodayPomodoros = 0;
        mLastDate = "SESSION";
    }

    public int getMode() {
        return mMode;
    }

    public int getStatus() {
        return mStatus;
    }

    public int getPresetIndex() {
        return mPresetIndex;
    }

    public int getAmbientMode() {
        return mAmbientMode;
    }

    public String getAmbientModeName() {
        return AMBIENT_NAMES[clamp(mAmbientMode, 0, AMBIENT_COUNT - 1)];
    }

    public int getWorkMins() {
        return mWorkMins;
    }

    public int getShortBreakMins() {
        return mShortBreakMins;
    }

    public int getLongBreakMins() {
        return mLongBreakMins;
    }

    public int getTotalPhaseSeconds() {
        return mTotalPhaseSeconds;
    }

    public int getRemainingSeconds() {
        return mRemainingSeconds;
    }

    public long getTotalWorkSeconds() {
        return mTotalWorkSeconds;
    }

    public long getTodayWorkSeconds() {
        return mTodayWorkSeconds;
    }

    public long getCurrentSessionWorkSeconds() {
        return mCurrentSessionWorkSeconds;
    }

    public int getTotalPomodoros() {
        return mTotalPomodoros;
    }

    public int getTodayPomodoros() {
        return mTodayPomodoros;
    }

    public String getLastDate() {
        return mLastDate;
    }

    /**
     * Returns the number of completed Pomodoros in the current 4-session cycle ({@code 0..4}).
     * When 4 sessions have just completed and the user is in {@code STATUS_DONE} (or in
     * {@code MODE_LONG_BREAK}), returns 4 to show a full 4-tomato cycle.
     */
    public int getCycleCompletedCount() {
        if (mTodayPomodoros <= 0) {
            return 0;
        }
        int mod = mTodayPomodoros % POMODOROS_PER_LONG_BREAK;
        if (mod == 0 && (mMode == MODE_LONG_BREAK || (mMode == MODE_WORK && mStatus == STATUS_DONE))) {
            return POMODOROS_PER_LONG_BREAK;
        }
        return mod;
    }

    /** Returns the duration in seconds configured for the specified phase mode. */
    public int getModeDurationSeconds(int mode) {
        switch (mode) {
            case MODE_WORK:
                return mWorkMins * 60;
            case MODE_SHORT_BREAK:
                return mShortBreakMins * 60;
            case MODE_LONG_BREAK:
                return mLongBreakMins * 60;
            default:
                return DEFAULT_WORK_MINS * 60;
        }
    }

    /** Returns the elapsed fraction ({@code 0.0f .. 1.0f}) of the current phase. */
    public float getProgressFraction() {
        if (mTotalPhaseSeconds <= 0) {
            return 0.0f;
        }
        int elapsed = mTotalPhaseSeconds - mRemainingSeconds;
        if (elapsed <= 0) return 0.0f;
        if (elapsed >= mTotalPhaseSeconds) return 1.0f;
        return (float) elapsed / (float) mTotalPhaseSeconds;
    }

    /**
     * Switches directly to the specified phase ({@link #MODE_WORK}, {@link #MODE_SHORT_BREAK},
     * or {@link #MODE_LONG_BREAK}) and resets the phase timer to {@link #STATUS_READY}.
     */
    public void setMode(int mode) {
        mMode = ((mode % MODE_COUNT) + MODE_COUNT) % MODE_COUNT;
        mStatus = STATUS_READY;
        mTotalPhaseSeconds = getModeDurationSeconds(mMode);
        mRemainingSeconds = mTotalPhaseSeconds;
    }

    /** Cycles forward (+1) or backward (-1) across the 3 timer modes. */
    public void cycleMode(int direction) {
        int next = (mMode + (direction >= 0 ? 1 : MODE_COUNT - 1)) % MODE_COUNT;
        setMode(next);
    }

    /** Resets the current phase countdown back to its full configured duration. */
    public void resetCurrentTimer() {
        mStatus = STATUS_READY;
        mTotalPhaseSeconds = getModeDurationSeconds(mMode);
        mRemainingSeconds = mTotalPhaseSeconds;
    }

    /**
     * Toggles the timer between running and paused states.
     * If the current phase is {@link #STATUS_DONE}, automatically advances to the next phase
     * in the Pomodoro cycle and starts running immediately (matching {@code pomodoro.c} lines 1386-1398).
     */
    public void toggleStartPause() {
        if (mStatus == STATUS_RUNNING) {
            mStatus = STATUS_PAUSED;
        } else {
            if (mStatus == STATUS_DONE) {
                advanceToNextPhase();
            }
            mStatus = STATUS_RUNNING;
        }
    }

    /** Explicitly pauses the countdown if it is currently running. */
    public void pause() {
        if (mStatus == STATUS_RUNNING) {
            mStatus = STATUS_PAUSED;
        }
    }

    /**
     * Advances to the next phase in the standard Pomodoro cycle and sets status to {@link #STATUS_READY}:
     * <ul>
     *   <li>From {@link #MODE_WORK}: transitions to {@link #MODE_LONG_BREAK} if {@code todayPomodoros > 0}
     *       and {@code todayPomodoros % 4 == 0}; otherwise transitions to {@link #MODE_SHORT_BREAK}.</li>
     *   <li>From {@link #MODE_SHORT_BREAK} or {@link #MODE_LONG_BREAK}: transitions to {@link #MODE_WORK}.</li>
     * </ul>
     */
    public void advanceToNextPhase() {
        if (mMode == MODE_WORK) {
            boolean isFourth = (mTodayPomodoros > 0) && (mTodayPomodoros % POMODOROS_PER_LONG_BREAK == 0);
            mMode = isFourth ? MODE_LONG_BREAK : MODE_SHORT_BREAK;
        } else {
            mMode = MODE_WORK;
        }
        mStatus = STATUS_READY;
        mTotalPhaseSeconds = getModeDurationSeconds(mMode);
        mRemainingSeconds = mTotalPhaseSeconds;
    }

    /**
     * Applies one of the built-in {@link #PRESETS} and resets the current phase timer if not running.
     */
    public void applyPreset(int presetIdx) {
        mPresetIndex = ((presetIdx % PRESETS.length) + PRESETS.length) % PRESETS.length;
        DurationPreset p = PRESETS[mPresetIndex];
        mWorkMins = p.workMins;
        mShortBreakMins = p.shortBreakMins;
        mLongBreakMins = p.longBreakMins;
        mStatus = STATUS_READY;
        mTotalPhaseSeconds = getModeDurationSeconds(mMode);
        mRemainingSeconds = mTotalPhaseSeconds;
    }

    /** Cycles to the next duration preset in {@link #PRESETS}. */
    public void cyclePreset() {
        applyPreset(mPresetIndex + 1);
    }

    /**
     * Adjusts the duration of the currently selected phase by {@code deltaMinutes} (e.g. -5, -1, +1, +5)
     * while the timer is not running, clamping to the valid range for each phase
     * (matching {@code pomodoro.c} lines 1427-1450).
     */
    public boolean adjustCurrentModeMinutes(int deltaMinutes) {
        if (mStatus == STATUS_RUNNING || deltaMinutes == 0) {
            return false;
        }
        if (mMode == MODE_WORK) {
            mWorkMins = clamp(mWorkMins + deltaMinutes, MIN_DURATION_MINS, MAX_WORK_MINS);
        } else if (mMode == MODE_SHORT_BREAK) {
            mShortBreakMins = clamp(mShortBreakMins + deltaMinutes, MIN_DURATION_MINS, MAX_SHORT_BREAK_MINS);
        } else {
            mLongBreakMins = clamp(mLongBreakMins + deltaMinutes, MIN_DURATION_MINS, MAX_LONG_BREAK_MINS);
        }
        mStatus = STATUS_READY;
        mTotalPhaseSeconds = getModeDurationSeconds(mMode);
        mRemainingSeconds = mTotalPhaseSeconds;
        return true;
    }

    /** Sets the ambient focus soundscape mode ({@code 0..AMBIENT_COUNT-1}). */
    public void setAmbientMode(int ambientMode) {
        mAmbientMode = ((ambientMode % AMBIENT_COUNT) + AMBIENT_COUNT) % AMBIENT_COUNT;
    }

    /** Cycles to the next ambient focus soundscape mode. */
    public int cycleAmbientMode() {
        setAmbientMode(mAmbientMode + 1);
        return mAmbientMode;
    }

    /**
     * Advances the countdown clock by {@code seconds} when in {@link #STATUS_RUNNING}.
     *
     * @param seconds number of elapsed seconds to process (must be {@code >= 1})
     * @return {@code true} if the current phase reached {@code 00:00} and transitioned to
     *         {@link #STATUS_DONE} during this tick; {@code false} otherwise.
     */
    public boolean tickSeconds(int seconds) {
        if (mStatus != STATUS_RUNNING || seconds <= 0) {
            return false;
        }
        for (int i = 0; i < seconds; i++) {
            if (mRemainingSeconds > 0) {
                mRemainingSeconds--;
            }
            if (mMode == MODE_WORK) {
                mTotalWorkSeconds++;
                mTodayWorkSeconds++;
                mCurrentSessionWorkSeconds++;
            }
            if (mRemainingSeconds <= 0) {
                mRemainingSeconds = 0;
                mStatus = STATUS_DONE;
                if (mMode == MODE_WORK) {
                    mTotalPomodoros++;
                    mTodayPomodoros++;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the calendar date (`YYYY-MM-DD`) has changed since the last saved session.
     * If a new calendar day is detected, resets today's work seconds and today's pomodoro count
     * while preserving lifetime totals (matching {@code pomodoro.c} lines 1147-1152).
     */
    public boolean checkDateRollover(String currentDate) {
        if (currentDate == null || currentDate.isEmpty() || "SESSION".equals(currentDate)) {
            return false;
        }
        boolean rolledOver = false;
        if (mLastDate != null && !mLastDate.isEmpty() && !"SESSION".equals(mLastDate)
                && !currentDate.equals(mLastDate)) {
            mTodayWorkSeconds = 0L;
            mTodayPomodoros = 0;
            rolledOver = true;
        }
        mLastDate = currentDate;
        return rolledOver;
    }

    /** Restores persisted state from storage or demo intents. */
    public void restoreState(
            int workMins,
            int shortBreakMins,
            int longBreakMins,
            int presetIdx,
            int ambientMode,
            long totalWorkSec,
            long todayWorkSec,
            int totalPomodoros,
            int todayPomodoros,
            String lastDate) {
        mWorkMins = clamp(workMins, MIN_DURATION_MINS, MAX_WORK_MINS);
        mShortBreakMins = clamp(shortBreakMins, MIN_DURATION_MINS, MAX_SHORT_BREAK_MINS);
        mLongBreakMins = clamp(longBreakMins, MIN_DURATION_MINS, MAX_LONG_BREAK_MINS);
        mPresetIndex = clamp(presetIdx, 0, PRESETS.length - 1);
        mAmbientMode = clamp(ambientMode, 0, AMBIENT_COUNT - 1);
        mTotalWorkSeconds = Math.max(0L, totalWorkSec);
        mTodayWorkSeconds = Math.max(0L, todayWorkSec);
        mTotalPomodoros = Math.max(0, totalPomodoros);
        mTodayPomodoros = Math.max(0, todayPomodoros);
        mLastDate = (lastDate != null && !lastDate.isEmpty()) ? lastDate : "SESSION";
        mTotalPhaseSeconds = getModeDurationSeconds(mMode);
        mRemainingSeconds = mTotalPhaseSeconds;
    }

    /** Configures an exact timer/stats snapshot for deterministic UI testing and screenshots. */
    public void configureDemoSnapshot(
            int mode,
            int status,
            int remainingSec,
            int presetIdx,
            int ambientMode,
            int todayPoms,
            int totalPoms,
            long todayWorkSec,
            long totalWorkSec) {
        applyPreset(presetIdx);
        setMode(mode);
        mStatus = clamp(status, STATUS_READY, STATUS_DONE);
        mTotalPhaseSeconds = getModeDurationSeconds(mMode);
        if (remainingSec >= 0 && remainingSec <= mTotalPhaseSeconds) {
            mRemainingSeconds = remainingSec;
        }
        setAmbientMode(ambientMode);
        mTodayPomodoros = Math.max(0, todayPoms);
        mTotalPomodoros = Math.max(mTodayPomodoros, totalPoms);
        mTodayWorkSeconds = Math.max(0L, todayWorkSec);
        mTotalWorkSeconds = Math.max(mTodayWorkSeconds, totalWorkSec);
        mCurrentSessionWorkSeconds = Math.max(0, mTotalPhaseSeconds - mRemainingSeconds);
    }

    /** Formats a countdown in seconds as {@code MM:SS} (or {@code H:MM:SS} if >= 100 minutes). */
    public static String formatCountdown(int totalSeconds) {
        int clamped = Math.max(0, totalSeconds);
        int mins = clamped / 60;
        int secs = clamped % 60;
        return String.format(Locale.US, "%02d:%02d", mins, secs);
    }

    /** Formats work seconds into decimal hours (e.g. {@code "2.25 hrs"}). */
    public static String formatHours(long totalSeconds) {
        double hrs = Math.max(0L, totalSeconds) / 3600.0;
        return String.format(Locale.US, "%.2f hrs", hrs);
    }

    /** Formats work seconds into {@code "Xh YYm ZZs"} (matching {@code pomodoro.c} line 1632). */
    public static String formatHms(long totalSeconds) {
        long clamped = Math.max(0L, totalSeconds);
        long h = clamped / 3600L;
        long m = (clamped % 3600L) / 60L;
        long s = clamped % 60L;
        return String.format(Locale.US, "%dh %02dm %02ds", h, m, s);
    }

    /** Returns the short display name for a phase mode. */
    public static String getModeTitle(int mode) {
        switch (mode) {
            case MODE_WORK:
                return "FOCUS SESSION";
            case MODE_SHORT_BREAK:
                return "SHORT BREAK";
            case MODE_LONG_BREAK:
                return "LONG BREAK";
            default:
                return "FOCUS SESSION";
        }
    }

    /** Returns the status banner headline matching {@code pomodoro.c} lines 1521-1552. */
    public String getHeaderHeadline() {
        if (mStatus == STATUS_RUNNING) {
            if (mMode == MODE_WORK) return "WORKING: DO NOT DISTURB";
            if (mMode == MODE_SHORT_BREAK) return "SHORT BREAK: RELAX & RECHARGE";
            return "LONG BREAK: DEEP REST";
        } else if (mStatus == STATUS_PAUSED) {
            return "PAUSED: TIMER STOPPED";
        } else if (mStatus == STATUS_DONE) {
            return (mMode == MODE_WORK) ? "WORK DONE! TAKE A BREAK" : "BREAK OVER! READY TO FOCUS";
        } else {
            if (mMode == MODE_WORK) return "READY TO FOCUS";
            if (mMode == MODE_SHORT_BREAK) return "SHORT BREAK: READY";
            return "LONG BREAK: READY";
        }
    }

    private static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }
}
