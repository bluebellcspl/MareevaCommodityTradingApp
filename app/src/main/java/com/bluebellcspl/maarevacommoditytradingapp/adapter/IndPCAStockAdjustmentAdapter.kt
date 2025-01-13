package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.EditableDecimalInputFilter
import com.bluebellcspl.maarevacommoditytradingapp.databinding.InvoiceStockAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAInvoiceAdjustmentHelper
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoiceStockModelItem


class IndPCAStockAdjustmentAdapter(var context: Context, var dataList: ArrayList<IndPCAInvoiceStockModelItem>,var invoiceAdjustmentHelper: IndPCAInvoiceAdjustmentHelper):RecyclerView.Adapter<IndPCAStockAdjustmentAdapter.MyViewHolder>() {
    val TAG = "IndPCAStockAdjustmentAdapter"
    private val commonUIUtility by lazy { CommonUIUtility(context) }
    inner class MyViewHolder(var binding: InvoiceStockAdapterBinding) :
        RecyclerView.ViewHolder(binding.root){
        fun calcutateData(model: IndPCAInvoiceStockModelItem) {
            try {
                var currentBAGS = binding.edtBagsInvoiceStockAdapter.text.toString().toDouble()
                var currentRATE = model.BillRate.toDouble()

                if (currentBAGS > -1) {
                    var calculatedWEIGHT = currentBAGS * model.CommodityBhartiPrice.toDouble()
                    var calculatedAMOUNT = ((calculatedWEIGHT / 20) * currentRATE)

                    var calculatedInvoiceApproxKG =
                        (calculatedWEIGHT / model.CommodityBhartiPrice.toDouble())
                    var calculatedInvoiceKG = (calculatedWEIGHT % model.CommodityBhartiPrice.toDouble())


                    model.UsedBillBags = currentBAGS.toString()
                    model.UsedBillRate = currentRATE.toString()
                    model.UsedBillAmount = commonUIUtility.formatDecimal(calculatedAMOUNT)
                    model.UsedBillWeight = commonUIUtility.formatDecimal(calculatedWEIGHT)
                    model.UsedBillApproxKg = commonUIUtility.formatDecimal(calculatedInvoiceApproxKG)
                    model.UsedBillKg = commonUIUtility.formatDecimal(calculatedInvoiceKG)
                    model.UsedBillGST = commonUIUtility.formatDecimal(calculatedAMOUNT * (model.TotalPct.toDouble() / 100))
                    model.UsedBillTotalAmount = commonUIUtility.formatDecimal(calculatedAMOUNT + model.UsedBillGST.toDouble())

                    binding.tvRateInvoiceStockAdapter.text = commonUIUtility.numberCurrencyFormat(model.BillRate.toDouble())
                    binding.tvWeightInvoiceStockAdapter.text = commonUIUtility.numberCurrencyFormat(model.UsedBillWeight.toDouble())
                    binding.tvAmountInvoiceStockAdapter.text = commonUIUtility.numberCurrencyFormat(model.UsedBillAmount.toDouble())

                    invoiceAdjustmentHelper.getCalculatedData(dataList)
                } else {
                    binding.tvWeightInvoiceStockAdapter.setText("0")
                    binding.tvAmountInvoiceStockAdapter.setText("0")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "calcutateData: ${e.message}")
            }
        }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            InvoiceStockAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = dataList[holder.adapterPosition]
        holder.binding.mChbInvoiceStockAdapter.visibility = View.GONE
        holder.binding.edtBagsInvoiceStockAdapter.isEnabled = true

        holder.binding.tvDateInvoiceStockAdapter.setText(model.Date)
        holder.binding.edtBagsInvoiceStockAdapter.setText(model.AvailableBags)
        holder.binding.tvWeightInvoiceStockAdapter.text = commonUIUtility.formatDecimal(model.AvailableWeight.toDouble())
        holder.binding.tvRateInvoiceStockAdapter.setText(commonUIUtility.numberCurrencyFormat(model.BillRate.toDouble()))
        holder.binding.tvAmountInvoiceStockAdapter.setText(commonUIUtility.numberCurrencyFormat(model.AvaliableAmount.toDouble()))
        holder.binding.edtBagsInvoiceStockAdapter.filters = arrayOf<InputFilter>(
            EditableDecimalInputFilter(7, 2)
        )

        var calculatedWEIGHT = model.AvailableBags.toDouble() * model.CommodityBhartiPrice.toDouble()
        var calculatedInvoiceApproxKG =
            (calculatedWEIGHT / model.CommodityBhartiPrice.toDouble())
        var calculatedInvoiceKG = (calculatedWEIGHT % model.CommodityBhartiPrice.toDouble())
        model.UsedBillBags = model.AvailableBags
        model.UsedBillRate = model.BillRate
        model.UsedBillAmount = model.AvaliableAmount
        model.UsedBillWeight = model.AvailableWeight
        model.UsedBillApproxKg = commonUIUtility.formatDecimal(calculatedInvoiceApproxKG)
        model.UsedBillKg = commonUIUtility.formatDecimal(calculatedInvoiceKG)

        holder.calcutateData(model)

        val calculationWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (holder.binding.edtBagsInvoiceStockAdapter.text.toString().isEmpty()) {
                    holder.binding.edtBagsInvoiceStockAdapter.setText("0")
                    holder.binding.edtBagsInvoiceStockAdapter.setSelection(1)
                    Log.d(TAG, "afterTextChanged: ZERO_CONDITION")
                }
                else if (holder.binding.edtBagsInvoiceStockAdapter.text.toString().length >= 2 && holder.binding.edtBagsInvoiceStockAdapter.text.toString()
                        .startsWith("0")
                ) {
                    val subStr =
                        holder.binding.edtBagsInvoiceStockAdapter.text.toString().substring(1)
                    holder.binding.edtBagsInvoiceStockAdapter.setText(subStr)
                    holder.binding.edtBagsInvoiceStockAdapter.setSelection(holder.binding.edtBagsInvoiceStockAdapter.text.toString().length)
                    Log.d(TAG, "afterTextChanged: REMOVE_ZERO_CONDITION")
                }
//                else if (holder.binding.edtBagsInvoiceStockAdapter.text.toString().endsWith(".")) {
//                    val stringBuilder =
//                        StringBuilder(holder.binding.edtBagsInvoiceStockAdapter.text.toString())
//                    stringBuilder.append("50")
//                    holder.binding.edtBagsInvoiceStockAdapter.setText(stringBuilder.toString())
//                    holder.binding.edtBagsInvoiceStockAdapter.setSelection(stringBuilder.length)
//                }
                else if (holder.binding.edtBagsInvoiceStockAdapter.text.toString()
                        .toDouble() > model.AvailableBags.toDouble()
                ) {
                    holder.binding.edtBagsInvoiceStockAdapter.setText(model.AvailableBags)
                    holder.binding.edtBagsInvoiceStockAdapter.setSelection(model.AvailableBags.length)
                }
                holder.calcutateData(model)
            }
        }

        holder.binding.edtBagsInvoiceStockAdapter.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                val text = holder.binding.edtBagsInvoiceStockAdapter.text.toString()
                if (text.contains(".")) {
                    val text = holder.binding.edtBagsInvoiceStockAdapter.text.toString()
                    val decimalIndex = text.indexOf(".")
                    if (decimalIndex != -1) {
                        val newText = StringBuilder(text)
                        newText.delete(decimalIndex, newText.length)
                        holder.binding.edtBagsInvoiceStockAdapter.setText(newText.toString())
                        holder.binding.edtBagsInvoiceStockAdapter.setSelection(newText.length)
                        return@OnKeyListener true
                    }
                }
            }
            false
        })

        holder.binding.edtBagsInvoiceStockAdapter.setOnEditorActionListener(OnEditorActionListener { view, actionId, event ->
            val result = actionId and EditorInfo.IME_MASK_ACTION
            when (result) {
                EditorInfo.IME_ACTION_DONE -> {
                    if (holder.binding.edtBagsInvoiceStockAdapter.text.toString().toDouble()==0.0){
                        holder.binding.edtBagsInvoiceStockAdapter.setText(model.AvailableBags)
                    }
                    return@OnEditorActionListener false
                }
                EditorInfo.IME_ACTION_NEXT -> {
                    if (holder.binding.edtBagsInvoiceStockAdapter.text.toString().toDouble()==0.0){
                        holder.binding.edtBagsInvoiceStockAdapter.setText(model.AvailableBags)
                    }
                    return@OnEditorActionListener false
                }

                else -> {
                    return@OnEditorActionListener false
                }
            }
        })
        holder.binding.edtBagsInvoiceStockAdapter.addTextChangedListener(calculationWatcher)

        holder.binding.edtBagsInvoiceStockAdapter.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus){
                if (holder.binding.edtBagsInvoiceStockAdapter.text.toString().toDouble()==0.0){
                    holder.binding.edtBagsInvoiceStockAdapter.setText(model.AvailableBags)
                }
            }
        }

    }

}