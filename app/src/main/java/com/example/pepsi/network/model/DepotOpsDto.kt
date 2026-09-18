package com.example.pepsi.network.model

// Depot module: current sales and stock, sales, and restock (receiving stock from the factory).

data class CurrentSaleResponse(
    val id: Int,
    val depot_id: Int?,
    val depot_name: String?,
    val product_id: Int?,
    val product_name: String?,
    val quantity_id: Int?,
    val quantity_value: String?,
    val sale_date: String?,
    val quantity_sold: Int,
    val sold_amount: Double,
)

data class CurrentStockResponse(
    val id: Int,
    val depot_id: Int?,
    val depot_name: String?,
    val product_id: Int,
    val product_name: String?,
    val quantity_id: Int,
    val quantity_value: String?,
    val current_amount: Int,
    val updated_at: String?,
)

data class SaleCreateRequest(
    val depot_id: Int,
    val product_id: Int,
    val quantity_id: Int,
    val quantity_sold: Int,
    val amount_sold: Double,
    val sold_by_id: Int,
)

data class SaleUpdateRequest(
    val quantity_sold: Int,
    val amount_sold: Double,
)

data class SaleResponse(
    val id: Int,
    val depot_id: Int?,
    val depot_name: String?,
    val product_id: Int?,
    val product_name: String?,
    val quantity_id: Int?,
    val quantity_value: String?,
    val quantity_sold: Int,
    val amount_sold: Double,
    val sold_by_id: Int?,
    val sale_date: String?,
    val sale_time: String?,
)

data class SaleListResponse(
    val items: List<SaleResponse>,
    val total: Int,
    val page: Int,
    val page_size: Int,
    val total_pages: Int,
)

data class RestockConfirmRequest(
    val quantity_received: Int,
    val confirmed_by_id: Int,
)

data class RestockRejectRequest(
    val reason: String,
    val quantity_received: Int,
    val confirmed_by_id: Int,
)

data class RestockUpdateRequest(
    val quantity_delivered: Int,
)

data class RestockResponse(
    val id: Int,
    val supply_history_id: Int?,
    val depot_id: Int?,
    val depot_name: String?,
    val product_id: Int?,
    val product_name: String?,
    val quantity_id: Int?,
    val quantity_value: String?,
    val quantity_delivered: Int,
    val supplier_id: Int?,
    val confirmed_by_id: Int?,
    val status: String?,
    val rejection_reason: String?,
    val restock_date: String?,
)

data class RestockListResponse(
    val items: List<RestockResponse>,
    val total: Int,
    val page: Int,
    val page_size: Int,
    val total_pages: Int,
)

/** A factory supply headed to a depot; `pending` ones are what a depot attendant still has to confirm or reject. */
data class SupplyHistoryResponse(
    val id: Int,
    val product_id: Int?,
    val quantity_id: Int?,
    val amount: Int,
    val product_name: String?,
    val quantity_value: String?,
    val status: String?,
    val rejection_reason: String?,
    val created_date: String?,
)
