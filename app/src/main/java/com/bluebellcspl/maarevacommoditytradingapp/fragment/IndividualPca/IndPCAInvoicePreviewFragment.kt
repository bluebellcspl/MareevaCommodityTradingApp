package com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca

import ConnectionCheck
import android.app.AlertDialog
import android.content.DialogInterface
import android.icu.text.NumberFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.IndPCAInvoicePreviewAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.AmountNumberToWords
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.EditableDecimalInputFilter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.FileDownloader
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.constants.URLHelper
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentIndPCAInvoicePreviewBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.InvoicePreviewCommissionPopupBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.InvoicePreviewFragment.FinalExpensePopupData
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchCityMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchIndBuyerName
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchIndPCAInvoicePreviewAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchIndPCATransportRateAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.POSTIndPCAInvoicePreviewInsertAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.FetchPerBoriRateIndPCADataModel
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCABuyerModel
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoiceBagAdjustmentModel
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoiceDataInsertModel
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoicePreviewModel
import com.bluebellcspl.maarevacommoditytradingapp.model.IndividualInvoiceDetailsModel
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.text.DecimalFormat
import java.util.regex.Pattern
import kotlin.math.floor

class IndPCAInvoicePreviewFragment : Fragment() {
    var _binding: FragmentIndPCAInvoicePreviewBinding? = null
    val binding get() = _binding!!
    val TAG = "IndPCAInvoicePreviewFragment"
    private val commonUIUtility: CommonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private lateinit var _InvoiceStockList : ArrayList<IndPCAInvoiceBagAdjustmentModel>
    private val args by navArgs<IndPCAInvoicePreviewFragmentArgs>()
    private var HSN_CODE = ""
    private lateinit var adapter: IndPCAInvoicePreviewAdapter
    private lateinit var _CityList : ArrayList<CityDetail>
    private lateinit var _CityNameList : ArrayList<String>
    private var CITY_ID = ""
    private var CITY_NAME = ""
    private var GST_STATUS: Boolean? = null
    private var CGST_AMOUNT: Double? = null
    private var SGST_AMOUNT: Double? = null
    private var BASIC_AMOUNT: Double? = null
    private var USED_BAGS: Double? = null
    private var TOTAL_GST: Double? = null
    private var VEHICLE_NO: String? = null
    private var _BuyerList:ArrayList<IndPCABuyerModel> = ArrayList()
    private lateinit var finalExpenses:FinalExpensePopupData
    private lateinit var invoiceDataFromAPI:IndPCAInvoicePreviewModel
    private val fileDownloader by lazy { FileDownloader.getInstance(requireContext()) }
    private val navController by lazy { findNavController() }
    private var isBuyerGSTINValid = false
    private var isBuyerPANValid = false
    private var isPcaGSTINValid = false
    private var isPcaPANValid = false
    private var SELECTED_BUYER_ID = ""
    private var SELECTED_BUYER_NAME = ""
    private var alertDialog: AlertDialog? = null
    private var isValidRTO = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_ind_p_c_a_invoice_preview, container, false)
        _InvoiceStockList = ArrayList(args.adjustBagList.toList())
        GST_STATUS = args.GSTSTATUS!!
        VEHICLE_NO = args.invoiceFetchDataModel.VehicleNo!!
        binding.rcViewIndPCAInvoicePreviewFragment.setHasFixedSize(true)
        binding.rcViewIndPCAInvoicePreviewFragment.setItemViewCacheSize(20)
        callAPI()

        //For Selecting Buyer City from DropDown
        _CityList = getCityfromDB()
        binding.actBuyerCityIndPCAInvoicePreviewFragment.setOnItemClickListener { adapterView, view, position, long ->
            var cityModel: CityDetail
            if (PrefUtil.getSystemLanguage().equals("gu")){
                cityModel = _CityList.find {it.CityName.equals(adapterView.getItemAtPosition(position).toString())}!!
            }
            else{
                cityModel = _CityList.find {it.CityName.equals(adapterView.getItemAtPosition(position).toString())}!!
            }

            Log.d(TAG, "onCreateView: SELECTED_CITY_NAME : ${cityModel.CityName}")
            Log.d(TAG, "onCreateView: SELECTED_CITY_ID : ${cityModel.CityId}")

            CITY_ID = cityModel.CityId
            CITY_NAME = cityModel.CityName
            invoiceDataFromAPI.BuyerCity = cityModel.CityId
            invoiceDataFromAPI.BuyerCityName = cityModel.CityName

            if (ConnectionCheck.isConnected(requireContext()))
            {
                val model = FetchPerBoriRateIndPCADataModel(
                    "All",
                    CITY_ID,
                    args.invoiceFetchDataModel.BuyerId,
                    args.invoiceFetchDataModel.CompanyCode,
                    PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString(),
                    ""+invoiceDataFromAPI.PCACityId,
                )

                FetchIndPCATransportRateAPI(requireContext(),this@IndPCAInvoicePreviewFragment,model)
            }else{
                commonUIUtility.showToast(requireContext().getString(R.string.no_internet_connection))
            }
        }

        //For Selecting Buyer from DropDown
        binding.actBuyerNameIndPCAInvoicePreviewFragment.setOnItemClickListener { adapterView, view, position, long ->
            val buyerModel = _BuyerList.find { it-> it.BuyerFullName.equals(adapterView.getItemAtPosition(position).toString()) }!!
            binding.actBuyerNameIndPCAInvoicePreviewFragment.setText(adapterView.getItemAtPosition(position).toString())
            Log.d(TAG, "onCreateView: SELECTED_BUYER_ID : ${buyerModel.InBuyerId}")
            Log.d(TAG, "onCreateView: SELECTED_BUYER_SHORT_NAME : ${buyerModel.BuyerShortName}")
            Log.d(TAG, "onCreateView: SELECTED_BUYER_ID : $SELECTED_BUYER_ID")
            Log.d(TAG, "onCreateView: SELECTED_BUYER_ID_IN_AFTER_TEXT_CHANGE : ${invoiceDataFromAPI.BuyerId}")
            setBuyerDataSelection(buyerModel)
        }

        binding.edtTotalAmountIndPCAInvoicePreviewFragment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s!!.isNotEmpty()) {
                    val amount = s.toString().replace(",", "")
                    val numberAmount = amount.toBigDecimal()
                    Log.d(TAG, "afterTextChanged: NUMBER_AMOUNT : $numberAmount")
                    val amountToWords = AmountNumberToWords.convert(numberAmount)
                    binding.tvAmountInWordsIndPCAInvoicePreviewFragment.setText(amountToWords)
                }
            }
        })
        binding.edtBuyerGSTINIndPCAInvoicePreviewFragment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s!!.isNotEmpty())
                {
                    if (isValidGSTIN(s!!.toString())) {
                        binding.edtBuyerGSTINContainerIndPCAInvoicePreviewFragment.boxStrokeColor = requireContext().getColor(R.color.newButtonColor)
                        isBuyerGSTINValid = true
                    } else {
                        binding.edtBuyerGSTINContainerIndPCAInvoicePreviewFragment.boxStrokeColor = requireContext().getColor(R.color.unReadChatBadge)
                        isBuyerGSTINValid = false
                    }
                }else
                {
                    isBuyerGSTINValid = false
                }
            }
        })
        binding.edtBuyerPANIndPCAInvoicePreviewFragment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s!!.isNotEmpty())
                {
                    if (isValidPAN(s!!.toString())) {
                        binding.edtBuyerPANContainerIndPCAInvoicePreviewFragment.boxStrokeColor = requireContext().getColor(R.color.newButtonColor)
                        isBuyerPANValid = true
                    } else {
                        binding.edtBuyerPANContainerIndPCAInvoicePreviewFragment.boxStrokeColor = requireContext().getColor(R.color.unReadChatBadge)
                        isBuyerPANValid = false
                    }
                }else
                {
                    isBuyerPANValid = false
                }
            }
        })
        binding.edtIndPCAGSTINIndPCAInvoicePreviewFragment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s!!.isNotEmpty())
                {
                    if (isValidGSTIN(s!!.toString())) {
                        binding.edtIndPCAGSTINContainerIndPCAInvoicePreviewFragment.boxStrokeColor = requireContext().getColor(R.color.newButtonColor)
                        isPcaGSTINValid = true
                    } else {
                        binding.edtIndPCAGSTINContainerIndPCAInvoicePreviewFragment.boxStrokeColor = requireContext().getColor(R.color.unReadChatBadge)
                        isPcaGSTINValid = false
                    }
                }else
                {
                    isPcaGSTINValid = false
                }
            }
        })
        binding.edtIndPCAPANIndPCAInvoicePreviewFragment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s!!.isNotEmpty())
                {
                    if (isValidPAN(s!!.toString())) {
                        binding.edtIndPCAPANContainerIndPCAInvoicePreviewFragment.boxStrokeColor = requireContext().getColor(R.color.newButtonColor)
                        isPcaPANValid = true
                    } else {
                        binding.edtIndPCAPANContainerIndPCAInvoicePreviewFragment.boxStrokeColor = requireContext().getColor(R.color.unReadChatBadge)
                        isPcaPANValid = false
                    }
                }else
                {
                    isPcaPANValid = false
                }
            }
        })

        binding.actBuyerCityIndPCAInvoicePreviewFragment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0!!.toString().isEmpty()){
                    CITY_ID = ""
                }
            }
        })

        binding.actBuyerCityIndPCAInvoicePreviewFragment.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.actBuyerCityIndPCAInvoicePreviewFragment.showDropDown()
            }
        }
        binding.actBuyerCityIndPCAInvoicePreviewFragment.setOnClickListener {
            binding.actBuyerCityIndPCAInvoicePreviewFragment.showDropDown()
        }

        binding.edtVehicleNoIndPCAInvoicePreviewFragment.addTextChangedListener(object :
            TextWatcher {
            private var isFormatting: Boolean = false
            private var beforeLength: Int = 0
            private var cursorPosition: Int = 0
            private var deleting: Boolean = false

            override fun afterTextChanged(s: Editable?) {
                Log.d(TAG, "afterTextChanged: =================================== START")
                Log.d(TAG, "afterTextChanged: BEFORE_LEN : $beforeLength")
                Log.d(TAG, "afterTextChanged: CURSOR_POS : $cursorPosition")
                if (isFormatting || s == null) return

                isFormatting = true

                // Remove all spaces
                val originalString = s.toString().replace(" ", "")
                val formattedString = StringBuilder()

                for (i in originalString.indices) {
                    formattedString.append(originalString[i])
                    // Add spaces after the respective positions
                    if (i == 1 || i == 3 || i == 5) {
                        formattedString.append(" ")
                    }
                }

                val newString = formattedString.toString().uppercase()

                // Calculate the new cursor position
                var newCursorPosition = cursorPosition
                if (deleting && cursorPosition > 0 && newCursorPosition > 0 && newString[cursorPosition - 1] == ' ') {
                    newCursorPosition--
                }

                binding.edtVehicleNoIndPCAInvoicePreviewFragment.setText(newString)
                if (newCursorPosition > newString.length) newCursorPosition = newString.length
                if (beforeLength == cursorPosition) newCursorPosition = newString.length
                if (newCursorPosition < 0) newCursorPosition = 0
                binding.edtVehicleNoIndPCAInvoicePreviewFragment.setSelection(newCursorPosition)

                Log.d(TAG, "afterTextChanged: =================================== END")
                Log.d(TAG, "afterTextChanged: BEFORE_LEN : $beforeLength")
                Log.d(TAG, "afterTextChanged: CURSOR_POS : $cursorPosition")
                Log.d(TAG, "afterTextChanged: NEW_STR : $newString")

                isFormatting = false

                // Validate the formatted RTO number
                if (isValidRtoNumber(s!!.toString())) {
                    binding.edtVehicleNoContainerIndPCAInvoicePreviewFragment.boxStrokeColor = requireContext().getColor(R.color.newButtonColor)
                    isValidRTO = true
                } else {
                    binding.edtVehicleNoContainerIndPCAInvoicePreviewFragment.boxStrokeColor = requireContext().getColor(R.color.unReadChatBadge)
                    isValidRTO = false
                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (isFormatting) return

                beforeLength = s?.length ?: 0
                cursorPosition = start
                deleting = count > after
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isFormatting) return

                cursorPosition = start + count
            }
        })
        setOnClickListener()
        return binding.root
    }

    private fun isValidRtoNumber(rtoNumber: String): Boolean {
        val rtoPattern = "^[A-Z]{2} [0-9]{2} [A-Z]{2} [0-9]{4}$"
        return Pattern.compile(rtoPattern).matcher(rtoNumber).matches()
    }

    private var buyerNameTextWatcher : TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun afterTextChanged(p0: Editable?) {
            if (p0!!.toString().isNotEmpty()){
                SELECTED_BUYER_NAME = p0!!.toString()
            }
        }
    }

    private var selfBuyerTextWatcher : TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//            invoiceDataFromAPI.BuyerId = ""
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun afterTextChanged(p0: Editable?) {
            if (p0!!.toString().isEmpty()){
                SELECTED_BUYER_ID = ""
                invoiceDataFromAPI.BuyerId = ""
                SELECTED_BUYER_NAME = p0!!.toString()
                resetBuyerData()
            }
        }
    }

    private fun isValidGSTIN(gstin: String): Boolean {
        val gstinRegex = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}[Z]{1}[0-9A-Z]{1}$")
        return gstinRegex.matches(gstin)
    }

    private fun isValidPAN(pan: String): Boolean {
        val regex = Regex("^[A-Z]{5}[0-9]{4}[A-Z]{1}\$")
        return pan.matches(regex)
    }

    private fun setOnClickListener() {
        try {
            binding.btnSubmitNDownloadIndPCAInvoicePreviewFragment.setOnClickListener {
                if (!isBuyerPANValid)
                {
                    commonUIUtility.showToast(getString(R.string.please_enter_valid_pan_alert_msg))
                    return@setOnClickListener
                }else if (!isBuyerGSTINValid){
                    commonUIUtility.showToast(getString(R.string.please_enter_valid_gstin_alert_msg))
                    return@setOnClickListener
                }else if (!isPcaGSTINValid){
                    commonUIUtility.showToast(getString(R.string.please_enter_valid_gstin_alert_msg))
                    return@setOnClickListener
                }else if (!isPcaPANValid){
                    commonUIUtility.showToast(getString(R.string.please_enter_valid_pan_alert_msg))
                    return@setOnClickListener
                }else if (binding.actBuyerNameIndPCAInvoicePreviewFragment.text.toString().isEmpty())
                {
                    commonUIUtility.showToast(getString(R.string.please_enter_buyer_name_alert_msg))
                    return@setOnClickListener
                }
                else if(binding.edtVehicleNoIndPCAInvoicePreviewFragment.text.toString().isNotEmpty() && !isValidRTO){
                    commonUIUtility.showToast(getString(R.string.please_enter_valid_rto_number_alert_msg))
                    return@setOnClickListener
                }
                else if (binding.edtBuyerAddressIndPCAInvoicePreviewFragment.text.toString().isEmpty())
                {
                    commonUIUtility.showToast(getString(R.string.enter_buyer_address_alert_msg))
                    return@setOnClickListener
                }else if (binding.actBuyerCityIndPCAInvoicePreviewFragment.text.toString().isEmpty())
                {
                    commonUIUtility.showToast(getString(R.string.please_enter_buyer_city_alert_msg))
                    return@setOnClickListener
                }else if (_CityList.find { it.CityName.equals(binding.actBuyerCityIndPCAInvoicePreviewFragment.text.toString().trim()) && it.CityId.toInt() == CITY_ID.toInt() }==null)
                {
                    commonUIUtility.showToast(requireContext().getString(R.string.select_city))
                    return@setOnClickListener
                }else
                {
                    showAlertDialog()
                }
            }
            binding.cvTotalCalculationIndPCAInvoicePreviewFragment.setOnClickListener {
                if (binding.actBuyerNameIndPCAInvoicePreviewFragment.text.toString().isEmpty() && invoiceDataFromAPI.BuyerId.equals("0")){
                    commonUIUtility.showToast(getString(R.string.select_or_enter_buyer))
                }else if (invoiceDataFromAPI.BuyerCity.equals("0") || binding.actBuyerCityIndPCAInvoicePreviewFragment.text.toString().isEmpty()){
                    commonUIUtility.showToast(requireContext().getString(R.string.select_city))
                }else
                {
                    showExpensePopup()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "setOnClickListener: ${e.message}")
        }
    }

    private fun callAPI() {
        try {
            if (ConnectionCheck.isConnected(requireContext())){
                FetchIndPCAInvoicePreviewAPI(requireContext(),this@IndPCAInvoicePreviewFragment,args.invoiceFetchDataModel)
            }else
            {
                commonUIUtility.showToast(requireContext().getString(R.string.no_internet_connection))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "callAPI: ${e.message}", )
        }
    }

    fun setInvoicePreviewData(responseJO: IndPCAInvoicePreviewModel) {
        try {
            invoiceDataFromAPI = responseJO
            if (responseJO.PCAPanNo.isEmpty()){
                binding.edtIndPCAPANIndPCAInvoicePreviewFragment.isEnabled = true
            }else{
                binding.edtIndPCAPANIndPCAInvoicePreviewFragment.isEnabled = false
            }
            if (responseJO.PCAGSTNo.isEmpty()){
                binding.edtIndPCAGSTINIndPCAInvoicePreviewFragment.isEnabled = true
            }else{
                binding.edtIndPCAGSTINIndPCAInvoicePreviewFragment.isEnabled = false
            }
            binding.tvPCATitleIndPCAInvoicePreviewFragment.setText(responseJO.PCAName)
            binding.edtIndPCAPANIndPCAInvoicePreviewFragment.setText(responseJO.PCAPanNo)
            binding.edtIndPCAGSTINIndPCAInvoicePreviewFragment.setText(responseJO.PCAGSTNo)
            binding.tvStateIndPCAInvoicePreviewFragment.text = responseJO.StateName
            binding.tvMobileNoIndPCAInvoicePreviewFragment.text = responseJO.PCAMobileNo

            binding.edtInvoiceDateIndPCAInvoicePreviewFragment.setText(responseJO.InvoiceDate)
            if (responseJO.BuyerFullName.isEmpty())
            {
                binding.actBuyerNameIndPCAInvoicePreviewFragment.setText(responseJO.BuyerShortName)
            }else{
                binding.actBuyerNameIndPCAInvoicePreviewFragment.setText(responseJO.BuyerFullName)
            }
            binding.edtBuyerAddressIndPCAInvoicePreviewFragment.setText(responseJO.BuyerAddress)
            binding.actBuyerCityIndPCAInvoicePreviewFragment.setText(responseJO.BuyerCityName)
            binding.edtBuyerGSTINIndPCAInvoicePreviewFragment.setText(responseJO.BuyerGSTNo)
            binding.edtBuyerPANIndPCAInvoicePreviewFragment.setText(responseJO.BuyerPanNo)
            binding.edtVehicleNoIndPCAInvoicePreviewFragment.setText(args.invoiceFetchDataModel.VehicleNo)

            HSN_CODE = responseJO.HSNASC
            bindAdjustInvoiceBag(_InvoiceStockList)

            SELECTED_BUYER_ID = responseJO.BuyerId
            SELECTED_BUYER_NAME = responseJO.BuyerFullName
            CITY_ID = responseJO.BuyerCity

            //For Call Transport Rate Directly After InvoiceData API if BuyerCItyId is Available
            if (responseJO.BuyerCity.isNotEmpty()){
                if (responseJO.BuyerCity.toInt()>0){

                    val model = FetchPerBoriRateIndPCADataModel(
                        "All",
                        CITY_ID,
                        args.invoiceFetchDataModel.BuyerId,
                        args.invoiceFetchDataModel.CompanyCode,
                        PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString(),
                        ""+invoiceDataFromAPI.PCACityId,
                    )

                    FetchIndPCATransportRateAPI(requireContext(),this@IndPCAInvoicePreviewFragment,model)
                }
            }

            if (responseJO.BuyerId.isNotEmpty()){
                if (responseJO.BuyerId.toInt()==0){
                    FetchIndBuyerName(requireContext(),this@IndPCAInvoicePreviewFragment)
                }else{
                    binding.actBuyerNameIndPCAInvoicePreviewFragment.setAdapter(null)
                }
            }

            //Setting TextWatcher for different Buyer
            if (responseJO.BuyerId.toInt()>0){
                binding.actBuyerNameIndPCAInvoicePreviewFragment.addTextChangedListener(buyerNameTextWatcher)
            }else if (responseJO.BuyerId.toInt()==0){
                responseJO.BuyerId = ""
                invoiceDataFromAPI.BuyerId = ""
                binding.actBuyerNameIndPCAInvoicePreviewFragment.addTextChangedListener(selfBuyerTextWatcher)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "setInvoicePreviewData: ${e.message}")
        }
    }

    private fun bindAdjustInvoiceBag(dataList:ArrayList<IndPCAInvoiceBagAdjustmentModel>){
        try {
            dataList.forEach {
                it.HSNCode = HSN_CODE
            }
            adapter = IndPCAInvoicePreviewAdapter(requireContext(),dataList)
            binding.rcViewIndPCAInvoicePreviewFragment.adapter = adapter
            binding.rcViewIndPCAInvoicePreviewFragment.invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindAdjustInvoiceBag: ${e.message}")
        }
    }

    fun getCityfromDB():ArrayList<CityDetail>{
        val localArrayList = ArrayList<CityDetail>()
        val cursor = DatabaseManager.ExecuteRawSql(Query.getCityData())
        try {
            if (cursor!=null && cursor.count>0)
            {
                while (cursor.moveToNext())
                {

                    val model = CityDetail(
                        cursor.getString(cursor.getColumnIndexOrThrow("CityName")),
                        cursor.getString(cursor.getColumnIndexOrThrow("CityId"))
                    )
                    localArrayList.add(model)
                }
            }
            var cityAdapter : ArrayAdapter<String>
            if(PrefUtil.getSystemLanguage().equals("gu")){
                _CityNameList = ArrayList(localArrayList.map { it.CityName })
                cityAdapter = commonUIUtility.getCustomArrayAdapter(_CityNameList)
            }else
            {
                _CityNameList = ArrayList(localArrayList.map { it.CityName })
                cityAdapter = commonUIUtility.getCustomArrayAdapter(_CityNameList)
            }

            binding.actBuyerCityIndPCAInvoicePreviewFragment.setAdapter(cityAdapter)
            localArrayList.forEach {
                Log.d(TAG, "getCityfromDB: ID - APMC : ${it.CityId} - ${it.CityName}")
            }

            cursor?.close()
        }catch (e:Exception)
        {
            cursor?.close()
            _CityNameList = ArrayList()
            localArrayList.clear()
            e.printStackTrace()
            Log.e(TAG, "getCityfromDB: ${e.message}")
        }
        return localArrayList
    }

    fun getBuyerFromAPI(dataList:ArrayList<IndPCABuyerModel>){
        _BuyerList.clear()
        _BuyerList = dataList
        val buyerAdapter = commonUIUtility.getCustomArrayAdapter(_BuyerList.map { it.BuyerFullName } as ArrayList<String>)
        binding.actBuyerNameIndPCAInvoicePreviewFragment.setAdapter(buyerAdapter)
    }

    private fun setBuyerDataSelection(buyerModel:IndPCABuyerModel){
        Log.d(TAG, "setBuyerDataSelection: SELECTED_BUYER_ID : ${buyerModel.InBuyerId}")
        Log.d(TAG, "setBuyerDataSelection: SELECTED_BUYER_NAME : ${buyerModel.BuyerFullName}")
        Log.d(TAG, "setBuyerDataSelection: SELECTED_CITY_ID : ${buyerModel.BuyerCity}")
        Log.d(TAG, "setBuyerDataSelection: SELECTED_CITY_NAME : ${buyerModel.BuyerCityName}")
        invoiceDataFromAPI.BuyerId = buyerModel.InBuyerId
        SELECTED_BUYER_ID = buyerModel.InBuyerId
        SELECTED_BUYER_NAME = buyerModel.BuyerFullName
        CITY_ID = buyerModel.BuyerCity
        CITY_NAME = buyerModel.BuyerCityName

        invoiceDataFromAPI.BuyerCity = buyerModel.BuyerCity
        invoiceDataFromAPI.BuyerCityName = buyerModel.BuyerCityName
        invoiceDataFromAPI.BuyerFullName = buyerModel.BuyerFullName
        invoiceDataFromAPI.BuyerAddress = buyerModel.BuyerAddress
        invoiceDataFromAPI.BuyerGSTNo = buyerModel.BuyerGSTIn
        invoiceDataFromAPI.BuyerPanNo = buyerModel.BuyerPanNo

        binding.actBuyerNameIndPCAInvoicePreviewFragment.setText(buyerModel.BuyerFullName)
        binding.actBuyerCityIndPCAInvoicePreviewFragment.setText(buyerModel.BuyerCityName)
        binding.edtBuyerAddressIndPCAInvoicePreviewFragment.setText(buyerModel.BuyerAddress)
        binding.edtBuyerGSTINIndPCAInvoicePreviewFragment.setText(buyerModel.BuyerGSTIn)
        binding.edtBuyerPANIndPCAInvoicePreviewFragment.setText(buyerModel.BuyerPanNo)

        val model = FetchPerBoriRateIndPCADataModel(
            "All",
            buyerModel.BuyerCity,
            buyerModel.InBuyerId,
            args.invoiceFetchDataModel.CompanyCode,
            PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString(),
            ""+invoiceDataFromAPI.PCACityId,
        )

        Log.d(TAG, "setBuyerDataSelection: MODEL_FOR_TRANSPORT_RATE : $model")
        FetchIndPCATransportRateAPI(requireContext(),this@IndPCAInvoicePreviewFragment,model)
    }

    fun updatePerBoriRate(perBoriRate:String){
        invoiceDataFromAPI.PerBoriRate = perBoriRate
        Log.d(TAG, "updatePerBoriRate: UPDATED_PER_BORI_RATE : ${invoiceDataFromAPI.PerBoriRate}")

        //Commission Data
        binding.edtGCACommIndPCAInvoicePreviewFragment.setText("0")
        binding.edtPCACommIndPCAInvoicePreviewFragment.setText(invoiceDataFromAPI.PCApcaCommissiom)
        binding.edtMarketFeesIndPCAInvoicePreviewFragment.setText(invoiceDataFromAPI.MarketCess)
        binding.edtLabourIndPCAInvoicePreviewFragment.setText(invoiceDataFromAPI.LabourCharges)
        binding.edtTransportIndPCAInvoicePreviewFragment.setText(invoiceDataFromAPI.PerBoriRate)
        var PCAComm = invoiceDataFromAPI.PCApcaCommissiom.toDouble()
//        var GCAComm = invoiceDataFromAPI.PCAgcaCommissiom.toDouble()
        var GCAComm = 0.0
        var MarketCess = invoiceDataFromAPI.MarketCess.toDouble()
        var Transport = invoiceDataFromAPI.PerBoriRate.toDouble()
        var Labour = invoiceDataFromAPI.LabourCharges.toDouble()

        //GST Data
        if (GST_STATUS!!) {
            TOTAL_GST = invoiceDataFromAPI.GSTTotalPct.toDouble()
            CGST_AMOUNT = TOTAL_GST!! / 2.0
            SGST_AMOUNT = TOTAL_GST!! / 2.0
        } else {
            TOTAL_GST = 0.0
            CGST_AMOUNT = 0.0
            SGST_AMOUNT = 0.0

        }

        var basicAmount = 0.0
        var usedBags = 0.0
        for (model in _InvoiceStockList!!) {
            basicAmount += model.BillAmount.toDouble()
            usedBags += model.BillBags.toDouble()
        }
        BASIC_AMOUNT = basicAmount
        USED_BAGS = usedBags
        val formattedAmount =
            NumberFormat.getCurrencyInstance().format(basicAmount).substring(1)
        binding.edtBasicAmountIndPCAInvoicePreviewFragment.setText(formattedAmount)
        val PCACommission = (basicAmount * PCAComm) / 100.00
        val GCACommission = (basicAmount * GCAComm) / 100.00
        val MarketFees = (basicAmount * MarketCess) / 100.00
        val TransportFees = usedBags * Transport
        val LabourFees = usedBags * Labour

        binding.edtBagsIndPCAInvoicePreviewFragment.setText(commonUIUtility.numberCurrencyFormat(usedBags))
        var totalAmount =
            basicAmount + PCACommission + GCACommission + MarketFees + TransportFees + LabourFees
        var GSTAmount = (totalAmount * TOTAL_GST!!) / 100.00
        var cgstAmt = DecimalFormat("0.00").format(GSTAmount / 2.0)
        var sgstAmt = DecimalFormat("0.00").format(GSTAmount / 2.0)
        var totalAmountWithGST = totalAmount + GSTAmount


        binding.edtGSTIndPCAInvoicePreviewFragment.setText(commonUIUtility.numberCurrencyFormat(GSTAmount))

        binding.edtTotalAmountIndPCAInvoicePreviewFragment.setText(commonUIUtility.numberCurrencyFormat(totalAmountWithGST))
        finalExpenses = FinalExpensePopupData(
            commonUIUtility.formatDecimal(GCACommission),
            "0.0",
            commonUIUtility.formatDecimal(PCACommission),
            invoiceDataFromAPI.PCApcaCommissiom,
            commonUIUtility.formatDecimal(MarketFees),
            invoiceDataFromAPI.MarketCess,
            commonUIUtility.formatDecimal(LabourFees),
            invoiceDataFromAPI.LabourCharges,
            commonUIUtility.formatDecimal(TransportFees),
            invoiceDataFromAPI.PerBoriRate,
            commonUIUtility.formatDecimal(GSTAmount),
            invoiceDataFromAPI.GSTTotalPct,
            cgstAmt.toString(),
            CGST_AMOUNT!!.toString(),
            sgstAmt.toString(),
            SGST_AMOUNT!!.toString(),
            commonUIUtility.formatDecimal(totalAmountWithGST)
        )
    }

    private fun showExpensePopup() {
        try {
            if (alertDialog==null || !alertDialog!!.isShowing){

                val alertDailogBuilder = AlertDialog.Builder(requireContext())
                val dialogBinding = InvoicePreviewCommissionPopupBinding.inflate(layoutInflater)
                val dialogView = dialogBinding.root
                alertDailogBuilder.setView(dialogView)
                alertDialog = alertDailogBuilder.create()
                alertDialog!!.setCanceledOnTouchOutside(true)
                alertDialog!!.setCancelable(true)
                alertDialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
                alertDialog!!.show()

                dialogBinding.llGCACOMMInvoicePreviewPopup.visibility = View.GONE
                //Populating Data In Popup
                dialogBinding.edtGCACommInvoicePreviewPopup.filters =
                    arrayOf<InputFilter>(EditableDecimalInputFilter(3, 2))
                dialogBinding.edtPCACommInvoicePreviewPopup.filters =
                    arrayOf<InputFilter>(EditableDecimalInputFilter(3, 2))
                dialogBinding.edtMarketFeesInvoicePreviewPopup.filters =
                    arrayOf<InputFilter>(EditableDecimalInputFilter(3, 2))
                dialogBinding.edtLabourInvoicePreviewPopup.filters =
                    arrayOf<InputFilter>(EditableDecimalInputFilter(5, 2))
                dialogBinding.edtTransportInvoicePreviewPopup.filters =
                    arrayOf<InputFilter>(EditableDecimalInputFilter(5, 2))

                var pcaCommAmount = 0.0
                var gcaCommAmount = 0.0
                var marketFeesAmount = 0.0
                var labourAmount = 0.0
                var transportAmount = 0.0

                dialogBinding.edtGCACommInvoicePreviewPopup.addTextChangedListener(object :
                    TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    }

                    override fun afterTextChanged(s: Editable?) {

                        if (dialogBinding.edtGCACommInvoicePreviewPopup.text.toString().isEmpty()) {
                            dialogBinding.edtGCACommInvoicePreviewPopup.setText("0")
                            dialogBinding.edtGCACommInvoicePreviewPopup.setSelection(1)
                            gcaCommAmount = 0.0
                            dialogBinding.tvGCACommAmountInvoicePreviewPopup.setText("0")
                        }
                        if (dialogBinding.edtGCACommInvoicePreviewPopup.text.toString().length >= 2 && dialogBinding.edtGCACommInvoicePreviewPopup.text.toString()
                                .startsWith("0") && !dialogBinding.edtGCACommInvoicePreviewPopup.text.toString()
                                .contains("0.")
                        ) {

                            val subStr =
                                dialogBinding.edtGCACommInvoicePreviewPopup.text.toString().substring(1)
                            dialogBinding.edtGCACommInvoicePreviewPopup.setText(subStr)
                            dialogBinding.edtGCACommInvoicePreviewPopup.setSelection(subStr.length)

                        } else {
                            var percentage =
                                dialogBinding.edtGCACommInvoicePreviewPopup.text.toString().toDouble()
                            Log.d(TAG, "afterTextChanged: EDT_PERCENT : $percentage")
                            if (percentage > 100) {
                                dialogBinding.edtGCACommInvoicePreviewPopup.setText("100")
                                dialogBinding.edtGCACommInvoicePreviewPopup.setSelection(s.toString().length)
                                percentage = 100.00
                            }
                            gcaCommAmount = BASIC_AMOUNT!! * percentage / 100.00
                            val formattedAmount =
                                NumberFormat.getCurrencyInstance().format(gcaCommAmount).substring(1)
                            dialogBinding.tvGCACommAmountInvoicePreviewPopup.setText(formattedAmount)

                            var totalAmount =
                                BASIC_AMOUNT!! + pcaCommAmount + gcaCommAmount + marketFeesAmount + labourAmount + transportAmount
                            var GSTAmount = (totalAmount * TOTAL_GST!!) / 100.00
                            var totalAmountWithGST = totalAmount + GSTAmount

                            val formattedGSTAmount =
                                NumberFormat.getCurrencyInstance().format(GSTAmount).substring(1)
                            dialogBinding.edtGSTAmountInvoicePreviewPopup.setText(formattedGSTAmount)

                            val formattedTotalAmount =
                                NumberFormat.getCurrencyInstance().format(totalAmountWithGST)
                                    .substring(1)
                            dialogBinding.edtTotalAmountInvoicePreviewPopup.setText(formattedTotalAmount)
                        }

                    }
                })
                dialogBinding.edtPCACommInvoicePreviewPopup.addTextChangedListener(object :
                    TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    }

                    override fun afterTextChanged(s: Editable?) {
                        if (dialogBinding.edtPCACommInvoicePreviewPopup.text.toString().isEmpty()) {
                            dialogBinding.edtPCACommInvoicePreviewPopup.setText("0")
                            dialogBinding.edtPCACommInvoicePreviewPopup.setSelection(1)
                            pcaCommAmount = 0.0
                            dialogBinding.tvPCACommAmountInvoicePreviewPopup.setText("0")
                        }
                        if (dialogBinding.edtPCACommInvoicePreviewPopup.text.toString().length >= 2 && dialogBinding.edtPCACommInvoicePreviewPopup.text.toString()
                                .startsWith("0") && !dialogBinding.edtPCACommInvoicePreviewPopup.text.toString()
                                .contains("0.")
                        ) {
                            val subStr =
                                dialogBinding.edtPCACommInvoicePreviewPopup.text.toString().substring(1)
                            dialogBinding.edtPCACommInvoicePreviewPopup.setText(subStr)
                            dialogBinding.edtPCACommInvoicePreviewPopup.setSelection(subStr.length)
                        } else {
                            var percentage =
                                dialogBinding.edtPCACommInvoicePreviewPopup.text.toString().toDouble()
                            if (percentage > 100) {
                                dialogBinding.edtPCACommInvoicePreviewPopup.setText("100")
                                dialogBinding.edtPCACommInvoicePreviewPopup.setSelection(s.toString().length)
                                percentage = 100.00
                            }
                            pcaCommAmount = BASIC_AMOUNT!! * percentage / 100.00
                            val formattedAmount =
                                NumberFormat.getCurrencyInstance().format(pcaCommAmount).substring(1)
                            dialogBinding.tvPCACommAmountInvoicePreviewPopup.setText(formattedAmount)

                            var totalAmount =
                                BASIC_AMOUNT!! + pcaCommAmount + gcaCommAmount + marketFeesAmount + labourAmount + transportAmount
                            var GSTAmount = (totalAmount * TOTAL_GST!!) / 100.00
                            var totalAmountWithGST = totalAmount + GSTAmount

                            val formattedGSTAmount =
                                NumberFormat.getCurrencyInstance().format(GSTAmount).substring(1)
                            dialogBinding.edtGSTAmountInvoicePreviewPopup.setText(formattedGSTAmount)

                            val formattedTotalAmount =
                                NumberFormat.getCurrencyInstance().format(totalAmountWithGST)
                                    .substring(1)
                            dialogBinding.edtTotalAmountInvoicePreviewPopup.setText(formattedTotalAmount)
                        }

                    }
                })
                dialogBinding.edtMarketFeesInvoicePreviewPopup.addTextChangedListener(object :
                    TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    }

                    override fun afterTextChanged(s: Editable?) {
                        if (dialogBinding.edtMarketFeesInvoicePreviewPopup.text.toString().isEmpty()) {
                            dialogBinding.edtMarketFeesInvoicePreviewPopup.setText("0")
                            dialogBinding.edtMarketFeesInvoicePreviewPopup.setSelection(1)
                            marketFeesAmount = 0.0
                            dialogBinding.tvMarketFeesAmountInvoicePreviewPopup.setText("0")
                        }
                        if (dialogBinding.edtMarketFeesInvoicePreviewPopup.text.toString().length >= 2 && dialogBinding.edtMarketFeesInvoicePreviewPopup.text.toString()
                                .startsWith("0") && !dialogBinding.edtMarketFeesInvoicePreviewPopup.text.toString()
                                .contains("0.")
                        ) {
                            val subStr =
                                dialogBinding.edtMarketFeesInvoicePreviewPopup.text.toString()
                                    .substring(1)
                            dialogBinding.edtMarketFeesInvoicePreviewPopup.setText(subStr)
                            dialogBinding.edtMarketFeesInvoicePreviewPopup.setSelection(1)
                        } else {
                            var percentage =
                                dialogBinding.edtMarketFeesInvoicePreviewPopup.text.toString()
                                    .toDouble()
                            if (percentage > 100) {

                                dialogBinding.edtMarketFeesInvoicePreviewPopup.setText("100")
                                dialogBinding.edtMarketFeesInvoicePreviewPopup.setSelection(s.toString().length)
                                percentage = 100.00
                            }
                            marketFeesAmount = BASIC_AMOUNT!! * percentage / 100.00
                            val formattedAmount =
                                NumberFormat.getCurrencyInstance().format(marketFeesAmount).substring(1)
                            dialogBinding.tvMarketFeesAmountInvoicePreviewPopup.setText(formattedAmount)

                            var totalAmount =
                                BASIC_AMOUNT!! + pcaCommAmount + gcaCommAmount + marketFeesAmount + labourAmount + transportAmount
                            var GSTAmount = (totalAmount * TOTAL_GST!!) / 100.00
                            var totalAmountWithGST = totalAmount + GSTAmount

                            val formattedGSTAmount =
                                NumberFormat.getCurrencyInstance().format(GSTAmount).substring(1)
                            dialogBinding.edtGSTAmountInvoicePreviewPopup.setText(formattedGSTAmount)

                            val formattedTotalAmount =
                                NumberFormat.getCurrencyInstance().format(totalAmountWithGST)
                                    .substring(1)
                            dialogBinding.edtTotalAmountInvoicePreviewPopup.setText(formattedTotalAmount)
                        }

                    }
                })
                dialogBinding.edtLabourInvoicePreviewPopup.addTextChangedListener(object :
                    TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    }

                    override fun afterTextChanged(s: Editable?) {
                        if (s.toString().isNotEmpty()) {


                        } else {
                            s!!.append("0")

                        }
                        if (dialogBinding.edtLabourInvoicePreviewPopup.text.toString().isEmpty()) {
                            dialogBinding.edtLabourInvoicePreviewPopup.setText("0")
                            dialogBinding.edtLabourInvoicePreviewPopup.setSelection(1)
                            labourAmount = 0.0
                            dialogBinding.tvLabourAmountInvoicePreviewPopup.setText("0")
                        }
                        if (dialogBinding.edtLabourInvoicePreviewPopup.text.toString().length >= 2 && dialogBinding.edtLabourInvoicePreviewPopup.text.toString()
                                .startsWith("0") && !dialogBinding.edtLabourInvoicePreviewPopup.text.toString()
                                .contains("0.")
                        ) {
                            val subStr =
                                dialogBinding.edtLabourInvoicePreviewPopup.text.toString().substring(1)
                            dialogBinding.edtLabourInvoicePreviewPopup.setText(subStr)
                            dialogBinding.edtLabourInvoicePreviewPopup.setSelection(1)
                        } else {
                            var percentage =
                                dialogBinding.edtLabourInvoicePreviewPopup.text.toString().toDouble()
                            labourAmount = USED_BAGS!! * percentage
                            val formattedAmount =
                                NumberFormat.getCurrencyInstance().format(labourAmount).substring(1)
                            dialogBinding.tvLabourAmountInvoicePreviewPopup.setText(formattedAmount)

                            var totalAmount =
                                BASIC_AMOUNT!! + pcaCommAmount + gcaCommAmount + marketFeesAmount + labourAmount + transportAmount
                            var GSTAmount = (totalAmount * TOTAL_GST!!) / 100.00
                            var totalAmountWithGST = totalAmount + GSTAmount

                            val formattedGSTAmount =
                                NumberFormat.getCurrencyInstance().format(GSTAmount).substring(1)
                            dialogBinding.edtGSTAmountInvoicePreviewPopup.setText(formattedGSTAmount)

                            val formattedTotalAmount =
                                NumberFormat.getCurrencyInstance().format(totalAmountWithGST)
                                    .substring(1)
                            dialogBinding.edtTotalAmountInvoicePreviewPopup.setText(formattedTotalAmount)
                        }

                    }
                })
                dialogBinding.edtTransportInvoicePreviewPopup.addTextChangedListener(object :
                    TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    }

                    override fun afterTextChanged(s: Editable?) {
                        if (dialogBinding.edtTransportInvoicePreviewPopup.text.toString().isEmpty()) {
                            dialogBinding.edtTransportInvoicePreviewPopup.setText("0")
                            dialogBinding.edtTransportInvoicePreviewPopup.setSelection(1)
                            transportAmount = 0.0
                            dialogBinding.tvTransportAmountInvoicePreviewPopup.setText("0")
                        }
                        if (dialogBinding.edtTransportInvoicePreviewPopup.text.toString().length >= 2 && dialogBinding.edtTransportInvoicePreviewPopup.text.toString()
                                .startsWith("0") && !dialogBinding.edtTransportInvoicePreviewPopup.text.toString()
                                .contains("0.")
                        ) {
                            val subStr =
                                dialogBinding.edtTransportInvoicePreviewPopup.text.toString()
                                    .substring(1)
                            dialogBinding.edtTransportInvoicePreviewPopup.setText(subStr)
                            dialogBinding.edtTransportInvoicePreviewPopup.setSelection(1)
                        } else {
                            var percentage =
                                dialogBinding.edtTransportInvoicePreviewPopup.text.toString().toDouble()
                            transportAmount = USED_BAGS!! * percentage
                            val formattedAmount =
                                NumberFormat.getCurrencyInstance().format(transportAmount).substring(1)
                            dialogBinding.tvTransportAmountInvoicePreviewPopup.setText(formattedAmount)

                            var totalAmount =
                                BASIC_AMOUNT!! + pcaCommAmount + gcaCommAmount + marketFeesAmount + labourAmount + transportAmount
                            var GSTAmount = (totalAmount * TOTAL_GST!!) / 100.00
                            var totalAmountWithGST = totalAmount + GSTAmount

                            val formattedGSTAmount =
                                NumberFormat.getCurrencyInstance().format(GSTAmount).substring(1)
                            dialogBinding.edtGSTAmountInvoicePreviewPopup.setText(formattedGSTAmount)

                            val formattedTotalAmount =
                                NumberFormat.getCurrencyInstance().format(totalAmountWithGST)
                                    .substring(1)
                            dialogBinding.edtTotalAmountInvoicePreviewPopup.setText(formattedTotalAmount)
                        }
                    }
                })

