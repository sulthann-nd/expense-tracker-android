package com.example.expensetracker.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.entity.TransactionEntity
import com.example.expensetracker.data.local.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class TransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    // 🔹 All transactions (for list screen)
    val transactions: StateFlow<List<TransactionEntity>> =
        repository.allTransactions
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // 🔹 Selected transaction (for edit screen)
    private val _selectedTransaction = MutableStateFlow<TransactionEntity?>(null)
    val selectedTransaction: StateFlow<TransactionEntity?> =
        _selectedTransaction.asStateFlow()

    // 🔹 Load one transaction by ID
    fun loadTransaction(id: UUID?) {
        if (id == null) return

        viewModelScope.launch {
            _selectedTransaction.value = repository.getById(id)
        }
    }

    // 🔹 Add new transaction
    fun addTransaction(
        amount: Double,
        currency: String = "INR",
        category: String,
        date: Date,
        paymentMethod: String,
        note: String?
    ) {
        val transaction = TransactionEntity(
            id = UUID.randomUUID(),
            amount = amount,
            currency = currency,
            category = category,
            date = date,
            paymentMethod = paymentMethod,
            note = note
        )

        viewModelScope.launch {
            repository.insert(transaction)
        }
    }

    // 🔹 Update existing transaction
    fun updateTransaction(
        id: UUID,
        amount: Double,
        currency: String = "INR",
        category: String,
        date: Date,
        paymentMethod: String,
        note: String?
    ) {
        val updated = TransactionEntity(
            id = id,
            amount = amount,
            currency = currency,
            category = category,
            date = date,
            paymentMethod = paymentMethod,
            note = note
        )

        viewModelScope.launch {
            repository.update(updated)
        }
    }

    // 🔹 Delete transaction
    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.delete(transaction)
        }
    }
}
