package com.wisdometer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "predictions")
data class Prediction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val question: String,
    val createdAt: Instant = Instant.now(),
    val reminderAt: Instant? = null,
    val resolvedAt: Instant? = null,
    val outcomeOptionId: Long? = null,
    val tags: String = "",  // comma-separated, e.g. "career,finance"
)

val Prediction.tagList: List<String>
    get() = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
