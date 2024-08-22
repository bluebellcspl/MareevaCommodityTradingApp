package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer.BuyerAuctionFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.BuyerAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.POSTBuyerAuctionData
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class POSTBuyerAuctionDataAPI(var context: Context,var activity: Activity,var fragment: Fragment,var model: POSTBuyerAuctionData) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "POSTBuyerAuctionDataAPI"

    init {
        postAuctionData()
    }

    private fun postAuctionData() {
        try {

            commonUIUtility.showProgress()

            val postBuyerAuctionData = Gson().toJsonTree(model).asJsonObject
            Log.d(TAG, "postAuctionData: BUYER_AUCTION_JSON : $postBuyerAuctionData")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO)
            {
                val result = APICall.POSTBuyerAuctionDetail(postBuyerAuctionData)
                if (result.isSuccessful)
                {
                    val responseJO = result.body()!!
                    if (responseJO.get("Success").asBoolean)
                    {
                        withContext(Dispatchers.Main){
                            commonUIUtility.dismissProgress()
                            commonUIUtility.showToast(context.getString(R.string.auction_inserted_successfully))
                            if (fragment is BuyerAuctionFragment)
                            {
                                (fragment as BuyerAuctionFragment).redirectToBuyerDashboard()
                            }
                        }
                        job.cancel()
                    }else
                    {
                        withContext(Dispatchers.Main){
                            commonUIUtility.dismissProgress()
                            commonUIUtility.showToast(responseJO.get("Message").asString)
                        }
                        job.cancel()
                    }
                }else
                {
                    Log.e(TAG, "postPCAUpdatedData: ${result.errorBody()}")
                    withContext(Dispatchers.Main){
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                    }
                    job.cancel()
                }
            }

        }catch (e:Exception)
        {
            job.cancel()
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
            e.printStackTrace()
            Log.e(TAG, "postAuctionData: ${e.message}")
        }
    }
}