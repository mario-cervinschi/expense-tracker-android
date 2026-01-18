package com.example.expensetracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.expensetracker.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM Transactions WHERE isDeletedLocally = 0")
    fun getAll(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE isSynced = 0 AND isDeletedLocally = 0")
    suspend fun getUnsyncedTransactions(): List<Transaction>

    @Query("SELECT * FROM transactions WHERE isDeletedLocally = 1")
    suspend fun getDeletedTransactions(): List<Transaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction): Int

    @Query("DELETE FROM Transactions WHERE _id = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM transactions WHERE isSynced = 1")
    suspend fun deleteSyncedOnly()

    @Query("DELETE FROM Transactions")
    suspend fun deleteAll()
}