package com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.IndPCAStockAdjustmentAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentIndPCAInvoiceStockAdjustmentBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoiceBagAdjustmentModel
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoiceStockModelItem
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAStockInsertItemModel
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceStockModelItem

class IndPCAInvoiceStockAdjustmentFragment : Fragment() {
    var _binding: FragmentIndPCAInvoiceStockAdjustmentBinding? = null
    val binding get() = _binding!!
    val TAG = "IndPCAInvoiceStockAdjustmentFragment"
    private val commonUIUtility: CommonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val navController: NavController by lazy { findNavController() }
    private val args by navArgs<IndPCAInvoiceStockAdjustmentFragmentArgs>()
    var _InvoiceSeletedList :ArrayList<IndPCAInvoiceStockModelItem> ? =null
    private val _AdjStockList by lazy { ArrayList<IndPCAStockInsertItemModel>() }
    lateinit var adapter : IndPCAStockAdjustmentAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_ind_p_c_a_invoice_stock_adjustment,
            container,
            false
        )
        _InvoiceSeletedList = ArrayList(args.invoiceSeletedList.toList())
        bindRcViewStock(_InvoiceSeletedList!!)
        return binding.root
    }

    private fun bindRcViewStock(dataList:ArrayList<IndPCAInvoiceStockModelItem>){
        if (dataList.isNotEmpty())
        {
            adapter = IndPCAStockAdjustmentAdapter(requireContext(),dataList)
            binding.rcViewIndPCAInvoiceStockAdjustmentFragment.adapter = adapter
            binding.rcViewIndPCAInvoiceStockAdjustmentFragment.invalidate()
        }else{
            binding.rcViewIndPCAInvoiceStockAdjustmentFragment.adapter = null
            binding.rcViewIndPCAInvoiceStockAdjustmentFragment.invalidate()
        }
    }

//    val model = IndPCAInvoiceBagAdjustmentModel(
//        stock.AvaliableAmount,
//        stock.BillApproxKg,
//        stock.AvailableBags,
//        stock.AvailableGST,
//        stock.BillKg,
//        stock.BillRate,
//        stock.AvailableTotalAmount,
//        stock.AvailableWeight,
//        stock.BuyerId,
//        stock.BuyerName,
//        stock.CommodityBhartiPrice,
//        stock.CommodityId,
//        stock.Cdate,
//        stock.CreateUser,
//        stock.Date,
//        stock.TotalPct,
//        stock.InStockId,
//        ""+stock.InPCAAuctionDetailId,
//        "",
//        ""+PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,""),
//        ""+PrefUtil.getString(PrefUtil.KEY_IND_PCA_ID,""),
//        stock.Udate,
//        stock.UpdateUser,
//    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}