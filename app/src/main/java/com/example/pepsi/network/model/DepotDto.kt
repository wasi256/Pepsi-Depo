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
