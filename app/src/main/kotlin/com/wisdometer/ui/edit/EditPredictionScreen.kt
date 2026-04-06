package com.wisdometer.ui.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wisdometer.ui.theme.WisdometerTypography
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPredictionScreen(
    predictionId: Long?,
    onDone: () -> Unit,
    viewModel: EditPredictionViewModel = hiltViewModel(),
) {
    LaunchedEffect(predictionId) {
        predictionId?.let { viewModel.loadPrediction(it) }
    }

    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (predictionId == null) "New Prediction" else "Edit Prediction",
                    style = WisdometerTypography.headlineMedium,
                )
                IconButton(onClick = onDone) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
        ) {
            OutlinedTextField(
                value = state.question,
                onValueChange = viewModel::setQuestion,
                label = { Text("Question") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Options", style = WisdometerTypography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            state.options.forEachIndexed { index, option ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = option.label,
                        onValueChange = { viewModel.setOptionLabel(index, it) },
                        label = { Text("Label") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = if (option.probability == 0) "" else option.probability.toString(),
                        onValueChange = { v ->
                            viewModel.setOptionProbability(index, v.toIntOrNull() ?: 0)
                        },
                        label = { Text("%") },
                        modifier = Modifier.width(72.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    if (state.options.size > 2) {
                        IconButton(onClick = { viewModel.removeOption(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove option")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            val sumColor = if (state.probabilitySum == 100)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.error
            Text(
                "Sum: ${state.probabilitySum}% (must equal 100%)",
                style = WisdometerTypography.bodySmall,
                color = sumColor,
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = viewModel::addOption) {
                Text("+ Add option")
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = state.tagsInput,
                onValueChange = viewModel::setTagsInput,
                label = { Text("Tags (comma-separated)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Reminder date picker
            val dateLabel = state.reminderAt?.let {
                java.time.ZonedDateTime.ofInstant(it, ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
            } ?: "Set reminder (optional)"
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(dateLabel)
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = state.reminderAt?.toEpochMilli()
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                viewModel.setReminder(java.time.Instant.ofEpochMilli(it))
                            }
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            viewModel.setReminder(null)
                            showDatePicker = false
                        }) { Text("Clear") }
                    },
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.save(onDone) },
                enabled = state.question.isNotBlank() && state.probabilitySum == 100 && !state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (state.isSaving) "Saving…" else "Save")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
