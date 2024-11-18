package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAAuctionFragment
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAAuctionListFragment
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCADashboardFragment
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchIndPCAAuctionAPI(var context: Context, var fragment: Fragment) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchIndPCAAuctionAPI"

    init {
        getIndPCAAuctionDetail()
    }

    private fun getIndPCAAuctionDetail() {
        try {
            commonUIUtility.showProgress()
            val JO  = JsonObject().apply {
                addProperty("IndividualPCARegId",PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,""))
                addProperty("IndividualPCAId","")
                addProperty("CompanyCode",PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,""))
                addProperty("MobileNo",PrefUtil.getString(PrefUtil.KEY_MOBILE_NO,""))
                addProperty("Date",DateUtility().getCompletionDate())
//                addProperty("Date","28-10-2024")
                addProperty("CommodityId",PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,""))
                addProperty("Language",PrefUtil.getSystemLanguage())
            }

            Log.d(TAG, "getIndPCAAuctionDetail: IND_PCA_AUCTION_JSON : $JO")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO){
                val result = APICall.getIndPCAAuctionDetail(JO)
                if (result.isSuccessful)
                {
                    val model = result.body()!!
                    withContext(Dispatchers.Main) {
                        commonUIUtility.dismissProgress()
                        if (fragment is IndPCAAuctionFragment)
                        {
                            (fragment as IndPCAAuctionFragment).updateAuctionUI(model)
                        }
                        else if(fragment is IndPCADashboardFragment){
                            (fragment as IndPCADashboardFragment).updateAuctionUI(model)
                        }else if(fragment is IndPCAAuctionListFragment){
                            (fragment as IndPCAAuctionListFragment).bindAuctionList(model)
                        }
                    }
                    job.complete()
                }else
                {
                    withContext(Dispatchers.Main) {
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                    }
                    job.cancel()
                }
            }

        }catch (e:Exception)
        {
            job.cancel()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
            e.printStackTrace()
            Log.e(TAG, "getPCAAuction: ${e.message}")
            commonUIUtility.dismissProgress()
        }
    }
}