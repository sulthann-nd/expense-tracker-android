package com.example.expensetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensetracker.ui.components.AmountField
import com.example.expensetracker.ui.components.CategoryPicker
import com.example.expensetracker.ui.components.DatePickerRow
import com.example.expensetracker.ui.components.NoteField
import com.example.expensetracker.ui.components.PaymentMethodPicker
import com.example.expensetracker.ui.transaction.TransactionViewModel
import java.util.Date

// Constants to avoid "Magic Strings"
private const val DEFAULT_CATEGORY = "Food"
private const val DEFAULT_PAYMENT = "Cash"
private const val NOTE_PLACEHOLDER = "" // Better to use empty than "Optional"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: TransactionViewModel,
    onReset: () -> Unit,
    categories: Set<String> = setOf("Food", "Transport", "Shopping", "Entertainment", "Bills", "Others"),
    onAddCategory: (String) -> Unit = {}
) {
    var amount by remember { mutableStateOf(0.0) }
    val maxAmount = 1e9
    var selectedCategory by remember { mutableStateOf(DEFAULT_CATEGORY) }
    var paymentMethod by remember { mutableStateOf(DEFAULT_PAYMENT) }
    var date by remember { mutableStateOf(Date()) }
    var note by remember { mutableStateOf(NOTE_PLACEHOLDER) }

    var showAlert by remember { mutableStateOf(false) }
    var alertTitle by remember { mutableStateOf("") }
    var alertMessage by remember { mutableStateOf("") }

    fun resetFormAfterSave() {
        amount = 0.0
        selectedCategory = DEFAULT_CATEGORY
        date = Date()
        paymentMethod = DEFAULT_PAYMENT
        note = NOTE_PLACEHOLDER
        onReset()
    }

    fun saveExpenseAction() {
        if (amount <= 0) {
            alertTitle = "Invalid amount"
            alertMessage = "Please enter an amount greater than zero."
            showAlert = true
            return
        }
        try {
            viewModel.addTransaction(
                amount = amount,
                category = selectedCategory,
                note = note.ifBlank { null },
                date = date,
                paymentMethod = paymentMethod ?: "Cash"
            )
            alertTitle = "Saved"
            alertMessage = "Expense saved successfully."
            resetFormAfterSave()
        } catch (e: Exception) {
            alertTitle = "Save Failed"
            alertMessage = e.localizedMessage ?: "Unknown error"
        }
        showAlert = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense") }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { resetFormAfterSave() }
                ) {
                    Text("Reset")
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { saveExpenseAction() }
                ) {
                    Text("Save")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AmountField(
                amount = amount,
                onAmountChange = { amount = it },
                maxAmount = maxAmount
            )

            CategoryPicker(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                categories = categories.sorted(),
                onAddCategory = onAddCategory
            )

            DatePickerRow(
                date = date,
                onDateChange = { date = it },
                label = "Date"
            )

            PaymentMethodPicker(
                paymentMethod = paymentMethod,
                onPaymentChange = { paymentMethod = it ?: DEFAULT_PAYMENT }
            )

            NoteField(
                note = note,
                onNoteChange = { note = it }
            )
        }
    }

    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            confirmButton = {
                TextButton(onClick = { showAlert = false }) {
                    Text("OK")
                }
            },
            title = { Text(text = alertTitle) },
            text = { Text(text = alertMessage) }
        )
    }
}