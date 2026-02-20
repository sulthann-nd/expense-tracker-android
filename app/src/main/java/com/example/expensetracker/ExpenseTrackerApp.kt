package com.example.expensetracker

import AppDatabase
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.data.local.repository.TransactionRepository
import com.example.expensetracker.ui.screen.ExchangeRateScreen
import com.example.expensetracker.ui.screens.AddExpenseScreen
import com.example.expensetracker.ui.screens.AnalyticsScreen
import com.example.expensetracker.ui.screens.DashboardScreen
import com.example.expensetracker.ui.screens.EditExpenseScreen
import com.example.expensetracker.ui.screens.ExpenseListScreen

import com.example.expensetracker.ui.transaction.TransactionViewModel
import com.example.expensetracker.ui.transaction.TransactionViewModelFactory
import com.example.expensetracker.ui.viewmodel.AnalyticsViewModel
import com.example.expensetracker.ui.viewmodel.DashboardViewModel
import com.example.expensetracker.ui.viewmodel.EditTransactionViewModel
import com.example.expensetracker.ui.viewmodel.ExchangeRateViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.UUID

// DataStore for preferences
private val Context.dataStore by preferencesDataStore(name = "app_preferences")
private val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
private val SELECTED_TAB = intPreferencesKey("selected_tab")
private val CATEGORIES = stringSetPreferencesKey("categories")

// Standard categories that cannot be deleted
private val STANDARD_CATEGORIES = setOf("Food", "Transport", "Shopping", "Entertainment", "Bills", "Others")

@Composable
fun ExpenseTrackerApp() {
    val context = LocalContext.current

    // Check if onboarding is completed
    val hasCompletedOnboarding by remember {
        context.dataStore.data
            .map { preferences -> preferences[HAS_COMPLETED_ONBOARDING] ?: false }
    }.collectAsState(initial = false)

    val selectedTab by remember {
        context.dataStore.data
            .map { preferences -> preferences[SELECTED_TAB] ?: 1 }
    }.collectAsState(initial = 1)

    var currentSelectedTab by remember { mutableStateOf(selectedTab) }
    var showAddExpense by remember { mutableStateOf(false) }
    var editingExpenseId by remember { mutableStateOf<String?>(null) }

    // Category management
    val categories by remember {
        context.dataStore.data
            .map { preferences -> preferences[CATEGORIES] ?: STANDARD_CATEGORIES }
    }.collectAsState(initial = STANDARD_CATEGORIES)

    // Initialize categories if empty
    LaunchedEffect(Unit) {
        context.dataStore.edit { preferences ->
            val currentCategories = preferences[CATEGORIES]
            if (currentCategories == null || currentCategories.isEmpty()) {
                preferences[CATEGORIES] = STANDARD_CATEGORIES
            }
        }
    }

    // Category management functions
    val addCategory: (String) -> Unit = { newCategory ->
        runBlocking {
            context.dataStore.edit { preferences ->
                val currentCategories = preferences[CATEGORIES] ?: STANDARD_CATEGORIES
                preferences[CATEGORIES] = currentCategories + newCategory
            }
        }
    }

    // Database and repositories
    val database = AppDatabase.getDatabase(context)
    val transactionDao = database.transactionDao()
    val repository = TransactionRepository(transactionDao)

    val removeCategory: (String) -> Unit = { categoryToRemove ->
        if (categoryToRemove !in STANDARD_CATEGORIES) {
            runBlocking {
                // Check if category is used in any transactions
                val transactionsWithCategory = repository.getTransactionsByCategory(categoryToRemove)
                if (transactionsWithCategory.isEmpty()) {
                    context.dataStore.edit { preferences ->
                        val currentCategories = preferences[CATEGORIES] ?: STANDARD_CATEGORIES
                        preferences[CATEGORIES] = currentCategories - categoryToRemove
                    }
                }
            }
        }
    }

    // ViewModels
    val transactionViewModel: TransactionViewModel = viewModel(
        factory = TransactionViewModelFactory(repository)
    )
    val exchangeRateViewModel: ExchangeRateViewModel = viewModel { ExchangeRateViewModel() }
    val dashboardViewModel: DashboardViewModel = viewModel { DashboardViewModel(repository, exchangeRateViewModel) }
    val analyticsViewModel: AnalyticsViewModel = viewModel { AnalyticsViewModel(repository, exchangeRateViewModel) }
    val editViewModel: EditTransactionViewModel = viewModel { EditTransactionViewModel(repository) }

    if (!hasCompletedOnboarding) {
        // Onboarding Flow
        Text("Welcome to Expense Tracker")
    } else {
        // Main App with Navigation
        Scaffold(
            bottomBar = {
                ExpenseTrackerBottomNavigation(
                    selectedTab = currentSelectedTab,
                    onTabSelected = { tabIndex ->
                        currentSelectedTab = tabIndex
                        runBlocking {
                            context.dataStore.edit { preferences ->
                                preferences[SELECTED_TAB] = tabIndex
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (currentSelectedTab) {
                    0 -> DashboardScreen(
                        viewModel = dashboardViewModel,
                        onNavigateToAnalytics = { currentSelectedTab = 2 },
                        onNavigateToAddExpense = { showAddExpense = true }
                    )
                    1 -> ExpenseListScreen(
                        viewModel = transactionViewModel,
                        onAddExpense = { showAddExpense = true },
                        onEditExpense = { expenseId -> editingExpenseId = expenseId },
                        categories = categories,
                        onRemoveCategory = removeCategory
                    )
                    2 -> AnalyticsScreen(
                        viewModel = analyticsViewModel
                    )
                    3 -> ExchangeRateScreen()
                }
            }
        }
    }

    // Modal screens (full screen, not dialog)
    if (showAddExpense) {
        AddExpenseScreen(
            viewModel = transactionViewModel,
            onReset = { showAddExpense = false },
            categories = categories,
            onAddCategory = addCategory
        )
    } else if (editingExpenseId != null) {
        EditExpenseScreen(
            transactionId = UUID.fromString(editingExpenseId),
            viewModel = editViewModel,
            exchangeRateViewModel = exchangeRateViewModel,
            onDismiss = { editingExpenseId = null }
        )
    }
}