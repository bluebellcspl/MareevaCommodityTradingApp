package com.bluebellcspl.maarevacommoditytradingapp.fragment.pca

import ConnectionCheck
import android.app.AlertDialog
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
import com.bluebellcspl.maarevacommoditytradingapp.constants.Constants
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentPCAInvoiceBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchInvoiceDataAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.AuctionDetailsModel
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceDataModel
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionPCAListModel
import com.bluebellcspl.maarevacommoditytradingapp.model.Shopwise
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.RecyclerViewHelper
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.util.Locale


class PCAInvoiceFragment : Fragment(),RecyclerViewHelper {
    lateinit var binding: FragmentPCAInvoiceBinding
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val TAG = "PCAInvoiceFragment"
    private val navController: NavController by lazy { findNavController() }
    lateinit var alertDialog: AlertDialog
    lateinit var adapter: InvoiceAdapter
    lateinit var shopWiseList: ArrayList<Shopwise>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_p_c_a_invoice, container, false)
        setOnClickListeners()
//        bindInvoiceRCView()

        if (ConnectionCheck.isConnected(requireContext())) {
            FetchInvoiceDataAPI(requireContext(), this, "", "")
        }

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
            })
            binding.rcViewPCAInvoiceFragment.adapter = adapter
            binding.rcViewPCAInvoiceFragment.invalidate()
        } catch (e: Exception) {
            Log.e(TAG, "bindInvoiceRCView: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun showFromDatePickerDialog(editText: TextInputEditText) {
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


        }
        if (!datePicker.isAdded) {
            datePicker.show(childFragmentManager, datePicker.toString())
        }
    }

    fun bindFilterForRecyclerview(invoiceModel: InvoiceDataModel) {
        try {
            shopWiseList = invoiceModel.ShopwiseList
            val stringArray = ArrayList<String>()

            for (i in shopWiseList) {
                Log.d(
                    TAG,
                    "bindFilterForRecyclerview: DATA_HAS_SHOP : ${i.ShopNo}-${i.ShopShortName}"
                )
                val customShopName = "${i.ShopNo}-${i.ShopShortName}"
                if (!stringArray.contains(customShopName)) {
                    stringArray.add("${i.ShopNo}-${i.ShopShortName}")
                }
            }
            stringArray.add(0,"ALL")
            val adapter = commonUIUtility.getCustomArrayAdapter(stringArray)
            binding.mactTotalBagsPCAInvoiceFragment.setAdapter(adapter)

            binding.mactTotalBagsPCAInvoiceFragment.setOnItemClickListener { parent, _, position, _ ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                if (selectedItem.equals("ALL"))
                {
                    for (i in 1 until stringArray.size)
                    {
                        addChip(parent.getItemAtPosition(i).toString())
                    }
                    binding.mactTotalBagsPCAInvoiceFragment.setText("")
                }else
                {
                    addChip(selectedItem)
                    binding.mactTotalBagsPCAInvoiceFragment.setText("")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindFilterForRecyclerview: ${e.message}")
        }
    }

    override fun onItemClick(postion: Int, onclickType: String) {
        TODO("Not yet implemented")
    }

    override fun onBuyerAuctionPCAItemClick(postion: Int, model: AuctionDetailsModel) {
        TODO("Not yet implemented")
    }

    override fun getBuyerAuctionDataList(dataList: ArrayList<AuctionDetailsModel>) {
        TODO("Not yet implemented")
    }

    override fun getLiveAuctionPCAData(postion: Int, model: LiveAuctionPCAListModel) {
        TODO("Not yet implemented")
    }
}