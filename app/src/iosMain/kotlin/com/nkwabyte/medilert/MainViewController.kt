package com.nkwabyte.medilert

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkwabyte.medilert.navigation.AppNavigation
import com.nkwabyte.medilert.ui.theme.MedilertTheme
import com.nkwabyte.medilert.viewmodel.AppViewModel

fun MainViewController() = ComposeUIViewController(configure = {
    enforceStrictPlistSanityCheck = false
}) {
    val appViewModel: AppViewModel = viewModel { AppViewModel() }
    val isDarkMode by appViewModel.isDarkMode.collectAsState()
    val fontScale  by appViewModel.fontScale.collectAsState()

    MedilertTheme(darkTheme = isDarkMode, fontScale = fontScale) {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppNavigation()
        }
    }
}
