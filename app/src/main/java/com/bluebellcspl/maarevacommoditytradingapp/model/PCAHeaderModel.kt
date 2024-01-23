package com.bluebellcspl.maarevacommoditytradingapp.model

data class PCAHeaderModel(
    var AvgPrice: String,
    var PCADetailModel: ArrayList<PCADetailModel>,
    var PCAName: String,
    var TotalCost: String,
    var TotalPurchasedBags: String,
    var Expandable: Boolean = false,
){
    fun isExpandable(): Boolean {
        return Expandable
    }
}