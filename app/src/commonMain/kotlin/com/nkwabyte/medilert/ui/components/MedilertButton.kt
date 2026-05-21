package com.nkwabyte.medilert.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nkwabyte.medilert.ui.theme.GhanaYellow
import com.nkwabyte.medilert.ui.theme.Poppins
import com.nkwabyte.medilert.ui.theme.PrimaryGreen
import com.nkwabyte.medilert.ui.theme.Surface
import com.nkwabyte.medilert.ui.theme.TextPrimary

@Composable
fun MedilertPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick, enabled = enabled,
        modifier = modifier.fillMaxWidth().height(60.dp),
        shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryGreen, contentColor = Surface,
            disabledContainerColor = Color(0xFFE5E7EB), disabledContentColor = Color(0xFF9CA3AF)
        )
    ) {
        Text(text = text, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
    }
}

@Composable
fun MedilertSecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick, enabled = enabled,
        modifier = modifier.fillMaxWidth().height(60.dp),
        shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GhanaYellow, contentColor = TextPrimary,
            disabledContainerColor = Color(0xFFE5E7EB), disabledContentColor = Color(0xFF9CA3AF)
        )
    ) {
        Text(text = text, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
    }
}
