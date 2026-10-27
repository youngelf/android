package com.eggwall.metronome;

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

/**
 * Custom hardware-accelerated 60 FPS Canvas view for the Precision Metronome.
 *
 * <p>Renders both Portrait and Landscape studio layouts with a cosine-eased
 * visual pendulum, interactive beat-accent sequencer orbs, circular BPM dial,
 * Tap Tempo, 4-bar Lock-In / Drop% inner-clock trainer, and an in-app Help
 * overlay modal (`[?]`).
 */
public final class MetronomeView extends View {

    private static final String PREFS_NAME = "metronome_prefs";
    private static final String KEY_BPM = "bpm";
    private static final String KEY_TIME_SIG = "time_sig";
    private static final String KEY_SUBDIV = "subdiv";
    private static final String KEY_SOUND_KIT = "sound_kit";
    private static final String KEY_DROP_PCT = "drop_pct";
    private static final String KEY_TRAINER = "trainer_idx";

    /* Studio Slate & Warm Amber/Coral/Cyan Palette */
    private static final int COL_BG_TOP = 0xFF0A0E17;
    private static final int COL_BG_BOT = 0xFF111827;
    private static final int COL_CARD = 0xFF172033;
    private static final int COL_CARD_ALT = 0xFF1E293B;
    private static final int COL_BORDER = 0xFF2E3C56;
    private static final int COL_AMBER = 0xFFF59E0B;
    private static final int COL_CORAL = 0xFFF97316;
    private static final int COL_CYAN = 0xFF38BDF8;
    private static final int COL_EMERALD = 0xFF10B981;
    private static final int COL_PURPLE = 0xFFA855F7;
    private static final int COL_ROSE = 0xFFF43F5E;
    private static final int COL_TEXT = 0xFFF8FAFC;
    private static final int COL_SUBTEXT = 0xFF94A3B8;
    private static final int COL_MUTED = 0xFF475569;

    private final MetronomeSynthEngine mEngine;
    private final SharedPreferences mPrefs;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path mPath = new Path();
    private LinearGradient mBgGrad;
    private int mLastW, mLastH;

    /* Interactive Hit Rectangles */
    private final RectF mBtnHelp = new RectF();
    private final RectF mBtnSoundHeader = new RectF();
    private final RectF mDialRect = new RectF();
    private final RectF[] mBeatRects = new RectF[12];
    private final RectF[] mBpmStepRects = new RectF[5]; // -5, -1, TAP, +1, +5
    private final RectF[] mGridCardRects = new RectF[4]; // TimeSig, Subdiv, Drop%, Trainer
    private final RectF mBtnPlay = new RectF();
    private final RectF mModalCloseRect = new RectF();

    private boolean mShowHelpModal = false;
    private boolean mDraggingDial = false;

    /* Tap Tempo Ring Buffer */
    private final long[] mTapTimes = new long[6];
    private int mTapCount = 0;

    public MetronomeView(Context context, MetronomeSynthEngine engine) {
        super(context);
        mEngine = engine;
        mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        setFocusable(true);
        setFocusableInTouchMode(true);

        for (int i = 0; i < mBeatRects.length; i++) mBeatRects[i] = new RectF();
        for (int i = 0; i < mBpmStepRects.length; i++) mBpmStepRects[i] = new RectF();
        for (int i = 0; i < mGridCardRects.length; i++) mGridCardRects[i] = new RectF();

        mTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        loadPreferences();
    }

    private void loadPreferences() {
        mEngine.setBpm(mPrefs.getInt(KEY_BPM, MetronomeTheory.DEFAULT_BPM));
        mEngine.setTimeSignatureIndex(mPrefs.getInt(KEY_TIME_SIG, 0), true);
        mEngine.setSubdivisionIndex(mPrefs.getInt(KEY_SUBDIV, 0));
        mEngine.setSoundKitIndex(mPrefs.getInt(KEY_SOUND_KIT, 0));
        mEngine.setDropPct(mPrefs.getInt(KEY_DROP_PCT, 0));
        mEngine.setTrainerIndex(mPrefs.getInt(KEY_TRAINER, 0));
    }

