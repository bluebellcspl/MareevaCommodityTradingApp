package com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.BuyerPrevAuctionAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentBuyerPreviousAuctionBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchBuyerPreviousAuctionAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.BuyerPrevAuctionMasterModel

class BuyerPreviousAuctionFragment : Fragment() {
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val navController by lazy { findNavController() }
    val TAG = "BuyerPreviousAuctionFragment"
    lateinit var binding: FragmentBuyerPreviousAuctionBinding
    private val navArgs by navArgs<BuyerPreviousAuctionFragmentArgs>()
    var PREV_AUCTION_SELECTED_DATE = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_buyer_previous_auction,
            container,
            false
        )
        PREV_AUCTION_SELECTED_DATE = navArgs.selectedPreviousAuctionDate
        if (ConnectionCheck.isConnected(requireContext()))
        {
            FetchBuyerPreviousAuctionAPI(requireContext(),this@BuyerPreviousAuctionFragment,PREV_AUCTION_SELECTED_DATE)
        }else{
            commonUIUtility.showToast(getString(R.string.no_internet_connection))
        }
        return binding.root
    }

    fun bindDataOfPrevAuction(modelData: BuyerPrevAuctionMasterModel) {
        try {
            binding.tvPreviousAuctionDateBuyerPrevAuctionFragment.setText("%s %s".format(resources.getString(R.string.date_lbl),modelData.Date))
            binding.tvPreviousAuctionAvgRateBuyerPrevAuctionFragment.setText("%s %s".format(resources.getString(R.string.avg_rate_lbl),modelData.LastPCATotalAvgRate))
            binding.tvPreviousAuctionPurchasedBagsBuyerPrevAuctionFragment.setText("%s %s".format(resources.getString(R.string.bags_lbl),modelData.LastTotalPurchasedBags))
            binding.tvPreviousAuctionTotalCostBuyerPrevAuctionFragment.setText("%s %s".format(resources.getString(R.string.total_cost_lbl),modelData.LastPCATotalCost))
            var adapter = BuyerPrevAuctionAdapter(requireContext(),modelData.PCAHeaderModel)
            binding.rcViewBuyerPrevAuctionFragment.adapter = adapter
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindDataOfPrevAuction: ${e.message}", )
        }
    }
}