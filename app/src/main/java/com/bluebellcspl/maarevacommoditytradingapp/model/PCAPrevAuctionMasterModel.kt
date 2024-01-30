package com.bluebellcspl.maarevacommoditytradingapp.model

data class PCAPrevAuctionMasterModel(
    var Date: String,
    var LastPCATotalAvgRate: String,
    var LastPCATotalCost: String,
    var LastTotalPurchasedBags: String,
    var PCAHeaderModel: ArrayList<PCAHeaderModel>
)