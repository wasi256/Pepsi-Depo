package com.example.pepsi.network.model

data class RoleCreateRequest(
    val name: String,
)

data class RoleResponse(
    val id: Int,
    val name: String,
)

data class RoleListResponse(
    val items: List<RoleResponse>,
    val total: Int,
    val page: Int,
    val page_size: Int,
)
