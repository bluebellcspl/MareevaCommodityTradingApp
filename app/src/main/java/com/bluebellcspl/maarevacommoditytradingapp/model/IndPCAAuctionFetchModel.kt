package com.bluebellcspl.maarevacommoditytradingapp.model

data class IndPCAAuctionFetchModel(
    var APMCId: String,
    var APMCName: String,
    var Action: String,
    var ApiIndividualPCAAuctionDetail: ArrayList<ApiIndividualPCAAuctionDetail>,
    var CommodityBhartiPrice: String,
    var CommodityId: String,
    var CommodityName: String,
    var CompanyCode: String,
    var CreateDate: String,
    var CreateUser: String,
    var Date: String,
    var IndividualPCAAuctionHeaderId: String,
    var IndividualPCAAuctionMasterId: String,
    var IndividualPCAId: String,
    var IndividualPCARegId: String,
    var RoleId: String,
    var TotalAmount: String,
    var TotalAveragePrice: String,
    var TotalBags: String,
    var TotalCost: String,
    var TotalPurchasedBags: String,
    var UpdateDate: String,
    var UpdateUser: String
)