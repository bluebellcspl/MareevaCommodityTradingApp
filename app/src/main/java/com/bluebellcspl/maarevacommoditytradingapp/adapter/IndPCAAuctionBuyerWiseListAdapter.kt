package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.icu.text.NumberFormat
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.databinding.IndPcaAuctionListAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.IndPcaBuyerAuctionListAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.ApiIndividualPCAAuctionDetail
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCABuyerWiseAuctionModel
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.RecyclerViewHelper

class IndPCAAuctionBuyerWiseListAdapter(
    var context: Context,
    var dataList: ArrayList<IndPCABuyerWiseAuctionModel>,
    var recyclerViewHelper: RecyclerViewHelper
) : RecyclerView.Adapter<IndPCAAuctionBuyerWiseListAdapter.MyViewHolder>() {
    inner class MyViewHolder(var binding: IndPcaBuyerAuctionListAdapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            IndPcaBuyerAuctionListAdapterBinding.inflate(
                android.view.LayoutInflater.from(
                    context
                ), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = dataList[holder.adapterPosition]
        holder.binding.tvBuyerIndPCABuyerAuctionListAdapter.setText(model.BuyerName)
        holder.binding.tvBagsIndPCABuyerAuctionListAdapter.setText(model.Bags)
        val rateNF = NumberFormat.getCurrencyInstance().format(model.Rate.toDouble()).substring(1)
        holder.binding.tvCurrentPriceIndPCABuyerAuctionListAdapter.setText(rateNF)
        val amountNF = NumberFormat.getCurrencyInstance().format(model.Total.toDouble()).substring(1)
        holder.binding.tvAmountIndPCABuyerAuctionListAdapter.setText(amountNF)

        holder.binding.cvIndPCABuyerAuctionListAdapter.setOnClickListener {
            recyclerViewHelper.onItemClick(holder.adapterPosition,"")
        }
    }
}