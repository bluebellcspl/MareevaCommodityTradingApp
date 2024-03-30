package com.bluebellcspl.maarevacommoditytradingapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatResponseModel(
    var Date: String,
    var FileExt: String,
    var FileMedia: String,
    var FromUser: String,
    var MessageType: String,
    var ToUser: String,
    var message: String,
    var messageId: String
):Parcelable