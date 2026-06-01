package com.nkwabyte.medilert.ui.screens.dashboard

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.model.DoseStatus
import com.nkwabyte.medilert.model.User
import com.nkwabyte.medilert.navigation.*
import com.nkwabyte.medilert.ui.components.BottomTabBar
import com.nkwabyte.medilert.ui.components.DashboardTab
import com.nkwabyte.medilert.ui.screens.medication.EmptyHistoryState
import com.nkwabyte.medilert.ui.screens.medication.HistoryEntryCard
import com.nkwabyte.medilert.ui.screens.medication.HistoryStatCard
import com.nkwabyte.medilert.ui.screens.medication.formatDateHeader
import com.nkwabyte.medilert.ui.screens.settings.SettingsScreen
import com.nkwabyte.medilert.ui.theme.*
import com.nkwabyte.medilert.viewmodel.AppViewModel
import com.nkwabyte.medilert.viewmodel.CaregiverViewModel
import com.nkwabyte.medilert.viewmodel.NavViewModel
import com.nkwabyte.medilert.viewmodel.TodayDoseInfo
import com.nkwabyte.medilert.viewmodel.WeekDayInfo
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CareGiverDashboardScreen(
    navViewModel: NavViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel(),
    caregiverViewModel: CaregiverViewModel = viewModel()
) {
    val activeTab by caregiverViewModel.activeTab.collectAsState()
    val currentUser by appViewModel.currentUser.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        when (activeTab) {
            DashboardTab.HOME -> CareGiverHomeContent(
                user = currentUser,
                caregiverViewModel = caregiverViewModel,
                onViewAll = { caregiverViewModel.setActiveTab(DashboardTab.HISTORY) },
                onAddPatientClick = { navViewModel.navigateTo(CaregiverAddPatient) }
            )
            DashboardTab.HISTORY -> CareGiverHistoryContent(caregiverViewModel = caregiverViewModel)
            DashboardTab.SETTINGS -> SettingsScreen(hideBackButton = true, isCaregiver = true)
        }
        BottomTabBar(
            activeTab = activeTab,
            onTabSelected = { caregiverViewModel.setActiveTab(it) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ── Home tab ──────────────────────────────────────────────────────────────────

@Composable
fun CareGiverHomeContent(
    user: User = User(),
    caregiverViewModel: CaregiverViewModel = viewModel(),
    onViewAll: () -> Unit = {},
    onAddPatientClick: () -> Unit = {}
) {
    val assignedPatients by caregiverViewModel.assignedPatients.collectAsState()
    val selectedPatient by caregiverViewModel.selectedPatient.collectAsState()
    val selectedDateStats by caregiverViewModel.selectedDateStats.collectAsState()
    val weekDaySummary by caregiverViewModel.weekDaySummary.collectAsState()
    val todayScheduleEnriched by caregiverViewModel.todayScheduleEnriched.collectAsState()
    val selectedDayDisplay by caregiverViewModel.selectedDayDisplay.collectAsState()

    val groupedSchedule = remember(todayScheduleEnriched) {
        val sessionOrder = listOf("Morning", "Afternoon", "Evening")
        val rawGroups = todayScheduleEnriched.groupBy { entry ->
            sessionOrder.firstOrNull { s -> entry.intakeTitle.contains(s, ignoreCase = true) }
                ?: entry.intakeTitle
        }
        val ordered = linkedMapOf<String, List<com.nkwabyte.medilert.viewmodel.TodayDoseInfo>>()
        sessionOrder.forEach { s -> rawGroups[s]?.let { ordered[s] = it } }
        rawGroups.forEach { (k, v) -> if (k !in sessionOrder) ordered[k] = v }
        ordered
    }

    val today = remember { SimpleDateFormat("EEEE, d MMMM, yyyy", Locale.getDefault()).format(Date()) }
    val firstName = remember(user.name) { user.name.split(" ").firstOrNull() ?: "You" }
    val initials = remember(user.name) {
        user.name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
    }

    // Dynamic header titles from selectedDayDisplay
    val dayLabel = selectedDayDisplay.first       // "Today", "Monday", etc.
    val dayDateStr = selectedDayDisplay.second    // "May 12, 2026"

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(bottom = 90.dp),
            contentPadding = PaddingValues(top = 52.dp, bottom = 32.dp)
        ) {
            item {
                // ── Header ────────────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                        Text(
                            "$firstName 👋",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Monitoring:",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Patient pill chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            assignedPatients.take(3).forEach { patient ->
                                val isSelected = patient.id == selectedPatient?.id
                                Box(
                                    modifier = Modifier
                                        .clickable { caregiverViewModel.selectPatient(patient.id) }
                                        .background(
                                            if (isSelected) DarkGreen else Color.Transparent,
                                            RoundedCornerShape(50)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) DarkGreen else BorderLight,
                                            RoundedCornerShape(50)
                                        )
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        patient.name.split(" ").firstOrNull() ?: patient.name,
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) Color.White else TextSecondary
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.Transparent, CircleShape)
                                    .border(1.dp, BorderLight, CircleShape)
                                    .clickable { onAddPatientClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add patient",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            today,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    // Profile avatar
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(PrimaryGreen.copy(alpha = 0.12f), CircleShape)
                            .border(2.dp, PrimaryGreen.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            initials.ifEmpty { "?" },
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PrimaryGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                if (assignedPatients.isEmpty()) {
                    NoPatientsHomeState(onAddPatientClick = onAddPatientClick)
                } else {
                    selectedPatient?.let { patient ->
                        val patientFirstName = patient.name.split(" ").firstOrNull() ?: patient.name

                        // ── Patient week ──────────────────────────────────────────────
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "$patientFirstName's Week",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextPrimary
                            )
                            Text(
                                "View all",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = PrimaryGreen,
                                modifier = Modifier.clickable { onViewAll() }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        WeekCalendarStrip(
                            days = weekDaySummary,
                            onDaySelected = { caregiverViewModel.selectDate(it) },
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // ── Adherence card (dynamic label) ────────────────────────────
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.linearGradient(listOf(DarkGreen, Color(0xFF0D3320))),
                                    shape = RoundedCornerShape(28.dp)
                                )
                                .padding(horizontal = 24.dp, vertical = 28.dp)
                        ) {
                            Column {
                                Text(
                                    "$dayLabel's adherence",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    "${selectedDateStats.adherence}%",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 60.sp,
                                    color = Color.White
                                )
                                Text(
                                    "${selectedDateStats.taken} of ${selectedDateStats.total} doses taken",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            Box(
                                modifier = Modifier.size(110.dp).align(Alignment.CenterEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { selectedDateStats.adherence / 100f },
                                    modifier = Modifier.size(90.dp),
                                    color = GhanaYellow,
                                    trackColor = Color.White.copy(alpha = 0.2f),
                                    strokeWidth = 10.dp,
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ── Stat cards ────────────────────────────────────────────────
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            HomeDailyStatCard(
                                value = "${selectedDateStats.taken}",
                                label = "Taken",
                                valueColor = PrimaryGreen,
                                containerColor = PrimaryGreen.copy(alpha = 0.08f),
                                icon = Icons.Default.CheckCircle,
                                iconTint = PrimaryGreen,
                                modifier = Modifier.weight(1f)
                            )
                            HomeDailyStatCard(
                                value = "${selectedDateStats.missed}",
                                label = "Missed",
                                valueColor = GhanaRed,
                                containerColor = GhanaRed.copy(alpha = 0.08f),
                                icon = Icons.Default.Cancel,
                                iconTint = GhanaRed,
                                modifier = Modifier.weight(1f)
                            )
                            HomeDailyStatCard(
                                value = "${selectedDateStats.upcoming}",
                                label = "Upcoming",
                                valueColor = Color(0xFF4A9EFF),
                                containerColor = Color(0xFF4A9EFF).copy(alpha = 0.08f),
                                icon = Icons.Default.Schedule,
                                iconTint = Color(0xFF4A9EFF),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // ── Schedule header (dynamic label + date) ────────────────────
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "$dayLabel's schedule",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextPrimary
                            )
                            Text(
                                dayDateStr,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            if (assignedPatients.isNotEmpty()) {
                if (todayScheduleEnriched.isEmpty()) {
                    item {
                        Text(
                            "No doses scheduled for ${selectedDayDisplay.first.lowercase()}",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                        )
                    }
                } else {
                    groupedSchedule.forEach { (title, doseItems) ->
                        item {
                            val headerColor = when {
                                title.contains("Morning", ignoreCase = true) -> GhanaRed
                                title.contains("Afternoon", ignoreCase = true) -> GhanaYellowDark
                                else -> DarkGreen
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(headerColor, CircleShape)
                                )
                                Text(
                                    title,
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = headerColor
                                )
                            }
                        }
                        items(doseItems) { info ->
                            TodayScheduleCard(
                                info = info,
                                modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 12.dp)
                            )
                        }
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = onAddPatientClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 100.dp),
            containerColor = GhanaYellow,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Add patient", modifier = Modifier.size(24.dp))
        }
    }
}

// ── History tab ───────────────────────────────────────────────────────────────

@Composable
fun CareGiverHistoryContent(
    caregiverViewModel: CaregiverViewModel = viewModel()
) {
    val assignedPatients by caregiverViewModel.assignedPatients.collectAsState()
    var selectedHistoryPatient by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(assignedPatients) {
        val current = selectedHistoryPatient ?: return@LaunchedEffect
        if (assignedPatients.none { it.id == current.id }) selectedHistoryPatient = null
    }

    if (selectedHistoryPatient == null) {
        HistoryPatientListScreen(
            patients = assignedPatients,
            onPatientClick = { patient ->
                caregiverViewModel.selectPatient(patient.id)
                selectedHistoryPatient = patient
            }
        )
    } else {
        HistoryPatientDetailScreen(
            patient = selectedHistoryPatient!!,
            caregiverViewModel = caregiverViewModel,
            onBack = { selectedHistoryPatient = null }
        )
    }
}

@Composable
private fun HistoryPatientListScreen(
    patients: List<User>,
    onPatientClick: (User) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(patients, searchQuery) {
        if (searchQuery.isBlank()) patients
        else patients.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        if (patients.isEmpty()) {
            NoPatientsState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(bottom = 90.dp),
                contentPadding = PaddingValues(top = 56.dp, bottom = 24.dp)
            ) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                        Text(
                            "Patient History",
                            fontFamily = Poppins, fontWeight = FontWeight.Bold,
                            fontSize = 24.sp, color = TextPrimary
                        )
                        Text(
                            "Select a patient to view their dose history",
                            fontFamily = Poppins, fontWeight = FontWeight.Medium,
                            fontSize = 13.sp, color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        placeholder = { Text("Search patients...", fontFamily = Poppins, fontSize = 14.sp, color = TextHint) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Surface, focusedContainerColor = Surface,
                            unfocusedBorderColor = BorderLight, focusedBorderColor = PrimaryGreen
                        ),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 14.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        "${filtered.size} patient${if (filtered.size != 1) "s" else ""}",
                        fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp, color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (filtered.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Search, contentDescription = null,
                                tint = TextSecondary.copy(alpha = 0.3f), modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No patients match \"$searchQuery\"",
                                fontFamily = Poppins, fontWeight = FontWeight.Medium,
                                fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(filtered) { patient ->
                        HistoryPatientCard(
                            patient = patient,
                            onClick = { onPatientClick(patient) },
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .padding(bottom = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryPatientDetailScreen(
    patient: User,
    caregiverViewModel: CaregiverViewModel,
    onBack: () -> Unit
) {
    val scheduleHistory by caregiverViewModel.selectedPatientSchedule.collectAsState()
    val weeklyStats by caregiverViewModel.weeklyStats.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredHistory = remember(scheduleHistory, searchQuery, selectedFilter) {
        scheduleHistory.filter { entry ->
            val matchesSearch = searchQuery.isBlank() ||
                    entry.medicationName.contains(searchQuery, ignoreCase = true)
            val matchesStatus = when (selectedFilter) {
                "Taken" -> entry.status == DoseStatus.TAKEN
                "Missed" -> entry.status == DoseStatus.MISSED
                "Upcoming" -> entry.status == DoseStatus.UPCOMING
                else -> true
            }
            matchesSearch && matchesStatus
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(bottom = 90.dp),
            contentPadding = PaddingValues(top = 56.dp, bottom = 24.dp)
        ) {
            item {
                // Back + patient name header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Text(
                        patient.name,
                        fontFamily = Poppins, fontWeight = FontWeight.Bold,
                        fontSize = 20.sp, color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        "Weekly adherence overview",
                        fontFamily = Poppins, fontWeight = FontWeight.Medium,
                        fontSize = 13.sp, color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                        Text("Weekly", fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.White.copy(alpha = 0.9f))
                        Text("adherence", fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.White.copy(alpha = 0.9f))
                        Text("${weeklyStats.adherence}%", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 64.sp, color = Color.White)
                        Text("${weeklyStats.taken} of ${weeklyStats.total} doses taken this week", fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                    Box(modifier = Modifier.size(120.dp).align(Alignment.CenterEnd), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { weeklyStats.adherence / 100f },
                            modifier = Modifier.size(100.dp),
                            color = GhanaYellow,
                            trackColor = Color.White.copy(alpha = 0.2f),
                            strokeWidth = 12.dp,
                            strokeCap = StrokeCap.Round
                        )
                        Text("${weeklyStats.adherence}%", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HistoryStatCard("${weeklyStats.taken}", "Taken", PrimaryGreen, modifier = Modifier.weight(1f))
                    HistoryStatCard("${weeklyStats.missed}", "Missed", GhanaRed, modifier = Modifier.weight(1f))
                    HistoryStatCard("${weeklyStats.upcoming}", "Upcoming", Color(0xFF4A9EFF), modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    placeholder = { Text("Search medications...", fontFamily = Poppins, fontSize = 14.sp, color = TextHint) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Surface, focusedContainerColor = Surface,
                        unfocusedBorderColor = BorderLight, focusedBorderColor = PrimaryGreen
                    ),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 14.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf("All", "Taken", "Missed", "Upcoming")) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryGreen, selectedLabelColor = Color.White,
                                containerColor = Surface, labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true, selected = selectedFilter == filter,
                                borderColor = if (selectedFilter == filter) PrimaryGreen else BorderLight,
                                selectedBorderColor = PrimaryGreen, borderWidth = 1.dp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "Results (${filteredHistory.size})",
                    fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp, color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (filteredHistory.isEmpty()) {
                item {
                    EmptyHistoryState(
                        hasHistory = scheduleHistory.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 40.dp)
                    )
                }
            } else {
                val grouped = filteredHistory.groupBy { it.date }
                grouped.forEach { (date, entries) ->
                    item {
                        Text(
                            formatDateHeader(date),
                            fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                            color = PrimaryGreen,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }
                    items(entries) { entry ->
                        HistoryEntryCard(
                            entry = entry,
                            modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryPatientCard(
    patient: User,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val avatarColors = listOf(
        Color(0xFF1B5E20), Color(0xFFB71C1C), Color(0xFF1A237E), Color(0xFF4A148C), Color(0xFF006064)
    )
    val avatarColor = avatarColors[kotlin.math.abs(patient.name.hashCode()) % avatarColors.size]
    val initials = patient.name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(20.dp))
            .border(1.dp, BorderLight, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(avatarColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                initials.ifEmpty { patient.name.take(1).uppercase() },
                fontFamily = Poppins, fontWeight = FontWeight.Bold,
                fontSize = 18.sp, color = Color.White
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                patient.name,
                fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp, color = TextPrimary
            )
            Text(
                "Tap to view dose history",
                fontFamily = Poppins, fontWeight = FontWeight.Medium,
                fontSize = 12.sp, color = TextSecondary
            )
        }

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(24.dp)
        )
    }
}

// ── Home composables ──────────────────────────────────────────────────────────

@Composable
private fun WeekCalendarStrip(
    days: List<WeekDayInfo>,
    onDaySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(20.dp))
            .border(1.dp, BorderLight, RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        days.forEach { day ->
            Column(
                modifier = Modifier.clickable { onDaySelected(day.date) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    day.dayLabel,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = if (day.isSelected) PrimaryGreen else TextSecondary
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            when {
                                day.isSelected -> DarkGreen
                                else -> Color.Transparent
                            },
                            CircleShape
                        )
                        .then(
                            when {
                                day.isToday && !day.isSelected ->
                                    Modifier.border(2.dp, DarkGreen, CircleShape)
                                else -> Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${day.dayNumber}",
                        fontFamily = Poppins,
                        fontWeight = if (day.isSelected || day.isToday) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp,
                        color = if (day.isSelected) Color.White else if (day.isToday) DarkGreen else TextPrimary
                    )
                }
                // Adherence dot: green ≥80%, yellow >0%, transparent otherwise
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            when {
                                day.adherence >= 80 -> PrimaryGreen
                                day.adherence > 0 -> GhanaYellow
                                day.hasDoses -> GhanaYellow.copy(alpha = 0.4f)
                                else -> Color.Transparent
                            },
                            CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun HomeDailyStatCard(
    value: String,
    label: String,
    valueColor: Color,
    containerColor: Color,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(containerColor, RoundedCornerShape(18.dp))
            .border(1.dp, BorderLight, RoundedCornerShape(18.dp))
            .padding(vertical = 14.dp, horizontal = 10.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = iconTint.copy(alpha = 0.4f),
            modifier = Modifier.size(18.dp).align(Alignment.TopEnd)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(value, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = valueColor)
            Text(label, fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun TodayScheduleCard(info: TodayDoseInfo, modifier: Modifier = Modifier) {
    val isMorning = info.intakeTitle.contains("Morning", ignoreCase = true)
    val isAfternoon = info.intakeTitle.contains("Afternoon", ignoreCase = true)

    val cardGradient = when {
        isMorning -> Brush.linearGradient(listOf(GhanaRed, GhanaRedLight))
        isAfternoon -> Brush.linearGradient(listOf(GhanaYellow, GhanaYellowDark))
        else -> Brush.linearGradient(listOf(DarkGreen, MediumGreen))
    }
    val cardIcon = when {
        isMorning -> Icons.Default.Medication
        isAfternoon -> Icons.Default.Science
        else -> Icons.Default.DarkMode
    }
    val isTaken = info.schedule.status == DoseStatus.TAKEN

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(cardGradient, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Left icon
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(cardIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        }

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                info.schedule.medicationName,
                fontFamily = Poppins, fontWeight = FontWeight.Bold,
                fontSize = 16.sp, color = Color.White
            )
            Text(
                "${info.frequency}, ${info.schedule.scheduledTime}",
                fontFamily = Poppins, fontWeight = FontWeight.Medium,
                fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Dosage quantity circles
            val doseCount = info.schedule.dose.coerceIn(1, 8)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(doseCount) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${info.schedule.dose} ${info.schedule.unit}",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            if (info.sideEffects.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Side effects: ${info.sideEffects}",
                    fontFamily = Poppins, fontWeight = FontWeight.Normal,
                    fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Right: status + speaker
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.White.copy(alpha = if (isTaken) 0.3f else 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = if (isTaken) Color.White else Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
            Icon(
                Icons.Default.Mic,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun NoPatientsHomeState(onAddPatientClick: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(80.dp).background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No patients linked yet",
            fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp, color = TextPrimary, textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Tap \"+ Add\" above to link a patient and start monitoring their medication adherence.",
            fontFamily = Poppins, fontWeight = FontWeight.Medium,
            fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// ── History composables ───────────────────────────────────────────────────────

@Composable
private fun NoPatientsState() {
    Column(
        modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(120.dp).background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Groups, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(60.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("No Patients Yet", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextPrimary, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Use \"+ Add\" on the Home tab to link a patient and start monitoring their adherence.",
            fontFamily = Poppins, fontWeight = FontWeight.Medium,
            fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
