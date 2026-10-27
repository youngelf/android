/*
 * FretboardView.java - Custom Canvas View for Fretboard Explorer
 *
 * Copyright 2026 Vikram Aggarwal
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.eggwall.fretboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
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

import java.util.Locale;
import java.util.Random;

/**
 * Interactive 6-string guitar fretboard canvas supporting Scales, Chords, and Chord Finder
 * modes, CAGED box brackets, rosewood fretboard rendering, and Note/Interval quiz drills.
 */
public final class FretboardView extends View {
    private static final String PREFS_NAME = "com.eggwall.fretboard.prefs";

    private final GuitarSynthEngine synth;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint monoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF tmpRect = new RectF();
    private final Path inlayPath = new Path();
    private final Random rng = new Random();

    private int mode = FretboardTheory.MODE_SCALES;
    private int rootPc = 0;      /* 0=C .. 11=B */
    private int scaleIdx = 5;    /* 0..15 (default Minor Pentatonic) */
    private int chordIdx = 0;    /* 0..11 (default Major) */
    private int cagedPos = 0;    /* 0=All, 1..5=Pos 1..5 */
    private int voicingIdx = 0;  /* 0..4 CAGED shape */
    private int tuningIdx = 0;   /* 0=Standard .. 5=Open D */
    private int labelMode = FretboardTheory.LABEL_INTERVALS;
    private boolean leftHanded = false;
    private static final int DROPDOWN_NONE = 0;
    private static final int DROPDOWN_KEY = 1;
    private static final int DROPDOWN_MODE = 2;

    private int activeDropdown = DROPDOWN_NONE;
    private boolean showAllFrets = true;
    private boolean showHelp = false;

    private final int[] lockedFrets = { -1, 3, 2, 0, 1, 0 }; /* Default C Major */
    private final FretboardTheory.ComputedVoicing currentVoicing =
            new FretboardTheory.ComputedVoicing();
    private final FretboardTheory.IdentifiedChord identifiedChord =
            new FretboardTheory.IdentifiedChord();

    private int quizMode = FretboardTheory.QUIZ_OFF;
    private int quizTargetValue = 0;
    private int quizScore = 0;
    private int quizTotal = 0;
    private String quizFeedback = "";
    private boolean quizLastCorrect = true;
    private long quizFeedbackUntilMs = 0L;

    /* Cached layout rectangles for hit-testing */
    private final RectF[] modeTabRects = new RectF[3];
    private final RectF keyDropdownBtnRect = new RectF();
    private final RectF selectorBoxRect = new RectF();
    private final RectF dropdownPanelRect = new RectF();
    private final RectF[] keyDropdownItemRects = new RectF[12];
    private final RectF[] modeDropdownItemRects = new RectF[16];
    private final RectF posUpBtnRect = new RectF();
    private final RectF posBadgeRect = new RectF();
    private final RectF posDownBtnRect = new RectF();
    private final RectF helpBtnRect = new RectF();
    private final RectF playBtnRect = new RectF();
    private final RectF labelBtnRect = new RectF();
    private final RectF tuningBtnRect = new RectF();
    private final RectF handBtnRect = new RectF();
    private final RectF quizBtnRect = new RectF();
    private final RectF clearBtnRect = new RectF();
    private final RectF neckRect = new RectF();
    private boolean neckVertical = false;
    private float nutW;
    private float fretColW;
    private float strRowH;
    private float fretRowH;
    private float strColW;

    private final Runnable animTick = new Runnable() {
        @Override
        public void run() {
            if (synth.isHighlightActive()
                    || (quizFeedbackUntilMs > SystemClock.uptimeMillis())) {
                invalidate();
                postDelayed(this, 24L);
            }
        }
    };

    public FretboardView(Context context, GuitarSynthEngine synthEngine) {
        super(context);
        this.synth = synthEngine;
        setFocusable(true);
        setFocusableInTouchMode(true);
        for (int i = 0; i < modeTabRects.length; i++) modeTabRects[i] = new RectF();
        for (int i = 0; i < keyDropdownItemRects.length; i++) keyDropdownItemRects[i] = new RectF();
        for (int i = 0; i < modeDropdownItemRects.length; i++) modeDropdownItemRects[i] = new RectF();
        textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        monoPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        recomputeTheoryState();
    }

    public boolean isShowingHelp() {
        return showHelp || activeDropdown != DROPDOWN_NONE;
    }

    public void dismissHelp() {
        showHelp = false;
        activeDropdown = DROPDOWN_NONE;
        invalidate();
    }

    public void loadState(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mode = FretboardTheory.clampIndex(sp.getInt("mode", FretboardTheory.MODE_SCALES), 3);
        rootPc = FretboardTheory.clampIndex(sp.getInt("rootPc", 0), 12);
        scaleIdx = FretboardTheory.clampIndex(sp.getInt("scaleIdx", 5), FretboardTheory.SCALES.length);
        chordIdx = FretboardTheory.clampIndex(sp.getInt("chordIdx", 0), FretboardTheory.CHORDS.length);
        cagedPos = FretboardTheory.clampIndex(sp.getInt("cagedPos", 0), 6);
        voicingIdx = FretboardTheory.clampIndex(sp.getInt("voicingIdx", 0), 5);
        tuningIdx = FretboardTheory.clampIndex(sp.getInt("tuningIdx", 0), FretboardTheory.TUNINGS.length);
        labelMode = FretboardTheory.clampIndex(sp.getInt("labelMode", 0), 3);
        leftHanded = sp.getBoolean("leftHanded", false);
        showAllFrets = sp.getBoolean("showAllFrets", true);
        for (int s = 0; s < FretboardTheory.NUM_STRINGS; s++) {
            int def = lockedFrets[s];
            int val = sp.getInt("locked_" + s, def);
            if (val < -1 || val > FretboardTheory.MAX_FRET) val = def;
            lockedFrets[s] = val;
        }
        recomputeTheoryState();
        invalidate();
    }

    public void saveState(Context ctx) {
        SharedPreferences.Editor ed = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        ed.putInt("mode", mode)
          .putInt("rootPc", rootPc)
          .putInt("scaleIdx", scaleIdx)
          .putInt("chordIdx", chordIdx)
          .putInt("cagedPos", cagedPos)
          .putInt("voicingIdx", voicingIdx)
          .putInt("tuningIdx", tuningIdx)
          .putInt("labelMode", labelMode)
          .putBoolean("leftHanded", leftHanded)
          .putBoolean("showAllFrets", showAllFrets);
        for (int s = 0; s < FretboardTheory.NUM_STRINGS; s++) {
            ed.putInt("locked_" + s, lockedFrets[s]);
        }
        ed.apply();
    }

    private void recomputeTheoryState() {
        FretboardTheory.computeChordVoicing(tuningIdx, rootPc, chordIdx, voicingIdx, currentVoicing);
        FretboardTheory.identifyLockedChord(tuningIdx, lockedFrets, identifiedChord);
    }

    private void scheduleHighlightLoop() {
        removeCallbacks(animTick);
        postDelayed(animTick, 16L);
        invalidate();
    }

    public void triggerAudition() {
        if (mode == FretboardTheory.MODE_SCALES) {
            synth.triggerScaleAudition(tuningIdx, rootPc, scaleIdx, cagedPos);
        } else if (mode == FretboardTheory.MODE_CHORDS) {
            synth.triggerChordStrum(tuningIdx, currentVoicing.frets);
        } else {
            synth.triggerChordStrum(tuningIdx, lockedFrets);
        }
        scheduleHighlightLoop();
    }

    private void pickNextQuizQuestion() {
        quizTargetValue = rng.nextInt(12);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        boolean portrait = h > w;
        float pad = Math.max(8f, Math.min(w, h) * 0.016f);

        /* Background */
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF0E131A);
        canvas.drawRect(0, 0, w, h, paint);

