package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer.EditPCAFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAListModelItem
import com.bluebellcspl.maarevacommoditytradingapp.model.POSTPCAInsertModel
import com.bluebellcspl.maarevacommoditytradingapp.model.RegErrorReponse
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class POSTPCAEditAPI(var context: Context, var activity: Activity, var fragment: Fragment, var model: PCAListModelItem,var marketCess:String) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "POSTPCAInsertAPI"

    init {
        DatabaseManager.initializeInstance(context)
        postPCAUpdatedData()
    }

    private fun postPCAUpdatedData() {
        try{
            commonUIUtility.showProgress()

            val postEditPCAJO = Gson().toJsonTree(model).asJsonObject
            Log.d(TAG, "postPCAUpdatedData: PCA_UPDATE_JSON : ${postEditPCAJO}")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)

            scope.launch(Dispatchers.IO){
                val result = APICall.postPCAUpdateData(postEditPCAJO)

                if (result.isSuccessful)
                {
                    val responseJo = result.body()!!
                    if (responseJo.get("Success").asBoolean)
                    {
                        withContext(Main){
                            commonUIUtility.dismissProgress()
                            commonUIUtility.showToast(responseJo.get("Message").asString)
                            (fragment as EditPCAFragment).successRedirect()
                        }
                        job.cancel()
                    }
                }else
                {
                    val errorResponseJO = Gson().fromJson(result.errorBody()!!.string(),RegErrorReponse::class.java)
                    if (!errorResponseJO.Success)
                    {
                        withContext(Main){
                            commonUIUtility.dismissProgress()
                            commonUIUtility.showToast(errorResponseJO.Message)
                            Log.e(TAG, "postPCAData: ERROR_RESPONSE : ${errorResponseJO.Message}")
                        }
                        job.cancel()
                    }
                }
            }
        }catch (e:Exception)
        {
            job.cancel()
            e.printStackTrace()
            Log.e(TAG, "postPCAUpdatedData: ${e.message}")
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
        }
    }

}