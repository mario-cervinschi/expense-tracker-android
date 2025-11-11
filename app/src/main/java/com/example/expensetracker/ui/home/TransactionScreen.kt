package com.example.expensetracker.ui.home

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(transactionId : String?, onClose : () -> Unit){
    val transactionViewModel = viewModel<TransactionViewModel>(factory = TransactionViewModel.Factory(transactionId))
    val transactionUiState = transactionViewModel.uiState

    var title by rememberSaveable { mutableStateOf("") }
    var dateInMillis by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var sumString by rememberSaveable { mutableStateOf("") }
    var income by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Log.d("TransactionScreen", "recompose, title = $title")

    LaunchedEffect(transactionUiState.submitResult) {
        if (transactionUiState.submitResult is com.example.expensetracker.data.Result.Success) {
            Log.d("TransactionScreen", "Closing screen")
            onClose()
        }
    }

    var fieldsInitialized by remember { mutableStateOf(transactionId == null) }
    LaunchedEffect(transactionId, transactionUiState.loadResult) {
        Log.d("TransactionScreen", "Fields initialized check = ${transactionUiState.loadResult}")
        if (fieldsInitialized) {
            return@LaunchedEffect
        }
        if (transactionUiState.loadResult is com.example.expensetracker.data.Result.Success) {
            val transaction = transactionUiState.transaction
            title = transaction.title
            dateInMillis = transaction.date.time
            sumString = transaction.sum.toString()
            income = transaction.income
            fieldsInitialized = true
            Log.d("TransactionScreen", "Fields initialized with: title=$title, date=$dateInMillis, sum=$sumString, income=$income")
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateInMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            dateInMillis = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = if (transactionId != null) R.string.transactionEdit else R.string.transactionAdd)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                actions = {
                    if (transactionId != null) {
                        Button(
                            onClick = {
                                Log.d("TransactionScreen", "delete transaction id = $transactionId")
                                transactionViewModel.deleteTransaction()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Transaction"
                            )
                        }
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (transactionUiState.loadResult is com.example.expensetracker.data.Result.Loading) {
                CircularProgressIndicator()
                return@Scaffold
            }
            if (transactionUiState.submitResult is com.example.expensetracker.data.Result.Loading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) { LinearProgressIndicator() }
            }
            if (transactionUiState.loadResult is com.example.expensetracker.data.Result.Error) {
                Text(text = "Failed to load item - ${(transactionUiState.loadResult as com.example.expensetracker.data.Result.Error).exception?.message}")
            }

            // Title TextField
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            )

            // Date TextField
            TextField(
                value = dateFormat.format(Date(dateInMillis)),
                onValueChange = { },
                label = { Text("Date") },
                readOnly = true,
                enabled = false,
                colors = TextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select date"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { showDatePicker = true }
            )

            // Sum TextField
            TextField(
                value = sumString,
                onValueChange = { sumString = it },
                label = { Text("Sum") },
                placeholder = { Text("0.00") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            )

            // Income Checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = income,
                    onCheckedChange = { income = it }
                )
                Text(
                    text = "Income",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (transactionUiState.submitResult is com.example.expensetracker.data.Result.Error) {
                Text(
                    text = "Failed to submit item - ${(transactionUiState.submitResult as com.example.expensetracker.data.Result.Error).exception?.message}",
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Button(onClick = {
                Log.d("TransactionScreen", "save item title = $title")
                val date = Date(dateInMillis)
                val sum = sumString.toDoubleOrNull() ?: 0.0
                transactionViewModel.saveOrUpdateItem(title, date, sum, income)
            },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Save")
            }
        }
    }
}

@Preview
@Composable
fun PreviewItemScreen() {
    TransactionScreen(transactionId = "1", onClose = {})
}