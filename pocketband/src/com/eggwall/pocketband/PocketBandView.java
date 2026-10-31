package com.eggwall.pocketband;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

/**
 * Interactive single-screen backing-band workstation, step sequencer, and 12-fret guitar scale map.
 */
public class PocketBandView extends View implements BandSynthEngine.StepListener {

    private static final String PREFS_NAME = "pocketband_prefs";

    private static final int COL_BG       = 0xFF0A0E17;
    private static final int COL_PANEL    = 0xFF131B2E;
    private static final int COL_CARD     = 0xFF1A253D;
    private static final int COL_BORDER   = 0xFF2B3B5E;
    private static final int COL_TEXT     = 0xFFF2F6FF;
    private static final int COL_DIM      = 0xFF8C9EC2;
    private static final int COL_GOLD     = 0xFFFFCA28;
    private static final int COL_CYAN     = 0xFF00E5FF;
    private static final int COL_EMERALD  = 0xFF00E676;
    private static final int COL_PINK     = 0xFFFF4081;
    private static final int COL_ORANGE   = 0xFFFF9100;
    private static final int COL_PURPLE   = 0xFFB388FF;

    private final BandSynthEngine mEngine;
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mMonoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /* Musical State */
    private int mStyleIdx = 0;
    private int mProgIdx = 0; // -1 = Style Default, 0..N-1 = Preset
    private int mKeyPc = 9;   // Default A
    private int mBpm = 110;
    private int mSwingPct = 65;
    private boolean mAutoFill = true;
    private boolean mMuteDrums = false;
    private boolean mMuteBass = false;
    private boolean mMuteChords = false;
    private int mVolDrums = 90;
    private int mVolBass = 90;
    private int mVolChords = 80;
    private BandTheory.BarChord[] mBars;
    private boolean mShowHelp = false;

    private static final int DROPDOWN_NONE  = 0;
    private static final int DROPDOWN_STYLE = 1;
    private static final int DROPDOWN_PROG  = 2;
    private static final int DROPDOWN_KEY   = 3;
    private int mActiveDropdown = DROPDOWN_NONE;

    /* Touch Hit Regions */
    private final RectF mBtnHelp = new RectF();
    private final RectF mBtnPlay = new RectF();
    private final RectF mBtnFill = new RectF();
    private final RectF mBtnAutoFill = new RectF();
    private final RectF mBtnReset = new RectF();

    private final RectF mBtnStyleDropdown = new RectF();
    private final RectF mBtnProgDropdown  = new RectF();
    private final RectF mBtnKeyDropdown   = new RectF();
    private final RectF mDropdownPanelRect = new RectF();
    private final RectF[] mStyleDropdownRects = new RectF[BandTheory.STYLES.length];
    private final RectF[] mProgDropdownRects  = new RectF[BandTheory.PROGRESSION_PRESETS.length];
    private final RectF[] mKeyDropdownRects   = new RectF[BandTheory.NOTE_NAMES.length];

    private final RectF mBtnBpmPrev   = new RectF(), mBtnBpmNext   = new RectF();

    private final RectF mBtnMuteDrums  = new RectF(), mBtnVolDrums  = new RectF();
    private final RectF mBtnMuteBass   = new RectF(), mBtnVolBass   = new RectF();
    private final RectF mBtnMuteChords = new RectF(), mBtnVolChords = new RectF();
    private final RectF mHelpModalRect = new RectF();
    private final RectF mHelpCloseRect = new RectF();
    private final RectF mHelpViewportRect = new RectF();
    private float mHelpScrollY = 0f;
    private float mHelpMaxScrollY = 0f;
    private float mHelpTouchStartY = 0f;
    private float mHelpLastTouchY = 0f;
    private boolean mHelpDragging = false;

    private RectF[] mBarRects = new RectF[0];
    private final RectF[][] mStepRects = new RectF[BandTheory.NUM_TRACKS][BandTheory.MAX_STEPS];

    public PocketBandView(Context context, BandSynthEngine engine) {
        super(context);
        mEngine = engine;
        setFocusable(true);
        setFocusableInTouchMode(true);
        mTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        mMonoPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        for (int t = 0; t < BandTheory.NUM_TRACKS; t++) {
            for (int s = 0; s < BandTheory.MAX_STEPS; s++) {
                mStepRects[t][s] = new RectF();
            }
        }
        for (int i = 0; i < mStyleDropdownRects.length; i++) {
            mStyleDropdownRects[i] = new RectF();
        }
        for (int i = 0; i < mProgDropdownRects.length; i++) {
            mProgDropdownRects[i] = new RectF();
        }
        for (int i = 0; i < mKeyDropdownRects.length; i++) {
            mKeyDropdownRects[i] = new RectF();
        }
        loadPrefs();
        mEngine.setStepListener(this);
        pushStateToEngine();
    }

    @Override
    public void onStepAdvanced(int barIdx, int stepIdx) {
        postInvalidateOnAnimation();
    }

    public void configureDemoState(
            int styleIdx, int progIdx, int keyPc, int bpm, int barIdx, int stepIdx, boolean showHelp) {
        mStyleIdx = ((styleIdx % BandTheory.STYLES.length) + BandTheory.STYLES.length)
                % BandTheory.STYLES.length;
        BandTheory.StylePreset st = BandTheory.STYLES[mStyleIdx];
        mProgIdx = (progIdx >= 0 && progIdx < BandTheory.PROGRESSION_PRESETS.length)
                ? progIdx : st.defaultProgIdx;
        mKeyPc = ((keyPc % 12) + 12) % 12;
        mBpm = Math.max(40, Math.min(240, bpm));
        mSwingPct = st.swingPct;
        mShowHelp = showHelp;
        mActiveDropdown = DROPDOWN_NONE;
        loadBarsFromProgression(mProgIdx);
        pushStateToEngine();
        mEngine.setPosition(barIdx, stepIdx);
        invalidate();
    }

    private void loadBarsFromProgression(int progIdx) {
        BandTheory.BarChord[] src = BandTheory.PROGRESSION_PRESETS[progIdx].bars;
        mBars = new BandTheory.BarChord[src.length];
        System.arraycopy(src, 0, mBars, 0, src.length);
        mBarRects = new RectF[mBars.length];
        for (int i = 0; i < mBars.length; i++) {
            mBarRects[i] = new RectF();
        }
    }

