package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.fragment.ChatBoxFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.POSTChatMediaModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class PostChatMediaAPI(
    var context: Context,
    var fragment: ChatBoxFragment,
    var chatMediaModel: POSTChatMediaModel
) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "PostChatMediaAPI"

    init {
        sendChatMedia()
    }

    private fun sendChatMedia() {
        try {
            Log.d(TAG, "sendMultiPartData: FILE_UPLOAD")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO) {
                val result = APICall.uploadFile(
                    chatMediaModel.Date.toRequestBody("multipart/form-data".toMediaType()),
                    chatMediaModel.FromUser.toRequestBody("multipart/form-data".toMediaType()),
                    chatMediaModel.ToUser.toRequestBody("multipart/form-data".toMediaType()),
                    chatMediaModel.messageId.toRequestBody("multipart/form-data".toMediaType()),
                    chatMediaModel.MessageType.toRequestBody("multipart/form-data".toMediaType()),
                    chatMediaModel.FileExt.toRequestBody("multipart/form-data".toMediaType()),
                    chatMediaModel.FileMedia
                )
                if (result.isSuccessful) {
                    Log.d(TAG, "sendMultiPartData: ${result.body()}")
                    job.cancel()
                } else {
                    Log.e(TAG, "sendMultiPartData: ERROR_RESPONSE ${result.errorBody()?.string()}")
                    job.cancel()
                }
            }
        } catch (e: Exception) {
            job.cancel()
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(
                context.getString(
                    R.string.please_try_again_later_alert_msg
                )
            )
            e.printStackTrace()
            Log.e(TAG, "sendChatMedia: ${e.message}")
        }
    }
}