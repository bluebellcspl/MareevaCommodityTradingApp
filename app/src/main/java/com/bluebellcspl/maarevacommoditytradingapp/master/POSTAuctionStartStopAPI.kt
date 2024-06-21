package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer.LiveAuctionFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.POSTAuctionStartStopAPIModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class POSTAuctionStartStopAPI(
    var context: Context,
    var activity: Activity,
    var fragment: Fragment,
    var model: POSTAuctionStartStopAPIModel
) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "POSTAuctionStartStopAPI"

    init {
        postAuctionStartStop()
    }

    private fun postAuctionStartStop() {
        try {
            commonUIUtility.showProgress()

            val postAuctionStartStopJO = Gson().toJsonTree(model).asJsonObject
            Log.d(TAG, "postAuctionStartStop: LIVE_AUCTION_PLAY_PAUSE_JSON : $postAuctionStartStopJO")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO)
            {
                val result = APICall.postAuctionStartStop(postAuctionStartStopJO)
                if (result.isSuccessful) {
                    val responseStr = result.body()!!
                    if (responseStr.contains("PCA Auction Status Updated Successfully", true)) {
                        withContext(Dispatchers.Main){
                            commonUIUtility.dismissProgress()
                            commonUIUtility.showToast("Auction Status Updated Successfully!")
                        }
                        job.cancel()

                    }
                } else {
                    Log.e(TAG, "postAuctionStartStop: ${result.errorBody()}")
                    withContext(Dispatchers.Main){
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast("Auction Status NOT Updated!")
                    }
                    job.cancel()
                }
            }
        } catch (e: Exception) {
            job.cancel()
            Log.e(TAG, "postAuctionStartStop: ${e.message}")
            e.printStackTrace()
            commonUIUtility.showToast("Please Try Again Later!")
            commonUIUtility.dismissProgress()
        }
    }
}