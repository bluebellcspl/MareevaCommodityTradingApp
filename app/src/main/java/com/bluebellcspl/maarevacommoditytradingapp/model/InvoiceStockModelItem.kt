package com.bluebellcspl.maarevacommoditytradingapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InvoiceStockModelItem(
    var AvailableBag1: String,
    var AvailableBags: String,
    var AvailableWeight: String,
    var AvailableWeight1: String,
    var AvaliableAmount: String,
    var AvaliableAmount1: String,
    var BhartiPrice: String,
    var Cdate: String,
    var CommodityId: String,
    var CommodityName: String,
    var CompanyCode: String,
    var CreateUser: String,
    var Date: String,
    var HSNCode: String,
    var PCAId: String,
    var PCARegId: String,
    var StockId: String,
    var TotalAmount: String,
    var TotalBags: String,
    var TotalInvoiceApproxKg: String,
    var TotalInvoiceKg: String,
    var TotalRate: String,
    var TotalWeightAfterAuctionInKg: String,
    var Udate: String,
    var UpdateUser: String,
    var UsedBagAmount: String,
    var UsedBagRate: String,
    var UsedBagWeightKg: String,
    var UsedBags: String,
    var UsedInvoiceApproxKg: String,
    var UsedInvoiceKg: String,
    var isSelected:Boolean = false
):Parcelable