package com.example.pepsi.network

import com.example.pepsi.network.model.DepotCreateRequest
import com.example.pepsi.network.model.DepotListResponse
import com.example.pepsi.network.model.DepotResponse
import com.example.pepsi.network.model.PersonnelCreateRequest
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

    @POST("admin/products")
    suspend fun createProduct(@Body body: ProductCreateRequest): Response<ProductResponse>

    @GET("admin/products")
    suspend fun listProducts(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 100,
    ): Response<ProductListResponse>

    @GET("admin/products/{product_id}")
    suspend fun getProduct(@Path("product_id") productId: Int): Response<ProductResponse>

    @POST("admin/quantities")
    suspend fun createQuantity(@Body body: QuantityCreateRequest): Response<QuantityResponse>

    @GET("admin/quantities")
    suspend fun listQuantities(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 100,
    ): Response<QuantityListResponse>

    @GET("admin/quantities/{quantity_id}")
    suspend fun getQuantity(@Path("quantity_id") quantityId: Int): Response<QuantityResponse>

    @POST("admin/prices")
    suspend fun createPrice(@Body body: PriceCreateRequest): Response<PriceResponse>

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
