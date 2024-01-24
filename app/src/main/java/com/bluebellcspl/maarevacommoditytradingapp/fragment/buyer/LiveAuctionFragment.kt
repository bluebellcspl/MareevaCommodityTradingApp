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
import com.bluebellcspl.maarevacommoditytradingapp.webSocketHelper.WebSocketClient

class LiveAuctionFragment : Fragment(), RecyclerViewHelper {
    lateinit var binding: FragmentLiveAuctionBinding
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val TAG = "LiveAuctionFragment"
    private lateinit var webSocketClient: WebSocketClient
    private var isWebSocketConnected = false
    lateinit var adapter: LiveAuctionListAdapter
    var lastPCAList: ArrayList<LiveAuctionPCAListModel> = ArrayList()
    var newAuctionData: LiveAuctionMasterModel? = null
    var COMMODITY_BHARTI = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_live_auction, container, false)
        var commodityId = PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID, "")
        var companyCode = PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE, "")
        var buyerRegId = PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "")
        COMMODITY_BHARTI =
            DatabaseManager.ExecuteScalar(Query.getCommodityBhartiByCommodityId(commodityId.toString()))!!

        webSocketClient = WebSocketClient(
            requireContext(),
            URLHelper.LIVE_AUCTION_SOCKET_URL.replace("<COMMODITY_ID>",commodityId.toString()).replace("<DATE>",DateUtility().getCompletionDate()).replace("<COMPANY_CODE>",companyCode.toString()).replace("<BUYER_REG_ID>",buyerRegId.toString()),
            viewLifecycleOwner,
            ::onMessageReceived
        )
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
        if (dataList.PCAList.isNotEmpty()) {

            if (dataList.PCAList != lastPCAList) {
//                Log.d(TAG, "onMessageReceived: LAST_PCA_LIST : $lastPCAList")
//                Log.d(TAG, "onMessageReceived: CURRENT_PCA_LIST : $dataList")
                var expandableList: ArrayList<ExpandableObject> = ArrayList()
                for (i in dataList.PCAList.indices) {
                    expandableList.add(ExpandableObject(false))
                }
                adapter =
                    LiveAuctionListAdapter(requireContext(), dataList.PCAList, expandableList, this)
                binding.rcViewLiveAuctionFragment.adapter = adapter
                binding.rcViewLiveAuctionFragment.invalidate()
                lastPCAList = dataList.PCAList
                newAuctionData = dataList
                calculateExpenses(dataList)
            }
        }
    }

    fun calculateExpenses(dataList: LiveAuctionMasterModel) {
        try {

            //            //Buyer Calculations
            binding.edtBuyerAllocatedBagsLiveAuctionFragment.setText(dataList.AllocatedBag)

            binding.edtBuyerBasicAmountLiveAuctionFragment.setText(dataList.Basic)
            val BuyerTotalAmountNF =
                NumberFormat.getCurrencyInstance().format(dataList.TotalCost.toDouble())
                    .substring(1)
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
                TOTAL_AuctionBags += PCAData.TotalPurchasedBags.toInt()
                TOTAL_AuctionCost += CURRENT_TOTAL_COST
                TOTAL_pcaExpense += CURRENT_pcaExpense
            }
            Log.d(TAG, "calculateExpenses: TOTAL_EXPENSE_PCA : $TOTAL_pcaExpense")
            Log.d(TAG, "calculateExpenses: TOTAL_PCA_BASIC   : $TOTAL_pcaBasic")

            binding.edtPCAPurchasedBagsLiveAuctionFragment.setText(
                "%s".format(
                    TOTAL_AuctionBags.toString()
                )
            )

            val PCATotalAmountNF =
                NumberFormat.getCurrencyInstance().format(TOTAL_AuctionCost).substring(1)
            binding.edtPCATotalAmountLiveAuctionFragment.setText(
                "%s".format(
                    PCATotalAmountNF
                )
            )
            var rate =
                TOTAL_AuctionCost / ((TOTAL_AuctionBags * COMMODITY_BHARTI.toDouble()) / 20.0)
            val RateNF = NumberFormat.getCurrencyInstance().format(rate).substring(1)
            binding.tvPCAAvgRateLiveAuctionFragment.setText(
                "%s".format(
                    RateNF
                )
            )

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "calculateExpenses: ${e.message}")
        }
    }

    fun showBuyerExpensesPopup(dataList: LiveAuctionMasterModel) {
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

            dialogBinding.tvlabelHeader.setText("Buyer's Expenses")
            dialogBinding.tvTotalBasicAmountBuyerExpenseDialog.setText(dataList.Basic)
            dialogBinding.tvTotalLabourChargeBuyerExpenseDialog.setText(dataList.TotalLabourCharge)
            dialogBinding.tvTotalGCACommissionBuyerExpenseDialog.setText(dataList.TotalGCAComm)
            dialogBinding.tvTotalPCACommissionBuyerExpenseDialog.setText(dataList.TotalPCAComm)
            dialogBinding.tvTotalTransportChargeBuyerExpenseDialog.setText(dataList.TotalTransportationCharge)
            dialogBinding.tvTotalMarketCessBuyerExpenseDialog.setText(dataList.TotalMarketCess)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "showBuyerExpenesePopup: ${e.message}")
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
                    currentPCABasic = ShopData.Amount.toDouble()
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
                TOTAL_AuctionBags += PCAData.TotalPurchasedBags.toInt()
                TOTAL_AuctionCost += CURRENT_TOTAL_COST
                TOTAL_pcaExpense += CURRENT_pcaExpense
            }
            val pcaBasicNF = NumberFormat.getCurrencyInstance().format(TOTAL_pcaBasic)
            dialogBinding.tvTotalBasicAmountBuyerExpenseDialog.setText(pcaBasicNF)
            val totalExpense = TOTAL_pcaCommCharge+TOTAL_gcaCommCharge+TOTAL_pcaMarketCess+TOTAL_pcaTransportationCharge+TOTAL_pcaLabourCharge
            val ExpensesNF = NumberFormat.getCurrencyInstance().format(totalExpense)
            dialogBinding.tvTotalExpenseBuyerExpenseDialog.setText(ExpensesNF)
            val pcaLabourChargeNF = NumberFormat.getCurrencyInstance().format(TOTAL_pcaLabourCharge)
            dialogBinding.tvTotalLabourChargeBuyerExpenseDialog.setText(pcaLabourChargeNF)
            val gcaCommChargeNF = NumberFormat.getCurrencyInstance().format(TOTAL_gcaCommCharge)
            dialogBinding.tvTotalGCACommissionBuyerExpenseDialog.setText(gcaCommChargeNF)
            val pcaCommChargeNF = NumberFormat.getCurrencyInstance().format(TOTAL_pcaCommCharge)
            dialogBinding.tvTotalPCACommissionBuyerExpenseDialog.setText(pcaCommChargeNF)
            val pcaTransportChargeNF =
                NumberFormat.getCurrencyInstance().format(TOTAL_pcaTransportationCharge)
            dialogBinding.tvTotalTransportChargeBuyerExpenseDialog.setText(pcaTransportChargeNF)
            val pcaMarketCessNF = NumberFormat.getCurrencyInstance().format(TOTAL_pcaMarketCess)
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
            var TOTAL_AuctionBags = 0


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
                TOTAL_AuctionBags += PCAData.TotalPurchasedBags.toInt()
                TOTAL_AuctionCost += CURRENT_TOTAL_COST
                TOTAL_pcaExpense += CURRENT_pcaExpense
