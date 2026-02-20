package com.example.expensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.entity.TransactionEntity
import com.example.expensetracker.data.local.repository.TransactionRepository
import com.example.expensetracker.utils.CurrencyConverter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.Date

class DashboardViewModel(
    private val repository: TransactionRepository,
    private val exchangeRateViewModel: ExchangeRateViewModel
) : ViewModel() {

    init {
        // Fetch latest exchange rates for currency conversion
        exchangeRateViewModel.fetchLatestRates()
    }

    // All expenses from Room database
    val allExpenses: StateFlow<List<TransactionEntity>> by lazy {
        repository.allTransactions
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // Filtered: Today's Spending
    val todaysSpending: StateFlow<Double> = combine(
        allExpenses,
        exchangeRateViewModel.latestRates
    ) { list, ratesResult ->
        val rates = ratesResult?.getOrNull()
        val calendar = Calendar.getInstance()
        list.filter { isSameDay(it.date, calendar.time) }
            .sumOf { CurrencyConverter.convertToINR(it.currency, it.amount, rates) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Filtered: This Month's Spending
    val thisMonthSpending: StateFlow<Double> = combine(
        allExpenses,
        exchangeRateViewModel.latestRates
    ) { list, ratesResult ->
        val rates = ratesResult?.getOrNull()
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        list.filter {
            val d = Calendar.getInstance().apply { time = it.date }
            d.get(Calendar.MONTH) == currentMonth && d.get(Calendar.YEAR) == currentYear
        }.sumOf { CurrencyConverter.convertToINR(it.currency, it.amount, rates) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Helper for date comparison
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}