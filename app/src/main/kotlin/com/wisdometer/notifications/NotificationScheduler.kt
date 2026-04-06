package com.wisdometer.notifications

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val workManager = WorkManager.getInstance(context)

    fun schedule(predictionId: Long, reminderAtMs: Long, question: String) {
        val delay = reminderAtMs - System.currentTimeMillis()
        if (delay <= 0) return

        val data = workDataOf(
            ReminderWorker.KEY_PREDICTION_ID to predictionId,
            ReminderWorker.KEY_QUESTION to question,
        )
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("reminder_$predictionId")
            .build()

        workManager.enqueueUniqueWork(
            "reminder_$predictionId",
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun cancel(predictionId: Long) {
        workManager.cancelUniqueWork("reminder_$predictionId")
    }

    fun cancelAll() {
        workManager.cancelAllWork()
    }
}
