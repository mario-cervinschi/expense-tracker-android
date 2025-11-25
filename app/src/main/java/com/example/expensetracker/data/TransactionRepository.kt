package com.example.expensetracker.data

import android.util.Log
import com.example.expensetracker.data.local.TransactionDao
import com.example.expensetracker.data.model.Transaction
import com.example.expensetracker.data.remote.Api
import com.example.expensetracker.data.remote.transactions.TransactionEvent
import com.example.expensetracker.data.remote.transactions.TransactionService
import com.example.expensetracker.data.remote.transactions.TransactionWsClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import kotlin.getValue

class TransactionRepository (
    private val transactionWsClient: TransactionWsClient,
    private val transactionService : TransactionService,
    private val transactionDao: TransactionDao
) {
    val transactionStream by lazy { transactionDao.getAll() }

    init {
        Log.d("TransactionRepository", "init")
    }

    private fun getBearerToken() = "Bearer ${Api.tokenInterceptor.token}"

    suspend fun refresh() {
        Log.d("TransactionRepository", "refresh started")
        try {
            val result = transactionService.find(authorization = getBearerToken())
            transactionDao.deleteAll()
            result.forEach { transactionDao.insert(it) }
            Log.d("TransactionRepository", "refresh succeeded")
        } catch (e: Exception) {
            Log.w("TransactionRepository", "refresh failed", e)
            throw e
        }
    }

    suspend fun openWsClient() {
        Log.d("TransactionRepository", "openWsClient")
        withContext(Dispatchers.IO) {
            getItemEvents().collect {
                Log.d("TransactionRepository", "Item event collected $it")
                if (it.isSuccess) {
                    val transactionEvent = it.getOrNull();
                    when (transactionEvent?.type) {
                        "created" -> handleTransactionCreated(transactionEvent.payload)
                        "updated" -> handleTransactionUpdated(transactionEvent.payload)
                        "deleted" -> handleTransactionDeleted(transactionEvent.payload)
                    }
                }
            }
        }
    }

    suspend fun closeWsClient() {
        Log.d("TransactionRepository", "closeWsClient")
        withContext(Dispatchers.IO) {
            transactionWsClient.closeSocket()
        }
    }

    suspend fun getItemEvents(): Flow<kotlin.Result<TransactionEvent>> = callbackFlow {
        Log.d("TransactionRepository", "getItemEvents started")

        val token = Api.tokenInterceptor.token
        if (token == null) {
            Log.e("TransactionRepository", "Token is null, closing WS flow")
            close()
            return@callbackFlow
        }

        transactionWsClient.openSocket(
            token = token,
            onEvent = {
                Log.d("TransactionRepository", "onEvent $it")
                if (it != null) {
                    trySend(kotlin.Result.success(it))
                }
            },
            onClosed = { close() },
            onFailure = { close() });

        awaitClose { transactionWsClient.closeSocket() }
    }

    suspend fun update(transaction: Transaction): Transaction {
        Log.d("TransactionRepository", "update $transaction...")
        val updatedItem =
            transactionService.update(transactionId = transaction._id, transaction = transaction, authorization = getBearerToken())
        Log.d("TransactionRepository", "update $transaction succeeded")
//        handleTransactionUpdated(updatedItem)
        return updatedItem
    }

    suspend fun delete(transaction: Transaction){
        Log.d("TransactionRepository", "delete ${transaction._id}...")
        try {
            transactionService.delete(
                transactionId = transaction._id,
                authorization = getBearerToken()
            )

//            handleTransactionDeleted(transaction)

            Log.d("TransactionRepository", "delete ${transaction._id} succeeded")
        } catch (e: Exception) {
            Log.e("TransactionRepository", "delete failed for ${transaction._id}", e)
            throw e
        }
    }

    suspend fun save(transaction: Transaction): Transaction {
        Log.d("TransactionRepository", "save $transaction...")
        val createdItem = transactionService.create(transaction = transaction, authorization = getBearerToken())

        Log.d("TransactionRepository", "save $transaction succeeded")

        return createdItem
    }

    private suspend fun handleTransactionDeleted(item: Transaction) {
        Log.d("TransactionRepository", "handleTransactionDeleted - todo $item")
        transactionDao.deleteById(item._id)
    }

    private suspend fun handleTransactionUpdated(item: Transaction) {
        Log.d("TransactionRepository", "handleTransactionUpdated...")
        transactionDao.update(item)
    }

    private suspend fun handleTransactionCreated(item: Transaction) {
        Log.d("TransactionRepository", "handleItemCreated...")
        transactionDao.insert(item)
    }

    suspend fun deleteAll() {
    }

    fun setToken(token: String) {
        transactionWsClient.authorize(token)
    }
}