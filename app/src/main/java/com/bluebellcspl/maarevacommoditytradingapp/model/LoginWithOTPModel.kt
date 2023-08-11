package com.bluebellcspl.maarevacommoditytradingapp.model

data class LoginWithOTPModel(
    var APMCId: String,
    var CommodityId: String,
    var DistrictId: String,
    var MobileNo: String,
    var StateId: String,
    var UserType: String
)