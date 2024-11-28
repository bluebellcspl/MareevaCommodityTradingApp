package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAAuctionFragment
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAAuctionListFragment
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchIndBuyerName(var context: Context, var fragment: Fragment) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchIndBuyerName"

    init {
        getIndBuyerName()
    }

    private fun getIndBuyerName() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject().apply {
                addProperty("IndividualPCARegId",PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,""))
                addProperty("Action","IndividualPCARegId")
            }

            Log.d(TAG, "getIndBuyerName: IND_PCA_BUYER_GET_JSON : $JO")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO){
                val result = APICall.getIndPCA_Buyers(JO)
                if (result.isSuccessful){
                    val response = result.body()!!
                    withContext(Dispatchers.Main){
                        commonUIUtility.dismissProgress()
                        if (fragment is IndPCAAuctionFragment){
                            (fragment as IndPCAAuctionFragment).getBuyerFromAPI(response)
                        }
                        else if (fragment is IndPCAAuctionListFragment){
                            (fragment as IndPCAAuctionListFragment).getBuyerFromAPI(response)
                        }
                    }
                    job.complete()
                }else
                {
                    withContext(Dispatchers.Main){
                        commonUIUtility.dismissProgress()
                        Log.e(TAG, "getIndBuyerName: ERROR_FETCHING_IND_BUYER : ${result.errorBody()}", )
                    }
                    job.cancel()
                }
            }
            job.complete()
        } catch (e: Exception) {
            commonUIUtility.dismissProgress()
            e.printStackTrace()
            Log.e(TAG, "getIndBuyerName: ${e.message}")
        }
    }

}