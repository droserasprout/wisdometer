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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.wisdometer.ui.theme.BarColors
import com.wisdometer.ui.theme.LocalWisdometerColors

/**
 * Bar chart of how often the user's top option falls in each 10%-wide probability bucket.
 * Shows whether you tend to make decisive (high confidence) or uncertain predictions.
 */
@Composable
fun ConfidenceChart(
    distribution: List<Pair<Int, Int>>,  // (bucketMidpoint, count)
    modifier: Modifier = Modifier,
) {
    val allZero = distribution.all { it.second == 0 }
    if (distribution.isEmpty() || allZero) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No predictions yet", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val barColor = BarColors[0]
    val gridColor = LocalWisdometerColors.current.chartGridLine
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = labelColor)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val left = 36f
        val right = 8f
        val top = 16f
        val bottom = 32f
        val cw = w - left - right
        val ch = h - top - bottom

        val maxCount = distribution.maxOf { it.second }.coerceAtLeast(1)
        val barCount = distribution.size
        val slotW = cw / barCount
        val barW = slotW * 0.6f
        val gap = slotW * 0.2f

        // Horizontal grid + Y labels at 0 and maxCount
        for ((fraction, label) in listOf(0f to "0", 0.5f to "${maxCount / 2}", 1f to "$maxCount")) {
            val y = top + ch * (1f - fraction)
            drawLine(gridColor, Offset(left, y), Offset(left + cw, y), strokeWidth = 1f)
            val m = textMeasurer.measure(label, labelStyle)
            drawText(m, topLeft = Offset(left - m.size.width - 4f, y - m.size.height / 2f))
        }

        // Bars
        distribution.forEachIndexed { i, (midpoint, count) ->
            val barH = ch * count.toFloat() / maxCount
            val x = left + i * slotW + gap
            val y = top + ch - barH

            drawRoundRect(
                color = barColor.copy(alpha = 0.85f),
                topLeft = Offset(x, y),
                size = Size(barW, barH),
                cornerRadius = CornerRadius(4f, 4f),
            )

            // X label (bucket range)
            val lo = midpoint - 5
            val hi = midpoint + 4
            val xLabel = "$lo–$hi"
            val m = textMeasurer.measure(xLabel, labelStyle)
            drawText(m, topLeft = Offset(x + barW / 2f - m.size.width / 2f, top + ch + 6f))

            // Count on top of bar
            if (count > 0) {
                val cm = textMeasurer.measure("$count", labelStyle)
                drawText(cm, topLeft = Offset(x + barW / 2f - cm.size.width / 2f, y - cm.size.height - 2f))
            }
        }
    }
}
