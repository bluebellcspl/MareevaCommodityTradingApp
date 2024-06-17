package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.InvoicePreviewFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceStockModelItem
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

class POSTInvoiceStockList(var context: Context, var fragment: InvoicePreviewFragment, var dataList:ArrayList<InvoiceStockModelItem>) {
    val job = Job()
    val scope = CoroutineScope(job)
    private val commonUIUtility by lazy { CommonUIUtility(context) }
    val TAG = "POSTInvoiceStockList"

    init {
        postInvoiceStockList()
    }

    private fun postInvoiceStockList() {
        try {
            val JO = JsonObject().apply {
                addProperty("PCARegId",PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,""))
                addProperty("CompanyCode",PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,""))
                addProperty("Language",PrefUtil.getSystemLanguage())
                addProperty("FromDate","")
                addProperty("CurrentDate","")
                addProperty("ToDate","")
                addProperty("Action","Update")
                add("InvoiceList",null)
                add("InvoicePreviewListTable",null)
                add("UpdatePCAStock",Gson().toJsonTree(dataList).asJsonArray)
            }

            Log.d(TAG, "postInvoiceStockList: JSON_UPDATE_INVOICE_LIST : $JO")
        } catch (e: Exception) {
            job.cancel()
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
            e.printStackTrace()
            Log.e(TAG, "postInvoiceStockList: ")
        }
    }
}