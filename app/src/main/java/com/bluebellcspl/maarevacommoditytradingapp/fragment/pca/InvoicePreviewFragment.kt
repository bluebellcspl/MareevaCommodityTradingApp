package com.bluebellcspl.maarevacommoditytradingapp.fragment.pca

import ConnectionCheck
import android.app.AlertDialog
import android.content.DialogInterface
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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.InvoicePreviewAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.AmountNumberToWords
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.EditableDecimalInputFilter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.FileDownloader
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.constants.URLHelper
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentInvoicePreviewBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.InvoicePreviewCommissionPopupBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchInvoicePreviewAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.POSTInvoiceDataAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceDetailsModel
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoicePreviewModel
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceStockModelItem
import com.bluebellcspl.maarevacommoditytradingapp.model.PostInvoiceDataModel
import java.text.DecimalFormat
import kotlin.math.floor


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
    private lateinit var finalExpenses:FinalExpensePopupData
    private val fileDownloader by lazy { FileDownloader.getInstance(requireContext()) }
    private val navController by lazy { findNavController() }
    private var isBuyerGSTINValid = false
    private var isBuyerPANValid = false
    private var alertDialog: AlertDialog? = null
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
        binding.edtTotalAmountInvoicePreviewFragment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s!!.isNotEmpty()) {
                    val amount = s.toString().replace(",", "")
                    val numberAmount = amount.toDouble()
                    Log.d(TAG, "afterTextChanged: NUMBER_AMOUNT : $numberAmount")
                    val amountToWords = AmountNumberToWords.convert(numberAmount)
                    binding.tvAmountInWordsInvoicePreviewFragment.setText(amountToWords)
                }
            }
        })
        binding.edtBuyerGSTINInvoicePreviewFragment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s!!.isNotEmpty())
                {
                    if (isValidGSTIN(s!!.toString())) {
                        binding.edtBuyerGSTINContainerInvoicePreviewFragment.boxStrokeColor = requireContext().getColor(R.color.newButtonColor)
                        isBuyerGSTINValid = true
                    } else {
                        binding.edtBuyerGSTINContainerInvoicePreviewFragment.boxStrokeColor = requireContext().getColor(R.color.unReadChatBadge)
                        isBuyerGSTINValid = false
                    }
                }else
                {
                    isBuyerGSTINValid = false
                }
            }
        })

        binding.edtBuyerPANInvoicePreviewFragment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s!!.isNotEmpty())
                {
                    if (isValidPAN(s!!.toString())) {
                        binding.edtBuyerPANContainerInvoicePreviewFragment.boxStrokeColor = requireContext().getColor(R.color.newButtonColor)
                        isBuyerPANValid = true
                    } else {
                        binding.edtBuyerPANContainerInvoicePreviewFragment.boxStrokeColor = requireContext().getColor(R.color.unReadChatBadge)
                        isBuyerPANValid = false
                    }
                }else
                {
                    isBuyerPANValid = false
                }
            }
        })
        setOnClickListener()
        return binding.root
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
            binding.btnSubmitNDownloadInvoicePreviewFragment.setOnClickListener {
                if (!isBuyerPANValid)
                {
                    commonUIUtility.showToast(getString(R.string.please_enter_valid_pan_alert_msg))
                    return@setOnClickListener
                }else if (!isBuyerGSTINValid){
                    commonUIUtility.showToast(getString(R.string.please_enter_valid_gstin_alert_msg))
                    return@setOnClickListener
                }else if (binding.edtBuyerNameInvoicePreviewFragment.text.toString().isEmpty())
                {
                    commonUIUtility.showToast(getString(R.string.please_enter_buyer_name_alert_msg))
                    return@setOnClickListener
                }
                else if (binding.edtBuyerAddressInvoicePreviewFragment.text.toString().isEmpty())
                {
                    commonUIUtility.showToast(getString(R.string.enter_buyer_address_alert_msg))
                    return@setOnClickListener
                }else if (binding.edtBuyerCityInvoicePreviewFragment.text.toString().isEmpty())
                {
                    commonUIUtility.showToast(getString(R.string.please_enter_buyer_city_alert_msg))
                    return@setOnClickListener
                }else
                {
                    showAlertDialog()
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
            var cgstAmt = DecimalFormat("0.00").format(GSTAmount / 2.0)
            var sgstAmt = DecimalFormat("0.00").format(GSTAmount / 2.0)
            var totalAmountWithGST = totalAmount + GSTAmount

            val formattedGSTAmount =
                NumberFormat.getCurrencyInstance().format(GSTAmount).substring(1)
            binding.edtGSTInvoicePreviewFragment.setText(formattedGSTAmount)

            val formattedTotalAmount =
                NumberFormat.getCurrencyInstance().format(totalAmountWithGST).substring(1)
            binding.edtTotalAmountInvoicePreviewFragment.setText(formattedTotalAmount)
            finalExpenses = FinalExpensePopupData(
                DecimalFormat("0.00").format(GCACommission),
                invoicePreviewModel.PCAgcaCommissiom,
                DecimalFormat("0.00").format(PCACommission),
                invoicePreviewModel.PCApcaCommissiom,
                DecimalFormat("0.00").format(MarketFees),
                invoicePreviewModel.MarketCess,
                DecimalFormat("0.00").format(LabourFees),
                invoicePreviewModel.LabourCharges,
                DecimalFormat("0.00").format(TransportFees),
                invoicePreviewModel.PerBoriRate,
                DecimalFormat("0.00").format(GSTAmount),
                invoicePreviewModel.GSTTotalPct,
                cgstAmt.toString(),
                CGST_AMOUNT!!.toString(),
                sgstAmt.toString(),
                SGST_AMOUNT!!.toString(),
                DecimalFormat("0.00").format(totalAmountWithGST)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "populateData: ${e.message}")
        }
    }

    fun bindRcView(dataList: ArrayList<InvoiceStockModelItem>) {
        try {
            dataList.forEach { model ->
                Log.d(TAG, "bindRcView: HSN_CODE : ${model.HsnAsc}")
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

    fun showAlertDialog() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle("Alert")
        alertDialog.setMessage("Do you want to Save Invoice?")
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
            val postInvoiceStockList = ArrayList<InvoiceDetailsModel>()
            _InvoiceStockList!!.forEach{model->
                val currentQTL = DecimalFormat("0.00").format(floor(model.UsedBagWeightKg.toDouble() / 100.0))
                val currentKG = model.UsedBagWeightKg.toDouble() - floor(model.UsedBagWeightKg.toDouble()/100.0)*100.0
                val invoiceKG = DecimalFormat("0.00").format(currentKG)

                val invoiceDetailsModel = InvoiceDetailsModel(
                    model.CommodityId,
                    model.UsedBags,
                    model.HsnAsc,
                    currentQTL,
                    model.UsedBagRate,
                    model.UsedBagAmount,
                    invoiceKG,
                    model.StockId
                )
                
                postInvoiceStockList.add(invoiceDetailsModel)
            }
            val invoiceDataModel = PostInvoiceDataModel(
                "Insert",
                binding.edtBuyerAddressInvoicePreviewFragment.text.toString().trim(),
                binding.edtBuyerCityInvoicePreviewFragment.text.toString().trim(),
                binding.edtBuyerGSTINInvoicePreviewFragment.text.toString().trim(),
                _invoicePreviewModel!!.BuyerId,
                binding.edtBuyerNameInvoicePreviewFragment.text.toString().trim(),
                binding.edtBuyerPANInvoicePreviewFragment.text.toString().trim(),
                PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE, "").toString(),
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString(),
                _invoicePreviewModel!!.InvoiceDate,
                finalExpenses.finalTotalAmount,
                binding.tvAmountInWordsInvoicePreviewFragment.text.toString().trim(),
                finalExpenses.finalCGSTAmount,
                finalExpenses.finalCGSTPCT,
                finalExpenses.finalGCACommAmount,
                finalExpenses.finalGCACommPCT,
                finalExpenses.finalGSTAmount,
                TOTAL_GST.toString(),
                finalExpenses.finalLabourAmount,
                finalExpenses.finalLabourRate,
                finalExpenses.finalMarketFeesAmount,
                finalExpenses.finalMarketFeesPCT,
                finalExpenses.finalPCACommAmount,
                finalExpenses.finalPCACommPCT,
                finalExpenses.finalSGSTAmount,
                finalExpenses.finalSGSTPCT,
                finalExpenses.finalTransportAmount,
                finalExpenses.finalTransportRate,
                _invoicePreviewModel!!.FinanceYear,
                postInvoiceStockList,
                _invoicePreviewModel!!.PCAId,
                _invoicePreviewModel!!.PCARegId,
                _invoicePreviewModel!!.PCARegId,
                binding.edtTransportVehicleInvoicePreviewFragment.text.toString().trim()
            )

            Log.d(TAG, "sendInvoiceData: INVOICE_DATA_MODEL : $invoiceDataModel")

            if (ConnectionCheck.isConnected(requireContext()))
            {
                POSTInvoiceDataAPI(requireContext(),this@InvoicePreviewFragment,_InvoiceStockList!!,invoiceDataModel)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "sendInvoiceData: ${e.message}")
        }
    }

    fun showExpensePopup() {
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

                dialogBinding.edtGCACommInvoicePreviewPopup.setText(_invoicePreviewModel!!.PCAgcaCommissiom)
                dialogBinding.edtPCACommInvoicePreviewPopup.setText(_invoicePreviewModel!!.PCApcaCommissiom)
                dialogBinding.edtMarketFeesInvoicePreviewPopup.setText(_invoicePreviewModel!!.MarketCess)
                dialogBinding.edtLabourInvoicePreviewPopup.setText(_invoicePreviewModel!!.LabourCharges)
                dialogBinding.edtTransportInvoicePreviewPopup.setText(_invoicePreviewModel!!.PerBoriRate)

                var gstPCString =
                    StringBuilder(dialogBinding.tvGSTTotalPercentageInvoicePreviewPopup.text.toString())
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
                    val totalGSTAmount = dialogBinding.edtGSTAmountInvoicePreviewPopup.text.toString().replace(",", "").toDouble()
                    val CGSTAmount = totalGSTAmount/2.0
                    val SGSTAmount = totalGSTAmount/2.0
                    val CGST_PCT = TOTAL_GST!!/2.0
                    val SGST_PCT = TOTAL_GST!!/2.0
                    finalExpenses = FinalExpensePopupData(
                        dialogBinding.tvGCACommAmountInvoicePreviewPopup.text.toString().replace(",", "").trim(),
                        dialogBinding.edtGCACommInvoicePreviewPopup.text.toString(),
                        dialogBinding.tvPCACommAmountInvoicePreviewPopup.text.toString().replace(",", "").trim(),
                        dialogBinding.edtPCACommInvoicePreviewPopup.text.toString(),
                        dialogBinding.tvMarketFeesAmountInvoicePreviewPopup.text.toString().replace(",", "").trim(),
                        dialogBinding.edtMarketFeesInvoicePreviewPopup.text.toString(),
                        dialogBinding.tvLabourAmountInvoicePreviewPopup.text.toString().replace(",", "").trim(),
                        dialogBinding.edtLabourInvoicePreviewPopup.text.toString(),
                        dialogBinding.tvTransportAmountInvoicePreviewPopup.text.toString().replace(",", "").trim(),
                        dialogBinding.edtTransportInvoicePreviewPopup.text.toString(),
                        dialogBinding.edtGSTAmountInvoicePreviewPopup.text.toString().replace(",", "").trim(),
                        dialogBinding.tvGSTTotalPercentageInvoicePreviewPopup.text.toString().replace("%","").trim(),
                        CGSTAmount.toString(),
                        CGST_PCT.toString(),
                        SGSTAmount.toString(),
                        SGST_PCT.toString(),
                        dialogBinding.edtTotalAmountInvoicePreviewPopup.text.toString().replace(",", "")
                    )
                    alertDialog!!.dismiss()
                }

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

    data class FinalExpensePopupData(
        var finalGCACommAmount: String,
        var finalGCACommPCT: String,
        var finalPCACommAmount: String,
        var finalPCACommPCT: String,
        var finalMarketFeesAmount: String,
        var finalMarketFeesPCT: String,
        var finalLabourAmount: String,
        var finalLabourRate: String,
        var finalTransportAmount: String,
        var finalTransportRate: String,
        var finalGSTAmount: String,
        var finalGSTPCT: String,
        var finalCGSTAmount:String,
        var finalCGSTPCT:String,
        var finalSGSTAmount:String,
        var finalSGSTPCT: String,
        var finalTotalAmount: String
    )

    override fun processData(dataList: ArrayList<InvoiceStockModelItem>) {
        try {
            dataList.forEach { model ->
                Log.d(TAG, "processData: HSN_CODE : ${model.HsnAsc}")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "processData: ${e.message}")
        }
    }

    fun downloadInvoice(invoiceId:String)
    {
        try {
            val fileURL = URLHelper.INVOICE_DOC.replace("<INVOICE_NO>",invoiceId)
            Log.d(TAG, "downloadInvoice: fileURL : $fileURL")
            fileDownloader.downloadFile(fileURL,"InVoiceGenerating_Report_Book.doc", "Downloading Invoice")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "downloadAuctionDetailReport: ${e.message}")
        }
    }

    fun successRedirect(){
        try {
            navController.navigate(InvoicePreviewFragmentDirections.actionInvoicePreviewFragmentToInvoiceStockFragment())
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "successRedirect: ${e.message}")
        }
    }
}