package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAProfileFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.PostDeleteIndPCAProfileModel
import com.bluebellcspl.maarevacommoditytradingapp.model.RegErrorReponse
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class POSTDeleteIndPCAProfile(
    var context: Context,
    var fragment: IndPCAProfileFragment,
    var model: PostDeleteIndPCAProfileModel
) {
    val job = Job()
    val scope = CoroutineScope(job)
    private val commonUIUtility by lazy { CommonUIUtility(context) }
    val TAG = "POSTDeleteIndPCAProfile"

    init {
        deleteIndPCAProfile()
    }

    private fun deleteIndPCAProfile() {
        try {
            model.Action = "DeleteIndividualPCA"
            val JO = Gson().toJsonTree(model).asJsonObject
            Log.d(TAG, "deleteIndPCAProfile: DELETE_IND_PCA_PROFILE_JSON : $JO")
            commonUIUtility.showBackupProgress()
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO) {
                val result = APICall.deleteIndPCAProfile(JO)
                if (result.isSuccessful) {
                    val deleteIndPCAResponseJO = result.body()!!
                    if (deleteIndPCAResponseJO.get("Success").asBoolean) {
                        withContext(Dispatchers.Main) {
//                                        commonUIUtility.dismissProgress()
                            fragment.downloadBackup(deleteIndPCAResponseJO.get("DownloadLink").asString)
                        }
                        job.complete()
                    } else {
                        val errorResponseJO = Gson().fromJson(
                            result.errorBody()!!.string(),
                            RegErrorReponse::class.java
                        )
                        if (!errorResponseJO.Success) {
                            withContext(Dispatchers.Main) {
                                Log.e(TAG,"deleteIndPCAProfile: ${errorResponseJO.Message}")
                                commonUIUtility.dismissProgress()
                                commonUIUtility.showToast(errorResponseJO.Message)
                            }
                            job.complete()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                    }
                    job.cancel()
                }
            }
            job.complete()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "deleteIndPCAProfile: ${e.message}")
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
        }
    }
}