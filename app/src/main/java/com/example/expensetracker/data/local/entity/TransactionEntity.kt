package com.example.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity (tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val amount: Double,
    val currency: String = "INR", // Default to INR
    val category: String,
    val date: Date,
    val note: String? = null,
    val paymentMethod: String
)