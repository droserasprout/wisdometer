package com.wisdometer.export

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExportFile(
    val version: Int = 1,
    @SerialName("exported_at") val exportedAt: String,
    val predictions: List<ExportedPrediction>,
)

@Serializable
data class ExportedPrediction(
    val id: Long,
    val question: String,
    val description: String = "",
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("reminder_at") val reminderAt: String? = null,
    @SerialName("resolved_at") val resolvedAt: String? = null,
    @SerialName("outcome_option_id") val outcomeOptionId: Long? = null,
    val tags: List<String>,
    val options: List<ExportedOption>,
)

@Serializable
data class ExportedOption(
    val label: String,
    val probability: Int,
    @SerialName("sort_order") val sortOrder: Int,
)
