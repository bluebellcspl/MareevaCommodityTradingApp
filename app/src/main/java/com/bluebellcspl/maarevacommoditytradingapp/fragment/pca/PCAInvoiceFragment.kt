package com.bluebellcspl.maarevacommoditytradingapp.fragment.pca

import ConnectionCheck
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.icu.text.NumberFormat
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.isEmpty
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.InvoiceAdapter
import com.bluebellcspl.maarevacommoditytradingapp.adapter.OnParentCheckedChangeListener
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentPCAInvoiceBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchInvoiceDataAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.POSTSelectedInvoiceShop
import com.bluebellcspl.maarevacommoditytradingapp.model.GCAData
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceDataModel
import com.bluebellcspl.maarevacommoditytradingapp.model.POSTSelectedInvoiceListModel
import com.bluebellcspl.maarevacommoditytradingapp.model.Shopwise
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.text.DecimalFormat
import java.util.Locale

class PCAInvoiceFragment : Fragment(), InvoiceSelectedDataCallBack {
    var _binding: FragmentPCAInvoiceBinding?=null
    val binding get() = _binding!!
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val TAG = "PCAInvoiceFragment"
    private val navController: NavController by lazy { findNavController() }
    lateinit var alertDialog: AlertDialog
    lateinit var adapter: InvoiceAdapter
    var shopWiseList: ArrayList<Shopwise> = arrayListOf()
    var shopNameAdapterList: ArrayList<String> = arrayListOf()
    var selectedChipList: ArrayList<String> = arrayListOf()
    var selectedShopList: ArrayList<Shopwise> = arrayListOf()
    var fromSelectedDate = 0L
    var toSelectedDate = 0L
    var COMMODITY_ID = ""
    var isExpanded = false
    lateinit var menuHost: MenuHost
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_p_c_a_invoice, container, false)
//        setRetainInstance(true)
        menuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.invoice_menu,menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId)
                {
                    R.id.btn_Invoice_Forward->{
                        redirectToInvoiceDetailFragment()
                    }
                }
                return true
            }
        },viewLifecycleOwner,Lifecycle.State.STARTED)

        binding.nestedScrollViewPCAInvoiceFragment.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, _, _, _ ->
            Log.d(TAG, "onCreateView: HEIGHT_NESTVIEW : ${binding.nestedScrollViewPCAInvoiceFragment.height}")
            if (!isExpanded) {
                animateHeight(binding.nestedScrollViewPCAInvoiceFragment, resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._200sdp))
                isExpanded = true
            }
        })

        setOnClickListeners()


        binding.btnShowDataPCAInvoiceFragment.setOnClickListener {
//            val filterShopList = arrayListOf<Shopwise>()
                selectedShopList.clear()
                selectedChipList.clear()
            for (currentChip in binding.ChipGroupPCAInvoiceFragment.children) {
                val chip = currentChip as Chip
                val shopName = chip.text.toString()
                val shortShopName = shopName.split("-")[1]
                for (shopModel in shopWiseList) {
                    if (shopModel.ShopShortName.equals(shortShopName)) {
                        selectedShopList.add(shopModel)
                        selectedChipList.add(chip.text.toString())
                    }
                }
            }

            bindInvoiceRCView(selectedShopList)

        }
        return binding.root
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

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "PCAInvoiceFragment?? onDestroy: DESTROYED")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "PCAInvoiceFragment?? onPause: PAUSED")
        Log.d(TAG, "onPause: SELECTED_CHIP_LIST : $selectedChipList")
