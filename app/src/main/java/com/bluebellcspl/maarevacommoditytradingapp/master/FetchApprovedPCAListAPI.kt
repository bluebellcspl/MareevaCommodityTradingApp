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
import com.bluebellcspl.maarevacommoditytradingapp.fragment.ProfileFragment
import com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer.BuyerDashboardFragment
import com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer.PCAListFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAListModelItem
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

class FetchApprovedPCAListAPI(
    var context: Context,
    var activity: Activity,
    var fragment: Fragment
) {
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
            var CURRENT_USER = PrefUtil.getString(PrefUtil.KEY_ROLE_NAME, "").toString()
            var Buyer_Reg_Id = ""
            if (CURRENT_USER.equals("pca", true)) {
                Buyer_Reg_Id = PrefUtil.getString(PrefUtil.KEY_BUYER_ID, "").toString()
            } else {
                Buyer_Reg_Id = PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString()
            }
            JO.addProperty("BuyerId", Buyer_Reg_Id)
            Log.d(TAG, "getPCAList: JSON : ${JO.toString()}")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.IO) {

                val result = APICall.getPCAMaster(JO)

                if (result.isSuccessful) {
                    val pcaList = result.body()!!
                    val approvedPCAList = ArrayList<PCAListModelItem>()
                    val unapprovedPCAList = ArrayList<PCAListModelItem>()
                    val list = ContentValues()
                    var data: PCAListModelItem? = null
                    if (pcaList.isNotEmpty())
                    {
                        DatabaseManager.deleteData(Constants.TBL_PCAMaster)
                        for (model in pcaList) {
                            list.put("APMCId", model.APMCId)
                            list.put("APMCName", model.APMCName)
                            list.put("Address", model.Address)
                            list.put("AdharNo", model.AdharNo)
                            list.put("AdharPhoto", model.AdharPhoto)
                            list.put("ApprStatus", model.ApprStatus)
                            list.put("BuyerId", model.BuyerId)
                            list.put("CityId", model.CityId)
                            list.put("CityName", model.CityName)
                            list.put("CommodityId", model.CommodityId)
                            list.put("CommodityName", model.CommodityName)
                            list.put("CompanyCode", model.CompanyCode)
                            list.put("CreateDate", model.CreateDate)
                            list.put("CreateUser", model.CreateUser)
                            list.put("DistrictId", model.DistrictId)
                            list.put("DistrictName", model.DistrictName)
                            list.put("EmailId", model.EmailId)
                            list.put("GCACommission", model.GCACommission)
                            list.put("GSTCertiPhoto", model.GSTCertiPhoto)
                            list.put("GSTNo", model.GSTNo)
                            list.put("IsActive", model.IsActive)
                            list.put("LabourCharges", model.LabourCharges)
                            list.put("LicenseCopyPhoto", model.LicenseCopyPhoto)
                            list.put("MarketCess", model.MarketCess)
                            list.put("Mobile2", model.Mobile2)
                            list.put("PCACommission", model.PCACommission)
                            list.put("PCAId", model.PCAId)
                            list.put("PCAName", model.PCAName)
                            list.put("PCAPhoneNumber", model.PCAPhoneNumber)
                            list.put("PCARegId", model.PCARegId)
                            list.put("PanCardNo", model.PanCardNo)
                            list.put("PanCardPhoto", model.PanCardPhoto)
                            list.put("ProfilePic", model.ProfilePic)
                            list.put("RoleId", model.RoleId)
                            list.put("RoleName", model.RoleName)
                            list.put("StateId", model.StateId)
                            list.put("StateName", model.StateName)
                            list.put("UpdateDate", model.UpdateDate)
                            list.put("UpdateUser", model.UpdateUser)

                            if (model.ApprStatus.equals("true")) {
                                approvedPCAList.add(model)
                            } else if (model.ApprStatus.equals("false")) {
                                unapprovedPCAList.add(model)
                            }
                            if (model.PCARegId.equals(
                                    PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString()
                                )
                            ) {
                                data = model
                            }

                            DatabaseManager.commonInsert(list, Constants.TBL_PCAMaster)
                        }
                    }

                    Log.d(TAG, "getApprovedPCAList: APPROVED_PCA_LIST : $approvedPCAList")
                    Log.d(TAG, "getApprovedPCAList: UNAPPROVED_PCA_LIST : $unapprovedPCAList")
//                    if (fragment is PCAListFragment) {
//                        withContext(Main) {
//                            commonUIUtility.dismissProgress()
//                            (fragment as PCAListFragment).bindApprovedPCAListRecyclerView(
//                                approvedPCAList
//                            )
//                            (fragment as PCAListFragment).bindUnapprovedPCAListRecyclerView(
//                                unapprovedPCAList
//                            )
//                        }
//                    } else if (fragment is BuyerDashboardFragment) {
//                        withContext(Main) {
//                            commonUIUtility.dismissProgress()
//                            (fragment as BuyerDashboardFragment).bindingApprovedPCACount(approvedPCAList)
//                        }
//                    } else if (fragment is ProfileFragment) {
//                        withContext(Main) {
//                            commonUIUtility.dismissProgress()
//                            Log.d(TAG, "getApprovedPCAList: PCA_DATA : $data")
//                            (fragment as ProfileFragment).bindPCAData(data)
//                        }
//                    }
                    handleResultInFragment(approvedPCAList,unapprovedPCAList,data)
                } else {
                    activity.runOnUiThread {
                        commonUIUtility.dismissProgress()
                    }
                    Log.e(TAG, "getApprovedPCAList: ${result.errorBody()}")
                }

            }

        } catch (e: Exception) {
            commonUIUtility.dismissProgress()
            e.printStackTrace()
            Log.e(TAG, "getApprovedPCAList: ${e.message}")
        }
    }


    private suspend fun handleResultInFragment(
        approvedPCAList: ArrayList<PCAListModelItem>,
        unapprovedPCAList: ArrayList<PCAListModelItem>,
        data: PCAListModelItem?
    ) {
        withContext(Dispatchers.Main) {
            commonUIUtility.dismissProgress()
            when (fragment) {
                is PCAListFragment -> {
                    (fragment as PCAListFragment).bindApprovedPCAListRecyclerView(approvedPCAList)
                    (fragment as PCAListFragment).bindUnapprovedPCAListRecyclerView(unapprovedPCAList)
                }
                is BuyerDashboardFragment -> {
                    (fragment as BuyerDashboardFragment).bindingApprovedPCACount(approvedPCAList)
                }
                is ProfileFragment -> {
                    Log.d(TAG, "handleResultInFragment: PCA_DATA : $data")
                    (fragment as ProfileFragment).bindPCAData(data)
                }
            }
        }
    }

}