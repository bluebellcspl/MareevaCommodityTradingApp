package com.bluebellcspl.maarevacommoditytradingapp.model

data class POSTSelectedInvoiceListModel(
    var CompanyCode: String,
    var FromDate: String,
    var GCADataList: ArrayList<GCAData>,
    var Language: String,
    var PCARegId: String,
    var ToDate: String
)