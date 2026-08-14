package com.nkwabyte.medilert.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nkwabyte.medilert.ui.theme.Poppins
import com.nkwabyte.medilert.util.tr

enum class UserRole {
    PATIENT,
    DOCTOR
}

@Composable
fun RoleBadge(
    role: UserRole,
    modifier: Modifier = Modifier
) {
    val isDoctor = role == UserRole.DOCTOR
    val backgroundColor = if (isDoctor) Color(0xFF0EA5E9).copy(alpha = 0.35f) else Color(0xFF10B981).copy(alpha = 0.35f)
    val borderColor = Color.White.copy(alpha = 0.45f)
    val icon = if (isDoctor) Icons.Default.MedicalServices else Icons.Default.Person
    val label = if (isDoctor) "Doctor".tr() else "Patient".tr()

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = Color.White
            )
        }
    }
}
