package com.bluebellcspl.maarevacommoditytradingapp.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PCAAuctionDetailModel(
    var APMCId: String,
    var Action: String,
    var ApiPCAAuctionDetail: ArrayList<ApiPCAAuctionDetail>,
    var AvgPrice: String,
    var BuyerBori: String,
    var BuyerId: String,
    var BuyerLowerPrice: String,
    var BuyerPCABudget: String,
    var BuyerUpperPrice: String,
    var CommodityBhartiPrice: String,
    var CommodityId: String,
    var CommodityName: String,
    var CompanyCode: String,
    var CreateDate: String,
    var CreateUser: String,
    var Date: String,
    var GCACommRate: String,
    var IsAuctionStop: String,
    var MCessRate: String,
    var PCAAuctionHeaderId: String,
    var PCAAuctionMasterId: String,
    var PCACommRate: String,
    var PCAId: String,
    var PCARegId: String,
    var PerBoriRate: String,
    var PerLabourCharge: String,
    var RemainingBags: String,
    var RoleId: String,
    var TotalCost: String,
    var TotalPurchasedBags: String,
    var UpdateDate: String,
    var UpdateUser: String
) : Parcelable