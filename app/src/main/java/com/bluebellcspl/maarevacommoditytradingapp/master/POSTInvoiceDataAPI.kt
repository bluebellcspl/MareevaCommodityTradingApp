package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.InvoicePreviewFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceStockModelItem
import com.bluebellcspl.maarevacommoditytradingapp.model.PostInvoiceDataModel
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

class POSTInvoiceDataAPI(var context: Context, var fragment: InvoicePreviewFragment, var dataList:ArrayList<InvoiceStockModelItem>,var invoiceDataModel:PostInvoiceDataModel) {
    val job = Job()
    val scope = CoroutineScope(job)
    private val commonUIUtility by lazy { CommonUIUtility(context) }
    val TAG = "POSTInvoiceDataAPI"

    init {
        postInvoiceData()
    }

    private fun postInvoiceData() {
        try {
            try {
                commonUIUtility.showProgress()
                val JO = JsonObject().apply {
                    addProperty("PCARegId", PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,""))
                    addProperty("CompanyCode", PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,""))
                    addProperty("Language", PrefUtil.getSystemLanguage())
                    addProperty("FromDate","")
                    addProperty("CurrentDate","")
                    addProperty("ToDate","")
                    addProperty("Action","Update")
                    add("InvoiceList",null)
                    add("InvoicePreviewListTable",null)
                    add("UpdatePCAStock", Gson().toJsonTree(dataList).asJsonArray)
                }

                val invoiceDataJO = Gson().toJsonTree(invoiceDataModel).asJsonObject
                Log.d(TAG, "postInvoiceData: UPDATE_STOCK_LIST_JSON : $JO")
                Log.d(TAG, "INVOICE_DATA_JSON : $invoiceDataJO")
                val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
                scope.launch(Dispatchers.IO)
                {
                    val api1 = async(Dispatchers.IO) {
                        APICall.postInvoiceStockList(JO)
                    }

                    val postInvoiceStockResponse = api1.await()
                    if (postInvoiceStockResponse.isSuccessful)
                    {
                        val responseJO = postInvoiceStockResponse.body()!!
                        if (responseJO.get("Success").asBoolean)
                        {

                            val api2 = async(Dispatchers.IO) {
                                APICall.postInvoiceData(invoiceDataJO)
                            }

                            val invoiceDataResponse =api2.await()
                            if (invoiceDataResponse.isSuccessful)
                            {
                                val invoiceResponseJO = invoiceDataResponse.body()!!
                                if (invoiceResponseJO.get("Success").asBoolean)
                                {
                                    val invoiceNo = invoiceResponseJO.get("invoiceNumber").asString
                                    withContext(Dispatchers.Main){
                                        fragment.downloadInvoice(invoiceNo)
                                        withContext(Dispatchers.IO){
                                            delay(2000)
                                        }
                                        commonUIUtility.dismissProgress()
                                        fragment.successRedirect()
                                    }
                                    job.cancel()

                                }
                            }else
                            {
                                withContext(Dispatchers.Main)
                                {
                                    Log.d(TAG, "postInvoiceData: ERROR_INVOICE_DATA_API")
                                    commonUIUtility.dismissProgress()
                                    commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                                }
                                job.cancel()
                            }
                        }
                    }else
                    {
                        withContext(Dispatchers.Main)
                        {
                            Log.d(TAG, "postInvoiceData: ERROR_INVOICE_STOCK_API")
                            commonUIUtility.dismissProgress()
                            commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                        }
                        job.cancel()
                    }

                }
            } catch (e: Exception) {
                job.cancel()
                commonUIUtility.dismissProgress()
                commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
                e.printStackTrace()
                Log.e(TAG, "postInvoiceStockList: ")
            }
        } catch (e: Exception) {
            job.cancel()
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
            e.printStackTrace()
            Log.e(TAG, "postInvoiceData: ${e.message}", )
        }
    }
}