    public void savePreferences() {
        mPrefs.edit()
                .putInt(KEY_BPM, mEngine.getBaseBpm())
                .putInt(KEY_TIME_SIG, mEngine.getTimeSignatureIndex())
                .putInt(KEY_SUBDIV, mEngine.getSubdivisionIndex())
                .putInt(KEY_SOUND_KIT, mEngine.getSoundKitIndex())
                .putInt(KEY_DROP_PCT, mEngine.getDropPct())
                .putInt(KEY_TRAINER, mEngine.getTrainerIndex())
                .apply();
    }

    /** Configures deterministic visual & engine state for automated demo screenshots. */
    public void configureDemoState(int bpm, int timeSigIdx, int subdivIdx, int soundKitIdx,
                                   int dropPct, int trainerIdx, boolean playing, boolean helpModal) {
        mEngine.setBpm(bpm);
        mEngine.setTimeSignatureIndex(timeSigIdx, true);
        mEngine.setSubdivisionIndex(subdivIdx);
        mEngine.setSoundKitIndex(soundKitIdx);
        mEngine.setDropPct(dropPct);
        mEngine.setTrainerIndex(trainerIdx);
        mEngine.setPlaying(playing);
        if (playing) {
            mEngine.configureDemoTelemetry(0, 4, 0.22, false);
        }
        mShowHelpModal = helpModal;
        invalidate();
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        if (w != mLastW || h != mLastH || mBgGrad == null) {
            mLastW = w;
            mLastH = h;
            mBgGrad = new LinearGradient(0, 0, 0, h, COL_BG_TOP, COL_BG_BOT, Shader.TileMode.CLAMP);
        }
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setShader(mBgGrad);
        canvas.drawRect(0, 0, w, h, mPaint);
        mPaint.setShader(null);

        float pad = dp(14f);
        float headerH = dp(48f);
        drawHeaderBar(canvas, pad, dp(8f), w - pad * 2f, headerH);

        float contentTop = dp(64f);
        float contentH = h - contentTop - dp(12f);
        if (w > h * 1.15f) {
            drawLandscapeLayout(canvas, pad, contentTop, w - pad * 2f, contentH);
        } else {
            drawPortraitLayout(canvas, pad, contentTop, w - pad * 2f, contentH);
        }

        if (mShowHelpModal) {
            drawHelpModal(canvas, w, h);
        }

        if (mEngine.isPlaying()) {
            postInvalidateOnAnimation();
        }
    }

    private void drawHeaderBar(Canvas c, float x, float y, float w, float h) {
        mBtnHelp.set(x, y, x + h, y + h);
        drawRoundCard(c, mBtnHelp, dp(12f), COL_CARD_ALT, COL_CYAN, dp(1.5f));
        drawCenteredText(c, "?", mBtnHelp.centerX(), mBtnHelp.centerY(), dp(20f), COL_CYAN);

        float soundW = dp(118f);
        mBtnSoundHeader.set(x + w - soundW, y, x + w, y + h);
        drawRoundCard(c, mBtnSoundHeader, dp(12f), COL_CARD_ALT, COL_AMBER, dp(1.2f));
        String kitBadge = MetronomeTheory.SOUND_KIT_BADGES[mEngine.getSoundKitIndex()];
        drawCenteredText(c, "KIT: " + kitBadge, mBtnSoundHeader.centerX(), mBtnSoundHeader.centerY(), dp(11.5f), COL_AMBER);

        float titleCx = (mBtnHelp.right + mBtnSoundHeader.left) * 0.5f;
        drawCenteredText(c, "PRECISION METRONOME", titleCx, y + h * 0.38f, dp(13.5f), COL_TEXT);
        String subStatus = mEngine.isPlaying()
                ? ("BAR " + (mEngine.getMeasureCount() + 1) + "  •  44.1 kHz CLOCK")
                : "44.1 kHz SYNTH  •  TAP ORB TO ACCENT";
        drawCenteredText(c, subStatus, titleCx, y + h * 0.74f, dp(9.0f), mEngine.isPlaying() ? COL_EMERALD : COL_SUBTEXT);
    }

