package com.eggwall.licktrainer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

/**
 * Custom interactive Canvas view for {@code LickTrainer}, featuring:
 * <ul>
 *   <li>Top Header with {@code ?} Help Pill, Category Selector, Lick Selector, Key Selector, and Audio Mix Pill</li>
 *   <li>Scrolling 6-String Guitar Tablature Grid with Pick-Direction Badges ({@code D/U/H/P//}),
 *       Beat Brackets, and Loop Range Selection</li>
 *   <li>Zoomed 8-Fret Guitar Neck Inspector showing Left-Hand Finger Numbers ({@code 1..4}),
 *       Scale Box Ghost Dots, and Jazz 7th Chord Tones ({@code 1-3-5-7})</li>
 *   <li>Transport &amp; Speed-Builder BPM Ladder Dock</li>
 *   <li>Responsive Portrait (vertical stack) and Landscape (two-pane split) layouts</li>
 * </ul>
 */
public class LickTrainerView extends View {

    private static final String PREFS_NAME = "licktrainer_prefs";

    private static final int COL_BG        = 0xFF0A0D14;
    private static final int COL_PANEL     = 0xFF131824;
    private static final int COL_PANEL_ALT = 0xFF1A2132;
    private static final int COL_BORDER    = 0xFF28334A;
    private static final int COL_TEXT      = 0xFFEAEFF8;
    private static final int COL_MUTED     = 0xFF8C9BB5;
    private static final int COL_CYAN      = 0xFF32D2FF;
    private static final int COL_GOLD      = 0xFFFFC83C;
    private static final int COL_GREEN     = 0xFF3CDC82;
    private static final int COL_CORAL     = 0xFFFF5A64;
    private static final int COL_PURPLE    = 0xFFB478FF;
    private static final int COL_WOOD      = 0xFF221914;

    private static final int[] FINGER_COLS = {
        COL_MUTED, COL_CYAN, COL_GREEN, COL_GOLD, COL_CORAL
    };

    private LickSynthEngine mSynth;

    private int mCatIdx = 0;
    private int mLickInCat = 0;
    private int mKeyIdx = 5; // Default Key of A (5th fret)
    private int mCurStep = 0;
    private int mBpm = 100;
    private int mBaseBpm = 100;
    private int mLadderIdx = 0;
    private int mMixMode = LickTheory.MIX_TAB_BACK_CLICK;
    private boolean mPlaying = false;
    private boolean mLoopSubRange = false;
    private int mLoopStartStep = 0;
    private int mLoopEndStep = 3;
    private int mBarsPlayed = 0;
    private boolean mShowHelp = false;
    private int mHelpTab = 0;

    private long mLastTickMs = 0L;
    private double mStepAccumMs = 0.0;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF mRectHelpPill = new RectF();
    private final RectF mRectCatPrev  = new RectF();
    private final RectF mRectCatNext  = new RectF();
    private final RectF mRectLickPrev = new RectF();
    private final RectF mRectLickNext = new RectF();
    private final RectF mRectKeyPill  = new RectF();
    private final RectF mRectMixPill  = new RectF();
    private final RectF mRectTabPanel = new RectF();
    private final RectF mRectNeckPanel = new RectF();
    private final RectF mRectPlayBtn  = new RectF();
    private final RectF mRectBpmMinus = new RectF();
    private final RectF mRectBpmPlus  = new RectF();
    private final RectF mRectLadderBtn = new RectF();
    private final RectF mRectLoopBtn  = new RectF();
    private final RectF mRectModalClose = new RectF();
    private final RectF mRectModalTab0  = new RectF();
    private final RectF mRectModalTab1  = new RectF();

    public LickTrainerView(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
        mTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        syncLickDefaults();
    }

    public void setSynthEngine(LickSynthEngine synth) {
        mSynth = synth;
    }

    private LickTheory.LickDef currentLick() {
        return LickTheory.LICKS[LickTheory.getGlobalLickIndex(mCatIdx, mLickInCat)];
    }

    private void syncLickDefaults() {
        LickTheory.LickDef lick = currentLick();
        mBpm = lick.defaultBpm;
        mBaseBpm = mBpm;
        mCurStep = 0;
        mLoopStartStep = 0;
        mLoopEndStep = Math.min(lick.numSteps - 1, lick.stepsPerBeat * 2 - 1);
        mBarsPlayed = 0;
    }

    public void loadState(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mCatIdx = LickTheory.clamp(sp.getInt("cat_idx", 0), 0, LickTheory.NUM_CATEGORIES - 1);
        mLickInCat = LickTheory.clamp(sp.getInt("lick_in_cat", 0), 0, LickTheory.CAT_COUNT[mCatIdx] - 1);
        mKeyIdx = LickTheory.clamp(sp.getInt("key_idx", 5), 0, LickTheory.NUM_KEYS - 1);
        mLadderIdx = LickTheory.clamp(sp.getInt("ladder_idx", 0), 0, LickTheory.NUM_LADDERS - 1);
        mMixMode = LickTheory.clamp(sp.getInt("mix_mode", LickTheory.MIX_TAB_BACK_CLICK), 0, LickTheory.MIX_COUNT - 1);
        syncLickDefaults();
        mBpm = LickTheory.clamp(sp.getInt("bpm", mBpm), LickTheory.MIN_BPM, LickTheory.MAX_BPM);
        mBaseBpm = mBpm;
    }

