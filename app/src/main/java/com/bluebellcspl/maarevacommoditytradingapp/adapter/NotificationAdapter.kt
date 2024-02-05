package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.databinding.NotificationAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.NotificationRTRMasterModelItem

class NotificationAdapter(var context: Context,var notificationList:ArrayList<NotificationRTRMasterModelItem>):RecyclerView.Adapter<NotificationAdapter.MyViewHolder>() {
    inner class MyViewHolder(var binding:NotificationAdapterBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(NotificationAdapterBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = notificationList[holder.adapterPosition]

        holder.binding.tvNotificationDateTimeNotificationAdapter.setText(model.Cdate)
        holder.binding.tvNotificationLblNotificationAdapter.setText(model.FullMsg)

//        if (model.ISRead.equals("false")){
//            holder.binding.cvNotificationNotificationAdapter.setCardBackgroundColor(context.getColor(
//                R.color.colorSecondaryDark))
//        }
//        else{
//            holder.binding.cvNotificationNotificationAdapter.setCardBackgroundColor(context.getColor(
//                R.color.white))
//        }
    }
}