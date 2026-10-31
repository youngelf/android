package com.eggwall.eartrainer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

/**
 * Custom 2D canvas view for the Android Ear Trainer studio.
 *
 * <p>Renders an acoustic studio interface in both Portrait and Landscape orientations:
 * <ul>
 *   <li>Top header bar with {@code [?]} help toggle, Mode/Difficulty/Style pills, and Streak/Score.</li>
 *   <li>6-string 12-fret guitar neck visualizer with pearl fret inlays, root/target markers,
 *       and interactive string/fret tapping.</li>
 *   <li>Interactive 15-key piano keyboard strip ({@code rootMidi .. rootMidi + 12}) for visual
 *       and tactile pitch exploration.</li>
 *   <li>Status banner showing the active prompt or revealed interval/chord/solfege/cadence theory
 *       plus remaining guess indicator dots.</li>
 *   <li>4x3 (12-item) answer keypad grid with per-item lifetime accuracy bars and post-reveal
 *       comparative auditioning.</li>
 *   <li>Bottom transport bar ({@code REPLAY}, {@code ROOT / HINT}, {@code STYLE}, {@code NEXT}).</li>
 *   <li>Scrollable/interactive {@code [?]} Help & Music Theory Reference modal overlay.</li>
 * </ul>
 */
public final class EarTrainerView extends View {

    private static final String PREFS_NAME = "eartrainer_prefs";
    private static final int WHITE_KEY_COUNT = 8; /* 1 octave + top root (C..C') */
    private static final int[] WHITE_OFFSETS = { 0, 2, 4, 5, 7, 9, 11, 12 };
    private static final int[] BLACK_OFFSETS = { 1, 3, -1, 6, 8, 10, -1 };

    private final EarTheory.TrainerSession mSession = new EarTheory.TrainerSession();
    private final EarSynthEngine mSynth = new EarSynthEngine();
    private final SharedPreferences mPrefs;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mRect = new RectF();
    private final EarTheory.FretPos mFretPos = new EarTheory.FretPos();

    /* Touch hit-test rectangles */
    private final RectF mBtnHelp = new RectF();
    private final RectF mBtnMode = new RectF();
    private final RectF mBtnTier = new RectF();
    private final RectF mBtnStyle = new RectF();
    private final RectF mFretboardRect = new RectF();
    private final RectF mPianoRect = new RectF();
    private final RectF mBannerRect = new RectF();
    private final RectF[] mGridRects = new RectF[EarTheory.NUM_ITEMS];
    private final RectF[] mActionRects = new RectF[4]; /* Replay, Root, Hint, Next */
    private final RectF mHelpModalRect = new RectF();
    private final RectF mHelpCloseRect = new RectF();
    private final RectF mHelpViewportRect = new RectF();
    private float mHelpScrollY = 0f;
    private float mHelpMaxScrollY = 0f;
    private float mHelpTouchStartY = 0f;
    private float mHelpLastTouchY = 0f;
    private boolean mHelpDragging = false;

    private boolean mShowHelp = false;
    private int mLastTappedMidi = -1;

    public EarTrainerView(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
        for (int i = 0; i < EarTheory.NUM_ITEMS; i++) {
            mGridRects[i] = new RectF();
        }
        for (int i = 0; i < mActionRects.length; i++) {
            mActionRects[i] = new RectF();
        }
        mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadStats();
        mSession.generateNewQuestion();
    }

    public void onResumeView() {
        mSynth.start();
        mSynth.playSessionItem(mSession, mSession.correctIdx);
    }

    public void onPauseView() {
        saveStats();
        mSynth.stop();
    }

    /**
     * Configures deterministic demo state for automated screenshot capture and UI verification.
     */
    public void configureDemoState(int mode, int tier, boolean revealed, boolean showHelp) {
        mSession.mode = Math.max(0, Math.min(EarTheory.NUM_MODES - 1, mode));
        mSession.tier = Math.max(0, Math.min(EarTheory.TIER_ALL_12, tier));
        mSession.rootStr = (mSession.mode == EarTheory.MODE_CHORDS) ? 1 : 0;
        mSession.rootFret = 5; /* A2 (MIDI 45) or D3 (MIDI 50) */
        mSession.rootMidi = EarTheory.OPEN_STRING_MIDI[mSession.rootStr] + mSession.rootFret;
        mSession.correctIdx = (mSession.mode == EarTheory.MODE_INTERVALS) ? 6 : 0; /* P5 or Major */
        mSession.auditionIdx = mSession.correctIdx;
        mSession.cursor = mSession.correctIdx;
        mSession.guessesLeft = revealed ? 2 : 3;
        for (int i = 0; i < EarTheory.NUM_ITEMS; i++) {
            mSession.wrongGuessed[i] = false;
        }
        if (revealed) {
            mSession.wrongGuessed[3] = true;
        }
        mSession.revealed = revealed;
        mSession.solved = revealed;
        mSession.streak = Math.max(mSession.streak, 7);
        mSession.bestStreak = Math.max(mSession.bestStreak, 14);
        mSession.sessionOk = Math.max(mSession.sessionOk, 18);
        mSession.sessionTot = Math.max(mSession.sessionTot, 20);
        int[] demoOk  = { 7, 8, 10, 9, 11, 5, 12, 6, 8, 5, 7, 12 };
        int[] demoTot = { 10, 10, 11, 10, 12, 8, 12, 9, 10, 8, 10, 12 };
        for (int i = 0; i < EarTheory.NUM_ITEMS; i++) {
            mSession.itemOk[mSession.mode][i] = demoOk[i];
            mSession.itemTot[mSession.mode][i] = demoTot[i];
        }
        mShowHelp = showHelp;
        invalidate();
    }