    private void applyStyleDefaults(int styleIdx) {
        mStyleIdx = ((styleIdx % BandTheory.STYLES.length) + BandTheory.STYLES.length)
                % BandTheory.STYLES.length;
        BandTheory.StylePreset st = BandTheory.STYLES[mStyleIdx];
        mBpm = st.defaultBpm;
        mSwingPct = st.swingPct;
        mProgIdx = st.defaultProgIdx;
        loadBarsFromProgression(mProgIdx);
        pushStateToEngine();
    }

    private void pushStateToEngine() {
        mEngine.syncState(
                mStyleIdx, mKeyPc, mBpm, mSwingPct, mAutoFill,
                mMuteDrums, mMuteBass, mMuteChords,
                mVolDrums, mVolBass, mVolChords, mBars);
    }

    public void savePrefs() {
        SharedPreferences.Editor ed =
                getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        ed.putInt("styleIdx", mStyleIdx);
        ed.putInt("progIdx", mProgIdx);
        ed.putInt("keyPc", mKeyPc);
        ed.putInt("bpm", mBpm);
        ed.putBoolean("autoFill", mAutoFill);
        ed.putBoolean("muteDrums", mMuteDrums);
        ed.putBoolean("muteBass", mMuteBass);
        ed.putBoolean("muteChords", mMuteChords);
        ed.putInt("volDrums", mVolDrums);
        ed.putInt("volBass", mVolBass);
        ed.putInt("volChords", mVolChords);
        ed.apply();
    }

    private void loadPrefs() {
        SharedPreferences sp = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mStyleIdx = sp.getInt("styleIdx", 0);
        if (mStyleIdx < 0 || mStyleIdx >= BandTheory.STYLES.length) mStyleIdx = 0;
        BandTheory.StylePreset st = BandTheory.STYLES[mStyleIdx];
        mProgIdx = sp.getInt("progIdx", st.defaultProgIdx);
        if (mProgIdx < 0 || mProgIdx >= BandTheory.PROGRESSION_PRESETS.length) {
            mProgIdx = st.defaultProgIdx;
        }
        mKeyPc = sp.getInt("keyPc", 9);
        mBpm = sp.getInt("bpm", st.defaultBpm);
        mSwingPct = st.swingPct;
        mAutoFill = sp.getBoolean("autoFill", true);
        mMuteDrums = sp.getBoolean("muteDrums", false);
        mMuteBass = sp.getBoolean("muteBass", false);
        mMuteChords = sp.getBoolean("muteChords", false);
        mVolDrums = sp.getInt("volDrums", 90);
        mVolBass = sp.getInt("volBass", 90);
        mVolChords = sp.getInt("volChords", 80);
        loadBarsFromProgression(mProgIdx);
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        c.drawColor(COL_BG);
        float pad = Math.max(8f, Math.min(w, h) * 0.014f);
        boolean landscape = w > h;

        float topH = Math.max(46f, h * (landscape ? 0.095f : 0.058f));
        drawHeaderBar(c, pad, pad, w - 2 * pad, topH);

        float contentTop = pad * 2 + topH;
        float contentH = h - contentTop - pad;

        if (landscape) {
            float leftW = (w - pad * 3) * 0.48f;
            float rightW = (w - pad * 3) - leftW;
            float ctrlH = contentH * 0.31f;
            float mixH = contentH * 0.18f;
            float progH = contentH - ctrlH - mixH - pad * 2;

            drawSelectorStrip(c, pad, contentTop, leftW, ctrlH);
            drawMixerStrip(c, pad, contentTop + ctrlH + pad, leftW, mixH);
            drawChordProgression(c, pad, contentTop + ctrlH + mixH + pad * 2, leftW, progH);

            float rightX = pad * 2 + leftW;
            float seqH = contentH * 0.50f;
            float fretH = contentH - seqH - pad;
            drawStepSequencer(c, rightX, contentTop, rightW, seqH);
            drawFretboardGuide(c, rightX, contentTop + seqH + pad, rightW, fretH);
        } else {
            float ctrlH = contentH * 0.150f;
            float mixH  = contentH * 0.092f;
            float progH = contentH * 0.230f;
            float seqH  = contentH * 0.268f;
            float fretH = contentH - ctrlH - mixH - progH - seqH - pad * 4;

            float y = contentTop;
            drawSelectorStrip(c, pad, y, w - 2 * pad, ctrlH);
            y += ctrlH + pad;
            drawMixerStrip(c, pad, y, w - 2 * pad, mixH);
            y += mixH + pad;
            drawChordProgression(c, pad, y, w - 2 * pad, progH);
            y += progH + pad;
            drawStepSequencer(c, pad, y, w - 2 * pad, seqH);
            y += seqH + pad;
            drawFretboardGuide(c, pad, y, w - 2 * pad, fretH);
        }

        if (mActiveDropdown != DROPDOWN_NONE) {
            drawDropdownModal(c, w, h, landscape);
        }

        if (mShowHelp) {
            drawHelpModal(c, w, h);
        }
    }

