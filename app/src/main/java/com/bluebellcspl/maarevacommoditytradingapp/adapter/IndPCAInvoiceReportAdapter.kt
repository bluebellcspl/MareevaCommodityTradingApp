package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.icu.text.NumberFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.databinding.InvoiceReportAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAInvoiceReportHelper
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.InvoiceReportFragment
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoiceReportModelItem
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceReportModelItem
import java.util.Locale

class IndPCAInvoiceReportAdapter(var context: Context, var dataList: ArrayList<IndPCAInvoiceReportModelItem>, var invoiceReportListHelper: IndPCAInvoiceReportHelper):RecyclerView.Adapter<IndPCAInvoiceReportAdapter.MyViewHolder>(),Filterable {
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

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                val charString = constraint.toString().lowercase(Locale.getDefault())

                filteredList = if (charString.isEmpty()) {
                    dataList
                } else {
                    val filtered = ArrayList<IndPCAInvoiceReportModelItem>()
                    for (user in dataList) {
                        if (user.InvoiceBags.lowercase(Locale.getDefault()).contains(charString) ||
                            user.InvoiceNo.lowercase(Locale.getDefault()).contains(charString) ||
                            user.VechicalNo.lowercase(Locale.getDefault()).contains(charString) ||
                            user.Date.lowercase(Locale.getDefault()).contains(charString) ||
                            user.FinalAmount.lowercase(Locale.getDefault()).contains(charString)||
                            user.BuyerName.lowercase(Locale.getDefault()).contains(charString)
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
                filteredList = results?.values as ArrayList<IndPCAInvoiceReportModelItem>
                notifyDataSetChanged()
            }
        }
    }

}