//        outState.putStringArrayList(SELECTED_CHIP_LIST_KEY,selectedChipList)

    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "PCAInvoiceFragment?? onResume: RESUMED")
        binding?.ChipGroupPCAInvoiceFragment?.removeAllViews()
        selectedShopList.clear()
        shopWiseList.clear()
        if (binding.edtFromDatePCAInvoiceFragment.text.toString().isNotEmpty() && binding.edtToDatePCAInvoiceFragment.text.toString().isNotEmpty())
        {
            for (chipName in selectedChipList)
            {
                addChip(chipName)
            }

            if (ConnectionCheck.isConnected(requireContext())) {
//            resetUI()
                FetchInvoiceDataAPI(
                    requireContext(),
                    this,
                    binding.edtFromDatePCAInvoiceFragment.text.toString().trim(),
                    binding.edtToDatePCAInvoiceFragment.text.toString().trim()
                )
            }
            commonUIUtility.showProgress()
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                for (currentChip in binding.ChipGroupPCAInvoiceFragment.children) {
                    val chip = currentChip as Chip
                    val shopName = chip.text.toString()
                    val shortShopName = shopName.split("-")[1]
                    for (shopModel in shopWiseList) {
                        if (shopModel.ShopShortName.equals(shortShopName)) {
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

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "PCAInvoiceFragment?? onStop: STOPPED")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "PCAInvoiceFragment?? onDestroyView: DESTROYED_VIEW")
        _binding = null
    }

    private fun addChip(text: String) {
        val chip = Chip(requireContext())
        chip.text = text
        chip.isCloseIconVisible = true
        chip.setChipBackgroundColorResource(R.color.colorPrimaryDark)
        chip.setTextColor(resources.getColor(R.color.white))
        chip.setCloseIconTintResource(R.color.white)
        chip.setOnCloseIconClickListener {
            binding.ChipGroupPCAInvoiceFragment.removeView(it)
            binding.ChipGroupPCAInvoiceFragment.forEach {
                it as Chip
                Log.d(TAG, "CHIP_GROUP_AFTER_REMOVING: ${it.text.toString()}")
            }
            if (binding.ChipGroupPCAInvoiceFragment.isEmpty())
            {
                binding.llGrandTotalPCAInvoiceFragment.visibility = View.GONE
                binding.rcViewPCAInvoiceFragment.adapter = null
                binding.rcViewPCAInvoiceFragment.invalidate()
            }
        }
        var chipNameExists = false
        for (i in 0 until binding.ChipGroupPCAInvoiceFragment.childCount) {
            val currentChip = binding.ChipGroupPCAInvoiceFragment.getChildAt(i) as Chip
            if (currentChip.text == chip.text) {
                chipNameExists = true
                break
            }
        }

        if (!chipNameExists) {
            binding.ChipGroupPCAInvoiceFragment.addView(chip)
        } else {
            commonUIUtility.showToast("Shop Already Selected!")
        }
    }

    private fun setOnClickListeners() {
        try {
            binding.edtFromDatePCAInvoiceFragment.inputType = InputType.TYPE_NULL
            binding.edtToDatePCAInvoiceFragment.inputType = InputType.TYPE_NULL

            binding.edtFromDatePCAInvoiceFragment.setOnClickListener {
                showFromDatePickerDialog(binding.edtFromDatePCAInvoiceFragment)
            }

            binding.edtToDatePCAInvoiceFragment.setOnClickListener {
                if (binding.edtFromDatePCAInvoiceFragment.text.toString().isNotEmpty()) {
                    showToDatePickerDialog(binding.edtToDatePCAInvoiceFragment)
                } else {
                    commonUIUtility.showToast("Please Select From Date!")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "setOnClickListeners: ${e.message}")
        }
    }

    private fun bindInvoiceRCView(datalist: ArrayList<Shopwise>) {
        try {
            if (isAdded)
            {
                binding.rcViewPCAInvoiceFragment.visibility = View.VISIBLE
                adapter =
                    InvoiceAdapter(requireContext(), datalist, object : OnParentCheckedChangeListener {
                        override fun onParentCheckedChange(position: Int, isChecked: Boolean) {
                            datalist[position].isSelected = isChecked
                            adapter.notifyItemChanged(position)
                        }
                    }, this)
                binding.rcViewPCAInvoiceFragment.adapter = adapter
                binding.rcViewPCAInvoiceFragment.invalidate()
            }
        } catch (e: Exception) {
            Log.e(TAG, "bindInvoiceRCView: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun showFromDatePickerDialog(editText: TextInputEditText) {
//        var selectedDate = if (editText.text.toString().isEmpty())
//        {
//            System.currentTimeMillis()
//        }else
//        {
//            DateUtility().getDateFromEditTextAsLong(editText,"yyyy-MM-dd")
//        }
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
            if (binding.edtToDatePCAInvoiceFragment.text.toString().isNotEmpty()) {
                if (DateUtility().isStartDateBeforeEndDate(binding.edtFromDatePCAInvoiceFragment.text.toString().trim(),binding.edtToDatePCAInvoiceFragment.text.toString().trim(),"yyyy-MM-dd"))
                {
                    if (ConnectionCheck.isConnected(requireContext())) {
                        resetUI()
                        FetchInvoiceDataAPI(
                            requireContext(),
                            this,
                            binding.edtFromDatePCAInvoiceFragment.text.toString().trim(),
                            binding.edtToDatePCAInvoiceFragment.text.toString().trim()
                        )
                    }
                }
            }

        }
        if (!datePicker.isAdded) {
            datePicker.show(parentFragmentManager, datePicker.toString())
        }
    }

    private fun showToDatePickerDialog(editText: TextInputEditText) {
//        var selectedDate = if (editText.text.toString().isEmpty())
//        {
//            System.currentTimeMillis()
//        }else
//        {
//            DateUtility().getDateFromEditTextAsLong(editText,"yyyy-MM-dd")
//        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.parse(binding.edtFromDatePCAInvoiceFragment.text.toString().trim())

        val calendarConstraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.from(date.time))
            .build()
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
            if (DateUtility().isStartDateBeforeEndDate(binding.edtFromDatePCAInvoiceFragment.text.toString().trim(),binding.edtToDatePCAInvoiceFragment.text.toString().trim(),"yyyy-MM-dd"))
            {
                if (ConnectionCheck.isConnected(requireContext())) {
                    resetUI()
                    FetchInvoiceDataAPI(
                        requireContext(),
                        this,
                        binding.edtFromDatePCAInvoiceFragment.text.toString().trim(),
                        binding.edtToDatePCAInvoiceFragment.text.toString().trim()
                    )
                }
            }
        }
        if (!datePicker.isAdded) {
            datePicker.show(parentFragmentManager, datePicker.toString())
        }
    }

    fun bindFilterForRecyclerview(invoiceModel: InvoiceDataModel) {
        try {
            shopWiseList = invoiceModel.ShopwiseList
            shopNameAdapterList = ArrayList<String>()

            for (i in shopWiseList) {
                Log.d(
                    TAG,
                    "bindFilterForRecyclerview: DATA_HAS_SHOP : ${i.ShopNo}-${i.ShopShortName}"
                )
                val customShopName = "${i.ShopNo}-${i.ShopShortName}"
                if (!shopNameAdapterList.contains(customShopName)) {
                    shopNameAdapterList.add("${i.ShopNo}-${i.ShopShortName}")
                }
            }
            shopNameAdapterList.add(0, "ALL")
            val adapter = commonUIUtility.getCustomArrayAdapter(shopNameAdapterList)
            binding.mactTotalBagsPCAInvoiceFragment.setAdapter(adapter)

            binding.mactTotalBagsPCAInvoiceFragment.setOnItemClickListener { parent, _, position, _ ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                if (selectedItem.equals("ALL")) {
                    for (i in 1 until shopNameAdapterList.size) {
                        addChip(parent.getItemAtPosition(i).toString())
                        selectedChipList.add(parent.getItemAtPosition(i).toString())
                    }
                    binding.mactTotalBagsPCAInvoiceFragment.setText("")
                } else {
                    addChip(selectedItem)
                    selectedChipList.add(selectedItem)
                    binding.mactTotalBagsPCAInvoiceFragment.setText("")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindFilterForRecyclerview: ${e.message}")
        }
    }

    override fun getSelectedInvoiceData(
        selectedInvoiceList: ArrayList<Shopwise>,
        parentPosition: Int
    ) {
//        var sum = 0.0
        var shop_bags = 0.0
        var shop_avg_rate = 0.0
        var shop_total_amount = 0.0
        var commodityBhartiPrice = 0.0
        for (shop in selectedInvoiceList) {
            commodityBhartiPrice = shop.CommodityBhartiPrice.toDouble()
            for (entry in shop.ShopEntries) {
                if (entry.isSelected) {
                    shop_bags += entry.Bags.toDouble()
                    shop_total_amount += entry.Amount.toDouble()
                }
            }
        }
        shop_avg_rate = shop_total_amount / ((shop_bags * commodityBhartiPrice) / 20.0)


        val naNChecker: Double = shop_avg_rate.let { if (it.isNaN()) 0.0 else it }
        var fr_shop_bags = DecimalFormat("0.00").format(shop_bags)
        var fr_shop_avg_rate = DecimalFormat("0.00").format(naNChecker)
        var fr_shop_total_amount = DecimalFormat("0.00").format(shop_total_amount)

        if (fr_shop_bags.contains(".")) {
            val decimalValue = fr_shop_bags.split(".")[1].toDouble()
            if (decimalValue > 0.0) {
                fr_shop_bags = DecimalFormat("0.00").format(shop_bags)
            } else {
                fr_shop_bags = DecimalFormat("0.00").format(shop_bags).split(".")[0]
            }
        }

        var formattedCurrentPrice =
            NumberFormat.getCurrencyInstance().format(fr_shop_avg_rate.toDouble()).substring(1)
        if (formattedCurrentPrice.contains(".")) {
            val decimalValue = formattedCurrentPrice.split(".")[1].toDouble()
            if (decimalValue > 0.0) {
                formattedCurrentPrice =
                    NumberFormat.getCurrencyInstance().format(fr_shop_avg_rate.toDouble())
                        .substring(1)
            } else {
                formattedCurrentPrice =
                    NumberFormat.getCurrencyInstance().format(fr_shop_avg_rate.toDouble())
                        .substring(1).split(".")[0]
            }
        }

        var formattedAmount =
            NumberFormat.getCurrencyInstance().format(fr_shop_total_amount.toDouble()).substring(1)
        if (formattedAmount.contains(".")) {
            val decimalValue = formattedAmount.split(".")[1].toDouble()
            if (decimalValue > 0.0) {
                formattedAmount =
                    NumberFormat.getCurrencyInstance().format(fr_shop_total_amount.toDouble())
                        .substring(1)
            } else {
                formattedAmount =
                    NumberFormat.getCurrencyInstance().format(fr_shop_total_amount.toDouble())
                        .substring(1).split(".")[0]
            }
        }

        if (isExpanded)
        {
            animateHeight(binding.nestedScrollViewPCAInvoiceFragment, resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._50sdp))
            Log.d(TAG, "getSelectedInvoiceData: HEIGHT_NESTVIEW : ${binding.nestedScrollViewPCAInvoiceFragment.height}")
            isExpanded = false
        }

        if (shop_bags > 0) {
            binding.llGrandTotalPCAInvoiceFragment.visibility = View.VISIBLE
            binding.tvTotBagPCAInvoiceFragment.setText(fr_shop_bags)
            binding.tvTotAvgPricePCAInvoiceFragment.setText(formattedCurrentPrice)
            binding.tvTotalAmountPCAInvoiceFragment.setText(formattedAmount)
        } else {
            binding.llGrandTotalPCAInvoiceFragment.visibility = View.GONE
        }

    }

    override fun onSaveButtonClick(dataList: ArrayList<Shopwise>) {
        try {
//            if (checkIfAllUsersInactive(dataList)) {
                val postInvoiceSelectedList = ArrayList<GCAData>()

                for (shop in dataList) {
                    if (shop.Amount.toDouble() > 0.0 && shop.PurchasedBag.toDouble() > 0.0) {
                        COMMODITY_ID = shop.CommodityId
                        val pcaAuctionDetailIdList = ArrayList<String>()
                        for (shopEntries in shop.ShopEntries) {
                            if (shopEntries.isSelected)
                            {
                                pcaAuctionDetailIdList.add(shopEntries.PCAAuctionDetailId)
                            }
                        }
                        val newPcaAuctionDetailIdList = pcaAuctionDetailIdList.joinToString(",")

                        val gcaDataModel = GCAData(
                            "Insert",
                            shop.Amount,
                            shop.PurchasedBag,
                            shop.CommodityBhartiPrice,
                            "",
                            shop.CommodityId,
                            PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE, "").toString(),
                            DateUtility().getyyyyMMdd(),
                            PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString(),
                            shop.CurrentPrice,
                            "",
                            "",
                            "",
                            "0",
                            "0",
                            "0",
                            "0",
                            "",
                            newPcaAuctionDetailIdList,
                            PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString(),
                            PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString(),
                            shop.ShopId,
                            "",
                            DateUtility().getyyyyMMdd(),
                            "",
                            "0"
                        )
                        
                        postInvoiceSelectedList.add(gcaDataModel)
                    }
                }

                postInvoiceSelectedList.forEach {
                    Log.d(TAG, "onSaveButtonClick: POST_SELECTED_INVOICE_LIST_ITEM : $it")
                }

                val postSelectedDataModel = POSTSelectedInvoiceListModel(
                    PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,"").toString(),
                    binding.edtFromDatePCAInvoiceFragment.text.toString().trim(),
                    postInvoiceSelectedList,
                    PrefUtil.getSystemLanguage()!!,
                    PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString(),
                    binding.edtToDatePCAInvoiceFragment.text.toString().trim()
                )

            if (ConnectionCheck.isConnected(requireContext()))
            {
                POSTSelectedInvoiceShop(requireContext(),this,postSelectedDataModel)
//                redirectToInvoiceDetailFragment()
            }
//            } else {
//                commonUIUtility.showToast("Please Select at least 1 Entry!")
//            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "onSaveButtonClick: ${e.message}")
        }
    }

    fun checkIfAllUsersInactive(shopList: ArrayList<Shopwise>): Boolean {
        var allInactive = false
        for (shop in shopList) {
            for (entry in shop.ShopEntries) {
                if (entry.isSelected) {
                    allInactive = true
                    break
                }
            }
        }
        return allInactive
    }

    fun resetUI() {
        binding.llGrandTotalPCAInvoiceFragment.visibility = View.GONE
        binding.ChipGroupPCAInvoiceFragment.removeAllViews()
        binding.rcViewPCAInvoiceFragment.visibility = View.GONE
    }

    fun redirectToInvoiceDetailFragment()
    {
        try {
            navController.navigate(
                PCAInvoiceFragmentDirections.actionPCAInvoiceFragmentToPCAInvoiceDetailFragment(
                    binding.edtFromDatePCAInvoiceFragment.text.toString(),
                    binding.edtToDatePCAInvoiceFragment.text.toString(),
                    COMMODITY_ID
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "redirectToInvoiceDetailFragment: ${e.message}", )
        }
    }
}

interface InvoiceSelectedDataCallBack {
    fun getSelectedInvoiceData(selectedInvoiceList: ArrayList<Shopwise>, parentPosition: Int)

    fun onSaveButtonClick(dataList: ArrayList<Shopwise>)
}