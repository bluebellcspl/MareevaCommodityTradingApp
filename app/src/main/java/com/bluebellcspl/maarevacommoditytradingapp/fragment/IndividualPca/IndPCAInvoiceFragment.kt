package com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca

import ConnectionCheck
import android.animation.ValueAnimator
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.isEmpty
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.IndPCAInvoiceAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentIndPCAInvoiceBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCADashboardFragment.CommodityDetail
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchIndPCAInvoiceDataAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.POSTIndPCAStockInsertAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.APIIndividualInvoiceShopwise
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoiceDataModel
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAStockInsertItemModel
import com.bluebellcspl.maarevacommoditytradingapp.model.POSTIndPCAStockInsertModel
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar
import java.util.Date
import java.util.Locale

class IndPCAInvoiceFragment : Fragment() {
    var _binding: FragmentIndPCAInvoiceBinding? = null
    val binding get() = _binding!!
    val TAG = "IndPCAInvoiceFragment"
    private val commonUIUtility: CommonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val navController: NavController by lazy { findNavController() }
    var _CommodityList : ArrayList<CommodityDetail> = ArrayList()
    var _CommodityNameList : ArrayList<String> = ArrayList()
    var fromSelectedDate = 0L
    var toSelectedDate = 0L
    var shopWiseList: ArrayList<APIIndividualInvoiceShopwise> = arrayListOf()
    var shopNameAdapterList: ArrayList<String> = arrayListOf()
    var selectedChipList: ArrayList<String> = arrayListOf()
    var selectedShopList: ArrayList<APIIndividualInvoiceShopwise> = arrayListOf()
    var COMMODITY_ID = ""
    var isExpanded = false
    var isShopAdded = false
    lateinit var adapter: IndPCAInvoiceAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_ind_p_c_a_invoice, container, false)

        //Commodity DropDown Selection
        binding.actCommodityIndPCAInvoiceFragment.setOnItemClickListener { adapterView, view, position, long ->
            var commodityModel: CommodityDetail
            if (PrefUtil.getSystemLanguage().equals("gu")){
                commodityModel = _CommodityList.find {it.CommodityName.equals(adapterView.getItemAtPosition(position).toString())}!!
            }
            else{
                commodityModel = _CommodityList.find {it.CommodityName.equals(adapterView.getItemAtPosition(position).toString())}!!
            }
            COMMODITY_ID = commodityModel.CommodityId
            Log.d(TAG, "onCreateView: SELECTED_COMMODITY_NAME : ${commodityModel.CommodityName}")
            Log.d(TAG, "onCreateView: SELECTED_COMMODITY_ID : ${commodityModel.CommodityId}")

//            PrefUtil.setString(PrefUtil.KEY_COMMODITY_NAME,commodityModel.CommodityName)
//            PrefUtil.setString(PrefUtil.KEY_COMMODITY_ID,commodityModel.CommodityId)

            Log.d(TAG, "onCreateView: SAVED_COMMODITY_NAME : ${PrefUtil.getString(PrefUtil.KEY_COMMODITY_NAME,"")}")
            Log.d(TAG, "onCreateView: SAVED_COMMODITY_ID : ${PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"")}")
        }

        //NestScrollView Resizing
        binding.nestedScrollViewIndPCAInvoiceFragment.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, _, _, _ ->
            Log.d(TAG, "onCreateView: HEIGHT_NESTVIEW : ${binding.nestedScrollViewIndPCAInvoiceFragment.height}")
            if (!isExpanded) {
                animateHeight(binding.nestedScrollViewIndPCAInvoiceFragment, resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._150sdp))
                isExpanded = true
            }
        })
        setOnClickListeners()

        binding.btnShowDataIndPCAInvoiceFragment.setOnClickListener {
            if (binding.edtFromDateIndPCAInvoiceFragment.text.toString().isEmpty() &&
                binding.edtToDateIndPCAInvoiceFragment.text.toString().isEmpty()){
                commonUIUtility.showToast(getString(R.string.please_select_date_alert_msg))
            }else if(COMMODITY_ID.isEmpty() || !_CommodityNameList.contains(binding.actCommodityIndPCAInvoiceFragment.text.toString().trim())){
                commonUIUtility.showToast(getString(R.string.please_select_proper_commodity_alert_msg))
            }
            else{
                selectedShopList.clear()
                selectedChipList.clear()
                for (currentChip in binding.ChipGroupIndPCAInvoiceFragment.children) {
                    val chip = currentChip as Chip
                    val shopName = chip.text.toString()
//                    val shortShopName = shopName.split("-")[1]
                    for (shopModel in shopWiseList) {
//                        if (shopModel.ShopShortName.equals(shortShopName)) {
                        if (shopModel.ShopNoName.equals(shopName) && shopModel.CommodityId.equals(COMMODITY_ID)) {
                            if (!selectedShopList.contains(shopModel))
                            {
                                selectedShopList.add(shopModel)
                                selectedChipList.add(chip.text.toString())
                            }
                        }
                    }
                }
                binding.rcViewIndPCAInvoiceFragment.invalidate()
                binding.rcViewIndPCAInvoiceFragment.adapter = null
                bindInvoiceRCView(selectedShopList)
            }
        }

        binding.nestedScrollViewIndPCAInvoiceFragment.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY) {
                Log.d("????", "onScrolled: USER_SCROLLING_DOWN")
                if (isExpanded) {
                    animateHeight(binding.nestedScrollViewIndPCAInvoiceFragment, resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._80sdp))
                    isExpanded = false
                }
            }
        }
        return binding.root
    }

    private fun setOnClickListeners() {
        try {
            binding.edtFromDateIndPCAInvoiceFragment.inputType = InputType.TYPE_NULL
            binding.edtToDateIndPCAInvoiceFragment.inputType = InputType.TYPE_NULL

            binding.edtFromDateIndPCAInvoiceFragment.setOnClickListener {
                showFromDatePickerDialog(binding.edtFromDateIndPCAInvoiceFragment)
            }

            binding.edtToDateIndPCAInvoiceFragment.setOnClickListener {
                if (binding.edtFromDateIndPCAInvoiceFragment.text.toString().isNotEmpty()) {
                    showToDatePickerDialog(binding.edtToDateIndPCAInvoiceFragment)
                } else {
                    commonUIUtility.showToast("Please Select From Date!")
                }
            }

            binding.btnSaveIndPCAInvoiceFragment.setOnClickListener {
                var isWeightZero = false
                var isAmountZero = false
                var isAmountBlank = false
                var isWeightBlank = false
                for (model in selectedShopList!!)
                {
                    for (shop in model.ShopEntries)
                    {
                        if (shop.isWeightBlank)
                        {
                            isWeightBlank = true
                            break
                        }
                        if (shop.isAmountBlank)
                        {
                            isAmountBlank = true
                            break
                        }
                        if (shop.BillWeight.toDouble()<0 || shop.BillWeight.toDouble()==0.0)
                        {
                            isWeightZero = true
                            break
                        }
                        if (shop.BillAmount.toDouble()<0 || shop.BillAmount.toDouble()==0.0)
                        {
                            isAmountZero = true
                            break
                        }
                    }

                }
                if (isWeightBlank){
                    commonUIUtility.showToast(getString(R.string.please_enter_weight_alert_msg))
                    return@setOnClickListener
                }else if (isAmountBlank) {
                    commonUIUtility.showToast(getString(R.string.please_enter_amount_alert_msg))
                    return@setOnClickListener
                }
                else if (isWeightZero){
                    commonUIUtility.showToast(getString(R.string.please_enter_weight_alert_msg))
                    return@setOnClickListener
                }else if (isAmountZero){
                    commonUIUtility.showToast(getString(R.string.please_enter_amount_alert_msg))
                    return@setOnClickListener
                }else{
                    Log.d(TAG, "setOnClickListeners: PROCEED_FOR_INSERT")
                    insertIndPCAInvoiceData(selectedShopList)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "setOnClickListeners: ${e.message}")
        }
    }

    private fun bindInvoiceRCView(datalist: ArrayList<APIIndividualInvoiceShopwise>) {
        try {
            if (isAdded)
            {
                if (datalist.isNotEmpty())
                {
                    binding.btnSaveIndPCAInvoiceFragment.visibility = View.VISIBLE
                    binding.rcViewIndPCAInvoiceFragment.visibility = View.VISIBLE
                    datalist.forEach {parentModel->
                        parentModel.ShopEntries.forEach {childModel->
                            childModel.BillAmount = childModel.Amount
                            childModel.BillWeight = childModel.Weight
                            childModel.BillGST = commonUIUtility.formatDecimal(childModel.Amount.toDouble() * (childModel.TotalPct.toDouble() / 100.0))
                            childModel.BillTotalAmount = commonUIUtility.formatDecimal(childModel.Amount.toDouble() + childModel.BillGST.toDouble())
                            childModel.BillBags = childModel.Bags
                            childModel.BillRate = commonUIUtility.formatDecimal(childModel.Amount.toDouble() / (childModel.Weight.toDouble() / 20.0))
                            var totalInvoiceApproxKG = commonUIUtility.formatDecimal(childModel.Weight.toDouble() / childModel.CommodityBhartiPrice.toDouble())
                            var totalInvoiceKG =commonUIUtility.formatDecimal(childModel.Weight.toDouble() % childModel.CommodityBhartiPrice.toDouble())
                            childModel.BillApproxKg = totalInvoiceApproxKG
                            childModel.BillKg = totalInvoiceKG
                        }
                    }
                    adapter =
                        IndPCAInvoiceAdapter(requireContext(), datalist)
                    binding.rcViewIndPCAInvoiceFragment.adapter = adapter
                    binding.rcViewIndPCAInvoiceFragment.invalidate()
                }else{
                    binding.btnSaveIndPCAInvoiceFragment.visibility = View.GONE
                    binding.rcViewIndPCAInvoiceFragment.visibility = View.GONE
                    commonUIUtility.showToast(requireContext().getString(R.string.no_data_found))
                }

            }
        } catch (e: Exception) {
            Log.e(TAG, "bindInvoiceRCView: ${e.message}")
            e.printStackTrace()
        }
    }

    fun bindFilterForRecyclerview(invoiceModel: IndPCAInvoiceDataModel) {
        try {
            shopWiseList = invoiceModel.APIIndividualInvoiceShopwiseList
            shopNameAdapterList = ArrayList<String>()
            for (i in shopWiseList) {
                Log.d(
                    TAG,
                    "bindFilterForRecyclerview: DATA_HAS_SHOP : ${i.ShopNo}-${i.ShopShortName}"
                )
                val customShopName = i.GujShopNoName
                if (!shopNameAdapterList.contains(customShopName)) {
                    shopNameAdapterList.add(i.GujShopNoName)
                }
            }
            val sortedShopNameList = commonUIUtility.sortAlphanumericList(shopNameAdapterList)
            sortedShopNameList.add(0, "ALL")
            val adapter = commonUIUtility.getCustomArrayAdapter(sortedShopNameList)
            binding.actShopIndPCAInvoiceFragment.setAdapter(adapter)

            binding.actShopIndPCAInvoiceFragment.setOnItemClickListener { parent, _, position, _ ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                if (selectedItem.equals("ALL")) {
                    // Start from 1 to skip "ALL" itself
                    for (i in 1 until sortedShopNameList.size) {
                        val shopName = sortedShopNameList[i]
                        addChip(shopName)
                        selectedChipList.add(shopName)
                        Log.d(TAG, "bindFilterForRecyclerview: SHOP_NAME N INDEX : $shopName - $i")
                    }
                    binding.actShopIndPCAInvoiceFragment.setText("")
                } else {
                    addChip(selectedItem)
                    selectedChipList.add(selectedItem)
                    binding.actShopIndPCAInvoiceFragment.setText("")
                }
            }

            //Bind Commodity Available From Shop
            for (i in shopWiseList)
            {
                val commodityModel = CommodityDetail(i.CommodityId,i.CommodityName,"")
                if (!_CommodityList.contains(commodityModel))
                {
                    _CommodityList.add(commodityModel)
                }
            }
                _CommodityNameList.forEach { model ->
                    Log.d(TAG, "bindFilterForRecyclerview: AVAILABLE_COMMODITY : $model")
                }
            _CommodityNameList = ArrayList(_CommodityList.map { it.CommodityName })
            val commodityAdapter = commonUIUtility.getCustomArrayAdapter(_CommodityNameList)
            binding.actCommodityIndPCAInvoiceFragment.setAdapter(commodityAdapter)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindFilterForRecyclerview: ${e.message}")
        }
    }

    private fun animateHeight(view: View, targetHeightPx: Int) {
        try {
            val initialHeight = view.height
            val animator = ValueAnimator.ofInt(initialHeight, targetHeightPx)
            animator.addUpdateListener { valueAnimator ->
                val animatedValue = valueAnimator.animatedValue as Int
                val layoutParams = view.layoutParams
                layoutParams.height = animatedValue
                view.layoutParams = layoutParams
            }
            animator.duration = 300
            animator.start()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "animateHeight: ${e.message}", )
        }
    }

    private fun addChip(text: String) {
        binding.nestedScrollViewIndPCAInvoiceFragment.visibility = View.VISIBLE
        val chip = Chip(requireContext())
        chip.text = text
        chip.isCloseIconVisible = true
        chip.setChipBackgroundColorResource(R.color.colorPrimaryDark)
        chip.setTextColor(resources.getColor(R.color.white))
        chip.setCloseIconTintResource(R.color.white)
        chip.setOnCloseIconClickListener {
            binding.ChipGroupIndPCAInvoiceFragment.removeView(it)
            binding.ChipGroupIndPCAInvoiceFragment.forEach {
                it as Chip
                Log.d(TAG, "CHIP_GROUP_AFTER_REMOVING: ${it.text.toString()}")
            }
            if (binding.ChipGroupIndPCAInvoiceFragment.isEmpty())
            {
                binding.btnSaveIndPCAInvoiceFragment.visibility = View.GONE
                binding.nestedScrollViewIndPCAInvoiceFragment.visibility = View.GONE
                binding.llGrandTotalIndPCAInvoiceFragment.visibility = View.GONE
                binding.rcViewIndPCAInvoiceFragment.adapter = null
                binding.rcViewIndPCAInvoiceFragment.invalidate()
            }
        }
        var chipNameExists = false
        for (i in 0 until binding.ChipGroupIndPCAInvoiceFragment.childCount) {
            val currentChip = binding.ChipGroupIndPCAInvoiceFragment.getChildAt(i) as Chip
            if (currentChip.text == chip.text) {
                chipNameExists = true
                break
            }
        }

        if (!chipNameExists) {
            binding.ChipGroupIndPCAInvoiceFragment.addView(chip)
        } else {
            if (!isShopAdded){
                commonUIUtility.showToast("Shop Already Selected!")
                isShopAdded = true
            }
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
            if (binding.edtToDateIndPCAInvoiceFragment.text.toString().isNotEmpty()) {
                if (DateUtility().isStartDateBeforeEndDate(binding.edtFromDateIndPCAInvoiceFragment.text.toString().trim(),binding.edtToDateIndPCAInvoiceFragment.text.toString().trim(),"yyyy-MM-dd"))
                {
                    if (ConnectionCheck.isConnected(requireContext())) {
                        resetUI()
                        FetchIndPCAInvoiceDataAPI(requireContext(),this@IndPCAInvoiceFragment,binding.edtFromDateIndPCAInvoiceFragment.text.toString().trim(),binding.edtToDateIndPCAInvoiceFragment.text.toString().trim())

                    }
                }
            }

        }
        if (!datePicker.isAdded) {
            datePicker.show(parentFragmentManager, datePicker.toString())
        }
    }

    private fun showToDatePickerDialog(editText: TextInputEditText) {

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = binding.edtFromDateIndPCAInvoiceFragment.text.toString().trim()

        val fromDate: Date? = try {
            dateFormat.parse(date)
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

//        val calendarConstraints = CalendarConstraints.Builder()
//            .setValidator(DateValidatorPointForward.from(fromDate.time))
//            .build()
        val builder =
            MaterialDatePicker.Builder.datePicker().setCalendarConstraints(calendarConstraints)
        if (toSelectedDate>0)
        {
            builder.setSelection(toSelectedDate)
        }

        val datePicker = builder.setTitleText("Select To Date").build()
        datePicker.addOnPositiveButtonClickListener {
            // Handle the selected date
            toSelectedDate = it
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = dateFormat.format(it)!!
            editText.setText(date)
            if (DateUtility().isStartDateBeforeEndDate(binding.edtFromDateIndPCAInvoiceFragment.text.toString().trim(),binding.edtToDateIndPCAInvoiceFragment.text.toString().trim(),"yyyy-MM-dd"))
            {
                if (ConnectionCheck.isConnected(requireContext())) {
                    resetUI()
                    FetchIndPCAInvoiceDataAPI(requireContext(),this,binding.edtFromDateIndPCAInvoiceFragment.text.toString().trim(),binding.edtToDateIndPCAInvoiceFragment.text.toString().trim())
                }
            }
        }
        if (!datePicker.isAdded) {
            datePicker.show(parentFragmentManager, datePicker.toString())
        }
    }

    fun resetUI() {
        binding.llGrandTotalIndPCAInvoiceFragment.visibility = View.GONE
        binding.ChipGroupIndPCAInvoiceFragment.removeAllViews()
        binding.rcViewIndPCAInvoiceFragment.visibility = View.GONE
        binding.btnSaveIndPCAInvoiceFragment.visibility = View.GONE
        if (isExpanded){
            animateHeight(binding.nestedScrollViewIndPCAInvoiceFragment, resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._80sdp))
            isExpanded = false
        }
        isShopAdded = false
        shopWiseList.clear()
        selectedChipList.clear()
        selectedShopList.clear()
    }
    fun navigateOnSuccessfulInsert(){
        navController.navigate(IndPCAInvoiceFragmentDirections.actionIndPCAInvoiceFragmentToIndPCAInvoiceStockFragment())
    }

    private fun insertIndPCAInvoiceData(datalist: ArrayList<APIIndividualInvoiceShopwise>){
        try {
            var ind_pca_id = ""
            val newStockList = ArrayList<IndPCAStockInsertItemModel>()
           for (model in datalist){
               for (entry in model.ShopEntries){
                   ind_pca_id = entry.IndividualPCAId
                   val insertStockModel = IndPCAStockInsertItemModel(
                       entry.BillAmount,
                       entry.BillApproxKg,
                       entry.BillBags,
                       entry.BillGST,
                       entry.BillKg,
                       entry.BillRate,
                       entry.BillTotalAmount,
                       entry.BillWeight,
                       entry.BuyerId,
                       entry.BuyerName,
                       entry.CommodityBhartiPrice,
                       entry.CommodityId,
                       DateUtility().getyyyyMMddDateTime(),
                       ""+PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString(),
                       entry.Date,
                       entry.TotalPct,
                       "",
                       entry.IndividualPCAAuctionDetailId,
                       entry.IndividualPCAAuctionMasterId,
                       "1",
                       "",
                       DateUtility().getyyyyMMddDateTime(),
                       "1",
                       ""+PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString()
                   )
                   PrefUtil.setString(PrefUtil.KEY_IND_PCA_ID,ind_pca_id)
                   newStockList.add(insertStockModel)
               }
           }
            val insertStockModel = POSTIndPCAStockInsertModel(
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString(),
                PrefUtil.getString(PrefUtil.KEY_IND_PCA_ID,"").toString(),
                PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,"").toString(),
                PrefUtil.getSystemLanguage().toString(),
                binding.edtFromDateIndPCAInvoiceFragment.text.toString().trim(),
                binding.edtToDateIndPCAInvoiceFragment.text.toString().trim(),
                "Insert",
                newStockList
            )

            if (ConnectionCheck.isConnected(requireContext())){
                POSTIndPCAStockInsertAPI(requireContext(),this@IndPCAInvoiceFragment,insertStockModel)
            }else{
                commonUIUtility.showToast(requireContext().getString(R.string.no_internet_connection))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "insertIndPCAInvoiceData: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        binding?.ChipGroupIndPCAInvoiceFragment?.removeAllViews()
        selectedShopList.clear()
        shopWiseList.clear()
        if (binding.edtFromDateIndPCAInvoiceFragment.text.toString().isNotEmpty() && binding.edtToDateIndPCAInvoiceFragment.text.toString().isNotEmpty())
        {
            for (chipName in selectedChipList)
            {
                addChip(chipName)
            }

            if (ConnectionCheck.isConnected(requireContext())) {
//            resetUI()
                FetchIndPCAInvoiceDataAPI(
                    requireContext(),
                    this,
                    binding.edtFromDateIndPCAInvoiceFragment.text.toString().trim(),
                    binding.edtToDateIndPCAInvoiceFragment.text.toString().trim()
                )
            }
            commonUIUtility.showProgress()
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                for (currentChip in binding.ChipGroupIndPCAInvoiceFragment.children) {
                    val chip = currentChip as Chip
                    val shopName = chip.text.toString()
                    for (shopModel in shopWiseList) {
                        if (shopModel.GujShopNoName.equals(shopName)) {
                            selectedShopList.add(shopModel)
                            selectedChipList.add(chip.text.toString())
                        }
                    }
                }
                commonUIUtility.dismissProgress()
                bindInvoiceRCView(selectedShopList)
            },2000)

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("??", "onDestroyView: ON_DESTROY_VIEW_IND_PCA_INVOICE")
    }

}