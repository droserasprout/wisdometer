package com.wisdometer.ui.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import com.wisdometer.domain.CalibrationPoint
import com.wisdometer.ui.theme.BarColors
import com.wisdometer.ui.theme.LocalWisdometerColors
import com.wisdometer.ui.theme.WisdometerTypography

/**
 * Calibration chart: predicted probability (X) vs actual hit rate (Y).
 * The dashed diagonal = perfect calibration. Points above = underconfident, below = overconfident.
 */
@Composable
fun CalibrationChart(
    points: List<CalibrationPoint>,
    modifier: Modifier = Modifier,
) {
    if (points.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No resolved predictions yet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val dotColor = BarColors[1]
    val gridColor = LocalWisdometerColors.current.chartGridLine
    val diagonalColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = WisdometerTypography.labelMedium.copy(color = labelColor)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val left = 48f
        val right = 16f
        val top = 16f
        val bottom = 32f
        val cw = w - left - right
        val ch = h - top - bottom

        fun xOf(pct: Float) = left + cw * pct / 100f
        fun yOf(rate: Float) = top + ch * (1f - rate / 100f)

        // Grid lines + Y labels at 0/25/50/75/100%
        for (pct in listOf(0, 25, 50, 75, 100)) {
            val y = yOf(pct.toFloat())
            drawLine(gridColor, Offset(left, y), Offset(left + cw, y), strokeWidth = 1f)
            val m = textMeasurer.measure("$pct%", labelStyle)
            drawText(m, topLeft = Offset(left - m.size.width - 6f, y - m.size.height / 2f))
        }

        // X labels at 0/50/100%
        for (pct in listOf(0, 50, 100)) {
            val x = xOf(pct.toFloat())
            val m = textMeasurer.measure("$pct%", labelStyle)
            drawText(m, topLeft = Offset(x - m.size.width / 2f, top + ch + 6f))
        }

        // Perfect calibration diagonal (dashed)
        drawLine(
            color = diagonalColor,
            start = Offset(xOf(0f), yOf(0f)),
            end = Offset(xOf(100f), yOf(100f)),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
        )

        // Data points — radius scales with sample count
        val maxCount = points.maxOf { it.count }
        for (pt in points) {
            val x = xOf(pt.predictedPct.toFloat())
            val y = yOf((pt.actualRate * 100).toFloat())
            val radius = 6f + 10f * (pt.count.toFloat() / maxCount)
            drawCircle(dotColor.copy(alpha = 0.25f), radius = radius, center = Offset(x, y))
            drawCircle(dotColor, radius = radius, center = Offset(x, y), style = Stroke(width = 2f))
        }
    }
}
