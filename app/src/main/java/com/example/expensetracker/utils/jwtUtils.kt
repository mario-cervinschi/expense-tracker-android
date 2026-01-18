package com.example.expensetracker.utils

import android.util.Base64
import android.util.Log
import org.json.JSONObject
import java.nio.charset.Charset

object JwtUtils {

    fun isTokenExpired(token: String?): Boolean {
        if (token.isNullOrEmpty()) return true

        return try {
            val split = token.split(".")
            if (split.size < 2) return true // Format invalid

            val payloadBase64 = split[1]
            val payloadString = String(Base64.decode(payloadBase64, Base64.URL_SAFE), Charset.defaultCharset())

            val jsonObject = JSONObject(payloadString)

            if (jsonObject.has("exp")) {
                val exp = jsonObject.getLong("exp")
                val currentTimeSeconds = System.currentTimeMillis() / 1000

                return exp < (currentTimeSeconds - 10)
            }

            false
        } catch (e: Exception) {
            Log.e("JwtUtils", "Error parsing token", e)
            true
        }
    }
}