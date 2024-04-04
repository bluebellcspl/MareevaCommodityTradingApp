package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.databinding.NotificationAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.NotificationRTRMasterModelItem

class NotificationAdapter(
    var context: Context,
//    var notificationList: ArrayList<NotificationRTRMasterModelItem>
) : RecyclerView.Adapter<NotificationAdapter.MyViewHolder>() {

    private var oldNotificationList = ArrayList<NotificationRTRMasterModelItem>()
    private var newNotificationList = ArrayList<NotificationRTRMasterModelItem>()

    private val diffCallBack = object : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldNotificationList.size
        }


        override fun getNewListSize(): Int {
            return newNotificationList.size
        }


        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldNotificationList[oldItemPosition].NotificationId == newNotificationList[newItemPosition].NotificationId
        }


        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldNotificationList[oldItemPosition]
            val newItem = newNotificationList[newItemPosition]
            return oldItem.NotificationId == newItem.NotificationId && oldItem.Cdate == newItem.Cdate && oldItem.FullMsg == newItem.FullMsg && oldItem.ISRead == newItem.ISRead
        }

    }

    fun submitList(newNotificationList: List<NotificationRTRMasterModelItem>) {
        val diffResult = DiffUtil.calculateDiff(diffCallBack)
        this.oldNotificationList = this.newNotificationList
        this.newNotificationList = ArrayList(newNotificationList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun addNewData(dataList:ArrayList<NotificationRTRMasterModelItem>){
        newNotificationList.addAll(dataList)
        notifyDataSetChanged()
    }

    inner class MyViewHolder(var binding: NotificationAdapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            NotificationAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
//        return notificationList.size
//        return differ.currentList.size
        return newNotificationList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = newNotificationList[holder.adapterPosition]

        holder.binding.tvNotificationDateTimeNotificationAdapter.setText(model.Cdate)
        holder.binding.tvNotificationLblNotificationAdapter.setText(model.FullMsg)
        if (model.ISRead.equals("false")) {
            holder.binding.cvNotificationNotificationAdapter.setCardBackgroundColor(
                context.getColor(
                    R.color.colorSecondaryDark
                )
            )
        } else {
            holder.binding.cvNotificationNotificationAdapter.setCardBackgroundColor(
                context.getColor(
                    R.color.white
                )
            )
        }
    }
}