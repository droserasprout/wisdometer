package com.wisdometer.ui.predictions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wisdometer.ui.components.PredictionCard
import androidx.compose.material3.MaterialTheme
import com.wisdometer.ui.theme.WisdometerTypography

@Composable
fun PredictionsScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToNew: () -> Unit,
    viewModel: PredictionsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    // Refresh compact mode whenever this screen becomes active
    LaunchedEffect(Unit) { viewModel.refreshCompact() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Text(
                "Wisdometer",
                style = WisdometerTypography.headlineLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToNew) {
                Icon(Icons.Default.Add, contentDescription = "New prediction")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Status + tag filter row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(StatusFilter.values().toList()) { filter ->
                    FilterChip(
                        selected = state.statusFilter == filter,
                        onClick = { viewModel.setStatusFilter(filter) },
                        label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
                items(state.availableTags) { tag ->
                    FilterChip(
                        selected = state.selectedTag == tag,
                        onClick = {
                            viewModel.setTagFilter(if (state.selectedTag == tag) null else tag)
                        },
                        label = { Text(tag) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.items, key = { it.prediction.id }) { item ->
                    PredictionCard(
                        item = item,
                        compact = state.compact,
                        onClick = { onNavigateToDetail(item.prediction.id) },
                    )
                }
            }
        }
    }
}
