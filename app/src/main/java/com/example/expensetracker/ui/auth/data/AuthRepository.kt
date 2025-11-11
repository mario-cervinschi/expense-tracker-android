package com.example.expensetracker.ui.auth.data

import android.util.Log
import com.example.expensetracker.data.remote.Api
import com.example.expensetracker.ui.auth.data.remote.AuthDataSource
import com.example.expensetracker.ui.auth.data.remote.TokenHolder
import com.example.expensetracker.ui.auth.data.remote.User

class AuthRepository(private val authDataSource: AuthDataSource) {
    init {
        Log.d("AuthRepository", "init")
    }

    fun clearToken(){
        Api.tokenInterceptor.token = null;
    }

    suspend fun login(email: String, password: String): Result<TokenHolder> {
        val user = User(email, password)
        val result = authDataSource.login(user)
        if(result.isSuccess){
            Api.tokenInterceptor.token = result.getOrNull()?.token
        }
        return result
    }

    suspend fun register(email: String, password: String, confirmationPassword: String): Result<TokenHolder> {
        if(password != confirmationPassword){
            return Result.failure(Exception("Passwords do not match"))
        }

        val user = User(email, password)
        val result = authDataSource.register(user)
        if(result.isSuccess){
            Api.tokenInterceptor.token = result.getOrNull()?.token
        }
        return result
    }
}