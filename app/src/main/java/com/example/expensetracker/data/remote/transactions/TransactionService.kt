package com.example.expensetracker.data.remote.transactions

import com.example.expensetracker.data.model.Transaction
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TransactionService {
    @GET("/api/item")
    suspend fun find(@Header("Authorization") authorization : String) : List<Transaction>

    @GET("/api/item/{id}")
    suspend fun findOne(@Header("Authorization") authorization : String, @Path("id") transactionId : String?) : Transaction

    @DELETE("/api/item/{id}")
    suspend fun delete(@Header("Authorization") authorization : String, @Path("id") transactionId : String?)

    @Headers("Content-Type: application/json")
    @POST("/api/item")
    suspend fun create(@Header("Authorization") authorization : String, @Body transaction: Transaction) : Transaction

    @Headers("Content-Type: application/json")
    @PUT("/api/item/{id}")
    suspend fun update(@Header("Authorization") authorization : String, @Path("id") transactionId : String?, @Body transaction: Transaction) : Transaction
}