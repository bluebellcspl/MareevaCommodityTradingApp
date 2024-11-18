package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.icu.text.NumberFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.databinding.IndPcaAuctionListAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.ApiIndividualPCAAuctionDetail
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.RecyclerViewHelper

class IndPCAAuctionListAdapter(
    var context: Context,
    var dataList: ArrayList<ApiIndividualPCAAuctionDetail>,
    var recyclerViewHelper: RecyclerViewHelper
) : RecyclerView.Adapter<IndPCAAuctionListAdapter.MyViewHolder>() {

    inner class MyViewHolder(var binding: IndPcaAuctionListAdapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            IndPcaAuctionListAdapterBinding.inflate(
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
        holder.binding.tvBagsIndPCAAuctionListAdapter.setText(model.Bags)
        val currentPriceNF = NumberFormat.getCurrencyInstance().format(model.CurrentPrice.toDouble()).substring(1)
        val amountNF = NumberFormat.getCurrencyInstance().format(model.Amount.toDouble()).substring(1)
        holder.binding.tvCurrentPriceIndPCAAuctionListAdapter.setText(currentPriceNF)
        holder.binding.tvAmountIndPCAAuctionListAdapter.setText(amountNF)
        if (model.BuyerName.isNotEmpty()){
            holder.binding.tvBuyerIndPCAAuctionListAdapter.setText(model.BuyerName)
        }else{
            holder.binding.tvBuyerIndPCAAuctionListAdapter.setText("")
        }
        val shopStringBuilder = StringBuilder()
        shopStringBuilder.append(model.ShopNo + "-" + model.ShortShopName)
        holder.binding.tvShopNameIndPCAAuctionListAdapter.setText(shopStringBuilder.toString())

        holder.binding.cvIndPCAAuctionListAdapter.setOnClickListener {
            recyclerViewHelper.onItemClick(holder.adapterPosition,"")
        }

    }
}