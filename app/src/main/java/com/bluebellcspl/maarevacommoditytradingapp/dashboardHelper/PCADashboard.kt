package com.bluebellcspl.maarevacommoditytradingapp.dashboardHelper

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentDashboardBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.PcaDashboardLayoutBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.DashboardFragment
import com.bluebellcspl.maarevacommoditytradingapp.fragment.DashboardFragmentDirections
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchShopMasterAPI

class PCADashboard(var context: Context, var activity: Activity,var fragment:DashboardFragment) {
    private val commonUIUtility = CommonUIUtility(context)
    private val TAG = "PCADashboard"
    lateinit var binding:PcaDashboardLayoutBinding
    init {
        bindPCADashboardComponent()
    }

    private fun bindPCADashboardComponent() {
        try {
            FetchShopMasterAPI(activity,activity)
            binding = fragment.binding.pcaDashboard
            binding.cvAuctionPCA.setOnClickListener {
                fragment.navController.navigate(DashboardFragmentDirections.actionDashboardFragmentToPCAAuctionFragment("Hello"))
            }

        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "bindPCADashboardComponent: ${e.message}", )
        }
    }

}