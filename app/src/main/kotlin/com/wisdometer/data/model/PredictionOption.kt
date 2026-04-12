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
    val weight: Int,  // 1–10; normalized to percentages for scoring and display
    val sortOrder: Int,
)
