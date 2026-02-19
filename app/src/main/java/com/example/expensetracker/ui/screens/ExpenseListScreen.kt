package com.example.expensetracker.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.expensetracker.ui.components.ExpenseRow
import com.example.expensetracker.ui.transaction.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ExpenseListScreen(
    viewModel: TransactionViewModel,
    onAddExpense: () -> Unit,
    onEditExpense: (String) -> Unit,
    categories: Set<String> = setOf("Food", "Transport", "Shopping", "Entertainment", "Bills", "Others"),
    onRemoveCategory: (String) -> Unit = {}
) {
    val transactions by viewModel.transactions.collectAsState()

    // State for sorting and filtering
    var sortBy by remember { mutableStateOf(SortOption.DATE_DESC) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Filter and sort transactions
    val filteredTransactions = remember(transactions, selectedCategory) {
        if (selectedCategory == null) transactions else transactions.filter { it.category == selectedCategory }
    }

    val sortedTransactions = remember(filteredTransactions, sortBy) {
        when (sortBy) {
            SortOption.DATE_DESC -> filteredTransactions.sortedByDescending { it.date }
            SortOption.DATE_ASC -> filteredTransactions.sortedBy { it.date }
            SortOption.AMOUNT_DESC -> filteredTransactions.sortedByDescending { it.amount }
            SortOption.AMOUNT_ASC -> filteredTransactions.sortedBy { it.amount }
        }
    }

    // Get unique categories from transactions for navigation (only show categories that have transactions)
    val transactionCategories = remember(transactions) {
        transactions.map { it.category }.distinct().sorted()
    }

    // Get all available categories (including those without transactions)
    val allCategories = remember(categories) {
        categories.sorted()
    }

    // Calculate category totals
    val categoryTotals = remember(transactions) {
        transactions.groupBy { it.category }
            .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
            .toSortedMap()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = selectedCategory ?: "All Expenses",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Sort")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        SortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.displayName) },
                                onClick = {
                                    sortBy = option
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExpense,
                containerColor = Color(0xFF007AFF) // iOS blue
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F2F2))
                .padding(padding)
        ) {
            // Category Navigation Bar
            if (transactionCategories.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryFilterButton(
                        category = null,
                        isSelected = selectedCategory == null,
                        onClick = { selectedCategory = null }
                    )
                    transactionCategories.forEach { category ->
                        CategoryFilterButton(
                            category = category,
                            isSelected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }
            }

            if (sortedTransactions.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (selectedCategory != null) "No expenses in $selectedCategory" else "No expenses yet",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the + button to add your first expense",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Show category total if filtering by category
                if (selectedCategory != null) {
                    val total = categoryTotals[selectedCategory] ?: 0.0
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$selectedCategory Total",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "₹${String.format("%.2f", total)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF007AFF)
                            )
                        }
                    }
                }

                // Group expenses by date
                val groupedExpenses = sortedTransactions.groupBy { transaction ->
                    getDateKey(transaction.date)
                }.toSortedMap(compareByDescending { it })

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedExpenses.forEach { (dateKey, expensesForDate) ->
                        item {
                            // Date header
                            Text(
                                text = formatDateHeader(dateKey),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        items(expensesForDate) { expense ->
                            ExpenseRow(
                                expense = expense,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .clickable(interactionSource = null, indication = null) { onEditExpense(expense.id.toString()) }
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class SortOption(val displayName: String) {
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    AMOUNT_DESC("Highest Amount"),
    AMOUNT_ASC("Lowest Amount")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryFilterButton(
    category: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    Button(
        onClick = onClick,
        modifier = if (onLongClick != null) {
            Modifier.combinedClickable(
                interactionSource = null,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
        } else Modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF007AFF) else Color.White,
            contentColor = if (isSelected) Color.White else Color(0xFF007AFF)
        ),
        border = if (!isSelected) ButtonDefaults.outlinedButtonBorder else null,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
    ) {
        Text(
            text = category ?: "All",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private fun getDateKey(date: Date): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(date)
}

private fun formatDateHeader(dateKey: String): String {
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateKey) ?: Date()
    val today = Date()
    val yesterday = Date(today.time - 24 * 60 * 60 * 1000)

    return when {
        getDateKey(date) == getDateKey(today) -> "Today"
        getDateKey(date) == getDateKey(yesterday) -> "Yesterday"
        else -> {
            val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            formatter.format(date)
        }
    }
}