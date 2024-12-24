package com.bluebellcspl.maarevacommoditytradingapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InvoicePreviewFetchDataModel(
    var BuyerId: String,
    var CommodityId: String,
    var CompanyCode: String,
    var Language: String,
    var PCARegId: String,
    var VehicleNo:String
):Parcelable