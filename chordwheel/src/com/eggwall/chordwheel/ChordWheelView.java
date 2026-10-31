package com.eggwall.chordwheel;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Custom hardware-accelerated Canvas View implementing the Circle of Fifths wheel,
 * 6-string guitar chord diagram inspector, 4-bar chord progression looper, and
 * interactive help/presets modals in both Landscape (side-by-side 2-pane layout)
 * and Portrait (vertical stacked layout).
 *
 * <p><b>Engineering Customization Guide:</b>
 * <ul>
 *   <li><b>Looper Tempo:</b> Change {@link #LOOP_STEP_MS} (default {@code 1400L} ms per bar,
 *       ~171 BPM quarter notes or ~85 BPM half-note bars) or expose a tempo slider.</li>
 *   <li><b>Color Palette / Theme:</b> Modify the {@code COL_*} ARGB constants below and the
 *       sector fill colors in {@link #drawCircleOfFifthsWheel}.</li>
 *   <li><b>Layout Proportions:</b> Adjust {@link #drawLandscapeLayout}, {@link #drawPortraitLayout},
 *       and {@link #drawWheelHeaderAndLegend} where a virtual coordinate unit {@code u} scales
 *       all typography, padding, and hit-target rectangles proportionally to any screen size.</li>
 *   <li><b>Persistence:</b> All user preferences are stored locally in {@link SharedPreferences}
 *       ({@link #PREFS_NAME}) via {@link #loadState} and {@link #saveState}.</li>
 * </ul>
 */
public class ChordWheelView extends View {

    /** Local-only SharedPreferences file name used to persist wheel/progression state across launches. */
    private static final String PREFS_NAME = "chordwheel_prefs";
    /** Duration in milliseconds of each chord step when the 4-bar progression looper is playing. */
    private static final long LOOP_STEP_MS = 1400L;

    /* Exact PowKiddy V90 Color Palette */
    private static final int COL_BG          = 0xFF0D121C;
    private static final int COL_CARD_FILL   = 0xFF151D2D;
    private static final int COL_CARD_BDR    = 0xFF334462;
    private static final int COL_BOTTOM_BG   = 0xFF111827;
    private static final int COL_BOTTOM_BDR  = 0xFF334155;
    private static final int COL_WHEEL_SEP   = 0xFF0B0F18;
    private static final int COL_SEL_BDR     = 0xFFFEF08A;
    private static final int COL_GOLD        = 0xFFFACC15;
    private static final int COL_SKY         = 0xFF38BDF8;
    private static final int COL_SKY_LIGHT   = 0xFFBAE6FD;
    private static final int COL_GREEN       = 0xFF4ADE80;
    private static final int COL_EMERALD     = 0xFF10B981;
    private static final int COL_TEXT_MAIN   = 0xFFF8FAFC;
    private static final int COL_TEXT_SUB    = 0xFFE2E8F0;
    private static final int COL_TEXT_MUTED  = 0xFF94A3B8;
    private static final int COL_FRET_WOOD   = 0xFF221B18;

    public static final class ProgSlot {
        public int sector;
        public int ring;
        public boolean use7th;
    }

    /* Application State */
    private int mKeySector = 0;         /* 0 = C Major */
    private int mCurSector = 0;
    private int mCurRing = 0;           /* 0 = Outer Major, 1 = Inner Minor, 2 = Center vii° */
    private boolean mKeyLocked = false; /* false = SEL:ROTATE, true = SEL:LOCKED */
    private boolean mSeventhMode = false;
    private int mVoicingIdx = 0;        /* 0 = Open/CAGED, 1 = E-Barre, 2 = A-Barre */
    private boolean mShowHelp = false;  /* Presets modal */
    private boolean mShowGuide = false; /* Top-left '?' How-It-Works Help modal */

    private final ProgSlot[] mProg = new ProgSlot[4];
    private int mProgWriteIdx = 0;
    private boolean mLoopPlaying = false;
    private int mLoopStep = 0;
    private int mPresetIdx = 0;

    private final MusicTheory.ResolvedChord mResolved = new MusicTheory.ResolvedChord();
    private final MusicTheory.GuitarVoicing mVoicing = new MusicTheory.GuitarVoicing();
    private final MusicTheory.ResolvedChord mSlotResolved = new MusicTheory.ResolvedChord();

    private GuitarSynthEngine mSynth;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    /* Reusable Drawing Primitives */
    private final Paint mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Path mSectorPath = new Path();
    private final RectF mOuterOval = new RectF();
    private final RectF mInnerOval = new RectF();
    private final RectF mTmpRect = new RectF();

    /* Dynamic Hit-Target Rectangles populated during onDraw() */
    private float mWheelCx, mWheelCy;
    private float mROuterMax, mROuterMin, mRInnerMax, mRInnerMin, mRHubMax;

    private final RectF mTopHelpPillRect  = new RectF();
    private final RectF mLockModePillRect = new RectF();
    private final RectF mSeventhPillRect  = new RectF();
    private final RectF mVoicingBarRect   = new RectF();
    private final RectF mFretboardCardRect = new RectF();
    private final RectF mFretGridRect     = new RectF();
    private final RectF mStrumBadgeRect   = new RectF();
    private final RectF mLoopPlayPillRect = new RectF();
    private final RectF mAddProgPillRect  = new RectF();
    private final RectF[] mProgSlotRects  = new RectF[4];
    private final RectF mHelpPillRect     = new RectF();

    /* Modal Hit Targets */
    private final RectF mModalBoxRect     = new RectF();
    private final RectF mModalLoopBtnRect = new RectF();
    private final RectF[] mModalPresetRects = new RectF[5];
    private final RectF mModalCloseRect   = new RectF();
    private final RectF mGuideViewportRect = new RectF();
    private float mGuideScrollY = 0.0f;
    private float mGuideMaxScrollY = 0.0f;
    private float mGuideTouchStartY = 0.0f;
    private float mGuideLastTouchY = 0.0f;
    private boolean mGuideDragging = false;

    /* Fretboard String Touch/Strum State */
    private final float[] mStringX = new float[6];
    private int mTouchMode = TOUCH_NONE;
    private static final int TOUCH_NONE      = 0;
    private static final int TOUCH_WHEEL     = 1;
    private static final int TOUCH_FRETBOARD = 2;

    private float mDownX, mDownY;
    private int mLastStrummedString = -1;
    private boolean mFretboardSwiped = false;
    private boolean mFretboardStrummedOnDrag = false;
    private final int mTouchSlop;

    /* Visual String Vibration Highlight Timestamp per String */
    private final long[] mStringPluckTimeMs = new long[6];

    private final Runnable mLoopRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mLoopPlaying) return;
            mLoopStep = (mLoopStep + 1) % 4;
            mCurSector = mProg[mLoopStep].sector;
            mCurRing = mProg[mLoopStep].ring;
            mSeventhMode = mProg[mLoopStep].use7th;
            strumCurrentSelection();
            invalidate();
            mHandler.postDelayed(this, LOOP_STEP_MS);
        }
    };

    public ChordWheelView(Context context) {
        this(context, null);
    }

    public ChordWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        setFocusableInTouchMode(true);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mStrokePaint.setStyle(Paint.Style.STROKE);
        mTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));

        for (int i = 0; i < 4; i++) {
            mProg[i] = new ProgSlot();
            mProgSlotRects[i] = new RectF();
        }
        for (int i = 0; i < 5; i++) mModalPresetRects[i] = new RectF();

        applyProgressionPreset(0);
    }

    public void setSynthEngine(GuitarSynthEngine synth) {
        mSynth = synth;
    }

    public void stopLoop() {
        mLoopPlaying = false;
        mHandler.removeCallbacks(mLoopRunnable);
        invalidate();
    }

    public void loadState(Context ctx) {
        applyProgressionPreset(0);
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mKeySector   = clamp(sp.getInt("key_sector", 0), 0, 11);
        mCurSector   = clamp(sp.getInt("cur_sector", 0), 0, 11);
        mCurRing     = clamp(sp.getInt("cur_ring", 0), 0, 2);
        mKeyLocked   = sp.getBoolean("key_locked", false);
        mSeventhMode = sp.getBoolean("seventh_mode", false);
        mVoicingIdx  = clamp(sp.getInt("voicing_idx", 0), 0, 2);
        mPresetIdx   = clamp(sp.getInt("preset_idx", 0), 0, 4);

        for (int i = 0; i < 4; i++) {
            mProg[i].sector = clamp(sp.getInt("prog_sec_" + i, mProg[i].sector), 0, 11);
            mProg[i].ring   = clamp(sp.getInt("prog_rng_" + i, mProg[i].ring), 0, 2);
            mProg[i].use7th = sp.getBoolean("prog_7th_" + i, mSeventhMode);
        }
        invalidate();
    }

    public void saveState(Context ctx) {
        SharedPreferences.Editor ed = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        ed.putInt("key_sector", mKeySector);
        ed.putInt("cur_sector", mCurSector);
        ed.putInt("cur_ring", mCurRing);
        ed.putBoolean("key_locked", mKeyLocked);
        ed.putBoolean("seventh_mode", mSeventhMode);
        ed.putInt("voicing_idx", mVoicingIdx);
        ed.putInt("preset_idx", mPresetIdx);
        for (int i = 0; i < 4; i++) {
            ed.putInt("prog_sec_" + i, mProg[i].sector);
            ed.putInt("prog_rng_" + i, mProg[i].ring);
            ed.putBoolean("prog_7th_" + i, mProg[i].use7th);
        }
        ed.apply();
    }

    /** Programmatic state configuration for automated testing & screenshot generation. */
    public void configureDemoState(int keySec, int curSec, int curRing,
                                   boolean locked, boolean seventh, int voicing,
                                   int preset, boolean loopPlaying, int loopStep,
                                   boolean showHelp, boolean showGuide) {
        stopLoop();
        mKeySector = ((keySec % 12) + 12) % 12;
        mCurSector = ((curSec % 12) + 12) % 12;
        mCurRing = clamp(curRing, 0, 2);
        mKeyLocked = locked;
        mSeventhMode = seventh;
        mVoicingIdx = ((voicing % 3) + 3) % 3;
        applyProgressionPreset(preset);
        mLoopPlaying = loopPlaying;
        mLoopStep = ((loopStep % 4) + 4) % 4;
        mShowHelp = showHelp;
        mShowGuide = showGuide;
        if (mLoopPlaying) {
            mHandler.postDelayed(mLoopRunnable, LOOP_STEP_MS);
        }
        strumCurrentSelection();
        invalidate();
    }

    private static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    public void applyProgressionPreset(int presetId) {
        presetId = ((presetId % 5) + 5) % 5;
        mPresetIdx = presetId;
        MusicTheory.ProgPreset p = MusicTheory.PRESETS[presetId];
        for (int i = 0; i < 4; i++) {
            mProg[i].sector = (mKeySector + p.sectorOffsets[i]) % 12;
            mProg[i].ring = p.rings[i];
            mProg[i].use7th = mSeventhMode;
        }
        mProgWriteIdx = 0;
    }

    public void strumCurrentSelection() {
        MusicTheory.resolveChordAt(mKeySector, mCurSector, mCurRing, mSeventhMode, mResolved);
        MusicTheory.buildGuitarVoicing(mResolved.rootPc, mResolved.qual, mVoicingIdx, mVoicing);
        long now = System.currentTimeMillis();
        int order = 0;
        for (int s = 0; s < 6; s++) {
            if (mVoicing.frets[s] >= 0) {
                mStringPluckTimeMs[s] = now + order * 22L;
                order++;
            } else {
                mStringPluckTimeMs[s] = 0L;
            }
        }
        if (mSynth != null) {
            mSynth.triggerStrum(mVoicing);
        }
        postInvalidateDelayed(30);
    }

    private void pluckSingleString(int s) {
        MusicTheory.resolveChordAt(mKeySector, mCurSector, mCurRing, mSeventhMode, mResolved);
        MusicTheory.buildGuitarVoicing(mResolved.rootPc, mResolved.qual, mVoicingIdx, mVoicing);
        if (s >= 0 && s < 6 && mVoicing.frets[s] >= 0) {
            mStringPluckTimeMs[s] = System.currentTimeMillis();
            if (mSynth != null) {
                mSynth.pluckSingleString(s, mVoicing.frets[s]);
            }
            invalidate();
        }
    }

    private void toggleLoopPlayback() {
        mLoopPlaying = !mLoopPlaying;
        mHandler.removeCallbacks(mLoopRunnable);
        if (mLoopPlaying) {
            mLoopStep = 0;
            mCurSector = mProg[0].sector;
            mCurRing = mProg[0].ring;
            mSeventhMode = mProg[0].use7th;
            strumCurrentSelection();
            mHandler.postDelayed(mLoopRunnable, LOOP_STEP_MS);
        } else if (mSynth != null) {
            mSynth.silenceAll();
        }
        invalidate();
    }

    private void selectSectorAndRing(int sector, int ring) {
        stopLoop();
        sector = ((sector % 12) + 12) % 12;
        ring = clamp(ring, 0, 2);

        if (ring == 2) {
            mCurRing = 2;
        } else if (!mKeyLocked) {
            int delta = (sector - mKeySector + 12) % 12;
            mKeySector = sector;
            mCurSector = sector;
            mCurRing = ring;
            for (int i = 0; i < 4; i++) {
                mProg[i].sector = (mProg[i].sector + delta) % 12;
            }
        } else {
            mCurSector = sector;
            mCurRing = ring;
        }
        strumCurrentSelection();
        saveState(getContext());
        invalidate();
    }

    private void rotateStep(int stepDelta) {
        stopLoop();
        if (!mKeyLocked) {
            mKeySector = (mKeySector + stepDelta + 12) % 12;
            mCurSector = (mCurSector + stepDelta + 12) % 12;
            for (int i = 0; i < 4; i++) {
                mProg[i].sector = (mProg[i].sector + stepDelta + 12) % 12;
            }
        } else {
            mCurSector = (mCurSector + stepDelta + 12) % 12;
        }
        strumCurrentSelection();
        saveState(getContext());
        invalidate();
    }

    private void cycleVoicing(int delta) {
        mVoicingIdx = (mVoicingIdx + delta + 3) % 3;
        strumCurrentSelection();
        saveState(getContext());
        invalidate();
    }

    private void toggleSeventhMode() {
        mSeventhMode = !mSeventhMode;
        for (int i = 0; i < 4; i++) {
            mProg[i].use7th = mSeventhMode;
        }
        strumCurrentSelection();
        saveState(getContext());
        invalidate();
    }

    private void toggleKeyLock() {
        mKeyLocked = !mKeyLocked;
        if (!mKeyLocked) {
            mCurSector = mKeySector;
        }
        saveState(getContext());
        invalidate();
    }

    private void queueCurrentChordToProgression() {
        mProg[mProgWriteIdx].sector = mCurSector;
        mProg[mProgWriteIdx].ring = mCurRing;
        mProg[mProgWriteIdx].use7th = mSeventhMode;
        mProgWriteIdx = (mProgWriteIdx + 1) % 4;
        strumCurrentSelection();
        saveState(getContext());
        invalidate();
    }

    /* =========================================================================
     * Responsive Landscape & Portrait Rendering
     * ========================================================================= */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int w = getWidth();
        final int h = getHeight();
        if (w <= 0 || h <= 0) return;

        canvas.drawColor(COL_BG);

        MusicTheory.resolveChordAt(mKeySector, mCurSector, mCurRing, mSeventhMode, mResolved);
        MusicTheory.buildGuitarVoicing(mResolved.rootPc, mResolved.qual, mVoicingIdx, mVoicing);

        final boolean isLandscape = (w >= h);
        if (isLandscape) {
            drawLandscapeLayout(canvas, w, h);
        } else {
            drawPortraitLayout(canvas, w, h);
        }

        if (mShowGuide) {
            drawGuideModal(canvas, w, h);
        } else if (mShowHelp) {
            drawHelpModal(canvas, w, h);
        }

        /* Keep animating string glow for 220 ms after pluck */
        long now = System.currentTimeMillis();
        for (int s = 0; s < 6; s++) {
            if (now >= mStringPluckTimeMs[s] && (now - mStringPluckTimeMs[s]) < 240L) {
                postInvalidateDelayed(25);
                break;
            }
        }
    }

    private void drawLandscapeLayout(Canvas canvas, int w, int h) {
        float u = Math.min(w / 320.0f, h / 240.0f);
        float pad = 3.5f * u;
        float bottomH = 25.0f * u;

        /* 1. Left Pane: Full-height Circle of Fifths Wheel + Legend + Corner '?' / 'LOCK' Pills */
        float leftW = Math.min(w * 0.47f, h * 0.94f);
        float headerY = 3.0f * u;
        float headerH = 17.0f * u;
        drawWheelHeaderAndLegend(canvas, pad, headerY, leftW - pad, headerH, h - pad, u);

        /* 2. Right Pane: Chord Card + Fretboard Card + Progression Loop Bar at Bottom of Right Pane */
        float rightX = leftW + pad;
        float rightW = w - rightX - pad;

        float loopY = h - bottomH - pad;
        float rightTopH = loopY - headerY - 3.5f * u;

        float card1Y = headerY;
        float card1H = rightTopH * 0.30f;
        float card2Y = card1Y + card1H + 3.5f * u;
        float card2H = rightTopH - card1H - 3.5f * u;

        drawChordInfoCard(canvas, rightX, card1Y, rightW, card1H, u);
        drawGuitarFretboardCard(canvas, rightX, card2Y, rightW, card2H, u);
        drawBottomProgressionBar(canvas, rightX, loopY, rightW, bottomH, u);
    }

    private void drawPortraitLayout(Canvas canvas, int w, int h) {
        float u = Math.min(w / 240.0f, h / 380.0f);
        float pad = 2.5f * u;
        float bottomH = 28.0f * u;
        float availH = h - bottomH - pad;

        /* Top Pane: Full-width Circle of Fifths Wheel + Corner '?' / 'LOCK' Pills + Legend */
        float paneW = w - 2.0f * pad;
        float headerY = 10.0f * u;
        float headerH = 17.5f * u;
        float legH = 14.5f * u;
        float topPaneH = Math.min(availH * 0.58f, headerY + paneW + legH + 4.0f * u);
        drawWheelHeaderAndLegend(canvas, pad, headerY, paneW, headerH, topPaneH, u);

        /* Bottom Pane: Chord Card + Fretboard Card (with Speaker Icon Strum Button) */
        float cardPad = 4.5f * u;
        float inspectorTop = topPaneH + 3.5f * u;
        float inspectorH = availH - inspectorTop;

        float card1H = inspectorH * 0.30f;
        float card2H = inspectorH - card1H - 3.5f * u;

        float card1Y = inspectorTop;
        float card2Y = card1Y + card1H + 3.5f * u;

        drawChordInfoCard(canvas, cardPad, card1Y, w - 2.0f * cardPad, card1H, u);
        drawGuitarFretboardCard(canvas, cardPad, card2Y, w - 2.0f * cardPad, card2H, u);

        /* Bottom Bar */
        drawBottomProgressionBar(canvas, 0, h - bottomH, w, bottomH, u);
    }

    private void drawWheelHeaderAndLegend(Canvas canvas, float x, float headerY, float paneW,
                                          float headerH, float paneBottomY, float u) {
        /* Diatonic Color Legend Pills at bottom of wheel pane */
        float legH = 14.5f * u;
        float legY = paneBottomY - legH;
        float legGap = 3.0f * u;
        float legW = (paneW - 2.0f * legGap) / 3.0f;

        mTmpRect.set(x, legY, x + legW, legY + legH);
        drawPill(canvas, mTmpRect, 0xFF925C0C, COL_GOLD, u);
        drawCenteredText(canvas, mTmpRect, "I / vi", 8.8f * u, 0xFFFFFFFF);

        mTmpRect.set(x + legW + legGap, legY, x + 2.0f * legW + legGap, legY + legH);
        drawPill(canvas, mTmpRect, 0xFF1D4EB2, 0xFF60A5FA, u);
        drawCenteredText(canvas, mTmpRect, "IV / ii", 8.8f * u, 0xFFFFFFFF);

        mTmpRect.set(x + 2.0f * (legW + legGap), legY, x + paneW, legY + legH);
        drawPill(canvas, mTmpRect, 0xFF0E845C, 0xFF34D399, u);
        drawCenteredText(canvas, mTmpRect, "V / iii", 8.8f * u, 0xFFFFFFFF);

        /* Full-width Circle of Fifths Wheel (starts at headerY since '?' and 'LOCK' sit in the corners) */
        float wheelTop = headerY;
        float wheelBot = legY - 2.0f * u;
        float wheelAvailH = wheelBot - wheelTop;
        float maxRadius = Math.min(paneW, wheelAvailH) * 0.495f;

        mWheelCx = x + paneW * 0.5f;
        mWheelCy = wheelTop + wheelAvailH * 0.5f;
        mROuterMax = maxRadius;
        mROuterMin = maxRadius * (56.5f / 84.0f);
        mRInnerMax = maxRadius * (55.0f / 84.0f);
        mRInnerMin = maxRadius * (31.5f / 84.0f);
        mRHubMax   = maxRadius * (30.0f / 84.0f);

        drawCircleOfFifthsWheel(canvas, u);

        /* Top-left '?' Help Pill & Top-right 'LOCK' Pill tucked into the upper corners */
        float helpBtnW = 22.0f * u;
        mTopHelpPillRect.set(x, headerY, x + helpBtnW, headerY + headerH);
        drawPill(canvas, mTopHelpPillRect, 0xFF1E3A5F, COL_SKY, u);
        drawCenteredText(canvas, mTopHelpPillRect, "?", 9.6f * u, COL_SEL_BDR);

        float lockW = 38.0f * u;
        mLockModePillRect.set(x + paneW - lockW, headerY, x + paneW, headerY + headerH);
        int lockFill = mKeyLocked ? 0xFF9A3412 : 0xFF172134;
        int lockBdr  = mKeyLocked ? 0xFFFB923C : COL_CARD_BDR;
        int lockTxt  = mKeyLocked ? 0xFFFFFFFF : COL_TEXT_SUB;
        drawPill(canvas, mLockModePillRect, lockFill, lockBdr, u);
        drawCenteredText(canvas, mLockModePillRect, "LOCK", 8.8f * u, lockTxt);
    }

    private void drawCircleOfFifthsWheel(Canvas canvas, float u) {
        /* Backdrop circle for clean dark separators */
        mFillPaint.setColor(COL_WHEEL_SEP);
        canvas.drawCircle(mWheelCx, mWheelCy, mROuterMax + 1.5f * u, mFillPaint);

        float gapDeg = 1.6f;
        float sweepDeg = 30.0f - gapDeg;

        /* Draw all 12 Outer & Inner sectors */
        for (int s = 0; s < 12; s++) {
            int rel = (s - mKeySector + 12) % 12;
            boolean selOuter = (mCurRing == 0 && mCurSector == s);
            boolean selInner = (mCurRing == 1 && mCurSector == s);

            int outerFill;
            if (rel == 0)       outerFill = selOuter ? 0xFFF59E0B : 0xFFB4700C;
            else if (rel == 11) outerFill = selOuter ? 0xFF2563EB : 0xFF1D4EB2;
            else if (rel == 1)  outerFill = selOuter ? 0xFF10B981 : 0xFF0E845C;
            else                outerFill = selOuter ? 0xFF475569 : 0xFF1A2334;

            int innerFill;
            if (rel == 0)       innerFill = selInner ? 0xFFD97706 : 0xFF8A4E0E;
            else if (rel == 11) innerFill = selInner ? 0xFF3B82F6 : 0xFF1E408E;
            else if (rel == 1)  innerFill = selInner ? 0xFF14B8A6 : 0xFF0F6E60;
            else                innerFill = selInner ? 0xFF475569 : 0xFF151D2C;

            float startDeg = s * 30.0f - 90.0f - 15.0f + gapDeg * 0.5f;

            drawAnnularSector(canvas, mWheelCx, mWheelCy, mROuterMin, mROuterMax,
                    startDeg, sweepDeg, outerFill, selOuter ? COL_SEL_BDR : 0, 1.5f * u);

            drawAnnularSector(canvas, mWheelCx, mWheelCy, mRInnerMin, mRInnerMax,
                    startDeg, sweepDeg, innerFill, selInner ? COL_SEL_BDR : 0, 1.5f * u);
        }

        /* Center Hub (Key Signature + vii° Diminished Chord) */
        int hubFill = (mCurRing == 2) ? 0xFFBE185D : 0xFF1E293B;
        int hubBdr  = (mCurRing == 2) ? COL_SEL_BDR : 0xFF60A5FA;
        mFillPaint.setColor(hubFill);
        canvas.drawCircle(mWheelCx, mWheelCy, mRHubMax, mFillPaint);
        mStrokePaint.setColor(hubBdr);
        mStrokePaint.setStrokeWidth(1.5f * u);
        canvas.drawCircle(mWheelCx, mWheelCy, mRHubMax, mStrokePaint);

        /* Sector Labels */
        float rOuterLbl = (mROuterMin + mROuterMax) * 0.5f;
        float rInnerLbl = (mRInnerMin + mRInnerMax) * 0.5f;
        float outerFont = Math.max(10.0f, mROuterMax * 0.135f);
        float innerFont = Math.max(8.5f, mROuterMax * 0.105f);

        for (int s = 0; s < 12; s++) {
            double ang = Math.toRadians(s * 30.0 - 90.0);
            float cosA = (float) Math.cos(ang);
            float sinA = (float) Math.sin(ang);
            int rel = (s - mKeySector + 12) % 12;
            boolean inKey = (rel == 0 || rel == 1 || rel == 11);

            float ox = mWheelCx + cosA * rOuterLbl;
            float oy = mWheelCy + sinA * rOuterLbl;
            boolean selO = (mCurRing == 0 && mCurSector == s);
            int oCol = selO ? 0xFFFFFFFF : (inKey ? 0xFFFFFBEB : COL_TEXT_MUTED);
            drawTextAtPoint(canvas, MusicTheory.WHEEL[s].majName, ox, oy, outerFont, oCol);

            float ix = mWheelCx + cosA * rInnerLbl;
            float iy = mWheelCy + sinA * rInnerLbl;
            boolean selI = (mCurRing == 1 && mCurSector == s);
            int iCol = selI ? 0xFFFFFFFF : (inKey ? 0xFFF1F5F9 : 0xFF78889E);
            drawTextAtPoint(canvas, MusicTheory.WHEEL[s].minName, ix, iy, innerFont, iCol);
        }

        /* Hub Labels */
        float hubStep = mRHubMax * 0.36f;
        String keyHub = "KEY " + MusicTheory.WHEEL[mKeySector].majName;
        drawTextAtPoint(canvas, keyHub, mWheelCx, mWheelCy - hubStep, outerFont * 0.92f, COL_GOLD);
        drawTextAtPoint(canvas, MusicTheory.WHEEL[mKeySector].keySig, mWheelCx, mWheelCy, innerFont, COL_SKY_LIGHT);

        String dimLbl = MusicTheory.WHEEL[mKeySector].dimRootName + (mSeventhMode ? "m7b5" : "dim");
        int dimCol = (mCurRing == 2) ? COL_SEL_BDR : 0xFFF472B6;
        drawTextAtPoint(canvas, dimLbl, mWheelCx, mWheelCy + hubStep, innerFont, dimCol);
    }

    private void drawAnnularSector(Canvas canvas, float cx, float cy,
                                   float rInner, float rOuter,
                                   float startDeg, float sweepDeg,
                                   int fillCol, int borderCol, float borderWidth) {
        mOuterOval.set(cx - rOuter, cy - rOuter, cx + rOuter, cy + rOuter);
        mInnerOval.set(cx - rInner, cy - rInner, cx + rInner, cy + rInner);

        mSectorPath.reset();
        mSectorPath.arcTo(mOuterOval, startDeg, sweepDeg, true);
        mSectorPath.arcTo(mInnerOval, startDeg + sweepDeg, -sweepDeg, false);
        mSectorPath.close();

        mFillPaint.setColor(fillCol);
        canvas.drawPath(mSectorPath, mFillPaint);

        if (borderCol != 0) {
            mStrokePaint.setColor(borderCol);
            mStrokePaint.setStrokeWidth(borderWidth);
            canvas.drawPath(mSectorPath, mStrokePaint);
        }
    }

    private void drawChordInfoCard(Canvas canvas, float x, float y, float w, float h, float u) {
        mTmpRect.set(x, y, x + w, y + h);
        drawCard(canvas, mTmpRect, COL_CARD_FILL, COL_CARD_BDR, u);

        /* Chord Name */
        float titleSize = Math.min(16.0f * u, h * 0.36f);
        mTextPaint.setTextSize(titleSize);
        mTextPaint.setColor(COL_GOLD);
        float nameX = x + 6.0f * u;
        float nameBaseline = y + titleSize + 3.0f * u;
        AppI18n.drawText(canvas, mResolved.chordName, nameX, nameBaseline, mTextPaint);
        float nameW = mTextPaint.measureText(mResolved.chordName);

        /* Roman Numeral Function Pill */
        int famFill = (mResolved.diatonicFamily == 1) ? 0xFF925C0C :
                      (mResolved.diatonicFamily == 2) ? 0xFF1D4EB2 :
                      (mResolved.diatonicFamily == 3) ? 0xFF0E845C : 0xFF44405C;
        int famBdr  = (mResolved.diatonicFamily == 1) ? COL_GOLD :
                      (mResolved.diatonicFamily == 2) ? 0xFF60A5FA :
                      (mResolved.diatonicFamily == 3) ? 0xFF34D399 : 0xFFA78BFA;

        mTextPaint.setTextSize(9.0f * u);
        float rpillW = mTextPaint.measureText(mResolved.romanNum) + 12.0f * u;
        float rpillH = Math.min(15.0f * u, h * 0.32f);
        float rpillX = nameX + nameW + 6.0f * u;
        float rpillY = y + 5.0f * u;
        mTmpRect.set(rpillX, rpillY, rpillX + rpillW, rpillY + rpillH);
        drawPill(canvas, mTmpRect, famFill, famBdr, u);
        drawCenteredText(canvas, mTmpRect, mResolved.romanNum, 9.0f * u, 0xFFFFFFFF);

        /* Triad / 7th Toggle Pill (B Button equivalent) */
        float bPillW = 46.0f * u;
        float bPillH = rpillH;
        mSeventhPillRect.set(x + w - bPillW - 5.0f * u, rpillY, x + w - 5.0f * u, rpillY + bPillH);
        int bFill = mSeventhMode ? 0xFF6D28D9 : 0xFF1E3A5F;
        int bBdr  = mSeventhMode ? 0xFFC084FC : 0xFF60A5FA;
        drawPill(canvas, mSeventhPillRect, bFill, bBdr, u);
        drawCenteredText(canvas, mSeventhPillRect, mSeventhMode ? "7THS" : "TRIADS", 8.8f * u, 0xFFFFFFFF);

        /* Function Description & Spelled Chord Notes + Scale Degree Numbers in Key */
        mTextPaint.setTextSize(9.2f * u);
        mTextPaint.setColor(COL_TEXT_MUTED);
        AppI18n.drawText(canvas, mResolved.funcDesc, nameX, y + h * 0.60f, mTextPaint);

        float notesY = y + h * 0.88f;
        mTextPaint.setTextSize(10.6f * u);
        mTextPaint.setColor(COL_TEXT_SUB);
        AppI18n.drawText(canvas, mResolved.notesStr, nameX, notesY, mTextPaint);

        float notesW = mTextPaint.measureText(mResolved.notesStr);
        mTextPaint.setColor(COL_SKY_LIGHT);
        AppI18n.drawText(canvas, "   (" + mResolved.degreesStr + ")", nameX + notesW, notesY, mTextPaint);
    }

    private void drawGuitarFretboardCard(Canvas canvas, float x, float y, float w, float h, float u) {
        mFretboardCardRect.set(x, y, x + w, y + h);
        drawCard(canvas, mFretboardCardRect, COL_CARD_FILL, COL_CARD_BDR, u);

        /* Top Voicing Header Bar (Tap or swipe left/right to cycle voicings) */
        float hdrH = Math.min(18.0f * u, h * 0.16f);
        mVoicingBarRect.set(x + 2.0f * u, y + 2.0f * u, x + w - 2.0f * u, y + 2.0f * u + hdrH);
        drawPill(canvas, mVoicingBarRect, 0xFF1B263B, 0xFF334462, u);

        mTextPaint.setTextSize(9.0f * u);
        mTextPaint.setColor(0xFF7DD3FC);
        AppI18n.drawText(canvas, "< " + mVoicing.label + " >", x + 8.0f * u, y + 2.0f * u + hdrH * 0.72f, mTextPaint);

        String swipeHint = AppI18n.tr("TAP / SWIPE");
        mTextPaint.setColor(COL_GOLD);
        float swW = mTextPaint.measureText(swipeHint);
        AppI18n.drawText(canvas, swipeHint, x + w - swW - 8.0f * u, y + 2.0f * u + hdrH * 0.72f, mTextPaint);

        /* Compute base fret (1 unless max fretted > 4) */
        int minFret = 99, maxFret = 0;
        for (int s = 0; s < 6; s++) {
            int f = mVoicing.frets[s];
            if (f > 0) {
                if (f < minFret) minFret = f;
                if (f > maxFret) maxFret = f;
            }
        }
        int baseFret = 1;
        if (maxFret > 4) {
            baseFret = (minFret < 99 && minFret > 1) ? minFret : (maxFret - 3);
        }

        /* 6-String x 4-Fret Grid Geometry + Side Speaker Button */
        float gridTop = y + hdrH + 14.0f * u;
        float gridBot = y + h - 15.0f * u;
        float gridH = Math.max(24.0f * u, gridBot - gridTop);
        float fretDy = gridH / 4.0f;

        /* Compact Speaker Icon Button on the right side of the fretboard */
        float spkSize = 24.0f * u;
        float spkX = x + w - spkSize - 5.0f * u;
        float spkY = gridTop + (gridH - spkSize) * 0.5f;
        mStrumBadgeRect.set(spkX, spkY, spkX + spkSize, spkY + spkSize);
        drawSpeakerButton(canvas, mStrumBadgeRect, u);

        float gridLeft = x + 26.0f * u;
        float gridRight = spkX - 9.0f * u;
        float gridW = Math.max(50.0f * u, gridRight - gridLeft);
        float strDx = gridW / 5.0f;

        mFretGridRect.set(gridLeft - 8.0f * u, gridTop - 10.0f * u,
                gridRight + 8.0f * u, gridBot + 12.0f * u);

        /* Fretboard Wood Background */
        mFillPaint.setColor(COL_FRET_WOOD);
        canvas.drawRect(gridLeft, gridTop, gridLeft + gridW, gridTop + gridH, mFillPaint);

        /* Nut Bar (ivory when baseFret == 1) or Base Fret Indicator ("5fr") */
        if (baseFret == 1) {
            mFillPaint.setColor(0xFFF1F5F9);
            canvas.drawRect(gridLeft, gridTop - 2.5f * u, gridLeft + gridW, gridTop + 1.0f * u, mFillPaint);
        } else {
            String frStr = baseFret + "fr";
            mTextPaint.setTextSize(9.0f * u);
            mTextPaint.setColor(COL_GOLD);
            float fw = mTextPaint.measureText(frStr);
            AppI18n.drawText(canvas, frStr, gridLeft - fw - 4.0f * u, gridTop + fretDy * 0.65f, mTextPaint);

            mFillPaint.setColor(COL_TEXT_MUTED);
            canvas.drawRect(gridLeft, gridTop, gridLeft + gridW, gridTop + 1.2f * u, mFillPaint);
        }

        /* Fret Wires */
        mFillPaint.setColor(0xFF788291);
        for (int f = 1; f <= 4; f++) {
            float fy = gridTop + f * fretDy;
            canvas.drawRect(gridLeft, fy, gridLeft + gridW, fy + 1.2f * u, mFillPaint);
        }

        boolean flats = MusicTheory.WHEEL[mKeySector].useFlats;
        String[] noteTbl = flats ? MusicTheory.NOTE_FLAT : MusicTheory.NOTE_SHARP;
        long now = System.currentTimeMillis();

        /* 6 Guitar Strings + X/O Markers + Sounded Note Labels */
        for (int s = 0; s < 6; s++) {
            float sx = gridLeft + s * strDx;
            mStringX[s] = sx;

            boolean vibrating = (now >= mStringPluckTimeMs[s] && (now - mStringPluckTimeMs[s]) < 220L);
            int strCol = vibrating ? COL_GOLD : ((s < 3) ? 0xFFCBD5E1 : 0xFF94A3B8);
            float strW = vibrating ? (2.2f * u) : ((s < 3) ? (1.5f * u) : (1.1f * u));
            mFillPaint.setColor(strCol);
            canvas.drawRect(sx - strW * 0.5f, gridTop, sx + strW * 0.5f, gridTop + gridH, mFillPaint);

            /* Above-Nut X / O Markers */
            float xoY = gridTop - 6.5f * u;
            if (mVoicing.frets[s] < 0) {
                drawTextAtPoint(canvas, "x", sx, xoY, 9.0f * u, 0xFFF87171);
            } else if (mVoicing.frets[s] == 0) {
                drawTextAtPoint(canvas, "o", sx, xoY, 9.0f * u, COL_GREEN);
            }

            /* Sounded Note Name beneath string */
            float noteY = gridTop + gridH + 8.5f * u;
            if (mVoicing.frets[s] >= 0) {
                int pc = (MusicTheory.STRING_MIDI[s] + mVoicing.frets[s]) % 12;
                boolean isRoot = (pc == mResolved.rootPc);
                drawTextAtPoint(canvas, noteTbl[pc], sx, noteY, 9.0f * u,
                        isRoot ? COL_GOLD : 0xFFCBD5E1);
            } else {
                drawTextAtPoint(canvas, MusicTheory.STRING_NAMES[s], sx, noteY, 8.5f * u, 0xFF64748B);
            }
        }

        /* Finger Dots on Fretted Strings */
        float dotRadius = Math.min(strDx, fretDy) * 0.36f;
        for (int s = 0; s < 6; s++) {
            int f = mVoicing.frets[s];
            if (f > 0) {
                int relF = f - baseFret + 1;
                if (relF >= 1 && relF <= 4) {
                    float cx = gridLeft + s * strDx;
                    float cy = gridTop + (relF - 0.5f) * fretDy;
                    int pc = (MusicTheory.STRING_MIDI[s] + f) % 12;
                    boolean isRoot = (pc == mResolved.rootPc);
                    int dotFill = isRoot ? 0xFFF59E0B : 0xFF0EA5E9;
                    int dotBdr  = isRoot ? COL_SEL_BDR : 0xFFE0F2FE;

                    mFillPaint.setColor(dotFill);
                    canvas.drawCircle(cx, cy, dotRadius, mFillPaint);
                    mStrokePaint.setColor(dotBdr);
                    mStrokePaint.setStrokeWidth(1.3f * u);
                    canvas.drawCircle(cx, cy, dotRadius, mStrokePaint);
                }
            }
        }
    }

    private void drawSpeakerButton(Canvas canvas, RectF rect, float u) {
        drawPill(canvas, rect, 0xFF14532D, COL_GREEN, u);

        float cx = rect.centerX();
        float cy = rect.centerY();
        float s = rect.width() * 0.24f;

        /* Speaker cone polygon */
        mSectorPath.reset();
        mSectorPath.moveTo(cx - 1.25f * s, cy - 0.45f * s);
        mSectorPath.lineTo(cx - 0.55f * s, cy - 0.45f * s);
        mSectorPath.lineTo(cx + 0.15f * s, cy - 1.05f * s);
        mSectorPath.lineTo(cx + 0.15f * s, cy + 1.05f * s);
        mSectorPath.lineTo(cx - 0.55f * s, cy + 0.45f * s);
        mSectorPath.lineTo(cx - 1.25f * s, cy + 0.45f * s);
        mSectorPath.close();

        mFillPaint.setColor(COL_GREEN);
        canvas.drawPath(mSectorPath, mFillPaint);

        /* Two curved sound wave arcs */
        mStrokePaint.setColor(COL_GREEN);
        mStrokePaint.setStrokeWidth(1.4f * u);
        float r1 = 0.65f * s;
        mTmpRect.set(cx - 0.1f * s - r1, cy - r1, cx - 0.1f * s + r1, cy + r1);
        canvas.drawArc(mTmpRect, -45.0f, 90.0f, false, mStrokePaint);

        float r2 = 1.20f * s;
        mTmpRect.set(cx - 0.1f * s - r2, cy - r2, cx - 0.1f * s + r2, cy + r2);
        canvas.drawArc(mTmpRect, -48.0f, 96.0f, false, mStrokePaint);
    }

    private void drawBottomProgressionBar(Canvas canvas, float x, float y, float w, float h, float u) {
        if (x > 0.5f) {
            /* Right-pane card style in Landscape mode */
            mTmpRect.set(x, y, x + w, y + h);
            drawCard(canvas, mTmpRect, COL_CARD_FILL, COL_CARD_BDR, u);
        } else {
            /* Full-width bottom bar style in Portrait mode */
            mFillPaint.setColor(COL_BOTTOM_BG);
            canvas.drawRect(x, y, x + w, y + h, mFillPaint);
            mFillPaint.setColor(COL_BOTTOM_BDR);
            canvas.drawRect(x, y, x + w, y + 1.2f * u, mFillPaint);
        }

        /* Use horizontal unit ux so all 7 pills fit cleanly in both Portrait and Landscape */
        float ux = Math.min(u, w / 285.0f);
        float pad = 3.5f * ux;
        float pillY = y + 3.0f * u;
        float pillH = h - 6.0f * u;

        /* 1. Play / Pause Loop Button */
        float playW = 46.0f * ux;
        mLoopPlayPillRect.set(x + pad, pillY, x + pad + playW, pillY + pillH);
        int playFill = mLoopPlaying ? COL_EMERALD : 0xFF1E293B;
        int playBdr  = mLoopPlaying ? 0xFF6EE7B7 : 0xFF475569;
        drawPill(canvas, mLoopPlayPillRect, playFill, playBdr, ux);
        drawCenteredText(canvas, mLoopPlayPillRect,
                mLoopPlaying ? "|| PAUSE" : "> LOOP", 8.2f * ux, 0xFFFFFFFF);

        /* 2. [+ ADD] Chord to Progression Pill */
        float addW = 34.0f * ux;
        float addX = mLoopPlayPillRect.right + 2.2f * ux;
        mAddProgPillRect.set(addX, pillY, addX + addW, pillY + pillH);
        drawPill(canvas, mAddProgPillRect, 0xFF172554, COL_SKY, ux);
        drawCenteredText(canvas, mAddProgPillRect, "+PROG", 8.0f * ux, COL_SKY_LIGHT);

        /* 3. Rightmost [PRESETS] Pill */
        float helpW = 44.0f * ux;
        mHelpPillRect.set(x + w - pad - helpW, pillY, x + w - pad, pillY + pillH);
        drawPill(canvas, mHelpPillRect, 0xFF1E3A5F, 0xFF60A5FA, ux);
        drawCenteredText(canvas, mHelpPillRect, "PRESETS", 8.0f * ux, COL_SEL_BDR);

        /* 4. Four Progression Chord Slots filling the middle */
        float slotsLeft = mAddProgPillRect.right + 2.5f * ux;
        float slotsRight = mHelpPillRect.left - 2.5f * ux;
        float slotGap = 2.0f * ux;
        float slotW = (slotsRight - slotsLeft - 3.0f * slotGap) / 4.0f;

        for (int i = 0; i < 4; i++) {
            MusicTheory.resolveChordAt(mKeySector, mProg[i].sector, mProg[i].ring, mProg[i].use7th, mSlotResolved);
            boolean activeStep = (mLoopPlaying && mLoopStep == i);
            boolean nextWrite  = (!mLoopPlaying && mProgWriteIdx == i);

            float sx = slotsLeft + i * (slotW + slotGap);
            mProgSlotRects[i].set(sx, pillY, sx + slotW, pillY + pillH);

            int sFill = activeStep ? 0xFFD97706 : 0xFF1A2438;
            int sBdr  = activeStep ? COL_SEL_BDR : (nextWrite ? COL_SKY : 0xFF405273);
            drawPill(canvas, mProgSlotRects[i], sFill, sBdr, ux);
            drawCenteredText(canvas, mProgSlotRects[i], mSlotResolved.chordName, 8.2f * ux,
                    activeStep ? 0xFFFFFFFF : COL_TEXT_SUB);
        }
    }

    private void drawHelpModal(Canvas canvas, int w, int h) {
        /* Dim backdrop */
        mFillPaint.setColor(0xCC060911);
        canvas.drawRect(0, 0, w, h, mFillPaint);

        float u = Math.min(w / 320.0f, h / 240.0f);
        float mw = Math.min(w - 20.0f * u, 310.0f * u);
        float mh = Math.min(h - 16.0f * u, 224.0f * u);
        float mx = (w - mw) * 0.5f;
        float my = (h - mh) * 0.5f;

        mModalBoxRect.set(mx, my, mx + mw, my + mh);
        drawCard(canvas, mModalBoxRect, 0xFF0F1726, 0xFF60A5FA, u);

        /* Header Bar */
        float hdrH = 22.0f * u;
        mTmpRect.set(mx + 2.0f * u, my + 2.0f * u, mx + mw - 2.0f * u, my + 2.0f * u + hdrH);
        mFillPaint.setColor(0xFF1C273C);
        canvas.drawRect(mTmpRect, mFillPaint);

        mModalCloseRect.set(mx + mw - 50.0f * u, my + 4.0f * u, mx + mw - 6.0f * u, my + 4.0f * u + hdrH - 4.0f * u);
        drawPill(canvas, mModalCloseRect, 0xFF7F1D1D, 0xFFF87171, u);
        drawCenteredText(canvas, mModalCloseRect, AppI18n.str(getContext(), R.string.close), 8.5f * u, 0xFFFFFFFF);

        mTextPaint.setTextSize(10.2f * u);
        mTextPaint.setColor(COL_TEXT_MAIN);
        AppI18n.drawTextFit(canvas, AppI18n.str(getContext(), R.string.help_presets_title),
                mx + 10.0f * u, my + 16.0f * u, mTextPaint, mModalCloseRect.left - (mx + 16.0f * u));

        /* Auto-Looper Start/Stop Button inside Modal */
        float py = my + hdrH + 6.0f * u;
        float loopBtnH = 22.0f * u;
        mModalLoopBtnRect.set(mx + 8.0f * u, py, mx + mw - 8.0f * u, py + loopBtnH);
        int lFill = mLoopPlaying ? COL_EMERALD : 0xFF172134;
        int lBdr  = mLoopPlaying ? 0xFF6EE7B7 : COL_SKY;
        drawPill(canvas, mModalLoopBtnRect, lFill, lBdr, u);
        drawCenteredText(canvas, mModalLoopBtnRect,
                mLoopPlaying ? AppI18n.str(getContext(), R.string.help_looper_pause)
                             : AppI18n.str(getContext(), R.string.help_looper_play),
                9.2f * u, 0xFFFFFFFF);

        /* 5 Tappable Progression Preset Rows */
        float presetTop = mModalLoopBtnRect.bottom + 5.0f * u;
        float presetRowH = 19.5f * u;
        for (int i = 0; i < 5; i++) {
            float ry = presetTop + i * (presetRowH + 2.5f * u);
            mModalPresetRects[i].set(mx + 8.0f * u, ry, mx + mw - 8.0f * u, ry + presetRowH);
            boolean sel = (mPresetIdx == i);
            int pFill = sel ? 0xFF1E3A8A : 0xFF162032;
            int pBdr  = sel ? COL_GOLD : 0xFF334462;
            drawPill(canvas, mModalPresetRects[i], pFill, pBdr, u);
            drawCenteredText(canvas, mModalPresetRects[i],
                    MusicTheory.PRESETS[i].name, 9.0f * uxOrU(u), sel ? COL_SEL_BDR : COL_TEXT_SUB);
        }

        /* Touch & Gesture Quick Summary Footer */
        float footY = mModalPresetRects[4].bottom + 6.0f * u;
        mTextPaint.setTextSize(8.2f * u);
        mTextPaint.setColor(COL_SKY_LIGHT);
        AppI18n.drawTextFit(canvas, AppI18n.str(getContext(), R.string.help_presets_touch),
                mx + 10.0f * u, footY + 9.0f * u, mTextPaint, mw - 20.0f * u);
        mTextPaint.setColor(COL_TEXT_MUTED);
        AppI18n.drawTextFit(canvas, AppI18n.str(getContext(), R.string.help_presets_keys),
                mx + 10.0f * u, footY + 20.0f * u, mTextPaint, mw - 20.0f * u);
    }

    private float uxOrU(float u) {
        return u;
    }

    private void drawGuideModal(Canvas canvas, int w, int h) {
        /* Dim backdrop */
        mFillPaint.setColor(0xCC060911);
        canvas.drawRect(0, 0, w, h, mFillPaint);

        float u = Math.min(w / 320.0f, h / 240.0f);
        float mw = Math.min(w - 16.0f * u, 308.0f * u);
        float mh = Math.min(h - 14.0f * u, 226.0f * u);
        float mx = (w - mw) * 0.5f;
        float my = (h - mh) * 0.5f;

        mModalBoxRect.set(mx, my, mx + mw, my + mh);
        drawCard(canvas, mModalBoxRect, 0xFF0F1726, 0xFF60A5FA, u);

        /* Header Bar */
        float hdrH = 22.0f * u;
        mTmpRect.set(mx + 2.0f * u, my + 2.0f * u, mx + mw - 2.0f * u, my + 2.0f * u + hdrH);
        mFillPaint.setColor(0xFF1C273C);
        canvas.drawRect(mTmpRect, mFillPaint);

        mModalCloseRect.set(mx + mw - 56.0f * u, my + 4.0f * u, mx + mw - 6.0f * u, my + 4.0f * u + hdrH - 4.0f * u);
        drawPill(canvas, mModalCloseRect, 0xFF7F1D1D, 0xFFF87171, u);
        drawCenteredText(canvas, mModalCloseRect, AppI18n.str(getContext(), R.string.close), 8.5f * u, 0xFFFFFFFF);

        mTextPaint.setTextSize(10.0f * u);
        mTextPaint.setColor(COL_TEXT_MAIN);
        AppI18n.drawTextFit(canvas, AppI18n.str(getContext(), R.string.help_guide_title),
                mx + 10.0f * u, my + 16.0f * u, mTextPaint, mModalCloseRect.left - (mx + 16.0f * u));

        Context ctx = getContext();
        String[] titles = {
            AppI18n.str(ctx, R.string.help_guide_sec1_title),
            AppI18n.str(ctx, R.string.help_guide_sec2_title),
            AppI18n.str(ctx, R.string.help_guide_sec3_title),
            AppI18n.str(ctx, R.string.help_guide_sec4_title),
            AppI18n.str(ctx, R.string.help_guide_sec5_title)
        };
        String[] bodies = {
            AppI18n.str(ctx, R.string.help_guide_sec1_body),
            AppI18n.str(ctx, R.string.help_guide_sec2_body),
            AppI18n.str(ctx, R.string.help_guide_sec3_body),
            AppI18n.str(ctx, R.string.help_guide_sec4_body),
            AppI18n.str(ctx, R.string.help_guide_sec5_body)
        };
        int[] titleCols = { COL_GOLD, COL_GREEN, COL_SKY_LIGHT, COL_GOLD, COL_EMERALD };

        mGuideViewportRect.set(mx + 6.0f * u, my + hdrH + 5.0f * u, mx + mw - 6.0f * u, my + mh - 6.0f * u);
        mGuideMaxScrollY = AppI18n.drawScrollableHelpBlock(
                canvas, mGuideViewportRect, titles, bodies, titleCols,
                COL_TEXT_SUB, 0xFF151E30, 0xFF283753, COL_SKY,
                9.0f * u, 8.2f * u, mGuideScrollY, mTextPaint, mFillPaint);
        mGuideScrollY = Math.max(0.0f, Math.min(mGuideScrollY, mGuideMaxScrollY));
    }

    /* =========================================================================
     * Drawing Helpers
     * ========================================================================= */
    private void drawCard(Canvas canvas, RectF rect, int fillCol, int borderCol, float u) {
        float r = 3.0f * u;
        mFillPaint.setColor(fillCol);
        canvas.drawRoundRect(rect, r, r, mFillPaint);
        mStrokePaint.setColor(borderCol);
        mStrokePaint.setStrokeWidth(1.2f * u);
        canvas.drawRoundRect(rect, r, r, mStrokePaint);
    }

    private void drawPill(Canvas canvas, RectF rect, int fillCol, int borderCol, float u) {
        float r = Math.min(rect.height() * 0.35f, 5.0f * u);
        mFillPaint.setColor(fillCol);
        canvas.drawRoundRect(rect, r, r, mFillPaint);
        mStrokePaint.setColor(borderCol);
        mStrokePaint.setStrokeWidth(1.2f * u);
        canvas.drawRoundRect(rect, r, r, mStrokePaint);
    }

    private void drawCenteredText(Canvas canvas, RectF box, String text, float textSize, int color) {
        text = AppI18n.tr(text);
        mTextPaint.setTextSize(textSize);
        mTextPaint.setColor(color);
        float tw = mTextPaint.measureText(text);
        float maxW = Math.max(8.0f, box.width() - 6.0f);
        if (tw > maxW && tw > 0.0f) {
            mTextPaint.setTextSize(textSize * (maxW / tw));
            tw = mTextPaint.measureText(text);
        }
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        float tx = box.centerX() - tw * 0.5f;
        float ty = box.centerY() - (fm.ascent + fm.descent) * 0.5f;
        AppI18n.drawText(canvas, text, tx, ty, mTextPaint);
    }

    private void drawTextAtPoint(Canvas canvas, String text, float cx, float cy, float textSize, int color) {
        text = AppI18n.tr(text);
        mTextPaint.setTextSize(textSize);
        mTextPaint.setColor(color);
        float tw = mTextPaint.measureText(text);
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        AppI18n.drawText(canvas, text, cx - tw * 0.5f, cy - (fm.ascent + fm.descent) * 0.5f, mTextPaint);
    }

    /* =========================================================================
     * Touch & Gesture Handling (Wheel Spin, String Strum, Voicing Swipe, Pills)
     * ========================================================================= */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        final float x = event.getX();
        final float y = event.getY();

        if (mShowGuide) {
            if (action == MotionEvent.ACTION_DOWN) {
                mGuideTouchStartY = y;
                mGuideLastTouchY = y;
                mGuideDragging = false;
                if (mModalCloseRect.contains(x, y) || !mModalBoxRect.contains(x, y)) {
                    mShowGuide = false;
                    invalidate();
                }
            } else if (action == MotionEvent.ACTION_MOVE) {
                float dy = mGuideLastTouchY - y;
                if (Math.abs(y - mGuideTouchStartY) > mTouchSlop) {
                    mGuideDragging = true;
                }
                if (mGuideDragging && mGuideMaxScrollY > 0.0f) {
                    mGuideScrollY = Math.max(0.0f, Math.min(mGuideMaxScrollY, mGuideScrollY + dy));
                    mGuideLastTouchY = y;
                    invalidate();
                }
            } else if (action == MotionEvent.ACTION_UP) {
                mGuideDragging = false;
            }
            return true;
        }

        if (mShowHelp) {
            if (action == MotionEvent.ACTION_DOWN) {
                if (mModalCloseRect.contains(x, y) || !mModalBoxRect.contains(x, y)) {
                    mShowHelp = false;
                    invalidate();
                    return true;
                }
                if (mModalLoopBtnRect.contains(x, y)) {
                    toggleLoopPlayback();
                    return true;
                }
                for (int i = 0; i < 5; i++) {
                    if (mModalPresetRects[i].contains(x, y)) {
                        applyProgressionPreset(i);
                        strumCurrentSelection();
                        saveState(getContext());
                        invalidate();
                        return true;
                    }
                }
            }
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mDownX = x;
                mDownY = y;
                mTouchMode = TOUCH_NONE;
                mLastStrummedString = -1;
                mFretboardSwiped = false;
                mFretboardStrummedOnDrag = false;

                /* 0. Check Top-Left '?' Help Pill and Top-Right 'LOCK' Pill first */
                if (mTopHelpPillRect.contains(x, y)) {
                    mShowGuide = true;
                    invalidate();
                    return true;
                }
                if (mLockModePillRect.contains(x, y)) {
                    toggleKeyLock();
                    return true;
                }

                /* 1. Check Circle of Fifths Wheel */
                if (handleWheelTouch(x, y, true)) {
                    mTouchMode = TOUCH_WHEEL;
                    return true;
                }

                /* 3. Check Triad / 7th Pill */
                if (mSeventhPillRect.contains(x, y)) {
                    toggleSeventhMode();
                    return true;
                }

                /* 4. Check Voicing Header Bar (tap cycles forward) */
                if (mVoicingBarRect.contains(x, y)) {
                    cycleVoicing(1);
                    return true;
                }

                /* 5. Check Speaker Icon Strum Button */
                if (mStrumBadgeRect.contains(x, y)) {
                    strumCurrentSelection();
                    return true;
                }

                /* 6. Check Guitar Fretboard Diagram (supports tap, string-strum drag, or voicing swipe) */
                if (mFretboardCardRect.contains(x, y)) {
                    mTouchMode = TOUCH_FRETBOARD;
                    int nearStr = findNearestString(x);
                    if (nearStr >= 0 && mFretGridRect.contains(x, y)) {
                        mLastStrummedString = nearStr;
                    }
                    return true;
                }

                /* 7. Check Bottom Bar Controls */
                if (mLoopPlayPillRect.contains(x, y)) {
                    toggleLoopPlayback();
                    return true;
                }
                if (mAddProgPillRect.contains(x, y)) {
                    queueCurrentChordToProgression();
                    return true;
                }
                if (mHelpPillRect.contains(x, y)) {
                    mShowHelp = true;
                    invalidate();
                    return true;
                }
                for (int i = 0; i < 4; i++) {
                    if (mProgSlotRects[i].contains(x, y)) {
                        stopLoop();
                        mProgWriteIdx = i;
                        mCurSector = mProg[i].sector;
                        mCurRing = mProg[i].ring;
                        mSeventhMode = mProg[i].use7th;
                        strumCurrentSelection();
                        saveState(getContext());
                        invalidate();
                        return true;
                    }
                }
                return true;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mTouchMode == TOUCH_WHEEL) {
                    handleWheelTouch(x, y, false);
                    return true;
                } else if (mTouchMode == TOUCH_FRETBOARD) {
                    float dx = x - mDownX;
                    float dy = y - mDownY;
                    if (mFretGridRect.contains(x, y)) {
                        /* Dragging across strings plucks each crossed string in real time */
                        int s = findNearestString(x);
                        if (s >= 0 && s != mLastStrummedString) {
                            pluckSingleString(s);
                            mLastStrummedString = s;
                            mFretboardStrummedOnDrag = true;
                        }
                    } else if (!mFretboardSwiped && Math.abs(dx) > mTouchSlop * 2.2f && Math.abs(dx) > Math.abs(dy) * 1.5f) {
                        /* Horizontal swipe outside the string grid cycles guitar voicing */
                        mFretboardSwiped = true;
                        cycleVoicing(dx < 0 ? 1 : -1);
                    }
                    return true;
                }
                break;
            }

            case MotionEvent.ACTION_UP: {
                if (mTouchMode == TOUCH_FRETBOARD && !mFretboardSwiped && !mFretboardStrummedOnDrag) {
                    float dx = x - mDownX;
                    float dy = y - mDownY;
                    if (Math.abs(dx) > mTouchSlop * 2.2f && Math.abs(dx) > Math.abs(dy) * 1.5f) {
                        /* Quick horizontal fling cycles guitar voicing backward/forward */
                        cycleVoicing(dx < 0 ? 1 : -1);
                    } else {
                        /* Single tap on a string or fretboard strums the chord */
                        strumCurrentSelection();
                    }
                }
                mTouchMode = TOUCH_NONE;
                return true;
            }

            case MotionEvent.ACTION_CANCEL: {
                mTouchMode = TOUCH_NONE;
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    private int findNearestString(float x) {
        if (mStringX[5] <= mStringX[0]) return -1;
        float halfSpacing = (mStringX[1] - mStringX[0]) * 0.55f;
        int best = -1;
        float bestDist = halfSpacing;
        for (int s = 0; s < 6; s++) {
            float d = Math.abs(x - mStringX[s]);
            if (d <= bestDist) {
                bestDist = d;
                best = s;
            }
        }
        return best;
    }

    private boolean handleWheelTouch(float x, float y, boolean isDown) {
        float dx = x - mWheelCx;
        float dy = y - mWheelCy;
        float r = (float) Math.hypot(dx, dy);
        if (r > mROuterMax * 1.04f) return false;

        if (r <= mRHubMax) {
            if (isDown || mCurRing != 2) {
                selectSectorAndRing(mCurSector, 2);
            }
            return true;
        }

        double deg = Math.toDegrees(Math.atan2(dy, dx)) + 90.0 + 15.0;
        while (deg < 0.0) deg += 360.0;
        while (deg >= 360.0) deg -= 360.0;
        int sec = ((int) (deg / 30.0)) % 12;
        int ring = (r >= mROuterMin) ? 0 : 1;

        if (isDown || sec != mCurSector || ring != mCurRing) {
            selectSectorAndRing(sec, ring);
        }
        return true;
    }

    /* =========================================================================
     * Hardware Key / DPAD / Gamepad Support (Matches PowKiddy V90 Controls)
     * ========================================================================= */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mShowGuide) {
            mShowGuide = false;
            invalidate();
            return true;
        }
        if (mShowHelp) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                applyProgressionPreset(mPresetIdx - 1);
                saveState(getContext());
                invalidate();
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                applyProgressionPreset(mPresetIdx + 1);
                saveState(getContext());
                invalidate();
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_BUTTON_A || keyCode == KeyEvent.KEYCODE_A
                    || keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                toggleLoopPlayback();
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_BUTTON_B || keyCode == KeyEvent.KEYCODE_B
                    || keyCode == KeyEvent.KEYCODE_BUTTON_Y || keyCode == KeyEvent.KEYCODE_Y
                    || keyCode == KeyEvent.KEYCODE_H || keyCode == KeyEvent.KEYCODE_ESCAPE
                    || keyCode == KeyEvent.KEYCODE_BACK) {
                mShowHelp = false;
                invalidate();
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                rotateStep(1);
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                rotateStep(-1);
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                stopLoop();
                mCurRing = (mCurRing < 2) ? (mCurRing + 1) : 0;
                strumCurrentSelection();
                saveState(getContext());
                invalidate();
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                stopLoop();
                mCurRing = (mCurRing > 0) ? (mCurRing - 1) : 2;
                strumCurrentSelection();
                saveState(getContext());
                invalidate();
                return true;
            case KeyEvent.KEYCODE_BUTTON_A:
            case KeyEvent.KEYCODE_A:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_SPACE:
                strumCurrentSelection();
                return true;
            case KeyEvent.KEYCODE_BUTTON_B:
            case KeyEvent.KEYCODE_B:
                toggleSeventhMode();
                return true;
            case KeyEvent.KEYCODE_BUTTON_R1:
            case KeyEvent.KEYCODE_RIGHT_BRACKET:
                cycleVoicing(1);
                return true;
            case KeyEvent.KEYCODE_BUTTON_L1:
            case KeyEvent.KEYCODE_LEFT_BRACKET:
            case KeyEvent.KEYCODE_TAB:
                cycleVoicing(-1);
                return true;
            case KeyEvent.KEYCODE_BUTTON_SELECT:
            case KeyEvent.KEYCODE_C:
                toggleKeyLock();
                return true;
            case KeyEvent.KEYCODE_BUTTON_X:
            case KeyEvent.KEYCODE_X:
                queueCurrentChordToProgression();
                return true;
            case KeyEvent.KEYCODE_BUTTON_Y:
            case KeyEvent.KEYCODE_Y:
            case KeyEvent.KEYCODE_H:
                mShowHelp = true;
                invalidate();
                return true;
            case KeyEvent.KEYCODE_P:
                toggleLoopPlayback();
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }
}
