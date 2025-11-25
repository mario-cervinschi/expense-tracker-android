package com.example.expensetracker.ui.home

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.R
import com.example.expensetracker.utils.NetworkStatusService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onTransactionClick: (id: String?) -> Unit,
    onAddTransaction: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val transactionsViewModel = viewModel<TransactionsViewModel>(factory = TransactionsViewModel.Factory)
    val transactionsUiState by transactionsViewModel.uiState.collectAsStateWithLifecycle(
        initialValue = listOf()
    )

    val context = LocalContext.current
    val networkObserver = remember { NetworkStatusService(context) }
    val isOnline by networkObserver.isOnline.collectAsState(initial = true)

    var showBackOnlineMessage by remember { mutableStateOf(false) }

    LaunchedEffect (isOnline) {
        if (isOnline) {
            showBackOnlineMessage = true
            kotlinx.coroutines.delay(2000)
            showBackOnlineMessage = false
        } else {
            showBackOnlineMessage = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.transactions)) },
                actions = {
                    Button(onClick = onLogout) { Text("Logout") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    Log.d("HomeScreen", "add")
                    onAddTransaction()
                },
            ) { Icon(Icons.Rounded.Add, "Add") }
        }
    ) {innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = !isOnline,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Red)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Offline Mode - No Internet Connection",
                        color = Color.White,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                }
            }

            AnimatedVisibility(
                visible = isOnline && showBackOnlineMessage,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF4CAF50))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Back Online",
                        color = Color.White,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                }
            }

            val totalIncome = transactionsUiState
                .filter { it.income }
                .sumOf { it.sum.toDouble() }

            val totalExpenses = transactionsUiState
                .filter { !it.income }
                .sumOf { it.sum.toDouble() }

            Spacer(Modifier.height(50.dp))
            DonutChart(income = totalIncome, expenses = totalExpenses)

            TransactionList(
                transactionList = transactionsUiState,
                onTransactionClick = onTransactionClick
            )
        }
    }
}

@Preview
@Composable
fun PreviewHomeScreen() {
    HomeScreen(onTransactionClick = {})
}