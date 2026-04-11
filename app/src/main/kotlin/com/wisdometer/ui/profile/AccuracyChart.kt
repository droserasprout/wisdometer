package com.wisdometer.ui.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.wisdometer.ui.theme.BarColors
import com.wisdometer.ui.theme.LocalWisdometerColors
import com.wisdometer.ui.theme.WisdometerTypography

@Composable
fun AccuracyChart(
    points: List<Pair<*, Double>>,
    xLabelFirst: String = "",
    xLabelLast: String = "",
    modifier: Modifier = Modifier,
) {
    if (points.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No resolved predictions yet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val lineColor = BarColors[0]
    val gridColor = LocalWisdometerColors.current.chartGridLine
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = WisdometerTypography.labelMedium.copy(color = labelColor)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val leftPad = 64f
        val rightPad = 16f
        val topPad = 16f
        val bottomPad = 36f
        val chartW = w - leftPad - rightPad
        val chartH = h - topPad - bottomPad

        // Y-axis grid lines and labels
        for (pct in listOf(0.0, 0.25, 0.5, 0.75, 1.0)) {
            val y = topPad + chartH * (1.0 - pct).toFloat()
            drawLine(gridColor, Offset(leftPad, y), Offset(leftPad + chartW, y), strokeWidth = 1f)
            val label = "${(pct * 100).toInt()}%"
            val measured = textMeasurer.measure(label, labelStyle)
            drawText(measured, topLeft = Offset(leftPad - measured.size.width - 8f, y - measured.size.height / 2f))
        }

        // Plot line
        val path = Path()
        points.forEachIndexed { i, (_, accuracy) ->
            val x = leftPad + chartW * (i.toFloat() / (points.size - 1).coerceAtLeast(1))
            val y = topPad + chartH * (1.0 - accuracy).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, lineColor, style = Stroke(width = 3f))

        // Plot dots
        points.forEachIndexed { i, (_, accuracy) ->
            val x = leftPad + chartW * (i.toFloat() / (points.size - 1).coerceAtLeast(1))
            val y = topPad + chartH * (1.0 - accuracy).toFloat()
            drawCircle(lineColor, radius = 5f, center = Offset(x, y))
        }

        // X-axis labels
        val xLabelY = topPad + chartH + 8f
        if (xLabelFirst.isNotEmpty()) {
            val measured = textMeasurer.measure(xLabelFirst, labelStyle)
            drawText(measured, topLeft = Offset(leftPad, xLabelY))
        }
        if (xLabelLast.isNotEmpty()) {
            val measured = textMeasurer.measure(xLabelLast, labelStyle)
            drawText(measured, topLeft = Offset(leftPad + chartW - measured.size.width, xLabelY))
        }
    }
}
