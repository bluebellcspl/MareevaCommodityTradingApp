package com.bluebellcspl.maarevacommoditytradingapp.fragment.pca

import ConnectionCheck
import android.app.AlertDialog
import android.icu.text.NumberFormat
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.InvoicePreviewAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.EditableDecimalInputFilter
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentInvoicePreviewBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.InvoicePreviewCommissionPopupBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchInvoicePreviewAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.POSTInvoiceStockList
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoicePreviewModel
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceStockModelItem


class InvoicePreviewFragment : Fragment(), InvoiceStockDetailHelper {
    private var _binding: FragmentInvoicePreviewBinding? = null
    val binding get() = _binding!!
    val TAG = "InvoicePreviewFragment"
    private val args: InvoicePreviewFragmentArgs by navArgs()
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private lateinit var adapter: InvoicePreviewAdapter
    var _InvoiceStockList: ArrayList<InvoiceStockModelItem>? = null
    private var GST_STATUS: Boolean? = null
    private var CGST_AMOUNT: Double? = null
    private var SGST_AMOUNT: Double? = null
    private var BASIC_AMOUNT: Double? = null
    private var USED_BAGS: Double? = null
    private var TOTAL_GST: Double? = null
    private var VEHICLE_NO: String? = null
    private var _invoicePreviewModel: InvoicePreviewModel? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_invoice_preview, container, false)
        _InvoiceStockList = ArrayList(args.invoiceStocKList.toList())
        GST_STATUS = args.gstStatus
        VEHICLE_NO = args.vehicleNo
        if (ConnectionCheck.isConnected(requireContext())) {
            FetchInvoicePreviewAPI(requireContext(), this)
        }
        bindRcView(_InvoiceStockList!!)
        setOnClickListener()
        return binding.root
    }

    private fun setOnClickListener() {
        try {
            binding.btnSubmitNDownloadInvoicePreviewFragment.setOnClickListener {
                var isHSNCode = true
                for (model in _InvoiceStockList!!) {
                    if (model.HSNCode.isEmpty()) {
                        isHSNCode = false
                        break
                    }
                }
                if (isHSNCode) {
                    if (ConnectionCheck.isConnected(requireContext()))
                    {
                        POSTInvoiceStockList(requireContext(), this@InvoicePreviewFragment,_InvoiceStockList!!)
                    }
                } else {
                    commonUIUtility.showToast("Please Enter HSN Code For All Items!")
                }
            }

            binding.cvTotalCalculationInvoicePreviewFragment.setOnClickListener {
                showExpensePopup()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "setOnClickListener: ${e.message}")
        }
    }

    fun populateData(invoicePreviewModel: InvoicePreviewModel) {
        try {
            _invoicePreviewModel = invoicePreviewModel
            //PCA Data
            binding.tvPCATitleInvoicePreviewFragment.setText(invoicePreviewModel.PCAName)
            binding.tvPANNoInvoicePreviewFragment.setText(invoicePreviewModel.PCAPanNo)
            binding.tvGSTINInvoicePreviewFragment.setText(invoicePreviewModel.PCAGSTNo)
            binding.tvStateInvoicePreviewFragment.setText(invoicePreviewModel.StateName)
            binding.tvMobileNoInvoicePreviewFragment.setText(invoicePreviewModel.PCAMobileNo)

            //Buyer Data
            binding.edtBuyerNameInvoicePreviewFragment.setText(invoicePreviewModel.BuyerName)
            binding.edtBuyerAddressInvoicePreviewFragment.setText(invoicePreviewModel.BuyerAddress)
            binding.edtBuyerGSTINInvoicePreviewFragment.setText(invoicePreviewModel.BuyerGSTNo)
            binding.edtBuyerPANInvoicePreviewFragment.setText(invoicePreviewModel.BuyerPanNo)
            binding.edtBuyerCityInvoicePreviewFragment.setText(invoicePreviewModel.BuyerCity)
            binding.edtInvoiceDateInvoicePreviewFragment.setText(invoicePreviewModel.InvoiceDate)
            binding.edtTransportVehicleInvoicePreviewFragment.setText(VEHICLE_NO)

            //Commission Data
            binding.edtGCACommInvoicePreviewFragment.setText(invoicePreviewModel.PCAgcaCommissiom)
            binding.edtPCACommInvoicePreviewFragment.setText(invoicePreviewModel.PCApcaCommissiom)
            binding.edtMarketFeesInvoicePreviewFragment.setText(invoicePreviewModel.MarketCess)
            binding.edtLabourInvoicePreviewFragment.setText(invoicePreviewModel.LabourCharges)
            binding.edtTransportInvoicePreviewFragment.setText(invoicePreviewModel.PerBoriRate)
            var PCAComm = invoicePreviewModel.PCApcaCommissiom.toDouble()
            var GCAComm = invoicePreviewModel.PCAgcaCommissiom.toDouble()
            var MarketCess = invoicePreviewModel.MarketCess.toDouble()
            var Transport = invoicePreviewModel.PerBoriRate.toDouble()
            var Labour = invoicePreviewModel.LabourCharges.toDouble()

            //GST Data
            if (GST_STATUS!!) {
                TOTAL_GST = invoicePreviewModel.GSTTotalPct.toDouble()
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
                basicAmount += model.UsedBagAmount.toDouble()
                usedBags += model.UsedBags.toDouble()
            }
            BASIC_AMOUNT = basicAmount
            USED_BAGS = usedBags
            val formattedAmount =
                NumberFormat.getCurrencyInstance().format(basicAmount).substring(1)
            binding.edtBasicAmountInvoicePreviewFragment.setText(formattedAmount)
            val PCACommission = (basicAmount * PCAComm) / 100.00
            val GCACommission = (basicAmount * GCAComm) / 100.00
            val MarketFees = (basicAmount * MarketCess) / 100.00
            val TransportFees = usedBags * Transport
            val LabourFees = usedBags * Labour
            val formattedBags = NumberFormat.getCurrencyInstance().format(usedBags).substring(1)
            binding.edtBagsInvoicePreviewFragment.setText(formattedBags)

            var totalAmount =
                basicAmount + PCACommission + GCACommission + MarketFees + TransportFees + LabourFees
            var GSTAmount = (totalAmount * TOTAL_GST!!) / 100.00
            var totalAmountWithGST = totalAmount + GSTAmount

            val formattedGSTAmount =
                NumberFormat.getCurrencyInstance().format(GSTAmount).substring(1)
            binding.edtGSTInvoicePreviewFragment.setText(formattedGSTAmount)

            val formattedTotalAmount =
                NumberFormat.getCurrencyInstance().format(totalAmountWithGST).substring(1)
            binding.edtTotalAmountInvoicePreviewFragment.setText(formattedTotalAmount)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "populateData: ${e.message}")
        }
    }

    fun bindRcView(dataList: ArrayList<InvoiceStockModelItem>) {
        try {
            dataList.forEach { model ->
                Log.d(TAG, "bindRcView: HSN_CODE : ${model.HSNCode}")
            }
            if (dataList.isNotEmpty()) {
                adapter = InvoicePreviewAdapter(requireContext(), dataList, this)
                binding.rcViewInvoicePreviewFragment.adapter = adapter
                binding.rcViewInvoicePreviewFragment.setHasFixedSize(true)
                binding.rcViewInvoicePreviewFragment.setItemViewCacheSize(20)
                binding.rcViewInvoicePreviewFragment.invalidate()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindRcView: ${e.message}")
        }
    }

    fun showExpensePopup() {
        try {
            val alertDailogBuilder = AlertDialog.Builder(requireContext())
            val dialogBinding = InvoicePreviewCommissionPopupBinding.inflate(layoutInflater)
            val dialogView = dialogBinding.root
            alertDailogBuilder.setView(dialogView)
            val alertDialog = alertDailogBuilder.create()
            alertDialog.setCanceledOnTouchOutside(true)
            alertDialog.setCancelable(true)
            alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()

            //Populating Data In Popup
            dialogBinding.edtGCACommInvoicePreviewPopup.filters =
                arrayOf<InputFilter>(EditableDecimalInputFilter(5, 2))
            dialogBinding.edtPCACommInvoicePreviewPopup.filters =
                arrayOf<InputFilter>(EditableDecimalInputFilter(5, 2))
            dialogBinding.edtMarketFeesInvoicePreviewPopup.filters =
                arrayOf<InputFilter>(EditableDecimalInputFilter(5, 2))
            dialogBinding.edtLabourInvoicePreviewPopup.filters =
                arrayOf<InputFilter>(EditableDecimalInputFilter(5, 2))
            dialogBinding.edtTransportInvoicePreviewPopup.filters =
                arrayOf<InputFilter>(EditableDecimalInputFilter(5, 2))

            var pcaCommAmount =0.0
            var gcaCommAmount =0.0
            var marketFeesAmount =0.0
            var labourAmount =0.0
            var transportAmount =0.0

            dialogBinding.edtGCACommInvoicePreviewPopup.addTextChangedListener(object : TextWatcher {
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
                            .startsWith("0") && !dialogBinding.edtGCACommInvoicePreviewPopup.text.toString().contains("0.")
                    ) {

                        val subStr =
                            dialogBinding.edtGCACommInvoicePreviewPopup.text.toString().substring(1)
                        dialogBinding.edtGCACommInvoicePreviewPopup.setText(subStr)
                        dialogBinding.edtGCACommInvoicePreviewPopup.setSelection(subStr.length)

                    }
                else
                    {
                        var percentage = dialogBinding.edtGCACommInvoicePreviewPopup.text.toString().toDouble()
                        Log.d(TAG, "afterTextChanged: EDT_PERCENT : $percentage")
                        if (percentage > 100) {
                            dialogBinding.edtGCACommInvoicePreviewPopup.setText("100")
                            dialogBinding.edtGCACommInvoicePreviewPopup.setSelection(dialogBinding.edtGCACommInvoicePreviewPopup.text.toString().length)
                        }
                        gcaCommAmount = BASIC_AMOUNT!! * percentage / 100.00
                        val formattedAmount =
                            NumberFormat.getCurrencyInstance().format(gcaCommAmount).substring(1)
                        dialogBinding.tvGCACommAmountInvoicePreviewPopup.setText(formattedAmount)

                        var totalAmount = BASIC_AMOUNT!! + pcaCommAmount + gcaCommAmount + marketFeesAmount + labourAmount + transportAmount
                        var GSTAmount = (totalAmount * TOTAL_GST!!) / 100.00
                        var totalAmountWithGST = totalAmount + GSTAmount

                        val formattedGSTAmount =
                            NumberFormat.getCurrencyInstance().format(GSTAmount).substring(1)
                        dialogBinding.edtGSTAmountInvoicePreviewPopup.setText(formattedGSTAmount)

                        val formattedTotalAmount =
                            NumberFormat.getCurrencyInstance().format(totalAmountWithGST).substring(1)
                        dialogBinding.edtTotalAmountInvoicePreviewPopup.setText(formattedTotalAmount)
                    }

                }
            })
            dialogBinding.edtPCACommInvoicePreviewPopup.addTextChangedListener(object : TextWatcher {
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
                        pcaCommAmount=0.0
                        dialogBinding.tvPCACommAmountInvoicePreviewPopup.setText("0")
                    }
                    if (dialogBinding.edtPCACommInvoicePreviewPopup.text.toString().length >= 2 && dialogBinding.edtPCACommInvoicePreviewPopup.text.toString()
                            .startsWith("0") && !dialogBinding.edtPCACommInvoicePreviewPopup.text.toString().contains("0.")
                    ) {
                        val subStr =
                            dialogBinding.edtPCACommInvoicePreviewPopup.text.toString().substring(1)
                        dialogBinding.edtPCACommInvoicePreviewPopup.setText(subStr)
                        dialogBinding.edtPCACommInvoicePreviewPopup.setSelection(subStr.length)
                    }else
                    {
                        var percentage = dialogBinding.edtPCACommInvoicePreviewPopup.text.toString().toDouble()
                        if (percentage > 100) {
                            dialogBinding.edtPCACommInvoicePreviewPopup.setText("100")
                            dialogBinding.edtPCACommInvoicePreviewPopup.setSelection(s.toString().length)
                        }
                        pcaCommAmount = BASIC_AMOUNT!! * percentage / 100.00
                        val formattedAmount =
                            NumberFormat.getCurrencyInstance().format(pcaCommAmount).substring(1)
                        dialogBinding.tvPCACommAmountInvoicePreviewPopup.setText(formattedAmount)

                        var totalAmount = BASIC_AMOUNT!! + pcaCommAmount + gcaCommAmount + marketFeesAmount + labourAmount + transportAmount
                        var GSTAmount = (totalAmount * TOTAL_GST!!) / 100.00
                        var totalAmountWithGST = totalAmount + GSTAmount

                        val formattedGSTAmount =
                            NumberFormat.getCurrencyInstance().format(GSTAmount).substring(1)
                        dialogBinding.edtGSTAmountInvoicePreviewPopup.setText(formattedGSTAmount)

                        val formattedTotalAmount =
                            NumberFormat.getCurrencyInstance().format(totalAmountWithGST).substring(1)
                        dialogBinding.edtTotalAmountInvoicePreviewPopup.setText(formattedTotalAmount)
                    }

                }
            })
            dialogBinding.edtMarketFeesInvoicePreviewPopup.addTextChangedListener(object : TextWatcher {
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
                            .startsWith("0") && !dialogBinding.edtMarketFeesInvoicePreviewPopup.text.toString().contains("0.")
                    )
                    {
                        val subStr =
                            dialogBinding.edtMarketFeesInvoicePreviewPopup.text.toString().substring(1)
                        dialogBinding.edtMarketFeesInvoicePreviewPopup.setText(subStr)
                        dialogBinding.edtMarketFeesInvoicePreviewPopup.setSelection(1)
                    }else
                    {
                        var percentage = dialogBinding.edtMarketFeesInvoicePreviewPopup.text.toString().toDouble()
                        if (percentage > 100) {

                            dialogBinding.edtMarketFeesInvoicePreviewPopup.setText("100")
                            dialogBinding.edtMarketFeesInvoicePreviewPopup.setSelection(dialogBinding.edtMarketFeesInvoicePreviewPopup.text.toString().length)
                        }
                        marketFeesAmount = BASIC_AMOUNT!! * percentage / 100.00
                        val formattedAmount =
                            NumberFormat.getCurrencyInstance().format(marketFeesAmount).substring(1)
                        dialogBinding.tvMarketFeesAmountInvoicePreviewPopup.setText(formattedAmount)

                        var totalAmount = BASIC_AMOUNT!! + pcaCommAmount + gcaCommAmount + marketFeesAmount + labourAmount + transportAmount
                        var GSTAmount = (totalAmount * TOTAL_GST!!) / 100.00
                        var totalAmountWithGST = totalAmount + GSTAmount

                        val formattedGSTAmount =
                            NumberFormat.getCurrencyInstance().format(GSTAmount).substring(1)
                        dialogBinding.edtGSTAmountInvoicePreviewPopup.setText(formattedGSTAmount)

                        val formattedTotalAmount =
                            NumberFormat.getCurrencyInstance().format(totalAmountWithGST).substring(1)
                        dialogBinding.edtTotalAmountInvoicePreviewPopup.setText(formattedTotalAmount)
                    }

                }
            })
            dialogBinding.edtLabourInvoicePreviewPopup.addTextChangedListener(object : TextWatcher {
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
                            .startsWith("0") && !dialogBinding.edtLabourInvoicePreviewPopup.text.toString().contains("0.")
                    ) {
                        val subStr =
                            dialogBinding.edtLabourInvoicePreviewPopup.text.toString().substring(1)
                        dialogBinding.edtLabourInvoicePreviewPopup.setText(subStr)
                        dialogBinding.edtLabourInvoicePreviewPopup.setSelection(1)
                    }
                    else{
                        var percentage = dialogBinding.edtLabourInvoicePreviewPopup.text.toString().toDouble()
                        labourAmount = USED_BAGS!! * percentage
                        val formattedAmount =
                            NumberFormat.getCurrencyInstance().format(labourAmount).substring(1)
                        dialogBinding.tvLabourAmountInvoicePreviewPopup.setText(formattedAmount)

                        var totalAmount = BASIC_AMOUNT!! + pcaCommAmount + gcaCommAmount + marketFeesAmount + labourAmount + transportAmount
                        var GSTAmount = (totalAmount * TOTAL_GST!!) / 100.00
                        var totalAmountWithGST = totalAmount + GSTAmount

                        val formattedGSTAmount =
                            NumberFormat.getCurrencyInstance().format(GSTAmount).substring(1)
                        dialogBinding.edtGSTAmountInvoicePreviewPopup.setText(formattedGSTAmount)

                        val formattedTotalAmount =
                            NumberFormat.getCurrencyInstance().format(totalAmountWithGST).substring(1)
                        dialogBinding.edtTotalAmountInvoicePreviewPopup.setText(formattedTotalAmount)
                    }

                }
            })
            dialogBinding.edtTransportInvoicePreviewPopup.addTextChangedListener(object : TextWatcher {
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
                            .startsWith("0") && !dialogBinding.edtTransportInvoicePreviewPopup.text.toString().contains("0.")
                    ) {
                        val subStr =
                            dialogBinding.edtTransportInvoicePreviewPopup.text.toString().substring(1)
                        dialogBinding.edtTransportInvoicePreviewPopup.setText(subStr)
                        dialogBinding.edtTransportInvoicePreviewPopup.setSelection(1)
                    }
                    else
                    {
                        var percentage = dialogBinding.edtTransportInvoicePreviewPopup.text.toString().toDouble()
                        transportAmount = USED_BAGS!! * percentage
                        val formattedAmount =
                            NumberFormat.getCurrencyInstance().format(transportAmount).substring(1)
                        dialogBinding.tvTransportAmountInvoicePreviewPopup.setText(formattedAmount)

                        var totalAmount = BASIC_AMOUNT!! + pcaCommAmount + gcaCommAmount + marketFeesAmount + labourAmount + transportAmount
                        var GSTAmount = (totalAmount * TOTAL_GST!!) / 100.00
                        var totalAmountWithGST = totalAmount + GSTAmount

                        val formattedGSTAmount =
                            NumberFormat.getCurrencyInstance().format(GSTAmount).substring(1)
                        dialogBinding.edtGSTAmountInvoicePreviewPopup.setText(formattedGSTAmount)

                        val formattedTotalAmount =
                            NumberFormat.getCurrencyInstance().format(totalAmountWithGST).substring(1)
                        dialogBinding.edtTotalAmountInvoicePreviewPopup.setText(formattedTotalAmount)
                    }
                }
            })

            dialogBinding.edtGCACommInvoicePreviewPopup.setText(_invoicePreviewModel!!.PCAgcaCommissiom)
            dialogBinding.edtPCACommInvoicePreviewPopup.setText(_invoicePreviewModel!!.PCApcaCommissiom)
            dialogBinding.edtMarketFeesInvoicePreviewPopup.setText(_invoicePreviewModel!!.MarketCess)
            dialogBinding.edtLabourInvoicePreviewPopup.setText(_invoicePreviewModel!!.LabourCharges)
            dialogBinding.edtTransportInvoicePreviewPopup.setText(_invoicePreviewModel!!.PerBoriRate)

            var gstPCString = StringBuilder(dialogBinding.tvGSTTotalPercentageInvoicePreviewPopup.text.toString())
            gstPCString.append("$TOTAL_GST%")
            dialogBinding.tvGSTTotalPercentageInvoicePreviewPopup.setText(gstPCString.toString())

            val formattedAmount =
                NumberFormat.getCurrencyInstance().format(BASIC_AMOUNT).substring(1)
            dialogBinding.edtBasicAmountInvoicePreviewPopup.setText(formattedAmount)

            dialogBinding.btnSaveInvoicePreviewPopup.setOnClickListener {
                binding.edtGCACommInvoicePreviewFragment.setText(dialogBinding.edtGCACommInvoicePreviewPopup.text.toString())
                binding.edtPCACommInvoicePreviewFragment.setText(dialogBinding.edtPCACommInvoicePreviewPopup.text.toString())
                binding.edtMarketFeesInvoicePreviewFragment.setText(dialogBinding.edtMarketFeesInvoicePreviewPopup.text.toString())
                binding.edtLabourInvoicePreviewFragment.setText(dialogBinding.edtLabourInvoicePreviewPopup.text.toString())
                binding.edtTransportInvoicePreviewFragment.setText(dialogBinding.edtTransportInvoicePreviewPopup.text.toString())
                binding.edtGSTInvoicePreviewFragment.setText(dialogBinding.edtGSTAmountInvoicePreviewPopup.text.toString())
                binding.edtTotalAmountInvoicePreviewFragment.setText(dialogBinding.edtTotalAmountInvoicePreviewPopup.text.toString())
                alertDialog.dismiss()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "showExpensePopup: ${e.message}")
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun processData(dataList: ArrayList<InvoiceStockModelItem>) {
        try {
            dataList.forEach { model ->
                Log.d(TAG, "processData: HSN_CODE : ${model.HSNCode}")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "processData: ${e.message}")
        }
    }
}