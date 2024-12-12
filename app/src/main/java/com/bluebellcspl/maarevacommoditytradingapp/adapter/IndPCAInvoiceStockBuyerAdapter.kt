package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.databinding.InvoiceStockAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAInvoiceStockHelper
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoiceStockModelItem
import java.text.NumberFormat

class IndPCAInvoiceStockBuyerAdapter(var context: Context, var dataList: ArrayList<IndPCAInvoiceStockModelItem>, var invoiceStockHelper: IndPCAInvoiceStockHelper,var onItemCheckedChangeListener: (Boolean) -> Unit):RecyclerView.Adapter<IndPCAInvoiceStockBuyerAdapter.MyViewHolder>() {
    val TAG = "IndPCAInvoiceStockBuyerAdapter"
    inner class MyViewHolder(var binding: InvoiceStockAdapterBinding) :
        RecyclerView.ViewHolder(binding.root)

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

        holder.binding.tvDateInvoiceStockAdapter.setText(model.Date)
        holder.binding.edtBagsInvoiceStockAdapter.setText(model.AvailableBags)
        val formatedWEIGHT = NumberFormat.getCurrencyInstance().format(model.AvailableWeight.toDouble()).substring(1)
        holder.binding.tvWeightInvoiceStockAdapter.setText(formatedWEIGHT)

        val formatedAMOUNT = NumberFormat.getCurrencyInstance().format(model.AvaliableAmount.toDouble()).substring(1)
        holder.binding.tvAmountInvoiceStockAdapter.setText(formatedAMOUNT)

        val formatedRATE = NumberFormat.getCurrencyInstance().format(model.BillRate.toDouble()).substring(1)
        holder.binding.tvRateInvoiceStockAdapter.setText(formatedRATE)

        holder.binding.mChbInvoiceStockAdapter.setOnCheckedChangeListener(null)
        holder.binding.mChbInvoiceStockAdapter.isChecked = model.isSelected

        holder.binding.mChbInvoiceStockAdapter.setOnCheckedChangeListener { _comoundButton, isChecked ->

            model.isSelected = _comoundButton.isChecked
            invoiceStockHelper.run {
                if (_comoundButton.isChecked) {
                    invoiceStockHelper.onItemSelected(model)
                } else {
                    invoiceStockHelper.onItemDeselected(model)
                }
            }
            onItemCheckedChangeListener(isChecked)
        }
    }
}