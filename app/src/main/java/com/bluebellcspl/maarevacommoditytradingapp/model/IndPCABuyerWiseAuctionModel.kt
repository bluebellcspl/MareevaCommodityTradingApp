package com.bluebellcspl.maarevacommoditytradingapp.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class IndPCABuyerWiseAuctionModel(
    var BuyerId: String,
    var BuyerName: String,
    var Rate:String,
    var Bags:String,
    var Total:String
): Parcelable
