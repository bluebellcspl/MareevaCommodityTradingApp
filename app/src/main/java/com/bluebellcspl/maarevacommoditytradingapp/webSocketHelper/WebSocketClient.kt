package com.bluebellcspl.maarevacommoditytradingapp.webSocketHelper

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
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
import okhttp3.logging.HttpLoggingInterceptor
import java.net.SocketTimeoutException
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
        try {

                Log.d(TAG, "connect: SOCKET_URL : $serverUrl")
                val request = Request.Builder()
                    .url(serverUrl)
                    .build()

                val client = createOkHttpClient()

            lifeCycleOwner.lifecycleScope.launch(Dispatchers.IO){
                webSocket = client.newWebSocket(request, object : WebSocketListener() {
                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        this@WebSocketClient.webSocket = null
                        Log.d(TAG, "onClosed: SOCKET_CLOSED")
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        this@WebSocketClient.webSocket = null
                        t.printStackTrace()
                        Log.e(TAG, "onFailure: ${t.message}")
                        Log.e(TAG, "onFailure: FAILED_RESPONSE $response")
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
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
                })
            }

        } catch (se:SocketTimeoutException) {
            se.printStackTrace()
            Log.e(TAG, "SOCKET_TIMEOUT_EXCEPTION : ${se.message}", )
            disconnect()
        }
        catch (e:Exception)
        {
            disconnect()
            e.printStackTrace()
            Log.e(TAG, "SOCKET_CONNECTION_ERROR : ${e.message}")
        }
        finally {
            Log.e(TAG, "FINALLY_BLOCK : ", )
        }
    }
    fun disconnect() {
        try {
            Log.d(TAG, "disconnect: SOCKET_DISCONNECTED")
//            webSocket?.cancel()
            webSocket?.close(1000,"Disconnect Socket")
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "disconnect: ${e.message}", )
        }
    }

    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        return OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.MINUTES)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .pingInterval(5, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }
}