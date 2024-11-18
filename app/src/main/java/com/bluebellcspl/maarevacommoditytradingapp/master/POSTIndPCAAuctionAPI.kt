package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAAuctionFragment
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAAuctionListFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAAuctionInsertModel
import com.bluebellcspl.maarevacommoditytradingapp.model.POSTPCAAuctionData
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class POSTIndPCAAuctionAPI(var context: Context, var fragment: Fragment, var model: IndPCAAuctionInsertModel,var position:Int) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "POSTIndPCAAuctionAPI"


    init {
        postIndPCAAuctionData()
    }

    private fun postIndPCAAuctionData() {
        try {
            commonUIUtility.showProgress()
            val JO = Gson().toJsonTree(model).asJsonObject

            if (fragment is IndPCAAuctionFragment) {
                Log.d(TAG, "postIndPCAAuctionData: POST_IND_PCA_AUCTION_JSON : $JO")
            }else if(fragment is IndPCAAuctionListFragment)
            {
                Log.d(TAG, "postIndPCAAuctionData: POST_IND_PCA_AUCTION_UPDATE_JSON : $JO")
            }


            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO){
                val result = APICall.postIndPCAAuctionData(JO)
                if (result.isSuccessful)
                {
                    val responseJO = result.body()!!

                    if (responseJO.get("Success").asBoolean){
                        withContext(Dispatchers.Main){
                            commonUIUtility.dismissProgress()
                            if (fragment is IndPCAAuctionFragment){
                                commonUIUtility.showToast(responseJO.get("Message").asString)
                                (fragment as IndPCAAuctionFragment).updateAfterInsert()
                                FetchIndPCAAuctionAPI(context,fragment)
                            }
                            else if (fragment is IndPCAAuctionListFragment){
                                if (responseJO.get("Message").asString.contains("Updated",true)){
                                    commonUIUtility.showToast(responseJO.get("Message").asString)
                                    FetchIndPCAAuctionAPI(context,fragment)
                                }else if (responseJO.get("Message").asString.contains("Delete",true)){
                                    commonUIUtility.showToast(responseJO.get("Message").asString)
                                    (fragment as IndPCAAuctionListFragment).deleteShopItem(position)
//                                    FetchIndPCAAuctionAPI(context,fragment)
                                }else{

                                }
                            } else {

                            }
                        }
                        job.complete()
                    }

                }else{
                    withContext(Dispatchers.Main){
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                    }
                    job.cancel()
                }
            }
            job.complete()

        }catch (e:Exception)
        {
            job.cancel()
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
            Log.e(TAG, "postPCAData: ${e.message}")
            e.printStackTrace()

        }
    }
}