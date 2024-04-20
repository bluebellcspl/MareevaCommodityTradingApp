package com.bluebellcspl.maarevacommoditytradingapp.model

data class Shopwise(
    var Amount: String,
    var CurrentPrice: String,
    var Date: String,
    var PurchasedBag: String,
    var ShopEntries: ArrayList<ShopEntry>,
    var ShopId: String,
    var ShopName: String,
    var ShopNo: String,
    var ShopShortName: String,
    var isExpandable:Boolean = false,
    var isSelected:Boolean = false
){
    fun getExpandable ():Boolean{
        return isExpandable
    }

    fun getSelected():Boolean{
        return isSelected
    }
}