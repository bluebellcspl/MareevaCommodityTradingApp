package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.icu.text.NumberFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.databinding.InvoiceReportAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.InvoiceReportFragment
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.InvoiceReportListHelper
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceReportModelItem
import java.util.Locale

class InvoiceReportAdapter(var context: Context, var dataList: ArrayList<InvoiceReportModelItem>,var invoiceReportListHelper: InvoiceReportListHelper) :
    RecyclerView.Adapter<InvoiceReportAdapter.MyViewHolder>(),Filterable {
        private val TAG = "InvoiceReportAdapter"
    private var filteredList: ArrayList<InvoiceReportModelItem> = dataList
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

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = filteredList[holder.adapterPosition]
        holder.binding.tvDateInvoiceReportAdapter.setText(DateUtility().formatToddMMyyyy(model.Date))
        holder.binding.tvBagsInvoiceReportAdapter.setText(model.InvoiceBags)
        holder.binding.tvBuyerNameInvoiceReportAdapter.setText(model.BuyerName)
        holder.binding.tvVehicleNoInvoiceReportAdapter.setText(model.VechicalNo)
        holder.binding.tvInvoiceNoInvoiceReportAdapter.setText(model.InvoiceNo)
        val finalAmountNF = NumberFormat.getCurrencyInstance().format(model.FinalAmount.toDouble()).substring(1)
        holder.binding.tvAmountInvoiceReportAdapter.setText(finalAmountNF)

        holder.binding.imgDocInvoiceReportAdapter.setOnClickListener {
            invoiceReportListHelper.onItemClicked(model,InvoiceReportFragment.DOC_TYPE)
        }

        holder.binding.imgPDFInvoiceReportAdapter.setOnClickListener {
            invoiceReportListHelper.onItemClicked(model,InvoiceReportFragment.PDF_TYPE)
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                val charString = constraint.toString().toLowerCase(Locale.getDefault())

                filteredList = if (charString.isEmpty()) {
                    dataList
                } else {
                    val filtered = ArrayList<InvoiceReportModelItem>()
                    for (user in dataList) {
                        if (user.InvoiceBags.toLowerCase(Locale.getDefault()).contains(charString) ||
                            user.InvoiceNo.toLowerCase(Locale.getDefault()).contains(charString) ||
                            user.VechicalNo.toLowerCase(Locale.getDefault()).contains(charString) ||
                            user.Date.toLowerCase(Locale.getDefault()).contains(charString) ||
                            user.FinalAmount.toLowerCase(Locale.getDefault()).contains(charString)
                        ) {
                            filtered.add(user)
                        }
                    }
                    filtered
                }
                filterResults.values = filteredList
                return filterResults
            }
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(p0: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as ArrayList<InvoiceReportModelItem>
                notifyDataSetChanged()
            }
        }
    }

}