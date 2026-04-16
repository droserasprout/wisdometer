package com.wisdometer.ui.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.wisdometer.domain.ConfidenceBucket
import com.wisdometer.ui.theme.ChartDim
import com.wisdometer.ui.theme.LocalWisdometerColors
import com.wisdometer.ui.theme.WisdometerTypography
import com.wisdometer.ui.theme.weightColor

/**
 * Per weight (1–10): bar height = total options assigned that weight across all predictions;
 * a horizontal tick on each bar marks how many of those were the actual resolved outcome.
 * Bar colors follow the same red→amber→green gradient as the option weight bar.
 */
@Composable
fun ConfidenceChart(
    distribution: List<ConfidenceBucket>,
    modifier: Modifier = Modifier,
) {
    val allZero = distribution.all { it.total == 0 }
    if (distribution.isEmpty() || allZero) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No predictions yet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val gridColor = LocalWisdometerColors.current.chartGridLine
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = WisdometerTypography.labelMedium.copy(color = labelColor)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val left = ChartDim.leftPad.toPx()
        val right = ChartDim.rightPad.toPx()
        val top = ChartDim.topPad.toPx()
        val bottom = ChartDim.bottomPad.toPx()
        val cw = w - left - right
        val ch = h - top - bottom
        val gridStroke = 1.dp.toPx()
        val tickStroke = 2.dp.toPx()
        val cornerPx = 2.dp.toPx()

        val maxTotal = distribution.maxOf { it.total }.coerceAtLeast(1)
        val barCount = distribution.size
        val slotW = cw / barCount
        val barW = slotW * 0.6f
        val gap = slotW * 0.2f

        for ((fraction, label) in listOf(0f to "0", 0.5f to "${maxTotal / 2}", 1f to "$maxTotal")) {
            val y = top + ch * (1f - fraction)
            drawLine(gridColor, Offset(left, y), Offset(left + cw, y), strokeWidth = gridStroke)
            val m = textMeasurer.measure(label, labelStyle)
            drawText(m, topLeft = Offset(left - m.size.width - 4.dp.toPx(), y - m.size.height / 2f))
        }

        distribution.forEachIndexed { i, bucket ->
            val color = weightColor(bucket.weight)
            val barH = ch * bucket.total.toFloat() / maxTotal
            val x = left + i * slotW + gap
            val y = top + ch - barH

            drawRoundRect(
                color = color.copy(alpha = 0.4f),
                topLeft = Offset(x, y),
                size = Size(barW, barH),
                cornerRadius = CornerRadius(cornerPx, cornerPx),
            )

            if (bucket.actual > 0) {
                val tickH = ch * bucket.actual.toFloat() / maxTotal
                val tickY = top + ch - tickH
                drawLine(
                    color = color,
                    start = Offset(x - 1.dp.toPx(), tickY),
                    end = Offset(x + barW + 1.dp.toPx(), tickY),
                    strokeWidth = tickStroke,
                )
            }

            val xLabel = "${bucket.weight}"
            val m = textMeasurer.measure(xLabel, labelStyle)
            drawText(m, topLeft = Offset(x + barW / 2f - m.size.width / 2f, top + ch + 4.dp.toPx()))

            if (bucket.total > 0) {
                val topText = if (bucket.actual > 0) "${bucket.actual}/${bucket.total}" else "${bucket.total}"
                val cm = textMeasurer.measure(topText, labelStyle)
                drawText(cm, topLeft = Offset(x + barW / 2f - cm.size.width / 2f, y - cm.size.height - 2.dp.toPx()))
            }
        }
    }
}
