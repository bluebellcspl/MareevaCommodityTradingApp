package com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca

import ConnectionCheck
import android.content.ContentValues
import android.icu.text.DecimalFormat
import android.icu.text.NumberFormat
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.EditableDecimalInputFilter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.constants.Constants
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentIndPCAAuctionBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCADashboardFragment.CommodityDetail
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchIndBuyerName
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchIndPCAAuctionAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.POSTIndPCAAuctionAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.ApiIndividualPCAAuctionDetail
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAAuctionFetchModel
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAAuctionInsertModel
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCABuyerModel

class IndPCAAuctionFragment : Fragment() {
    var _binding: FragmentIndPCAAuctionBinding? = null
    val binding get() = _binding!!
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val TAG = "PCAAuctionFragment"
    private val navController: NavController by lazy { findNavController() }
    private lateinit var buyerList:ArrayList<String>
    var _BuyerList:ArrayList<IndPCABuyerModel> = ArrayList()
    private lateinit var _ShopList:ArrayList<ShopNewDetails>
    var SELECTED_BUYER_ID= ""
    var SELECTED_BUYER_NAME= ""
    var BUYER_UPPER_LIMIT = "0.0"
    var BUYER_LOWER_LIMIT = "0.0"
    var REMAINING_BAG = "0"
    var PURCHASED_BAG = "0"
    var BUYER_BORI = "0"
    var AVG_PRICE = "0.0"
    var TOTAL_AMOUNT = "0.0"
    var shopId = ""
    var shopNo = ""
    var post_AvgPrice = 0.0
    var post_CumulativeTotal = 0.0
    var post_RemainingBags = 0f
    var post_PurchasedBags = 0f
    var post_CurrentTotal = 0.0
    var isWritten = false
    lateinit var apiDataforPost: IndPCAAuctionFetchModel
    lateinit var commodityBhartiRate: String
    lateinit var _CommodityList : ArrayList<CommodityDetail>
    var pcaBagList : ArrayList<ApiIndividualPCAAuctionDetail> = ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_ind_p_c_a_auction, container, false)
        Log.d("??", "onCreateView: ON_CREATE_VIEW_IND_PCA_FRAGMENT")
        binding.tvHeaderCommodityNDate.setText(DateUtility().getCompletionDate())
        if (PrefUtil.getSystemLanguage()!!.equals("gu")){
            var gujCommodityName = DatabaseManager.ExecuteScalar(Query.getGujaratiCommodityName(PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"")!!))
            if (gujCommodityName.equals("invalid")){
                gujCommodityName = ""
            }
            binding.actCommodityIndPCAAuctionFragment.setText(gujCommodityName)
        }else{

            binding.actCommodityIndPCAAuctionFragment.setText(PrefUtil.getString(PrefUtil.KEY_COMMODITY_NAME,""))
        }
        _ShopList = getShopDetails()
