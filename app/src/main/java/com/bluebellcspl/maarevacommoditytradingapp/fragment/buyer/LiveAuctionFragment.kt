package com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer

import android.app.AlertDialog
import android.icu.text.NumberFormat
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.LiveAuctionListAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.BuyerExpenseDialogLayoutBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentLiveAuctionBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.POSTAuctionStartStopAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.AuctionDetailsModel
import com.bluebellcspl.maarevacommoditytradingapp.model.ExpandableObject
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionPCAListModel
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAListModel
import com.bluebellcspl.maarevacommoditytradingapp.model.POSTAuctionStartStopAPIModel
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.RecyclerViewHelper
import com.bluebellcspl.maarevacommoditytradingapp.webSocketHelper.WebSocketClient

class LiveAuctionFragment : Fragment(), RecyclerViewHelper {
    lateinit var binding: FragmentLiveAuctionBinding
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val TAG = "LiveAuctionFragment"
    private lateinit var webSocketClient: WebSocketClient
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
            "ws://maarevaapi.bbcspldev.in/MaarevaApi/MaarevaApi/BuyersLiveAuctionRtr?CommodityId=$commodityId&Date=${DateUtility().getCompletionDate()}&CompanyCode=$companyCode&BuyerRegId=$buyerRegId",
            viewLifecycleOwner,
            ::onMessageReceived
        )

        webSocketClient.connect()

        setOnClickListener()
        return binding.root
    }

    private fun setOnClickListener() {
        try {
            binding.cvBuyerExpenseLiveAuctionFragment.setOnClickListener {
                if (lastPCAList.isNotEmpty()) {
                    showBuyerExpensesPopup(newAuctionData!!)
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
                Log.d(TAG, "onMessageReceived: LAST_PCA_LIST : $lastPCAList")
                Log.d(TAG, "onMessageReceived: CURRENT_PCA_LIST : $dataList")
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
            //Buyer Calculations
            binding.edtBuyerAllocatedBagsLiveAuctionFragment.setText(dataList.AllocatedBag)
            binding.edtBuyerBasicAmountLiveAuctionFragment.setText(dataList.Basic)
            val BuyerTotalAmountNF = NumberFormat.getCurrencyInstance().format(dataList.TotalCost.toDouble()).substring(1)
            binding.edtBuyerTotalAmountLiveAuctionFragment.setText(BuyerTotalAmountNF)
            var totalBuyerExpense =
                dataList.TotalGCAComm.toDouble() + dataList.TotalPCAComm.toDouble() + dataList.TotalMarketCess.toDouble() + dataList.TotalLabourCharge.toDouble() + dataList.TotalTransportationCharge.toDouble()
            binding.tvBuyerExpensesLiveAuctionFragment.setText(
                String.format(
                    "%.2f",
                    totalBuyerExpense
                )
            )

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
            binding.edtPCABasicAmountLiveAuctionFragment.setText(String.format("%.2f", pcaBasic))
            binding.edtPCAPurchasedBagsLiveAuctionFragment.setText(
                String.format(
                    "%s",
                    pcaTotalPurchasedBags.toString()
                )
            )
            binding.tvPCAExpensesLiveAuctionFragment.setText(String.format("%.2f", pcaExpense))

            val PCATotalAmountNF = NumberFormat.getCurrencyInstance().format(pcaTotalAmount).substring(1)
            binding.edtPCATotalAmountLiveAuctionFragment.setText(PCATotalAmountNF)
            var rate = pcaBasic / ((pcaTotalPurchasedBags * COMMODITY_BHARTI.toDouble()) / 20.0)
            val RateNF = NumberFormat.getCurrencyInstance().format(rate).substring(1)
            binding.tvPCAAvgRateLiveAuctionFragment.setText(RateNF)
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

            //PCAs Calculations
            var pcaBasic = 0.0
            var pcaExpense = 0.0
            var pcaTotalAmount = 0.0
            var pcaTotalPurchasedBags = 0
            var pcaMarketCess = 0.0
            var pcaCommCharge = 0.0
            var gcaCommCharge = 0.0
            var pcaTransportationCharge = 0.0
            var pcaLabourCharge = 0.0
            for (PCAData in dataList.PCAList) {
                for (ShopData in PCAData.ShopList) {
                    pcaBasic += ShopData.Amount.toDouble()
                }
                pcaTotalPurchasedBags += PCAData.TotalPurchasedBags.toInt()
                pcaExpense += PCAData.PCACommCharge.toDouble() + PCAData.GCACommCharge.toDouble() + PCAData.TransportationCharge.toDouble() + PCAData.LabourCharge.toDouble()
                pcaTotalAmount += pcaBasic + pcaExpense
                pcaMarketCess += PCAData.MarketCessCharge.toDouble()
                pcaCommCharge += PCAData.PCACommCharge.toDouble()
                gcaCommCharge += PCAData.GCACommCharge.toDouble()
                pcaTransportationCharge += PCAData.TransportationCharge.toDouble()
                pcaLabourCharge += PCAData.LabourCharge.toDouble()
            }
            dialogBinding.tvTotalBasicAmountBuyerExpenseDialog.setText(
                String.format(
                    "%.2f",
                    pcaBasic
                )
            )
            dialogBinding.tvTotalLabourChargeBuyerExpenseDialog.setText(
                String.format(
                    "%.2f",
                    pcaLabourCharge
                )
            )
            dialogBinding.tvTotalGCACommissionBuyerExpenseDialog.setText(
                String.format(
                    "%.2f",
                    gcaCommCharge
                )
            )
            dialogBinding.tvTotalPCACommissionBuyerExpenseDialog.setText(
                String.format(
                    "%.2f",
                    pcaCommCharge
                )
            )
            dialogBinding.tvTotalTransportChargeBuyerExpenseDialog.setText(
                String.format(
                    "%.2f",
                    pcaTransportationCharge
                )
            )
            dialogBinding.tvTotalMarketCessBuyerExpenseDialog.setText(
                String.format(
                    "%.2f",
                    pcaMarketCess
                )
            )

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "showBuyerExpenesePopup: ${e.message}")
        }
    }

    override fun onItemClick(postion: Int, onclickType: String) {
        try {
            var isAuctionStop = ""
            if (onclickType.equals("start", true)) {
                isAuctionStop="false"
            } else {
                isAuctionStop="true"
            }
            val model = POSTAuctionStartStopAPIModel(
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString(),
                PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID, "").toString(),
                PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE, "").toString(),
                DateUtility().getCompletionDate(),
                lastPCAList[postion].PCARegId.toString(),
                "2",
                DateUtility().getyyyyMMdd(),
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString(),
                isAuctionStop
            )
            POSTAuctionStartStopAPI(
                requireContext(),
                requireActivity(),
                this@LiveAuctionFragment,
                model
            )
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

//    override fun onDestroy() {
//        webSocketClient.disconnect()
//        super.onDestroy()
//    }

    override fun onDestroyView() {
        webSocketClient.disconnect()
        super.onDestroyView()
    }
}