package com.bluebellcspl.maarevacommoditytradingapp.model

data class IndPCAStockBuyerWiseModelItem(
    var BuyerId: String,
    var BuyerName: String,
    var CommodityBhartiPrice: String,
    var CommodityId: String,
    var CommodityName: String,
    var EarliestDate: String,
    var InPCAId: String,
    var InPCARegId: String,
    var LatestDate: String,
    var TotalAvailableAmount: String,
    var TotalAvailableBags: String,
    var TotalAvailableGST: String,
    var TotalAvailableTotalAmount: String,
    var TotalAvailableWeight: String,
    var TotalBillRate: String
)