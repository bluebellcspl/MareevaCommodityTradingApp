package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.databinding.InvoiceDataAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.ShopEntriesAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.ShopEntry
import com.bluebellcspl.maarevacommoditytradingapp.model.Shopwise

class InvoiceAdapter(var context: Context, datalist: ArrayList<Shopwise>) :
    RecyclerView.Adapter<InvoiceAdapter.MyViewHolder>() {
    var shopList: ArrayList<Shopwise> = datalist
    lateinit var shopEntryAdapter:ShopEntriesAdapter
    val TAG = "InvoiceAdapter"


    inner class MyViewHolder(var binding: InvoiceDataAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindShopEntriesList(shopEntryList: ArrayList<ShopEntry>) {
            shopEntryAdapter = ShopEntriesAdapter(context, shopEntryList,object : ShopEntriesListener {
                override fun onItemCheckedStateChanged(position: Int) {
                    TODO("Not yet implemented")
                }
            })
            binding.rcViewShopEntries.adapter = shopEntryAdapter
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            InvoiceDataAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return shopList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = shopList[holder.adapterPosition]
        val shopEntriesList = model.ShopEntries

        holder.binding.tvDateInvoiceAdapter.text = model.Date
        holder.binding.tvShopNameInvoiceAdapter.text = model.ShopName
        holder.binding.tvBagAmountTotalPriceInvoiceAdapter.text = "%s/%s/%s".format(
            model.PurchasedBag,
            model.CurrentPrice,
            model.Amount
        )

        holder.binding.rlShopHeaderInvoiceAdapter.setOnClickListener {
            model.isExpandable = !model.isExpandable
            notifyItemChanged(holder.adapterPosition)
        }

        //Recycler for ShopEntries
        holder.bindShopEntriesList(shopEntriesList)

        holder.binding.mChbShopInvoiceAdapter.setOnCheckedChangeListener(null)
        holder.binding.mChbShopInvoiceAdapter.isChecked = model.isSelected


        holder.binding.mChbShopInvoiceAdapter.setOnCheckedChangeListener { _, isChecked ->
            model.isSelected = isChecked
            shopList[holder.adapterPosition].ShopEntries.forEach { it.isSelected = isChecked }
            notifyItemChanged(holder.adapterPosition)
        }

        val isExpandable: Boolean = model.isExpandable
        holder.binding.llExpandableInvoiceAdapter.visibility =
            if (isExpandable) View.VISIBLE else View.GONE
    }

}

class ShopEntriesAdapter(var context: Context, var shopEntriesList: ArrayList<ShopEntry>,var shopEntriesListener: ShopEntriesListener) :
    RecyclerView.Adapter<ShopEntriesAdapter.MyViewHolder>() {
    val TAG = "ShopEntriesAdapter"

    inner class MyViewHolder(var binding: ShopEntriesAdapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ShopEntriesAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return shopEntriesList.size
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = shopEntriesList[holder.adapterPosition]
        holder.binding.tvBagShopEntriesAdapter.text = model.Bags
        holder.binding.tvPriceShopEntriesAdapter.text = model.CurrentPrice
        holder.binding.tvTotalPriceShopEntriesAdapter.text = model.Amount

        holder.binding.mChbShopEntriesAdapter.setOnCheckedChangeListener(null)
        holder.binding.mChbShopEntriesAdapter.isChecked = model.isSelected

        holder.binding.mChbShopEntriesAdapter.setOnCheckedChangeListener { _, isChecked ->
            model.isSelected = isChecked
            shopEntriesListener.onItemCheckedStateChanged(holder.adapterPosition)
        }
    }
//    fun checkAllSelected(): Boolean {
//        for (entry in shopEntriesList) {
//            if (!entry.isSelected) {
//                return false
//            }
//        }
//        return true
//    }

}

interface ShopEntriesListener {
    fun onItemCheckedStateChanged(position: Int)
}