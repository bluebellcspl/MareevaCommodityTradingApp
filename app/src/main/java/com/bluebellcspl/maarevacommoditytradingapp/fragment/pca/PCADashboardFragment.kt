package com.bluebellcspl.maarevacommoditytradingapp.fragment.pca

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
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentPCADashboardBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchCityMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchCommodityMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchPCAAuctionDetailAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchShopMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchTransportationMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAAuctionDetailModel


class PCADashboardFragment : Fragment() {
    lateinit var binding:FragmentPCADashboardBinding
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val navController by lazy { findNavController() }
    val TAG = "PCADashboardFragment"
    lateinit var menuHost: MenuHost
    var COMMODITY_BHARTI = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_p_c_a_dashboard, container, false)
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        FetchCityMasterAPI(requireContext(),requireActivity())
        FetchTransportationMasterAPI(requireContext(),requireActivity())
        FetchCommodityMasterAPI(requireContext(), requireActivity())
        FetchShopMasterAPI(requireContext(), requireActivity())
        FetchPCAAuctionDetailAPI(requireContext(), requireActivity(), this)
        menuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.ds_menu,menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId){
                    R.id.btn_Profile->{
                        navController.navigate(PCADashboardFragmentDirections.actionPCADashboardFragmentToProfileOptionFragment())
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)
        var commodityId = PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID, "")
        COMMODITY_BHARTI =
            DatabaseManager.ExecuteScalar(Query.getCommodityBhartiByCommodityId(commodityId.toString()))!!
        binding.tvCommodityNewPCADashboardFragment.setText(PrefUtil.getString(PrefUtil.KEY_COMMODITY_NAME,""))
        binding.tvDateNewPCADashboardFragment.setText(DateUtility().getCompletionDate())
        setOnClickListeners()
        return binding.root
    }

    private fun setOnClickListeners() {
        try {
            binding.cvPCAAuctionNewPCADashboardFragment.setOnClickListener {
                navController.navigate(PCADashboardFragmentDirections.actionPCADashboardFragmentToPCAAuctionFragment("Hello"))
            }
            binding.btnPurchasedBagsNewPCADashboardFragment.setOnClickListener {
                navController.navigate(PCADashboardFragmentDirections.actionPCADashboardFragmentToPCAAuctionFragment("Hello"))
            }
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "setOnClickListeners: ${e.message}", )
        }
    }
    fun bindBuyerAllocatedData(modelData:PCAAuctionDetailModel)
    {
        try {
            //Allocation Calculation
            if (modelData.PerBoriRate.isEmpty())
            {
                modelData.PerBoriRate = "0"
            }
            if (modelData.PerLabourCharge.isEmpty())
            {
                modelData.PerLabourCharge = "0"
            }
            binding.tvAllocatedBagsNewPCADashboardFragment.setText("%s %s".format(resources.getString(R.string.bags_lbl),modelData.BuyerBori))
            var pcaAllocatedBags = modelData.BuyerBori.toInt()
            var commodityBhartiPrice = modelData.CommodityBhartiPrice.toDouble()
            var buyerUpperLimit = modelData.BuyerUpperPrice.toDouble()
            var buyerLowerLimit = modelData.BuyerLowerPrice.toDouble()

            var pcaMarketCess = (((pcaAllocatedBags*COMMODITY_BHARTI.toDouble())/20)*((buyerUpperLimit+buyerLowerLimit)/2)*modelData.MCessRate.toDouble())/100.00
            var pcaCommRate = (((pcaAllocatedBags*COMMODITY_BHARTI.toDouble())/20)*((buyerUpperLimit+buyerLowerLimit)/2)*modelData.PCACommRate.toDouble())/100.00
            var gcaCommRate = (((pcaAllocatedBags*COMMODITY_BHARTI.toDouble())/20)*((buyerUpperLimit+buyerLowerLimit)/2)*modelData.GCACommRate.toDouble())/100.00
            var pcaLabourCharge = pcaAllocatedBags * modelData.PerLabourCharge.toDouble()
            var pcaTransportationCharge = pcaAllocatedBags * modelData.PerBoriRate.toDouble()
            var pcaAllocatedBasic = modelData.BuyerPCABudget.toDouble()

            var totalPCABudget = pcaMarketCess+pcaCommRate+gcaCommRate+pcaLabourCharge+pcaTransportationCharge+pcaAllocatedBasic

            val AllocatedBuyerCost = NumberFormat.getCurrencyInstance().format(totalPCABudget).substring(1)
            binding.tvAllocatedTotalCostNewPCADashboardFragment.setText("%s %s".format(resources.getString(R.string.cost_lbl),AllocatedBuyerCost))
            var rate = totalPCABudget / ((pcaAllocatedBags * COMMODITY_BHARTI.toDouble()) / 20.0)
            val AllocatedBuyerAvgRate = NumberFormat.getCurrencyInstance().format(rate).substring(1)
            binding.tvAllocatedRateNewPCADashboardFragment.setText("%s %s".format(resources.getString(R.string.rate_lbl),AllocatedBuyerAvgRate))

            //Purchased Calculation

            var pcaTotalPurchasedBag = modelData.TotalPurchasedBags.toInt()
            binding.tvPurchasedBagsNewPCADashboardFragment.setText("%s %d".format(resources.getString(R.string.bags_lbl),pcaTotalPurchasedBag))

            var purchased_pcaMarketCess = (((pcaTotalPurchasedBag*COMMODITY_BHARTI.toDouble())/20)*((buyerUpperLimit+buyerLowerLimit)/2)*modelData.MCessRate.toDouble())/100.00
            var purchased_pcaCommRate = (((pcaTotalPurchasedBag*COMMODITY_BHARTI.toDouble())/20)*((buyerUpperLimit+buyerLowerLimit)/2)*modelData.PCACommRate.toDouble())/100.00
            var purchased_gcaCommRate = (((pcaTotalPurchasedBag*COMMODITY_BHARTI.toDouble())/20)*((buyerUpperLimit+buyerLowerLimit)/2)*modelData.GCACommRate.toDouble())/100.00
            var purchased_pcaLabourCharge = pcaTotalPurchasedBag * modelData.PerLabourCharge.toDouble()
            var purchased_pcaTransportationCharge = pcaTotalPurchasedBag * modelData.PerBoriRate.toDouble()
            var purchased_pcaBasic = modelData.TotalCost.toDouble()

            var PCAtotalPurchasedCost = purchased_pcaMarketCess+purchased_pcaCommRate+purchased_gcaCommRate+purchased_pcaLabourCharge+purchased_pcaTransportationCharge+purchased_pcaBasic

            val PurchasedPCACost = NumberFormat.getCurrencyInstance().format(PCAtotalPurchasedCost).substring(1)
            binding.tvPurchasedTotalCostNewPCADashboardFragment.setText("%s %s".format(resources.getString(R.string.cost_lbl),PurchasedPCACost))
            var purchased_Avgrate = 0.0
            if (pcaTotalPurchasedBag.toInt()>0 && PCAtotalPurchasedCost>0.0)
            {
                purchased_Avgrate = PCAtotalPurchasedCost / ((pcaTotalPurchasedBag * COMMODITY_BHARTI.toDouble()) / 20.0)
            }
            val PcaPurchasedAvgRate = NumberFormat.getCurrencyInstance().format(purchased_Avgrate).substring(1)
            binding.tvPurchasedAvgRateNewPCADashboardFragment.setText("%s %s".format(resources.getString(R.string.rate_lbl),PcaPurchasedAvgRate))


        }catch (e:Exception)
        {
            e.message
            Log.e(TAG, "bindBuyerAllocatedData: ", )
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
}