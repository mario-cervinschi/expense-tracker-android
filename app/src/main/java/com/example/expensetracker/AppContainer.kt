package com.example.expensetracker

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import com.example.expensetracker.data.TransactionRepository
import com.example.expensetracker.data.UserPreferencesRepository
import com.example.expensetracker.data.local.AppDatabase
import com.example.expensetracker.data.remote.Api
import com.example.expensetracker.data.remote.transactions.TransactionService
import com.example.expensetracker.data.remote.transactions.TransactionWsClient
import com.example.expensetracker.ui.auth.data.AuthRepository
import com.example.expensetracker.ui.auth.data.remote.AuthDataSource
import com.example.expensetracker.utils.NetworkStatusService
import kotlin.getValue

val Context.userPreferencesDataStore by preferencesDataStore(
    name = "user_preferences"
)

class AppContainer(val context: Context) {
    init {
    }

    private val transactionService: TransactionService = Api.retrofit.create(TransactionService::class.java)
    private val transactionWsClient: TransactionWsClient = TransactionWsClient(Api.okHttpClient)
    private val authDataSource: AuthDataSource = AuthDataSource()
    private val database: AppDatabase by lazy { AppDatabase.getDatabase(context) }
    val networkStatusService: NetworkStatusService by lazy {
        NetworkStatusService(context)
    }

    val transactionRepository: TransactionRepository by lazy {
        TransactionRepository(transactionWsClient, transactionService, database.transactionDao(), context, networkStatusService)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(authDataSource)
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context.userPreferencesDataStore)
    }
}