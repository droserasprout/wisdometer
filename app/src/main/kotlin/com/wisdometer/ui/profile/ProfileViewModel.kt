package com.wisdometer.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisdometer.data.model.PredictionWithOptions
import com.wisdometer.data.model.tagList
import com.wisdometer.data.repository.PredictionRepository
import com.wisdometer.domain.ScoringEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class TagAccuracy(val tag: String, val closeness: Double, val brierScore: Double)

data class ProfileUiState(
    val totalPredictions: Int = 0,
    val resolvedPredictions: Int = 0,
    val openPredictions: Int = 0,
    val avgConfidence: Double = 0.0,
    val simpleCloseness: Double = 0.0,
    val brierScore: Double = 0.0,
    val tagAccuracies: List<TagAccuracy> = emptyList(),
    val accuracyOverTime: List<Pair<Long, Double>> = emptyList(),
    val accuracyOverCount: List<Pair<Int, Double>> = emptyList(),
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: PredictionRepository,
    private val engine: ScoringEngine,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = repository.getAllPredictions()
        .map { all ->
            val resolved = all.filter { it.isResolved }
            val tags = all.flatMap { it.prediction.tagList }.distinct()
            val tagAccuracies = tags.map { tag ->
                TagAccuracy(
                    tag = tag,
                    closeness = engine.simpleClosenessForTag(all, tag),
                    brierScore = engine.brierScoreForTag(all, tag),
                )
            }
            ProfileUiState(
                totalPredictions = all.size,
                resolvedPredictions = resolved.size,
                openPredictions = all.size - resolved.size,
                avgConfidence = engine.avgConfidence(all),
                simpleCloseness = engine.simpleCloseness(resolved),
                brierScore = engine.brierScore(resolved),
                tagAccuracies = tagAccuracies,
                accuracyOverTime = engine.accuracyOverTime(resolved),
                accuracyOverCount = engine.accuracyOverCount(resolved),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())
}
