package com.example.pepsi.network.model

data class RoleCreateRequest(
    val name: String,
)

data class RoleResponse(
    val id: Int,
    val name: String,
)
