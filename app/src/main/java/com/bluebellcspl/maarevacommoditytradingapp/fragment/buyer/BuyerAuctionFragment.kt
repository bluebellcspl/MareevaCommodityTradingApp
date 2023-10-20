package com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer

import android.app.AlertDialog
import android.content.DialogInterface
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.BuyerAuctionListAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.BuyerAuctionDetailDialogLayoutBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.BuyerExpenseDialogLayoutBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentBuyerAuctionBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchBuyerAuctionDetailAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.AuctionDetailsModel
import com.bluebellcspl.maarevacommoditytradingapp.model.BuyerAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.Detail
import com.bluebellcspl.maarevacommoditytradingapp.model.FetchBuyerAuctionDetail
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.RecyclerViewHelper
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Locale


class BuyerAuctionFragment : Fragment(), RecyclerViewHelper {
    lateinit var binding: FragmentBuyerAuctionBinding
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val TAG = "BuyerAuctionFragment"
    private val navController by lazy { findNavController() }
    lateinit var datePicker: MaterialDatePicker<Long>
    lateinit var alertDialog: AlertDialog
    lateinit var adapter: BuyerAuctionListAdapter
    var auctionDetailList: ArrayList<AuctionDetailsModel> = ArrayList()
    var auctionDetailList2: ArrayList<AuctionDetailsModel> = ArrayList()
    lateinit var pcaDetailList: ArrayList<newPCAModel>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_buyer_auction, container, false)
        FetchBuyerAuctionDetailAPI(requireContext(), requireActivity(), this)
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val calendarConstraints =
            CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now())
                .build()
        datePicker = MaterialDatePicker.Builder.datePicker().setSelection(today)
            .setCalendarConstraints(calendarConstraints)
            .setTitleText("Select Date").build()
        binding.tvDateBuyerAuctionFragment.text = DateUtility().getCompletionDate()
        datePicker.addOnPositiveButtonClickListener {
            val selectedDate = datePicker.selection
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = dateFormat.format(selectedDate)
            binding.tvDateBuyerAuctionFragment.text = date.toString()
        }

        binding.fabAddAuctionBuyerAuctionFragment.visibility = View.GONE

        binding.cvTotalAmountBuyerAuctionFragment.setOnClickListener {
            showExpenseAuctionDialog(auctionDetailList)
        }

        binding.btnSubmitBuyerAuctionFragment.setOnClickListener {
            showAlertDialog()
        }
        //new design changes
        pcaDetailList = getPCADetails()
        Log.d(TAG, "onCreateView: PCA_DETAIL_LIST : $pcaDetailList")

        binding.edtTotalBagsBuyerAuctionFragment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString().isNotEmpty()) {
                    allocateBagsAutomatically(p0.toString().trim())
                } else {
//                    val detailModel = Detail("","","","","","","","","","","","","")
//                    auctionDetailList.add(detailModel)
                    bindRecyclerView(auctionDetailList)
                    binding.rcViewBuyerAuctionFragment.invalidate()
                    binding.rcViewBuyerAuctionFragment.adapter = null
                    binding.tvTotalAmountBuyerAuctionFragment.setText("")
                    binding.tvLeftBagsBuyerAuctionFragment.setText("")
                }
            }
        })
        return binding.root
    }

    private fun getPCADetails(): ArrayList<newPCAModel> {
        var dataList: ArrayList<newPCAModel> = ArrayList()
        try {
            val cursor = DatabaseManager.ExecuteRawSql(Query.getPCADetail())
            if (cursor != null && cursor.count > 0) {
                dataList.clear()
                while (cursor.moveToNext()) {
                    val model = newPCAModel(
                        cursor.getString(cursor.getColumnIndexOrThrow("PCAId")),
                        cursor.getString(cursor.getColumnIndexOrThrow("PCAName")),
                        cursor.getString(cursor.getColumnIndexOrThrow("PCARegId")),
                        cursor.getString(cursor.getColumnIndexOrThrow("GCACommission")),
                        cursor.getString(cursor.getColumnIndexOrThrow("PCACommission")),
                        cursor.getString(cursor.getColumnIndexOrThrow("MarketCess")),
                        cursor.getString(cursor.getColumnIndexOrThrow("LabourCharges")),
//                        cursor.getString(cursor.getColumnIndexOrThrow("TransportationCharges"))
                        ""
                    )
                    dataList.add(model)
                }
            }
            cursor?.close()
        } catch (e: Exception) {
            dataList.clear()
            e.printStackTrace()
            Log.e(TAG, "getPCAName: ${e.message}")
        }
        Log.d(TAG, "getPCAName: APPROVED_PCA_LIST : $dataList")
        return dataList
    }

    fun updateUIFromAPIData(dataFromAPI: BuyerAuctionMasterModel) {
//        auctionDetailList = ArrayList()
        try {
            if (dataFromAPI.BudgetAmount.isEmpty())
            {
                dataFromAPI.BudgetAmount = "0.0"
            }
            binding.edtTotalBagsBuyerAuctionFragment.setText(dataFromAPI.TotalBags)
            binding.tvTotalAmountBuyerAuctionFragment.setText(dataFromAPI.BudgetAmount)
            binding.edtOtherCommissionBuyerAuctionFragment.setText(dataFromAPI.Expenses)
            binding.tvBasicAmountBuyerAuctionFragment.setText(dataFromAPI.TotalBasic)
            binding.tvLeftBagsBuyerAuctionFragment.setText(dataFromAPI.LeftBags)
            auctionDetailList = dataFromAPI.AuctionDetailsModel
            bindRecyclerView(auctionDetailList)
        } catch (e: Exception) {
            Log.e(TAG, "updateUIFromAPIData: ${e.message}")
            e.printStackTrace()
        }
    }

    fun bindRecyclerView(dataList: ArrayList<AuctionDetailsModel>) {
        try {
            if (dataList.isNotEmpty()) {
                adapter = BuyerAuctionListAdapter(requireContext(), dataList, this)
                binding.rcViewBuyerAuctionFragment.adapter = adapter
                binding.rcViewBuyerAuctionFragment.invalidate()
            } else {
                commonUIUtility.showToast("No Details Found!")
            }
        } catch (e: Exception) {
            Log.e(TAG, "bindRecyclerView: ${e.message}")
            e.printStackTrace()
        }
    }

    fun allocateBagsAutomatically(bags: String) {
        try {
            auctionDetailList2 = ArrayList()
            val pcaCount = pcaDetailList.size
            val bagsPerPerson = distributeBagsEvenly(bags.toDouble(), pcaCount)
            for (i in 0 until pcaCount) {
                val auctionDetailsModel = AuctionDetailsModel(
                    auctionDetailList[i].APMCId,
                    auctionDetailList[i].APMCName,
                    auctionDetailList[i].Amount,
                    auctionDetailList[i].AuctionMasterId,
                    bagsPerPerson[i].toString(),
                    auctionDetailList[i].BuyerCityId,
                    auctionDetailList[i].CommodityId,
                    auctionDetailList[i].DetailsId,
                    auctionDetailList[i].DistrictId,
                    auctionDetailList[i].GCACommCharge,
                    auctionDetailList[i].GCACommRate,
                    auctionDetailList[i].LowerLimit,
                    auctionDetailList[i].MarketCessCharge,
                    auctionDetailList[i].MarketCessRate,
                    auctionDetailList[i].PCACityId,
                    auctionDetailList[i].PCACommCharge,
                    auctionDetailList[i].PCACommRate,
                    auctionDetailList[i].PCAId,
                    auctionDetailList[i].PCALowerLimit,
                    auctionDetailList[i].PCAName,
                    auctionDetailList[i].PCARegId,
                    auctionDetailList[i].PCAUpperLimit,
                    auctionDetailList[i].PerBoriRate,
                    auctionDetailList[i].StateId,
                    auctionDetailList[i].TransportId,
                    auctionDetailList[i].TransportationCharge,
                    auctionDetailList[i].UpdGCACommRate,
                    auctionDetailList[i].UpdLabourCharge,
                    auctionDetailList[i].UpdMarketCessRate,
                    auctionDetailList[i].UpdPCACommRate,
                    auctionDetailList[i].UpdPerBoriRate,
                    auctionDetailList[i].UpdTransportId,
                    auctionDetailList[i].UpperLimit,
                    "0.0"
                )
                auctionDetailList2.add(auctionDetailsModel)
            }
            bindRecyclerView(auctionDetailList2)
            calculateOtherExpenses(auctionDetailList2)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "allocateBagsAutomatically: ${e.message}")
        }
    }

    fun distributeBagsEvenly(totalBags: Double, numberOfSellers: Int): List<Int> {
        val totalBagsInt = totalBags.toInt()
        val bagsPerSeller = totalBagsInt / numberOfSellers
        val remainingBags = totalBagsInt % numberOfSellers

        val distributedBags = MutableList(numberOfSellers) { bagsPerSeller }

        // Distribute remaining bags evenly among sellers
        for (i in 0 until remainingBags) {
            distributedBags[i]++
        }

        return distributedBags
    }

    override fun onItemClick(postion: Int, onclickType: String) {

    }

    override fun onBuyerAuctionPCAItemClick(postion: Int, model: AuctionDetailsModel) {
//        showPCAAddAuctionDialog(model,postion)
    }

    override fun getBuyerAuctionDataList(dataList: ArrayList<AuctionDetailsModel>) {
//        dataList.forEach { model ->
//            Log.d(TAG, "getBuyerAuctionDataList: ELEMENT : $model")
//        }
        auctionDetailList = dataList
        calculateOtherExpenses(auctionDetailList)
    }

    fun calculateOtherExpenses(dataList: ArrayList<AuctionDetailsModel>) {
        if (dataList.isEmpty()) {
            commonUIUtility.showToast("No Bags are Allocated!")
        } else {
            var leftBags = ""
            var ab: Int = 0
            var basic: Double = 0.0
            var total:Double = 0.0
            for (model in dataList) {
                ab += model.Bags.toInt()
                basic += model.PCABasic.toDouble()
                total += model.Amount.toDouble()
                Log.d(TAG, "calculateOtherExpenses: ALLOCATED_BAGS : $ab")
                Log.d(TAG, "calculateOtherExpenses: TOTAL_AMOUNT : $total")
                leftBags = (binding.edtTotalBagsBuyerAuctionFragment.text.toString().trim()
                    .toInt() - ab).toString()
            }
            binding.tvLeftBagsBuyerAuctionFragment.setText(leftBags)
            binding.tvBasicAmountBuyerAuctionFragment.setText("%.2f".format(basic))
            binding.tvTotalAmountBuyerAuctionFragment.setText("%.2f".format(total))
        }
    }

    fun showExpenseAuctionDialog(dataList: ArrayList<AuctionDetailsModel>) {
        try {
            val alertDailogBuilder = AlertDialog.Builder(requireContext())
            val dialogBinding = BuyerExpenseDialogLayoutBinding.inflate(layoutInflater)
            val dialogView = dialogBinding.root
            alertDailogBuilder.setView(dialogView)
            alertDialog = alertDailogBuilder.create()
            alertDialog.setCanceledOnTouchOutside(true)
            alertDialog.setCancelable(true)
            alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()

            var basic: Double = 0.0
            var total:Double = 0.0
            var pcaCommission = 0.0
            var gcaCommission = 0.0
            var marketCess = 0.0
            var transportCharge = 0.0
            var labourCharge = 0.0
            for (model in dataList) {
                basic += model.PCABasic.toDouble()
                total += model.Amount.toDouble()
                pcaCommission += model.PCACommCharge.toDouble()
                gcaCommission += model.GCACommCharge.toDouble()
                marketCess += model.MarketCessCharge.toDouble()
                transportCharge += model.TransportationCharge.toDouble()
                Log.d(TAG, "showExpenseAuctionDialog: TC : ${model.TransportationCharge}")
                labourCharge += model.UpdLabourCharge.toDouble()
                Log.d(TAG, "showExpenseAuctionDialog: TOTAL_AMOUNT : $total")
                Log.d(TAG, "showExpenseAuctionDialog: TOTAL_PCACOMMISSION : $pcaCommission")
                Log.d(TAG, "showExpenseAuctionDialog: TOTAL_GCACOMMISSION : $gcaCommission")
                Log.d(TAG, "showExpenseAuctionDialog: TOTAL_MARKETCESS : $marketCess")
                Log.d(TAG, "showExpenseAuctionDialog: TOTAL_TRANSPORTATION_CHARGE : $transportCharge")
                Log.d(TAG, "showExpenseAuctionDialog: TOTAL_LABOUR_CHARGE : $labourCharge")
            }
            if (binding.tvTotalAmountBuyerAuctionFragment.text.toString().equals("") || binding.tvTotalAmountBuyerAuctionFragment.text.toString().isEmpty())
            {
                dialogBinding.tvTotalBasicAmountBuyerExpenseDialog.setText("0.0")
                dialogBinding.tvTotalPCACommissionBuyerExpenseDialog.setText("0.0")
                dialogBinding.tvTotalGCACommissionBuyerExpenseDialog.setText("0.0")
                dialogBinding.tvTotalMarketCessBuyerExpenseDialog.setText("0.0")
                dialogBinding.tvTotalTransportChargeBuyerExpenseDialog.setText("0.0")
                dialogBinding.tvTotalLabourChargeBuyerExpenseDialog.setText("0.0")
            }else {
                dialogBinding.tvTotalBasicAmountBuyerExpenseDialog.setText("%.2f".format(basic))
                dialogBinding.tvTotalPCACommissionBuyerExpenseDialog.setText("%.2f".format(pcaCommission))
                dialogBinding.tvTotalGCACommissionBuyerExpenseDialog.setText("%.2f".format(gcaCommission))
                dialogBinding.tvTotalMarketCessBuyerExpenseDialog.setText("%.2f".format(marketCess))
                dialogBinding.tvTotalTransportChargeBuyerExpenseDialog.setText("%.2f".format(transportCharge))
                dialogBinding.tvTotalLabourChargeBuyerExpenseDialog.setText("%.2f".format(labourCharge))
            }

        } catch (e: Exception) {
            Log.e(TAG, "showExpenseAuctionDialog: ${e.message}")
            e.printStackTrace()
        }
    }

    fun showAlertDialog(){
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle("Alert")
        alertDialog.setMessage("Do you want to submit Data?")
        alertDialog.setPositiveButton("YES",object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                p0!!.dismiss()
            }
        })
        alertDialog.setNegativeButton("NO",object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                p0!!.dismiss()
            }
        })
        alertDialog.show()
    }

    data class newPCAModel(
        var PCAId: String,
        var PCAName: String,
        var PCARegId: String,
        var GCACommission: String,
        var PCACommission: String,
        var MarketCess: String,
        var LabourCharges: String,
        var TransportationCharges: String
    )
}