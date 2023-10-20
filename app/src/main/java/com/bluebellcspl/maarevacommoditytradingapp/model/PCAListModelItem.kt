package com.bluebellcspl.maarevacommoditytradingapp.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PCAListModelItem(
    var APMCId: String,
    var APMCName: String,
    var Address: String,
    var AdharNo: String,
    var AdharPhoto: String,
    var ApprStatus: String,
    var BuyerId: String,
    var CityId: String,
    var CityName: String,
    var CommodityId: String,
    var CommodityName: String,
    var CompanyCode: String,
    var CreateDate: String,
    var CreateUser: String,
    var DistrictId: String,
    var DistrictName: String,
    var EmailId: String,
    var GCACommission: String,
    var GSTCertiPhoto: String,
    var GSTNo: String,
    var IsActive: String,
    var LabourCharges: String,
    var LicenseCopyPhoto: String,
    var MarketCess: String,
    var Mobile2: String,
    var PCACommission: String,
    var PCAId: String,
    var PCAName: String,
    var PCAPhoneNumber: String,
    var PCARegId: String,
    var PanCardNo: String,
    var PanCardPhoto: String,
    var ProfilePic: String,
    var RoleId: String,
    var RoleName: String,
    var StateId: String,
    var StateName: String,
    var UpdateDate: String,
    var UpdateUser: String
):Parcelable
