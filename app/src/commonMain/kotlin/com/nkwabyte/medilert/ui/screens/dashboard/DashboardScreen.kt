package com.nkwabyte.medilert.ui.screens.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.model.DoseStatus
import com.nkwabyte.medilert.model.MedicationSchedule
import com.nkwabyte.medilert.generated.resources.Res
import com.nkwabyte.medilert.generated.resources.img_auth_signup
import com.nkwabyte.medilert.navigation.AddMedication1
import com.nkwabyte.medilert.navigation.EditMedication
import com.nkwabyte.medilert.navigation.ProfilePage
import org.jetbrains.compose.resources.painterResource
import com.nkwabyte.medilert.ui.components.BottomTabBar
import com.nkwabyte.medilert.ui.components.DashboardTab
import com.nkwabyte.medilert.ui.screens.medication.MedicationHistoryScreen
import com.nkwabyte.medilert.ui.screens.settings.SettingsScreen
import com.nkwabyte.medilert.ui.theme.Background
import com.nkwabyte.medilert.ui.theme.BorderLight
import com.nkwabyte.medilert.ui.theme.DarkGreen
import com.nkwabyte.medilert.ui.theme.GhanaRed
import com.nkwabyte.medilert.ui.theme.GhanaRedLight
import com.nkwabyte.medilert.ui.theme.GhanaYellow
import com.nkwabyte.medilert.ui.theme.GhanaYellowDark
import com.nkwabyte.medilert.ui.theme.MediumGreen
import com.nkwabyte.medilert.ui.theme.Poppins
import com.nkwabyte.medilert.ui.theme.PrimaryGreen
import com.nkwabyte.medilert.ui.theme.Surface
import com.nkwabyte.medilert.ui.theme.SurfaceVariant
import com.nkwabyte.medilert.ui.theme.TextPrimary
import com.nkwabyte.medilert.ui.theme.TextSecondary
import com.nkwabyte.medilert.util.GhanaianPhrases
import com.nkwabyte.medilert.util.HapticFeedback
import com.nkwabyte.medilert.viewmodel.AppViewModel
import com.nkwabyte.medilert.viewmodel.MedicationViewModel
import com.nkwabyte.medilert.viewmodel.NavViewModel
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

private fun sessionOf(scheduledTime: String): String {
    return try {
        val rawHour = scheduledTime.substringBefore(':').trim().toIntOrNull() ?: 0
        val isPM = scheduledTime.contains("PM", ignoreCase = true)
        val isAM = scheduledTime.contains("AM", ignoreCase = true)
        val hour24 = when {
            isPM && rawHour != 12 -> rawHour + 12
            isAM && rawHour == 12 -> 0
            else -> rawHour
        }
        when (hour24) {
            in 0..11 -> "Morning"
            in 12..17 -> "Afternoon"
            else -> "Evening"
        }
    } catch (e: Exception) { "Morning" }
}

