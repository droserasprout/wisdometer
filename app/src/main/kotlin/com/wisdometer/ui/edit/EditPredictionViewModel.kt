package com.wisdometer.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisdometer.data.model.Prediction
import com.wisdometer.data.model.PredictionOption
import com.wisdometer.data.repository.PredictionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class OptionDraft(
    val id: Long = 0,
    val label: String = "",
    val probability: Int = 0,
    val sortOrder: Int = 0,
)

data class EditUiState(
    val question: String = "",
    val description: String = "",
    val options: List<OptionDraft> = listOf(OptionDraft(sortOrder = 0), OptionDraft(sortOrder = 1)),
    val reminderAt: Instant? = null,
    val tagsInput: String = "",
    val probabilitySum: Int = 0,
    val isSaving: Boolean = false,
    val isLoaded: Boolean = false,
    // preserved from original — never overwritten on edit
    val createdAt: Instant = Instant.now(),
    val resolvedAt: Instant? = null,
    val outcomeOptionId: Long? = null,
)

@HiltViewModel
class EditPredictionViewModel @Inject constructor(
    private val repository: PredictionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EditUiState())
    val state: StateFlow<EditUiState> = _state.asStateFlow()

    private var editingPredictionId: Long? = null

    fun loadPrediction(id: Long) {
        if (_state.value.isLoaded) return
        viewModelScope.launch {
            repository.getPredictionById(id).firstOrNull()?.let { item ->
                editingPredictionId = id
                _state.update {
                    it.copy(
                        question = item.prediction.title,
                        description = item.prediction.description,
                        options = item.sortedOptions.mapIndexed { i, opt ->
                            OptionDraft(opt.id, opt.label, opt.probability, i)
                        },
                        reminderAt = item.prediction.reminderAt,
                        tagsInput = item.prediction.tags.replace(",", ", "),
                        probabilitySum = item.options.sumOf { opt -> opt.probability },
                        isLoaded = true,
                        createdAt = item.prediction.createdAt,
                        resolvedAt = item.prediction.resolvedAt,
                        outcomeOptionId = item.prediction.outcomeOptionId,
                    )
                }
            }
        }
    }

    fun setQuestion(q: String) = _state.update { it.copy(question = q) }
    fun setDescription(d: String) = _state.update { it.copy(description = d) }
    fun setStartDate(instant: Instant) = _state.update { it.copy(createdAt = instant) }
    fun setEndDate(instant: Instant?) = _state.update { it.copy(reminderAt = instant) }
    fun setTagsInput(t: String) = _state.update { it.copy(tagsInput = t) }

    fun setOptionLabel(index: Int, label: String) = updateOption(index) { it.copy(label = label) }
    fun setOptionProbability(index: Int, prob: Int) {
        updateOption(index) { it.copy(probability = prob.coerceIn(0, 100)) }
        recalcSum()
    }

    fun addOption() = _state.update { s ->
        val newOptions = s.options + OptionDraft(sortOrder = s.options.size)
        s.copy(options = newOptions)
    }

    fun removeOption(index: Int) = _state.update { s ->
        val newOptions = s.options.toMutableList().also { it.removeAt(index) }
            .mapIndexed { i, opt -> opt.copy(sortOrder = i) }
        s.copy(options = newOptions, probabilitySum = newOptions.sumOf { it.probability })
    }

    private fun updateOption(index: Int, transform: (OptionDraft) -> OptionDraft) =
        _state.update { s ->
            val newOptions = s.options.toMutableList()
            newOptions[index] = transform(newOptions[index])
            s.copy(options = newOptions)
        }

    private fun recalcSum() = _state.update { s ->
        s.copy(probabilitySum = s.options.sumOf { it.probability })
    }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        if (s.question.isBlank() || s.probabilitySum != 100) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val tags = s.tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }.joinToString(",")
            val prediction = Prediction(
                id = editingPredictionId ?: 0L,
                title = s.question.trim(),
                description = s.description.trim(),
                createdAt = s.createdAt,
                reminderAt = s.reminderAt,
                resolvedAt = s.resolvedAt,
                outcomeOptionId = s.outcomeOptionId,
                tags = tags,
            )
            val options = s.options.mapIndexed { i, draft ->
                PredictionOption(
                    id = draft.id,
                    predictionId = editingPredictionId ?: 0L,
                    label = draft.label,
                    probability = draft.probability,
                    sortOrder = i,
                )
            }
            repository.savePrediction(prediction, options)
            onDone()
        }
    }
}
