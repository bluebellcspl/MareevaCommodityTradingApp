package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAAuctionReportFragment
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCADashboardFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAAuctionReportModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchIndPCAAuctionReport(var context: Context, var fragment: Fragment,var date:String) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchIndPCAAuctionReport"

    init {
        getAuctionReportData()
    }

    private fun getAuctionReportData() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject().apply {
                addProperty("IndividualPCARegId", PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,""))
                addProperty("IndividualPCAId","")
                addProperty("CompanyCode", PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,""))
                addProperty("MobileNo", PrefUtil.getString(PrefUtil.KEY_MOBILE_NO,""))
                addProperty("Date", date)
//                addProperty("Date","28-10-2024")
                addProperty("CommodityId", PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,""))
                addProperty("Language", PrefUtil.getSystemLanguage())
            }

            Log.d("??", "getAuctionReportData: IND_PCA_AUCTION_REPORT_JSON : $JO")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO){
                val result = APICall.getIndPCAAuctionReportData(JO)
                if (result.isSuccessful)
                {
                    val response = result.body()!!
                    if (response.has("Success")){
                        withContext(Dispatchers.Main){
                            commonUIUtility.dismissProgress()
//                            commonUIUtility.showToast(context.getString(R.string.no_data_found))
                        }
                        job.complete()
                    }else{
                        val responseModel = Gson().fromJson(response, IndPCAAuctionReportModel::class.java)
                        withContext(Dispatchers.Main){
                            if (fragment is IndPCAAuctionReportFragment){
                                commonUIUtility.dismissProgress()
                                (fragment as IndPCAAuctionReportFragment).bindReportData(responseModel)
                            }else if (fragment is IndPCADashboardFragment){
                                commonUIUtility.dismissProgress()
                                (fragment as IndPCADashboardFragment).bindReportData(responseModel)
                            }
                        }
                        job.complete()
                    }
                }else
                {
                    withContext(Dispatchers.Main){
                        commonUIUtility.dismissProgress()
                        Log.e(TAG, "getAuctionReportData: ${result.errorBody()}", )
//                        commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                    }
                    job.cancel()
                }
            }
            job.complete()
        }catch (e:Exception)
        {
            job.cancel()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
            e.printStackTrace()
            Log.e(TAG, "getAuctionReportData: ${e.message}")
            commonUIUtility.dismissProgress()
        }
    }
}