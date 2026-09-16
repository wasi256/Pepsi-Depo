package com.example.pepsi.data.network

import com.example.pepsi.data.network.model.CurrentUserResponse
import com.example.pepsi.data.network.model.LoginRequest
import com.example.pepsi.data.network.model.TokenResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @GET("auth/me")
    suspend fun me(): CurrentUserResponse
}
