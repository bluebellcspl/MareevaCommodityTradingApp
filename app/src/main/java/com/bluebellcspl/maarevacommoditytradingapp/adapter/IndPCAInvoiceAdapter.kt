package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.icu.text.NumberFormat
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.databinding.IndPcaInvoiceRowAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.IndPcaInvoiceTransactionAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.APIIndividualInvoiceShopwise
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAShopEntries
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.RecyclerViewHelper
import java.text.DecimalFormat

class IndPCAInvoiceAdapter(
    private val context: Context,
    private var dataList: ArrayList<APIIndividualInvoiceShopwise>
) : RecyclerView.Adapter<IndPCAInvoiceAdapter.MyViewHolder>() {

    inner class MyViewHolder(private val binding: IndPcaInvoiceTransactionAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(model: APIIndividualInvoiceShopwise) {
            binding.tvShopNameIndPCAInvoiceAdapter.text = buildString {
                append(model.ShopNo)
                append(" - ")
                append(model.ShopShortName)
            }
            binding.tvDateIndPCAInvoiceAdapter.text = model.Date
            binding.tvBagIndPCAInvoiceAdapter.text = model.PurchasedBag
            binding.tvPriceIndPCAInvoiceAdapter.text = numberFormat(model.CurrentPrice.toDouble())
            binding.tvAmountIndPCAInvoiceAdapter.text = numberFormat(model.Amount.toDouble())

            binding.rlShopHeaderIndPCAInvoiceAdapter.setOnClickListener {
                model.isExpandable = !model.isExpandable
                notifyItemChanged(position)
            }

            binding.llExpandableIndPCAInvoiceAdapter.visibility =
                if (model.isExpandable) View.VISIBLE else View.GONE

            // Bind shop entries
            val shopRowAdapter = IndPCAShopRowAdapter(
                context,
                model.ShopEntries,
                adapterPosition,
                object : OnChildCalculationEdit {
                    override fun onChildCalculationEdited(
                        parentPosition: Int,
                        childPosition: Int,
                        shopList: ArrayList<IndPCAShopEntries>
                    ) {
                        dataList[parentPosition].ShopEntries = shopList
                        updateTotalData(parentPosition, dataList)
                    }
                })
            binding.rcViewShopEntries.adapter = shopRowAdapter
        }

        private fun updateTotalData(
            parentPosition: Int,
            dataList: ArrayList<APIIndividualInvoiceShopwise>
        ) {
            var bags = 0.0
            var amount = 0.0
            var rate = 0.0
            var commodityBhartiPrice = 0.0
            var weight = 0.0
            for (entry in dataList[parentPosition].ShopEntries) {
                bags += entry.BillBags.toDouble()
                amount += entry.Amount.toDouble()
                weight+= entry.BillWeight.toDouble()
                commodityBhartiPrice = entry.CommodityBhartiPrice.toDouble()
            }
//            rate = amount / ((bags * commodityBhartiPrice) / 20.0)
            rate = amount / (weight / 20.0)

            dataList[parentPosition].CurrentPrice = formatDecimal(rate)
            dataList[parentPosition].Amount = formatDecimal(amount)
            dataList[parentPosition].PurchasedBag = formatDecimal(bags)

            Log.d("???", "updateTotalData: ${dataList[parentPosition]}")
            binding.tvBagIndPCAInvoiceAdapter.text =
                numberFormat(dataList[parentPosition].PurchasedBag.toDouble())
            binding.tvPriceIndPCAInvoiceAdapter.text =
                numberFormat(dataList[parentPosition].CurrentPrice.toDouble())
            binding.tvAmountIndPCAInvoiceAdapter.text =
                numberFormat(dataList[parentPosition].Amount.toDouble())
        }
    }

    private fun formatDecimal(value: Double): String {
        return DecimalFormat("0.00").format(value)
    }

    private fun numberFormat(value: Double): String {
        return NumberFormat.getCurrencyInstance().format(value).substring(1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = IndPcaInvoiceTransactionAdapterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
}

class IndPCAShopRowAdapter(
    private val context: Context,
    private var dataList: ArrayList<IndPCAShopEntries>,
    private val parentPosition: Int,
    private val onChildCalculationEdit: OnChildCalculationEdit
) : RecyclerView.Adapter<IndPCAShopRowAdapter.MyViewHolder>() {

    private val TAG = "IndPCAShopRowAdapter"
    private val WEIGHT_DIVISOR = 20.0
    private val commonUIUtility = CommonUIUtility(context)

    inner class MyViewHolder(var binding: IndPcaInvoiceRowAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            // Set up TextWatchers
            binding.edtWeightIndPCAInvoiceRowAdapter.addTextChangedListener(createWeightTextWatcher { model ->
                model.Weight = binding.edtWeightIndPCAInvoiceRowAdapter.text.toString()
                calculateWeight(model)
            })

            binding.edtAmountIndPCAInvoiceRowAdapter.addTextChangedListener(createAmountTextWatcher { model ->
                model.Amount = binding.edtAmountIndPCAInvoiceRowAdapter.text.toString()
                calculateAmount(model)
            })
        }

        private fun createAmountTextWatcher(onAfterTextWatcher: (IndPCAShopEntries) -> Unit): TextWatcher {
            return object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val model = dataList[adapterPosition]

                    if (binding.edtAmountIndPCAInvoiceRowAdapter.text.toString().isEmpty()) {
                        binding.edtAmountIndPCAInvoiceRowAdapter.setText("0")
                        binding.edtAmountIndPCAInvoiceRowAdapter.setSelection(1)
                    }
                    if (binding.edtAmountIndPCAInvoiceRowAdapter.text.toString().length >= 2 && binding.edtAmountIndPCAInvoiceRowAdapter.text.toString()
                            .startsWith("0")
                    ) {
                        val subStr =
                            binding.edtAmountIndPCAInvoiceRowAdapter.text.toString().substring(1)
                        binding.edtAmountIndPCAInvoiceRowAdapter.setText(subStr)
                        binding.edtAmountIndPCAInvoiceRowAdapter.setSelection(1)
                    }
                    onAfterTextWatcher(model)
                }
            }
        }

        private fun createWeightTextWatcher(onAfterTextWatcher: (IndPCAShopEntries) -> Unit): TextWatcher {
            return object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val model = dataList[adapterPosition]

                    if (binding.edtWeightIndPCAInvoiceRowAdapter.text.toString().isEmpty()) {
                        binding.edtWeightIndPCAInvoiceRowAdapter.setText("0")
                        binding.edtWeightIndPCAInvoiceRowAdapter.setSelection(1)
                    }
                    if (binding.edtWeightIndPCAInvoiceRowAdapter.text.toString().length >= 2 && binding.edtWeightIndPCAInvoiceRowAdapter.text.toString()
                            .startsWith("0")
                    ) {
                        val subStr =
                            binding.edtWeightIndPCAInvoiceRowAdapter.text.toString().substring(1)
                        binding.edtWeightIndPCAInvoiceRowAdapter.setText(subStr)
                        binding.edtWeightIndPCAInvoiceRowAdapter.setSelection(1)
                    }
                    onAfterTextWatcher(model)
                }
            }
        }

        fun calculateWeight(model: IndPCAShopEntries) {
            try {
                val currentWeight = binding.edtWeightIndPCAInvoiceRowAdapter.text.toString().toDouble()

                val currentAmount = if (binding.edtAmountIndPCAInvoiceRowAdapter.text.toString().isEmpty()){
                    model.Amount.toDouble()
                }else
                {
                    binding.edtAmountIndPCAInvoiceRowAdapter.text.toString().toDouble()
                }


//                    val currentWeight = weightText.toDoubleOrNull() ?: 0.0
//                    val currentAmount = amountText.toDoubleOrNull() ?: 0.0

                if (currentWeight > 0.0) {
                    var currentRate = 0.0

                    if (currentAmount.equals(model.Amount.toDouble())){
                        currentRate = model.CurrentPrice.toDouble()
                    }else{
                        currentRate = formatDecimal(currentAmount / (currentWeight / WEIGHT_DIVISOR)).toDouble()
                    }
                    val bagsAfterWeightCalc =
                        formatDecimal(currentWeight / model.CommodityBhartiPrice.toDouble())
                    val calculatedAmount =
                        formatDecimal((currentWeight / WEIGHT_DIVISOR) * currentRate)
                    val currGST =
                        formatDecimal(calculatedAmount.toDouble() * (model.TotalPct.toDouble() / 100.0))
                    val currTOTAmount =
                        formatDecimal(currGST.toDouble() + calculatedAmount.toDouble())

                    var totalInvoiceApproxKG = formatDecimal(currentWeight / model.CommodityBhartiPrice.toDouble())
                    var totalInvoiceKG =formatDecimal(currentWeight % model.CommodityBhartiPrice.toDouble())

                    // Update model values
                    model.BillWeight = DecimalFormat("0.00").format(currentWeight)
                    model.BillAmount = DecimalFormat("0.00").format(currentAmount)
                    model.BillRate = currentRate.toString()
                    model.BillBags = bagsAfterWeightCalc
                    model.BillTotalAmount = currTOTAmount
                    model.BillGST = currGST
                    model.BillApproxKg = totalInvoiceApproxKG
                    model.BillKg = totalInvoiceKG

                    Log.d(TAG,"calculateWeightNAmount: ========================================")
                    Log.d(TAG, "calculateWeight: BILL_BAGS : ${model.BillBags}")
                    Log.d(TAG, "calculateWeight: BILL_AMOUNT : ${model.BillAmount}")
                    Log.d(TAG, "calculateWeight: BILL_RATE : ${model.BillRate}")
                    Log.d(TAG, "calculateWeight: BILL_WEIGHT : ${model.BillWeight}")
                    Log.d(TAG,"calculateWeight: BILL_TOTAL_AMOUNT : ${model.BillTotalAmount}")
                    Log.d(TAG, "calculateWeight: BILL_GST : ${model.BillGST}")
                    Log.d(TAG,"calculateWeight: BILL_APPROX_KG : ${model.BillApproxKg}")

                    binding.tvRateIndPCAInvoiceRowAdapter.text =
                        numberFormat(currentRate)
                    binding.tvGSTIndPCAInvoiceRowAdapter.text = numberFormat(currGST.toDouble())
                    binding.tvTotAmountIndPCAInvoiceRowAdapter.text =
                        numberFormat(currTOTAmount.toDouble())

                    binding.tvBagsIndPCAInvoiceRowAdapter.text = model.BillBags

                } else {
                    model.BillWeight = "0"
                    model.BillBags = "0"
                    model.BillApproxKg = "0"
                    model.BillKg = "0"
                    resetWeightCalculatedFields()
                }
                    model.isWeightBlank = false
                    onChildCalculationEdit.onChildCalculationEdited(parentPosition,adapterPosition,dataList)
                Log.d(TAG, "calculateWeightNAmount: onChildCalculationEdit : CALLED")

            } catch (e: NumberFormatException) {
                Log.e(TAG, "calculateWeightNAmount: ${e.message}")
            }
        }
        fun calculateAmount(model: IndPCAShopEntries) {
            try {
                val currentWeight = binding.edtWeightIndPCAInvoiceRowAdapter.text.toString().toDouble()
                val currentAmount = binding.edtAmountIndPCAInvoiceRowAdapter.text.toString().toDouble()


//                    val currentWeight = weightText.toDoubleOrNull() ?: 0.0
//                    val currentAmount = amountText.toDoubleOrNull() ?: 0.0

                if (currentWeight > 0.0 && currentAmount > 0.0) {
                    val currentRate =
                        formatDecimal(currentAmount / (currentWeight / WEIGHT_DIVISOR))
                    val bagsAfterWeightCalc =
                        formatDecimal(currentWeight / model.CommodityBhartiPrice.toDouble())
                    val calculatedAmount =
                        formatDecimal((currentWeight / WEIGHT_DIVISOR) * currentRate.toDouble())
                    val currGST =
                        formatDecimal(calculatedAmount.toDouble() * (model.TotalPct.toDouble() / 100.0))
                    val currTOTAmount =
                        formatDecimal(currGST.toDouble() + calculatedAmount.toDouble())

                    var totalInvoiceApproxKG = formatDecimal(currentWeight / model.CommodityBhartiPrice.toDouble())
                    var totalInvoiceKG =formatDecimal(currentWeight % model.CommodityBhartiPrice.toDouble())

                    // Update model values
                    model.BillWeight = DecimalFormat("0.00").format(currentWeight)
                    model.BillAmount = DecimalFormat("0.00").format(currentAmount)
                    model.BillRate = currentRate
                    model.BillBags = bagsAfterWeightCalc
                    model.BillTotalAmount = currTOTAmount
                    model.BillGST = currGST
                    model.BillApproxKg = totalInvoiceApproxKG
                    model.BillKg = totalInvoiceKG

                    Log.d(TAG,"calculateWeightNAmount: ========================================")
                    Log.d(TAG, "calculateWeightNAmount: BILL_BAGS : ${model.BillBags}")
                    Log.d(TAG, "calculateWeightNAmount: BILL_AMOUNT : ${model.BillAmount}")
                    Log.d(TAG, "calculateWeightNAmount: BILL_RATE : ${model.BillRate}")
                    Log.d(TAG, "calculateWeightNAmount: BILL_WEIGHT : ${model.BillWeight}")
                    Log.d(TAG,"calculateWeightNAmount: BILL_TOTAL_AMOUNT : ${model.BillTotalAmount}")
                    Log.d(TAG, "calculateWeightNAmount: BILL_GST : ${model.BillGST}")
                    Log.d(TAG,"calculateWeightNAmount: BILL_APPROX_KG : ${model.BillApproxKg}")

                    binding.tvRateIndPCAInvoiceRowAdapter.text =
                        numberFormat(currentRate.toDouble())
                    binding.tvGSTIndPCAInvoiceRowAdapter.text = numberFormat(currGST.toDouble())
                    binding.tvTotAmountIndPCAInvoiceRowAdapter.text =
                        numberFormat(currTOTAmount.toDouble())

                    binding.tvBagsIndPCAInvoiceRowAdapter.text = model.BillBags

                } else {
                    model.BillAmount = "0"
                    model.BillRate = "0"
                    model.BillTotalAmount = "0"
                    model.BillGST = "0"
                    resetCalculatedFields()
                }
                model.isAmountBlank = false
                onChildCalculationEdit.onChildCalculationEdited(parentPosition,adapterPosition,dataList)
                Log.d(TAG, "calculateWeightNAmount: onChildCalculationEdit : CALLED")

            } catch (e: NumberFormatException) {
                Log.e(TAG, "calculateWeightNAmount: ${e.message}")
            }
        }

        private fun resetCalculatedFields() {
            binding.tvRateIndPCAInvoiceRowAdapter.text = "0"
            binding.tvGSTIndPCAInvoiceRowAdapter.text = "0"
            binding.tvTotAmountIndPCAInvoiceRowAdapter.text = "0"
            binding.tvBagsIndPCAInvoiceRowAdapter.text = "0"
        }
        private fun resetWeightCalculatedFields() {
            binding.tvBagsIndPCAInvoiceRowAdapter.text = "0"
        }

        private fun formatDecimal(value: Double): String {
            return DecimalFormat("0.00").format(value)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = IndPcaInvoiceRowAdapterBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = dataList[position]
        model.Date = model.Date.split("T")[0]
        holder.binding.tvBuyerNameIndPCAInvoiceRowAdapter.text = model.BuyerName
        holder.binding.tvBagsIndPCAInvoiceRowAdapter.text = model.Bags
        holder.binding.tvRateIndPCAInvoiceRowAdapter.text =
            numberFormat(model.CurrentPrice.toDouble())
        holder.binding.edtAmountIndPCAInvoiceRowAdapter.setHint(model.Amount)
        holder.binding.edtWeightIndPCAInvoiceRowAdapter.setHint(model.Weight)
        holder.binding.tvHAmountIndPCAInvoiceRowAdapter.setText(model.Amount)
        holder.binding.tvHWeightIndPCAInvoiceRowAdapter.setText(model.Weight)

        model.isAmountBlank = true
        model.isWeightBlank = true

        holder.binding.edtWeightIndPCAInvoiceRowAdapter.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus){
                val edtText = holder.binding.edtWeightIndPCAInvoiceRowAdapter.text.toString()

                // Check if the EditText is empty
                if (edtText.isEmpty()) {
                    // Request focus back if it's empty
                    commonUIUtility.showToast(context.getString(R.string.weight_cannot_be_empty_alert_msg))
                    holder.binding.edtWeightIndPCAInvoiceRowAdapter.requestFocus()
                }
                // Check if the value is 0.0
                else if (edtText.toDouble() == 0.0) {
                    val formattedAmount = formatDecimal(holder.binding.tvHWeightIndPCAInvoiceRowAdapter.text.toString().toDouble())
                    holder.binding.edtWeightIndPCAInvoiceRowAdapter.setText(formattedAmount)
                }else if (edtText.endsWith(".")) {
                    holder.binding.edtWeightIndPCAInvoiceRowAdapter.append("0")
                }
            }else{
                holder.binding.edtAmountIndPCAInvoiceRowAdapter.clearFocus()
            }
        }

        holder.binding.edtAmountIndPCAInvoiceRowAdapter.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                val edtText = holder.binding.edtAmountIndPCAInvoiceRowAdapter.text.toString()

                // Check if the EditText is empty
                if (edtText.isEmpty()) {
                    // Request focus back if it's empty
                    commonUIUtility.showToast(context.getString(R.string.amount_cannot_be_empty_alert_msg))
                    holder.binding.edtAmountIndPCAInvoiceRowAdapter.requestFocus()
                }
                // Check if the value is 0.0
                else if (edtText.toDouble() == 0.0) {
                    val formattedAmount = formatDecimal(holder.binding.tvHAmountIndPCAInvoiceRowAdapter.text.toString().toDouble())
                    holder.binding.edtAmountIndPCAInvoiceRowAdapter.setText(formattedAmount)
                }else if (edtText.endsWith(".")) {
                    holder.binding.edtAmountIndPCAInvoiceRowAdapter.append("0")
                }
            }else{
                holder.binding.edtWeightIndPCAInvoiceRowAdapter.clearFocus()
            }
        }

        holder.binding.edtWeightIndPCAInvoiceRowAdapter.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                if (holder.binding.edtWeightIndPCAInvoiceRowAdapter.text.toString().isEmpty()) {
                    commonUIUtility.showToast(context.getString(R.string.weight_cannot_be_empty_alert_msg))
                    holder.binding.edtWeightIndPCAInvoiceRowAdapter.requestFocus()
                    true
                } else {
                    false // Move to the next field
                }
            } else {
                false
            }
        }


        val GSTAmount = formatDecimal(model.Amount.toDouble() * (model.TotalPct.toDouble() / 100.0))
        val totAmount = formatDecimal(model.Amount.toDouble() + GSTAmount.toDouble())

        holder.binding.tvGSTIndPCAInvoiceRowAdapter.text = numberFormat(GSTAmount.toDouble())
        holder.binding.tvTotAmountIndPCAInvoiceRowAdapter.text = numberFormat(totAmount.toDouble())

        model.BillAmount = model.Amount
        model.BillWeight = model.Weight
        model.BillGST = formatDecimal(model.Amount.toDouble() * (model.TotalPct.toDouble() / 100.0))
        model.BillTotalAmount = formatDecimal(model.Amount.toDouble() + model.BillGST.toDouble())
        model.BillBags = model.Bags
        model.BillRate = formatDecimal(model.Amount.toDouble() / (model.Weight.toDouble() / 20.0))
    }
}

private fun formatDecimal(value: Double): String {
    return DecimalFormat("0.00").format(value)
}

private fun numberFormat(value: Double): String {
    return NumberFormat.getCurrencyInstance().format(value).substring(1)
}


interface OnChildCalculationEdit {
    fun onChildCalculationEdited(
        parentPosition: Int,
        childPosition: Int,
        dataList: ArrayList<IndPCAShopEntries>
    )
}