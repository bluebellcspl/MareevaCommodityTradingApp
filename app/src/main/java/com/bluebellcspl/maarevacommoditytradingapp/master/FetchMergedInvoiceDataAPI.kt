package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.PCAInvoiceDetailFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceEntryMergedModel
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceEntryMergedModelItem
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchMergedInvoiceDataAPI(var context: Context,var fragment:PCAInvoiceDetailFragment,var startDate:String,var endDate:String) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchMergedInvoiceDataAPI"

    init {
        getMergedInvoiceData()
    }

    private fun getMergedInvoiceData() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject().apply {
                addProperty("FromDate", startDate)
                addProperty("ToDate", endDate)
                addProperty("PCARegId", PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString())
                addProperty("CompanyCode", PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE, "").toString())
                addProperty("Language", PrefUtil.getSystemLanguage())
            }

            Log.d(TAG, "getMergedInvoiceData: FETCH_MERGED_INVOICE_JSON : $JO")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO)
            {
                val result = APICall.getMergedInvoiceData(JO)
                if (result.isSuccessful)
                {
                    if (result.body()!!.isJsonArray)
                    {
                        val mergedList = Gson().fromJson(result.body()!!, InvoiceEntryMergedModel::class.java)
                        withContext(Dispatchers.Main)
                        {
                            commonUIUtility.dismissProgress()
                            fragment.bindRcView(mergedList)
                        }
                    }
                    if (result.body()!!.isJsonObject)
                    {
                        val responseJO = result.body()!!.asJsonObject
                        if (responseJO.get("Success").asBoolean && responseJO.get("Message").asString.contains("No",true))
                        {
                            val mergedList = arrayListOf<InvoiceEntryMergedModelItem>()
                            withContext(Dispatchers.Main)
                            {
                                commonUIUtility.dismissProgress()
                                fragment.bindRcView(mergedList)
                            }
                        }
                    }

                }else
                {
                    withContext(Dispatchers.Main)
                    {
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                    }
                }
            }
        }catch (e:Exception)
        {
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
            e.printStackTrace()
            Log.e(TAG, "getMergedInvoiceData: ${e.message}", )
        }
    }

}