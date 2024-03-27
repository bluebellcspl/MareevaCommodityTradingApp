package com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer

import ConnectionCheck
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.icu.text.NumberFormat
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.LoginActivity
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.constants.Constants
import com.bluebellcspl.maarevacommoditytradingapp.constants.URLHelper
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentBuyerDashboardBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchAPMCMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchApprovedPCAListAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchBuyerAuctionDetailAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchBuyerPreviousAuctionAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchCityMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchCommodityMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchNotificationAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchTransportationMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.BuyerAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.BuyerPrevAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionPCAListModel
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAListModelItem
import com.bluebellcspl.maarevacommoditytradingapp.webSocketHelper.SocketHandler
import com.bluebellcspl.maarevacommoditytradingapp.webSocketHelper.WebSocketClient
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.Locale


class BuyerDashboardFragment : Fragment() {
    private var isConnectingWebSocket = false
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
    private var webSocket: WebSocket? = null
    var commodityId = ""
    var companyCode = ""
    var buyerRegId = ""
    var NOTIFICATION_COUNT = 0
    lateinit var filter: IntentFilter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_buyer_dashboard, container, false)
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        Log.d(TAG, "onCreateView: CURRENT_SYSTEM_LANGUAGE : ${PrefUtil.getSystemLanguage().toString()}")
        if (PrefUtil.getSystemLanguage().toString().isNullOrEmpty())
        {
            PrefUtil.setSystemLanguage("en")
        }
        if (PrefUtil.getString(PrefUtil.KEY_LANGUAGE, "").equals("gu")) {
            binding.tvCommodityNewBuyerDashboardFragment.setText(
                DatabaseManager.ExecuteScalar(
                    Query.getGujaratiCommodityNameByCommodityId(
                        PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID, "").toString()
                    )
                )
            )
        } else {
            binding.tvCommodityNewBuyerDashboardFragment.setText(
                PrefUtil.getString(
                    PrefUtil.KEY_COMMODITY_NAME,
                    ""
                ).toString()
            )
        }
        binding.tvDateNewBuyerDashboardFragment.setText(DateUtility().getCompletionDate())
        commodityId = PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID, "").toString()
        companyCode = PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE, "").toString()
        buyerRegId = PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString()
        COMMODITY_BHARTI =
            DatabaseManager.ExecuteScalar(Query.getCommodityBhartiByCommodityId(commodityId.toString()))!!

        filter = IntentFilter("ACTION_NOTIFICATION_RECEIVED")
        fetchDataFromAPI()

        binding.swipeToRefreshBuyerDashboardFragment.setOnRefreshListener {
            binding.swipeToRefreshBuyerDashboardFragment.isRefreshing = false
            updateNotificationCount()
            fetchDataFromAPI()
        }
        menuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.ds_menu, menu)

                val notificationMenuItem = menu.findItem(R.id.nav_Notification)
