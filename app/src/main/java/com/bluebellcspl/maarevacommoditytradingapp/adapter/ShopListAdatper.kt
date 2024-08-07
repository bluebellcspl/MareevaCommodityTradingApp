package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.icu.text.NumberFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.databinding.ShopListAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionShopListModel

class ShopListAdatper(var context: Context,var dataList:ArrayList<LiveAuctionShopListModel>):RecyclerView.Adapter<ShopListAdatper.MyViewHolder>() {
    private var oldShopList = ArrayList<LiveAuctionShopListModel>()
    private var newShopList = dataList

    private val diffCallBack = object : DiffUtil.Callback(){
        override fun getOldListSize(): Int {
            return oldShopList.size
        }

        override fun getNewListSize(): Int {
            return newShopList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldShopList[oldItemPosition].ShopId==newShopList[newItemPosition].ShopId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldShopList[oldItemPosition]==newShopList[newItemPosition]
        }

    }

    fun submitList(latestShopList:List<LiveAuctionShopListModel>){
        val differ = DiffUtil.calculateDiff(diffCallBack)
        oldShopList.clear()
        oldShopList = ArrayList(latestShopList)
        differ.dispatchUpdatesTo(this)
    }
    inner class MyViewHolder(var binding: ShopListAdapterBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(ShopListAdapterBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return newShopList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = dataList[holder.adapterPosition]
        if (PrefUtil.getSystemLanguage().toString().equals("gu"))
        {
            if (model.GujaratiShortShopName.isNotEmpty())
            {
                holder.binding.ShopName.setText(model.GujaratiShortShopName)
            }else
            {
                holder.binding.ShopName.setText(model.ShortShopName)
            }

        }else
        {
            holder.binding.ShopName.setText(model.ShortShopName)
        }
        val formatedCurrentPrice = formatAmount(model.CurrentPrice)
        val formatedAmount = formatAmount(model.Amount)
        val currentNf = NumberFormat.getCurrencyInstance().format(formatedCurrentPrice.toDouble()).substring(1)
        val totalAmountNf = NumberFormat.getCurrencyInstance().format(formatedAmount.toDouble()).substring(1)
        holder.binding.shopRate.setText(currentNf)
        holder.binding.ShopNo.setText(model.ShopNo)
        holder.binding.tvBags.setText(model.Bags)
        holder.binding.tvTotalAmount.setText(totalAmountNf)
    }

    private fun formatAmount(amount: String): String {
        // Use StringBuilder for manipulation
        val stringBuilder = StringBuilder(amount)

        // Check if the string contains a decimal point
        val decimalIndex = stringBuilder.indexOf(".")

        if (decimalIndex != -1) {
            // Check the decimal part
            val decimalPart = stringBuilder.substring(decimalIndex + 1)

            if (decimalPart.toInt() == 0) {
                // If the decimal part is zero, remove the decimal point and trailing zeroes
                stringBuilder.setLength(decimalIndex)
            }
        }

        return stringBuilder.toString()
    }
}