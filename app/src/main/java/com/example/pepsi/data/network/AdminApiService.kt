package com.example.pepsi.data.network

import com.example.pepsi.data.network.model.DepotRead
import com.example.pepsi.data.network.model.PageResponse
import com.example.pepsi.data.network.model.PersonnelRead
import com.example.pepsi.data.network.model.ProductRead
import com.example.pepsi.data.network.model.QuantityRead
import com.example.pepsi.data.network.model.RoleRead
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AdminApiService {

    @GET("admin/products")
    suspend fun getProducts(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 100,
    ): PageResponse<ProductRead>

    @GET("admin/products/{product_id}")
    suspend fun getProduct(@Path("product_id") productId: Int): ProductRead

    @GET("admin/quantities")
    suspend fun getQuantities(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 100,
    ): PageResponse<QuantityRead>

    @GET("admin/quantities/{quantity_id}")
    suspend fun getQuantity(@Path("quantity_id") quantityId: Int): QuantityRead

    @GET("admin/depots")
    suspend fun getDepots(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 100,
    ): PageResponse<DepotRead>

    @GET("admin/roles")
    suspend fun getRoles(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 100,
    ): PageResponse<RoleRead>

    @GET("admin/personnel")
    suspend fun getPersonnel(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 100,
        @Query("role_id") roleId: Int? = null,
        @Query("depot_id") depotId: Int? = null,
    ): PageResponse<PersonnelRead>

    @GET("admin/personnel/{personnel_id}")
    suspend fun getPersonnelById(@Path("personnel_id") personnelId: Int): PersonnelRead
}
