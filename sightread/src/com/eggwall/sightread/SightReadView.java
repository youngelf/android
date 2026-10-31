package com.eggwall.sightread;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

import com.eggwall.sightread.SightReadTheory.KeySigDef;
import com.eggwall.sightread.SightReadTheory.NeckPosition;
import com.eggwall.sightread.SightReadTheory.SightPiece;
import com.eggwall.sightread.SightReadTheory.StaffNote;

import java.util.List;

/**
 * Custom full-screen Canvas view rendering the SightRead engraved Treble Clef musical staff,
 * interactive 6-string guitar fretboard, transport controls, and Help Modal.
 */
public class SightReadView extends View {

    // Studio & Sheet-Music Palette
    private static final int COL_BG_PAPER      = 0xFFFAF6EE;
    private static final int COL_HEADER_BG     = 0xFF1E242E;
    private static final int COL_CONTROL_BG    = 0xFFF2ECE0;
    private static final int COL_CARD_BG       = 0xFFFFFDF9;
    private static final int COL_INK_DARK      = 0xFF181C22;
    private static final int COL_STAFF_LINE    = 0xFF424752;
    private static final int COL_GOLD_ACCENT   = 0xFFD49B28;
    private static final int COL_GOLD_SOFT     = 0x38D49B28;
    private static final int COL_GREEN_OK      = 0xFF228B55;
    private static final int COL_RED_ERR       = 0xFFD8432E;
    private static final int COL_BLUE_INFO     = 0xFF2D6DA3;
    private static final int COL_NECK_WOOD     = 0xFF3A2616;
    private static final int COL_NECK_BORDER   = 0xFF23150B;
    private static final int COL_FRET_WIRE     = 0xFFC0B6A8;
    private static final int COL_NUT_BONE      = 0xFFEDE3D2;
    private static final int COL_STRING_STEEL  = 0xFFDCDCDC;
    private static final int COL_STRING_BRONZE = 0xFFD4AF7A;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path mPath = new Path();
    private final RectF mRect = new RectF();

    private final SightReadSynthEngine mSynth = new SightReadSynthEngine();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final SightPiece mPiece = new SightPiece();

    private int mKeyIdx = 0;
    private int mPosIdx = 0;
    private int mPracticeMode = SightReadTheory.MODE_ETUDE;
    private int mExerciseNum = 1;
    private int mBpm = SightReadTheory.DEFAULT_BPM;
    private boolean mShowHints = true;
    private boolean mShowHelp = false;
    private boolean mShowExercisePicker = false;
    private boolean mIsPlaying = false;

    private int mCurrentIdx = 0;
    private int mScoreHits = 0;
    private int mScoreAttempts = 0;
    private int mStreak = 0;
    private String mStatusText = "Tap the matching string & fret on the guitar neck to sight-read!";
    private int mStatusColor = COL_BLUE_INFO;

    private int mFlashString = -1;
    private int mFlashFret = -1;
    private boolean mFlashCorrect = true;
    private long mFlashUntilMs = 0L;

    // Interactive Hit Regions
    private final RectF mBtnPlay = new RectF();
    private final RectF mBtnMute = new RectF();
    private final RectF mBtnHelp = new RectF();
    private final RectF[] mCtrlBtns = new RectF[7];
    private final RectF[] mExerciseBtns = new RectF[SightReadTheory.NUM_EXERCISES];
    private final RectF mStaffCard = new RectF();
    private final RectF mNeckRect = new RectF();
    private final RectF mModalRect = new RectF();
    private final RectF mModalCloseBtn = new RectF();
    private final RectF mModalViewportRect = new RectF();
    private float mHelpScrollY = 0f;
    private float mHelpMaxScrollY = 0f;
    private float mHelpTouchStartY = 0f;
    private float mHelpLastTouchY = 0f;
    private boolean mHelpDragging = false;

