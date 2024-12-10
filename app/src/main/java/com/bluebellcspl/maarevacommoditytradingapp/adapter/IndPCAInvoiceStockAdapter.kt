package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.icu.text.NumberFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.databinding.IndPcaInvoiceStockAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAInvoiceStockAdapterListener
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoiceStockModelItem
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAStockBuyerWiseModelItem
import java.text.DecimalFormat

class IndPCAInvoiceStockAdapter(var context: Context, var dataList:ArrayList<IndPCAStockBuyerWiseModelItem>, var indPCAInvoiceStockAdapterListener: IndPCAInvoiceStockAdapterListener): RecyclerView.Adapter<IndPCAInvoiceStockAdapter.MyViewHolder>() {
    inner class MyViewHolder(var binding: IndPcaInvoiceStockAdapterBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(IndPcaInvoiceStockAdapterBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = dataList[holder.adapterPosition]
        holder.binding.tvBuyerNameIndPCAInvoiceStockAdapter.text = model.BuyerName
        holder.binding.tvRateIndPCAInvoiceStockAdapter.text=numberFormat(model.TotalBillRate.toDouble())
        holder.binding.tvWeightIndPCAInvoiceStockAdapter.text = numberFormat(model.TotalAvailableWeight.toDouble())
        holder.binding.tvAmountIndPCAInvoiceStockAdapter.text = numberFormat(model.TotalAvailableAmount.toDouble())
        holder.binding.tvBagIndPCAInvoiceStockAdapter.text = formatDecimal(model.TotalAvailableBags.toDouble())
        holder.binding.cvIndPCAInvoiceStockAdapter.setOnClickListener {
            indPCAInvoiceStockAdapterListener.onInvoiceStockItemClick(model,holder.adapterPosition)
        }
    }

    private fun formatDecimal(value: Double): String {
        return DecimalFormat("0.00").format(value)
    }

    private fun numberFormat(value: Double): String {
        return NumberFormat.getCurrencyInstance().format(value).substring(1)
    }
}