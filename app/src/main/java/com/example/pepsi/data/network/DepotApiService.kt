package com.example.pepsi.data.network

import com.example.pepsi.data.network.model.AdminDepotDto
import com.example.pepsi.data.network.model.DepotStockDto
import com.example.pepsi.data.network.model.PaginatedResponse
import com.example.pepsi.data.network.model.RestockConfirmRequest
import com.example.pepsi.data.network.model.RestockPage
import com.example.pepsi.data.network.model.RestockRejectRequest
import com.example.pepsi.data.network.model.RestockResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Endpoints documented at https://pepsi-depot-demo.onrender.com/docs
 */
interface DepotApiService {

    @GET("admin/depots")
    suspend fun getDepots(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 10,
    ): PaginatedResponse<AdminDepotDto>

    @GET("depot/restock/{entry_id}")
    suspend fun getRestockEntry(@Path("entry_id") entryId: Int): RestockResponse

    @POST("depot/restock/{supply_history_id}/confirm")
    suspend fun confirmRestock(
        @Path("supply_history_id") supplyHistoryId: Int,
        @Body request: RestockConfirmRequest,
    ): RestockResponse

    @POST("depot/restock/{supply_history_id}/reject")
    suspend fun rejectRestock(
        @Path("supply_history_id") supplyHistoryId: Int,
        @Body request: RestockRejectRequest,
    ): RestockResponse

    @GET("depot/restock")
    suspend fun getRestockHistory(
        @Query("depot_id") depotId: Int? = null,
        @Query("status") status: String? = null,
        @Query("product_name") productName: String? = null,
        @Query("quantity") quantity: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 10,
    ): RestockPage

    @GET("depot/stock")
    suspend fun getDepotStock(@Query("depot_id") depotId: Int? = null): List<DepotStockDto>
}
