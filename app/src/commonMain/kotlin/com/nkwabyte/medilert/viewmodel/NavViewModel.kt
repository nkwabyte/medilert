package com.nkwabyte.medilert.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.nkwabyte.medilert.navigation.AppDestination
import com.nkwabyte.medilert.navigation.Splash

class NavViewModel : ViewModel() {

    val backStack = mutableStateListOf<AppDestination>(Splash)

    fun navigateTo(destination: AppDestination) {
        if (backStack.lastOrNull() != destination) {
            backStack.add(destination)
        }
    }

    fun navigateAndClearStack(destination: AppDestination) {
        backStack.clear()
        backStack.add(destination)
    }

    fun replaceTop(destination: AppDestination) {
        if (backStack.isNotEmpty()) backStack.removeAt(backStack.size - 1)
        backStack.add(destination)
    }

    fun popBack() {
        if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
    }
}
