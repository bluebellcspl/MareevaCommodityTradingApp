package com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer

import ConnectionCheck
import android.icu.text.NumberFormat
import android.icu.text.SimpleDateFormat
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.constants.Constants
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentBuyerDashboardBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchApprovedPCAListAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchBuyerAuctionDetailAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchBuyerPreviousAuctionAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchCityMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchCommodityMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchTransportationMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.BuyerAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.BuyerPrevAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionPCAListModel
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAListModelItem
import com.bluebellcspl.maarevacommoditytradingapp.webSocketHelper.WebSocketClient
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.Locale


class BuyerDashboardFragment : Fragment() {
    lateinit var binding: FragmentBuyerDashboardBinding
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val navController by lazy { findNavController() }
    val TAG = "BuyerDashboardFragment"
    var newAuctionData: LiveAuctionMasterModel? = null
    var lastPCAList: ArrayList<LiveAuctionPCAListModel> = ArrayList()
    private lateinit var webSocketClient: WebSocketClient
    private var isWebSocketConnected = false
    lateinit var menuHost: MenuHost
    var COMMODITY_BHARTI = ""
    var PREV_AUCTION_SELECTED_DATE = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_buyer_dashboard, container, false)
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        binding.tvCommodityNewBuyerDashboardFragment.setText(
            PrefUtil.getString(
                PrefUtil.KEY_COMMODITY_NAME,
                ""
            ).toString()
        )
        binding.tvDateNewBuyerDashboardFragment.setText(DateUtility().getCompletionDate())
        var commodityId = PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID, "")
        var companyCode = PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE, "")
        var buyerRegId = PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "")
        COMMODITY_BHARTI =
            DatabaseManager.ExecuteScalar(Query.getCommodityBhartiByCommodityId(commodityId.toString()))!!
        if (ConnectionCheck.isConnected(requireContext()))
        {
            val approvedListJOB = lifecycleScope.async {
                FetchApprovedPCAListAPI(requireContext(), requireActivity(), this@BuyerDashboardFragment)
            }
            val cityMasterJOB = lifecycleScope.async {

                FetchCityMasterAPI(requireContext(), requireActivity())
            }
            val transportationMasterJOB = lifecycleScope.async {
                FetchTransportationMasterAPI(requireContext(), requireActivity())
            }
            val commodityMasterJOB = lifecycleScope.async {

                FetchCommodityMasterAPI(requireContext(), requireActivity())
            }
            val buyerAuctionFetchJOB = lifecycleScope.async {

                FetchBuyerAuctionDetailAPI(requireContext(), requireActivity(), this@BuyerDashboardFragment)
            }
            val buyerPreviousAuctionJOB = lifecycleScope.async {
//        FetchBuyerPreviousAuctionAPI(requireContext(), this@BuyerDashboardFragment, PREV_AUCTION_SELECTED_DATE)
            }

            lifecycleScope.launch(Dispatchers.IO){
                approvedListJOB.await()
                cityMasterJOB.await()
                transportationMasterJOB.await()
                commodityMasterJOB.await()
                buyerAuctionFetchJOB.await()
            }

        }else
        {
            commonUIUtility.showToast(getString(R.string.no_internet_connection))
        }


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
                menuInflater.inflate(R.menu.ds_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.btn_Profile -> {
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

            binding.btnDatePickerNewBuyerDashboardFragment.setOnClickListener {
                showDatePickerDialog()
            }

            binding.cvPreviousAuctionNewBuyerDashboardFragment.setOnClickListener {
                navController.navigate(
                    BuyerDashboardFragmentDirections.actionBuyerDashboardFragmentToBuyerPreviousAuctionFragment(
                        PREV_AUCTION_SELECTED_DATE
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "setOnclickListeners: ${e.message}")
        }
    }

    fun bindingApprovedPCACount(dataList: ArrayList<PCAListModelItem>) {
        try {
//                binding.buyerDashboard.tvPCACountBuyer.setText(dataList.size.toString())
            binding.tvPCACountNewBuyerDashboardFragment.setText(
                String.format(
                    "%s %d",
                    requireContext().getString(R.string.total_pcas_lbl_new),
                    dataList.size
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindingApprovedPCACount: ${e.message}")
        }
    }

    fun bindBuyerAllocatedData(buyerData: BuyerAuctionMasterModel) {
        try {
            Log.d(TAG, "bindBuyerAllocatedData: ALLOCATED_TOTAL_COST : ${buyerData.TotalCost}")
            Log.d(TAG, "bindBuyerAllocatedData: ALLOCATED_COMMODITY_BHARTI : ${COMMODITY_BHARTI}")
            if (buyerData.TotalCost.isNotEmpty() && COMMODITY_BHARTI.isNotEmpty() && !COMMODITY_BHARTI.contains("invalid")) {
                binding.tvAllocatedBagsNewBuyerDashboardFragment.setText(
                    "%s %s".format(
                        requireContext().getString(
                            R.string.bags_lbl
                        ), buyerData.AllocatedBags
                    )
                )
                var rate =
                    buyerData.TotalCost.toDouble() / ((buyerData.AllocatedBags.toDouble() * COMMODITY_BHARTI.toDouble()) / 20.0)
                val BuyerRateNF = NumberFormat.getCurrencyInstance().format(rate).substring(1)
                Log.d(TAG, "bindBuyerAllocatedData: ALLOCATED_RATE : $BuyerRateNF")
                binding.tvAllocatedRateNewBuyerDashboardFragment.setText(
                    "%s %s".format(
                        requireContext().getString(
                            R.string.rate_lbl
                        ), BuyerRateNF
                    )
                )
                val BuyerTotalAmountNF =
                    NumberFormat.getCurrencyInstance().format(buyerData.TotalCost.toDouble())
                        .substring(1)
                binding.tvAllocatedTotalCostNewBuyerDashboardFragment.setText(
                    "%s %s".format(
                        requireContext().getString(R.string.cost_lbl),
                        BuyerTotalAmountNF
                    )
                )
            }
        } catch (e: Exception) {
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
        } catch (e: Exception) {
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
            var TOTAL_AuctionBags = 0


            for (PCAData in dataList.PCAList) {
                var currentPCABasic = 0.0
                var CURRENT_pcaMarketCess = 0.0
                var CURRENT_pcaCommCharge = 0.0
                var CURRENT_gcaCommCharge = 0.0
                var CURRENT_pcaTransportationCharge = 0.0
                var CURRENT_pcaLabourCharge = 0.0
                var CURRENT_Shop_Amount = 0.0
                var CURRENT_TOTAL_COST = 0.0
                var CURRENT_pcaExpense = 0.0
                for (ShopData in PCAData.ShopList) {
                    currentPCABasic += ShopData.Amount.toDouble()
                    var SHOP_CURRENT_PRICE = ShopData.CurrentPrice.toDouble()
                    var SHOP_CURRENT_BAGS = ShopData.Bags.toInt()

                    var pcaMarketCess =
                        (((SHOP_CURRENT_BAGS * PCAData.CommodityBhartiPrice.toDouble()) / 20) * (SHOP_CURRENT_PRICE) * PCAData.MarketCessCharge.toDouble()) / 100.00
                    var pcaCommCharge =
                        (((SHOP_CURRENT_BAGS * PCAData.CommodityBhartiPrice.toDouble()) / 20) * (SHOP_CURRENT_PRICE) * PCAData.PCACommCharge.toDouble()) / 100.00
                    var gcaCommCharge =
                        (((SHOP_CURRENT_BAGS * PCAData.CommodityBhartiPrice.toDouble()) / 20) * (SHOP_CURRENT_PRICE) * PCAData.GCACommCharge.toDouble()) / 100.00
                    if (PCAData.TransportationCharge.isEmpty()) {
                        PCAData.TransportationCharge = "0"
                    }
                    if (PCAData.LabourCharge.isEmpty()) {
                        PCAData.LabourCharge = "0"
                    }
                    var pcaLabourCharge = SHOP_CURRENT_BAGS * PCAData.LabourCharge.toDouble()
                    var pcaTransportationCharge =
                        SHOP_CURRENT_BAGS * PCAData.TransportationCharge.toDouble()
                    var amount =
                        ((SHOP_CURRENT_BAGS * PCAData.CommodityBhartiPrice.toDouble()) / 20) * SHOP_CURRENT_PRICE
                    var expense =
                        pcaCommCharge + gcaCommCharge + pcaMarketCess + pcaLabourCharge + pcaTransportationCharge
                    CURRENT_Shop_Amount += amount
                    CURRENT_pcaCommCharge += pcaCommCharge
                    CURRENT_gcaCommCharge += gcaCommCharge
                    CURRENT_pcaMarketCess += pcaMarketCess
                    CURRENT_pcaLabourCharge += pcaLabourCharge
                    CURRENT_pcaTransportationCharge += pcaTransportationCharge

                    CURRENT_pcaExpense += expense
                    CURRENT_TOTAL_COST += amount + expense
                }


                TOTAL_pcaMarketCess += CURRENT_pcaMarketCess
                TOTAL_pcaCommCharge += CURRENT_pcaCommCharge
                TOTAL_gcaCommCharge += CURRENT_gcaCommCharge
                TOTAL_pcaTransportationCharge += CURRENT_pcaTransportationCharge
                TOTAL_pcaLabourCharge += CURRENT_pcaLabourCharge
                TOTAL_AuctionBags += PCAData.TotalPurchasedBags.toInt()
                TOTAL_AuctionCost += CURRENT_TOTAL_COST
                TOTAL_pcaExpense += CURRENT_pcaExpense
            }
            Log.d(TAG, "calculateExpenses: TOTAL_EXPENSE_PCA : $TOTAL_pcaExpense")
            Log.d(TAG, "calculateExpenses: TOTAL_PCA_BASIC   : $TOTAL_pcaBasic")

            binding.tvPurchasedBagsNewBuyerDashboardFragment.setText(
                "%s %s".format(
                    requireContext().getString(
                        R.string.bags_lbl
                    ), TOTAL_AuctionBags.toString()
                )
            )

            val PCATotalAmountNF =
                NumberFormat.getCurrencyInstance().format(TOTAL_AuctionCost).substring(1)
            binding.tvPurchasedTotalCostNewBuyerDashboardFragment.setText(
                "%s %s".format(
                    requireContext().getString(R.string.cost_lbl),
                    PCATotalAmountNF
                )
            )
            var rate =
                TOTAL_AuctionCost / ((TOTAL_AuctionBags * COMMODITY_BHARTI.toDouble()) / 20.0)
            val RateNF = NumberFormat.getCurrencyInstance().format(rate).substring(1)
            binding.tvPurchasedAvgRateNewBuyerDashboardFragment.setText(
                "%s %s".format(
                    requireContext().getString(R.string.rate_lbl),
                    RateNF
                )
            )

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "calculateExpenses: ${e.message}")
        }
    }

    private fun showDatePickerDialog() {
        val calendarConstraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.before(System.currentTimeMillis() + Constants.OneDayInMillies))
            .build()
        val builder =
            MaterialDatePicker.Builder.datePicker().setCalendarConstraints(calendarConstraints)

        val datePicker = builder.build()
        datePicker.addOnPositiveButtonClickListener {
            // Handle the selected date
            val selectedDateInMillis = it
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            val date = dateFormat.format(selectedDateInMillis)
            PREV_AUCTION_SELECTED_DATE = date
            FetchBuyerPreviousAuctionAPI(
                requireContext(),
                this@BuyerDashboardFragment,
                PREV_AUCTION_SELECTED_DATE
            )
        }
        if (!datePicker.isAdded) {
            datePicker.show(childFragmentManager, datePicker.toString())
        }
    }

    fun bindPreviousAuctionData(modelData: BuyerPrevAuctionMasterModel) {
        try {
            binding.tvPreviousAuctionDateNewBuyerDashboardFragment.setText(
                "%s %s".format(
                    resources.getString(
                        R.string.date_lbl
                    ), modelData.Date
                )
            )
            binding.tvPreviousAuctionAvgRateNewBuyerDashboardFragment.setText(
                "%s %s".format(
                    resources.getString(R.string.avg_rate_lbl),
                    modelData.LastPCATotalAvgRate
                )
            )
            binding.tvPreviousAuctionPurchasedBagsNewBuyerDashboardFragment.setText(
                "%s %s".format(
                    resources.getString(R.string.bags_lbl),
                    modelData.LastTotalPurchasedBags
                )
            )
            binding.tvPreviousAuctionTotalCostNewBuyerDashboardFragment.setText(
                "%s %s".format(
                    resources.getString(R.string.total_cost_lbl),
                    modelData.LastPCATotalCost
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindPreviousAuctionData: ${e.message}")
        }
    }


    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        if (!isWebSocketConnected) {
            Log.d(TAG, "onResume: WEB_SOCKET_CONNECT onResume")
            if (ConnectionCheck.isConnected(requireContext())){
                webSocketClient.connect()
                isWebSocketConnected = true
            }else{
                commonUIUtility.showToast(getString(R.string.no_internet_connection))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isWebSocketConnected) {
            Log.d(TAG, "onStart: WEB_SOCKET_CONNECT onStart")
            if (ConnectionCheck.isConnected(requireContext())){
                webSocketClient.connect()
                isWebSocketConnected = true
            }else{
                commonUIUtility.showToast(getString(R.string.no_internet_connection))
            }
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        if (isWebSocketConnected) {
            Log.d(TAG, "onStop: WEB_SOCKET_DISCONNECT onStop")
            webSocketClient.disconnect()
            isWebSocketConnected = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Disconnect the WebSocket in onDestroy to ensure proper cleanup
        newAuctionData = null
        lastPCAList = ArrayList()
        if (isWebSocketConnected) {
            Log.d(TAG, "onDestroy: WEB_SOCKET_DISCONNECT onDestroy")
            webSocketClient.disconnect()
            isWebSocketConnected = false
        }
    }

    // ... (other methods)

    // Ensure the WebSocket is disconnected when the fragment is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        newAuctionData = null
        lastPCAList = ArrayList()
        if (isWebSocketConnected) {
            Log.d(TAG, "onDestroyView: WEB_SOCKET_DISCONNECT onDestroyView")
            webSocketClient.disconnect()
            isWebSocketConnected = false
        }
    }
}