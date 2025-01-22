package com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca

import ConnectionCheck
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.Environment
import android.os.Parcel
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.IndPCAInvoiceReportAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.FileDownloader
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.constants.URLHelper
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentIndPCAInvoiceReportBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCADashboardFragment.CommodityDetail
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.InvoiceReportFragment
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.InvoiceReportFragment.Companion
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchAPMCIntCommodityAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchIndPCAInvoiceReportAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoiceReportModelItem
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoiceReportModelJSON
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.Locale


class IndPCAInvoiceReportFragment : Fragment(),IndPCAInvoiceReportHelper {
    var _binding: FragmentIndPCAInvoiceReportBinding? = null
    val binding get() = _binding!!
    val TAG = "IndPCAInvoiceReportFragment"
    private lateinit var _CommodityList : ArrayList<CommodityDetail>
    private lateinit var _CommodityNameList : ArrayList<String>
    private var SELECTED_COMMODITY_ID = ""
    private var SELECTED_COMMODITY_NAME = ""
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val fileDownloader by lazy { FileDownloader.getInstance(requireContext()) }
    var fromSelectedDate = 0L
    var toSelectedDate = 0L
    private lateinit var invoiceJSONModel : IndPCAInvoiceReportModelJSON
    private var _InvoiceReportList:ArrayList<IndPCAInvoiceReportModelItem>?=null
    private lateinit var adapter: IndPCAInvoiceReportAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_ind_p_c_a_invoice_report, container, false)
        if (ConnectionCheck.isConnected(requireContext()))
        {
            FetchAPMCIntCommodityAPI(requireContext(),this@IndPCAInvoiceReportFragment)
        }else{
            commonUIUtility.showToast(requireContext().getString(R.string.no_internet_connection))
        }
        //Commodity DropDown
        SELECTED_COMMODITY_ID = PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"")!!

        if (PrefUtil.getSystemLanguage()!!.equals("gu")){
            var gujCommodityName = DatabaseManager.ExecuteScalar(
                Query.getGujaratiCommodityName(
                    PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"")!!))!!
            if (gujCommodityName.equals("invalid")){
                gujCommodityName = ""
            }
            PrefUtil.setString(PrefUtil.KEY_COMMODITY_NAME,gujCommodityName)
            binding.actCommodityIndPCAInvoiceReportFragment.setText(gujCommodityName)
        }else{
            var CommodityName = DatabaseManager.ExecuteScalar(
                Query.getCommodityName(
                    PrefUtil.getString(
                        PrefUtil.KEY_COMMODITY_ID,"")!!))!!
            PrefUtil.setString(PrefUtil.KEY_COMMODITY_NAME,CommodityName)
            if (CommodityName.equals("invalid")){
                CommodityName = ""
            }
            binding.actCommodityIndPCAInvoiceReportFragment.setText(CommodityName)
        }

        binding.actCommodityIndPCAInvoiceReportFragment.setOnItemClickListener { adapterView, view, position, long ->
            var commodityModel: CommodityDetail
            if (PrefUtil.getSystemLanguage().equals("gu")){
                commodityModel = _CommodityList.find {it.CommodityGujName.equals(adapterView.getItemAtPosition(position).toString())}!!
            }
            else{
                commodityModel = _CommodityList.find {it.CommodityName.equals(adapterView.getItemAtPosition(position).toString())}!!
            }

            Log.d(TAG, "onCreateView: SELECTED_COMMODITY_NAME : ${commodityModel.CommodityName}")
            Log.d(TAG, "onCreateView: SELECTED_COMMODITY_ID : ${commodityModel.CommodityId}")


            SELECTED_COMMODITY_ID = commodityModel.CommodityId
            SELECTED_COMMODITY_NAME = commodityModel.CommodityName

            if (ConnectionCheck.isConnected(requireContext()))
            {
                invoiceJSONModel.ToDate = DateUtility().formatToyyyyMMdd(binding.edtToDateIndPCAInvoiceReportFragment.text.toString().trim())
                invoiceJSONModel.FromDate = DateUtility().formatToyyyyMMdd(binding.edtFromDateIndPCAInvoiceReportFragment.text.toString().trim())
                invoiceJSONModel.CommodityId = SELECTED_COMMODITY_ID
                FetchIndPCAInvoiceReportAPI(requireContext(),this@IndPCAInvoiceReportFragment,invoiceJSONModel)
            }else{
                commonUIUtility.showToast(requireContext().getString(R.string.no_internet_connection))
            }
        }

        binding.edtFromDateIndPCAInvoiceReportFragment.setText(DateUtility().formatToddMMyyyy(DateUtility().getyyyyMMdd()))
        binding.edtToDateIndPCAInvoiceReportFragment.setText(DateUtility().formatToddMMyyyy(DateUtility().getyyyyMMdd()))

        invoiceJSONModel = IndPCAInvoiceReportModelJSON(
            "DateWiseDate",
            SELECTED_COMMODITY_ID,
            PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,"").toString(),
            DateUtility().formatToyyyyMMdd(binding.edtFromDateIndPCAInvoiceReportFragment.text.toString().trim()),
            PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString(),
            DateUtility().formatToyyyyMMdd(binding.edtToDateIndPCAInvoiceReportFragment.text.toString().trim())
        )
        FetchIndPCAInvoiceReportAPI(requireContext(),this@IndPCAInvoiceReportFragment,invoiceJSONModel)

        setOnClickListeners()
        return binding.root
    }

    fun bindCommodityList(commodityList: ArrayList<CommodityDetail>){
        _CommodityList = commodityList
        var commodityAdapter : ArrayAdapter<String>
        if(PrefUtil.getSystemLanguage().equals("gu")){
            _CommodityNameList = ArrayList(commodityList.map { it.CommodityGujName })
            commodityAdapter = commonUIUtility.getCustomArrayAdapter(_CommodityNameList)
        }else
        {
            _CommodityNameList = ArrayList(commodityList.map { it.CommodityName })
            commodityAdapter = commonUIUtility.getCustomArrayAdapter(_CommodityNameList)
        }

        binding.actCommodityIndPCAInvoiceReportFragment.setAdapter(commodityAdapter)
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
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            val date = dateFormat.format(it)!!
            editText.setText(date)
            if (binding.edtToDateIndPCAInvoiceReportFragment.text.toString().isNotEmpty()) {
                if (DateUtility().isStartDateBeforeEndDate(binding.edtFromDateIndPCAInvoiceReportFragment.text.toString().trim(),binding.edtToDateIndPCAInvoiceReportFragment.text.toString().trim(),"dd-MM-yyyy"))
                {
//                    invoiceReportJSONParamModel = InvoiceReportJSONParamModel(
//                        PrefUtil.ACTION_RETRIEVE,
//                        PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,"").toString(),
//                        binding.edtFromDateIndPCAInvoiceReportFragment.text.toString().trim(),
//                        PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString(),
//                        binding.edtToDateIndPCAInvoiceReportFragment.text.toString().trim(),
//                    )
//                    if (ConnectionCheck.isConnected(requireContext())) {
//                        FetchInvoiceReportAPI(requireContext(),this,invoiceReportJSONParamModel)
//                    }
                    if (!_CommodityNameList.contains(binding.actCommodityIndPCAInvoiceReportFragment.text.toString().trim()) || binding.actCommodityIndPCAInvoiceReportFragment.text.toString().trim().isEmpty()){
                        commonUIUtility.showToast(requireContext().getString(R.string.please_select_proper_commodity_alert_msg))
                    }else{
                        invoiceJSONModel.ToDate = DateUtility().formatToyyyyMMdd(binding.edtToDateIndPCAInvoiceReportFragment.text.toString().trim())
                        invoiceJSONModel.FromDate = DateUtility().formatToyyyyMMdd(binding.edtFromDateIndPCAInvoiceReportFragment.text.toString().trim())
                        invoiceJSONModel.CommodityId = SELECTED_COMMODITY_ID

                        if (ConnectionCheck.isConnected(requireContext())){
                            FetchIndPCAInvoiceReportAPI(requireContext(),this@IndPCAInvoiceReportFragment,invoiceJSONModel)
                        }else{
                            commonUIUtility.showToast(requireContext().getString(R.string.no_internet_connection))
                        }
                    }
                }
            }

        }
        if (!datePicker.isAdded) {
            datePicker.show(parentFragmentManager, datePicker.toString())
        }
    }

    private fun showToDatePickerDialog(editText: TextInputEditText) {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val fromDateString = binding.edtFromDateIndPCAInvoiceReportFragment.text.toString().trim()

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
                    binding.edtFromDateIndPCAInvoiceReportFragment.text.toString().trim(),
                    binding.edtToDateIndPCAInvoiceReportFragment.text.toString().trim(),
                    "dd-MM-yyyy"
                )
            ) {
                // Step 9: Create JSON Param Model and Fetch the Invoice Report

                if (!_CommodityNameList.contains(binding.actCommodityIndPCAInvoiceReportFragment.text.toString().trim()) || binding.actCommodityIndPCAInvoiceReportFragment.text.toString().trim().isEmpty()){
                    commonUIUtility.showToast(requireContext().getString(R.string.please_select_proper_commodity_alert_msg))
                }else{
                    invoiceJSONModel.ToDate = DateUtility().formatToyyyyMMdd(binding.edtToDateIndPCAInvoiceReportFragment.text.toString().trim())
                    invoiceJSONModel.FromDate = DateUtility().formatToyyyyMMdd(binding.edtFromDateIndPCAInvoiceReportFragment.text.toString().trim())
                    invoiceJSONModel.CommodityId = SELECTED_COMMODITY_ID

                    if (ConnectionCheck.isConnected(requireContext())){
                        FetchIndPCAInvoiceReportAPI(requireContext(),this@IndPCAInvoiceReportFragment,invoiceJSONModel)
                    }else{
                        commonUIUtility.showToast(requireContext().getString(R.string.no_internet_connection))
                    }
                }
            }
        }

        if (!datePicker.isAdded) {
            datePicker.show(parentFragmentManager, datePicker.toString())
        }
    }

    private fun setOnClickListeners() {
        try {
            binding.edtFromDateIndPCAInvoiceReportFragment.inputType = InputType.TYPE_NULL
            binding.edtToDateIndPCAInvoiceReportFragment.inputType = InputType.TYPE_NULL

            binding.edtFromDateIndPCAInvoiceReportFragment.setOnClickListener {
                showFromDatePickerDialog(binding.edtFromDateIndPCAInvoiceReportFragment)
            }

            binding.edtToDateIndPCAInvoiceReportFragment.setOnClickListener {
                if (binding.edtFromDateIndPCAInvoiceReportFragment.text.toString().isNotEmpty()) {
                    showToDatePickerDialog(binding.edtToDateIndPCAInvoiceReportFragment)
                } else {
                    commonUIUtility.showToast("Please Select From Date!")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "setOnClickListeners: ${e.message}")
        }
    }

    companion object{
        val PDF_TYPE = "pdf"
        val DOC_TYPE = "doc"
    }

    fun populateData(dataList: ArrayList<IndPCAInvoiceReportModelItem>)
    {
        if (dataList.isNotEmpty())
        {
            _InvoiceReportList = dataList
            adapter = IndPCAInvoiceReportAdapter(requireContext(),_InvoiceReportList!!,this@IndPCAInvoiceReportFragment)
            binding.rcViewIndPCAInvoiceReportFragment.adapter = adapter
            binding.rcViewIndPCAInvoiceReportFragment.invalidate()
        }
        else{
            binding.rcViewIndPCAInvoiceReportFragment.adapter = null
            binding.rcViewIndPCAInvoiceReportFragment.invalidate()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: ON_DESTROY_VIEW")
        _binding = null
    }

    override fun onReportItemClick(model: IndPCAInvoiceReportModelItem, fileType: String) {
        try {
            val invoiceNo = model.InvoiceNo
            var fileUrl = ""
            var desc = ""
            val fileNameStringBuilder = StringBuilder()
            if (fileType.equals(InvoiceReportFragment.PDF_TYPE))
            {
                fileUrl = URLHelper.IND_PCA_INVOICE_PDF.replace("<INVOICE_NO>",invoiceNo).replace("<PCA_REG_ID>",PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString())
                desc = "Downloading Invoice PDF"
                fileNameStringBuilder.clear()
                fileNameStringBuilder.append("PCA_Invoice_${model.InvoiceNo}_${model.Date}.pdf")
            }else if (fileType.equals(InvoiceReportFragment.DOC_TYPE))
            {
                fileUrl = URLHelper.IND_PCA_INVOICE_DOC.replace("<INVOICE_NO>",invoiceNo).replace("<PCA_REG_ID>",PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString())
                desc = "Downloading Invoice Doc"
                fileNameStringBuilder.clear()
                fileNameStringBuilder.append("PCA_Invoice_${model.InvoiceNo}_${model.Date}.doc")
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

interface IndPCAInvoiceReportHelper{
    fun onReportItemClick(model:IndPCAInvoiceReportModelItem,fileType:String)
}