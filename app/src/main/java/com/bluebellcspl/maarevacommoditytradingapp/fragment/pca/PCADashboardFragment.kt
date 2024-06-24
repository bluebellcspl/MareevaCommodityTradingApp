package com.bluebellcspl.maarevacommoditytradingapp.fragment.pca

import ConnectionCheck
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.LoginActivity
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.constants.Constants
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentPCADashboardBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchCityMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchPCAAuctionDetailAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchPCAPreviousAuctionAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchShopMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAAuctionDetailModel
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAPrevAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.ShopMasterAPICallModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Locale


class PCADashboardFragment : Fragment() {
    var _binding: FragmentPCADashboardBinding?=null
    val binding get() = _binding!!
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val navController by lazy { findNavController() }
    val TAG = "PCADashboardFragment"
    lateinit var menuHost: MenuHost
    var COMMODITY_BHARTI = ""
    var PREV_AUCTION_SELECTED_DATE = ""
    var NOTIFICATION_COUNT = 0
    var CHAT_NOTIFICATION_COUNT=0
    lateinit var filter: IntentFilter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_p_c_a_dashboard, container, false)
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        filter = IntentFilter("ACTION_NOTIFICATION_RECEIVED")
        Log.d(TAG, "onCreateView: CURRENT_SYSTEM_LANGUAGE : ${PrefUtil.getSystemLanguage().toString()}")
        if (PrefUtil.getSystemLanguage().toString().isNullOrEmpty())
        {
            PrefUtil.setSystemLanguage("en")
        }
        fetchDataFromAPI()
        binding.swipeToRefreshPCADashboardFragment.setOnRefreshListener {
            binding.swipeToRefreshPCADashboardFragment.isRefreshing = false
            updateNotificationCount()
            fetchDataFromAPI()
        }
        menuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.ds_menu, menu)

                val notificationMenuItem = menu.findItem(R.id.nav_Notification)
                val chatMenuItem = menu.findItem(R.id.btn_Chat)
                chatMenuItem.setVisible(true)
                val invoiceMenuItem = menu.findItem(R.id.btn_Invoice)
                invoiceMenuItem.setVisible(true)
                if (NOTIFICATION_COUNT>0)
                {
                    notificationMenuItem.setActionView(R.layout.notification_badge)
                    val view = notificationMenuItem.actionView
                    val badgeCounter = view?.findViewById<TextView>(R.id.tv_Notification_Badge)
                    badgeCounter?.setText(NOTIFICATION_COUNT.toString())
                    notificationMenuItem.actionView?.setOnClickListener {
                        navController.navigate(PCADashboardFragmentDirections.actionPCADashboardFragmentToNotificationFragment())
                    }
                }else
                {
                    notificationMenuItem.setActionView(null)
                }

                if (CHAT_NOTIFICATION_COUNT > 0) {
                    chatMenuItem.setActionView(R.layout.message_badge)
                    val view = chatMenuItem.actionView
                    val badgeCounter = view?.findViewById<TextView>(R.id.tv_Message_Badge)
                    badgeCounter?.setText(CHAT_NOTIFICATION_COUNT.toString())
                    chatMenuItem.actionView?.setOnClickListener {
                        navController.navigate(PCADashboardFragmentDirections.actionPCADashboardFragmentToPCAChatListFragment())
                    }
                } else {
                    chatMenuItem.setActionView(null)
                }

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.btn_Profile -> {
                        navController.navigate(PCADashboardFragmentDirections.actionPCADashboardFragmentToProfileOptionFragment())
                    }
                    R.id.nav_Notification->{
                        navController.navigate(PCADashboardFragmentDirections.actionPCADashboardFragmentToNotificationFragment())
                    }
                    R.id.btn_Invoice->{
                        navController.navigate(PCADashboardFragmentDirections.actionPCADashboardFragmentToPCAInvoiceFragment())
                    }

                    R.id.btn_InvoiceStock->{
                        navController.navigate(PCADashboardFragmentDirections.actionPCADashboardFragmentToInvoiceStockFragment())
                    }

                    R.id.btn_InvoiceReport->{
                        navController.navigate(PCADashboardFragmentDirections.actionPCADashboardFragmentToInvoiceReportFragment())
                    }

                    R.id.btn_Chat->{
                        navController.navigate(PCADashboardFragmentDirections.actionPCADashboardFragmentToPCAChatListFragment())
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)
        updateNotificationCount()
        var commodityId = PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID, "")
        COMMODITY_BHARTI =
            DatabaseManager.ExecuteScalar(Query.getCommodityBhartiByCommodityId(commodityId.toString()))!!
        if (PrefUtil.getSystemLanguage().equals("gu")) {
            binding.tvCommodityNewPCADashboardFragment.setText(
                DatabaseManager.ExecuteScalar(
                    Query.getGujaratiCommodityNameByCommodityId(
                        PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID, "").toString()
                    )
                )
            )
        } else {
            binding.tvCommodityNewPCADashboardFragment.setText(
                PrefUtil.getString(
                    PrefUtil.KEY_COMMODITY_NAME,
                    ""
                ).toString()
            )
        }
        binding.tvDateNewPCADashboardFragment.setText(DateUtility().getCompletionDate())
        setOnClickListeners()
        return binding.root
    }

    private fun fetchDataFromAPI() {
        try {
            if(ConnectionCheck.isConnected(requireContext())) {
//                FetchCityMasterAPI(requireContext(), requireActivity())
                FetchShopMasterAPI(requireContext(), requireActivity(), ShopMasterAPICallModel(PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString(), "GetAPMCwise",PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,"").toString()))
                FetchPCAAuctionDetailAPI(requireContext(), requireActivity(), this)
                FetchPCAPreviousAuctionAPI(requireContext(),this@PCADashboardFragment,PREV_AUCTION_SELECTED_DATE)
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchDataFromAPI: ${e.message}", )
            e.printStackTrace()
        }
    }

    private fun setOnClickListeners() {
        try {
            binding.cvPCAAuctionNewPCADashboardFragment.setOnClickListener {
                navController.navigate(
                    PCADashboardFragmentDirections.actionPCADashboardFragmentToPCAAuctionFragment(
                        "Hello"
                    )
                )
            }
            binding.btnPurchasedBagsNewPCADashboardFragment.setOnClickListener {
                navController.navigate(
                    PCADashboardFragmentDirections.actionPCADashboardFragmentToPCAAuctionFragment(
                        "Hello"
                    )
                )
            }

            binding.cvPreviousAuctionNewPCADashboardFragment.setOnClickListener {
                navController.navigate(PCADashboardFragmentDirections.actionPCADashboardFragmentToPCAPreviousAuctionFragment(PREV_AUCTION_SELECTED_DATE))
            }

            binding.btnDatePickerNewPCADashboardFragment.setOnClickListener {
                showDatePickerDialog()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "setOnClickListeners: ${e.message}")
        }
    }

    fun bindBuyerAllocatedData(modelData: PCAAuctionDetailModel) {
        try {
            //Allocation Calculation
            if (modelData.PerBoriRate.isEmpty()) {
                modelData.PerBoriRate = "0"
            }
            if (modelData.PerLabourCharge.isEmpty()) {
                modelData.PerLabourCharge = "0"
            }
            val tvAllocationBagsStringBuilder = StringBuilder().apply {
                append(requireContext().applicationContext.getString(R.string.bags_lbl))
                append(" ${modelData.BuyerBori}")
            }
            binding.tvAllocatedBagsNewPCADashboardFragment.setText(tvAllocationBagsStringBuilder.toString())
            var pcaAllocatedBags = modelData.BuyerBori.toFloat()
            var commodityBhartiPrice = modelData.CommodityBhartiPrice.toDouble()
            var buyerUpperLimit = modelData.BuyerUpperPrice.toDouble()
            var buyerLowerLimit = modelData.BuyerLowerPrice.toDouble()

            var pcaMarketCess =
                (((pcaAllocatedBags * COMMODITY_BHARTI.toDouble()) / 20) * ((buyerUpperLimit + buyerLowerLimit) / 2) * modelData.MCessRate.toDouble()) / 100.00
            var pcaCommRate =
                (((pcaAllocatedBags * COMMODITY_BHARTI.toDouble()) / 20) * ((buyerUpperLimit + buyerLowerLimit) / 2) * modelData.PCACommRate.toDouble()) / 100.00
            var gcaCommRate =
                (((pcaAllocatedBags * COMMODITY_BHARTI.toDouble()) / 20) * ((buyerUpperLimit + buyerLowerLimit) / 2) * modelData.GCACommRate.toDouble()) / 100.00
            var pcaLabourCharge = pcaAllocatedBags * modelData.PerLabourCharge.toDouble()
            var pcaTransportationCharge = pcaAllocatedBags * modelData.PerBoriRate.toDouble()
            var pcaAllocatedBasic = modelData.BuyerPCABudget.toDouble()

//            var totalPCABudget = pcaMarketCess+pcaCommRate+gcaCommRate+pcaLabourCharge+pcaTransportationCharge+pcaAllocatedBasic
            var totalPCABudget = pcaAllocatedBasic

            val AllocatedBuyerCost =
                NumberFormat.getCurrencyInstance().format(totalPCABudget).substring(1)

            val tvAllocationBuyerCostStringBuilder = StringBuilder().apply {
                append(requireContext().applicationContext.getString(R.string.cost_lbl))
                append(" $AllocatedBuyerCost")
            }

            binding.tvAllocatedTotalCostNewPCADashboardFragment.setText(tvAllocationBuyerCostStringBuilder.toString())
            var rate = totalPCABudget / ((pcaAllocatedBags * COMMODITY_BHARTI.toDouble()) / 20.0)
            val AllocatedBuyerAvgRate = NumberFormat.getCurrencyInstance().format(rate).substring(1)

            val tvAllocationRateStringBuilder = StringBuilder().apply {
                append(requireContext().applicationContext.getString(R.string.rate_lbl))
                append(" $AllocatedBuyerAvgRate")
            }
            binding.tvAllocatedRateNewPCADashboardFragment.setText(tvAllocationRateStringBuilder.toString())

            //Purchased Calculation

            var pcaTotalPurchasedBag = modelData.TotalPurchasedBags.toFloat()
            val tvPurchasedBagsStringBuilder = StringBuilder().apply {
                append(requireContext().applicationContext.getString(R.string.bags_lbl))
                append(" $pcaTotalPurchasedBag")
            }
            binding.tvPurchasedBagsNewPCADashboardFragment.setText(tvPurchasedBagsStringBuilder.toString())

            var purchased_pcaMarketCess =
                (((pcaTotalPurchasedBag * COMMODITY_BHARTI.toDouble()) / 20) * ((buyerUpperLimit + buyerLowerLimit) / 2) * modelData.MCessRate.toDouble()) / 100.00
            var purchased_pcaCommRate =
                (((pcaTotalPurchasedBag * COMMODITY_BHARTI.toDouble()) / 20) * ((buyerUpperLimit + buyerLowerLimit) / 2) * modelData.PCACommRate.toDouble()) / 100.00
            var purchased_gcaCommRate =
                (((pcaTotalPurchasedBag * COMMODITY_BHARTI.toDouble()) / 20) * ((buyerUpperLimit + buyerLowerLimit) / 2) * modelData.GCACommRate.toDouble()) / 100.00
            var purchased_pcaLabourCharge =
                pcaTotalPurchasedBag * modelData.PerLabourCharge.toDouble()
            var purchased_pcaTransportationCharge =
                pcaTotalPurchasedBag * modelData.PerBoriRate.toDouble()
            var purchased_pcaBasic = modelData.TotalCost.toDouble()

//            var PCAtotalPurchasedCost = purchased_pcaMarketCess+purchased_pcaCommRate+purchased_gcaCommRate+purchased_pcaLabourCharge+purchased_pcaTransportationCharge+purchased_pcaBasic
            var PCAtotalPurchasedCost = purchased_pcaBasic

            val PurchasedPCACost =
                NumberFormat.getCurrencyInstance().format(PCAtotalPurchasedCost).substring(1)
            val tvPurchasedCostStringBuilder = StringBuilder().apply {
                append(requireContext().applicationContext.getString(R.string.cost_lbl))
                append(" $PurchasedPCACost")
            }
            binding.tvPurchasedTotalCostNewPCADashboardFragment.setText(tvPurchasedCostStringBuilder.toString())
            var purchased_Avgrate = 0.0
            if (pcaTotalPurchasedBag> 0 && PCAtotalPurchasedCost > 0.0) {
                purchased_Avgrate =
                    PCAtotalPurchasedCost / ((pcaTotalPurchasedBag * COMMODITY_BHARTI.toDouble()) / 20.0)
            }
            val PcaPurchasedAvgRate =
                NumberFormat.getCurrencyInstance().format(purchased_Avgrate).substring(1)

            val tvPurchasedAvgRateStringBuilder = StringBuilder().apply {
                append(requireContext().applicationContext.getString(R.string.rate_lbl))
                append(" $PcaPurchasedAvgRate")
            }
            binding.tvPurchasedAvgRateNewPCADashboardFragment.setText(tvPurchasedAvgRateStringBuilder.toString())


        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindBuyerAllocatedData: ${e.message}")
        }
    }

    fun bindPreviousAuctionData(modelData: PCAPrevAuctionMasterModel) {
        try {
            PREV_AUCTION_SELECTED_DATE = modelData.Date
            val tvPreviousDateStringBuilder = StringBuilder().apply {
                append(requireContext().applicationContext.getString(R.string.date_lbl))
                append(" ${modelData.Date}")
            }
            val tvPreviousBagsStringBuilder = StringBuilder().apply {
                append(requireContext().applicationContext.getString(R.string.bags_lbl))
                append(" ${modelData.LastTotalPurchasedBags}")
            }
            val tvPreviousTotalStringBuilder = StringBuilder().apply {
                append(requireContext().applicationContext.getString(R.string.cost_lbl))
                append(" ${modelData.LastPCATotalCost}")
            }
            val tvPreviousAvgRateStringBuilder = StringBuilder().apply {
                append(requireContext().applicationContext.getString(R.string.avg_rate_lbl))
                append(" ${modelData.LastPCATotalAvgRate}")
            }
            binding.tvPreviousAuctionDateNewPCADashboardFragment.setText(tvPreviousDateStringBuilder.toString())
            binding.tvPreviousAuctionAvgRateNewPCADashboardFragment.setText(tvPreviousAvgRateStringBuilder.toString())
            binding.tvPreviousAuctionPurchasedBagsNewPCADashboardFragment.setText(tvPreviousBagsStringBuilder.toString())
            binding.tvPreviousAuctionTotalCostNewPCADashboardFragment.setText(tvPreviousTotalStringBuilder.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindPreviousAuctionData: ${e.message}")
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
            FetchPCAPreviousAuctionAPI(
                requireContext(),
                this@PCADashboardFragment,
                PREV_AUCTION_SELECTED_DATE
            )
        }
        if (!datePicker.isAdded) {
            datePicker.show(childFragmentManager, datePicker.toString())
        }
    }

    fun updateNotificationCount(){
        try {

            NOTIFICATION_COUNT = DatabaseManager.ExecuteScalar(Query.getTMPTUnseenNotification())!!.toInt()
            Log.d(TAG, "updateNotificationCount: NOTIFICATION_COUNT : $NOTIFICATION_COUNT")
            CHAT_NOTIFICATION_COUNT =DatabaseManager.ExecuteScalar(Query.getTMPTUnseenChatNotification())!!.toInt()
            Log.d(TAG, "updateNotificationCount: CHAT_NOTIFICATION_COUNT : $CHAT_NOTIFICATION_COUNT")
            requireActivity().runOnUiThread {
                menuHost.invalidateMenu()
            }

        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "updateNotificationCount: ${e.message}")
        }
    }

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateNotificationCount()
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

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        requireContext().registerReceiver(notificationReceiver,filter)
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        requireContext().unregisterReceiver(notificationReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}