package com.bluebellcspl.maarevacommoditytradingapp.fragment.pca

import ConnectionCheck
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.PrevAuctionShopListAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.FileDownloader
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.constants.URLHelper
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentPCAPreviousAuctionBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchPCAPreviousAuctionAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAPrevAuctionMasterModel


class PCAPreviousAuctionFragment : Fragment() {
    var _binding:FragmentPCAPreviousAuctionBinding? = null
    val binding get() = _binding!!
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val navController by lazy { findNavController() }
    val TAG = "PCAPreviousAuctionFragment"
    private val fileDownloader by lazy { FileDownloader.getInstance(requireContext()) }
    lateinit var menuHost: MenuHost
    var PREV_AUCTION_SELECTED_DATE = ""
    private val args by navArgs<PCAPreviousAuctionFragmentArgs>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_p_c_a_previous_auction, container, false)
        PREV_AUCTION_SELECTED_DATE = args.selectPreviousAuctionDate

        if (PREV_AUCTION_SELECTED_DATE.isBlank() || PREV_AUCTION_SELECTED_DATE.equals(""))
        {
            PREV_AUCTION_SELECTED_DATE = DateUtility().getCompletionDate()
        }
        if (ConnectionCheck.isConnected(requireContext()))
        {
            FetchPCAPreviousAuctionAPI(requireContext(),this@PCAPreviousAuctionFragment,PREV_AUCTION_SELECTED_DATE)
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

    fun bindDataOfPrevAuction(modelData: PCAPrevAuctionMasterModel) {
        try {
            binding.tvPreviousAuctionDatePCAPrevAuctionFragment.setText("%s %s".format(resources.getString(R.string.date_lbl),modelData.Date))
            binding.tvPreviousAuctionAvgRatePCAPrevAuctionFragment.setText("%s %s".format(resources.getString(R.string.avg_rate_lbl),modelData.LastPCATotalAvgRate))
            binding.tvPreviousAuctionPurchasedBagsPCAPrevAuctionFragment.setText("%s %s".format(resources.getString(R.string.bags_lbl),modelData.LastTotalPurchasedBags))
            binding.tvPreviousAuctionTotalCostPCAPrevAuctionFragment.setText("%s %s".format(resources.getString(R.string.total_cost_lbl),modelData.LastPCATotalCost))
            var shopList = modelData.PCAHeaderModel[0].PCADetailModel
            var adapter = PrevAuctionShopListAdapter(requireContext(),shopList)
            binding.rcViewPCAPrevAuctionFragment.adapter = adapter
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindDataOfPrevAuction: ${e.message}", )
        }
    }

    fun downloadAuctionReport()
    {
        try {
            val fileURL = URLHelper.PCA_AUCTION_REPORT.replace("<COMMODITY_ID>",
                PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"").toString()).replace("<DATE>",
                PREV_AUCTION_SELECTED_DATE).replace("<COMPANY_CODE>",
                PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,"").toString()).replace("<PCA_REG_ID>",
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString())

            fileDownloader.downloadFile(fileURL,"PCA_Auction_Report_$PREV_AUCTION_SELECTED_DATE.xlsx")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "downloadAuctionReport: ${e.message}")
        }
    }

    fun downloadAuctionDetailReport()
    {
        try {
            val fileUrl = URLHelper.PCA_AUCTION_DETAIL_REPORT.replace("<COMMODITY_ID>",
                PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"").toString()).replace("<DATE>",
                PREV_AUCTION_SELECTED_DATE).replace("<COMPANY_CODE>",
                PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,"").toString()).replace("<PCA_REG_ID>",
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString())

            fileDownloader.downloadFile(fileUrl,"PCA_Auction_Detail_Report_$PREV_AUCTION_SELECTED_DATE.xlsx")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "downloadAuctionDetailReport: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}