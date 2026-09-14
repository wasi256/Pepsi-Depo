package com.example.pepsi.data.network.model

import com.google.gson.annotations.SerializedName

data class AdminDepotDto(
    val id: Int,
    val name: String,
    val location: String,
)

data class RestockConfirmRequest(
    @SerializedName("depot_id") val depotId: Int,
    @SerializedName("quantity_received") val quantityReceived: Int,
    @SerializedName("supplier_id") val supplierId: Int? = null,
    @SerializedName("confirmed_by_id") val confirmedById: Int? = null,
)

data class RestockRejectRequest(
    @SerializedName("depot_id") val depotId: Int,
    val reason: String,
    @SerializedName("quantity_received") val quantityReceived: Int? = null,
    @SerializedName("supplier_id") val supplierId: Int? = null,
    @SerializedName("confirmed_by_id") val confirmedById: Int? = null,
)

data class RestockResponse(
    val id: Int,
    @SerializedName("supply_history_id") val supplyHistoryId: Int,
    @SerializedName("depot_id") val depotId: Int,
    @SerializedName("depot_name") val depotName: String,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("product_name") val productName: String,
    @SerializedName("quantity_id") val quantityId: Int,
    @SerializedName("quantity_value") val quantityValue: String,
    @SerializedName("quantity_delivered") val quantityDelivered: Int,
    @SerializedName("supplier_id") val supplierId: Int?,
    @SerializedName("confirmed_by_id") val confirmedById: Int?,
    val status: String,
    @SerializedName("rejection_reason") val rejectionReason: String?,
    @SerializedName("restock_date") val restockDate: String,
)

/**
 * `/depot/restock`'s paged envelope, distinct from [PaginatedResponse] because it also
 * reports `total_pages`.
 */
data class RestockPage(
    val items: List<RestockResponse>,
    val total: Int,
    val page: Int,
    @SerializedName("page_size") val pageSize: Int,
    @SerializedName("total_pages") val totalPages: Int,
)

data class DepotStockDto(
    val id: Int,
    @SerializedName("depot_id") val depotId: Int,
    @SerializedName("depot_name") val depotName: String,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("product_name") val productName: String,
    @SerializedName("quantity_id") val quantityId: Int,
    @SerializedName("quantity_value") val quantityValue: String,
    @SerializedName("current_amount") val currentAmount: Int,
    @SerializedName("updated_at") val updatedAt: String,
)