    private void drawHeaderBar(Canvas c, float x, float y, float w, float h) {
        drawCard(c, x, y, x + w, y + h, COL_PANEL, COL_BORDER);
        float helpH = h * 0.44f;
        float helpY = y + (h - helpH) * 0.5f;

        // Smaller, less prominent top-left [?] button
        float helpW = helpH;
        mBtnHelp.set(x + 8f, helpY, x + 8f + helpW, helpY + helpH);
        drawPillButton(c, mBtnHelp, "?", mShowHelp ? COL_GOLD : COL_CARD,
                mShowHelp ? 0xFF101420 : COL_DIM, helpH * 0.48f);

        // Right transport buttons: smaller in height (STOP, FILL, AUTO:ON, RST)
        float btnH = h * 0.56f;
        float btnY = y + (h - btnH) * 0.5f;
        float rightEdge = x + w - 8f;
        float bW = Math.min(w * 0.132f, h * 1.55f);
        float gap = 6f;

        mBtnReset.set(rightEdge - bW * 0.82f, btnY, rightEdge, btnY + btnH);
        rightEdge = mBtnReset.left - gap;
        mBtnAutoFill.set(rightEdge - bW * 0.98f, btnY, rightEdge, btnY + btnH);
        rightEdge = mBtnAutoFill.left - gap;
        mBtnFill.set(rightEdge - bW * 0.82f, btnY, rightEdge, btnY + btnH);
        rightEdge = mBtnFill.left - gap;
        mBtnPlay.set(rightEdge - bW * 0.98f, btnY, rightEdge, btnY + btnH);

        // Title & active chord readout (bounded so it never overlaps the right transport buttons)
        float textX = mBtnHelp.right + 10f;
        float availTextW = Math.max(40f, mBtnPlay.left - textX - 10f);
        int curBar = mEngine.getCurBar();
        int curStep = mEngine.getCurStep();
        BandTheory.BarChord curBc = (mBars != null && curBar < mBars.length) ? mBars[curBar] : null;
        String chordLbl = BandTheory.formatChordLabel(mKeyPc, curBc);
        String title = AppI18n.tr("Band");
        mTextPaint.setColor(COL_TEXT);
        float titleTs = h * 0.34f;
        mTextPaint.setTextSize(titleTs);
        float titleW = mTextPaint.measureText(title);
        if (titleW > availTextW && titleW > 0f) {
            mTextPaint.setTextSize(titleTs * (availTextW / titleW));
        }
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        AppI18n.drawText(c, title, textX, y + h * 0.45f, mTextPaint);

        mMonoPaint.setColor(COL_CYAN);
        float subTs = h * 0.25f;
        mMonoPaint.setTextSize(subTs);
        mMonoPaint.setTextAlign(Paint.Align.LEFT);
        String sub = chordLbl + " (" + BandTheory.formatChordNotes(mKeyPc, curBc)
                + ")  BAR " + (curBar + 1) + "/" + mBars.length + "  BEAT " + ((curStep / 2) + 1);
        sub = AppI18n.tr(sub);
        float subW = mMonoPaint.measureText(sub);
        if (subW > availTextW && subW > 0f) {
            mMonoPaint.setTextSize(subTs * (availTextW / subW));
        }
        AppI18n.drawText(c, sub, textX, y + h * 0.82f, mMonoPaint);

        boolean playing = mEngine.isPlaying();
        drawPillButton(c, mBtnPlay, playing ? "STOP" : "PLAY",
                playing ? COL_EMERALD : COL_PINK, 0xFF081018, btnH * 0.42f);
        boolean fillQ = mEngine.isFillQueued();
        drawPillButton(c, mBtnFill, "FILL",
                fillQ ? COL_ORANGE : COL_CARD, fillQ ? 0xFF081018 : COL_ORANGE, btnH * 0.40f);
        drawPillButton(c, mBtnAutoFill, mAutoFill ? "AUTO:ON" : "AUTO:OFF",
                mAutoFill ? 0xFF1E3A5F : COL_CARD, mAutoFill ? COL_CYAN : COL_DIM, btnH * 0.35f);
        drawPillButton(c, mBtnReset, "RST", COL_CARD, COL_DIM, btnH * 0.40f);
    }

    private void drawSelectorStrip(Canvas c, float x, float y, float w, float h) {
        float gap = 6f;
        float cellW = (w - gap) * 0.5f;
        float cellH = (h - gap) * 0.5f;

        BandTheory.StylePreset st = BandTheory.STYLES[mStyleIdx];
        BandTheory.ProgressionPreset pr = BandTheory.PROGRESSION_PRESETS[mProgIdx];
        String keyStr = BandTheory.NOTE_NAMES[mKeyPc] + " " + st.beatsPerBar + "/4 \u25BE";
        String bpmStr = mBpm + " bpm";

        // Top row: full-cell dropdown buttons (no < or > buttons, no sub-labels, larger text)
        mBtnStyleDropdown.set(x, y, x + cellW, y + cellH);
        mBtnProgDropdown.set(x + cellW + gap, y, x + cellW * 2f + gap, y + cellH);
        drawDropdownTriggerCell(c, mBtnStyleDropdown, st.name + " \u25BE",
                COL_GOLD, mActiveDropdown == DROPDOWN_STYLE);
        drawDropdownTriggerCell(c, mBtnProgDropdown, pr.shortLabel + " \u25BE",
                COL_CYAN, mActiveDropdown == DROPDOWN_PROG);

        // Bottom row: KEY (tappable dropdown, no left/right arrows) and TEMPO (- and + buttons)
        mBtnKeyDropdown.set(x, y + cellH + gap, x + cellW, y + cellH * 2f + gap);
        drawKeyDropdownCell(c, mBtnKeyDropdown, "KEY", keyStr,
                COL_EMERALD, mActiveDropdown == DROPDOWN_KEY);
        drawStepperCell(c, x + cellW + gap, y + cellH + gap, cellW, cellH,
                "TEMPO", bpmStr, COL_ORANGE, mBtnBpmPrev, mBtnBpmNext);
    }

    private void drawDropdownTriggerCell(
            Canvas c, RectF rect, String value, int accentCol, boolean isOpen) {
        value = AppI18n.tr(value);
        int bg = isOpen ? 0xFF233252 : COL_PANEL;
        int border = isOpen ? accentCol : COL_BORDER;
        drawCard(c, rect.left, rect.top, rect.right, rect.bottom, bg, border);

        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(accentCol);
        float availW = Math.max(20f, rect.width() - 20f);
        float valTs = rect.height() * 0.44f;
        mTextPaint.setTextSize(valTs);
        float mW = mTextPaint.measureText(value);
        if (mW > availW && mW > 0f) {
            valTs = valTs * (availW / mW);
            mTextPaint.setTextSize(valTs);
        }
        AppI18n.drawText(c, value, rect.centerX(), rect.centerY() + valTs * 0.35f, mTextPaint);
    }

    private void drawKeyDropdownCell(
            Canvas c, RectF rect, String label, String value, int accentCol, boolean isOpen) {
        label = AppI18n.tr(label);
        value = AppI18n.tr(value);
        int bg = isOpen ? 0xFF233252 : COL_PANEL;
        int border = isOpen ? accentCol : COL_BORDER;
        drawCard(c, rect.left, rect.top, rect.right, rect.bottom, bg, border);

        float cx = rect.centerX();
        float h = rect.height();
        float availW = Math.max(20f, rect.width() - 16f);

        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(COL_DIM);
        mTextPaint.setTextSize(h * 0.22f);
        AppI18n.drawText(c, label, cx, rect.top + h * 0.33f, mTextPaint);

        mTextPaint.setColor(accentCol);
        float valTs = h * 0.38f;
        mTextPaint.setTextSize(valTs);
        float mW = mTextPaint.measureText(value);
        if (mW > availW && mW > 0f) {
            mTextPaint.setTextSize(valTs * (availW / mW));
        }
        AppI18n.drawText(c, value, cx, rect.top + h * 0.77f, mTextPaint);
    }

