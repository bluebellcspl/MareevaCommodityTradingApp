package com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca

import ConnectionCheck
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.IndPCAInvoiceStockBuyerAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentIndPCAInvoiceStockDetailBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAInvoiceStockFragment.IndPCAInvoiceStockFetchAPIModel
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchIndPCAInvoiceStockAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoiceStockModelItem


class IndPCAInvoiceStockDetailFragment : Fragment(),IndPCAInvoiceStockHelper {
    var _binding: FragmentIndPCAInvoiceStockDetailBinding? = null
    val binding get() = _binding!!
    val TAG = "IndPCAInvoiceStockDetailFragment"
    private val commonUIUtility: CommonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val navController: NavController by lazy { findNavController() }
    private lateinit  var _StockList: ArrayList<IndPCAInvoiceStockModelItem>
    private var _invoiceList:ArrayList<IndPCAInvoiceStockModelItem> = ArrayList()
    private lateinit var adapter:IndPCAInvoiceStockBuyerAdapter
    private val args by navArgs<IndPCAInvoiceStockDetailFragmentArgs>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_ind_p_c_a_invoice_stock_detail, container, false)
        binding.rlSelectAllIndPCAInvoiceStockDetailFragment.visibility = View.GONE
        binding.rcViewIndPCAInvoiceStockDetailFragment.setHasFixedSize(true)
        callAPI()
        binding.tvBuyerNameIndPCAInvoiceStockDetailFragment.text = args.buyerStockModel.BuyerName
        if (PrefUtil.getSystemLanguage().equals("gu")){
            binding.tvCommodityIndPCAInvoiceStockDetailFragment.text = args.buyerStockModel.CommodityName
        }else{
            binding.tvCommodityIndPCAInvoiceStockDetailFragment.text = args.buyerStockModel.CommodityName
        }
        binding.mChbSelectAllIndPCAInvoiceStockDetailFragment.setOnCheckedChangeListener { _, isChecked ->
            _StockList.forEach {
                it.isSelected = isChecked
                if (isChecked) {
                    if (!_invoiceList.contains(it)) _invoiceList.add(it)
                } else {
                    _invoiceList.remove(it)
                }
            }
            Log.d("????", "Select All: Invoice list size: ${_invoiceList.size}")
            adapter.notifyDataSetChanged() // Refresh the adapter
        }

        binding.btnNextIndPCAInvoiceStockDetailFragment.setOnClickListener {
            if (_invoiceList.isNotEmpty()) {
                _invoiceList.forEach {
                    Log.d("????", "onCreateView: ")
                    Log.d("????", "onCreateView: DATE: ${it.Date}")
                    Log.d("????", "onCreateView: BAGS: ${it.AvailableBags}")
                    Log.d("????", "onCreateView: RATE: ${it.BillRate}")
                    Log.d("????", "onCreateView: WEIGHT: ${it.AvailableWeight}")
                    Log.d("????", "onCreateView: AMOUNT: ${it.AvaliableAmount}")
                    Log.d("????", "onCreateView: =======================================================================")
                }
            }else{
                commonUIUtility.showToast("Please Select At least 1 entry")
            }
        }

        return binding.root
    }

    private fun callAPI(){
        if (ConnectionCheck.isConnected(requireContext())){
            val model = IndPCAInvoiceStockFetchAPIModel(
                "All",
                ""+args.buyerStockModel.BuyerId,
                ""+args.buyerStockModel.CommodityId,
                ""+ PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,""),
                "",
                ""+ PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,""),
                ""+PrefUtil.getSystemLanguage().toString()
            )
            FetchIndPCAInvoiceStockAPI(requireContext(),this@IndPCAInvoiceStockDetailFragment,model)
        }else{
            commonUIUtility.showToast(requireContext().getString(R.string.no_internet_connection))
        }
    }

    fun bindStockListView(dataList:ArrayList<IndPCAInvoiceStockModelItem>) {
        try {
            _StockList = dataList
            if(dataList.isNotEmpty())
            {
                calculateData(dataList)
                adapter = IndPCAInvoiceStockBuyerAdapter(requireContext(),dataList,this@IndPCAInvoiceStockDetailFragment){
                    updateSelectAllCheckbox(_StockList)
                }
                binding.rcViewIndPCAInvoiceStockDetailFragment.adapter = adapter
                binding.rlSelectAllIndPCAInvoiceStockDetailFragment.visibility = View.VISIBLE
                binding.rcViewIndPCAInvoiceStockDetailFragment.invalidate()
            }else
            {
                binding.rcViewIndPCAInvoiceStockDetailFragment.adapter = null
                binding.rcViewIndPCAInvoiceStockDetailFragment.invalidate()
                binding.rlSelectAllIndPCAInvoiceStockDetailFragment.visibility = View.GONE
                _StockList.clear()
                _invoiceList.clear()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "setOnClickListeners: ${e.message}")
        }
    }

    private fun calculateData(dataList: ArrayList<IndPCAInvoiceStockModelItem>){
        try {
            var bags = 0.0
            var weight = 0.0
            var amount = 0.0
            var rate = 0.0

            dataList.forEach {
                bags += it.AvailableBags.toDouble()
                weight += it.AvailableWeight.toDouble()
                amount += it.AvaliableAmount.toDouble()
            }
            rate = amount/(weight/20.0)

            binding.tvAmountIndPCAInvoiceStockDetailFragment.text = commonUIUtility.numberCurrencyFormat(commonUIUtility.formatDecimal(amount).toDouble())
            binding.tvRateIndPCAInvoiceStockDetailFragment.text = commonUIUtility.numberCurrencyFormat(commonUIUtility.formatDecimal(rate).toDouble())
            binding.tvWeightIndPCAInvoiceStockDetailFragment.text = commonUIUtility.formatDecimal(weight)
            binding.tvBagsIndPCAInvoiceStockDetailFragment.text = commonUIUtility.formatDecimal(bags)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "calculateData: ${e.message}", )
        }
    }

    override fun onItemSelected(model: Any) {
        if (model is IndPCAInvoiceStockModelItem && !_invoiceList.contains(model)) {
            _invoiceList.add(model)
            Log.d("????", "onItemSelected: Added to invoice list. Size: ${_invoiceList.size}")
        }
    }

    override fun onItemDeselected(model: Any) {
        if (model is IndPCAInvoiceStockModelItem && _invoiceList.contains(model)) {
            _invoiceList.remove(model)
            Log.d("????", "onItemDeselected: Removed from invoice list. Size: ${_invoiceList.size}")
        }
    }


    private fun updateSelectAllCheckbox(dataList: ArrayList<IndPCAInvoiceStockModelItem>) {
        binding.mChbSelectAllIndPCAInvoiceStockDetailFragment.setOnCheckedChangeListener(null) // Temporarily remove listener
        binding.mChbSelectAllIndPCAInvoiceStockDetailFragment.isChecked = dataList.all { it.isSelected }
        binding.mChbSelectAllIndPCAInvoiceStockDetailFragment.setOnCheckedChangeListener { _, isChecked ->
            _StockList.forEach { it.isSelected = isChecked }
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

interface IndPCAInvoiceStockHelper{
    fun onItemSelected(model: Any)

    fun  onItemDeselected(model: Any)
}