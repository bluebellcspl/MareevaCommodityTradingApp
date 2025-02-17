package com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca

import ConnectionCheck
import android.icu.text.NumberFormat
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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.IndPCAAuctionReportAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.FileDownloader
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.constants.URLHelper
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentIndPCAAuctionReportBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchIndPCAAuctionReport
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAAuctionReportModel

class IndPCAAuctionReportFragment : Fragment() {
    var _binding: FragmentIndPCAAuctionReportBinding? = null
    val binding get() = _binding!!
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val TAG = "IndPCAAuctionReportFragment"
    lateinit var menuHost: MenuHost
    lateinit var adapter:IndPCAAuctionReportAdapter
    private val arg by navArgs<IndPCAAuctionReportFragmentArgs>()
    var PREV_AUCTION_SELECTED_DATE = ""
    private val fileDownloader by lazy { FileDownloader.getInstance(requireContext()) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_ind_p_c_a_auction_report, container, false)
        binding.rcViewIndPCAAuctionReportFragment.layoutManager = LinearLayoutManager(requireContext())
        PREV_AUCTION_SELECTED_DATE = arg.selectedDate

        if (PREV_AUCTION_SELECTED_DATE.isBlank() || PREV_AUCTION_SELECTED_DATE.equals(""))
        {
            PREV_AUCTION_SELECTED_DATE = DateUtility().getCompletionDate()
        }

        if (ConnectionCheck.isConnected(requireContext()))
        {
            FetchIndPCAAuctionReport(requireContext(),this@IndPCAAuctionReportFragment,PREV_AUCTION_SELECTED_DATE)
        }else{
            commonUIUtility.showToast(requireContext().getString(R.string.no_internet_connection))
        }



        menuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.ind_pca_report_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.btn_DownloadPCAAuctionDetail -> {
//                        logoutDialog()
                        downloadAuctionDetailReport()
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)


        return binding.root
    }

    fun bindReportData(model:IndPCAAuctionReportModel){
        try {
            val stringBuilder = StringBuilder()
            stringBuilder.append(requireContext().getString(R.string.date_lbl) +" ${model.Date}")
            binding.tvPreviousAuctionDateIndPCAAuctionReportFragment.setText(stringBuilder.toString())
            stringBuilder.clear()

            val totalAvgRateNF = NumberFormat.getCurrencyInstance().format(model.TotalAveragePrice.toDouble()).substring(1)
            stringBuilder.append(requireContext().getString(R.string.avg_rate_lbl) +" $totalAvgRateNF")
            binding.tvPreviousAuctionAvgRateIndPCAAuctionReportFragment.setText(stringBuilder.toString())
            stringBuilder.clear()

            val totalAmountNF = NumberFormat.getCurrencyInstance().format(model.TotalAmount.toDouble()).substring(1)
            stringBuilder.append(requireContext().getString(R.string.cost_lbl) +" $totalAmountNF")
            binding.tvPreviousAuctionTotalCostIndPCAAuctionReportFragment.setText(stringBuilder.toString())
            stringBuilder.clear()

            stringBuilder.append(requireContext().getString(R.string.bags_lbl) +" ${model.TotalBags}")
            binding.tvPreviousAuctionPurchasedBagsIndPCAAuctionReportFragment.setText(stringBuilder.toString())
            stringBuilder.clear()

            val dataList = model.IndividualPCAAuctionHeaderModel[0].IndividualPCAAuctionDetail
            if (dataList.isNotEmpty()){
                adapter = IndPCAAuctionReportAdapter(requireContext(),dataList)
                binding.rcViewIndPCAAuctionReportFragment.adapter = adapter
                binding.rcViewIndPCAAuctionReportFragment.invalidate()
            }else
            {
                commonUIUtility.showToast(requireContext().getString(R.string.no_data_found))
            }
        }catch (e:Exception)
        {
            Log.e(TAG, "bindReportData: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun downloadAuctionDetailReport()
    {
        try {

            val fileUrl = URLHelper.IND_PCA_AUCTION_DETAIL_REPORT.replace("<COMMODITY_ID>",
                PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"").toString()).replace("<DATE>",
                PREV_AUCTION_SELECTED_DATE).replace("<COMPANY_CODE>",
                PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,"").toString()).replace("<PCA_REG_ID>",
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString()).replace("<LANGUAGE>",PrefUtil.getSystemLanguage()!!)

            val fileName = StringBuilder()
            fileName.append("PCA_Auction_Detail_Report_")
            fileName.append(PREV_AUCTION_SELECTED_DATE)
            fileName.append("_")
            fileName.append(DateUtility().generateUnixTimestamp())
            fileName.append(".xlsx")

            fileDownloader.downloadFile(fileUrl,fileName.toString(), "Downloading Report")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "downloadAuctionDetailReport: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}