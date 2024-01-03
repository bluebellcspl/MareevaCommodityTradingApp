package com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer

import android.app.AlertDialog
import android.icu.text.NumberFormat
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
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
//            binding.tvBuyerExpensesLiveAuctionFragment.setText(String.format("%.2f",totalBuyerExpense))

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
                for (ShopData in PCAData.ShopList) {
                    pcaBasic += ShopData.Amount.toDouble()
                }
                pcaTotalPurchasedBags = PCAData.TotalPurchasedBags.toInt()
                pcaMarketCess = (((pcaTotalPurchasedBags*PCAData.CommodityBhartiPrice.toDouble())/20)*((PCAData.BuyerUpperLimit.toDouble()+PCAData.BuyerLowerLimit.toDouble())/2)*PCAData.MarketCessCharge.toDouble())/100.00
                pcaCommCharge= (((pcaTotalPurchasedBags*PCAData.CommodityBhartiPrice.toDouble())/20)*((PCAData.BuyerUpperLimit.toDouble()+PCAData.BuyerLowerLimit.toDouble())/2)*PCAData.PCACommCharge.toDouble())/100.00
                gcaCommCharge = (((pcaTotalPurchasedBags*PCAData.CommodityBhartiPrice.toDouble())/20)*((PCAData.BuyerUpperLimit.toDouble()+PCAData.BuyerLowerLimit.toDouble())/2)*PCAData.GCACommCharge.toDouble())/100.00
                pcaLabourCharge = pcaTotalPurchasedBags*PCAData.LabourCharge.toDouble()
                pcaTransportationCharge = pcaTotalPurchasedBags*PCAData.TransportationCharge.toDouble()

                pcaExpense = pcaMarketCess+pcaCommCharge+gcaCommCharge+pcaLabourCharge+pcaTransportationCharge

                TOTAL_pcaMarketCess += pcaMarketCess
                TOTAL_pcaCommCharge += pcaCommCharge
                TOTAL_gcaCommCharge += gcaCommCharge
                TOTAL_pcaTransportationCharge += pcaTransportationCharge
                TOTAL_pcaLabourCharge += pcaLabourCharge
                TOTAL_pcaBasic += pcaBasic
                TOTAL_AuctionBags+=pcaTotalPurchasedBags
                TOTAL_pcaExpense = TOTAL_pcaCommCharge+TOTAL_gcaCommCharge+TOTAL_pcaLabourCharge+TOTAL_pcaMarketCess+TOTAL_pcaTransportationCharge
                TOTAL_AuctionCost = TOTAL_pcaBasic+TOTAL_pcaExpense
            }
            pcaTotalAmount = pcaBasic + pcaExpense
//            binding.edtPCABasicAmountLiveAuctionFragment.setText(String.format("%.2f", pcaBasic))
//            binding.tvPCAExpensesLiveAuctionFragment.setText(String.format("%.2f", pcaExpense))

            binding.edtPCAPurchasedBagsLiveAuctionFragment.setText(String.format("%s",TOTAL_AuctionBags.toString()))

            val PCATotalAmountNF = NumberFormat.getCurrencyInstance().format(TOTAL_AuctionCost).substring(1)
            binding.edtPCATotalAmountLiveAuctionFragment.setText(PCATotalAmountNF)
            var rate = TOTAL_AuctionCost / ((TOTAL_AuctionBags * COMMODITY_BHARTI.toDouble()) / 20.0)
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
            var TOTAL_pcaMarketCess = 0.0
            var TOTAL_pcaCommCharge = 0.0
            var TOTAL_gcaCommCharge = 0.0
            var TOTAL_pcaTransportationCharge = 0.0
            var TOTAL_pcaLabourCharge = 0.0
            var TOTAL_pcaExpense = 0.0
            var TOTAL_pcaBasic = 0.0
            var TOTAL_AuctionCost = 0.0

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


                pcaMarketCess = (((pcaTotalPurchasedBags*PCAData.CommodityBhartiPrice.toDouble())/20)*((PCAData.BuyerUpperLimit.toDouble()+PCAData.BuyerLowerLimit.toDouble())/2)*PCAData.MarketCessCharge.toDouble())/100.00
                pcaCommCharge= (((pcaTotalPurchasedBags*PCAData.CommodityBhartiPrice.toDouble())/20)*((PCAData.BuyerUpperLimit.toDouble()+PCAData.BuyerLowerLimit.toDouble())/2)*PCAData.PCACommCharge.toDouble())/100.00
                gcaCommCharge = (((pcaTotalPurchasedBags*PCAData.CommodityBhartiPrice.toDouble())/20)*((PCAData.BuyerUpperLimit.toDouble()+PCAData.BuyerLowerLimit.toDouble())/2)*PCAData.GCACommCharge.toDouble())/100.00
                pcaLabourCharge = pcaTotalPurchasedBags*PCAData.LabourCharge.toDouble()
                pcaTransportationCharge = pcaTotalPurchasedBags*PCAData.TransportationCharge.toDouble()

                pcaExpense += pcaMarketCess+pcaCommCharge+gcaCommCharge+pcaLabourCharge+pcaTransportationCharge
                pcaTotalAmount += pcaBasic + pcaExpense

                TOTAL_pcaMarketCess += pcaMarketCess
                TOTAL_pcaCommCharge += pcaCommCharge
                TOTAL_gcaCommCharge += gcaCommCharge
                TOTAL_pcaTransportationCharge += pcaTransportationCharge
                TOTAL_pcaLabourCharge += pcaLabourCharge
                TOTAL_pcaBasic += pcaBasic
                TOTAL_pcaExpense = TOTAL_pcaCommCharge+TOTAL_gcaCommCharge+TOTAL_pcaLabourCharge+TOTAL_pcaMarketCess+TOTAL_pcaTransportationCharge
                TOTAL_AuctionCost = TOTAL_pcaBasic+TOTAL_pcaExpense
            }
            val pcaBasicNF = NumberFormat.getCurrencyInstance().format(pcaBasic).substring(1)
            dialogBinding.tvTotalBasicAmountBuyerExpenseDialog.setText(pcaBasicNF)
            val pcaLabourChargeNF = NumberFormat.getCurrencyInstance().format(TOTAL_pcaLabourCharge).substring(1)
            dialogBinding.tvTotalLabourChargeBuyerExpenseDialog.setText(pcaLabourChargeNF)
            val gcaCommChargeNF = NumberFormat.getCurrencyInstance().format(TOTAL_gcaCommCharge).substring(1)
            dialogBinding.tvTotalGCACommissionBuyerExpenseDialog.setText(gcaCommChargeNF)
            val pcaCommChargeNF = NumberFormat.getCurrencyInstance().format(TOTAL_pcaCommCharge).substring(1)
            dialogBinding.tvTotalPCACommissionBuyerExpenseDialog.setText(pcaCommChargeNF)
            val pcaTransportChargeNF = NumberFormat.getCurrencyInstance().format(TOTAL_pcaTransportationCharge).substring(1)
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
                "",
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

    override fun onDestroyView() {
        webSocketClient.disconnect()
        super.onDestroyView()
    }
}