    private void drawPortraitLayout(Canvas c, float x, float y, float w, float h) {
        float gap = dp(10f);
        float pendH = h * 0.17f;
        RectF pendCard = new RectF(x, y, x + w, y + pendH);
        drawPendulumCard(c, pendCard);

        float curY = pendCard.bottom + gap;
        float beatH = h * 0.13f;
        RectF beatCard = new RectF(x, curY, x + w, curY + beatH);
        drawBeatSequencerCard(c, beatCard);

        curY = beatCard.bottom + gap;
        float dialH = h * 0.28f;
        mDialRect.set(x, curY, x + w, curY + dialH);
        drawBpmDialCard(c, mDialRect);

        curY = mDialRect.bottom + gap;
        float stepH = h * 0.085f;
        drawBpmStepperRow(c, x, curY, w, stepH);

        curY += stepH + gap;
        float gridH = h * 0.165f;
        drawParameterGrid(c, x, curY, w, gridH);

        curY += gridH + gap;
        float playH = Math.max(dp(44f), (y + h) - curY);
        mBtnPlay.set(x, curY, x + w, curY + playH);
        drawTransportButton(c, mBtnPlay);
    }

    private void drawLandscapeLayout(Canvas c, float x, float y, float w, float h) {
        float gap = dp(12f);
        float leftW = w * 0.46f;
        float rightX = x + leftW + gap;
        float rightW = w - leftW - gap;

        float pendH = h * 0.34f;
        RectF pendCard = new RectF(x, y, x + leftW, y + pendH);
        drawPendulumCard(c, pendCard);

        float dialY = pendCard.bottom + gap;
        mDialRect.set(x, dialY, x + leftW, y + h);
        drawBpmDialCard(c, mDialRect);

        float rY = y;
        float beatH = h * 0.24f;
        RectF beatCard = new RectF(rightX, rY, rightX + rightW, rY + beatH);
        drawBeatSequencerCard(c, beatCard);

        rY = beatCard.bottom + gap;
        float stepH = h * 0.16f;
        drawBpmStepperRow(c, rightX, rY, rightW, stepH);

        rY += stepH + gap;
        float gridH = h * 0.34f;
        drawParameterGrid(c, rightX, rY, rightW, gridH);

        rY += gridH + gap;
        mBtnPlay.set(rightX, rY, rightX + rightW, y + h);
        drawTransportButton(c, mBtnPlay);
    }

    private void drawPendulumCard(Canvas c, RectF r) {
        drawRoundCard(c, r, dp(14f), COL_CARD, COL_BORDER, dp(1.2f));
        float cx = r.centerX();
        float pivotY = r.bottom - dp(12f);
        float armLen = r.height() - dp(24f);
        float maxDeg = 38f;

        /* Draw tick arc */
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(dp(2f));
        mPaint.setColor(0xFF28354E);
        RectF arcRect = new RectF(cx - armLen, pivotY - armLen, cx + armLen, pivotY + armLen);
        c.drawArc(arcRect, 270f - maxDeg, maxDeg * 2f, false, mPaint);

        float angleDeg = 0f;
        if (mEngine.isPlaying()) {
            angleDeg = MetronomeTheory.computePendulumAngleDeg(
                    mEngine.getCurrentBeat(), mEngine.getBeatPhase0to1(), maxDeg);
        }
        double rad = Math.toRadians(angleDeg);
        float tipX = cx + (float) Math.sin(rad) * armLen;
        float tipY = pivotY - (float) Math.cos(rad) * armLen;

        int armColor = mEngine.isPlaying()
                ? (mEngine.isLastBeatDropped() ? COL_PURPLE : (mEngine.getCurrentBeat() == 0 ? COL_CORAL : COL_CYAN))
                : COL_AMBER;

        mPaint.setColor(armColor);
        mPaint.setStrokeWidth(dp(3.5f));
        c.drawLine(cx, pivotY, tipX, tipY, mPaint);

        mPaint.setStyle(Paint.Style.FILL);
        float weightR = dp(7.5f);
        float weightX = cx + (float) Math.sin(rad) * (armLen * 0.72f);
        float weightY = pivotY - (float) Math.cos(rad) * (armLen * 0.72f);
        c.drawCircle(weightX, weightY, weightR, mPaint);
        mPaint.setColor(COL_TEXT);
        c.drawCircle(cx, pivotY, dp(5f), mPaint);

        /* Status telemetry badges inside pendulum corners */
        MetronomeTheory.TimeSignature ts = MetronomeTheory.TIME_SIGNATURES[mEngine.getTimeSignatureIndex()];
        drawLeftText(c, ts.name + " " + ts.feelName.toUpperCase(), r.left + dp(12f), r.top + dp(18f), dp(11f), COL_CYAN);

        int drop = mEngine.getDropPct();
        int bar = mEngine.getMeasureCount();
        String lockLabel = (drop == 0) ? " STEADY CLICK"
                : (bar < MetronomeTheory.LOCK_IN_MEASURES
                        ? ("LOCK-IN " + (bar + 1) + "/4")
                        : (mEngine.isLastBeatDropped() ? "STEALTH DROP!" : ("DROP " + drop + "% ACTIVE")));
        int lockCol = (drop > 0 && bar >= 4) ? COL_PURPLE : COL_SUBTEXT;
        drawRightText(c, lockLabel, r.right - dp(12f), r.top + dp(18f), dp(11f), lockCol);
    }

