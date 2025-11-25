package com.example.expensetracker.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.expensetracker.MyApplication
import com.example.expensetracker.data.TransactionRepository
import com.example.expensetracker.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TransactionsViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    val uiState: Flow<List<Transaction>> = transactionRepository.transactionStream

    init {
        Log.d("TransactionsViewModel", "init")
        loadTransactions()
    }

    fun loadTransactions() {
        Log.d("TransactionsViewModel", "loadTransactions...")
        viewModelScope.launch {
            try {
                transactionRepository.refresh()
            } catch (e: Exception) {
                Log.w("TransactionsViewModel", "Nu s-a putut face refresh: ${e.message}")
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                TransactionsViewModel(app.container.transactionRepository)
            }
        }
    }
}