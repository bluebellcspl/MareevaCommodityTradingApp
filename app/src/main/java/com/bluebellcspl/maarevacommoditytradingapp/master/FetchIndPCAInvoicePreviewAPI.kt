package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAInvoicePreviewFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoicePreviewModel
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoicePreviewFetchDataModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchIndPCAInvoicePreviewAPI(
    var context: Context,
    var fragment: IndPCAInvoicePreviewFragment,
    var invoiceFetchDataModel: InvoicePreviewFetchDataModel
) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchIndPCAInvoicePreviewAPI"

    init {
        getInvoicePreviewData()
    }

    private fun getInvoicePreviewData() {
        try {
            commonUIUtility.showProgress()
            val JO = Gson().toJsonTree(invoiceFetchDataModel).asJsonObject
            Log.d(TAG, "getInvoicePreviewData: IND_PCA_INVOICE_PREVIEW_JSON : $JO")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO) {
                val result = APICall.getIndPCAInvoicePreviewData(JO)
                if (result.isSuccessful) {
                    val responseJO = result.body()!!
                    if (responseJO.has("Success")) {
                        if (!responseJO.get("Success").asBoolean) {
                            withContext(Dispatchers.Main) {
                                commonUIUtility.dismissProgress()
                                commonUIUtility.showToast(responseJO.get("Message").asString)
                            }
                            job.complete()
                        }
                    }else {
                        val invoiceDataJO =
                            Gson().fromJson(responseJO, IndPCAInvoicePreviewModel::class.java)
                        withContext(Dispatchers.Main) {
                            commonUIUtility.dismissProgress()
                            fragment.setInvoicePreviewData(invoiceDataJO)
                        }
                        job.complete()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                    }
                    job.cancel()
                }
            }
            job.complete()
        } catch (e: Exception) {
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
            e.printStackTrace()
            Log.e(TAG, "getInvoicePreviewData: ${e.message}")
        }
    }

}