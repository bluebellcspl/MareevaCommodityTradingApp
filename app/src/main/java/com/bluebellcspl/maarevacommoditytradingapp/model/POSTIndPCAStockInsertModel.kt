package com.bluebellcspl.maarevacommoditytradingapp.model

data class POSTIndPCAStockInsertModel(
    var IndividualPCARegId: String,
    var IndividualPCAId: String,
    var CompanyCode: String,
    var Language: String,
    var FromDate: String,
    var ToDate: String,
    var Action: String,
    var IndividualPCAAtockInvoiceList: ArrayList<IndPCAStockInsertItemModel>,
)