package com.example.expensetracker.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun NoteField(
    note: String,
    onNoteChange: (String) -> Unit
) {
    OutlinedTextField(
        value = note,
        onValueChange = { onNoteChange(it) },
        label = { Text("Note (Optional)") },
        placeholder = { Text("Enter details...") },
        modifier = Modifier.fillMaxWidth(),
        maxLines = 3
    )
}