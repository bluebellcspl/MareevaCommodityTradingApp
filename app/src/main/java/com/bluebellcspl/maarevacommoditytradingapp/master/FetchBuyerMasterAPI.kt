package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.fragment.ProfileFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.BuyerMasterModelItem
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchBuyerMasterAPI(var context: Context, var activity: Activity, var fragment: Fragment) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchBuyerMasterAPI"

    init {
        getBuyerMaster()
    }

    private fun getBuyerMaster() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject()
            JO.addProperty("CompanyCode", "MAT189")
            JO.addProperty("Action", "All")
            JO.addProperty("BuyerId", PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,""))

            Log.d(TAG, "getBuyerMaster: FETCH_BUYER_MASTER_JSON : ${JO.toString()}")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO)
            {
                val result = APICall.getBuyerMaster(JO)
                if (result.isSuccessful) {
                    val buyerMasterModel = result.body()!!
                    if (fragment is ProfileFragment) {
                        withContext(Dispatchers.Main)
                        {
                            var model:BuyerMasterModelItem? = null
                            for(data in buyerMasterModel)
                            {
                                if (data.BuyerRegId.equals(PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString()))
                                {
                                    model = data
                                }
                            }
                            commonUIUtility.dismissProgress()
                            (fragment as ProfileFragment).bindBuyerData(model)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main)
                    {
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                        Log.e(TAG, "getBuyerMaster: ${result.errorBody()}")
                    }
                }
            }
        } catch (e: Exception) {
            commonUIUtility.dismissProgress()
            e.printStackTrace()
            Log.e(TAG, "getBuyerMaster: ${e.message}")
        }
    }
}