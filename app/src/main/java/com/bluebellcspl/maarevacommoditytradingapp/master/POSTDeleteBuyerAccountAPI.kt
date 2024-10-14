package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.fragment.ProfileFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.PostDeleteBuyerProfileModel
import com.bluebellcspl.maarevacommoditytradingapp.model.RegErrorReponse
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class POSTDeleteBuyerAccountAPI(var context: Context,var fragment: ProfileFragment,var model: PostDeleteBuyerProfileModel) {
    val job = Job()
    val scope = CoroutineScope(job)
    private val commonUIUtility by lazy { CommonUIUtility(context) }
    val TAG = "POSTDeleteBuyerAccountAPI"
    
    init {
        deleteBuyerAccount()
    }

    private fun deleteBuyerAccount() {
        try {
            commonUIUtility.showBackupProgress()
            val JO = Gson().toJsonTree(model).asJsonObject

            Log.d(TAG, "deleteBuyerAccount: DELETE_BUYER_ACCOUNT_JSON : $JO")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO){
                val result = APICall.deleteBuyerProfile(JO)
                if (result.isSuccessful)
                {
                    val responseJO = result.body()!!
                    if (responseJO.has("DownloadLink"))
                    {
                        withContext(Dispatchers.Main){
//                            commonUIUtility.dismissProgress()
                            fragment.downloadBackup(responseJO.get("DownloadLink").asString)
                        }
                        job.complete()
                    }else
                    {
                        withContext(Dispatchers.Main){
                            commonUIUtility.dismissProgress()
                            commonUIUtility.showToast(responseJO.get("Message").asString)
                            fragment.logout()
                        }
                        job.complete()
                    }
                }else
                {
                    val errorResponseJO = Gson().fromJson(result.errorBody()!!.string(), RegErrorReponse::class.java)
                    if (!errorResponseJO.Success){
                        withContext(Dispatchers.Main){
                            Log.e(TAG, "deleteBuyerAccount: ${errorResponseJO.Message}")
                            commonUIUtility.dismissProgress()
                            commonUIUtility.showToast(errorResponseJO.Message)
                        }
                        job.complete()
                    }
                }
            }
            job.complete()
        }catch (e:Exception)
        {
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
            e.printStackTrace()
            Log.e(TAG, "deleteBuyerAccount: ${e.message}")
        }
    }
}