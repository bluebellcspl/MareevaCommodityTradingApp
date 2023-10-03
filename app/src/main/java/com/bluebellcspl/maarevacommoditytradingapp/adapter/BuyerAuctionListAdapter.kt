package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.databinding.BuyerAuctionItemAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.Detail
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.RecyclerViewHelper

class BuyerAuctionListAdapter(var context: Context, var dataList:ArrayList<Detail>, var recyclerViewHelper: RecyclerViewHelper):RecyclerView.Adapter<BuyerAuctionListAdapter.MyViewHolder>() {
    inner class MyViewHolder(var binding:BuyerAuctionItemAdapterBinding):
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            BuyerAuctionItemAdapterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = dataList[holder.adapterPosition]
        holder.binding.tvPCANameBuyerAuctionItemAdapter.setText(model.PCAName)
        holder.binding.tvAmountBuyerAuctionItemAdapter.setText(model.Amount)
        holder.binding.tvBagsBuyerAuctionItemAdapter.setText(model.Bag)
        holder.binding.tvLowerLimitBuyerAuctionItemAdapter.setText(model.LowerLimit)
        holder.binding.tvUpperLimitBuyerAuctionItemAdapter.setText(model.UpperLimit)
        holder.binding.tvLastDayPriceBuyerAuctionItemAdapter.setText(model.LastDayPrice)

        if (model.Bag.isEmpty() || model.Bag.equals(""))
        {
            holder.binding.cvAuctionDetailsBuyerAuctionItemAdapter.visibility = View.GONE
        }

        holder.binding.cvAuctionDetailsBuyerAuctionItemAdapter.setOnClickListener {
             recyclerViewHelper.onBuyerAuctionPCAItemClick(holder.adapterPosition,model)
        }
    }
}