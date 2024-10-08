package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.fragment.ProfileFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.PostDeletePCAProfileModel
import com.bluebellcspl.maarevacommoditytradingapp.model.RegErrorReponse
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class POSTDeletePCAAccountAPI(var context: Context,var fragment:ProfileFragment,var model: PostDeletePCAProfileModel) {
    val job = Job()
    val scope = CoroutineScope(job)
    private val commonUIUtility by lazy { CommonUIUtility(context) }
    val TAG = "POSTDeletePCAAccountAPI"

    init {
        postDeletePCA()
    }

    private fun postDeletePCA() {
        try {
            commonUIUtility.showBackupProgress()
            val JO = Gson().toJsonTree(model).asJsonObject
            Log.d(TAG, "postDeletePCA: DELETE_PCA_PROFILE_JSON : $JO")
            val APICAll = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO){
                val result = APICAll.deletePCAProfile(JO)
                if (result.isSuccessful)
                {
                    val responseJO = result.body()!!
                    if (responseJO.has("DownloadLink"))
                    {
                        withContext(Dispatchers.Main){
//                            commonUIUtility.dismissProgress()
                            fragment.downloadBackup(responseJO.get("DownloadLink").asString)
                        }
                    }else
                    {
                        withContext(Dispatchers.Main){
                            commonUIUtility.dismissProgress()
                            commonUIUtility.showToast(responseJO.get("Message").asString)
                            fragment.logout()
                        }
                    }
                }
                else
                {
                    val errorResponseJO = Gson().fromJson(result.errorBody()!!.string(), RegErrorReponse::class.java)
                    if (!errorResponseJO.Success){
                        withContext(Dispatchers.Main){
                            Log.e(TAG, "postDeletePCA: ${errorResponseJO.Message}")
                            commonUIUtility.dismissProgress()
                            commonUIUtility.showToast(errorResponseJO.Message)
                        }
                    }
                }
            }
        }catch (e:Exception)
        {
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
            e.printStackTrace()
            Log.e(TAG, "postDeletePCA: ${e.message}")
        }
    }
}