        float curY = pad;
        float headerH = Math.max(48f, Math.min(68f, h * (portrait ? 0.056f : 0.105f)));
        drawHeaderBar(canvas, pad, curY, w - 2 * pad, headerH, portrait);
        curY += headerH + pad * 0.7f;

        float selectorRowH = Math.max(56f, Math.min(84f, h * (portrait ? 0.062f : 0.125f)));
        drawKeyAndSelectorRow(canvas, pad, curY, w - 2 * pad, selectorRowH, portrait);
        curY += selectorRowH + pad * 0.8f;

        float footerH = Math.max(54f, Math.min(82f, h * (portrait ? 0.075f : 0.145f)));
        float neckTop = curY;
        float neckBot = h - footerH - pad * 1.1f;
        float neckAreaH = Math.max(120f, neckBot - neckTop);
        neckVertical = portrait;
        if (portrait) {
            drawVerticalFretboardNeck(canvas, pad, neckTop, w - 2 * pad, neckAreaH);
        } else {
            float sideW = Math.max(136f, (w - 2 * pad) * 0.145f);
            float neckW = (w - 2 * pad) - sideW - pad;
            drawFretboardNeck(canvas, pad, neckTop, neckW, neckAreaH);
            drawSidePositionCard(canvas, pad + neckW + pad, neckTop + neckAreaH * 0.08f,
                    sideW, neckAreaH * 0.84f, false);
        }

        drawTheoryCardFooter(canvas, pad, h - footerH - pad * 0.5f, w - 2 * pad, footerH, portrait);

