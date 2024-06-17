package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.model.POSTSeenNotificationModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class POSTSeenNotificationAPI(var context:Context,var fragment:Fragment,var notificationList:ArrayList<POSTSeenNotificationModel>) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "POSTSeenNotificationAPI"

    init {
        syncSeenNotification()
    }

    private fun syncSeenNotification() {
        try {
            val JS = Gson().toJsonTree(notificationList).asJsonArray
            Log.d(TAG, "syncSeenNotification: SYNC_NOTIFICATION_LIST : $JS")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)

            scope.launch(Dispatchers.IO){
                val result = APICall.postUnseenNotificationSync(JS)
                if (result.isSuccessful)
                {
                    val responseJS = result.body()!!
                    DatabaseManager.ExecuteQuery(Query.updateTMPNotificationSeenStatus())
                }else
                {
                    withContext(Dispatchers.Main)
                    {
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
                    }
                    job.cancel()
                }
            }
        }catch (e:Exception){
            job.cancel()
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
            e.printStackTrace()
            Log.e(TAG, "syncSeenNotification: ${e.message}")
        }
    }
}