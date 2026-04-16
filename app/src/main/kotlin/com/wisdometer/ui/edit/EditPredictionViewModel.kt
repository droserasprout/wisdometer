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
    val weight: Int = 5,
    val sortOrder: Int = 0,
)

data class EditUiState(
    val question: String = "",
    val description: String = "",
    val options: List<OptionDraft> = listOf(OptionDraft(sortOrder = 0), OptionDraft(sortOrder = 1)),
    val reminderAt: Instant? = null,
    val tagsInput: String = "",
    val isSaving: Boolean = false,
    val isLoaded: Boolean = false,
    val showValidation: Boolean = false,
    val isDirty: Boolean = false,
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
                            OptionDraft(opt.id, opt.label, opt.weight, i)
                        },
                        reminderAt = item.prediction.reminderAt,
                        tagsInput = item.prediction.tags.replace(",", ", "),
                        isLoaded = true,
                        isDirty = false,
                        createdAt = item.prediction.createdAt,
                        resolvedAt = item.prediction.resolvedAt,
                        outcomeOptionId = item.prediction.outcomeOptionId,
                    )
                }
            }
        }
    }

    fun setQuestion(q: String) = edit { it.copy(question = q) }
    fun setDescription(d: String) = edit { it.copy(description = d) }
    fun setStartDate(instant: Instant) = edit { it.copy(createdAt = instant) }
    fun setEndDate(instant: Instant?) = edit { it.copy(reminderAt = instant) }
    fun setTagsInput(t: String) = edit { it.copy(tagsInput = t) }

    fun setOptionLabel(index: Int, label: String) = updateOption(index) { it.copy(label = label) }
    fun setOptionWeight(index: Int, weight: Int) {
        updateOption(index) { it.copy(weight = weight.coerceIn(1, 10)) }
    }

    fun addOption() = edit { s ->
        s.copy(options = s.options + OptionDraft(sortOrder = s.options.size))
    }

    fun removeOption(index: Int) = edit { s ->
        val newOptions = s.options.toMutableList().also { it.removeAt(index) }
            .mapIndexed { i, opt -> opt.copy(sortOrder = i) }
        s.copy(options = newOptions)
    }

    private fun updateOption(index: Int, transform: (OptionDraft) -> OptionDraft) =
        edit { s ->
            val newOptions = s.options.toMutableList()
            newOptions[index] = transform(newOptions[index])
            s.copy(options = newOptions)
        }

    private inline fun edit(transform: (EditUiState) -> EditUiState) {
        _state.update { transform(it).copy(isDirty = true) }
    }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        if (s.question.isBlank() || s.reminderAt == null || s.isSaving) {
            _state.update { it.copy(showValidation = true) }
            return
        }
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
                    weight = draft.weight,
                    sortOrder = i,
                )
            }
            repository.savePrediction(prediction, options)
            onDone()
        }
    }
}
