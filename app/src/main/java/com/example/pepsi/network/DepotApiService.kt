package com.example.pepsi.network

import com.example.pepsi.network.model.CurrentSaleResponse
import com.example.pepsi.network.model.CurrentStockResponse
import com.example.pepsi.network.model.RestockConfirmRequest
import com.example.pepsi.network.model.RestockListResponse
import com.example.pepsi.network.model.RestockRejectRequest
import com.example.pepsi.network.model.RestockResponse
import com.example.pepsi.network.model.RestockUpdateRequest
import com.example.pepsi.network.model.SaleCreateRequest
import com.example.pepsi.network.model.SaleListResponse
import com.example.pepsi.network.model.SaleResponse
import com.example.pepsi.network.model.SaleUpdateRequest
import com.example.pepsi.network.model.SupplyHistoryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface DepotApiService {

    @GET("depot/sales-current")
    suspend fun listCurrentSales(@Query("depot_id") depotId: Int? = null): Response<List<CurrentSaleResponse>>

    @GET("depot/stock")
    suspend fun listCurrentStock(@Query("depot_id") depotId: Int? = null): Response<List<CurrentStockResponse>>

    @POST("depot/sales")
    suspend fun recordSales(@Body body: List<SaleCreateRequest>): Response<List<SaleResponse>>

    @GET("depot/sales")
    suspend fun listSales(
        @Query("depot_id") depotId: Int? = null,
        @Query("product_name") productName: String? = null,
        @Query("quantity") quantity: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 10,
    ): Response<SaleListResponse>

    @GET("depot/sales/{sale_id}")
    suspend fun getSale(@Path("sale_id") saleId: Int): Response<SaleResponse>

    @PUT("depot/sales/{sale_id}")
    suspend fun updateSale(
        @Path("sale_id") saleId: Int,
        @Body body: SaleUpdateRequest,
    ): Response<SaleResponse>

    @DELETE("depot/sales/{sale_id}")
    suspend fun deleteSale(@Path("sale_id") saleId: Int): Response<Unit>

    @POST("depot/restock/{supply_history_id}/confirm")
    suspend fun confirmRestock(
        @Path("supply_history_id") supplyHistoryId: Int,
        @Body body: RestockConfirmRequest,
    ): Response<RestockResponse>

    @POST("depot/restock/{supply_history_id}/reject")
    suspend fun rejectRestock(
        @Path("supply_history_id") supplyHistoryId: Int,
        @Body body: RestockRejectRequest,
    ): Response<RestockResponse>

    @GET("depot/restock")
    suspend fun listRestock(
        @Query("depot_id") depotId: Int? = null,
        @Query("status") status: String? = null,
        @Query("product_name") productName: String? = null,
        @Query("quantity") quantity: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 10,
    ): Response<RestockListResponse>

    @GET("depot/restock/{entry_id}")
    suspend fun getRestockEntry(@Path("entry_id") entryId: Int): Response<RestockResponse>

    @PUT("depot/restock/{entry_id}")
    suspend fun updateRestockEntry(
        @Path("entry_id") entryId: Int,
        @Body body: RestockUpdateRequest,
    ): Response<RestockResponse>

    @DELETE("depot/restock/{entry_id}")
    suspend fun deleteRestockEntry(@Path("entry_id") entryId: Int): Response<Unit>
}

/** Factory supplies are where a depot finds the stock that is waiting to be confirmed. */
interface FactorySupplyApiService {

    @GET("factory/supplies")
    suspend fun listSupplies(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 10,
    ): Response<List<SupplyHistoryResponse>>
}
