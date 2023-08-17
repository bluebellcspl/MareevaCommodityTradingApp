package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer.PCAListFragment
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchUnapprovedPCAListAPI(var context: Context, var activity: Activity, var fragment: Fragment) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchUnapprovedPCAListAPI"

    init {
        DatabaseManager.initializeInstance(context)
        getUnapprovedPCAList()
    }

    private fun getUnapprovedPCAList() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject()
            JO.addProperty("CompanyCode", "MAT189")
            JO.addProperty("Action", "All")
            JO.addProperty("BuyerId", "2")
            Log.d(TAG, "getUnapprovedPCAList: JSON : ${JO.toString()}")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO){

                val result = APICall.getUnapprovedPCAList(JO)

                val approvedPCAList = result.body()!!
                if (result.isSuccessful)
                {
                    if (fragment is PCAListFragment)
                    {
                        withContext(Dispatchers.Main){
                            commonUIUtility.dismissProgress()
                            (fragment as PCAListFragment).bindUnapprovedPCAListRecyclerView(approvedPCAList)
                        }
                    }
                }else
                {
                    activity.runOnUiThread {
                        commonUIUtility.dismissProgress()
                    }
                    Log.e(TAG, "getUnapprovedPCAList: ${result.errorBody()}", )
                }
            }

        }catch (e:Exception){
            commonUIUtility.dismissProgress()
            e.printStackTrace()
            Log.e(TAG, "getUnapprovedPCAList: ${e.message}")
        }
    }

}