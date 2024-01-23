package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer.BuyerAuctionFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.BuyerAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.POSTBuyerAuctionData
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class POSTBuyerAuctionDataAPI(var context: Context,var activity: Activity,var fragment: Fragment,var model: POSTBuyerAuctionData) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "POSTBuyerAuctionDataAPI"

    init {
        postAuctionData()
    }

    private fun postAuctionData() {
        try {

            val JO = JsonObject()
            JO.addProperty("Action",model.Action)
            JO.addProperty("AllocatedBags",model.AllocatedBags)
            JO.addProperty("AuctionMasterId",model.AuctionMasterId)
            JO.addProperty("BudgetAmount",model.BudgetAmount)
            JO.addProperty("BuyerCityId",model.BuyerCityId)
            JO.addProperty("BuyerRegId",model.BuyerRegId)
            JO.addProperty("CommodityId",model.CommodityId)
            JO.addProperty("CommodityBhartiPrice",model.CommodityBhartiPrice)
            JO.addProperty("CommodityName",model.CommodityName)
            JO.addProperty("CompanyCode",model.CompanyCode)
            JO.addProperty("CreateDate",model.CreateDate)
            JO.addProperty("CreateUser",model.CreateUser)
            JO.addProperty("Date",model.Date)
            JO.addProperty("LeftBags",model.LeftBags)
            JO.addProperty("RoleId",model.RoleId)
            JO.addProperty("TotalBags",model.TotalBags)
            JO.addProperty("TotalBasic",model.TotalBasic)
            JO.addProperty("TotalCost",model.TotalCost)
            JO.addProperty("TotalGCAComm",model.TotalGCAComm)
            JO.addProperty("TotalLabourCharge",model.TotalLabourCharge)
            JO.addProperty("TotalMarketCess",model.TotalMarketCess)
            JO.addProperty("TotalPCAComm",model.TotalPCAComm)
            JO.addProperty("TotalPCAs",model.TotalPCAs)
            JO.addProperty("TotalTransportationCharge",model.TotalTransportationCharge)
            JO.addProperty("UpdateDate",model.UpdateDate)
            JO.addProperty("UpdateUser",model.UpdateUser)
            JO.add("AuctionDetailsModel",Gson().toJsonTree(model.AuctionDetailsModel))
            commonUIUtility.showProgress()
            Log.d(TAG, "postAuctionData: JSON : $JO")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO)
            {
                val result = APICall.POSTBuyerAuctionDetail(JO)
                if (result.isSuccessful)
                {
                    val response = result.body()!!
                    if (response.contains("Successfully"))
                    {
                        withContext(Dispatchers.Main){
                            commonUIUtility.dismissProgress()
                            commonUIUtility.showToast(context.getString(R.string.auction_inserted_successfully))
                            if (fragment is BuyerAuctionFragment)
                            {
                                (fragment as BuyerAuctionFragment).redirectToBuyerDashboard()
                            }
                        }
                    }else if (response.contains("Something went Wrong!"))
                    {
                        withContext(Dispatchers.Main){
                            commonUIUtility.dismissProgress()
                            commonUIUtility.showToast(response)
                        }
                    }
                }else
                {
                    Log.e(TAG, "postPCAUpdatedData: ${result.errorBody()}")
                    withContext(Dispatchers.Main){
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                    }
                }
            }

        }catch (e:Exception)
        {
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
            e.printStackTrace()
            Log.e(TAG, "postAuctionData: ${e.message}")
        }
    }
}