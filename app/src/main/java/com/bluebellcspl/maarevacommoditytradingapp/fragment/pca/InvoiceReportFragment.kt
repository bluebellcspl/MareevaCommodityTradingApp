package com.bluebellcspl.maarevacommoditytradingapp.fragment.pca

import ConnectionCheck
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.Environment
import android.os.Parcel
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.InvoiceReportAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.FileDownloader
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.constants.URLHelper
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentInvoiceReportBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchInvoiceReportAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceReportJSONParamModel
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceReportModelItem
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.Locale


class InvoiceReportFragment : Fragment(),InvoiceReportListHelper {
    var _binding : FragmentInvoiceReportBinding?=null
    val binding get() = _binding!!
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val fileDownloader by lazy { FileDownloader.getInstance(requireContext()) }
    lateinit var _invoiceReportList:ArrayList<InvoiceReportModelItem>
    private val TAG = "InvoiceReportFragment"
    lateinit var adapter: InvoiceReportAdapter
    var fromSelectedDate = 0L
    var toSelectedDate = 0L
    lateinit var menuHost: MenuHost
    lateinit var invoiceReportJSONParamModel: InvoiceReportJSONParamModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding=DataBindingUtil.inflate(inflater,R.layout.fragment_invoice_report, container, false)

        menuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.invoice_report_menu,menu)

                val searchItem = menu?.findItem(R.id.btn_Search_InvoiceReport)
                val searchView = searchItem?.actionView as SearchView
                val searchText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)

                searchText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

                searchText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                searchView.queryHint = "Search"

                searchView.setOnQueryTextListener(object : OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        adapter.filter.filter(newText)
                        return false
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId){
                 R.id.btn_Search_InvoiceReport->{
                     return true
                 }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)

        binding.edtFromDateInvoiceReportFragment.setText(DateUtility().getyyyyMMdd())
        binding.edtToDateInvoiceReportFragment.setText(DateUtility().getyyyyMMdd())
        if (binding.edtFromDateInvoiceReportFragment.text.toString().isNotEmpty() && binding.edtToDateInvoiceReportFragment.text.toString().isNotEmpty())
        {
            fromSelectedDate = System.currentTimeMillis()
            toSelectedDate = System.currentTimeMillis()

            invoiceReportJSONParamModel = InvoiceReportJSONParamModel(
                PrefUtil.ACTION_RETRIEVE,
                PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,"").toString(),
                binding.edtFromDateInvoiceReportFragment.text.toString().trim(),
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString(),
                binding.edtToDateInvoiceReportFragment.text.toString().trim(),
            )
            if (ConnectionCheck.isConnected(requireContext())) {
                FetchInvoiceReportAPI(requireContext(),this,invoiceReportJSONParamModel)
            }
        }
        setOnClickListeners()
        return binding.root
    }
    private fun setOnClickListeners() {
        try {
            binding.edtFromDateInvoiceReportFragment.inputType = InputType.TYPE_NULL
            binding.edtToDateInvoiceReportFragment.inputType = InputType.TYPE_NULL

            binding.edtFromDateInvoiceReportFragment.setOnClickListener {
                showFromDatePickerDialog(binding.edtFromDateInvoiceReportFragment)
            }

            binding.edtToDateInvoiceReportFragment.setOnClickListener {
                if (binding.edtFromDateInvoiceReportFragment.text.toString().isNotEmpty()) {
                    showToDatePickerDialog(binding.edtToDateInvoiceReportFragment)
                } else {
                    commonUIUtility.showToast("Please Select From Date!")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "setOnClickListeners: ${e.message}")
        }
    }
    fun populateData(dataList: ArrayList<InvoiceReportModelItem>)
    {
        if (dataList.isNotEmpty())
        {
            _invoiceReportList = dataList
            adapter = InvoiceReportAdapter(requireContext(),_invoiceReportList,this)
            binding.rcViewInvoiceReportFragment.adapter = adapter
            binding.rcViewInvoiceReportFragment.invalidate()
        }
        else{
            binding.rcViewInvoiceReportFragment.adapter = null
            binding.rcViewInvoiceReportFragment.invalidate()
        }
    }

    private fun showFromDatePickerDialog(editText: TextInputEditText) {
        val calendarConstraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.before(System.currentTimeMillis()))
            .build()
        val builder = MaterialDatePicker.Builder.datePicker().setCalendarConstraints(calendarConstraints)
        if (fromSelectedDate>0L)
        {
            builder.setSelection(fromSelectedDate)
        }
        val datePicker = builder.setTitleText("Select From Date").build()
        datePicker.addOnPositiveButtonClickListener {
            // Handle the selected date
            fromSelectedDate = it
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = dateFormat.format(it)!!
            editText.setText(date)
            if (binding.edtToDateInvoiceReportFragment.text.toString().isNotEmpty()) {
                if (DateUtility().isStartDateBeforeEndDate(binding.edtFromDateInvoiceReportFragment.text.toString().trim(),binding.edtToDateInvoiceReportFragment.text.toString().trim(),"yyyy-MM-dd"))
                {
                    invoiceReportJSONParamModel = InvoiceReportJSONParamModel(
                        PrefUtil.ACTION_RETRIEVE,
                        PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,"").toString(),
                        binding.edtFromDateInvoiceReportFragment.text.toString().trim(),
                        PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString(),
                        binding.edtToDateInvoiceReportFragment.text.toString().trim(),
                    )
                    if (ConnectionCheck.isConnected(requireContext())) {
                        FetchInvoiceReportAPI(requireContext(),this,invoiceReportJSONParamModel)
                    }
                }
            }

        }
        if (!datePicker.isAdded) {
            datePicker.show(parentFragmentManager, datePicker.toString())
        }
    }

