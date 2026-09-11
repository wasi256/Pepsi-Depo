package com.example.pepsi.common.model

enum class Gender {
    MALE,
    FEMALE,
}

enum class Role(val label: String) {
    SYSTEM_ADMIN("System Admin"),
    MANAGER("Manager"),
    DEPO_ATTENDANT("Depo Attendant"),
}

data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val tel: String,
    val companyEmail: String,
    val password: String,
    val gender: Gender,
    val role: Role,
) {
    val fullName: String get() = "$firstName $lastName"
}
