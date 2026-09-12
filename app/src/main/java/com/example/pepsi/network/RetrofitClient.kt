package com.example.pepsi.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Shared Retrofit instance for the Pepsi Depo ERP backend. API service interfaces
 * (e.g. `RetrofitClient.instance.create(AdminApiService::class.java)`) are added
 * as the endpoints they cover are wired up.
 */
object RetrofitClient {
    private const val BASE_URL = "https://pepsi-depot-demo.onrender.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
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
}
