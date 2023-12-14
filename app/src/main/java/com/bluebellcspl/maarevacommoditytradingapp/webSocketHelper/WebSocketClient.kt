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

class WebSocketClient(var context: Context,var serverUrl:String, var lifeCycleOwner:LifecycleOwner,var onMessageReceived:(liveAuction:LiveAuctionMasterModel)->Unit) {
    private var webSocket : WebSocket? = null
    private val TAG = "WebSocketClient"
    fun connect(){
        Log.d(TAG, "connect: SOCKET_URL : $serverUrl")
        val request = Request.Builder()
            .url(serverUrl)
            .build()

        val webSocketListerner = object : WebSocketListener(){

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
//                val jsonArray: JsonArray = JsonParser().parse(text).asJsonArray
                val jsonObject: JsonObject = JsonParser().parse(text).asJsonObject
                Log.d(TAG, "onMessage: NEW_JSON_OBJECT : ${jsonObject.toString()}")
                val userListType = object : TypeToken<LiveAuctionMasterModel>() {}.type
                var liveAuctionObject : LiveAuctionMasterModel = gson.fromJson(jsonObject.toString(),userListType)

//                (context as Activity).runOnUiThread {
////                    liveAuctionFragment.onMessageReceived(liveAuctionObject)
//                    onMessageReceived(liveAuctionObject)
//                }



                lifeCycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    onMessageReceived(liveAuctionObject)
                }

            }

            override fun onOpen(webSocket: WebSocket, response: Response) {
                this@WebSocketClient.webSocket = webSocket
                Log.d(TAG, "onOpen: SOCKET_CONNECTED")
//                (context as Activity).runOnUiThread {
//                    Toast.makeText(context,"Socket Connected!",Toast.LENGTH_SHORT).show()
//                }
//                send(ChatMessage(userName,"Joined the Chat",""))
//                var liveAuctionList = ArrayList<LiveAuctionMaster>()
//                webSocket.send("{\n" +
//                        "    \"FromUserId\":\"1\",\n" +
//                        "    \"ToUserId\":\"37\",\n" +
//                        "    \"CompanyCode\":\"MAT189\",\n" +
//                        "    \"Action\":\"All\",\n" +
//                        "}")
            }

        }

        val client = OkHttpClient()
        webSocket = client.newWebSocket(request,webSocketListerner)
    }

//    fun send(message: ChatMessage) {
//        webSocket?.send(Gson().toJson(message))
//    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
    }
}