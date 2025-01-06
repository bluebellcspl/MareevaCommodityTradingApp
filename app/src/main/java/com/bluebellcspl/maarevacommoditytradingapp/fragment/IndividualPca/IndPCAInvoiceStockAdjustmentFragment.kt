package com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.IndPCAStockAdjustmentAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentIndPCAInvoiceStockAdjustmentBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.InvoiceStockPopupFinalBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.InvoiceStockDetailFragmentDirections
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoiceBagAdjustmentModel
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoiceStockModelItem
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAStockInsertItemModel
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceEntryMergedModelItem
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoicePreviewFetchDataModel
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceStockModelItem
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.util.regex.Pattern

class IndPCAInvoiceStockAdjustmentFragment : Fragment(),IndPCAInvoiceAdjustmentHelper {
    var _binding: FragmentIndPCAInvoiceStockAdjustmentBinding? = null
    val binding get() = _binding!!
    val TAG = "IndPCAInvoiceStockAdjustmentFragment"
    private val commonUIUtility: CommonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val navController: NavController by lazy { findNavController() }
    private val args by navArgs<IndPCAInvoiceStockAdjustmentFragmentArgs>()
    var _InvoiceSeletedList :ArrayList<IndPCAInvoiceStockModelItem> ? =null
//    private val _AdjStockList by lazy { ArrayList<IndPCAInvoiceBagAdjustmentModel>() }
    private var _AdjStockList:ArrayList<IndPCAInvoiceBagAdjustmentModel> = ArrayList()
    lateinit var adapter : IndPCAStockAdjustmentAdapter
    var BUYER_ID = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_ind_p_c_a_invoice_stock_adjustment,
            container,
            false
        )
        _InvoiceSeletedList = ArrayList(args.invoiceSeletedList.toList())
        bindRcViewStock(_InvoiceSeletedList!!)
        binding.btnNextIndPCAInvoiceStockAdjustmentFragment.setOnClickListener {
            _AdjStockList.clear()
            _InvoiceSeletedList!!.forEach {stock->
                Log.d("????", "onCreateView: DATE = : ${stock.Date}")
                Log.d("????", "onCreateView: BAGS = : ${stock.UsedBillBags}")
                Log.d("????", "onCreateView: AMOUNT = : ${stock.UsedBillAmount}")
                Log.d("????", "onCreateView: WEIGHT = : ${stock.UsedBillWeight}")
                Log.d(TAG, "onCreateView: STOCK_ITEM : $stock")
                Log.d("????", "onCreateView: =======================================================================")
                val model = IndPCAInvoiceBagAdjustmentModel(
                            stock.UsedBillAmount,
                            stock.UsedBillApproxKg,
                            stock.UsedBillBags,
                            stock.UsedBillGST,
                            stock.UsedBillKg,
                            stock.UsedBillRate,
                            stock.UsedBillTotalAmount,
                            stock.UsedBillWeight,
                            stock.BuyerId,
                            stock.BuyerName,
                            stock.CommodityBhartiPrice,
                            stock.CommodityId,
                    stock.CommodityName,
                    DateUtility().getyyyyMMddDateTime(),
                            stock.CreateUser,
                    DateUtility().formatToyyyyMMdd(stock.Date),
                            stock.TotalPct,
                    "",
                            stock.InStockId,
                            ""+stock.InPCAAuctionDetailId,
                            "",
                            ""+PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,""),
                    ""+PrefUtil.getString(PrefUtil.KEY_IND_PCA_ID,""),
                    DateUtility().getyyyyMMddDateTime(),
                    PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString(),
                )

                _AdjStockList.add(model)
            }
            shopFinalPopup()
        }
        return binding.root
    }

    private fun bindRcViewStock(dataList:ArrayList<IndPCAInvoiceStockModelItem>){
        if (dataList.isNotEmpty())
        {
            adapter = IndPCAStockAdjustmentAdapter(requireContext(),dataList,this@IndPCAInvoiceStockAdjustmentFragment)
            binding.rcViewIndPCAInvoiceStockAdjustmentFragment.adapter = adapter
            binding.rcViewIndPCAInvoiceStockAdjustmentFragment.invalidate()
            binding.btnNextIndPCAInvoiceStockAdjustmentFragment.visibility = View.VISIBLE
        }else{
            binding.rcViewIndPCAInvoiceStockAdjustmentFragment.adapter = null
            binding.rcViewIndPCAInvoiceStockAdjustmentFragment.invalidate()
        }
    }

    private fun calculateTotalData(dataList: ArrayList<IndPCAInvoiceStockModelItem>){
        try {
            var bag = 0.0
            var total = 0.0
            var gst = 0.0
            var rate = 0.0
            var weight = 0.0
            var commodityName = ""
            var buyerName = ""
            for(model in dataList){
                bag += model.UsedBillBags.toDouble()
                total += model.UsedBillAmount.toDouble()
                weight += model.UsedBillWeight.toDouble()
                commodityName = model.CommodityName
                buyerName = model.BuyerName
                BUYER_ID = model.BuyerId
            }
            rate = total/(weight / 20.0)

            binding.tvBagsIndPCAInvoiceStockAdjustmentFragment.text = commonUIUtility.formatDecimal(bag)
            binding.tvWeightIndPCAInvoiceStockAdjustmentFragment.text = commonUIUtility.formatDecimal(weight)
            if (rate>0.0){

                binding.tvRateIndPCAInvoiceStockAdjustmentFragment.text = commonUIUtility.numberCurrencyFormat(rate)
            }else{
                binding.tvRateIndPCAInvoiceStockAdjustmentFragment.setText("")
            }
            binding.tvAmountIndPCAInvoiceStockAdjustmentFragment.text = commonUIUtility.numberCurrencyFormat(total)
            binding.tvCommodityIndPCAInvoiceStockAdjustmentFragment.setText(commodityName)
            binding.tvBuyerNameIndPCAInvoiceStockAdjustmentFragment.setText(buyerName)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "calculateTotalData: ${e.message}")
        }
    }

    private fun shopFinalPopup()
    {
        try {
            val alertDailogBuilder = AlertDialog.Builder(requireContext())
            val dialogBinding = InvoiceStockPopupFinalBinding.inflate(layoutInflater)
            val dialogView = dialogBinding.root
            alertDailogBuilder.setView(dialogView)
            val alertDialog = alertDailogBuilder.create()
            alertDialog.setCanceledOnTouchOutside(true)
            alertDialog.setCancelable(true)
            alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()

            dialogBinding.actGSTInvoiceStockPopup.inputType = InputType.TYPE_NULL
            var taxAdapter = commonUIUtility.setCustomArrayAdapter(requireActivity().resources.getStringArray(R.array.tax_Array))
            dialogBinding.actGSTInvoiceStockPopup.setAdapter(taxAdapter)
            var isValidRTO = false
            var gstStatus:Boolean? = null
            dialogBinding.edtVehicleNoInvoiceStockPopup.addTextChangedListener(object :
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

                    val newString = formattedString.toString().toUpperCase()

                    // Calculate the new cursor position
                    var newCursorPosition = cursorPosition
                    if (deleting && cursorPosition > 0 && newCursorPosition > 0 && newString[cursorPosition - 1] == ' ') {
                        newCursorPosition--
                    }

                    dialogBinding.edtVehicleNoInvoiceStockPopup.setText(newString)
                    if (newCursorPosition > newString.length) newCursorPosition = newString.length
                    if (beforeLength == cursorPosition) newCursorPosition = newString.length
                    if (newCursorPosition < 0) newCursorPosition = 0
                    dialogBinding.edtVehicleNoInvoiceStockPopup.setSelection(newCursorPosition)

                    Log.d(TAG, "afterTextChanged: =================================== END")
                    Log.d(TAG, "afterTextChanged: BEFORE_LEN : $beforeLength")
                    Log.d(TAG, "afterTextChanged: CURSOR_POS : $cursorPosition")
                    Log.d(TAG, "afterTextChanged: NEW_STR : $newString")

                    isFormatting = false

                    // Validate the formatted RTO number
                    if (isValidRtoNumber(s!!.toString())) {
                        dialogBinding.edtVehicleNoContainerInvoiceStockPopup.boxStrokeColor = requireContext().getColor(R.color.newButtonColor)
                        isValidRTO = true
                    } else {
                        dialogBinding.edtVehicleNoContainerInvoiceStockPopup.boxStrokeColor = requireContext().getColor(R.color.unReadChatBadge)
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

            dialogBinding.actGSTInvoiceStockPopup.setOnItemClickListener { adapter, view, position, id ->
                if (position == 1)
                {
                    gstStatus = false
                }else
                {
                    gstStatus = true
                }
            }


            dialogBinding.btnNextInvoiceStockPopup.setOnClickListener {

                if (dialogBinding.actGSTInvoiceStockPopup.text.toString().isEmpty()){
                    commonUIUtility.showToast(getString(R.string.please_select_gst_alert_msg))
                    return@setOnClickListener
                }else
                {
                    if (dialogBinding.edtVehicleNoInvoiceStockPopup.text.toString().isNotEmpty()){
                        if (!isValidRTO){
                            commonUIUtility.showToast(getString(R.string.please_enter_valid_rto_number_alert_msg))
                            return@setOnClickListener
                        }
                    }
                    val rtoNumber = dialogBinding.edtVehicleNoInvoiceStockPopup.text.toString()

//                    navController.navigate(InvoiceStockDetailFragmentDirections.actionInvoiceStockDetailFragmentToInvoicePreviewFragment(_InvoiceStockList!!.toTypedArray(),gstStatus!!,rtoNumber))
                    val invoiceFetchDataModel = InvoicePreviewFetchDataModel(
                        BUYER_ID,
                        PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"").toString(),
                        PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,"").toString(),
                        PrefUtil.getSystemLanguage().toString(),
                        PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString(),
                        rtoNumber
                    )

                    navController.navigate(IndPCAInvoiceStockAdjustmentFragmentDirections.actionIndPCAInvoiceStockAdjustmentFragmentToIndPCAInvoicePreviewFragment(invoiceFetchDataModel,_AdjStockList.toTypedArray(),gstStatus!!))
                }
                alertDialog.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "shopFinalPopup: ${e.message}", )
        }
    }

    private fun isValidRtoNumber(rtoNumber: String): Boolean {
        val rtoPattern = "^[A-Z]{2} [0-9]{2} [A-Z]{2} [0-9]{4}$"
        return Pattern.compile(rtoPattern).matcher(rtoNumber).matches()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getCalculatedData(dataList: Any) {
        if (dataList is ArrayList<*>){
            calculateTotalData(dataList as ArrayList<IndPCAInvoiceStockModelItem>)
        }
    }
}

interface IndPCAInvoiceAdjustmentHelper{
    fun getCalculatedData(dataList:Any)
}