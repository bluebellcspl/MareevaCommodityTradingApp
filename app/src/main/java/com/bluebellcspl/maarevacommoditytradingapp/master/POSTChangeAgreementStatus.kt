package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.Context
import android.util.Log
import com.bluebellcspl.maarevacommoditytradingapp.LoginActivity
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class POSTChangeAgreementStatus(var context: Context,var activity:Activity,var status:String) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "POSTChangeAgreementStatus"

    init {
        setChangeAgreementStatus()
    }

    private fun setChangeAgreementStatus() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject()
            JO.addProperty("MobileNo",PrefUtil.getString(PrefUtil.KEY_MOBILE_NO,"").toString())
            JO.addProperty("RegId",PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString())
            JO.addProperty("RoleId",PrefUtil.getString(PrefUtil.KEY_ROLE_ID,"").toString())
            JO.addProperty("CompanyCode",PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,"").toString())
            JO.addProperty("IsAgreementRead",status)

            Log.d(TAG, "setChangeAgreementStatus: JSON : $JO")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO){
                val result = APICall.POSTChangeAgreementStatus(JO)
                if (result.isSuccessful)
                {
                    if (result.body()!!.contains("Successfully")){
                        val loginBinding = (activity as LoginActivity).binding
                        if (loginBinding.mchbRememberLogin.isChecked)
                        {
                            PrefUtil.setBoolean(PrefUtil.KEY_LOGGEDIN,true)
                        }
                        withContext(Dispatchers.Main){
                            commonUIUtility.dismissProgress()
                            (activity as LoginActivity).redirectToHome()
                        }
                    }else
                    {
                        withContext(Dispatchers.Main){
                            commonUIUtility.dismissProgress()
                            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
                        }
                    }

                }else
                {
                    commonUIUtility.dismissProgress()
                    Log.e(TAG, "setChangeAgreementStatus: ${result.errorBody()}", )
                }
            }
        } catch (e: Exception) {
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
            e.printStackTrace()
            Log.e(TAG, "setChangeAgreementStatus: ${e.message}")
        }
    }
}