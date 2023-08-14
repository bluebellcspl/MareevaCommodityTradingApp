package com.bluebellcspl.maarevacommoditytradingapp.dashboardHelper

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentDashboardBinding

class PCADashboard(var context: Context, var activity: Activity, var binding: FragmentDashboardBinding) {
    private val commonUIUtility = CommonUIUtility(context)
    private val TAG = "PCADashboard"
    init {
        bindPCADashboardComponent()
    }

    private fun bindPCADashboardComponent() {
        try {
            binding.pcaDashboard.cvNotificationPCA.visibility = View.VISIBLE
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "bindPCADashboardComponent: ${e.message}", )
        }
    }

}