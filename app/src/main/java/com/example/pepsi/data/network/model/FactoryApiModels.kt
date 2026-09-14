package com.example.pepsi.data.network.model

import com.google.gson.annotations.SerializedName

data class AdminProductDto(
    val id: Int,
    val name: String,
)

data class AdminQuantityDto(
    val id: Int,
    val quantity: String,
)

/**
 * The Admin `products`/`quantities` endpoints wrap their results in a
 * pagination envelope rather than returning a bare array.
 */
data class PaginatedResponse<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    @SerializedName("page_size") val pageSize: Int,
)

data class ProductionRequest(
    @SerializedName("product_id") val productId: Int,
    @SerializedName("quantity_produced") val quantityProduced: Int,
    @SerializedName("production_date") val productionDate: String? = null,
)

data class ProductionResponse(
    val id: Int,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("product_name") val productName: String,
    @SerializedName("quantity_produced") val quantityProduced: Int,
    @SerializedName("production_date") val productionDate: String?,
    @SerializedName("created_date") val createdDate: String,
)

data class SupplyRequest(
    @SerializedName("product_id") val productId: Int,
    @SerializedName("quantity_id") val quantityId: Int,
    val amount: Int,
)

data class SupplyResponse(
    val id: Int,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("quantity_id") val quantityId: Int,
    val amount: Int,
    @SerializedName("product_name") val productName: String,
    @SerializedName("quantity_value") val quantityValue: String,
    val status: String,
    @SerializedName("rejection_reason") val rejectionReason: String?,
    @SerializedName("created_date") val createdDate: String,
)

data class FactoryStockDto(
    val id: Int,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("product_name") val productName: String,
    @SerializedName("available_quantity") val availableQuantity: Int,
    @SerializedName("updated_date") val updatedDate: String,
)