    public void saveState(Context ctx) {
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putInt("cat_idx", mCatIdx)
                .putInt("lick_in_cat", mLickInCat)
                .putInt("key_idx", mKeyIdx)
                .putInt("bpm", mBpm)
                .putInt("ladder_idx", mLadderIdx)
                .putInt("mix_mode", mMixMode)
                .apply();
    }

    public void configureDemoState(int catIdx, int lickInCat, int keyIdx, int curStep,
                                   int bpm, int ladderIdx, int mixMode,
                                   boolean playing, boolean loopBar, boolean showHelp) {
        mCatIdx = LickTheory.clamp(catIdx, 0, LickTheory.NUM_CATEGORIES - 1);
        mLickInCat = LickTheory.clamp(lickInCat, 0, LickTheory.CAT_COUNT[mCatIdx] - 1);
        mKeyIdx = LickTheory.clamp(keyIdx, 0, LickTheory.NUM_KEYS - 1);
        syncLickDefaults();
        mCurStep = LickTheory.clamp(curStep, 0, currentLick().numSteps - 1);
        if (bpm > 0) {
            mBpm = LickTheory.clamp(bpm, LickTheory.MIN_BPM, LickTheory.MAX_BPM);
            mBaseBpm = mBpm;
        }
        mLadderIdx = LickTheory.clamp(ladderIdx, 0, LickTheory.NUM_LADDERS - 1);
        mMixMode = LickTheory.clamp(mixMode, 0, LickTheory.MIX_COUNT - 1);
        mLoopSubRange = !loopBar;
        mShowHelp = showHelp;
        mPlaying = playing;
        mLastTickMs = SystemClock.uptimeMillis();
        invalidate();
    }

    public void stopPlayback() {
        mPlaying = false;
        if (mSynth != null) mSynth.silenceAll();
        invalidate();
    }

    public void previewCurrentStep() {
        if (mSynth == null) return;
        LickTheory.LickDef lick = currentLick();
        LickTheory.LickStep st = lick.steps[LickTheory.clamp(mCurStep, 0, lick.numSteps - 1)];
        int midi = LickTheory.getStepMidi(st, mKeyIdx);
        if (midi > 0 && mMixMode != LickTheory.MIX_CLICK_ONLY) {
            mSynth.triggerLeadNote(midi, st.pick);
        }
    }

    private void triggerSequencerStep(int stepIdx) {
        if (mSynth == null) return;
        LickTheory.LickDef lick = currentLick();
        LickTheory.LickStep st = lick.steps[stepIdx];
        boolean isBeatStart = (stepIdx % lick.stepsPerBeat) == 0;
        int beatIdx = stepIdx / lick.stepsPerBeat;

        if (isBeatStart && mMixMode != LickTheory.MIX_TAB_ONLY) {
            mSynth.triggerMetronomeClick(beatIdx == 0);
        }
        if (isBeatStart && mMixMode == LickTheory.MIX_TAB_BACK_CLICK) {
            int[] backing = LickTheory.getBackingChordMidi(lick, beatIdx, mKeyIdx);
            mSynth.triggerBackingChord(backing);
        }
        if (st.str >= 0 && mMixMode != LickTheory.MIX_CLICK_ONLY) {
            int midi = LickTheory.getStepMidi(st, mKeyIdx);
            mSynth.triggerLeadNote(midi, st.pick);
        }
    }

    private void advanceSequencerClock() {
        if (!mPlaying) return;
        long now = SystemClock.uptimeMillis();
        if (mLastTickMs == 0L) {
            mLastTickMs = now;
            return;
        }
        long dt = now - mLastTickMs;
        mLastTickMs = now;
        if (dt > 200L) dt = 200L;

        LickTheory.LickDef lick = currentLick();
        double stepDurMs = (60000.0 / Math.max(40, mBpm)) / lick.stepsPerBeat;
        mStepAccumMs += dt;
        while (mStepAccumMs >= stepDurMs) {
            mStepAccumMs -= stepDurMs;
            int minStep = mLoopSubRange ? mLoopStartStep : 0;
            int maxStep = mLoopSubRange ? mLoopEndStep : (lick.numSteps - 1);
            mCurStep++;
            if (mCurStep > maxStep) {
                mCurStep = minStep;
                mBarsPlayed++;
                LickTheory.LadderPreset lp = LickTheory.LADDERS[mLadderIdx];
                if (lp.barsPerStep > 0 && (mBarsPlayed % lp.barsPerStep) == 0) {
                    int goal = Math.min(LickTheory.MAX_BPM, mBaseBpm + lp.goalOffset);
                    mBpm = Math.min(goal, mBpm + lp.bpmStep);
                }
            }
            triggerSequencerStep(mCurStep);
        }
        postInvalidateOnAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        canvas.drawColor(COL_BG);
        boolean landscape = w > h;
        float pad = Math.max(10f, Math.min(w, h) * 0.018f);

        if (landscape) {
            float headerH = Math.max(88f, h * 0.17f);
            drawHeader(canvas, pad, pad, w - 2 * pad, headerH);

            float contentTop = pad * 2 + headerH;
            float contentH = h - contentTop - pad;

            float leftW = (w - 3 * pad) * 0.64f;
            float rightX = pad * 2 + leftW;
            float rightW = w - pad - rightX;

            // Compact horizontal Tablature (top) + compact horizontal Fretboard (bottom) on left pane
            float tabH = (contentH - pad) * 0.50f;
            float neckH = contentH - tabH - pad;

            mRectTabPanel.set(pad, contentTop, pad + leftW, contentTop + tabH);
            drawTabPanel(canvas, mRectTabPanel);

            mRectNeckPanel.set(pad, mRectTabPanel.bottom + pad, pad + leftW, contentTop + contentH);
            drawNeckPanel(canvas, mRectNeckPanel);

            // Right pane: Step/Finger Inspector card on top + Transport/Speed-Ladder cards below
            float inspH = contentH * 0.44f;
            RectF inspRect = new RectF(rightX, contentTop, rightX + rightW, contentTop + inspH);
            drawInspectorCard(canvas, inspRect);

            RectF dockRect = new RectF(rightX, inspRect.bottom + pad, rightX + rightW, contentTop + contentH);
            drawTransportDock(canvas, dockRect, true);
        } else {
            // Portrait:
            // 1. Sleek 2-row Header (11.5% of h)
            float headerH = Math.max(96f, h * 0.115f);
            drawHeader(canvas, pad, pad, w - 2 * pad, headerH);

            float y = pad * 2 + headerH;
            float remH = h - y - pad;

            // 2. Compact 6-String Tablature Staff (~24.5% of remH)
            float tabH = remH * 0.245f;
            mRectTabPanel.set(pad, y, w - pad, y + tabH);
            drawTabPanel(canvas, mRectTabPanel);
            y += tabH + pad;

            // 3. Step, Note, Picking & Left-Hand Finger Legend Inspector Card (~19% of remH)
            float inspH = remH * 0.19f;
            RectF inspRect = new RectF(pad, y, w - pad, y + inspH);
            drawInspectorCard(canvas, inspRect);
            y += inspH + pad;

            // 4. Compact 6-String Guitar Fretboard (~24.5% of remH)
            float neckH = remH * 0.245f;
            mRectNeckPanel.set(pad, y, w - pad, y + neckH);
            drawNeckPanel(canvas, mRectNeckPanel);
            y += neckH + pad;

            // 5. Prominent Two-Row Transport & Speed-Builder Dock at bottom
            RectF dockRect = new RectF(pad, y, w - pad, h - pad);
            drawTransportDock(canvas, dockRect, false);
        }

        if (mShowHelp) {
            drawHelpModal(canvas, w, h);
        }
        if (mPlaying) {
            advanceSequencerClock();
        }
    }

