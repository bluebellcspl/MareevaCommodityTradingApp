package com.bluebellcspl.maarevacommoditytradingapp.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class APIIndividualInvoiceShopwise(
    var Amount: String,
    var CommodityId: String,
    var CommodityName: String,
    var CurrentPrice: String,
    var Date: String,
    var PurchasedBag: String,
    var ShopEntries: ArrayList<IndPCAShopEntries>,
    var ShopId: String,
    var ShopNoName: String,
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