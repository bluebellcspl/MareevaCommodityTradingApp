package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAProfileFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.PostDeleteIndPCAProfileModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CheckIndPCACurrentAuctionAPI (var context: Context, var fragment: IndPCAProfileFragment,var model:PostDeleteIndPCAProfileModel) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "CheckIndPCACurrentAuctionAPI"

    init {
        checkCurrentIndPCAuction()
    }

    private fun checkCurrentIndPCAuction() {
        try {
            commonUIUtility.showProgress()
            val JO = Gson().toJsonTree(model).asJsonObject
            Log.d(TAG, "checkCurrentIndPCAuction: CHECK_CURRENT_IND_PCA_AUCTION_JSON : $JO")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO){
                val result = APICall.checkCurrentIndPCAAuction(JO)
                if (result.isSuccessful)
                {
                    val response = result.body()!!
                    if (response.get("Success").asBoolean){
                        var auctionStatus = response.get("Status").asBoolean
                        withContext(Dispatchers.Main){
                            commonUIUtility.dismissProgress()
                            fragment.pcaAuctionLiveCheck(auctionStatus)
                        }
                        job.complete()
                    }else{
                        withContext(Dispatchers.Main){
                            commonUIUtility.dismissProgress()
                        }
                        job.complete()
                    }
                }else
                {
                    withContext(Dispatchers.Main){
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                    }
                    job.cancel()
                }
            }
            job.complete()

        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG, "checkCurrentIndPCAuction: ${e.message}")
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
        }
    }
}