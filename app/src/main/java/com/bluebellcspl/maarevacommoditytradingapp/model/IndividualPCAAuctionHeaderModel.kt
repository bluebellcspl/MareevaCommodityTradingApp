package com.bluebellcspl.maarevacommoditytradingapp.model

data class IndividualPCAAuctionHeaderModel(
    var APMCId: String,
    var APMCName: String,
    var CommodityId: String,
    var CompanyCode: String,
    var Date: String,
    var IndividualPCAAuctionDetail: ArrayList<IndividualPCAAuctionDetail>,
    var IndividualPCAAuctionHeaderId: String,
    var IndividualPCAAuctionMasterId: String,
    var IndividualPCAId: String,
    var IndividualPCAMobile: String,
    var IndividualPCAName: String,
    var IndividualPCARegId: String,
    var TotalCost: String,
    var TotalPurchasedBags: String
)