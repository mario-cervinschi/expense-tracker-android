package com.example.expensetracker

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.expensetracker.data.TransactionRepository
import com.example.expensetracker.data.UserPreferences
import com.example.expensetracker.data.UserPreferencesRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ExpenseTrackerViewModel (
    private val userPreferencesRepository: UserPreferencesRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    init{

    }

    fun logout() {
        viewModelScope.launch {
            transactionRepository.closeWsClient()
            userPreferencesRepository.save(UserPreferences())
        }
    }

    fun openWebSocketConnection(){
        viewModelScope.launch {
//            transactionRepository.openWsClient()
        }
    }

    suspend fun verifyToken(): Result<Unit> {
        return try {
            Log.d("ExpenseTrackerVM", "Verifying token by refreshing transactions...")
            transactionRepository.refresh()
            Log.d("ExpenseTrackerVM", "Token verified. Refresh successful.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("ExpenseTrackerVM", "Token verification failed", e)
            Result.failure(e)
        }
    }

    fun startWebSocketListener() {
        viewModelScope.launch {
            Log.d("ExpenseTrackerVM", "Starting WebSocket listener...")
            transactionRepository.openWsClient()
        }
    }
    fun getErrorMessage(exception: Throwable?): String {
        return when (exception) {
            is HttpException -> {
                if (exception.code() == 401 || exception.code() == 403) {
                    "Sesiune invalidă. Te rog, autentifică-te din nou."
                } else {
                    "Eroare de server neașteptată: ${exception.code()}"
                }
            }
            is IOException, is java.net.UnknownHostException -> {
                "Serverul nu răspunde. Verifică conexiunea la internet."
            }
            else -> "A apărut o eroare necunoscută."
        }
    }

    fun setToken(token: String) {
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                ExpenseTrackerViewModel(
                    app.container.userPreferencesRepository,
                    app.container.transactionRepository
                )
            }
        }
    }
}