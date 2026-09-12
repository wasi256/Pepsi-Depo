package com.example.pepsi.network.model

data class PersonnelCreateRequest(
    val role_id: Int,
    val depot_id: Int,
    val name: String,
    val email: String,
    val gender: String,
    val contact: String,
    val salary: Double,
)

data class PersonnelResponse(
    val id: Int,
    val role_id: Int?,
    val depot_id: Int?,
    val name: String,
    val email: String?,
    val gender: String,
    val contact: String,
    val salary: String?,
    val created_at: String?,
)
