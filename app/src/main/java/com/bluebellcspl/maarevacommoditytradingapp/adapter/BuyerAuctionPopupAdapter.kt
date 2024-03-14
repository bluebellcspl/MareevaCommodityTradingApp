package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.icu.text.NumberFormat
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.BuyerAuctionPopupPreviewAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.AuctionDetailsModel
import java.text.DecimalFormat

class BuyerAuctionPopupAdapter(
    var context: Context,
    var dataList: ArrayList<AuctionDetailsModel>,
    var commodityBhartiPrice: String
) : RecyclerView.Adapter<BuyerAuctionPopupAdapter.MyViewHolder>() {
    val TAG = "BuyerAuctionPopupAdapter"
    inner class MyViewHolder(var binding: BuyerAuctionPopupPreviewAdapterBinding) :
        RecyclerView.ViewHolder(binding.root){
        fun calcutateData(model: AuctionDetailsModel) {
            try {
                var upperLimit = binding.tvUpperLimitBuyerAuctionPreviewAdapter.text.toString().trim()
                var lowerLimit = binding.tvLowerLimitBuyerAuctionPreviewAdapter.text.toString().trim()
                var bags = binding.tvPCABagsBuyerAuctionPreviewAdapter.text.toString().trim()
                if (upperLimit.isNotEmpty() && lowerLimit.isNotEmpty() && bags.isNotEmpty()) {
                    if (model.UpdMarketCessRate.isEmpty())
                    {
                        model.UpdMarketCessRate = "0"
                    }
                    if (model.UpdPerBoriRate.isEmpty())
                    {
                        model.UpdPerBoriRate = "0"
                    }
                    if (model.UpdLabourCharge.isEmpty())
                    {
                        model.UpdLabourCharge = "0"
                    }
                    if (model.LabourCharge==null)
                    {
                        model.LabourCharge="0"
                    }
                    val BasicAmount =
                        ((bags.toDouble() * commodityBhartiPrice.toDouble()) / 20) * ((upperLimit.toDouble() + lowerLimit.toDouble()) / 2)
                    Log.d(TAG, "afterTextChanged: BAGS_AMOUNT : $BasicAmount")
                    var totalAmount = 0.0

                    var transportCharge =0.0
                    var labourCharge = 0.0

                    if (bags.toInt()>0 && upperLimit.toDouble()>0.0 && lowerLimit.toDouble()>0.0)
                    {
                        transportCharge =(bags.toDouble() * model.UpdPerBoriRate.toDouble())
                        labourCharge = model.UpdLabourCharge.toDouble() * bags.toDouble()
                    }

                    val gcaCommission = ((BasicAmount * model.UpdGCACommRate.toDouble()) / 100.0)
                    val pcaCommission = (BasicAmount * model.UpdPCACommRate.toDouble()) / 100.0
                    val marketCess = (BasicAmount * model.UpdMarketCessRate.toDouble()) / 100.0

                    Log.d(TAG, "afterTextChanged: PCA_NAME_MODEL : ${model.PCAName}")
                    Log.d(TAG, "afterTextChanged: MARKETCESS : $marketCess")
                    Log.d(TAG, "afterTextChanged: PCACOMISSION : $pcaCommission")
                    Log.d(TAG, "afterTextChanged: GCACOMISSION : $gcaCommission")
                    Log.d(TAG,"afterTextChanged: TRANSPORTATION_CHARGE at $adapterPosition : $transportCharge")
                    Log.d(TAG, "afterTextChanged: LABOURCHARGES : $labourCharge")
                    if (upperLimit.toDouble()>0.0 && lowerLimit.toDouble()>0.0 && bags.toInt()>0) {
                        totalAmount =BasicAmount + gcaCommission + pcaCommission + marketCess + transportCharge + labourCharge
                    }else
                    {
                        totalAmount = 0.0
                    }

                    Log.d(TAG, "afterTextChanged: TOTAL_AMOUNT : $totalAmount")
                    Log.d(
                        TAG,
                        "afterTextChanged: ================================================================================"
                    )

//                    binding.tvAmountBuyerAuctionPreviewAdapter.setText("%.2f".format(totalAmount))
                    val nf = NumberFormat.getCurrencyInstance().format(totalAmount).substring(1)
                    binding.tvAmountBuyerAuctionPreviewAdapter.setText(nf)
                    model.Bags = bags
                    model.Amount = DecimalFormat("0.00").format(totalAmount)
                    model.LowerLimit = lowerLimit
                    model.UpperLimit = upperLimit
                    model.Basic = DecimalFormat("0.00").format(BasicAmount)
                    model.TransportationCharge = DecimalFormat("0.00").format(transportCharge)
                    model.PerBoriRate = model.UpdPerBoriRate
                    model.PCACommCharge = DecimalFormat("0.00").format(pcaCommission)
                    model.PCACommRate = model.UpdPCACommRate
                    model.GCACommCharge = DecimalFormat("0.00").format(gcaCommission)
                    model.GCACommRate = model.UpdGCACommRate
                    model.LabourCharge = DecimalFormat("0.00").format(labourCharge)
                    model.PerBoriLabourCharge = model.UpdLabourCharge
                    model.MarketCessCharge = DecimalFormat("0.00").format(marketCess)
                    model.MarketCessRate = model.UpdMarketCessRate
                } else {
                    binding.tvAmountBuyerAuctionPreviewAdapter.setText("")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "calcutateData: ${e.message}")
            }
        }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(BuyerAuctionPopupPreviewAdapterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = dataList[holder.adapterPosition]
        if (PrefUtil.getSystemLanguage().equals("gu")) {
            if (model.GujaratiPCAShortName.isNotEmpty())
            {
                holder.binding.tvPCANameBuyerAuctionPreviewAdapter.setText(model.GujaratiPCAShortName)
            }else
            {
                holder.binding.tvPCANameBuyerAuctionPreviewAdapter.setText(model.PCAShortName)
            }
        } else {
            holder.binding.tvPCANameBuyerAuctionPreviewAdapter.setText(model.PCAShortName)
        }
        val nf = NumberFormat.getCurrencyInstance().format(model.Amount.toDouble())
        holder.binding.tvAmountBuyerAuctionPreviewAdapter.setText(nf.toString())
        holder.binding.tvPCABagsBuyerAuctionPreviewAdapter.setText("%s %s".format(context.getString(R.string.bags_lbl),model.Bags))
        var pcaLowerLimit = ""
        var pcaUpperLimit = ""
        if (model.LowerLimit.toDouble()>0.0 && model.UpperLimit.toDouble()>0.0)
        {
            pcaLowerLimit = model.LowerLimit
            pcaUpperLimit = model.UpperLimit
        }else
        {
            pcaLowerLimit = model.PCALowerLimit
            pcaUpperLimit = model.PCAUpperLimit
        }
        holder.binding.tvLowerLimitBuyerAuctionPreviewAdapter.setText(pcaLowerLimit)
        holder.binding.tvUpperLimitBuyerAuctionPreviewAdapter.setText(pcaUpperLimit)

        model.Basic = "0.0"
        holder.calcutateData(model)
        //TextWatcher
        val calculationTextWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if (holder.binding.tvPCABagsBuyerAuctionPreviewAdapter.text.toString().isNullOrBlank()) {
                    holder.binding.tvPCABagsBuyerAuctionPreviewAdapter.setText("0")
//                    holder.binding.tvPCABagsBuyerAuctionPreviewAdapter.setSelection(1)
                }
                if (holder.binding.tvPCABagsBuyerAuctionPreviewAdapter.text.toString().length >= 2 && holder.binding.tvPCABagsBuyerAuctionPreviewAdapter.text.toString()
                        .startsWith("0")
                ) {
                    val subStr =
                        holder.binding.tvPCABagsBuyerAuctionPreviewAdapter.text.toString().substring(1)
                    holder.binding.tvPCABagsBuyerAuctionPreviewAdapter.setText(subStr)
//                    holder.binding.tvPCABagsBuyerAuctionPreviewAdapter.setSelection(1)
                }
                holder.calcutateData(model)
            }
        }
        holder.binding.tvUpperLimitBuyerAuctionPreviewAdapter.addTextChangedListener(calculationTextWatcher)
        holder.binding.tvPCABagsBuyerAuctionPreviewAdapter.addTextChangedListener(calculationTextWatcher)
        holder.binding.tvLowerLimitBuyerAuctionPreviewAdapter.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                holder.binding.tvUpperLimitBuyerAuctionPreviewAdapter.setText("")
            }
        })
    }
}