package com.nkwabyte.medilert.data.service

import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.data.repository.MedicationRepository
import com.nkwabyte.medilert.model.DoseStatus
import com.nkwabyte.medilert.model.Medication
import com.nkwabyte.medilert.model.MedicationIntake
import com.nkwabyte.medilert.model.MedicationSchedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.math.ceil

class MedicationService(
    private val repository: MedicationRepository = MedicationRepository()
) {
    val medications: Flow<List<Medication>> = repository.medicationsFlow()

    val scheduleHistory: Flow<List<MedicationSchedule>> = combine(
        repository.medicationsFlow(),
        repository.doseRecordsFlow()
    ) { medications, doseRecords ->
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.todayIn(tz)
        val windowStart = today.minus(90, DateTimeUnit.DAY)
        val windowEnd = today.plus(30, DateTimeUnit.DAY)

        val generated = medications.flatMap { med ->
            val medStart = if (med.startDate.isNotEmpty())
                LocalDate.parse(med.startDate)
            else windowStart
            val medEnd = resolveEndDate(med, medStart)
            val from = if (medStart > windowStart) medStart else windowStart
            val to = if (medEnd < windowEnd) medEnd else windowEnd
            if (from > to) emptyList()
            else generateScheduleForDateRange(listOf(med), from, to)
        }

        val recordById = doseRecords.associateBy { it.id }
        val overlaid = generated.map { slot -> recordById[slot.id] ?: slot }
        val overlaidIds = overlaid.map { it.id }.toSet()
        val olderHistory = doseRecords.filter { it.id !in overlaidIds }
        (overlaid + olderHistory).sortedByDescending { it.date }
    }

    val todayAdherence: Flow<Int> = scheduleHistory.map { schedules ->
        val todayKey = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        val today = schedules.filter { it.date == todayKey }
        (computeAdherence(today) * 100).toInt()
    }

    val todayCounts: Flow<Triple<Int, Int, Int>> = scheduleHistory.map { schedules ->
        val todayKey = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        val today = schedules.filter { it.date == todayKey }
        Triple(
            today.count { it.status == DoseStatus.TAKEN },
            today.count { it.status == DoseStatus.MISSED },
            today.count { it.status == DoseStatus.UPCOMING }
        )
    }

    fun computeAdherence(schedules: List<MedicationSchedule>): Float {
        val completed = schedules.filter { it.status != DoseStatus.UPCOMING }
        if (completed.isEmpty()) return 0f
        val taken = completed.count { it.status == DoseStatus.TAKEN }
        return taken.toFloat() / completed.size.toFloat()
    }

    fun generateScheduleForDateRange(
        medications: List<Medication>,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<MedicationSchedule> {
        val schedules = mutableListOf<MedicationSchedule>()
        var current = startDate
        while (current <= endDate) {
            val dateStr = current.toString()
            medications.forEach { med ->
                if (med.startDate.isEmpty()) return@forEach
                val medStart = LocalDate.parse(med.startDate)
                if (current < medStart) return@forEach

                val medEnd = if (med.endDate.isNotEmpty()) LocalDate.parse(med.endDate) else null
                if (medEnd != null && current > medEnd) return@forEach

                val daysBetween = (current.toEpochDays() - medStart.toEpochDays()).toLong()
                val shouldAdd = when (med.frequency) {
                    "Once daily", "Twice daily", "Three times daily", "Four times daily" -> true
                    "Every other day" -> daysBetween % 2 == 0L
                    "Weekly" -> current.dayOfWeek == medStart.dayOfWeek
                    else -> false
                }
                if (shouldAdd) {
                    med.intakes.forEachIndexed { index, intake ->
                        schedules.add(createSchedule(med, dateStr, intake, index))
                    }
                }
            }
            current = current.plus(1, DateTimeUnit.DAY)
        }
        return schedules
    }

    private fun createSchedule(
        medication: Medication,
        dateStr: String,
        intake: MedicationIntake,
        index: Int
    ): MedicationSchedule = MedicationSchedule(
        id = "${medication.id}_${dateStr}_$index",
        medicationId = medication.id,
        medicationName = medication.name,
        date = dateStr,
        scheduledTime = intake.time,
        dose = intake.dose,
        unit = medication.unit,
        status = DoseStatus.UPCOMING
    )

    private fun resolveEndDate(medication: Medication, startDate: LocalDate): LocalDate {
        if (medication.endDate.isNotEmpty()) {
            return LocalDate.parse(medication.endDate)
        }
        val dailyUnits = medication.intakes.sumOf { it.dose }.coerceAtLeast(1)
        val daysPerCycle = when (medication.frequency) {
            "Every other day" -> 2
            "Weekly" -> 7
            else -> 1
        }
        val daysSupply = (ceil(medication.currentInventory.toDouble() / dailyUnits) * daysPerCycle)
            .toInt().coerceAtLeast(1)
        return startDate.plus(daysSupply - 1, DateTimeUnit.DAY)
    }

    suspend fun addMedication(medication: Medication): FirebaseResult<Unit> =
        repository.addMedication(medication)

    suspend fun updateMedication(medication: Medication): FirebaseResult<Unit> =
        repository.updateMedication(medication)

    suspend fun deleteMedication(medicationId: String): FirebaseResult<Unit> =
        repository.deleteMedication(medicationId)

    suspend fun markDoseTaken(schedule: MedicationSchedule): FirebaseResult<Unit> {
        val now = Clock.System.now().toEpochMilliseconds().let {
            val totalSeconds = it / 1000
            val hour = ((totalSeconds / 3600) % 24).toInt()
            val minute = ((totalSeconds / 60) % 60).toInt()
            val isPM = hour >= 12
            val h12 = when { hour == 0 -> 12; hour > 12 -> hour - 12; else -> hour }
            "$h12:${minute.toString().padStart(2, '0')} ${if (isPM) "PM" else "AM"}"
        }
        return repository.recordDoseStatus(schedule, DoseStatus.TAKEN, now)
    }

    suspend fun markDoseMissed(schedule: MedicationSchedule): FirebaseResult<Unit> =
        repository.recordDoseStatus(schedule, DoseStatus.MISSED)

    suspend fun markDoseSkipped(schedule: MedicationSchedule): FirebaseResult<Unit> =
        repository.recordDoseStatus(schedule, DoseStatus.SKIPPED)

    fun todayDateKey(): String {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val dayName = today.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        val monthName = today.month.name.lowercase().replaceFirstChar { it.uppercase() }
        return "$dayName, $monthName ${today.dayOfMonth}"
    }
}
