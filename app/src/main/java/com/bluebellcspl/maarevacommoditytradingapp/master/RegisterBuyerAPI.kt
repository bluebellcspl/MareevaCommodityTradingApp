package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.Context
import android.util.Log
import com.bluebellcspl.maarevacommoditytradingapp.RegisterActivity
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.model.RegisterBuyerModel
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

class RegisterBuyerAPI(var context: Context, var activity: Activity, var model: RegisterBuyerModel) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "RegisterBuyerAPI"

    init {
        DatabaseManager.initializeInstance(context)
        registerBuyer()
    }

    private fun registerBuyer() {
        try {
            commonUIUtility.showProgress()
            val JO =JsonObject()
            JO.addProperty("Name",model.Name)
            JO.addProperty("Location",model.Location)
            JO.addProperty("MobileNo",model.MobileNo)
            JO.addProperty("CreateDate",model.CreateDate)
            JO.addProperty("CreateUser",model.CreateUser)

            Log.d(TAG, "registerBuyer: JSON : ${JO.toString()}")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO){
                val result =  APICall.registerBuyer(JO)
                if (result.isSuccessful)
                {
                    val resultStr = result.body()!!

                    if (resultStr.contains("Register Successfully"))
                    {
                        withContext(Main){
                            commonUIUtility.dismissProgress()
                            commonUIUtility.showToast(context.getString(R.string.buyer_registered_successfully_alert_msg))
                            if (activity is RegisterActivity){
                                (activity as RegisterActivity).redirectToLogin()
                            }
                        }
                    }
                    else if (resultStr.contains("Already Exist"))
                    {
                        withContext(Main){
                            commonUIUtility.dismissProgress()
                            commonUIUtility.showToast(context.getString(R.string.this_mobile_is_already_registered_alert_msg))
                        }
                    }
                }else
                {
                    withContext(Main){
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                    }
                    Log.e(TAG, "registerBuyer: ${result.errorBody().toString()}", )
                }
            }
        }catch (e:Exception){
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
            Log.e(TAG, "registerBuyer: ${e.message}")
            e.printStackTrace()
        }
    }

}