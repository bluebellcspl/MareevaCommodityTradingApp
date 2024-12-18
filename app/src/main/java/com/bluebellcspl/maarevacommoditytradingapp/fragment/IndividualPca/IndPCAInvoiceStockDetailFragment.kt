package com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca

import ConnectionCheck
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
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
    private var _StockList: ArrayList<IndPCAInvoiceStockModelItem> = ArrayList()
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

        binding.btnNextIndPCAInvoiceStockDetailFragment.setOnClickListener {
            Log.d(TAG, "onCreateView: INVOICE_LIST_SIZE : ${_invoiceList.size}")
            if (_invoiceList.isNotEmpty()){
                navController.navigate(IndPCAInvoiceStockDetailFragmentDirections.actionIndPCAInvoiceStockDetailFragmentToIndPCAInvoiceStockAdjustmentFragment(_invoiceList.toTypedArray()))
            }else{
                commonUIUtility.showToast(getString(R.string.please_select_at_least_1_entry_alert_msg))
            }
        }

        binding.mChbSelectAllIndPCAInvoiceStockDetailFragment.setOnCheckedChangeListener { _, isChecked ->
            _StockList.forEach {
                it.isSelected = isChecked
                if (isChecked) {
//                    if (!_invoiceList.contains(it)) _invoiceList.add(it)
                    onItemSelected(it)
                } else {
//                    _invoiceList.remove(it)
                    onItemDeselected(it)
                }
            }
            Log.d("????", "Select All: Invoice list size: ${_invoiceList.size}")
            adapter.notifyDataSetChanged() // Refresh the adapter
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
                PrefUtil.setString(PrefUtil.KEY_IND_PCA_ID,dataList[0].InPCAId)
                adapter = IndPCAInvoiceStockBuyerAdapter(requireContext(),dataList,this@IndPCAInvoiceStockDetailFragment){
                    updateSelectAllCheckbox(_StockList)
                }
                binding.rcViewIndPCAInvoiceStockDetailFragment.adapter = adapter
                binding.rlSelectAllIndPCAInvoiceStockDetailFragment.visibility = View.VISIBLE
                binding.rcViewIndPCAInvoiceStockDetailFragment.invalidate()
                calculateData(dataList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "setOnClickListeners: ${e.message}")
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

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ON_RESUME")
        commonUIUtility.showProgress()
        Handler(Looper.getMainLooper()).postDelayed({
            _StockList.forEach {
                _invoiceList.forEach { model->
                    if (it.Date.equals(model.Date) && it.AvailableBags.equals(model.AvailableBags) && it.AvailableWeight.equals(model.AvailableWeight) && it.AvaliableAmount.equals(model.AvaliableAmount)){
                        it.isSelected = true
                    }
                }
            }
            commonUIUtility.dismissProgress()
            bindStockListView(_StockList)
        },1000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: ON_DESTROY_VIEW")
        _binding = null
    }

    override fun onItemSelected(model: IndPCAInvoiceStockModelItem) {
        Log.d(TAG, "onItemSelected: SELECTED_MODEL : DATE : ${model.Date}")
        Log.d(TAG, "onItemSelected: SELECTED_MODEL : BAGS : ${model.AvailableBags}")
        Log.d(TAG, "onItemSelected: SELECTED_MODEL : AMOUNT : ${model.AvaliableAmount}")
        Log.d(TAG, "onItemSelected: SELECTED_MODEL : WEIGHT : ${model.AvailableWeight}")
        Log.d(TAG, "onItemSelected: SELECTED_MODEL : IS_SELECTED : ${model.isSelected}")
//
//        if(!_invoiceList.contains(model)){
//            _invoiceList.add(model)
//        }
        val currentModel = _invoiceList.find {it.Date.equals(model.Date) && it.AvailableBags.equals(model.AvailableBags) && it.AvailableWeight.equals(model.AvailableWeight) && it.AvaliableAmount.equals(model.AvaliableAmount)}
        if (currentModel==null){
            _invoiceList.add(model)
        }
    }

    override fun onItemDeselected(model: IndPCAInvoiceStockModelItem) {
        Log.d(TAG, "onItemSelected: UNSELECTED_MODEL : DATE : ${model.Date}")
        Log.d(TAG, "onItemSelected: UNSELECTED_MODEL : BAGS : ${model.AvailableBags}")
        Log.d(TAG, "onItemSelected: UNSELECTED_MODEL : AMOUNT : ${model.AvaliableAmount}")
        Log.d(TAG, "onItemSelected: UNSELECTED_MODEL : WEIGHT : ${model.AvailableWeight}")
        Log.d(TAG, "onItemSelected: UNSELECTED_MODEL : IS_SELECTED : ${model.isSelected}")
        val currentModel = _invoiceList.find {it.Date.equals(model.Date) && it.AvailableBags.equals(model.AvailableBags) && it.AvailableWeight.equals(model.AvailableWeight) && it.AvaliableAmount.equals(model.AvaliableAmount)}
        _invoiceList.remove(currentModel)
    }
}

interface IndPCAInvoiceStockHelper{
    fun onItemSelected(model: IndPCAInvoiceStockModelItem)

    fun  onItemDeselected(model: IndPCAInvoiceStockModelItem)
}