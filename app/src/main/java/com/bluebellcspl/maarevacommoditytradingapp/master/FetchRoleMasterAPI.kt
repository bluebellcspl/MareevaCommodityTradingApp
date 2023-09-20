package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.bluebellcspl.maarevacommoditytradingapp.LoginActivity
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

class FetchRoleMasterAPI(var context:Context, var activity: Activity) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchRoleMasterAPI"

    init {
        DatabaseManager.initializeInstance(context)
        getRoleMaster()
    }

    private fun getRoleMaster() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject()
            JO.addProperty("CompanyCode","MAT189")
            JO.addProperty("Action","All")
            Log.d(TAG, "getRoleMaster: JSON : $JO")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO){
                val result = APICall.getRoleMaster(JO)

                if (result.isSuccessful)
                {
                    val roleMasterModel = result.body()!!
                    val list = ContentValues()
                    DatabaseManager.deleteData(Constants.TBL_RoleMaster)
                    for (model in roleMasterModel){
                        list.put("RoleId",model.RoleId)
                        list.put("RoleName",model.RoleName)
                        list.put("IsActive",model.IsActive)
                        list.put("CompanyCode",model.CompanyCode)
                        list.put("IsUser",model.IsUser)
                        list.put("CreateUser",model.CreateUser)
                        list.put("UpdateUser",model.UpdateUser)
                        list.put("CreateDate",model.CreateDate)
                        list.put("UpdateDate",model.UpdateDate)
                        list.put("activeStatus1",model.activeStatus1)

                        DatabaseManager.commonInsert(list,Constants.TBL_RoleMaster)
                    }
                    withContext(Dispatchers.Main){
                        commonUIUtility.dismissProgress()
                        if (activity is LoginActivity){
//                            (activity as LoginActivity).bindRoleDropDown()
                        }
                    }
                }else
                {
                    withContext(Main){
                        commonUIUtility.dismissProgress()
                    }
                    Log.e(TAG, "getRoleMaster: ${result.errorBody().toString()}", )
                }
            }
        }catch (e:Exception)
        {
            commonUIUtility.dismissProgress()
            e.printStackTrace()
            Log.e(TAG, "getRoleMaster: ${e.message}")
        }
    }
}