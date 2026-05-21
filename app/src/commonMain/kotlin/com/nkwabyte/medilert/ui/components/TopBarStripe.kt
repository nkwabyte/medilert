package com.nkwabyte.medilert.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nkwabyte.medilert.ui.theme.GhanaRed
import com.nkwabyte.medilert.ui.theme.GhanaYellow
import com.nkwabyte.medilert.ui.theme.PrimaryGreen

@Composable
fun TopBarStripe(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().height(6.dp)) {
        Box(modifier = Modifier.weight(1f).height(6.dp).background(GhanaRed))
        Box(modifier = Modifier.weight(1f).height(6.dp).background(GhanaYellow))
        Box(modifier = Modifier.weight(1f).height(6.dp).background(PrimaryGreen))
    }
}
