package com.bluebellcspl.maarevacommoditytradingapp.master

import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.constants.Constants
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCADashboardFragment
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCADashboardFragment.CommodityDetail
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAInvoiceReportFragment
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchAPMCIntCommodityAPI(var context:Context,var fragment:Fragment) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchAPMCIntCommodityAPI"

    init {
        getCommodityAPMCWise()
    }

    private fun getCommodityAPMCWise() {
        try {
            val JO = JsonObject().apply {
                addProperty("CompanyCode","MAT189")
                addProperty("Language",PrefUtil.getSystemLanguage())
                addProperty("Action","GetCommodityList")
                addProperty("APMCId",PrefUtil.getString(PrefUtil.KEY_APMC_ID,""))
            }

            Log.d(TAG, "getCommodityAPMCWise: COMMODITY_FETCH_JSON : $JO")
            commonUIUtility.showProgress()

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO){
                val result = APICall.getAMPCIntCommodity(JO)
                if (result.isSuccessful)
                {
                    val commdoityListAPMCWise = result.body()!!
                    val list = ContentValues()
                    val commodityAPMCWiseForDD = ArrayList<CommodityDetail>()
                    if (commdoityListAPMCWise.isNotEmpty()){
                        DatabaseManager.deleteData(Constants.TBL_APMCIntCommodityMaster)
                        for (model in commdoityListAPMCWise) {
                            val commodityDetail = CommodityDetail(
                                model.CommodityId,
                                model.CommodityName,
                                model.CommodityName
                            )
                            commodityAPMCWiseForDD.add(commodityDetail)

                            list.put("InteId",model.InteId)
                            list.put("APMCId",model.APMCId)
                            list.put("APMCName",model.APMCName)
                            list.put("CreateDate",model.CreateDate)
                            list.put("IsActive",model.IsActive)
                            list.put("CommodityId",model.CommodityId)
                            list.put("CommodityName",model.CommodityName)
                            list.put("CommodityBhartiValue",model.CommodityBhartiValue)
                            list.put("cdate",model.cdate)
                            list.put("CreateUser",model.CreateUser)
                            list.put("CreatedUserName",model.CreatedUserName)

                            DatabaseManager.commonInsert(list,Constants.TBL_APMCIntCommodityMaster)
                        }
                    }
                    withContext(Dispatchers.Main){
                        commonUIUtility.dismissProgress()
                        if (fragment is IndPCADashboardFragment){
                            (fragment as IndPCADashboardFragment).bindCommodityAPMCWise(commodityAPMCWiseForDD)
                        }else if (fragment is IndPCAInvoiceReportFragment){
                            (fragment as IndPCAInvoiceReportFragment).bindCommodityList(commodityAPMCWiseForDD)
                        }
                    }
                    job.complete()

                }else{
                    withContext(Dispatchers.Main) {
                        Log.e(TAG, "getCommodityAPMCWise: ${result.errorBody()}", )
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                    }
                }
            }
            job.complete()
        }catch (e:Exception){
            e.printStackTrace()
            Log.e(TAG, "getCommodityAPMCWise: ${e.message}")
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
        }
    }
}