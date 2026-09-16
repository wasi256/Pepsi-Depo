package com.example.pepsi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.pepsi.data.repository.FactoryRepository
import com.example.pepsi.data.session.SessionManager
import com.example.pepsi.navigation.PepsiNavHost
import com.example.pepsi.theme.PepsiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            LaunchedEffect(Unit) {
                val savedToken = SessionManager.restoreSavedToken(context)
                if (savedToken != null) {
                    FactoryRepository.fetchCurrentUser()
                        .onSuccess { user -> SessionManager.onLoggedIn(context, savedToken, user) }
                        .onFailure { SessionManager.logOut(context) }
                }
                SessionManager.finishRestoring()
            }
            PepsiTheme {
                PepsiNavHost()
            }
        }
    }
}
