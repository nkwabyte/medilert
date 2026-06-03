package com.nkwabyte.medilert.ui.screens.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.generated.resources.Res
import com.nkwabyte.medilert.generated.resources.img_auth_setup
import com.nkwabyte.medilert.model.UserRole
import com.nkwabyte.medilert.navigation.UserProfileComplete
import com.nkwabyte.medilert.ui.components.AuthScreenShell
import com.nkwabyte.medilert.ui.screens.auth.AuthInputField
import com.nkwabyte.medilert.ui.theme.BorderLight
import com.nkwabyte.medilert.ui.theme.GhanaYellow
import com.nkwabyte.medilert.ui.theme.Poppins
import com.nkwabyte.medilert.ui.theme.PrimaryGreen
import com.nkwabyte.medilert.ui.theme.Surface
import com.nkwabyte.medilert.ui.theme.TextPrimary
import com.nkwabyte.medilert.ui.theme.TextSecondary
import com.nkwabyte.medilert.viewmodel.AppViewModel
import com.nkwabyte.medilert.viewmodel.NavViewModel
import com.nkwabyte.medilert.viewmodel.SignupViewModel

private data class RoleOption(
    val role: UserRole,
    val label: String,
    val subtitle: String,
    val icon: ImageVector
)

private val roles = listOf(
    RoleOption(UserRole.PATIENT, "Patient", "Track your own medications", Icons.Default.Person),
    RoleOption(UserRole.DOCTOR, "Doctor", "Monitor patient adherence", Icons.Default.LocalHospital),
    RoleOption(UserRole.PHARMACIST, "Pharmacist", "Manage prescriptions", Icons.Default.MedicalServices),
    RoleOption(UserRole.GUARDIAN, "Guardian", "Care for a family member", Icons.Default.FamilyRestroom),
)

private data class ProfessionalFieldConfig(val label: String, val placeholder: String, val icon: ImageVector)

private fun professionalFieldFor(role: UserRole): ProfessionalFieldConfig? = when (role) {
    UserRole.DOCTOR -> ProfessionalFieldConfig("Medical Specialty", "e.g. Cardiologist, General Practitioner", Icons.Default.LocalHospital)
    UserRole.PHARMACIST -> ProfessionalFieldConfig("Institution / Pharmacy", "e.g. Korle Bu Hospital Pharmacy", Icons.Default.Work)
    UserRole.GUARDIAN -> ProfessionalFieldConfig("Relationship to Patient", "e.g. Parent, Spouse, Sibling", Icons.Default.FamilyRestroom)
    UserRole.PATIENT -> null
}

@Composable
fun UserRoleScreen(
    navViewModel: NavViewModel = viewModel { NavViewModel() },
    appViewModel: AppViewModel = viewModel { AppViewModel() },
    signupViewModel: SignupViewModel = viewModel { SignupViewModel() }
) {
    var selectedRole by remember { mutableStateOf(UserRole.PATIENT) }
    var professionalDetail by remember { mutableStateOf("") }
    val fieldConfig = professionalFieldFor(selectedRole)

    AuthScreenShell(
        imageRes = Res.drawable.img_auth_setup,
        imageHeight = 220.dp,
        onBack = { navViewModel.popBack() }
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text("I am a...", fontFamily = Poppins, fontWeight = FontWeight.Bold,
            fontSize = 28.sp, color = TextPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Select your role to personalize your experience",
            fontFamily = Poppins, fontWeight = FontWeight.Normal,
            fontSize = 14.sp, color = TextSecondary)

        Spacer(modifier = Modifier.height(28.dp))

        roles.forEach { option ->
            RoleCard(
                option = option,
                isSelected = selectedRole == option.role,
                onClick = {
                    if (selectedRole != option.role) {
                        selectedRole = option.role
                        professionalDetail = ""
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        AnimatedVisibility(
            visible = fieldConfig != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            fieldConfig?.let { config ->
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    AuthInputField(
                        label = config.label,
                        value = professionalDetail,
                        onValueChange = { professionalDetail = it },
                        placeholder = config.placeholder,
                        leadingIcon = { Icon(config.icon, contentDescription = null, tint = TextSecondary) }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = selectedRole == UserRole.DOCTOR || selectedRole == UserRole.PHARMACIST,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                LicenseInfoBanner()
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                appViewModel.setUserRole(selectedRole)
                signupViewModel.setUserRole(selectedRole, professionalDetail.trim())
                navViewModel.navigateTo(UserProfileComplete)
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GhanaYellow)
        ) {
            Text("Continue", fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp, color = TextPrimary)
        }
    }
}

@Composable
private fun LicenseInfoBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrimaryGreen.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
            .border(1.dp, PrimaryGreen.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Badge, contentDescription = null,
            tint = PrimaryGreen, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            "Your professional credentials may be verified before patient access is granted.",
            fontFamily = Poppins, fontWeight = FontWeight.Medium,
            fontSize = 12.sp, color = PrimaryGreen
        )
    }
}

@Composable
private fun RoleCard(option: RoleOption, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) PrimaryGreen.copy(alpha = 0.04f) else Surface, RoundedCornerShape(20.dp))
            .border(2.dp, if (isSelected) PrimaryGreen else BorderLight, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (isSelected) PrimaryGreen else PrimaryGreen.copy(alpha = 0.1f), RoundedCornerShape(18.dp))
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) PrimaryGreen else PrimaryGreen.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(option.icon, contentDescription = null,
                tint = if (isSelected) Color.White else PrimaryGreen, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(option.label, fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp, color = TextPrimary)
            Text(option.subtitle, fontFamily = Poppins, fontWeight = FontWeight.Medium,
                fontSize = 13.sp, color = TextSecondary)
        }
        if (isSelected) {
            Box(
                modifier = Modifier.size(24.dp).background(PrimaryGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null,
                    tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}
