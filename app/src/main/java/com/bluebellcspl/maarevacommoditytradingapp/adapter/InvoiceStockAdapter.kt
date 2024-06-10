package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.databinding.InvoiceDetailAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.InvoiceStockAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.InvoiceStockHelper
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceStockModelItem
import java.text.NumberFormat
import java.util.ArrayList

class InvoiceStockAdapter(var context: Context, var dataList: ArrayList<InvoiceStockModelItem>,var invoiceStockHelper: InvoiceStockHelper) :
    RecyclerView.Adapter<InvoiceStockAdapter.MyViewHolder>() {

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

        val formatedRATE = NumberFormat.getCurrencyInstance().format(model.TotalRate.toDouble()).substring(1)
        holder.binding.tvRateInvoiceStockAdapter.setText(formatedRATE)

        holder.binding.mChbInvoiceStockAdapter.setOnCheckedChangeListener(null)
        holder.binding.mChbInvoiceStockAdapter.setChecked(model.isSelected)
        holder.binding.mChbInvoiceStockAdapter.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isChecked)
            {
                model.isSelected = true
                invoiceStockHelper.onItemSelected(model)
            }else
            {
                model.isSelected = false
                invoiceStockHelper.onItemDeselected(model)
            }
        }
    }

}