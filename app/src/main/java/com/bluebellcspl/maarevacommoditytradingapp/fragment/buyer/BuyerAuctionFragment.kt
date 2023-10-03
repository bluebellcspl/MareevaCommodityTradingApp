package com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer

import android.app.AlertDialog
import android.icu.text.DecimalFormat
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.BuyerAuctionListAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.BuyerAuctionDetailDialogLayoutBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentBuyerAuctionBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.PcaAuctionDetailDailogLayoutBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchBuyerAuctionDetailAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.Detail
import com.bluebellcspl.maarevacommoditytradingapp.model.FetchBuyerAuctionDetail
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.RecyclerViewHelper
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CalendarConstraints.DateValidator
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.JsonObject
import java.math.RoundingMode
import java.util.Locale


class BuyerAuctionFragment : Fragment(),RecyclerViewHelper {
    lateinit var binding: FragmentBuyerAuctionBinding
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val TAG = "BuyerAuctionFragment"
    private val navController by lazy { findNavController() }
    lateinit var datePicker: MaterialDatePicker<Long>
    lateinit var alertDialog: AlertDialog
    lateinit var adapter: BuyerAuctionListAdapter
    lateinit var auctionDetailList: ArrayList<Detail>
    lateinit var pcaList : ArrayList<String>
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

//        binding.fabAddAuctionBuyerAuctionFragment.setOnClickListener {
//            showPCAAddAuctionDialog()
//        }

        binding.fabAddAuctionBuyerAuctionFragment.visibility=View.GONE

