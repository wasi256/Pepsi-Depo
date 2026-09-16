package com.example.pepsi.data.session

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.pepsi.data.network.model.CurrentUserResponse

private const val PREFS_NAME = "pepsi_session"
private const val KEY_TOKEN = "token"

/**
 * Holds the current Factory API session (JWT + who's logged in) as Compose state so
 * any screen recomposes the moment login/logout happens. The token is persisted to
 * plain SharedPreferences so the manager doesn't have to sign in again every launch —
 * this is a demo backend, not a source of real secrets.
 */
object SessionManager {

    var token: String? by mutableStateOf(null)
        private set

    var currentUser: CurrentUserResponse? by mutableStateOf(null)
        private set

    /** True from app start until the saved token (if any) has been checked against /auth/me. */
    var isRestoring: Boolean by mutableStateOf(true)
        private set

    val isLoggedIn: Boolean
        get() = token != null && currentUser != null

    /** Synchronously loads a saved token, if any, so the caller can validate it. */
    fun restoreSavedToken(context: Context): String? {
        val saved = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_TOKEN, null)
        token = saved
        return saved
    }

    fun finishRestoring() {
        isRestoring = false
    }

    fun onLoggedIn(context: Context, accessToken: String, user: CurrentUserResponse) {
        token = accessToken
        currentUser = user
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TOKEN, accessToken)
            .apply()
    }

    fun logOut(context: Context) {
        token = null
        currentUser = null
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_TOKEN)
            .apply()
    }
}
