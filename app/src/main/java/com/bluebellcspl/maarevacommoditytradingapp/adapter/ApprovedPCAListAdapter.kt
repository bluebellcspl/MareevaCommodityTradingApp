package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.databinding.PcaListItemAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAListModelItem
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.RecyclerViewHelper

class ApprovedPCAListAdapter(var context: Context,var dataList:ArrayList<PCAListModelItem>,var recyclerViewHelper: RecyclerViewHelper):RecyclerView.Adapter<ApprovedPCAListAdapter.MyViewHolder>() {
    inner class MyViewHolder(var binding:PcaListItemAdapterBinding):RecyclerView.ViewHolder(binding.root){
        init {
            binding.cvAuctionDetailsPCAListItem.setOnClickListener {
                recyclerViewHelper.onItemClick(adapterPosition,"ApprovedList")
            }
        }
    }

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
        holder.binding.tvPCANamePCAListItem.setText(model.PCAName)
        holder.binding.tvPCACommissionPCAListItem.setText(model.PCACommission)
        holder.binding.tvGCACommissionPCAListItem.setText(model.GCACommission)
        holder.binding.tvMarketCessPCAListItem.setText(model.MarketCess)
        holder.binding.tvApprovedStatusPCAListItem.setTextAppearance(R.style.confirmVisitStatusText)
        holder.binding.tvApprovedStatusPCAListItem.gravity = Gravity.CENTER
        holder.binding.tvApprovedStatusPCAListItem.setText(context.getString(R.string.approved))
        holder.binding.tvApprovedStatusPCAListItem.setBackgroundResource(R.drawable.approved_pca_tile_bg)
    }
}