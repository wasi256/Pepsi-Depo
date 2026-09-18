package com.example.pepsi.network

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import retrofit2.Response

/**
 * Best-effort human-readable message from a failed response's error body. Handles
 * both FastAPI shapes: `{"detail": "Invalid credentials"}` and the 422 validation
 * list `{"detail": [{"loc": [...], "msg": "..."}]}`.
 */
fun Response<*>.readErrorMessage(): String {
    val fallback = "Request failed (${code()})"
    val body = errorBody()?.string()
    if (body.isNullOrBlank()) return fallback
    return try {
        val detail = (JsonParser.parseString(body) as? JsonObject)?.get("detail")
        when {
            detail == null || detail.isJsonNull -> fallback
            detail.isJsonPrimitive -> detail.asString.ifBlank { fallback }
            detail is JsonArray -> detail.mapNotNull { item ->
                val entry = item as? JsonObject ?: return@mapNotNull null
                val field = (entry.get("loc") as? JsonArray)?.lastOrNull()?.takeIf { it.isJsonPrimitive }?.asString
                val message = entry.get("msg")?.takeIf { it.isJsonPrimitive }?.asString ?: return@mapNotNull null
                if (field != null) "$field: $message" else message
            }.joinToString("; ").ifBlank { fallback }
            else -> fallback
        }
    } catch (e: Exception) {
        "$fallback: $body"
    }
}
