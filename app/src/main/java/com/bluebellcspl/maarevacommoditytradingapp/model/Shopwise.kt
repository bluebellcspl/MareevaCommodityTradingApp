package com.bluebellcspl.maarevacommoditytradingapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Shopwise(
    var Amount: String,
    var CommodityBhartiPrice: String,
    var CommodityId: String,
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
):Parcelable{
    fun getExpandable ():Boolean{
        return isExpandable
    }

    fun getSelected():Boolean{
        return isSelected
    }
}