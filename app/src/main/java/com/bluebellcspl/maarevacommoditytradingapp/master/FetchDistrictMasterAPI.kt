package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
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

class FetchDistrictMasterAPI(var context: Context, var activity: Activity) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchDistrictMasterAPI"

    init {
        DatabaseManager.initializeInstance(context)
        getDistrictMaster()
    }

        private fun getDistrictMaster() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject()
            JO.addProperty("CompanyCode", "MAT189")
            JO.addProperty("Action", "All")
            Log.d(TAG, "getDistrictMaster: JSON : ${JO.toString()}")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)

            scope.launch(Dispatchers.IO){
                val result = APICall.getDistrictMaster(JO)

                if (result.isSuccessful)
                {
                    val districtMasterModel = result.body()!!
                    val list = ContentValues()
                    DatabaseManager.deleteData(Constants.TBL_DistrictMaster)
                    for (model in districtMasterModel)
                    {
                        list.put("DistrictId",model.DistrictId)
                        list.put("DistrictName",model.DistrictName)
                        list.put("StateId",model.StateId)
                        list.put("StateName",model.StateName)
                        list.put("CompanyCode",model.CompanyCode)
                        list.put("IsActive",model.IsActive)
                        list.put("CreateUser",model.CreateUser)
                        list.put("CreateDate",model.CreateDate)
                        list.put("UpdateDate",model.UpdateDate)
                        list.put("UpdateUser",model.UpdateUser)

                        DatabaseManager.commonInsert(list,Constants.TBL_DistrictMaster)
                    }
                    withContext(Dispatchers.Main){
                        commonUIUtility.dismissProgress()
                    }
                }else
                {
                    withContext(Dispatchers.Main){
                        commonUIUtility.dismissProgress()
                    }
                    Log.e(TAG, "getDistrictMaster: ${result.errorBody().toString()}")
                }
            }

        }catch (e:Exception)
        {
            commonUIUtility.dismissProgress()
            e.printStackTrace()
            Log.e(TAG, "getDistrictMaster: ${e.message}", )
        }
    }
}