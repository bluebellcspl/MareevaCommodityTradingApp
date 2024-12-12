package com.bluebellcspl.maarevacommoditytradingapp.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class IndPCAInvoiceStockModelItem(
    var AvailableBag1: String,
    var AvailableBags: String,
    var AvailableGST: String,
    var AvailableTotalAmount: String,
    var AvailableWeight: String,
    var AvailableWeight1: String,
    var AvaliableAmount: String,
    var AvaliableAmount1: String,
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
    var Date: String,
    var HsnAsc: String,
    var InPCAAuctionDetailId: String,
    var InPCAId: String,
    var InPCARegId: String,
    var InStockId: String,
    var Udate: String = "",
    var UpdateUser: String = "",
    var UsedBillAmount: String,
    var UsedBillApproxKg: String,
    var UsedBillBags: String,
    var UsedBillGST: String,
    var UsedBillKg: String,
    var UsedBillRate: String,
    var UsedBillTotalAmount: String,
    var UsedBillWeight: String,
    var isSelected:Boolean = false
):Parcelable