//                val chatMenuItem = menu.findItem(R.id.btn_Chat)
//                chatMenuItem.setVisible(false)
                val invoiceMenuItem = menu.findItem(R.id.btn_Invoice)
                invoiceMenuItem.setVisible(false)
                if (NOTIFICATION_COUNT > 0) {
                    notificationMenuItem.setActionView(R.layout.notification_badge)
                    val view = notificationMenuItem.actionView
                    val badgeCounter = view?.findViewById<TextView>(R.id.tv_Notification_Badge)
                    badgeCounter?.setText(NOTIFICATION_COUNT.toString())
                    notificationMenuItem.actionView?.setOnClickListener {
                        navController.navigate(BuyerDashboardFragmentDirections.actionBuyerDashboardFragmentToNotificationFragment())
                    }
                } else {
                    notificationMenuItem.setActionView(null)
                }

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.btn_Profile -> {
                        navController.navigate(BuyerDashboardFragmentDirections.actionBuyerDashboardFragmentToProfileOptionFragment())
                    }
                    R.id.nav_Notification -> {
                        navController.navigate(BuyerDashboardFragmentDirections.actionBuyerDashboardFragmentToNotificationFragment())
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)
        updateNotificationCount()
        setOnclickListeners()
        return binding.root
    }

    private fun fetchDataFromAPI() {
        try {
            if (ConnectionCheck.isConnected(requireContext())) {
                FetchApprovedPCAListAPI(
                    requireContext(),
                    requireActivity(),
                    this@BuyerDashboardFragment
                )
                FetchCityMasterAPI(requireContext(), requireActivity())
                FetchBuyerAuctionDetailAPI(
                    requireContext(),
                    requireActivity(),
                    this@BuyerDashboardFragment
                )
                FetchBuyerPreviousAuctionAPI(
                    requireContext(),
                    this@BuyerDashboardFragment,
                    PREV_AUCTION_SELECTED_DATE
                )
            } else {
                commonUIUtility.showToast(getString(R.string.no_internet_connection))
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchDataFromAPI: ${e.message}")
            e.printStackTrace()
        }
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
            if (buyerData.TotalCost.isNotEmpty() && COMMODITY_BHARTI.isNotEmpty() && !COMMODITY_BHARTI.contains(
                    "invalid"
                )
            ) {
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
            if (dataList.AllocatedBag.isNotEmpty() && dataList.TotalCost.isNotEmpty() && dataList.Basic.isNotEmpty()) {
                if (dataList.PCAList.isNotEmpty()) {

                    if (dataList.PCAList != lastPCAList) {
                        Log.d(TAG, "onMessageReceived: LAST_PCA_LIST : $lastPCAList")
                        Log.d(TAG, "onMessageReceived: CURRENT_PCA_LIST : $dataList")
                        lastPCAList = dataList.PCAList
                        newAuctionData = dataList
                        calculateExpenses(dataList)
                    }
                }
            } else {
                disconnect()
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
            var TOTAL_AuctionBags = 0f

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
                    var SHOP_CURRENT_BAGS = ShopData.Bags.toFloat()

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
                TOTAL_AuctionBags += PCAData.TotalPurchasedBags.toFloat()
                TOTAL_AuctionCost += CURRENT_TOTAL_COST
                TOTAL_pcaExpense += CURRENT_pcaExpense
            }
            Log.d(TAG, "calculateExpenses: TOTAL_EXPENSE_PCA : $TOTAL_pcaExpense")
            Log.d(TAG, "calculateExpenses: TOTAL_PCA_BASIC   : $TOTAL_pcaBasic")

            if (dataList.TotalCost.isNotEmpty() && !COMMODITY_BHARTI.contains("invalid") && COMMODITY_BHARTI.isNotEmpty()) {

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

            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "calculateExpenses: ${e.message}")
        }
    }

    private fun showDatePickerDialog() {
        val calendarConstraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.before(System.currentTimeMillis() - Constants.OneDayInMillies))
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
            PREV_AUCTION_SELECTED_DATE = modelData.Date
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

        if (!isWebSocketConnected && !isConnectingWebSocket) {
            Log.d(TAG, "onResume: WEB_SOCKET_CONNECT onResume")

            // Set the flag to indicate that a connection attempt is in progress
            isConnectingWebSocket = true

            // Delay WebSocket connection by 3 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                if (ConnectionCheck.isConnected(requireContext())) {
//                    webSocketClient.connect()
                    val LIVE_SOCKET_API = URLHelper.TESTING_LIVE_AUCTION_SOCKET_URL.replace(
                        "<COMMODITY_ID>",
                        commodityId.toString()
                    ).replace("<DATE>", DateUtility().getCompletionDate())
                        .replace("<COMPANY_CODE>", companyCode.toString())
                        .replace("<BUYER_REG_ID>", buyerRegId.toString())
                    Log.d(TAG, "onResume: BUYER_LIVE_AUCTION_SOCKET_API : $LIVE_SOCKET_API")

                    lifecycleScope.launch(Dispatchers.IO)
                    {
                        webSocket = SocketHandler.getWebSocket(
                            LIVE_SOCKET_API,
                            MyWebSocketListener()
                        )
                    }
                    isWebSocketConnected = true
                } else {
                    commonUIUtility.showToast(getString(R.string.no_internet_connection))
                }

                // Reset the flag after the connection attempt
                isConnectingWebSocket = false
            }, 3000) // 3000 milliseconds = 3 seconds
        }
    }

    override fun onStart() {
        super.onStart()

//        commonUIUtility.dismissProgress()
        requireContext().registerReceiver(notificationReceiver, filter)
        if (!isWebSocketConnected && !isConnectingWebSocket) {
            Log.d(TAG, "onStart: WEB_SOCKET_CONNECT onStart")

            // Set the flag to indicate that a connection attempt is in progress
            isConnectingWebSocket = true

            // Delay WebSocket connection by 3 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                if (ConnectionCheck.isConnected(requireContext())) {
//                    webSocketClient.connect()
                    val LIVE_SOCKET_API = URLHelper.TESTING_LIVE_AUCTION_SOCKET_URL.replace(
                        "<COMMODITY_ID>",
                        commodityId.toString()
                    ).replace("<DATE>", DateUtility().getCompletionDate())
                        .replace("<COMPANY_CODE>", companyCode.toString())
                        .replace("<BUYER_REG_ID>", buyerRegId.toString())
                    Log.d(TAG, "onStart: BUYER_LIVE_AUCTION_SOCKET_API : $LIVE_SOCKET_API")

                    lifecycleScope.launch(Dispatchers.IO)
                    {
                        webSocket = SocketHandler.getWebSocket(
                            LIVE_SOCKET_API,
                            MyWebSocketListener()
                        )
                    }
                    isWebSocketConnected = true
                } else {
                    commonUIUtility.showToast(getString(R.string.no_internet_connection))
                }

                // Reset the flag after the connection attempt
                isConnectingWebSocket = false
            }, 2000)
        }
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(notificationReceiver)
        if (isWebSocketConnected) {
            Log.d(TAG, "onStop: WEB_SOCKET_DISCONNECT onStop")
//            webSocketClient.disconnect()
            disconnect()
            isWebSocketConnected = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Disconnect the WebSocket in onDestroy to ensure proper cleanup
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        newAuctionData = null
        lastPCAList = ArrayList()
        Log.d(TAG, "onDestroy: WEB_SOCKET_DISCONNECT onDestroy")
        disconnect()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        newAuctionData = null
        lastPCAList = ArrayList()
        Log.d(TAG, "onDestroyView: WEB_SOCKET_DISCONNECT onDestroyView")
        disconnect()
    }

    private inner class MyWebSocketListener : WebSocketListener() {
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            this@BuyerDashboardFragment.webSocket = null
            Log.d(TAG, "onClosed: SOCKET_CLOSED")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            this@BuyerDashboardFragment.webSocket = null
            t.printStackTrace()
            Log.e(TAG, "onFailure: ${t.message}")
            Log.e(TAG, "onFailure: FAILED_RESPONSE $response")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            //FOR RECYCLERVIEW
            val gson = Gson()
            val jsonObject: JsonObject = JsonParser().parse(text).asJsonObject
            Log.d(TAG, "onMessage: NEW_JSON_OBJECT : ${jsonObject.toString()}")
            val userListType = object : TypeToken<LiveAuctionMasterModel>() {}.type
            var liveAuctionObject: LiveAuctionMasterModel =
                gson.fromJson(jsonObject.toString(), userListType)
            lifecycleScope.launch(Dispatchers.Main) {
                onMessageReceived(liveAuctionObject)
            }
        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            this@BuyerDashboardFragment.webSocket = webSocket
            Log.d(TAG, "onOpen: SOCKET_CONNECTED")
        }

    }

    fun disconnect() {
        try {
            Log.d(TAG, "disconnect: SOCKET_DISCONNECTED")
//            webSocket?.close(1000, "Disconnect Socket")
            webSocket?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "disconnect: ${e.message}")
        }
    }

    fun updateNotificationCount() {
        try {

            NOTIFICATION_COUNT =
                DatabaseManager.ExecuteScalar(Query.getTMPTUnseenNotification())!!.toInt()
            Log.d(TAG, "updateNotificationCount: NOTIFICATION_COUNT : $NOTIFICATION_COUNT")
            requireActivity().runOnUiThread {
                menuHost.invalidateMenu()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "updateNotificationCount: ${e.message}")
        }
    }

    fun redirectToLogin(){
        try {
            PrefUtil.setBoolean(PrefUtil.KEY_LOGGEDIN,false)
            requireActivity().startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "redirectToLogin: ${e.message}", )
        }
    }

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateNotificationCount()
        }
    }
}