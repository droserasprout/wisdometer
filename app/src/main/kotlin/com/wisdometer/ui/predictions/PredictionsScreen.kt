package com.wisdometer.ui.predictions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wisdometer.ui.components.PredictionCard
import com.wisdometer.ui.theme.Dim
import com.wisdometer.ui.theme.WisdometerTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionsScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToNew: () -> Unit,
    viewModel: PredictionsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToNew) {
                Icon(Icons.Default.Add, contentDescription = "New prediction")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            StatusFilterRow(
                selected = state.statusFilter,
                onSelect = viewModel::setStatusFilter,
                modifier = Modifier.padding(horizontal = Dim.md, vertical = Dim.sm),
            )

            if (state.availableTags.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Dim.md),
                    horizontalArrangement = Arrangement.spacedBy(Dim.sm),
                ) {
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
                Spacer(modifier = Modifier.height(Dim.sm))
            }

            if (state.items.isEmpty()) {
                val hasFilters = state.statusFilter != StatusFilter.ALL || state.selectedTag != null
                EmptyState(
                    hasFilters = hasFilters,
                    onClearFilters = {
                        viewModel.setStatusFilter(StatusFilter.ALL)
                        viewModel.setTagFilter(null)
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = Dim.md, vertical = Dim.xs),
                    verticalArrangement = Arrangement.spacedBy(Dim.md),
                ) {
                    items(state.items, key = { it.prediction.id }) { item ->
                        PredictionCard(
                            item = item,
                            onClick = { onNavigateToDetail(item.prediction.id) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusFilterRow(
    selected: StatusFilter,
    onSelect: (StatusFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filters = StatusFilter.values()
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        filters.forEachIndexed { i, filter ->
            SegmentedButton(
                selected = selected == filter,
                onClick = { onSelect(filter) },
                shape = SegmentedButtonDefaults.itemShape(index = i, count = filters.size),
            ) {
                Text(filter.name.lowercase().replaceFirstChar { it.uppercase() })
            }
        }
    }
}

@Composable
private fun EmptyState(
    hasFilters: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Outlined.Lightbulb,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(Dim.sm))
        Text(
            if (hasFilters) "No matches" else "No predictions yet",
            style = WisdometerTypography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(Dim.xs))
        Text(
            if (hasFilters) "Try clearing filters" else "Tap + to add your first prediction",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (hasFilters) {
            Spacer(modifier = Modifier.height(Dim.sm))
            TextButton(onClick = onClearFilters) { Text("Clear filters") }
        }
    }
}
