package com.bluebellcspl.maarevacommoditytradingapp.model

data class POSTAuctionStartStopAPIModel(
    var BuyerRegId: String,
    var CommodityId: String,
    var CompanyCode: String,
    var Date: String,
    var PCARegId: String,
    var Typeofuser: String,
    var UpdateDate: String,
    var UpdateUser: String,
    var isAuctionStop: String
)