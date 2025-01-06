package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAInvoiceStockFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.CommodityListModel
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAStockBuyerWiseModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FetchCommodityForStockAPI(var context: Context, var fragment: Fragment, var model: IndPCAInvoiceStockFragment.IndPCAInvoiceStockFetchAPIModel){
    val job = Job()
    val scope = CoroutineScope(job)
    private val commonUIUtility by lazy { CommonUIUtility(context)}
    val TAG = "FetchCommodityForStockAPI"

    init {
        getCommodityList()
    }

    private fun getCommodityList() {
        try {
            commonUIUtility.showProgress()
            val JO = Gson().toJsonTree(model).asJsonObject
            Log.d(TAG, "getCommodityList: STOCK_COMMODITY_LIST_JSON : $JO")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO) {
                val result = APICall.getIndPCAInvoiceStockAPI(JO)
                if (result.isSuccessful) {
                    val responseData = result.body()!!
                    if (responseData.isJsonObject){
                        val responseJO = responseData.asJsonObject
                        if (!responseJO.get("Success").asBoolean){
                            withContext(Dispatchers.Main){
                                commonUIUtility.dismissProgress()
                                commonUIUtility.showToast(context.getString(R.string.no_data_found))
                            }
                            job.cancel()
                        }
                    }else if (responseData.isJsonArray){
                        val responseJA = responseData.asJsonArray
                        val userListType = object : TypeToken<CommodityListModel>() {}.type
                        var commodityList: CommodityListModel = Gson().fromJson(responseJA, userListType)
                        withContext(Dispatchers.Main){
                          commonUIUtility.dismissProgress()
                          if (fragment is IndPCAInvoiceStockFragment){
                              (fragment as IndPCAInvoiceStockFragment).bindStockCommodityList(commodityList)
                          }
                        }
                        job.complete()
                    }
                }else
                {
                    withContext(Dispatchers.Main){
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                    }
                    job.cancel()
                }
            }
            job.complete()
        } catch (e: Exception) {
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
            e.printStackTrace()
            Log.e(TAG, "getCommodityList: ${e.message}")

        }
    }
}