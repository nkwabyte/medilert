package com.nkwabyte.medilert.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkwabyte.medilert.data.FirebaseResult
import com.nkwabyte.medilert.data.service.MedicationService
import com.nkwabyte.medilert.model.Medication
import com.nkwabyte.medilert.model.MedicationIntake
import com.nkwabyte.medilert.model.MedicationSchedule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MedicationUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class MedicationViewModel(
    private val medicationService: MedicationService = MedicationService()
) : ViewModel() {

    val medications: StateFlow<List<Medication>> = medicationService.medications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val scheduleHistory: StateFlow<List<MedicationSchedule>> = medicationService.scheduleHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val todayAdherence: StateFlow<Int> = medicationService.todayAdherence
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val todayCounts: StateFlow<Triple<Int, Int, Int>> = medicationService.todayCounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Triple(0, 0, 0))

    private val _uiState = MutableStateFlow(MedicationUiState())
    val uiState: StateFlow<MedicationUiState> = _uiState.asStateFlow()

    private val _draftMedication = MutableStateFlow(Medication())
    val draftMedication: StateFlow<Medication> = _draftMedication.asStateFlow()

    private val _draftFrequency = MutableStateFlow("Once daily")
    val draftFrequency: StateFlow<String> = _draftFrequency.asStateFlow()

    fun setDraftName(name: String) { _draftMedication.value = _draftMedication.value.copy(name = name) }
    fun updateDraftName(name: String) = setDraftName(name)

    fun setDraftFrequency(freq: String) {
        _draftFrequency.value = freq
        _draftMedication.value = _draftMedication.value.copy(frequency = freq)
    }
    fun updateDraftFrequency(freq: String) = setDraftFrequency(freq)

    fun setDraftMedication(med: Medication) {
        _draftMedication.value = med
        _draftFrequency.value = med.frequency
    }

    fun updateDraftMedication(updatedMedication: Medication) {
        _draftMedication.value = updatedMedication
        if (updatedMedication.frequency != _draftFrequency.value) {
            _draftFrequency.value = updatedMedication.frequency
        }
    }

    fun updateDraftIntakes(intakes: List<MedicationIntake>) {
        _draftMedication.value = _draftMedication.value.copy(intakes = intakes)
    }

    fun updateDraftInventory(current: Int, threshold: Int) {
        _draftMedication.value = _draftMedication.value.copy(
            currentInventory = current, refillThreshold = threshold
        )
    }

    fun updateDraftDates(startDate: String, endDate: String) {
        _draftMedication.value = _draftMedication.value.copy(startDate = startDate, endDate = endDate)
    }

    fun updateDraftInstructions(instructions: String) {
        _draftMedication.value = _draftMedication.value.copy(instructions = instructions)
    }

    fun startEditMedication(id: String) {
        val med = medications.value.find { it.id == id } ?: return
        setDraftMedication(med)
    }

    fun clearDraft() {
        _draftMedication.value = Medication()
        _draftFrequency.value = "Once daily"
    }

    fun saveDraftMedication() {
        viewModelScope.launch {
            _uiState.value = MedicationUiState(isLoading = true)
            when (val result = medicationService.addMedication(_draftMedication.value)) {
                is FirebaseResult.Success -> {
                    clearDraft()
                    _uiState.value = MedicationUiState(successMessage = "Medication saved")
                }
                is FirebaseResult.Error -> _uiState.value = MedicationUiState(errorMessage = result.message)
                else -> Unit
            }
        }
    }

    fun saveDraft() = saveDraftMedication()

    fun updateMedication(medication: Medication, onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = MedicationUiState(isLoading = true)
            when (val result = medicationService.updateMedication(medication)) {
                is FirebaseResult.Success -> {
                    _uiState.value = MedicationUiState(successMessage = "Medication updated")
                    onComplete()
                }
                is FirebaseResult.Error -> _uiState.value = MedicationUiState(errorMessage = result.message)
                else -> Unit
            }
        }
    }

    fun deleteMedication(medicationId: String) {
        viewModelScope.launch {
            when (val result = medicationService.deleteMedication(medicationId)) {
                is FirebaseResult.Error -> _uiState.value = MedicationUiState(errorMessage = result.message)
                else -> Unit
            }
        }
    }

    fun markDoseTaken(schedule: MedicationSchedule) {
        viewModelScope.launch { medicationService.markDoseTaken(schedule) }
    }

    fun markDoseMissed(schedule: MedicationSchedule) {
        viewModelScope.launch { medicationService.markDoseMissed(schedule) }
    }

    fun markDoseSkipped(schedule: MedicationSchedule) {
        viewModelScope.launch { medicationService.markDoseSkipped(schedule) }
    }

    fun getHistoryByDate(): Map<String, List<MedicationSchedule>> =
        scheduleHistory.value.groupBy { it.date }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}
