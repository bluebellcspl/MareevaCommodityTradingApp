package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.constants.Constants
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.fragment.DashboardFragment
import com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer.PCAListFragment
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchApprovedPCAListAPI(var context: Context, var activity: Activity,var fragment:Fragment) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "FetchApprovedPCAListAPI"

    init {
        DatabaseManager.initializeInstance(context)
        getApprovedPCAList()
    }

    private fun getApprovedPCAList() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject()
            JO.addProperty("CompanyCode", "MAT189")
            JO.addProperty("Action", "All")
            JO.addProperty("BuyerId", PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString())
            Log.d(TAG, "getApprovedPCAList: JSON : ${JO.toString()}")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO){

                val result = APICall.getApprovedPCAList(JO)

                if (result.isSuccessful)
                {
                    val approvedPCAList = result.body()!!
                    val list = ContentValues()
                    DatabaseManager.deleteData(Constants.TBL_ApprovedPCAMaster)
                    for(model in approvedPCAList)
                    {
                        list.put("PCAId",model.PCAId)
                        list.put("StateId",model.StateId)
                        list.put("StateName",model.StateName)
                        list.put("DistrictId",model.DistrictId)
                        list.put("DistrictName",model.DistrictName)
                        list.put("APMCId",model.APMCId)
                        list.put("APMCName",model.APMCName)
                        list.put("CommodityId",model.CommodityId)
                        list.put("CommodityName",model.CommodityName)
                        list.put("PCAName",model.PCAName)
                        list.put("PCAPhoneNumber",model.PCAPhoneNumber)
                        list.put("Address",model.Address)
                        list.put("EmailId",model.EmailId)
                        list.put("BuyerId",model.BuyerId)
                        list.put("RoleId",model.RoleId)
                        list.put("RoleName",model.RoleName)
                        list.put("ApprStatus",model.ApprStatus)
                        list.put("GCACommission",model.GCACommission)
                        list.put("PCACommission",model.PCACommission)
                        list.put("MarketCess",model.MarketCess)
                        list.put("IsActive",model.IsActive)
                        list.put("CompanyCode",model.CompanyCode)
                        list.put("CreateUser",model.CreateUser)
                        list.put("CreateDate",model.CreateDate)
                        list.put("UpdateUser",model.UpdateUser)
                        list.put("UpdateDate",model.UpdateDate)

                        DatabaseManager.commonInsert(list,Constants.TBL_ApprovedPCAMaster)
                    }
                    if (fragment is PCAListFragment)
                    {
                        withContext(Main){
                            commonUIUtility.dismissProgress()
                            (fragment as PCAListFragment).bindApprovedPCAListRecyclerView(approvedPCAList)
                        }
                    }else if (fragment is DashboardFragment)
                    {
                        withContext(Main){
                            commonUIUtility.dismissProgress()
                            (fragment as DashboardFragment).bindingApprovedPCACount(approvedPCAList)
                        }
                    }
                }else
                {
                    activity.runOnUiThread {
                        commonUIUtility.dismissProgress()
                    }
                    Log.e(TAG, "getApprovedPCAList: ${result.errorBody()}", )
                }
            }

        }catch (e:Exception){
            commonUIUtility.dismissProgress()
            e.printStackTrace()
            Log.e(TAG, "getApprovedPCAList: ${e.message}")
        }
    }

}