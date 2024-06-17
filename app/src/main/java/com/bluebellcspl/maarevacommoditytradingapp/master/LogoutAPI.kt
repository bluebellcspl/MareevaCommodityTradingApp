package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.LoginActivity
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.constants.Constants
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LogoutAPI(var context: Context,var activity: Activity,var fragment: Fragment) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "LogoutAPI"

    init {
        logoutUser()
    }

    private fun logoutUser() {
        try {
            commonUIUtility.showProgress()
            Log.d(TAG, "logoutUser: PROGRESS_START")
            val JO = JsonObject()
            JO.addProperty("RegId",PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString())
            JO.addProperty("RoleId",PrefUtil.getString(PrefUtil.KEY_ROLE_ID,"").toString())
            JO.addProperty("CompanyCode",PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,"").toString())

            Log.d(TAG, "logoutUser: JSON : $JO")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.Main){
                val result = APICall.Logout(JO)
                if (result.isSuccessful)
                {
                    if (result.body()!!.contains("Updated Successfully")){
                        withContext(Dispatchers.Main){
                            commonUIUtility.dismissProgress()
                            Log.d(TAG, "logoutUser: PROGRESS_END")
                            PrefUtil.deletePreference()
                            DatabaseManager.deleteData(Constants.TBL_TempNotificationMaster)
                            DatabaseManager.deleteData(Constants.TBL_NotificationMaster)
                        }
                        job.cancel()

                    }
                }else
                {
                    withContext(Dispatchers.Main){
                        commonUIUtility.dismissProgress()
                        Log.d(TAG, "logoutUser: PROGRESS_END")
//                        commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
                        Log.e(TAG, "logoutUser: ERROR : ${result.errorBody().toString()}", )
                    }
                    job.cancel()
                }
            }
        } catch (e: Exception) {
            job.cancel()
            commonUIUtility.dismissProgress()
            Log.d(TAG, "logoutUser: PROGRESS_END")
            Log.e(TAG, "logoutUser: ${e.message}")
            e.printStackTrace()
            commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
        }
    }
}