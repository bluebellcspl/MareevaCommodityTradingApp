package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.databinding.PcaListItemAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAListModelItem

class ApprovedPCAListAdapter(var context: Context,var dataList:ArrayList<PCAListModelItem>):RecyclerView.Adapter<ApprovedPCAListAdapter.MyViewHolder>() {
    inner class MyViewHolder(var binding:PcaListItemAdapterBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(PcaListItemAdapterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = dataList[holder.adapterPosition]
        holder.binding.tvPCANamePCAAuctionFragment.setText(model.PCAName)
        holder.binding.tvPCACommissionPCAAuctionFragment.setText(model.PCACommission)
        holder.binding.tvGCACommissionPCAAuctionFragment.setText(model.GCACommission)
        holder.binding.tvMarketCessPCAAuctionFragment.setText(model.MarketCess)
        holder.binding.tvApprovedStatusPCAAuctionFragment.setTextAppearance(R.style.confirmVisitStatusText)
        holder.binding.tvApprovedStatusPCAAuctionFragment.gravity = Gravity.CENTER
        holder.binding.tvApprovedStatusPCAAuctionFragment.setText(context.getString(R.string.approved))
        holder.binding.tvApprovedStatusPCAAuctionFragment.setBackgroundResource(R.drawable.approved_pca_tile_bg)
    }
}