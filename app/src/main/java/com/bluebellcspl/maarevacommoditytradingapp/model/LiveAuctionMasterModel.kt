package com.bluebellcspl.maarevacommoditytradingapp.model

data class LiveAuctionMasterModel(
    var AllocatedBag: String,
    var Basic: String,
    var BudgetAmount: String,
    var PCAList: ArrayList<LiveAuctionPCAListModel>,
    var TotalCost: String,
    var TotalGCAComm: String,
    var TotalLabourCharge: String,
    var TotalMarketCess: String,
    var TotalPCAComm: String,
    var TotalTransportationCharge: String
)