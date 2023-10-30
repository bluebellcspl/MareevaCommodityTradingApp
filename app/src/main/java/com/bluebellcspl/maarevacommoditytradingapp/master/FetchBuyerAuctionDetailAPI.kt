package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer.BuyerAuctionFragment
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchBuyerAuctionDetailAPI(var context: Context, var activity: Activity,var fragment: Fragment) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchBuyerAuctionDetailAPI"

    init {
        DatabaseManager.initializeInstance(context)
        getFetchBuyerAuctionDetail()
    }

    private fun getFetchBuyerAuctionDetail() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject()
            JO.addProperty("CommodityId",PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,""))
//            JO.addProperty("APMCId",PrefUtil.getString(PrefUtil.KEY_APMC_ID,""))
//            JO.addProperty("DistrictId",PrefUtil.getString(PrefUtil.KEY_DISTRICT_ID,""))
//            JO.addProperty("StateId",PrefUtil.getString(PrefUtil.KEY_STATE_ID,""))
            JO.addProperty("BuyerRegId",PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,""))
            JO.addProperty("CompanyCode",PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,""))
            JO.addProperty("Date",DateUtility().getyyyyMMdd())
//            JO.addProperty("Date","2023-10-27")
            JO.addProperty("Action","All")
//            JO.addProperty("AuctionMasterId","")
//            JO.addProperty("UserName","")
//            JO.addProperty("RegId",PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,""))
//            JO.addProperty("MobileNo",PrefUtil.getString(PrefUtil.KEY_MOBILE_NO,""))

            Log.d(TAG, "getFetchBuyerAuctionDetail: FETCH_BUYER_AUCTION_DETAIL_JSON : ${JO.toString()}")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)

            scope.launch(Dispatchers.IO){
                val result = APICall.getBuyerAuctionDetail(JO)
                if (result.isSuccessful)
                {
                    Log.d(TAG, "getFetchBuyerAuctionDetail: RESPONSE : ${result.body()}")
                    if (result.body().toString().contains("No Data Found"))
                    {
                        withContext(Main){
                            commonUIUtility.dismissProgress()
                        }
                    }
                    else
                    {
                        val resultModel = result.body()!!
                        withContext(Main){
                            commonUIUtility.dismissProgress()
                            if(fragment is BuyerAuctionFragment){
                                (fragment as BuyerAuctionFragment).updateUIFromAPIData(resultModel)
                            }
                        }
                    }
                }else
                {
                    Log.e(TAG, "postPCAUpdatedData: ${result.errorBody()}")
                    withContext(Main){
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                    }
                }
            }
        }catch (e:Exception)
        {
            commonUIUtility.dismissProgress()
            e.printStackTrace()
            Log.e(TAG, "getFetchBuyerAuctionDetail: ${e.message}")
        }
    }

}