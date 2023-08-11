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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchAPMCMasterAPI(var context: Context, var activity: Activity) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchAPMCMasterAPI"

    init {
        DatabaseManager.initializeInstance(context)
        getAPMCMaster()
    }

    private fun getAPMCMaster() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject()
            JO.addProperty("CompanyCode", "MAT189")
            JO.addProperty("Action", "All")
            Log.d(TAG, "getAPMCMaster: JSON : ${JO.toString()}")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)

            scope.launch(Dispatchers.IO){
                val result = APICall.getAPMCMaster(JO)

                if (result.isSuccessful)
                {
                    val apmcMasterModel = result.body()!!
                    val list = ContentValues()
                    DatabaseManager.deleteData(Constants.TBL_APMCMaster)
                    for (model in apmcMasterModel)
                    {
                        list.put("APMCId",model.APMCId)
                        list.put("APMCName",model.APMCName)
                        list.put("SrNo",model.SrNo)
                        list.put("Location",model.Location)
                        list.put("EDate",model.EDate)
                        list.put("EstablishmentDate",model.EstablishmentDate)
                        list.put("EstabishmentDetails",model.EstabishmentDetails)
                        list.put("InfrastructureFacility",model.InfrastructureFacility)
                        list.put("NoOfShop",model.NoOfShop)
                        list.put("NoOfTraders",model.NoOfTraders)
                        list.put("NoOfBoardMember",model.NoOfBoardMember)
                        list.put("ChairmanName",model.ChairmanName)
                        list.put("WiseChairman",model.WiseChairman)
                        list.put("SecretaryName",model.SecretaryName)
                        list.put("NoOfEmployee",model.NoOfEmployee)
                        list.put("LaboratoryFacility",model.LaboratoryFacility)
                        list.put("BankFacility",model.BankFacility)
                        list.put("WarehouseFacility",model.WarehouseFacility)
                        list.put("WeightbridgeFacility",model.WeightbridgeFacility)
                        list.put("StateId",model.StateId)
                        list.put("StateName",model.StateName)
                        list.put("DistrictId",model.DistrictId)
                        list.put("DistrictName",model.DistrictName)
                        list.put("CompanyCode",model.CompanyCode)
                        list.put("IsActive",model.IsActive)
                        list.put("CreateUser",model.CreateUser)
                        list.put("CreateDate",model.CreateDate)
                        list.put("UpdateDate",model.UpdateDate)
                        list.put("UpdateUser",model.UpdateUser)

                        DatabaseManager.commonInsert(list, Constants.TBL_APMCMaster)
                    }
                    withContext(Dispatchers.Main){
                        commonUIUtility.dismissProgress()
                        if (activity is LoginActivity){
                            (activity as LoginActivity).bindAPMCDropDown()
                        }
                    }
                }else
                {
                    withContext(Dispatchers.Main){
                        commonUIUtility.dismissProgress()
                    }
                    Log.e(TAG, "getAPMCMaster: ${result.errorBody().toString()}")
                }
            }

        }catch (e:Exception)
        {
            commonUIUtility.dismissProgress()
            e.printStackTrace()
            Log.e(TAG, "getAPMCMaster: ${e.message}")
        }
    }
}