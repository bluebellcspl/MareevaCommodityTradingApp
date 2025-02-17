package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.Context
import android.util.Log
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.RegisterActivity
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.model.IndividualPCARegisterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.RegErrorReponse
import com.bluebellcspl.maarevacommoditytradingapp.model.RegisterBuyerModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterIndividualPCAAPI(
    var context: Context,
    var activity: Activity,
    var model: IndividualPCARegisterModel
) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "RegisterIndividualPCAAPI"

    init {
        DatabaseManager.initializeInstance(context)
        registerIndividualPCA()
    }

    private fun registerIndividualPCA() {
        try {
            val JO = Gson().toJsonTree(model).asJsonObject
            Log.d(TAG, "registerIndividualPCA: IND_PCA_REGISTER_JSON : $JO")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO) {
                val result = APICall.registerIndividualPCA(JO)
                if (result.isSuccessful) {
                    val resultJO = result.body()!!

                    if (resultJO.get("Success").asBoolean) {
                        if (resultJO.get("Message").asString.contains("Successfully", true)) {
                            withContext(Main) {
                                commonUIUtility.dismissProgress()
                                commonUIUtility.showToast(context.getString(R.string.pca_registered_successfully))
                                if (activity is RegisterActivity) {
                                    (activity as RegisterActivity).redirectToLogin()
                                }
                            }
                        }
                    }
                } else {

                    val errorBodyString = result.errorBody()?.string()
                    val errorResult = Gson().fromJson(errorBodyString, RegErrorReponse::class.java)
                    errorResult?.let {
                        if (!errorResult.Success) {
                            val errorMessage = errorResult.Message
                            if (errorMessage.contains("Already", true)) {
                                withContext(Dispatchers.Main) {
                                    commonUIUtility.dismissProgress()
                                    commonUIUtility.showToast(context.getString(R.string.this_mobile_is_already_registered_alert_msg))
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    commonUIUtility.dismissProgress()
                                    commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                                }
                            }
                        }else
                        {
                            withContext(Dispatchers.Main) {
                                commonUIUtility.dismissProgress()
                                commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
                            }
                        }
                    }
                    Log.e(TAG, "registerBuyer: ${result.errorBody()!!.string()}")
                }
            }
        }catch (e:Exception){
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
            Log.e(TAG, "registerBuyer: ${e.message}")
            e.printStackTrace()
        }
    }

}