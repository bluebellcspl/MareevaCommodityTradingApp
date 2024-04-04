package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer.BuyerChatListFragment
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.PCAChatListFragment
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchChatRecipientAPI(var context: Context, var fragment: Fragment) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchChatRecipientAPI"

    init {
        getChatRecipients()
    }

    private fun getChatRecipients() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject().apply {
                addProperty("RoleId",PrefUtil.getString(PrefUtil.KEY_ROLE_ID,""))
                addProperty("RegisterId",PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,""))
                addProperty("CompanyCode",PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,""))
            }
            Log.d(TAG, "getChatRecipients: JSON : ${JO.toString()}")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO){
                val result = APICall.getChatRecipient(JO)
                if (result.isSuccessful)
                {
                    val recipientData = result.body()!!
                    if (fragment is PCAChatListFragment)
                    {
                        withContext(Dispatchers.Main){
                            commonUIUtility.dismissProgress()
                            (fragment as PCAChatListFragment).bindBuyerData(recipientData)
                        }
                    }
                    else if (fragment is BuyerChatListFragment)
                    {
                        withContext(Dispatchers.Main){
                            commonUIUtility.dismissProgress()
                            (fragment as BuyerChatListFragment).bindChatListView(recipientData)
                        }
                    }
                }else
                {
                    withContext(Dispatchers.Main){
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
                    }
                    Log.e(TAG, "getChatRecipients: ${result.errorBody()!!.string()}", )
                }
            }
        } catch (e: Exception) {
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
            e.printStackTrace()
            Log.e(TAG, "getChatRecipients: ${e.message}")
        }
    }
}