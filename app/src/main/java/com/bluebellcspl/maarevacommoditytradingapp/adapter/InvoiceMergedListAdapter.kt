package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.icu.text.NumberFormat
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.databinding.InvoiceDetailAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.InvoiceDetailHelper
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceEntryMergedModelItem
import java.text.DecimalFormat

class InvoiceMergedListAdapter(
    var context: Context,
    var dataList: ArrayList<InvoiceEntryMergedModelItem>,
    var invoiceDetailHelper: InvoiceDetailHelper
) : RecyclerView.Adapter<InvoiceMergedListAdapter.MyViewHolder>() {
    val TAG = "InvoiceMergedListAdapter"

    inner class MyViewHolder(var binding: InvoiceDetailAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun calculateData(model: InvoiceEntryMergedModelItem) {
            try {
                if (binding.tvAmountInvoiceDetailAdapter.text.toString()
                        .isNotEmpty() && binding.tvWeightInvoiceDetailAdapter.text.toString()
                        .isNotEmpty()
                ) {
                    if (binding.tvAmountInvoiceDetailAdapter.text.toString()
                            .toDouble() > 0.0 && binding.tvWeightInvoiceDetailAdapter.text.toString()
                            .toDouble() > 0.0
                    ) {
                        var currentAmount =
                            binding.tvAmountInvoiceDetailAdapter.text.toString().toDouble()
                        var currentWeight =
                            binding.tvWeightInvoiceDetailAdapter.text.toString().toDouble()
                        var currentRate = DecimalFormat("0.00").format((currentAmount / (currentWeight/20.0)))
                        var formattedRate = NumberFormat.getCurrencyInstance().format(currentRate.toDouble()).substring(1)
                        binding.tvRateInvoiceDetailAdapter.setText(formattedRate)

                        var totalInvoiceApproxKG = (currentWeight / model.BhartiPrice.toDouble())
                        var totalInvoiceKG = (currentWeight % model.BhartiPrice.toDouble())

                        Log.d(TAG, "calculateData: TOTAL_INVOICE_APPROX_KG : $totalInvoiceApproxKG")
                        Log.d(TAG, "calculateData: TOTAL_INVOICE_KG : $totalInvoiceKG")
                        Log.d(TAG, "calculateData: CURRENT_RATE : $formattedRate")

                        model.InvoiceKg = DecimalFormat("0.00").format(totalInvoiceKG)
                        model.InvoiceApproxKg = DecimalFormat("0.00").format(totalInvoiceApproxKG)
                        model.WeightAfterAuctionInKg = DecimalFormat("0.00").format(currentWeight)
                        model.AmountT = DecimalFormat("0.00").format(currentAmount)
                        model.CurrentPriceT = currentRate
                        model.InvoiceAmount = DecimalFormat("0.00").format(currentAmount)

                        invoiceDetailHelper.getCalculatedData(dataList)
                    }else
                    {
                        binding.tvRateInvoiceDetailAdapter.setText("0")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "calculateData: ${e.message}", )
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            InvoiceDetailAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = dataList[holder.adapterPosition]
        holder.binding.tvDateInvoiceDetailAdapter.setText(model.InvoiceDate)
        holder.binding.tvBagsInvoiceDetailAdapter.setText(model.BagsT)
        holder.binding.tvPCANameInvoiceDetailAdapter.setText("%s-%s".format(model.ShopNo,model.ShortShopName))
        holder.binding.tvAmountInvoiceDetailAdapter.setText(model.AmountT)
        holder.binding.tvWeightInvoiceDetailAdapter.setText(model.Previousweight)
        model.AmountT = "0"
        model.CurrentPriceT = "0"
        holder.calculateData(model)
        holder.binding.tvAmountInvoiceDetailAdapter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if (holder.binding.tvAmountInvoiceDetailAdapter.text.toString().isBlank()) {
                    holder.binding.tvAmountInvoiceDetailAdapter.setText("0")
                    holder.binding.tvAmountInvoiceDetailAdapter.setSelection(1)
                }
                if (holder.binding.tvAmountInvoiceDetailAdapter.text.toString().length >= 2 && holder.binding.tvAmountInvoiceDetailAdapter.text.toString()
                        .startsWith("0")
                ) {
                    val subStr =
                        holder.binding.tvAmountInvoiceDetailAdapter.text.toString().substring(1)
                    holder.binding.tvAmountInvoiceDetailAdapter.setText(subStr)
                    holder.binding.tvAmountInvoiceDetailAdapter.setSelection(1)
                }
                holder.calculateData(model)
            }
        })

        holder.binding.tvWeightInvoiceDetailAdapter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if (holder.binding.tvWeightInvoiceDetailAdapter.text.toString().isEmpty()) {
                    holder.binding.tvWeightInvoiceDetailAdapter.setText("0")
                    holder.binding.tvWeightInvoiceDetailAdapter.setSelection(1)
                }
                if (holder.binding.tvWeightInvoiceDetailAdapter.text.toString().length >= 2 && holder.binding.tvWeightInvoiceDetailAdapter.text.toString()
                        .startsWith("0")
                ) {
                    val subStr =
                        holder.binding.tvWeightInvoiceDetailAdapter.text.toString().substring(1)
                    holder.binding.tvWeightInvoiceDetailAdapter.setText(subStr)
                    holder.binding.tvWeightInvoiceDetailAdapter.setSelection(1)
                }
                holder.calculateData(model)

            }
        })
    }
}