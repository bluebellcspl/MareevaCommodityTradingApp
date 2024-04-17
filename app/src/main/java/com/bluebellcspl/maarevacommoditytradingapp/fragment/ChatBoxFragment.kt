package com.bluebellcspl.maarevacommoditytradingapp.fragment

import ConnectionCheck
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.ChatImageActivity
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.ChatBoxMessageAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.constants.URLHelper
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentChatBoxBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.PostChatMediaAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.PreviousChatAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.ChatHistoryModel
import com.bluebellcspl.maarevacommoditytradingapp.model.ChatImageInfoModel
import com.bluebellcspl.maarevacommoditytradingapp.model.ChatResponseModel
import com.bluebellcspl.maarevacommoditytradingapp.model.POSTChatMediaModel
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.ChatRecyclerViewHelper
import com.bluebellcspl.maarevacommoditytradingapp.webSocketHelper.SocketHandler
import com.devlomi.record_view.OnRecordListener
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.IOException
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class ChatBoxFragment : Fragment(), ChatRecyclerViewHelper {
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
    var hasResumed =false
    val TAG = "ChatBoxFragment"
    var selectedImgURI: Uri? = null
    private lateinit var mediaRecord: MediaRecorder
    private var audioRecordedPath: String = ""
    var PAGE = 1
    var ItemPerPage = 20
    var isLoading = false
    var isInitialDataLoaded = false
    var hasNextPage = false
    private val getImageContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val uri: Uri = data?.data!!
                selectedImgURI = data?.data!!
                sendImageToServer()
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                commonUIUtility.showToast("Task Cancelled")
            } else {
                commonUIUtility.showToast("Error")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat_box, container, false)
        (activity as AppCompatActivity?)!!.supportActionBar!!.setTitle(args.userChatInfoModel.ShortName)
        binding.btnVoiceRecord.setRecordView(binding.recordView)
        binding.btnVoiceRecord.isListenForRecord = false
        SENDER_ID = args.userChatInfoModel.SenderId
        RECEIVER_ID = args.userChatInfoModel.ReceiverId
        chatBoxMessageAdapter = ChatBoxMessageAdapter(requireContext(), SENDER_ID, this)
        binding.rcViewChat.setHasFixedSize(true)
        binding.rcViewChat.adapter = chatBoxMessageAdapter
        if (args.userChatInfoModel.ReceiverRollId.equals("1")) {
            binding.llChatBox.visibility = View.GONE
        }
        setOnClickListeners()


        binding.rcViewChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val linearLayoutManager = binding.rcViewChat.layoutManager as? LinearLayoutManager
                linearLayoutManager?.let {
                    if (dy < 0 && it.findFirstVisibleItemPosition() == 0) {
                        if (!isLoading && hasNextPage) {
                            PAGE++
                            isLoading = true
                            binding.progressBarChatBox.visibility = View.VISIBLE
                            PreviousChatAPI(
                                requireContext(),
                                this@ChatBoxFragment,
                                args.userChatInfoModel,
                                PAGE,
                                ItemPerPage,false
                            )
                        }
                    }
                }
            }
        })


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

            binding.btnImage.setOnClickListener {
                checkPermissions()
            }

            binding.btnVoiceRecord.setOnClickListener {
                checkRecordingPermissions()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "setOnClickListeners: ${e.message}")
        }
    }

    private fun onNewMessageReceived(model: ChatResponseModel) {
        try {
            binding.rcViewChat.smoothScrollToPosition(chatBoxMessageAdapter.itemCount)
            chatBoxMessageAdapter.addMessage(model)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "onNewMessageReceived: ${e.message}")
        }
    }

    private fun checkPermissions() {
        var permissionList = ArrayList<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG, "checkPermissions: ${Build.VERSION.CODENAME} - READ_MEDIA_IMAGES")
            permissionList.add(android.Manifest.permission.READ_MEDIA_IMAGES)
            permissionList.add(android.Manifest.permission.READ_MEDIA_AUDIO)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            Log.d(TAG, "checkPermissions: ${Build.VERSION.CODENAME} - READ_EXTERNAL_STORAGE")
            permissionList.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        try {
            Dexter.withContext(requireContext()).withPermissions(permissionList)
                .withListener(object :
                    MultiplePermissionsListener {
                    override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                        if (p0!!.areAllPermissionsGranted()) {
                            ImagePicker.with(requireActivity())
                                .compress(1024)
                                .galleryOnly()
                                .createIntent { intent ->
                                    getImageContent.launch(intent)
                                }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?
                    ) {
                        p1!!.continuePermissionRequest()
                    }
                }).onSameThread().check()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "checkPermissions: ${e.message}")
        }
    }

    private fun sendImageToServer() {
        try {
            val file = prepareFilePart(requireContext(), "FileMedia", selectedImgURI!!)
            val chatResponseModel = POSTChatMediaModel(
                Date = DateUtility().getyyyyMMdd(),
                FileExt = getFileExtension(requireContext(), selectedImgURI!!),
                FileMedia = file,
                FromUser = SENDER_ID,
                MessageType = "file",
                ToUser = RECEIVER_ID,
                message = "",
                messageId = ""
            )
            PostChatMediaAPI(requireContext(), this@ChatBoxFragment, chatResponseModel)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "sendImageToServer: ${e.message}")
        }
    }

    private fun prepareFilePart(
        context: Context,
        partName: String,
        fileUri: Uri
    ): MultipartBody.Part {
        val contentResolver: ContentResolver = context.contentResolver
        val file: File = getFileFromUri(context, fileUri)!!
        val requestFile: RequestBody = file.asRequestBody("multipart/form-data".toMediaType())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }

    fun getFileFromUri(context: Context, uri: Uri): File? {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        var file: File? = null
        try {
            val contentResolver = context.contentResolver
            val fileExtension = getFileExtension(context, uri)
            val fileName = "IMG_${System.currentTimeMillis()}.$fileExtension"
            val outputPath = "${context.cacheDir}/$fileName"
            inputStream = contentResolver.openInputStream(uri)
            outputStream = FileOutputStream(outputPath)
            if (inputStream != null) {
                val buf = ByteArray(1024)
                var len: Int
                while (inputStream.read(buf).also { len = it } > 0) {
                    outputStream.write(buf, 0, len)
                }
                file = File(outputPath)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
                outputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return file
    }

    private fun getFileExtension(context: Context, uri: Uri): String {
        return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val mime = context.contentResolver.getType(uri)
            mime?.let {
                MimeTypeMap.getSingleton().getExtensionFromMimeType(it)
            } ?: ""
        } else {
            MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path)).toString())
        }
    }

    private fun checkRecordingPermissions() {
        var permissionList = ArrayList<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG, "checkPermissions: ${Build.VERSION.CODENAME} - RECORD_AUDIO")
            permissionList.add(android.Manifest.permission.RECORD_AUDIO)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            Log.d(TAG, "checkPermissions: ${Build.VERSION.CODENAME} - RECORD_AUDIO")
            permissionList.add(android.Manifest.permission.RECORD_AUDIO)
        }
        try {
            Dexter.withContext(requireActivity()).withPermissions(permissionList)
                .withListener(object :
                    MultiplePermissionsListener {
                    override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                        if (p0!!.areAllPermissionsGranted()) {
                            bindVoiceRecorder()
                        }
                        if (p0!!.isAnyPermissionPermanentlyDenied) {
                            Toast.makeText(
                                requireActivity(), "Please Grant Recording Permission!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?
                    ) {
                        p1!!.continuePermissionRequest()
                    }
                }).onSameThread().check()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "checkPermissions: ${e.message}")
        }
    }

    private fun bindVoiceRecorder() {
        try {
            binding.btnVoiceRecord.isListenForRecord = true

            binding.recordView.setOnRecordListener(object : OnRecordListener {
                override fun onStart() {
                    //Start Recording..
                    Log.d("RecordView", "onStart")
                    setupRecording(requireActivity())
                    try {
                        mediaRecord.prepare()
                        mediaRecord.start()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e(TAG, "onStart: ${e.message}")
                    }

                    binding.recordView.visibility = View.VISIBLE
                    binding.edtChatMessage.visibility = View.GONE
                    binding.btnImage.visibility = View.GONE
                }

                override fun onCancel() {
                    //On Swipe To Cancel
                    Log.d("RecordView", "onCancel")
                    mediaRecord.reset()
                    mediaRecord.release()
                    val deleteRecordingFile = File(audioRecordedPath)
                    if (deleteRecordingFile.exists()) {
                        deleteRecordingFile.delete()
                        binding.recordView.visibility = View.GONE
                        binding.edtChatMessage.visibility = View.VISIBLE
                        binding.btnImage.visibility = View.VISIBLE
                    }
                }

                override fun onFinish(recordTime: Long, limitReached: Boolean) {
                    //Stop Recording..
                    //limitReached to determine if the Record was finished when time limit reached.

                    Log.d("RecordView", "onFinish")

                    mediaRecord.stop()
                    mediaRecord.release()
                    binding.recordView.visibility = View.GONE
                    binding.edtChatMessage.visibility = View.VISIBLE
                    binding.btnImage.visibility = View.VISIBLE

                    sendRecordingMessage(audioRecordedPath)
                }

                override fun onLessThanSecond() {
                    //When the record time is less than One Second
                    Log.d("RecordView", "onLessThanSecond")
                    mediaRecord.reset()
                    mediaRecord.release()
                    val deleteRecordingFile = File(audioRecordedPath)
                    if (deleteRecordingFile.exists()) {
                        deleteRecordingFile.delete()
                        binding.recordView.visibility = View.GONE
                        binding.edtChatMessage.visibility = View.VISIBLE
                        binding.btnImage.visibility = View.VISIBLE
                    }
                }

                override fun onLock() {
                    //When Lock gets activated
                    Log.d("RecordView", "onLock")
                }
            })

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindVoiceRecorder: ${e.message}")
        }
    }

    private fun sendRecordingMessage(audioRecordedPath: String) {
        try {
            Log.d(TAG, "sendRecordingMessage: RECORDED_AUDIO : $audioRecordedPath")
            Log.d(
                TAG,
                "sendRecordingMessage: RECORDED_AUDIO_URI : ${Uri.fromFile(File(audioRecordedPath))}"
            )
            val file = prepareFilePart(
                requireActivity(),
                "FileMedia",
                Uri.fromFile(File(audioRecordedPath))
            )
            val chatResponseModel = POSTChatMediaModel(
                Date = DateUtility().getyyyyMMdd(),
                FileExt = ".mp3",
                FileMedia = file,
                FromUser = SENDER_ID,
                MessageType = "audio",
                ToUser = RECEIVER_ID,
                message = "",
                messageId = ""
            )
            PostChatMediaAPI(requireContext(), this@ChatBoxFragment, chatResponseModel)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "sendRecordingMessage: ${e.message}")
        }
    }

    private fun setupRecording(context: Context) {
        mediaRecord = MediaRecorder()
//        val file = File("${context.cacheDir}/ChatMedia/Recording")
        mediaRecord.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecord.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecord.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

        val file =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).path + "/ChatMedia/Recording")

        if (!file.exists()) {
            file.mkdirs()
        }
        val fileName = "REC_" + System.currentTimeMillis().toString() + ".mp3"
        val filePath = File(file.absolutePath + File.separator + fileName)
        audioRecordedPath = filePath.absolutePath
        mediaRecord.setOutputFile(audioRecordedPath)
    }

    fun loadChatHistory(previousChats: ChatHistoryModel) {
        try {
            chatBoxMessageAdapter.loadPreviousChat(previousChats)
            binding.rcViewChat.recycledViewPool.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadInitialChatHistory(previousChats: ChatHistoryModel) {
        try {
            chatBoxMessageAdapter.loadInitialChat(previousChats)
            binding.rcViewChat.scrollToPosition(previousChats.size-1)
        } catch (e: Exception) {
            e.printStackTrace()
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
            requireActivity().runOnUiThread {
                    PreviousChatAPI(
                        requireContext(),
                        this@ChatBoxFragment,
                        args.userChatInfoModel,
                        PAGE,
                        ItemPerPage,
                        true
                    )
            }
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
                if (ConnectionCheck.isConnected(requireContext())) {
                    webSocket = SocketHandler.getWebSocket(
                        URLHelper.LIVE_CHAT_SOCKET,
                        this@ChatBoxFragment.ChatSocketListener()
                    )
                }
            }
            isWebSocketConnected = true

            // Reset the flag after the connection attempt
            isConnectingWebSocket = false
        }
    }

    override fun onResume() {
        super.onResume()
        hasResumed = true
        if (!isWebSocketConnected && !isConnectingWebSocket && webSocket == null) {
            Log.d(TAG, "onResume: WEB_SOCKET_CONNECT onResume")

            // Set the flag to indicate that a connection attempt is in progress
            isConnectingWebSocket = true

            lifecycleScope.launch(Dispatchers.IO) {
                Log.d(TAG, "onResume: CONNCECTING_SOCKET : onResume")
                if (ConnectionCheck.isConnected(requireContext())) {
                    webSocket = SocketHandler.getWebSocket(
                        URLHelper.LIVE_CHAT_SOCKET,
                        this@ChatBoxFragment.ChatSocketListener()
                    )
                }
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

    override fun onImageItemClick(chatMessage: ChatResponseModel) {
        val chatImageInfoModel = ChatImageInfoModel(
            chatMessage.FromUser,
            chatMessage.ToUser,
            chatMessage.FileMedia
        )

//        navController.navigate(
//            ChatBoxFragmentDirections.actionChatBoxFragmentToChatImageViewFragment(
//                chatImageInfoModel
//            )
//        )
        val intent = Intent(requireActivity(),ChatImageActivity::class.java)
        intent.putExtra("ChatMessage",chatMessage)
        requireActivity().startActivity(intent)
    }
}