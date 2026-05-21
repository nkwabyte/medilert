@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.nkwabyte.medilert.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.data.repository.CareRelationshipRepository
import com.nkwabyte.medilert.data.service.CareRelationshipService
import com.nkwabyte.medilert.model.DoseStatus
import com.nkwabyte.medilert.model.Medication
import com.nkwabyte.medilert.model.MedicationSchedule
import com.nkwabyte.medilert.model.User
import com.nkwabyte.medilert.model.UserRole
import com.nkwabyte.medilert.ui.components.DashboardTab
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.math.ceil

data class CaregiverWeekStats(val taken: Int, val missed: Int, val upcoming: Int, val total: Int, val adherence: Int)
data class CaregiverDayStats(val taken: Int, val missed: Int, val upcoming: Int, val total: Int, val adherence: Int)
data class WeekDayInfo(val date: String, val dayLabel: String, val dayNumber: Int, val hasDoses: Boolean, val isToday: Boolean, val isSelected: Boolean, val adherence: Int)
data class TodayDoseInfo(val schedule: MedicationSchedule, val frequency: String, val sideEffects: String, val intakeTitle: String, val weekDots: List<Boolean>)

class CaregiverViewModel(
    private val careService: CareRelationshipService = CareRelationshipService(),
    private val repository: CareRelationshipRepository = CareRelationshipRepository()
) : ViewModel() {

    val assignedPatients: StateFlow<List<User>> = careService.assignedPatientProfilesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _allPatients = MutableStateFlow<List<User>>(emptyList())
    val allPatients: StateFlow<List<User>> = _allPatients.asStateFlow()

    private var patientDirectoryJob: Job? = null

    val availablePatients: StateFlow<List<User>> = combine(allPatients, assignedPatients) { all, assigned ->
        val assignedIds = assigned.map { it.id }.toSet()
        all.filter { it.id !in assignedIds }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedPatientId = MutableStateFlow<String?>(null)

    private val _selectedDate = MutableStateFlow(
        Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
    )

    val selectedPatient: StateFlow<User?> = combine(assignedPatients, _selectedPatientId) { patients, id ->
        if (id != null) patients.find { it.id == id } else patients.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val selectedDayDisplay: StateFlow<Pair<String, String>> = _selectedDate.map { dateStr ->
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        val localDate = LocalDate.parse(dateStr)
        val label = if (dateStr == today) "Today" else localDate.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        val monthName = localDate.month.name.lowercase().replaceFirstChar { it.uppercase() }
        Pair(label, "$monthName ${localDate.dayOfMonth}, ${localDate.year}")
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000),
        run {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val monthName = today.month.name.lowercase().replaceFirstChar { it.uppercase() }
            Pair("Today", "$monthName ${today.dayOfMonth}, ${today.year}")
        }
    )

    val selectedPatientSchedule: StateFlow<List<MedicationSchedule>> = selectedPatient
        .flatMapLatest { patient ->
            if (patient == null) flowOf(emptyList())
            else combine(
                repository.patientMedicationsFlow(patient.id),
                repository.patientDoseRecordsFlow(patient.id)
            ) { medications, doseRecords -> buildScheduleHistory(medications, doseRecords) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val weeklyStats: StateFlow<CaregiverWeekStats> = selectedPatientSchedule
        .map { computeWeeklyStats(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CaregiverWeekStats(0, 0, 0, 0, 0))

    val selectedDateStats: StateFlow<CaregiverDayStats> = combine(
        selectedPatientSchedule, _selectedDate
    ) { schedules, dateStr -> computeDayStats(schedules, dateStr) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CaregiverDayStats(0, 0, 0, 0, 0))

    val weekDaySummary: StateFlow<List<WeekDayInfo>> = combine(
        selectedPatientSchedule, _selectedDate
    ) { schedules, selectedDate -> buildWeekDaySummary(schedules, selectedDate) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val todaySchedule: StateFlow<List<MedicationSchedule>> = combine(
        selectedPatientSchedule, _selectedDate
    ) { schedules, dateStr ->
        schedules.filter { it.date == dateStr }.sortedBy { it.scheduledTime }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedPatientMedications: StateFlow<List<Medication>> = selectedPatient
        .flatMapLatest { patient ->
            if (patient == null) flowOf(emptyList())
            else repository.patientMedicationsFlow(patient.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val todayScheduleEnriched: StateFlow<List<TodayDoseInfo>> = combine(
        selectedPatientSchedule, selectedPatientMedications, _selectedDate
    ) { allSchedules, medications, selectedDate ->
        buildTodayScheduleEnriched(medications, allSchedules, selectedDate)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _activeTab = MutableStateFlow(DashboardTab.HOME)
    val activeTab: StateFlow<DashboardTab> = _activeTab.asStateFlow()

    fun setActiveTab(tab: DashboardTab) { _activeTab.value = tab }

    private val _assigningIds = MutableStateFlow<Set<String>>(emptySet())
    val assigningIds: StateFlow<Set<String>> = _assigningIds.asStateFlow()

    private val _assignmentMessage = MutableStateFlow<String?>(null)
    val assignmentMessage: StateFlow<String?> = _assignmentMessage.asStateFlow()

    fun selectPatient(patientId: String) {
        _selectedPatientId.value = patientId
        _selectedDate.value = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
    }

    fun selectDate(dateStr: String) { _selectedDate.value = dateStr }

    fun loadPatientDirectory(callerRole: UserRole) {
        patientDirectoryJob?.cancel()
        patientDirectoryJob = viewModelScope.launch {
            careService.allPatientProfilesFlow(callerRole).collect { patients ->
                _allPatients.value = patients
            }
        }
    }

    fun assignPatient(patient: User, callerRole: UserRole, caregiverName: String) {
        if (patient.id.isBlank()) return
        viewModelScope.launch {
            _assigningIds.value = _assigningIds.value + patient.id
            val result = careService.assignPatient(
                patientId = patient.id,
                callerRole = callerRole,
                caregiverName = caregiverName,
                patientName = patient.name
            )
            _assigningIds.value = _assigningIds.value - patient.id
            _assignmentMessage.value = when (result) {
                is FirebaseResult.Success -> "${patient.name} added"
                is FirebaseResult.Error -> result.message
                FirebaseResult.Loading -> null
            }
        }
    }

    fun clearAssignmentMessage() { _assignmentMessage.value = null }

    private fun buildScheduleHistory(medications: List<Medication>, doseRecords: List<MedicationSchedule>): List<MedicationSchedule> {
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.todayIn(tz)
        val windowStart = today.minus(90, DateTimeUnit.DAY)
        val windowEnd = today.plus(30, DateTimeUnit.DAY)

        val generated = medications.flatMap { med ->
            val medStart = if (med.startDate.isNotEmpty()) LocalDate.parse(med.startDate) else windowStart
            val medEnd = resolveEndDate(med, medStart)
            val from = if (medStart > windowStart) medStart else windowStart
            val to = if (medEnd < windowEnd) medEnd else windowEnd
            if (from > to) emptyList()
            else generateScheduleForRange(listOf(med), from, to)
        }

        val recordById = doseRecords.associateBy { it.id }
        val overlaid = generated.map { slot -> recordById[slot.id] ?: slot }
        val overlaidIds = overlaid.map { it.id }.toSet()
        val olderHistory = doseRecords.filter { it.id !in overlaidIds }
        return (overlaid + olderHistory).sortedByDescending { it.date }
    }

    private fun resolveEndDate(medication: Medication, startDate: LocalDate): LocalDate {
        if (medication.endDate.isNotEmpty()) return LocalDate.parse(medication.endDate)
        val dailyUnits = medication.intakes.sumOf { it.dose }.coerceAtLeast(1)
        val daysPerCycle = when (medication.frequency) { "Every other day" -> 2; "Weekly" -> 7; else -> 1 }
        val daysSupply = (ceil(medication.currentInventory.toDouble() / dailyUnits) * daysPerCycle).toInt().coerceAtLeast(1)
        return startDate.plus(daysSupply - 1, DateTimeUnit.DAY)
    }

    private fun generateScheduleForRange(medications: List<Medication>, from: LocalDate, to: LocalDate): List<MedicationSchedule> {
        val schedules = mutableListOf<MedicationSchedule>()
        var current = from
        while (current <= to) {
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
                        schedules.add(MedicationSchedule(
                            id = "${med.id}_${dateStr}_$index", medicationId = med.id,
                            medicationName = med.name, date = dateStr, scheduledTime = intake.time,
                            dose = intake.dose, unit = med.unit, status = DoseStatus.UPCOMING
                        ))
                    }
                }
            }
            current = current.plus(1, DateTimeUnit.DAY)
        }
        return schedules
    }

    private fun buildTodayScheduleEnriched(medications: List<Medication>, allSchedules: List<MedicationSchedule>, selectedDate: String): List<TodayDoseInfo> {
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.todayIn(tz)
        val monday = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
        val weekDates = (0..5).map { offset -> monday.plus(offset, DateTimeUnit.DAY).toString() }
        val scheduleById = allSchedules.associateBy { it.id }
        val medById = medications.associateBy { it.id }
        val todaySlots = allSchedules.filter { it.date == selectedDate }.sortedBy { it.scheduledTime }
        return todaySlots.map { slot ->
            val med = medById[slot.medicationId]
            val intakeIndex = slot.id.substringAfterLast("_").toIntOrNull() ?: 0
            val intake = med?.intakes?.getOrNull(intakeIndex)
            val weekDots = weekDates.map { date ->
                scheduleById["${slot.medicationId}_${date}_${intakeIndex}"]?.status == DoseStatus.TAKEN
            }
            val frequencyText = when (med?.frequency) {
                "Once daily", "Twice daily", "Three times daily", "Four times daily" -> "Everyday"
                "Every other day" -> "Every other day"
                "Weekly" -> "Weekly"
                else -> med?.frequency ?: ""
            }
            TodayDoseInfo(
                schedule = slot, frequency = frequencyText, sideEffects = med?.sideEffects ?: "",
                intakeTitle = intake?.title.orEmpty().ifEmpty { timeOfDayLabel(slot.scheduledTime) },
                weekDots = weekDots
            )
        }
    }

    private fun timeOfDayLabel(time: String): String {
        val isPM = time.contains("PM", ignoreCase = true)
        val hour = time.split(":").firstOrNull()?.toIntOrNull() ?: 0
        val h24 = if (isPM && hour != 12) hour + 12 else if (!isPM && hour == 12) 0 else hour
        return when { h24 < 12 -> "Morning"; h24 < 17 -> "Afternoon"; else -> "Evening" }
    }

    private fun computeDayStats(schedules: List<MedicationSchedule>, dateStr: String): CaregiverDayStats {
        val day = schedules.filter { it.date == dateStr }
        val taken = day.count { it.status == DoseStatus.TAKEN }
        val missed = day.count { it.status == DoseStatus.MISSED }
        val upcoming = day.count { it.status == DoseStatus.UPCOMING }
        val total = day.size
        val adherence = if (total > 0) (taken * 100) / total else 0
        return CaregiverDayStats(taken, missed, upcoming, total, adherence)
    }

    private fun computeWeeklyStats(schedules: List<MedicationSchedule>): CaregiverWeekStats {
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.todayIn(tz)
        val monday = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
        val weekDates = (0..6).map { offset -> monday.plus(offset, DateTimeUnit.DAY).toString() }.toSet()
        val weekSchedules = schedules.filter { it.date in weekDates }
        val taken = weekSchedules.count { it.status == DoseStatus.TAKEN }
        val missed = weekSchedules.count { it.status == DoseStatus.MISSED }
        val upcoming = weekSchedules.count { it.status == DoseStatus.UPCOMING }
        val total = weekSchedules.size
        val adherence = if (total > 0) (taken * 100) / total else 0
        return CaregiverWeekStats(taken, missed, upcoming, total, adherence)
    }

    private fun buildWeekDaySummary(schedules: List<MedicationSchedule>, selectedDate: String): List<WeekDayInfo> {
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.todayIn(tz)
        val todayStr = today.toString()
        val monday = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
        val dayLabels = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
        return (0..6).map { offset ->
            val date = monday.plus(offset, DateTimeUnit.DAY)
            val dateStr = date.toString()
            val daySchedules = schedules.filter { it.date == dateStr }
            val taken = daySchedules.count { it.status == DoseStatus.TAKEN }
            val total = daySchedules.size
            val adherence = if (total > 0) (taken * 100) / total else 0
            WeekDayInfo(
                date = dateStr, dayLabel = dayLabels[offset], dayNumber = date.dayOfMonth,
                hasDoses = daySchedules.isNotEmpty(), isToday = dateStr == todayStr,
                isSelected = dateStr == selectedDate, adherence = adherence
            )
        }
    }
}
