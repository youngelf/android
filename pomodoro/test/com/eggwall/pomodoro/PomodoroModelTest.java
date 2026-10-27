package com.eggwall.pomodoro;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link PomodoroModel} covering phase transitions, 4-Pomodoro long-break cycles,
 * duration adjustments and clamping, preset loading, work-hour statistics tracking, and calendar
 * date rollover.
 */
public class PomodoroModelTest {

    private PomodoroModel mModel;

    @Before
    public void setUp() {
        mModel = new PomodoroModel();
    }

    @Test
    public void testInitialDefaults() {
        assertEquals(PomodoroModel.MODE_WORK, mModel.getMode());
        assertEquals(PomodoroModel.STATUS_READY, mModel.getStatus());
        assertEquals(25, mModel.getWorkMins());
        assertEquals(5, mModel.getShortBreakMins());
        assertEquals(15, mModel.getLongBreakMins());
        assertEquals(25 * 60, mModel.getRemainingSeconds());
        assertEquals(0, mModel.getTodayPomodoros());
        assertEquals(0, mModel.getTotalPomodoros());
        assertEquals(0L, mModel.getTodayWorkSeconds());
        assertEquals(0L, mModel.getTotalWorkSeconds());
        assertEquals(0.0f, mModel.getProgressFraction(), 1e-5f);
        assertEquals("READY TO FOCUS", mModel.getHeaderHeadline());
    }

    @Test
    public void testFourSessionPomodoroCycleTransitions() {
        // Complete Pomodoro #1 -> Short Break
        mModel.toggleStartPause();
        assertEquals(PomodoroModel.STATUS_RUNNING, mModel.getStatus());
        assertTrue(mModel.tickSeconds(25 * 60));
        assertEquals(PomodoroModel.STATUS_DONE, mModel.getStatus());
        assertEquals(1, mModel.getTodayPomodoros());
        assertEquals(1, mModel.getTotalPomodoros());
        assertEquals(1, mModel.getCycleCompletedCount());

        // Pressing Start/Pause from DONE advances to Short Break and starts running
        mModel.toggleStartPause();
        assertEquals(PomodoroModel.MODE_SHORT_BREAK, mModel.getMode());
        assertEquals(PomodoroModel.STATUS_RUNNING, mModel.getStatus());
        assertEquals(5 * 60, mModel.getRemainingSeconds());

        // Complete Short Break -> back to Work
        assertTrue(mModel.tickSeconds(5 * 60));
        assertEquals(PomodoroModel.STATUS_DONE, mModel.getStatus());
        mModel.advanceToNextPhase();
        assertEquals(PomodoroModel.MODE_WORK, mModel.getMode());
        assertEquals(PomodoroModel.STATUS_READY, mModel.getStatus());

        // Complete Pomodoros #2 and #3 -> Short Break each time
        for (int p = 2; p <= 3; p++) {
            mModel.toggleStartPause();
            assertTrue(mModel.tickSeconds(25 * 60));
            assertEquals(p, mModel.getTodayPomodoros());
            assertEquals(p, mModel.getCycleCompletedCount());
            mModel.advanceToNextPhase();
            assertEquals(PomodoroModel.MODE_SHORT_BREAK, mModel.getMode());
            mModel.advanceToNextPhase();
            assertEquals(PomodoroModel.MODE_WORK, mModel.getMode());
        }

        // Complete Pomodoro #4 -> Long Break!
        mModel.toggleStartPause();
        assertTrue(mModel.tickSeconds(25 * 60));
        assertEquals(4, mModel.getTodayPomodoros());
        assertEquals(4, mModel.getCycleCompletedCount());
        mModel.advanceToNextPhase();
        assertEquals(PomodoroModel.MODE_LONG_BREAK, mModel.getMode());
        assertEquals(15 * 60, mModel.getRemainingSeconds());
        assertEquals(4, mModel.getCycleCompletedCount());
    }

