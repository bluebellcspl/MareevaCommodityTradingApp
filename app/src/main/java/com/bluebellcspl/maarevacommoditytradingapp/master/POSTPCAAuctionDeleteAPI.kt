package com.bluebellcspl.maarevacommoditytradingapp.master

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.adapter.PCAAuctionListAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.PCAAuctionListFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.ApiPCAAuctionDetail
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class POSTPCAAuctionDeleteAPI(
    var context: Context,
    var activity: Activity,
    var fragment: Fragment,
    var pcaAuctionList: ArrayList<ApiPCAAuctionDetail>,
    var position: Int,
    var adapter: PCAAuctionListAdapter
) {
    val job = Job()
    val scope = CoroutineScope(job)
    val commonUIUtility = CommonUIUtility(context)
    val TAG = "POSTPCAAuctionDeleteAPI"

    init {
        deletePCAData()
    }

    private fun deletePCAData() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject()
            var pcaAuctionDetailId = pcaAuctionList[position].PCAAuctionDetailId
            var pcaAuctionMasterId = pcaAuctionList[position].PCAAuctionMasterId
            JO.addProperty("RegId", PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString())
            JO.addProperty(
                "CompanyCode",
                PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE, "").toString()
            )
            JO.addProperty("PCAAuctionDetailId", pcaAuctionDetailId)
            JO.addProperty("PCAAuctionMasterId", pcaAuctionMasterId)

            Log.d(TAG, "deletePCAData: PCA_DELETE_DATA_JSON : $JO")

            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            scope.launch(Dispatchers.Main)
            {
                val result = APICall.postPCAAuctionDataDelete(JO)
                if (result.isSuccessful) {
                    val responseJO = result.body()!!
                    if (responseJO.get("Message").asString.contains("Shop Details Deleted Successfully")) {
                        withContext(Dispatchers.Main)
                        {
                            commonUIUtility.dismissProgress()
                            commonUIUtility.showToast("Shop Data Deleted Successfully!")
                            adapter.notifyItemRemoved(position)
                            pcaAuctionList.removeAt(position)
                            (fragment as PCAAuctionListFragment).binding.rcViewPCAAuctionListFrament.invalidate()
                        }
                    } else {
                        withContext(Dispatchers.Main)
                        {
                            commonUIUtility.dismissProgress()
                            commonUIUtility.showToast("Shop Data NOT Deleted!")
                            adapter.notifyDataSetChanged()
                            (fragment as PCAAuctionListFragment).binding.rcViewPCAAuctionListFrament.invalidate()
                        }
                    }
                } else {
                    Log.e(TAG, "deletePCAData: ${result.errorBody()}")
                    withContext(Dispatchers.Main) {
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast("Data NOT Deleted!")
                        if (fragment is PCAAuctionListFragment) {
                            (fragment as PCAAuctionListFragment).binding.rcViewPCAAuctionListFrament.invalidate()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast("Please Try Again Later!")
            e.printStackTrace()
            Log.e(TAG, "deletePCAData: ${e.message}")
        }
    }
}