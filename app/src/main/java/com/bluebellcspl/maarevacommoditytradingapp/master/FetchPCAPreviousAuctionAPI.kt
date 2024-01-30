package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer.BuyerDashboardFragment
import com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer.BuyerPreviousAuctionFragment
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.PCADashboardFragment
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.PCAPreviousAuctionFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAPrevAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchPCAPreviousAuctionAPI(
    var context: Context,
    var fragment: Fragment,
    var selectedDate: String
) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchPCAPreviousAuctionAPI"

    init {
        getPCAPreviousAuction()
    }

    private fun getPCAPreviousAuction() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject()
            JO.addProperty("CommodityId", PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID, ""))
            JO.addProperty("BuyerRegId", PrefUtil.getString(PrefUtil.KEY_BUYER_ID, ""))
            JO.addProperty("CompanyCode", PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE, ""))
            JO.addProperty("Date", selectedDate)
            JO.addProperty("PCARegId", PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, ""))

            Log.d(TAG, "getPCAPreviousAuction: BUYER_PREVIOUS_AUCTION_JSON : ${JO.toString()}")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)

            scope.launch(Dispatchers.IO) {
                try {
                    val result = APICall.getPCAPreviousAuction(JO)
                    if (result.isSuccessful) {
                        val pcaPrevAuctionMasterModel = result.body()!!
                        Log.d(
                            TAG,
                            "getPCAPreviousAuction: RESPONSE_BUYER_PREV_AUCTION : $pcaPrevAuctionMasterModel"
                        )
                        handleResult(pcaPrevAuctionMasterModel)
                    } else {
                        withContext(Dispatchers.Main) {
                            commonUIUtility.dismissProgress()
                            Log.e(TAG, "getPCAPreviousAuction ERROR : ${result.errorBody()}")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                        Log.e(TAG, "getPCAPreviousAuction: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }

        } catch (e: Exception) {
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
            e.printStackTrace()
            Log.e(TAG, "getPCAPreviousAuction: ${e.message}")
        }
    }


    private suspend fun handleResult(pcaPrevAuctionMasterModel: PCAPrevAuctionMasterModel) {
        withContext(Dispatchers.Main) {
            commonUIUtility.dismissProgress()

            when (fragment) {
                is PCADashboardFragment -> {
                    (fragment as PCADashboardFragment).bindPreviousAuctionData(
                        pcaPrevAuctionMasterModel
                    )
                }
                is PCAPreviousAuctionFragment -> {
                    (fragment as PCAPreviousAuctionFragment).bindDataOfPrevAuction(pcaPrevAuctionMasterModel)
                }
            }
        }
    }
}