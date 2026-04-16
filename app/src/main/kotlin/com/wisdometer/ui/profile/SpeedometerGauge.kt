package com.wisdometer.ui.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.wisdometer.ui.theme.SemanticColors
import com.wisdometer.ui.theme.WisdometerTypography
import kotlin.math.cos
import kotlin.math.sin

/**
 * Half-circle speedometer gauge. Arc carries a red→amber→green gradient; a needle
 * pivots from the arc's center-bottom to the point on the arc matching [fraction].
 *
 * @param fraction Needle position in [0f, 1f]. 0 = arc's left end, 1 = arc's right end.
 * @param invert When true, the gradient is reversed so green sits on the left
 *   (i.e. "lower is better" metrics like Brier).
 */
@Composable
fun SpeedometerGauge(
    fraction: Float,
    valueText: String,
    label: String,
    modifier: Modifier = Modifier,
    invert: Boolean = false,
) {
    val needleColor = MaterialTheme.colorScheme.onSurface
    val valueColor = MaterialTheme.colorScheme.onSurface
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f),
        ) {
            val strokeWidth = size.height * 0.14f
            val inset = strokeWidth / 2f
            val arcDiameter = size.width - strokeWidth
            val radius = arcDiameter / 2f
            val arcSize = Size(arcDiameter, arcDiameter)
            val topLeft = Offset(inset, inset)
            val cx = size.width / 2f
            val cy = inset + radius

            val segments = 48
            val totalSweep = 180f
            val segSweep = totalSweep / segments
            for (i in 0 until segments) {
                val t = i.toFloat() / (segments - 1)
                val gradientT = if (invert) 1f - t else t
                val col = if (gradientT < 0.5f) {
                    lerp(SemanticColors.red, SemanticColors.amber, gradientT * 2f)
                } else {
                    lerp(SemanticColors.amber, SemanticColors.green, (gradientT - 0.5f) * 2f)
                }
                drawArc(
                    color = col,
                    startAngle = 180f + i * segSweep,
                    sweepAngle = segSweep + 0.8f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                )
            }

            val clamped = fraction.coerceIn(0f, 1f)
            val needleAngleDeg = 180f + clamped * 180f
            val needleRad = Math.toRadians(needleAngleDeg.toDouble())
            val needleLen = radius - strokeWidth / 2f - 2.dp.toPx()
            val nx = cx + (needleLen * cos(needleRad)).toFloat()
            val ny = cy + (needleLen * sin(needleRad)).toFloat()
            drawLine(
                color = needleColor,
                start = Offset(cx, cy),
                end = Offset(nx, ny),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawCircle(
                color = needleColor,
                radius = strokeWidth * 0.35f,
                center = Offset(cx, cy),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(valueText, style = WisdometerTypography.titleLarge, color = valueColor)
        Text(label, style = MaterialTheme.typography.labelSmall, color = labelColor)
    }
}
