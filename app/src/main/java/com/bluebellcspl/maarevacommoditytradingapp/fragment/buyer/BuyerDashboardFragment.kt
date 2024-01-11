package com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer

import android.icu.text.NumberFormat
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentBuyerDashboardBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchApprovedPCAListAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchBuyerAuctionDetailAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchCityMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchCommodityMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchTransportationMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.BuyerAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionPCAListModel
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAListModelItem
import com.bluebellcspl.maarevacommoditytradingapp.webSocketHelper.WebSocketClient
import okhttp3.WebSocketListener


class BuyerDashboardFragment : Fragment() {
    lateinit var binding:FragmentBuyerDashboardBinding
    private val commonUIUtility by lazy { CommonUIUtility(requireContext())}
    private val navController by lazy { findNavController() }
    val TAG = "BuyerDashboardFragment"
    var newAuctionData: LiveAuctionMasterModel? = null
    var lastPCAList: ArrayList<LiveAuctionPCAListModel> = ArrayList()
    private lateinit var webSocketClient: WebSocketClient
    private var isWebSocketConnected = false
    lateinit var menuHost: MenuHost
    var COMMODITY_BHARTI=""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_buyer_dashboard, container, false)
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        binding.tvCommodityNewBuyerDashboardFragment.setText(PrefUtil.getString(PrefUtil.KEY_COMMODITY_NAME,"").toString())
        binding.tvDateNewBuyerDashboardFragment.setText(DateUtility().getCompletionDate())
        var commodityId = PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID, "")
        var companyCode = PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE, "")
        var buyerRegId = PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "")
        COMMODITY_BHARTI =
            DatabaseManager.ExecuteScalar(Query.getCommodityBhartiByCommodityId(commodityId.toString()))!!
        FetchApprovedPCAListAPI(requireContext(),requireActivity(),this)
        FetchBuyerAuctionDetailAPI(requireContext(), requireActivity(), this@BuyerDashboardFragment)
        FetchCityMasterAPI(requireContext(),requireActivity())
        FetchTransportationMasterAPI(requireContext(),requireActivity())
        FetchCommodityMasterAPI(requireContext(), requireActivity())

        webSocketClient = WebSocketClient(
            requireContext(),
            "ws://maarevaapi.bbcspldev.in/MaarevaApi/MaarevaApi/BuyersLiveAuctionRtr?CommodityId=$commodityId&Date=${DateUtility().getCompletionDate()}&CompanyCode=$companyCode&BuyerRegId=$buyerRegId",
            viewLifecycleOwner,
            ::onMessageReceived
        )

