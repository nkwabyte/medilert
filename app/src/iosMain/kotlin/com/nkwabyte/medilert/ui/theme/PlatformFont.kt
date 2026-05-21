package com.nkwabyte.medilert.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.nkwabyte.medilert.generated.resources.Res
import com.nkwabyte.medilert.generated.resources.poppins_bold
import com.nkwabyte.medilert.generated.resources.poppins_medium
import com.nkwabyte.medilert.generated.resources.poppins_regular
import com.nkwabyte.medilert.generated.resources.poppins_semibold
import org.jetbrains.compose.resources.Font

@Composable
internal actual fun loadPoppins(): FontFamily = FontFamily(
    Font(Res.font.poppins_regular, FontWeight.Normal),
    Font(Res.font.poppins_medium, FontWeight.Medium),
    Font(Res.font.poppins_semibold, FontWeight.SemiBold),
    Font(Res.font.poppins_bold, FontWeight.Bold)
)