    private void drawBeatSequencerCard(Canvas c, RectF r) {
        drawRoundCard(c, r, dp(14f), COL_CARD, COL_BORDER, dp(1.2f));
        int beats = mEngine.getBeatsPerBar();
        float padX = dp(10f);
        float availW = r.width() - padX * 2f;
        float slotW = availW / beats;
        float orbR = Math.min(dp(18f), Math.min(slotW * 0.38f, r.height() * 0.28f));

        long elapsedMs = SystemClock.uptimeMillis() - mEngine.getLastBeatUptimeMs();
        float pulseScale = (mEngine.isPlaying() && elapsedMs >= 0 && elapsedMs < 140)
                ? (1.0f + 0.22f * (1.0f - elapsedMs / 140f)) : 1.0f;

        for (int i = 0; i < mBeatRects.length; i++) {
            if (i >= beats) {
                mBeatRects[i].setEmpty();
                continue;
            }
            float cx = r.left + padX + slotW * (i + 0.5f);
            float cy = r.centerY() - dp(5f);
            mBeatRects[i].set(cx - slotW * 0.48f, r.top + dp(4f), cx + slotW * 0.48f, r.bottom - dp(4f));

            int state = mEngine.getBeatState(i);
            boolean active = mEngine.isPlaying() && (mEngine.getCurrentBeat() == i);
            int baseCol = (state == MetronomeTheory.BEAT_ACCENT) ? COL_CORAL
                    : (state == MetronomeTheory.BEAT_SUB_ACCENT) ? COL_AMBER
                    : (state == MetronomeTheory.BEAT_NORMAL) ? COL_CYAN : COL_MUTED;

            float drawR = active ? orbR * pulseScale : orbR;
            if (active) {
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor((baseCol & 0x00FFFFFF) | 0x44000000);
                c.drawCircle(cx, cy, drawR * 1.38f, mPaint);
            }

            mPaint.setStyle(state == MetronomeTheory.BEAT_MUTE ? Paint.Style.STROKE : Paint.Style.FILL);
            mPaint.setStrokeWidth(dp(2f));
            mPaint.setColor(baseCol);
            c.drawCircle(cx, cy, drawR, mPaint);

            int numCol = (state == MetronomeTheory.BEAT_MUTE) ? COL_SUBTEXT : COL_BG_TOP;
            drawCenteredText(c, String.valueOf(i + 1), cx, cy, dp(11.5f), numCol);
            drawCenteredText(c, MetronomeTheory.getBeatStateLabel(state),
                    cx, r.bottom - dp(10f), dp(8.5f), active ? COL_TEXT : COL_SUBTEXT);
        }
    }

    private void drawBpmDialCard(Canvas c, RectF r) {
        drawRoundCard(c, r, dp(16f), COL_CARD, COL_BORDER, dp(1.2f));
        float cx = r.centerX();
        float cy = r.centerY() + dp(4f);
        float radius = Math.min(r.width(), r.height()) * 0.41f;

        RectF arc = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(dp(9f));
        mPaint.setColor(COL_CARD_ALT);
        c.drawArc(arc, 135f, 270f, false, mPaint);

        int effBpm = mEngine.getEffectiveBpm();
        float norm = (float) (effBpm - MetronomeTheory.MIN_BPM)
                / (float) (MetronomeTheory.MAX_BPM - MetronomeTheory.MIN_BPM);
        mPaint.setColor(COL_AMBER);
        c.drawArc(arc, 135f, 270f * norm, false, mPaint);

        /* Knob thumb indicator on the dial ring */
        double thumbRad = Math.toRadians(135f + 270f * norm);
        float tx = cx + (float) Math.cos(thumbRad) * radius;
        float ty = cy + (float) Math.sin(thumbRad) * radius;
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(COL_TEXT);
        c.drawCircle(tx, ty, dp(6.5f), mPaint);

        drawCenteredText(c, String.valueOf(effBpm), cx, cy - dp(10f), radius * 0.64f, COL_TEXT);
        String marking = MetronomeTheory.getTempoMarking(effBpm);
        drawCenteredText(c, marking + "  •  BPM", cx, cy + radius * 0.36f, dp(12f), COL_AMBER);
        drawCenteredText(c, MetronomeTheory.getTempoDescription(effBpm),
                cx, cy + radius * 0.66f, dp(10.5f), COL_SUBTEXT);
    }

