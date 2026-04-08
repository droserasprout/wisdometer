package com.wisdometer.data.dao

import androidx.room.*
import com.wisdometer.data.model.Prediction
import com.wisdometer.data.model.PredictionOption
import com.wisdometer.data.model.PredictionWithOptions
import kotlinx.coroutines.flow.Flow

@Dao
interface PredictionDao {

    @Transaction
    @Query("""
        SELECT * FROM predictions
        ORDER BY
          CASE
            WHEN reminderAt IS NOT NULL AND resolvedAt IS NULL AND reminderAt > :nowMs THEN 0
            WHEN resolvedAt IS NULL THEN 1
            ELSE 2
          END ASC,
          CASE
            WHEN reminderAt IS NOT NULL AND resolvedAt IS NULL AND reminderAt > :nowMs
            THEN reminderAt
            ELSE NULL
          END ASC,
          createdAt DESC
    """)
    fun getAllWithOptions(nowMs: Long): Flow<List<PredictionWithOptions>>

    @Transaction
    @Query("SELECT * FROM predictions WHERE id = :id")
    fun getWithOptionsById(id: Long): Flow<PredictionWithOptions?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPrediction(prediction: Prediction): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertOptions(options: List<PredictionOption>)

    @Update
    suspend fun updatePrediction(prediction: Prediction)

    @Query("DELETE FROM prediction_options WHERE predictionId = :predictionId")
    suspend fun deleteOptionsForPrediction(predictionId: Long)

    @Delete
    suspend fun deletePrediction(prediction: Prediction)

    @Transaction
    suspend fun upsertPredictionWithOptions(prediction: Prediction, options: List<PredictionOption>): Long {
        val id = if (prediction.id == 0L) {
            insertPrediction(prediction)
        } else {
            updatePrediction(prediction)
            prediction.id
        }
        deleteOptionsForPrediction(id)
        insertOptions(options.map { it.copy(predictionId = id) })
        return id
    }

    // For import: check for duplicates by question + createdAt
    @Query("SELECT COUNT(*) FROM predictions WHERE question = :question AND createdAt = :createdAt")
    suspend fun countByQuestionAndCreatedAt(question: String, createdAt: Long): Int

    @Query("SELECT * FROM prediction_options WHERE predictionId = :predictionId")
    suspend fun getOptionsForPrediction(predictionId: Long): List<PredictionOption>

    @Query("UPDATE predictions SET outcomeOptionId = :outcomeOptionId WHERE id = :predictionId")
    suspend fun updateOutcomeOptionId(predictionId: Long, outcomeOptionId: Long)

    @Transaction
    @Query("SELECT * FROM predictions WHERE resolvedAt IS NOT NULL")
    suspend fun getAllResolvedWithOptions(): List<PredictionWithOptions>
}