//        webSocketClient.connect()
        menuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.ds_menu,menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId){
                    R.id.btn_Profile->{
                        navController.navigate(BuyerDashboardFragmentDirections.actionBuyerDashboardFragmentToProfileOptionFragment())
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)
        setOnclickListeners()
        return binding.root
    }

    private fun setOnclickListeners() {
        try {
            binding.cvPCAListNewBuyerDashboardFragment.setOnClickListener {
                navController.navigate(BuyerDashboardFragmentDirections.actionBuyerDashboardFragmentToPCAListFragment())
            }
            binding.fabAddPCANewBuyerDashboardFragment.setOnClickListener {
                navController.navigate(BuyerDashboardFragmentDirections.actionBuyerDashboardFragmentToAddPCAFragment())
            }
            binding.cvBuyerAuctionNewBuyerDashboardFragment.setOnClickListener {
                navController.navigate(BuyerDashboardFragmentDirections.actionBuyerDashboardFragmentToBuyerAuctionFragment())
            }
            binding.btnAllocatedBagsNewBuyerDashboardFragment.setOnClickListener {
                navController.navigate(BuyerDashboardFragmentDirections.actionBuyerDashboardFragmentToBuyerAuctionFragment())
            }
            binding.cvLiveAuctionNewBuyerDashboardFragment.setOnClickListener {
                navController.navigate(BuyerDashboardFragmentDirections.actionBuyerDashboardFragmentToLiveAuctionFragment())
            }
            binding.btnPurchasedBagsNewBuyerDashboardFragment.setOnClickListener {
                navController.navigate(BuyerDashboardFragmentDirections.actionBuyerDashboardFragmentToLiveAuctionFragment())
            }
        }catch (e:Exception) {
            e.printStackTrace()
            Log.e(TAG, "setOnclickListeners: ${e.message}", )
        }
    }

    fun bindingApprovedPCACount(dataList:ArrayList<PCAListModelItem>){
        try {
//                binding.buyerDashboard.tvPCACountBuyer.setText(dataList.size.toString())
            binding.tvPCACountNewBuyerDashboardFragment.setText(String.format("%s %d",requireContext().getString(R.string.total_pcas_lbl_new),dataList.size))
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "bindingApprovedPCACount: ${e.message}", )
        }
    }

    fun bindBuyerAllocatedData(buyerData: BuyerAuctionMasterModel)
    {
        try {
            binding.tvAllocatedBagsNewBuyerDashboardFragment.setText("%s %s".format(requireContext().getString(R.string.bags_lbl),buyerData.AllocatedBags))
            var rate = buyerData.TotalCost.toDouble() / ((buyerData.AllocatedBags.toDouble() * COMMODITY_BHARTI.toDouble()) / 20.0)
            val BuyerRateNF = NumberFormat.getCurrencyInstance().format(rate).substring(1)
            binding.tvAllocatedRateNewBuyerDashboardFragment.setText("%s %s".format(requireContext().getString(R.string.rate_lbl),BuyerRateNF))
            val BuyerTotalAmountNF = NumberFormat.getCurrencyInstance().format(buyerData.TotalCost.toDouble()).substring(1)
            binding.tvAllocatedTotalCostNewBuyerDashboardFragment.setText("%s %s".format(requireContext().getString(R.string.cost_lbl),BuyerTotalAmountNF))
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "bindBuyerAllocatedData: ${e.message}")
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
            var TOTAL_pcaMarketCess = 0.0
            var TOTAL_pcaCommCharge = 0.0
            var TOTAL_gcaCommCharge = 0.0
            var TOTAL_pcaTransportationCharge = 0.0
            var TOTAL_pcaLabourCharge = 0.0
            var TOTAL_pcaExpense = 0.0
            var TOTAL_pcaBasic = 0.0
            var TOTAL_AuctionCost = 0.0
            var TOTAL_AuctionBags=0
            for (PCAData in dataList.PCAList) {
                var currentPCABasic = 0.0
                for (ShopData in PCAData.ShopList) {
                    currentPCABasic += ShopData.Amount.toDouble()
                }
                var pcaTotalPurchasedBags = PCAData.TotalPurchasedBags.toInt()
                var pcaMarketCess = (((pcaTotalPurchasedBags*PCAData.CommodityBhartiPrice.toDouble())/20)*((PCAData.BuyerUpperLimit.toDouble()+PCAData.BuyerLowerLimit.toDouble())/2)*PCAData.MarketCessCharge.toDouble())/100.00
                var pcaCommCharge= (((pcaTotalPurchasedBags*PCAData.CommodityBhartiPrice.toDouble())/20)*((PCAData.BuyerUpperLimit.toDouble()+PCAData.BuyerLowerLimit.toDouble())/2)*PCAData.PCACommCharge.toDouble())/100.00
                var gcaCommCharge = (((pcaTotalPurchasedBags*PCAData.CommodityBhartiPrice.toDouble())/20)*((PCAData.BuyerUpperLimit.toDouble()+PCAData.BuyerLowerLimit.toDouble())/2)*PCAData.GCACommCharge.toDouble())/100.00
                if (PCAData.TransportationCharge.isEmpty())
                {
                    PCAData.TransportationCharge = "0"
                }
                if (PCAData.LabourCharge.isEmpty())
                {
                    PCAData.LabourCharge = "0"
                }
                var pcaLabourCharge = pcaTotalPurchasedBags*PCAData.LabourCharge.toDouble()
                var pcaTransportationCharge = pcaTotalPurchasedBags*PCAData.TransportationCharge.toDouble()


                TOTAL_pcaMarketCess += pcaMarketCess
                TOTAL_pcaCommCharge += pcaCommCharge
                TOTAL_gcaCommCharge += gcaCommCharge
                TOTAL_pcaTransportationCharge += pcaTransportationCharge
                TOTAL_pcaLabourCharge += pcaLabourCharge
                TOTAL_pcaBasic += currentPCABasic
                TOTAL_AuctionBags+=pcaTotalPurchasedBags
            }
            TOTAL_pcaExpense = TOTAL_pcaCommCharge+TOTAL_gcaCommCharge+TOTAL_pcaLabourCharge+TOTAL_pcaMarketCess+TOTAL_pcaTransportationCharge
            TOTAL_AuctionCost = TOTAL_pcaBasic+TOTAL_pcaExpense
            Log.d(TAG, "calculateExpenses: TOTAL_EXPENSE_PCA : $TOTAL_pcaExpense")
            Log.d(TAG, "calculateExpenses: TOTAL_PCA_BASIC   : $TOTAL_pcaBasic")

            binding.tvPurchasedBagsNewBuyerDashboardFragment.setText("%s %s".format(requireContext().getString(R.string.bags_lbl),TOTAL_AuctionBags.toString()))

            val PCATotalAmountNF = NumberFormat.getCurrencyInstance().format(TOTAL_AuctionCost).substring(1)
            binding.tvPurchasedTotalCostNewBuyerDashboardFragment.setText("%s %s".format(requireContext().getString(R.string.cost_lbl),PCATotalAmountNF))
            var rate = TOTAL_AuctionCost / ((TOTAL_AuctionBags * COMMODITY_BHARTI.toDouble()) / 20.0)
            val RateNF = NumberFormat.getCurrencyInstance().format(rate).substring(1)
            binding.tvPurchasedAvgRateNewBuyerDashboardFragment.setText("%s %s".format(requireContext().getString(R.string.rate_lbl),RateNF))

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "calculateExpenses: ${e.message}")
        }
    }


    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(false)
//        webSocketClient.connect()
        if (!isWebSocketConnected) {
            webSocketClient.connect()
            isWebSocketConnected = true
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isWebSocketConnected) {
            webSocketClient.connect()
            isWebSocketConnected = true
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        if (isWebSocketConnected) {
            webSocketClient.disconnect()
            isWebSocketConnected = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Disconnect the WebSocket in onDestroy to ensure proper cleanup
        newAuctionData= null
        lastPCAList = ArrayList()
        if (isWebSocketConnected) {
            webSocketClient.disconnect()
            isWebSocketConnected = false
        }
    }

    // ... (other methods)

    // Ensure the WebSocket is disconnected when the fragment is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
         newAuctionData= null
         lastPCAList = ArrayList()
        if (isWebSocketConnected) {
            webSocketClient.disconnect()
            isWebSocketConnected = false
        }
    }
}