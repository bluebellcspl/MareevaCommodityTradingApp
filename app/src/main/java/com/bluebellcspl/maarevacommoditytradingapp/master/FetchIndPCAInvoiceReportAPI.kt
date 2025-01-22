package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAInvoiceReportFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoiceReportModel
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoiceReportModelJSON
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAStockBuyerWiseModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchIndPCAInvoiceReportAPI(var context: Context, var fragment: IndPCAInvoiceReportFragment, var invoiceReportJSON: IndPCAInvoiceReportModelJSON) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchIndPCAInvoiceReportAPI"

    init {
        getIndPCAInvoiceReport()
    }

    private fun getIndPCAInvoiceReport() {
        try {
            commonUIUtility.showProgress()
            val JO = Gson().toJsonTree(invoiceReportJSON).asJsonObject
            Log.d(TAG, "getIndPCAInvoiceReport: IND_PCA_INVOICE_REPORT_JSON :$JO")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO){
                val result = APICall.getIndPCAInvoiceReport(JO)
                if (result.isSuccessful){
                    val response = result.body()!!
                    if (response.isJsonObject){
                        val responseJO = response.asJsonObject
                        if (!responseJO.get("Success").asBoolean){
                            withContext(Dispatchers.Main){
                                commonUIUtility.dismissProgress()
                                commonUIUtility.showToast(context.getString(R.string.no_data_found))
                            }
                            job.cancel()
                        }
                    }else if (response.isJsonArray)
                    {
                        val responseJA = response.asJsonArray
                        val userListType = object : TypeToken<IndPCAInvoiceReportModel>() {}.type
                        var invoiceReportList: IndPCAInvoiceReportModel = Gson().fromJson(responseJA, userListType)
                        withContext(Dispatchers.Main){
                            commonUIUtility.dismissProgress()
                            fragment.populateData(invoiceReportList)
                        }
                    }
                }else
                {
                    Log.e(TAG, "getIndPCAInvoiceReport: ${result.errorBody()}", )
                    withContext(Dispatchers.Main){
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                    }
                }
            }
            job.complete()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "getIndPCAInvoiceReport: ${e.message}")
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
            commonUIUtility.dismissProgress()

        }
    }
}