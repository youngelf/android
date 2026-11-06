package com.eggwall.rhythmtrainer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom hardware-accelerated Canvas View for Rhythm Trainer.
 * Compatible with Android API 19+ (KitKat) through API 36 with zero external dependencies.
 */
public class RhythmTrainerView extends View implements RhythmAudioEngine.PlaybackListener {

    private static final String PREFS_NAME = "rhythm_trainer_prefs";
    private static final int MAX_BARS = 4;

    // Studio color palette
    private static final int COL_BG_TOP = 0xFF0B101B;
    private static final int COL_BG_BOT = 0xFF111827;
    private static final int COL_CARD_DARK = 0xFF162032;
    private static final int COL_CARD_BORDER = 0xFF283548;
    private static final int COL_SCORE_BG = 0xFFFFFDF7;
    private static final int COL_SCORE_ROW_BG = 0xFFF8F5EC;
    private static final int COL_SCORE_BORDER = 0xFFD6D0C2;
    private static final int COL_INK = 0xFF0F172A;
    private static final int COL_INK_MUTED = 0xFF475569;
    private static final int COL_TEXT_MAIN = 0xFFF8FAFC;
    private static final int COL_TEXT_SUB = 0xFF94A3B8;
    private static final int COL_CYAN = 0xFF38BDF8;
    private static final int COL_PRIMARY = 0xFF2563EB;
    private static final int COL_EMERALD = 0xFF10B981;
    private static final int COL_EMERALD_DARK = 0xFF059669;
    private static final int COL_CRIMSON = 0xFFEF4444;
    private static final int COL_AMBER = 0xFFF59E0B;
    private static final int COL_AMBER_DARK = 0xFF92400E;

    private final float dp;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint serifPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    private final RhythmAudioEngine audioEngine = new RhythmAudioEngine();

    // State
    private TimeSignature timeSignature = TimeSignature.FOUR_FOUR;
    private List<Measure> measures = RhythmBuilder.defaultMeasuresFor(TimeSignature.FOUR_FOUR);
    private boolean isEditMode = false;
    private ElementPosition selectedPosition = new ElementPosition(0, 0);
    private boolean isPlaying = false;
    private Integer countdownValue = null;
    private ElementPosition activePlayingPosition = null;
    private int bpm = 100;
    private boolean loopPlayback = false;
    private boolean showHelp = false;
    private float helpScrollY = 0f;
    private float helpMaxScrollY = 0f;
    private float helpLastTouchY = 0f;
    private boolean helpDragging = false;
    private boolean draggingTempo = false;

    // Hit-test rectangles
    private final RectF rectHelpBtn = new RectF();
    private final RectF rectLoopBtn = new RectF();
    private final RectF rectPlayBtn = new RectF();
    private final RectF[] rectTimeSigs = new RectF[TimeSignature.values().length];
    private final RectF rectBpmMinus = new RectF();
    private final RectF rectBpmSlider = new RectF();
    private final RectF rectBpmPlus = new RectF();
    private final RectF rectEditToggle = new RectF();
    private final RectF rectRandomBtn = new RectF();

    private final List<CellHit> cellHits = new ArrayList<CellHit>();
    private final RectF[] rectNoteTiles = new RectF[NoteDuration.values().length];
    private final RectF[] rectRestTiles = new RectF[NoteDuration.values().length];
    private final RectF rectAddBar = new RectF();
    private final RectF rectRemoveBar = new RectF();
    private final RectF rectRandomDock = new RectF();
    private final RectF rectQuarterReset = new RectF();
    private final RectF rectDoneEditDock = new RectF();

    private final RectF rectHelpModal = new RectF();
    private final RectF rectHelpClose = new RectF();

    private static final class CellHit {
        final ElementPosition pos;
        final RectF rect;

        CellHit(ElementPosition pos, RectF rect) {
            this.pos = pos;
            this.rect = rect;
        }
    }

    public RhythmTrainerView(Context context) {
        super(context);
        dp = context.getResources().getDisplayMetrics().density;
        serifPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        for (int i = 0; i < rectTimeSigs.length; i++) {
            rectTimeSigs[i] = new RectF();
        }
        for (int i = 0; i < rectNoteTiles.length; i++) {
            rectNoteTiles[i] = new RectF();
            rectRestTiles[i] = new RectF();
        }
        loadPrefs();
    }

    public void setShowHelp(boolean show) {
        this.showHelp = show;
        this.helpScrollY = 0f;
        invalidate();
    }

    public void stopAudio() {
        audioEngine.stopPlayback();
        isPlaying = false;
        countdownValue = null;
        activePlayingPosition = null;
        invalidate();
    }

    private void loadPrefs() {
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        bpm = Math.max(40, Math.min(200, prefs.getInt("bpm", 100)));
        loopPlayback = prefs.getBoolean("loop", false);
        int sigOrd = prefs.getInt("time_sig", 0);
        TimeSignature[] sigs = TimeSignature.values();
        if (sigOrd >= 0 && sigOrd < sigs.length) {
            timeSignature = sigs[sigOrd];
            measures = RhythmBuilder.defaultMeasuresFor(timeSignature);
        }
    }

    private void savePrefs() {
        getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt("bpm", bpm)
                .putBoolean("loop", loopPlayback)
                .putInt("time_sig", timeSignature.ordinal())
                .apply();
    }

    @Override
    public void onCountdownTick(Integer count) {
        this.countdownValue = count;
        invalidate();
    }

    @Override
    public void onBarChanged(ElementPosition activeBarPosition) {
        this.activePlayingPosition = activeBarPosition;
        invalidate();
    }

    @Override
    public void onPlaybackCompleted() {
        this.isPlaying = false;
        this.countdownValue = null;
        this.activePlayingPosition = null;
        invalidate();
    }

    private RhythmElement getSelectedElement() {
        if (measures.isEmpty()) return null;
        int mIdx = Math.max(0, Math.min(selectedPosition.measureIndex, measures.size() - 1));
        Measure m = measures.get(mIdx);
        if (m.elements.isEmpty()) return null;
        int eIdx = Math.max(0, Math.min(selectedPosition.elementIndex, m.elements.size() - 1));
        return m.elements.get(eIdx);
    }

    private ElementPosition coercePosition(List<Measure> list, ElementPosition pos) {
        if (list == null || list.isEmpty()) return new ElementPosition(0, 0);
        int mIdx = Math.max(0, Math.min(pos.measureIndex, list.size() - 1));
        List<RhythmElement> elems = list.get(mIdx).elements;
        int eIdx = elems.isEmpty() ? 0 : Math.max(0, Math.min(pos.elementIndex, elems.size() - 1));
        return new ElementPosition(mIdx, eIdx);
    }

