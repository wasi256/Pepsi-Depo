package com.example.pepsi.network

import com.example.pepsi.auth.AuthSession
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Shared Retrofit instance for the Pepsi Depo ERP backend. Every request carries
 * the signed-in user's bearer token; a 401 on an authenticated call means the token
 * expired or was revoked, so the session is cleared and the app returns to sign-in.
 */
object RetrofitClient {
    private const val BASE_URL = "https://pepsi-depot-demo.onrender.com/"

    /** Full request/response bodies are only logged when this is set (debug builds). */
    @Volatile
    var logBodies: Boolean = false

    private fun logger(level: HttpLoggingInterceptor.Level) = HttpLoggingInterceptor().apply {
        this.level = level
        redactHeader("Authorization")
    }

    private val bodyLogger = logger(HttpLoggingInterceptor.Level.BODY)
    private val basicLogger = logger(HttpLoggingInterceptor.Level.BASIC)

    // /auth/* bodies carry passwords and access tokens, so they are never logged in full.
    private val loggingGate = Interceptor { chain ->
        val isAuthCall = chain.request().url.encodedPath.startsWith("/auth/")
        val logger = if (logBodies && !isAuthCall) bodyLogger else basicLogger
        logger.intercept(chain)
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val isLogin = original.url.encodedPath.endsWith("/auth/login")
        val token = AuthSession.token
        val request = if (token != null && !isLogin) {
            original.newBuilder().header("Authorization", "Bearer $token").build()
        } else {
            original
        }
        val response = chain.proceed(request)
        if (response.code == 401 && token != null && !isLogin) AuthSession.signOut()
        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingGate)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val adminApi: AdminApiService by lazy { instance.create(AdminApiService::class.java) }
    val authApi: AuthApiService by lazy { instance.create(AuthApiService::class.java) }
}
