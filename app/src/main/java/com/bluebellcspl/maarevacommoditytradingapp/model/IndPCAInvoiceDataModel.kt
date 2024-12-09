package com.bluebellcspl.maarevacommoditytradingapp.model

data class IndPCAInvoiceDataModel(
    var APIIndividualInvoiceShopwiseList: ArrayList<APIIndividualInvoiceShopwise>,
    var CommodityId: String,
    var CompanyCode: String,
    var CreateDate: String,
    var CreateUser: String,
    var FromDate: String,
    var IndividualPCARegId: String,
    var Language: String,
    var ToDate: String,
    var UpdateDate: String,
    var UpdateUser: String
)