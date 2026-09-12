package com.example.pepsi.network.model

/** Shape of a FastAPI 422 validation error response body. */
data class ValidationErrorResponse(
    val detail: List<ValidationErrorDetail>?,
)

data class ValidationErrorDetail(
    val loc: List<Any>?,
    val msg: String?,
    val type: String?,
)
