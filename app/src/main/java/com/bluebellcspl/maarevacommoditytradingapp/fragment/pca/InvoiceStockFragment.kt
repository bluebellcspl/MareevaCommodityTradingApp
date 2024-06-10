package com.bluebellcspl.maarevacommoditytradingapp.fragment.pca

import ConnectionCheck
import android.icu.text.NumberFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.InvoiceStockAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentInvoiceStockBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchInvoiceStockAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceStockModel
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceStockModelItem
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.RecyclerViewHelper
import com.google.android.material.chip.Chip
import java.text.DecimalFormat

class InvoiceStockFragment : Fragment(),InvoiceStockHelper {
    val TAG = "InvoiceStockFragment"
    var _binding: FragmentInvoiceStockBinding?=null
    val binding get() = _binding!!
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    lateinit var adapter:InvoiceStockAdapter
    private val navController by lazy { findNavController() }
    private var _invoiceList:ArrayList<InvoiceStockModelItem> = ArrayList()
    private var receivedStockList:ArrayList<InvoiceStockModelItem> = ArrayList()
    private val menuHost:MenuHost by lazy { requireActivity() }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_invoice_stock, container, false)
        if (_invoiceList.isEmpty())
        {
            refreshList()
        }
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.invoice_menu,menu)

                val invoiceMenuItem = menu.findItem(R.id.btn_Invoice_Forward)
                invoiceMenuItem.setIcon(R.drawable.baseline_refresh_24)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId){
                    R.id.btn_Invoice_Forward->{
                        _invoiceList = ArrayList()
                        refreshList()
                    }
                }
                return true
            }
        },viewLifecycleOwner,Lifecycle.State.STARTED)
        setOnCLickListener()
        return _binding!!.root
    }

    private fun setOnCLickListener() {
        try {
            binding.btnNextInvoiceStockFragment.setOnClickListener {
                if (_invoiceList.isNotEmpty())
                {
                    _invoiceList.forEach { model->
                        Log.d(TAG, "setOnCLickListener: SELECT_ITEM : ${model.Date} - ${model.AvailableBags}")
                    }

                    navController.navigate(InvoiceStockFragmentDirections.actionInvoiceStockFragmentToInvoiceStockDetailFragment(_invoiceList.toTypedArray()))
                }else
                {
                    commonUIUtility.showToast(getString(R.string.please_select_at_least_1_entry_alert_msg))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "setOnCLickListener: ${e.message}", )
        }
    }

    fun bindInvoiceList(invoiceStockList:ArrayList<InvoiceStockModelItem>){
        try {
            if (invoiceStockList.isNotEmpty())
            {
                receivedStockList = invoiceStockList
                adapter = InvoiceStockAdapter(requireActivity(),invoiceStockList,this)
                binding.rcViewInvoiceStockFragment.adapter = adapter
                binding.rcViewInvoiceStockFragment.invalidate()
                calculateTOTAL(invoiceStockList)
            }else
            {
                binding.llStockDetailInvoiceStockFragment.visibility = View.GONE
            }
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "bindInvoiceList: ${e.message}", )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onItemSelected(model: InvoiceStockModelItem) {
        try {
            _invoiceList.add(model)
            Log.d(TAG, "onItemSelected: SIZE_AFTER_ADDING : ${_invoiceList.size}")
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "onItemSelected: ${e.message}", )
        }
    }

    override fun onItemDeselected(model: InvoiceStockModelItem) {
        try {
            _invoiceList.remove(model)
            Log.d(TAG, "onItemSelected: SIZE_AFTER_REMOVING : ${_invoiceList.size}")
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "onItemDeselected: ${e.message}", )
        }
    }

    private fun calculateTOTAL(dataList:ArrayList<InvoiceStockModelItem>){
        try {
            var TOT_BAGS = 0.0
            var TOT_WEIGHT = 0.0
            var TOT_AMOUNT = 0.0
            var bhartiPrice = 0.0
            for (model in dataList)
            {
                TOT_BAGS += model.AvailableBags.toDouble()
                TOT_WEIGHT += model.AvailableWeight.toDouble()
                TOT_AMOUNT += model.AvaliableAmount.toDouble()
                bhartiPrice = model.BhartiPrice.toDouble()
            }
            val formateTOTAL_BAGS = TOT_BAGS.toInt()
            val formateTOTAL_WEIGHT = NumberFormat.getCurrencyInstance().format(TOT_WEIGHT).substring(1)
            val formateTOTAL_AMOUNT = NumberFormat.getCurrencyInstance().format(TOT_AMOUNT).substring(1)

            val remainingBags = DecimalFormat("0.00").format((TOT_WEIGHT%bhartiPrice))

            val TOT_RATE = ( TOT_AMOUNT /((TOT_BAGS*bhartiPrice)/20.0))

            val formatedTOT_RATE = NumberFormat.getCurrencyInstance().format(TOT_RATE).substring(1)

            binding.llStockDetailInvoiceStockFragment.visibility = View.VISIBLE
            binding.edtBagsInvoiceStockFragment.setText(formateTOTAL_BAGS.toString())
            binding.tvAmountInvoiceStockFragment.setText(formateTOTAL_AMOUNT)
            binding.tvRateInvoiceStockFragment.setText(formatedTOT_RATE)
            binding.tvWeightInvoiceStockFragment.setText(formateTOTAL_WEIGHT)
            binding.tvRemainingKGInvoiceStockFragment.setText(remainingBags)
        }catch (e:Exception)
        {
            binding.llStockDetailInvoiceStockFragment.visibility = View.GONE
            e.printStackTrace()
            Log.e(TAG, "calculateTOTAL: ${e.message}", )
        }
    }

    private fun refreshList (){
        if (ConnectionCheck.isConnected(requireContext()))
        {
            _invoiceList.clear()
            FetchInvoiceStockAPI(requireContext(),this)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: INVOICE_LIST_SIZE : $_invoiceList")
        commonUIUtility.showProgress()
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            for (i in 0 until receivedStockList.size)
            {
                for (j in 0 until _invoiceList.size)
                {
                    if (receivedStockList[i].equals(_invoiceList[j]))
                    {
                        receivedStockList.set(i,_invoiceList[j])
                    }
                }
            }
            commonUIUtility.dismissProgress()
            bindInvoiceList(receivedStockList)
        },1000)
    }

}

interface InvoiceStockHelper{
    fun onItemSelected(model:InvoiceStockModelItem)

    fun  onItemDeselected(model:InvoiceStockModelItem)
}