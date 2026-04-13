package com.wisdometer.ui.predictions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisdometer.data.model.PredictionWithOptions
import com.wisdometer.data.model.tagList
import com.wisdometer.data.repository.PredictionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class StatusFilter { ALL, OPEN, RESOLVED }

data class PredictionsUiState(
    val items: List<PredictionWithOptions> = emptyList(),
    val statusFilter: StatusFilter = StatusFilter.ALL,
    val selectedTag: String? = null,
    val availableTags: List<String> = emptyList(),
)

@HiltViewModel
class PredictionsViewModel @Inject constructor(
    private val repository: PredictionRepository,
) : ViewModel() {

    private val _statusFilter = MutableStateFlow(StatusFilter.ALL)
    private val _selectedTag = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PredictionsUiState> = combine(
        repository.getAllPredictions(),
        _statusFilter,
        _selectedTag,
    ) { all, statusFilter, selectedTag ->
        val filtered = all
            .filter { item ->
                when (statusFilter) {
                    StatusFilter.ALL -> true
                    StatusFilter.OPEN -> !item.isResolved
                    StatusFilter.RESOLVED -> item.isResolved
                }
            }
            .filter { item ->
                selectedTag == null || selectedTag in item.prediction.tagList
            }
        val tags = all.flatMap { it.prediction.tagList }.distinct().sorted()
        PredictionsUiState(filtered, statusFilter, selectedTag, tags)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PredictionsUiState())

    fun setStatusFilter(filter: StatusFilter) { _statusFilter.value = filter }
    fun setTagFilter(tag: String?) { _selectedTag.value = tag }
}
