package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAInvoiceStockFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoiceStockModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchIndPCAInvoiceStockAPI(var context: Context, var fragment: Fragment,var model: IndPCAInvoiceStockFragment.IndPCAInvoiceStockFetchAPIModel) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchIndPCAInvoiceStockAPI"

    init {
        getIndPCAInvoiceStockAPI()
    }

    private fun getIndPCAInvoiceStockAPI() {
        try {
            commonUIUtility.showProgress()
            val JO = Gson().toJsonTree(model).asJsonObject
            Log.d(TAG, "getIndPCAInvoiceStockAPI: IND_PCA_INVOICE_STOCK_JSON : $JO")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO) {
                val result = APICall.getIndPCAInvoiceStockAPI(JO)
                if (result.isSuccessful) {
                    val mainResponseJO = result.body()!!
                    if (mainResponseJO.isJsonObject)
                    {
                        val responseJO = mainResponseJO.asJsonObject
                        if (!responseJO.get("Success").asBoolean)
                        {
                            withContext(Dispatchers.Main){
                                commonUIUtility.dismissProgress()
                                commonUIUtility.showToast(responseJO.get("Message").asString)
                                if (fragment is IndPCAInvoiceStockFragment){
                                    (fragment as IndPCAInvoiceStockFragment).onNoDataFound()
                                }
                            }
                            job.complete()
                        }
                    }else if (mainResponseJO.isJsonArray){
                        val responseJA = mainResponseJO.asJsonArray
                        val userListType = object : TypeToken<IndPCAInvoiceStockModel>() {}.type
                        var indPCAStockFetchList:IndPCAInvoiceStockModel = Gson().fromJson(responseJA, userListType)
//                        withContext(Dispatchers.Main){
//                            commonUIUtility.dismissProgress()
//                            if (fragment is IndPCAInvoiceStockFragment){
//                                (fragment as IndPCAInvoiceStockFragment).bindAPIStockData(indPCAStockFetchList)
//                            }
//                        }
                        job.complete()
                    }
                }else
                {
                    withContext(Dispatchers.Main){
                        Log.e(TAG, "getIndPCAInvoiceStockAPI: ERROR_JSON : ${result.errorBody()}", )
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                    }
                    job.cancel()
                }
            }
            job.complete()
        }catch (e:Exception)
        {
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
            e.printStackTrace()
            Log.e(TAG, "getIndPCAInvoiceStockAPI: ${e.message}")
        }
    }
}