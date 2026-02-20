package com.example.expensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.entity.TransactionEntity
import com.example.expensetracker.data.local.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class EditTransactionViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val _transaction = MutableStateFlow<TransactionEntity?>(null)
    val transaction: StateFlow<TransactionEntity?> = _transaction

    // Equivalent to SwiftUI's loadExpenseIfNeeded()
    fun loadTransaction(id: UUID) {
        viewModelScope.launch {
            _transaction.value = repository.getById(id)
        }
    }

    // Equivalent to SwiftUI's saveEditedExpense()
    fun saveTransaction(
        id: UUID?,
        amount: Double,
        currency: String = "INR",
        category: String,
        date: Date,
        paymentMethod: String,
        note: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                id = id ?: UUID.randomUUID(),
                amount = amount,
                currency = currency,
                category = category,
                date = date,
                paymentMethod = paymentMethod,
                note = if (note.isNullOrBlank()) null else note
            )

            if (id == null) {
                repository.insert(entity)
            } else {
                repository.update(entity)
            }
            onSuccess()
        }
    }

    // Equivalent to SwiftUI's performDelete()
    fun deleteTransaction(id: UUID, onDeleted: () -> Unit) {
        viewModelScope.launch {
            val entity = repository.getById(id)
            entity?.let {
                repository.delete(it)
                onDeleted()
            }
        }
    }
}