    private void drawBpmStepperRow(Canvas c, float x, float y, float w, float h) {
        String[] labels = { "-5", "-1", "TAP TEMPO", "+1", "+5" };
        float[] weights = { 0.17f, 0.17f, 0.32f, 0.17f, 0.17f };
        float gap = dp(6f);
        float availW = w - gap * 4f;
        float curX = x;
        for (int i = 0; i < 5; i++) {
            float bw = availW * weights[i];
            mBpmStepRects[i].set(curX, y, curX + bw, y + h);
            boolean isTap = (i == 2);
            int stroke = isTap ? COL_CYAN : COL_BORDER;
            int textCol = isTap ? COL_CYAN : COL_TEXT;
            drawRoundCard(c, mBpmStepRects[i], dp(10f), COL_CARD_ALT, stroke, dp(1.2f));
            drawCenteredText(c, labels[i], mBpmStepRects[i].centerX(), mBpmStepRects[i].centerY(),
                    isTap ? dp(12.5f) : dp(14f), textCol);
            curX += bw + gap;
        }
    }

    private void drawParameterGrid(Canvas c, float x, float y, float w, float h) {
        float gap = dp(8f);
        float cw = (w - gap) * 0.5f;
        float ch = (h - gap) * 0.5f;

        mGridCardRects[0].set(x, y, x + cw, y + ch);
        mGridCardRects[1].set(x + cw + gap, y, x + w, y + ch);
        mGridCardRects[2].set(x, y + ch + gap, x + cw, y + h);
        mGridCardRects[3].set(x + cw + gap, y + ch + gap, x + w, y + h);

        MetronomeTheory.TimeSignature ts = MetronomeTheory.TIME_SIGNATURES[mEngine.getTimeSignatureIndex()];
        MetronomeTheory.Subdivision sub = MetronomeTheory.SUBDIVISIONS[mEngine.getSubdivisionIndex()];
        int drop = mEngine.getDropPct();
        String trainer = MetronomeTheory.TRAINER_LABELS[mEngine.getTrainerIndex()];

        drawParamTile(c, mGridCardRects[0], "TIME SIGNATURE", ts.name + "  (" + ts.feelName + ")", COL_CYAN);
        drawParamTile(c, mGridCardRects[1], "SUBDIVISION", sub.shortBadge + " • " + sub.label, COL_AMBER);
        drawParamTile(c, mGridCardRects[2], "STEALTH DROP %",
                drop == 0 ? "0% (OFF)" : (drop + "% AFTER 4 BARS"), drop > 0 ? COL_PURPLE : COL_EMERALD);
        drawParamTile(c, mGridCardRects[3], "SPEED TRAINER", trainer,
                mEngine.getTrainerIndex() > 0 ? COL_CORAL : COL_SUBTEXT);
    }

    private void drawParamTile(Canvas c, RectF r, String title, String value, int accent) {
        drawRoundCard(c, r, dp(11f), COL_CARD_ALT, COL_BORDER, dp(1f));
        drawCenteredText(c, title, r.centerX(), r.top + r.height() * 0.34f, dp(9.5f), COL_SUBTEXT);
        drawCenteredText(c, value, r.centerX(), r.top + r.height() * 0.70f, dp(12f), accent);
    }

    private void drawTransportButton(Canvas c, RectF r) {
        boolean playing = mEngine.isPlaying();
        int fill = playing ? COL_ROSE : COL_EMERALD;
        drawRoundCard(c, r, dp(14f), fill, COL_TEXT, dp(1.2f));
        String label = playing ? "■   STOP METRONOME   (SPACE)" : "▶   START METRONOME   (SPACE)";
        drawCenteredText(c, label, r.centerX(), r.centerY(), dp(15f), COL_BG_TOP);
    }