    private void drawHeader(Canvas c, float x, float y, float w, float h) {
        mPaint.setColor(COL_PANEL);
        c.drawRoundRect(new RectF(x, y, x + w, y + h), 14f, 14f, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2f);
        mPaint.setColor(COL_BORDER);
        c.drawRoundRect(new RectF(x, y, x + w, y + h), 14f, 14f, mPaint);
        mPaint.setStyle(Paint.Style.FILL);

        float rowH = (h - 16f) * 0.5f;
        float r1Y = y + 6f;
        float r2Y = r1Y + rowH + 4f;
        float arrowW = Math.min(rowH * 0.80f, w * 0.075f);

        // Top-left ? Help pill
        mRectHelpPill.set(x + 8f, r1Y, x + 8f + Math.min(rowH * 1.0f, w * 0.09f), r1Y + rowH);
        drawPill(c, mRectHelpPill, "?", COL_PANEL_ALT, COL_CYAN, rowH * 0.46f);

        // Category selector [< CAT >]
        float catX = mRectHelpPill.right + 8f;
        float catW = w * 0.44f;
        mRectCatPrev.set(catX, r1Y, catX + arrowW, r1Y + rowH);
        mRectCatNext.set(catX + catW - arrowW, r1Y, catX + catW, r1Y + rowH);
        drawPill(c, mRectCatPrev, "<", COL_PANEL_ALT, COL_TEXT, rowH * 0.42f);
        drawPill(c, mRectCatNext, ">", COL_PANEL_ALT, COL_TEXT, rowH * 0.42f);
        RectF catMid = new RectF(mRectCatPrev.right + 4f, r1Y, mRectCatNext.left - 4f, r1Y + rowH);
        drawPill(c, catMid, LickTheory.CATEGORY_NAMES[mCatIdx], COL_BG, COL_GOLD, rowH * 0.36f);

        // Key Pill & Audio Mix Pill on right of row 1
        float rightW = (x + w - 8f) - (catX + catW + 8f);
        float keyW = rightW * 0.50f;
        mRectKeyPill.set(catX + catW + 8f, r1Y, catX + catW + 8f + keyW, r1Y + rowH);
        mRectMixPill.set(mRectKeyPill.right + 6f, r1Y, x + w - 8f, r1Y + rowH);
        LickTheory.KeyAnchor ka = LickTheory.KEYS[mKeyIdx];
        drawPill(c, mRectKeyPill, "KEY: " + ka.name + " (" + ka.anchorFret + "fr)", COL_PANEL_ALT, COL_GREEN, rowH * 0.34f);
        drawPill(c, mRectMixPill, LickTheory.MIX_NAMES[mMixMode], COL_PANEL_ALT, COL_CYAN, rowH * 0.34f);

        // Row 2: Lick selector [< #01 TITLE — SUBTITLE >]
        LickTheory.LickDef lick = currentLick();
        int globalIdx = LickTheory.getGlobalLickIndex(mCatIdx, mLickInCat);
        mRectLickPrev.set(x + 8f, r2Y, x + 8f + arrowW, r2Y + rowH);
        mRectLickNext.set(x + w - 8f - arrowW, r2Y, x + w - 8f, r2Y + rowH);
        drawPill(c, mRectLickPrev, "<", COL_PANEL_ALT, COL_CYAN, rowH * 0.42f);
        drawPill(c, mRectLickNext, ">", COL_PANEL_ALT, COL_CYAN, rowH * 0.42f);

        RectF lickMid = new RectF(mRectLickPrev.right + 6f, r2Y, mRectLickNext.left - 6f, r2Y + rowH);
        String title = String.format("#%02d %s  •  %s", globalIdx + 1, lick.title, lick.subtitle);
        drawPill(c, lickMid, title, COL_BG, COL_TEXT, rowH * 0.34f);
    }

