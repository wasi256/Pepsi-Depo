package com.example.pepsi.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.pepsi.network.RetrofitClient
import com.example.pepsi.network.model.AuthUser
import com.example.pepsi.network.model.LoginResponse
import com.google.gson.Gson

/**
 * The signed-in user, their access token and the permissions the API granted them.
 *
 * [user] and [permissions] are Compose state, so any composable that reads them
 * (directly or through [AppAccess]) recomposes when the user signs in or out.
 * The session is persisted so it survives an app restart; a 401 from the API
 * signs the user out again (see [RetrofitClient]).
 */
object AuthSession {

    private const val PREFS_NAME = "pepsi_auth_session"
    private const val KEY_TOKEN = "token"
    private const val KEY_USER = "user"

    private val gson = Gson()
    private var prefs: SharedPreferences? = null

    @Volatile
    var token: String? = null
        private set

    var user: AuthUser? by mutableStateOf(null)
        private set

    var permissions: Set<String> by mutableStateOf(emptySet())
        private set

    /** The depot the signed-in personnel belongs to, when it could be resolved. */
    var depotId: Int? by mutableStateOf(null)
        private set

    val isSignedIn: Boolean get() = user != null && token != null

    fun init(context: Context) {
        val store = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = store
        val savedToken = store.getString(KEY_TOKEN, null)
        val savedUser = store.getString(KEY_USER, null)?.let {
            runCatching { gson.fromJson(it, AuthUser::class.java) }.getOrNull()
        }
        if (savedToken != null && savedUser != null) apply(savedToken, savedUser)
    }

    fun signIn(response: LoginResponse) {
        apply(response.access_token, response.user)
        prefs?.edit()
            ?.putString(KEY_TOKEN, response.access_token)
            ?.putString(KEY_USER, gson.toJson(response.user))
            ?.apply()
    }

    fun signOut() {
        token = null
        user = null
        permissions = emptySet()
        depotId = null
        prefs?.edit()?.clear()?.apply()
    }

    fun has(permission: String): Boolean = permission in permissions

    /**
     * Looks up which depot the signed-in personnel is assigned to. Best effort:
     * a role without `admin.personnel:read` simply gets no depot filter, and the
     * depot endpoints then return whatever the API scopes to that user.
     */
    suspend fun resolveDepot() {
        val personnelId = user?.personnel_id ?: return
        depotId = try {
            val response = RetrofitClient.adminApi.getPersonnel(personnelId)
            if (response.isSuccessful) response.body()?.depot_id else null
        } catch (e: Exception) {
            null
        }
    }

    private fun apply(newToken: String, newUser: AuthUser) {
        token = newToken
        user = newUser
        permissions = newUser.permissions.orEmpty().toSet()
    }
}
