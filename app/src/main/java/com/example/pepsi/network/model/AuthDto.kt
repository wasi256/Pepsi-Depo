package com.example.pepsi.network.model

data class LoginRequest(
    val email: String,
    val password: String,
)

data class AuthUser(
    val id: Int,
    val username: String?,
    val personnel_id: Int?,
    val personnel_name: String?,
    val role_id: Int?,
    val role_name: String?,
    val permissions: List<String>?,
)

data class LoginResponse(
    val access_token: String,
    val token_type: String?,
    val user: AuthUser,
)

data class AuthUserCreateRequest(
    val username: String,
    val password: String,
    val personnel_id: Int,
)

data class AuthUserResponse(
    val id: Int,
    val username: String,
    val personnel_id: Int?,
    val created_at: String?,
)

data class ModuleResponse(
    val id: Int,
    val key: String,
    val name: String?,
    val description: String?,
)

data class PermissionResponse(
    val id: Int,
    val module_id: Int,
    val module_key: String,
    val module_name: String?,
    val action: String,
)

data class RolePermissionsRequest(
    val permission_ids: List<Int>,
)
