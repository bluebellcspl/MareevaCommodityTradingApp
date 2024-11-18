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
import com.bluebellcspl.maarevacommoditytradingapp.model.RegErrorReponse
import com.google.gson.Gson
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

            val loginCheckJO = Gson().toJsonTree(model).asJsonObject

            Log.d(TAG, "getLoginForAdmin: JSON : $loginCheckJO")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)

            scope.launch(Dispatchers.IO){
                val result = APICall.getLoginCheck(loginCheckJO)
                if (result.isSuccessful)
                {
                    val resultJO = result.body()!!
                    if (resultJO.has("Success"))
                    {
                        withContext(Main){
                            commonUIUtility.dismissProgress()
                            commonUIUtility.showToast(resultJO.get("Message").asString)
                        }
                    }else{
                        val isAgreementRead = resultJO.get("IsAgreementRead").asString
                        PrefUtil.setString(PrefUtil.KEY_REGISTER_ID,resultJO.get("RegisterId").asString)
                        PrefUtil.setString(PrefUtil.KEY_NAME,resultJO.get("Name").asString)
                        PrefUtil.setString(PrefUtil.KEY_LOCATION,resultJO.get("Location").asString)
                        PrefUtil.setString(PrefUtil.KEY_MOBILE_NO,resultJO.get("MobileNo").asString)
                        PrefUtil.setString(PrefUtil.KEY_BUYER_ID,resultJO.get("BuyerId").asString)
                        PrefUtil.setString(PrefUtil.KEY_ROLE_ID,resultJO.get("RoleId").asString)
                        PrefUtil.setString(PrefUtil.KEY_ROLE_NAME,resultJO.get("RoleName").asString)
                        PrefUtil.setString(PrefUtil.KEY_APMC_ID,resultJO.get("APMCId").asString)
                        PrefUtil.setString(PrefUtil.KEY_APMC_NAME,resultJO.get("APMCName").asString)
                        PrefUtil.setString(PrefUtil.KEY_COMMODITY_ID,resultJO.get("CommodityId").asString)
                        PrefUtil.setString(PrefUtil.KEY_COMMODITY_NAME,resultJO.get("CommodityName").asString)
                        PrefUtil.setString(PrefUtil.KEY_COMMODITY_NAME_GUJ,resultJO.get("GujaratiCommodityName").asString)
                        PrefUtil.setString(PrefUtil.KEY_USER_NAME,resultJO.get("UserName").asString)
                        PrefUtil.setString(PrefUtil.KEY_USER_PASSWORD,resultJO.get("UserPassword").asString)
                        PrefUtil.setString(PrefUtil.KEY_IsActive,resultJO.get("IsActive").asString)
                        PrefUtil.setString(PrefUtil.KEY_COMPANY_CODE,resultJO.get("CompanyCode").asString)
                        PrefUtil.setString(PrefUtil.KEY_IsUser,resultJO.get("IsUser").asString)

                        if (activity is LoginActivity)
                        {

                            val loginBinding = (activity as LoginActivity).binding
                            if (isAgreementRead.equals("true",true))
                            {
//                                if (loginBinding.mchbRememberLogin.isChecked)
//                                {
//
//                                }
                                PrefUtil.setBoolean(PrefUtil.KEY_LOGGEDIN,true)
                                withContext(Main){
                                    commonUIUtility.dismissProgress()
                                    (activity as LoginActivity).redirectToHome()
                                }
                            }else
                            {
                                withContext(Dispatchers.Main){
                                    commonUIUtility.dismissProgress()
                                    (activity as LoginActivity).showPCATermsAndConditionDialog()
                                }
                            }

                        }
                    }
                }else
                {
                    val errorResult = Gson().fromJson(result.errorBody()!!.string(),RegErrorReponse::class.java)
                    errorResult?.let {
                        if (!errorResult.Success) {
                            val errorMessage = errorResult.Message
                            withContext(Dispatchers.Main) {
                                commonUIUtility.dismissProgress()
                                commonUIUtility.showToast(errorMessage)
                            }
                            job.cancel()
                        }else
                        {
                            withContext(Dispatchers.Main) {
                                commonUIUtility.dismissProgress()
                                commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
                            }
                            job.cancel()
                        }
                    }
                    Log.e(TAG, "getLoginForAdmin: ${result.errorBody().toString()}", )
                }
            }
        } catch (e: Exception) {
            job.cancel()
            commonUIUtility.dismissProgress()
            e.printStackTrace()
            Log.e(TAG, "getLoginOTP: ${e.message}")
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
        }
    }

}