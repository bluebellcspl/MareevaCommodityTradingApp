package com.bluebellcspl.maarevacommoditytradingapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class IndPCAInvoiceBagAdjustmentModel(
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
    var CommodityBhartiPrice: String,
    var CommodityId: String,
    var CommodityName: String,
    var CreateDate: String,
    var CreateUser: String,
    var Date: String,
    var GSTPct: String,
    var HSNCode: String,
    var InStockId: String,
    var IndividualPCAAuctionDetailId: String,
    var IndividualPCAAuctionMasterId: String,
    var IndividualPCARegId: String,
    var IndividualPCAId: String,
    var UpdateDate: String,
    var UpdateUser: String
):Parcelable