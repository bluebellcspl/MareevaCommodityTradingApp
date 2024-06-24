package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.InvoiceReportFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceReportJSONParamModel
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceReportModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchInvoiceReportAPI(var context: Context, var fragment: InvoiceReportFragment, var jsonModel: InvoiceReportJSONParamModel) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchInvoiceReportAPI"

    init {
        getInvoiceReportData()
    }

    private fun getInvoiceReportData() {
        try {
            commonUIUtility.showProgress()
            val invoiceReportJSONParamModel = Gson().toJsonTree(jsonModel).asJsonObject
            Log.d(TAG, "getInvoiceReportData: INVOICE_REPORT_JSON : $invoiceReportJSONParamModel")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO){
                val result = APICall.getInvoiceReport(invoiceReportJSONParamModel)
                if (result.isSuccessful)
                {
                    val apiResponse = result.body()!!
                    if (apiResponse.isJsonObject){
                        val responseJO = apiResponse.asJsonObject
                        if (responseJO.has("Success"))
                        {
                            if (!responseJO.get("Success").asBoolean)
                            {
                                withContext(Dispatchers.Main)
                                {
                                    commonUIUtility.dismissProgress()
                                    commonUIUtility.showToast(responseJO.get("Message").asString)
                                    fragment.populateData(arrayListOf())
                                }
                                job.cancel()
                            }
                        }
                    }
                    else if(apiResponse.isJsonArray){
                        val responseJA = apiResponse.asJsonArray
                        val invoiceReportData = Gson().fromJson(responseJA, InvoiceReportModel::class.java)

                        withContext(Dispatchers.Main)
                        {
                            commonUIUtility.dismissProgress()
                            fragment.populateData(invoiceReportData)
                        }
                        job.cancel()
                    }
                }
            }
        }catch (e:Exception)
        {
            job.cancel()
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
            e.printStackTrace()
            Log.e(TAG, "getInvoiceReportData: ${e.message}", )
        }
    }
}