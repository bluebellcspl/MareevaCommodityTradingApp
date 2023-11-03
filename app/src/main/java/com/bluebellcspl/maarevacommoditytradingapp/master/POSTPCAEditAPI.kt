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
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
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
            val JO = JsonObject()

            JO.addProperty(  "PCAId",model.PCAId)
//            JO.addProperty(  "RegId",model.RegId)
            JO.addProperty(  "RegId",PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString())
            JO.addProperty(  "CommodityId",model.CommodityId)
            JO.addProperty(  "PCAName",model.PCAName)
            JO.addProperty(  "PCAPhoneNumber",model.PCAPhoneNumber)
            JO.addProperty(  "Typeofuser","2")
            JO.addProperty(  "Address",model.Address)
            JO.addProperty(  "EmailId",model.EmailId)
            JO.addProperty(  "BuyerId",model.BuyerId)
            JO.addProperty(  "RoleId",model.RoleId)
            JO.addProperty(  "ApprStatus",model.ApprStatus)
            JO.addProperty(  "GCACommission",model.GCACommission)
            JO.addProperty(  "PCACommission",model.PCACommission)
            JO.addProperty(  "MarketCess",model.MarketCess)
//            JO.addProperty(  "MarketCess",marketCess)
            JO.addProperty(  "IsActive",model.IsActive)
            JO.addProperty(  "CompanyCode",model.CompanyCode)
            JO.addProperty(  "CreateUser",model.CreateUser)
            JO.addProperty(  "CreateDate",model.CreateDate)
            JO.addProperty(  "UpdateUser",model.UpdateUser)
            JO.addProperty(  "UpdateDate",model.UpdateDate)
            JO.addProperty(  "Action",PrefUtil.ACTION_UPDATE)
            JO.addProperty(  "CurrentUserRole","")
            JO.addProperty(  "CityId",model.CityId)
            JO.addProperty(  "CityName",model.CityName)
            JO.addProperty(  "StateId",model.StateId)
            JO.addProperty(  "StateName",model.StateName)
            JO.addProperty(  "DistrictId",model.DistrictId)
            JO.addProperty(  "DistrictName",model.DistrictName)
            JO.addProperty(  "APMCId",model.APMCId)
            JO.addProperty(  "APMCName",model.APMCName)
            JO.addProperty(  "CommodityName",model.CommodityName)

            Log.d(TAG, "postPCAUpdatedData: PCA_UPDATE_JSON : ${JO.toString()}")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)

            scope.launch(Dispatchers.IO){
                val result = APICall.postPCAUpdateData(JO)

                if (result.isSuccessful)
                {
                    if (result.body()!!.contains("PCA Details Updated successfully")){
                        if (fragment is EditPCAFragment)
                        {
                            withContext(Main){
                                commonUIUtility.dismissProgress()
                                (fragment as EditPCAFragment).successRedirect()
                            }
                        }
                    }
                }else
                {
                    Log.e(TAG, "postPCAUpdatedData: ${result.errorBody()}")
                    withContext(Main){
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(context.getString(R.string.please_try_again_later_alert_msg))
                    }
                }
            }
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "postPCAUpdatedData: ${e.message}")
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(context.getString(R.string.sorry_something_went_wrong_alert_msg))
        }
    }

}