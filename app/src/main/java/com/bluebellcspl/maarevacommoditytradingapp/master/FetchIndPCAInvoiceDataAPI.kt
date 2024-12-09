package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAInvoiceFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoiceDataModel
import com.bluebellcspl.maarevacommoditytradingapp.model.RegErrorReponse
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchIndPCAInvoiceDataAPI (var context: Context, var fragment: IndPCAInvoiceFragment, var startDate:String, var endDate:String) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchIndPCAInvoiceDataAPI"

    init {
        getInvoiceData()
    }

    private fun getInvoiceData() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject().apply {
                addProperty("CompanyCode", PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE, ""))
                addProperty("FromDate", startDate)
                addProperty("ToDate", endDate)
                addProperty("IndividualPCARegId", PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, ""))
                addProperty("Language", PrefUtil.getSystemLanguage())
            }

            Log.d(TAG, "getInvoiceData: FETCH_IND_PCA_INVOICE_DATA : $JO")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO) {
                val result = APICall.getIndPCAInvoiceData(JO)
                if (result.isSuccessful){
                    val invoiceJO = result.body()!!
                    if (invoiceJO.has("Success")){
                        withContext(Dispatchers.Main){
                            commonUIUtility.dismissProgress()
                            commonUIUtility.showToast("No Data Found!")
                        }
                    }else{
                        val invoiceData = Gson().fromJson(invoiceJO, IndPCAInvoiceDataModel::class.java)
                        withContext(Dispatchers.Main){
                            commonUIUtility.dismissProgress()
                            fragment.bindFilterForRecyclerview(invoiceData)
                        }
                    }
                }else
                {
//                    withContext(Dispatchers.Main) {
//                        commonUIUtility.dismissProgress()
//                        commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
//                    }
                    Log.e(TAG, "getInvoiceData: ${result.errorBody()?.string()}", )
                    val errorResult = Gson().fromJson(result.errorBody()!!.string(),
                        RegErrorReponse::class.java)
                    errorResult?.let {
                        if (!errorResult.Success) {
                            withContext(Dispatchers.Main) {
                                commonUIUtility.dismissProgress()
                                commonUIUtility.showToast(errorResult.Message)
                            }
                            job.cancel()
                        }else
                        {
                            withContext(Dispatchers.Main) {
                                commonUIUtility.dismissProgress()
                                commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
                            }
                            job.cancel()
                        }
                    }
                    Log.e(TAG, "getLoginForAdmin: ${result.errorBody().toString()}")
                }
            }
        }catch (e:Exception)
        {
            job.cancel()
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
            e.printStackTrace()
        }
    }
}