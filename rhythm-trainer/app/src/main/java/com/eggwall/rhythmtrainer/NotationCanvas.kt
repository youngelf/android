package com.eggwall.rhythmtrainer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Renders the full rhythm score with time signature, bar lines (including final double bar line),
 * and standard musical notation for notes and rests without a 5-line staff or clef.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RhythmScoreView(
    timeSignature: TimeSignature,
    measures: List<Measure>,
    isEditMode: Boolean,
    selectedPosition: ElementPosition,
    activePlayingPosition: ElementPosition?,
    onElementClick: (ElementPosition) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        measures.forEachIndexed { measureIndex, measure ->
            val isLastMeasure = measureIndex == measures.lastIndex
            MeasureBarRow(
                measureIndex = measureIndex,
                measure = measure,
                timeSignature = timeSignature,
                showTimeSignature = measureIndex == 0,
                isLastMeasure = isLastMeasure,
                isEditMode = isEditMode,
                selectedPosition = selectedPosition,
                activePlayingPosition = activePlayingPosition,
                onElementClick = onElementClick
            )
        }
    }
}

@Composable
private fun MeasureBarRow(
    measureIndex: Int,
    measure: Measure,
    timeSignature: TimeSignature,
    showTimeSignature: Boolean,
    isLastMeasure: Boolean,
    isEditMode: Boolean,
    selectedPosition: ElementPosition,
    activePlayingPosition: ElementPosition?,
    onElementClick: (ElementPosition) -> Unit
) {
    val barColor = MaterialTheme.colorScheme.onSurface
    val subtleGuideColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppI18n.tr("Bar ${measureIndex + 1}"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = AppI18n.tr("${measure.totalSixteenthUnits / 4.0} quarter beats (${timeSignature.label})"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp),
                contentAlignment = Alignment.Center
            ) {
                // Subtle horizontal rhythm reference line (not a 5-line staff)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val midY = size.height * 0.50f
                    drawLine(
                        color = subtleGuideColor,
                        start = Offset(0f, midY),
                        end = Offset(size.width, midY),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left opening bar line
                    BarLineCanvas(
                        color = barColor,
                        isDoubleEnd = false,
                        modifier = Modifier
                            .width(8.dp)
                            .height(92.dp)
                    )

                    // Stacked Time Signature at the beginning of Bar 1
                    if (showTimeSignature) {
                        StackedTimeSignatureGlyph(
                            timeSignature = timeSignature,
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .width(28.dp)
                        )
                    }

                    // Notes and rests inside this bar, weighted smoothly by duration
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        measure.elements.forEachIndexed { elemIndex, element ->
                            val pos = ElementPosition(measureIndex, elemIndex)
                            val isSelected = isEditMode && selectedPosition == pos
                            val isPlaying = activePlayingPosition == pos
                            // Visual width weight scales gracefully with musical duration
                            val widthWeight = when (element.duration) {
                                NoteDuration.WHOLE -> 4.0f
                                NoteDuration.HALF -> 2.6f
                                NoteDuration.QUARTER -> 1.8f
                                NoteDuration.EIGHTH -> 1.3f
                                NoteDuration.SIXTEENTH -> 1.0f
                            }

                            RhythmElementCell(
                                element = element,
                                isSelected = isSelected,
                                isPlaying = isPlaying,
                                isEditMode = isEditMode,
                                onClick = { onElementClick(pos) },
                                modifier = Modifier
                                    .weight(widthWeight)
                                    .height(124.dp)
                            )
                        }
                    }

                    // Right closing bar line (double bar line on final measure)
                    BarLineCanvas(
                        color = barColor,
                        isDoubleEnd = isLastMeasure,
                        modifier = Modifier
                            .width(if (isLastMeasure) 14.dp else 8.dp)
                            .height(92.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StackedTimeSignatureGlyph(
    timeSignature: TimeSignature,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = timeSignature.numerator.toString(),
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Serif,
            lineHeight = 24.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = timeSignature.denominator.toString(),
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Serif,
            lineHeight = 24.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BarLineCanvas(
    color: Color,
    isDoubleEnd: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val topY = size.height * 0.08f
        val bottomY = size.height * 0.92f
        if (!isDoubleEnd) {
            val x = size.width * 0.5f
            drawLine(
                color = color,
                start = Offset(x, topY),
                end = Offset(x, bottomY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        } else {
            // Standard final double bar line: thin inner line + thick outer line
            val thinX = size.width * 0.25f
            val thickX = size.width * 0.75f
            drawLine(
                color = color,
                start = Offset(thinX, topY),
                end = Offset(thinX, bottomY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Butt
            )
            drawLine(
                color = color,
                start = Offset(thickX, topY),
                end = Offset(thickX, bottomY),
                strokeWidth = 5.dp.toPx(),
                cap = StrokeCap.Butt
            )
        }
    }
}

@Composable
private fun RhythmElementCell(
    element: RhythmElement,
    isSelected: Boolean,
    isPlaying: Boolean,
    isEditMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isPlaying -> Color(0xFF10B981).copy(alpha = 0.24f)
        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.70f)
        isEditMode -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        else -> Color.Transparent
    }
    val borderColor = when {
        isPlaying -> Color(0xFF059669)
        isSelected -> MaterialTheme.colorScheme.primary
        isEditMode -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        else -> Color.Transparent
    }
    val symbolColor = when {
        isPlaying -> Color(0xFF065F46)
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(
                width = if (isPlaying || isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Musical symbol canvas
        MusicalSymbolIcon(
            duration = element.duration,
            isRest = element.isRest,
            tint = symbolColor,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        // Duration badge (shown clearly in both View/Play Mode and Edit Mode)
        Text(
            text = if (element.isRest) "${element.duration.fractionText} R" else element.duration.fractionText,
            fontSize = 11.sp,
            fontWeight = if (isPlaying || isSelected) FontWeight.Bold else FontWeight.Medium,
            color = symbolColor,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/**
 * Draws a single musical note or rest symbol for [duration] and [isRest] using standard musical notation.
 */
@Composable
fun MusicalSymbolIcon(
    duration: NoteDuration,
    isRest: Boolean,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    symbolSize: Dp = 48.dp,
    modifier: Modifier = Modifier.size(symbolSize)
) {
    Canvas(modifier = modifier) {
        if (isRest) {
            drawRestSymbol(duration = duration, color = tint)
        } else {
            drawNoteSymbol(duration = duration, color = tint)
        }
    }
}

private fun DrawScope.drawNoteSymbol(
    duration: NoteDuration,
    color: Color
) {
    val cx = size.width * 0.46f
    val headCenterY = if (duration == NoteDuration.WHOLE) size.height * 0.54f else size.height * 0.68f
    val headRx = (size.minDimension * 0.16f).coerceIn(10f, 22f)
    val headRy = headRx * 0.72f
    val stemStroke = (headRx * 0.20f).coerceAtLeast(2.5f)

    // 1. Draw Notehead
    when (duration) {
        NoteDuration.WHOLE -> {
            // Whole note: hollow oval with thicker top/bottom walls, no stem
            drawOval(
                color = color,
                topLeft = Offset(cx - headRx * 1.15f, headCenterY - headRy),
                size = Size(headRx * 2.3f, headRy * 2.0f),
                style = Stroke(width = stemStroke * 1.45f)
            )
        }

        NoteDuration.HALF -> {
            // Half note: tilted hollow oval notehead + stem
            withTransform({
                rotate(degrees = -22f, pivot = Offset(cx, headCenterY))
            }) {
                drawOval(
                    color = color,
                    topLeft = Offset(cx - headRx, headCenterY - headRy),
                    size = Size(headRx * 2f, headRy * 2f),
                    style = Stroke(width = stemStroke * 1.35f)
                )
            }
        }

        NoteDuration.QUARTER,
        NoteDuration.EIGHTH,
        NoteDuration.SIXTEENTH -> {
            // Filled tilted oval notehead
            withTransform({
                rotate(degrees = -22f, pivot = Offset(cx, headCenterY))
            }) {
                drawOval(
                    color = color,
                    topLeft = Offset(cx - headRx, headCenterY - headRy),
                    size = Size(headRx * 2f, headRy * 2f),
                    style = Fill
                )
            }
        }
    }

    // 2. Draw Stem (for Half, Quarter, Eighth, Sixteenth notes)
    if (duration != NoteDuration.WHOLE) {
        val stemX = cx + headRx * 0.88f
        val stemBottomY = headCenterY - headRy * 0.15f
        val stemTopY = size.height * 0.16f

        drawLine(
            color = color,
            start = Offset(stemX, stemBottomY),
            end = Offset(stemX, stemTopY),
            strokeWidth = stemStroke,
            cap = StrokeCap.Round
        )

        // 3. Draw Flags for Eighth (1 flag) and Sixteenth (2 flags)
        if (duration == NoteDuration.EIGHTH || duration == NoteDuration.SIXTEENTH) {
            drawNoteFlag(
                stemX = stemX,
                flagTopY = stemTopY,
                flagWidth = headRx * 1.45f,
                flagDrop = (stemBottomY - stemTopY) * 0.42f,
                strokeWidth = stemStroke * 1.2f,
                color = color
            )
        }
        if (duration == NoteDuration.SIXTEENTH) {
            val secondFlagOffset = (stemBottomY - stemTopY) * 0.24f
            drawNoteFlag(
                stemX = stemX,
                flagTopY = stemTopY + secondFlagOffset,
                flagWidth = headRx * 1.45f,
                flagDrop = (stemBottomY - stemTopY) * 0.40f,
                strokeWidth = stemStroke * 1.2f,
                color = color
            )
        }
    }
}

private fun DrawScope.drawNoteFlag(
    stemX: Float,
    flagTopY: Float,
    flagWidth: Float,
    flagDrop: Float,
    strokeWidth: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(stemX, flagTopY)
        cubicTo(
            stemX + flagWidth * 0.45f, flagTopY + flagDrop * 0.18f,
            stemX + flagWidth * 1.15f, flagTopY + flagDrop * 0.48f,
            stemX + flagWidth * 0.72f, flagTopY + flagDrop
        )
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

private fun DrawScope.drawRestSymbol(
    duration: NoteDuration,
    color: Color
) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.52f
    val unit = size.minDimension * 0.15f

    when (duration) {
        NoteDuration.WHOLE -> {
            // Whole rest: horizontal reference line with filled block HANGING BELOW it
            val lineY = cy - unit * 0.45f
            val blockWidth = unit * 2.2f
            val blockHeight = unit * 0.95f
            drawLine(
                color = color,
                start = Offset(cx - blockWidth * 0.85f, lineY),
                end = Offset(cx + blockWidth * 0.85f, lineY),
                strokeWidth = (unit * 0.22f).coerceAtLeast(2.5f),
                cap = StrokeCap.Round
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(cx - blockWidth / 2f, lineY),
                size = Size(blockWidth, blockHeight),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }

        NoteDuration.HALF -> {
            // Half rest: horizontal reference line with filled block SITTING ON TOP of it ("hat rest")
            val lineY = cy + unit * 0.45f
            val blockWidth = unit * 2.2f
            val blockHeight = unit * 0.95f
            drawLine(
                color = color,
                start = Offset(cx - blockWidth * 0.85f, lineY),
                end = Offset(cx + blockWidth * 0.85f, lineY),
                strokeWidth = (unit * 0.22f).coerceAtLeast(2.5f),
                cap = StrokeCap.Round
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(cx - blockWidth / 2f, lineY - blockHeight),
                size = Size(blockWidth, blockHeight),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }

        NoteDuration.QUARTER -> {
            // Standard squiggly quarter rest
            val stroke = (unit * 0.34f).coerceAtLeast(3f)
            val path = Path().apply {
                moveTo(cx - unit * 0.25f, cy - unit * 1.65f)
                lineTo(cx + unit * 0.55f, cy - unit * 0.65f)
                lineTo(cx - unit * 0.45f, cy + unit * 0.15f)
                lineTo(cx + unit * 0.45f, cy + unit * 0.95f)
                cubicTo(
                    cx - unit * 0.55f, cy + unit * 0.85f,
                    cx - unit * 0.70f, cy + unit * 1.65f,
                    cx + unit * 0.10f, cy + unit * 1.85f
                )
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        NoteDuration.EIGHTH -> {
            // Eighth rest: diagonal stem + single hooked bulb at top
            val stroke = (unit * 0.28f).coerceAtLeast(2.5f)
            val topX = cx + unit * 0.45f
            val topY = cy - unit * 1.15f
            val bottomX = cx - unit * 0.35f
            val bottomY = cy + unit * 1.35f

            drawLine(
                color = color,
                start = Offset(topX, topY),
                end = Offset(bottomX, bottomY),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawRestHook(
                attachX = topX - unit * 0.10f,
                attachY = topY + unit * 0.25f,
                unit = unit,
                stroke = stroke,
                color = color
            )
        }

        NoteDuration.SIXTEENTH -> {
            // Sixteenth rest: diagonal stem + two hooked bulbs
            val stroke = (unit * 0.28f).coerceAtLeast(2.5f)
            val topX = cx + unit * 0.55f
            val topY = cy - unit * 1.45f
            val bottomX = cx - unit * 0.45f
            val bottomY = cy + unit * 1.65f

            drawLine(
                color = color,
                start = Offset(topX, topY),
                end = Offset(bottomX, bottomY),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawRestHook(
                attachX = topX - unit * 0.10f,
                attachY = topY + unit * 0.25f,
                unit = unit,
                stroke = stroke,
                color = color
            )
            drawRestHook(
                attachX = topX - unit * 0.42f,
                attachY = topY + unit * 1.20f,
                unit = unit,
                stroke = stroke,
                color = color
            )
        }
    }
}

private fun DrawScope.drawRestHook(
    attachX: Float,
    attachY: Float,
    unit: Float,
    stroke: Float,
    color: Color
) {
    val bulbCenter = Offset(attachX - unit * 0.65f, attachY - unit * 0.10f)
    drawCircle(
        color = color,
        radius = unit * 0.30f,
        center = bulbCenter
    )
    val hookPath = Path().apply {
        moveTo(bulbCenter.x, bulbCenter.y + unit * 0.15f)
        quadraticTo(
            bulbCenter.x + unit * 0.35f,
            attachY + unit * 0.25f,
            attachX,
            attachY
        )
    }
    drawPath(
        path = hookPath,
        color = color,
        style = Stroke(width = stroke, cap = StrokeCap.Round)
    )
}
