package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.provider.ContactsContract.Data
import android.util.Log
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.constants.Constants
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchStateMasterAPI(var context: Context, var activity: Activity) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchStateMasterAPI"

    init {
        DatabaseManager.initializeInstance(context)
        getStateMaster()
    }

    private fun getStateMaster() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject()
            JO.addProperty("CompanyCode", "MAT189")
            JO.addProperty("Action", "All")
            Log.d(TAG, "getStateMaster: JSON : ${JO.toString()}")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)

            scope.launch(Dispatchers.IO) {
                val result = APICall.getStateMaster(JO)
                if (result.isSuccessful) {
                    val stateMasterModel = result.body()!!
                    val list = ContentValues()
                    DatabaseManager.deleteData(Constants.TBL_StateMaster)
                    for (model in stateMasterModel) {
                        list.put("StateId", model.StateId)
                        list.put("StateName", model.StateName)
                        list.put("CompanyCode", model.CompanyCode)
                        list.put("IsActive", model.IsActive)
                        list.put("CreateUser", model.CreateUser)
                        list.put("CreateDate", model.CreateDate)
                        list.put("UpdateDate", model.UpdateDate)
                        list.put("UpdateUser", model.UpdateUser)

                        DatabaseManager.commonInsert(list,Constants.TBL_StateMaster)
                    }
                    withContext(Main) {
                        commonUIUtility.dismissProgress()
                    }
                } else {
                    withContext(Main) {
                        commonUIUtility.dismissProgress()
                    }
                    Log.e(TAG, "getStateMaster: ${result.errorBody()}")
                }
            }
        } catch (e: Exception) {
            commonUIUtility.dismissProgress()
            e.printStackTrace()
            Log.e(TAG, "getStateMaster: ${e.message}")
        }
    }
}