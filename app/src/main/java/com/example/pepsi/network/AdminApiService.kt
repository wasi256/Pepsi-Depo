package com.example.pepsi.network

import com.example.pepsi.network.model.DepotCreateRequest
import com.example.pepsi.network.model.DepotResponse
import com.example.pepsi.network.model.PersonnelCreateRequest
import com.example.pepsi.network.model.PersonnelResponse
import com.example.pepsi.network.model.PersonnelRoleAssignRequest
import com.example.pepsi.network.model.RoleCreateRequest
import com.example.pepsi.network.model.RoleResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface AdminApiService {

    @POST("admin/roles")
    suspend fun createRole(@Body body: RoleCreateRequest): Response<RoleResponse>

    @POST("admin/personnel")
    suspend fun registerPersonnel(@Body body: PersonnelCreateRequest): Response<PersonnelResponse>

    @PATCH("admin/personnel/{personnel_id}/role")
    suspend fun assignPersonnelRole(
        @Path("personnel_id") personnelId: Int,
        @Body body: PersonnelRoleAssignRequest,
    ): Response<PersonnelResponse>

    @POST("admin/depots")
    suspend fun createDepot(@Body body: DepotCreateRequest): Response<DepotResponse>
}