    @Test
    public void testWorkSecondsTrackedOnlyDuringFocusMode() {
        mModel.toggleStartPause();
        assertFalse(mModel.tickSeconds(600)); // 10 minutes of focus
        assertEquals(600L, mModel.getTodayWorkSeconds());
        assertEquals(600L, mModel.getTotalWorkSeconds());
        assertEquals(600L, mModel.getCurrentSessionWorkSeconds());
        assertEquals(15 * 60, mModel.getRemainingSeconds());
        assertEquals(0.4f, mModel.getProgressFraction(), 1e-4f);

        // Switch to Short Break and run 120 seconds: work seconds must not increase
        mModel.setMode(PomodoroModel.MODE_SHORT_BREAK);
        mModel.toggleStartPause();
        assertFalse(mModel.tickSeconds(120));
        assertEquals(600L, mModel.getTodayWorkSeconds());
        assertEquals(600L, mModel.getTotalWorkSeconds());
    }

    @Test
    public void testAdjustMinutesAndClamping() {
        // Adjust work minutes while ready
        assertTrue(mModel.adjustCurrentModeMinutes(10));
        assertEquals(35, mModel.getWorkMins());
        assertEquals(35 * 60, mModel.getRemainingSeconds());

        // Clamp upper bound (120 mins for Work)
        assertTrue(mModel.adjustCurrentModeMinutes(500));
        assertEquals(PomodoroModel.MAX_WORK_MINS, mModel.getWorkMins());

        // Clamp lower bound (1 min)
        assertTrue(mModel.adjustCurrentModeMinutes(-500));
        assertEquals(PomodoroModel.MIN_DURATION_MINS, mModel.getWorkMins());

        // Cannot adjust while running
        mModel.toggleStartPause();
        assertFalse(mModel.adjustCurrentModeMinutes(5));
    }

    @Test
    public void testPresetsAndAmbientCycling() {
        mModel.applyPreset(1); // Deep Work: 50 / 10 / 20
        assertEquals(1, mModel.getPresetIndex());
        assertEquals(50, mModel.getWorkMins());
        assertEquals(10, mModel.getShortBreakMins());
        assertEquals(20, mModel.getLongBreakMins());

        mModel.cyclePreset(); // Studio: 45 / 15 / 30
        assertEquals(2, mModel.getPresetIndex());
        assertEquals(45, mModel.getWorkMins());
        assertEquals(15, mModel.getShortBreakMins());
        assertEquals(30, mModel.getLongBreakMins());

        assertEquals(PomodoroModel.AMBIENT_SILENCE, mModel.getAmbientMode());
        assertEquals(PomodoroModel.AMBIENT_SOFT_TICK, mModel.cycleAmbientMode());
        assertEquals("SOFT TICK", mModel.getAmbientModeName());
        assertEquals(PomodoroModel.AMBIENT_RAIN, mModel.cycleAmbientMode());
        assertEquals("RAIN", mModel.getAmbientModeName());
        assertEquals(PomodoroModel.AMBIENT_BROWN_NOISE, mModel.cycleAmbientMode());
        assertEquals("BROWN NOISE", mModel.getAmbientModeName());
        assertEquals(PomodoroModel.AMBIENT_SILENCE, mModel.cycleAmbientMode());
    }

    @Test
    public void testDateRolloverResetsTodayButKeepsLifetimeTotals() {
        mModel.restoreState(25, 5, 15, 0, 0, 18000L, 3600L, 12, 3, "2026-10-22");
        assertEquals(3600L, mModel.getTodayWorkSeconds());
        assertEquals(3, mModel.getTodayPomodoros());

        // Same date -> no rollover
        assertFalse(mModel.checkDateRollover("2026-10-22"));
        assertEquals(3600L, mModel.getTodayWorkSeconds());
        assertEquals(3, mModel.getTodayPomodoros());

        // New date -> today resets to 0, lifetime totals remain intact
        assertTrue(mModel.checkDateRollover("2026-10-23"));
        assertEquals(0L, mModel.getTodayWorkSeconds());
        assertEquals(0, mModel.getTodayPomodoros());
        assertEquals(18000L, mModel.getTotalWorkSeconds());
        assertEquals(12, mModel.getTotalPomodoros());
        assertEquals("2026-10-23", mModel.getLastDate());
    }

    @Test
    public void testFormatters() {
        assertEquals("25:00", PomodoroModel.formatCountdown(1500));
        assertEquals("00:09", PomodoroModel.formatCountdown(9));
        assertEquals("1.50 hrs", PomodoroModel.formatHours(5400));
        assertEquals("2h 05m 09s", PomodoroModel.formatHms(7509));
    }
}
