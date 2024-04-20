package com.bluebellcspl.maarevacommoditytradingapp.model

data class InvoiceDataModel(
    var CompanyCode: String,
    var CreateDate: String?,
    var CreateUser: String?,
    var FromDate: String,
    var Language: String,
    var PCARegId: String,
    var ShopwiseList: ArrayList<Shopwise>,
    var ToDate: String,
    var UpdateDate: String?,
    var UpdateUser: String?
)