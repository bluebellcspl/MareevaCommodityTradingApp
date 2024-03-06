package com.bluebellcspl.maarevacommoditytradingapp.model

data class CommodityMasterModelItem(
    var CommodityId: Int,
    var CommodityName: String,
    var GujaratiCommodityName: String?,
    var Bharti: String,
    var CompanyCode: String,
    var CreateDate: String,
    var CreateUser: String,
    var IsActive: Boolean,
    var UpdateDate: String,
    var UpdateUser: String
)