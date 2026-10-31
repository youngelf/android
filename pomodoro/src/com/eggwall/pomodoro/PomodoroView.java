package com.eggwall.pomodoro;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Custom 2D Canvas UI for the Pomodoro Focus Timer & Work-Hours Tracker.
 *
 * <p>Adapts automatically between Portrait (vertical stack) and Landscape (two-column split:
 * left circular progress dial + primary transport controls, right preset/ambient selectors +
 * work-hours telemetry card). Includes a built-in {@code [?]} Help & Philosophy modal overlay
 * and full D-Pad / keyboard navigation.
 */
public class PomodoroView extends View {

    private static final String PREFS_NAME = "pomodoro_prefs";
    private static final String KEY_WORK_MINS = "work_mins";
    private static final String KEY_SHORT_MINS = "short_break_mins";
    private static final String KEY_LONG_MINS = "long_break_mins";
    private static final String KEY_PRESET_IDX = "preset_idx";
    private static final String KEY_AMBIENT_MODE = "ambient_mode";
    private static final String KEY_TOTAL_WORK_SEC = "total_work_sec";
    private static final String KEY_TODAY_WORK_SEC = "today_work_sec";
    private static final String KEY_TOTAL_POMS = "total_pomodoros";
    private static final String KEY_TODAY_POMS = "today_pomodoros";
    private static final String KEY_LAST_DATE = "last_date";

    private final PomodoroModel mModel = new PomodoroModel();
    private final PomodoroSynthEngine mSynth = new PomodoroSynthEngine();

    private final Paint mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mCardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mRingTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mRingProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mClockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF mTempRect = new RectF();
    private final RectF mRingBounds = new RectF();
    private final Path mLeafPath = new Path();

    /* Interactive hit-test regions */
    private final RectF mBtnHelp = new RectF();
    private final RectF mBtnHeaderAmbient = new RectF();
    private final RectF[] mBtnModes = { new RectF(), new RectF(), new RectF() };
    private final RectF[] mBtnAdjust = { new RectF(), new RectF(), new RectF(), new RectF() };
    private final RectF mBtnStartPause = new RectF();
    private final RectF mBtnReset = new RectF();
    private final RectF mBtnSkip = new RectF();
    private final RectF[] mBtnPresets = { new RectF(), new RectF(), new RectF(), new RectF() };
    private final RectF[] mBtnAmbients = { new RectF(), new RectF(), new RectF(), new RectF() };
    private final RectF mRingTapZone = new RectF();
    private final RectF mHelpModalRect = new RectF();
    private final RectF mHelpCloseRect = new RectF();
    private final RectF mHelpViewportRect = new RectF();
    private float mHelpScrollY = 0f;
    private float mHelpMaxScrollY = 0f;
    private float mHelpTouchStartY = 0f;
    private float mHelpLastTouchY = 0f;
    private boolean mHelpDragging = false;

    private static final int[] ADJUST_DELTAS = { -5, -1, +1, +5 };
    private static final String[] ADJUST_LABELS = { "-5m", "-1m", "+1m", "+5m" };

    private boolean mShowHelpModal = false;
    private long mLastTickUptimeMs = 0L;
    private int mCachedW = 0;
    private int mCachedH = 0;

    private final Runnable mTickRunnable = new Runnable() {
        @Override
        public void run() {
            if (mModel.getStatus() == PomodoroModel.STATUS_RUNNING) {
                long now = SystemClock.elapsedRealtime();
                if (mLastTickUptimeMs == 0L) {
                    mLastTickUptimeMs = now;
                }
                long elapsedMs = now - mLastTickUptimeMs;
                if (elapsedMs >= 1000L) {
                    int secs = (int) (elapsedMs / 1000L);
                    mLastTickUptimeMs += secs * 1000L;
                    boolean completed = mModel.tickSeconds(secs);
                    if (completed) {
                        mSynth.triggerCompletionChime(mModel.getMode() == PomodoroModel.MODE_WORK);
                        mSynth.setAmbientState(mModel.getAmbientMode(), false);
                        saveStats();
                    } else if (mModel.getRemainingSeconds() % 15 == 0) {
                        saveStats();
                    }
                    invalidate();
                }
                postDelayed(this, 200L);
            }
        }
    };

    public PomodoroView(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);

        mStrokePaint.setStyle(Paint.Style.STROKE);
        mRingTrackPaint.setStyle(Paint.Style.STROKE);
        mRingTrackPaint.setStrokeCap(Paint.Cap.ROUND);
        mRingProgressPaint.setStyle(Paint.Style.STROKE);
        mRingProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        mClockPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        mClockPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

