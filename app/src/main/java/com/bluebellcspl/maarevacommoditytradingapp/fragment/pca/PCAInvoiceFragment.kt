package com.bluebellcspl.maarevacommoditytradingapp.fragment.pca

import ConnectionCheck
import android.app.AlertDialog
import android.icu.text.NumberFormat
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.InvoiceAdapter
import com.bluebellcspl.maarevacommoditytradingapp.adapter.OnParentCheckedChangeListener
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentPCAInvoiceBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchInvoiceDataAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceDataModel
import com.bluebellcspl.maarevacommoditytradingapp.model.Shopwise
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.text.DecimalFormat
import java.util.Locale


class PCAInvoiceFragment : Fragment(),InvoiceSelectedDataCallBack {
    lateinit var binding: FragmentPCAInvoiceBinding
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val TAG = "PCAInvoiceFragment"
    private val navController: NavController by lazy { findNavController() }
    lateinit var alertDialog: AlertDialog
    lateinit var adapter: InvoiceAdapter
    var shopWiseList: ArrayList<Shopwise> = arrayListOf()
    var shopNameAdapterList : ArrayList<String> = arrayListOf()
    var selectedChipList:ArrayList<String> = arrayListOf()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_p_c_a_invoice, container, false)
        setRetainInstance(true)
        if (savedInstanceState!= null) {
            shopWiseList = savedInstanceState.getParcelableArrayList("ShopList")?: ArrayList()
            selectedChipList = savedInstanceState.getStringArrayList("SelectedChipList")?: ArrayList()
            shopNameAdapterList = savedInstanceState.getStringArrayList("ShopNameAdapterList")?: ArrayList()

            for (s_chip in selectedChipList) {
                addChip(s_chip)
            }

            binding.mactTotalBagsPCAInvoiceFragment.setAdapter(commonUIUtility.getCustomArrayAdapter(shopNameAdapterList))
            adapter.updateList(shopWiseList)
            Log.d(TAG, "onCreateView: SAVED_INSTANCE_NOT_NULL")
        } else {
            Log.d(TAG, "onCreateView: SAVED_INSTANCE_NULL")
        }

        setOnClickListeners()


        binding.btnShowDataPCAInvoiceFragment.setOnClickListener {
            val filterShopList = arrayListOf<Shopwise>()

            for (currentChip in binding.ChipGroupPCAInvoiceFragment.children) {
                val chip = currentChip as Chip
                val shopName = chip.text.toString()
                val shortShopName = shopName.split("-")[1]
                for (shopModel in shopWiseList) {
                    if (shopModel.ShopShortName.equals(shortShopName)) {
                        filterShopList.add(shopModel)
                    }
                }
            }

            bindInvoiceRCView(filterShopList)

        }
        return binding.root
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: DESTROYED")
        super.onDestroy()
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView: DESTROYED_VIEW")
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
//        val state = binding.rcViewPCAInvoiceFragment.layoutManager?.onSaveInstanceState()
//        outState.putParcelable("rcViewState", state)
        outState.putString("FromDate", binding.edtFromDatePCAInvoiceFragment.text.toString().trim())
        outState.putString("ToDate", binding.edtToDatePCAInvoiceFragment.text.toString().trim())
        outState.putParcelableArrayList("ShopList", shopWiseList)
        outState.putStringArrayList("SelectedChipList", selectedChipList)
        outState.putStringArrayList("ShopNameAdapterList", shopNameAdapterList)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
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
                }else
                {
                    commonUIUtility.showToast("Please Select From Date!")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "setOnClickListeners: ${e.message}")
        }
    }

    private fun bindInvoiceRCView(datalist:ArrayList<Shopwise>) {
        try {
            binding.rcViewPCAInvoiceFragment.visibility = View.VISIBLE
            adapter = InvoiceAdapter(requireContext(), datalist,object : OnParentCheckedChangeListener {
                override fun onParentCheckedChange(position: Int, isChecked: Boolean) {
                    datalist[position].isSelected = isChecked
                    adapter.notifyItemChanged(position)
                }
            },this)
            binding.rcViewPCAInvoiceFragment.adapter = adapter
            binding.rcViewPCAInvoiceFragment.invalidate()
        } catch (e: Exception) {
            Log.e(TAG, "bindInvoiceRCView: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun showFromDatePickerDialog(editText: TextInputEditText) {
        val calendarConstraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.before(System.currentTimeMillis()))
            .build()
        val builder =
            MaterialDatePicker.Builder.datePicker().setCalendarConstraints(calendarConstraints)

        val datePicker = builder.setTitleText("Select From Date").build()
        datePicker.addOnPositiveButtonClickListener {
            // Handle the selected date
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = dateFormat.format(it)!!
            editText.setText(date)
            if (binding.edtToDatePCAInvoiceFragment.text.toString().isNotEmpty()){
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
            datePicker.show(childFragmentManager, datePicker.toString())
        }
    }

    private fun showToDatePickerDialog(editText: TextInputEditText) {
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
        if (!datePicker.isAdded) {
            datePicker.show(childFragmentManager, datePicker.toString())
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
            shopNameAdapterList.add(0,"ALL")
            val adapter = commonUIUtility.getCustomArrayAdapter(shopNameAdapterList)
            binding.mactTotalBagsPCAInvoiceFragment.setAdapter(adapter)

            binding.mactTotalBagsPCAInvoiceFragment.setOnItemClickListener { parent, _, position, _ ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                if (selectedItem.equals("ALL"))
                {
                    for (i in 1 until shopNameAdapterList.size)
                    {
                        addChip(parent.getItemAtPosition(i).toString())
                        selectedChipList.add(parent.getItemAtPosition(i).toString())
                    }
                    binding.mactTotalBagsPCAInvoiceFragment.setText("")
                }else
                {
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

    override fun getSelectedInvoiceData(selectedInvoiceList: ArrayList<Shopwise>,parentPosition:Int) {
        var sum = 0.0
        var shop_bags = 0.0
        var shop_avg_rate = 0.0
        var shop_total_amount = 0.0
        var commodityBhartiPrice = 0.0
        for (shop in selectedInvoiceList){
            commodityBhartiPrice = shop.CommodityBhartiPrice.toDouble()
            for (entry in shop.ShopEntries) {
                if (entry.isSelected) {
                    shop_bags += entry.Bags.toDouble()
                    shop_total_amount  += entry.Amount.toDouble()
                }
            }
        }
        shop_avg_rate = shop_total_amount / ((shop_bags * commodityBhartiPrice)/20.0)


        val naNChecker : Double = shop_avg_rate.let { if (it.isNaN()) 0.0 else it }
        var fr_shop_bags = DecimalFormat("0.00").format(shop_bags)
        var fr_shop_avg_rate = DecimalFormat("0.00").format(naNChecker)
        var fr_shop_total_amount = DecimalFormat("0.00").format(shop_total_amount)

        if (fr_shop_bags.contains("."))
        {
            val decimalValue = fr_shop_bags.split(".")[1].toDouble()
            if (decimalValue>0.0)
            {
                fr_shop_bags = DecimalFormat("0.00").format(shop_bags)
            }else
            {
                fr_shop_bags = DecimalFormat("0.00").format(shop_bags).split(".")[0]
            }
        }

        var formattedCurrentPrice =
            NumberFormat.getCurrencyInstance().format(fr_shop_avg_rate.toDouble()).substring(1)
        if (formattedCurrentPrice.contains("."))
        {
            val decimalValue = formattedCurrentPrice.split(".")[1].toDouble()
            if (decimalValue>0.0)
            {
                formattedCurrentPrice = NumberFormat.getCurrencyInstance().format(fr_shop_avg_rate.toDouble()).substring(1)
            }else
            {
                formattedCurrentPrice = NumberFormat.getCurrencyInstance().format(fr_shop_avg_rate.toDouble()).substring(1).split(".")[0]
            }
        }

        var formattedAmount =
            NumberFormat.getCurrencyInstance().format(fr_shop_total_amount.toDouble()).substring(1)
        if (formattedAmount.contains("."))
        {
            val decimalValue = formattedAmount.split(".")[1].toDouble()
            if (decimalValue>0.0)
            {
                formattedAmount = NumberFormat.getCurrencyInstance().format(fr_shop_total_amount.toDouble()).substring(1)
            }else
            {
                formattedAmount = NumberFormat.getCurrencyInstance().format(fr_shop_total_amount.toDouble()).substring(1).split(".")[0]
            }
        }

        if (shop_bags>0)
        {
            binding.llGrandTotalPCAInvoiceFragment.visibility = View.VISIBLE
            binding.tvTotBagPCAInvoiceFragment.setText(fr_shop_bags)
            binding.tvTotAvgPricePCAInvoiceFragment.setText(formattedCurrentPrice)
            binding.tvTotalAmountPCAInvoiceFragment.setText(formattedAmount)
        }else
        {
            binding.llGrandTotalPCAInvoiceFragment.visibility = View.GONE
        }

    }

    override fun onSaveButtonClick(dataList: ArrayList<Shopwise>) {
        try {
//            Log.d(TAG, "onSaveButtonClick: SELECTED_SHOP_LIST : $dataList")
            if (checkIfAllUsersInactive(dataList))
            {
                navController.navigate(PCAInvoiceFragmentDirections.actionPCAInvoiceFragmentToPCAInvoiceDetailFragment())
            }else
            {
                commonUIUtility.showToast("Please Select at least 1 Entry!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "onSaveButtonClick: ${e.message}", )
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

    private fun resetUI()
    {
        binding.llGrandTotalPCAInvoiceFragment.visibility = View.GONE
        binding.ChipGroupPCAInvoiceFragment.removeAllViews()
        binding.rcViewPCAInvoiceFragment.visibility = View.GONE
    }

}

interface InvoiceSelectedDataCallBack{
    fun getSelectedInvoiceData(selectedInvoiceList:ArrayList<Shopwise>,parentPosition: Int)

    fun onSaveButtonClick(dataList:ArrayList<Shopwise>)
}