    private void selectTimeSignature(TimeSignature sig) {
        stopAudio();
        timeSignature = sig;
        measures = RhythmBuilder.defaultMeasuresFor(sig);
        selectedPosition = new ElementPosition(0, 0);
        savePrefs();
        invalidate();
    }

    private void togglePlayPause() {
        if (isPlaying) {
            stopAudio();
        } else {
            isEditMode = false;
            isPlaying = true;
            countdownValue = Integer.valueOf(RhythmAudioEngine.COUNTDOWN_START);
            activePlayingPosition = null;
            invalidate();
            audioEngine.startPlayback(measures, bpm, loopPlayback, this);
        }
    }

    private void assignDuration(NoteDuration duration, boolean isRest) {
        if (isPlaying) {
            stopAudio();
        }
        isEditMode = true;
        if (measures.isEmpty()) return;
        selectedPosition = coercePosition(measures, selectedPosition);
        Measure current = measures.get(selectedPosition.measureIndex);
        RhythmBuilder.UpdateResult res = RhythmBuilder.updateElementInMeasure(
                current,
                selectedPosition.elementIndex,
                duration,
                isRest,
                timeSignature.sixteenthUnitsPerBar
        );
        List<Measure> updated = new ArrayList<Measure>(measures);
        updated.set(selectedPosition.measureIndex, res.measure);
        measures = updated;
        selectedPosition = new ElementPosition(selectedPosition.measureIndex, res.selectedIndex);
        invalidate();
    }

    private void addMeasure() {
        stopAudio();
        if (measures.size() >= MAX_BARS) return;
        List<Measure> updated = new ArrayList<Measure>(measures);
        updated.add(RhythmBuilder.quarterNoteBar(timeSignature));
        measures = updated;
        selectedPosition = new ElementPosition(measures.size() - 1, 0);
        invalidate();
    }

    private void removeLastMeasure() {
        stopAudio();
        if (measures.size() <= 1) return;
        List<Measure> updated = new ArrayList<Measure>(measures.subList(0, measures.size() - 1));
        measures = updated;
        selectedPosition = coercePosition(measures, selectedPosition);
        invalidate();
    }

    private void randomizeRhythm() {
        stopAudio();
        int count = Math.max(1, measures.size());
        List<Measure> updated = new ArrayList<Measure>(count);
        for (int i = 0; i < count; i++) {
            updated.add(RhythmBuilder.randomMeasure(timeSignature));
        }
        measures = updated;
        selectedPosition = new ElementPosition(0, 0);
        invalidate();
    }

    private void resetToQuarterNotes() {
        stopAudio();
        int count = Math.max(1, measures.size());
        List<Measure> updated = new ArrayList<Measure>(count);
        for (int i = 0; i < count; i++) {
            updated.add(RhythmBuilder.quarterNoteBar(timeSignature));
        }
        measures = updated;
        selectedPosition = new ElementPosition(0, 0);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        // Background gradient
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(0, 0, 0, h, COL_BG_TOP, COL_BG_BOT, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, paint);
        paint.setShader(null);

        boolean landscape = w > h * 1.15f;
        float pad = 10f * dp;
        float headerH = (landscape ? 46f : 54f) * dp;

        drawHeader(canvas, pad, pad * 0.6f, w - pad * 2f, headerH);

        float topY = pad * 0.6f + headerH + 6f * dp;
        float controlH = (landscape ? 68f : 78f) * dp;
        drawControlBar(canvas, pad, topY, w - pad * 2f, controlH);

        float bodyTop = topY + controlH + 8f * dp;
        float bodyH = h - bodyTop - pad;

        if (landscape) {
            float scoreW = (w - pad * 3f) * 0.56f;
            float dockW = (w - pad * 3f) - scoreW;
            drawScoreCard(canvas, pad, bodyTop, scoreW, bodyH);
            drawBottomDock(canvas, pad * 2f + scoreW, bodyTop, dockW, bodyH);
        } else {
            float dockH = Math.min(bodyH * 0.44f, 232f * dp);
            float scoreH = bodyH - dockH - 8f * dp;
            drawScoreCard(canvas, pad, bodyTop, w - pad * 2f, scoreH);
            drawBottomDock(canvas, pad, bodyTop + scoreH + 8f * dp, w - pad * 2f, dockH);
        }

        if (showHelp) {
            drawHelpModal(canvas, w, h);
        }
    }

    private void drawHeader(Canvas canvas, float x, float y, float w, float h) {
        // Help button [?]
        float btnSize = Math.min(h - 8f * dp, 36f * dp);
        float cy = y + h * 0.5f;
        rectHelpBtn.set(x, cy - btnSize * 0.5f, x + btnSize, cy + btnSize * 0.5f);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF1E293B);
        canvas.drawOval(rectHelpBtn, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f * dp);
        paint.setColor(COL_CYAN);
        canvas.drawOval(rectHelpBtn, paint);

        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(16f * dp);
        textPaint.setColor(COL_CYAN);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("?", rectHelpBtn.centerX(), rectHelpBtn.centerY() + 5.5f * dp, textPaint);

        // Right buttons: Loop + Play/Pause
        float playW = 86f * dp;
        float loopW = 64f * dp;
        float btnH = Math.min(h - 6f * dp, 38f * dp);
        rectPlayBtn.set(x + w - playW, cy - btnH * 0.5f, x + w, cy + btnH * 0.5f);
        rectLoopBtn.set(rectPlayBtn.left - 6f * dp - loopW, cy - btnH * 0.5f, rectPlayBtn.left - 6f * dp, cy + btnH * 0.5f);

        // Loop pill
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(loopPlayback ? 0xFF0E7490 : 0xFF1E293B);
        canvas.drawRoundRect(rectLoopBtn, 10f * dp, 10f * dp, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.2f * dp);
        paint.setColor(loopPlayback ? COL_CYAN : COL_CARD_BORDER);
        canvas.drawRoundRect(rectLoopBtn, 10f * dp, 10f * dp, paint);

        textPaint.setTextSize(11.5f * dp);
        textPaint.setColor(loopPlayback ? COL_TEXT_MAIN : COL_TEXT_SUB);
        AppI18n.drawTextFit(canvas, "Loop", rectLoopBtn.centerX(), rectLoopBtn.centerY() + 4f * dp, loopW - 10f * dp, textPaint);

