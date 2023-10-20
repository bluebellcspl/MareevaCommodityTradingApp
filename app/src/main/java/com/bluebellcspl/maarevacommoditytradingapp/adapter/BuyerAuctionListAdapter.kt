package com.bluebellcspl.maarevacommoditytradingapp.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.databinding.BuyerAuctionItemAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.AuctionDetailsModel
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.RecyclerViewHelper
import java.math.RoundingMode
import java.text.DecimalFormat

class BuyerAuctionListAdapter(var context: Context, var dataList:ArrayList<AuctionDetailsModel>, var recyclerViewHelper: RecyclerViewHelper):RecyclerView.Adapter<BuyerAuctionListAdapter.MyViewHolder>() {

    private val TAG = "BuyerAuctionListAdapter"
    inner class MyViewHolder(var binding:BuyerAuctionItemAdapterBinding):
        RecyclerView.ViewHolder(binding.root){
        fun calcutateData(model: AuctionDetailsModel){
            try {
                var upperLimit = binding.tvUpperLimitBuyerAuctionItemAdapter.text.toString().trim()
                var lowerLimit =binding.tvLowerLimitBuyerAuctionItemAdapter.text.toString().trim()
                val bags = binding.tvBagsBuyerAuctionItemAdapter.text.toString().trim()
                if (upperLimit.isNotEmpty() && lowerLimit.isNotEmpty() && bags.isNotEmpty()) {

                    val BasicAmount = ((bags.toDouble() * 75) / 20 ) * ((upperLimit.toDouble() + lowerLimit.toDouble()) / 2)
                    Log.d(TAG, "afterTextChanged: BAGS_AMOUNT : $BasicAmount")
                    var totalAmount = 0.0

                    if (model.UpdGCACommRate.isEmpty())
                    {
                        model.UpdGCACommRate = "0.0"
                    }
                    if (model.UpdPCACommRate.isEmpty())
                    {
                        model.UpdPCACommRate = "0.0"
                    }
                    if (model.UpdMarketCessRate.isEmpty())
                    {
                        model.UpdMarketCessRate = "0.0"
                    }
                    if (model.UpdPerBoriRate.isEmpty())
                    {
                        model.UpdPerBoriRate = "0.0"
                    }
                    val gcaCommission = ((BasicAmount * model.UpdGCACommRate.toDouble())/100.0)
                    val pcaCommission = (BasicAmount * model.UpdPCACommRate.toDouble())/100.0
                    val marketCess = (BasicAmount * model.UpdMarketCessRate.toDouble())/100.0
                    var transportCharge = 0.0
                    if (model.TransportationCharge.toDouble()<1)
                    {
                        model.TransportationCharge = "0"
                        transportCharge = 0.0
                    }else
                    {
                     transportCharge = (model.Bags.toDouble() * model.UpdPerBoriRate.toDouble())
                    }
                    var labourCharge = model.UpdLabourCharge.toDouble()

                    Log.d(TAG, "afterTextChanged: MARKETCESS : $marketCess")
                    Log.d(TAG, "afterTextChanged: PCACOMISSION : $pcaCommission")
                    Log.d(TAG, "afterTextChanged: GCACOMISSION : $gcaCommission")
                    Log.d(TAG, "afterTextChanged: TRANSPORTATION_CHARGE : $transportCharge")
                    Log.d(TAG, "afterTextChanged: LABOURCHARGES : $labourCharge")
                    totalAmount = BasicAmount + gcaCommission+pcaCommission + marketCess +transportCharge+ labourCharge

                    Log.d(TAG, "afterTextChanged: TOTAL_AMOUNT : $totalAmount")
                    Log.d(TAG, "afterTextChanged: ================================================================================")
                    if (upperLimit.equals("0") && lowerLimit.equals("0")){
                        totalAmount = 0.0
                        transportCharge = 0.0
                        labourCharge = 0.0
                    }

                    binding.tvAmountBuyerAuctionItemAdapter.setText("%.2f".format(totalAmount))

                    model.Bags = bags
                    model.Amount = totalAmount.toString()
                    model.LowerLimit = lowerLimit
                    model.UpperLimit = upperLimit
                    model.PCABasic = BasicAmount.toString()
                    model.TransportationCharge = transportCharge.toString()
                    model.PerBoriRate = model.UpdPerBoriRate
                    model.PCACommCharge =  pcaCommission.toString()
                    model.PCACommRate =  model.UpdPCACommRate
                    model.GCACommCharge = gcaCommission.toString()
                    model.GCACommRate = model.UpdGCACommRate
                    model.UpdLabourCharge = labourCharge.toString()
                    model.MarketCessCharge = marketCess.toString()
                    recyclerViewHelper.getBuyerAuctionDataList(dataList)
                } else {
                    binding.tvAmountBuyerAuctionItemAdapter.setText("")
                }
            }catch (e:Exception)
            {
                e.printStackTrace()
                Log.e(TAG, "calcutateData: ${e.message}")
            }
        }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            BuyerAuctionItemAdapterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = dataList[holder.adapterPosition]
        holder.binding.tvPCANameBuyerAuctionItemAdapter.setText(model.PCAName)
        holder.binding.tvAmountBuyerAuctionItemAdapter.setText(model.Amount)
        holder.binding.tvBagsBuyerAuctionItemAdapter.setText(model.Bags)
        var pcaLowerLimit = ""
        var pcaUpperLimit = ""
        if (model.PCAUpperLimit.equals("0") && model.PCALowerLimit.equals("0"))
        {
            if ((model.LowerLimit.isNotEmpty() && model.LowerLimit.toDouble()>0.0) && (model.UpperLimit.isNotEmpty() && model.UpperLimit.toDouble()>0.0))
            {
                pcaLowerLimit = model.LowerLimit
                pcaUpperLimit = model.UpperLimit
            }else
            {
                pcaLowerLimit = "0"
                pcaUpperLimit = "0"
            }
        }else{
            pcaLowerLimit = model.PCALowerLimit
            pcaUpperLimit = model.PCAUpperLimit
        }
        holder.binding.tvLowerLimitBuyerAuctionItemAdapter.setText(pcaLowerLimit)
        holder.binding.tvUpperLimitBuyerAuctionItemAdapter.setText(pcaUpperLimit)
//        holder.binding.tvLastDayPriceBuyerAuctionItemAdapter.setText(model.LastDayPrice)
        model.PCABasic = "0.0"
        if (model.Bags.isEmpty() || model.Bags.equals("") || model.Bags.toInt()<1)
        {
            holder.binding.cvAuctionDetailsBuyerAuctionItemAdapter.visibility = View.GONE
            holder.binding.cvBagCountBuyerAuctionItemAdapter.visibility = View.GONE
        }
        if (model.Amount.isEmpty())
        {
            model.Amount = "0.0"
        }
        holder.calcutateData(model)
        //TextWatcher
        val calculationTextWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                holder.calcutateData(model)
            }
        }
        holder.binding.tvUpperLimitBuyerAuctionItemAdapter.addTextChangedListener(calculationTextWatcher)
        holder.binding.tvBagsBuyerAuctionItemAdapter.addTextChangedListener(calculationTextWatcher)
        holder.binding.tvLowerLimitBuyerAuctionItemAdapter.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun afterTextChanged(p0: Editable?) {
                    holder.binding.tvUpperLimitBuyerAuctionItemAdapter.setText("")
                }
            })
    }
}