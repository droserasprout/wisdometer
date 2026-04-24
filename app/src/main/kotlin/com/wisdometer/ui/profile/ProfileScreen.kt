package com.wisdometer.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wisdometer.share.ShareImageRenderer
import com.wisdometer.ui.theme.Dim
import com.wisdometer.ui.theme.WisdometerTypography
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val chartDateFormatter = DateTimeFormatter.ofPattern("MMM d").withZone(ZoneId.systemDefault())

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var useTimeAxis by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dim.md),
    ) {
        Text("Profile", style = WisdometerTypography.headlineLarge)
        Spacer(modifier = Modifier.height(Dim.md))

        SectionHeader("Events")
        Spacer(modifier = Modifier.height(Dim.sm))
        val valOrDots: (Int) -> String = { if (state.isLoaded) it.toString() else "…" }
        Row(horizontalArrangement = Arrangement.spacedBy(Dim.sm), modifier = Modifier.fillMaxWidth()) {
            CompactStatTile(label = "Total", value = valOrDots(state.totalPredictions), modifier = Modifier.weight(1f))
            CompactStatTile(label = "Open", value = valOrDots(state.openPredictions), modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(Dim.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(Dim.sm), modifier = Modifier.fillMaxWidth()) {
            CompactStatTile(label = "Resolved", value = valOrDots(state.resolvedPredictions), modifier = Modifier.weight(1f))
            CompactStatTile(label = "Won", value = valOrDots(state.wonPredictions), modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(Dim.lg))
        SectionHeader("Stats")
        Spacer(modifier = Modifier.height(Dim.sm))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = Dim.CardShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(Dim.md)) {
                Row(horizontalArrangement = Arrangement.spacedBy(Dim.md)) {
                    SpeedometerGauge(
                        fraction = state.simpleCloseness.toFloat(),
                        valueText = if (state.isLoaded) "${(state.simpleCloseness * 100).roundToInt()}%" else "…",
                        label = "Accuracy",
                        modifier = Modifier.weight(1f),
                    )
                    SpeedometerGauge(
                        fraction = (state.brierScore / 2.0).toFloat(),
                        valueText = if (state.isLoaded) "%.2f".format(state.brierScore) else "…",
                        label = "Brier score",
                        invert = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(modifier = Modifier.height(Dim.md))
                Row(horizontalArrangement = Arrangement.spacedBy(Dim.md)) {
                    SpeedometerGauge(
                        fraction = (state.avgConfidence / 10.0).toFloat(),
                        valueText = if (state.isLoaded) "${(state.avgConfidence * 10).roundToInt()}%" else "…",
                        label = "Confidence (all)",
                        modifier = Modifier.weight(1f),
                    )
                    SpeedometerGauge(
                        fraction = (state.avgConfidenceOpen / 10.0).toFloat(),
                        valueText = if (state.isLoaded) "${(state.avgConfidenceOpen * 10).roundToInt()}%" else "…",
                        label = "Confidence (open)",
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(modifier = Modifier.height(Dim.md))
                ConfidenceChart(
                    distribution = state.confidenceDistribution,
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(Dim.lg))
        SectionHeader("Other")
        Spacer(modifier = Modifier.height(Dim.sm))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = Dim.CardShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(Dim.md)) {
                Text("Accuracy over time", style = WisdometerTypography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(Dim.sm)) {
                    FilterChip(
                        selected = useTimeAxis,
                        onClick = { useTimeAxis = true },
                        label = { Text("Over time") },
                    )
                    FilterChip(
                        selected = !useTimeAxis,
                        onClick = { useTimeAxis = false },
                        label = { Text("By count") },
                    )
                }
                Spacer(modifier = Modifier.height(Dim.sm))
                val chartPoints = if (useTimeAxis) state.accuracyOverTime else state.accuracyOverCount
                val xFirst = chartPoints.firstOrNull()?.first?.let { v ->
                    if (useTimeAxis) chartDateFormatter.format(Instant.ofEpochMilli(v as Long))
                    else "#$v"
                } ?: ""
                val xLast = chartPoints.lastOrNull()?.first?.let { v ->
                    if (useTimeAxis) chartDateFormatter.format(Instant.ofEpochMilli(v as Long))
                    else "#$v"
                } ?: ""
                AccuracyChart(
                    points = chartPoints,
                    xLabelFirst = xFirst,
                    xLabelLast = xLast,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(Dim.md))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = Dim.CardShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(Dim.md)) {
                Text("Calibration", style = WisdometerTypography.titleMedium)
                Text(
                    "Predicted % vs actual hit rate · dashed = perfect",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(Dim.sm))
                CalibrationChart(
                    points = state.calibration,
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                )
            }
        }

        if (state.tagAccuracies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Dim.md))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = Dim.CardShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(Dim.md)) {
                    Text("By Tag", style = WisdometerTypography.titleMedium)
                    Spacer(modifier = Modifier.height(Dim.sm))
                    state.tagAccuracies.forEach { ta ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(ta.tag, style = WisdometerTypography.bodyMedium)
                            Text(
                                "${(ta.closeness * 100).roundToInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Dim.md))
        val ctx = androidx.compose.ui.platform.LocalContext.current
        OutlinedButton(
            shape = Dim.ButtonShape,
            onClick = {
                ShareImageRenderer.shareProfileStats(
                    context = ctx,
                    accuracy = (state.simpleCloseness * 100).roundToInt(),
                    brierScore = state.brierScore,
                    totalPredictions = state.totalPredictions,
                    resolvedPredictions = state.resolvedPredictions,
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp)
                    .padding(end = Dim.xs),
            )
            Text("Share Stats")
        }
        Spacer(modifier = Modifier.height(Dim.md))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun CompactStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = Dim.CardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = Dim.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                value,
                style = WisdometerTypography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
