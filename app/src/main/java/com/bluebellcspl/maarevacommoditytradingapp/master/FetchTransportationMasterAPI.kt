package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.provider.ContactsContract.Data
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

class FetchTransportationMasterAPI(var context: Context,var activity: Activity) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchTransportationMasterAPI"

    init {
        DatabaseManager.initializeInstance(context)
        getTransportationMaster()
    }

    private fun getTransportationMaster() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject()
//            JO.addProperty("BuyerId", PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,""))
            JO.addProperty("CompanyCode", "MAT189")
            JO.addProperty("Action", PrefUtil.ACTION_RETRIEVE)

            Log.d(TAG, "getTransportationMaster: JSON : $JO")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)

            scope.launch(Dispatchers.IO){
                val result = APICall.getTransportationMaster(JO)

                if (result.isSuccessful)
                {
                    val transportationMasterModel = result.body()!!

                    val list = ContentValues()
                    if (transportationMasterModel.isNotEmpty())
                    {
                        DatabaseManager.deleteData(Constants.TBL_TransportationMaster)
                        for(model in transportationMasterModel)
                        {
                            list.put("Cdate",model.Cdate)
                            list.put("City1",model.City1)
                            list.put("City2",model.City2)
                            list.put("CityName",model.CityName)
                            list.put("CityName2",model.CityName2)
                            list.put("CompanyCode",model.CompanyCode)
                            list.put("CreateUser",model.CreateUser)
                            list.put("IsActive",model.IsActive)
                            list.put("PerBoriRate",model.PerBoriRate)
                            list.put("TransportId",model.TransportId)

                            DatabaseManager.commonInsert(list,Constants.TBL_TransportationMaster)
                        }
                    }

                    withContext(Dispatchers.Main)
                    {
                        commonUIUtility.dismissProgress()
                    }
                }else
                {
                    commonUIUtility.dismissProgress()
                    Log.e(TAG, "getTransportationMaster: ${result.errorBody()}", )
                }
            }
        }catch (e:Exception)
        {
            commonUIUtility.dismissProgress()
            e.printStackTrace()
            Log.e(TAG, "getTransportationMaster: ${e.message}")
        }
    }
}