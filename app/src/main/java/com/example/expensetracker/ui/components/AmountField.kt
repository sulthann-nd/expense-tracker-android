package com.example.expensetracker.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun AmountField(
    amount: Double,
    onAmountChange: (Double) -> Unit,
    maxAmount: Double = 1e9
) {
    OutlinedTextField(
        value = if (amount == 0.0) "" else amount.toString(),
        onValueChange = { newValue ->
            val parsed = newValue.toDoubleOrNull() ?: 0.0
            if (parsed <= maxAmount) onAmountChange(parsed)
        },
        label = { Text("Amount") },
        prefix = { Text("$") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
}