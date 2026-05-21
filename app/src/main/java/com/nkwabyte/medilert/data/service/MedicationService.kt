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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

/**
 * MedicationService owns all medication business logic.
 *
 * Data model:
 *  - Firestore `medications` collection: one document per prescription definition.
 *  - Firestore `schedules` collection: one document per ACTIONED dose only
 *    (TAKEN / MISSED / SKIPPED). UPCOMING entries are never written to Firestore;
 *    they are derived in-memory from the medication definition on every read.
 *
 * ACID guarantees:
 *  - addMedication  → 1 Firestore write (atomic by definition)
 *  - updateMedication → 1 Firestore write (atomic by definition)
 *  - markDose*      → 1 Firestore write (atomic by definition)
 *  - deleteMedication → medication doc + N dose records; chunked batches, where
 *    N is small (only actioned doses), so a single batch almost always suffices.
 */
class MedicationService(
    private val repository: MedicationRepository = MedicationRepository()
) {
    // ── Live Firestore streams ────────────────────────────────────────────────

    /** Live list of all medication definitions for the current user. */
    val medications: Flow<List<Medication>> = repository.medicationsFlow()

    /**
     * Combined live schedule:
     *  1. Derives UPCOMING slots in-memory from medication definitions for a
     *     rolling window (past 90 days → next 30 days), respecting each
     *     medication's start date and supply duration.
     *  2. Overlays actioned dose records (TAKEN / MISSED / SKIPPED) from Firestore
     *     onto the generated slots — same deterministic ID, so the upsert is exact.
     *  3. Appends any historical dose records that fall outside the window.
     *
     * Emits a new list whenever either the medication definitions or dose records
     * change in Firestore.
     */
    val scheduleHistory: Flow<List<MedicationSchedule>> = combine(
        repository.medicationsFlow(),
        repository.doseRecordsFlow()
    ) { medications, doseRecords ->
        val windowStart = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -90) }.time
        val windowEnd = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }.time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Generate expected schedule for each medication within its valid range,
        // clamped to the rolling window so we don't generate unbounded future slots.
        val generated = medications.flatMap { med ->
            val medStart = if (med.startDate.isNotEmpty())
                dateFormat.parse(med.startDate) ?: windowStart
            else windowStart
            val medEnd = resolveEndDate(med, medStart)
            val from = if (medStart.after(windowStart)) medStart else windowStart
            val to = if (medEnd.before(windowEnd)) medEnd else windowEnd
            if (from.after(to)) emptyList()
            else generateScheduleForDateRange(listOf(med), from, to)
        }

        // Overlay: replace a generated UPCOMING slot with its Firestore dose record
        // if the user has already acted on it (same deterministic ID).
        val recordById = doseRecords.associateBy { it.id }
        val overlaid = generated.map { slot -> recordById[slot.id] ?: slot }

        // Include dose records that fall outside the generation window (old history).
        val overlaidIds = overlaid.map { it.id }.toSet()
        val olderHistory = doseRecords.filter { it.id !in overlaidIds }

        (overlaid + olderHistory).sortedByDescending { it.date }
    }

    // ── Derived adherence streams ─────────────────────────────────────────────

    /** Today's adherence as a 0–100 Int (TAKEN / total scheduled today). */
    val todayAdherence: Flow<Int> = scheduleHistory.map { schedules ->
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val today = schedules.filter { it.date == todayKey }
        (computeAdherence(today) * 100).toInt()
    }

    /** (taken, missed, upcoming) counts for today. */
    val todayCounts: Flow<Triple<Int, Int, Int>> = scheduleHistory.map { schedules ->
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val today = schedules.filter { it.date == todayKey }
        Triple(
            today.count { it.status == DoseStatus.TAKEN },
            today.count { it.status == DoseStatus.MISSED },
            today.count { it.status == DoseStatus.UPCOMING }
        )
    }

    // ── Adherence calculation ─────────────────────────────────────────────────

    fun computeAdherence(schedules: List<MedicationSchedule>): Float {
        val completed = schedules.filter { it.status != DoseStatus.UPCOMING }
        if (completed.isEmpty()) return 0f
        val taken = completed.count { it.status == DoseStatus.TAKEN }
        return taken.toFloat() / completed.size.toFloat()
    }

    // ── Schedule generation ───────────────────────────────────────────────────

    fun generateScheduleForDateRange(
        medications: List<Medication>,
        startDate: Date,
        endDate: Date
    ): List<MedicationSchedule> {
        val schedules = mutableListOf<MedicationSchedule>()
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val loopEnd = Calendar.getInstance()
        loopEnd.time = endDate

        while (calendar.time.before(loopEnd.time) || isSameDay(calendar, loopEnd)) {
            val dateStr = dateFormat.format(calendar.time)
            medications.forEach { medication ->
                if (medication.startDate.isEmpty()) return@forEach
                val medStart = dateFormat.parse(medication.startDate) ?: return@forEach
                val medStartCal = Calendar.getInstance().also { it.time = medStart }

                if (!calendar.time.before(medStart) || isSameDay(calendar, medStartCal)) {
                    val medEnd = if (medication.endDate.isNotEmpty())
                        dateFormat.parse(medication.endDate) else null
                    val medEndCal = medEnd?.let { Calendar.getInstance().also { c -> c.time = it } }

                    if (medEndCal == null || !calendar.time.after(medEndCal.time) || isSameDay(
                            calendar,
                            medEndCal
                        )
                    ) {
                        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                        val daysBetween = TimeUnit.MILLISECONDS.toDays(
                            calendar.timeInMillis - medStartCal.timeInMillis
                        )
                        val shouldAdd = when (medication.frequency) {
                            "Once daily", "Twice daily",
                            "Three times daily", "Four times daily" -> true

                            "Every other day" -> daysBetween % 2 == 0L
                            "Weekly" -> dayOfWeek == medStartCal.get(Calendar.DAY_OF_WEEK)
                            else -> false // "As needed" — not pre-scheduled
                        }
                        if (shouldAdd) {
                            medication.intakes.forEachIndexed { index, intake ->
                                schedules.add(createSchedule(medication, dateStr, intake, index))
                            }
                        }
                    }
                }
            }
            calendar.add(Calendar.DATE, 1)
        }
        return schedules
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean =
        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)

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

    /**
     * Resolves the effective end date for schedule generation.
     *
     * If the medication has an explicit endDate, use it.
     * Otherwise calculate supply duration from inventory:
     *   daysSupply = ceil(currentInventory / dailyUnits) × daysPerCycle
     *
     * daysPerCycle: 1 for all daily frequencies, 2 for every-other-day, 7 for weekly.
     * dailyUnits: sum of all intake doses per application cycle.
     */
    private fun resolveEndDate(medication: Medication, startDate: Date): Date {
        if (medication.endDate.isNotEmpty()) {
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .parse(medication.endDate) ?: startDate
        }
        val dailyUnits = medication.intakes.sumOf { it.dose }.coerceAtLeast(1)
        val daysPerCycle = when (medication.frequency) {
            "Every other day" -> 2
            "Weekly" -> 7
            else -> 1
        }
        val daysSupply = (ceil(medication.currentInventory.toDouble() / dailyUnits) * daysPerCycle)
            .toInt().coerceAtLeast(1)
        return Calendar.getInstance().apply {
            time = startDate
            add(Calendar.DAY_OF_YEAR, daysSupply - 1)
        }.time
    }

    // ── Medication CRUD ───────────────────────────────────────────────────────

    /** Single atomic Firestore write. No schedule documents are pre-generated. */
    suspend fun addMedication(medication: Medication): FirebaseResult<Unit> =
        repository.addMedication(medication)

    /**
     * Single atomic Firestore write. Past dose records are preserved as-is;
     * future UPCOMING slots are re-derived automatically from the new definition.
     */
    suspend fun updateMedication(medication: Medication): FirebaseResult<Unit> =
        repository.updateMedication(medication)

    suspend fun deleteMedication(medicationId: String): FirebaseResult<Unit> =
        repository.deleteMedication(medicationId)

    // ── Dose recording ────────────────────────────────────────────────────────

    suspend fun markDoseTaken(schedule: MedicationSchedule): FirebaseResult<Unit> {
        val now = SimpleDateFormat("h:mm a", Locale.ENGLISH).format(Date())
        return repository.recordDoseStatus(schedule, DoseStatus.TAKEN, now)
    }

    suspend fun markDoseMissed(schedule: MedicationSchedule): FirebaseResult<Unit> =
        repository.recordDoseStatus(schedule, DoseStatus.MISSED)

    suspend fun markDoseSkipped(schedule: MedicationSchedule): FirebaseResult<Unit> =
        repository.recordDoseStatus(schedule, DoseStatus.SKIPPED)

    // ── Helpers ───────────────────────────────────────────────────────────────

    fun todayDateKey(): String =
        SimpleDateFormat("EEEE, MMMM d", Locale.ENGLISH).format(Date())
}
