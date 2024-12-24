package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAInvoicePreviewFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.FetchPerBoriRateIndPCADataModel
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoicePreviewFetchDataModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchIndPCATransportRateAPI(
    var context: Context,
    var fragment: IndPCAInvoicePreviewFragment,
    var fetchTransportRateModel: FetchPerBoriRateIndPCADataModel
) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchIndPCATransportRateAPI"

    init {
        getTransportRate()
    }

    private fun getTransportRate() {
        try {
            val JO = Gson().toJsonTree(fetchTransportRateModel).asJsonObject
            Log.d(TAG, "getTransportRate: IND_PCA_TRANSPORT_RATE_JSON : $JO")
            commonUIUtility.showProgress()
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO) {
                val result = APICall.getIndPCATransportRate(JO)
                if (result.isSuccessful) {
                    val responseJO = result.body()!!
                    if (responseJO.has("Success")){
                        if (!responseJO.get("Success").asBoolean){
                            withContext(Dispatchers.Main) {
                                commonUIUtility.dismissProgress()
                                commonUIUtility.showToast("Transport Rate Not Available")
                            }
                            job.cancel()
                        }
                    }else{
                        val responseDataJO = result.body()!!
                        withContext(Dispatchers.Main){
                            commonUIUtility.dismissProgress()
                            fragment.updatePerBoriRate(responseDataJO.get("PerBoriRate").asString)
                        }
                        job.complete()
                    }
                }
                else{
                    withContext(Dispatchers.Main){
                        Log.e(TAG, "getTransportRate: ${result.errorBody()}", )
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                    }
                }
            }
            job.complete()
        } catch (e: Exception) {
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
            e.printStackTrace()
            Log.e(TAG, "getTransportRate: ${e.message}")
        }
    }

}