    private void drawStepperCell(
            Canvas c, float x, float y, float w, float h,
            String label, String value, int accentCol, RectF prevOut, RectF nextOut) {
        label = AppI18n.tr(label);
        value = AppI18n.tr(value);
        drawCard(c, x, y, x + w, y + h, COL_PANEL, COL_BORDER);
        float btnW = Math.min(h * 0.82f, w * 0.19f);
        float pad = 5f;
        prevOut.set(x + pad, y + pad, x + pad + btnW, y + h - pad);
        nextOut.set(x + w - pad - btnW, y + pad, x + w - pad, y + h - pad);

        drawPillButton(c, prevOut, "-", COL_CARD, accentCol, h * 0.42f);
        drawPillButton(c, nextOut, "+", COL_CARD, accentCol, h * 0.42f);

        float cx = x + w * 0.5f;
        float availMidW = Math.max(20f, nextOut.left - prevOut.right - 10f);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(COL_DIM);
        float lblTs = Math.min(h * 0.22f, availMidW * 0.15f);
        mTextPaint.setTextSize(lblTs);
        AppI18n.drawText(c, label, cx, y + h * 0.33f, mTextPaint);

        mTextPaint.setColor(accentCol);
        float valTs = h * 0.36f;
        mTextPaint.setTextSize(valTs);
        float mW = mTextPaint.measureText(value);
        if (mW > availMidW && mW > 0f) {
            mTextPaint.setTextSize(valTs * (availMidW / mW));
        }
        AppI18n.drawText(c, value, cx, y + h * 0.77f, mTextPaint);
    }

    private void drawMixerStrip(Canvas c, float x, float y, float w, float h) {
        float gap = 6f;
        float chW = (w - gap * 2f) / 3f;
        drawMixerChannel(c, x, y, chW, h, "DRUMS", !mMuteDrums, mVolDrums,
                COL_ORANGE, mBtnMuteDrums, mBtnVolDrums);
        drawMixerChannel(c, x + chW + gap, y, chW, h, "BASS", !mMuteBass, mVolBass,
                COL_CYAN, mBtnMuteBass, mBtnVolBass);
        drawMixerChannel(c, x + (chW + gap) * 2f, y, chW, h, "CHORDS", !mMuteChords, mVolChords,
                COL_PURPLE, mBtnMuteChords, mBtnVolChords);
    }

    private void drawMixerChannel(
            Canvas c, float x, float y, float w, float h,
            String name, boolean active, int volPct, int col, RectF muteOut, RectF volOut) {
        drawCard(c, x, y, x + w, y + h, COL_PANEL, active ? col : COL_BORDER);
        float pad = 5f;
        float muteW = w * 0.54f;
        muteOut.set(x + pad, y + pad, x + pad + muteW, y + h - pad);
        volOut.set(muteOut.right + 4f, y + pad, x + w - pad, y + h - pad);

        drawTwoLinePillButton(c, muteOut, name, active ? "ON" : "MUTE",
                active ? col : COL_CARD, active ? 0xFF081018 : COL_DIM);
        drawPillButton(c, volOut, volPct + "%", COL_CARD, active ? col : COL_DIM, h * 0.30f);
    }

    private void drawChordProgression(Canvas c, float x, float y, float w, float h) {
        drawCard(c, x, y, x + w, y + h, COL_PANEL, COL_BORDER);
        int n = mBars.length;
        int curBar = mEngine.getCurBar();
        int curStep = mEngine.getCurStep();
        int spb = BandTheory.STYLES[mStyleIdx].stepsPerBar;

        float headerH = h * 0.22f;
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setColor(COL_DIM);
        mTextPaint.setTextSize(Math.min(headerH * 0.46f, w * 0.023f));
        AppI18n.drawText(c, "CHORD PROGRESSION GRID (TAP BAR TO AUDITION)", x + 10f, y + headerH * 0.72f, mTextPaint);

        // 16-segment / bar-beat LED strip along top-right of progression card
        int totalBeats = 16;
        float ledAreaW = w * 0.34f;
        float ledW = (ledAreaW - (totalBeats - 1) * 3f) / totalBeats;
        float ledH = headerH * 0.46f;
        float ledX0 = x + w - ledAreaW - 10f;
        float ledY0 = y + (headerH - ledH) * 0.5f;
        int activeStep16 = ((curBar * spb + curStep) * 2) % totalBeats;
        for (int i = 0; i < totalBeats; i++) {
            float lx = ledX0 + i * (ledW + 3f);
            mPaint.setStyle(Paint.Style.FILL);
            if (i == activeStep16 || i == activeStep16 + 1) {
                mPaint.setColor((i % 4 == 0) ? COL_GOLD : COL_EMERALD);
            } else {
                mPaint.setColor((i % 4 == 0) ? 0xFF2C3E66 : 0xFF19233A);
            }
            c.drawRoundRect(lx, ledY0, lx + ledW, ledY0 + ledH, 3f, 3f, mPaint);
        }

        int cols = (n <= 4) ? n : 4;
        int rows = (n + cols - 1) / cols;
        float pad = 8f;
        float gridTop = y + headerH + 2f;
        float gridH = h - headerH - pad;
        float cellW = (w - pad * (cols + 1)) / cols;
        float cellH = (gridH - pad * rows) / rows;

        for (int i = 0; i < n; i++) {
            int r = i / cols;
            int col = i % cols;
            float bx = x + pad + col * (cellW + pad);
            float by = gridTop + r * (cellH + pad);
            mBarRects[i].set(bx, by, bx + cellW, by + cellH);

            boolean isCur = (i == curBar);
            BandTheory.BarChord bc = mBars[i];
            int bg = isCur ? 0xFF1E352F : COL_CARD;
            int border = isCur ? COL_EMERALD : COL_BORDER;
            drawCard(c, bx, by, bx + cellW, by + cellH, bg, border);

            mTextPaint.setTextAlign(Paint.Align.LEFT);
            mTextPaint.setColor(isCur ? COL_EMERALD : COL_DIM);
            mTextPaint.setTextSize(cellH * 0.27f);
            AppI18n.drawText(c, String.valueOf(i + 1), bx + 8f, by + cellH * 0.31f, mTextPaint);

            mTextPaint.setTextAlign(Paint.Align.RIGHT);
            mTextPaint.setColor(COL_GOLD);
            AppI18n.drawText(c, bc.roman, bx + cellW - 7f, by + cellH * 0.30f, mTextPaint);

            mTextPaint.setTextAlign(Paint.Align.CENTER);
            mTextPaint.setColor(COL_TEXT);
            mTextPaint.setTextSize(cellH * 0.44f);
            AppI18n.drawText(c, BandTheory.formatChordLabel(mKeyPc, bc), bx + cellW * 0.5f, by + cellH * 0.76f, mTextPaint);

            // Beat progress dots inside active bar
            if (isCur) {
                int beats = spb / 2;
                int curBeat = curStep / 2;
                float dotR = Math.max(2.5f, cellH * 0.055f);
                for (int b = 0; b < beats; b++) {
                    mPaint.setColor(b == curBeat ? COL_GOLD : 0xFF3A506B);
                    c.drawCircle(bx + 12f + b * (dotR * 3.0f), by + cellH - dotR * 1.8f, dotR, mPaint);
                }
            }
        }
    }