//        buyerList = getBuyerList()
        _CommodityList = getCommodityfromDB()
        commodityBhartiRate = DatabaseManager.ExecuteScalar(Query.getCommodityBhartiByCommodityId(PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"")!!)).toString()

        binding.edtBagsIndPCAAuctionFragment.filters =arrayOf<InputFilter>(EditableDecimalInputFilter(5, 2))
        binding.edtCurrentPriceIndPCAAuctionFragment.filters =arrayOf<InputFilter>(EditableDecimalInputFilter(7, 2))

        if (ConnectionCheck.isConnected(requireContext())){
            FetchIndPCAAuctionAPI(requireContext(),this@IndPCAAuctionFragment)
            FetchIndBuyerName(requireContext(),this@IndPCAAuctionFragment)
        }else{
            commonUIUtility.showToast(requireContext().getString(R.string.no_internet_connection))
        }
        binding.actShopNameIndPCAAuctionFragment.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.actShopNameIndPCAAuctionFragment.showDropDown()
        }

        binding.actCommodityIndPCAAuctionFragment.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.actCommodityIndPCAAuctionFragment.showDropDown()
        }
        binding.actShopNameIndPCAAuctionFragment.setOnItemClickListener { adapterView, view, position,long ->
            var shopModel:ShopNewDetails
            if (PrefUtil.getSystemLanguage().equals("gu")){
                shopModel = _ShopList.find { it-> it.GujaratiShopNoName.equals(adapterView.getItemAtPosition(position).toString())}!!
            }else{
                shopModel = _ShopList.find { it-> it.ShopNoName.equals(adapterView.getItemAtPosition(position).toString())}!!
            }
            Log.d(TAG, "onCreateView: SELECTED_SHOP_NAME : ${shopModel.ShopNoName}")
            Log.d(TAG, "onCreateView: SELECTED_SHOP_ID : ${shopModel.ShopId}")
            shopId = shopModel.ShopId
            shopNo = shopModel.ShopNo
        }

        binding.actCommodityIndPCAAuctionFragment.setOnItemClickListener { adapterView, view, position, long ->
            var commodityModel: CommodityDetail
            if (PrefUtil.getSystemLanguage().equals("gu")){
                commodityModel = _CommodityList.find {it.CommodityGujName.equals(adapterView.getItemAtPosition(position).toString())}!!
            }
            else{
                commodityModel = _CommodityList.find {it.CommodityName.equals(adapterView.getItemAtPosition(position).toString())}!!
            }
            Log.d(TAG, "onCreateView: SELECTED_COMMODITY_NAME : ${commodityModel.CommodityName}")
            Log.d(TAG, "onCreateView: SELECTED_COMMODITY_ID : ${commodityModel.CommodityId}")

            PrefUtil.setString(PrefUtil.KEY_COMMODITY_NAME,commodityModel.CommodityName)
            PrefUtil.setString(PrefUtil.KEY_COMMODITY_ID,commodityModel.CommodityId)

            Log.d(TAG, "onCreateView: SAVED_COMMODITY_NAME : ${PrefUtil.getString(PrefUtil.KEY_COMMODITY_NAME,"")}")
            Log.d(TAG, "onCreateView: SAVED_COMMODITY_ID : ${PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"")}")

            commodityBhartiRate = DatabaseManager.ExecuteScalar(Query.getCommodityBhartiByCommodityId(commodityModel.CommodityId))!!

            if (ConnectionCheck.isConnected(requireContext())){
                FetchIndPCAAuctionAPI(requireContext(),this@IndPCAAuctionFragment)
            } else{
                commonUIUtility.showToast(requireContext().getString(R.string.no_internet_connection))
            }

            Log.d(TAG, "onCreateView: IND_PCA_REG_ID : ${PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"")}")
//            Log.d(TAG, "onCreateView: IND_PCA_ID : ${PrefUtil.getString(PrefUtil.,"")}")

            binding.actShopNameIndPCAAuctionFragment.setText("")
            binding.actBuyerIndPCAAuctionFragment.setText("")
            binding.edtBagsIndPCAAuctionFragment.setText("")
            binding.edtCurrentPriceIndPCAAuctionFragment.setText("")
            binding.edtTotalAmountIndPCAAuctionFragment.setText("")
        }
        //TextWatcher
        binding.edtBagsIndPCAAuctionFragment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0!!.toString().isEmpty()) {
                    binding.edtBagsIndPCAAuctionFragment.setText("0")
//                    binding.tvRemainingBagsIndPCAAuctionFragment.setText(REMAINING_BAG)
                    binding.tvPurchasedBagsIndPCAAuctionFragment.setText(PURCHASED_BAG)
                    binding.edtBagsIndPCAAuctionFragment.setSelection(1)
                } else if (p0!!.toString().length >= 2 && p0!!.toString().startsWith("0")) {
                    val subStr =
                        binding.edtBagsIndPCAAuctionFragment.text.toString().substring(1)
                    binding.edtBagsIndPCAAuctionFragment.setText(subStr)
                    binding.edtBagsIndPCAAuctionFragment.setSelection(1)
                } else {
                    var decimalBags = p0!!.toString().trim()
                    var bags = p0!!.toString().trim()
//                    if (decimalBags.contains("."))
//                    {
//                        val decimal = p0!!.toString().trim().split(".")[1]
//                        val currentBag = p0!!.toString().trim().split(".")[0]
//                        bags= "$currentBag.5"
//                        Log.d(TAG, "afterTextChanged: NEW_DECIMAL_BAGS : $bags")
//                    }
//                    else
//                    {
//                        bags = p0!!.toString().trim()
//                    }
                    if (p0!!.endsWith(".")) {
                        val stringBuilder =
                            StringBuilder(binding.edtBagsIndPCAAuctionFragment.text.toString())
                        stringBuilder.append("50")
                        binding.edtBagsIndPCAAuctionFragment.setText(stringBuilder.toString())
                        binding.edtBagsIndPCAAuctionFragment.setSelection(stringBuilder.length)
                        bags = stringBuilder.toString()
                    }else if (bags.toFloat() > 0) {
                        post_PurchasedBags = PURCHASED_BAG.toFloat() + bags.toFloat()
                        post_RemainingBags = BUYER_BORI.toFloat() - post_PurchasedBags
//                        binding.tvRemainingBagsIndPCAAuctionFragment.setText(post_RemainingBags.toString())
                        binding.tvPurchasedBagsIndPCAAuctionFragment.setText(post_PurchasedBags.toString())
                    }else {
//                        binding.tvRemainingBagsIndPCAAuctionFragment.setText(post_RemainingBags.toString())
                        binding.tvPurchasedBagsIndPCAAuctionFragment.setText(PURCHASED_BAG)
                    }
                    calculateExpense(pcaBagList)
                }
            }
        })

        binding.edtBagsIndPCAAuctionFragment.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                val text = binding.edtBagsIndPCAAuctionFragment.text.toString()
                if (text.contains(".")) {
                    val text = binding.edtBagsIndPCAAuctionFragment.text.toString()
                    val decimalIndex = text.indexOf(".")
                    if (decimalIndex != -1) {
                        val newText = StringBuilder(text)
                        newText.delete(decimalIndex, newText.length)
                        binding.edtBagsIndPCAAuctionFragment.setText(newText.toString())
                        binding.edtBagsIndPCAAuctionFragment.setSelection(newText.length)
                        return@OnKeyListener true
                    }
                }
            }
            false
        })

        binding.edtCurrentPriceIndPCAAuctionFragment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0!!.toString().isNullOrBlank()) {
                    binding.edtCurrentPriceIndPCAAuctionFragment.setText("0")
                    val avgPrice = NumberFormat.getCurrencyInstance().format(AVG_PRICE.toDouble()).substring(1)
                    binding.tvAveragePriceIndPCAAuctionFragment.setText(avgPrice)
                    val totalAmount =
                        NumberFormat.getCurrencyInstance().format(TOTAL_AMOUNT.toDouble()).substring(1)
                    binding.tvTotalAmountIndPCAAuctionFragment.setText(totalAmount)
                    binding.edtCurrentPriceIndPCAAuctionFragment.setSelection(1)
                } else if (p0!!.toString().length >= 2 && p0!!.toString().startsWith("0")) {
                    val subStr =
                        binding.edtCurrentPriceIndPCAAuctionFragment.text.toString().substring(1)
                    binding.edtCurrentPriceIndPCAAuctionFragment.setText(subStr)
                    binding.edtCurrentPriceIndPCAAuctionFragment.setSelection(1)
                } else {
                    calculateExpense(pcaBagList)
                }
            }
        })

        binding.actBuyerIndPCAAuctionFragment.setOnItemClickListener { adapterView, view, position, long ->
            val buyerModel = _BuyerList.find { it-> it.BuyerShortName.equals(adapterView.getItemAtPosition(position).toString()) }!!
            binding.actBuyerIndPCAAuctionFragment.setText(adapterView.getItemAtPosition(position).toString())
            Log.d(TAG, "onCreateView: SELECTED_BUYER_ID : ${buyerModel.InBuyerId}")
            Log.d(TAG, "onCreateView: SELECTED_BUYER_SHORT_NAME : ${buyerModel.BuyerShortName}")
            SELECTED_BUYER_ID = buyerModel.InBuyerId
            SELECTED_BUYER_NAME = buyerModel.BuyerShortName
            Log.d(TAG, "onCreateView: SELECTED_BUYER_ID : $SELECTED_BUYER_ID")
            isWritten=false
            getBuyerPreviousPurchaseData(pcaBagList,SELECTED_BUYER_ID)
        }

        binding.actBuyerIndPCAAuctionFragment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0!!.toString().isNotEmpty()) {
                    SELECTED_BUYER_ID = ""
                    SELECTED_BUYER_NAME = ""
                    Log.d(TAG, "afterTextChanged: SELECTED_BUYER_ID : $SELECTED_BUYER_ID")
                    Log.d(TAG, "afterTextChanged: SELECTED_BUYER_NAME : $SELECTED_BUYER_NAME")
                    isWritten=true
                }else{
                    SELECTED_BUYER_ID = "0"
                    SELECTED_BUYER_NAME = "Self"
                }
            }
        })

        binding.edtBagsIndPCAAuctionFragment.setOnFocusChangeListener { view, b ->
            if (b) {
                if (binding.actBuyerIndPCAAuctionFragment.text.toString().isNotEmpty() && SELECTED_BUYER_ID.isEmpty() && isWritten)
                {
                    if(_BuyerList.find { it-> it.BuyerShortName.equals(binding.actBuyerIndPCAAuctionFragment.text.toString().trim()) } != null){
                        val buyerModel = _BuyerList.find { it-> it.BuyerShortName.equals(binding.actBuyerIndPCAAuctionFragment.text.toString().trim()) }
                        SELECTED_BUYER_ID = buyerModel!!.InBuyerId
                        SELECTED_BUYER_NAME = buyerModel.BuyerShortName
                        Log.d(TAG, "onCreateView: ON_FOCUS : SELECTED_BUYER_ID : $SELECTED_BUYER_ID")

                        getBuyerPreviousPurchaseData(pcaBagList,SELECTED_BUYER_ID)
                    }
                }
            }
        }
