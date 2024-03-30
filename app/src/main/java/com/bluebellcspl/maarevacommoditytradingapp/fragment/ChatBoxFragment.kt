package com.bluebellcspl.maarevacommoditytradingapp.fragment

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.ChatBoxMessageAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.constants.URLHelper
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentChatBoxBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.ChatResponseModel
import com.bluebellcspl.maarevacommoditytradingapp.webSocketHelper.SocketHandler
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class ChatBoxFragment : Fragment() {
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    lateinit var binding: FragmentChatBoxBinding
    private val navController by lazy { findNavController() }
    private val args by navArgs<ChatBoxFragmentArgs>()
    private var webSocket: WebSocket? = null
    private var isWebSocketConnected = false
    private var isConnectingWebSocket = false
    lateinit var RECEIVER_ID: String
    lateinit var SENDER_ID: String
    lateinit var chatBoxMessageAdapter: ChatBoxMessageAdapter
    val TAG = "ChatBoxFragment"
    var selectedImgURI: Uri? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat_box, container, false)
        (activity as AppCompatActivity?)!!.supportActionBar!!.setTitle(args.userChatInfoModel.ShortName)
        SENDER_ID = args.userChatInfoModel.SenderId
        RECEIVER_ID = args.userChatInfoModel.ReceiverId
        chatBoxMessageAdapter = ChatBoxMessageAdapter(requireContext(), SENDER_ID)
        binding.rcViewChat.adapter = chatBoxMessageAdapter
        setOnClickListeners()
        return binding.root
    }

    private fun setOnClickListeners() {
        try {
            binding.btnSend.setOnClickListener {
                if (binding.edtChatMessage.text.toString().trim().isNotEmpty()) {
                    if (webSocket != null) {
                        var chatMessageModel = ChatResponseModel(
                            DateUtility().getyyyyMMdd(),
                            "",
                            "",
                            SENDER_ID,
                            "text",
                            RECEIVER_ID,
                            if (binding.edtChatMessage.text.toString().isNotEmpty()) {
                                binding.edtChatMessage.text.toString().trim()
                            } else {
                                ""
                            },
                            ""
                        )
                        var chatJson = Gson().toJson(chatMessageModel)
                        webSocket?.send(chatJson)
                        binding.edtChatMessage.setText("")
                    }
                } else {
                    commonUIUtility.showToast("Please Enter Message!")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "setOnClickListeners: ${e.message}")
        }
    }

    private fun onNewMessageReceived(model: ChatResponseModel) {
        try {
            binding.rcViewChat.scrollToPosition(chatBoxMessageAdapter.itemCount)
            chatBoxMessageAdapter.addMessage(model)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "onNewMessageReceived: ${e.message}")
        }
    }

    private inner class ChatSocketListener : WebSocketListener() {
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            this@ChatBoxFragment.webSocket = null
            super.onClosed(webSocket, code, reason)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            this@ChatBoxFragment.webSocket = null
            super.onFailure(webSocket, t, response)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            try {
                Log.d(TAG, "onMessage: RECEIVED_MESSAGE : $text")
                val jsonObject = JsonParser().parse(text).asJsonObject
                var gson = Gson()
                var newChatMessage =
                    gson.fromJson(jsonObject.toString(), ChatResponseModel::class.java)
                lifecycleScope.launch(Dispatchers.Main)
                {
                    if ((newChatMessage.FromUser.equals(SENDER_ID) && newChatMessage.ToUser.equals(
                            RECEIVER_ID
                        )) ||
                        (newChatMessage.FromUser.equals(RECEIVER_ID) && newChatMessage.ToUser.equals(
                            SENDER_ID
                        ))
                    ) {
                        onNewMessageReceived(newChatMessage!!)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "onMessage: ${e.message}")
            }
        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            this@ChatBoxFragment.webSocket = webSocket
            super.onOpen(webSocket, response)
            Log.d(TAG, "onOpen: WEB_SOCKET_ID : ${webSocket.toString()}")
            Log.d(TAG, "onOpen: SOCKET_CONNECTED")
        }
    }

    private fun disconnectSocket() {
        try {
            webSocket?.cancel()
            webSocket = null
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "disconnectSocket: ${e.message}")
        }
    }

    override fun onStart() {
        super.onStart()

        if (!isWebSocketConnected && !isConnectingWebSocket) {
            Log.d(TAG, "onStart: WEB_SOCKET_CONNECT onStart")

            // Set the flag to indicate that a connection attempt is in progress
            isConnectingWebSocket = true

            lifecycleScope.launch(Dispatchers.IO) {
                Log.d(TAG, "onStart: CONNCECTING_SOCKET : onStart")
                webSocket = SocketHandler.getWebSocket(
                    URLHelper.TESTING_CHAT_SOCKET,
                    this@ChatBoxFragment.ChatSocketListener()
                )
            }
            isWebSocketConnected = true

            // Reset the flag after the connection attempt
            isConnectingWebSocket = false
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isWebSocketConnected && !isConnectingWebSocket && webSocket == null) {
            Log.d(TAG, "onResume: WEB_SOCKET_CONNECT onResume")

            // Set the flag to indicate that a connection attempt is in progress
            isConnectingWebSocket = true

            lifecycleScope.launch(Dispatchers.IO) {
                Log.d(TAG, "onResume: CONNCECTING_SOCKET : onResume")
                webSocket = SocketHandler.getWebSocket(
                    URLHelper.TESTING_CHAT_SOCKET,
                    this@ChatBoxFragment.ChatSocketListener()
                )
                // Set the flag to indicate that the socket is now connected
                isWebSocketConnected = true
                // Reset the flag after the connection attempt
                isConnectingWebSocket = false
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (isWebSocketConnected) {
            Log.d(TAG, "onStop: WEB_SOCKET_DISCONNECT onStop")
//            webSocketClient.disconnect()
            disconnectSocket()
            isWebSocketConnected = false
        }
    }
}