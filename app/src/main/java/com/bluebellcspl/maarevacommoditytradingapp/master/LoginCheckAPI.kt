package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.Context
import android.util.Log
import com.bluebellcspl.maarevacommoditytradingapp.LoginActivity
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.model.LoginForAdminModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.log

class LoginCheckAPI(
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
            JO.addProperty("Typeofuser", model.Typeofuser)
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

            scope.launch(Dispatchers.IO){
                val result = APICall.getLoginCheck(JO)
                if (result.isSuccessful)
                {
                    val resultJO = result.body()!!
                    if (resultJO.has("Succcess"))
                    {
                        withContext(Main){
                            commonUIUtility.dismissProgress()
                            commonUIUtility.showToast(context.getString(R.string.invalid_credentials_or_otp_alert_msg))
                        }
                    }else{
                        PrefUtil.setString(PrefUtil.KEY_EMP_NAME,resultJO.get("Name").asString)
                        PrefUtil.setString(PrefUtil.KEY_MOBILE_NO,resultJO.get("MobileNo").asString)
                        PrefUtil.setString(PrefUtil.KEY_ROLE_ID,resultJO.get("RoleId").asString)
                        PrefUtil.setString(PrefUtil.KEY_ROLE_NAME,resultJO.get("RoleName").asString)
                        PrefUtil.setString(PrefUtil.KEY_STATE_ID,resultJO.get("StateId").asString)
                        PrefUtil.setString(PrefUtil.KEY_STATE_NAME,resultJO.get("StateName").asString)
                        PrefUtil.setString(PrefUtil.KEY_DISTRICT_ID,resultJO.get("DistrictId").asString)
                        PrefUtil.setString(PrefUtil.KEY_DISTRICT_NAME,resultJO.get("DistrictName").asString)
                        PrefUtil.setString(PrefUtil.KEY_APMC_ID,resultJO.get("APMCId").asString)
                        PrefUtil.setString(PrefUtil.KEY_APMC_NAME,resultJO.get("APMCName").asString)
                        PrefUtil.setString(PrefUtil.KEY_COMMODITY_ID,resultJO.get("CommodityId").asString)
                        PrefUtil.setString(PrefUtil.KEY_COMMODITY_NAME,resultJO.get("CommodityName").asString)
                        PrefUtil.setString(PrefUtil.KEY_COMPANY_CODE,resultJO.get("CompanyCode").asString)
                        PrefUtil.setString(PrefUtil.KEY_REGISTER_ID,resultJO.get("RegisterId").asString)
                        PrefUtil.setString(PrefUtil.KEY_CREATE_USER,resultJO.get("CreateUser").asString)

                        if (activity is LoginActivity)
                        {
                            val loginBinding = (activity as LoginActivity).binding
                            if (loginBinding.mchbRememberLogin.isChecked)
                                {
                                PrefUtil.setBoolean(PrefUtil.KEY_LOGGEDIN,true)
                            }

                            withContext(Main){
                                commonUIUtility.dismissProgress()
                                (activity as LoginActivity).redirectToHome()
                            }

                        }
                    }
                }else
                {
                    withContext(Main){
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.invalid_credentials_or_otp_alert_msg))
                    }
                    Log.e(TAG, "getLoginForAdmin: ${result.errorBody().toString()}", )
                }
            }
        } catch (e: Exception) {
            commonUIUtility.dismissProgress()
            e.printStackTrace()
            Log.e(TAG, "getLoginOTP: ${e.message}")
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
        }
    }

}