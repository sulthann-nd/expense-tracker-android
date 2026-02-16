package com.example.expensetracker.ui.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.entity.TransactionEntity
import com.example.expensetracker.data.local.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.Date
import java.util.UUID

data class CategorySlice(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val percent: Float,
    val color: Color
)

class AnalyticsViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val _selectedMonthDate = MutableStateFlow(Date())
    val selectedMonthDate: StateFlow<Date> = _selectedMonthDate

    // Combine all transactions from repository with the selected date filter
    val filteredTransactions: StateFlow<List<TransactionEntity>> by lazy {
        combine(
            repository.allTransactions,
            _selectedMonthDate
        ) { all, selectedDate ->
            val calendar = Calendar.getInstance()
            calendar.time = selectedDate
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)

            all.filter {
                val d = Calendar.getInstance().apply { time = it.date }
                d.get(Calendar.YEAR) == year && d.get(Calendar.MONTH) == month
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    val slices: StateFlow<List<CategorySlice>> by lazy {
        filteredTransactions.map { transactions ->
            val total = transactions.sumOf { it.amount }
            if (total <= 0) {
                listOf(CategorySlice(name = "No Data", percent = 1f, color = Color.Gray))
            } else {
                val grouped = transactions.groupBy { it.category }
                grouped.map { (category, list) ->
                    val catTotal = list.sumOf { it.amount }
                    CategorySlice(
                        name = category,
                        percent = (catTotal / total).toFloat(),
                        color = getCategoryColor(category)
                    )
                }.sortedByDescending { it.percent }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun updateSelectedMonth(year: Int, month: Int) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1) // Convert from 1-based to 0-based
        cal.set(Calendar.DAY_OF_MONTH, 1)
        _selectedMonthDate.value = cal.time
    }

    // Daily Series logic (last 7 days of selected month)
    fun getDailySeries(transactions: List<TransactionEntity>, days: Int = 7): List<Double> {
        val calendar = Calendar.getInstance()
        val series = mutableListOf<Double>()

        // Use selected month's end or today if current month
        val now = Calendar.getInstance()
        val selected = Calendar.getInstance().apply { time = _selectedMonthDate.value }

        val isCurrentMonth = now.get(Calendar.YEAR) == selected.get(Calendar.YEAR) &&
                now.get(Calendar.MONTH) == selected.get(Calendar.MONTH)

        val endDate = if (isCurrentMonth) now else {
            val cal = selected.clone() as Calendar
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            cal
        }

        for (i in (days - 1) downTo 0) {
            val day = endDate.clone() as Calendar
            day.add(Calendar.DAY_OF_YEAR, -i)

            val daySum = transactions.filter {
                val tCal = Calendar.getInstance().apply { time = it.date }
                tCal.get(Calendar.YEAR) == day.get(Calendar.YEAR) &&
                        tCal.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR)
            }.sumOf { it.amount }

            series.add(daySum)
        }
        return series
    }

    private fun getCategoryColor(category: String): Color {
        val colors = listOf(
            Color(0xFF4CAF50), // Green
            Color(0xFFFF9800), // Orange
            Color(0xFF2196F3), // Blue
            Color(0xFFF44336), // Red
            Color(0xFF9C27B0), // Purple
            Color(0xFF00BCD4), // Cyan
            Color(0xFFFFEB3B), // Yellow
            Color(0xFF795548), // Brown
            Color(0xFF607D8B), // Blue Grey
            Color(0xFFE91E63), // Pink
        )

        // For standard categories, use fixed colors
        return when (category) {
            "Shopping" -> colors[0]
            "Food" -> colors[1]
            "Transport" -> colors[2]
            "Entertainment" -> colors[3]
            "Bills" -> colors[4]
            else -> {
                // For custom categories, use hash to select color
                val index = category.hashCode().mod(colors.size)
                colors[index]
            }
        }
    }
}