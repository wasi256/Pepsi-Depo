package com.example.pepsi.network.model

data class ProductCreateRequest(
    val name: String,
)

data class ProductResponse(
    val id: Int,
    val name: String,
)

data class ProductListResponse(
    val items: List<ProductResponse>,
    val total: Int,
    val page: Int,
    val page_size: Int,
)
