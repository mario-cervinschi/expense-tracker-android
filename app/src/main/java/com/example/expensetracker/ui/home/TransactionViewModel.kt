package com.example.expensetracker.ui.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.expensetracker.MyApplication
import com.example.expensetracker.data.Result
import com.example.expensetracker.data.TransactionRepository
import com.example.expensetracker.data.model.Transaction
import kotlinx.coroutines.launch
import java.util.Date

data class TransactionUiState(
    val transId : String? = null,
    val transaction : Transaction = Transaction(),
    var loadResult : com.example.expensetracker.data.Result<Transaction>? = null,
    var submitResult : com.example.expensetracker.data.Result<Transaction>? = null
)

class TransactionViewModel(private val transactionId: String?, private val transactionRepository: TransactionRepository) : ViewModel()
{
    var uiState : TransactionUiState by mutableStateOf(TransactionUiState(loadResult = com.example.expensetracker.data.Result.Loading))
        private set

    init {
        if(transactionId != null) loadTransaction()
        else {
            uiState = uiState.copy(
                loadResult = com.example.expensetracker.data.Result.Success(Transaction())
            )
        }
    }

    fun loadTransaction() {
        viewModelScope.launch {
            transactionRepository.transactions.collect { transactions ->
                val transaction = transactions.find { it._id == transactionId }
                if (transaction != null) {
                    Log.d("TransactionViewModel", "Found transaction: ${transaction.title}")
                    uiState = uiState.copy(
                        transaction = transaction,
                        loadResult = com.example.expensetracker.data.Result.Success(transaction)
                    )
                } else {
                    Log.d("TransactionViewModel", "Transaction not found for id: $transactionId")
                    uiState = uiState.copy(
                        loadResult = com.example.expensetracker.data.Result.Error(Exception("Transaction not found"))
                    )
                }
            }
        }
    }

    fun saveOrUpdateItem(title: String, date : Date, sum : Double, income : Boolean) {
        viewModelScope.launch {
            try {
                uiState = uiState.copy(submitResult = com.example.expensetracker.data.Result.Loading)
                val item = uiState.transaction.copy(title = title, date = date, sum = sum, income = income)
                val savedTransaction: Transaction;
                if (transactionId == null) {
                    savedTransaction = transactionRepository.save(item)
                } else {
                    savedTransaction = transactionRepository.update(item)
                }
                uiState = uiState.copy(submitResult = com.example.expensetracker.data.Result.Success(savedTransaction))
            } catch (e: Exception) {
                uiState = uiState.copy(submitResult = com.example.expensetracker.data.Result.Error(e))
            }
        }
    }

    fun deleteTransaction(){
        if(transactionId == null) return
        viewModelScope.launch {
            try{
                uiState = uiState.copy(submitResult = Result.Loading)
                transactionRepository.delete(transaction = uiState.transaction)
                uiState = uiState.copy(submitResult = Result.Success(uiState.transaction))
            } catch (e: Exception) {
                uiState = uiState.copy(submitResult = Result.Error(e))
            }
        }
    }

    companion object {
        fun Factory(transactionId: String?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                TransactionViewModel(transactionId, app.container.transactionRepository)
            }
        }
    }
}