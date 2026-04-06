package com.wisdometer.notifications

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor() {
    fun cancelAll() = Unit
    fun schedule(predictionId: Long, reminderAtMs: Long, question: String) = Unit
    fun cancel(predictionId: Long) = Unit
}
