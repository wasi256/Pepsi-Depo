package com.example.pepsi.network.model

data class QuantityCreateRequest(
    val quantity: String,
)

data class QuantityResponse(
    val id: Int,
    val quantity: String,
)

data class QuantityListResponse(
    val items: List<QuantityResponse>,
    val total: Int,
    val page: Int,
    val page_size: Int,
)
