package com.example.pepsi.data.network.model

import com.google.gson.annotations.SerializedName

// --- Auth ---

data class LoginRequest(
    val email: String,
    val password: String,
)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    val user: CurrentUserResponse,
)

data class CurrentUserResponse(
    val id: Int,
    val username: String,
    @SerializedName("personnel_id") val personnelId: Int,
    @SerializedName("personnel_name") val personnelName: String,
    @SerializedName("role_id") val roleId: Int?,
    @SerializedName("role_name") val roleName: String?,
    val permissions: List<String>,
)

// --- Pagination envelope (Admin list endpoints) ---

data class PageResponse<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    @SerializedName("page_size") val pageSize: Int,
)

// --- Admin: products, quantities, depots, roles, personnel ---

data class ProductRead(
    val id: Int,
    val name: String,
)

data class QuantityRead(
    val id: Int,
    val quantity: String,
)

data class DepotRead(
    val id: Int,
    val name: String,
    val location: String,
)

data class RoleRead(
    val id: Int,
    val name: String,
)

data class PersonnelRead(
    val id: Int,
    @SerializedName("role_id") val roleId: Int?,
    @SerializedName("depot_id") val depotId: Int?,
    val name: String,
    val email: String?,
    val gender: String?,
    val contact: String,
    val salary: String?,
    @SerializedName("created_at") val createdAt: String,
)

// --- Factory: production ---

data class ProductionCreate(
    @SerializedName("product_id") val productId: Int,
    @SerializedName("quantity_id") val quantityId: Int,
    @SerializedName("quantity_produced") val quantityProduced: Int,
    @SerializedName("production_date") val productionDate: String? = null,
)

data class ProductionResponse(
    val id: Int,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("product_name") val productName: String,
    @SerializedName("quantity_id") val quantityId: Int?,
    @SerializedName("quantity_value") val quantityValue: String?,
    @SerializedName("quantity_produced") val quantityProduced: Int,
    @SerializedName("production_date") val productionDate: String,
    @SerializedName("created_date") val createdDate: String,
)

// --- Factory: current stock ---

data class FactoryStockResponse(
    val id: Int,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("product_name") val productName: String,
    @SerializedName("quantity_id") val quantityId: Int?,
    @SerializedName("quantity_value") val quantityValue: String?,
    @SerializedName("available_quantity") val availableQuantity: Int,
    @SerializedName("updated_date") val updatedDate: String,
)

// --- Factory: supplies ---

data class SupplyCreate(
    @SerializedName("product_id") val productId: Int,
    @SerializedName("quantity_id") val quantityId: Int,
    val amount: Int,
    @SerializedName("depot_id") val depotId: Int,
    @SerializedName("supplier_id") val supplierId: Int,
)

data class SupplyResponse(
    val id: Int,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("quantity_id") val quantityId: Int,
    val amount: Int,
    @SerializedName("product_name") val productName: String,
    @SerializedName("quantity_value") val quantityValue: String,
    @SerializedName("depot_id") val depotId: Int?,
    @SerializedName("depot_name") val depotName: String?,
    @SerializedName("supplier_id") val supplierId: Int?,
    @SerializedName("supplier_name") val supplierName: String?,
    val status: String,
    @SerializedName("rejection_reason") val rejectionReason: String?,
    @SerializedName("created_date") val createdDate: String,
)

// --- Errors ---

data class ValidationErrorDetail(
    val loc: List<Any>,
    val msg: String,
    val type: String,
)

data class HttpValidationError(
    val detail: List<ValidationErrorDetail>,
)
