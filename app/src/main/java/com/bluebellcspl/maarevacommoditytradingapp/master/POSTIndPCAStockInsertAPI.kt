package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAInvoiceFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.POSTIndPCAStockInsertModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class POSTIndPCAStockInsertAPI(var context: Context, var fragment: Fragment,var insertStockModel:POSTIndPCAStockInsertModel) {
    val job = Job()
    val scope = CoroutineScope(job)
    private val commonUIUtility by lazy { CommonUIUtility(context) }
    val TAG = "POSTIndPCAStockInsertAPI"

    init {
        sendIndPcaStockInsertData()
    }

    private fun sendIndPcaStockInsertData() {
        try {
            commonUIUtility.showProgress()
            val JO = Gson().toJsonTree(insertStockModel).asJsonObject
            Log.d(TAG, "sendIndPcaStockInsertData: IND_PCA_STOCK_INSERT_JSON : $JO")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO)
            {
                val result = APICall.postIndPCAStockDataIns(JO)
                if (result.isSuccessful) {
                    val responseJO = result.body()!!
                    if (responseJO.get("Success").asBoolean){
                        withContext(Dispatchers.Main){
                            commonUIUtility.dismissProgress()
                            commonUIUtility.showToast(context.getString(R.string.inserted_successfully_alert_msg))
                            if (fragment is IndPCAInvoiceFragment)
                            {
                                (fragment as IndPCAInvoiceFragment).resetUI()
                                (fragment as IndPCAInvoiceFragment).navigateOnSuccessfulInsert()
                            }
                        }
                        job.complete()
                    }
                }else{
                    withContext(Dispatchers.Main){
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                    }
                    job.cancel()
                    Log.e(TAG, "sendIndPcaStockInsertData: ${result.errorBody()}")
                }
            }
            job.complete()
        }catch (e:Exception)
        {
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
            e.printStackTrace()
            Log.e(TAG, "sendIndPcaStockInsertData: ${e.message}")
        }
    }
}