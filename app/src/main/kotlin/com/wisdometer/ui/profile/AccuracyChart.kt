package com.wisdometer.ui.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisdometer.ui.theme.BarColors

@Composable
fun AccuracyChart(
    points: List<Pair<*, Double>>,
    modifier: Modifier = Modifier,
) {
    if (points.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No resolved predictions yet", fontSize = 12.sp, color = Color.Gray)
        }
        return
    }

    val lineColor = BarColors[0]
    val gridColor = Color(0xFFE0E0E0)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val padH = 24f
        val padV = 16f
        val chartW = w - 2 * padH
        val chartH = h - 2 * padV

        // Draw grid lines at 0%, 25%, 50%, 75%, 100%
        for (pct in listOf(0.0, 0.25, 0.5, 0.75, 1.0)) {
            val y = padV + chartH * (1.0 - pct).toFloat()
            drawLine(gridColor, Offset(padH, y), Offset(padH + chartW, y), strokeWidth = 1f)
        }

        // Plot line
        val path = Path()
        points.forEachIndexed { i, (_, accuracy) ->
            val x = padH + chartW * (i.toFloat() / (points.size - 1).coerceAtLeast(1))
            val y = padV + chartH * (1.0 - accuracy).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, lineColor, style = Stroke(width = 3f))

        // Plot dots
        points.forEachIndexed { i, (_, accuracy) ->
            val x = padH + chartW * (i.toFloat() / (points.size - 1).coerceAtLeast(1))
            val y = padV + chartH * (1.0 - accuracy).toFloat()
            drawCircle(lineColor, radius = 5f, center = Offset(x, y))
        }
    }
}
