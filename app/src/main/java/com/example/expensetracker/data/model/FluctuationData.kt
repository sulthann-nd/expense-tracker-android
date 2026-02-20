package com.example.expensetracker.data.model

data class FluctuationData(
    val from: String,
    val to: String,
    val startRate: Double,
    val endRate: Double,
    val change: Double,
    val changePct: Double
)