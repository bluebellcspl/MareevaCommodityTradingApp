package com.bluebellcspl.maarevacommoditytradingapp.model

data class Detail(
    var Amount: String,
    var Bag: String,
    var CreateUser: String,
    var DetailsId: String,
    var GCACommission: String,
    var LastDayPrice: String,
    var LowerLimit: String,
    var MarketCess: String,
    var PCACommission: String,
    var PCAId: String,
    var PCAName: String,
    var PCARegId: String,
    var UpperLimit: String,
    var Basic:String = ""
)