@Composable
fun DashboardScreen(
    navViewModel: NavViewModel = viewModel { NavViewModel() },
    appViewModel: AppViewModel = viewModel { AppViewModel() },
    medicationViewModel: MedicationViewModel = viewModel { MedicationViewModel() }
) {
    val activeTab by appViewModel.activeDashboardTab.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        AnimatedContent(
            targetState = activeTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.fillMaxSize(),
            label = "dashboard_tab"
        ) { tab ->
            when (tab) {
                DashboardTab.HOME -> HomeTab(
                    navViewModel = navViewModel,
                    appViewModel = appViewModel,
                    medicationViewModel = medicationViewModel,
                    onViewAllHistory = { appViewModel.setDashboardTab(DashboardTab.HISTORY) }
                )

                DashboardTab.HISTORY -> MedicationHistoryScreen(
                    navViewModel = navViewModel,
                    medicationViewModel = medicationViewModel,
                    hideBackButton = true
                )

                DashboardTab.SETTINGS -> SettingsScreen(
                    navViewModel = navViewModel,
                    appViewModel = appViewModel,
                    hideBackButton = true
                )
            }
        }

        BottomTabBar(
            activeTab = activeTab,
            onTabSelected = { appViewModel.setDashboardTab(it) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun HomeTab(
    navViewModel: NavViewModel = viewModel { NavViewModel() },
    appViewModel: AppViewModel = viewModel { AppViewModel() },
    medicationViewModel: MedicationViewModel = viewModel { MedicationViewModel() },
    onViewAllHistory: () -> Unit = {}
) {
    val currentUser by appViewModel.currentUser.collectAsState()
    val selectedLanguage by appViewModel.selectedLanguage.collectAsState()
    val photoBytes by appViewModel.profilePhotoBytes.collectAsState()
    val scheduleHistory by medicationViewModel.scheduleHistory.collectAsState()
    val medications by medicationViewModel.medications.collectAsState()

    val todayDateStr = remember {
        val ldt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        "${ldt.year}-${ldt.monthNumber.toString().padStart(2, '0')}-${ldt.dayOfMonth.toString().padStart(2, '0')}"
    }
    var selectedDateStr by remember { mutableStateOf(todayDateStr) }

    val currentHour = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour }
    val greeting = remember(currentHour) {
        when (currentHour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }
    val localGreeting = remember(selectedLanguage, currentHour) {
        GhanaianPhrases.greeting(selectedLanguage, currentHour)
    }

    val userName = remember(currentUser.name) {
        currentUser.name.split(" ").firstOrNull() ?: "User"
    }

    val currentDate = remember {
        val ldt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val dow = ldt.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        val mon = ldt.month.name.lowercase().replaceFirstChar { it.uppercase() }
        "$dow, ${ldt.dayOfMonth} $mon, ${ldt.year}"
    }

    // Filter schedule for selected date, cross-referencing active medications to exclude
    // orphaned schedule documents left behind by deleted or test medications.
    val selectedSchedule = remember(scheduleHistory, selectedDateStr, medications) {
        val activeIds = medications.map { it.id }.toSet()
        scheduleHistory
            .filter { it.date == selectedDateStr && it.medicationId in activeIds }
            .sortedBy { it.scheduledTime }
    }

    // Group schedule by session in display order: Morning → Afternoon → Evening
    val sessionGroups = remember(selectedSchedule) {
        val grouped = selectedSchedule.groupBy { sessionOf(it.scheduledTime) }
        listOf("Morning", "Afternoon", "Evening").mapNotNull { name ->
            grouped[name]?.let { name to it }
        }
    }

    // Which schedule entry the Edit dialog is open for (null = closed)
    var editingSchedule by remember { mutableStateOf<MedicationSchedule?>(null) }

    // Get formatted day name for selected date
    val selectedDayInfo = remember(selectedDateStr) {
        try {
            val parts = selectedDateStr.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            val isToday = selectedDateStr == todayDateStr
            val ld = LocalDate(year, month, day)
            val dayOfWeekName = ld.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
            val monthName = ld.month.name.lowercase().replaceFirstChar { it.uppercase() }
            val dayName = if (isToday) "Today" else dayOfWeekName
            val fullDate = "$dayOfWeekName, $day $monthName"
            Pair(dayName, fullDate)
        } catch (e: Exception) {
            Pair("Today", currentDate.split(",").first())
        }
    }

    // Generate week calendar data
    val weekDays = remember(scheduleHistory, medications) {
        val activeIds = medications.map { it.id }.toSet()
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val todayDate = LocalDate(now.year, now.monthNumber, now.dayOfMonth)

        // Find Monday of the current week
        val daysSinceMonday = todayDate.dayOfWeek.ordinal // Monday=0 in kotlinx.datetime
        val mondayDate = todayDate.minus(daysSinceMonday, DateTimeUnit.DAY)

        (0..6).map { dayOffset ->
            val day = mondayDate.plus(dayOffset, DateTimeUnit.DAY)
            val dayAbbr = when (day.dayOfWeek) {
                DayOfWeek.MONDAY -> "Mo"
                DayOfWeek.TUESDAY -> "Tu"
                DayOfWeek.WEDNESDAY -> "We"
                DayOfWeek.THURSDAY -> "Th"
                DayOfWeek.FRIDAY -> "Fr"
                DayOfWeek.SATURDAY -> "Sa"
                DayOfWeek.SUNDAY -> "Su"
                else -> ""
            }
            val dateNum = day.dayOfMonth.toString()
            val dayStr = "${day.year}-${day.monthNumber.toString().padStart(2, '0')}-${day.dayOfMonth.toString().padStart(2, '0')}"
            val isToday = day == todayDate

            // Only count schedules belonging to currently active medications
            val daySchedules = scheduleHistory.filter { it.date == dayStr && it.medicationId in activeIds }
            val dayTaken = daySchedules.count { it.status == DoseStatus.TAKEN }
            val dayTotal = daySchedules.count { it.status != DoseStatus.UPCOMING }
            val dayAdherence = if (dayTotal > 0) (dayTaken * 100) / dayTotal else 0

            // Return day abbreviation, date number, date string, isToday flag, and adherence
            mapOf(
                "abbr" to dayAbbr,
                "num" to dateNum,
                "dateStr" to dayStr,
                "isToday" to isToday,
                "adherence" to dayAdherence
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(bottom = 90.dp),
            contentPadding = PaddingValues(top = 0.dp)
        ) {
            item {
                // ── Greeting header — image + gradient ────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                ) {
                    Image(
                        painter = painterResource(Res.drawable.img_auth_signup),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Dark-green gradient — heavier at bottom so text pops
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0.00f to Color(0xFF071407).copy(alpha = 0.30f),
                                    0.50f to Color(0xFF071407).copy(alpha = 0.52f),
                                    1.00f to Color(0xFF071407).copy(alpha = 0.82f)
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .statusBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                .border(2.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                                .clip(CircleShape)
                                .clickable { navViewModel.navigateTo(ProfilePage) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (photoBytes != null) {
                                coil3.compose.AsyncImage(
                                    model = photoBytes,
                                    contentDescription = "Profile photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                "$greeting, $userName",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                            if (localGreeting.isNotEmpty()) {
                                Text(
                                    localGreeting,
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.80f)
                                )
                            }
                            Text(
                                currentDate,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        }
                    }
                }

                // This Week header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "This Week",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                    Text(
                        "View all",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = PrimaryGreen,
                        modifier = Modifier
                            .clickable { onViewAllHistory() }
                            .padding(vertical = 8.dp, horizontal = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Week calendar row - Live and dynamic
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .background(Surface, RoundedCornerShape(24.dp))
                        .border(1.dp, BorderLight, RoundedCornerShape(24.dp))
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weekDays.forEach { dayData ->
                        val day = dayData["abbr"] as String
                        val date = dayData["num"] as String
                        val dateStr = dayData["dateStr"] as String
                        val isToday = dayData["isToday"] as Boolean
                        val adherence = dayData["adherence"] as Int
                        val isSelected = dateStr == selectedDateStr

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.clickable { selectedDateStr = dateStr }
                        ) {
                            Text(
                                day,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) PrimaryGreen else TextSecondary
                            )
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (isSelected) PrimaryGreen else SurfaceVariant,
                                        CircleShape
                                    )
                                    .then(
                                        if (isToday && !isSelected)
                                            Modifier.border(2.dp, PrimaryGreen, CircleShape)
                                        else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    date,
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) Color.White else if (isToday) PrimaryGreen else TextPrimary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(
                                        when {
                                            adherence >= 80 -> PrimaryGreen
                                            adherence > 0 -> GhanaYellow
                                            else -> Color.Transparent
                                        },
                                        CircleShape
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selected-day adherence stats (drives both the adherence card and status blocks)
                val selTaken = selectedSchedule.count { it.status == DoseStatus.TAKEN }
                val selMissed = selectedSchedule.count { it.status == DoseStatus.MISSED }
                val selUpcoming = selectedSchedule.count { it.status == DoseStatus.UPCOMING }
                val selCompleted = selTaken + selMissed
                // Denominator is all scheduled doses, not just actioned ones.
                // 1 taken out of 2 scheduled = 50%, not 100%.
                val selAdherencePct = if (selectedSchedule.isNotEmpty()) (selTaken * 100) / selectedSchedule.size else 0

                // Adherence card:
                //  - past day  → show whenever doses exist (day is over; 0 taken = 0%)
                //  - today     → show only once at least one dose is actioned
                //  - future day→ hidden (nothing has happened yet)
                val showAdherenceCard = selectedSchedule.isNotEmpty() &&
                    (selectedDateStr < todayDateStr || selCompleted > 0)
                if (showAdherenceCard) {
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
                                "${selectedDayInfo.first}'s adherence",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                "$selAdherencePct%",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 64.sp,
                                color = Color.White
                            )
                            Text(
                                "$selTaken of ${selectedSchedule.size} doses taken",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
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
                                progress = { selAdherencePct / 100f },
                                modifier = Modifier.size(100.dp),
                                color = GhanaYellow,
                                trackColor = Color.White.copy(alpha = 0.2f),
                                strokeWidth = 12.dp,
                                strokeCap = StrokeCap.Round
                            )
                            Text(
                                "$selAdherencePct%",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Status blocks
                if (selectedSchedule.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatusBlock(
                            label = "Taken",
                            count = selTaken,
                            color = PrimaryGreen,
                            icon = Icons.Default.CheckCircle,
                            modifier = Modifier.weight(1f)
                        )
                        StatusBlock(
                            label = "Missed",
                            count = selMissed,
                            color = GhanaRed,
                            icon = Icons.Default.Cancel,
                            modifier = Modifier.weight(1f)
                        )
                        StatusBlock(
                            label = "Upcoming",
                            count = selUpcoming,
                            color = Color(0xFF4A9EFF),
                            icon = Icons.Default.Schedule,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Today's schedule header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${selectedDayInfo.first}'s schedule",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = TextPrimary
                    )
                    Box(
                        modifier = Modifier
                            .background(SurfaceVariant, RoundedCornerShape(50.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            selectedDayInfo.second,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Empty state or session-grouped schedule list
            if (selectedSchedule.isEmpty()) {
                item {
                    EmptyScheduleState(
                        isToday = selectedDateStr == todayDateStr,
                        dayName = selectedDayInfo.first,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 40.dp)
                    )
                }
            } else {
                sessionGroups.forEach { (session, schedules) ->
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        SessionHeader(
                            session = session,
                            onEdit = {
                                editingSchedule =
                                    schedules.firstOrNull { it.status == DoseStatus.UPCOMING }
                                        ?: schedules.first()
                            }
                        )
                    }
                    items(schedules, key = { it.id }) { schedule ->
                        MedicationScheduleCard(
                            schedule = schedule,
                            onEdit = { editingSchedule = schedule },
                            onClick = { medicationViewModel.markDoseTaken(schedule) },
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Dose update dialog
        editingSchedule?.let { schedule ->
            DoseUpdateDialog(
                schedule = schedule,
                onDismiss = { editingSchedule = null },
                onMarkTaken = {
                    HapticFeedback.success()
                    medicationViewModel.markDoseTaken(schedule)
                    editingSchedule = null
                },
                onMarkMissed = {
                    HapticFeedback.error()
                    medicationViewModel.markDoseMissed(schedule)
                    editingSchedule = null
                },
                onMarkSkipped = {
                    HapticFeedback.light()
                    medicationViewModel.markDoseSkipped(schedule)
                    editingSchedule = null
                },
                onEditDetails = {
                    editingSchedule = null
                    navViewModel.navigateTo(EditMedication(schedule.medicationId))
                }
            )
        }

        // FAB
        FloatingActionButton(
            onClick = { navViewModel.navigateTo(AddMedication1) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 24.dp, bottom = 100.dp)
                .size(68.dp),
            shape = CircleShape,
            containerColor = PrimaryGreen,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add medication",
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun EmptyScheduleState(
    modifier: Modifier = Modifier,
    isToday: Boolean = true,
    dayName: String = "Today",
) {
    val title = if (isToday) "All Clear for Today!" else "No Schedule for $dayName!"
    val subtitle = if (isToday)
        "You have no scheduled medications for today.\nEnjoy your day!"
    else
        "You have no scheduled medications for $dayName."

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
                Icons.Default.EventAvailable,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            title,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            subtitle,
            fontFamily = Poppins,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun StatusBlock(
    label: String,
    count: Int,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(96.dp)
            .background(color.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            Text(
                count.toString(),
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = if (icon != null) 22.sp else 28.sp,
                color = color
            )
        }
    }
}

@Composable
fun MedicationScheduleCard(
    schedule: MedicationSchedule,
    onEdit: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val session = sessionOf(schedule.scheduledTime)

    val cardGradient = when (session) {
        "Morning" -> listOf(GhanaRed, GhanaRedLight)
        "Afternoon" -> listOf(GhanaYellow, GhanaYellowDark)
        else -> listOf(DarkGreen, MediumGreen)
    }
    val sessionAccent = when (session) {
        "Morning" -> GhanaRed
        "Afternoon" -> GhanaYellowDark
        else -> PrimaryGreen
    }
    val statusText = when (schedule.status) {
        DoseStatus.TAKEN -> "Taken"
        DoseStatus.MISSED -> "Missed"
        DoseStatus.SKIPPED -> "Skipped"
        DoseStatus.UPCOMING -> "Upcoming"
    }

    // Choose icon based on medication unit
    val medIcon = when {
        schedule.unit.contains("injection", ignoreCase = true) ||
        schedule.unit.contains("vial", ignoreCase = true) -> Icons.Default.Vaccines
        schedule.unit.contains("liquid", ignoreCase = true) ||
        schedule.unit.contains("syrup", ignoreCase = true) ||
        schedule.unit.contains("ml", ignoreCase = true) -> Icons.Default.MedicalServices
        schedule.unit.contains("capsule", ignoreCase = true) ||
        schedule.unit.contains("tablet", ignoreCase = true) -> Icons.Default.Medication
        else -> Icons.Default.MedicalServices
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .background(Surface, RoundedCornerShape(50.dp))
                    .border(1.dp, BorderLight, RoundedCornerShape(50.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(sessionAccent, CircleShape)
                )
                Text(
                    schedule.scheduledTime,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = sessionAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tapping any card opens the dose update dialog
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(cardGradient),
                    shape = RoundedCornerShape(32.dp)
                )
                .clickable(onClick = onEdit)
                .padding(20.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        medIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        schedule.medicationName,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            schedule.scheduledTime,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Dosage quantity circles
                    val doseCount = schedule.dose.coerceIn(1, 10)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(doseCount) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(Color.White, CircleShape)
                                    .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${schedule.dose} ${schedule.unit}",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Status indicator (top-right)
            when (schedule.status) {
                DoseStatus.TAKEN -> {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.TopEnd)
                            .background(Color.White.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Taken",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                DoseStatus.MISSED, DoseStatus.SKIPPED -> {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(50.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            statusText,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
                DoseStatus.UPCOMING -> Unit
            }

            // Speaker icon (bottom-right) — always visible for upcoming, dimmed for others
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.BottomEnd)
                    .background(Color.White.copy(alpha = if (schedule.status == DoseStatus.UPCOMING) 0.25f else 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Audio reminder",
                    tint = Color.White.copy(alpha = if (schedule.status == DoseStatus.UPCOMING) 1f else 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SessionHeader(
    session: String,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = when (session) {
        "Morning" -> GhanaRed
        "Afternoon" -> GhanaYellowDark
        else -> PrimaryGreen
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(accentColor, CircleShape)
            )
            Text(
                session,
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = TextPrimary
            )
        }
        Text(
            "Edit",
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = accentColor,
            modifier = Modifier
                .clickable { onEdit() }
                .padding(vertical = 4.dp, horizontal = 8.dp)
        )
    }
}

@Composable
private fun DoseUpdateDialog(
    schedule: MedicationSchedule,
    onDismiss: () -> Unit,
    onMarkTaken: () -> Unit,
    onMarkMissed: () -> Unit,
    onMarkSkipped: () -> Unit,
    onEditDetails: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        title = {
            Column {
                Text(
                    schedule.medicationName,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                Text(
                    "Scheduled: ${schedule.scheduledTime}",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "How was this dose?",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(PrimaryGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .border(1.dp, PrimaryGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .clickable { onMarkTaken() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Taken", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = PrimaryGreen)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(GhanaRed.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .border(1.dp, GhanaRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .clickable { onMarkMissed() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Cancel, contentDescription = null, tint = GhanaRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Missed", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = GhanaRed)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(GhanaYellow.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .border(1.dp, GhanaYellow.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable { onMarkSkipped() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = GhanaYellowDark, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Skipped", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = GhanaYellowDark)
                        }
                    }
                }

                // Edit medication details option
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryGreen.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .border(1.dp, PrimaryGreen.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .clickable { onEditDetails() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                        Text("Edit Medication Details", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = PrimaryGreen)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = Poppins, fontWeight = FontWeight.Medium, color = TextSecondary)
            }
        }
    )
}