//            }
            val pcaBasicNF = NumberFormat.getCurrencyInstance().format(TOTAL_pcaBasic)
            dialogBinding.tvTotalBasicAmountBuyerExpenseDialog.setText(pcaBasicNF)
            val totalExpense = TOTAL_pcaCommCharge+TOTAL_gcaCommCharge+TOTAL_pcaMarketCess+TOTAL_pcaTransportationCharge+TOTAL_pcaLabourCharge
            val ExpensesNF = NumberFormat.getCurrencyInstance().format(totalExpense)
            dialogBinding.tvTotalExpenseBuyerExpenseDialog.setText(ExpensesNF)
            val pcaLabourChargeNF = NumberFormat.getCurrencyInstance().format(TOTAL_pcaLabourCharge)
            dialogBinding.tvTotalLabourChargeBuyerExpenseDialog.setText(pcaLabourChargeNF)
            val gcaCommChargeNF = NumberFormat.getCurrencyInstance().format(TOTAL_gcaCommCharge)
            dialogBinding.tvTotalGCACommissionBuyerExpenseDialog.setText(gcaCommChargeNF)
            val pcaCommChargeNF = NumberFormat.getCurrencyInstance().format(TOTAL_pcaCommCharge)
            dialogBinding.tvTotalPCACommissionBuyerExpenseDialog.setText(pcaCommChargeNF)
            val pcaTransportChargeNF =
                NumberFormat.getCurrencyInstance().format(TOTAL_pcaTransportationCharge)
            dialogBinding.tvTotalTransportChargeBuyerExpenseDialog.setText(pcaTransportChargeNF)
            val pcaMarketCessNF = NumberFormat.getCurrencyInstance().format(TOTAL_pcaMarketCess)
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
//        webSocketClient.connect()
        if (!isWebSocketConnected) {
            if (ConnectionCheck.isConnected(requireContext())) {

                webSocketClient.connect()
                isWebSocketConnected = true
            } else {
                commonUIUtility.showToast("No Internet Connection!")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isWebSocketConnected) {
            if (ConnectionCheck.isConnected(requireContext())) {
                webSocketClient.connect()
                isWebSocketConnected = true
            } else {
                commonUIUtility.showToast("No Internet Connection!")
            }
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
        newAuctionData = null
        lastPCAList = ArrayList()
        if (isWebSocketConnected) {
            webSocketClient.disconnect()
            isWebSocketConnected = false
        }
    }

}