package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer.BuyerAuctionFragment
import com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer.BuyerDashboardFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.BuyerAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

class FetchBuyerAuctionDetailAPI(var context: Context, var activity: Activity,var fragment: Fragment) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchBuyerAuctionDetailAPI"

    init {
        DatabaseManager.initializeInstance(context)
        getFetchBuyerAuctionDetail()
    }

    private fun getFetchBuyerAuctionDetail() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject()
            JO.addProperty("CommodityId",PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,""))
            JO.addProperty("BuyerRegId",PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,""))
            JO.addProperty("CompanyCode",PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,""))
            JO.addProperty("Date",DateUtility().getyyyyMMdd())
            JO.addProperty("Action","All")

            Log.d(TAG, "getFetchBuyerAuctionDetail: FETCH_BUYER_AUCTION_DETAIL_JSON : ${JO.toString()}")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)

            scope.launch(Dispatchers.IO){
                val result = APICall.getBuyerAuctionDetail(JO)
                if (result.isSuccessful)
                {
                    Log.d(TAG, "getFetchBuyerAuctionDetail: RESPONSE : ${result.body()}")
                    handleSuccess(result.body()!!)
                }else
                {
                    Log.e(TAG, "postPCAUpdatedData: ${result.errorBody()}")
                    withContext(Main){
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                    }
                    job.cancel()
                }
            }
        }catch (e:Exception)
        {
            commonUIUtility.dismissProgress()
            e.printStackTrace()
            Log.e(TAG, "getFetchBuyerAuctionDetail: ${e.message}")
        }
    }

    private suspend fun handleSuccess(resultBody:BuyerAuctionMasterModel ) {
        if (resultBody!!.IsActive.equals("False",true))
        {
            withContext(Main){
                commonUIUtility.dismissProgress()
                when(fragment){
                    is BuyerDashboardFragment->{
                        (fragment as BuyerDashboardFragment).redirectToLogin()
                    }
                    is BuyerAuctionFragment->{
                        (fragment as BuyerAuctionFragment).redirectToLogin()
                    }
                }
            }
            job.cancel()
        }else
        {
            withContext(Dispatchers.Main) {
                commonUIUtility.dismissProgress()
                when (fragment) {
                    is BuyerDashboardFragment -> {
                        (fragment as BuyerDashboardFragment).bindBuyerAllocatedData(resultBody!!)
                    }
                    is BuyerAuctionFragment -> {
                        if (resultBody.toString().contains("No Data Found")) {
                            // Handle "No Data Found" case
//                        commonUIUtility.dismissProgress()
                        } else {
                            (fragment as BuyerAuctionFragment).updateUIFromAPIData(resultBody!!)
                        }
                    }
                }
            }
            job.cancel()
        }
    }

}