package com.bluebellcspl.maarevacommoditytradingapp.fragment.pca

import android.app.AlertDialog
import android.icu.text.DateFormat
import android.net.wifi.rtt.CivicLocationKeys.STATE
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.navArgs
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentPCAAuctionBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.PcaAuctionDetailDailogLayoutBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Calendar


class PCAAuctionFragment : Fragment() {
    lateinit var binding:FragmentPCAAuctionBinding
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val TAG = "PCAAuctionFragment"
    lateinit var navController: NavController
    lateinit var alertDialog: AlertDialog
    private val args by navArgs<PCAAuctionFragmentArgs>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_p_c_a_auction, container, false)
        binding.tvAuctionItemCountPCAAuctionFragment.setText(args.userType)

        binding.fabAddAuctionPCAAuctionFragment.setOnClickListener {
            showPCAAddAuctionDialog()
        }
        return binding.root
    }

    fun showPCAAddAuctionDialog() {
        try {
            val alertDailogBuilder = AlertDialog.Builder(requireContext())
            val dialogBinding = PcaAuctionDetailDailogLayoutBinding.inflate(layoutInflater)
            val dialogView = dialogBinding.root
            alertDailogBuilder.setView(dialogView)
            alertDialog = alertDailogBuilder.create()
            alertDialog.setCanceledOnTouchOutside(true)
            alertDialog.setCancelable(true)
            alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()

            var shopNameList:ArrayList<String> = ArrayList()
            shopNameList.clear()
            shopNameList = getShopName()
            val shopAdapter = commonUIUtility.getCustomArrayAdapter(shopNameList)
            dialogBinding.actShopNamePCAAuctionDialog.setAdapter(shopAdapter)
            val shopTextWatcher:TextWatcher = object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun afterTextChanged(p0: Editable?) {
                    if (p0.toString().isNotEmpty())
                    {
                        val shopNo = DatabaseManager.ExecuteScalar(Query.getShopNoByShopName(p0.toString().trim(),PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString()))!!
                        dialogBinding.edtShopNoPCAAuctionDialog.setText("")
                        dialogBinding.edtShopNoPCAAuctionDialog.setText(shopNo)
                    }
                }
            }
            dialogBinding.actShopNamePCAAuctionDialog.addTextChangedListener(shopTextWatcher)
            dialogBinding.tvCurrentTimePCAAuctionDialog.setText(DateUtility().get12HourTime())
            dialogBinding.cvCurrentTimePCAAuctionDialog.setOnClickListener {
                showTimePickerDialog(dialogBinding.tvCurrentTimePCAAuctionDialog)
            }

        } catch (e: Exception) {
            Log.e(TAG, "showTaskAllocationDialog: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun showTimePickerDialog(textView: TextView) {
        val currentTime = Calendar.getInstance()
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
        val currentMinute = currentTime.get(Calendar.MINUTE)

        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(currentHour)
            .setMinute(currentMinute)
            .setTitleText("Select Time")
            .build()

        timePicker.addOnPositiveButtonClickListener {
            val selectedHour = timePicker.hour
            val selectedMinute = timePicker.minute

            val selectedTime = Calendar.getInstance()
            selectedTime.set(Calendar.HOUR_OF_DAY, selectedHour)
            selectedTime.set(Calendar.MINUTE, selectedMinute)

            val timeFormat: DateFormat = DateFormat.getTimeInstance(DateFormat.SHORT)
            val selectedTimeString = timeFormat.format(selectedTime.time)

//            val textViewSelectedTime = findViewById<TextView>(R.id.textViewSelectedTime)
            textView.text = selectedTimeString
        }
        if (!timePicker.isAdded) {
            timePicker.show(childFragmentManager, "timePicker")
        }
    }
    private fun getShopName():ArrayList<String>{
        var dataList:ArrayList<String> = ArrayList()
        try {
            val cursor = DatabaseManager.ExecuteRawSql(Query.getShopName(PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString()))
            if (cursor!=null && cursor.count>0)
            {
                dataList.clear()
                while (cursor.moveToNext())
                {
                    dataList.add(cursor.getString(cursor.getColumnIndexOrThrow("ShopName")))
                }
            }
            cursor?.close()
        }catch (e:Exception)
        {
            dataList.clear()
            e.printStackTrace()
            Log.e(TAG, "getShopNameFROMDB: ${e.message}")
        }
        Log.d(TAG, "getShopNameFROMDB: SHOPLIST : $dataList")
        return dataList
    }
}