package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.constants.Constants
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.model.ShopMasterAPICallModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchShopMasterAPI(var context: Context, var activity: Activity,var model: ShopMasterAPICallModel) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchShopMasterAPI"

    init {
        DatabaseManager.initializeInstance(context)
        getShopMaster()
    }

    private fun getShopMaster() {
        try {
            commonUIUtility.showProgress()
            Log.d(TAG, "getShopMaster: PROGRESS_START")

            var shopAPICallJO = Gson().toJsonTree(model).asJsonObject
            Log.d(TAG, "getShopMaster: JSON : $shopAPICallJO")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)

            scope.launch(Dispatchers.IO){
                val result = APICall.getShopMaster(shopAPICallJO)
                if (result.isSuccessful)
                {
                    val shopMasterModel = result.body()!!
                    val list = ContentValues()
                    if (shopMasterModel.isNotEmpty())
                    {
                        DatabaseManager.deleteData(Constants.TBL_ShopMaster)
                        for(model in shopMasterModel)
                        {
                            list.put("APMCId",model.APMCId)
                            list.put("APMCName",model.APMCName)
                            list.put("CompanyCode",model.CompanyCode)
                            list.put("CreateDate",model.CreateDate)
                            list.put("CreateUser",model.CreateUser)
                            list.put("DistrictId",model.DistrictId)
                            list.put("DistrictName",model.DistrictName)
                            list.put("GujaratiShopName",model.GujaratiShopName)
                            list.put("GujaratiShortShopName",model.GujaratiShortShopName)
                            list.put("IsActive",model.IsActive)
                            list.put("ShopAddress",model.ShopAddress)
                            list.put("ShopId",model.ShopId)
                            list.put("ShopName",model.ShopName)
                            list.put("ShopNo",model.ShopNo)
                            list.put("ShortShopName",model.ShortShopName)
                            list.put("StateId",model.StateId)
                            list.put("StateName",model.StateName)
                            list.put("UpdateDate",model.UpdateDate)
                            list.put("UpdateUser",model.UpdateUser)
                            DatabaseManager.commonInsert(list,Constants.TBL_ShopMaster)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        commonUIUtility.dismissProgress()
                        Log.d(TAG, "getShopMaster: PROGRESS_END")
                    }
                    job.cancel()
                }else
                {
                    withContext(Dispatchers.Main) {
                        commonUIUtility.dismissProgress()
                        Log.d(TAG, "getShopMaster: PROGRESS_END")
                    }
                    job.cancel()
                    Log.e(TAG, "getShopMaster: ${result.errorBody()}")
                }
            }
        }catch (e:Exception)
        {
            job.cancel()
            commonUIUtility.dismissProgress()
            Log.d(TAG, "getShopMaster: PROGRESS_END")
            e.printStackTrace()
            Log.e(TAG, "getShopMaster: ${e.message}")
        }
    }
}