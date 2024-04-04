package com.bluebellcspl.maarevacommoditytradingapp.model

import okhttp3.MultipartBody


data class POSTChatMediaModel(
    var Date: String,
    var FileExt: String,
    var FileMedia: MultipartBody.Part,
    var FromUser: String,
    var MessageType: String,
    var ToUser: String,
    var message: String,
    var messageId: String
)
