package com.bluebellcspl.maarevacommoditytradingapp.model

data class BuyerPrevAuctionMasterModel(
    var Date: String,
    var LastPCATotalAvgRate: String,
    var LastPCATotalCost: String,
    var LastTotalPurchasedBags: String,
    var PCAHeaderModel: ArrayList<PCAHeaderModel>
)