package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.fragment.DashboardFragment
import com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer.PCAListFragment
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchApprovedPCAListAPI(var context: Context, var activity: Activity,var fragment:Fragment) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchApprovedPCAListAPI"

    init {
        DatabaseManager.initializeInstance(context)
        getApprovedPCAList()
    }

    private fun getApprovedPCAList() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject()
            JO.addProperty("CompanyCode", "MAT189")
            JO.addProperty("Action", "All")
            JO.addProperty("BuyerId", "2")
            Log.d(TAG, "getApprovedPCAList: JSON : ${JO.toString()}")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO){

                val result = APICall.getApprovedPCAList(JO)

                val approvedPCAList = result.body()!!
                if (result.isSuccessful)
                {
                    if (fragment is PCAListFragment)
                    {
                        withContext(Main){
                            commonUIUtility.dismissProgress()
                            (fragment as PCAListFragment).bindApprovedPCAListRecyclerView(approvedPCAList)
                        }
                    }else if (fragment is DashboardFragment)
                    {
                        withContext(Main){
                            commonUIUtility.dismissProgress()
                            (fragment as DashboardFragment).bindingApprovedPCACount(approvedPCAList)
                        }
                    }
                }else
                {
                    activity.runOnUiThread {
                        commonUIUtility.dismissProgress()
                    }
                    Log.e(TAG, "getApprovedPCAList: ${result.errorBody()}", )
                }
            }

        }catch (e:Exception){
            commonUIUtility.dismissProgress()
            e.printStackTrace()
            Log.e(TAG, "getApprovedPCAList: ${e.message}")
        }
    }

}