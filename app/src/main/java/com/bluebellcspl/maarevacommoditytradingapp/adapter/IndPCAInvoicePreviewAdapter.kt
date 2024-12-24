package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.databinding.InvoicePreviewItemAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoiceBagAdjustmentModel

class IndPCAInvoicePreviewAdapter(var context: Context,var dataList:ArrayList<IndPCAInvoiceBagAdjustmentModel>):RecyclerView.Adapter<IndPCAInvoicePreviewAdapter.MyViewHolder>() {
    val TAG = "IndPCAInvoicePreviewAdapter"
    private val commonUIUtility by lazy { CommonUIUtility(context) }
    inner class MyViewHolder(var binding: InvoicePreviewItemAdapterBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(InvoicePreviewItemAdapterBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var model = dataList[holder.adapterPosition]
        holder.binding.edtCommodityNameInvoicePreviewItemAdapter.setText(model.CommodityName)

        holder.binding.edtBagsInvoicePreviewItemAdapter.setText(commonUIUtility.formatDecimal(model.BillBags.toDouble()))
        holder.binding.edtWeightInvoicePreviewItemAdapter.setText(commonUIUtility.formatDecimal(model.BillWeight.toDouble()))
        holder.binding.edtRateInvoicePreviewItemAdapter.setText(commonUIUtility.numberCurrencyFormat(model.BillRate.toDouble()))
        holder.binding.edtAmountInvoicePreviewItemAdapter.setText(commonUIUtility.numberCurrencyFormat(model.BillAmount.toDouble()))
        holder.binding.edtHSNCodeInvoicePreviewItemAdapter.setText(model.HSNCode)
    }

}