package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.util.Log
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

class FetchCityMasterAPI(var context: Context, var activity: Activity) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchCityMasterAPI"

    init {
        DatabaseManager.initializeInstance(context)
        getCityMaster()
    }

    private fun getCityMaster() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject()
            JO.addProperty("BuyerId", PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,""))
            JO.addProperty("CompanyCode", PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,""))
            JO.addProperty("Action",PrefUtil.ACTION_RETRIEVE)

            Log.d(TAG, "getCityMaster: JSON : $JO")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO)
            {
                val result = APICall.getCityMaster(JO)

                if (result.isSuccessful)
                {
                    val cityMasterModel = result.body()!!
                    val list = ContentValues()
                    DatabaseManager.deleteData(Constants.TBL_CityMaster)

                    for(model in cityMasterModel)
                    {
                        list.put("CityId",model.CityId)
                        list.put("CityName",model.CityName)
                        list.put("CompanyCode",model.CompanyCode)
                        list.put("CreateDate",model.CreateDate)
                        list.put("CreateUser",model.CreateUser)
                        list.put("DistrictId",model.DistrictId)
                        list.put("DistrictName",model.DistrictName)
                        list.put("IsActive",model.IsActive)
                        list.put("StateId",model.StateId)
                        list.put("StateName",model.StateName)
                        list.put("UpdateDate",model.UpdateDate)
                        list.put("UpdateUser",model.UpdateUser)

                        DatabaseManager.commonInsert(list,Constants.TBL_CityMaster)
                    }
                    withContext(Dispatchers.Main)
                    {
                        commonUIUtility.dismissProgress()
                    }
                    job.cancel()
                }else
                {
                    job.cancel()
                    commonUIUtility.dismissProgress()
                    Log.e(TAG, "getCityMaster: ${result.errorBody()}")
                }
            }
        }catch (e:Exception)
        {
            job.cancel()
            commonUIUtility.dismissProgress()
            e.printStackTrace()
            Log.e(TAG, "getCityMaster: ${e.message}")
        }
    }
}