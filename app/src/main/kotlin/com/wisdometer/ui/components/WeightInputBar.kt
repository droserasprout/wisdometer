package com.wisdometer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.wisdometer.ui.theme.weightColor

@Composable
fun WeightInputBar(
    weight: Int,
    onWeightChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentWeight by rememberUpdatedState(weight)
    val currentOnChange by rememberUpdatedState(onWeightChange)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    val w0 = positionToWeight(down.position.x, size.width)
                    if (w0 != currentWeight) currentOnChange(w0)
                    drag(down.id) { change ->
                        val w = positionToWeight(change.position.x, size.width)
                        if (w != currentWeight) currentOnChange(w)
                        change.consume()
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(24.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            for (i in 1..10) {
                val segmentColor = if (i <= weight) weightColor(i) else weightColor(i).copy(alpha = 0.15f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(segmentColor),
                )
            }
        }
    }
}

private fun positionToWeight(x: Float, width: Int): Int {
    if (width <= 0) return 1
    return ((x / width) * 10f).toInt().coerceIn(0, 9) + 1
}
