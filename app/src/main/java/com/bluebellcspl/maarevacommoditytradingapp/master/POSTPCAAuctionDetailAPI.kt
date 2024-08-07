package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.PCAAuctionFragment
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.PCADashboardFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAAuctionErrorResponse
import com.bluebellcspl.maarevacommoditytradingapp.model.POSTPCAAuctionData
import com.bluebellcspl.maarevacommoditytradingapp.model.RegErrorReponse
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class POSTPCAAuctionDetailAPI(var context: Context,var activity: Activity,var fragment: Fragment,var model:POSTPCAAuctionData) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "POSTPCAAuctionDetailAPI"

    init {
        postPCAData()
    }

    private fun postPCAData() {
        try {
            commonUIUtility.showProgress()

            val JO1 = JsonObject()
            JO1.addProperty("Date", DateUtility().getyyyyMMdd())
            JO1.addProperty("CompanyCode", PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,""))
            JO1.addProperty("RegId", PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,""))
            JO1.addProperty("BuyerId", PrefUtil.getString(PrefUtil.KEY_BUYER_ID,""))
            JO1.addProperty("CommodityId", PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,""))

            val postPCAAuctionDataJO =Gson().toJsonTree(model).asJsonObject
            Log.d(TAG, "postPCAData: POST_PCA_AUCTION_JSON : $postPCAAuctionDataJO")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO)
            {
                val result = APICall.postPCAAuctionDataInsUpd(postPCAAuctionDataJO)
                if (result.isSuccessful)
                {
                    val responseJO = result.body()!!
                    if (responseJO.get("Message").asString.contains("Insert Successfully",true))
                    {
                        if (fragment is PCAAuctionFragment)
                        {
                            withContext(Dispatchers.Main)
                            {
                                (fragment as PCAAuctionFragment).clearData()
                                commonUIUtility.showToast("Bags Inserted Successfully!")
                                commonUIUtility.dismissProgress()
                                FetchPCAAuctionDetailAPI(context, activity, fragment)
                            }
                            job.complete()
                        }
                    }else if(responseJO.get("Message").asString.contains("Updated Successfully",true)){
                        withContext(Dispatchers.Main)
                        {
                            commonUIUtility.showToast("Bags Updated Successfully!")
                            commonUIUtility.dismissProgress()
                            FetchPCAAuctionDetailAPI(context, activity, fragment)
                        }
                        job.complete()
                    }else{
                        withContext(Dispatchers.Main)
                        {
                            commonUIUtility.showToast("Bags NOT Updated!")
                            commonUIUtility.dismissProgress()
                        }
                        job.cancel()
                    }
                }else
                {
                    val errorResponseJO = Gson().fromJson(result.errorBody()!!.string(),RegErrorReponse::class.java)
                    if (!errorResponseJO.Success)
                    {
                        if (errorResponseJO.Message.contains("Current auction is stopped",true)){
                            withContext(Main){
                                commonUIUtility.dismissProgress()
                                (fragment as PCAAuctionFragment).noAuctionPopup(context.getString(R.string.current_auction_is_stopped_lbl))
                                Log.e(TAG, "postPCAData: ERROR_RESPONSE : ${errorResponseJO.Message}")
                            }
                            job.cancel()
                        }
                    }
                    withContext(Dispatchers.Main)
                    {
                        commonUIUtility.showToast("Bags NOT Inserted!")
                        commonUIUtility.dismissProgress()
                    }
                    job.cancel()

                }
            }

        }catch (e:Exception)
        {
            job.cancel()
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast("Please Try Again Later!")
            Log.e(TAG, "postPCAData: ${e.message}")
            e.printStackTrace()
        }
    }
}