    private void drawStepSequencer(Canvas c, float x, float y, float w, float h) {
        drawCard(c, x, y, x + w, y + h, COL_PANEL, COL_BORDER);
        BandTheory.StylePreset st = BandTheory.STYLES[mStyleIdx];
        int spb = st.stepsPerBar;
        int curStep = mEngine.getCurStep();

        float pad = 7f;
        float headerH = h * 0.16f;
        float labelW = w * 0.17f;
        float gridW = w - labelW - pad * 2f;
        float stepGap = 4f;
        float cellW = (gridW - stepGap * (spb - 1)) / spb;
        float rowGap = 4f;
        float rowsH = h - headerH - pad * 2f;
        float cellH = (rowsH - rowGap * (BandTheory.NUM_TRACKS - 1)) / BandTheory.NUM_TRACKS;

        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setColor(COL_DIM);
        mTextPaint.setTextSize(Math.min(headerH * 0.48f, labelW * 0.16f));
        AppI18n.drawText(c, "STEP SEQ", x + pad, y + headerH * 0.76f, mTextPaint);

        // Step header labels: 1 & 2 & 3 & 4 &
        mMonoPaint.setTextAlign(Paint.Align.CENTER);
        mMonoPaint.setTextSize(Math.min(headerH * 0.52f, cellW * 0.42f));
        for (int s = 0; s < spb; s++) {
            float sx = x + pad + labelW + s * (cellW + stepGap) + cellW * 0.5f;
            String lbl = (s % 2 == 0) ? String.valueOf((s / 2) + 1) : "&";
            mMonoPaint.setColor(s == curStep ? COL_GOLD : COL_DIM);
            AppI18n.drawText(c, lbl, sx, y + headerH * 0.80f, mMonoPaint);
        }

        for (int t = 0; t < BandTheory.NUM_TRACKS; t++) {
            float ry = y + headerH + pad * 0.5f + t * (cellH + rowGap);
            mTextPaint.setTextAlign(Paint.Align.LEFT);
            mTextPaint.setColor(t == BandTheory.TRK_BASS ? COL_CYAN : COL_TEXT);
            mTextPaint.setTextSize(cellH * 0.44f);
            AppI18n.drawTextFit(c, BandTheory.TRACK_NAMES[t], x + pad, ry + cellH * 0.66f, mTextPaint, labelW - 6f);

            for (int s = 0; s < BandTheory.MAX_STEPS; s++) {
                if (s >= spb) {
                    mStepRects[t][s].setEmpty();
                    continue;
                }
                float sx = x + pad + labelW + s * (cellW + stepGap);
                mStepRects[t][s].set(sx, ry, sx + cellW, ry + cellH);

                int val = mEngine.getStepValue(mStyleIdx, t, s);
                boolean isCur = (s == curStep);
                int bg = COL_CARD;
                int fg = COL_DIM;
                String txt;

                if (t == BandTheory.TRK_BASS) {
                    txt = BandTheory.BASS_DEG_LABELS[Math.max(0, Math.min(6, val))];
                    if (val > 0) {
                        bg = (val == 1) ? 0xFF005B66 : 0xFF1C3F6E;
                        fg = (val == 1) ? COL_GOLD : COL_CYAN;
                    }
                } else {
                    txt = BandTheory.DRUM_STEP_LABELS[Math.max(0, Math.min(2, val))];
                    if (val == 2) {
                        bg = 0xFF663D00;
                        fg = COL_GOLD;
                    } else if (val == 1) {
                        bg = 0xFF1B4332;
                        fg = COL_EMERALD;
                    }
                }

                drawCard(c, sx, ry, sx + cellW, ry + cellH, bg, isCur ? COL_GOLD : COL_BORDER);
                mMonoPaint.setColor(fg);
                mMonoPaint.setTextSize(cellH * 0.46f);
                mMonoPaint.setTextAlign(Paint.Align.CENTER);
                c.drawText(txt, sx + cellW * 0.5f, ry + cellH * 0.67f, mMonoPaint);
            }
        }
    }

