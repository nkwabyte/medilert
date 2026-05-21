package com.nkwabyte.medilert

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.nkwabyte.medilert.navigation.AppNavigation
import com.nkwabyte.medilert.ui.theme.MedilertTheme

fun MainViewController() = ComposeUIViewController {
    MedilertTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppNavigation()
        }
    }
}
