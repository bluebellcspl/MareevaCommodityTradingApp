package com.bluebellcspl.maarevacommoditytradingapp.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ApiPCAAuctionDetail(
    var Amount: String,
    var Bags: String,
    var CreateUser: String,
    var CurrentPrice: String,
    var PCAAuctionDetailId: String,
    var PCAAuctionMasterId: String,
    var ShopId: String,
    var ShopName: String,
    var ShopNo: String,
    var UpdateUser: String
):Parcelable