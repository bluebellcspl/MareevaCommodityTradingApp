package com.bluebellcspl.maarevacommoditytradingapp.fragment.pca

import android.app.AlertDialog
import android.icu.text.NumberFormat
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.InvoiceStockDetailAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentInvoiceStockDetailBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.InvoiceStockPopupFinalBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceStockModelItem
import java.text.DecimalFormat
import java.util.regex.Pattern

class InvoiceStockDetailFragment : Fragment(),InvoiceStockDetailHelper {

    private val TAG = "InvoiceStockDetailFragment"
    var _binding : FragmentInvoiceStockDetailBinding?=null
    val binding get() = _binding!!
    private val args by navArgs<InvoiceStockDetailFragmentArgs>()
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val navController by lazy { findNavController() }
    var _InvoiceStockList :ArrayList<InvoiceStockModelItem> ? =null
    lateinit var adapter: InvoiceStockDetailAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_invoice_stock_detail, container, false)
        _InvoiceStockList = ArrayList(args.invoiceStocKList.toList())
        bindInvoiceList(_InvoiceStockList!!)
        setOnClickListener()
        return _binding!!.root
    }

    private fun setOnClickListener() {
        try {
            binding.btnNextInvoiceStockDetailFragment.setOnClickListener {
                var isBagsZero = false
                for (model in _InvoiceStockList!!)
                {
                    if (model.UsedBags.toDouble()<0 || model.UsedBags.toDouble()==0.0)
                    {
                        isBagsZero = true
                        break
                    }
                }

                if (!isBagsZero)
                {
                    _InvoiceStockList!!.forEach {model->
                        Log.d(TAG, "setOnClickListener: EDITED_BAGS : ${model.UsedBags}")
                        Log.d(TAG, "setOnClickListener: EDITED_WEIGHT : ${model.UsedBagWeightKg}")
                    }
                    shopFinalPopup()
                }else
                {
                    commonUIUtility.showToast("Please Fill All Entries!")
                }
            }
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "setOnClickListener: ${e.message}", )
        }
    }

    fun bindInvoiceList(invoiceStockList:ArrayList<InvoiceStockModelItem>){
        try {
            if (invoiceStockList.isNotEmpty())
            {
                adapter = InvoiceStockDetailAdapter(requireActivity(),invoiceStockList,this)
                binding.rcViewInvoiceStockDetailFragment.adapter = adapter
                binding.rcViewInvoiceStockDetailFragment.invalidate()
                processData(invoiceStockList)
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

    override fun processData(dataList: ArrayList<InvoiceStockModelItem>) {
        try {
            var TOT_BAGS = 0.0
            var TOT_WEIGHT = 0.0
            var TOT_AMOUNT = 0.0
            var bhartiPrice = 0.0
            for (model in dataList)
            {
                    TOT_BAGS += model.UsedBags.toDouble()
                    TOT_WEIGHT += model.UsedBagWeightKg.toDouble()
                    TOT_AMOUNT += model.UsedBagAmount.toDouble()

                bhartiPrice = model.BhartiPrice.toDouble()

            }
            val formateTOTAL_BAGS = TOT_BAGS
            val formateTOTAL_WEIGHT = NumberFormat.getCurrencyInstance().format(TOT_WEIGHT).substring(1)
            val formateTOTAL_AMOUNT = NumberFormat.getCurrencyInstance().format(TOT_AMOUNT).substring(1)

            val TOT_RATE = ( TOT_AMOUNT /((TOT_BAGS*bhartiPrice)/20.0))

            val formatedTOT_RATE = NumberFormat.getCurrencyInstance().format(TOT_RATE).substring(1)

            binding.edtBagsInvoiceStockDetailFragment.setText(formateTOTAL_BAGS.toString())
            binding.tvAmountInvoiceStockDetailFragment.setText(formateTOTAL_AMOUNT)
            binding.tvRateInvoiceStockDetailFragment.setText(formatedTOT_RATE)
            binding.tvWeightInvoiceStockDetailFragment.setText(formateTOTAL_WEIGHT)
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "processData: ${e.message}", )
        }
    }

    private fun shopFinalPopup()
    {
        try {
            val alertDailogBuilder = AlertDialog.Builder(requireContext())
            val dialogBinding = InvoiceStockPopupFinalBinding.inflate(layoutInflater)
            val dialogView = dialogBinding.root
            alertDailogBuilder.setView(dialogView)
            val alertDialog = alertDailogBuilder.create()
            alertDialog.setCanceledOnTouchOutside(true)
            alertDialog.setCancelable(true)
            alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()

            dialogBinding.actGSTInvoiceStockPopup.inputType = InputType.TYPE_NULL
            var taxAdapter = commonUIUtility.setCustomArrayAdapter(requireActivity().resources.getStringArray(R.array.tax_Array))
            dialogBinding.actGSTInvoiceStockPopup.setAdapter(taxAdapter)
            var isValidRTO = false

            dialogBinding.edtVehicleNoInvoiceStockPopup.addTextChangedListener(object : TextWatcher {
                private var isFormatting: Boolean = false
                private var cursorPosition: Int = 0
                private var beforeLength: Int = 0

                override fun afterTextChanged(s: Editable?) {
                    if (isFormatting) return

                    isFormatting = true

                    // Remove all spaces
                    val originalString = s.toString().replace(" ", "")

                    val formattedString = StringBuilder()

                    for (i in originalString.indices) {
                        formattedString.append(originalString[i])
                        // Add spaces after the respective positions
                        if (i == 1 || i == 3 || i == 5) {
                            formattedString.append(" ")
                        }
                    }

                    cursorPosition += formattedString.length - s!!.length

                    dialogBinding.edtVehicleNoInvoiceStockPopup.setText(formattedString.toString().toUpperCase())
                    dialogBinding.edtVehicleNoInvoiceStockPopup.setSelection(cursorPosition)

                    isFormatting = false

                    // Validate the formatted RTO number
                    if (isValidRtoNumber(s!!.toString())) {
                        dialogBinding.edtVehicleNoContainerInvoiceStockPopup.boxStrokeColor = requireContext().getColor(R.color.newButtonColor)
                        isValidRTO = true
                    } else {
                        dialogBinding.edtVehicleNoContainerInvoiceStockPopup.boxStrokeColor = requireContext().getColor(R.color.unReadChatBadge)
                        isValidRTO = false
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    if (isFormatting) return

                    beforeLength = s?.length ?: 0
                    cursorPosition = start
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (isFormatting) return

                    cursorPosition = start + count
                }
            })

            dialogBinding.btnNextInvoiceStockPopup.setOnClickListener {
                alertDialog.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "shopFinalPopup: ${e.message}", )
        }
    }

    private fun isValidRtoNumber(rtoNumber: String): Boolean {
        val rtoPattern = "^[A-Z]{2} [0-9]{2} [A-Z]{2} [0-9]{4}$"
        return Pattern.compile(rtoPattern).matcher(rtoNumber).matches()
    }

}

interface InvoiceStockDetailHelper{
    fun processData(dataList:ArrayList<InvoiceStockModelItem>)
}