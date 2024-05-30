package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.InvoiceStockFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceStockModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchInvoiceStockAPI(var context: Context,var fragment:InvoiceStockFragment) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchInoviceStockAPI"

    init {
        getInvoiceStock()
    }

    private fun getInvoiceStock() {
        try {
            val JO = JsonObject().apply {
                addProperty("CurrentDate",DateUtility().getyyyyMMdd())
                addProperty("PCARegId",PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,""))
                addProperty("CompanyCode",PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,""))
                addProperty("Language",PrefUtil.getSystemLanguage())
            }

            Log.d(TAG, "getInvoiceStock: JSON_INVOICE_STOCK_GET : $JO")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO)
            {
                val result = APICall.getInvoiceStockList(JO)
                if (result.isSuccessful)
                {
                    if (result.body()!!.isJsonArray)
                    {
                        val invoiceStockList = Gson().fromJson(result.body()!!,InvoiceStockModel::class.java)
                    }
                    else if (result.body()!!.isJsonObject)
                    {
                        val responseJO = result.body()!!.asJsonObject
                        if (!responseJO.get("Success").asBoolean)
                        {
                            withContext(Dispatchers.Main)
                            {
                                commonUIUtility.dismissProgress()
//                                commonUIUtility.showToast(responseJO.get())
                            }
                        }
                    }
                }else
                {
                    Log.e(TAG, "getInvoiceStock: ERROR_RESPONSE : ${result.errorBody()!!.string()}", )
                    withContext(Dispatchers.Main)
                    {
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                    }
                }
            }
            job.cancel()
        }catch (e:Exception)
        {
            job.cancel()
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
            e.printStackTrace()
            Log.e(TAG, "getInvoiceStock: ${e.message}")
        }
    }
}