package com.nkwabyte.medilert.ui.screens.medication

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.model.DoseStatus
import com.nkwabyte.medilert.model.MedicationSchedule
import com.nkwabyte.medilert.ui.theme.*
import com.nkwabyte.medilert.viewmodel.MedicationViewModel
import com.nkwabyte.medilert.viewmodel.NavViewModel
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationHistoryScreen(
    navViewModel: NavViewModel = viewModel { NavViewModel() },
    medicationViewModel: MedicationViewModel = viewModel { MedicationViewModel() },
    hideBackButton: Boolean = false
) {
    val scheduleHistory by medicationViewModel.scheduleHistory.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<String?>(null) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Clock.System.now().toEpochMilliseconds()
    )

    val filteredHistory = remember(scheduleHistory, searchQuery, selectedFilter, selectedDate) {
        scheduleHistory.filter { schedule ->
            val matchesSearch = searchQuery.isBlank() ||
                schedule.medicationName.contains(searchQuery, ignoreCase = true) ||
                schedule.date.contains(searchQuery, ignoreCase = true)

            val matchesStatus = when (selectedFilter) {
                "Taken" -> schedule.status == DoseStatus.TAKEN
                "Missed" -> schedule.status == DoseStatus.MISSED
                "Upcoming" -> schedule.status == DoseStatus.UPCOMING
                else -> true
            }

            val matchesDate = selectedDate == null || schedule.date == selectedDate

            matchesSearch && matchesStatus && matchesDate
        }.sortedByDescending { toHistorySortKey(it.date, it.scheduledTime) }
    }

    val weeklyStats = remember(scheduleHistory) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val mondayOffset = today.dayOfWeek.ordinal
        val monday = today.minus(mondayOffset, DateTimeUnit.DAY)
        val sunday = monday.plus(6, DateTimeUnit.DAY)
        val weekStart = monday.toString()
        val weekEnd = sunday.toString()
        val week = scheduleHistory.filter { it.date >= weekStart && it.date <= weekEnd }
        val taken    = week.count { it.status == DoseStatus.TAKEN }
        val missed   = week.count { it.status == DoseStatus.MISSED }
        val upcoming = week.count { it.status == DoseStatus.UPCOMING }
        val total    = week.size
        val adherence = if (total > 0) (taken * 100) / total else 0
        WeekStats(taken, missed, upcoming, total, adherence)
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            PrimaryGreen.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(bottom = if (hideBackButton) 90.dp else 0.dp),
            contentPadding = PaddingValues(top = 56.dp, bottom = 24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!hideBackButton) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Surface, CircleShape)
                                .border(1.dp, BorderLight, CircleShape)
                                .clickable { navViewModel.popBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ChevronLeft,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(40.dp))
                    }

                    Text(
                        "History",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.size(40.dp))
                }

                if (scheduleHistory.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(listOf(PrimaryGreen, DarkGreen)),
                                shape = RoundedCornerShape(28.dp)
                            )
                            .padding(24.dp)
                    ) {
                        Column {
                            Text(
                                "Weekly",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                "adherence",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                "${weeklyStats.adherence}%",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 64.sp,
                                color = Color.White
                            )
                            Text(
                                "${weeklyStats.taken} of ${weeklyStats.total} doses taken this week",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .align(Alignment.CenterEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { weeklyStats.adherence / 100f },
                                modifier = Modifier.size(100.dp),
                                color = GhanaYellow,
                                trackColor = Color.White.copy(alpha = 0.2f),
                                strokeWidth = 12.dp,
                                strokeCap = StrokeCap.Round
                            )
                            Text(
                                "${weeklyStats.adherence}%",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HistoryStatCard(
                            "${weeklyStats.taken}",
                            "Taken",
                            PrimaryGreen,
                            modifier = Modifier.weight(1f)
                        )
                        HistoryStatCard(
                            "${weeklyStats.missed}",
                            "Missed",
                            GhanaRed,
                            modifier = Modifier.weight(1f)
                        )
                        HistoryStatCard(
                            "${weeklyStats.upcoming}",
                            "Upcoming",
                            TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    placeholder = {
                        Text(
                            "Search medications...",
                            fontFamily = Poppins,
                            fontSize = 14.sp,
                            color = TextHint
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = TextSecondary
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Surface,
                        focusedContainerColor = Surface,
                        unfocusedBorderColor = BorderLight,
                        focusedBorderColor = PrimaryGreen
                    ),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf("All", "Taken", "Missed", "Upcoming")) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    filter,
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryGreen,
                                selectedLabelColor = Color.White,
                                containerColor = Surface,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedFilter == filter,
                                borderColor = if (selectedFilter == filter) PrimaryGreen else BorderLight,
                                selectedBorderColor = PrimaryGreen,
                                borderWidth = 1.dp
                            )
                        )
                    }

                    item {
                        FilterChip(
                            selected = selectedDate != null,
                            onClick = { showDatePicker = true },
                            label = {
                                Text(
                                    selectedDate ?: "Select Date",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            trailingIcon = {
                                if (selectedDate != null) {
                                    IconButton(
                                        onClick = { selectedDate = null },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Clear date",
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryGreen,
                                selectedLabelColor = Color.White,
                                containerColor = Surface,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedDate != null,
                                borderColor = if (selectedDate != null) PrimaryGreen else BorderLight,
                                selectedBorderColor = PrimaryGreen,
                                borderWidth = 1.dp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Results (${filteredHistory.size})",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (filteredHistory.isEmpty()) {
                item {
                    EmptyHistoryState(
                        hasHistory = scheduleHistory.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 40.dp)
                    )
                }
            } else {
                val groupedHistory = filteredHistory.groupBy { it.date }
                groupedHistory.forEach { (date, entries) ->
                    item {
                        Text(
                            formatDateHeader(date),
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PrimaryGreen,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }

                    items(entries) { entry ->
                        HistoryEntryCard(
                            entry = entry,
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = Instant.fromEpochMilliseconds(millis)
                                .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                        }
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("OK", fontFamily = Poppins, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", fontFamily = Poppins, fontWeight = FontWeight.SemiBold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = PrimaryGreen,
                    todayContentColor = PrimaryGreen,
                    todayDateBorderColor = PrimaryGreen
                )
            )
        }
    }
}

@Composable
fun EmptyHistoryState(hasHistory: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (hasHistory) Icons.Default.SearchOff else Icons.Default.HistoryToggleOff,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            if (hasHistory) "No Results Found" else "No History Yet",
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            if (hasHistory)
                "Try adjusting your filters or search query"
            else
                "Your medication history will appear here once you start tracking your doses",
            fontFamily = Poppins,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun HistoryStatCard(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.07f), RoundedCornerShape(20.dp))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                value,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = color
            )
            Text(
                label,
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun HistoryEntryCard(entry: MedicationSchedule, modifier: Modifier = Modifier) {
    val (statusColor, statusIcon, statusLabel) = when (entry.status) {
        DoseStatus.TAKEN -> Triple(PrimaryGreen, Icons.Default.CheckCircle, "Taken")
        DoseStatus.MISSED -> Triple(GhanaRed, Icons.Default.Cancel, "Missed")
        DoseStatus.SKIPPED -> Triple(GhanaYellow, Icons.Default.RemoveCircle, "Skipped")
        DoseStatus.UPCOMING -> Triple(TextSecondary, Icons.Default.Schedule, "Upcoming")
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(20.dp))
            .border(1.dp, BorderLight, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            statusIcon,
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(32.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                entry.medicationName,
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = TextPrimary
            )
            Text(
                "${entry.dose} ${entry.unit} • ${entry.scheduledTime}",
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = TextSecondary
            )
            if (entry.takenAt.isNotBlank() && entry.status == DoseStatus.TAKEN) {
                Text(
                    "Taken at ${entry.takenAt}",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = PrimaryGreen.copy(alpha = 0.7f)
                )
            }
        }
        Box(
            modifier = Modifier
                .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                statusLabel,
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = statusColor
            )
        }
    }
}

fun formatDateHeader(date: String): String {
    return try {
        val localDate = LocalDate.parse(date)
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val yesterday = today.minus(1, DateTimeUnit.DAY)
        val dayName = localDate.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        val monthName = localDate.month.name.lowercase().replaceFirstChar { it.uppercase() }
        val formatted = "$dayName, ${localDate.dayOfMonth} $monthName"
        when (localDate) {
            today -> "Today, $formatted"
            yesterday -> "Yesterday, $formatted"
            else -> formatted
        }
    } catch (e: Exception) {
        date
    }
}

private fun toHistorySortKey(date: String, scheduledTime: String): String {
    val parts = scheduledTime.trim().split(" ")
    val hhmm = parts.getOrElse(0) { "00:00" }
    val amPm = parts.getOrElse(1) { "AM" }.uppercase()
    val timeParts = hhmm.split(":")
    val h = timeParts.getOrElse(0) { "0" }.toIntOrNull() ?: 0
    val m = timeParts.getOrElse(1) { "00" }
    val h24 = when {
        amPm == "PM" && h != 12 -> h + 12
        amPm == "AM" && h == 12 -> 0
        else -> h
    }
    return "$date ${h24.toString().padStart(2, '0')}:$m"
}

internal data class WeekStats(
    val taken: Int,
    val missed: Int,
    val upcoming: Int,
    val total: Int,
    val adherence: Int
)
