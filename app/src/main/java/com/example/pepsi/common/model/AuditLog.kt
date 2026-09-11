package com.example.pepsi.common.model

import java.util.Date

data class AuditLog(
    val id: String,
    val timestamp: Date,
    val actor: String,
    val action: String,
    val details: String,
)
