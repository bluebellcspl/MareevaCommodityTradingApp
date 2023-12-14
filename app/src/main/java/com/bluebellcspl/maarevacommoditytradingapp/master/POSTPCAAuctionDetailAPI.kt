package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.PCAAuctionFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.POSTPCAAuctionData
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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


            val JO = JsonObject()
            JO.addProperty("PCAAuctionHeaderId",model.PCAAuctionHeaderId)
            JO.addProperty("PCAAuctionDetailId",model.PCAAuctionDetailId)
            JO.addProperty("PCAAuctionMasterId",model.PCAAuctionMasterId)
            JO.addProperty("Date",model.Date)
            JO.addProperty("BuyerId",model.BuyerId)
            JO.addProperty("RoleId",model.RoleId)
            JO.addProperty("PCARegId",model.PCARegId)
            JO.addProperty("PCAId",model.PCAId)
            JO.addProperty("CommodityId",model.CommodityId)
            JO.addProperty("CommodityBhartiPrice",model.CommodityBhartiPrice)
            JO.addProperty("APMCId",model.APMCId)
            JO.addProperty("BuyerBori",model.BuyerBori)
            JO.addProperty("BuyerLowerPrice",model.BuyerLowerPrice)
            JO.addProperty("BuyerUpperPrice",model.BuyerUpperPrice)
            JO.addProperty("AvgPrice",model.AvgPrice)
            JO.addProperty("RemainingBags",model.RemainingBags)
            JO.addProperty("TotalCost",model.TotalCost)
            JO.addProperty("TotalPurchasedBags",model.TotalPurchasedBags)
            JO.addProperty("ShopId",model.ShopId)
            JO.addProperty("ShopNo",model.ShopNo)
            JO.addProperty("Bags",model.Bags)
            JO.addProperty("CurrentPrice",model.CurrentPrice)
            JO.addProperty("Amount",model.Amount)
            JO.addProperty("CompanyCode",model.CompanyCode)
            JO.addProperty("CreateUser",model.CreateUser)
            JO.addProperty("CreateDate",model.CreateDate)
            JO.addProperty("UpdateUser",model.UpdateUser)
            JO.addProperty("UpdateDate",model.UpdateDate)
            JO.addProperty("action",model.action)

            Log.d(TAG, "postPCAData: POST_PCA_AUCTION_JSON : $JO")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO)
            {
                val checkIsAuctionStatus = APICall.checkPCAIsAuctionStop(JO1)
                if (checkIsAuctionStatus.isSuccessful)
                {
                    val checkStatusResponse = checkIsAuctionStatus.body()!!
                    var status =checkStatusResponse.get("IsAuctionStop").asString
                    if (status.equals("false",true))
                    {
                        val result = APICall.postPCAAuctionDataInsUpd(JO)

                        if (result.isSuccessful)
                        {
                            val responseJO = result.body()!!
                            if (responseJO.get("Message").asString.contains("PCA Auction Insert Successfully",true))
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
                                }
                            }else if(responseJO.get("Message").asString.contains("Shop Details Updated Successfully",true)){
                                withContext(Dispatchers.Main)
                                {
                                    commonUIUtility.showToast("Bags Updated Successfully!")
                                    commonUIUtility.dismissProgress()
                                    FetchPCAAuctionDetailAPI(context, activity, fragment)
                                }
                            }else{
                                withContext(Dispatchers.Main)
                                {
                                    commonUIUtility.showToast("Bags NOT Updated!")
                                    commonUIUtility.dismissProgress()
                                }
                            }
                        }else
                        {
                            Log.e(TAG, "postPCAData: ${result.errorBody()}")
                            withContext(Dispatchers.Main)
                            {
                                commonUIUtility.showToast("Bags NOT Inserted!")
                                commonUIUtility.dismissProgress()
                            }
                        }
                    }else
                    {
                        withContext(Dispatchers.Main)
                        {
                            commonUIUtility.dismissProgress()
                            if (fragment is PCAAuctionFragment) {
                                (fragment as PCAAuctionFragment).noAuctionPopup()
                            }
                        }
                    }
                }else
                {
                    withContext(Dispatchers.Main){
                        Log.e(TAG, "postPCAData: ${checkIsAuctionStatus.errorBody()}")
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast("Data NOT Inserted 2!")
                    }
                }
            }

        }catch (e:Exception)
        {
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast("Please Try Again Later!")
            Log.e(TAG, "postPCAData: ${e.message}")
            e.printStackTrace()
        }
    }
}