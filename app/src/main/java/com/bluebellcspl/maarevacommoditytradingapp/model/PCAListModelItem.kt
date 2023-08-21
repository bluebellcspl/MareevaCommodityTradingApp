package com.bluebellcspl.maarevacommoditytradingapp.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PCAListModelItem(
    var APMCId: String,
    var APMCName: String,
    var Address: String,
    var ApprStatus: String,
    var BuyerId: String,
    var CommodityId: String,
    var CommodityName: String,
    var CompanyCode: String,
    var CreateDate: String,
    var CreateUser: String,
    var DistrictId: String,
    var DistrictName: String,
    var EmailId: String,
    var GCACommission: String,
    var IsActive: String,
    var MarketCess: String,
    var PCACommission: String,
    var PCAId: String,
    var PCAName: String,
    var PCAPhoneNumber: String,
    var RoleId: String,
    var RoleName: String,
    var StateId: String,
    var StateName: String,
    var UpdateDate: String,
    var UpdateUser: String,
    var RegId: String,
    var Typeofuser: String
):Parcelable