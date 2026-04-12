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

    /** Normalized probabilities (0.0–1.0) derived from option weights. */
    val normalizedProbabilities: Map<Long, Double>
        get() {
            val total = options.sumOf { it.weight }.toDouble()
            if (total == 0.0) return options.associate { it.id to 0.0 }
            return options.associate { it.id to it.weight / total }
        }

    /** Normalized percentages (0–100) derived from option weights. */
    val normalizedPercentages: Map<Long, Int>
        get() = normalizedProbabilities.mapValues { (_, v) -> Math.round(v * 100).toInt() }
}
