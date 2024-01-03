package com.bluebellcspl.maarevacommoditytradingapp.webSocketHelper

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer.LiveAuctionFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionMasterModel
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class WebSocketClient(
    var context: Context,
    var serverUrl: String,
    var lifeCycleOwner: LifecycleOwner,
    var onMessageReceived: (liveAuction: LiveAuctionMasterModel) -> Unit
) {
    private var webSocket: WebSocket? = null
    private val TAG = "WebSocketClient"
    fun connect() {
        Log.d(TAG, "connect: SOCKET_URL : $serverUrl")
        val request = Request.Builder()
            .url(serverUrl)
            .build()

        val webSocketListerner = object : WebSocketListener() {

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                this@WebSocketClient.webSocket = null
                Log.d(TAG, "onClosed: SOCKET_CLOSED")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                t.printStackTrace()
                Log.e(TAG, "onFailure: ${t.message}")
                Log.e(TAG, "onFailure: FAILED_RESPONSE $response")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "onMessage: SOCKET_RECEIVED_MSG : $text")
                //FOR RECYCLERVIEW
                val gson = Gson()
                val jsonObject: JsonObject = JsonParser().parse(text).asJsonObject
                Log.d(TAG, "onMessage: NEW_JSON_OBJECT : ${jsonObject.toString()}")
                val userListType = object : TypeToken<LiveAuctionMasterModel>() {}.type
                var liveAuctionObject: LiveAuctionMasterModel =
                    gson.fromJson(jsonObject.toString(), userListType)

                lifeCycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    onMessageReceived(liveAuctionObject)
                }

            }

            override fun onOpen(webSocket: WebSocket, response: Response) {
                this@WebSocketClient.webSocket = webSocket
                Log.d(TAG, "onOpen: SOCKET_CONNECTED")
            }

        }

        val client = OkHttpClient.Builder()
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .build()
        webSocket = client.newWebSocket(request, webSocketListerner)
    }
    fun disconnect() {
        try {
//            webSocket?.close(1001, "User disconnected")
            Log.d(TAG, "disconnect: SOCKET_DISCONNECTED")
            webSocket?.cancel()
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "disconnect: ${e.message}", )
        }
    }
}