        //new design changes
         pcaList = getPCAName()
        binding.btnSubmitBuyerAuctionFragment.setOnClickListener {
            if (binding.edtTotalBagsBuyerAuctionFragment.text.toString().trim().isNotEmpty())
            {
                allocateBagsAutomatically()
            }
        }
        return binding.root
    }

    fun showPCAAddAuctionDialog(model:Detail,postion: Int) {
        try {
            val alertDailogBuilder = AlertDialog.Builder(requireContext())
            val dialogBinding = BuyerAuctionDetailDialogLayoutBinding.inflate(layoutInflater)
            val dialogView = dialogBinding.root
            alertDailogBuilder.setView(dialogView)
            alertDialog = alertDailogBuilder.create()
            alertDialog.setCanceledOnTouchOutside(true)
            alertDialog.setCancelable(true)
            alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()


//            val pcaAdapter = commonUIUtility.getCustomArrayAdapter(pcaList)
//            dialogBinding.actPCABuyerAuctionDialog.setAdapter(pcaAdapter)
//            dialogBinding.actPCABuyerAuctionDialog.isEnabled = false
            dialogBinding.actPCABuyerAuctionDialog.setText(model.PCAName)
            dialogBinding.edtBagsBuyerAuctionDialog.setText(model.Bag)
            dialogBinding.edtUpperLimitBuyerAuctionDialog.setText(model.UpperLimit)
            dialogBinding.edtLowerLimitBuyerAuctionDialog.setText(model.LowerLimit)
            dialogBinding.edtAmountBuyerAuctionDialog.setText(model.Amount)


            val calculationTextWatcher: TextWatcher = object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun afterTextChanged(p0: Editable?) {
                    val upperLimit = p0.toString().trim()
                    val lowerLimit =
                        dialogBinding.edtLowerLimitBuyerAuctionDialog.text.toString().trim()
                    val bags = dialogBinding.edtBagsBuyerAuctionDialog.text.toString().trim()
                        .trim()
                    if (upperLimit.isNotEmpty() && lowerLimit.isNotEmpty() && bags.isNotEmpty()) {
                        val amount = ((bags.toFloat() * 75) / 20 ) * ((upperLimit.toInt() + lowerLimit.toInt()) / 2)
                        Log.d(TAG, "afterTextChanged: BAGS_AMOUNT : $amount")
                        dialogBinding.edtAmountBuyerAuctionDialog.setText(amount.toString())
                    } else {
                        dialogBinding.edtAmountBuyerAuctionDialog.setText("")
                    }
                }
            }

            dialogBinding.edtUpperLimitBuyerAuctionDialog.addTextChangedListener(
                calculationTextWatcher
            )
            dialogBinding.edtBagsBuyerAuctionDialog.addTextChangedListener(calculationTextWatcher)
            dialogBinding.edtLowerLimitBuyerAuctionDialog.addTextChangedListener(
                calculationTextWatcher
            )

            dialogBinding.btnSaveBuyerAuctionDialog.setOnClickListener {
                if (dialogBinding.actPCABuyerAuctionDialog.text.toString().isEmpty()) {
                    commonUIUtility.showToast(resources.getString(R.string.please_enter_pca_name_alert_msg))
                } else if (dialogBinding.edtBagsBuyerAuctionDialog.text.toString().isEmpty()) {
                    commonUIUtility.showToast("Please Add Bags!")
                } else if (dialogBinding.edtLowerLimitBuyerAuctionDialog.text.toString()
                        .isEmpty()
                ) {
                    commonUIUtility.showToast("Please Enter Lower Limit!")
                } else if (dialogBinding.edtUpperLimitBuyerAuctionDialog.text.toString()
                        .isEmpty()
                ) {
                    commonUIUtility.showToast("Please Enter Upper Limit!")
                }
//                else if (dialogBinding.edtLastDayPriceBuyerAuctionDialog.text.toString()
//                        .isEmpty()
//                ) {
//                    commonUIUtility.showToast("Please Enter Last Day Price!")
//                }
                else {
                    val model = Detail(
                        dialogBinding.edtAmountBuyerAuctionDialog.text.toString().trim(),
                        dialogBinding.edtBagsBuyerAuctionDialog.text.toString().trim(),
                        model.CreateUser,
                        model.DetailsId,
                        model.GCACommission,
                        model.LastDayPrice,
                        dialogBinding.edtLowerLimitBuyerAuctionDialog.text.toString().trim(),
                        model.MarketCess,
                        model.PCACommission,
                        model.PCAId,
                        dialogBinding.actPCABuyerAuctionDialog.text.toString().trim(),
                        model.PCARegId,
                        dialogBinding.edtUpperLimitBuyerAuctionDialog.text.toString().trim()
                    )

                    auctionDetailList[postion] = model
                    alertDialog.dismiss()
                    bindRecyclerView(auctionDetailList)
                }

            }
        } catch (e: Exception) {
            Log.e(TAG, "showTaskAllocationDialog: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun getPCAName(): ArrayList<String> {
        var dataList: ArrayList<String> = ArrayList()
        try {
            val cursor = DatabaseManager.ExecuteRawSql(Query.getApprovedPCAName())
            if (cursor != null && cursor.count > 0) {
                dataList.clear()
                while (cursor.moveToNext()) {
                    dataList.add(cursor.getString(cursor.getColumnIndexOrThrow("PCAName")))
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

    fun updateUIFromAPIData(dataFromAPI: FetchBuyerAuctionDetail) {
        try {
            binding.edtTotalBagsBuyerAuctionFragment.setText(dataFromAPI.Header.TotalBags)
            binding.edtBudgetAmountBuyerAuctionFragment.setText(dataFromAPI.Header.BudgetAmount)
            binding.edtOtherCommissionBuyerAuctionFragment.setText(dataFromAPI.Header.OtherCommission)
            binding.tvBasicAmountBuyerAuctionFragment.setText(dataFromAPI.Header.Basic)
            binding.tvLeftBagsBuyerAuctionFragment.setText(dataFromAPI.Header.LeftBags)
            binding.tvGCACommissionBuyerAuctionFragment.setText(dataFromAPI.Header.GCACommission)
            binding.tvPCACommissionBuyerAuctionFragment.setText(dataFromAPI.Header.PCACommission)
            binding.tvMCCommissionBuyerAuctionFragment.setText(dataFromAPI.Header.MCCommission)
            binding.tvTotalAvgRateBuyerAuctionFragment.setText(dataFromAPI.Header.TotalAvgRate)
            auctionDetailList = dataFromAPI.Details
            bindRecyclerView(auctionDetailList)
        } catch (e: Exception) {
            Log.e(TAG, "updateUIFromAPIData: ${e.message}")
            e.printStackTrace()
        }
    }

    fun bindRecyclerView(dataList: ArrayList<Detail>) {
        try {
            if (dataList.isNotEmpty()) {
                adapter = BuyerAuctionListAdapter(requireContext(), dataList,this)
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

    fun allocateBagsAutomatically(){
        try {
            auctionDetailList= ArrayList()
            val pcaCount = pcaList.size
            val bagPerPCA = binding.edtTotalBagsBuyerAuctionFragment.text.toString().trim().toFloat()/pcaCount
            val remainingBags = binding.edtTotalBagsBuyerAuctionFragment.text.toString().trim().toFloat() % pcaCount
            val newbags = Math.floor(bagPerPCA.toDouble())
            val roundoff = String.format("%.2f0", newbags)
            val bagsPerPerson = distributeSchoolBags(binding.edtTotalBagsBuyerAuctionFragment.text.toString().toDouble(),pcaCount)
            for (i in 0 until pcaCount)
            {
                val detailModel = Detail("",bagsPerPerson[i].toString(),"","","","","","","","",pcaList[i],"","")
                auctionDetailList.add(detailModel)
            }
            bindRecyclerView(auctionDetailList)
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "allocateBagsAutomatically: ${e.message}")
        }
    }

    fun distributeSchoolBags(totalBags: Double, numberOfSellers: Int): List<Int> {
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

    override fun onBuyerAuctionPCAItemClick(postion: Int, model: Detail) {
        showPCAAddAuctionDialog(model,postion)
    }
}