    private void drawFretboardGuide(Canvas c, float x, float y, float w, float h) {
        drawCard(c, x, y, x + w, y + h, COL_PANEL, COL_BORDER);
        BandTheory.StylePreset st = BandTheory.STYLES[mStyleIdx];
        int curBar = mEngine.getCurBar();
        BandTheory.BarChord curBc = (mBars != null && curBar < mBars.length) ? mBars[curBar] : null;

        float pad = 8f;
        float headerH = h * 0.22f;
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setColor(COL_GOLD);
        mTextPaint.setTextSize(Math.min(headerH * 0.44f, w * 0.022f));
        AppI18n.drawText(c, "SOLO SCALE: " + BandTheory.NOTE_NAMES[mKeyPc] + " " + st.scaleName,
                x + pad, y + headerH * 0.74f, mTextPaint);

        // Legend on right
        mTextPaint.setTextAlign(Paint.Align.RIGHT);
        mTextPaint.setColor(COL_DIM);
        mTextPaint.setTextSize(Math.min(headerH * 0.38f, w * 0.019f));
        AppI18n.drawText(c, "GOLD=Key  GRN=Chord  PINK=Blue  CYN=Scale", x + w - pad, y + headerH * 0.74f, mTextPaint);

        float neckLeft = x + pad + w * 0.045f;
        float neckRight = x + w - pad;
        float neckTop = y + headerH + 4f;
        float neckBottom = y + h - headerH * 0.75f;
        float fretW = (neckRight - neckLeft) / 13f;
        float strH = (neckBottom - neckTop) / 5f;

        // Draw fret wires (0 = nut .. 12)
        for (int f = 0; f <= 12; f++) {
            float fx = neckLeft + (f + 0.5f) * fretW;
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(f == 0 ? 4f : 1.5f);
            mPaint.setColor(f == 0 ? COL_GOLD : 0xFF33476B);
            c.drawLine(fx, neckTop, fx, neckBottom, mPaint);

            if (f == 0 || f == 3 || f == 5 || f == 7 || f == 9 || f == 12) {
                mMonoPaint.setColor(COL_DIM);
                mMonoPaint.setTextSize(headerH * 0.30f);
                mMonoPaint.setTextAlign(Paint.Align.CENTER);
                AppI18n.drawText(c, String.valueOf(f), fx, y + h - 5f, mMonoPaint);
            }
        }

        // Draw 6 strings and scale note markers
        float noteR = Math.min(fretW * 0.36f, strH * 0.42f);
        for (int s = 0; s < 6; s++) {
            float sy = neckTop + s * strH;
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(1.2f + s * 0.35f);
            mPaint.setColor(0xFF62779E);
            c.drawLine(neckLeft + fretW * 0.5f, sy, neckRight - fretW * 0.5f, sy, mPaint);

            mMonoPaint.setColor(COL_TEXT);
            mMonoPaint.setTextSize(strH * 0.55f);
            mMonoPaint.setTextAlign(Paint.Align.CENTER);
            AppI18n.drawText(c, BandTheory.GTR_STR_LABELS[s], x + pad + w * 0.018f, sy + strH * 0.18f, mMonoPaint);

            for (int f = 0; f <= 12; f++) {
                int cls = BandTheory.classifyScaleFret(st, mKeyPc, curBc, s, f);
                if (cls <= 0) continue;
                float fx = neckLeft + (f + 0.5f) * fretW;
                int dotCol = (cls == 4) ? COL_GOLD
                        : (cls == 3) ? COL_EMERALD
                        : (cls == 2) ? COL_PINK : COL_CYAN;
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(dotCol);
                c.drawCircle(fx, sy, noteR, mPaint);

                int pc = (BandTheory.GTR_STR_OPEN_PC[s] + f) % 12;
                mMonoPaint.setColor(0xFF081018);
                mMonoPaint.setTextSize(noteR * 1.05f);
                mMonoPaint.setTextAlign(Paint.Align.CENTER);
                AppI18n.drawText(c, BandTheory.NOTE_NAMES[pc], fx, sy + noteR * 0.36f, mMonoPaint);
            }
        }
    }

    private void drawHelpModal(Canvas c, int w, int h) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(0xD8050811);
        c.drawRect(0, 0, w, h, mPaint);

        float mw = w * 0.90f;
        float mh = h * 0.84f;
        float mx = (w - mw) * 0.5f;
        float my = (h - mh) * 0.5f;
        mHelpModalRect.set(mx, my, mx + mw, my + mh);
        drawCard(c, mx, my, mx + mw, my + mh, COL_PANEL, COL_GOLD);

        float pad = Math.max(14f, mw * 0.03f);
        float hdrH = Math.max(44f, mh * 0.105f);
        float closeW = Math.max(96f, mw * 0.18f);
        mHelpCloseRect.set(mx + mw - pad - closeW, my + 10f, mx + mw - pad, my + 10f + hdrH - 6f);
        drawPillButton(c, mHelpCloseRect, AppI18n.str(getContext(), R.string.close),
                0xFF3A1E24, 0xFFF85149, (hdrH - 6f) * 0.42f);

        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setColor(COL_GOLD);
        mTextPaint.setTextSize(Math.min(hdrH * 0.48f, 24f));
        AppI18n.drawTextFit(c, AppI18n.str(getContext(), R.string.help_title),
                mx + pad, my + 10f + (hdrH - 6f) * 0.68f, mTextPaint,
                mHelpCloseRect.left - (mx + pad + 12f));

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
        int[] titleCols = { COL_GOLD, COL_CYAN, COL_EMERALD, COL_GOLD };

