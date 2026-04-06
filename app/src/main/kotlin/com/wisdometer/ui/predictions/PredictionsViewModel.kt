package com.wisdometer.ui.predictions

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisdometer.data.model.PredictionWithOptions
import com.wisdometer.data.model.tagList
import com.wisdometer.data.repository.PredictionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class StatusFilter { ALL, OPEN, RESOLVED }

data class PredictionsUiState(
    val items: List<PredictionWithOptions> = emptyList(),
    val statusFilter: StatusFilter = StatusFilter.ALL,
    val selectedTag: String? = null,
    val availableTags: List<String> = emptyList(),
    val compact: Boolean = false,
)

@HiltViewModel
class PredictionsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PredictionRepository,
) : ViewModel() {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("wisdometer_settings", Context.MODE_PRIVATE)

    private val _statusFilter = MutableStateFlow(StatusFilter.ALL)
    private val _selectedTag = MutableStateFlow<String?>(null)
    private val _compact = MutableStateFlow(prefs.getBoolean("compact_mode", false))

    val uiState: StateFlow<PredictionsUiState> = combine(
        repository.getAllPredictions(),
        _statusFilter,
        _selectedTag,
        _compact,
    ) { all, statusFilter, selectedTag, compact ->
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
        PredictionsUiState(filtered, statusFilter, selectedTag, tags, compact)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PredictionsUiState())

    fun setStatusFilter(filter: StatusFilter) { _statusFilter.value = filter }
    fun setTagFilter(tag: String?) { _selectedTag.value = tag }

    fun refreshCompact() {
        _compact.value = prefs.getBoolean("compact_mode", false)
    }
}
