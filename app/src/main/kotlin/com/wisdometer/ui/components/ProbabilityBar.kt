package com.wisdometer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisdometer.ui.theme.SecondaryText

@Composable
fun ProbabilityBar(
    label: String,
    probability: Int,
    barColor: Color,
    isActualOutcome: Boolean,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val barHeight: Dp = if (compact) 6.dp else 10.dp
    val verticalPadding: Dp = if (compact) 2.dp else 4.dp

    Column(modifier = modifier.padding(vertical = verticalPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (isActualOutcome) "✓ $label" else label,
                fontSize = if (compact) 11.sp else 13.sp,
                color = if (isActualOutcome) barColor else SecondaryText,
            )
            Text(
                text = "$probability%",
                fontSize = if (compact) 11.sp else 13.sp,
                color = SecondaryText,
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(RoundedCornerShape(50))
                .background(barColor.copy(alpha = 0.15f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = probability / 100f)
                    .clip(RoundedCornerShape(50))
                    .background(barColor),
            )
        }
    }
}
