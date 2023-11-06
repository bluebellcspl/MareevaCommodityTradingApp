package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FetchPCAAuctionDetailAPI(var context: Context, var activity: Activity, var fragment: Fragment) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchPCAAuctionDetailAPI"

    init {
        getPCAAuction()
    }

    private fun getPCAAuction() {
        try {
            val JO = JsonObject()
            JO.addProperty("Date",DateUtility().getyyyyMMdd())
            JO.addProperty("CompanyCode",PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,""))
            JO.addProperty("MobileNo",PrefUtil.getString(PrefUtil.KEY_MOBILE_NO,""))
            JO.addProperty("RegId",PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,""))

            Log.d(TAG, "getPCAAuction: JSON : $JO")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO){
                val result = APICall.getPCAAuctionDetail(JO)
                if (result.isSuccessful)
                {

                }else
                {

                }
            }
        }catch (e:Exception)
        {
            commonUIUtility.showToast("Please Try Again Later!")
            e.printStackTrace()
            Log.e(TAG, "getPCAAuction: ${e.message}")
            commonUIUtility.dismissProgress()
        }
    }
}