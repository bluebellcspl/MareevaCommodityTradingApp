package com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.BuyerPrevAuctionAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.FileDownloader
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.constants.URLHelper
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentBuyerPreviousAuctionBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchBuyerPreviousAuctionAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.BuyerPrevAuctionMasterModel

class BuyerPreviousAuctionFragment : Fragment() {
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val navController by lazy { findNavController() }
    val TAG = "BuyerPreviousAuctionFragment"
    lateinit var binding: FragmentBuyerPreviousAuctionBinding
    private val navArgs by navArgs<BuyerPreviousAuctionFragmentArgs>()
    private val fileDownloader by lazy { FileDownloader.getInstance(requireContext()) }
    lateinit var menuHost: MenuHost
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
        if (PREV_AUCTION_SELECTED_DATE.isBlank() || PREV_AUCTION_SELECTED_DATE.equals(""))
        {
            PREV_AUCTION_SELECTED_DATE = DateUtility().getCompletionDate()
        }
        if (ConnectionCheck.isConnected(requireContext()))
        {
            FetchBuyerPreviousAuctionAPI(requireContext(),this@BuyerPreviousAuctionFragment,PREV_AUCTION_SELECTED_DATE)
        }else{
            commonUIUtility.showToast(getString(R.string.no_internet_connection))
        }

        menuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.file_download_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.btn_DownloadReport -> {
                     downloadAuctionReport()
                    }
                    R.id.btn_DownloadDetailReport->{
                        downloadAuctionDetailReport()
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)
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

    fun downloadAuctionReport()
    {
        try {
            val fileURL = URLHelper.BUYER_AUCTION_REPORT.replace("<COMMODITY_ID>",PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"").toString()).replace("<DATE>",
                PREV_AUCTION_SELECTED_DATE).replace("<COMPANY_CODE>",PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,"").toString()).replace("<BUYER_REG_ID>",PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString())

            fileDownloader.downloadFile(fileURL,"Auction_Report_$PREV_AUCTION_SELECTED_DATE.xlsx")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "downloadAuctionReport: ${e.message}")
        }
    }

    fun downloadAuctionDetailReport()
    {
        try {
            val fileUrl = URLHelper.BUYER_AUCTION_DETAIL_REPORT.replace("<COMMODITY_ID>",PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"").toString()).replace("<DATE>",
                PREV_AUCTION_SELECTED_DATE).replace("<COMPANY_CODE>",PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,"").toString()).replace("<BUYER_REG_ID>",PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString())

            fileDownloader.downloadFile(fileUrl,"Auction_Detail_Report_$PREV_AUCTION_SELECTED_DATE.xlsx")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "downloadAuctionDetailReport: ${e.message}")
        }
    }
}