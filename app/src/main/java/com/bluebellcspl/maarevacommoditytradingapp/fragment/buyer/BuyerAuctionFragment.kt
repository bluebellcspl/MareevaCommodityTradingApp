package com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer

import android.app.AlertDialog
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.BuyerAuctionDetailDialogLayoutBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentBuyerAuctionBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.PcaAuctionDetailDailogLayoutBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CalendarConstraints.DateValidator
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Locale


class BuyerAuctionFragment : Fragment() {
    lateinit var binding: FragmentBuyerAuctionBinding
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val TAG = "BuyerAuctionFragment"
    private val navController by lazy { findNavController() }
    lateinit var datePicker: MaterialDatePicker<Long>
    lateinit var alertDialog: AlertDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_buyer_auction, container, false)
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val calendarConstraints =
            CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now())
                .build()
        datePicker = MaterialDatePicker.Builder.datePicker().setSelection(today)
            .setCalendarConstraints(calendarConstraints)
            .setTitleText("Select Date").build()
        binding.tvDateBuyerAuctionFragment.text = DateUtility().getyyyyMMdd()
        binding.cvDateBuyerAuctionFragment.setOnClickListener {
            if (!datePicker.isAdded) {
                datePicker.show(childFragmentManager, "Date_Picker")
            }
        }
        datePicker.addOnPositiveButtonClickListener {
            val selectedDate = datePicker.selection
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = dateFormat.format(selectedDate)
            binding.tvDateBuyerAuctionFragment.text = date.toString()
        }

        binding.fabAddAuctionBuyerAuctionFragment.setOnClickListener {
            showPCAAddAuctionDialog()
        }
        return binding.root
    }

    fun showPCAAddAuctionDialog() {
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

            val approvedPCAList = getPCAName()
            val pcaAdapter = commonUIUtility.getCustomArrayAdapter(approvedPCAList)
            dialogBinding.actPCABuyerAuctionDialog.setAdapter(pcaAdapter)

        } catch (e: Exception) {
            Log.e(TAG, "showTaskAllocationDialog: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun getPCAName():ArrayList<String>{
        var dataList:ArrayList<String> = ArrayList()
        try {
            val cursor = DatabaseManager.ExecuteRawSql(Query.getApprovedPCAName())
            if (cursor!=null && cursor.count>0)
            {
                dataList.clear()
                while (cursor.moveToNext())
                {
                    dataList.add(cursor.getString(cursor.getColumnIndexOrThrow("PCAName")))
                }
            }
            cursor?.close()
        }catch (e:Exception)
        {
            dataList.clear()
            e.printStackTrace()
            Log.e(TAG, "getPCAName: ${e.message}")
        }
        Log.d(TAG, "getPCAName: APPROVED_PCA_LIST : $dataList")
        return dataList
    }
}