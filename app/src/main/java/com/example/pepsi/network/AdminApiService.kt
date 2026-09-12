package com.example.pepsi.network

import com.example.pepsi.network.model.PersonnelCreateRequest
import com.example.pepsi.network.model.PersonnelResponse
import com.example.pepsi.network.model.RoleCreateRequest
import com.example.pepsi.network.model.RoleResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AdminApiService {

    @POST("admin/roles")
    suspend fun createRole(@Body body: RoleCreateRequest): Response<RoleResponse>

    @POST("admin/personnel")
    suspend fun registerPersonnel(@Body body: PersonnelCreateRequest): Response<PersonnelResponse>
}
