package com.bluebellcspl.maarevacommoditytradingapp.model

data class IndPCAAuctionReportModel(
    var CommodityBhartiPrice: String,
    var CommodityId: String,
    var CommodityName: String,
    var Date: String,
    var IndividualPCAAuctionHeaderModel: ArrayList<IndividualPCAAuctionHeaderModel>,
    var TotalAmount: String,
    var TotalAveragePrice: String,
    var TotalBags: String
)