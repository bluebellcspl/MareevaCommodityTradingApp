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

class InvoiceAdapter(
    var context: Context,
    datalist: ArrayList<Shopwise>,
    val parentCheckedChangeListener: OnParentCheckedChangeListener
) :
    RecyclerView.Adapter<InvoiceAdapter.MyViewHolder>(), OnChildCheckedChangeListener {
    var shopList: ArrayList<Shopwise> = datalist
    lateinit var shopEntryAdapter: ShopEntriesAdapter
    val TAG = "InvoiceAdapter"

    inner class MyViewHolder(var binding: InvoiceDataAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindShopEntriesList(shopEntryList: ArrayList<ShopEntry>) {
            shopEntryAdapter = ShopEntriesAdapter(context, shopEntryList,adapterPosition,this@InvoiceAdapter)
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
        val model = shopList[position]
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
            notifyItemChanged(position)
        }

        // Recycler for ShopEntries
        holder.bindShopEntriesList(shopEntriesList)

        holder.binding.mChbShopInvoiceAdapter.setOnCheckedChangeListener(null)
        holder.binding.mChbShopInvoiceAdapter.isChecked = model.isSelected

        holder.binding.mChbShopInvoiceAdapter.setOnCheckedChangeListener { _, isChecked ->
            model.isSelected = isChecked
            shopList[position].ShopEntries.forEach { it.isSelected = isChecked }
            notifyItemChanged(position)
        }

        val isExpandable: Boolean = model.isExpandable
        holder.binding.llExpandableInvoiceAdapter.visibility =
            if (isExpandable) View.VISIBLE else View.GONE

        calculateItemSum(holder)
    }

    override fun onChildCheckedChange(parentPosition: Int, childPosition: Int, isChecked: Boolean) {
        try {
            shopList[parentPosition].ShopEntries[childPosition].isSelected = isChecked
            var allChecked = true
            for (entry in shopList[parentPosition].ShopEntries) {
                if (!entry.isSelected) {
                    allChecked = false
                    break
                }
            }
            shopList[parentPosition].isSelected = allChecked
            notifyItemChanged(parentPosition)
            parentCheckedChangeListener.onParentCheckedChange(parentPosition, allChecked)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "onChildCheckedChange: ${e.message}", )
        }
    }

    private fun calculateItemSum(holder: MyViewHolder) {
        var sum = 0.0
        var shop_bags = 0.0
        var shop_avg_rate = 0.0
        var shop_total_amount = 0.0
//        for (shop in shopList) {
            for (entry in shopList[holder.adapterPosition].ShopEntries) {
                if (entry.isSelected) {
                    shop_bags += entry.Bags.toDouble()
                    // shop_avg_rate
                    shop_total_amount  += entry.Amount.toDouble()
                }
//            }
        }
        holder.binding.tvBagAmountTotalPriceInvoiceAdapter.text = "%s/%s/%s".format(
            shop_bags,
            shop_avg_rate,
            shop_total_amount
        )
        Log.d("TotalSum", "Total Sum: $sum")
    }

}

class ShopEntriesAdapter(
    var context: Context,
    var shopEntriesList: ArrayList<ShopEntry>,
    var parentPosition:Int,
    var childCheckedChangeListener: OnChildCheckedChangeListener
) :
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
        val model = shopEntriesList[position]
        holder.binding.tvBagShopEntriesAdapter.text = model.Bags
        holder.binding.tvPriceShopEntriesAdapter.text = model.CurrentPrice
        holder.binding.tvTotalPriceShopEntriesAdapter.text = model.Amount

        holder.binding.mChbShopEntriesAdapter.setOnCheckedChangeListener(null)
        holder.binding.mChbShopEntriesAdapter.isChecked = model.isSelected

        holder.binding.mChbShopEntriesAdapter.setOnCheckedChangeListener { _, isChecked ->
            model.isSelected = isChecked
//            shopEntriesListener.onItemCheckedStateChanged(holder.adapterPosition, isChecked)
            childCheckedChangeListener.onChildCheckedChange(parentPosition, holder.adapterPosition, isChecked)
        }
    }
}

interface OnParentCheckedChangeListener {
    fun onParentCheckedChange(position: Int, isChecked: Boolean)
}

interface OnChildCheckedChangeListener {
    fun onChildCheckedChange(parentPosition: Int, childPosition: Int, isChecked: Boolean)
}