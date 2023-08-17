package com.bluebellcspl.maarevacommoditytradingapp.dashboardHelper

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentDashboardBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.DashboardFragment
import com.bluebellcspl.maarevacommoditytradingapp.fragment.DashboardFragmentDirections

class BuyerDashboard(var context: Context,var activity: Activity,var fragment: DashboardFragment) {
    private val commonUIUtility = CommonUIUtility(context)
    private val TAG = "BuyerDashboard"
    init {
        bindBuyerDashboardComponent()
    }

    private fun bindBuyerDashboardComponent() {
        try {
            fragment.binding.buyerDashboard.cvNotificationBuyer.visibility = View.VISIBLE
            fragment.binding.buyerDashboard.cvAddPCABuyer.setOnClickListener {
                fragment.navController.navigate(DashboardFragmentDirections.actionDashboardFragmentToAddPCAFragment())
            }
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.d(TAG, "bindBuyerDashboardComponent: ${e.message}")
        }
    }
}