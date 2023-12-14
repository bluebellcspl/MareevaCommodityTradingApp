package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.icu.text.NumberFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.databinding.ShopListAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionShopListModel

class ShopListAdatper(var context: Context,var dataList:ArrayList<LiveAuctionShopListModel>):RecyclerView.Adapter<ShopListAdatper.MyViewHolder>() {
    inner class MyViewHolder(var binding: ShopListAdapterBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(ShopListAdapterBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = dataList[holder.adapterPosition]

        holder.binding.ShopName.setText(model.ShopName)
        val currentNf = NumberFormat.getCurrencyInstance().format(model.CurrentPrice.toDouble())
        val totalAmountNf = NumberFormat.getCurrencyInstance().format(model.Amount.toDouble())
        holder.binding.shopRate.setText(currentNf)
        holder.binding.ShopNo.setText(model.ShopNo)
        holder.binding.tvBags.setText(model.Bags)
        holder.binding.tvTotalAmount.setText(totalAmountNf)
    }
}