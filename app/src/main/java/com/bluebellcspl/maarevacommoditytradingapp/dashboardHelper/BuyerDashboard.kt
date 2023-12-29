package com.bluebellcspl.maarevacommoditytradingapp.dashboardHelper

import android.app.Activity
import android.content.Context
import android.icu.text.NumberFormat
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.BuyerDashboardLayoutBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.DashboardFragment
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchAPMCMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.BuyerAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionPCAListModel
import com.bluebellcspl.maarevacommoditytradingapp.webSocketHelper.WebSocketClient

class BuyerDashboard(var context: Context,var activity: Activity,var fragment: DashboardFragment,var lifecycleOwner: LifecycleOwner){
    private val commonUIUtility = CommonUIUtility(context)
    private val TAG = "BuyerDashboard"
    lateinit var binding:BuyerDashboardLayoutBinding
    var newAuctionData: LiveAuctionMasterModel? = null
    var lastPCAList: ArrayList<LiveAuctionPCAListModel> = ArrayList()
    private lateinit var webSocketClient: WebSocketClient
    var COMMODITY_BHARTI=""
    init {
//        bindBuyerDashboardComponent()
    }

    private fun bindBuyerDashboardComponent() {
        try {
            binding = fragment.binding.buyerDashboard
            FetchAPMCMasterAPI(context,activity)
//            binding.tvCommodityBuyer.setText(PrefUtil.getString(PrefUtil.KEY_COMMODITY_NAME,"").toString())
            //WebSocket Connection for LiveAuction Data
            var commodityId = PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID, "")
            var companyCode = PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE, "")
            var buyerRegId = PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "")
            COMMODITY_BHARTI =
                DatabaseManager.ExecuteScalar(Query.getCommodityBhartiByCommodityId(commodityId.toString()))!!
            webSocketClient = WebSocketClient(
                context,
                "ws://maarevaapi.bbcspldev.in/MaarevaApi/MaarevaApi/BuyersLiveAuctionRtr?CommodityId=$commodityId&Date=${DateUtility().getCompletionDate()}&CompanyCode=$companyCode&BuyerRegId=$buyerRegId",
                lifecycleOwner,
                ::onMessageReceived
            )

