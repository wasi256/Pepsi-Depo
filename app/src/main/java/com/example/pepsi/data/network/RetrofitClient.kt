package com.example.pepsi.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Single Retrofit instance for the Pepsi Depo backend, hosted at pepsi-depot-demo.onrender.com
 * (Swagger docs: https://pepsi-depot-demo.onrender.com/docs). Render's free tier spins the
 * service down when idle, so the first request after a while can take a while to respond —
 * timeouts are set generously to account for that cold start.
 */
object RetrofitClient {

    private const val BASE_URL = "https://pepsi-depot-demo.onrender.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun <T> createService(serviceClass: Class<T>): T = instance.create(serviceClass)
}