        loadStats();
    }

    public void onActivityResume() {
        mModel.checkDateRollover(getCurrentDateString());
        mSynth.start();
        mSynth.setAmbientState(mModel.getAmbientMode(), mModel.getStatus() == PomodoroModel.STATUS_RUNNING);
        if (mModel.getStatus() == PomodoroModel.STATUS_RUNNING) {
            mLastTickUptimeMs = SystemClock.elapsedRealtime();
            removeCallbacks(mTickRunnable);
            postDelayed(mTickRunnable, 200L);
        }
        invalidate();
    }

    public void onActivityPause() {
        removeCallbacks(mTickRunnable);
        mSynth.setAmbientState(mModel.getAmbientMode(), false);
        mSynth.stop();
        saveStats();
    }

    /** Configures deterministic state for automated verification and documentation screenshots. */
    public void configureDemoState(
            int mode, int status, int remainingSec, int presetIdx, int ambientMode,
            int todayPoms, int totalPoms, long todayWorkSec, long totalWorkSec, boolean showHelp) {
        removeCallbacks(mTickRunnable);
        mModel.configureDemoSnapshot(
                mode, status, remainingSec, presetIdx, ambientMode,
                todayPoms, totalPoms, todayWorkSec, totalWorkSec);
        mShowHelpModal = showHelp;
        mSynth.setAmbientState(mModel.getAmbientMode(), false);
        invalidate();
    }

    private String getCurrentDateString() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    private void loadStats() {
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String today = getCurrentDateString();
        mModel.restoreState(
                prefs.getInt(KEY_WORK_MINS, PomodoroModel.DEFAULT_WORK_MINS),
                prefs.getInt(KEY_SHORT_MINS, PomodoroModel.DEFAULT_SHORT_BREAK_MINS),
                prefs.getInt(KEY_LONG_MINS, PomodoroModel.DEFAULT_LONG_BREAK_MINS),
                prefs.getInt(KEY_PRESET_IDX, 0),
                prefs.getInt(KEY_AMBIENT_MODE, PomodoroModel.AMBIENT_SILENCE),
                prefs.getLong(KEY_TOTAL_WORK_SEC, 0L),
                prefs.getLong(KEY_TODAY_WORK_SEC, 0L),
                prefs.getInt(KEY_TOTAL_POMS, 0),
                prefs.getInt(KEY_TODAY_POMS, 0),
                prefs.getString(KEY_LAST_DATE, today));
        if (mModel.checkDateRollover(today)) {
            saveStats();
        }
    }

    private void saveStats() {
        SharedPreferences.Editor ed = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        ed.putInt(KEY_WORK_MINS, mModel.getWorkMins());
        ed.putInt(KEY_SHORT_MINS, mModel.getShortBreakMins());
        ed.putInt(KEY_LONG_MINS, mModel.getLongBreakMins());
        ed.putInt(KEY_PRESET_IDX, mModel.getPresetIndex());
        ed.putInt(KEY_AMBIENT_MODE, mModel.getAmbientMode());
        ed.putLong(KEY_TOTAL_WORK_SEC, mModel.getTotalWorkSeconds());
        ed.putLong(KEY_TODAY_WORK_SEC, mModel.getTodayWorkSeconds());
        ed.putInt(KEY_TOTAL_POMS, mModel.getTotalPomodoros());
        ed.putInt(KEY_TODAY_POMS, mModel.getTodayPomodoros());
        ed.putString(KEY_LAST_DATE, mModel.getLastDate());
        ed.apply();
    }

    private int getAccentColor() {
        int status = mModel.getStatus();
        if (status == PomodoroModel.STATUS_PAUSED) return 0xFFF2C94C; // Amber Gold
        int mode = mModel.getMode();
        if (mode == PomodoroModel.MODE_WORK) return 0xFFFF5C5C; // Crimson Tomato
        if (mode == PomodoroModel.MODE_SHORT_BREAK) return 0xFF38D39F; // Emerald Mint
        return 0xFF4DA6FF; // Cerulean Azure
    }

    private int getAccentDimColor() {
        int mode = mModel.getMode();
        if (mode == PomodoroModel.MODE_WORK) return 0xFF421C22;
        if (mode == PomodoroModel.MODE_SHORT_BREAK) return 0xFF153830;
        return 0xFF172C46;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        if (w != mCachedW || h != mCachedH) {
            mCachedW = w;
            mCachedH = h;
            mBgPaint.setShader(new LinearGradient(
                    0, 0, 0, h, 0xFF0E1117, 0xFF171C26, Shader.TileMode.CLAMP));
        }
        canvas.drawRect(0, 0, w, h, mBgPaint);

        float pad = Math.min(w, h) * 0.030f;
        boolean isLandscape = (w > h * 1.15f);
        float topBarH = Math.max(56f, h * (isLandscape ? 0.115f : 0.072f));
        drawTopBar(canvas, pad, pad, w - 2 * pad, topBarH);

        float contentTop = pad + topBarH + pad * 0.60f;
        float contentH = h - contentTop - pad;

        if (isLandscape) {
            drawLandscapeLayout(canvas, pad, contentTop, w - 2 * pad, contentH);
        } else {
            drawPortraitLayout(canvas, pad, contentTop, w - 2 * pad, contentH);
        }

        if (mShowHelpModal) {
            drawHelpModal(canvas, w, h);
        }
    }

    private void drawTopBar(Canvas c, float x, float y, float w, float h) {
        int accent = getAccentColor();
        boolean running = (mModel.getStatus() == PomodoroModel.STATUS_RUNNING);

        /* Left [?] Help button */
        float helpW = h * 1.02f;
        mBtnHelp.set(x, y, x + helpW, y + h);
        drawButtonSurface(c, mBtnHelp, mShowHelpModal ? accent : 0xFF212836, 0xFF39445B, h * 0.26f);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setFakeBoldText(true);
        mTextPaint.setTextSize(h * 0.46f);
        mTextPaint.setColor(mShowHelpModal ? 0xFF0E1117 : 0xFFE6EDF3);
        AppI18n.drawText(c, "?", mBtnHelp.centerX(), mBtnHelp.centerY() + h * 0.16f, mTextPaint);

        /* Prominent full-width Status Banner (e.g. WORKING: DO NOT DISTURB) */
        float gap = h * 0.18f;
        mTempRect.set(mBtnHelp.right + gap, y, x + w, y + h);
        drawButtonSurface(c, mTempRect, running ? getAccentDimColor() : 0xFF1A202C, accent, h * 0.26f);
        String headline = AppI18n.tr(mModel.getHeaderHeadline());
        mTextPaint.setFakeBoldText(true);
        mTextPaint.setTextSize(Math.min(h * 0.42f, (mTempRect.width() * 1.50f) / Math.max(10, headline.length())));
        mTextPaint.setColor(accent);
        AppI18n.drawText(c, headline, mTempRect.centerX(),
                mTempRect.centerY() + mTextPaint.getTextSize() * 0.35f, mTextPaint);
    }

    private void drawPortraitLayout(Canvas c, float x, float y, float w, float h) {
        float gap = h * 0.016f;
        float modeH = h * 0.068f;
        drawModeTabs(c, x, y, w, modeH);

        float curY = y + modeH + gap;
        float ringCardH = h * 0.467f;
        drawDialCard(c, x, curY, w, ringCardH);

        curY += ringCardH + gap;
        float ctrlH = h * 0.088f;
        drawTransportControls(c, x, curY, w, ctrlH);

        curY += ctrlH + gap;
        float selH = h * 0.148f;
        drawPresetAndAmbientCard(c, x, curY, w, selH);

        curY += selH + gap;
        float statsH = Math.max(40f, y + h - curY);
        drawStatsCard(c, x, curY, w, statsH);
    }

    private void drawLandscapeLayout(Canvas c, float x, float y, float w, float h) {
        float gap = w * 0.02f;
        float leftW = w * 0.48f;
        float rightW = w - leftW - gap;
        float rightX = x + leftW + gap;

        float vGap = h * 0.025f;
        float ctrlH = h * 0.155f;
        float dialH = h - ctrlH - vGap;
        drawDialCard(c, x, y, leftW, dialH);
        drawTransportControls(c, x, y + dialH + vGap, leftW, ctrlH);

        float modeH = h * 0.135f;
        drawModeTabs(c, rightX, y, rightW, modeH);

        float rY = y + modeH + vGap;
        float selH = h * 0.355f;
        drawPresetAndAmbientCard(c, rightX, rY, rightW, selH);

        rY += selH + vGap;
        float statsH = Math.max(40f, y + h - rY);
        drawStatsCard(c, rightX, rY, rightW, statsH);
    }

    private void drawModeTabs(Canvas c, float x, float y, float w, float h) {
        String[] labels = {
            "FOCUS (" + mModel.getWorkMins() + "m)",
            "SHORT (" + mModel.getShortBreakMins() + "m)",
            "LONG (" + mModel.getLongBreakMins() + "m)"
        };
        float gap = w * 0.018f;
        float tabW = (w - 2 * gap) / 3f;
        int activeMode = mModel.getMode();
        int accent = getAccentColor();

        for (int i = 0; i < 3; i++) {
            float tx = x + i * (tabW + gap);
            mBtnModes[i].set(tx, y, tx + tabW, y + h);
            boolean sel = (i == activeMode);
            drawButtonSurface(c, mBtnModes[i], sel ? getAccentDimColor() : 0xFF1A202C,
                    sel ? accent : 0xFF2D3648, h * 0.26f);
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            mTextPaint.setFakeBoldText(sel);
            mTextPaint.setTextSize(Math.min(h * 0.36f, tabW * 0.13f));
            mTextPaint.setColor(sel ? 0xFFFFFFFF : 0xFF9AA5B8);
            AppI18n.drawText(c, labels[i], mBtnModes[i].centerX(), mBtnModes[i].centerY() + h * 0.13f, mTextPaint);
        }
    }

    private void drawDialCard(Canvas c, float x, float y, float w, float h) {
        mTempRect.set(x, y, x + w, y + h);
        drawButtonSurface(c, mTempRect, 0xFF151A24, 0xFF283142, 22f);

        int accent = getAccentColor();
        float cx = x + w * 0.5f;
        float adjustBarH = h * 0.118f;
        float availDialH = h - adjustBarH - h * 0.065f;
        float radius = Math.min(w * 0.40f, availDialH * 0.40f);
        float cy = y + availDialH * 0.48f;

        float strokeW = Math.max(10f, radius * 0.105f);
        mRingTrackPaint.setStrokeWidth(strokeW);
        mRingTrackPaint.setColor(0xFF252E3F);
        mRingBounds.set(cx - radius, cy - radius, cx + radius, cy + radius);
        mRingTapZone.set(mRingBounds);
        c.drawOval(mRingBounds, mRingTrackPaint);

        float progress = mModel.getProgressFraction();
        if (mModel.getStatus() == PomodoroModel.STATUS_READY) {
            progress = 1.0f;
        }
        mRingProgressPaint.setStrokeWidth(strokeW);
        mRingProgressPaint.setColor(accent);
        c.drawArc(mRingBounds, -90f, 360f * progress, false, mRingProgressPaint);

        /* Clean, centered MM:SS countdown inside the ring (no tomato icon, mode label, or cycle circles) */
        String clockText = PomodoroModel.formatCountdown(mModel.getRemainingSeconds());
        mClockPaint.setTextSize(radius * 0.62f);
        mClockPaint.setColor(0xFFF0F6FC);
        AppI18n.drawText(c, clockText, cx, cy + mClockPaint.getTextSize() * 0.34f, mClockPaint);

        /* Speaker sound toggle button in the far lower-right corner of the dial card, well separated from the circle */
        float cornerPad = Math.max(12f, Math.min(w, h) * 0.030f);
        float spkBtnSize = Math.max(46f, Math.min(w, availDialH) * 0.145f);
        float spkRight = x + w - cornerPad;
        float spkBottom = y + h - cornerPad;
        mBtnHeaderAmbient.set(spkRight - spkBtnSize, spkBottom - spkBtnSize, spkRight, spkBottom);
        boolean ambOn = mModel.getAmbientMode() != PomodoroModel.AMBIENT_SILENCE;
        int spkBorder = ambOn ? 0xFF38D39F : 0xFF39445B;
        int spkFill = ambOn ? 0xFF133329 : 0xFF202736;
        drawButtonSurface(c, mBtnHeaderAmbient, spkFill, spkBorder, spkBtnSize * 0.30f);
        drawSpeakerIcon(c, mBtnHeaderAmbient.centerX(), mBtnHeaderAmbient.centerY(),
                spkBtnSize * 0.30f, ambOn);

        /* Duration fine-adjustment pills (-5m, -1m, +1m, +5m) */
        float btnY = y + h - adjustBarH - cornerPad;
        float btnGap = w * 0.018f;
        float maxHalfW = (mBtnHeaderAmbient.left - w * 0.025f) - cx;
        float totalBtnW = Math.min(Math.min(w * 0.68f, radius * 2.0f), maxHalfW * 2f);
        float btnW = (totalBtnW - 3 * btnGap) / 4f;
        float startX = cx - totalBtnW * 0.5f;
        boolean canAdjust = mModel.getStatus() != PomodoroModel.STATUS_RUNNING;

        for (int i = 0; i < 4; i++) {
            float bx = startX + i * (btnW + btnGap);
            mBtnAdjust[i].set(bx, btnY, bx + btnW, btnY + adjustBarH);
            drawButtonSurface(c, mBtnAdjust[i], canAdjust ? 0xFF202736 : 0xFF171C26,
                    canAdjust ? 0xFF38445C : 0xFF232A38, adjustBarH * 0.30f);
            mTextPaint.setTextSize(adjustBarH * 0.40f);
            mTextPaint.setFakeBoldText(true);
            mTextPaint.setColor(canAdjust ? 0xFFD2DCE8 : 0xFF545D6E);
            AppI18n.drawText(c, ADJUST_LABELS[i], mBtnAdjust[i].centerX(),
                    mBtnAdjust[i].centerY() + adjustBarH * 0.14f, mTextPaint);
        }
    }

    private void drawTransportControls(Canvas c, float x, float y, float w, float h) {
        int accent = getAccentColor();
        int status = mModel.getStatus();
        String primaryLabel = (status == PomodoroModel.STATUS_RUNNING) ? "PAUSE"
                : (status == PomodoroModel.STATUS_PAUSED) ? "RESUME"
                : (status == PomodoroModel.STATUS_DONE) ? "NEXT PHASE" : "START FOCUS";
        if (status == PomodoroModel.STATUS_READY && mModel.getMode() != PomodoroModel.MODE_WORK) {
            primaryLabel = "START BREAK";
        }

        float gap = w * 0.022f;
        float sideW = w * 0.24f;
        float mainW = w - 2 * sideW - 2 * gap;

        mBtnReset.set(x, y, x + sideW, y + h);
        mBtnStartPause.set(x + sideW + gap, y, x + sideW + gap + mainW, y + h);
        mBtnSkip.set(mBtnStartPause.right + gap, y, x + w, y + h);

        drawButtonSurface(c, mBtnReset, 0xFF202736, 0xFF39445B, h * 0.28f);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setFakeBoldText(true);
        mTextPaint.setTextSize(Math.min(h * 0.34f, sideW * 0.22f));
        mTextPaint.setColor(0xFFD2DCE8);
        AppI18n.drawTextFit(c, "RESET", mBtnReset.centerX(), mBtnReset.centerY() + h * 0.12f, mTextPaint, mBtnReset.width() - 12f);

        drawButtonSurface(c, mBtnStartPause, accent, accent, h * 0.28f);
        if (status == PomodoroModel.STATUS_RUNNING) {
            drawPauseBarsIcon(c, mBtnStartPause.centerX(), mBtnStartPause.centerY(), h * 0.42f, 0xFF0E1117);
        } else {
            mTextPaint.setTextSize(Math.min(h * 0.37f, mainW * 0.13f));
            mTextPaint.setColor(0xFF0E1117);
            AppI18n.drawText(c, primaryLabel, mBtnStartPause.centerX(), mBtnStartPause.centerY() + h * 0.13f, mTextPaint);
        }

        drawButtonSurface(c, mBtnSkip, 0xFF202736, 0xFF39445B, h * 0.28f);
        mTextPaint.setTextSize(Math.min(h * 0.34f, sideW * 0.22f));
        mTextPaint.setColor(0xFFD2DCE8);
        AppI18n.drawTextFit(c, "SKIP >>", mBtnSkip.centerX(), mBtnSkip.centerY() + h * 0.12f, mTextPaint, mBtnSkip.width() - 12f);
    }

    private void drawPresetAndAmbientCard(Canvas c, float x, float y, float w, float h) {
        mTempRect.set(x, y, x + w, y + h);
        drawButtonSurface(c, mTempRect, 0xFF151A24, 0xFF283142, 18f);

        int accent = getAccentColor();
        float inPad = w * 0.028f;
        float gap = w * 0.018f;
        float pillW = (w - 2 * inPad - 3 * gap) / 4f;

        float availH = h - 2f * inPad;
        float labelH = availH * 0.16f;
        float ambRowH = availH * 0.33f;
        float sectionGap = availH * 0.08f;
        float presetRowH = availH * 0.27f;

        /* 1. Background sound at the top (30% shorter button height) */
        float labelY1 = y + inPad + labelH * 0.80f;
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setFakeBoldText(true);
        mTextPaint.setTextSize(labelH * 0.82f);
        mTextPaint.setColor(0xFF8B949E);
        AppI18n.drawText(c, "Background sound", x + inPad, labelY1, mTextPaint);

        float aY = y + inPad + labelH + availH * 0.02f;
        for (int i = 0; i < 4; i++) {
            float ax = x + inPad + i * (pillW + gap);
            mBtnAmbients[i].set(ax, aY, ax + pillW, aY + ambRowH);
            boolean sel = (mModel.getAmbientMode() == i);
            int ambCol = (i > 0 && sel) ? 0xFF38D39F : accent;
            int ambDim = (i > 0 && sel) ? 0xFF14352B : getAccentDimColor();
            drawButtonSurface(c, mBtnAmbients[i], sel ? ambDim : 0xFF202736,
                    sel ? ambCol : 0xFF343F54, ambRowH * 0.25f);
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            mTextPaint.setFakeBoldText(sel);
            mTextPaint.setTextSize(Math.min(ambRowH * 0.40f, pillW * 0.145f));
            mTextPaint.setColor(sel ? ambCol : 0xFFD2DCE8);
            AppI18n.drawTextFit(c, PomodoroModel.AMBIENT_NAMES[i], mBtnAmbients[i].centerX(), mBtnAmbients[i].centerY() + mTextPaint.getTextSize() * 0.35f, mTextPaint, pillW - 10f);
        }

        /* 2. Compact Duration Presets below Background sound */
        float labelY2 = aY + ambRowH + sectionGap + labelH * 0.72f;
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setFakeBoldText(true);
        mTextPaint.setTextSize(labelH * 0.78f);
        mTextPaint.setColor(0xFF8B949E);
        AppI18n.drawText(c, "DURATION PRESETS (WORK / SHORT / LONG)", x + inPad, labelY2, mTextPaint);

        float pY = labelY2 + availH * 0.03f;
        for (int i = 0; i < 4; i++) {
            float px = x + inPad + i * (pillW + gap);
            mBtnPresets[i].set(px, pY, px + pillW, pY + presetRowH);
            boolean sel = (mModel.getPresetIndex() == i);
            drawButtonSurface(c, mBtnPresets[i], sel ? getAccentDimColor() : 0xFF202736,
                    sel ? accent : 0xFF343F54, presetRowH * 0.28f);
            PomodoroModel.DurationPreset preset = PomodoroModel.PRESETS[i];
            String compactLabel = AppI18n.tr(preset.name + " " + preset.badge.replace(" ", ""));
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            mTextPaint.setFakeBoldText(sel);
            mTextPaint.setTextSize(Math.min(presetRowH * 0.44f, (pillW * 1.48f) / Math.max(8, compactLabel.length())));
            mTextPaint.setColor(sel ? 0xFFFFFFFF : 0xFFC9D1D9);
            AppI18n.drawText(c, compactLabel, mBtnPresets[i].centerX(),
                    mBtnPresets[i].centerY() + mTextPaint.getTextSize() * 0.35f, mTextPaint);
        }
    }

    private void drawStatsCard(Canvas c, float x, float y, float w, float h) {
        mTempRect.set(x, y, x + w, y + h);
        drawButtonSurface(c, mTempRect, 0xFF151A24, 0xFF283142, 18f);

        float colW = w / 3f;
        String[] headers = { "TODAY (" + mModel.getLastDate() + ")", "ALL-TIME TOTAL", "CURRENT SESSION" };
        String[] mainVals = {
            PomodoroModel.formatHours(mModel.getTodayWorkSeconds()),
            PomodoroModel.formatHours(mModel.getTotalWorkSeconds()),
            PomodoroModel.formatHms(mModel.getCurrentSessionWorkSeconds())
        };
        String[] subVals = {
            mModel.getTodayPomodoros() + " Pomodoros",
            mModel.getTotalPomodoros() + " Pomodoros",
            PomodoroModel.getModeTitle(mModel.getMode())
        };
        int[] valColors = { 0xFF38D39F, 0xFFF2C94C, getAccentColor() };

        for (int i = 0; i < 3; i++) {
            float cx = x + colW * (i + 0.5f);
            if (i > 0) {
                mStrokePaint.setStrokeWidth(1.5f);
                mStrokePaint.setColor(0xFF262F40);
                c.drawLine(x + colW * i, y + h * 0.18f, x + colW * i, y + h * 0.82f, mStrokePaint);
            }
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            mTextPaint.setFakeBoldText(true);
            mTextPaint.setTextSize(Math.min(h * 0.16f, colW * 0.085f));
            mTextPaint.setColor(0xFF8B949E);
            AppI18n.drawText(c, headers[i], cx, y + h * 0.28f, mTextPaint);

            mTextPaint.setTextSize(Math.min(h * 0.26f, colW * 0.145f));
            mTextPaint.setColor(valColors[i]);
            AppI18n.drawText(c, mainVals[i], cx, y + h * 0.58f, mTextPaint);

            mTextPaint.setFakeBoldText(false);
            mTextPaint.setTextSize(Math.min(h * 0.16f, colW * 0.095f));
            mTextPaint.setColor(0xFFC9D1D9);
            AppI18n.drawText(c, subVals[i], cx, y + h * 0.82f, mTextPaint);
        }
    }

    private void drawPauseBarsIcon(Canvas c, float cx, float cy, float iconH, int color) {
        float barW = Math.max(5f, iconH * 0.26f);
        float gap = Math.max(6f, iconH * 0.28f);
        float top = cy - iconH * 0.5f;
        float bot = cy + iconH * 0.5f;
        mIconPaint.setStyle(Paint.Style.FILL);
        mIconPaint.setColor(color);
        mTempRect.set(cx - gap * 0.5f - barW, top, cx - gap * 0.5f, bot);
        c.drawRoundRect(mTempRect, barW * 0.35f, barW * 0.35f, mIconPaint);
        mTempRect.set(cx + gap * 0.5f, top, cx + gap * 0.5f + barW, bot);
        c.drawRoundRect(mTempRect, barW * 0.35f, barW * 0.35f, mIconPaint);
    }

    private void drawSpeakerIcon(Canvas c, float cx, float cy, float r, boolean soundEnabled) {
        int speakerCol = soundEnabled ? 0xFF38D39F : 0xFF9AA5B8;
        mIconPaint.setStyle(Paint.Style.FILL);
        mIconPaint.setColor(speakerCol);

        /* Speaker body + flare cone */
        mLeafPath.reset();
        mLeafPath.moveTo(cx - r * 0.85f, cy - r * 0.34f);
        mLeafPath.lineTo(cx - r * 0.38f, cy - r * 0.34f);
        mLeafPath.lineTo(cx + r * 0.18f, cy - r * 0.84f);
        mLeafPath.lineTo(cx + r * 0.18f, cy + r * 0.84f);
        mLeafPath.lineTo(cx - r * 0.38f, cy + r * 0.34f);
        mLeafPath.lineTo(cx - r * 0.85f, cy + r * 0.34f);
        mLeafPath.close();
        c.drawPath(mLeafPath, mIconPaint);

        mIconPaint.setStyle(Paint.Style.STROKE);
        mIconPaint.setStrokeCap(Paint.Cap.ROUND);
        float sw = Math.max(2.4f, r * 0.18f);
        mIconPaint.setStrokeWidth(sw);

        if (soundEnabled) {
            /* Bright green acoustic wave arcs */
            mIconPaint.setColor(0xFF38D39F);
            float w1 = r * 0.48f;
            mTempRect.set(cx - w1 + r * 0.12f, cy - w1, cx + w1 + r * 0.12f, cy + w1);
            c.drawArc(mTempRect, -45f, 90f, false, mIconPaint);

            float w2 = r * 0.82f;
            mTempRect.set(cx - w2 + r * 0.12f, cy - w2, cx + w2 + r * 0.12f, cy + w2);
            c.drawArc(mTempRect, -45f, 90f, false, mIconPaint);
        } else {
            /* Crossed-out diagonal slash when sound is OFF */
            mIconPaint.setColor(0xFFFF5C5C);
            mIconPaint.setStrokeWidth(sw * 1.15f);
            c.drawLine(cx - r * 0.82f, cy - r * 0.82f, cx + r * 0.82f, cy + r * 0.82f, mIconPaint);
        }
    }

    private void drawHelpModal(Canvas c, int w, int h) {
        mCardPaint.setColor(0xD8090C10);
        c.drawRect(0, 0, w, h, mCardPaint);

        float mw = Math.min(w * 0.90f, 920f);
        float mh = Math.min(h * 0.84f, 680f);
        float mx = (w - mw) * 0.5f;
        float my = (h - mh) * 0.5f;
        mHelpModalRect.set(mx, my, mx + mw, my + mh);
        drawButtonSurface(c, mHelpModalRect, 0xFF161C28, getAccentColor(), 24f);

        float pad = Math.max(14f, mw * 0.035f);
        float hdrH = Math.max(46f, mh * 0.11f);
        float closeW = Math.max(100f, mw * 0.18f);
        mHelpCloseRect.set(mx + mw - pad - closeW, my + 10f, mx + mw - pad, my + 10f + hdrH - 6f);
        drawButtonSurface(c, mHelpCloseRect, 0xFF133329, 0xFF38D39F, (hdrH - 6f) * 0.30f);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setFakeBoldText(true);
        mTextPaint.setTextSize((hdrH - 6f) * 0.42f);
        mTextPaint.setColor(0xFF38D39F);
        AppI18n.drawTextFit(c, AppI18n.str(getContext(), R.string.close),
                mHelpCloseRect.centerX(), mHelpCloseRect.centerY() + mTextPaint.getTextSize() * 0.35f,
                mTextPaint, mHelpCloseRect.width() - 12f);

        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setFakeBoldText(true);
        mTextPaint.setTextSize(Math.min(hdrH * 0.48f, 24f));
        mTextPaint.setColor(getAccentColor());
        AppI18n.drawTextFit(c, AppI18n.str(getContext(), R.string.help_title),
                mx + pad, my + 10f + (hdrH - 6f) * 0.68f, mTextPaint,
                mHelpCloseRect.left - (mx + pad + 12f));

        Context ctx = getContext();
        String[] titles = {
            AppI18n.str(ctx, R.string.help_sec1_title),
            AppI18n.str(ctx, R.string.help_sec2_title),
            AppI18n.str(ctx, R.string.help_sec3_title)
        };
        String[] bodies = {
            AppI18n.str(ctx, R.string.help_sec1_body),
            AppI18n.str(ctx, R.string.help_sec2_body),
            AppI18n.str(ctx, R.string.help_sec3_body)
        };
        int[] titleCols = { 0xFFF2C94C, 0xFF38D39F, getAccentColor() };

        mHelpViewportRect.set(mx + pad, mHelpCloseRect.bottom + 8f, mx + mw - pad, my + mh - 14f);
        float titleSz = Math.max(15f, Math.min(20f, Math.min(w, h) * 0.032f));
        float bodySz = Math.max(13.5f, Math.min(18f, Math.min(w, h) * 0.028f));
        mHelpMaxScrollY = AppI18n.drawScrollableHelpBlock(
                c, mHelpViewportRect, titles, bodies, titleCols,
                0xFFD2DCE8, 0xFF202736, 0xFF343F54, getAccentColor(),
                titleSz, bodySz, mHelpScrollY, mTextPaint, mCardPaint);
        mHelpScrollY = Math.max(0f, Math.min(mHelpScrollY, mHelpMaxScrollY));
    }

    private void drawButtonSurface(Canvas c, RectF r, int fillColor, int borderColor, float cornerRadius) {
        mCardPaint.setColor(fillColor);
        c.drawRoundRect(r, cornerRadius, cornerRadius, mCardPaint);
        mStrokePaint.setStrokeWidth(2f);
        mStrokePaint.setColor(borderColor);
        c.drawRoundRect(r, cornerRadius, cornerRadius, mStrokePaint);
    }

    private void syncTimerLoop() {
        removeCallbacks(mTickRunnable);
        boolean running = (mModel.getStatus() == PomodoroModel.STATUS_RUNNING);
        mSynth.setAmbientState(mModel.getAmbientMode(), running);
        if (running) {
            mLastTickUptimeMs = SystemClock.elapsedRealtime();
            postDelayed(mTickRunnable, 200L);
        }
        saveStats();
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();

        if (mShowHelpModal) {
            if (action == MotionEvent.ACTION_DOWN) {
                mHelpTouchStartY = y;
                mHelpLastTouchY = y;
                mHelpDragging = false;
                if (mHelpCloseRect.contains(x, y) || !mHelpModalRect.contains(x, y)) {
                    mShowHelpModal = false;
                    mSynth.triggerUiClick();
                    invalidate();
                }
            } else if (action == MotionEvent.ACTION_MOVE) {
                float dy = mHelpLastTouchY - y;
                if (Math.abs(y - mHelpTouchStartY) > 10f) {
                    mHelpDragging = true;
                }
                if (mHelpDragging && mHelpMaxScrollY > 0f) {
                    mHelpScrollY = Math.max(0f, Math.min(mHelpMaxScrollY, mHelpScrollY + dy));
                    mHelpLastTouchY = y;
                    invalidate();
                }
            } else if (action == MotionEvent.ACTION_UP) {
                mHelpDragging = false;
            }
            return true;
        }

        if (action != MotionEvent.ACTION_DOWN) {
            return super.onTouchEvent(event);
        }

        if (mBtnHelp.contains(x, y)) {
            mShowHelpModal = true;
            mHelpScrollY = 0f;
            mSynth.triggerUiClick();
            invalidate();
            return true;
        }

        if (mBtnHeaderAmbient.contains(x, y)) {
            mModel.cycleAmbientMode();
            mSynth.triggerUiClick();
            syncTimerLoop();
            return true;
        }

        if (mBtnStartPause.contains(x, y) || mRingTapZone.contains(x, y)) {
            mModel.toggleStartPause();
            mSynth.triggerUiClick();
            syncTimerLoop();
            return true;
        }

        if (mBtnReset.contains(x, y)) {
            mModel.resetCurrentTimer();
            mSynth.triggerUiClick();
            syncTimerLoop();
            return true;
        }

        if (mBtnSkip.contains(x, y)) {
            mModel.advanceToNextPhase();
            mSynth.triggerUiClick();
            syncTimerLoop();
            return true;
        }

        for (int i = 0; i < 3; i++) {
            if (mBtnModes[i].contains(x, y)) {
                mModel.setMode(i);
                mSynth.triggerUiClick();
                syncTimerLoop();
                return true;
            }
        }

        for (int i = 0; i < 4; i++) {
            if (mBtnAdjust[i].contains(x, y)) {
                if (mModel.adjustCurrentModeMinutes(ADJUST_DELTAS[i])) {
                    mSynth.triggerUiClick();
                    syncTimerLoop();
                }
                return true;
            }
            if (mBtnPresets[i].contains(x, y)) {
                mModel.applyPreset(i);
                mSynth.triggerUiClick();
                syncTimerLoop();
                return true;
            }
            if (mBtnAmbients[i].contains(x, y)) {
                mModel.setAmbientMode(i);
                mSynth.triggerUiClick();
                syncTimerLoop();
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mShowHelpModal) {
            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE
                    || keyCode == KeyEvent.KEYCODE_H || keyCode == KeyEvent.KEYCODE_SPACE
                    || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                mShowHelpModal = false;
                mSynth.triggerUiClick();
                invalidate();
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_SPACE:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_BUTTON_A:
                mModel.toggleStartPause();
                mSynth.triggerUiClick();
                syncTimerLoop();
                return true;
            case KeyEvent.KEYCODE_R:
            case KeyEvent.KEYCODE_DEL:
            case KeyEvent.KEYCODE_BUTTON_B:
                mModel.resetCurrentTimer();
                mSynth.triggerUiClick();
                syncTimerLoop();
                return true;
            case KeyEvent.KEYCODE_N:
            case KeyEvent.KEYCODE_BUTTON_Y:
                mModel.advanceToNextPhase();
                mSynth.triggerUiClick();
                syncTimerLoop();
                return true;
            case KeyEvent.KEYCODE_P:
            case KeyEvent.KEYCODE_BUTTON_X:
                mModel.cyclePreset();
                mSynth.triggerUiClick();
                syncTimerLoop();
                return true;
            case KeyEvent.KEYCODE_M:
                mModel.cycleAmbientMode();
                mSynth.triggerUiClick();
                syncTimerLoop();
                return true;
            case KeyEvent.KEYCODE_H:
            case KeyEvent.KEYCODE_SLASH:
                mShowHelpModal = true;
                mSynth.triggerUiClick();
                invalidate();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mModel.cycleMode(-1);
                mSynth.triggerUiClick();
                syncTimerLoop();
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mModel.cycleMode(1);
                mSynth.triggerUiClick();
                syncTimerLoop();
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                if (mModel.adjustCurrentModeMinutes(1)) {
                    mSynth.triggerUiClick();
                    syncTimerLoop();
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (mModel.adjustCurrentModeMinutes(-1)) {
                    mSynth.triggerUiClick();
                    syncTimerLoop();
                }
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }
}
