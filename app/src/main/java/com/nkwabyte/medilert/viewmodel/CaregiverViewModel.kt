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
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

data class CaregiverWeekStats(
    val taken: Int,
    val missed: Int,
    val upcoming: Int,
    val total: Int,
    val adherence: Int
)

data class CaregiverDayStats(
    val taken: Int,
    val missed: Int,
    val upcoming: Int,
    val total: Int,
    val adherence: Int
)

data class WeekDayInfo(
    val date: String,
    val dayLabel: String,
    val dayNumber: Int,
    val hasDoses: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean,
    val adherence: Int
)

data class TodayDoseInfo(
    val schedule: MedicationSchedule,
    val frequency: String,
    val sideEffects: String,
    val intakeTitle: String,
    val weekDots: List<Boolean>
)

/**
 * Drives the caregiver History tab.
 *
 * Mirrors MedicationService.scheduleHistory but scoped to a selected patient:
 *  1. Streams the patient's medication definitions and actioned dose records.
 *  2. Generates UPCOMING slots in-memory (never reads them from Firestore).
 *  3. Overlays actioned records by deterministic ID.
 */
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
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )

    // Auto-selects first patient when none is explicitly chosen
    val selectedPatient: StateFlow<User?> = combine(assignedPatients, _selectedPatientId) { patients, id ->
        if (id != null) patients.find { it.id == id } else patients.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val selectedDayDisplay: StateFlow<Pair<String, String>> = _selectedDate
        .map { dateStr ->
            val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = df.format(Date())
            val displayDf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            val dayNameDf = SimpleDateFormat("EEEE", Locale.getDefault())
            val date = df.parse(dateStr) ?: Date()
            val label = if (dateStr == today) "Today" else dayNameDf.format(date)
            Pair(label, displayDf.format(date))
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            Pair("Today", SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date()))
        )

    val selectedPatientSchedule: StateFlow<List<MedicationSchedule>> = selectedPatient
        .flatMapLatest { patient ->
            if (patient == null) flowOf(emptyList())
            else combine(
                repository.patientMedicationsFlow(patient.id),
                repository.patientDoseRecordsFlow(patient.id)
            ) { medications, doseRecords ->
                buildScheduleHistory(medications, doseRecords)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val weeklyStats: StateFlow<CaregiverWeekStats> = selectedPatientSchedule
        .map { computeWeeklyStats(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CaregiverWeekStats(0, 0, 0, 0, 0))

    val selectedDateStats: StateFlow<CaregiverDayStats> = combine(
        selectedPatientSchedule, _selectedDate
    ) { schedules, dateStr ->
        computeDayStats(schedules, dateStr)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CaregiverDayStats(0, 0, 0, 0, 0))

    val weekDaySummary: StateFlow<List<WeekDayInfo>> = combine(
        selectedPatientSchedule, _selectedDate
    ) { schedules, selectedDate ->
        buildWeekDaySummary(schedules, selectedDate)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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
        selectedPatientSchedule,
        selectedPatientMedications,
        _selectedDate
    ) { allSchedules, medications, selectedDate ->
        buildTodayScheduleEnriched(medications, allSchedules, selectedDate)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _activeTab = MutableStateFlow(DashboardTab.HOME)
    val activeTab: StateFlow<DashboardTab> = _activeTab.asStateFlow()

    fun setActiveTab(tab: DashboardTab) {
        _activeTab.value = tab
    }

    private val _assigningIds = MutableStateFlow<Set<String>>(emptySet())
    val assigningIds: StateFlow<Set<String>> = _assigningIds.asStateFlow()

    private val _assignmentMessage = MutableStateFlow<String?>(null)
    val assignmentMessage: StateFlow<String?> = _assignmentMessage.asStateFlow()

    fun selectPatient(patientId: String) {
        _selectedPatientId.value = patientId
        _selectedDate.value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun selectDate(dateStr: String) {
        _selectedDate.value = dateStr
    }

    fun loadPatientDirectory(callerRole: UserRole) {
        android.util.Log.d("PatientDir", "VM: loadPatientDirectory called, role=$callerRole")
        patientDirectoryJob?.cancel()
        patientDirectoryJob = viewModelScope.launch {
            android.util.Log.d("PatientDir", "VM: Starting collection from service...")
            careService.allPatientProfilesFlow(callerRole).collect { patients ->
                android.util.Log.d("PatientDir", "VM: Received ${patients.size} patients from service")
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

    fun clearAssignmentMessage() {
        _assignmentMessage.value = null
    }

    // ── Schedule history construction (mirrors MedicationService logic) ────────

    private fun buildScheduleHistory(
        medications: List<Medication>,
        doseRecords: List<MedicationSchedule>
    ): List<MedicationSchedule> {
        val windowStart = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -90) }.time
        val windowEnd = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }.time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val generated = medications.flatMap { med ->
            val medStart = if (med.startDate.isNotEmpty())
                dateFormat.parse(med.startDate) ?: windowStart
            else windowStart
            val medEnd = resolveEndDate(med, medStart)
            val from = if (medStart.after(windowStart)) medStart else windowStart
            val to = if (medEnd.before(windowEnd)) medEnd else windowEnd
            if (from.after(to)) emptyList()
            else generateScheduleForRange(listOf(med), from, to)
        }

        val recordById = doseRecords.associateBy { it.id }
        val overlaid = generated.map { slot -> recordById[slot.id] ?: slot }
        val overlaidIds = overlaid.map { it.id }.toSet()
        val olderHistory = doseRecords.filter { it.id !in overlaidIds }
        return (overlaid + olderHistory).sortedByDescending { it.date }
    }

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

    private fun generateScheduleForRange(
        medications: List<Medication>,
        from: Date,
        to: Date
    ): List<MedicationSchedule> {
        val schedules = mutableListOf<MedicationSchedule>()
        val cal = Calendar.getInstance().apply { time = from }
        val loopEnd = Calendar.getInstance().apply { time = to }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        while (!cal.time.after(loopEnd.time)) {
            val dateStr = dateFormat.format(cal.time)
            medications.forEach { med ->
                if (med.startDate.isEmpty()) return@forEach
                val medStart = dateFormat.parse(med.startDate) ?: return@forEach
                val medStartCal = Calendar.getInstance().apply { time = medStart }
                if (cal.time.before(medStart) && !isSameDay(cal, medStartCal)) return@forEach

                val medEnd = if (med.endDate.isNotEmpty()) dateFormat.parse(med.endDate) else null
                val medEndCal = medEnd?.let { Calendar.getInstance().apply { time = it } }
                if (medEndCal != null && cal.time.after(medEndCal.time) && !isSameDay(cal, medEndCal)) return@forEach

                val daysBetween = TimeUnit.MILLISECONDS.toDays(cal.timeInMillis - medStartCal.timeInMillis)
                val shouldAdd = when (med.frequency) {
                    "Once daily", "Twice daily",
                    "Three times daily", "Four times daily" -> true
                    "Every other day" -> daysBetween % 2 == 0L
                    "Weekly" -> cal.get(Calendar.DAY_OF_WEEK) == medStartCal.get(Calendar.DAY_OF_WEEK)
                    else -> false
                }
                if (shouldAdd) {
                    med.intakes.forEachIndexed { index, intake ->
                        schedules.add(
                            MedicationSchedule(
                                id = "${med.id}_${dateStr}_$index",
                                medicationId = med.id,
                                medicationName = med.name,
                                date = dateStr,
                                scheduledTime = intake.time,
                                dose = intake.dose,
                                unit = med.unit,
                                status = DoseStatus.UPCOMING
                            )
                        )
                    }
                }
            }
            cal.add(Calendar.DATE, 1)
        }
        return schedules
    }

    private fun isSameDay(c1: Calendar, c2: Calendar) =
        c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)

    private fun buildTodayScheduleEnriched(
        medications: List<Medication>,
        allSchedules: List<MedicationSchedule>,
        selectedDate: String
    ): List<TodayDoseInfo> {
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todaySlots = allSchedules.filter { it.date == selectedDate }.sortedBy { it.scheduledTime }
        val monday = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) }
        val weekDates = (0..5).map { offset ->
            val cal = monday.clone() as Calendar
            cal.add(Calendar.DAY_OF_YEAR, offset)
            df.format(cal.time)
        }
        val scheduleById = allSchedules.associateBy { it.id }
        val medById = medications.associateBy { it.id }
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
                schedule = slot,
                frequency = frequencyText,
                sideEffects = med?.sideEffects ?: "",
                intakeTitle = intake?.title.orEmpty().ifEmpty { timeOfDayLabel(slot.scheduledTime) },
                weekDots = weekDots
            )
        }
    }

    private fun timeOfDayLabel(time: String): String {
        val isPM = time.contains("PM", ignoreCase = true)
        val hour = time.split(":").firstOrNull()?.toIntOrNull() ?: 0
        val h24 = if (isPM && hour != 12) hour + 12 else if (!isPM && hour == 12) 0 else hour
        return when {
            h24 < 12 -> "Morning"
            h24 < 17 -> "Afternoon"
            else -> "Evening"
        }
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

    private fun buildWeekDaySummary(
        schedules: List<MedicationSchedule>,
        selectedDate: String
    ): List<WeekDayInfo> {
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = df.format(Date())
        val monday = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) }
        val dayLabels = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
        return (0..6).map { offset ->
            val cal = monday.clone() as Calendar
            cal.add(Calendar.DAY_OF_YEAR, offset)
            val dateStr = df.format(cal.time)
            val daySchedules = schedules.filter { it.date == dateStr }
            val taken = daySchedules.count { it.status == DoseStatus.TAKEN }
            val total = daySchedules.size
            val adherence = if (total > 0) (taken * 100) / total else 0
            WeekDayInfo(
                date = dateStr,
                dayLabel = dayLabels[offset],
                dayNumber = cal.get(Calendar.DAY_OF_MONTH),
                hasDoses = daySchedules.isNotEmpty(),
                isToday = dateStr == todayStr,
                isSelected = dateStr == selectedDate,
                adherence = adherence
            )
        }
    }

    private fun computeWeeklyStats(schedules: List<MedicationSchedule>): CaregiverWeekStats {
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val mondayCal = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) }
        val sundayCal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            add(Calendar.DAY_OF_YEAR, 6)
        }
        val weekStart = df.format(mondayCal.time)
        val weekEnd = df.format(sundayCal.time)
        val week = schedules.filter { it.date in weekStart..weekEnd }
        val taken = week.count { it.status == DoseStatus.TAKEN }
        val missed = week.count { it.status == DoseStatus.MISSED }
        val upcoming = week.count { it.status == DoseStatus.UPCOMING }
        val total = week.size
        val adherence = if (total > 0) (taken * 100) / total else 0
        return CaregiverWeekStats(taken, missed, upcoming, total, adherence)
    }
}
