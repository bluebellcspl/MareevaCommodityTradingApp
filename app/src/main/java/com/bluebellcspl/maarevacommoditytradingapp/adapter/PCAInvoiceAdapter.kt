package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.databinding.PcaInvoiceAdapterLayoutBinding

class PCAInvoiceAdapter(var context: Context,var dataList:ArrayList<Int>):RecyclerView.Adapter<PCAInvoiceAdapter.MyViewHolder>() {
    inner class MyViewHolder(var binding:PcaInvoiceAdapterLayoutBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(PcaInvoiceAdapterLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

    }

}