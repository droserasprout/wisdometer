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
import androidx.compose.ui.unit.dp
import com.wisdometer.domain.CalibrationPoint
import com.wisdometer.ui.theme.BarColors
import com.wisdometer.ui.theme.ChartDim
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
        val left = ChartDim.leftPad.toPx()
        val right = ChartDim.rightPad.toPx()
        val top = ChartDim.topPad.toPx()
        val bottom = ChartDim.bottomPad.toPx()
        val cw = w - left - right
        val ch = h - top - bottom
        val gridStroke = 1.dp.toPx()
        val diagonalStroke = 1.5.dp.toPx()
        val dotStroke = 1.5.dp.toPx()

        fun xOf(pct: Float) = left + cw * pct / 100f
        fun yOf(rate: Float) = top + ch * (1f - rate / 100f)

        for (pct in listOf(0, 25, 50, 75, 100)) {
            val y = yOf(pct.toFloat())
            drawLine(gridColor, Offset(left, y), Offset(left + cw, y), strokeWidth = gridStroke)
            val m = textMeasurer.measure("$pct%", labelStyle)
            drawText(m, topLeft = Offset(left - m.size.width - 4.dp.toPx(), y - m.size.height / 2f))
        }

        for (pct in listOf(0, 50, 100)) {
            val x = xOf(pct.toFloat())
            val m = textMeasurer.measure("$pct%", labelStyle)
            drawText(m, topLeft = Offset(x - m.size.width / 2f, top + ch + 4.dp.toPx()))
        }

        drawLine(
            color = diagonalColor,
            start = Offset(xOf(0f), yOf(0f)),
            end = Offset(xOf(100f), yOf(100f)),
            strokeWidth = diagonalStroke,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())),
        )

        val maxCount = points.maxOf { it.count }
        val baseRadius = 4.dp.toPx()
        val radiusRange = 6.dp.toPx()
        for (pt in points) {
            val x = xOf(pt.predictedPct.toFloat())
            val y = yOf((pt.actualRate * 100).toFloat())
            val radius = baseRadius + radiusRange * (pt.count.toFloat() / maxCount)
            drawCircle(dotColor.copy(alpha = 0.25f), radius = radius, center = Offset(x, y))
            drawCircle(dotColor, radius = radius, center = Offset(x, y), style = Stroke(width = dotStroke))
        }
    }
}