//                dialogBinding.edtGCACommInvoicePreviewPopup.setText(invoiceDataFromAPI!!.PCAgcaCommissiom)
                dialogBinding.edtGCACommInvoicePreviewPopup.setText("0")
                dialogBinding.edtPCACommInvoicePreviewPopup.setText(invoiceDataFromAPI!!.PCApcaCommissiom)
                dialogBinding.edtMarketFeesInvoicePreviewPopup.setText(invoiceDataFromAPI!!.MarketCess)
                dialogBinding.edtLabourInvoicePreviewPopup.setText(invoiceDataFromAPI!!.LabourCharges)
                dialogBinding.edtTransportInvoicePreviewPopup.setText(invoiceDataFromAPI!!.PerBoriRate)

                var gstPCString =
                    StringBuilder(dialogBinding.tvGSTTotalPercentageInvoicePreviewPopup.text.toString())
                gstPCString.append("$TOTAL_GST%")
                dialogBinding.tvGSTTotalPercentageInvoicePreviewPopup.setText(gstPCString.toString())

                val formattedAmount =
                    NumberFormat.getCurrencyInstance().format(BASIC_AMOUNT).substring(1)
                dialogBinding.edtBasicAmountInvoicePreviewPopup.setText(formattedAmount)

                dialogBinding.btnSaveInvoicePreviewPopup.setOnClickListener {
                    binding.edtGCACommIndPCAInvoicePreviewFragment.setText(dialogBinding.edtGCACommInvoicePreviewPopup.text.toString())
                    binding.edtPCACommIndPCAInvoicePreviewFragment.setText(dialogBinding.edtPCACommInvoicePreviewPopup.text.toString())
                    binding.edtMarketFeesIndPCAInvoicePreviewFragment.setText(dialogBinding.edtMarketFeesInvoicePreviewPopup.text.toString())
                    binding.edtLabourIndPCAInvoicePreviewFragment.setText(dialogBinding.edtLabourInvoicePreviewPopup.text.toString())
                    binding.edtTransportIndPCAInvoicePreviewFragment.setText(dialogBinding.edtTransportInvoicePreviewPopup.text.toString())
                    binding.edtGSTIndPCAInvoicePreviewFragment.setText(dialogBinding.edtGSTAmountInvoicePreviewPopup.text.toString())
                    binding.edtTotalAmountIndPCAInvoicePreviewFragment.setText(dialogBinding.edtTotalAmountInvoicePreviewPopup.text.toString())
                    val totalGSTAmount = dialogBinding.edtGSTAmountInvoicePreviewPopup.text.toString().replace(",", "").toDouble()
                    val CGSTAmount = totalGSTAmount/2.0
                    val SGSTAmount = totalGSTAmount/2.0
                    val CGST_PCT = TOTAL_GST!!/2.0
                    val SGST_PCT = TOTAL_GST!!/2.0
                    finalExpenses = FinalExpensePopupData(
                        ""+dialogBinding.tvGCACommAmountInvoicePreviewPopup.text.toString().replace(",", "").trim(),
                        ""+dialogBinding.edtGCACommInvoicePreviewPopup.text.toString(),
                        ""+dialogBinding.tvPCACommAmountInvoicePreviewPopup.text.toString().replace(",", "").trim(),
                        ""+dialogBinding.edtPCACommInvoicePreviewPopup.text.toString(),
                        ""+dialogBinding.tvMarketFeesAmountInvoicePreviewPopup.text.toString().replace(",", "").trim(),
                        ""+dialogBinding.edtMarketFeesInvoicePreviewPopup.text.toString(),
                        ""+dialogBinding.tvLabourAmountInvoicePreviewPopup.text.toString().replace(",", "").trim(),
                        ""+dialogBinding.edtLabourInvoicePreviewPopup.text.toString(),
                        ""+dialogBinding.tvTransportAmountInvoicePreviewPopup.text.toString().replace(",", "").trim(),
                        ""+dialogBinding.edtTransportInvoicePreviewPopup.text.toString(),
                        ""+dialogBinding.edtGSTAmountInvoicePreviewPopup.text.toString().replace(",", "").trim(),
                        ""+dialogBinding.tvGSTTotalPercentageInvoicePreviewPopup.text.toString().replace("%","").trim(),
                        ""+CGSTAmount.toString(),
                        ""+CGST_PCT.toString(),
                        ""+SGSTAmount.toString(),
                        ""+SGST_PCT.toString(),
                        ""+dialogBinding.edtTotalAmountInvoicePreviewPopup.text.toString().replace(",", "")
                    )
                    alertDialog!!.dismiss()
                }

            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "showExpensePopup: ${e.message}")
        }
    }

    private fun showAlertDialog() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle("Alert")
        alertDialog.setMessage(getString(R.string.do_you_want_to_save_invoice_alert_lbl))
        alertDialog.setPositiveButton(
            requireContext().getString(R.string.yes),
            object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
//                postAuctionData()
                    sendInvoiceData()
                }
            })
        alertDialog.setNegativeButton(
            requireContext().getString(R.string.no),
            object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    p0!!.dismiss()
                }
            })
        alertDialog.show()
    }

    private fun sendInvoiceData() {
        try {
            val invoiceBagList = ArrayList<IndividualInvoiceDetailsModel>()
            _InvoiceStockList.forEach { invoiceBagModel->
                val currentQTL = DecimalFormat("0.00").format(floor(invoiceBagModel.BillWeight.toDouble() / 100.0))
                val currentKG = invoiceBagModel.BillWeight.toDouble() - floor(invoiceBagModel.BillWeight.toDouble()/100.0) *100.0
                val invoiceKG = DecimalFormat("0.00").format(currentKG)
                val model = IndividualInvoiceDetailsModel(
                    ""+invoiceBagModel.CommodityId,
                            ""+invoiceBagModel.BillAmount,
                            ""+invoiceBagModel.BillBags,
                            ""+invoiceBagModel.HSNCode,
                            ""+currentQTL,
                            ""+invoiceBagModel.BillRate,
                            ""+invoiceKG,
                            ""+invoiceBagModel.InStockId
                    
                )
                invoiceBagList.add(model)
            }
            val postIndPCAInvoiceInsertModel = IndPCAInvoiceDataInsertModel(
                "Insert",
                        ""+binding.edtBuyerAddressIndPCAInvoicePreviewFragment.text.toString().trim(),
                        ""+invoiceDataFromAPI.BuyerCity,
                        ""+binding.actBuyerCityIndPCAInvoicePreviewFragment.text.toString().trim(),
                        ""+binding.edtBuyerGSTINIndPCAInvoicePreviewFragment.text.toString().trim(),
                        ""+invoiceDataFromAPI.BuyerId,
                        ""+binding.actBuyerNameIndPCAInvoicePreviewFragment.text.toString().trim(),
                        ""+binding.edtBuyerPANIndPCAInvoicePreviewFragment.text.toString().trim(),
                        ""+invoiceDataFromAPI.CompanyCode,
                        ""+DateUtility().getyyyyMMddDateTime(),
                        ""+PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,""),
                        ""+DateUtility().getyyyyMMdd(),
                        "Android",
                        ""+finalExpenses.finalTotalAmount,
                        ""+binding.tvAmountInWordsIndPCAInvoicePreviewFragment.text.toString().trim(),
                        ""+finalExpenses.finalCGSTAmount,
                        ""+finalExpenses.finalCGSTPCT,
                        ""+finalExpenses.finalGSTAmount,
                        ""+finalExpenses.finalGSTPCT,
                        ""+finalExpenses.finalLabourAmount,
                        ""+finalExpenses.finalLabourRate,
                        ""+finalExpenses.finalMarketFeesAmount,
                        ""+finalExpenses.finalMarketFeesPCT,
                        ""+finalExpenses.finalPCACommAmount,
                        ""+finalExpenses.finalPCACommPCT,
                        ""+finalExpenses.finalSGSTAmount,
                        ""+finalExpenses.finalSGSTPCT,
                        ""+BASIC_AMOUNT,
                        ""+finalExpenses.finalTransportAmount,
                        ""+finalExpenses.finalTransportRate,
                        ""+invoiceDataFromAPI.FinanceYear,
                        ""+binding.edtIndPCAGSTINIndPCAInvoicePreviewFragment.text.toString().trim(),
                        ""+binding.edtIndPCAPANIndPCAInvoicePreviewFragment.text.toString().trim(),
                        ""+invoiceDataFromAPI.PCARegId,
                        invoiceBagList,
                        "",
                        ""+DateUtility().getyyyyMMddDateTime(),
                        ""+invoiceDataFromAPI.PCARegId,
                        ""+binding.edtVehicleNoIndPCAInvoicePreviewFragment.text.toString().trim(),

            )

            Log.d(TAG, "sendInvoiceData: INVOICE_MODEL : $postIndPCAInvoiceInsertModel")

            if (ConnectionCheck.isConnected(requireContext()))
            {
                POSTIndPCAInvoicePreviewInsertAPI(requireContext(),this@IndPCAInvoicePreviewFragment,postIndPCAInvoiceInsertModel,_InvoiceStockList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "sendInvoiceData: ${e.message}")
        }
    }

    fun downloadInvoice(invoiceId:String)
    {
        try {
            val fileURL = URLHelper.IND_PCA_INVOICE_PDF.replace("<INVOICE_NO>",invoiceId).replace("<PCA_REG_ID>",PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString())
            Log.d(TAG, "downloadInvoice: fileURL : $fileURL")
            fileDownloader.downloadFile(fileURL,"Individual_PCA_Report_Book.pdf", "Downloading Invoice")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "downloadAuctionDetailReport: ${e.message}")
        }
    }

    private fun resetBuyerData(){
        binding.edtBuyerAddressIndPCAInvoicePreviewFragment.setText("")
        binding.actBuyerCityIndPCAInvoicePreviewFragment.setText("")
        binding.edtBuyerGSTINIndPCAInvoicePreviewFragment.setText("")
        binding.edtBuyerPANIndPCAInvoicePreviewFragment.setText("")
        invoiceDataFromAPI.PerBoriRate="0"
    }

    fun successRedirect(){
        try {
            navController.navigate(IndPCAInvoicePreviewFragmentDirections.actionIndPCAInvoicePreviewFragmentToIndPCAInvoiceStockFragment())
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "successRedirect: ${e.message}")
        }
    }

    data class CityDetail(var CityName:String,var CityId:String)

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: ON_DESTROY_VIEW")
        _binding = null
    }
}