    private void drawHelpModal(Canvas c, int w, int h) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(0xD8050811);
        c.drawRect(0, 0, w, h, mPaint);

        float mw = Math.min(w - dp(28f), dp(460f));
        float mh = Math.min(h - dp(36f), dp(420f));
        RectF card = new RectF((w - mw) * 0.5f, (h - mh) * 0.5f, (w + mw) * 0.5f, (h + mh) * 0.5f);
        drawRoundCard(c, card, dp(18f), COL_CARD, COL_CYAN, dp(2f));

        drawCenteredText(c, "PRECISION METRONOME • QUICK GUIDE",
                card.centerX(), card.top + dp(28f), dp(15f), COL_CYAN);

        String[] lines = {
            "• SAMPLE-ACCURATE CLOCK: 44.1 kHz PCM synthesizer with <0.03 ms jitter.",
            "• BEAT ACCENT ORBS: Tap any numbered beat circle (1..12) to cycle",
            "  between ACCENT (Coral), NORMAL (Cyan), SUB-ACCENT (Amber), & MUTE.",
            "• BPM DIAL & TAP TEMPO: Drag the circular BPM ring, use -5/-1/+1/+5,",
            "  or tap 'TAP TEMPO' repeatedly to match any live song's groove.",
            "• 4-BAR LOCK-IN & DROP %: Plays 4 full bars to lock in your timing,",
            "  then randomly silences clicks to train your internal rhythm clock.",
            "• SPEED TRAINER: Automatically steps up BPM every 4 bars (+2/+4/+5).",
            "• SOUND KITS: Tap 'KIT' in the top right for V90, Woodblock, Rim, or Sine."
        };
        float ly = card.top + dp(64f);
        float stepY = (card.height() - dp(122f)) / lines.length;
        for (String line : lines) {
            drawLeftText(c, line, card.left + dp(18f), ly, dp(11.5f), COL_TEXT);
            ly += stepY;
        }

