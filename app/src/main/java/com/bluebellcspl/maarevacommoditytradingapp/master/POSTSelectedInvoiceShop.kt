package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.PCAInvoiceDetailFragment
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.PCAInvoiceFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.POSTSelectedInvoiceListModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class POSTSelectedInvoiceShop(
    var context: Context,
    var fragment: Fragment,
    var dataModel: POSTSelectedInvoiceListModel
) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "POSTSelectedInvoiceShop"

    init {
        sendSelectedData()
    }

    private fun sendSelectedData() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject().apply {
                addProperty("FromDate", dataModel.FromDate)
                addProperty("ToDate", dataModel.ToDate)
                addProperty("PCARegId", dataModel.PCARegId)
                addProperty("CompanyCode", dataModel.CompanyCode)
                addProperty("Language", dataModel.Language)
                add("InvoiceList", Gson().toJsonTree(dataModel.GCADataList).asJsonArray)
            }

            Log.d(TAG, "sendSelectedData: SELECTED_INVOICE_JSON : $JO")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO)
            {
                val result = APICall.postSelectedInvoiceData(JO)
                if (result.isSuccessful) {
                    val responseJO = result.body()!!
                    if (responseJO.get("Success").asBoolean) {
                        if (fragment is PCAInvoiceFragment)
                        {
                            withContext(Dispatchers.Main)
                            {
                                commonUIUtility.dismissProgress()
                                (fragment as PCAInvoiceFragment).redirectToInvoiceDetailFragment()
                            }
                            job.cancel()
                        }

                        if (fragment is PCAInvoiceDetailFragment)
                        {
                            withContext(Dispatchers.Main)
                            {
                                commonUIUtility.dismissProgress()
                                commonUIUtility.showToast("Data Added Successfully!")
                                (fragment as PCAInvoiceDetailFragment).refreshData()
                            }
                            job.cancel()
                        }
                    }
                } else {
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
            e.printStackTrace()
            Log.e(TAG, "sendSelectedData: ${e.message}")
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
        }
    }
}