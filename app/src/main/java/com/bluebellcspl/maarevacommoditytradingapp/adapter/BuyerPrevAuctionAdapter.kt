package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.icu.text.NumberFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.databinding.LiveAuctionAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionShopListModel
import com.bluebellcspl.maarevacommoditytradingapp.model.PCADetailModel
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAHeaderModel

class BuyerPrevAuctionAdapter(var context: Context, var dataList: ArrayList<PCAHeaderModel>) :
    RecyclerView.Adapter<BuyerPrevAuctionAdapter.MyViewHolder>() {
    inner class MyViewHolder(var binding: LiveAuctionAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.llHeaderLiveAuctionAdapter.setOnClickListener {
                val model = dataList[adapterPosition]
                model.Expandable = !model.isExpandable()
                notifyItemChanged(adapterPosition)
            }
        }
        fun bindShopList(shopList: ArrayList<PCADetailModel>) {
            val adapter = PrevAuctionShopListAdapter(context,shopList)
            binding.rcViewPCAShopListLiveAuctionAdapter.adapter = adapter
            binding.rcViewPCAShopListLiveAuctionAdapter.invalidate()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LiveAuctionAdapterBinding.inflate(
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
        val modeldata = dataList[holder.adapterPosition]
        holder.binding.tvPCAName.setText(modeldata.PCAName)
        holder.binding.tvPCATotalBags.setText(modeldata.TotalPurchasedBags)
        val PCATotalAmountNF =
            NumberFormat.getCurrencyInstance().format(modeldata.TotalCost.toDouble()).substring(1)
        holder.binding.tvPCATotalAmount.setText(PCATotalAmountNF)
        val PCAAvgRateNF = NumberFormat.getCurrencyInstance().format(modeldata.AvgPrice.toDouble()).substring(1)
        holder.binding.fabPauseAuctionLiveAucionFragment.visibility = View.GONE
        holder.binding.fabStartAuctionLiveAucionFragment.visibility = View.GONE
        holder.binding.tvPCAAvgRate.setText(PCAAvgRateNF)
        holder.bindShopList(modeldata.PCADetailModel)

        val isExpandable: Boolean = modeldata.isExpandable()
        holder.binding.llExpandableLiveAuctionAdapter.visibility = if (isExpandable) View.VISIBLE else View.GONE
    }
}