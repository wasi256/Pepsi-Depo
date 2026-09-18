package com.example.pepsi.network

import com.example.pepsi.network.model.AuthUserCreateRequest
import com.example.pepsi.network.model.AuthUserResponse
import com.example.pepsi.network.model.LoginRequest
import com.example.pepsi.network.model.LoginResponse
import com.example.pepsi.network.model.ModuleResponse
import com.example.pepsi.network.model.PermissionResponse
import com.example.pepsi.network.model.RolePermissionsRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("auth/users")
    suspend fun createUsers(@Body body: List<AuthUserCreateRequest>): Response<List<AuthUserResponse>>

    @GET("auth/modules")
    suspend fun listModules(): Response<List<ModuleResponse>>

    @GET("auth/permissions")
    suspend fun listPermissions(): Response<List<PermissionResponse>>

    @GET("auth/roles/{role_id}/permissions")
    suspend fun listRolePermissions(@Path("role_id") roleId: Int): Response<List<PermissionResponse>>

    @POST("auth/roles/{role_id}/permissions")
    suspend fun assignRolePermissions(
        @Path("role_id") roleId: Int,
        @Body body: RolePermissionsRequest,
    ): Response<List<PermissionResponse>>

    @DELETE("auth/roles/{role_id}/permissions/{permission_id}")
    suspend fun revokeRolePermission(
        @Path("role_id") roleId: Int,
        @Path("permission_id") permissionId: Int,
    ): Response<Unit>
}
