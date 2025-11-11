package com.example.expensetracker.data.remote

import android.util.Log
import okhttp3.Interceptor

class TokenInterceptor constructor() : Interceptor {
    var token: String? = null

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val original = chain.request()
        val originalUrl = original.url

        if (token == null){
            Log.d("TokenInterceptor", "Token is null")
            return chain.proceed(original)
        }
        val requestBuilder = original.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .url(originalUrl)
        val request = requestBuilder.build()
        Log.d("TokenInterceptor", "Authorization bearer added")
        return chain.proceed(request)
    }
}