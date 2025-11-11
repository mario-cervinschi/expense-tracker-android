package com.example.expensetracker.data.remote.transactions

import android.util.Log
import com.example.expensetracker.data.remote.Api
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.Date

class TransactionWsClient (private val okHttpClient : OkHttpClient){
    var webSocket: WebSocket? = null

    suspend fun openSocket(
        token: String,
        onEvent: (event : TransactionEvent?) -> Unit,
        onClosed: () -> Unit,
        onFailure: () -> Unit
    ) {
        withContext(Dispatchers.IO){
            Log.d("wsClient", "openSocket")
            val request = Request.Builder().url(Api.wsUrl).build()
            webSocket = okHttpClient.newWebSocket(
                request,
                TransactionWebSocketListener(   token, onEvent = onEvent, onClosed = onClosed, onFailure = onFailure)
            )
            okHttpClient.dispatcher.executorService.shutdown()
        }
    }

    fun closeSocket(){
        Log.d("wsClient", "closeSocket")
        webSocket?.close(1000, "")
    }

    inner class TransactionWebSocketListener(
        private val token: String,
        private val onEvent: (event : TransactionEvent?) -> Unit,
        private val onClosed: () -> Unit,
        private val onFailure: () -> Unit
    ) : WebSocketListener() {
        private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(Date::class.java, DateAdapter())
            .build()
        private val transactionEventJsonAdapter : JsonAdapter<TransactionEvent> =
            moshi.adapter(TransactionEvent::class.java)

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d("TransactionWsClient", "onOpen")
            authorize(token)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("TransactionWsClient", "onMessage string $text")
            val transactionEvent = transactionEventJsonAdapter.fromJson(text)
            onEvent(transactionEvent)
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d("TransactionWsClient", "onMessage bytes $bytes")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {}

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d("TransactionWsClient", "onClosed bytes $code $reason")
            onClosed()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.d("TransactionWsClient", "onFailure bytes $t")
            onFailure()
        }
    }

    fun authorize(token: String) {
        val auth = """
            {
              "type":"authorization",
              "payload":{
                "token": "$token"
              }
            }
        """.trimIndent()
        Log.d("TransactionWsClient", "auth $auth")
        webSocket?.send(auth)
    }
}