//        if (binding.actBuyerIndPCAAuctionFragment.text.toString().isNotEmpty()){
//            getBuyerPreviousPurchaseData(pcaBagList,binding.actBuyerIndPCAAuctionFragment.text.toString())
//        }
        setOnClickListeners()
        return binding.root
    }

    private fun setOnClickListeners() {
        try {
            binding.btnAddIndPCAAuctionFragment.setOnClickListener {
//                val lowerSub =  BUYER_LOWER_LIMIT.split(".")[0]
//                val upperSub = BUYER_UPPER_LIMIT.split(".")[0]
//                Log.d(TAG, "onCreateView: LOWER_SUB_LENGTH : ${lowerSub.length}")
//                Log.d(TAG, "onCreateView: UPPER_SUB_LENGTH : ${upperSub.length}")
//                if (!binding.edtCurrentPriceIndPCAAuctionFragment.text.toString().contains(".") && binding.edtCurrentPriceIndPCAAuctionFragment.text.toString().length<lowerSub.length-1) {
//                    commonUIUtility.showAlertWithOkButton("Current Price Must be Greater Than Lower Limit Price")
//                }
//                else if (binding.edtCurrentPriceIndPCAAuctionFragment.text.toString().contains(".") && binding.edtCurrentPriceIndPCAAuctionFragment.text.toString().split(".")[0].length < lowerSub.length-1) {
//                    commonUIUtility.showAlertWithOkButton("Current Price Must be Lesser Than Upper Limit Price")
//                }
//                else if (!binding.edtCurrentPriceIndPCAAuctionFragment.text.toString().contains(".") && binding.edtCurrentPriceIndPCAAuctionFragment.text.toString().length > upperSub.length+1) {
//                    commonUIUtility.showAlertWithOkButton("Current Price Must be Lesser Than Upper Limit Price")
//                }
//                else if (binding.edtCurrentPriceIndPCAAuctionFragment.text.toString().contains(".") && binding.edtCurrentPriceIndPCAAuctionFragment.text.toString().split(".")[0].length > upperSub.length+1) {
//                    commonUIUtility.showAlertWithOkButton("Current Price Must be Lesser Than Upper Limit Price")
//                }
                if (shopId.equals("invalid") || shopId.isEmpty()) {
                    commonUIUtility.showAlertWithOkButton(getString(R.string.please_select_proper_shop_alert_msg))
                } else if (binding.actShopNameIndPCAAuctionFragment.text.toString()
                        .isEmpty()) {
                    commonUIUtility.showAlertWithOkButton(getString(R.string.please_enter_shop_name_or_shop_no_alert_msg))
                } else if (binding.edtBagsIndPCAAuctionFragment.text.toString().endsWith(".") || binding.edtBagsIndPCAAuctionFragment.text.toString().startsWith(".")) {
                    commonUIUtility.showToast(getString(R.string.please_enter_valid_input_alert_msg))
                } else if (binding.edtBagsIndPCAAuctionFragment.text.toString().isEmpty() || binding.edtBagsIndPCAAuctionFragment.text.toString().toFloat() < 1) {
                    commonUIUtility.showToast(getString(R.string.please_enter_bags_alert_msg))
                }else if (binding.edtCurrentPriceIndPCAAuctionFragment.text.toString().isEmpty() || binding.edtCurrentPriceIndPCAAuctionFragment.text.toString().toFloat() < 1) {
                    commonUIUtility.showToast(getString(R.string.please_enter_current_price_alert_msg))
                } else if (_ShopList.isEmpty())
                {
                    binding.actShopNoIndPCAAuctionFragment.isEnabled = false
                    binding.actShopNameIndPCAAuctionFragment.isEnabled = false
                    binding.actShopNoIndPCAAuctionFragment.setText("")
                    binding.actShopNameIndPCAAuctionFragment.setText("")
                    commonUIUtility.showToast(getString(R.string.shop_list_is_blank_alert_msg))
                }
                else{
                    insertBuyer(binding.actBuyerIndPCAAuctionFragment.text.toString())
                    buyerList=getBuyerList()
                    postIndPCAAuctionData()
                }
            }

            binding.btnListIndPCAAuctionFragment.setOnClickListener {
//                navController.navigate(IndPCAAuctionFragmentDirections.actionIndPCAAuctionFragmentToIndPCAAuctionListFragment())
                navController.navigate(IndPCAAuctionFragmentDirections.actionIndPCAAuctionFragmentToIndPCAAuctionBuyerListFragment())
            }
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "setOnClickListeners: ${e.printStackTrace()}")
        }
    }

    fun calculateExpense(dataList: ArrayList<ApiIndividualPCAAuctionDetail>) {
        try {
            var newbags = binding.edtBagsIndPCAAuctionFragment.text.toString().trim()
            var bags = ""
            if (newbags.contains("."))
            {
                val currentBag = newbags.split(".")[0]
                bags= "$currentBag.5"
            }else
            {
                bags= binding.edtBagsIndPCAAuctionFragment.text.toString().trim()
            }
            var currentPrice = binding.edtCurrentPriceIndPCAAuctionFragment.text.toString().trim()
            if (bags.isNotEmpty() && currentPrice.isNotEmpty()) {
                //Current Calculation of Bags
                var total = 0.0
                if (bags.toFloat() > 0 && currentPrice.toDouble() > 0) {
                    total =
                        ((bags.toDouble() * commodityBhartiRate.toDouble()) / 20) * (currentPrice.toDouble())
                }
                val totalCostNF = NumberFormat.getCurrencyInstance().format(total).substring(1)
                post_CurrentTotal = total
                binding.edtTotalAmountIndPCAAuctionFragment.setText(totalCostNF.toString())

                //Header Calculation for Total Cost from ArrayList
                var cumulativeTotal = 0.0
                var totalPurchasedBags = 0f
                for (i in 0 until dataList.size) {
                    cumulativeTotal += dataList[i].Amount.toDouble()
                    totalPurchasedBags += dataList[i].Bags.toFloat()
                }
                post_AvgPrice =
                    (cumulativeTotal + total) / (((totalPurchasedBags + bags.toFloat()) * commodityBhartiRate.toDouble()) / 20)
                val formattedPost_AvgPrice = DecimalFormat("########0.00").format(post_AvgPrice)
                val fomattedCumilativeTotal = DecimalFormat("########0.00").format(cumulativeTotal)
                Log.d(TAG, "calculateExpense: AVG_PRICE : $formattedPost_AvgPrice")
                val AvgPriceNF = NumberFormat.getCurrencyInstance().format(formattedPost_AvgPrice.toDouble()).substring(1)
//                binding.tvAveragePriceIndPCAAuctionFragment.setText(String.format("%.2f",post_AvgPrice))
                binding.tvAveragePriceIndPCAAuctionFragment.setText(AvgPriceNF)

                Log.d(TAG, "calculateExpense: CUMULATIVE_TOTAL : $fomattedCumilativeTotal")
                val cumulativeTotalNF =NumberFormat.getCurrencyInstance().format(fomattedCumilativeTotal.toDouble() + total).substring(1)
                post_CumulativeTotal = fomattedCumilativeTotal.toDouble() + total
//                binding.tvTotalAmountIndPCAAuctionFragment.setText(String.format("%.2f",post_CumulativeTotal))
                binding.tvTotalAmountIndPCAAuctionFragment.setText(cumulativeTotalNF)

            } else {
                binding.edtTotalAmountIndPCAAuctionFragment.setText("")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "calculateExpense: ${e.message}")
        }
    }

    private fun postIndPCAAuctionData() {
        try {
            if (SELECTED_BUYER_ID.isEmpty() && SELECTED_BUYER_NAME.isEmpty() && binding.actBuyerIndPCAAuctionFragment.text.toString().isEmpty())
            {
                SELECTED_BUYER_NAME = "Self"
                SELECTED_BUYER_ID = "0"
            }else if(SELECTED_BUYER_NAME.isEmpty() && binding.actBuyerIndPCAAuctionFragment.text.toString().isNotEmpty()){
                SELECTED_BUYER_NAME = binding.actBuyerIndPCAAuctionFragment.text.toString().trim()
            }
            var indPCAAuctionModel = IndPCAAuctionInsertModel(
                "Confirm",
                "%.2f".format(post_CurrentTotal),
                "%.2f".format(post_AvgPrice),
                ""+binding.edtBagsIndPCAAuctionFragment.text.toString().trim(),
                ""+SELECTED_BUYER_ID,
                ""+SELECTED_BUYER_NAME,
                commodityBhartiRate,
                ""+PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"").toString(),
                ""+PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,"").toString(),
                ""+DateUtility().getyyyyMMdd(),
                ""+PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString(),
                ""+binding.edtCurrentPriceIndPCAAuctionFragment.text.toString(),
                ""+DateUtility().getyyyyMMdd(),
                ""+apiDataforPost.APMCId,
                "",
                ""+apiDataforPost.IndividualPCAAuctionHeaderId,
                ""+apiDataforPost.IndividualPCAAuctionMasterId,
                ""+apiDataforPost.IndividualPCAId,
                ""+PrefUtil.getString(PrefUtil.KEY_MOBILE_NO,"").toString(),
                ""+apiDataforPost.IndividualPCARegId,
                "",
                ""+PrefUtil.getString(PrefUtil.KEY_ROLE_ID,"").toString(),
                shopId,
                shopNo,
                "%.2f".format(post_CumulativeTotal),
                ""+post_PurchasedBags,
                ""+DateUtility().getyyyyMMdd(),
                ""+PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString()
            )

            if (ConnectionCheck.isConnected(requireContext()))
            {
                POSTIndPCAAuctionAPI(requireContext(),this@IndPCAAuctionFragment,indPCAAuctionModel,0)
            }
            else{
                commonUIUtility.showToast(requireContext().getString(R.string.no_internet_connection))
            }
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "postIndPCAAuctionData: ${e.message}")
        }
    }


    fun updateAuctionUI(auctionModel:IndPCAAuctionFetchModel){
        try {
            val avgPriceNF = NumberFormat.getCurrencyInstance().format(auctionModel.TotalAveragePrice.toDouble()).substring(1)
            val totalAmountNF = NumberFormat.getCurrencyInstance().format(auctionModel.TotalAmount.toDouble()).substring(1)
            binding.tvPurchasedBagsIndPCAAuctionFragment.setText(auctionModel.TotalBags)
            binding.tvTotalAmountIndPCAAuctionFragment.setText(totalAmountNF)
            binding.tvAveragePriceIndPCAAuctionFragment.setText(avgPriceNF)
//            commodityBhartiRate = auctionModel.CommodityBhartiPrice
            TOTAL_AMOUNT = auctionModel.TotalAmount
            AVG_PRICE = auctionModel.TotalAveragePrice
            PURCHASED_BAG = auctionModel.TotalBags
            pcaBagList = auctionModel.ApiIndividualPCAAuctionDetail
            apiDataforPost = auctionModel
            if (SELECTED_BUYER_NAME.isNotEmpty() && SELECTED_BUYER_ID.isNotEmpty())
            {
                getBuyerPreviousPurchaseData(pcaBagList,SELECTED_BUYER_ID)
            }
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "updateAuctionUI: ${e.message}")
        }
    }

    fun updateAfterInsert(){
        binding.edtTotalAmountIndPCAAuctionFragment.setText("")
        binding.edtBagsIndPCAAuctionFragment.setText("")
        binding.edtCurrentPriceIndPCAAuctionFragment.setText("")
    }

    private fun getShopDetails():ArrayList<ShopNewDetails>{
        val cursor = DatabaseManager.ExecuteRawSql(Query.getShopNoName())
        var shopList = ArrayList<ShopNewDetails>()
        try {
            if (cursor!=null && cursor.count>0)
            {
                while (cursor.moveToNext())
                {
                    val shopModel = ShopNewDetails(
                        cursor.getString(cursor.getColumnIndexOrThrow("ShopId")),
                        cursor.getString(cursor.getColumnIndexOrThrow("ShopNo")),
                        cursor.getString(cursor.getColumnIndexOrThrow("ShopNoName")),
                        cursor.getString(cursor.getColumnIndexOrThrow("GujaratiShopNoName"))
                    )

                    shopList.add(shopModel)
                }
            }
            cursor?.close()
            Log.d(TAG, "getShopDetails: ShopList : $shopList")
            var shopAdapter : ArrayAdapter<String>
           if(PrefUtil.getSystemLanguage().equals("gu")){
               val newShopNoList = shopList.map { it.GujaratiShopNoName } as ArrayList<String>
               val newDataList = commonUIUtility.sortAlphanumericList(newShopNoList)
               shopAdapter = commonUIUtility.getCustomArrayAdapter(newDataList)
           }else
           {
               val newShopNoList = shopList.map { it.ShopNoName } as ArrayList<String>
               val newDataList = commonUIUtility.sortAlphanumericList(newShopNoList)
               shopAdapter = commonUIUtility.getCustomArrayAdapter(newDataList)
           }
            binding.actShopNameIndPCAAuctionFragment.setAdapter(shopAdapter)
            shopList.forEach {
                Log.d(TAG, "getShopDetails: ShopModel $it")
            }
        }catch (e:Exception)
        {
            cursor?.close()
            shopList.clear()
            e.printStackTrace()
            Log.e(TAG, "getShopDetails: ${e.message}")
        }
        return shopList
    }

    private fun getCommodityfromDB():ArrayList<CommodityDetail>{
        val localArrayList = ArrayList<CommodityDetail>()
        val cursor = DatabaseManager.ExecuteRawSql(Query.getCommodityDetail())
        try {
            if (cursor!=null && cursor.count>0)
            {
                while (cursor.moveToNext())
                {

                    val model = CommodityDetail(
                        cursor.getString(cursor.getColumnIndexOrThrow("CommodityId")),
                        cursor.getString(cursor.getColumnIndexOrThrow("CommodityName")),
                        cursor.getString(cursor.getColumnIndexOrThrow("CommodityName"))
                    )
                    localArrayList.add(model)
                }
            }
            var commodityAdapter : ArrayAdapter<String>
            if(PrefUtil.getSystemLanguage().equals("gu")){
                commodityAdapter = commonUIUtility.getCustomArrayAdapter(ArrayList(localArrayList.map { it.CommodityGujName }))
            }else
            {
                commodityAdapter = commonUIUtility.getCustomArrayAdapter(ArrayList(localArrayList.map { it.CommodityName }))
            }

            binding.actCommodityIndPCAAuctionFragment.setAdapter(commodityAdapter)
            localArrayList.forEach {
                Log.d(TAG, "getCommodityfromDB: ID - APMC : ${it.CommodityId} - ${it.CommodityName}")
            }
            Log.d(TAG, "getCommodityfromDB: AMPCList : $localArrayList")
            cursor?.close()
        }catch (e:Exception)
        {
            cursor?.close()
            localArrayList.clear()
            e.printStackTrace()
            Log.e(TAG, "getCommodityfromDB: ${e.message}")
        }
        return localArrayList
    }

    private fun getBuyerList():ArrayList<String>{
        val cursor = DatabaseManager.ExecuteRawSql(Query.getBuyerList())
        var buyerList = ArrayList<String>()
        try {
            if (cursor!=null && cursor.count>0)
            {
                while (cursor.moveToNext())
                {
                    buyerList.add(cursor.getString(cursor.getColumnIndexOrThrow("BuyerName")))
                }
            }
            cursor?.close()
            Log.d(TAG, "getBuyerList: ShopList : $buyerList")
            val buyerAdapter = commonUIUtility.getCustomArrayAdapter(buyerList)
            binding.actBuyerIndPCAAuctionFragment.setAdapter(buyerAdapter)
            buyerList.forEach {
                Log.d(TAG, "getBuyerList: Buyer Name : $it")
            }
        }catch (e:Exception)
        {
            cursor?.close()
            buyerList.clear()
            e.printStackTrace()
            Log.e(TAG, "getBuyerList: ${e.message}")
        }
        return buyerList
    }

    fun getBuyerFromAPI(dataList:ArrayList<IndPCABuyerModel>){
        _BuyerList.clear()
        _BuyerList = dataList
        val buyerAdapter = commonUIUtility.getCustomArrayAdapter(_BuyerList.map { it.BuyerShortName } as ArrayList<String>)
        binding.actBuyerIndPCAAuctionFragment.setAdapter(buyerAdapter)
    }

    private fun getBuyerPreviousPurchaseData(dataList:ArrayList<ApiIndividualPCAAuctionDetail>, buyerId:String){
        try {
            binding.cvInfoCard1IndPCAAuctionFragment.visibility = View.VISIBLE
            var buyerBags = 0.0
            var buyerTotalAmount = 0.0
            var buyerAveragePrice = 0.0
            var buyerName = ""
            if (buyerId.isNotEmpty()){
                if (dataList.isNotEmpty()){
                    for (i in 0 until dataList.size)
                    {
                        if (dataList[i].BuyerId.equals(buyerId))
                        {
                            buyerName = dataList[i].BuyerName
                            buyerBags += dataList[i].Bags.toDouble()
                            buyerTotalAmount += dataList[i].Amount.toDouble()
                        }
                    }
                    buyerAveragePrice =
                        (buyerTotalAmount) / ((buyerBags * commodityBhartiRate.toDouble()) / 20)

                    val formattedPost_AvgPrice = DecimalFormat("########0.00").format(buyerAveragePrice)
                    val fomattedCumilativeTotal = DecimalFormat("########0.00").format(buyerTotalAmount)
                    Log.d(TAG, "calculateExpense: AVG_PRICE : $formattedPost_AvgPrice")
                    val AvgPriceNF = NumberFormat.getCurrencyInstance().format(formattedPost_AvgPrice.toDouble()).substring(1)
                    if (buyerAveragePrice>0){
                        binding.tvBuyerAveragePriceIndPCAAuctionFragment.setText(AvgPriceNF)
                    }else{
                        binding.tvBuyerAveragePriceIndPCAAuctionFragment.setText("0.0")
                    }

                    Log.d(TAG, "calculateExpense: CUMULATIVE_TOTAL : $fomattedCumilativeTotal")
                    val cumulativeTotalNF =NumberFormat.getCurrencyInstance().format(buyerTotalAmount).substring(1)
                    binding.tvBuyersBudgetIndPCAAuctionFragment.setText(cumulativeTotalNF)
                    binding.tvBuyerBagsIndPCAAuctionFragment.setText("$buyerBags")
                    binding.tvBuyerNameIndPCAAuctionFragment.setText(buyerName)
                }else{
                    binding.tvBuyerAveragePriceIndPCAAuctionFragment.setText("")
                    binding.tvBuyersBudgetIndPCAAuctionFragment.setText("")
                    binding.tvBuyerBagsIndPCAAuctionFragment.setText("")
                }
            }else{
                binding.tvBuyerNameIndPCAAuctionFragment.setText("")
                binding.tvBuyerAveragePriceIndPCAAuctionFragment.setText("")
                binding.tvBuyersBudgetIndPCAAuctionFragment.setText("")
                binding.tvBuyerBagsIndPCAAuctionFragment.setText("")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "getBuyerPreviousPurchaseData: ${e.message}", )
        }
    }

    private fun insertBuyer(BuyerName:String){
        try {
            if (binding.actBuyerIndPCAAuctionFragment.text.toString().isNotEmpty() && !buyerList.contains(BuyerName)){
                val contentValue = ContentValues()
                contentValue.put("BuyerName",BuyerName)
                contentValue.put("CreateDate",DateUtility().getyyyyMMddDateTime())
                DatabaseManager.commonInsert(contentValue,Constants.TBL_BuyerData)
            }
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "insertBuyer: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("??", "onResume: ON_RESUME_IND_AUCTION_FRAGMENT")
        if (ConnectionCheck.isConnected(requireContext())){
            FetchIndPCAAuctionAPI(requireContext(),this@IndPCAAuctionFragment)
        }else{
            commonUIUtility.showToast(requireContext().getString(R.string.no_internet_connection))
        }
        binding.actShopNameIndPCAAuctionFragment.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.actShopNameIndPCAAuctionFragment.showDropDown()
        }
    }
    data class ShopNewDetails(var ShopId:String,var ShopNo:String,var ShopNoName:String,var GujaratiShopNoName:String)

    override fun onDestroy() {
        super.onDestroy()
        Log.d("??", "onDestroy: ON_DESTROY_IND_AUCTION_FRAGMENT")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("??", "onDestroy: ON_DESTROY_VIEW_IND_AUCTION_FRAGMENT")
    }
}