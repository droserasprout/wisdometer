package com.wisdometer.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisdometer.data.model.PredictionWithOptions
import com.wisdometer.data.repository.PredictionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class PredictionDetailViewModel @Inject constructor(
    private val repository: PredictionRepository,
) : ViewModel() {

    private val _predictionId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val item: StateFlow<PredictionWithOptions?> = _predictionId
        .filterNotNull()
        .flatMapLatest { repository.getPredictionById(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun load(id: Long) { _predictionId.value = id }

    fun setOutcome(optionId: Long) {
        val current = item.value ?: return
        viewModelScope.launch {
            repository.savePrediction(
                current.prediction.copy(
                    outcomeOptionId = optionId,
                    resolvedAt = Instant.now(),
                ),
                current.options,
            )
        }
    }

    fun unresolve() {
        val current = item.value ?: return
        viewModelScope.launch {
            repository.savePrediction(
                current.prediction.copy(
                    outcomeOptionId = null,
                    resolvedAt = null,
                ),
                current.options,
            )
        }
    }

    fun delete(onDone: () -> Unit) {
        val current = item.value ?: return
        viewModelScope.launch {
            repository.deletePrediction(current.prediction)
            onDone()
        }
    }
}
