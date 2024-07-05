package com.bluebellcspl.maarevacommoditytradingapp.fragment.pca

import ConnectionCheck
import android.icu.text.NumberFormat
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.InvoiceMergedListAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentPCAInvoiceDetailBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchMergedInvoiceDataAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.POSTSelectedInvoiceShop
import com.bluebellcspl.maarevacommoditytradingapp.model.GCAData
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceEntryMergedModelItem
import com.bluebellcspl.maarevacommoditytradingapp.model.POSTSelectedInvoiceListModel
import java.text.DecimalFormat

class PCAInvoiceDetailFragment : Fragment(),InvoiceDetailHelper{
    var _binding: FragmentPCAInvoiceDetailBinding?=null
    val binding get() = _binding!!
    private val args by navArgs<PCAInvoiceDetailFragmentArgs>()
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    lateinit var mergedList: ArrayList<InvoiceEntryMergedModelItem>
    var postDataList= ArrayList<InvoiceEntryMergedModelItem>()
    private val TAG = "PCAInvoiceDetailFragment"
    lateinit var adapter:InvoiceMergedListAdapter
    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_p_c_a_invoice_detail,
            container,
            false
        )
        binding.edtToDateInvoiceDetailFragment.setText(args.endDate)
        binding.edtFromDateInvoiceDetailFragment.setText(args.startDate)
        if (ConnectionCheck.isConnected(requireContext()))
        {
            FetchMergedInvoiceDataAPI(requireContext(),this,args.startDate,args.endDate)
        }
        setOnClickListener()
        return binding.root
    }

    private fun setOnClickListener() {
        try {
            binding.btnProceedInvoiceDetailFragment.setOnClickListener {
                if (postDataList.isNotEmpty())
                {

                    var isAmountFilled = true
                    for (mergedEntry in postDataList)
                    {
                        if(mergedEntry.AmountT.toDouble()<1 && mergedEntry.WeightAfterAuctionInKg.toDouble()<1)
                        {
                            isAmountFilled = false
                        }
                    }

                    if (isAmountFilled)
                    {
                        postDataList.forEach {
                            Log.d(TAG, "setOnClickListener: POST_MERGED_DATA : $it")
                            Log.d(TAG, "setOnClickListener: =======================================================================")
                        }

                        postMergedCalculatedData()
                    }
                    else{
                        commonUIUtility.showToast("Please fill all fields!")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "setOnClickListener: ${e.message}", )
        }
    }

    fun bindRcView(dataList : ArrayList<InvoiceEntryMergedModelItem>)
    {
        mergedList = dataList
        try {
            if (dataList.isNotEmpty()){
                adapter = InvoiceMergedListAdapter(requireContext(),dataList,this)
                binding.rcViewInvoiceDetailFragment.adapter = adapter
                binding.rcViewInvoiceDetailFragment.invalidate()
            }else
            {
//                binding.rcViewInvoiceDetailFragment.adapter = null
//                binding.rcViewInvoiceDetailFragment.invalidate()
                commonUIUtility.showToast(getString(R.string.no_data_found))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindRcView: ${e.message}", )
        }
    }

    fun postMergedCalculatedData(){
        try {
            val postInvoiceSelectedList = ArrayList<GCAData>()
            for (shop in postDataList) {
                if (shop.AmountT.toDouble() > 0.0 && shop.BagsT.toDouble() > 0.0) {

                    val gcaDataModel = GCAData(
                        "Update",
                        "",
                        "",
                        shop.BhartiPrice,
                        "",
                        shop.CommodityId,
                        PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE, "").toString(),
                        DateUtility().getyyyyMMdd(),
                        PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString(),
                        "",
                        DateUtility().formatToyyyyMMdd(shop.InvoiceDate),
                        ""+shop.GCAInvoiceId,
                        ""+shop.GCAInvoiceNo,
                        ""+shop.InvoiceAmount,
                        ""+shop.InvoiceApproxKg,
                        ""+shop.InvoiceKg,
                        ""+shop.InvoiceRate,
                        "",
                        "",
                        PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString(),
                        PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString(),
                        "",
                        "",
                        DateUtility().getyyyyMMdd(),
                        PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString(),
                        ""+shop.WeightAfterAuctionInKg
                    )

                    postInvoiceSelectedList.add(gcaDataModel)
                }
            }

            postInvoiceSelectedList.forEach {
                Log.d(TAG, "onSaveButtonClick: POST_SELECTED_INVOICE_LIST_ITEM : $it")
            }

            val postSelectedDataModel = POSTSelectedInvoiceListModel(
                PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,"").toString(),
                binding.edtFromDateInvoiceDetailFragment.text.toString().trim(),
                postInvoiceSelectedList,
                PrefUtil.getSystemLanguage()!!,
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString(),
                binding.edtToDateInvoiceDetailFragment.text.toString().trim()
            )

            if (ConnectionCheck.isConnected(requireContext()))
            {
                POSTSelectedInvoiceShop(requireContext(),this,postSelectedDataModel)
//                redirectToInvoiceDetailFragment()
            }
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "postMergedCalculatedData: ${e.message}", )
        }
    }

    override fun getCalculatedData(dataList: ArrayList<InvoiceEntryMergedModelItem>) {
        try {
            postDataList = dataList
            var grandTotalAmount = 0.0
            var grandTotalWeight = 0.0
            var grandTotalBags = 0.0
            for (model in dataList)
            {
                grandTotalAmount+=model.AmountT.toDouble()
                grandTotalBags +=model.BagsT.toDouble()
                grandTotalWeight += model.WeightAfterAuctionInKg.toDouble()
            }
            var formattedWeight = DecimalFormat("0.00").format(grandTotalWeight)
            var formattedAmount = DecimalFormat("0.00").format(grandTotalAmount)
            var formattedBags = DecimalFormat("0.00").format(grandTotalBags)
            var formattedRate = DecimalFormat("0.00").format((grandTotalAmount/(grandTotalWeight/20.0)))

            val weightNF = NumberFormat.getCurrencyInstance().format(formattedWeight.toDouble()).substring(1)
            weightNF.plus("KG")

            val amountNF = NumberFormat.getCurrencyInstance().format(formattedAmount.toDouble()).substring(1)
            val rateNF = NumberFormat.getCurrencyInstance().format(formattedRate.toDouble()).substring(1)

            binding.tvTotBagInvoiceDetailFragment.setText(formattedBags)
            binding.tvTotalAmountInvoiceDetailFragment.setText(amountNF)
            binding.tvTotWeightInvoiceDetailFragment.setText(weightNF)
            binding.tvTotalRateInvoiceDetailFragment.setText(rateNF)

            if (grandTotalBags>0.0)
            {
                binding.llGrandTotalInvoiceDetailFragment.visibility = View.VISIBLE
            }else
            {
                binding.llGrandTotalInvoiceDetailFragment.visibility = View.GONE
            }


        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "getCalculatedData: ${e.message}", )
        }
    }

    fun refreshData()
    {
        navController.navigate(PCAInvoiceDetailFragmentDirections.actionPCAInvoiceDetailFragmentToInvoiceStockFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

interface InvoiceDetailHelper{
    fun getCalculatedData(dataList:ArrayList<InvoiceEntryMergedModelItem>)
}