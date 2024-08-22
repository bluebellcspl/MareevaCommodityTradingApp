package com.bluebellcspl.maarevacommoditytradingapp.model

data class PCAHeaderModel(
    var AvgPrice: String,
    var PCADetailModel: ArrayList<PCADetailModel>,
    var PCAGujaratiName: String,
    var PCAGujaratiShortName: String,
    var PCAName: String,
    var PCAShortName: String,
    var TotalCost: String,
    var TotalPurchasedBags: String,
    var Expandable: Boolean = false,
){
    fun isExpandable(): Boolean {
        return Expandable
    }
}