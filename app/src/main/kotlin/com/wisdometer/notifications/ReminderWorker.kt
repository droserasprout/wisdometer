package com.wisdometer.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wisdometer.MainActivity
import com.wisdometer.REMINDER_CHANNEL_ID
import com.wisdometer.data.dao.PredictionDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val dao: PredictionDao,
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_PREDICTION_ID = "prediction_id"
        const val KEY_QUESTION = "question"
    }

    override suspend fun doWork(): Result {
        val predictionId = inputData.getLong(KEY_PREDICTION_ID, -1L)
        val question = inputData.getString(KEY_QUESTION) ?: return Result.failure()

        // Don't notify if already resolved
        val resolved = dao.getAllResolvedWithOptions()
        if (resolved.any { it.prediction.id == predictionId }) {
            return Result.success()
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("prediction_id", predictionId)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            predictionId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(applicationContext, REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Time to resolve")
            .setContentText("\"$question\"")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(predictionId.toInt(), notification)
        return Result.success()
    }
}
