package com.nkwabyte.medilert.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.nkwabyte.medilert.navigation.AppDestination
import com.nkwabyte.medilert.navigation.Splash

class NavViewModel : ViewModel() {

    val backStack = mutableStateListOf<AppDestination>(Splash)

    /** Push [destination] onto the back stack. No-op if it's already the top entry. */
    fun navigateTo(destination: AppDestination) {
        if (backStack.lastOrNull() != destination) {
            backStack.add(destination)
        }
    }

    /** Clear the entire back stack and navigate to [destination]. Useful for root-level transitions (e.g. login → dashboard). */
    fun navigateAndClearStack(destination: AppDestination) {
        backStack.clear()
        backStack.add(destination)
    }

    /** Replace the current top entry with [destination] without adding to history. */
    fun replaceTop(destination: AppDestination) {
        if (backStack.isNotEmpty()) backStack.removeAt(backStack.size - 1)
        backStack.add(destination)
    }

    /** Go back one step. No-op if the back stack has only one entry. */
    fun popBack() {
        if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
    }
}
