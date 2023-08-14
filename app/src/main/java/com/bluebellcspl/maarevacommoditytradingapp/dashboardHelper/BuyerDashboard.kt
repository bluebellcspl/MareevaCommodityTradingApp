package com.bluebellcspl.maarevacommoditytradingapp.dashboardHelper

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentDashboardBinding

class BuyerDashboard(var context: Context,var activity: Activity,var binding:FragmentDashboardBinding) {
    private val commonUIUtility = CommonUIUtility(context)
    private val TAG = "BuyerDashboard"
    init {
        bindBuyerDashboardComponent()
    }

    private fun bindBuyerDashboardComponent() {
        try {
            binding.buyerDashboard.cvNotificationBuyer.visibility = View.VISIBLE
            commonUIUtility.showProgress()
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.d(TAG, "bindBuyerDashboardComponent: ${e.message}")
        }
    }
}