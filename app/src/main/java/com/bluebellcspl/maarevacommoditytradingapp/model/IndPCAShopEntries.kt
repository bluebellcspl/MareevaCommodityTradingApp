package com.bluebellcspl.maarevacommoditytradingapp.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class IndPCAShopEntries(
    var Amount: String,
    var Bags: String,
    var BillAmount: String,
    var BillApproxKg: String,
    var BillBags: String,
    var BillGST: String,
    var BillKg: String,
    var BillRate: String,
    var BillTotalAmount: String,
    var BillWeight: String,
    var BuyerId: String,
    var BuyerName: String,
    var Cdate: String,
    var CommodityBhartiPrice: String,
    var CommodityId: String,
    var CommodityName: String,
    var CompanyCode: String,
    var CreateUser: String,
    var CurrentPrice: String,
    var Date: String,
    var GCAInvoiceNo: String,
    var GstId: String,
    var IndividualPCAAuctionDetailId: String,
    var IndividualPCAAuctionMasterId: String,
    var IndividualPCAId: String,
    var IndividualPCARegId: String,
    var InvStatus: String,
    var ShopAddress: String,
    var ShopId: String,
    var ShopNo: String,
    var ShopNoName: String,
    var ShortShopName: String,
    var TotalPct: String,
    var Udate: String,
    var UpdateStatus: String,
    var UpdateUser: String,
    var Weight: String,
    var isExpandable:Boolean = false
):Parcelable{
    fun getExpandable ():Boolean{
        return isExpandable
    }
}