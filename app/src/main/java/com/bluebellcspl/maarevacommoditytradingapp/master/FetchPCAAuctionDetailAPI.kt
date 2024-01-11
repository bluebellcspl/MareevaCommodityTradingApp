package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.PCAAuctionFragment
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.PCAAuctionListFragment
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.PCADashboardFragment
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            commonUIUtility.showProgress()
            val JO = JsonObject()
            JO.addProperty("Date",DateUtility().getyyyyMMdd())
            JO.addProperty("CompanyCode",PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,""))
            JO.addProperty("RegId",PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,""))
            JO.addProperty("BuyerId",PrefUtil.getString(PrefUtil.KEY_BUYER_ID,""))
            JO.addProperty("CommodityId",PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,""))

            Log.d(TAG, "getPCAAuction: JSON : $JO")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO){
                val result = APICall.getPCAAuctionDetail(JO)
                if (result.isSuccessful)
                {
                    val pcaAuctionDetailModel = result.body()!!
                    Log.d(TAG, "getPCAAuction: RESPONSE : $pcaAuctionDetailModel")
                    withContext(Dispatchers.Main)
                    {
                        commonUIUtility.dismissProgress()
                        if (pcaAuctionDetailModel.IsAuctionStop.equals("false",true))
                        {
                            if (fragment is PCAAuctionFragment)
                            {
                                (fragment as PCAAuctionFragment).updateUIFromAPIData(pcaAuctionDetailModel)
                            }else if (fragment is PCAAuctionListFragment)
                            {
                                (fragment as PCAAuctionListFragment).bindAuctionList(pcaAuctionDetailModel.ApiPCAAuctionDetail)
                            }else if (fragment is PCADashboardFragment)
                            {
                                (fragment as PCADashboardFragment).bindBuyerAllocatedData(pcaAuctionDetailModel)
                            }
                        }else
                        {
                            withContext(Dispatchers.Main)
                            {
                                commonUIUtility.dismissProgress()
                                if (fragment is PCAAuctionFragment)
                                {
                                    (fragment as PCAAuctionFragment).noAuctionPopup()
                                }
                            }
                        }

                    }
                }else
                {
                    Log.e(TAG, "getPCAAuction: ${result.errorBody()}")
                    withContext(Dispatchers.Main)
                    {
                        commonUIUtility.dismissProgress()
                        if (fragment is PCAAuctionFragment)
                        {
                            (fragment as PCAAuctionFragment).noAuctionPopup()
                        }
                    }
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