    private void drawTabPanel(Canvas c, RectF r) {
        mPaint.setColor(COL_PANEL);
        c.drawRoundRect(r, 14f, 14f, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2f);
        mPaint.setColor(COL_BORDER);
        c.drawRoundRect(r, 14f, 14f, mPaint);
        mPaint.setStyle(Paint.Style.FILL);

        LickTheory.LickDef lick = currentLick();
        int nSteps = lick.numSteps;
        float leftMargin = r.left + 42f;
        float rightMargin = r.right - 14f;
        float gridW = Math.max(10f, rightMargin - leftMargin);
        float colW = gridW / nSteps;

        float topPad = Math.max(38f, r.height() * 0.19f);
        float botPad = Math.max(30f, r.height() * 0.16f);
        float topY = r.top + topPad;
        float botY = r.bottom - botPad;
        float strSpacing = (botY - topY) / 5f;

        // Beat headers, vertical beat dividers & active step column highlight
        for (int s = 0; s < nSteps; s++) {
            float cx = leftMargin + (s + 0.5f) * colW;
            boolean inLoop = !mLoopSubRange || (s >= mLoopStartStep && s <= mLoopEndStep);
            if (s == mCurStep) {
                mPaint.setColor(0x3632D2FF);
                c.drawRoundRect(new RectF(cx - colW * 0.48f, r.top + 4f, cx + colW * 0.48f, r.bottom - 4f), 8f, 8f, mPaint);
            } else if (!inLoop) {
                mPaint.setColor(0x55000000);
                c.drawRect(cx - colW * 0.5f, topY - 8f, cx + colW * 0.5f, botY + 8f, mPaint);
            }
            if (s % lick.stepsPerBeat == 0) {
                int beat = s / lick.stepsPerBeat;
                float beatX = leftMargin + s * colW;
                if (s > 0) {
                    mPaint.setColor(0x4432D2FF);
                    mPaint.setStrokeWidth(1.5f);
                    c.drawLine(beatX, topY - 4f, beatX, botY + 4f, mPaint);
                }
                mTextPaint.setColor(COL_MUTED);
                mTextPaint.setTextSize(Math.min(20f, topPad * 0.48f));
                mTextPaint.setTextAlign(Paint.Align.LEFT);
                String bLbl = lick.isJazz7ths()
                        ? LickTheory.DIATONIC_7THS[lick.beatDeg[beat & 3]].roman
                        : ("BEAT " + (beat + 1));
                c.drawText(bLbl, beatX + 4f, r.top + topPad * 0.54f, mTextPaint);
            }
        }

        // 6 Tablature string lines (top line = high 'e' str 5, bottom line = low 'E' str 0)
        float strLabelTs = Math.min(26f, strSpacing * 0.72f);
        for (int row = 0; row < 6; row++) {
            int strIdx = 5 - row;
            float sy = topY + row * strSpacing;
            mPaint.setColor(COL_BORDER);
            mPaint.setStrokeWidth(1.5f + row * 0.25f);
            c.drawLine(leftMargin, sy, rightMargin, sy, mPaint);

            mTextPaint.setColor(COL_CYAN);
            mTextPaint.setTextSize(strLabelTs);
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            c.drawText(String.valueOf(LickTheory.STR_LABELS[strIdx]), r.left + 21f, sy + strLabelTs * 0.35f, mTextPaint);
        }

        // Draw large, circle-free fret numbers directly on the tab lines + picking badges at bottom
        float numTs = Math.min(strSpacing * 0.90f, colW * 0.78f);
        float pickTs = Math.min(22f, Math.min(botPad * 0.68f, colW * 0.54f));
        for (int s = 0; s < nSteps; s++) {
            LickTheory.LickStep st = lick.steps[s];
            float cx = leftMargin + (s + 0.5f) * colW;
            if (st.str >= 0) {
                int row = 5 - st.str;
                float sy = topY + row * strSpacing;
                int fret = LickTheory.getActualFret(st, mKeyIdx);
                String fretStr = String.valueOf(fret);
                int fCol = FINGER_COLS[LickTheory.clamp(st.finger, 0, 4)];
                boolean isCur = (s == mCurStep);

                mTextPaint.setTextSize(numTs);
                float tw = mTextPaint.measureText(fretStr);
                float boxHalfW = Math.max(tw * 0.58f + 5f, numTs * 0.42f);
                float boxHalfH = numTs * 0.50f;
                RectF numBg = new RectF(cx - boxHalfW, sy - boxHalfH, cx + boxHalfW, sy + boxHalfH);

                // Clean rectangular backing so the string line does not strike through the fret number
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(isCur ? COL_GOLD : COL_PANEL);
                c.drawRoundRect(numBg, 6f, 6f, mPaint);

                mTextPaint.setColor(isCur ? COL_BG : fCol);
                mTextPaint.setTextAlign(Paint.Align.CENTER);
                Paint.FontMetrics fm = mTextPaint.getFontMetrics();
                c.drawText(fretStr, cx, sy - (fm.ascent + fm.descent) * 0.5f, mTextPaint);
            }

            // Pick badge at bottom of column (D = Down, U = Up, H/P// = Legato)
            mTextPaint.setColor(st.pick == 'D' ? COL_CYAN : (st.pick == 'U' ? COL_GOLD : COL_PURPLE));
            mTextPaint.setTextSize(pickTs);
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            c.drawText(String.valueOf(st.pick), cx, r.bottom - botPad * 0.22f, mTextPaint);
        }
    }

