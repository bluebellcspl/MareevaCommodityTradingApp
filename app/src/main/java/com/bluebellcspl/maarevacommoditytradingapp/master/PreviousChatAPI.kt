package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.fragment.ChatBoxFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.UserChatInfoModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PreviousChatAPI(
    var context: Context,
    var fragment:Fragment,
    var userChatInfoModel: UserChatInfoModel,
    var Pages: Int,
    var ItemPerPage: Int
) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "PreviousChatAPI"

    init {
        loadPreviousChat()
    }

    private fun loadPreviousChat() {
        try {
            val JO = JsonObject()
            JO.addProperty("CompanyCode", "MAT189")
            JO.addProperty("FromUserId", userChatInfoModel.SenderId)
            JO.addProperty("ToUserId", userChatInfoModel.ReceiverId)
            JO.addProperty("Action", "All")
            Log.d(TAG, "loadPreviousChat: JSON : ${JO.toString()}")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO)
            {
                val result = APICall.getPreviousChat(JO,Pages,ItemPerPage)
                if (result.isSuccessful)
                {
                    val previousChatList = result.body()!!
                    if (fragment is ChatBoxFragment){
                        withContext(Dispatchers.Main){
                            (fragment as ChatBoxFragment).binding.progressBarChatBox.visibility = View.GONE
                            (fragment as ChatBoxFragment).hasNextPage = result.headers()["hasnextpage"].toBoolean()
                            (fragment as ChatBoxFragment).loadChatHistory(previousChatList)
                        }
                    }
                }else
                {
                    Log.e(TAG, "loadPreviousChat: ERROR_RESPONSE : ${result.errorBody()!!.string()}", )
                    if (fragment is ChatBoxFragment){
                        withContext(Dispatchers.Main){
                            (fragment as ChatBoxFragment).binding.progressBarChatBox.visibility = View.GONE
                            commonUIUtility.showToast("No More Chat Found!")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "loadPreviousChat: ${e.message}")
        }
    }
}