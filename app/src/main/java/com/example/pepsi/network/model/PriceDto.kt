package com.example.pepsi.network.model

data class PriceCreateRequest(
    val quantity_id: Int,
    val amount: Double,
)

data class PriceUpdateRequest(
    val amount: Double,
)

data class PriceResponse(
    val id: Int,
    val quantity_id: Int,
    val amount: Double,
)

data class PriceListResponse(
    val items: List<PriceResponse>,
    val total: Int,
    val page: Int,
    val page_size: Int,
)