    private void loadStats() {
        mSession.mode = mPrefs.getInt("mode", EarTheory.MODE_INTERVALS);
        mSession.tier = mPrefs.getInt("tier", EarTheory.TIER_ALL_12);
        mSession.intStyle = mPrefs.getInt("intStyle", EarTheory.INT_STYLE_ASC);
        mSession.chdStyle = mPrefs.getInt("chdStyle", EarTheory.CHD_STYLE_ARP_STRUM);
        mSession.degStyle = mPrefs.getInt("degStyle", EarTheory.DEG_STYLE_CADENCE_NOTE);
        mSession.progStyle = mPrefs.getInt("progStyle", EarTheory.PROG_STYLE_MELODIC);
        mSession.bestStreak = mPrefs.getInt("bestStreak", 0);
        for (int m = 0; m < EarTheory.NUM_MODES; m++) {
            for (int i = 0; i < EarTheory.NUM_ITEMS; i++) {
                mSession.itemOk[m][i] = mPrefs.getInt("ok_" + m + "_" + i, 0);
                mSession.itemTot[m][i] = mPrefs.getInt("tot_" + m + "_" + i, 0);
            }
        }
    }

    private void saveStats() {
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt("mode", mSession.mode);
        ed.putInt("tier", mSession.tier);
        ed.putInt("intStyle", mSession.intStyle);
        ed.putInt("chdStyle", mSession.chdStyle);
        ed.putInt("degStyle", mSession.degStyle);
        ed.putInt("progStyle", mSession.progStyle);
        ed.putInt("bestStreak", mSession.bestStreak);
        for (int m = 0; m < EarTheory.NUM_MODES; m++) {
            for (int i = 0; i < EarTheory.NUM_ITEMS; i++) {
                ed.putInt("ok_" + m + "_" + i, mSession.itemOk[m][i]);
                ed.putInt("tot_" + m + "_" + i, mSession.itemTot[m][i]);
            }
        }
        ed.apply();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        canvas.drawColor(0xFF10131C);
        float pad = Math.min(w, h) * 0.018f;
        boolean landscape = w > h;

        /* 1. Top Header Bar */
        float topH = Math.max(46f, Math.min(h * 0.095f, 82f));
        drawHeaderBar(canvas, pad, pad, w - 2 * pad, topH);

        /* 2. Bottom Transport Bar (tall, prominent thumb targets for frequent Replay/Next use) */
        float botH = landscape
                ? Math.max(92f, h * 0.145f)
                : Math.max(110f, h * 0.105f);
        float botY = h - pad - botH;
        drawBottomBar(canvas, pad, botY, w - 2 * pad, botH);

        float bodyY = pad + topH + pad;
        float bodyH = botY - pad - bodyY;

        if (landscape) {
            float leftW = (w - 3 * pad) * 0.48f;
            float rightX = pad + leftW + pad;
            float rightW = w - pad - rightX;

            float fretH = bodyH * 0.54f;
            float pianoH = bodyH * 0.29f;
            float bannerH = bodyH - fretH - pianoH - 2 * pad;

            mFretboardRect.set(pad, bodyY, pad + leftW, bodyY + fretH);
            mPianoRect.set(pad, mFretboardRect.bottom + pad, pad + leftW, mFretboardRect.bottom + pad + pianoH);
            mBannerRect.set(pad, mPianoRect.bottom + pad, pad + leftW, bodyY + bodyH);

            drawFretboard(canvas, mFretboardRect);
            drawPianoStrip(canvas, mPianoRect);
            drawStatusBanner(canvas, mBannerRect);
            drawKeypadGrid(canvas, rightX, bodyY, rightW, bodyH);
        } else {
            float fretH = bodyH * 0.28f;
            float pianoH = bodyH * 0.145f;
            float bannerH = bodyH * 0.078f;
            float gridH = bodyH - fretH - pianoH - bannerH - 3 * pad;

            mFretboardRect.set(pad, bodyY, w - pad, bodyY + fretH);
            mPianoRect.set(pad, mFretboardRect.bottom + pad, w - pad, mFretboardRect.bottom + pad + pianoH);
            mBannerRect.set(pad, mPianoRect.bottom + pad, w - pad, mPianoRect.bottom + pad + bannerH);

            drawFretboard(canvas, mFretboardRect);
            drawPianoStrip(canvas, mPianoRect);
            drawStatusBanner(canvas, mBannerRect);
            drawKeypadGrid(canvas, pad, mBannerRect.bottom + pad, w - 2 * pad, gridH);
        }

        if (mShowHelp) {
            drawHelpModal(canvas, w, h);
        }
    }

