package com.bluebellcspl.maarevacommoditytradingapp.model

data class UserChatInfoModel(
    var SenderId:String,
    var ReceiverId:String,
    var SenderRollId:String,
    var ReceiverRollId:String,
    var Name:String,
    var ShortName:String,
    var GujaratiName:String,
    var GujaratiShortName:String,
    var AppoveStatus:String,
    var IsActive:String
)