    private int mStepEighthCursor = 0;
    private final Runnable mTickRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mIsPlaying) return;
            onTransportEighthTick();
            long eighthMs = Math.max(90L, (30000L) / Math.max(40, mBpm));
            mHandler.postDelayed(this, eighthMs);
        }
    };

    public SightReadView(Context context) {
        super(context);
        for (int i = 0; i < mCtrlBtns.length; i++) {
            mCtrlBtns[i] = new RectF();
        }
        for (int i = 0; i < mExerciseBtns.length; i++) {
            mExerciseBtns[i] = new RectF();
        }
        loadPiece();
    }

    public void configureDemoState(int clef, int key, int pos, int ex, int mode, boolean showHelp) {
        stopTransport();
        mKeyIdx = ((key % SightReadTheory.NUM_KEYS) + SightReadTheory.NUM_KEYS) % SightReadTheory.NUM_KEYS;
        mPosIdx = ((pos % SightReadTheory.NUM_POSITIONS) + SightReadTheory.NUM_POSITIONS) % SightReadTheory.NUM_POSITIONS;
        mExerciseNum = Math.max(1, Math.min(99, ex));
        mPracticeMode = ((mode % SightReadTheory.NUM_MODES) + SightReadTheory.NUM_MODES) % SightReadTheory.NUM_MODES;
        mShowHelp = showHelp;
        mShowExercisePicker = false;
        loadPiece();
        invalidate();
    }

    public void onHostPause() {
        stopTransport();
    }

    public void onHostDestroy() {
        stopTransport();
        mSynth.shutdown();
    }

    private void loadPiece() {
        SightReadTheory.generateSightPiece(mPiece, mExerciseNum, mPosIdx, mKeyIdx);
        mCurrentIdx = 0;
        mStepEighthCursor = 0;
        if (mPiece.numNotes > 0) {
            StaffNote first = mPiece.notes[0];
            mStatusText = "Target: " + first.label + "  |  " + SightReadTheory.levelDescription(mPiece.level);
            mStatusColor = COL_BLUE_INFO;
        }
    }

    private void startTransport() {
        if (mIsPlaying) return;
        mIsPlaying = true;
        mStepEighthCursor = 0;
        mCurrentIdx = 0;
        mHandler.removeCallbacks(mTickRunnable);
        mTickRunnable.run();
        invalidate();
    }

    private void stopTransport() {
        mIsPlaying = false;
        mHandler.removeCallbacks(mTickRunnable);
        invalidate();
    }

    private void onTransportEighthTick() {
        if (mPiece.numNotes <= 0) return;
        int totalEighths = mPiece.numBars * 8;
        int curEighth = mStepEighthCursor % totalEighths;
        if (curEighth % 2 == 0) {
            mSynth.playMetronomeClick((curEighth % 8) == 0);
        }
        for (int i = 0; i < mPiece.numNotes; i++) {
            StaffNote n = mPiece.notes[i];
            int noteEighth = n.barIdx * 8 + n.eighthInBar;
            if (noteEighth == curEighth) {
                mCurrentIdx = i;
                int durMs = (int) ((30000L * n.durEighths) / Math.max(40, mBpm));
                mSynth.playNote(n.midi, durMs);
                mStatusText = "Playing " + n.label + " (Str " + (6 - n.str) + ", Fret " + n.fret + ")";
                mStatusColor = COL_GOLD_ACCENT;
                break;
            }
        }
        mStepEighthCursor++;
        if (mStepEighthCursor >= totalEighths) {
            if (mPracticeMode == SightReadTheory.MODE_SCROLL) {
                mExerciseNum = (mExerciseNum % SightReadTheory.NUM_EXERCISES) + 1;
                loadPiece();
            } else {
                mStepEighthCursor = 0;
            }
        }
        invalidate();
    }

    private String[] buildControlLabels() {
        return new String[] {
            "KEY: " + SightReadTheory.KEYS[mKeyIdx].badge,
            SightReadTheory.POSITIONS[mPosIdx].shortName,
            "MODE: " + SightReadTheory.MODE_NAMES[mPracticeMode],
            SightReadTheory.exerciseLabel(mExerciseNum) + " \u25BE",
            "BPM: " + mBpm + " +",
            "HINT: " + (mShowHints ? "ON" : "OFF"),
            "NEW PIECE"
        };
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float dp = getResources().getDisplayMetrics().density;
        boolean landscape = w > h * 1.15f;

        canvas.drawColor(COL_BG_PAPER);

        float headerH = Math.max(44f * dp, h * (landscape ? 0.115f : 0.075f));
        drawHeader(canvas, w, headerH, dp, landscape);

        float ctrlH = landscape ? 0f : Math.max(72f * dp, h * 0.12f);
        if (!landscape) {
            drawPortraitControls(canvas, 0, headerH, w, ctrlH, dp);
        }

        float bannerY = headerH + ctrlH;
        float bannerH = Math.max(26f * dp, h * 0.046f);
        drawStatusBanner(canvas, bannerY, w, bannerH, dp);

        float contentTop = bannerY + bannerH + 6f * dp;
        float contentH = h - contentTop - 8f * dp;

        // 30%+ more vertical space allocated to the fretboard neck
        float neckH = contentH * (landscape ? 0.45f : 0.351f);
        float staffH = contentH - neckH - 10f * dp;
        mStaffCard.set(10f * dp, contentTop, w - 10f * dp, contentTop + staffH);
        drawStaffCard(canvas, mStaffCard, dp);

        float neckTop = mStaffCard.bottom + 8f * dp;
        mNeckRect.set(10f * dp, neckTop, w - 10f * dp, neckTop + neckH);
        drawGuitarNeck(canvas, mNeckRect, dp);

        if (mFlashUntilMs > System.currentTimeMillis()) {
            postInvalidateDelayed(40);
        }

        if (mShowExercisePicker) {
            drawExercisePickerModal(canvas, w, h, dp, landscape);
        } else if (mShowHelp) {
            drawHelpModal(canvas, w, h, dp);
        }
    }

    private void drawHeader(Canvas c, float w, float h, float dp, boolean landscape) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(COL_HEADER_BG);
        c.drawRect(0, 0, w, h, mPaint);

        float btnH = h * (landscape ? 0.72f : 0.64f);
        float btnY = (h - btnH) * 0.5f;
        float helpW = (landscape ? 30f : 36f) * dp;

        // Top-left '?' help pill
        mBtnHelp.set(8f * dp, btnY, 8f * dp + helpW, btnY + btnH);
        drawPillButton(c, mBtnHelp, "?", COL_GOLD_ACCENT, COL_HEADER_BG, (landscape ? 13f : 14f) * dp);

        float muteW = (landscape ? 54f : 66f) * dp;
        float playW = (landscape ? 54f : 70f) * dp;

        mBtnMute.set(w - muteW - 8f * dp, btnY, w - 8f * dp, btnY + btnH);
        mBtnPlay.set(mBtnMute.left - playW - 5f * dp, btnY, mBtnMute.left - 5f * dp, btnY + btnH);

        drawPillButton(c, mBtnPlay, mIsPlaying ? "STOP" : "PLAY",
                mIsPlaying ? COL_RED_ERR : COL_GREEN_OK, 0xFFFFFFFF, (landscape ? 10f : 11f) * dp);
        drawPillButton(c, mBtnMute, mSynth.isMuted() ? "MUTED" : "SOUND",
                mSynth.isMuted() ? 0xFF454C59 : COL_BLUE_INFO, 0xFFFFFFFF, (landscape ? 9.5f : 10.5f) * dp);

        float titleX = mBtnHelp.right + 8f * dp;
        mTextPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        mTextPaint.setColor(0xFFFDF8EE);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setTextSize((landscape ? 14f : 16.5f) * dp);

        if (landscape) {
            AppI18n.drawText(c, "SIGHTREAD", titleX, h * 0.62f, mTextPaint);
            float titleW = mTextPaint.measureText("SIGHTREAD");
            float ctrlLeft = titleX + titleW + 8f * dp;
            float ctrlRight = mBtnPlay.left - 6f * dp;
            float availW = Math.max(100f * dp, ctrlRight - ctrlLeft);

            String[] labels = buildControlLabels();
            float[] weights = { 0.92f, 1.08f, 0.95f, 1.65f, 0.84f, 0.80f, 0.92f };
            float totalWeight = 0f;
            for (float wt : weights) totalWeight += wt;

            float gap = 4f * dp;
            float netW = availW - gap * (labels.length - 1);
            float curX = ctrlLeft;
            for (int i = 0; i < labels.length; i++) {
                float bw = netW * (weights[i] / totalWeight);
                mCtrlBtns[i].set(curX, btnY, curX + bw, btnY + btnH);
                int bg = (i == 6) ? COL_GOLD_ACCENT
                       : (i == 5 && mShowHints) ? 0xFFDCEBE2
                       : (i == 3) ? 0xFFFFF3D1
                       : COL_CARD_BG;
                int fg = (i == 6) ? COL_HEADER_BG : COL_INK_DARK;
                drawPillButton(c, mCtrlBtns[i], labels[i], bg, fg, 8.8f * dp);
                curX += bw + gap;
            }
        } else {
            AppI18n.drawText(c, "SIGHTREAD", titleX, h * 0.46f, mTextPaint);
            mTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
            mTextPaint.setColor(0xFFB8C0CC);
            mTextPaint.setTextSize(10.5f * dp);
            String sub = "TREBLE CLEF • "
                    + SightReadTheory.KEYS[mKeyIdx].badge + " • "
                    + SightReadTheory.POSITIONS[mPosIdx].shortName;
            AppI18n.drawText(c, sub, titleX, h * 0.80f, mTextPaint);
        }
    }

    private void drawPortraitControls(Canvas c, float x, float y, float w, float h, float dp) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(COL_CONTROL_BG);
        c.drawRect(x, y, x + w, y + h, mPaint);

        String[] labels = buildControlLabels();
        int cols = 4;
        int rows = 2;
        float pad = 5f * dp;
        float cellW = (w - pad * (cols + 1)) / cols;
        float cellH = (h - pad * (rows + 1)) / rows;

        // Row 0: KEY (0), POS (1), MODE (2), BPM (4)
        int[] row0Indices = { 0, 1, 2, 4 };
        float row0Y = y + pad;
        for (int col = 0; col < 4; col++) {
            int idx = row0Indices[col];
            float bx = x + pad + col * (cellW + pad);
            mCtrlBtns[idx].set(bx, row0Y, bx + cellW, row0Y + cellH);
            drawPillButton(c, mCtrlBtns[idx], labels[idx], COL_CARD_BG, COL_INK_DARK, 9.8f * dp);
        }

        // Row 1: Exercise Name Picker (3, spanning 2 columns), HINT (5), NEW PIECE (6)
        float row1Y = y + pad + cellH + pad;
        float exW = cellW * 2f + pad;
        mCtrlBtns[3].set(x + pad, row1Y, x + pad + exW, row1Y + cellH);
        drawPillButton(c, mCtrlBtns[3], labels[3], 0xFFFFF3D1, COL_INK_DARK, 10.2f * dp);

        float hintX = mCtrlBtns[3].right + pad;
        mCtrlBtns[5].set(hintX, row1Y, hintX + cellW, row1Y + cellH);
        drawPillButton(c, mCtrlBtns[5], labels[5], mShowHints ? 0xFFDCEBE2 : COL_CARD_BG, COL_INK_DARK, 9.8f * dp);

        float newX = mCtrlBtns[5].right + pad;
        mCtrlBtns[6].set(newX, row1Y, newX + cellW, row1Y + cellH);
        drawPillButton(c, mCtrlBtns[6], labels[6], COL_GOLD_ACCENT, COL_HEADER_BG, 9.8f * dp);
    }

    private void drawStatusBanner(Canvas c, float y, float w, float h, float dp) {
        mPaint.setColor(0xFFE6DEC8);
        c.drawRect(0, y, w, y + h, mPaint);

        mTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        mTextPaint.setTextSize(11f * dp);
        mTextPaint.setColor(mStatusColor);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        AppI18n.drawText(c, mStatusText, 12f * dp, y + h * 0.68f, mTextPaint);

        int pct = mScoreAttempts > 0 ? (100 * mScoreHits / mScoreAttempts) : 100;
        String scoreStr = "Score: " + mScoreHits + "/" + mScoreAttempts + " (" + pct + "%)  Streak: " + mStreak;
        mTextPaint.setColor(COL_INK_DARK);
        mTextPaint.setTextAlign(Paint.Align.RIGHT);
        AppI18n.drawText(c, scoreStr, w - 12f * dp, y + h * 0.68f, mTextPaint);
    }

    private void drawStaffCard(Canvas c, RectF card, float dp) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(COL_CARD_BG);
        c.drawRoundRect(card, 8f * dp, 8f * dp, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(1.2f * dp);
        mPaint.setColor(0xFFD5CCB8);
        c.drawRoundRect(card, 8f * dp, 8f * dp, mPaint);

        // Single Treble Clef staff: steps 2, 4, 6, 8, 10 (step 6 = middle line B4)
        float halfSpace = Math.min(7.5f * dp, card.height() / 19f);
        float centerStepY = card.centerY() + 6.0f * halfSpace;

        float staffLeft = card.left + 16f * dp;
        float staffRight = card.right - 14f * dp;

        // Draw the 5 Treble Staff Lines (E4=2, G4=4, B4=6, D5=8, F5=10)
        mPaint.setColor(COL_STAFF_LINE);
        mPaint.setStrokeWidth(1.25f * dp);
        for (int step : SightReadTheory.TREBLE_LINE_STEPS) {
            float ly = centerStepY - step * halfSpace;
            c.drawLine(staffLeft, ly, staffRight, ly, mPaint);
        }

        // Left staff barline
        float topY = centerStepY - 10f * halfSpace;
        float botY = centerStepY - 2f * halfSpace;
        mPaint.setStrokeWidth(2.2f * dp);
        c.drawLine(staffLeft, topY, staffLeft, botY, mPaint);

        // Engraved Vector Treble Clef (G-Clef) anchored on the E4 line (botY) and G4 spiral (step=4)
        drawTrebleClef(c, staffLeft + 4f * dp, botY, halfSpace);

        // Key Signature on Treble Staff
        KeySigDef keyDef = SightReadTheory.KEYS[mKeyIdx];
        int sf = keyDef.sharpsOrFlats;
        float accX = staffLeft + 34f * dp;
        mTextPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        mTextPaint.setColor(COL_INK_DARK);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setTextSize(halfSpace * 3.1f);
        int count = Math.abs(sf);
        for (int i = 0; i < count; i++) {
            String sym = sf > 0 ? "\u266F" : "\u266D";
            int st = sf > 0 ? SightReadTheory.TREBLE_SHARP_STEPS[i] : SightReadTheory.TREBLE_FLAT_STEPS[i];
            AppI18n.drawText(c, sym, accX + i * 7.5f * dp, centerStepY - st * halfSpace + halfSpace, mTextPaint);
        }

        // 4/4 Time Signature on Treble Staff
        float timeX = accX + count * 7.5f * dp + 8f * dp;
        mTextPaint.setTextSize(halfSpace * 3.6f);
        AppI18n.drawText(c, "4", timeX, centerStepY - 6.2f * halfSpace, mTextPaint);
        AppI18n.drawText(c, "4", timeX, centerStepY - 2.2f * halfSpace, mTextPaint);

        float notesStart = timeX + 22f * dp;
        float notesEnd = staffRight - 10f * dp;
        float barMidX = (notesStart + notesEnd) * 0.5f;

        // Measure barline & end double barline
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(COL_STAFF_LINE);
        mPaint.setStrokeWidth(1.4f * dp);
        c.drawLine(barMidX, topY, barMidX, botY, mPaint);
        c.drawLine(staffRight - 4.5f * dp, topY, staffRight - 4.5f * dp, botY, mPaint);
        mPaint.setStrokeWidth(2.8f * dp);
        c.drawLine(staffRight, topY, staffRight, botY, mPaint);

        // Draw Notes on Treble Staff
        int nNotes = mPiece.numNotes;
        for (int i = 0; i < nNotes; i++) {
            StaffNote n = mPiece.notes[i];
            float barStart = (n.barIdx == 0) ? notesStart : barMidX + 8f * dp;
            float barSpan = ((n.barIdx == 0) ? barMidX : notesEnd) - barStart - 8f * dp;
            float nx = barStart + (n.eighthInBar + 0.6f) * (barSpan / 8f);

            int vStep = n.diatonicStep;
            float ny = centerStepY - vStep * halfSpace;

            // Active Note Highlight Column
            if (i == mCurrentIdx) {
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(COL_GOLD_SOFT);
                float hlTop = Math.min(topY - 12f * dp, ny - 14f * dp);
                float hlBot = Math.max(botY + 14f * dp, ny + 14f * dp);
                mRect.set(nx - 11f * dp, hlTop, nx + 11f * dp, hlBot);
                c.drawRoundRect(mRect, 6f * dp, 6f * dp, mPaint);
            }

            // Treble Staff Ledger lines (below E4 step=2 or above F5 step=10)
            List<Integer> ledgers = SightReadTheory.computeLedgerSteps(vStep, false);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(1.35f * dp);
            mPaint.setColor(COL_INK_DARK);
            for (int lStep : ledgers) {
                float ly = centerStepY - lStep * halfSpace;
                c.drawLine(nx - 8.5f * dp, ly, nx + 8.5f * dp, ly, mPaint);
            }

            // Notehead & Stem
            int noteCol = (i < mCurrentIdx) ? COL_GREEN_OK
                        : (i == mCurrentIdx) ? COL_GOLD_ACCENT : COL_INK_DARK;
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(noteCol);
            float rx = halfSpace * 1.32f;
            float ry = halfSpace * 0.96f;
            mRect.set(nx - rx, ny - ry, nx + rx, ny + ry);
            c.drawOval(mRect, mPaint);

            boolean stemUp = vStep < 6;
            float stemX = stemUp ? (nx + rx - 1f * dp) : (nx - rx + 1f * dp);
            float stemEndY = stemUp ? (ny - halfSpace * 6.2f) : (ny + halfSpace * 6.2f);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(1.65f * dp);
            c.drawLine(stemX, ny, stemX, stemEndY, mPaint);

            if (n.durEighths == 1) {
                mPaint.setStrokeWidth(2.6f * dp);
                c.drawLine(stemX, stemEndY, stemX + 6f * dp,
                        stemEndY + (stemUp ? 5f * dp : -5f * dp), mPaint);
            }
        }
    }

    /**
     * Renders an authentic calligraphic Treble G-Clef vector outline anchored at {@code e4LineY}
     * (the bottom line of the 5-line Treble Staff).
     */
    private void drawTrebleClef(Canvas c, float leftX, float e4LineY, float halfSpace) {
        float s = halfSpace / 80f;
        float ox = leftX - 120f * s;
        float oy = e4LineY;
        Path p = mPath;
        p.reset();
        p.setFillType(Path.FillType.WINDING);
        p.moveTo(ox + 434f * s, oy - 2f * s);
        p.quadTo(ox + 464f * s, oy - -103f * s, ox + 464f * s, oy - -170f * s);
        p.quadTo(ox + 464f * s, oy - -223f * s, ox + 427.0f * s, oy - -257.0f * s);
        p.quadTo(ox + 390f * s, oy - -291f * s, ox + 337f * s, oy - -291f * s);
        p.quadTo(ox + 287f * s, oy - -291f * s, ox + 250.0f * s, oy - -261.5f * s);
        p.quadTo(ox + 213f * s, oy - -232f * s, ox + 213f * s, oy - -190f * s);
        p.quadTo(ox + 213f * s, oy - -160f * s, ox + 233.5f * s, oy - -133.5f * s);
        p.quadTo(ox + 254f * s, oy - -107f * s, ox + 283.5f * s, oy - -107.0f * s);
        p.quadTo(ox + 313f * s, oy - -107f * s, ox + 331.5f * s, oy - -128.5f * s);
        p.quadTo(ox + 350f * s, oy - -150f * s, ox + 350f * s, oy - -178f * s);
        p.quadTo(ox + 350f * s, oy - -240f * s, ox + 280f * s, oy - -240f * s);
        p.quadTo(ox + 298f * s, oy - -268f * s, ox + 338f * s, oy - -268f * s);
        p.quadTo(ox + 353f * s, oy - -268f * s, ox + 368.5f * s, oy - -263.5f * s);
        p.quadTo(ox + 384f * s, oy - -259f * s, ox + 401.0f * s, oy - -248.0f * s);
        p.quadTo(ox + 418f * s, oy - -237f * s, ox + 428.5f * s, oy - -213.5f * s);
        p.quadTo(ox + 439f * s, oy - -190f * s, ox + 439f * s, oy - -157f * s);
        p.quadTo(ox + 439f * s, oy - -136f * s, ox + 411f * s, oy - -6f * s);
        p.quadTo(ox + 389f * s, oy - -12f * s, ox + 356f * s, oy - -12f * s);
        p.quadTo(ox + 259f * s, oy - -12f * s, ox + 189.5f * s, oy - 60.0f * s);
        p.quadTo(ox + 120f * s, oy - 132f * s, ox + 120f * s, oy - 232f * s);
        p.quadTo(ox + 120f * s, oy - 267f * s, ox + 131.5f * s, oy - 303.0f * s);
        p.quadTo(ox + 143f * s, oy - 339f * s, ox + 157.5f * s, oy - 366.5f * s);
        p.quadTo(ox + 172f * s, oy - 394f * s, ox + 200.5f * s, oy - 428.5f * s);
        p.quadTo(ox + 229f * s, oy - 463f * s, ox + 248.5f * s, oy - 483.5f * s);
        p.quadTo(ox + 268f * s, oy - 504f * s, ox + 303f * s, oy - 539f * s);
        p.quadTo(ox + 280f * s, oy - 621f * s, ox + 280f * s, oy - 689f * s);
        p.quadTo(ox + 280f * s, oy - 779f * s, ox + 313.0f * s, oy - 839.5f * s);
        p.quadTo(ox + 346f * s, oy - 900f * s, ox + 379f * s, oy - 900f * s);
        p.quadTo(ox + 389f * s, oy - 900f * s, ox + 401.5f * s, oy - 887.0f * s);
        p.quadTo(ox + 414f * s, oy - 874f * s, ox + 426.0f * s, oy - 851.0f * s);
        p.quadTo(ox + 438f * s, oy - 828f * s, ox + 446.5f * s, oy - 790.5f * s);
        p.quadTo(ox + 455f * s, oy - 753f * s, ox + 455f * s, oy - 710f * s);
        p.quadTo(ox + 455f * s, oy - 551f * s, ox + 342f * s, oy - 447f * s);
        p.lineTo(ox + 368f * s, oy - 329f * s);
        p.quadTo(ox + 384f * s, oy - 332f * s, ox + 397f * s, oy - 332f * s);
        p.quadTo(ox + 458f * s, oy - 332f * s, ox + 500.0f * s, oy - 282.5f * s);
        p.quadTo(ox + 542f * s, oy - 233f * s, ox + 542f * s, oy - 162f * s);
        p.quadTo(ox + 542f * s, oy - 44f * s, ox + 434f * s, oy - 2f * s);
        p.close();
        p.moveTo(ox + 426f * s, oy - 746f * s);
        p.quadTo(ox + 426f * s, oy - 801f * s, ox + 394f * s, oy - 801f * s);
        p.quadTo(ox + 358f * s, oy - 801f * s, ox + 333.0f * s, oy - 748.0f * s);
        p.quadTo(ox + 308f * s, oy - 695f * s, ox + 308f * s, oy - 630f * s);
        p.quadTo(ox + 308f * s, oy - 588f * s, ox + 321f * s, oy - 557f * s);
        p.quadTo(ox + 359f * s, oy - 580f * s, ox + 392.5f * s, oy - 639.5f * s);
        p.quadTo(ox + 426f * s, oy - 699f * s, ox + 426f * s, oy - 746f * s);
        p.close();
        p.moveTo(ox + 498f * s, oy - 128f * s);
        p.quadTo(ox + 498f * s, oy - 183f * s, ox + 466.0f * s, oy - 216.0f * s);
        p.quadTo(ox + 434f * s, oy - 249f * s, ox + 383f * s, oy - 249f * s);
        p.lineTo(ox + 428f * s, oy - 23f * s);
        p.quadTo(ox + 498f * s, oy - 52f * s, ox + 498f * s, oy - 128f * s);
        p.close();
        p.moveTo(ox + 407f * s, oy - 17f * s);
        p.lineTo(ox + 361f * s, oy - 247f * s);
        p.quadTo(ox + 334f * s, oy - 241f * s, ox + 311.5f * s, oy - 214.0f * s);
        p.quadTo(ox + 289f * s, oy - 187f * s, ox + 289f * s, oy - 158f * s);
        p.quadTo(ox + 289f * s, oy - 143f * s, ox + 295.0f * s, oy - 128.5f * s);
        p.quadTo(ox + 301f * s, oy - 114f * s, ox + 309.5f * s, oy - 104.0f * s);
        p.quadTo(ox + 318f * s, oy - 94f * s, ox + 327.0f * s, oy - 86.0f * s);
        p.quadTo(ox + 336f * s, oy - 78f * s, ox + 342.0f * s, oy - 74.5f * s);
        p.quadTo(ox + 348f * s, oy - 71f * s, ox + 348f * s, oy - 71f * s);
        p.lineTo(ox + 340f * s, oy - 66f * s);
        p.quadTo(ox + 307f * s, oy - 75f * s, ox + 277.5f * s, oy - 106.0f * s);
        p.quadTo(ox + 248f * s, oy - 137f * s, ox + 248f * s, oy - 184f * s);
        p.quadTo(ox + 248f * s, oy - 231f * s, ox + 277.5f * s, oy - 270.5f * s);
        p.quadTo(ox + 307f * s, oy - 310f * s, ox + 343f * s, oy - 323f * s);
        p.lineTo(ox + 325f * s, oy - 430f * s);
        p.quadTo(ox + 168f * s, oy - 299f * s, ox + 168f * s, oy - 177f * s);
        p.quadTo(ox + 168f * s, oy - 104f * s, ox + 223.0f * s, oy - 55.0f * s);
        p.quadTo(ox + 278f * s, oy - 6f * s, ox + 348f * s, oy - 6f * s);
        p.quadTo(ox + 365f * s, oy - 6f * s, ox + 407f * s, oy - 17f * s);
        p.close();

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(COL_INK_DARK);
        c.drawPath(p, mPaint);
    }

    private void drawGuitarNeck(Canvas c, RectF neck, float dp) {
        NeckPosition pos = SightReadTheory.POSITIONS[mPosIdx];
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(COL_NECK_WOOD);
        c.drawRoundRect(neck, 8f * dp, 8f * dp, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2f * dp);
        mPaint.setColor(COL_NECK_BORDER);
        c.drawRoundRect(neck, 8f * dp, 8f * dp, mPaint);

        float colW = neck.width() / 5f; // Col 0 = Open string, Cols 1..4 = Frets baseFret..baseFret+3
        float vertPad = 5f * dp;
        float rowH = (neck.height() - 2f * vertPad) / 6f;

        // Nut or position divider on right of column 0
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(pos.minFret == 0 ? COL_NUT_BONE : COL_FRET_WIRE);
        c.drawRect(neck.left + colW - 3f * dp, neck.top, neck.left + colW + 2f * dp, neck.bottom, mPaint);

        // Fret wires & pearl inlay dots
        for (int col = 0; col < 5; col++) {
            int fretNum = (col == 0) ? 0 : (pos.baseFret + col - 1);
            float cx = neck.left + (col + 0.5f) * colW;
            if (col > 0) {
                float wireX = neck.left + (col + 1) * colW;
                if (col < 4) {
                    mPaint.setColor(COL_FRET_WIRE);
                    c.drawRect(wireX - 1f * dp, neck.top, wireX + 1f * dp, neck.bottom, mPaint);
                }
                // Pearl inlay dots on frets 3, 5, 7, 9, 12
                float midY = neck.top + vertPad + 3f * rowH;
                if (fretNum == 3 || fretNum == 5 || fretNum == 7 || fretNum == 9) {
                    mPaint.setColor(0x44FFFDF5);
                    c.drawCircle(cx, midY, 5.5f * dp, mPaint);
                } else if (fretNum == 12) {
                    mPaint.setColor(0x44FFFDF5);
                    c.drawCircle(cx, neck.top + vertPad + rowH * 2f, 5f * dp, mPaint);
                    c.drawCircle(cx, neck.top + vertPad + rowH * 4f, 5f * dp, mPaint);
                }
            }
        }

        StaffNote target = (mCurrentIdx < mPiece.numNotes) ? mPiece.notes[mCurrentIdx] : null;
        boolean flashActive = mFlashUntilMs > System.currentTimeMillis();

        // Draw 6 strings (top row = 1st string high 'e', str=5; bottom row = 6th string low 'E', str=0)
        for (int row = 0; row < 6; row++) {
            int str = 5 - row;
            float sy = neck.top + vertPad + (row + 0.5f) * rowH;

            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth((1.3f + (5 - str) * 0.40f) * dp);
            mPaint.setColor(str >= 3 ? COL_STRING_STEEL : COL_STRING_BRONZE);
            c.drawLine(neck.left + 8f * dp, sy, neck.right - 8f * dp, sy, mPaint);

            // String name badge on left
            mTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
            mTextPaint.setColor(0xFFFFF6E5);
            mTextPaint.setTextSize(11.5f * dp);
            mTextPaint.setTextAlign(Paint.Align.LEFT);
            AppI18n.drawText(c, String.valueOf(SightReadTheory.STRING_LABELS[str]), neck.left + 6f * dp, sy - 3f * dp, mTextPaint);

            for (int col = 0; col < 5; col++) {
                int fretNum = (col == 0) ? 0 : (pos.baseFret + col - 1);
                float cx = neck.left + (col + 0.5f) * colW;

                boolean isTarget = target != null && target.str == str && target.fret == fretNum;
                boolean isFlashed = flashActive && mFlashString == str && mFlashFret == fretNum;

                if (isFlashed || (isTarget && mShowHints && mPracticeMode != SightReadTheory.MODE_QUIZ)) {
                    float r = Math.min(rowH * 0.40f, 13.5f * dp);
                    mPaint.setStyle(Paint.Style.FILL);
                    mPaint.setColor(isFlashed ? (mFlashCorrect ? COL_GREEN_OK : COL_RED_ERR) : COL_GOLD_ACCENT);
                    c.drawCircle(cx, sy, r, mPaint);
                    mPaint.setStyle(Paint.Style.STROKE);
                    mPaint.setStrokeWidth(1.5f * dp);
                    mPaint.setColor(0xCCFFFFFF);
                    c.drawCircle(cx, sy, r * 0.72f, mPaint);
                }
            }
        }

        // Fret numbers as compact numbers in the top-right corner of each fret
        mTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        mTextPaint.setTextSize(10.5f * dp);
        mTextPaint.setTextAlign(Paint.Align.RIGHT);
        mTextPaint.setColor(0xFFE8D9C2);
        for (int col = 0; col < 5; col++) {
            int fretNum = (col == 0) ? 0 : (pos.baseFret + col - 1);
            float colRight = neck.left + (col + 1) * colW;
            float numX = colRight - (col == 0 ? 6.5f * dp : 5.5f * dp);
            AppI18n.drawText(c, String.valueOf(fretNum), numX, neck.top + 12f * dp, mTextPaint);
        }
    }

    private void drawExercisePickerModal(Canvas c, float w, float h, float dp, boolean landscape) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(0xB810141A);
        c.drawRect(0, 0, w, h, mPaint);

        float mw = Math.min(w * 0.94f, (landscape ? 680f : 390f) * dp);
        float mh = Math.min(h * 0.88f, (landscape ? 330f : 520f) * dp);
        mModalRect.set((w - mw) * 0.5f, (h - mh) * 0.5f, (w + mw) * 0.5f, (h + mh) * 0.5f);

        mPaint.setColor(COL_CARD_BG);
        c.drawRoundRect(mModalRect, 12f * dp, 12f * dp, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2f * dp);
        mPaint.setColor(COL_GOLD_ACCENT);
        c.drawRoundRect(mModalRect, 12f * dp, 12f * dp, mPaint);

        mTextPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        mTextPaint.setTextSize(14.5f * dp);
        mTextPaint.setColor(COL_INK_DARK);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        AppI18n.drawText(c, "SELECT EXERCISE (1\u201324)", mModalRect.left + 14f * dp, mModalRect.top + 24f * dp, mTextPaint);

        int cols = landscape ? 4 : 2;
        int rows = SightReadTheory.NUM_EXERCISES / cols;
        float pad = 6f * dp;
        float gridTop = mModalRect.top + 34f * dp;
        float gridBot = mModalRect.bottom - 10f * dp;
        float gridW = mModalRect.width() - 24f * dp;
        float gridH = gridBot - gridTop;
        float cellW = (gridW - pad * (cols - 1)) / cols;
        float cellH = (gridH - pad * (rows - 1)) / rows;

        int activeIdx = ((mExerciseNum - 1) % SightReadTheory.NUM_EXERCISES + SightReadTheory.NUM_EXERCISES)
                % SightReadTheory.NUM_EXERCISES;

        for (int i = 0; i < SightReadTheory.NUM_EXERCISES; i++) {
            int r = i / cols;
            int col = i % cols;
            float bx = mModalRect.left + 12f * dp + col * (cellW + pad);
            float by = gridTop + r * (cellH + pad);
            mExerciseBtns[i].set(bx, by, bx + cellW, by + cellH);
            boolean sel = (i == activeIdx);
            int bg = sel ? COL_GOLD_ACCENT : COL_CONTROL_BG;
            int fg = sel ? COL_HEADER_BG : COL_INK_DARK;
            String lbl = (i + 1) + ". " + SightReadTheory.EXERCISE_NAMES[i];
            drawPillButton(c, mExerciseBtns[i], lbl, bg, fg, (landscape ? 9.5f : 10f) * dp);
        }
    }

    private void drawHelpModal(Canvas c, float w, float h, float dp) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(0xC810141A);
        c.drawRect(0, 0, w, h, mPaint);

        float mw = Math.min(w * 0.94f, 640f * dp);
        float mh = Math.min(h * 0.92f, 470f * dp);
        mModalRect.set((w - mw) * 0.5f, (h - mh) * 0.5f, (w + mw) * 0.5f, (h + mh) * 0.5f);

        mPaint.setColor(COL_CARD_BG);
        c.drawRoundRect(mModalRect, 12f * dp, 12f * dp, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2f * dp);
        mPaint.setColor(COL_GOLD_ACCENT);
        c.drawRoundRect(mModalRect, 12f * dp, 12f * dp, mPaint);

        float tx = mModalRect.left + 14f * dp;
        float ty = mModalRect.top + 22f * dp;
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        mTextPaint.setTextSize(14.5f * dp);
        mTextPaint.setColor(COL_INK_DARK);
        AppI18n.drawTextFit(c, AppI18n.str(getContext(), R.string.help_title),
                tx, ty, mTextPaint, mw - 28f * dp);

        Context ctx = getContext();
        String[] titles = {
            AppI18n.str(ctx, R.string.help_sec1_title),
            AppI18n.str(ctx, R.string.help_sec2_title),
            AppI18n.str(ctx, R.string.help_sec3_title),
            AppI18n.str(ctx, R.string.help_sec4_title)
        };
        String[] bodies = {
            AppI18n.str(ctx, R.string.help_sec1_body),
            AppI18n.str(ctx, R.string.help_sec2_body),
            AppI18n.str(ctx, R.string.help_sec3_body),
            AppI18n.str(ctx, R.string.help_sec4_body)
        };
        int[] titleCols = { COL_HEADER_BG, COL_BLUE_INFO, COL_GREEN_OK, COL_HEADER_BG };

        float bw = 136f * dp;
        float bh = 28f * dp;
        mModalCloseBtn.set(mModalRect.centerX() - bw * 0.5f, mModalRect.bottom - bh - 7f * dp,
                mModalRect.centerX() + bw * 0.5f, mModalRect.bottom - 7f * dp);
        drawPillButton(c, mModalCloseBtn, AppI18n.str(ctx, R.string.got_it),
                COL_GOLD_ACCENT, COL_HEADER_BG, 11.5f * dp);

        mModalViewportRect.set(mModalRect.left + 10f * dp, mModalRect.top + 30f * dp,
                mModalRect.right - 10f * dp, mModalCloseBtn.top - 6f * dp);
        mTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        mHelpMaxScrollY = AppI18n.drawScrollableHelpBlock(
                c, mModalViewportRect, titles, bodies, titleCols,
                COL_INK_DARK, 0xFFF3EBDD, 0xFFD2C5B0, COL_HEADER_BG,
                11.0f * dp, 9.8f * dp, mHelpScrollY, mTextPaint, mPaint);
        mHelpScrollY = Math.max(0f, Math.min(mHelpScrollY, mHelpMaxScrollY));
    }

    private void drawPillButton(Canvas c, RectF r, String text, int bg, int fg, float textSize) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(bg);
        c.drawRoundRect(r, 6f, 6f, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(1.5f);
        mPaint.setColor(0x33000000);
        c.drawRoundRect(r, 6f, 6f, mPaint);

        mTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        mTextPaint.setTextSize(textSize);
        mTextPaint.setColor(fg);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        AppI18n.drawText(c, text, r.centerX(), r.centerY() + textSize * 0.35f, mTextPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();

        if (mShowHelp) {
            if (action == MotionEvent.ACTION_DOWN) {
                mHelpTouchStartY = y;
                mHelpLastTouchY = y;
                mHelpDragging = false;
                if (mModalCloseBtn.contains(x, y) || !mModalRect.contains(x, y)) {
                    mShowHelp = false;
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

        if (action != MotionEvent.ACTION_DOWN) return true;

        if (mShowExercisePicker) {
            for (int i = 0; i < SightReadTheory.NUM_EXERCISES; i++) {
                if (mExerciseBtns[i].contains(x, y)) {
                    mExerciseNum = i + 1;
                    loadPiece();
                    break;
                }
            }
            mShowExercisePicker = false;
            invalidate();
            return true;
        }

        if (mBtnHelp.contains(x, y)) {
            mShowHelp = true;
            mHelpScrollY = 0f;
            invalidate();
            return true;
        }
        if (mBtnPlay.contains(x, y)) {
            if (mIsPlaying) stopTransport(); else startTransport();
            return true;
        }
        if (mBtnMute.contains(x, y)) {
            mSynth.setMuted(!mSynth.isMuted());
            invalidate();
            return true;
        }

        for (int i = 0; i < mCtrlBtns.length; i++) {
            if (mCtrlBtns[i].contains(x, y)) {
                handleControlTap(i);
                return true;
            }
        }

        if (mNeckRect.contains(x, y)) {
            float vertPad = 5f * getResources().getDisplayMetrics().density;
            float rowH = Math.max(1f, (mNeckRect.height() - 2f * vertPad) / 6f);
            int col = Math.min(4, Math.max(0, (int) ((x - mNeckRect.left) / (mNeckRect.width() / 5f))));
            int row = Math.min(5, Math.max(0, (int) ((y - (mNeckRect.top + vertPad)) / rowH)));
            int str = 5 - row;
            NeckPosition pos = SightReadTheory.POSITIONS[mPosIdx];
            int fret = (col == 0) ? 0 : (pos.baseFret + col - 1);
            handleFretTap(str, fret);
            return true;
        }
        return true;
    }

    private void handleControlTap(int idx) {
        switch (idx) {
            case 0: mKeyIdx = (mKeyIdx + 1) % SightReadTheory.NUM_KEYS; loadPiece(); break;
            case 1: mPosIdx = (mPosIdx + 1) % SightReadTheory.NUM_POSITIONS; loadPiece(); break;
            case 2: mPracticeMode = (mPracticeMode + 1) % SightReadTheory.NUM_MODES; break;
            case 3: mShowExercisePicker = true; break;
            case 4: mBpm = (mBpm >= 160) ? 60 : (mBpm + 20); break;
            case 5: mShowHints = !mShowHints; break;
            case 6: mExerciseNum = (mExerciseNum % SightReadTheory.NUM_EXERCISES) + 1; loadPiece(); break;
        }
        invalidate();
    }

    private void handleFretTap(int str, int fret) {
        if (mCurrentIdx >= mPiece.numNotes) return;
        StaffNote target = mPiece.notes[mCurrentIdx];
        boolean ok = SightReadTheory.matchesNoteByFretboard(target, str, fret, false);
        mFlashString = str;
        mFlashFret = fret;
        recordAttempt(target, ok, SightReadTheory.midiForStringFret(str, fret));
    }

    private void recordAttempt(StaffNote target, boolean ok, int playedMidi) {
        mScoreAttempts++;
        mFlashCorrect = ok;
        mFlashUntilMs = System.currentTimeMillis() + 360L;
        if (ok) {
            mScoreHits++;
            mStreak++;
            mSynth.playNote(target.midi, 320);
            mCurrentIdx++;
            if (mCurrentIdx >= mPiece.numNotes) {
                mStatusText = "Bravo! Completed Ex #" + mExerciseNum + " — Loading next exercise...";
                mStatusColor = COL_GREEN_OK;
                mExerciseNum = (mExerciseNum % 24) + 1;
                loadPiece();
            } else {
                StaffNote next = mPiece.notes[mCurrentIdx];
                mStatusText = "Correct (" + target.label + ")! Next: " + next.label;
                mStatusColor = COL_GREEN_OK;
            }
        } else {
            mStreak = 0;
            mSynth.playErrorBuzz();
            mStatusText = "Try again! Expected " + target.label + " (String " + (6 - target.str) + ", Fret " + target.fret + ")";
            mStatusColor = COL_RED_ERR;
        }
        invalidate();
    }
}
