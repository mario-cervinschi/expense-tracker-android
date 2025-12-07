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
import com.example.expensetracker.data.remote.Api
import com.example.expensetracker.utils.JwtUtils
import com.example.expensetracker.utils.NetworkStatusService
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ExpenseTrackerViewModel (
    private val userPreferencesRepository: UserPreferencesRepository,
    private val transactionRepository: TransactionRepository,
    private val networkStatusService: NetworkStatusService
) : ViewModel() {

    private var wasOffline = false

    init{
        observeNetworkStatus()
    }

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            networkStatusService.isOnline.collect { isOnline ->
                Log.d("ExpenseTrackerVM", "Network status changed: isOnline=$isOnline")

                if (isOnline && wasOffline) {
                    // Tocmai ai revenit online
                    Log.d("ExpenseTrackerVM", "Reconnected! Syncing and checking for new transactions...")
                    transactionRepository.syncOfflineChanges()
                }

                wasOffline = !isOnline
            }
        }
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
        val token = Api.tokenInterceptor.token

        Log.d("ExpenseTrackerVM", "Verifying token locally...")
        if (!JwtUtils.isTokenExpired(token)) {
            Log.d("ExpenseTrackerVM", "Token is valid (not expired). Skipping Login.")

            if (Api.tokenInterceptor.token == null && token != null) {
                Api.tokenInterceptor.token = token
            }

            return Result.success(Unit)
        } else {
            Log.d("ExpenseTrackerVM", "Token expired or invalid.")
            return Result.failure(Exception("Token expired"))
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
                    "Invalid session. Please log in again."
                } else {
                    "Unknown error: ${exception.code()}"
                }
            }
            is IOException, is java.net.UnknownHostException -> {
                "Server not responding. Verify internet connection."
            }
            else -> "Unknown error."
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
                    app.container.transactionRepository,
                    app.container.networkStatusService
                )
            }
        }
    }
}