    private void drawHeaderBar(Canvas c, float x, float y, float w, float h) {
        mRect.set(x, y, x + w, y + h);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(0xFF1A2030);
        c.drawRoundRect(mRect, 12f, 12f, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2f);
        mPaint.setColor(0xFF323E58);
        c.drawRoundRect(mRect, 12f, 12f, mPaint);

        float inPad = h * 0.14f;
        float btnH = h - 2 * inPad;
        float helpW = btnH * 1.05f;
        mBtnHelp.set(x + inPad, y + inPad, x + inPad + helpW, y + h - inPad);
        drawPill(c, mBtnHelp, "?", 0xFF28344E, 0xFF6EC6FF, 0xFFFFFFFF);

        float remW = w - helpW - inPad * 6;
        float modeW = remW * 0.27f;
        float tierW = remW * 0.20f;
        float styleW = remW * 0.23f;

        mBtnMode.set(mBtnHelp.right + inPad, y + inPad, mBtnHelp.right + inPad + modeW, y + h - inPad);
        mBtnTier.set(mBtnMode.right + inPad, y + inPad, mBtnMode.right + inPad + tierW, y + h - inPad);
        mBtnStyle.set(mBtnTier.right + inPad, y + inPad, mBtnTier.right + inPad + styleW, y + h - inPad);

        drawPill(c, mBtnMode, EarTheory.MODE_NAMES[mSession.mode], 0xFF24344C, 0xFF5CE6B0, 0xFF6BFFB8);
        int tierCol = (mSession.tier == 0) ? 0xFF5CE682 : (mSession.tier == 1 ? 0xFFFFCC44 : 0xFFFF786E);
        drawPill(c, mBtnTier, EarTheory.TIER_NAMES[mSession.tier], 0xFF252A3C, tierCol, tierCol);
        drawPill(c, mBtnStyle, mSession.getCurrentStyleLabel(), 0xFF252A3C, 0xFF6EA8FF, 0xFFA4CAFF);

        /* Right-aligned Streak & Accuracy */
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.RIGHT);
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mPaint.setTextSize(btnH * 0.34f);
        mPaint.setColor(0xFFFFD454);
        float rx = x + w - inPad * 1.2f;
        AppI18n.drawText(c, "STRK " + mSession.streak + " (BEST " + mSession.bestStreak + ")", rx, y + h * 0.43f, mPaint);
        mPaint.setColor(0xFFA8B4CC);
        AppI18n.drawText(c, mSession.sessionOk + "/" + mSession.sessionTot + " (" + mSession.getSessionAccuracyPercent() + "%)", rx, y + h * 0.80f, mPaint);
    }

    private void drawPill(Canvas c, RectF r, String text, int bg, int border, int fg) {
        text = AppI18n.tr(text);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(bg);
        c.drawRoundRect(r, 12f, 12f, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2.2f);
        mPaint.setColor(border);
        c.drawRoundRect(r, 12f, 12f, mPaint);

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(fg);
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mPaint.setTextAlign(Paint.Align.CENTER);
        float ts = Math.min(r.height() * 0.34f, 44f);
        mPaint.setTextSize(ts);
        float maxW = Math.max(16f, r.width() - 20f);
        float measured = mPaint.measureText(text);
        if (measured > maxW && measured > 0f) {
            ts = ts * (maxW / measured);
            mPaint.setTextSize(ts);
        }
        Paint.FontMetrics fm = mPaint.getFontMetrics();
        AppI18n.drawText(c, text, r.centerX(), r.centerY() - (fm.ascent + fm.descent) * 0.5f, mPaint);
    }

    private void drawFretboard(Canvas c, RectF r) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(0xFF2A1B12); /* Rosewood fingerboard */
        c.drawRoundRect(r, 12f, 12f, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2f);
        mPaint.setColor(0xFF5A3D28);
        c.drawRoundRect(r, 12f, 12f, mPaint);

        float leftGutter = r.width() * 0.085f;
        float nutX = r.left + leftGutter;
        float fbW = r.width() - leftGutter - r.width() * 0.025f;
        float fretStep = fbW / 12f;
        float topPad = r.height() * 0.14f;
        float botPad = r.height() * 0.20f;
        float strSpan = r.height() - topPad - botPad;
        float strStep = strSpan / 5f;

        /* Nut & Fret wires */
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(0xFFE6DCC8);
        c.drawRect(nutX - 4f, r.top + topPad * 0.6f, nutX + 3f, r.bottom - botPad * 0.8f, mPaint);

        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mPaint.setTextSize(botPad * 0.58f);

        for (int f = 1; f <= 12; f++) {
            float fx = nutX + f * fretStep;
            mPaint.setColor(0xFF8C8C96);
            mPaint.setStrokeWidth(2f);
            c.drawLine(fx, r.top + topPad * 0.65f, fx, r.bottom - botPad * 0.85f, mPaint);

            if (f == 3 || f == 5 || f == 7 || f == 9 || f == 12) {
                float cx = fx - fretStep * 0.5f;
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(0x55E8E4D8);
                float rad = Math.min(fretStep, strStep) * 0.22f;
                if (f == 12) {
                    c.drawCircle(cx, r.top + topPad + strStep * 1.5f, rad, mPaint);
                    c.drawCircle(cx, r.top + topPad + strStep * 3.5f, rad, mPaint);
                } else {
                    c.drawCircle(cx, r.top + topPad + strSpan * 0.5f, rad, mPaint);
                }
                mPaint.setColor(0xFFA89F91);
                AppI18n.drawText(c, String.valueOf(f), cx, r.bottom - botPad * 0.18f, mPaint);
            }
        }

        /* 6 Guitar Strings (top = high e [idx 5], bottom = low E [idx 0]) */
        mPaint.setTextSize(strStep * 0.58f);
        for (int s = 0; s < 6; s++) {
            int visualRow = 5 - s;
            float sy = r.top + topPad + visualRow * strStep;
            mPaint.setColor(s < 3 ? 0xFFD0A868 : 0xFFD8DCE6); /* Wound bronze vs plain steel */
            mPaint.setStrokeWidth(1.5f + (5 - s) * 0.45f);
            c.drawLine(nutX, sy, nutX + fbW, sy, mPaint);

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(0xFFC8B89A);
            AppI18n.drawText(c, EarTheory.STRING_NAMES[s], r.left + leftGutter * 0.45f, sy + strStep * 0.20f, mPaint);
        }

        /* Draw Root Note Marker (always visible) */
        drawFretMarker(c, nutX, fretStep, r.top + topPad, strStep,
                mSession.rootStr, mSession.rootFret, "R", 0xFF32D278, 0xFFFFFFFF);

        /* Draw Revealed / Auditioned Interval, Chord, Solfege, or Progression Notes */
        if (mSession.revealed) {
            int idx = mSession.auditionIdx;
            if (mSession.mode == EarTheory.MODE_INTERVALS || mSession.mode == EarTheory.MODE_DEGREES) {
                int semi = (mSession.mode == EarTheory.MODE_INTERVALS)
                        ? EarTheory.INTERVALS[idx].semitones
                        : EarTheory.SCALE_DEGREES[idx].semitones;
                EarTheory.findGuitarIntervalPos(mSession.rootStr, mSession.rootFret, semi, mFretPos);
                String badge = (mSession.mode == EarTheory.MODE_INTERVALS)
                        ? EarTheory.INTERVALS[idx].shortName
                        : EarTheory.SCALE_DEGREES[idx].solfege;
                drawFretMarker(c, nutX, fretStep, r.top + topPad, strStep,
                        mFretPos.stringIdx, mFretPos.fret, badge, 0xFFFFB428, 0xFF10131C);
            } else if (mSession.mode == EarTheory.MODE_CHORDS) {
                EarTheory.ChordDef cd = EarTheory.CHORDS[idx];
                for (int n = 1; n < cd.numNotes; n++) {
                    int s = mSession.rootStr + cd.strOff[n];
                    int f = mSession.rootFret + cd.fretOff[n];
                    drawFretMarker(c, nutX, fretStep, r.top + topPad, strStep,
                            s, f, String.valueOf(n + 1), 0xFFFFB428, 0xFF10131C);
                }
            } else if (mSession.mode == EarTheory.MODE_PROGRESSIONS) {
                EarTheory.MelodicPatternDef mp = EarTheory.PROGRESSIONS[idx];
                for (int n = 1; n < mp.melodySemitones.length; n++) {
                    int semi = mp.melodySemitones[n];
                    if (semi == 0) continue;
                    EarTheory.findGuitarIntervalPos(mSession.rootStr, mSession.rootFret, semi, mFretPos);
                    drawFretMarker(c, nutX, fretStep, r.top + topPad, strStep,
                            mFretPos.stringIdx, mFretPos.fret, String.valueOf(n + 1), 0xFF64C8FF, 0xFF10131C);
                }
            }
        }
    }

    private void drawFretMarker(Canvas c, float nutX, float fretStep, float topY, float strStep,
                                int strIdx, int fret, String label, int fillCol, int textCol) {
        if (strIdx < 0 || strIdx > 5 || fret < 0 || fret > 12) return;
        float cx = (fret == 0) ? (nutX - fretStep * 0.28f) : (nutX + (fret - 0.5f) * fretStep);
        float cy = topY + (5 - strIdx) * strStep;
        float rad = Math.min(fretStep * 0.40f, strStep * 0.44f);

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(fillCol);
        c.drawCircle(cx, cy, rad, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2f);
        mPaint.setColor(0xFFFFFFFF);
        c.drawCircle(cx, cy, rad, mPaint);

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(textCol);
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(rad * 0.95f);
        Paint.FontMetrics fm = mPaint.getFontMetrics();
        AppI18n.drawText(c, label, cx, cy - (fm.ascent + fm.descent) * 0.5f, mPaint);
    }

    private void drawPianoStrip(Canvas c, RectF r) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(0xFF181D2A);
        c.drawRoundRect(r, 10f, 10f, mPaint);

        int[] activeMidis = mSession.revealed
                ? EarTheory.getItemMidiNotes(mSession.mode, mSession.auditionIdx, mSession.rootMidi)
                : new int[]{ mSession.rootMidi };

        float pad = 4f;
        float keyW = (r.width() - 2 * pad) / WHITE_KEY_COUNT;
        float keyH = r.height() - 2 * pad;

        /* White keys (offsets relative to rootMidi so the octave layout is always clear) */
        for (int i = 0; i < WHITE_KEY_COUNT; i++) {
            int semi = WHITE_OFFSETS[i];
            int midi = mSession.rootMidi + semi;
            float kx = r.left + pad + i * keyW;
            mRect.set(kx + 1.5f, r.top + pad, kx + keyW - 1.5f, r.top + pad + keyH);

            int col = 0xFFECEFF4;
            if (midi == mSession.rootMidi) col = 0xFF42D682;
            else if (containsMidi(activeMidis, midi)) col = 0xFFFFBE3C;
            else if (midi == mLastTappedMidi) col = 0xFF74C0FC;

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(col);
            c.drawRoundRect(mRect, 6f, 6f, mPaint);

            mPaint.setColor(0xFF2B303C);
            mPaint.setTextAlign(Paint.Align.CENTER);
            mPaint.setTypeface(Typeface.DEFAULT_BOLD);
            mPaint.setTextSize(Math.min(keyW * 0.30f, keyH * 0.24f));
            AppI18n.drawText(c, EarTheory.formatMidiNote(midi), mRect.centerX(), mRect.bottom - keyH * 0.10f, mPaint);
        }

        /* Black keys */
        float bkW = keyW * 0.62f;
        float bkH = keyH * 0.60f;
        for (int i = 0; i < BLACK_OFFSETS.length; i++) {
            int semi = BLACK_OFFSETS[i];
            if (semi < 0) continue;
            int midi = mSession.rootMidi + semi;
            float cx = r.left + pad + (i + 1) * keyW;
            mRect.set(cx - bkW * 0.5f, r.top + pad, cx + bkW * 0.5f, r.top + pad + bkH);

            int col = 0xFF202534;
            if (containsMidi(activeMidis, midi)) col = 0xFFFF9E22;
            else if (midi == mLastTappedMidi) col = 0xFF4DABF7;

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(col);
            c.drawRoundRect(mRect, 5f, 5f, mPaint);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(1.5f);
            mPaint.setColor(0xFF48526A);
            c.drawRoundRect(mRect, 5f, 5f, mPaint);
        }
    }

    private boolean containsMidi(int[] arr, int target) {
        for (int m : arr) {
            if (m == target) return true;
        }
        return false;
    }

    private void drawStatusBanner(Canvas c, RectF r) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mSession.revealed
                ? (mSession.solved ? 0xFF163226 : 0xFF381E24)
                : 0xFF1A2234);
        c.drawRoundRect(r, 10f, 10f, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2f);
        mPaint.setColor(mSession.revealed
                ? (mSession.solved ? 0xFF42D682 : 0xFFFF6B6B)
                : 0xFF364666);
        c.drawRoundRect(r, 10f, 10f, mPaint);

        String rootStr = EarTheory.formatMidiNote(mSession.rootMidi);
        String line1;
        String line2;
        if (!mSession.revealed) {
            line1 = "Root: " + rootStr + "  |  Identify the " + EarTheory.MODE_FULL_TITLES[mSession.mode];
            line2 = "Tap any answer pad below (" + mSession.guessesLeft + " tries left) or tap Piano/Fretboard to explore";
        } else {
            int idx = mSession.auditionIdx;
            String prefix = (idx == mSession.correctIdx)
                    ? (mSession.solved ? "CORRECT: " : "ANSWER: ")
                    : "COMPARE: ";
            if (mSession.mode == EarTheory.MODE_INTERVALS) {
                EarTheory.IntervalDef id = EarTheory.INTERVALS[idx];
                line1 = prefix + id.shortName + " - " + id.fullName + " (" + id.semitones + " semitones)";
                line2 = "Song Ref: " + id.mnemonic + "  |  Tap any pad to compare, or NEXT";
            } else if (mSession.mode == EarTheory.MODE_CHORDS) {
                EarTheory.ChordDef cd = EarTheory.CHORDS[idx];
                line1 = prefix + cd.shortName + " - " + cd.fullName + " [" + cd.formula + "]";
                line2 = "Root " + rootStr + "  |  Tap any pad to audition chord voicings, or NEXT";
            } else if (mSession.mode == EarTheory.MODE_DEGREES) {
                EarTheory.ScaleDegreeDef sd = EarTheory.SCALE_DEGREES[idx];
                line1 = prefix + sd.shortName + " (" + sd.solfege + ") - " + sd.fullName;
                line2 = sd.resolutionTendency + "  |  Tap any pad to compare";
            } else {
                EarTheory.MelodicPatternDef mp = EarTheory.PROGRESSIONS[idx];
                line1 = prefix + mp.shortName + " (" + mp.subLabel + ") - " + mp.fullName;
                line2 = mp.description + "  |  Tap any pad to compare";
            }
        }

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.LEFT);
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);
        float dotR = Math.min(r.height() * 0.12f, 8.5f);
        float dotsReserveW = (EarTheory.MAX_GUESSES * dotR * 2.8f) + 28f;
        float maxLine1W = Math.max(40f, r.width() - dotsReserveW - 16f);
        float line1Ts = Math.min(r.height() * 0.36f, r.width() * 0.036f);
        mPaint.setTextSize(line1Ts);
        line1 = AppI18n.tr(line1);
        line2 = AppI18n.tr(line2);
        float m1 = mPaint.measureText(line1);
        if (m1 > maxLine1W && m1 > 0f) {
            mPaint.setTextSize(line1Ts * (maxLine1W / m1));
        }
        mPaint.setColor(0xFFFFFFFF);
        AppI18n.drawText(c, line1, r.left + 14f, r.top + r.height() * 0.43f, mPaint);

        mPaint.setTypeface(Typeface.DEFAULT);
        float maxLine2W = Math.max(40f, r.width() - 28f);
        float line2Ts = Math.min(r.height() * 0.28f, r.width() * 0.030f);
        mPaint.setTextSize(line2Ts);
        float m2 = mPaint.measureText(line2);
        if (m2 > maxLine2W && m2 > 0f) {
            mPaint.setTextSize(line2Ts * (maxLine2W / m2));
        }
        mPaint.setColor(0xFFB8C6E0);
        AppI18n.drawText(c, line2, r.left + 14f, r.top + r.height() * 0.80f, mPaint);

        /* Remaining guess dots on right side */
        for (int g = 0; g < EarTheory.MAX_GUESSES; g++) {
            float dx = r.right - 18f - g * (dotR * 2.8f);
            float dy = r.top + r.height() * 0.34f;
            mPaint.setColor(g < mSession.guessesLeft ? 0xFF5CE6B0 : 0xFF3A4258);
            c.drawCircle(dx, dy, dotR, mPaint);
        }
    }

    private void drawKeypadGrid(Canvas c, float x, float y, float w, float h) {
        float gap = Math.min(w, h) * 0.025f;
        float cellW = (w - 3 * gap) / 4f;
        float cellH = (h - 2 * gap) / 3f;
        int style = (mSession.mode == EarTheory.MODE_PROGRESSIONS) ? mSession.progStyle : 0;

        for (int i = 0; i < EarTheory.NUM_ITEMS; i++) {
            int col = i % 4;
            int row = i / 4;
            float cx = x + col * (cellW + gap);
            float cy = y + row * (cellH + gap);
            RectF cr = mGridRects[i];
            cr.set(cx, cy, cx + cellW, cy + cellH);

            boolean en = mSession.isEnabled(i);
            int bg = 0xFF21293C;
            int border = 0xFF3B4866;
            int fg = 0xFFF0F4FC;

            if (!en) {
                bg = 0xFF141824;
                border = 0xFF22283A;
                fg = 0xFF485268;
            } else if (mSession.revealed) {
                if (i == mSession.correctIdx) {
                    bg = 0xFF1B4D36;
                    border = 0xFF46E891;
                    fg = 0xFFFFFFFF;
                } else if (i == mSession.auditionIdx) {
                    bg = 0xFF4A3816;
                    border = 0xFFFFC042;
                } else if (mSession.wrongGuessed[i]) {
                    bg = 0xFF3A1A22;
                    border = 0xFF883848;
                    fg = 0xFF987880;
                }
            } else if (mSession.wrongGuessed[i]) {
                bg = 0xFF341820;
                border = 0xFF9E3A4A;
                fg = 0xFF886068;
            }

            if (i == mSession.cursor && en) {
                border = 0xFF74C0FC;
            }

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(bg);
            c.drawRoundRect(cr, 12f, 12f, mPaint);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(i == mSession.cursor ? 3.5f : 2f);
            mPaint.setColor(border);
            c.drawRoundRect(cr, 12f, 12f, mPaint);

            /* Main short label */
            String label = EarTheory.getItemShortName(mSession.mode, i);
            String sub = EarTheory.getItemSubLabel(mSession.mode, i, style);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(fg);
            mPaint.setTypeface(Typeface.DEFAULT_BOLD);
            mPaint.setTextAlign(Paint.Align.CENTER);
            mPaint.setTextSize(Math.min(cellH * 0.34f, cellW / Math.max(3.2f, label.length() * 0.65f)));
            AppI18n.drawText(c, label, cr.centerX(), cr.top + cellH * 0.48f, mPaint);

            /* Sub-label */
            mPaint.setTypeface(Typeface.DEFAULT);
            mPaint.setTextSize(Math.min(cellH * 0.21f, cellW * 0.18f));
            mPaint.setColor(en ? 0xFF94A6C8 : 0xFF3E4658);
            AppI18n.drawText(c, sub, cr.centerX(), cr.top + cellH * 0.74f, mPaint);

            /* Per-item accuracy bar along bottom of cell */
            int tot = mSession.itemTot[mSession.mode][i];
            if (tot > 0 && en) {
                int ok = mSession.itemOk[mSession.mode][i];
                float ratio = (float) ok / (float) tot;
                float barW = (cellW - 16f) * ratio;
                mRect.set(cr.left + 8f, cr.bottom - 7f, cr.left + 8f + barW, cr.bottom - 3f);
                mPaint.setColor(ratio >= 0.75f ? 0xFF42D682 : (ratio >= 0.5f ? 0xFFFFC042 : 0xFFFF6B6B));
                c.drawRoundRect(mRect, 2f, 2f, mPaint);
            }
        }
    }

    /**
     * Returns true only when an external hardware game controller (gamepad/joystick) or
     * external physical alphabetic keyboard is attached to the device.
     */
    private boolean hasHardwareControllerOrKeyboard() {
        int[] devIds = InputDevice.getDeviceIds();
        for (int id : devIds) {
            InputDevice dev = InputDevice.getDevice(id);
            if (dev == null || dev.isVirtual()) continue;
            if (Build.VERSION.SDK_INT >= 29 && !dev.isExternal()) continue;
            String name = dev.getName() != null ? dev.getName().toLowerCase() : "";
            if (name.contains("qwerty") || name.contains("translated set")
                    || name.contains("virtio") || name.contains("goldfish")
                    || name.contains("avr") || name.contains("headset")) {
                continue;
            }
            int sources = dev.getSources();
            if ((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
                    || (sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK) {
                return true;
            }
            if ((sources & InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD
                    && dev.getKeyboardType() == InputDevice.KEYBOARD_TYPE_ALPHABETIC) {
                return true;
            }
        }
        return false;
    }

    private void drawBottomBar(Canvas c, float x, float y, float w, float h) {
        float gap = w * 0.02f;
        float btnW = (w - 3 * gap) / 4f;
        boolean showKeyHints = hasHardwareControllerOrKeyboard();
        String[] labels = showKeyHints
                ? new String[] {
                    "REPLAY (Y)",
                    "ROOT NOTE (X)",
                    mSession.revealed ? "STYLE (L1)" : "HINT -2 (SEL)",
                    mSession.revealed ? "NEXT >> (A)" : "SKIP >>"
                }
                : new String[] {
                    "REPLAY",
                    "ROOT NOTE",
                    mSession.revealed ? "STYLE" : "HINT (-2)",
                    mSession.revealed ? "NEXT >>" : "SKIP >>"
                };
        int[] borders = { 0xFF5CE6B0, 0xFF6EC6FF, 0xFFFFC042, 0xFFFF8866 };

        for (int i = 0; i < 4; i++) {
            float bx = x + i * (btnW + gap);
            mActionRects[i].set(bx, y, bx + btnW, y + h);
            drawPill(c, mActionRects[i], labels[i], 0xFF1E2638, borders[i], 0xFFFFFFFF);
        }
    }

    private void drawHelpModal(Canvas c, int w, int h) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(0xD8080B12);
        c.drawRect(0, 0, w, h, mPaint);

        float mw = w * 0.90f;
        float mh = h * 0.84f;
        float mx = (w - mw) * 0.5f;
        float my = (h - mh) * 0.5f;
        mHelpModalRect.set(mx, my, mx + mw, my + mh);

        mPaint.setColor(0xFF182032);
        c.drawRoundRect(mHelpModalRect, 18f, 18f, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3f);
        mPaint.setColor(0xFF6EC6FF);
        c.drawRoundRect(mHelpModalRect, 18f, 18f, mPaint);
        mPaint.setStyle(Paint.Style.FILL);

        float pad = Math.max(14f, mw * 0.03f);
        float hdrH = Math.max(44f, mh * 0.105f);
        float closeW = Math.max(96f, mw * 0.18f);
        mHelpCloseRect.set(mx + mw - pad - closeW, my + 10f, mx + mw - pad, my + 10f + hdrH - 6f);
        drawPill(c, mHelpCloseRect, AppI18n.str(getContext(), R.string.close), 0xFF3A1A22, 0xFFFF6B6B, 0xFFFFFFFF);

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.LEFT);
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mPaint.setTextSize(Math.min(hdrH * 0.46f, 22f));
        mPaint.setColor(0xFF6BFFB8);
        AppI18n.drawTextFit(c, AppI18n.str(getContext(), R.string.help_title),
                mx + pad, my + 10f + (hdrH - 6f) * 0.68f, mPaint,
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
        int[] titleCols = { 0xFF6EC6FF, 0xFFFFC042, 0xFF5CE6B0 };

        mHelpViewportRect.set(mx + pad, mHelpCloseRect.bottom + 8f, mx + mw - pad, my + mh - 12f);
        float titleSz = Math.max(15f, Math.min(20f, Math.min(w, h) * 0.032f));
        float bodySz = Math.max(13.5f, Math.min(18f, Math.min(w, h) * 0.028f));
        Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHelpMaxScrollY = AppI18n.drawScrollableHelpBlock(
                c, mHelpViewportRect, titles, bodies, titleCols,
                0xFFE2EAF8, 0xFF1E293E, 0xFF2D3C5A, 0xFF6EC6FF,
                titleSz, bodySz, mHelpScrollY, mPaint, fillPaint);
        mHelpScrollY = Math.max(0f, Math.min(mHelpScrollY, mHelpMaxScrollY));
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
                if (mHelpCloseRect.contains(x, y) || !mHelpModalRect.contains(x, y)) {
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
        if (action != MotionEvent.ACTION_DOWN) {
            return true;
        }
        if (mBtnHelp.contains(x, y)) {
            mShowHelp = true;
            mHelpScrollY = 0f;
            invalidate();
            return true;
        }
        if (mBtnMode.contains(x, y)) {
            mSession.cycleMode(1);
            saveStats();
            mSynth.playSessionItem(mSession, mSession.correctIdx);
            invalidate();
            return true;
        }
        if (mBtnTier.contains(x, y)) {
            mSession.cycleTier();
            saveStats();
            mSynth.playSessionItem(mSession, mSession.correctIdx);
            invalidate();
            return true;
        }
        if (mBtnStyle.contains(x, y)) {
            mSession.cycleStyle();
            saveStats();
            mSynth.playSessionItem(mSession, mSession.revealed ? mSession.auditionIdx : mSession.correctIdx);
            invalidate();
            return true;
        }

        /* Interactive Piano Strip touch */
        if (mPianoRect.contains(x, y)) {
            handlePianoTouch(x, y);
            return true;
        }

        /* Interactive Guitar Fretboard touch */
        if (mFretboardRect.contains(x, y)) {
            handleFretboardTouch(x, y);
            return true;
        }

        /* 4x3 Keypad Grid touch */
        for (int i = 0; i < EarTheory.NUM_ITEMS; i++) {
            if (mGridRects[i].contains(x, y)) {
                handleGridSelection(i);
                return true;
            }
        }

        /* Bottom Transport Bar */
        if (mActionRects[0].contains(x, y)) {
            mSynth.playSessionItem(mSession, mSession.revealed ? mSession.auditionIdx : mSession.correctIdx);
            invalidate();
            return true;
        }
        if (mActionRects[1].contains(x, y)) {
            mSynth.playRootReference(mSession.rootMidi);
            invalidate();
            return true;
        }
        if (mActionRects[2].contains(x, y)) {
            if (mSession.revealed) {
                mSession.cycleStyle();
                mSynth.playSessionItem(mSession, mSession.auditionIdx);
            } else {
                mSession.useHintEliminateWrong();
            }
            invalidate();
            return true;
        }
        if (mActionRects[3].contains(x, y)) {
            mSession.generateNewQuestion();
            mSynth.playSessionItem(mSession, mSession.correctIdx);
            invalidate();
            return true;
        }
        return true;
    }

    private void handlePianoTouch(float x, float y) {
        float pad = 4f;
        float keyW = (mPianoRect.width() - 2 * pad) / WHITE_KEY_COUNT;
        float keyH = mPianoRect.height() - 2 * pad;
        float bkW = keyW * 0.62f;
        float bkH = keyH * 0.60f;

        if (y <= mPianoRect.top + pad + bkH) {
            for (int i = 0; i < BLACK_OFFSETS.length; i++) {
                int semi = BLACK_OFFSETS[i];
                if (semi < 0) continue;
                float cx = mPianoRect.left + pad + (i + 1) * keyW;
                if (x >= cx - bkW * 0.5f && x <= cx + bkW * 0.5f) {
                    mLastTappedMidi = mSession.rootMidi + semi;
                    mSynth.playNoteImmediate(mLastTappedMidi, 1100);
                    invalidate();
                    return;
                }
            }
        }
        int wIdx = Math.max(0, Math.min(WHITE_KEY_COUNT - 1, (int) ((x - mPianoRect.left - pad) / keyW)));
        mLastTappedMidi = mSession.rootMidi + WHITE_OFFSETS[wIdx];
        mSynth.playNoteImmediate(mLastTappedMidi, 1100);
        invalidate();
    }

    private void handleFretboardTouch(float x, float y) {
        float leftGutter = mFretboardRect.width() * 0.085f;
        float nutX = mFretboardRect.left + leftGutter;
        float fbW = mFretboardRect.width() - leftGutter - mFretboardRect.width() * 0.025f;
        float fretStep = fbW / 12f;
        float topPad = mFretboardRect.height() * 0.14f;
        float botPad = mFretboardRect.height() * 0.20f;
        float strStep = (mFretboardRect.height() - topPad - botPad) / 5f;

        int visualRow = Math.round((y - (mFretboardRect.top + topPad)) / strStep);
        int strIdx = Math.max(0, Math.min(5, 5 - visualRow));
        int fret = (x < nutX) ? 0 : Math.max(1, Math.min(12, (int) ((x - nutX) / fretStep) + 1));
        int midi = EarTheory.OPEN_STRING_MIDI[strIdx] + fret;
        mLastTappedMidi = midi;
        mSynth.playNoteImmediate(midi, 1150);
        invalidate();
    }

    private void handleGridSelection(int idx) {
        if (mSession.revealed) {
            /* In revealed state, tapping any pad auditions that interval/chord for comparison */
            mSession.auditionIdx = idx;
            mSession.cursor = idx;
            mSynth.playSessionItem(mSession, idx);
            invalidate();
            return;
        }
        int res = mSession.submitGuess(idx);
        if (res == EarTheory.TrainerSession.GUESS_CORRECT || res == EarTheory.TrainerSession.GUESS_WRONG_REVEALED) {
            saveStats();
            mSynth.playSessionItem(mSession, mSession.correctIdx);
        } else if (res == EarTheory.TrainerSession.GUESS_WRONG_RETRY) {
            mSynth.playSessionItem(mSession, idx);
        }
        invalidate();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mShowHelp) {
            mShowHelp = false;
            invalidate();
            return true;
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mSession.cursor = (mSession.cursor + 11) % 12;
                invalidate();
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mSession.cursor = (mSession.cursor + 1) % 12;
                invalidate();
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                mSession.cursor = (mSession.cursor + 8) % 12;
                invalidate();
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mSession.cursor = (mSession.cursor + 4) % 12;
                invalidate();
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_BUTTON_A:
                handleGridSelection(mSession.cursor);
                return true;
            case KeyEvent.KEYCODE_SPACE:
            case KeyEvent.KEYCODE_BUTTON_Y:
                mSynth.playSessionItem(mSession, mSession.revealed ? mSession.auditionIdx : mSession.correctIdx);
                return true;
            case KeyEvent.KEYCODE_R:
            case KeyEvent.KEYCODE_BUTTON_X:
                mSynth.playRootReference(mSession.rootMidi);
                return true;
            case KeyEvent.KEYCODE_N:
            case KeyEvent.KEYCODE_BUTTON_B:
                mSession.generateNewQuestion();
                mSynth.playSessionItem(mSession, mSession.correctIdx);
                invalidate();
                return true;
            case KeyEvent.KEYCODE_H:
                mShowHelp = true;
                invalidate();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
