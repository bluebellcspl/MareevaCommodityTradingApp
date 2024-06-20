package com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer

import ConnectionCheck
import android.app.AlertDialog
import android.icu.text.NumberFormat
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.LiveAuctionListAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.constants.URLHelper
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.BuyerExpenseDialogLayoutBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentLiveAuctionBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.POSTAuctionStartStopAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.AuctionDetailsModel
import com.bluebellcspl.maarevacommoditytradingapp.model.ExpandableObject
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionPCAListModel
import com.bluebellcspl.maarevacommoditytradingapp.model.POSTAuctionStartStopAPIModel
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.RecyclerViewHelper
import com.bluebellcspl.maarevacommoditytradingapp.webSocketHelper.SocketHandler
import com.bluebellcspl.maarevacommoditytradingapp.webSocketHelper.WebSocketClient
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class LiveAuctionFragment : Fragment(), RecyclerViewHelper {
    var _binding: FragmentLiveAuctionBinding?=null
    private val binding get() = _binding!!
    private val commonUIUtility by lazy { CommonUIUtility(requireContext().applicationContext) }
    private val TAG = "LiveAuctionFragment"
    private var isWebSocketConnected = false
    lateinit var adapter: LiveAuctionListAdapter
    var lastPCAList: ArrayList<LiveAuctionPCAListModel> = ArrayList()
    var newAuctionData: LiveAuctionMasterModel? = null
    var COMMODITY_BHARTI = ""
    private var webSocket: WebSocket? = null
    private var isConnectingWebSocket = false
    var commodityId = ""
    var companyCode = ""
    var buyerRegId = ""
    var WEB_SOCKET_ID = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_live_auction, container, false)
         commodityId = PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID, "").toString()
         companyCode = PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE, "").toString()
         buyerRegId = PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString()
        COMMODITY_BHARTI =
            DatabaseManager.ExecuteScalar(Query.getCommodityBhartiByCommodityId(commodityId.toString()))!!
        setOnClickListener()
        return binding.root
    }

    private fun setOnClickListener() {
        try {
            binding.edtPCATotalAmountLiveAuctionFragment.setOnClickListener {
                if (lastPCAList.isNotEmpty()) {
                    showPCAExpensesPopup(newAuctionData!!)
                } else {
                    commonUIUtility.showToast("No Buyer Expense Data")
                }
            }
            binding.cvPCAExpenseLiveAuctionFragment.setOnClickListener {
                if (lastPCAList.isNotEmpty()) {
                    showPCAExpensesPopup(newAuctionData!!)
                } else {
                    commonUIUtility.showToast("No PCA Expense Data")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "setOnClickListener: ${e.message}")
        }
    }

    fun onMessageReceived(dataList: LiveAuctionMasterModel) {

        if (!isAdded) return

        if (dataList.PCAList.isNotEmpty()) {

            if (dataList.PCAList != lastPCAList) {
                var expandableList: ArrayList<ExpandableObject> = ArrayList()
                for (i in dataList.PCAList.indices) {
                    expandableList.add(ExpandableObject(false))
                }
                adapter =
                    LiveAuctionListAdapter(requireContext(), dataList.PCAList, expandableList, this)
                binding.rcViewLiveAuctionFragment.adapter = adapter
                adapter.submitList(dataList.PCAList)
                lastPCAList = dataList.PCAList
                newAuctionData = dataList
                calculateExpenses(dataList)
            }
        }
    }

    fun calculateExpenses(dataList: LiveAuctionMasterModel) {
        try {
            if (dataList.TotalTransportationCharge.isEmpty())
            {
                dataList.TotalTransportationCharge = "0"
            }
            //            //Buyer Calculations
            binding.edtBuyerAllocatedBagsLiveAuctionFragment.setText(dataList.AllocatedBag)

            binding.edtBuyerBasicAmountLiveAuctionFragment.setText(dataList.Basic)
            val BuyerTotalAmountNF =
                NumberFormat.getCurrencyInstance().format(dataList.TotalCost.toDouble()).substring(1)
            binding.edtBuyerTotalAmountLiveAuctionFragment.setText(BuyerTotalAmountNF)

            var totalBuyerExpense =
                dataList.TotalGCAComm.toDouble() + dataList.TotalPCAComm.toDouble() + dataList.TotalMarketCess.toDouble() + dataList.TotalLabourCharge.toDouble() + dataList.TotalTransportationCharge.toDouble()
//            binding.tvBuyerExpensesLiveAuctionFragment.setText(String.format("%.2f",totalBuyerExpense))
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
                    var currentTotalAmount = amount + expense
                    CURRENT_Shop_Amount += amount
                    CURRENT_pcaCommCharge += pcaCommCharge
                    CURRENT_gcaCommCharge += gcaCommCharge
                    CURRENT_pcaMarketCess += pcaMarketCess
                    CURRENT_pcaLabourCharge += pcaLabourCharge
                    CURRENT_pcaTransportationCharge += pcaTransportationCharge

                    CURRENT_pcaExpense += expense
                    CURRENT_TOTAL_COST += amount + expense
                    TOTAL_pcaBasic += currentPCABasic
                    Log.d(
                        TAG,
                        "calculateExpenses: ==============================================================================================="
                    )
                    Log.d(TAG, "calculateExpenses: PCA_NAME : ${PCAData.PCAName}")
                    Log.d(
                        TAG,
                        "calculateExpenses SHOP_NO & SHOP_NAME : ${ShopData.ShopNo} ${ShopData.ShopName}"
                    )
                    Log.d(TAG, "calculateExpenses: SHOP_BAGS : ${ShopData.Bags}")
                    Log.d(TAG, "calculateExpenses: SHOP_CURRENT_PRICE : $SHOP_CURRENT_PRICE")
                    Log.d(TAG, "calculateExpenses: COMMODITY_BHARTI : $COMMODITY_BHARTI")
                    Log.d(TAG, "calculateExpenses: Basic : $currentPCABasic")
                    Log.d(
                        TAG,
                        "calculateExpenses: GCA_COMM(% ${PCAData.GCACommCharge}) : $gcaCommCharge"
                    )
                    Log.d(
                        TAG,
                        "calculateExpenses: PCA_COMM(% ${PCAData.PCACommCharge}) : $pcaCommCharge"
                    )
                    Log.d(
                        TAG,
                        "calculateExpenses: MARKETCESS(% ${PCAData.MarketCessCharge}) : $pcaMarketCess"
                    )
                    Log.d(TAG, "calculateExpenses: TransportationCharge : $pcaTransportationCharge")
                    Log.d(TAG, "calculateExpenses: LabourCharge : $pcaLabourCharge")
                    Log.d(TAG, "calculateExpenses: AMOUNT : $amount")
                    Log.d(TAG, "calculateExpenses: CurrentExpense : $expense")
                    Log.d(TAG, "calculateExpenses: CURRENT_TotalCost : $currentTotalAmount")
                    Log.d(
                        TAG,
                        "calculateExpenses: ==============================================================================================="
                    )
                    Log.d(TAG, "calculateExpenses: TOTAL_AUCTION_EXPENSE : $CURRENT_pcaExpense")
                    Log.d(TAG, "calculateExpenses: TOTAL_AUCTION_AMOUNT : $CURRENT_TOTAL_COST")
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

            binding.edtPCAPurchasedBagsLiveAuctionFragment.setText("%s".format(TOTAL_AuctionBags.toString()))

            val PCATotalAmountNF =
                NumberFormat.getCurrencyInstance().format(TOTAL_AuctionCost).substring(1)
            binding.edtPCATotalAmountLiveAuctionFragment.setText("%s".format(PCATotalAmountNF))
            var rate =
                TOTAL_AuctionCost / ((TOTAL_AuctionBags * COMMODITY_BHARTI.toDouble()) / 20.0)
            val RateNF = NumberFormat.getCurrencyInstance().format(rate).substring(1)
            binding.tvPCAAvgRateLiveAuctionFragment.setText("%s".format(RateNF))
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "calculateExpenses: ${e.message}")
        }
    }

    fun showPCAExpensesPopup(dataList: LiveAuctionMasterModel) {
        try {
            val alertDailogBuilder = AlertDialog.Builder(requireContext())
            val dialogBinding = BuyerExpenseDialogLayoutBinding.inflate(layoutInflater)
            val dialogView = dialogBinding.root
            alertDailogBuilder.setView(dialogView)
            val alertDialog = alertDailogBuilder.create()
            alertDialog.setCanceledOnTouchOutside(true)
            alertDialog.setCancelable(true)
            alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()

            dialogBinding.tvlabelHeader.setText("PCA's Expenses")
            dialogBinding.llExpensesBuyerExpenseDialog.setOnClickListener {
                if (dialogBinding.llExpandableExpensesBuyerExpenseDialog.isVisible)
                {
                    dialogBinding.llExpandableExpensesBuyerExpenseDialog.visibility = View.GONE
                }else
                {
                    dialogBinding.llExpandableExpensesBuyerExpenseDialog.visibility = View.VISIBLE
                }
            }

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
                    currentPCABasic = ShopData.Amount.toDouble()
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

                    CURRENT_Shop_Amount += amount
                    CURRENT_pcaCommCharge += pcaCommCharge
                    CURRENT_gcaCommCharge += gcaCommCharge
                    CURRENT_pcaMarketCess += pcaMarketCess
                    CURRENT_pcaLabourCharge += pcaLabourCharge
                    CURRENT_pcaTransportationCharge += pcaTransportationCharge
                    TOTAL_pcaBasic += currentPCABasic
                    CURRENT_pcaExpense += pcaCommCharge + gcaCommCharge + pcaMarketCess +pcaLabourCharge + pcaTransportationCharge
                    CURRENT_TOTAL_COST += amount + pcaCommCharge + gcaCommCharge + pcaMarketCess +pcaLabourCharge + pcaTransportationCharge

                    Log.d(
                        TAG,
                        "showPCAExpensesPopup: ==============================================================================================="
                    )
                    Log.d(
                        TAG,
                        "showPCAExpensesPopup SHOP_NO & SHOP_NAME : ${ShopData.ShopNo} ${ShopData.ShopName}"
                    )
                    Log.d(TAG, "showPCAExpensesPopup: SHOP_BAGS : ${ShopData.Bags}")
                    Log.d(TAG, "showPCAExpensesPopup: Basic : $currentPCABasic")
                    Log.d(TAG, "showPCAExpensesPopup: AMOUNT : $amount")
                    Log.d(TAG, "showPCAExpensesPopup: CurrentExpense : $CURRENT_pcaExpense")
                    Log.d(
                        TAG,
                        "showPCAExpensesPopup: GCA_COMM(%${PCAData.GCACommCharge}) : $gcaCommCharge"
                    )
                    Log.d(
                        TAG,
                        "showPCAExpensesPopup: PCA_COMM(%${PCAData.PCACommCharge}) : $pcaCommCharge"
                    )
                    Log.d(
                        TAG,
                        "showPCAExpensesPopup: MARKETCESS(%${PCAData.MarketCessCharge}) : $pcaMarketCess"
                    )
                    Log.d(
                        TAG,
                        "showPCAExpensesPopup: TransportationCharge : $pcaTransportationCharge"
                    )
                    Log.d(TAG, "showPCAExpensesPopup: LabourCharge : $pcaLabourCharge")
                    Log.d(TAG, "showPCAExpensesPopup: TotalCost : $CURRENT_TOTAL_COST")
                    Log.d(
                        TAG,
                        "showPCAExpensesPopup: ==============================================================================================="
                    )
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
            val pcaBasicNF = NumberFormat.getCurrencyInstance().format(TOTAL_pcaBasic).substring(1)
            dialogBinding.tvTotalBasicAmountBuyerExpenseDialog.setText(pcaBasicNF)
            val totalExpense = TOTAL_pcaCommCharge+TOTAL_gcaCommCharge+TOTAL_pcaMarketCess+TOTAL_pcaTransportationCharge+TOTAL_pcaLabourCharge
            val ExpensesNF = NumberFormat.getCurrencyInstance().format(totalExpense).substring(1)
            dialogBinding.tvTotalExpenseBuyerExpenseDialog.setText(ExpensesNF)
            val pcaLabourChargeNF = NumberFormat.getCurrencyInstance().format(TOTAL_pcaLabourCharge).substring(1)
            dialogBinding.tvTotalLabourChargeBuyerExpenseDialog.setText(pcaLabourChargeNF)
            val gcaCommChargeNF = NumberFormat.getCurrencyInstance().format(TOTAL_gcaCommCharge).substring(1)
            dialogBinding.tvTotalGCACommissionBuyerExpenseDialog.setText(gcaCommChargeNF)
            val pcaCommChargeNF = NumberFormat.getCurrencyInstance().format(TOTAL_pcaCommCharge).substring(1)
            dialogBinding.tvTotalPCACommissionBuyerExpenseDialog.setText(pcaCommChargeNF)
            val pcaTransportChargeNF =
                NumberFormat.getCurrencyInstance().format(TOTAL_pcaTransportationCharge).substring(1)
            dialogBinding.tvTotalTransportChargeBuyerExpenseDialog.setText(pcaTransportChargeNF)
            val pcaMarketCessNF = NumberFormat.getCurrencyInstance().format(TOTAL_pcaMarketCess).substring(1)
            dialogBinding.tvTotalMarketCessBuyerExpenseDialog.setText(pcaMarketCessNF)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "showBuyerExpenesePopup: ${e.message}")
        }
    }
    fun showPCADataPopup(PCAData: LiveAuctionPCAListModel) {
        try {
            val alertDailogBuilder = AlertDialog.Builder(requireContext())
            val dialogBinding = BuyerExpenseDialogLayoutBinding.inflate(layoutInflater)
            val dialogView = dialogBinding.root
            alertDailogBuilder.setView(dialogView)
            val alertDialog = alertDailogBuilder.create()
            alertDialog.setCanceledOnTouchOutside(true)
            alertDialog.setCancelable(true)
            alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()

            dialogBinding.tvlabelHeader.setText("PCA's Expenses")
            dialogBinding.llExpensesBuyerExpenseDialog.setOnClickListener {
                if (dialogBinding.llExpandableExpensesBuyerExpenseDialog.isVisible)
                {
                    dialogBinding.llExpandableExpensesBuyerExpenseDialog.visibility = View.GONE
                }else
                {
                    dialogBinding.llExpandableExpensesBuyerExpenseDialog.visibility = View.VISIBLE
                }
            }

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


//            for (PCAData in dataList.PCAList) {
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
                    currentPCABasic = ShopData.Amount.toDouble()
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

                    CURRENT_Shop_Amount += amount
                    CURRENT_pcaCommCharge += pcaCommCharge
                    CURRENT_gcaCommCharge += gcaCommCharge
                    CURRENT_pcaMarketCess += pcaMarketCess
                    CURRENT_pcaLabourCharge += pcaLabourCharge
                    CURRENT_pcaTransportationCharge += pcaTransportationCharge
                    TOTAL_pcaBasic += currentPCABasic
                    CURRENT_pcaExpense += pcaCommCharge + gcaCommCharge + pcaMarketCess +pcaLabourCharge + pcaTransportationCharge
                    CURRENT_TOTAL_COST += amount + pcaCommCharge + gcaCommCharge + pcaMarketCess +pcaLabourCharge + pcaTransportationCharge

                    Log.d(
                        TAG,
                        "showPCAExpensesPopup: ==============================================================================================="
                    )
                    Log.d(
                        TAG,
                        "showPCAExpensesPopup SHOP_NO & SHOP_NAME : ${ShopData.ShopNo} ${ShopData.ShopName}"
                    )
                    Log.d(TAG, "showPCAExpensesPopup: SHOP_BAGS : ${ShopData.Bags}")
                    Log.d(TAG, "showPCAExpensesPopup: Basic : $currentPCABasic")
                    Log.d(TAG, "showPCAExpensesPopup: AMOUNT : $amount")
                    Log.d(TAG, "showPCAExpensesPopup: CurrentExpense : $CURRENT_pcaExpense")
                    Log.d(
                        TAG,
                        "showPCAExpensesPopup: GCA_COMM(%${PCAData.GCACommCharge}) : $gcaCommCharge"
                    )
                    Log.d(
                        TAG,
                        "showPCAExpensesPopup: PCA_COMM(%${PCAData.PCACommCharge}) : $pcaCommCharge"
                    )
                    Log.d(
                        TAG,
                        "showPCAExpensesPopup: MARKETCESS(%${PCAData.MarketCessCharge}) : $pcaMarketCess"
                    )
                    Log.d(
                        TAG,
                        "showPCAExpensesPopup: TransportationCharge : $pcaTransportationCharge"
                    )
                    Log.d(TAG, "showPCAExpensesPopup: LabourCharge : $pcaLabourCharge")
                    Log.d(TAG, "showPCAExpensesPopup: TotalCost : $CURRENT_TOTAL_COST")
                    Log.d(
                        TAG,
                        "showPCAExpensesPopup: ==============================================================================================="
                    )
                }


                TOTAL_pcaMarketCess += CURRENT_pcaMarketCess
                TOTAL_pcaCommCharge += CURRENT_pcaCommCharge
                TOTAL_gcaCommCharge += CURRENT_gcaCommCharge
                TOTAL_pcaTransportationCharge += CURRENT_pcaTransportationCharge
                TOTAL_pcaLabourCharge += CURRENT_pcaLabourCharge
                TOTAL_AuctionBags += PCAData.TotalPurchasedBags.toFloat()
                TOTAL_AuctionCost += CURRENT_TOTAL_COST
                TOTAL_pcaExpense += CURRENT_pcaExpense
//            }
            val pcaBasicNF = NumberFormat.getCurrencyInstance().format(TOTAL_pcaBasic).substring(1)
            dialogBinding.tvTotalBasicAmountBuyerExpenseDialog.setText(pcaBasicNF)
            val totalExpense = TOTAL_pcaCommCharge+TOTAL_gcaCommCharge+TOTAL_pcaMarketCess+TOTAL_pcaTransportationCharge+TOTAL_pcaLabourCharge
            val ExpensesNF = NumberFormat.getCurrencyInstance().format(totalExpense).substring(1)
            dialogBinding.tvTotalExpenseBuyerExpenseDialog.setText(ExpensesNF)
            val pcaLabourChargeNF = NumberFormat.getCurrencyInstance().format(TOTAL_pcaLabourCharge).substring(1)
            dialogBinding.tvTotalLabourChargeBuyerExpenseDialog.setText(pcaLabourChargeNF)
            val gcaCommChargeNF = NumberFormat.getCurrencyInstance().format(TOTAL_gcaCommCharge).substring(1)
            dialogBinding.tvTotalGCACommissionBuyerExpenseDialog.setText(gcaCommChargeNF)
            val pcaCommChargeNF = NumberFormat.getCurrencyInstance().format(TOTAL_pcaCommCharge).substring(1)
            dialogBinding.tvTotalPCACommissionBuyerExpenseDialog.setText(pcaCommChargeNF)
            val pcaTransportChargeNF =
                NumberFormat.getCurrencyInstance().format(TOTAL_pcaTransportationCharge).substring(1)
            dialogBinding.tvTotalTransportChargeBuyerExpenseDialog.setText(pcaTransportChargeNF)
            val pcaMarketCessNF = NumberFormat.getCurrencyInstance().format(TOTAL_pcaMarketCess).substring(1)
            dialogBinding.tvTotalMarketCessBuyerExpenseDialog.setText(pcaMarketCessNF)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "showBuyerExpenesePopup: ${e.message}")
        }
    }

    override fun onItemClick(postion: Int, onclickType: String) {
        try {
            var isAuctionStop = ""
            if (onclickType.equals("start", true)) {
                isAuctionStop = "false"
            } else {
                isAuctionStop = "true"
            }
            val model = POSTAuctionStartStopAPIModel(
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString(),
                PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID, "").toString(),
                PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE, "").toString(),
                DateUtility().getCompletionDate(),
                lastPCAList[postion].PCARegId.toString(),
                "2",
                "",
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString(),
                isAuctionStop
            )
            if (ConnectionCheck.isConnected(requireContext())) {
                POSTAuctionStartStopAPI(
                    requireContext(),
                    requireActivity(),
                    this@LiveAuctionFragment,
                    model
                )
            } else {
                commonUIUtility.showToast("No Internet Connection!")
            }
        } catch (e: Exception) {
            Log.e(TAG, "onItemClick: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onBuyerAuctionPCAItemClick(postion: Int, model: AuctionDetailsModel) {
        TODO("Not yet implemented")
    }

    override fun getBuyerAuctionDataList(dataList: ArrayList<AuctionDetailsModel>) {
        TODO("Not yet implemented")
    }

    override fun getLiveAuctionPCAData(postion: Int, model: LiveAuctionPCAListModel) {
        showPCADataPopup(model)
    }

    override fun onResume() {
        super.onResume()
        if (!isWebSocketConnected && !isConnectingWebSocket) {
            Log.d(TAG, "onResume: WEB_SOCKET_CONNECT onResume")

            // Set the flag to indicate that a connection attempt is in progress
            isConnectingWebSocket = true

                if (ConnectionCheck.isConnected(requireContext().applicationContext)) {
//                    webSocketClient.connect()
                    lifecycleScope.launch(Dispatchers.IO)
                    {
                        val LIVE_SOCKET_API = URLHelper.LIVE_AUCTION_SOCKET_URL.replace(
                            "<COMMODITY_ID>",
                            commodityId.toString()
                        ).replace("<DATE>", DateUtility().getCompletionDate())
                            .replace("<COMPANY_CODE>", companyCode.toString())
                            .replace("<BUYER_REG_ID>", buyerRegId.toString())

                        Log.d(TAG, "onResume: LIVE_AUCTION_SOCKET_API : $LIVE_SOCKET_API")
                        
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
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isWebSocketConnected && !isConnectingWebSocket) {
            Log.d(TAG, "onStart: WEB_SOCKET_CONNECT onStart")

            // Set the flag to indicate that a connection attempt is in progress
            isConnectingWebSocket = true

                if (ConnectionCheck.isConnected(requireContext())) {
                    lifecycleScope.launch(Dispatchers.IO)
                    {

                        val LIVE_SOCKET_API = URLHelper.LIVE_AUCTION_SOCKET_URL.replace(
                            "<COMMODITY_ID>",
                            commodityId.toString()
                        ).replace("<DATE>", DateUtility().getCompletionDate())
                            .replace("<COMPANY_CODE>", companyCode.toString())
                            .replace("<BUYER_REG_ID>", buyerRegId.toString())
                        Log.d(TAG, "onStart: LIVE_AUCTION_SOCKET_API : $LIVE_SOCKET_API")
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
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        if (isWebSocketConnected) {
            disconnect()
            isWebSocketConnected = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Disconnect the WebSocket in onDestroy to ensure proper cleanup
        newAuctionData = null
        lastPCAList = ArrayList()
        disconnect()
    }

    fun disconnect() {
        try {
            Log.d(TAG, "disconnect: SOCKET_DISCONNECTED")
            webSocket?.close(1000, "Disconnect Socket")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "disconnect: ${e.message}")
        }
    }

    private inner class MyWebSocketListener : WebSocketListener() {
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            this@LiveAuctionFragment.webSocket = null
            Log.d(TAG, "onClosed: SOCKET_CLOSED")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            this@LiveAuctionFragment.webSocket = null
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
            this@LiveAuctionFragment.webSocket = webSocket
            WEB_SOCKET_ID = webSocket.toString()
            Log.d(TAG, "onOpen: WEB_SOCKET_ID : $WEB_SOCKET_ID")
            Log.d(TAG, "onOpen: SOCKET_CONNECTED")
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}