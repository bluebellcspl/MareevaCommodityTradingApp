package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.Context
import android.util.Log
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.model.LoginForAdminModel
import com.bluebellcspl.maarevacommoditytradingapp.model.LoginWithOTPModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.example.maarevacommoditytradingapp.R
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

class LoginForAdminAPI(
    var context: Context,
    var activity: Activity,
    var model: LoginForAdminModel
) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "LoginWithOTPAPI"

    init {
        DatabaseManager.initializeInstance(context)
        getLoginForAdmin()
    }

    private fun getLoginForAdmin() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject()
            JO.addProperty("MobileNo", model.MobileNo)
            JO.addProperty("UserType", model.Typeofuser)
            JO.addProperty("StateId", model.StateId)
            JO.addProperty("DistrictId", model.DistrictId)
            JO.addProperty("APMCId", model.APMCId)
            JO.addProperty("CommodityId", model.CommodityId)
            JO.addProperty("UserName", model.UserName)
            JO.addProperty("UserPassword", model.UserPassword)
            JO.addProperty("OTP", model.OTP)
            JO.addProperty("RoleName", model.RoleName)
            JO.addProperty("CompanyCode", model.CompanyCode)
            JO.addProperty("Action", model.Action)

            Log.d(TAG, "getLoginForAdmin: JSON : ${JO.toString()}")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
        } catch (e: Exception) {
            commonUIUtility.dismissProgress()
            e.printStackTrace()
            Log.e(TAG, "getLoginOTP: ${e.message}")
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
        }
    }

}