        mHelpViewportRect.set(mx + pad, mHelpCloseRect.bottom + 8f, mx + mw - pad, my + mh - 12f);
        float titleSz = Math.max(15f, Math.min(20f, Math.min(w, h) * 0.032f));
        float bodySz = Math.max(13.5f, Math.min(18f, Math.min(w, h) * 0.028f));
        mHelpMaxScrollY = AppI18n.drawScrollableHelpBlock(
                c, mHelpViewportRect, titles, bodies, titleCols,
                COL_TEXT, COL_CARD, 0xFF283448, COL_GOLD,
                titleSz, bodySz, mHelpScrollY, mTextPaint, mPaint);
        mHelpScrollY = Math.max(0f, Math.min(mHelpScrollY, mHelpMaxScrollY));
    }

    private void drawDropdownModal(Canvas c, int w, int h, boolean landscape) {
        float topY = (mActiveDropdown == DROPDOWN_KEY)
                ? (mBtnKeyDropdown.bottom + 6f)
                : (mBtnStyleDropdown.bottom + 6f);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(0xC0050811);
        c.drawRect(0, topY, w, h, mPaint);

        float pad = Math.max(10f, w * 0.02f);
        int cols = 2;
        if (mActiveDropdown == DROPDOWN_STYLE) {
            int count = BandTheory.STYLES.length;
            int rows = (count + cols - 1) / cols;
            float panelW = landscape ? Math.min(w * 0.62f, 820f) : (w - 2 * pad);
            float headerH = 38f;
            float gap = 8f;
            float maxAvailH = (h - topY - pad) - headerH - (rows + 1) * gap - 6f;
            float cellH = Math.min(maxAvailH / rows,
                    landscape ? Math.max(40f, h * 0.080f) : Math.max(54f, h * 0.048f));
            float panelH = headerH + rows * cellH + (rows + 1) * gap + 6f;
            float panelX = pad;

            mDropdownPanelRect.set(panelX, topY, panelX + panelW, topY + panelH);
            drawCard(c, mDropdownPanelRect.left, mDropdownPanelRect.top,
                    mDropdownPanelRect.right, mDropdownPanelRect.bottom, COL_PANEL, COL_GOLD);

            mTextPaint.setTextAlign(Paint.Align.LEFT);
            mTextPaint.setColor(COL_GOLD);
            mTextPaint.setTextSize(20f);
            AppI18n.drawText(c, "SELECT BACKING STYLE", panelX + 14f, topY + 28f, mTextPaint);

            float gridTop = topY + headerH + 4f;
            float cellW = (panelW - (cols + 1) * gap) / cols;
            for (int i = 0; i < count; i++) {
                int r = i / cols;
                int col = i % cols;
                float cx = panelX + gap + col * (cellW + gap);
                float cy = gridTop + r * (cellH + gap);
                mStyleDropdownRects[i].set(cx, cy, cx + cellW, cy + cellH);
                boolean sel = (mStyleIdx == i);
                drawPillButton(c, mStyleDropdownRects[i], BandTheory.STYLES[i].name,
                        sel ? COL_GOLD : COL_CARD, sel ? 0xFF081018 : COL_TEXT, cellH * 0.36f);
            }
        } else if (mActiveDropdown == DROPDOWN_PROG) {
            int count = BandTheory.PROGRESSION_PRESETS.length;
            int rows = (count + cols - 1) / cols;
            float panelW = landscape ? Math.min(w * 0.62f, 820f) : (w - 2 * pad);
            float cellH = landscape ? Math.max(46f, h * 0.088f) : Math.max(58f, h * 0.052f);
            float headerH = 38f;
            float gap = 8f;
            float panelH = headerH + rows * cellH + (rows + 1) * gap + 6f;
            float panelX = pad;

            mDropdownPanelRect.set(panelX, topY, panelX + panelW, topY + panelH);
            drawCard(c, mDropdownPanelRect.left, mDropdownPanelRect.top,
                    mDropdownPanelRect.right, mDropdownPanelRect.bottom, COL_PANEL, COL_CYAN);

            mTextPaint.setTextAlign(Paint.Align.LEFT);
            mTextPaint.setColor(COL_CYAN);
            mTextPaint.setTextSize(20f);
            AppI18n.drawText(c, "SELECT CHORD PROGRESSION", panelX + 14f, topY + 28f, mTextPaint);

            float gridTop = topY + headerH + 4f;
            float cellW = (panelW - (cols + 1) * gap) / cols;
            for (int i = 0; i < count; i++) {
                int r = i / cols;
                int col = i % cols;
                float cx = panelX + gap + col * (cellW + gap);
                float cy = gridTop + r * (cellH + gap);
                mProgDropdownRects[i].set(cx, cy, cx + cellW, cy + cellH);
                boolean sel = (mProgIdx == i);
                drawPillButton(c, mProgDropdownRects[i], BandTheory.PROGRESSION_PRESETS[i].shortLabel,
                        sel ? COL_CYAN : COL_CARD, sel ? 0xFF081018 : COL_TEXT, cellH * 0.36f);
            }
        } else if (mActiveDropdown == DROPDOWN_KEY) {
            int keyCols = 4;
            int count = BandTheory.NOTE_NAMES.length;
            int rows = (count + keyCols - 1) / keyCols;
            float panelW = landscape ? Math.min(w * 0.52f, 680f) : (w - 2 * pad);
            float headerH = 38f;
            float gap = 8f;
            float maxAvailH = (h - topY - pad) - headerH - (rows + 1) * gap - 6f;
            float cellH = Math.min(maxAvailH / rows,
                    landscape ? Math.max(44f, h * 0.088f) : Math.max(56f, h * 0.050f));
            float panelH = headerH + rows * cellH + (rows + 1) * gap + 6f;
            float panelX = pad;

            mDropdownPanelRect.set(panelX, topY, panelX + panelW, topY + panelH);
            drawCard(c, mDropdownPanelRect.left, mDropdownPanelRect.top,
                    mDropdownPanelRect.right, mDropdownPanelRect.bottom, COL_PANEL, COL_EMERALD);

            mTextPaint.setTextAlign(Paint.Align.LEFT);
            mTextPaint.setColor(COL_EMERALD);
            mTextPaint.setTextSize(20f);
            AppI18n.drawText(c, "SELECT KEY", panelX + 14f, topY + 28f, mTextPaint);

            float gridTop = topY + headerH + 4f;
            float cellW = (panelW - (keyCols + 1) * gap) / keyCols;
            for (int i = 0; i < count; i++) {
                int r = i / keyCols;
                int col = i % keyCols;
                float cx = panelX + gap + col * (cellW + gap);
                float cy = gridTop + r * (cellH + gap);
                mKeyDropdownRects[i].set(cx, cy, cx + cellW, cy + cellH);
                boolean sel = (mKeyPc == i);
                drawPillButton(c, mKeyDropdownRects[i], BandTheory.NOTE_NAMES[i],
                        sel ? COL_EMERALD : COL_CARD, sel ? 0xFF081018 : COL_TEXT, cellH * 0.40f);
            }
        }
    }

    private void drawCard(Canvas c, float l, float t, float r, float b, int fillCol, int strokeCol) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(fillCol);
        c.drawRoundRect(l, t, r, b, 10f, 10f, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2f);
        mPaint.setColor(strokeCol);
        c.drawRoundRect(l, t, r, b, 10f, 10f, mPaint);
    }

    private void drawTwoLinePillButton(
            Canvas c, RectF rect, String line1, String line2, int bgCol, int fgCol) {
        line1 = AppI18n.tr(line1);
        line2 = AppI18n.tr(line2);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(bgCol);
        c.drawRoundRect(rect, 8f, 8f, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(1.8f);
        mPaint.setColor(fgCol);
        c.drawRoundRect(rect, 8f, 8f, mPaint);

        mTextPaint.setColor(fgCol);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        float maxW = Math.max(10f, rect.width() - 10f);

        float ts1 = rect.height() * 0.31f;
        mTextPaint.setTextSize(ts1);
        float mW1 = mTextPaint.measureText(line1);
        if (mW1 > maxW && mW1 > 0f) {
            ts1 = ts1 * (maxW / mW1);
            mTextPaint.setTextSize(ts1);
        }
        AppI18n.drawText(c, line1, rect.centerX(), rect.centerY() - rect.height() * 0.07f, mTextPaint);

        float ts2 = rect.height() * 0.28f;
        mTextPaint.setTextSize(ts2);
        AppI18n.drawText(c, line2, rect.centerX(), rect.centerY() + rect.height() * 0.33f, mTextPaint);
    }

    private void drawPillButton(Canvas c, RectF rect, String label, int bgCol, int fgCol, float textSize) {
        label = AppI18n.tr(label);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(bgCol);
        c.drawRoundRect(rect, 8f, 8f, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(1.8f);
        mPaint.setColor(fgCol);
        c.drawRoundRect(rect, 8f, 8f, mPaint);

        mTextPaint.setColor(fgCol);
        float ts = textSize;
        mTextPaint.setTextSize(ts);
        float maxW = Math.max(10f, rect.width() - 10f);
        float mW = mTextPaint.measureText(label);
        if (mW > maxW && mW > 0f) {
            ts = ts * (maxW / mW);
            mTextPaint.setTextSize(ts);
        }
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        AppI18n.drawText(c, label, rect.centerX(), rect.centerY() + ts * 0.35f, mTextPaint);
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
            return super.onTouchEvent(event);
        }
        if (mActiveDropdown != DROPDOWN_NONE) {
            if (mActiveDropdown == DROPDOWN_STYLE) {
                for (int i = 0; i < mStyleDropdownRects.length; i++) {
                    if (mStyleDropdownRects[i].contains(x, y)) {
                        applyStyleDefaults(i);
                        mActiveDropdown = DROPDOWN_NONE;
                        invalidate();
                        return true;
                    }
                }
            } else if (mActiveDropdown == DROPDOWN_PROG) {
                for (int i = 0; i < mProgDropdownRects.length; i++) {
                    if (mProgDropdownRects[i].contains(x, y)) {
                        mProgIdx = i;
                        loadBarsFromProgression(mProgIdx);
                        pushStateToEngine();
                        mActiveDropdown = DROPDOWN_NONE;
                        invalidate();
                        return true;
                    }
                }
            } else if (mActiveDropdown == DROPDOWN_KEY) {
                for (int i = 0; i < mKeyDropdownRects.length; i++) {
                    if (mKeyDropdownRects[i].contains(x, y)) {
                        mKeyPc = i;
                        pushStateToEngine();
                        mActiveDropdown = DROPDOWN_NONE;
                        invalidate();
                        return true;
                    }
                }
            }
            if (mBtnStyleDropdown.contains(x, y)) {
                mActiveDropdown = (mActiveDropdown == DROPDOWN_STYLE) ? DROPDOWN_NONE : DROPDOWN_STYLE;
            } else if (mBtnProgDropdown.contains(x, y)) {
                mActiveDropdown = (mActiveDropdown == DROPDOWN_PROG) ? DROPDOWN_NONE : DROPDOWN_PROG;
            } else if (mBtnKeyDropdown.contains(x, y)) {
                mActiveDropdown = (mActiveDropdown == DROPDOWN_KEY) ? DROPDOWN_NONE : DROPDOWN_KEY;
            } else {
                mActiveDropdown = DROPDOWN_NONE;
            }
            invalidate();
            return true;
        }
        if (mBtnHelp.contains(x, y)) {
            mShowHelp = true;
            invalidate();
            return true;
        }
        if (mBtnPlay.contains(x, y)) {
            mEngine.setPlaying(!mEngine.isPlaying(), false);
            invalidate();
            return true;
        }
        if (mBtnFill.contains(x, y)) {
            mEngine.setFillQueued(!mEngine.isFillQueued());
            invalidate();
            return true;
        }
        if (mBtnAutoFill.contains(x, y)) {
            mAutoFill = !mAutoFill;
            pushStateToEngine();
            invalidate();
            return true;
        }
        if (mBtnReset.contains(x, y)) {
            mEngine.resetStylePatternToDefault(mStyleIdx);
            loadBarsFromProgression(mProgIdx);
            pushStateToEngine();
            invalidate();
            return true;
        }
        if (mBtnStyleDropdown.contains(x, y)) {
            mActiveDropdown = DROPDOWN_STYLE;
            invalidate();
            return true;
        }
        if (mBtnProgDropdown.contains(x, y)) {
            mActiveDropdown = DROPDOWN_PROG;
            invalidate();
            return true;
        }
        if (mBtnKeyDropdown.contains(x, y)) {
            mActiveDropdown = DROPDOWN_KEY;
            invalidate();
            return true;
        }
        if (mBtnBpmPrev.contains(x, y) || mBtnBpmNext.contains(x, y)) {
            int d = mBtnBpmNext.contains(x, y) ? 5 : -5;
            mBpm = Math.max(40, Math.min(240, mBpm + d));
            pushStateToEngine();
            invalidate();
            return true;
        }
        if (mBtnMuteDrums.contains(x, y)) {
            mMuteDrums = !mMuteDrums;
            pushStateToEngine();
            invalidate();
            return true;
        }
        if (mBtnMuteBass.contains(x, y)) {
            mMuteBass = !mMuteBass;
            pushStateToEngine();
            invalidate();
            return true;
        }
        if (mBtnMuteChords.contains(x, y)) {
            mMuteChords = !mMuteChords;
            pushStateToEngine();
            invalidate();
            return true;
        }
        if (mBtnVolDrums.contains(x, y)) {
            mVolDrums = (mVolDrums >= 100) ? 40 : mVolDrums + 20;
            pushStateToEngine();
            invalidate();
            return true;
        }
        if (mBtnVolBass.contains(x, y)) {
            mVolBass = (mVolBass >= 100) ? 40 : mVolBass + 20;
            pushStateToEngine();
            invalidate();
            return true;
        }
        if (mBtnVolChords.contains(x, y)) {
            mVolChords = (mVolChords >= 100) ? 40 : mVolChords + 20;
            pushStateToEngine();
            invalidate();
            return true;
        }

        for (int i = 0; i < mBarRects.length; i++) {
            if (mBarRects[i].contains(x, y)) {
                mBars[i] = BandTheory.nextChordCandidate(mBars[i], 1);
                pushStateToEngine();
                mEngine.triggerChordPreview(mKeyPc, mBars[i]);
                invalidate();
                return true;
            }
        }

        int spb = BandTheory.STYLES[mStyleIdx].stepsPerBar;
        for (int t = 0; t < BandTheory.NUM_TRACKS; t++) {
            for (int s = 0; s < spb; s++) {
                if (mStepRects[t][s].contains(x, y)) {
                    int cur = mEngine.getStepValue(mStyleIdx, t, s);
                    int mod = (t == BandTheory.TRK_BASS) ? 7 : 3;
                    mEngine.setStepValue(mStyleIdx, t, s, (cur + 1) % mod);
                    invalidate();
                    return true;
                }
            }
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_SPACE:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                mEngine.setPlaying(!mEngine.isPlaying(), false);
                invalidate();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mKeyPc = (mKeyPc + 11) % 12;
                pushStateToEngine();
                invalidate();
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mKeyPc = (mKeyPc + 1) % 12;
                pushStateToEngine();
                invalidate();
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                mBpm = Math.min(240, mBpm + 5);
                pushStateToEngine();
                invalidate();
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mBpm = Math.max(40, mBpm - 5);
                pushStateToEngine();
                invalidate();
                return true;
            case KeyEvent.KEYCODE_H:
                mShowHelp = !mShowHelp;
                invalidate();
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }
}
