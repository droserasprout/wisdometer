package com.wisdometer.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class PredictionWithOptions(
    @Embedded val prediction: Prediction,
    @Relation(
        parentColumn = "id",
        entityColumn = "predictionId",
    )
    val options: List<PredictionOption>,
) {
    val isResolved: Boolean get() = prediction.resolvedAt != null
    val sortedOptions: List<PredictionOption> get() = options.sortedBy { it.sortOrder }
    val actualOption: PredictionOption? get() = options.find { it.id == prediction.outcomeOptionId }
}