        if (activeDropdown != DROPDOWN_NONE) {
            drawDropdownModal(canvas, w, h, portrait, curY - pad * 0.3f);
        }
        if (showHelp) {
            drawHelpModal(canvas, w, h);
        }
    }

    private void drawHeaderBar(Canvas c, float x, float y, float w, float h, boolean portrait) {
        paint.setColor(0xFF17202C);
        tmpRect.set(x, y, x + w, y + h);
        c.drawRoundRect(tmpRect, 10f, 10f, paint);

        float titleW = portrait ? w * 0.28f : w * 0.22f;
        textPaint.setColor(0xFFE6EDF3);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(Math.min(h * 0.42f, titleW * 0.18f));
        c.drawText("FRETBOARD", x + 12f, y + h * 0.64f, textPaint);

        float helpW = h * 0.92f;
        helpBtnRect.set(x + w - helpW - 6f, y + 5f, x + w - 6f, y + h - 5f);
        drawPill(c, helpBtnRect, "?", showHelp, 0xFF2E3B4E, 0xFFD2A8FF);

        float playW = portrait ? h * 1.45f : h * 2.1f;
        playBtnRect.set(helpBtnRect.left - playW - 6f, y + 5f, helpBtnRect.left - 6f, y + h - 5f);
        String playLbl = (mode == FretboardTheory.MODE_SCALES) ? "\u25B6 Scale" : "\u25B6 Strum";
        drawPill(c, playBtnRect, portrait ? "\u25B6" : playLbl, false, 0xFF1F6FEB, 0xFF58A6FF);

        float tabsLeft = x + titleW;
        float tabsRight = playBtnRect.left - 8f;
        float tabW = (tabsRight - tabsLeft) / 3f;
        String[] tabNames = portrait
                ? new String[] { "Scales", "Chords", "Finder" }
                : new String[] { "1: Scales", "2: Chords", "3: Finder" };
        for (int i = 0; i < 3; i++) {
            modeTabRects[i].set(tabsLeft + i * tabW + 2f, y + 5f, tabsLeft + (i + 1) * tabW - 2f, y + h - 5f);
            drawPill(c, modeTabRects[i], tabNames[i], mode == i, 0xFF212B3A, 0xFF3FB950);
        }
    }

    /**
     * Draws the single tall Key dropdown button ("Key: C ▾") and the single tall
     * Scale/Mode/Chord dropdown button ("Dorian Mode ▾") across the top bar.
     * In Landscape mode, the 4 option pills sit to the right of the dropdown buttons.
     */
    private void drawKeyAndSelectorRow(Canvas c, float x, float y, float w, float h, boolean portrait) {
        float gap = 8f;
        float keyBtnW = portrait ? w * 0.33f : w * 0.17f;
        float modeBtnW = portrait ? (w - keyBtnW - gap) : w * 0.39f;

        keyDropdownBtnRect.set(x, y, x + keyBtnW, y + h);
        String keyLabel = "Key: " + FretboardTheory.NOTE_NAMES[rootPc] + " \u25BE";
        drawLargeDropdownButton(c, keyDropdownBtnRect, keyLabel,
                activeDropdown == DROPDOWN_KEY, 0xFF231C18, 0xFFF0883E);

        float modeX = keyDropdownBtnRect.right + gap;
        selectorBoxRect.set(modeX, y, modeX + modeBtnW, y + h);
        String modeLabel;
        int modeAccent;
        if (mode == FretboardTheory.MODE_SCALES) {
            FretboardTheory.ScaleDef sd = FretboardTheory.SCALES[scaleIdx];
            modeLabel = sd.name + " \u25BE";
            modeAccent = 0xFF58A6FF;
        } else if (mode == FretboardTheory.MODE_CHORDS) {
            FretboardTheory.ChordQualityDef cd = FretboardTheory.CHORDS[chordIdx];
            modeLabel = FretboardTheory.NOTE_NAMES[rootPc] + cd.suffix + " (" + cd.name + ") \u25BE";
            modeAccent = 0xFFA371F7;
        } else {
            modeLabel = "Finder: " + identifiedChord.chordName;
            modeAccent = 0xFF3FB950;
        }
        drawLargeDropdownButton(c, selectorBoxRect, modeLabel,
                activeDropdown == DROPDOWN_MODE, 0xFF192332, modeAccent);

        if (!portrait) {
            float optX = selectorBoxRect.right + gap;
            float optW = (x + w) - optX;
            float bw = optW / 4f;

            labelBtnRect.set(optX + 1.5f, y, optX + bw - 1.5f, y + h);
            tuningBtnRect.set(optX + bw + 1.5f, y, optX + 2 * bw - 1.5f, y + h);
            handBtnRect.set(optX + 2 * bw + 1.5f, y, optX + 3 * bw - 1.5f, y + h);
            quizBtnRect.set(optX + 3 * bw + 1.5f, y, optX + 4 * bw - 1.5f, y + h);

            String[] lblModeNames = { "Lbl: R/3/5", "Lbl: Notes", "Lbl: Deg" };
            drawPill(c, labelBtnRect, lblModeNames[labelMode], false, 0xFF1E293B, 0xFF79C0FF);
            drawPill(c, tuningBtnRect, FretboardTheory.TUNINGS[tuningIdx].shortName, tuningIdx != 0, 0xFF1E293B, 0xFFD2A8FF);
            drawPill(c, handBtnRect, leftHanded ? "Lefty" : "Righty", leftHanded, 0xFF1E293B, 0xFFFFA657);
            String[] qNames = { "Quiz: Off", "Quiz: Note", "Quiz: Intv" };
            drawPill(c, quizBtnRect, qNames[quizMode], quizMode != FretboardTheory.QUIZ_OFF, 0xFF1E293B, 0xFF3FB950);
        }
    }

    private void drawLargeDropdownButton(
            Canvas c, RectF r, String text, boolean open, int bgColor, int accentColor) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(open ? accentColor : bgColor);
        c.drawRoundRect(r, 10f, 10f, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.2f);
        paint.setColor(accentColor);
        c.drawRoundRect(r, 10f, 10f, paint);
        paint.setStyle(Paint.Style.FILL);

        textPaint.setColor(open ? 0xFF0B0F17 : 0xFFF0F6FC);
        textPaint.setTextAlign(Paint.Align.CENTER);
        float maxTs = Math.min(r.height() * 0.44f, (r.width() * 1.48f) / Math.max(6, text.length()));
        textPaint.setTextSize(Math.max(13f, maxTs));
        c.drawText(text, r.centerX(), r.centerY() + textPaint.getTextSize() * 0.35f, textPaint);
    }

    private void drawFretboardNeck(Canvas c, float x, float y, float w, float h) {
        float numRowH = Math.max(18f, h * 0.11f);
        float boardTop = y + numRowH;
        float boardH = h - numRowH * 1.85f;
        neckRect.set(x, boardTop, x + w, boardTop + boardH);

        nutW = Math.max(30f, w * 0.056f);
        fretColW = (w - nutW) / FretboardTheory.MAX_FRET;
        strRowH = boardH / FretboardTheory.NUM_STRINGS;

        /* Rosewood neck with subtle vertical gradient */
        paint.setShader(new LinearGradient(x, boardTop, x, boardTop + boardH,
                0xFF3E2618, 0xFF28170E, Shader.TileMode.CLAMP));
        paint.setStyle(Paint.Style.FILL);
        c.drawRoundRect(neckRect, 8f, 8f, paint);
        paint.setShader(null);

        /* Nut column */
        paint.setColor(0xFFE8DFCA);
        if (!leftHanded) {
            c.drawRect(x + nutW - 6f, boardTop, x + nutW, boardTop + boardH, paint);
        } else {
            c.drawRect(x + w - nutW, boardTop, x + w - nutW + 6f, boardTop + boardH, paint);
        }

        /* Fret wires and fret numbers */
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(Math.min(numRowH * 0.72f, fretColW * 0.44f));
        for (int f = 0; f <= FretboardTheory.MAX_FRET; f++) {
            float cx = getFretCenterX(f);
            textPaint.setColor((f == 0 || f == 3 || f == 5 || f == 7 || f == 9 || f == 12 || f == 15)
                    ? 0xFFD8DEE9 : 0xFF7D8590);
            c.drawText(f == 0 ? "OPEN" : Integer.toString(f), cx, y + numRowH * 0.74f, textPaint);
            if (f > 0) {
                float wireX = leftHanded
                        ? (x + w - nutW - f * fretColW)
                        : (x + nutW + f * fretColW);
                paint.setColor(0xFF9EA7B3);
                paint.setStrokeWidth(2.2f);
                c.drawLine(wireX, boardTop, wireX, boardTop + boardH, paint);
            }
        }

        /* Pearloid ~ wave fret markers at 3, 5, 7, 9, 12, 15 (12th fret between A&D and G&B) */
        int[] inlays = { 3, 5, 7, 9, 12, 15 };
        for (int f : inlays) {
            float cx = getFretCenterX(f);
            if (f == 12) {
                drawFretInlayWave(c, cx, boardTop + strRowH * 2.0f, fretColW * 0.26f, strRowH * 0.085f);
                drawFretInlayWave(c, cx, boardTop + strRowH * 4.0f, fretColW * 0.26f, strRowH * 0.085f);
            } else {
                drawFretInlayWave(c, cx, boardTop + boardH * 0.5f, fretColW * 0.26f, strRowH * 0.085f);
            }
        }

        /* CAGED box highlight or Chord Voicing span bracket */
        int boxLo = 0, boxHi = FretboardTheory.MAX_FRET;
        if (mode == FretboardTheory.MODE_SCALES) {
            if (cagedPos > 0) {
                int[] range = FretboardTheory.computeCagedBoxRange(tuningIdx, rootPc, cagedPos);
                boxLo = range[0];
                boxHi = range[1];
                drawFretSpanHighlight(c, boxLo, boxHi, boardTop, boardH, 0x263FB950, 0xFF3FB950,
                        FretboardTheory.CAGED_POS_NAMES[cagedPos]);
            } else {
                drawFretSpanHighlight(c, 0, FretboardTheory.MAX_FRET, boardTop, boardH,
                        0x103FB950, 0x883FB950, "ALL NECK (FRETS 0\u201315)");
            }
        } else if (mode == FretboardTheory.MODE_CHORDS) {
            drawFretSpanHighlight(c, currentVoicing.baseFret,
                    Math.max(currentVoicing.baseFret, currentVoicing.maxFret),
                    boardTop, boardH, 0x22A371F7, 0xFFA371F7,
                    FretboardTheory.CAGED_POS_NAMES[voicingIdx + 1]);
        }

        /* Strings (display row 0 = high 1st string s=5 .. row 5 = low 6th string s=0) */
        for (int s = 0; s < FretboardTheory.NUM_STRINGS; s++) {
            float sy = getStringCenterY(s);
            paint.setColor(s < 3 ? 0xFFD0B38B : 0xFFD8DEE9);
            paint.setStrokeWidth(1.4f + (5 - s) * 0.52f);
            c.drawLine(x, sy, x + w, sy, paint);
        }

        /* Barre chord indicator bar in Chords mode */
        if (mode == FretboardTheory.MODE_CHORDS && currentVoicing.barreFret > 0) {
            float bx = getFretCenterX(currentVoicing.barreFret);
            float topY = getStringCenterY(5);
            float botY = getStringCenterY(currentVoicing.barreLowStr);
            float barR = Math.min(fretColW, strRowH) * 0.28f;
            paint.setColor(0x9958A6FF);
            tmpRect.set(bx - barR, topY - barR, bx + barR, botY + barR);
            c.drawRoundRect(tmpRect, barR, barR, paint);
        }

        /* Draw Note Dots across all 6 strings and 16 frets */
        float dotR = Math.min(fretColW * 0.37f, strRowH * 0.35f);
        boolean hiActive = synth.isHighlightActive();
        int hiStr = synth.getActivePlayString();
        int hiFret = synth.getActivePlayFret();

        for (int s = 0; s < FretboardTheory.NUM_STRINGS; s++) {
            float sy = getStringCenterY(s);
            if (mode == FretboardTheory.MODE_CHORDS && currentVoicing.frets[s] < 0) {
                drawMutedMarker(c, getFretCenterX(0), sy, dotR);
            } else if (mode == FretboardTheory.MODE_EXPLORER && lockedFrets[s] < 0) {
                drawMutedMarker(c, getFretCenterX(0), sy, dotR);
            }

            for (int f = 0; f <= FretboardTheory.MAX_FRET; f++) {
                int pc = FretboardTheory.getPitchClass(tuningIdx, s, f);
                int relIv = (pc - rootPc + 12) % 12;
                boolean isPlucked = hiActive && hiStr == s && hiFret == f;
                drawFretNote(c, s, f, pc, relIv, boxLo, boxHi, dotR, isPlucked);
            }
        }
    }

    /**
     * Renders the 15-fret guitar neck vertically along the length of the Portrait screen
     * (narrowed to leave room for the right-hand side control rail), with Open/low frets at
     * the top, Fret 15 at the bottom, and the 6 strings arranged left-to-right.
     */
    private void drawVerticalFretboardNeck(Canvas c, float x, float y, float w, float h) {
        float strHeaderH = Math.max(24f, h * 0.032f);
        float leftGutterW = Math.max(46f, w * 0.115f);
        float sideRailW = Math.max(132f, w * 0.305f);
        float gap = 12f;
        float boardW = Math.min(w - leftGutterW - sideRailW - gap, h * 0.36f);
        float boardLeft = x + leftGutterW;
        float boardTop = y + strHeaderH;
        float boardH = h - strHeaderH - 4f;
        neckRect.set(boardLeft, boardTop, boardLeft + boardW, boardTop + boardH);

        /* Right Side Rail: Position Up/Down selector card + 4 tall utility buttons */
        float sideX = boardLeft + boardW + gap;
        float sideActualW = (x + w) - sideX;
        float posCardH = boardH * 0.44f;
        drawSidePositionCard(c, sideX, boardTop, sideActualW, posCardH, true);
        drawPortraitSideUtilityColumn(c, sideX, boardTop + posCardH + 12f,
                sideActualW, boardH - posCardH - 12f);

        nutW = Math.max(36f, boardH * 0.056f);
        fretRowH = (boardH - nutW) / FretboardTheory.MAX_FRET;
        strColW = boardW / FretboardTheory.NUM_STRINGS;

        /* Rosewood neck with subtle horizontal woodgrain gradient */
        paint.setShader(new LinearGradient(boardLeft, boardTop, boardLeft + boardW, boardTop,
                0xFF28170E, 0xFF3E2618, Shader.TileMode.CLAMP));
        paint.setStyle(Paint.Style.FILL);
        c.drawRoundRect(neckRect, 10f, 10f, paint);
        paint.setShader(null);

        /* Top string labels (E A D G B e for Righty, e B G D A E for Lefty) */
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(Math.min(strHeaderH * 0.68f, strColW * 0.38f));
        textPaint.setColor(0xFFC8B89A);
        for (int s = 0; s < FretboardTheory.NUM_STRINGS; s++) {
            float sx = getStringCoordX(s);
            int openPc = FretboardTheory.getPitchClass(tuningIdx, s, 0);
            String sLbl = (s == 5 && openPc == 4) ? "e" : FretboardTheory.NOTE_NAMES[openPc];
            c.drawText(sLbl, sx, y + strHeaderH * 0.72f, textPaint);
        }

        /* Horizontal ivory nut bar at the top of the neck (below fret 0 / OPEN row) */
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFFE8DFCA);
        c.drawRect(boardLeft, boardTop + nutW - 6f, boardLeft + boardW, boardTop + nutW + 1f, paint);

        /* Horizontal fret wires (f = 1..15) and fret numbers along the left gutter */
        textPaint.setTextAlign(Paint.Align.RIGHT);
        float fretNumTs = Math.min(fretRowH * 0.44f, leftGutterW * 0.42f);
        textPaint.setTextSize(fretNumTs);
        for (int f = 0; f <= FretboardTheory.MAX_FRET; f++) {
            float cy = getFretCoordY(f);
            boolean isMarkerFret = (f == 0 || f == 3 || f == 5 || f == 7 || f == 9 || f == 12 || f == 15);
            textPaint.setColor(isMarkerFret ? 0xFFD8DEE9 : 0xFF7D8590);
            c.drawText(f == 0 ? "OPEN" : Integer.toString(f), boardLeft - 12f, cy + fretNumTs * 0.34f, textPaint);
            if (f > 0) {
                float wireY = boardTop + nutW + f * fretRowH;
                paint.setColor(0xFF9EA7B3);
                paint.setStrokeWidth(2.2f);
                c.drawLine(boardLeft, wireY, boardLeft + boardW, wireY, paint);
            }
        }

        /* Pearloid ~ wave fret markers at 3, 5, 7, 9, 12, 15 (12th fret between A&D and G&B) */
        int[] inlays = { 3, 5, 7, 9, 12, 15 };
        for (int f : inlays) {
            float cy = getFretCoordY(f);
            if (f == 12) {
                drawFretInlayWave(c, boardLeft + strColW * 2.0f, cy, strColW * 0.145f, fretRowH * 0.11f);
                drawFretInlayWave(c, boardLeft + strColW * 4.0f, cy, strColW * 0.145f, fretRowH * 0.11f);
            } else {
                drawFretInlayWave(c, boardLeft + boardW * 0.5f, cy, strColW * 0.145f, fretRowH * 0.11f);
            }
        }

        /* CAGED box highlight or Chord Voicing span bracket (vertical range) */
        int boxLo = 0, boxHi = FretboardTheory.MAX_FRET;
        if (mode == FretboardTheory.MODE_SCALES) {
            if (cagedPos > 0) {
                int[] range = FretboardTheory.computeCagedBoxRange(tuningIdx, rootPc, cagedPos);
                boxLo = range[0];
                boxHi = range[1];
                drawVerticalFretSpanHighlight(c, boxLo, boxHi, boardLeft, boardW, 0x263FB950, 0xFF3FB950);
            } else {
                drawVerticalFretSpanHighlight(c, 0, FretboardTheory.MAX_FRET, boardLeft, boardW,
                        0x103FB950, 0x883FB950);
            }
        } else if (mode == FretboardTheory.MODE_CHORDS) {
            drawVerticalFretSpanHighlight(c, currentVoicing.baseFret,
                    Math.max(currentVoicing.baseFret, currentVoicing.maxFret),
                    boardLeft, boardW, 0x22A371F7, 0xFFA371F7);
        }

        /* 6 Vertical Guitar Strings */
        for (int s = 0; s < FretboardTheory.NUM_STRINGS; s++) {
            float sx = getStringCoordX(s);
            paint.setColor(s < 3 ? 0xFFD0B38B : 0xFFD8DEE9);
            paint.setStrokeWidth(1.5f + (5 - s) * 0.52f);
            c.drawLine(sx, boardTop, sx, boardTop + boardH, paint);
        }

        /* Horizontal Barre chord indicator bar in Chords mode */
        if (mode == FretboardTheory.MODE_CHORDS && currentVoicing.barreFret > 0) {
            float by = getFretCoordY(currentVoicing.barreFret);
            float x1 = getStringCoordX(5);
            float x2 = getStringCoordX(currentVoicing.barreLowStr);
            float barR = Math.min(fretRowH, strColW) * 0.28f;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0x9958A6FF);
            tmpRect.set(Math.min(x1, x2) - barR, by - barR, Math.max(x1, x2) + barR, by + barR);
            c.drawRoundRect(tmpRect, barR, barR, paint);
        }

        /* Draw Note Dots across all 6 strings and 16 frets */
        float dotR = Math.min(fretRowH * 0.36f, strColW * 0.34f);
        boolean hiActive = synth.isHighlightActive();
        int hiStr = synth.getActivePlayString();
        int hiFret = synth.getActivePlayFret();

        for (int s = 0; s < FretboardTheory.NUM_STRINGS; s++) {
            float sx = getStringCoordX(s);
            if (mode == FretboardTheory.MODE_CHORDS && currentVoicing.frets[s] < 0) {
                drawMutedMarker(c, sx, getFretCoordY(0), dotR);
            } else if (mode == FretboardTheory.MODE_EXPLORER && lockedFrets[s] < 0) {
                drawMutedMarker(c, sx, getFretCoordY(0), dotR);
            }

            for (int f = 0; f <= FretboardTheory.MAX_FRET; f++) {
                int pc = FretboardTheory.getPitchClass(tuningIdx, s, f);
                int relIv = (pc - rootPc + 12) % 12;
                boolean isPlucked = hiActive && hiStr == s && hiFret == f;
                drawFretNote(c, s, f, pc, relIv, boxLo, boxHi, dotR, isPlucked);
            }
        }
    }

    /**
     * Draws the Side Position Card with Up (▲) and Down (▼) arrows on the side of the screen
     * to step through All Neck and CAGED Positions 1..5 (or Chord Voicings 1..5), or Mute All in Finder mode.
     */
    private void drawSidePositionCard(Canvas c, float x, float y, float w, float h, boolean portrait) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF151E2A);
        tmpRect.set(x, y, x + w, y + h);
        c.drawRoundRect(tmpRect, 12f, 12f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.6f);
        paint.setColor(0xFF2B384B);
        c.drawRoundRect(tmpRect, 12f, 12f, paint);
        paint.setStyle(Paint.Style.FILL);

        float inPad = 8f;
        if (mode == FretboardTheory.MODE_EXPLORER) {
            posUpBtnRect.setEmpty();
            posDownBtnRect.setEmpty();
            posBadgeRect.setEmpty();
            clearBtnRect.set(x + inPad, y + h * 0.22f, x + w - inPad, y + h * 0.78f);
            drawPill(c, clearBtnRect, "Mute All (X)", false, 0xFF3A1E24, 0xFFF85149);
            return;
        }
        clearBtnRect.setEmpty();

        float titleH = Math.max(20f, h * 0.11f);
        textPaint.setColor(0xFF8B949E);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(Math.min(titleH * 0.72f, w * 0.12f));
        c.drawText("POSITION", x + w * 0.5f, y + inPad + titleH * 0.72f, textPaint);

        float bodyTop = y + inPad + titleH + 4f;
        float bodyH = (y + h - inPad) - bodyTop;
        float btnH = bodyH * 0.28f;
        float gap = 6f;
        float badgeH = bodyH - 2 * btnH - 2 * gap;

        posUpBtnRect.set(x + inPad, bodyTop, x + w - inPad, bodyTop + btnH);
        posBadgeRect.set(x + inPad, posUpBtnRect.bottom + gap, x + w - inPad, posUpBtnRect.bottom + gap + badgeH);
        posDownBtnRect.set(x + inPad, posBadgeRect.bottom + gap, x + w - inPad, bodyTop + bodyH);

        int accent = (mode == FretboardTheory.MODE_CHORDS) ? 0xFFA371F7 : 0xFF3FB950;
        drawPill(c, posUpBtnRect, "\u25B2", false, 0xFF212B3A, accent);
        drawPill(c, posDownBtnRect, "\u25BC", false, 0xFF212B3A, accent);

        /* Highlighted Position Badge in the center */
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF1B2736);
        c.drawRoundRect(posBadgeRect, 8f, 8f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);
        paint.setColor(accent);
        c.drawRoundRect(posBadgeRect, 8f, 8f, paint);
        paint.setStyle(Paint.Style.FILL);

        String posTitle;
        String fretSub;
        if (mode == FretboardTheory.MODE_SCALES) {
            if (cagedPos == 0) {
                posTitle = "ALL NECK";
                fretSub = "Frets 0\u201315";
            } else {
                int[] range = FretboardTheory.computeCagedBoxRange(tuningIdx, rootPc, cagedPos);
                posTitle = FretboardTheory.CAGED_POS_NAMES[cagedPos];
                fretSub = String.format(Locale.US, "Frets %d\u2013%d", range[0], range[1]);
            }
        } else {
            posTitle = FretboardTheory.CAGED_POS_NAMES[voicingIdx + 1];
            fretSub = String.format(Locale.US, "Frets %d\u2013%d",
                    currentVoicing.baseFret, Math.max(currentVoicing.baseFret, currentVoicing.maxFret));
        }

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(accent);
        float ts1 = Math.min(posBadgeRect.height() * 0.28f, (posBadgeRect.width() * 1.45f) / Math.max(6, posTitle.length()));
        textPaint.setTextSize(Math.max(11f, ts1));
        c.drawText(posTitle, posBadgeRect.centerX(), posBadgeRect.centerY() - posBadgeRect.height() * 0.06f, textPaint);

        textPaint.setColor(0xFFD8DEE9);
        float ts2 = Math.min(posBadgeRect.height() * 0.22f, posBadgeRect.width() * 0.14f);
        textPaint.setTextSize(Math.max(10f, ts2));
        c.drawText(fretSub, posBadgeRect.centerX(), posBadgeRect.centerY() + posBadgeRect.height() * 0.28f, textPaint);
    }

    /**
     * Stacks the 4 utility buttons (Labels, Tuning, Handedness, Quiz) along the lower
     * portion of the Portrait right-side rail with generous button height.
     */
    private void drawPortraitSideUtilityColumn(Canvas c, float x, float y, float w, float h) {
        float gap = 8f;
        float btnH = (h - 3 * gap) / 4f;

        labelBtnRect.set(x, y, x + w, y + btnH);
        tuningBtnRect.set(x, y + (btnH + gap), x + w, y + 2 * btnH + gap);
        handBtnRect.set(x, y + 2 * (btnH + gap), x + w, y + 3 * btnH + 2 * gap);
        quizBtnRect.set(x, y + 3 * (btnH + gap), x + w, y + 4 * btnH + 3 * gap);

        String[] lblModeNames = { "Lbl: R/3/5", "Lbl: Notes", "Lbl: Deg" };
        drawPill(c, labelBtnRect, lblModeNames[labelMode], false, 0xFF1E293B, 0xFF79C0FF);
        drawPill(c, tuningBtnRect, "Tune: " + FretboardTheory.TUNINGS[tuningIdx].shortName,
                tuningIdx != 0, 0xFF1E293B, 0xFFD2A8FF);
        drawPill(c, handBtnRect, leftHanded ? "Hand: Lefty" : "Hand: Righty",
                leftHanded, 0xFF1E293B, 0xFFFFA657);
        String[] qNames = { "Quiz: Off", "Quiz: Note", "Quiz: Intv" };
        drawPill(c, quizBtnRect, qNames[quizMode], quizMode != FretboardTheory.QUIZ_OFF, 0xFF1E293B, 0xFF3FB950);
    }

    /**
     * Renders the drop-down overlay menu when the user taps either the Key button (all 12 keys)
     * or the Mode/Scale/Chord button (all 14 scales/modes or all 12 chord qualities).
     */
    private void drawDropdownModal(Canvas c, int w, int h, boolean portrait, float topY) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xB806090F);
        c.drawRect(0, topY, w, h, paint);

        float pad = Math.max(12f, w * 0.025f);
        if (activeDropdown == DROPDOWN_KEY) {
            int cols = 4;
            int rows = 3;
            float panelW = portrait ? (w - 2 * pad) : Math.min(w * 0.56f, 640f);
            float cellH = portrait ? Math.max(60f, h * 0.058f) : Math.max(52f, h * 0.095f);
            float headerH = 38f;
            float panelH = headerH + rows * cellH + (rows + 1) * 10f + 8f;
            float panelX = portrait ? pad : keyDropdownBtnRect.left;
            if (panelX + panelW > w - pad) panelX = w - pad - panelW;

            dropdownPanelRect.set(panelX, topY + 4f, panelX + panelW, topY + 4f + panelH);
            paint.setColor(0xFF16202E);
            c.drawRoundRect(dropdownPanelRect, 14f, 14f, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.2f);
            paint.setColor(0xFFF0883E);
            c.drawRoundRect(dropdownPanelRect, 14f, 14f, paint);
            paint.setStyle(Paint.Style.FILL);

            textPaint.setColor(0xFFF0883E);
            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setTextSize(20f);
            c.drawText("SELECT KEY (ROOT NOTE)", panelX + 16f, topY + 32f, textPaint);

            float gridTop = topY + 4f + headerH + 6f;
            float gap = 10f;
            float cellW = (panelW - (cols + 1) * gap) / cols;
            for (int k = 0; k < 12; k++) {
                int r = k / cols;
                int col = k % cols;
                float cx = panelX + gap + col * (cellW + gap);
                float cy = gridTop + r * (cellH + gap);
                keyDropdownItemRects[k].set(cx, cy, cx + cellW, cy + cellH);
                drawPill(c, keyDropdownItemRects[k], FretboardTheory.NOTE_NAMES[k],
                        rootPc == k, 0xFF212B3A, 0xFFF0883E);
            }
        } else if (activeDropdown == DROPDOWN_MODE) {
            int count = (mode == FretboardTheory.MODE_CHORDS)
                    ? FretboardTheory.CHORDS.length
                    : FretboardTheory.SCALES.length;
            int cols = 2;
            int rows = (count + cols - 1) / cols;
            float panelW = portrait ? (w - 2 * pad) : Math.min(w * 0.76f, 860f);
            float cellH = portrait ? Math.max(54f, h * 0.050f) : Math.max(44f, h * 0.076f);
            float headerH = 38f;
            float gap = 8f;
            float panelH = headerH + rows * cellH + (rows + 1) * gap + 8f;
            float panelX = portrait ? pad : Math.max(pad, selectorBoxRect.left - 40f);
            if (panelX + panelW > w - pad) panelX = w - pad - panelW;

            int accent = (mode == FretboardTheory.MODE_CHORDS) ? 0xFFA371F7 : 0xFF58A6FF;
            dropdownPanelRect.set(panelX, topY + 4f, panelX + panelW, topY + 4f + panelH);
            paint.setColor(0xFF16202E);
            c.drawRoundRect(dropdownPanelRect, 14f, 14f, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.2f);
            paint.setColor(accent);
            c.drawRoundRect(dropdownPanelRect, 14f, 14f, paint);
            paint.setStyle(Paint.Style.FILL);

            textPaint.setColor(accent);
            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setTextSize(20f);
            String title = (mode == FretboardTheory.MODE_CHORDS)
                    ? "SELECT CHORD QUALITY" : "SELECT SCALE / MODE";
            c.drawText(title, panelX + 16f, topY + 32f, textPaint);

            float gridTop = topY + 4f + headerH + 4f;
            float cellW = (panelW - (cols + 1) * gap) / cols;
            for (int i = 0; i < modeDropdownItemRects.length; i++) {
                if (i >= count) {
                    modeDropdownItemRects[i].setEmpty();
                    continue;
                }
                int r = i / cols;
                int col = i % cols;
                float cx = panelX + gap + col * (cellW + gap);
                float cy = gridTop + r * (cellH + gap);
                modeDropdownItemRects[i].set(cx, cy, cx + cellW, cy + cellH);
                boolean selected;
                String lbl;
                if (mode == FretboardTheory.MODE_CHORDS) {
                    FretboardTheory.ChordQualityDef cd = FretboardTheory.CHORDS[i];
                    lbl = FretboardTheory.NOTE_NAMES[rootPc] + cd.suffix + " \u2014 " + cd.name;
                    selected = (chordIdx == i);
                } else {
                    lbl = FretboardTheory.SCALES[i].name;
                    selected = (scaleIdx == i);
                }
                drawPill(c, modeDropdownItemRects[i], lbl, selected, 0xFF212B3A, accent);
            }
        }
    }

    /**
     * Draws a pearloid tilde (~) wave inlay centered at (cx, cy) inside the inter-string gap,
     * visually distinct from circular fingering/scale note dots.
     */
    private void drawFretInlayWave(Canvas c, float cx, float cy, float halfW, float amp) {
        float strokeW = Math.max(2.6f, Math.min(halfW, amp) * 0.58f);

        inlayPath.reset();
        inlayPath.moveTo(cx - halfW, cy + amp * 0.35f);
        inlayPath.cubicTo(
                cx - halfW * 0.42f, cy - amp * 1.55f,
                cx + halfW * 0.42f, cy + amp * 1.55f,
                cx + halfW, cy - amp * 0.35f);

        Paint.Cap prevCap = paint.getStrokeCap();
        Paint.Join prevJoin = paint.getStrokeJoin();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);

        /* Subtle dark wood-recess outline followed by pearloid wave stroke */
        paint.setStrokeWidth(strokeW + 1.8f);
        paint.setColor(0x66140B06);
        c.drawPath(inlayPath, paint);

        paint.setStrokeWidth(strokeW);
        paint.setColor(0x99E6EDF3);
        c.drawPath(inlayPath, paint);

        paint.setStrokeCap(prevCap);
        paint.setStrokeJoin(prevJoin);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawVerticalFretSpanHighlight(
            Canvas c, int fLo, int fHi, float left, float w, int fillColor, int strokeColor) {
        float y1 = getFretCoordY(fLo) - fretRowH * 0.46f;
        float y2 = getFretCoordY(fHi) + fretRowH * 0.46f;
        tmpRect.set(left + 2f, Math.min(y1, y2), left + w - 2f, Math.max(y1, y2));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fillColor);
        c.drawRoundRect(tmpRect, 8f, 8f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(strokeColor);
        c.drawRoundRect(tmpRect, 8f, 8f, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawFretSpanHighlight(
            Canvas c, int fLo, int fHi, float top, float h, int fillColor, int strokeColor, String caption) {
        float x1 = getFretCenterX(fLo) - fretColW * 0.46f;
        float x2 = getFretCenterX(fHi) + fretColW * 0.46f;
        float left = Math.min(x1, x2);
        float right = Math.max(x1, x2);
        tmpRect.set(left, top + 2f, right, top + h - 2f);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fillColor);
        c.drawRoundRect(tmpRect, 8f, 8f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(strokeColor);
        c.drawRoundRect(tmpRect, 8f, 8f, paint);
        paint.setStyle(Paint.Style.FILL);

        textPaint.setColor(strokeColor);
        textPaint.setTextSize(Math.min(h * 0.085f, 22f));
        textPaint.setTextAlign(Paint.Align.CENTER);
        c.drawText(caption, (left + right) * 0.5f, top + h + textPaint.getTextSize() * 1.15f, textPaint);
    }

    private void drawMutedMarker(Canvas c, float cx, float cy, float r) {
        textPaint.setColor(0xFFF85149);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(r * 1.25f);
        c.drawText("X", cx, cy + r * 0.42f, textPaint);
    }

    private void drawFretNote(
            Canvas c, int s, int f, int pc, int relIv, int boxLo, int boxHi, float r, boolean isPlucked) {
        float cx = neckVertical ? getStringCoordX(s) : getFretCenterX(f);
        float cy = neckVertical ? getFretCoordY(f) : getStringCenterY(s);

        if (quizMode != FretboardTheory.QUIZ_OFF) {
            paint.setColor(isPlucked ? 0xFF3FB950 : 0x4430363D);
            c.drawCircle(cx, cy, r * 0.72f, paint);
            return;
        }

        boolean active = false;
        boolean ghost = false;
        int fingerNum = 0;

        if (mode == FretboardTheory.MODE_SCALES) {
            int inScale = FretboardTheory.SCALES[scaleIdx].mask[relIv];
            if (inScale > 0) {
                if (cagedPos == 0 || (f >= boxLo && f <= boxHi)) {
                    active = true;
                } else if (showAllFrets) {
                    ghost = true;
                }
            }
        } else if (mode == FretboardTheory.MODE_CHORDS) {
            if (currentVoicing.frets[s] == f) {
                active = true;
                fingerNum = currentVoicing.fingers[s];
            } else if (FretboardTheory.CHORDS[chordIdx].mask[relIv] > 0) {
                ghost = true;
            }
        } else {
            if (lockedFrets[s] == f) {
                active = true;
            } else if (showAllFrets) {
                ghost = true;
            }
        }

        if (!active && !ghost && !isPlucked) return;

        int fill = getIntervalFillColor(relIv);
        int alpha = ghost && !isPlucked ? 78 : 242;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor((fill & 0x00FFFFFF) | (alpha << 24));
        float drawR = isPlucked ? r * 1.18f : (ghost ? r * 0.74f : r);
        c.drawCircle(cx, cy, drawR, paint);

        if (isPlucked || relIv == 0) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(isPlucked ? 3.2f : 2.0f);
            paint.setColor(isPlucked ? 0xFFFFFFFF : 0xFFFFD866);
            c.drawCircle(cx, cy, drawR, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        String lbl = FretboardTheory.formatDotLabel(mode, labelMode, pc, relIv, fingerNum);
        textPaint.setColor(ghost && !isPlucked ? 0xBBE6EDF3 : 0xFFFFFFFF);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(drawR * (lbl.length() > 2 ? 0.68f : 0.82f));
        c.drawText(lbl, cx, cy + textPaint.getTextSize() * 0.35f, textPaint);
    }

    private static int getIntervalFillColor(int relIv) {
        if (relIv == 0) return 0xFFEA6A2A;                      /* Root: Warm Amber-Red */
        if (relIv == 3 || relIv == 4) return 0xFF2EA043;        /* 3rd: Emerald */
        if (relIv == 7) return 0xFF1F6FEB;                      /* 5th: Royal Blue */
        if (relIv == 10 || relIv == 11) return 0xFF8957E5;      /* 7th: Purple */
        if (relIv == 6) return 0xFFD29922;                      /* Blue note b5: Gold */
        return 0xFF485466;                                      /* Passing scale tone */
    }

    private void drawTheoryCardFooter(Canvas c, float x, float y, float w, float h, boolean portrait) {
        paint.setColor(0xFF161F2B);
        tmpRect.set(x, y, x + w, y + h);
        c.drawRoundRect(tmpRect, 10f, 10f, paint);

        String line1, line2, rightBadge;
        if (quizMode != FretboardTheory.QUIZ_OFF) {
            line1 = FretboardTheory.formatQuizPrompt(quizMode, rootPc, quizTargetValue, quizScore, quizTotal);
            line2 = (SystemClock.uptimeMillis() < quizFeedbackUntilMs && !quizFeedback.isEmpty())
                    ? quizFeedback
                    : "Tap any matching fret on the 6 strings to score.";
            rightBadge = String.format(Locale.US, "Score: %d/%d", quizScore, quizTotal);
        } else if (mode == FretboardTheory.MODE_SCALES) {
            FretboardTheory.ScaleDef sd = FretboardTheory.SCALES[scaleIdx];
            line1 = String.format(Locale.US, "%s %s  \u2022  Formula: %s  \u2022  Notes: %s",
                    FretboardTheory.NOTE_NAMES[rootPc], sd.name, sd.formula,
                    FretboardTheory.formatScaleNotes(rootPc, scaleIdx));
            line2 = String.format(Locale.US, "Skeleton Chord: %s  |  Position: %s  |  Tap any fret to audition note",
                    sd.skeletonChord, FretboardTheory.CAGED_POS_NAMES[cagedPos]);
            rightBadge = FretboardTheory.TUNINGS[tuningIdx].name;
        } else if (mode == FretboardTheory.MODE_CHORDS) {
            FretboardTheory.ChordQualityDef cd = FretboardTheory.CHORDS[chordIdx];
            line1 = String.format(Locale.US, "%s%s (%s)  \u2022  Formula: %s  \u2022  Notes: %s",
                    FretboardTheory.NOTE_NAMES[rootPc], cd.suffix, cd.name, cd.formula,
                    FretboardTheory.formatChordNotes(rootPc, chordIdx));
            line2 = String.format(Locale.US, "Voicing: %s  |  Frets (6\u21921): %s",
                    FretboardTheory.CAGED_POS_NAMES[voicingIdx + 1],
                    FretboardTheory.formatVoicingTab(currentVoicing));
            rightBadge = currentVoicing.barreFret > 0
                    ? ("Barre Fret " + currentVoicing.barreFret) : "Open / Movable";
        } else {
            line1 = "Identified Chord: " + identifiedChord.chordName + "   \u2022   Notes: " + identifiedChord.notesStr;
            line2 = "Tap any fret to lock/mute string  |  Tap \u25B6 Strum to hear custom voicing";
            rightBadge = FretboardTheory.TUNINGS[tuningIdx].name;
        }

        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setColor(0xFFF0F6FC);
        textPaint.setTextSize(Math.min(h * 0.30f, portrait ? w * 0.033f : w * 0.022f));
        c.drawText(line1, x + 14f, y + h * 0.42f, textPaint);

        textPaint.setColor(quizMode != FretboardTheory.QUIZ_OFF && !quizLastCorrect ? 0xFFF85149 : 0xFF8B949E);
        textPaint.setTextSize(Math.min(h * 0.25f, portrait ? w * 0.029f : w * 0.019f));
        c.drawText(line2, x + 14f, y + h * 0.78f, textPaint);

        if (!portrait) {
            monoPaint.setColor(0xFF58A6FF);
            monoPaint.setTextAlign(Paint.Align.RIGHT);
            monoPaint.setTextSize(Math.min(h * 0.27f, w * 0.020f));
            c.drawText(rightBadge, x + w - 14f, y + h * 0.44f, monoPaint);
        }
    }

    private void drawHelpModal(Canvas c, int w, int h) {
        paint.setColor(0xCC06090F);
        c.drawRect(0, 0, w, h, paint);

        float mw = Math.min(w * 0.88f, 760f);
        float mh = Math.min(h * 0.82f, 460f);
        float mx = (w - mw) * 0.5f;
        float my = (h - mh) * 0.5f;
        tmpRect.set(mx, my, mx + mw, my + mh);
        paint.setColor(0xFF182230);
        c.drawRoundRect(tmpRect, 16f, 16f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(0xFF58A6FF);
        c.drawRoundRect(tmpRect, 16f, 16f, paint);
        paint.setStyle(Paint.Style.FILL);

        String[] lines = {
            "FRETBOARD EXPLORER \u2014 QUICK GUIDE",
            "\u2022 Modes: Switch between 1: Scales (16 scales + 5 CAGED boxes), 2: Chords (12 qualities), and 3: Chord Finder.",
            "\u2022 Audio Synthesis: Tap any fret to pluck a Karplus-Strong guitar note, or tap \u25B6 to strum/audition.",
            "\u2022 Color Legend: Root (Amber)  |  3rd (Emerald)  |  5th (Royal Blue)  |  7th (Purple)  |  Blue b5 (Gold).",
            "\u2022 Labels & Tunings: Cycle Intervals (R,3,5), Note Names, Scale Degrees, 6 Guitar Tunings, and Lefty flip.",
            "\u2022 Fretboard Quiz: Toggle Note or Interval Quiz to test your neck mastery across all 6 strings.",
            "Tap anywhere to close this guide."
        };
        float ty = my + mh * 0.16f;
        float step = mh * 0.115f;
        textPaint.setTextAlign(Paint.Align.LEFT);
        for (int i = 0; i < lines.length; i++) {
            textPaint.setColor(i == 0 ? 0xFFF0883E : (i == lines.length - 1 ? 0xFF58A6FF : 0xFFE6EDF3));
            textPaint.setTextSize(i == 0 ? Math.min(mh * 0.068f, 24f) : Math.min(mh * 0.048f, 17f));
            c.drawText(lines[i], mx + 24f, ty + i * step, textPaint);
        }
    }

    private void drawPill(Canvas c, RectF r, String text, boolean active, int bgColor, int accentColor) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(active ? accentColor : bgColor);
        c.drawRoundRect(r, 7f, 7f, paint);
        if (!active) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.4f);
            paint.setColor(0xFF30363D);
            c.drawRoundRect(r, 7f, 7f, paint);
            paint.setStyle(Paint.Style.FILL);
        }
        textPaint.setColor(active ? 0xFF0B0F17 : 0xFFE6EDF3);
        textPaint.setTextAlign(Paint.Align.CENTER);
        float maxTs = Math.min(r.height() * 0.44f, (r.width() * 1.55f) / Math.max(3, text.length()));
        textPaint.setTextSize(Math.max(10f, maxTs));
        c.drawText(text, r.centerX(), r.centerY() + textPaint.getTextSize() * 0.35f, textPaint);
    }

    private float getFretCenterX(int fret) {
        if (!leftHanded) {
            return (fret == 0)
                    ? (neckRect.left + nutW * 0.5f)
                    : (neckRect.left + nutW + (fret - 0.5f) * fretColW);
        } else {
            return (fret == 0)
                    ? (neckRect.right - nutW * 0.5f)
                    : (neckRect.right - nutW - (fret - 0.5f) * fretColW);
        }
    }

    private float getStringCenterY(int stringIdx) {
        int displayRow = (FretboardTheory.NUM_STRINGS - 1) - stringIdx;
        return neckRect.top + (displayRow + 0.5f) * strRowH;
    }

    private float getStringCoordX(int stringIdx) {
        int col = leftHanded ? ((FretboardTheory.NUM_STRINGS - 1) - stringIdx) : stringIdx;
        return neckRect.left + (col + 0.5f) * strColW;
    }

    private float getFretCoordY(int fret) {
        return (fret == 0)
                ? (neckRect.top + nutW * 0.5f)
                : (neckRect.top + nutW + (fret - 0.5f) * fretRowH);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) return true;
        float x = event.getX();
        float y = event.getY();

        if (showHelp) {
            showHelp = false;
            invalidate();
            return true;
        }
        if (activeDropdown != DROPDOWN_NONE) {
            if (activeDropdown == DROPDOWN_KEY) {
                for (int k = 0; k < 12; k++) {
                    if (keyDropdownItemRects[k].contains(x, y)) {
                        rootPc = k;
                        activeDropdown = DROPDOWN_NONE;
                        recomputeTheoryState();
                        invalidate();
                        return true;
                    }
                }
            } else if (activeDropdown == DROPDOWN_MODE) {
                for (int i = 0; i < modeDropdownItemRects.length; i++) {
                    if (!modeDropdownItemRects[i].isEmpty() && modeDropdownItemRects[i].contains(x, y)) {
                        if (mode == FretboardTheory.MODE_CHORDS) {
                            chordIdx = i;
                        } else {
                            scaleIdx = i;
                        }
                        activeDropdown = DROPDOWN_NONE;
                        recomputeTheoryState();
                        invalidate();
                        return true;
                    }
                }
            }
            activeDropdown = DROPDOWN_NONE;
            invalidate();
            return true;
        }
        if (helpBtnRect.contains(x, y)) {
            showHelp = true;
            invalidate();
            return true;
        }
        if (playBtnRect.contains(x, y)) {
            triggerAudition();
            return true;
        }
        for (int i = 0; i < 3; i++) {
            if (modeTabRects[i].contains(x, y)) {
                mode = i;
                activeDropdown = DROPDOWN_NONE;
                recomputeTheoryState();
                invalidate();
                return true;
            }
        }
        if (keyDropdownBtnRect.contains(x, y)) {
            activeDropdown = DROPDOWN_KEY;
            invalidate();
            return true;
        }
        if (selectorBoxRect.contains(x, y)) {
            if (mode == FretboardTheory.MODE_SCALES || mode == FretboardTheory.MODE_CHORDS) {
                activeDropdown = DROPDOWN_MODE;
                invalidate();
            }
            return true;
        }
        if ((!posUpBtnRect.isEmpty() && posUpBtnRect.contains(x, y))
                || (!posDownBtnRect.isEmpty() && posDownBtnRect.contains(x, y))
                || (!posBadgeRect.isEmpty() && posBadgeRect.contains(x, y))) {
            int delta = (!posUpBtnRect.isEmpty() && posUpBtnRect.contains(x, y)) ? -1 : 1;
            if (mode == FretboardTheory.MODE_SCALES) {
                cagedPos = (cagedPos + delta + 6) % 6;
            } else if (mode == FretboardTheory.MODE_CHORDS) {
                voicingIdx = (voicingIdx + delta + 5) % 5;
                recomputeTheoryState();
            }
            invalidate();
            return true;
        }
        if (!clearBtnRect.isEmpty() && clearBtnRect.contains(x, y)) {
            for (int s = 0; s < FretboardTheory.NUM_STRINGS; s++) lockedFrets[s] = -1;
            recomputeTheoryState();
            invalidate();
            return true;
        }
        if (labelBtnRect.contains(x, y)) {
            labelMode = (labelMode + 1) % 3;
            invalidate();
            return true;
        }
        if (tuningBtnRect.contains(x, y)) {
            tuningIdx = (tuningIdx + 1) % FretboardTheory.TUNINGS.length;
            recomputeTheoryState();
            invalidate();
            return true;
        }
        if (handBtnRect.contains(x, y)) {
            leftHanded = !leftHanded;
            invalidate();
            return true;
        }
        if (quizBtnRect.contains(x, y)) {
            quizMode = (quizMode + 1) % 3;
            if (quizMode != FretboardTheory.QUIZ_OFF) pickNextQuizQuestion();
            invalidate();
            return true;
        }
        if (neckRect.contains(x, y)) {
            handleNeckTap(x, y);
            return true;
        }
        return true;
    }

    private void handleNeckTap(float x, float y) {
        int stringIdx;
        int fret;
        if (neckVertical) {
            int col = (int) ((x - neckRect.left) / strColW);
            col = FretboardTheory.clampIndex(col, FretboardTheory.NUM_STRINGS);
            stringIdx = leftHanded ? ((FretboardTheory.NUM_STRINGS - 1) - col) : col;
            fret = (y < neckRect.top + nutW)
                    ? 0
                    : 1 + (int) ((y - neckRect.top - nutW) / fretRowH);
        } else {
            int displayRow = (int) ((y - neckRect.top) / strRowH);
            displayRow = FretboardTheory.clampIndex(displayRow, FretboardTheory.NUM_STRINGS);
            stringIdx = (FretboardTheory.NUM_STRINGS - 1) - displayRow;

            if (!leftHanded) {
                fret = (x < neckRect.left + nutW)
                        ? 0
                        : 1 + (int) ((x - neckRect.left - nutW) / fretColW);
            } else {
                fret = (x > neckRect.right - nutW)
                        ? 0
                        : 1 + (int) ((neckRect.right - nutW - x) / fretColW);
            }
        }
        fret = FretboardTheory.clampIndex(fret, FretboardTheory.NUM_FRETS);

        if (quizMode != FretboardTheory.QUIZ_OFF) {
            quizTotal++;
            quizLastCorrect = FretboardTheory.isQuizAnswerCorrect(
                    quizMode, tuningIdx, rootPc, quizTargetValue, stringIdx, fret);
            if (quizLastCorrect) {
                quizScore++;
                quizFeedback = "\u2713 Correct! " + FretboardTheory.NOTE_NAMES[
                        FretboardTheory.getPitchClass(tuningIdx, stringIdx, fret)];
                pickNextQuizQuestion();
            } else {
                quizFeedback = "\u2717 Try again! Tapped " + FretboardTheory.NOTE_NAMES[
                        FretboardTheory.getPitchClass(tuningIdx, stringIdx, fret)];
            }
            quizFeedbackUntilMs = SystemClock.uptimeMillis() + 2000L;
        } else if (mode == FretboardTheory.MODE_EXPLORER) {
            lockedFrets[stringIdx] = (lockedFrets[stringIdx] == fret) ? -1 : fret;
            recomputeTheoryState();
        }

        if (mode != FretboardTheory.MODE_EXPLORER || lockedFrets[stringIdx] >= 0) {
            synth.pluckNote(tuningIdx, stringIdx, fret);
        }
        scheduleHighlightLoop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            triggerAudition();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_H || keyCode == KeyEvent.KEYCODE_SLASH) {
            showHelp = !showHelp;
            invalidate();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
