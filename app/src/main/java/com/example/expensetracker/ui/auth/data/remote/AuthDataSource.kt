package com.example.expensetracker.ui.auth.data.remote

import android.util.Log
import com.example.expensetracker.data.remote.Api
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

class AuthDataSource() {
    interface AuthService{
        @Headers("Content-Type: application/json")
        @POST("/api/auth/login")
        suspend fun login(@Body user: User): TokenHolder

        @Headers("Content-Type: application/json")
        @POST("/api/auth/signup")
        suspend fun register(@Body user: User): TokenHolder
    }

    private val authService: AuthService = Api.retrofit.create(AuthService::class.java)

    suspend fun login(user: User): Result<TokenHolder> {
        try{
            return Result.success(authService.login(user))
        } catch (e: Exception){
            Log.w("AuthDataSource", "login failed")
            return Result.failure(e)
        }
    }

    suspend fun register(user: User): Result<TokenHolder> {
        try{
            return Result.success(authService.register(user))
        } catch (e: Exception){
            Log.w("AuthDataSource", "login failed")
            return Result.failure(e)
        }
    }
}