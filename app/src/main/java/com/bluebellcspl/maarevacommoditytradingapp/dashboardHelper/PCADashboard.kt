package com.bluebellcspl.maarevacommoditytradingapp.dashboardHelper

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.databinding.PcaDashboardLayoutBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.DashboardFragment

class PCADashboard(var context: Context, var activity: Activity,var fragment:DashboardFragment,var lifecycleOwner: LifecycleOwner) {
    private val commonUIUtility = CommonUIUtility(context)
    private val TAG = "PCADashboard"
    lateinit var binding:PcaDashboardLayoutBinding
    init {
        bindPCADashboardComponent()
    }

    private fun bindPCADashboardComponent() {
        try {
//            FetchShopMasterAPI(context, activity)
//            binding = fragment.binding.pcaDashboard
//            binding.cvAuctionPCA.setOnClickListener {
//                fragment.navController.navigate(DashboardFragmentDirections.actionDashboardFragmentToPCAAuctionFragment("Hello"))
//            }

        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "bindPCADashboardComponent: ${e.message}", )
        }
    }

}