package com.wisdometer.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "prediction_options",
    foreignKeys = [
        ForeignKey(
            entity = Prediction::class,
            parentColumns = ["id"],
            childColumns = ["predictionId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("predictionId")],
)
data class PredictionOption(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val predictionId: Long,
    val label: String,
    val probability: Int,  // 0–100; all options for a prediction must sum to 100
    val sortOrder: Int,
)
