package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.databinding.ShopEntriesAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.ShopEntry

//class ShopEntriesAdapter(var context: Context, var shopEntriesList: ArrayList<ShopEntry>) :
//    RecyclerView.Adapter<ShopEntriesAdapter.MyViewHolder>() {
//    val TAG = "ShopEntriesAdapter"
//
//    inner class MyViewHolder(var binding: ShopEntriesAdapterBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
//        return MyViewHolder(
//            ShopEntriesAdapterBinding.inflate(
//                LayoutInflater.from(parent.context),
//                parent,
//                false
//            )
//        )
//    }
//
//    override fun getItemCount(): Int {
//        return shopEntriesList.size
//    }
//
//    fun checkAll(isChecked: Boolean) {
//        shopEntriesList.forEach {
//            it.isSelected = isChecked
//        }
//        notifyDataSetChanged()
//    }
//
//    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//        val model = shopEntriesList[holder.adapterPosition]
//        holder.binding.tvBagShopEntriesAdapter.setText(model.Bags)
//        holder.binding.tvPriceShopEntriesAdapter.setText(model.CurrentPrice)
//        holder.binding.tvTotalPriceShopEntriesAdapter.setText(model.Amount)
//        holder.binding.mChbShopEntriesAdapter.isChecked = model.isSelected
//        holder.binding.mChbShopEntriesAdapter.setOnCheckedChangeListener { _, isChecked ->
//            model.isSelected = isChecked
//        }
//    }
//}