package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.icu.text.NumberFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.databinding.IndPcaAuctionReportAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.IndividualPCAAuctionDetail

class IndPCAAuctionReportAdapter(
    var context: Context,
    var dataList: ArrayList<IndividualPCAAuctionDetail>
) : RecyclerView.Adapter<IndPCAAuctionReportAdapter.MyViewHolder>() {
    inner class MyViewHolder(var binding: IndPcaAuctionReportAdapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            IndPcaAuctionReportAdapterBinding.inflate(
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

        val currentNf = NumberFormat.getCurrencyInstance().format(model.CurrentPrice.toDouble())
        val totalAmountNf = NumberFormat.getCurrencyInstance().format(model.Amount.toDouble())
        holder.binding.shopRate.setText(currentNf)
        holder.binding.ShopName.setText(model.ShortShopName)
        holder.binding.tvBags.setText(model.Bags)
        holder.binding.tvBuyerName.setText(model.BuyerName)
        holder.binding.tvTotalAmount.setText(totalAmountNf)
    }
}