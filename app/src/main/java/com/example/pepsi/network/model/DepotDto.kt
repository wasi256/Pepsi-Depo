package com.example.pepsi.network.model

data class DepotCreateRequest(
    val name: String,
    val location: String,
)

data class DepotResponse(
    val id: Int,
    val name: String,
    val location: String,
)

data class DepotListResponse(
    val items: List<DepotResponse>,
    val total: Int,
    val page: Int,
    val page_size: Int,
)
