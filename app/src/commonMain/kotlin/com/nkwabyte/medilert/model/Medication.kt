@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.nkwabyte.medilert.model

import kotlinx.serialization.Serializable

@Serializable
data class Medication(
    val id: String = kotlin.uuid.Uuid.random().toString(),
    val name: String = "",
    val frequency: String = "Once daily",
    val dose: Int = 1,
    val intakes: List<MedicationIntake> = emptyList(),
    val instructions: String = "",
    val remindRefill: Boolean = true,
    val currentInventory: Int = 30,
    val refillThreshold: Int = 10,
    val startDate: String = "",
    val endDate: String = "",
    val unit: String = "tablet(s)",
    val icon: Int = 0,
    val sideEffects: String = "",
    val notes: String = ""
)

@Serializable
data class MedicationIntake(
    val title: String = "",
    val time: String = "8:00 AM",
    val dose: Int = 1
)
