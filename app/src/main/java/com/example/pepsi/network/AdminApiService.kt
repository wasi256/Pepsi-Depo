package com.example.pepsi.network

import com.example.pepsi.network.model.DepotCreateRequest
import com.example.pepsi.network.model.DepotListResponse
import com.example.pepsi.network.model.DepotResponse
import com.example.pepsi.network.model.PersonnelCreateRequest
import com.example.pepsi.network.model.PersonnelDepotAssignRequest
import com.example.pepsi.network.model.PersonnelListResponse
import com.example.pepsi.network.model.PersonnelResponse
import com.example.pepsi.network.model.PersonnelRoleAssignRequest
import com.example.pepsi.network.model.PriceCreateRequest
import com.example.pepsi.network.model.PriceListResponse
import com.example.pepsi.network.model.PriceResponse
import com.example.pepsi.network.model.PriceUpdateRequest
import com.example.pepsi.network.model.ProductCreateRequest
import com.example.pepsi.network.model.ProductListResponse
import com.example.pepsi.network.model.ProductResponse
import com.example.pepsi.network.model.QuantityCreateRequest
import com.example.pepsi.network.model.QuantityListResponse
import com.example.pepsi.network.model.QuantityResponse
import com.example.pepsi.network.model.RoleCreateRequest
import com.example.pepsi.network.model.RoleListResponse
import com.example.pepsi.network.model.RoleResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AdminApiService {

    // --- Roles ---

    @POST("admin/roles")
    suspend fun createRoles(@Body body: List<RoleCreateRequest>): Response<List<RoleResponse>>

    @GET("admin/roles")
    suspend fun listRoles(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 100,
    ): Response<RoleListResponse>

    @GET("admin/roles/{role_id}")
    suspend fun getRole(@Path("role_id") roleId: Int): Response<RoleResponse>

    @PUT("admin/roles/{role_id}")
    suspend fun updateRole(
        @Path("role_id") roleId: Int,
        @Body body: RoleCreateRequest,
    ): Response<RoleResponse>

    @DELETE("admin/roles/{role_id}")
    suspend fun deleteRole(@Path("role_id") roleId: Int): Response<Unit>

    // --- Personnel ---

    @POST("admin/personnel")
    suspend fun registerPersonnel(@Body body: List<PersonnelCreateRequest>): Response<List<PersonnelResponse>>

    @GET("admin/personnel")
    suspend fun listPersonnel(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 100,
        @Query("role_id") roleId: Int? = null,
        @Query("depot_id") depotId: Int? = null,
    ): Response<PersonnelListResponse>

    @GET("admin/personnel/{personnel_id}")
    suspend fun getPersonnel(@Path("personnel_id") personnelId: Int): Response<PersonnelResponse>

    @PUT("admin/personnel/{personnel_id}")
    suspend fun updatePersonnel(
        @Path("personnel_id") personnelId: Int,
        @Body body: PersonnelCreateRequest,
    ): Response<PersonnelResponse>

    @DELETE("admin/personnel/{personnel_id}")
    suspend fun deletePersonnel(@Path("personnel_id") personnelId: Int): Response<Unit>

    @PATCH("admin/personnel/{personnel_id}/role")
    suspend fun assignPersonnelRole(
        @Path("personnel_id") personnelId: Int,
        @Body body: PersonnelRoleAssignRequest,
    ): Response<PersonnelResponse>

    @PATCH("admin/personnel/{personnel_id}/depot")
    suspend fun assignPersonnelDepot(
        @Path("personnel_id") personnelId: Int,
        @Body body: PersonnelDepotAssignRequest,
    ): Response<PersonnelResponse>

    // --- Depots ---

    @POST("admin/depots")
    suspend fun createDepots(@Body body: List<DepotCreateRequest>): Response<List<DepotResponse>>

    @GET("admin/depots")
    suspend fun listDepots(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 100,
    ): Response<DepotListResponse>

    @GET("admin/depots/{depot_id}")
    suspend fun getDepot(@Path("depot_id") depotId: Int): Response<DepotResponse>

    @PUT("admin/depots/{depot_id}")
    suspend fun updateDepot(
        @Path("depot_id") depotId: Int,
        @Body body: DepotCreateRequest,
    ): Response<DepotResponse>

    @DELETE("admin/depots/{depot_id}")
    suspend fun deleteDepot(@Path("depot_id") depotId: Int): Response<Unit>

    // --- Products ---

    @POST("admin/products")
    suspend fun createProducts(@Body body: List<ProductCreateRequest>): Response<List<ProductResponse>>

    @GET("admin/products")
    suspend fun listProducts(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 100,
    ): Response<ProductListResponse>

    @GET("admin/products/{product_id}")
    suspend fun getProduct(@Path("product_id") productId: Int): Response<ProductResponse>

    @PUT("admin/products/{product_id}")
    suspend fun updateProduct(
        @Path("product_id") productId: Int,
        @Body body: ProductCreateRequest,
    ): Response<ProductResponse>

    @DELETE("admin/products/{product_id}")
    suspend fun deleteProduct(@Path("product_id") productId: Int): Response<Unit>

    // --- Quantities ---

    @POST("admin/quantities")
    suspend fun createQuantities(@Body body: List<QuantityCreateRequest>): Response<List<QuantityResponse>>

    @GET("admin/quantities")
    suspend fun listQuantities(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 100,
    ): Response<QuantityListResponse>

    @GET("admin/quantities/{quantity_id}")
    suspend fun getQuantity(@Path("quantity_id") quantityId: Int): Response<QuantityResponse>

    @PUT("admin/quantities/{quantity_id}")
    suspend fun updateQuantity(
        @Path("quantity_id") quantityId: Int,
        @Body body: QuantityCreateRequest,
    ): Response<QuantityResponse>

    @DELETE("admin/quantities/{quantity_id}")
    suspend fun deleteQuantity(@Path("quantity_id") quantityId: Int): Response<Unit>

    // --- Prices ---

    @POST("admin/prices")
    suspend fun createPrices(@Body body: List<PriceCreateRequest>): Response<List<PriceResponse>>

    @GET("admin/prices")
    suspend fun listPrices(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 100,
    ): Response<PriceListResponse>

    @GET("admin/prices/{quantity_id}")
    suspend fun getPrice(@Path("quantity_id") quantityId: Int): Response<PriceResponse>

    @PUT("admin/prices/{quantity_id}")
    suspend fun updatePrice(
        @Path("quantity_id") quantityId: Int,
        @Body body: PriceUpdateRequest,
    ): Response<PriceResponse>

    @DELETE("admin/prices/{quantity_id}")
    suspend fun deletePrice(@Path("quantity_id") quantityId: Int): Response<Unit>
}