        // Play / Pause pill
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(isPlaying ? COL_CRIMSON : COL_EMERALD_DARK);
        canvas.drawRoundRect(rectPlayBtn, 10f * dp, 10f * dp, paint);
        textPaint.setTextSize(12.5f * dp);
        textPaint.setColor(0xFFFFFFFF);
        String playLabel = isPlaying ? AppI18n.tr("Pause") : AppI18n.tr("Play");
        AppI18n.drawTextFit(canvas, (isPlaying ? "■ " : "▶ ") + playLabel, rectPlayBtn.centerX(), rectPlayBtn.centerY() + 4.5f * dp, playW - 10f * dp, textPaint);

        // Title + Subtitle
        float titleLeft = rectHelpBtn.right + 10f * dp;
        float titleMaxW = rectLoopBtn.left - titleLeft - 8f * dp;
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setColor(COL_TEXT_MAIN);
        textPaint.setTextSize(16.5f * dp);
        AppI18n.drawTextFit(canvas, "Rhythm Trainer", titleLeft, y + h * 0.45f, titleMaxW, textPaint);

        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setColor(COL_CYAN);
        textPaint.setTextSize(10.5f * dp);
        AppI18n.drawTextFit(canvas, "Single-Frequency Musical Rhythm Practice (440 Hz)", titleLeft, y + h * 0.82f, titleMaxW, textPaint);
    }

    private void drawControlBar(Canvas canvas, float x, float y, float w, float h) {
        RectF card = new RectF(x, y, x + w, y + h);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COL_CARD_DARK);
        canvas.drawRoundRect(card, 12f * dp, 12f * dp, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1f * dp);
        paint.setColor(COL_CARD_BORDER);
        canvas.drawRoundRect(card, 12f * dp, 12f * dp, paint);

        float inPad = 8f * dp;
        float rowH = (h - inPad * 2.6f) * 0.5f;
        float row1Y = y + inPad * 0.8f;
        float row2Y = row1Y + rowH + inPad * 1.0f;

        // Row 1: Time Signature pills (4/4, 3/4, 2/2, 5/4, 7/4) + Edit + Randomize
        TimeSignature[] sigs = TimeSignature.values();
        float sigAreaW = (w - inPad * 2f) * 0.58f;
        float sigGap = 4f * dp;
        float sigW = (sigAreaW - sigGap * (sigs.length - 1)) / sigs.length;

        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < sigs.length; i++) {
            float sx = x + inPad + i * (sigW + sigGap);
            rectTimeSigs[i].set(sx, row1Y, sx + sigW, row1Y + rowH);
            boolean active = sigs[i] == timeSignature;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(active ? COL_PRIMARY : 0xFF0F172A);
            canvas.drawRoundRect(rectTimeSigs[i], 8f * dp, 8f * dp, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(active ? 1.5f * dp : 1f * dp);
            paint.setColor(active ? COL_CYAN : COL_CARD_BORDER);
            canvas.drawRoundRect(rectTimeSigs[i], 8f * dp, 8f * dp, paint);

            textPaint.setTextSize(12f * dp);
            textPaint.setColor(active ? 0xFFFFFFFF : COL_TEXT_SUB);
            canvas.drawText(sigs[i].label, rectTimeSigs[i].centerX(), rectTimeSigs[i].centerY() + 4.2f * dp, textPaint);
        }

        // Right side of Row 1: Edit Mode toggle + Randomize
        float rightStart = x + inPad + sigAreaW + 6f * dp;
        float rightAvail = (x + w - inPad) - rightStart;
        float editW = rightAvail * 0.52f - 3f * dp;
        float randW = rightAvail - editW - 6f * dp;

        rectEditToggle.set(rightStart, row1Y, rightStart + editW, row1Y + rowH);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(isEditMode ? COL_AMBER : 0xFF1E293B);
        canvas.drawRoundRect(rectEditToggle, 8f * dp, 8f * dp, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(isEditMode ? 0xFFFDE68A : COL_CARD_BORDER);
        canvas.drawRoundRect(rectEditToggle, 8f * dp, 8f * dp, paint);
        textPaint.setTextSize(11f * dp);
        textPaint.setColor(isEditMode ? 0xFF0F172A : COL_TEXT_MAIN);
        AppI18n.drawTextFit(canvas, isEditMode ? "Done Editing" : "Edit", rectEditToggle.centerX(), rectEditToggle.centerY() + 4f * dp, editW - 8f * dp, textPaint);

        rectRandomBtn.set(rectEditToggle.right + 6f * dp, row1Y, rectEditToggle.right + 6f * dp + randW, row1Y + rowH);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF1E293B);
        canvas.drawRoundRect(rectRandomBtn, 8f * dp, 8f * dp, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(COL_CARD_BORDER);
        canvas.drawRoundRect(rectRandomBtn, 8f * dp, 8f * dp, paint);
        textPaint.setColor(COL_CYAN);
        AppI18n.drawTextFit(canvas, "Randomize", rectRandomBtn.centerX(), rectRandomBtn.centerY() + 4f * dp, randW - 8f * dp, textPaint);

        // Row 2: Tempo label + [-5] + interactive slider + [+5]
        float tempoLabelW = 104f * dp;
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(11.5f * dp);
        textPaint.setColor(COL_TEXT_MAIN);
        AppI18n.drawTextFit(canvas, "Tempo: " + bpm + " BPM", x + inPad, row2Y + rowH * 0.68f, tempoLabelW, textPaint);

        float stepW = 34f * dp;
        rectBpmMinus.set(x + inPad + tempoLabelW + 4f * dp, row2Y, x + inPad + tempoLabelW + 4f * dp + stepW, row2Y + rowH);
        rectBpmPlus.set(x + w - inPad - stepW, row2Y, x + w - inPad, row2Y + rowH);
        rectBpmSlider.set(rectBpmMinus.right + 8f * dp, row2Y, rectBpmPlus.left - 8f * dp, row2Y + rowH);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF0F172A);
        canvas.drawRoundRect(rectBpmMinus, 7f * dp, 7f * dp, paint);
        canvas.drawRoundRect(rectBpmPlus, 7f * dp, 7f * dp, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(COL_CARD_BORDER);
        canvas.drawRoundRect(rectBpmMinus, 7f * dp, 7f * dp, paint);
        canvas.drawRoundRect(rectBpmPlus, 7f * dp, 7f * dp, paint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(11f * dp);
        textPaint.setColor(COL_TEXT_MAIN);
        canvas.drawText("-5", rectBpmMinus.centerX(), rectBpmMinus.centerY() + 4f * dp, textPaint);
        canvas.drawText("+5", rectBpmPlus.centerX(), rectBpmPlus.centerY() + 4f * dp, textPaint);

        // Slider track
        float trackY = rectBpmSlider.centerY();
        float frac = (bpm - 40f) / 160f;
        if (frac < 0f) frac = 0f;
        if (frac > 1f) frac = 1f;
        float thumbX = rectBpmSlider.left + frac * rectBpmSlider.width();

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f * dp);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(0xFF334155);
        canvas.drawLine(rectBpmSlider.left, trackY, rectBpmSlider.right, trackY, paint);
        paint.setColor(COL_CYAN);
        canvas.drawLine(rectBpmSlider.left, trackY, thumbX, trackY, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COL_CYAN);
        canvas.drawCircle(thumbX, trackY, 7.5f * dp, paint);
        paint.setColor(0xFFFFFFFF);
        canvas.drawCircle(thumbX, trackY, 3.2f * dp, paint);
    }

    private void drawScoreCard(Canvas canvas, float x, float y, float w, float h) {
        cellHits.clear();
        RectF card = new RectF(x, y, x + w, y + h);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COL_SCORE_BG);
        canvas.drawRoundRect(card, 14f * dp, 14f * dp, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f * dp);
        paint.setColor(COL_SCORE_BORDER);
        canvas.drawRoundRect(card, 14f * dp, 14f * dp, paint);

        float pad = 10f * dp;
        float curY = y + pad;
        float availH = h - pad * 2f;

        // Optional Countdown Banner when counting in 4..3..2..1
        if (countdownValue != null) {
            float bannerH = Math.min(44f * dp, availH * 0.24f);
            RectF banner = new RectF(x + pad, curY, x + w - pad, curY + bannerH);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFFFEF3C7);
            canvas.drawRoundRect(banner, 10f * dp, 10f * dp, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.2f * dp);
            paint.setColor(COL_AMBER);
            canvas.drawRoundRect(banner, 10f * dp, 10f * dp, paint);

            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setTextSize(12.5f * dp);
            textPaint.setColor(COL_AMBER_DARK);
            AppI18n.drawTextFit(canvas, "Count-In Clicks", banner.left + 10f * dp, banner.centerY() + 4.5f * dp, banner.width() * 0.45f, textPaint);

            float orbR = Math.min(13f * dp, bannerH * 0.36f);
            float orbSpacing = orbR * 2.4f;
            float orbsRight = banner.right - 12f * dp - orbR;
            for (int i = 0; i < 4; i++) {
                int num = i + 1; // 4 on left, 1 on right
                float cx = orbsRight - (num - 1) * orbSpacing;
                boolean active = countdownValue.intValue() == num;
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(active ? COL_AMBER : 0xFFFDE68A);
                canvas.drawCircle(cx, banner.centerY(), orbR, paint);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(active ? 2f * dp : 1f * dp);
                paint.setColor(COL_AMBER_DARK);
                canvas.drawCircle(cx, banner.centerY(), orbR, paint);

                textPaint.setTextAlign(Paint.Align.CENTER);
                textPaint.setTextSize(12f * dp);
                textPaint.setColor(active ? 0xFFFFFFFF : COL_AMBER_DARK);
                canvas.drawText(String.valueOf(num), cx, banner.centerY() + 4f * dp, textPaint);
            }

            curY += bannerH + 6f * dp;
            availH -= (bannerH + 6f * dp);
        }

        int barCount = Math.max(1, measures.size());
        float gap = 6f * dp;
        float rowH = (availH - gap * (barCount - 1)) / barCount;

        for (int mIdx = 0; mIdx < measures.size(); mIdx++) {
            Measure measure = measures.get(mIdx);
            float ry = curY + mIdx * (rowH + gap);
            RectF rowRect = new RectF(x + pad, ry, x + w - pad, ry + rowH);
            drawMeasureRow(canvas, mIdx, measure, mIdx == 0, mIdx == measures.size() - 1, rowRect);
        }
    }

    private void drawMeasureRow(
            Canvas canvas,
            int measureIndex,
            Measure measure,
            boolean showTimeSig,
            boolean isLastMeasure,
            RectF rowRect) {
        boolean barPlaying = isPlaying
                && countdownValue == null
                && activePlayingPosition != null
                && activePlayingPosition.measureIndex == measureIndex;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(barPlaying ? 0xFFECFDF5 : COL_SCORE_ROW_BG);
        canvas.drawRoundRect(rowRect, 10f * dp, 10f * dp, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(barPlaying ? 1.8f * dp : 1f * dp);
        paint.setColor(barPlaying ? COL_EMERALD_DARK : COL_SCORE_BORDER);
        canvas.drawRoundRect(rowRect, 10f * dp, 10f * dp, paint);

        float headerH = Math.min(20f * dp, rowRect.height() * 0.22f);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(11f * dp);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setColor(barPlaying ? COL_EMERALD_DARK : COL_PRIMARY);
        AppI18n.drawText(canvas, "Bar " + (measureIndex + 1), rowRect.left + 8f * dp, rowRect.top + headerH * 0.78f, textPaint);

        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setTextSize(10f * dp);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        textPaint.setColor(COL_INK_MUTED);
        String beatsStr = (measure.totalSixteenthUnits / 4.0) + " quarter beats (" + timeSignature.label + ")";
        AppI18n.drawTextFit(canvas, beatsStr, rowRect.right - 8f * dp, rowRect.top + headerH * 0.78f, rowRect.width() * 0.65f, textPaint);

        float scoreTop = rowRect.top + headerH + 2f * dp;
        float scoreBot = rowRect.bottom - 4f * dp;
        float scoreMidY = (scoreTop + scoreBot) * 0.5f;

        // Horizontal rhythm reference line (not a 5-line staff)
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.2f * dp);
        paint.setColor(0x5594A3B8);
        canvas.drawLine(rowRect.left + 6f * dp, scoreMidY, rowRect.right - 6f * dp, scoreMidY, paint);

        // Left opening barline
        float barTop = scoreTop + (scoreBot - scoreTop) * 0.10f;
        float barBot = scoreBot - (scoreBot - scoreTop) * 0.10f;
        float leftBarX = rowRect.left + 10f * dp;
        paint.setStrokeWidth(2.6f * dp);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(COL_INK);
        canvas.drawLine(leftBarX, barTop, leftBarX, barBot, paint);

        float contentLeft = leftBarX + 6f * dp;
        if (showTimeSig) {
            float sigW = 24f * dp;
            float sigCx = contentLeft + sigW * 0.5f;
            float sigH = barBot - barTop;
            serifPaint.setColor(COL_INK);
            serifPaint.setTextAlign(Paint.Align.CENTER);
            serifPaint.setTextSize(Math.min(22f * dp, sigH * 0.48f));
            canvas.drawText(String.valueOf(timeSignature.numerator), sigCx, scoreMidY - 2f * dp, serifPaint);
            canvas.drawText(String.valueOf(timeSignature.denominator), sigCx, scoreMidY + serifPaint.getTextSize() * 0.88f, serifPaint);
            contentLeft += sigW + 4f * dp;
        }

        // Right closing barline (double barline on final measure)
        float rightEdge = rowRect.right - 10f * dp;
        float contentRight;
        if (isLastMeasure) {
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStrokeWidth(4.2f * dp);
            canvas.drawLine(rightEdge, barTop, rightEdge, barBot, paint);
            paint.setStrokeWidth(1.8f * dp);
            canvas.drawLine(rightEdge - 5f * dp, barTop, rightEdge - 5f * dp, barBot, paint);
            contentRight = rightEdge - 11f * dp;
        } else {
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(2.6f * dp);
            canvas.drawLine(rightEdge, barTop, rightEdge, barBot, paint);
            contentRight = rightEdge - 6f * dp;
        }

        // Proportional cells for each RhythmElement in this measure
        float totalWeight = 0f;
        List<RhythmElement> elems = measure.elements;
        float[] weights = new float[elems.size()];
        for (int i = 0; i < elems.size(); i++) {
            float w = 1f;
            switch (elems.get(i).duration) {
                case WHOLE: w = 4.0f; break;
                case HALF: w = 2.6f; break;
                case QUARTER: w = 1.8f; break;
                case EIGHTH: w = 1.3f; break;
                case SIXTEENTH: w = 1.0f; break;
            }
            weights[i] = w;
            totalWeight += w;
        }

        float cellGap = 4f * dp;
        float availW = Math.max(20f, (contentRight - contentLeft) - cellGap * Math.max(0, elems.size() - 1));
        float curX = contentLeft;

        for (int eIdx = 0; eIdx < elems.size(); eIdx++) {
            RhythmElement el = elems.get(eIdx);
            float cellW = (availW * (weights[eIdx] / Math.max(0.01f, totalWeight)));
            RectF cellRect = new RectF(curX, scoreTop + 2f * dp, curX + cellW, scoreBot - 2f * dp);
            ElementPosition pos = new ElementPosition(measureIndex, eIdx);
            cellHits.add(new CellHit(pos, new RectF(cellRect)));

            boolean isSelected = isEditMode && !isPlaying && selectedPosition.equals(pos);
            int fillCol = 0x00000000;
            int borderCol = 0x00000000;
            int glyphCol = COL_INK;

            if (barPlaying) {
                fillCol = 0x4410B981;
                borderCol = COL_EMERALD_DARK;
                glyphCol = 0xFF065F46;
            } else if (isSelected) {
                fillCol = 0xCCDBEAFE;
                borderCol = COL_PRIMARY;
                glyphCol = 0xFF1E3A8A;
            } else if (isEditMode && !isPlaying) {
                fillCol = 0x55E2E8F0;
                borderCol = 0x6694A3B8;
            }

            if (fillCol != 0) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(fillCol);
                canvas.drawRoundRect(cellRect, 8f * dp, 8f * dp, paint);
            }
            if (borderCol != 0) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth((barPlaying || isSelected) ? 1.8f * dp : 1f * dp);
                paint.setColor(borderCol);
                canvas.drawRoundRect(cellRect, 8f * dp, 8f * dp, paint);
            }

            float badgeH = Math.min(14f * dp, cellRect.height() * 0.24f);
            RectF symbolRect = new RectF(cellRect.left + 2f * dp, cellRect.top + 2f * dp, cellRect.right - 2f * dp, cellRect.bottom - badgeH);
            drawMusicalSymbol(canvas, symbolRect, el.duration, el.isRest, glyphCol);

            textPaint.setTypeface((barPlaying || isSelected) ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            textPaint.setTextSize(Math.min(10.5f * dp, badgeH * 0.82f));
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setColor(glyphCol);
            String fracLabel = el.isRest ? (el.duration.fractionText + " R") : el.duration.fractionText;
            AppI18n.drawTextFit(canvas, fracLabel, cellRect.centerX(), cellRect.bottom - 2.5f * dp, cellRect.width() - 4f * dp, textPaint);

            curX += cellW + cellGap;
        }
    }

    private void drawBottomDock(Canvas canvas, float x, float y, float w, float h) {
        RectF card = new RectF(x, y, x + w, y + h);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COL_CARD_DARK);
        canvas.drawRoundRect(card, 14f * dp, 14f * dp, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(isEditMode ? 1.6f * dp : 1f * dp);
        paint.setColor(isEditMode ? COL_AMBER : COL_CARD_BORDER);
        canvas.drawRoundRect(card, 14f * dp, 14f * dp, paint);

        float pad = 8f * dp;
        float curY = y + pad;

        // Header line inside dock
        RhythmElement sel = getSelectedElement();
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(11.5f * dp);
        textPaint.setColor(isEditMode ? COL_AMBER : COL_CYAN);

        String titleText;
        if (isEditMode && sel != null) {
            titleText = AppI18n.tr("Edit Mode") + " — "
                    + AppI18n.tr("Bar " + (selectedPosition.measureIndex + 1) + ": " + sel.duration.durationLabel(sel.isRest));
        } else {
            titleText = AppI18n.tr("Rhythm Note Durations (" + timeSignature.label + ")");
        }
        AppI18n.drawTextFit(canvas, titleText, x + pad, curY + 11f * dp, w - pad * 2f, textPaint);
        curY += 15f * dp;

        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setTextSize(9.8f * dp);
        textPaint.setColor(COL_TEXT_SUB);
        String hintText = AppI18n.tr("Ready to play. Press Play for a 4..3..2..1 click count-in followed by the rhythm.");
        AppI18n.drawTextFit(canvas, hintText, x + pad, curY + 10f * dp, w - pad * 2f, textPaint);
        curY += 15f * dp;

        float availH = (y + h - pad) - curY;
        float actionRowH = Math.min(32f * dp, availH * 0.22f);
        float tilesAreaH = availH - actionRowH - 6f * dp;
        float tileRowH = (tilesAreaH - 5f * dp) * 0.5f;

        NoteDuration[] durations = NoteDuration.values();
        int maxUnits = timeSignature.sixteenthUnitsPerBar;
        float labelColW = 42f * dp;
        float tilesLeft = x + pad + labelColW;
        float tilesW = (x + w - pad) - tilesLeft;
        float tileGap = 5f * dp;
        float tileW = (tilesW - tileGap * (durations.length - 1)) / durations.length;

        // Row 1: NOTES
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(9.5f * dp);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setColor(COL_CYAN);
        AppI18n.drawTextFit(canvas, "NOTES", x + pad, curY + tileRowH * 0.56f, labelColW - 4f * dp, textPaint);

        for (int i = 0; i < durations.length; i++) {
            NoteDuration d = durations[i];
            float tx = tilesLeft + i * (tileW + tileGap);
            rectNoteTiles[i].set(tx, curY, tx + tileW, curY + tileRowH);
            boolean enabled = d.sixteenthUnits <= maxUnits;
            boolean active = isEditMode && !isPlaying && sel != null && sel.duration == d && !sel.isRest;
            drawDurationTile(canvas, rectNoteTiles[i], d, false, active, enabled);
        }

        curY += tileRowH + 5f * dp;

        // Row 2: RESTS
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(9.5f * dp);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setColor(COL_AMBER);
        AppI18n.drawTextFit(canvas, "RESTS", x + pad, curY + tileRowH * 0.56f, labelColW - 4f * dp, textPaint);

        for (int i = 0; i < durations.length; i++) {
            NoteDuration d = durations[i];
            float tx = tilesLeft + i * (tileW + tileGap);
            rectRestTiles[i].set(tx, curY, tx + tileW, curY + tileRowH);
            boolean enabled = d.sixteenthUnits <= maxUnits;
            boolean active = isEditMode && !isPlaying && sel != null && sel.duration == d && sel.isRest;
            drawDurationTile(canvas, rectRestTiles[i], d, true, active, enabled);
        }

        curY += tileRowH + 6f * dp;

        // Row 3: Bar Management & Preset Buttons
        float btnGap = 5f * dp;
        float btnW = (w - pad * 2f - btnGap * 4f) / 5f;
        RectF[] btns = new RectF[] { rectAddBar, rectRemoveBar, rectRandomDock, rectQuarterReset, rectDoneEditDock };
        String[] labels = new String[] {
                "Add Bar",
                "Remove Bar",
                "Randomize",
                "Quarter Notes",
                isEditMode ? "Exit Edit Mode" : "Edit Mode"
        };
        boolean[] enabledArr = new boolean[] {
                measures.size() < MAX_BARS,
                measures.size() > 1,
                true,
                true,
                true
        };

        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(10f * dp);

        for (int i = 0; i < btns.length; i++) {
            float bx = x + pad + i * (btnW + btnGap);
            btns[i].set(bx, curY, bx + btnW, curY + actionRowH);
            boolean en = enabledArr[i];
            boolean highlight = (i == 4 && isEditMode);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(!en ? 0xFF0F172A : (highlight ? COL_PRIMARY : 0xFF1E293B));
            canvas.drawRoundRect(btns[i], 7f * dp, 7f * dp, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1f * dp);
            paint.setColor(!en ? 0xFF1E293B : (highlight ? COL_CYAN : COL_CARD_BORDER));
            canvas.drawRoundRect(btns[i], 7f * dp, 7f * dp, paint);

            textPaint.setColor(!en ? 0xFF475569 : (highlight ? 0xFFFFFFFF : COL_TEXT_MAIN));
            AppI18n.drawTextFit(canvas, labels[i], btns[i].centerX(), btns[i].centerY() + 3.6f * dp, btnW - 6f * dp, textPaint);
        }
    }

    private void drawDurationTile(
            Canvas canvas,
            RectF rect,
            NoteDuration duration,
            boolean isRest,
            boolean isSelected,
            boolean enabled) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(!enabled ? 0xFF0F172A : (isSelected ? 0xFF1E3A8A : 0xFF0F172A));
        canvas.drawRoundRect(rect, 8f * dp, 8f * dp, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(isSelected ? 1.8f * dp : 1f * dp);
        paint.setColor(!enabled ? 0xFF1E293B : (isSelected ? COL_CYAN : COL_CARD_BORDER));
        canvas.drawRoundRect(rect, 8f * dp, 8f * dp, paint);

        int fg = !enabled ? 0xFF475569 : (isSelected ? 0xFFFFFFFF : COL_TEXT_MAIN);
        float labelH = Math.min(14f * dp, rect.height() * 0.28f);
        RectF iconRect = new RectF(rect.left + 2f * dp, rect.top + 2f * dp, rect.right - 2f * dp, rect.bottom - labelH);
        drawMusicalSymbol(canvas, iconRect, duration, isRest, fg);

        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(Math.min(9.5f * dp, labelH * 0.78f));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(fg);
        String title = AppI18n.tr(duration.displayName) + " " + duration.fractionText;
        AppI18n.drawTextFit(canvas, title, rect.centerX(), rect.bottom - 3f * dp, rect.width() - 4f * dp, textPaint);
    }

    private void drawMusicalSymbol(Canvas canvas, RectF box, NoteDuration duration, boolean isRest, int color) {
        if (isRest) {
            drawRestSymbol(canvas, box, duration, color);
        } else {
            drawNoteSymbol(canvas, box, duration, color);
        }
    }

    private void drawNoteSymbol(Canvas canvas, RectF box, NoteDuration duration, int color) {
        float w = box.width();
        float h = box.height();
        float minDim = Math.min(w, h);
        float cx = box.left + w * 0.44f;
        float headRx = Math.max(5.0f * dp, Math.min(12.5f * dp, minDim * 0.21f));
        float headRy = headRx * 0.72f;
        float stemStroke = Math.max(1.8f * dp, headRx * 0.22f);

        paint.setColor(color);
        if (duration == NoteDuration.WHOLE) {
            float headCy = box.centerY();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(stemStroke * 1.4f);
            RectF oval = new RectF(cx - headRx * 1.15f, headCy - headRy, cx + headRx * 1.15f, headCy + headRy);
            canvas.drawOval(oval, paint);
            return;
        }

        float stemLen = Math.min(h * 0.54f, headRx * 4.6f);
        float headCy = box.centerY() + stemLen * 0.42f;

        // Tilted hollow (HALF) or filled (QUARTER, EIGHTH, SIXTEENTH) notehead
        int save = canvas.save();
        canvas.rotate(-22f, cx, headCy);
        RectF oval = new RectF(cx - headRx, headCy - headRy, cx + headRx, headCy + headRy);
        if (duration == NoteDuration.HALF) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(stemStroke * 1.35f);
            canvas.drawOval(oval, paint);
        } else {
            paint.setStyle(Paint.Style.FILL);
            canvas.drawOval(oval, paint);
        }
        canvas.restoreToCount(save);

        // Stem
        float stemX = cx + headRx * 0.86f;
        float stemBotY = headCy - headRy * 0.12f;
        float stemTopY = stemBotY - stemLen;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stemStroke);
        paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(stemX, stemBotY, stemX, stemTopY, paint);

        float flagW = headRx * 1.45f;
        float flagDrop = Math.min(stemLen * 0.46f, headRx * 2.2f);
        if (duration == NoteDuration.EIGHTH || duration == NoteDuration.SIXTEENTH) {
            drawNoteFlag(canvas, stemX, stemTopY, flagW, flagDrop, stemStroke * 1.15f, color);
        }
        if (duration == NoteDuration.SIXTEENTH) {
            float offset = Math.min(stemLen * 0.26f, headRx * 1.25f);
            drawNoteFlag(canvas, stemX, stemTopY + offset, flagW, flagDrop, stemStroke * 1.15f, color);
        }
    }

    private void drawNoteFlag(Canvas canvas, float stemX, float flagTopY, float flagW, float flagDrop, float strokeW, int color) {
        path.reset();
        path.moveTo(stemX, flagTopY);
        path.cubicTo(
                stemX + flagW * 0.45f, flagTopY + flagDrop * 0.18f,
                stemX + flagW * 1.15f, flagTopY + flagDrop * 0.48f,
                stemX + flagW * 0.72f, flagTopY + flagDrop
        );
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeW);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setColor(color);
        canvas.drawPath(path, paint);
    }

    private void drawRestSymbol(Canvas canvas, RectF box, NoteDuration duration, int color) {
        float cx = box.centerX();
        float cy = box.top + box.height() * 0.52f;
        float unit = Math.min(box.width(), box.height()) * 0.15f;
        paint.setColor(color);

        switch (duration) {
            case WHOLE: {
                float lineY = cy - unit * 0.45f;
                float blockW = unit * 2.2f;
                float blockH = unit * 0.95f;
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(Math.max(1.8f * dp, unit * 0.22f));
                paint.setStrokeCap(Paint.Cap.ROUND);
                canvas.drawLine(cx - blockW * 0.85f, lineY, cx + blockW * 0.85f, lineY, paint);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRoundRect(new RectF(cx - blockW * 0.5f, lineY, cx + blockW * 0.5f, lineY + blockH), 2f * dp, 2f * dp, paint);
                break;
            }
            case HALF: {
                float lineY = cy + unit * 0.45f;
                float blockW = unit * 2.2f;
                float blockH = unit * 0.95f;
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(Math.max(1.8f * dp, unit * 0.22f));
                paint.setStrokeCap(Paint.Cap.ROUND);
                canvas.drawLine(cx - blockW * 0.85f, lineY, cx + blockW * 0.85f, lineY, paint);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRoundRect(new RectF(cx - blockW * 0.5f, lineY - blockH, cx + blockW * 0.5f, lineY), 2f * dp, 2f * dp, paint);
                break;
            }
            case QUARTER: {
                float stroke = Math.max(2f * dp, unit * 0.34f);
                path.reset();
                path.moveTo(cx - unit * 0.25f, cy - unit * 1.65f);
                path.lineTo(cx + unit * 0.55f, cy - unit * 0.65f);
                path.lineTo(cx - unit * 0.45f, cy + unit * 0.15f);
                path.lineTo(cx + unit * 0.45f, cy + unit * 0.95f);
                path.cubicTo(
                        cx - unit * 0.55f, cy + unit * 0.85f,
                        cx - unit * 0.70f, cy + unit * 1.65f,
                        cx + unit * 0.10f, cy + unit * 1.85f
                );
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(stroke);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setStrokeJoin(Paint.Join.ROUND);
                canvas.drawPath(path, paint);
                break;
            }
            case EIGHTH: {
                float stroke = Math.max(1.8f * dp, unit * 0.28f);
                float topX = cx + unit * 0.45f;
                float topY = cy - unit * 1.15f;
                float botX = cx - unit * 0.35f;
                float botY = cy + unit * 1.35f;
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(stroke);
                paint.setStrokeCap(Paint.Cap.ROUND);
                canvas.drawLine(topX, topY, botX, botY, paint);
                drawRestHook(canvas, topX - unit * 0.10f, topY + unit * 0.25f, unit, stroke, color);
                break;
            }
            case SIXTEENTH: {
                float stroke = Math.max(1.8f * dp, unit * 0.28f);
                float topX = cx + unit * 0.55f;
                float topY = cy - unit * 1.45f;
                float botX = cx - unit * 0.45f;
                float botY = cy + unit * 1.65f;
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(stroke);
                paint.setStrokeCap(Paint.Cap.ROUND);
                canvas.drawLine(topX, topY, botX, botY, paint);
                drawRestHook(canvas, topX - unit * 0.10f, topY + unit * 0.25f, unit, stroke, color);
                drawRestHook(canvas, topX - unit * 0.42f, topY + unit * 1.20f, unit, stroke, color);
                break;
            }
        }
    }

    private void drawRestHook(Canvas canvas, float attachX, float attachY, float unit, float stroke, int color) {
        float bx = attachX - unit * 0.65f;
        float by = attachY - unit * 0.10f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawCircle(bx, by, unit * 0.30f, paint);

        path.reset();
        path.moveTo(bx, by + unit * 0.15f);
        path.quadTo(bx + unit * 0.35f, attachY + unit * 0.25f, attachX, attachY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawPath(path, paint);
    }

    private void drawHelpModal(Canvas canvas, int w, int h) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xD8050811);
        canvas.drawRect(0, 0, w, h, paint);

        float mw = Math.min(w * 0.92f, 540f * dp);
        float mh = Math.min(h * 0.88f, 580f * dp);
        rectHelpModal.set((w - mw) * 0.5f, (h - mh) * 0.5f, (w + mw) * 0.5f, (h + mh) * 0.5f);

        paint.setColor(0xFF0F172A);
        canvas.drawRoundRect(rectHelpModal, 18f * dp, 18f * dp, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f * dp);
        paint.setColor(COL_CYAN);
        canvas.drawRoundRect(rectHelpModal, 18f * dp, 18f * dp, paint);

        float pad = 16f * dp;
        float closeW = 86f * dp;
        float closeH = 36f * dp;
        rectHelpClose.set(
                rectHelpModal.right - pad - closeW,
                rectHelpModal.top + pad,
                rectHelpModal.right - pad,
                rectHelpModal.top + pad + closeH
        );
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COL_PRIMARY);
        canvas.drawRoundRect(rectHelpClose, 9f * dp, 9f * dp, paint);

        Context ctx = getContext();
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(13f * dp);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextAlign(Paint.Align.CENTER);
        AppI18n.drawTextFit(canvas, AppI18n.str(ctx, R.string.got_it), rectHelpClose.centerX(), rectHelpClose.centerY() + 4.5f * dp, closeW - 10f * dp, textPaint);

        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(17f * dp);
        textPaint.setColor(COL_CYAN);
        float titleMaxW = rectHelpClose.left - (rectHelpModal.left + pad) - 10f * dp;
        AppI18n.drawTextFit(canvas, AppI18n.str(ctx, R.string.help_title), rectHelpModal.left + pad, rectHelpClose.centerY() + 5.5f * dp, titleMaxW, textPaint);

        RectF contentArea = new RectF(
                rectHelpModal.left + pad,
                rectHelpClose.bottom + 12f * dp,
                rectHelpModal.right - pad,
                rectHelpModal.bottom - pad
        );

        String[] titles = new String[] {
                AppI18n.str(ctx, R.string.help_sec1_title),
                AppI18n.str(ctx, R.string.help_sec2_title),
                AppI18n.str(ctx, R.string.help_sec3_title)
        };
        String[] bodies = new String[] {
                AppI18n.str(ctx, R.string.help_sec1_body),
                AppI18n.str(ctx, R.string.help_sec2_body),
                AppI18n.str(ctx, R.string.help_sec3_body)
        };
        int[] accents = new int[] { COL_CYAN, COL_EMERALD, COL_AMBER };

        helpMaxScrollY = AppI18n.drawScrollableHelpBlock(
                canvas,
                contentArea,
                helpScrollY,
                titles,
                bodies,
                accents,
                14.5f,
                13.0f,
                0xFFE2E8F0,
                0xFF1E293B,
                dp
        );
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int action = event.getActionMasked();

        if (showHelp) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    helpLastTouchY = y;
                    helpDragging = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float dy = helpLastTouchY - y;
                    if (Math.abs(dy) > 4f * dp) {
                        helpDragging = true;
                    }
                    if (helpDragging && helpMaxScrollY > 0f) {
                        helpScrollY = Math.max(0f, Math.min(helpMaxScrollY, helpScrollY + dy));
                        helpLastTouchY = y;
                        invalidate();
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (!helpDragging) {
                        if (rectHelpClose.contains(x, y) || !rectHelpModal.contains(x, y)) {
                            showHelp = false;
                            invalidate();
                        }
                    }
                    helpDragging = false;
                    return true;
            }
            return true;
        }

        if (action == MotionEvent.ACTION_DOWN) {
            if (rectBpmSlider.contains(x, y)) {
                draggingTempo = true;
                updateBpmFromTouch(x);
                return true;
            }
            if (rectHelpBtn.contains(x, y)) {
                showHelp = true;
                helpScrollY = 0f;
                invalidate();
                return true;
            }
            if (rectPlayBtn.contains(x, y)) {
                togglePlayPause();
                return true;
            }
            if (rectLoopBtn.contains(x, y)) {
                loopPlayback = !loopPlayback;
                savePrefs();
                invalidate();
                return true;
            }
            TimeSignature[] sigs = TimeSignature.values();
            for (int i = 0; i < sigs.length; i++) {
                if (rectTimeSigs[i].contains(x, y)) {
                    selectTimeSignature(sigs[i]);
                    return true;
                }
            }
            if (rectEditToggle.contains(x, y) || rectDoneEditDock.contains(x, y)) {
                if (!isEditMode && isPlaying) {
                    stopAudio();
                }
                isEditMode = !isEditMode;
                invalidate();
                return true;
            }
            if (rectRandomBtn.contains(x, y) || rectRandomDock.contains(x, y)) {
                randomizeRhythm();
                return true;
            }
            if (rectBpmMinus.contains(x, y)) {
                bpm = Math.max(40, bpm - 5);
                savePrefs();
                invalidate();
                return true;
            }
            if (rectBpmPlus.contains(x, y)) {
                bpm = Math.min(200, bpm + 5);
                savePrefs();
                invalidate();
                return true;
            }
            for (int i = 0; i < cellHits.size(); i++) {
                CellHit hit = cellHits.get(i);
                if (hit.rect.contains(x, y)) {
                    selectedPosition = hit.pos;
                    if (!isPlaying) {
                        isEditMode = true;
                    }
                    invalidate();
                    return true;
                }
            }
            NoteDuration[] durations = NoteDuration.values();
            int maxUnits = timeSignature.sixteenthUnitsPerBar;
            for (int i = 0; i < durations.length; i++) {
                if (durations[i].sixteenthUnits <= maxUnits) {
                    if (rectNoteTiles[i].contains(x, y)) {
                        assignDuration(durations[i], false);
                        return true;
                    }
                    if (rectRestTiles[i].contains(x, y)) {
                        assignDuration(durations[i], true);
                        return true;
                    }
                }
            }
            if (rectAddBar.contains(x, y)) {
                addMeasure();
                return true;
            }
            if (rectRemoveBar.contains(x, y)) {
                removeLastMeasure();
                return true;
            }
            if (rectQuarterReset.contains(x, y)) {
                resetToQuarterNotes();
                return true;
            }
            return true;
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (draggingTempo) {
                updateBpmFromTouch(x);
                return true;
            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (draggingTempo) {
                draggingTempo = false;
                savePrefs();
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    private void updateBpmFromTouch(float x) {
        float w = Math.max(1f, rectBpmSlider.width());
        float frac = (x - rectBpmSlider.left) / w;
        if (frac < 0f) frac = 0f;
        if (frac > 1f) frac = 1f;
        bpm = Math.round(40f + frac * 160f);
        invalidate();
    }
}