//            webSocketClient.connect()
//            binding.tvCommodityNewBuyer.setText(PrefUtil.getString(PrefUtil.KEY_COMMODITY_NAME,"").toString())
//            binding.tvDateNewBuyer.setText(DateUtility().getCompletionDate())
//            binding.cvAddPCABuyer.setOnClickListener {
//                fragment.navController.navigate(DashboardFragmentDirections.actionDashboardFragmentToAddPCAFragment())
//            }
//            binding.fabAddPCABuyer.setOnClickListener {
//                fragment.navController.navigate(DashboardFragmentDirections.actionDashboardFragmentToAddPCAFragment())
//            }
//
//            binding.cvPCAListBuyer.setOnClickListener {
//                fragment.navController.navigate(DashboardFragmentDirections.actionDashboardFragmentToPCAListFragment())
//            }
//
//            binding.cvAuctionBuyer.setOnClickListener {
//                fragment.navController.navigate(DashboardFragmentDirections.actionDashboardFragmentToBuyerAuctionFragment())
//            }
//
//            binding.cvLiveAuctionBuyer.setOnClickListener {
//                fragment.navController.navigate(DashboardFragmentDirections.actionDashboardFragmentToLiveAuctionFragment())
//            }
//
//            //new Dashboard Redesign Binding
//            binding.cvPCAListNewBuyer.setOnClickListener {
//                fragment.navController.navigate(DashboardFragmentDirections.actionDashboardFragmentToPCAListFragment())
//            }
//            binding.fabAddPCANewBuyer.setOnClickListener {
//                fragment.navController.navigate(DashboardFragmentDirections.actionDashboardFragmentToAddPCAFragment())
//            }
//            binding.cvBuyerAuctionNewBuyer.setOnClickListener {
//                fragment.navController.navigate(DashboardFragmentDirections.actionDashboardFragmentToBuyerAuctionFragment())
//            }
//            binding.btnAllocatedBagsNewBuyer.setOnClickListener {
//                fragment.navController.navigate(DashboardFragmentDirections.actionDashboardFragmentToBuyerAuctionFragment())
//            }
//            binding.cvLiveAuctionNewBuyer.setOnClickListener {
//                fragment.navController.navigate(DashboardFragmentDirections.actionDashboardFragmentToLiveAuctionFragment())
//            }
//            binding.btnPurchasedBagsNewBuyer.setOnClickListener {
//                fragment.navController.navigate(DashboardFragmentDirections.actionDashboardFragmentToLiveAuctionFragment())
//            }
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.d(TAG, "bindBuyerDashboardComponent: ${e.message}")
        }
    }

    fun onMessageReceived(dataList: LiveAuctionMasterModel) {
        try {
            if (dataList.PCAList.isNotEmpty()) {

                if (dataList.PCAList != lastPCAList) {
                    Log.d(TAG, "onMessageReceived: LAST_PCA_LIST : $lastPCAList")
                    Log.d(TAG, "onMessageReceived: CURRENT_PCA_LIST : $dataList")
                    lastPCAList = dataList.PCAList
                    newAuctionData = dataList
                    calculateExpenses(dataList)
                }
            }
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "onMessageReceived: ${e.message}")
        }
    }

    fun calculateExpenses(dataList: LiveAuctionMasterModel) {
        try {
            //PCAs Calculations
            var pcaBasic = 0.0
            var pcaExpense = 0.0
            var pcaTotalAmount = 0.0
            var pcaTotalPurchasedBags = 0
//            var pcaMarketCess = 0.0
//            var pcaCommCharge = 0.0
//            var gcaCommCharge = 0.0
//            var pcaTransportationCharge = 0.0
//            var pcaLabourCharge = 0.0
            for (PCAData in dataList.PCAList) {
                for (ShopData in PCAData.ShopList) {
                    pcaBasic += ShopData.Amount.toDouble()
                }
                pcaTotalPurchasedBags += PCAData.TotalPurchasedBags.toInt()
                pcaExpense += PCAData.PCACommCharge.toDouble() + PCAData.GCACommCharge.toDouble() + PCAData.TransportationCharge.toDouble() + PCAData.LabourCharge.toDouble() + PCAData.MarketCessCharge.toDouble()
            }
            pcaTotalAmount = pcaBasic + pcaExpense
            binding.tvPurchasedBagsNewBuyer.setText("%s %s".format(context.getString(R.string.bags_lbl),pcaTotalPurchasedBags.toString()))
//            binding.edtPCAPurchasedBagsLiveAuctionFragment.setText(String.format("%s",pcaTotalPurchasedBags.toString()))


            val PCATotalAmountNF = NumberFormat.getCurrencyInstance().format(pcaTotalAmount).substring(1)
            binding.tvPurchasedTotalCostNewBuyer.setText("%s %s".format(context.getString(R.string.cost_lbl),PCATotalAmountNF))
            var rate = pcaBasic / ((pcaTotalPurchasedBags * COMMODITY_BHARTI.toDouble()) / 20.0)
            val RateNF = NumberFormat.getCurrencyInstance().format(rate).substring(1)
            binding.tvPurchasedAvgRateNewBuyer.setText("%s %s".format(context.getString(R.string.rate_lbl),RateNF))
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "calculateExpenses: ${e.message}")
        }
    }

    fun bindBuyerAllocatedData(buyerData:BuyerAuctionMasterModel)
    {
        try {
            binding.tvAllocatedBagsNewBuyer.setText("%s %s".format(context.getString(R.string.bags_lbl),buyerData.AllocatedBags))
            binding.tvAllocatedRateNewBuyer.setText("%s".format(context.getString(R.string.rate_lbl)))
            binding.tvAllocatedTotalCostNewBuyer.setText("%s %s".format(context.getString(R.string.cost_lbl),buyerData.TotalCost))
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "bindBuyerAllocatedData: ${e.message}")
        }
    }

}