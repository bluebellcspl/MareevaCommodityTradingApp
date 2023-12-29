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


class BuyerDashboardFragment : Fragment() {
    lateinit var binding:FragmentBuyerDashboardBinding
    private val commonUIUtility by lazy { CommonUIUtility(requireContext())}
    private val navController by lazy { findNavController() }
    val TAG = "BuyerDashboardFragment"
    var newAuctionData: LiveAuctionMasterModel? = null
    var lastPCAList: ArrayList<LiveAuctionPCAListModel> = ArrayList()
    private lateinit var webSocketClient: WebSocketClient
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

        webSocketClient.connect()
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
            binding.tvAllocatedRateNewBuyerDashboardFragment.setText("%s".format(requireContext().getString(R.string.rate_lbl)))
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
            binding.tvPurchasedBagsNewBuyerDashboardFragment.setText("%s %s".format(requireContext().getString(R.string.bags_lbl),pcaTotalPurchasedBags.toString()))
//            binding.edtPCAPurchasedBagsLiveAuctionFragment.setText(String.format("%s",pcaTotalPurchasedBags.toString()))


            val PCATotalAmountNF = NumberFormat.getCurrencyInstance().format(pcaTotalAmount).substring(1)
            binding.tvPurchasedTotalCostNewBuyerDashboardFragment.setText("%s %s".format(requireContext().getString(R.string.cost_lbl),PCATotalAmountNF))
            var rate = pcaBasic / ((pcaTotalPurchasedBags * COMMODITY_BHARTI.toDouble()) / 20.0)
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
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onDestroy() {
        webSocketClient.disconnect()
        super.onDestroy()
    }
}