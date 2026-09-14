package com.example.pepsi.network

import com.example.pepsi.network.model.ValidationErrorResponse
import com.google.gson.Gson
import retrofit2.Response

/** Best-effort human-readable message from a failed response's error body. */
fun Response<*>.readErrorMessage(): String {
    val body = errorBody()?.string()
    if (body.isNullOrBlank()) return "Request failed (${code()})"
    return try {
        val parsed = Gson().fromJson(body, ValidationErrorResponse::class.java)
        parsed.detail
            ?.joinToString("; ") { "${it.loc?.lastOrNull()}: ${it.msg}" }
            ?.takeIf { it.isNotBlank() }
            ?: "Request failed (${code()})"
    } catch (e: Exception) {
        "Request failed (${code()}): $body"
    }
}