//    private fun showToDatePickerDialog(editText: TextInputEditText) {
//
//        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//        val date = dateFormat.parse(binding.edtFromDateInvoiceReportFragment.text.toString().trim())
//
//        val calendarConstraints = CalendarConstraints.Builder()
//            .setValidator(DateValidatorPointForward.from(date.time))
//            .build()
//        val builder =
//            MaterialDatePicker.Builder.datePicker().setCalendarConstraints(calendarConstraints)
//        if (toSelectedDate>0)
//        {
//            builder.setSelection(toSelectedDate)
//        }
//
//        val datePicker = builder.setTitleText("Select To Date").build()
//        datePicker.addOnPositiveButtonClickListener {
//            // Handle the selected date
//            toSelectedDate = it
//            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
//            val date = dateFormat.format(it)!!
//            editText.setText(date)
//            if (DateUtility().isStartDateBeforeEndDate(binding.edtFromDateInvoiceReportFragment.text.toString().trim(),binding.edtToDateInvoiceReportFragment.text.toString().trim(),"yyyy-MM-dd"))
//            {
//                invoiceReportJSONParamModel = InvoiceReportJSONParamModel(
//                    PrefUtil.ACTION_RETRIEVE,
//                    PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,"").toString(),
//                    binding.edtFromDateInvoiceReportFragment.text.toString().trim(),
//                    PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString(),
//                    binding.edtToDateInvoiceReportFragment.text.toString().trim(),
//                )
//                if (ConnectionCheck.isConnected(requireContext())) {
//                    FetchInvoiceReportAPI(requireContext(),this,invoiceReportJSONParamModel)
//                }
//            }
//        }
//        if (!datePicker.isAdded) {
//            datePicker.show(parentFragmentManager, datePicker.toString())
//        }
//    }

    private fun showToDatePickerDialog(editText: TextInputEditText) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fromDateString = binding.edtFromDateInvoiceReportFragment.text.toString().trim()

        val fromDate: Date? = try {
            dateFormat.parse(fromDateString)
        } catch (e: Exception) {
            null
        }

        if (fromDate == null) {
            editText.error = "Invalid 'From Date'"
            return
        }


        val today = Calendar.getInstance()

        val fromDateCalendar = Calendar.getInstance().apply {
            time = fromDate
        }

        val calendarConstraints = CalendarConstraints.Builder()
            .setStart(fromDateCalendar.timeInMillis)  // Set the start to the 'From Date'
            .setEnd(today.timeInMillis)  // Set the end to today's date
            .setValidator(object : CalendarConstraints.DateValidator {
                override fun describeContents(): Int = 0

                override fun writeToParcel(dest: Parcel, flags: Int) {}

                override fun isValid(date: Long): Boolean {
                    return (date >= fromDateCalendar.timeInMillis && date <= today.timeInMillis)
                }
            })
            .build()

        val builder = MaterialDatePicker.Builder.datePicker()
            .setCalendarConstraints(calendarConstraints)

        if (toSelectedDate > 0) {
            builder.setSelection(toSelectedDate)
        }

        val datePicker = builder.setTitleText("Select To Date").build()

        datePicker.addOnPositiveButtonClickListener { selectedDateInMillis ->
            toSelectedDate = selectedDateInMillis

            val selectedDateFormatted = dateFormat.format(selectedDateInMillis)
            editText.setText(selectedDateFormatted)

            if (DateUtility().isStartDateBeforeEndDate(
                    binding.edtFromDateInvoiceReportFragment.text.toString().trim(),
                    binding.edtToDateInvoiceReportFragment.text.toString().trim(),
                    "yyyy-MM-dd"
                )
            ) {
                // Step 9: Create JSON Param Model and Fetch the Invoice Report
                invoiceReportJSONParamModel = InvoiceReportJSONParamModel(
                    PrefUtil.ACTION_RETRIEVE,
                    PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE, "").toString(),
                    binding.edtFromDateInvoiceReportFragment.text.toString().trim(),
                    PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString(),
                    binding.edtToDateInvoiceReportFragment.text.toString().trim(),
                )

                if (ConnectionCheck.isConnected(requireContext())) {
                    FetchInvoiceReportAPI(requireContext(), this, invoiceReportJSONParamModel)
                }
            }
        }

        if (!datePicker.isAdded) {
            datePicker.show(parentFragmentManager, datePicker.toString())
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object{
        val PDF_TYPE = "pdf"
        val DOC_TYPE = "doc"
    }

    override fun onItemClicked(model: InvoiceReportModelItem, type: String) {
        try {
            val invoiceNo = model.InvoiceNo
            var fileUrl = ""
            var desc = ""
            val fileNameStringBuilder = StringBuilder()
            if (type.equals(PDF_TYPE))
            {
                fileUrl = URLHelper.INVOICE_PDF.replace("<INVOICE_NO>",invoiceNo).replace("<PCA_REG_ID>",PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString())
                desc = "Downloading Invoice PDF"
                fileNameStringBuilder.clear()
                fileNameStringBuilder.append("Invoice_${model.InvoiceNo}_${model.Date}.pdf")
            }else if (type.equals(DOC_TYPE))
            {
                fileUrl = URLHelper.INVOICE_DOC.replace("<INVOICE_NO>",invoiceNo).replace("<PCA_REG_ID>",PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString())
                desc = "Downloading Invoice Doc"
                fileNameStringBuilder.clear()
                fileNameStringBuilder.append("Invoice_${model.InvoiceNo}_${model.Date}.doc")
            }
            Log.d(TAG, "onItemClicked: CURRENT_REG_ID : ${PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"")}")
            deleteFileIfExists(fileNameStringBuilder.toString())
            fileDownloader.downloadFile(fileUrl,fileNameStringBuilder.toString(),desc)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "onItemClicked: ${e.message}")
        }
    }

    fun deleteFileIfExists(fileName: String) {
        try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val fileToDelete = File(downloadDir, fileName)
            if (fileToDelete.exists()) {
                fileToDelete.delete()
            } else {
                Log.d(TAG, "deleteFileIfExists: FILE DOES NOT EXIST")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "deleteFileIfExists: ${e.message}")
        }
    }
}
interface InvoiceReportListHelper{
    fun onItemClicked(model:InvoiceReportModelItem,type:String)
}