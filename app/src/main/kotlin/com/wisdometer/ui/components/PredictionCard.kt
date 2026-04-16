package com.wisdometer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.wisdometer.data.model.Prediction
import com.wisdometer.data.model.PredictionWithOptions
import com.wisdometer.data.model.tagList
import com.wisdometer.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFmtShort = DateTimeFormatter.ofPattern("MMM d").withZone(ZoneId.systemDefault())

@Composable
fun PredictionCard(
    item: PredictionWithOptions,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val wisdometerColors = LocalWisdometerColors.current
    val resolvedAlpha = if (item.isResolved) wisdometerColors.resolvedCardAlpha else 1f
    val topOptionId = item.sortedOptions.maxByOrNull { it.weight }?.id
    val tags = item.prediction.tagList

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(resolvedAlpha)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = item.prediction.title,
                    style = WisdometerTypography.titleMedium,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                )
                StatusBadge(isResolved = item.isResolved)
            }

            if (item.prediction.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.prediction.description,
                    style = WisdometerTypography.bodySmall,
                )
            }

            SectionDivider()

            item.sortedOptions.forEach { option ->
                ProbabilityBar(
                    label = option.label,
                    weight = option.weight,
                    barColor = weightColor(option.weight),
                    isActualOutcome = option.id == item.prediction.outcomeOptionId,
                    isTopPrediction = option.id == topOptionId,
                )
            }

            SectionDivider()

            if (tags.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Label,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = tags.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            TimelineFooter(item.prediction)
        }
    }
}

@Composable
private fun SectionDivider() {
    Spacer(modifier = Modifier.height(12.dp))
    HorizontalDivider(
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun TimelineFooter(prediction: Prediction) {
    val end = prediction.reminderAt
    if (end == null) {
        LegacyDatesRow(prediction)
        return
    }
    val labelStyle = MaterialTheme.typography.bodySmall
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text("Created  ${dateFmtShort.format(prediction.createdAt)}", style = labelStyle, color = labelColor)
            prediction.updatedAt?.let {
                Text("Updated  ${dateFmtShort.format(it)}", style = labelStyle, color = labelColor)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("Start  ${dateFmtShort.format(prediction.createdAt)}", style = labelStyle, color = labelColor)
            Text("End    ${dateFmtShort.format(end)}", style = labelStyle, color = labelColor)
        }
    }

    Spacer(modifier = Modifier.height(6.dp))

    val total = (end.toEpochMilli() - prediction.createdAt.toEpochMilli()).coerceAtLeast(1L)
    val reference = prediction.resolvedAt ?: Instant.now()
    val elapsed = reference.toEpochMilli() - prediction.createdAt.toEpochMilli()
    val ratio = (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(labelColor.copy(alpha = 0.15f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(ratio)
                .fillMaxHeight()
                .background(labelColor.copy(alpha = 0.6f)),
        )
    }
}

@Composable
private fun LegacyDatesRow(prediction: Prediction) {
    val labelStyle = MaterialTheme.typography.bodySmall
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                Icons.Outlined.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = labelColor,
            )
            Text(dateFmtShort.format(prediction.createdAt), style = labelStyle, color = labelColor)
        }
        prediction.updatedAt?.let { updatedAt ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = labelColor,
                )
                Text(dateFmtShort.format(updatedAt), style = labelStyle, color = labelColor)
            }
        }
    }
}
