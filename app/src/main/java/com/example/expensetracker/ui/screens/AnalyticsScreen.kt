package com.example.expensetracker.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.expensetracker.ui.components.DailyLineChart
import com.example.expensetracker.ui.components.DonutChart
import com.example.expensetracker.ui.components.LegendRow
import com.example.expensetracker.ui.components.MonthPickerBottomSheet
import com.example.expensetracker.ui.components.MonthSelectorButton
import com.example.expensetracker.ui.components.SummaryRow
import com.example.expensetracker.ui.viewmodel.AnalyticsViewModel
import java.time.ZoneId
import java.util.Locale
import com.example.expensetracker.ui.components.CategorySlice as ComponentCategorySlice

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel) {
    val transactions by viewModel.filteredTransactions.collectAsState()
    val selectedDate by viewModel.selectedMonthDate.collectAsState()
    val slices by viewModel.slices.collectAsState()

    val totalAmount = transactions.sumOf { it.amount }
    val dailySeries = viewModel.getDailySeries(transactions)
    val avgSpend = if (dailySeries.isNotEmpty()) dailySeries.average() else 0.0
    val topCategory = slices.maxByOrNull { it.percent }?.name ?: "—"
    
    // Convert Date to LocalDate
    val selectedLocalDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    
    // Convert ViewModel CategorySlice to Component CategorySlice
    val componentSlices = slices.map { slice ->
        ComponentCategorySlice(
            id = slice.id,
            name = slice.name,
            percent = slice.percent,
            color = slice.color
        )
    }

    var showMonthPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7)) // systemGroupedBackground
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        // Month Selector
        Row(Modifier.padding(16.dp)) {
            MonthSelectorButton(
                date = selectedLocalDate,
                onClick = { showMonthPicker = true }
            )
        }

        // Spending by Category Card
        AnalyticsCard {
            Text("Spending by Category", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                DonutChart(slices = componentSlices, modifier = Modifier.size(140.dp))
                Column(Modifier.padding(start = 16.dp)) {
                    componentSlices.forEach { slice ->
                        LegendRow(slice)
                    }
                }
            }
        }

        // Daily Spending Card
        AnalyticsCard {
            Text("Daily Spending (Last 7 days)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            DailyLineChart(values = dailySeries, modifier = Modifier.height(120.dp).fillMaxWidth())
        }

        // Summary Card
        AnalyticsCard {
            SummaryRow("Top Category", topCategory)
            HorizontalDivider(Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
            SummaryRow("Average Daily Spend", "$${String.format(Locale.getDefault(), "%.2f", avgSpend)}")
            HorizontalDivider(Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
            SummaryRow("Total This Month", "$${String.format(Locale.getDefault(), "%.2f", totalAmount)}", highlight = true)
        }
    }

    if (showMonthPicker) {
        MonthPickerBottomSheet(
            currentDate = selectedLocalDate,
            onDismiss = { showMonthPicker = false },
            onDateSelected = { y, m -> viewModel.updateSelectedMonth(y, m) }
        )
    }
}

@Composable
fun AnalyticsCard(content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) { content() }
    }
}