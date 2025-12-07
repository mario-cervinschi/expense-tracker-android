package com.example.expensetracker.data

import android.content.Context
import android.util.Log
import com.example.expensetracker.data.local.TransactionDao
import com.example.expensetracker.data.model.Transaction
import com.example.expensetracker.data.remote.Api
import com.example.expensetracker.data.remote.transactions.TransactionEvent
import com.example.expensetracker.data.remote.transactions.TransactionService
import com.example.expensetracker.data.remote.transactions.TransactionWsClient
import com.example.expensetracker.utils.NetworkStatusService
import com.example.expensetracker.utils.notification.NotificationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.getValue

class TransactionRepository (
    private val transactionWsClient: TransactionWsClient,
    private val transactionService : TransactionService,
    private val transactionDao: TransactionDao,
    private val context: Context,
    private val networkStatusService: NetworkStatusService
) {
    private val notificationService = NotificationService(context)
    val transactionStream by lazy { transactionDao.getAll() }
    private fun getBearerToken() = "Bearer ${Api.tokenInterceptor.token}"

    private val sharedPrefs = context.getSharedPreferences("transaction_snapshot", Context.MODE_PRIVATE)
    private val SNAPSHOT_KEY = "last_known_ids"

    init {
        Log.d("TransactionRepository", "init")
    }

    suspend fun refresh() {
        try {
            val remoteData = transactionService.find(authorization = getBearerToken())

            transactionDao.deleteSyncedOnly()

            remoteData.forEach {
                transactionDao.insert(it.copy(isSynced = true))
            }
        } catch (e: Exception) {
            Log.w("Repo", "Refresh failed", e)
        }
    }

    suspend fun refreshAndNotify() {
        try {
            // 1. Salvează snapshot-ul curent (înainte de refresh)
            val oldSnapshot = loadSnapshot()

            // 2. Fetch date noi de pe server
            val remoteData = transactionService.find(authorization = getBearerToken())

            // 3. Identifică tranzacții noi (care nu erau în snapshot)
            val newTransactions = remoteData.filter { transaction ->
                !oldSnapshot.contains(transaction._id)
            }

            // 4. Trimite notificări pentru tranzacții noi
            if (newTransactions.isNotEmpty()) {
                Log.d("TransactionRepository", "Found ${newTransactions.size} new transactions while offline")
                newTransactions.forEach { transaction ->
                    notificationService.showNewTransactionNotification(
                        transaction.title,
                        transaction.sum.toDouble(),
                        transaction.income
                    )
                }
            }

            // 5. Update database
            transactionDao.deleteSyncedOnly()
            remoteData.forEach {
                transactionDao.insert(it.copy(isSynced = true))
            }

            // 6. Salvează noul snapshot
            saveSnapshot(remoteData.map { it._id })

        } catch (e: Exception) {
            Log.w("Repo", "Refresh and notify failed", e)
        }
    }

    private fun loadSnapshot(): Set<String> {
        val idsString = sharedPrefs.getString(SNAPSHOT_KEY, "") ?: ""
        return if (idsString.isEmpty()) {
            emptySet()
        } else {
            idsString.split(",").toSet()
        }
    }

    private fun saveSnapshot(transactionIds: List<String>) {
        val idsString = transactionIds.joinToString(",")
        sharedPrefs.edit().putString(SNAPSHOT_KEY, idsString).apply()
        Log.d("TransactionRepository", "Snapshot saved: ${transactionIds.size} IDs")
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
        if (networkStatusService.hasInternetConnection()) {
            return try {
                Log.d("TransactionRepository", "Online: Updating API...")
                val updatedItem = transactionService.update(
                    transactionId = transaction._id,
                    transaction = transaction,
                    authorization = getBearerToken()
                )
                transactionDao.update(updatedItem.copy(isSynced = true))
                updatedItem
            } catch (e: Exception) {
                Log.w("TransactionRepository", "API update failed, saving offline", e)
                updateOffline(transaction)
            }
        } else {
            return updateOffline(transaction)
        }
    }

    private suspend fun updateOffline(transaction: Transaction): Transaction {
        val offlineTransaction = transaction.copy(isSynced = false)
        transactionDao.update(offlineTransaction)
        return offlineTransaction
    }

    suspend fun delete(transaction: Transaction){
        if (networkStatusService.hasInternetConnection()) {
            try {
                Log.d("TransactionRepository", "Online: Deleting from API...")
                transactionService.delete(
                    transactionId = transaction._id,
                    authorization = getBearerToken()
                )
                transactionDao.deleteById(transaction._id)
            } catch (e: Exception) {
                Log.w("TransactionRepository", "API delete failed, marking offline delete", e)
                deleteOffline(transaction)
            }
        } else {
            deleteOffline(transaction)
        }
    }

    private suspend fun deleteOffline(transaction: Transaction) {
        val deletedTransaction = transaction.copy(
            isDeletedLocally = true,
            isSynced = false
        )
        transactionDao.update(deletedTransaction)
    }

    suspend fun save(transaction: Transaction): Transaction {
        if (networkStatusService.hasInternetConnection()) {
            return try {
                Log.d("TransactionRepository", "Online: Saving to API...")
                val createdItem = transactionService.create(transaction = transaction, authorization = getBearerToken())

                transactionDao.insert(createdItem.copy(isSynced = true))

                Log.d("TransactionRepository", "Save to API succeeded")
                createdItem
            } catch (e: Exception) {
                Log.w("TransactionRepository", "API save failed, falling back to offline", e)
                saveOffline(transaction)
            }
        } else {
            Log.d("TransactionRepository", "Offline: Saving locally...")
            return saveOffline(transaction)
        }
    }

    private suspend fun saveOffline(transaction: Transaction): Transaction {
        val offlineId = if (transaction._id.isNullOrEmpty()) UUID.randomUUID().toString() else transaction._id

        val offlineTransaction = transaction.copy(
            _id = offlineId,
            isSynced = false,
            isDeletedLocally = false
        )

        transactionDao.insert(offlineTransaction)
        return offlineTransaction
    }

    private fun isLocalId(id: String): Boolean {
        return id.contains("-") || id.length > 30
    }

    suspend fun syncOfflineChanges() {
        if (!networkStatusService.hasInternetConnection()) return

        Log.d("TransactionRepository", "SYNC: Starting background sync...")
        val token = getBearerToken()

        val unsynced = transactionDao.getUnsyncedTransactions()

        unsynced.forEach { localTrans ->
            try {
                if (isLocalId(localTrans._id)) {
                    Log.d("TransactionRepository", "SYNC: Found NEW item (Local ID), creating on server: ${localTrans.title}")

                    val transToUpload = localTrans.copy(
                        _id = "",
                        isSynced = true
                    )

                    val savedServerTrans = transactionService.create( token, transToUpload)

                    transactionDao.deleteById(localTrans._id)
                    transactionDao.insert(savedServerTrans.copy(isSynced = true))

                } else {
                    Log.d("TransactionRepository", "SYNC: Found EXISTING item (Server ID), updating server: ${localTrans.title}")

                    val transToUpdate = localTrans.copy(isSynced = true)

                    val updatedServerTrans = transactionService.update(
                        transactionId = localTrans._id,
                        transaction = transToUpdate,
                        authorization = token
                    )

                    transactionDao.update(updatedServerTrans.copy(isSynced = true))
                }
            } catch (e: Exception) {
                Log.e("TransactionRepository", "SYNC: Failed to upload ${localTrans._id}", e)
            }
        }

        val deletedLocally = transactionDao.getDeletedTransactions()
        deletedLocally.forEach { trans ->
            try {
                if (isLocalId(trans._id)) {
                    transactionDao.deleteById(trans._id)
                } else {
                    transactionService.delete( token, trans._id)
                    transactionDao.deleteById(trans._id)
                }
            } catch (e: Exception) {
                if ((e as? retrofit2.HttpException)?.code() == 404) {
                    transactionDao.deleteById(trans._id)
                }
            }
        }

        refreshAndNotify()
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
        notificationService.showNewTransactionNotification(item.title, item.sum.toDouble(), item.income)
    }

    suspend fun deleteAll() {
    }

    fun setToken(token: String) {
        transactionWsClient.authorize(token)
    }
}