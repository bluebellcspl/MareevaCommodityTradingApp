package com.bluebellcspl.maarevacommoditytradingapp.fragment.pca

import android.app.AlertDialog
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.PCAInvoiceAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.constants.Constants
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentPCAInvoiceBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.util.Locale


class PCAInvoiceFragment : Fragment() {
    lateinit var binding:FragmentPCAInvoiceBinding
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val TAG = "PCAInvoiceFragment"
    private val navController: NavController by lazy { findNavController() }
    lateinit var alertDialog: AlertDialog
    lateinit var adapter:PCAInvoiceAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_p_c_a_invoice, container, false)
        setOnClickListeners()
//        bindInvoiceRCView()

        return binding.root
    }

    private fun setOnClickListeners() {
        try {
            binding.edtFromDatePCAInvoiceFragment.inputType = InputType.TYPE_NULL
            binding.edtToDatePCAInvoiceFragment.inputType = InputType.TYPE_NULL
            binding.btnShowDataPCAInvoiceFragment.setOnClickListener {
                binding.edtTotalBagsContainerPCAInvoiceFragment.visibility = View.VISIBLE
                bindInvoiceRCView()
            }



            binding.edtFromDatePCAInvoiceFragment.setOnClickListener {
                showFromDatePickerDialog(binding.edtFromDatePCAInvoiceFragment)
            }

            binding.edtToDatePCAInvoiceFragment.setOnClickListener {
                if (binding.edtFromDatePCAInvoiceFragment.text.toString().isNotEmpty())
                {
                    showToDatePickerDialog(binding.edtToDatePCAInvoiceFragment)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "setOnClickListeners: ${e.message}", )
        }
    }

    private fun bindInvoiceRCView() {
        try {
            var dataList = arrayListOf(1,23,58,46,32,45)
            adapter = PCAInvoiceAdapter(requireContext(),dataList)
            binding.rcViewPCAInvoiceFragment.adapter = adapter
            binding.rcViewPCAInvoiceFragment.invalidate()
        }catch (e:Exception)
        {
            Log.e(TAG, "bindInvoiceRCView: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun showFromDatePickerDialog(editText:TextInputEditText) {
        val calendarConstraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.before(System.currentTimeMillis() - Constants.OneDayInMillies))
            .build()
        val builder =
            MaterialDatePicker.Builder.datePicker().setCalendarConstraints(calendarConstraints)

        val datePicker = builder.setTitleText("Select From Date").build()
        datePicker.addOnPositiveButtonClickListener {
            // Handle the selected date
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = dateFormat.format(it)!!
            editText.setText(date)


        }
        if (!datePicker.isAdded) {
            datePicker.show(childFragmentManager, datePicker.toString())
        }
    }

    private fun showToDatePickerDialog(editText:TextInputEditText) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.parse(binding.edtFromDatePCAInvoiceFragment.text.toString().trim())

        val calendarConstraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.from(date.time))
            .build()
        val builder =
            MaterialDatePicker.Builder.datePicker().setCalendarConstraints(calendarConstraints)

        val datePicker = builder.setTitleText("Select To Date").build()
        datePicker.addOnPositiveButtonClickListener {
            // Handle the selected date
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = dateFormat.format(it)!!
            editText.setText(date)


        }
        if (!datePicker.isAdded) {
            datePicker.show(childFragmentManager, datePicker.toString())
        }
    }
}