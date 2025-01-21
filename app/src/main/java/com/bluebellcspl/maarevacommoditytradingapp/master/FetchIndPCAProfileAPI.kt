package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAProfileFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAProfileModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchIndPCAProfileAPI (
    var context: Context,
    var fragment: Fragment
) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchIndPCAProfileAPI"

    init {
        getIndPCAProfile()
    }

    private fun getIndPCAProfile() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject().apply {
                addProperty("CompanyCode",PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,""))
                addProperty("APMCId",PrefUtil.getString(PrefUtil.KEY_APMC_ID,""))
                addProperty("Action","All")
                addProperty("BuyerId","")
                addProperty("PCAId",PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,""))
            }

            Log.d(TAG, "getIndPCAProfile: IND_PROFILE_JSON : $JO")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO){
                val result = APICall.getIndPCAProfile(JO)
                if (result.isSuccessful)
                {
                    val responseJO = Gson().fromJson(result.body()!!,IndPCAProfileModel::class.java)
                    withContext(Dispatchers.Main){
                        if (fragment is IndPCAProfileFragment){
                            commonUIUtility.dismissProgress()
                            (fragment as IndPCAProfileFragment).getProfileData(responseJO)
                        }
                    }
                    job.complete()
                }else{
                    withContext(Dispatchers.Main){
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.no_data_found))
                    }
                    job.cancel()
                }
            }
            job.complete()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "getIndPCAProfile: ${e.message}")
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
        }
    }
}