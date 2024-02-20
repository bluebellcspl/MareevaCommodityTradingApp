package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.icu.text.NumberFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.databinding.LiveAuctionAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.ExpandableObject
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionPCAListModel
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionShopListModel
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.RecyclerViewHelper


class LiveAuctionListAdapter(
    var context: Context,
    var dataList: ArrayList<LiveAuctionPCAListModel>,
    var expandableList: ArrayList<ExpandableObject>,
    var recyclerViewHelper: RecyclerViewHelper
) : RecyclerView.Adapter<LiveAuctionListAdapter.MyViewHolder>() {
    val TAG = "LiveAuctionListAdapter"

    inner class MyViewHolder(var binding: LiveAuctionAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.llHeaderLiveAuctionAdapter.setOnClickListener {
                val model = expandableList[adapterPosition]
                model.Expandable = !model.isExpandable()
                notifyItemChanged(adapterPosition)
            }

            binding.llHeaderLiveAuctionAdapter.setOnLongClickListener {
                val model = dataList[adapterPosition]
                recyclerViewHelper.getLiveAuctionPCAData(adapterPosition,model)
                true
            }
        }

        fun bindShopList(shopList: ArrayList<LiveAuctionShopListModel>) {
            val adapter = ShopListAdatper(context, shopList)
            binding.rcViewPCAShopListLiveAuctionAdapter.adapter = adapter
            binding.rcViewPCAShopListLiveAuctionAdapter.invalidate()
        }

        fun calcutionTotalPCAAmount(dataModel: LiveAuctionPCAListModel) {
            try {
//                var pcaBasic = 0.0
//                var pcaExpense = 0.0
//                for (pcaData in dataModel.ShopList) {
//                    pcaBasic += pcaData.Amount.toDouble()
//                }
//
//
//                pcaExpense =
//                    dataModel.PCACommCharge.toDouble() + dataModel.GCACommCharge.toDouble() + dataModel.TransportationCharge.toDouble() + dataModel.LabourCharge.toDouble()+dataModel.MarketCessCharge.toDouble()
//                var totalPCACost = pcaExpense+pcaBasic
//                var pcaTotalBags = dataModel.TotalPurchasedBags.toFloat()

                var currentPCABasic = 0.0
                var CURRENT_pcaMarketCess = 0.0
                var CURRENT_pcaCommCharge = 0.0
                var CURRENT_gcaCommCharge = 0.0
                var CURRENT_pcaTransportationCharge = 0.0
                var CURRENT_pcaLabourCharge = 0.0
                var CURRENT_Shop_Amount = 0.0
                var CURRENT_TOTAL_COST = 0.0
                var CURRENT_pcaExpense = 0.0
                var TOTAL_pcaBasic=0.0
                for (ShopData in dataModel.ShopList) {
                    currentPCABasic = ShopData.Amount.toDouble()
                    var SHOP_CURRENT_PRICE = ShopData.CurrentPrice.toDouble()
                    var SHOP_CURRENT_BAGS = ShopData.Bags.toFloat()

                    var pcaMarketCess =
                        (((SHOP_CURRENT_BAGS * dataModel.CommodityBhartiPrice.toDouble()) / 20) * (SHOP_CURRENT_PRICE) * dataModel.MarketCessCharge.toDouble()) / 100.00
                    var pcaCommCharge =
                        (((SHOP_CURRENT_BAGS * dataModel.CommodityBhartiPrice.toDouble()) / 20) * (SHOP_CURRENT_PRICE) * dataModel.PCACommCharge.toDouble()) / 100.00
                    var gcaCommCharge =
                        (((SHOP_CURRENT_BAGS * dataModel.CommodityBhartiPrice.toDouble()) / 20) * (SHOP_CURRENT_PRICE) * dataModel.GCACommCharge.toDouble()) / 100.00
                    if (dataModel.TransportationCharge.isEmpty()) {
                        dataModel.TransportationCharge = "0"
                    }
                    if (dataModel.LabourCharge.isEmpty()) {
                        dataModel.LabourCharge = "0"
                    }
                    var pcaLabourCharge = SHOP_CURRENT_BAGS * dataModel.LabourCharge.toDouble()
                    var pcaTransportationCharge =
                        SHOP_CURRENT_BAGS * dataModel.TransportationCharge.toDouble()
                    var amount =
                        ((SHOP_CURRENT_BAGS * dataModel.CommodityBhartiPrice.toDouble()) / 20) * SHOP_CURRENT_PRICE

                    CURRENT_Shop_Amount += amount
                    CURRENT_pcaCommCharge += pcaCommCharge
                    CURRENT_gcaCommCharge += gcaCommCharge
                    CURRENT_pcaMarketCess += pcaMarketCess
                    CURRENT_pcaLabourCharge += pcaLabourCharge
                    CURRENT_pcaTransportationCharge += pcaTransportationCharge
                    TOTAL_pcaBasic += currentPCABasic
                    CURRENT_pcaExpense += pcaCommCharge + gcaCommCharge + pcaMarketCess +pcaLabourCharge + pcaTransportationCharge
                    CURRENT_TOTAL_COST += amount + pcaCommCharge + gcaCommCharge + pcaMarketCess +pcaLabourCharge + pcaTransportationCharge
                }
                val PCATOTALNF = NumberFormat.getCurrencyInstance().format(CURRENT_TOTAL_COST).substring(1)
                binding.tvPCATotalAmount.setText(PCATOTALNF)
                var pcaAvgRate = CURRENT_TOTAL_COST/((dataModel.TotalPurchasedBags.toFloat()*dataModel.CommodityBhartiPrice.toDouble())/20)
                val AvgRateNF = NumberFormat.getCurrencyInstance().format(pcaAvgRate).substring(1)
                binding.tvPCAAvgRate.setText(AvgRateNF)

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "calcutionTotalPCAAmount: ${e.message}")

            }
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
        val model = dataList[holder.adapterPosition]
        val expanable = expandableList[holder.adapterPosition]
        holder.binding.tvPCAName.setText(model.PCAName)
        holder.bindShopList(model.ShopList)
//        val AvgPriceNF =
//            NumberFormat.getCurrencyInstance().format(model.AvgPrice.toDouble()).substring(1)
//        holder.binding.tvPCAAvgRate.setText(AvgPriceNF)
        holder.binding.tvPCATotalBags.setText(model.TotalPurchasedBags)
        holder.calcutionTotalPCAAmount(model)

        if (model.IsAuctionStop.equals("False", true)) {
            holder.binding.fabPauseAuctionLiveAucionFragment.visibility = View.VISIBLE
            holder.binding.fabStartAuctionLiveAucionFragment.visibility = View.GONE
        } else {
            holder.binding.fabPauseAuctionLiveAucionFragment.visibility = View.GONE
            holder.binding.fabStartAuctionLiveAucionFragment.visibility = View.VISIBLE
        }

        holder.binding.fabStartAuctionLiveAucionFragment.setOnClickListener {
            holder.binding.fabPauseAuctionLiveAucionFragment.visibility = View.VISIBLE
            holder.binding.fabStartAuctionLiveAucionFragment.visibility = View.GONE
            recyclerViewHelper.onItemClick(holder.adapterPosition, "start")
        }
        holder.binding.fabPauseAuctionLiveAucionFragment.setOnClickListener {
            holder.binding.fabPauseAuctionLiveAucionFragment.visibility = View.GONE
            holder.binding.fabStartAuctionLiveAucionFragment.visibility = View.VISIBLE
            recyclerViewHelper.onItemClick(holder.adapterPosition, "stop")
        }
        val isExpandable: Boolean = expanable.isExpandable()
        holder.binding.llExpandableLiveAuctionAdapter.visibility =
            if (isExpandable) View.VISIBLE else View.GONE
    }
}