    private void drawInspectorCard(Canvas c, RectF r) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(COL_PANEL);
        c.drawRoundRect(r, 14f, 14f, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2f);
        mPaint.setColor(COL_BORDER);
        c.drawRoundRect(r, 14f, 14f, mPaint);
        mPaint.setStyle(Paint.Style.FILL);

        LickTheory.LickDef lick = currentLick();
        LickTheory.LickStep cur = lick.steps[LickTheory.clamp(mCurStep, 0, lick.numSteps - 1)];
        int midi = LickTheory.getStepMidi(cur, mKeyIdx);
        String noteName = LickTheory.getNoteNameForMidi(midi);
        int fret = cur.str >= 0 ? LickTheory.getActualFret(cur, mKeyIdx) : 0;
        String strName = cur.str >= 0 ? String.valueOf(LickTheory.STR_LABELS[cur.str]) : "-";
        String fingerName = LickTheory.FINGER_NAMES[LickTheory.clamp(cur.finger, 0, 4)];
        String pickDesc = (cur.pick == 'D') ? "DOWN (D)"
                : (cur.pick == 'U') ? "UP (U)"
                : (cur.pick == 'H') ? "HAMMER (H)"
                : (cur.pick == 'P') ? "PULL (P)" : "SLIDE (/)";

        float pad = 10f;
        float topRowH = (r.height() - pad * 3f) * 0.54f;
        float botRowH = r.height() - topRowH - pad * 3f;

        // Top row: 4 status badges (STEP, NOTE/FRET, FINGER, PICK/TONE)
        float cellGap = 8f;
        float cellW = (r.width() - pad * 2f - cellGap * 3f) / 4f;
        float y1 = r.top + pad;
        String[] topLabels = {
            String.format("STEP %02d/%02d", mCurStep + 1, lick.numSteps),
            "NOTE: " + noteName + " (" + strName + ":" + fret + "fr)",
            "FINGER: " + fingerName,
            lick.isJazz7ths()
                    ? ("TONE: " + LickTheory.getJazzStepIntervalLabel(lick, mCurStep, mKeyIdx))
                    : ("PICK: " + pickDesc)
        };
        int[] topCols = {
            COL_CYAN,
            COL_GOLD,
            FINGER_COLS[LickTheory.clamp(cur.finger, 0, 4)],
            cur.pick == 'D' ? COL_CYAN : (cur.pick == 'U' ? COL_GOLD : COL_PURPLE)
        };
        for (int i = 0; i < 4; i++) {
            float bx = r.left + pad + i * (cellW + cellGap);
            RectF br = new RectF(bx, y1, bx + cellW, y1 + topRowH);
            drawPill(c, br, topLabels[i], COL_PANEL_ALT, topCols[i], topRowH * 0.38f);
        }

