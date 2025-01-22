package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.icu.text.NumberFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.databinding.InvoiceReportAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAInvoiceReportHelper
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.InvoiceReportFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoiceReportModelItem

class IndPCAInvoiceReportAdapter(var context: Context, var dataList: ArrayList<IndPCAInvoiceReportModelItem>, var invoiceReportListHelper: IndPCAInvoiceReportHelper):RecyclerView.Adapter<IndPCAInvoiceReportAdapter.MyViewHolder>() {
    private val TAG = "IndPCAInvoiceReportAdapter"
    private var filteredList: ArrayList<IndPCAInvoiceReportModelItem> = dataList
    inner class MyViewHolder(var binding: InvoiceReportAdapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            InvoiceReportAdapterBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = filteredList[holder.adapterPosition]
        holder.binding.tvDateInvoiceReportAdapter.setText(model.NewDateFormat)
        holder.binding.tvBagsInvoiceReportAdapter.setText(model.InvoiceBags)
        holder.binding.tvBuyerNameInvoiceReportAdapter.setText(model.BuyerName)
        holder.binding.tvVehicleNoInvoiceReportAdapter.setText(model.VechicalNo)
        holder.binding.tvInvoiceNoInvoiceReportAdapter.setText(model.InvoiceNo)
        val finalAmountNF = NumberFormat.getCurrencyInstance().format(model.FinalAmount.toDouble()).substring(1)
        holder.binding.tvAmountInvoiceReportAdapter.setText(finalAmountNF)

        holder.binding.imgDocInvoiceReportAdapter.setOnClickListener {
            invoiceReportListHelper.onReportItemClick(model, InvoiceReportFragment.DOC_TYPE)
        }

        holder.binding.imgPDFInvoiceReportAdapter.setOnClickListener {
            invoiceReportListHelper.onReportItemClick(model, InvoiceReportFragment.PDF_TYPE)
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

}