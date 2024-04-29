package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.icu.text.NumberFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.databinding.InvoiceDataAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.ShopEntriesAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.InvoiceSelectedDataCallBack
import com.bluebellcspl.maarevacommoditytradingapp.model.ShopEntry
import com.bluebellcspl.maarevacommoditytradingapp.model.Shopwise
import java.text.DecimalFormat

class InvoiceAdapter(
    var context: Context,
    datalist: ArrayList<Shopwise>,
    val parentCheckedChangeListener: OnParentCheckedChangeListener,
    val invoiceSelectedDataCallBack: InvoiceSelectedDataCallBack
) :
    RecyclerView.Adapter<InvoiceAdapter.MyViewHolder>(), OnChildCheckedChangeListener {
    var shopList: ArrayList<Shopwise> = datalist
    lateinit var shopEntryAdapter: ShopEntriesAdapter
    val TAG = "InvoiceAdapter"

    init {
        for (shop in shopList)
        {
            shop.isSelected = true
            for (currentShopEntries in shop.ShopEntries)
            {
                currentShopEntries.isSelected = true
            }
        }
    }

    fun updateList(newShopList:ArrayList<Shopwise>)
    {
        shopList.clear()
        shopList.addAll(newShopList)
        notifyDataSetChanged()
    }
    
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

        holder.binding.tvDateInvoiceAdapter.text = DateUtility().formatToddMMyyyy(model.Date)
        holder.binding.tvShopNameInvoiceAdapter.text = model.ShopShortName
        holder.binding.tvBagInvoiceAdapter.text = "%s".format(model.PurchasedBag)
        holder.binding.tvPriceInvoiceAdapter.text = "%s".format(model.CurrentPrice)
        holder.binding.tvAmountInvoiceAdapter.text = "%s".format(model.Amount)

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

        if (holder.adapterPosition == shopList.size-1)
        {
            holder.binding.btnSavePCAInvoiceFragment.visibility = View.VISIBLE
        }else
        {
            holder.binding.btnSavePCAInvoiceFragment.visibility = View.GONE
        }

        holder.binding.btnSavePCAInvoiceFragment.setOnClickListener {
            invoiceSelectedDataCallBack.onSaveButtonClick(shopList)
        }

        val isExpandable: Boolean = model.isExpandable
        holder.binding.llExpandableInvoiceAdapter.visibility =
            if (isExpandable) View.VISIBLE else View.GONE

        calculateItemSum(holder)
        invoiceSelectedDataCallBack.getSelectedInvoiceData(shopList,holder.adapterPosition)

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
        var commodityBhartiPrice = shopList[holder.adapterPosition].CommodityBhartiPrice.toDouble()
            for (entry in shopList[holder.adapterPosition].ShopEntries) {
                if (entry.isSelected) {
                    shop_bags += entry.Bags.toDouble()
                    shop_total_amount  += entry.Amount.toDouble()
                }
        }
        shop_avg_rate = shop_total_amount / ((shop_bags * commodityBhartiPrice)/20.0)
        val naNChecker : Double = shop_avg_rate.let { if (it.isNaN()) 0.0 else it }
        var fr_shop_avg_rate = DecimalFormat("0.00").format(naNChecker)
        var fr_shop_total_amount = DecimalFormat("0.00").format(shop_total_amount)
        var fr_shop_bags = DecimalFormat("0.00").format(shop_bags)
        if (fr_shop_bags.contains("."))
        {
            val decimalValue = fr_shop_bags.split(".")[1].toDouble()
            if (decimalValue>0.0)
            {
                fr_shop_bags = DecimalFormat("0.00").format(shop_bags)
            }else
            {
                fr_shop_bags = DecimalFormat("0.00").format(shop_bags).split(".")[0]
            }
        }

        var formattedCurrentPrice =NumberFormat.getCurrencyInstance().format(fr_shop_avg_rate.toDouble()).substring(1)
        if (formattedCurrentPrice.contains("."))
        {
            val decimalValue = formattedCurrentPrice.split(".")[1].toDouble()
            if (decimalValue>0.0)
            {
                formattedCurrentPrice = NumberFormat.getCurrencyInstance().format(fr_shop_avg_rate.toDouble()).substring(1)
            }else
            {
                formattedCurrentPrice = NumberFormat.getCurrencyInstance().format(fr_shop_avg_rate.toDouble()).substring(1).split(".")[0]
            }
        }

        var formattedAmount =NumberFormat.getCurrencyInstance().format(fr_shop_total_amount.toDouble()).substring(1)
        if (formattedAmount.contains("."))
        {
            val decimalValue = formattedAmount.split(".")[1].toDouble()
            if (decimalValue>0.0)
            {
                formattedAmount = NumberFormat.getCurrencyInstance().format(fr_shop_total_amount.toDouble()).substring(1)
            }else
            {
                formattedAmount = NumberFormat.getCurrencyInstance().format(fr_shop_total_amount.toDouble()).substring(1).split(".")[0]
            }
        }

        holder.binding.tvBagInvoiceAdapter.text = "%s".format(fr_shop_bags)
        holder.binding.tvPriceInvoiceAdapter.text = "%s".format(formattedCurrentPrice)
        holder.binding.tvAmountInvoiceAdapter.text = "%s".format(formattedAmount)
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
        var formattedBags =DecimalFormat("0.00").format(model.Bags.toDouble())
        if (formattedBags.contains("."))
        {
            val decimalValue = formattedBags.split(".")[1].toDouble()
            if (decimalValue>0.0)
            {
                formattedBags =DecimalFormat("0.00").format(model.Bags.toDouble())
            }else
            {
                formattedBags = DecimalFormat("0.00").format(model.Bags.toDouble()).split(".")[0]
            }
        }
        var formattedCurrentPrice =NumberFormat.getCurrencyInstance().format(model.CurrentPrice.toDouble()).substring(1)
        if (formattedCurrentPrice.contains("."))
        {
            val decimalValue = formattedCurrentPrice.split(".")[1].toDouble()
            if (decimalValue>0.0)
            {
                formattedCurrentPrice = NumberFormat.getCurrencyInstance().format(model.CurrentPrice.toDouble()).substring(1)
            }else
            {
                formattedCurrentPrice = NumberFormat.getCurrencyInstance().format(model.CurrentPrice.toDouble()).substring(1).split(".")[0]
            }
        }
        var formattedAmount =NumberFormat.getCurrencyInstance().format(model.Amount.toDouble()).substring(1)
        if (formattedAmount.contains("."))
        {
            val decimalValue = formattedAmount.split(".")[1].toDouble()
            if (decimalValue>0.0)
            {
                formattedAmount = NumberFormat.getCurrencyInstance().format(model.Amount.toDouble()).substring(1)
            }else
            {
                formattedAmount = NumberFormat.getCurrencyInstance().format(model.Amount.toDouble()).substring(1).split(".")[0]
            }
        }
        holder.binding.tvBagShopEntriesAdapter.text = formattedBags
        holder.binding.tvPriceShopEntriesAdapter.text = formattedCurrentPrice
        holder.binding.tvTotalPriceShopEntriesAdapter.text = formattedAmount

        holder.binding.mChbShopEntriesAdapter.setOnCheckedChangeListener(null)
        holder.binding.mChbShopEntriesAdapter.isChecked = model.isSelected

        holder.binding.mChbShopEntriesAdapter.setOnCheckedChangeListener { _, isChecked ->
            model.isSelected = isChecked
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