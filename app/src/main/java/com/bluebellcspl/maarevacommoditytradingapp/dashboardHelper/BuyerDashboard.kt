package com.bluebellcspl.maarevacommoditytradingapp.dashboardHelper

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.databinding.BuyerDashboardLayoutBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentDashboardBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.DashboardFragment
import com.bluebellcspl.maarevacommoditytradingapp.fragment.DashboardFragmentDirections

class BuyerDashboard(var context: Context,var activity: Activity,var fragment: DashboardFragment) {
    private val commonUIUtility = CommonUIUtility(context)
    private val TAG = "BuyerDashboard"
    lateinit var binding:BuyerDashboardLayoutBinding
    init {
        bindBuyerDashboardComponent()
    }

    private fun bindBuyerDashboardComponent() {
        try {
            binding = fragment.binding.buyerDashboard
            binding.tvCommodityBuyer.setText(PrefUtil.getString(PrefUtil.KEY_COMMODITY_NAME,"").toString())
            binding.cvAddPCABuyer.setOnClickListener {
                fragment.navController.navigate(DashboardFragmentDirections.actionDashboardFragmentToAddPCAFragment())
            }
            binding.fabAddPCABuyer.setOnClickListener {
                fragment.navController.navigate(DashboardFragmentDirections.actionDashboardFragmentToAddPCAFragment())
            }

            binding.cvPCAListBuyer.setOnClickListener {
                fragment.navController.navigate(DashboardFragmentDirections.actionDashboardFragmentToPCAListFragment())
            }

            binding.cvAuctionBuyer.setOnClickListener {
                fragment.navController.navigate(DashboardFragmentDirections.actionDashboardFragmentToBuyerAuctionFragment())
            }

        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.d(TAG, "bindBuyerDashboardComponent: ${e.message}")
        }
    }
}