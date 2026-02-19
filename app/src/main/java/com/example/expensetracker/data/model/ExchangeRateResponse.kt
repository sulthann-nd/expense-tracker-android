package com.example.expensetracker.data.model

data class ExchangeRateResponse(
    val success: Boolean,
    val timestamp: Long? = null,
    val base: String,
    val date: String,
    val rates: Map<String, Double>,
    // For historical, these might be null
    val historical: Boolean? = null,
    val error: ErrorResponse? = null
)

data class ErrorResponse(
    val code: Int,
    val type: String,
    val info: String
)