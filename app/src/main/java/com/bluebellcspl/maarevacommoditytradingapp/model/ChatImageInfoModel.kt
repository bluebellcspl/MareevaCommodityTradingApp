package com.bluebellcspl.maarevacommoditytradingapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatImageInfoModel(
    var SenderId:String,
    var ReceiverId:String,
    var ImageUrl:String
):Parcelable
