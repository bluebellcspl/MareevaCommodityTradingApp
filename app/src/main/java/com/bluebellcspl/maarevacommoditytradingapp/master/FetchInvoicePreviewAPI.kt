package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.InvoicePreviewFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoicePreviewModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchInvoicePreviewAPI(var context: Context,var fragment: InvoicePreviewFragment) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchInvoicePreviewAPI"

    init {
        getInvoicePreview()
    }

    private fun getInvoicePreview() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject().apply {
                addProperty("PCARegId", PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,""))
                addProperty("CompanyCode", PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,""))
            }
            Log.d(TAG, "getInvoicePreview: INVOICE_PREVIEW_JSON : $JO")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO)
            {
                val result = APICall.getInvoicePreview(JO)
                if (result.isSuccessful)
                {
                    val resultJO = result.body()!!
                    if (resultJO.has("Success"))
                    {
                        if (!resultJO.get("Success").asBoolean)
                        {
                            withContext(Dispatchers.Main)
                            {
                                commonUIUtility.dismissProgress()
                                commonUIUtility.showToast(resultJO.get("Message").asString)
                            }
                            job.cancel()
                        }
                    }else
                    {
                        val invoicePreviewData = Gson().fromJson(resultJO, InvoicePreviewModel::class.java)
                        withContext(Dispatchers.Main){
                            commonUIUtility.dismissProgress()
                            fragment.populateData(invoicePreviewData)
                        }
                        job.cancel()
                    }
                }else
                {
                    withContext(Dispatchers.Main)
                    {
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
            Log.e(TAG, "getInvoicePreview: ${e.message}")
        }
    }

}