package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchUserMasterAPI(var context: Context, var activity: Activity, var fragment: Fragment) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchUserMasterAPI"

    init {
        DatabaseManager.initializeInstance(context)
        getFetchUserMaster()
    }

    private fun getFetchUserMaster() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject()
            JO.addProperty("CompanyCode",PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,""))
            JO.addProperty("Action",PrefUtil.ACTION_RETRIEVE)
            JO.addProperty("BuyerId",PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,""))

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO){
                val result = APICall.getUserMaster(JO)

                if (result.isSuccessful)
                {
                    val userMasterModel = result.body()!!
                    if (userMasterModel.isNotEmpty())
                    {
                        userMasterModel.forEach{model->
                            PrefUtil.setString(PrefUtil.KEY_BUYER_CITY_ID,model.CityId)
                        }
                    }
                    withContext(Dispatchers.Main)
                    {
                        commonUIUtility.dismissProgress()
                    }
                    job.cancel()
                }else
                {
                    withContext(Dispatchers.Main)
                    {
                        commonUIUtility.dismissProgress()
                    }
                    job.cancel()
                    Log.e(TAG, "getFetchUserMaster: ${result.errorBody()}")
                }
            }
        }catch (e:Exception)
        {
            job.cancel()
            e.printStackTrace()
            Log.e(TAG, "getFetchUserMaster: ${e.message}")
            commonUIUtility.dismissProgress()
        }
    }
}