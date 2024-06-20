package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.icu.text.NumberFormat
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.databinding.InvoicePreviewItemAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.InvoiceStockDetailHelper
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceStockModelItem

class InvoicePreviewAdapter(var context: Context,var dataList:ArrayList<InvoiceStockModelItem>,var invoiceDetailHelper: InvoiceStockDetailHelper):RecyclerView.Adapter<InvoicePreviewAdapter.MyViewHolder>() {
    val TAG = "InvoicePreviewAdapter"
    inner class MyViewHolder(var binding:InvoicePreviewItemAdapterBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(InvoicePreviewItemAdapterBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var model = dataList[holder.adapterPosition]
        holder.binding.edtCommodityNameInvoicePreviewItemAdapter.setText(model.CommodityName)

        var formattedBags = NumberFormat.getCurrencyInstance().format(model.UsedBags.toDouble()).substring(1)
        holder.binding.edtBagsInvoicePreviewItemAdapter.setText(formattedBags)

        var formattedWeight = NumberFormat.getCurrencyInstance().format(model.UsedBagWeightKg.toDouble()).substring(1)
        holder.binding.edtWeightInvoicePreviewItemAdapter.setText(formattedWeight)

        var formattedRate = NumberFormat.getCurrencyInstance().format(model.UsedBagRate.toDouble()).substring(1)
        holder.binding.edtRateInvoicePreviewItemAdapter.setText(formattedRate)

        var formattedAmount = NumberFormat.getCurrencyInstance().format(model.UsedBagAmount.toDouble()).substring(1)
        holder.binding.edtAmountInvoicePreviewItemAdapter.setText(formattedAmount)
        holder.binding.edtHSNCodeInvoicePreviewItemAdapter.setText(model.HsnAsc)

//        if (holder.binding.edtHSNCodeInvoicePreviewItemAdapter.text.toString().isEmpty()){
//            model.HsnAsc = ""
//        }
//        holder.binding.edtHSNCodeInvoicePreviewItemAdapter.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//                if (s.toString().isNotEmpty())
//                {
//                    model.HsnAsc = s.toString()
//                    invoiceDetailHelper.processData(dataList)
//                }
//            }
//        })
    }

}