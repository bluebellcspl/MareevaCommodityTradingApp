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
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.EditableDecimalInputFilter
import com.bluebellcspl.maarevacommoditytradingapp.databinding.InvoiceStockAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.InvoiceStockDetailHelper
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceStockModelItem
import java.text.DecimalFormat
import java.text.NumberFormat

class InvoiceStockDetailAdapter(
    var context: Context,
    var dataList: ArrayList<InvoiceStockModelItem>,
    var invoiceStockHelper: InvoiceStockDetailHelper
) : RecyclerView.Adapter<InvoiceStockDetailAdapter.MyViewHolder>() {
    private val TAG = "InvoiceStockDetailAdapter"

    inner class MyViewHolder(var binding: InvoiceStockAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun calcutateData(model: InvoiceStockModelItem) {
            try {
                var currentBAGS = binding.edtBagsInvoiceStockAdapter.text.toString().toDouble()
                var currentWEIGHT = model.AvailableWeight.toDouble()
                var currentRATE = model.TotalRate.toDouble()
                var currentAMOUNT = model.AvaliableAmount.toDouble()

                if (currentBAGS > -1) {
                    var calculatedWEIGHT = currentBAGS * model.BhartiPrice.toDouble()
                    var calculatedAMOUNT = ((calculatedWEIGHT / 20) * currentRATE)

                    var calculatedInvoiceApproxKG =
                        (calculatedWEIGHT / model.BhartiPrice.toDouble())
                    var calculatedInvoiceKG = (calculatedWEIGHT % model.BhartiPrice.toDouble())


                    model.UsedBags = currentBAGS.toString()
                    model.UsedBagRate = currentRATE.toString()
                    model.UsedBagAmount = DecimalFormat("0.00").format(calculatedAMOUNT)
                    model.UsedBagWeightKg = DecimalFormat("0.00").format(calculatedWEIGHT)
                    model.UsedInvoiceApproxKg =
                        DecimalFormat("0.00").format(calculatedInvoiceApproxKG)
                    model.UsedInvoiceKg = DecimalFormat("0.00").format(calculatedInvoiceKG)

                    val formatedAMOUNT =
                        NumberFormat.getCurrencyInstance().format(model.UsedBagAmount.toDouble())
                            .substring(1)
                    val formatedWEIGHT =
                        NumberFormat.getCurrencyInstance().format(model.UsedBagWeightKg.toDouble())
                            .substring(1)
                    val formatedRATE =
                        NumberFormat.getCurrencyInstance().format(model.TotalRate.toDouble())
                            .substring(1)

                    binding.tvRateInvoiceStockAdapter.setText(formatedRATE)
                    binding.tvWeightInvoiceStockAdapter.setText(formatedWEIGHT)
                    binding.tvAmountInvoiceStockAdapter.setText(formatedAMOUNT)

                    invoiceStockHelper.processData(dataList)
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

        holder.binding.edtBagsInvoiceStockAdapter.isEnabled = true
        holder.binding.edtBagsInvoiceStockAdapter.setBackgroundResource(R.color.subtotalBG)
        holder.binding.mChbInvoiceStockAdapter.visibility = View.GONE
        val model = dataList[holder.adapterPosition]
        holder.binding.tvDateInvoiceStockAdapter.setText(model.Date)
        holder.binding.edtBagsInvoiceStockAdapter.setText(model.AvailableBags)

        val formatedWEIGHT =NumberFormat.getCurrencyInstance().format(model.AvailableWeight.toDouble()).substring(1)
        holder.binding.tvWeightInvoiceStockAdapter.setText(formatedWEIGHT)

        val formatedAMOUNT =NumberFormat.getCurrencyInstance().format(model.AvaliableAmount.toDouble()).substring(1)
        holder.binding.tvAmountInvoiceStockAdapter.setText(formatedAMOUNT)

        val formatedRATE =
            NumberFormat.getCurrencyInstance().format(model.TotalRate.toDouble()).substring(1)
        holder.binding.tvRateInvoiceStockAdapter.setText(formatedRATE)

        holder.calcutateData(model)
        holder.binding.edtBagsInvoiceStockAdapter.filters = arrayOf<InputFilter>(
            EditableDecimalInputFilter(7, 2)
        )
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
//                    val stringBuilder =
//                        StringBuilder(holder.binding.edtBagsInvoiceStockAdapter.text.toString())
//                    stringBuilder.deleteCharAt(0)
//                    holder.binding.edtBagsInvoiceStockAdapter.setText(stringBuilder)
                }
                else if (holder.binding.edtBagsInvoiceStockAdapter.text.toString().endsWith(".")) {
                    val stringBuilder =
                        StringBuilder(holder.binding.edtBagsInvoiceStockAdapter.text.toString())
                    stringBuilder.append("50")
                    holder.binding.edtBagsInvoiceStockAdapter.setText(stringBuilder.toString())
                    holder.binding.edtBagsInvoiceStockAdapter.setSelection(stringBuilder.length)
                }
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
        holder.binding.edtBagsInvoiceStockAdapter.addTextChangedListener(calculationWatcher)
    }
}