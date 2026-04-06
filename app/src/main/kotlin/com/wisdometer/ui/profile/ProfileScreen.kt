package com.wisdometer.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wisdometer.ui.theme.WisdometerTypography
import kotlin.math.roundToInt

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var useTimeAxis by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("Profile", style = WisdometerTypography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Overall accuracy card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "${(state.simpleCloseness * 100).roundToInt()}% Accuracy",
                    style = WisdometerTypography.headlineLarge,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Text(
                        "Brier Score: ${"%.2f".format(state.brierScore)}",
                        style = WisdometerTypography.bodySmall,
                    )
                    var showTooltip by remember { mutableStateOf(false) }
                    IconButton(onClick = { showTooltip = !showTooltip }) {
                        Text("?")
                    }
                    if (showTooltip) {
                        AlertDialog(
                            onDismissRequest = { showTooltip = false },
                            title = { Text("Brier Score") },
                            text = { Text("Brier score measures calibration — 0.0 is perfect, 2.0 is worst.") },
                            confirmButton = {
                                TextButton(onClick = { showTooltip = false }) { Text("OK") }
                            },
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary stats card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("Summary", style = WisdometerTypography.titleMedium)
                Text("Total: ${state.totalPredictions}")
                Text("Resolved: ${state.resolvedPredictions}")
                Text("Open: ${state.openPredictions}")
                Text("Avg Confidence: ${(state.avgConfidence).roundToInt()}%")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Accuracy chart card with toggle
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Accuracy", style = WisdometerTypography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                Spacer(modifier = Modifier.height(8.dp))
                val chartPoints = if (useTimeAxis) state.accuracyOverTime else state.accuracyOverCount
                AccuracyChart(
                    points = chartPoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                )
            }
        }

        // Tag accuracy breakdown
        if (state.tagAccuracies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("By Tag", style = WisdometerTypography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
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
                                style = WisdometerTypography.bodySmall,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
