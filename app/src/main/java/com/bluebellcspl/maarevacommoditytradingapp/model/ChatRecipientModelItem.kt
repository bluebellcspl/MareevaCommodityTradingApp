package com.bluebellcspl.maarevacommoditytradingapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatRecipientModelItem(
    var GujName: String,
    var GujShortName: String,
    var MobileNo: String,
    var MsgReadCount: String,
    var Name: String,
    var ProfileImage: String,
    var RegisterId: String,
    var RoleId: String,
    var ShortName: String
):Parcelable