        mModalCloseRect.set(card.centerX() - dp(90f), card.bottom - dp(46f),
                card.centerX() + dp(90f), card.bottom - dp(12f));
        drawRoundCard(c, mModalCloseRect, dp(10f), COL_CYAN, COL_TEXT, dp(1f));
        drawCenteredText(c, "GOT IT • RESUME", mModalCloseRect.centerX(), mModalCloseRect.centerY(), dp(12.5f), COL_BG_TOP);
    }

    private void drawRoundCard(Canvas c, RectF r, float rad, int fillCol, int strokeCol, float strokeW) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(fillCol);
        c.drawRoundRect(r, rad, rad, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(strokeW);
        mPaint.setColor(strokeCol);
        c.drawRoundRect(r, rad, rad, mPaint);
    }

    private void drawCenteredText(Canvas c, String text, float cx, float cy, float sizePx, int color) {
        mTextPaint.setTextSize(sizePx);
        mTextPaint.setColor(color);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        c.drawText(text, cx, cy - (fm.ascent + fm.descent) * 0.5f, mTextPaint);
    }

    private void drawLeftText(Canvas c, String text, float x, float cy, float sizePx, int color) {
        mTextPaint.setTextSize(sizePx);
        mTextPaint.setColor(color);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        c.drawText(text, x, cy - (fm.ascent + fm.descent) * 0.5f, mTextPaint);
    }

    private void drawRightText(Canvas c, String text, float x, float cy, float sizePx, int color) {
        mTextPaint.setTextSize(sizePx);
        mTextPaint.setColor(color);
        mTextPaint.setTextAlign(Paint.Align.RIGHT);
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        c.drawText(text, x, cy - (fm.ascent + fm.descent) * 0.5f, mTextPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int action = event.getActionMasked();

        if (mShowHelpModal) {
            if (action == MotionEvent.ACTION_DOWN) {
                mShowHelpModal = false;
                invalidate();
            }
            return true;
        }

        if (action == MotionEvent.ACTION_DOWN) {
            if (mBtnHelp.contains(x, y)) {
                mShowHelpModal = true;
                invalidate();
                return true;
            }
            if (mBtnSoundHeader.contains(x, y)) {
                mEngine.setSoundKitIndex(mEngine.getSoundKitIndex() + 1);
                savePreferences();
                invalidate();
                return true;
            }
            if (mBtnPlay.contains(x, y)) {
                mEngine.setPlaying(!mEngine.isPlaying());
                invalidate();
                return true;
            }
            for (int i = 0; i < mEngine.getBeatsPerBar(); i++) {
                if (mBeatRects[i].contains(x, y)) {
                    mEngine.cycleBeatState(i);
                    invalidate();
                    return true;
                }
            }
            int[] deltas = { -5, -1, 0, +1, +5 };
            for (int i = 0; i < 5; i++) {
                if (mBpmStepRects[i].contains(x, y)) {
                    if (i == 2) {
                        handleTapTempo();
                    } else {
                        mEngine.setBpm(mEngine.getBaseBpm() + deltas[i]);
                        savePreferences();
                    }
                    invalidate();
                    return true;
                }
            }
            if (mGridCardRects[0].contains(x, y)) {
                mEngine.setTimeSignatureIndex(mEngine.getTimeSignatureIndex() + 1, true);
                savePreferences();
                invalidate();
                return true;
            }
            if (mGridCardRects[1].contains(x, y)) {
                mEngine.setSubdivisionIndex(mEngine.getSubdivisionIndex() + 1);
                savePreferences();
                invalidate();
                return true;
            }
            if (mGridCardRects[2].contains(x, y)) {
                int nextDrop = (mEngine.getDropPct() + 10);
                if (nextDrop > 80) nextDrop = 0;
                mEngine.setDropPct(nextDrop);
                savePreferences();
                invalidate();
                return true;
            }
            if (mGridCardRects[3].contains(x, y)) {
                mEngine.setTrainerIndex(mEngine.getTrainerIndex() + 1);
                savePreferences();
                invalidate();
                return true;
            }
            if (mDialRect.contains(x, y)) {
                mDraggingDial = true;
                updateBpmFromDialTouch(x, y);
                return true;
            }
        } else if (action == MotionEvent.ACTION_MOVE && mDraggingDial) {
            updateBpmFromDialTouch(x, y);
            return true;
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (mDraggingDial) {
                mDraggingDial = false;
                savePreferences();
            }
        }
        return true;
    }

    private void updateBpmFromDialTouch(float x, float y) {
        float dx = x - mDialRect.centerX();
        float dy = y - mDialRect.centerY();
        double deg = Math.toDegrees(Math.atan2(dy, dx));
        double rel = (deg - 135.0 + 360.0) % 360.0;
        if (rel > 270.0) {
            rel = (rel < 315.0) ? 270.0 : 0.0;
        }
        int bpm = MetronomeTheory.MIN_BPM
                + (int) Math.round((rel / 270.0) * (MetronomeTheory.MAX_BPM - MetronomeTheory.MIN_BPM));
        mEngine.setBpm(bpm);
        invalidate();
    }

    private void handleTapTempo() {
        long now = SystemClock.uptimeMillis();
        if (mTapCount > 0 && (now - mTapTimes[mTapCount - 1]) > MetronomeTheory.TAP_RESET_THRESHOLD_MS) {
            mTapCount = 0;
        }
        if (mTapCount < mTapTimes.length) {
            mTapTimes[mTapCount++] = now;
        } else {
            System.arraycopy(mTapTimes, 1, mTapTimes, 0, mTapTimes.length - 1);
            mTapTimes[mTapTimes.length - 1] = now;
        }
        mEngine.triggerPreviewClick(MetronomeTheory.BEAT_ACCENT);
        int computed = MetronomeTheory.computeTapTempoBpm(mTapTimes, mTapCount);
        if (computed > 0) {
            mEngine.setBpm(computed);
            savePreferences();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_SPACE:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                mEngine.setPlaying(!mEngine.isPlaying());
                invalidate();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mEngine.setBpm(mEngine.getBaseBpm() - 1);
                savePreferences();
                invalidate();
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mEngine.setBpm(mEngine.getBaseBpm() + 1);
                savePreferences();
                invalidate();
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                mEngine.setBpm(mEngine.getBaseBpm() + 5);
                savePreferences();
                invalidate();
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mEngine.setBpm(mEngine.getBaseBpm() - 5);
                savePreferences();
                invalidate();
                return true;
            case KeyEvent.KEYCODE_T:
                handleTapTempo();
                invalidate();
                return true;
            case KeyEvent.KEYCODE_H:
            case KeyEvent.KEYCODE_SLASH:
                mShowHelpModal = !mShowHelpModal;
                invalidate();
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }
}
