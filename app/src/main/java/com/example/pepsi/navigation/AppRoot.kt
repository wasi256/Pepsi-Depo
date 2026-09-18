package com.example.pepsi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.pepsi.auth.AppAccess
import com.example.pepsi.auth.AuthSession
import com.example.pepsi.auth.CreateAccountScreen
import com.example.pepsi.auth.LoginScreen
import com.example.pepsi.auth.NoAccessScreen

/**
 * Top of the UI: sign-in and account creation while nobody is signed in, then the
 * app itself, starting on the first section the user's permissions allow.
 */
@Composable
fun AppRoot() {
    val user = AuthSession.user

    if (user == null) {
        var creatingAccount by rememberSaveable { mutableStateOf(false) }
        if (creatingAccount) {
            CreateAccountScreen(
                onBack = { creatingAccount = false },
                onCreated = { creatingAccount = false },
            )
        } else {
            LoginScreen(onCreateAccount = { creatingAccount = true })
        }
        return
    }

    val home = AppAccess.homeRoute()
    if (home == null) {
        NoAccessScreen()
        return
    }

    // Keyed on the user so signing out and back in as someone else starts from a clean navigation stack.
    key(user.id) {
        LaunchedEffect(user.id) { AuthSession.resolveDepot() }
        PepsiNavHost(startRoute = home)
    }
}