        // Bottom row: Left-Hand Finger Color Legend (1:Index, 2:Middle, 3:Ring, 4:Pinky)
        float y2 = y1 + topRowH + pad;
        String[] fNames = { "1: INDEX", "2: MIDDLE", "3: RING", "4: PINKY" };
        for (int i = 0; i < 4; i++) {
            int fNum = i + 1;
            float bx = r.left + pad + i * (cellW + cellGap);
            RectF br = new RectF(bx, y2, bx + cellW, y2 + botRowH);
            boolean activeFinger = (cur.finger == fNum);
            drawPill(c, br, fNames[i],
                    activeFinger ? FINGER_COLS[fNum] : COL_BG,
                    activeFinger ? COL_BG : FINGER_COLS[fNum],
                    botRowH * 0.40f);
        }
    }

    private void drawNeckPanel(Canvas c, RectF r) {
        mPaint.setColor(COL_WOOD);
        c.drawRoundRect(r, 14f, 14f, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2f);
        mPaint.setColor(COL_BORDER);
        c.drawRoundRect(r, 14f, 14f, mPaint);
        mPaint.setStyle(Paint.Style.FILL);

        LickTheory.LickDef lick = currentLick();
        int startFret = LickTheory.computeNeckWindowStartFret(lick, mKeyIdx);
        int numFrets = 8;
        float leftX = r.left + 42f;
        float rightX = r.right - 16f;
        float topPad = Math.max(26f, r.height() * 0.14f);
        float botPad = Math.max(26f, r.height() * 0.15f);
        float topY = r.top + topPad;
        float botY = r.bottom - botPad;
        float fretW = (rightX - leftX) / numFrets;
        float strH = (botY - topY) / 5f;

        // Fret wires, pearl position inlays & fret numbers
        for (int f = 0; f <= numFrets; f++) {
            float fx = leftX + f * fretW;
            int fretNum = startFret + f;
            mPaint.setColor(fretNum == 0 ? COL_GOLD : COL_BORDER);
            mPaint.setStrokeWidth(fretNum == 0 ? 5f : 2f);
            c.drawLine(fx, topY, fx, botY, mPaint);
            if (f < numFrets) {
                int dispFret = startFret + f + 1;
                float midX = fx + fretW * 0.5f;
                if (dispFret == 3 || dispFret == 5 || dispFret == 7 || dispFret == 9 || dispFret == 15) {
                    mPaint.setStyle(Paint.Style.FILL);
                    mPaint.setColor(0x33EAEFF8);
                    c.drawCircle(midX, (topY + botY) * 0.5f, Math.min(fretW, strH) * 0.20f, mPaint);
                } else if (dispFret == 12) {
                    mPaint.setStyle(Paint.Style.FILL);
                    mPaint.setColor(0x33EAEFF8);
                    float ir = Math.min(fretW, strH) * 0.18f;
                    c.drawCircle(midX, topY + strH * 1.5f, ir, mPaint);
                    c.drawCircle(midX, topY + strH * 3.5f, ir, mPaint);
                }
                mTextPaint.setColor(COL_MUTED);
                mTextPaint.setTextSize(Math.min(20f, botPad * 0.65f));
                mTextPaint.setTextAlign(Paint.Align.CENTER);
                c.drawText(dispFret + "fr", midX, r.bottom - botPad * 0.22f, mTextPaint);
            }
        }

        // 6 Guitar Strings + left-hand string labels (e, B, G, D, A, E)
        float strLabelTs = Math.min(24f, strH * 0.68f);
        for (int row = 0; row < 6; row++) {
            int strIdx = 5 - row;
            float sy = topY + row * strH;
            mPaint.setColor(row < 3 ? 0xFFB8C4D8 : 0xFFC8A064);
            mPaint.setStrokeWidth(1.5f + row * 0.40f);
            c.drawLine(leftX, sy, rightX, sy, mPaint);

            mTextPaint.setColor(COL_MUTED);
            mTextPaint.setTextSize(strLabelTs);
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            c.drawText(String.valueOf(LickTheory.STR_LABELS[strIdx]), r.left + 21f, sy + strLabelTs * 0.35f, mTextPaint);
        }

        // Ghost dots for non-active steps first, then active step on top
        float dotR = Math.min(fretW * 0.34f, strH * 0.44f);
        for (int pass = 0; pass < 2; pass++) {
            for (int s = 0; s < lick.numSteps; s++) {
                boolean isActive = (s == mCurStep);
                if ((pass == 0 && isActive) || (pass == 1 && !isActive)) continue;
                LickTheory.LickStep st = lick.steps[s];
                if (st.str < 0) continue;
                int actualFret = LickTheory.getActualFret(st, mKeyIdx);
                int relFret = actualFret - startFret;
                if (relFret < 0 || relFret > numFrets) continue;
                float cx = (relFret == 0) ? (leftX - dotR * 0.4f) : (leftX + (relFret - 0.5f) * fretW);
                float cy = topY + (5 - st.str) * strH;
                int fCol = FINGER_COLS[LickTheory.clamp(st.finger, 0, 4)];
                float rad = isActive ? dotR * 1.16f : dotR * 0.86f;

                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(isActive ? fCol : 0xCC161C28);
                c.drawCircle(cx, cy, rad, mPaint);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(isActive ? 3f : 2f);
                mPaint.setColor(isActive ? COL_TEXT : fCol);
                c.drawCircle(cx, cy, rad, mPaint);
                mPaint.setStyle(Paint.Style.FILL);

                String lbl = lick.isJazz7ths()
                        ? LickTheory.getJazzStepDegreeNum(lick, s, mKeyIdx)
                        : String.valueOf(st.finger);
                mTextPaint.setColor(isActive ? COL_BG : fCol);
                mTextPaint.setTextSize(rad * 1.05f);
                mTextPaint.setTextAlign(Paint.Align.CENTER);
                Paint.FontMetrics fm = mTextPaint.getFontMetrics();
                c.drawText(lbl, cx, cy - (fm.ascent + fm.descent) * 0.5f, mTextPaint);
            }
        }
    }

    private void drawTransportDock(Canvas c, RectF r, boolean verticalStack) {
        mPaint.setColor(COL_PANEL);
        c.drawRoundRect(r, 14f, 14f, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2f);
        mPaint.setColor(COL_BORDER);
        c.drawRoundRect(r, 14f, 14f, mPaint);
        mPaint.setStyle(Paint.Style.FILL);

        float pad = 10f;
        float gap = 10f;
        float rowH = (r.height() - pad * 2f - gap) * 0.5f;
        float r1Y = r.top + pad;
        float r2Y = r1Y + rowH + gap;
        float availW = r.width() - pad * 2f;

        // Row 1: Prominent PLAY / PAUSE button + [- 100 BPM +] stepper
        float playW = availW * 0.42f;
        float bpmW = availW - playW - gap;
        mRectPlayBtn.set(r.left + pad, r1Y, r.left + pad + playW, r1Y + rowH);
        drawPill(c, mRectPlayBtn, mPlaying ? "PAUSE" : "PLAY",
                mPlaying ? COL_CORAL : COL_GREEN, COL_BG, rowH * 0.40f);

        float bpmX = mRectPlayBtn.right + gap;
        float stepBtnW = bpmW * 0.26f;
        mRectBpmMinus.set(bpmX, r1Y, bpmX + stepBtnW, r1Y + rowH);
        mRectBpmPlus.set(bpmX + bpmW - stepBtnW, r1Y, bpmX + bpmW, r1Y + rowH);
        RectF bpmMid = new RectF(mRectBpmMinus.right + 6f, r1Y, mRectBpmPlus.left - 6f, r1Y + rowH);
        drawPill(c, mRectBpmMinus, "- 5", COL_PANEL_ALT, COL_CYAN, rowH * 0.36f);
        drawPill(c, bpmMid, mBpm + " BPM", COL_BG, COL_GOLD, rowH * 0.38f);
        drawPill(c, mRectBpmPlus, "+ 5", COL_PANEL_ALT, COL_CYAN, rowH * 0.36f);

        // Row 2: Prominent Speed-Builder LADDER selector + Sub-Range LOOP toggle
        float halfW = (availW - gap) * 0.5f;
        mRectLadderBtn.set(r.left + pad, r2Y, r.left + pad + halfW, r2Y + rowH);
        drawPill(c, mRectLadderBtn, "LADDER: " + LickTheory.LADDERS[mLadderIdx].shortLabel,
                COL_PANEL_ALT, mLadderIdx > 0 ? COL_GOLD : COL_TEXT, rowH * 0.34f);

        mRectLoopBtn.set(mRectLadderBtn.right + gap, r2Y, r.right - pad, r2Y + rowH);
        String loopTxt = mLoopSubRange ? "BEAT 1 ONLY" : "FULL BAR";
        drawPill(c, mRectLoopBtn, "LOOP: " + loopTxt,
                COL_PANEL_ALT, mLoopSubRange ? COL_CYAN : COL_GREEN, rowH * 0.34f);
    }

    private void drawHelpModal(Canvas c, int w, int h) {
        mPaint.setColor(0xD805070B);
        c.drawRect(0, 0, w, h, mPaint);

        float mw = w * 0.88f;
        float mh = h * 0.82f;
        RectF mr = new RectF((w - mw) * 0.5f, (h - mh) * 0.5f, (w + mw) * 0.5f, (h + mh) * 0.5f);
        mPaint.setColor(COL_PANEL);
        c.drawRoundRect(mr, 18f, 18f, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2.5f);
        mPaint.setColor(COL_CYAN);
        c.drawRoundRect(mr, 18f, 18f, mPaint);
        mPaint.setStyle(Paint.Style.FILL);

        mRectModalTab0.set(mr.left + 16f, mr.top + 14f, mr.left + 190f, mr.top + 56f);
        mRectModalTab1.set(mr.left + 200f, mr.top + 14f, mr.left + 390f, mr.top + 56f);
        mRectModalClose.set(mr.right - 110f, mr.top + 14f, mr.right - 16f, mr.top + 56f);

        drawPill(c, mRectModalTab0, "HOW TO USE", mHelpTab == 0 ? COL_CYAN : COL_PANEL_ALT,
                mHelpTab == 0 ? COL_BG : COL_TEXT, 18f);
        drawPill(c, mRectModalTab1, "JAZZ 7THS & SPEED", mHelpTab == 1 ? COL_CYAN : COL_PANEL_ALT,
                mHelpTab == 1 ? COL_BG : COL_TEXT, 18f);
        drawPill(c, mRectModalClose, "CLOSE", COL_CORAL, COL_BG, 18f);

        String[] lines = (mHelpTab == 0) ? new String[]{
            "1. SELECT CATEGORY & LICK: Browse 33 exercises across Pentatonic, Legato,",
            "   Alternate Picking, Blues/Rock, Spider Warmups, and Jazz 7th Arpeggios.",
            "2. 6-STRING TAB SEQUENCER: Tap any step column on the tablature grid to",
            "   audition that note, inspect its left-hand finger (1=Index .. 4=Pinky),",
            "   and check the picking badge (D=Down, U=Up, H=Hammer, P=Pull, /=Slide).",
            "3. NECK INSPECTOR: Tap any string/fret on the zoomed 8-fret guitar neck",
            "   to pluck that pitch directly through the 12-voice Karplus-Strong synth.",
            "4. TRANSPOSE TO ANY KEY: Tap KEY to shift all 33 exercises from E to Eb."
        } : new String[]{
            "1. SPEED-BUILDER BPM LADDER: Tap LADDER to auto-increment BPM (+2/+5/+10 BPM)",
            "   every 2, 4, or 8 bars so your picking speed builds effortlessly.",
            "2. SUB-RANGE LOOP: Toggle LOOP between FULL BAR and BEAT 1 to isolate",
            "   difficult 4-note picking cells before linking the full 16-step phrase.",
            "3. JAZZ 7THS (LICKS #26-#33): Displays diatonic Roman numerals (Imaj7..viim7b5)",
            "   and chord-tone intervals (1, 3, b3, 5, b5, 7, b7) with backing chords."
        };

        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setColor(COL_TEXT);
        mTextPaint.setTextSize(Math.min(22f, mh * 0.045f));
        float ty = mr.top + 96f;
        for (String line : lines) {
            c.drawText(line, mr.left + 24f, ty, mTextPaint);
            ty += Math.min(34f, mh * 0.068f);
        }
    }

    private void drawPill(Canvas c, RectF r, String text, int bgCol, int fgCol, float textSize) {
        mPaint.setColor(bgCol);
        c.drawRoundRect(r, 10f, 10f, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(1.5f);
        mPaint.setColor(COL_BORDER);
        c.drawRoundRect(r, 10f, 10f, mPaint);
        mPaint.setStyle(Paint.Style.FILL);

        mTextPaint.setColor(fgCol);
        float ts = Math.min(textSize, r.height() * 0.44f);
        mTextPaint.setTextSize(ts);
        float maxW = Math.max(12f, r.width() - 12f);
        float measured = mTextPaint.measureText(text);
        if (measured > maxW && measured > 0f) {
            ts = ts * (maxW / measured);
            mTextPaint.setTextSize(ts);
        }
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        c.drawText(text, r.centerX(), r.centerY() + ts * 0.35f, mTextPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
            return true;
        }
        float x = event.getX();
        float y = event.getY();

        if (mShowHelp) {
            if (mRectModalClose.contains(x, y)) {
                mShowHelp = false;
            } else if (mRectModalTab0.contains(x, y)) {
                mHelpTab = 0;
            } else if (mRectModalTab1.contains(x, y)) {
                mHelpTab = 1;
            } else {
                mShowHelp = false;
            }
            invalidate();
            return true;
        }

        if (mRectHelpPill.contains(x, y)) {
            mShowHelp = true;
            invalidate();
            return true;
        }
        if (mRectCatPrev.contains(x, y)) {
            mCatIdx = (mCatIdx + LickTheory.NUM_CATEGORIES - 1) % LickTheory.NUM_CATEGORIES;
            mLickInCat = 0;
            syncLickDefaults();
            previewCurrentStep();
            invalidate();
            return true;
        }
        if (mRectCatNext.contains(x, y)) {
            mCatIdx = (mCatIdx + 1) % LickTheory.NUM_CATEGORIES;
            mLickInCat = 0;
            syncLickDefaults();
            previewCurrentStep();
            invalidate();
            return true;
        }
        if (mRectLickPrev.contains(x, y)) {
            int cnt = LickTheory.CAT_COUNT[mCatIdx];
            mLickInCat = (mLickInCat + cnt - 1) % cnt;
            syncLickDefaults();
            previewCurrentStep();
            invalidate();
            return true;
        }
        if (mRectLickNext.contains(x, y)) {
            int cnt = LickTheory.CAT_COUNT[mCatIdx];
            mLickInCat = (mLickInCat + 1) % cnt;
            syncLickDefaults();
            previewCurrentStep();
            invalidate();
            return true;
        }
        if (mRectKeyPill.contains(x, y)) {
            mKeyIdx = (mKeyIdx + 1) % LickTheory.NUM_KEYS;
            previewCurrentStep();
            invalidate();
            return true;
        }
        if (mRectMixPill.contains(x, y)) {
            mMixMode = (mMixMode + 1) % LickTheory.MIX_COUNT;
            previewCurrentStep();
            invalidate();
            return true;
        }
        if (mRectPlayBtn.contains(x, y)) {
            mPlaying = !mPlaying;
            mLastTickMs = SystemClock.uptimeMillis();
            mStepAccumMs = 0.0;
            if (mPlaying) {
                triggerSequencerStep(mCurStep);
            } else if (mSynth != null) {
                mSynth.silenceAll();
            }
            invalidate();
            return true;
        }
        if (mRectBpmMinus.contains(x, y)) {
            mBpm = LickTheory.clamp(mBpm - 5, LickTheory.MIN_BPM, LickTheory.MAX_BPM);
            mBaseBpm = mBpm;
            invalidate();
            return true;
        }
        if (mRectBpmPlus.contains(x, y)) {
            mBpm = LickTheory.clamp(mBpm + 5, LickTheory.MIN_BPM, LickTheory.MAX_BPM);
            mBaseBpm = mBpm;
            invalidate();
            return true;
        }
        if (mRectLadderBtn.contains(x, y)) {
            mLadderIdx = (mLadderIdx + 1) % LickTheory.NUM_LADDERS;
            mBarsPlayed = 0;
            invalidate();
            return true;
        }
        if (mRectLoopBtn.contains(x, y)) {
            mLoopSubRange = !mLoopSubRange;
            mLoopStartStep = 0;
            mLoopEndStep = currentLick().stepsPerBeat - 1;
            invalidate();
            return true;
        }
        if (mRectTabPanel.contains(x, y)) {
            LickTheory.LickDef lick = currentLick();
            float leftMargin = mRectTabPanel.left + 42f;
            float gridW = Math.max(10f, (mRectTabPanel.right - 14f) - leftMargin);
            int step = (int) ((x - leftMargin) / (gridW / lick.numSteps));
            mCurStep = LickTheory.clamp(step, 0, lick.numSteps - 1);
            previewCurrentStep();
            invalidate();
            return true;
        }
        if (mRectNeckPanel.contains(x, y) && mSynth != null) {
            LickTheory.LickDef lick = currentLick();
            int startFret = LickTheory.computeNeckWindowStartFret(lick, mKeyIdx);
            float leftX = mRectNeckPanel.left + 42f;
            float rightX = mRectNeckPanel.right - 16f;
            float topPad = Math.max(26f, mRectNeckPanel.height() * 0.14f);
            float botPad = Math.max(26f, mRectNeckPanel.height() * 0.15f);
            float topY = mRectNeckPanel.top + topPad;
            float botY = mRectNeckPanel.bottom - botPad;
            int relFret = LickTheory.clamp((int) (((x - leftX) / (rightX - leftX)) * 8f) + 1, 0, 8);
            int row = LickTheory.clamp(Math.round(((y - topY) / (botY - topY)) * 5f), 0, 5);
            int strIdx = 5 - row;
            mSynth.pluckFretboardNote(strIdx, startFret + relFret);
            invalidate();
            return true;
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE || keyCode == KeyEvent.KEYCODE_ENTER) {
            mPlaying = !mPlaying;
            mLastTickMs = SystemClock.uptimeMillis();
            invalidate();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
