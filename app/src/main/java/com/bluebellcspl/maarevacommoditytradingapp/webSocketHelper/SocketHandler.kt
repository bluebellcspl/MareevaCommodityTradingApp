package com.bluebellcspl.maarevacommoditytradingapp.webSocketHelper

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object SocketHandler {

    fun getWebSocket(serverUrl:String,webSocketListener: WebSocketListener):WebSocket{
        val request = Request.Builder()
            .url(serverUrl)
            .build()

        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        val client =  OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.MINUTES)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .pingInterval(5, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()

        return client.newWebSocket(request,webSocketListener)
    }
}