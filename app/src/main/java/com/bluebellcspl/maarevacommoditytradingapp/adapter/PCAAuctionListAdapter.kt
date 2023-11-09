package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.icu.text.NumberFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.databinding.PcaAuctionListAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.ApiPCAAuctionDetail
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.RecyclerViewHelper

class PCAAuctionListAdapter(var context: Context,var dataList:ArrayList<ApiPCAAuctionDetail>,var recyclerViewHelper: RecyclerViewHelper):RecyclerView.Adapter<PCAAuctionListAdapter.MyViewHolder>() {
    inner class MyViewHolder(var binding:PcaAuctionListAdapterBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            PcaAuctionListAdapterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = dataList[holder.adapterPosition]
        holder.binding.tvShopNoPCAAuctionListAdapter.setText(model.ShopNo)
        holder.binding.tvShopNamePCAAuctionListAdapter.setText(model.ShopName)
        holder.binding.tvBagsPCAAuctionListAdapter.setText(model.Bags)
        val amountNF = NumberFormat.getCurrencyInstance().format(model.Amount.toDouble())
        holder.binding.tvAmountPCAAuctionListAdapter.setText(amountNF)

        holder.binding.cvPCAAuctionListAdapter.setOnClickListener {
            recyclerViewHelper.onItemClick(holder.adapterPosition,"")
        }
    }
}