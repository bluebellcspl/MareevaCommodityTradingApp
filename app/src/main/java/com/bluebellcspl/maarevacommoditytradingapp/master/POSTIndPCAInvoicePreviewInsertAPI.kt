package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAInvoicePreviewFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoiceBagAdjustmentModel
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoiceDataInsertModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class POSTIndPCAInvoicePreviewInsertAPI(
    var context: Context,
    var fragment: IndPCAInvoicePreviewFragment,
    var model: IndPCAInvoiceDataInsertModel,
    var _StockList: ArrayList<IndPCAInvoiceBagAdjustmentModel>
) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "POSTIndPCAInvoicePreviewInsertAPI"

    init {
        sendIndPCAInvoicePreviewData()
    }

    private fun sendIndPCAInvoicePreviewData() {
        try {
            commonUIUtility.showProgress()
            val stockListJO = JsonObject().apply {
                addProperty("IndividualPCARegId", PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, ""))
                addProperty("IndividualPCAId", PrefUtil.getString(PrefUtil.KEY_IND_PCA_ID, ""))
                addProperty("CompanyCode", PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE, ""))
                addProperty("Language", PrefUtil.getSystemLanguage())
                addProperty("FromDate", "")
                addProperty("ToDate", "")
                addProperty("Action", "Update")
                add("IndividualPCAAtockInvoiceList", Gson().toJsonTree(_StockList).asJsonArray)
            }

            val invoiceJO = Gson().toJsonTree(model).asJsonObject

            Log.d(TAG, "sendIndPCAInvoicePreviewData: STOCK_LIST_UPDATE_JSON : $stockListJO")
            Log.d(TAG, "sendIndPCAInvoicePreviewData: INVOICE_INSERT_JSON : $invoiceJO")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO) {
//                val stockResult =

                val stockAPI = async(Dispatchers.IO) {
                    APICall.postIndPCAStockList(stockListJO)
                }

                val stockAPIResponse = stockAPI.await()
                if (stockAPIResponse.isSuccessful) {
                    val stockResponseJO = stockAPIResponse.body()!!
                    if (stockResponseJO.get("Success").asBoolean) {
                        val invoiceAPI = async(Dispatchers.IO) {
                            APICall.postIndPCAInvoiceInsert(invoiceJO)
                        }

                        val invoiceAPIResponse = invoiceAPI.await()

                        if (invoiceAPIResponse.isSuccessful) {
                            val invoiceResponseJO = invoiceAPIResponse.body()!!
                            if (invoiceResponseJO.get("Success").asBoolean) {
                                withContext(Dispatchers.Main){
                                    fragment.downloadInvoice(invoiceResponseJO.get("invoiceNumber").asString)
                                    withContext(Dispatchers.IO){
                                        delay(2000)
                                    }
                                    commonUIUtility.dismissProgress()
                                    fragment.successRedirect()
                                }
                                job.complete()
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "sendIndPCAInvoicePreviewData: ERROR_INVOICE_STOCK_UPDATE_API")
                    Log.e(TAG, "sendIndPCAInvoicePreviewData: ${stockAPIResponse.errorBody()}")
                    withContext(Dispatchers.Main) {
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                    }
                    job.cancel()
                }
            }
            job.complete()
        } catch (e: Exception) {
            e.printStackTrace()
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
            Log.e(TAG, "sendIndPCAInvoicePreviewData: ${e.message}")
        }
    }

}