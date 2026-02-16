package com.example.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensetracker.ui.components.AmountField
import com.example.expensetracker.ui.components.CategoryPicker
import com.example.expensetracker.ui.components.DatePickerRow
import com.example.expensetracker.ui.components.NoteField
import com.example.expensetracker.ui.components.PaymentMethodPicker
import com.example.expensetracker.ui.viewmodel.EditTransactionViewModel
import java.util.Date
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseScreen(
    transactionId: UUID?,
    viewModel: EditTransactionViewModel,
    onDismiss: () -> Unit
) {
    // State variables mapped from SwiftUI @State
    var amount by remember { mutableStateOf(0.0) }
    var selectedCategory by remember { mutableStateOf("Food") }
    var date by remember { mutableStateOf(Date()) }
    var paymentMethod by remember { mutableStateOf<String?>("Cash") }
    var note by remember { mutableStateOf("") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val categories = listOf("Food", "Transport", "Shopping", "Entertainment", "Bills", "Others")

    // Lifecycle: Load data if ID exists
    LaunchedEffect(transactionId) {
        transactionId?.let { viewModel.loadTransaction(it) }
    }

    // Observe data from ViewModel and update local UI state
    val existingTransaction by viewModel.transaction.collectAsState()
    LaunchedEffect(existingTransaction) {
        existingTransaction?.let {
            amount = it.amount
            selectedCategory = it.category
            date = it.date
            paymentMethod = it.paymentMethod
            note = it.note ?: ""
        }
    }

    Scaffold(
        bottomBar = {
            // Bottom Action Bar (Blue and Red Buttons)
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    HorizontalDivider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.3f))
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFF2F2F7)) // systemGray6 equivalent
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Delete Button
                        Button(
                            onClick = { showDeleteConfirmation = true },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        // Save Button
                        Button(
                            onClick = {
                                viewModel.saveTransaction(
                                    id = transactionId,
                                    amount = amount,
                                    category = selectedCategory,
                                    date = date,
                                    paymentMethod = paymentMethod ?: "Cash",
                                    note = note,
                                    onSuccess = onDismiss
                                )
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            // Top Gray Line
            HorizontalDivider(thickness = 1.dp, color = Color.Gray.copy(alpha = 0.6f))

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Amount Field
                AmountField(amount = amount, onAmountChange = { amount = it }, maxAmount = 10000.0)
                CustomDivider()

                // Category Picker
                CategoryPicker(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                    categories = categories
                )
                CustomDivider()

                // Date Row
                DatePickerRow(
                    date = date,
                    onDateChange = { date = it },
                    label = "Date"
                )
                CustomDivider()

                // Payment Method Picker
                PaymentMethodPicker(
                    paymentMethod = paymentMethod,
                    onPaymentChange = { paymentMethod = it }
                )
                CustomDivider()

                // Note Row
                NoteField(
                    note = note,
                    onNoteChange = { note = it }
                )
            }
        }

        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Delete Expense") },
                text = { Text("This will permanently remove the expense. Are you sure?") },
                confirmButton = {
                    TextButton(onClick = {
                        transactionId?.let { viewModel.deleteTransaction(it, onDeleted = onDismiss) }
                    }) {
                        Text("Delete", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun CustomDivider() {
    HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), thickness = 1.dp)
}