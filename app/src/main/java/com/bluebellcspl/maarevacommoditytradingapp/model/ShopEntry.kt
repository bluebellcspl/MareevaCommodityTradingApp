package com.bluebellcspl.maarevacommoditytradingapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShopEntry(
    var Amount:String,
    var Bags:String,
    var Cdate:String,
    var CompanyCode:String,
    var CreateUser:String,
    var CurrentPrice:String,
    var Date:String,
    var GCACommCharge:String,
    var GCACommRate:String,
    var GCAInvoiceNo:String,
    var InvStatus:String,
    var LabourCharge:String,
    var MarketCessCharge:String,
    var MarketCessRate:String,
    var PCAAuctionDetailId:String,
    var PCAAuctionMasterId:String,
    var PCACommCharge:String,
    var PCACommRate:String,
    var PerBoriLabourCharge:String,
    var PerBoriRate:String,
    var ShopAddress:String,
    var ShopId:String,
    var ShopNo:String,
    var ShopNoName:String,
    var ShortShopName:String,
    var TotalExpense:String,
    var TransportationCharge:String,
    var Udate:String,
    var UpdateUser:String,
    var isSelected:Boolean = false
):Parcelable{
    fun getSelected():Boolean{
        return isSelected
    }
}