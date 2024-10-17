package com.bluebellcspl.maarevacommoditytradingapp.fragment.pca

import ConnectionCheck
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.icu.text.DecimalFormat
import android.icu.text.NumberFormat
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bluebellcspl.maarevacommoditytradingapp.LoginActivity
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.EditableDecimalInputFilter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentPCAAuctionBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchPCAAuctionDetailAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.POSTPCAAuctionDetailAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.ApiPCAAuctionDetail
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAAuctionDetailModel
import com.bluebellcspl.maarevacommoditytradingapp.model.POSTPCAAuctionData
import java.util.Locale


class PCAAuctionFragment : Fragment() {
    var _binding: FragmentPCAAuctionBinding? = null
    val binding get() = _binding!!
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val TAG = "PCAAuctionFragment"
    private val navController: NavController by lazy { findNavController() }
    lateinit var alertDialog: AlertDialog
    private val args by navArgs<PCAAuctionFragmentArgs>()
    lateinit var pcaBoriList: ArrayList<ApiPCAAuctionDetail>
    lateinit var shopNameList: ArrayList<String>
    lateinit var shopNoList: ArrayList<String>
    var BUYER_UPPER_LIMIT = "0.0"
    var BUYER_LOWER_LIMIT = "0.0"
    var REMAINING_BAG = "0"
    var PURCHASED_BAG = "0"
    var BUYER_BORI = "0"
    var AVG_PRICE = "0.0"
    var TOTAL_AMOUNT = "0.0"
    var shopId = ""
    var post_AvgPrice = 0.0
    var post_CumulativeTotal = 0.0
    var post_RemainingBags = 0f
    var post_PurchasedBags = 0f
    var post_CurrentTotal = 0.0
    lateinit var apiDataforPost: PCAAuctionDetailModel
    lateinit var commodityBhartiRate: String
    private lateinit var _ShopDataList : ArrayList<ShopSelectionData>
//    private var _filterSortedShopList = ArrayList<ShopSelectionData>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_p_c_a_auction, container, false)

        val commodityNameNDateBuilder = StringBuilder()
        if (PrefUtil.getSystemLanguage().equals("en"))
        {
            commodityNameNDateBuilder.append(PrefUtil.getString(PrefUtil.KEY_COMMODITY_NAME,""))
        }else
        {
//            val gujCommodityName = DatabaseManager.ExecuteScalar(Query.getGujaratiCommodityName(PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"").toString()))!!
//            commodityNameNDateBuilder.append(gujCommodityName)
            commodityNameNDateBuilder.append(PrefUtil.getString(PrefUtil.KEY_COMMODITY_NAME_GUJ,""))
        }
        commodityNameNDateBuilder.append(" - ")
        commodityNameNDateBuilder.append(DateUtility().getyyyyMMdd())
        binding.tvHeaderCommodityNDate.setText(commodityNameNDateBuilder.toString())

        FetchPCAAuctionDetailAPI(requireContext(), requireActivity(), this)
//        commodityBhartiRate = DatabaseManager.ExecuteScalar(Query.getCommodityBhartiByCommodityId(PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"").toString()))!!

//        shopNameList = getShopNameFromDB()
        shopNoList = getShopNoFromDb()

        getShopData()
        var shopAdapter:ArrayAdapter<String>
        if (PrefUtil.getSystemLanguage().equals("en"))
        {
//            _ShopDataList = getShopData().sortedBy { it.ShortShopName } as ArrayList<ShopSelectionData>
            _ShopDataList = ArrayList(getShopData().sortedBy { it.ShortShopName })
            val shopNameAdapter = commonUIUtility.getCustomArrayAdapter(ArrayList(_ShopDataList.map { it.ShortShopName }))
            binding.actShopNamePCAAuctionFragment.setAdapter(shopNameAdapter)
        }else
        {
            _ShopDataList = ArrayList(getShopData().sortedBy { it.ShortGujShopName })
            val shopNameAdapter = commonUIUtility.getCustomArrayAdapter(ArrayList(_ShopDataList.map { it.ShortGujShopName }))
            binding.actShopNamePCAAuctionFragment.setAdapter(shopNameAdapter)
        }

        binding.actShopNoPCAAuctionFragment.threshold = 100
        binding.actShopNamePCAAuctionFragment.threshold = 100

        binding.actShopNamePCAAuctionFragment.setOnItemClickListener { parent, view, position, id ->
            val selectedShop = _ShopDataList[position]
            Log.d(TAG, "onCreateView: SHOP_ID : ${selectedShop.ShopId}")
            Log.d(TAG, "onCreateView: SHOP_NO : ${selectedShop.ShopNo}")
            Log.d(TAG, "onCreateView: SHOP_NAME : ${selectedShop.ShortShopName}")
            Log.d(TAG, "onCreateView: SHOP_NAME_GUJ : ${selectedShop.ShortGujShopName}")
            shopId = selectedShop.ShopId
            binding.actShopNoPCAAuctionFragment.setText(selectedShop.ShopNo)
        }

        binding.edtBagsPCAAuctionFragment.filters =arrayOf<InputFilter>(EditableDecimalInputFilter(5, 2))
        binding.edtCurrentPricePCAAuctionFragment.filters =arrayOf<InputFilter>(EditableDecimalInputFilter(7, 2))


        binding.actShopNoPCAAuctionFragment.setOnItemClickListener { adapterView, view, i, l ->
            shopId = DatabaseManager.ExecuteScalar(Query.getShopIdByShopNo(binding.actShopNoPCAAuctionFragment.text.toString().trim(),PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString()))!!
            var shopName =if (PrefUtil.getSystemLanguage().toString().equals("en"))
            {
                DatabaseManager.ExecuteScalar(Query.getShortShopNameByShopNo(binding.actShopNoPCAAuctionFragment.text.toString().trim(),PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString()))!!
            }else
            {
                DatabaseManager.ExecuteScalar(Query.getGujShortShopNameByShopNo(binding.actShopNoPCAAuctionFragment.text.toString().trim(),PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString()))!!
            }

            binding.actShopNamePCAAuctionFragment.setText(shopName)
        }
//        binding.actShopNamePCAAuctionFragment.setOnItemClickListener { adapterView, view, i, l ->
//            var shopNo = if (PrefUtil.getSystemLanguage().toString().equals("en"))
//            {
//                DatabaseManager.ExecuteScalar(Query.getShopNoByShortShopName(binding.actShopNamePCAAuctionFragment.text.toString().trim(),PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString()))!!
//            }else
//            {
//                DatabaseManager.ExecuteScalar(Query.getShopNoByGujShortShopName(binding.actShopNamePCAAuctionFragment.text.toString().trim(),PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString()))!!
//            }
//            shopId = DatabaseManager.ExecuteScalar(Query.getShopIdByShopNo(shopNo,PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString()))!!
//            binding.actShopNoPCAAuctionFragment.setText(shopNo)
//        }

        binding.btnListPCAAuctionFragment.setOnClickListener {
            navController.navigate(PCAAuctionFragmentDirections.actionPCAAuctionFragmentToPCAAuctionListFragment(apiDataforPost))
        }

        binding.btnAddPCAAuctionFragment.setOnClickListener {
            val lowerSub =  BUYER_LOWER_LIMIT.split(".")[0]
            val upperSub = BUYER_UPPER_LIMIT.split(".")[0]
            Log.d(TAG, "onCreateView: LOWER_SUB_LENGTH : ${lowerSub.length}")
            Log.d(TAG, "onCreateView: UPPER_SUB_LENGTH : ${upperSub.length}")
            if (!binding.edtCurrentPricePCAAuctionFragment.text.toString().contains(".") && binding.edtCurrentPricePCAAuctionFragment.text.toString().length<lowerSub.length-1) {
                commonUIUtility.showAlertWithOkButton("Current Price Must be Greater Than Lower Limit Price")
            }else if (binding.edtCurrentPricePCAAuctionFragment.text.toString().contains(".") && binding.edtCurrentPricePCAAuctionFragment.text.toString().split(".")[0].length < lowerSub.length-1) {
                commonUIUtility.showAlertWithOkButton("Current Price Must be Lesser Than Upper Limit Price")
            }else if (!binding.edtCurrentPricePCAAuctionFragment.text.toString().contains(".") && binding.edtCurrentPricePCAAuctionFragment.text.toString().length > upperSub.length+1) {
                commonUIUtility.showAlertWithOkButton("Current Price Must be Lesser Than Upper Limit Price")
            }else if (binding.edtCurrentPricePCAAuctionFragment.text.toString().contains(".") && binding.edtCurrentPricePCAAuctionFragment.text.toString().split(".")[0].length > upperSub.length+1) {
                commonUIUtility.showAlertWithOkButton("Current Price Must be Lesser Than Upper Limit Price")
            }
            else if (shopId.equals("invalid")) {
                commonUIUtility.showAlertWithOkButton("Please Select proper shop!")
                binding.edtBagsPCAAuctionFragment.setText("")
            } else if (binding.actShopNamePCAAuctionFragment.text.toString()
                    .isEmpty() || binding.actShopNoPCAAuctionFragment.text.toString().isEmpty()
            ) {
                commonUIUtility.showAlertWithOkButton("Please Enter Shop Name or Shop No!")
            } else if (binding.edtBagsPCAAuctionFragment.text.toString().endsWith(".") || binding.edtBagsPCAAuctionFragment.text.toString().startsWith(".")) {
                commonUIUtility.showToast("Please Enter Valid Input!")
            } else if (binding.edtBagsPCAAuctionFragment.text.toString().toFloat() < 1) {
                commonUIUtility.showToast("Please Enter Bags!")
            } else if (shopNoList.isEmpty())
            {
                binding.actShopNoPCAAuctionFragment.isEnabled = false
                binding.actShopNamePCAAuctionFragment.isEnabled = false
                binding.actShopNoPCAAuctionFragment.setText("")
                binding.actShopNamePCAAuctionFragment.setText("")
                commonUIUtility.showToast("Shop List is Blank!")
            }else {
//                alertForSubmitData()
                postPCAData()
            }
        }

        //TextWatcher
        binding.edtBagsPCAAuctionFragment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0!!.toString().isEmpty()) {
                    binding.edtBagsPCAAuctionFragment.setText("0")
                    binding.tvRemainingBagsPCAAuctionFragment.setText(REMAINING_BAG)
                    binding.tvPurchasedBagsPCAAuctionFragment.setText(PURCHASED_BAG)
                    binding.edtBagsPCAAuctionFragment.setSelection(1)
                } else if (p0!!.toString().length >= 2 && p0!!.toString().startsWith("0")) {
                    val subStr =
                        binding.edtBagsPCAAuctionFragment.text.toString().substring(1)
                    binding.edtBagsPCAAuctionFragment.setText(subStr)
                    binding.edtBagsPCAAuctionFragment.setSelection(1)
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
                            StringBuilder(binding.edtBagsPCAAuctionFragment.text.toString())
                        stringBuilder.append("50")
                        binding.edtBagsPCAAuctionFragment.setText(stringBuilder.toString())
                        binding.edtBagsPCAAuctionFragment.setSelection(stringBuilder.length)
                        bags = stringBuilder.toString()
                    }else if (bags.toFloat() > 0) {
                        post_PurchasedBags = PURCHASED_BAG.toFloat() + bags.toFloat()
                        post_RemainingBags = BUYER_BORI.toFloat() - post_PurchasedBags
                        binding.tvRemainingBagsPCAAuctionFragment.setText(post_RemainingBags.toString())
                        binding.tvPurchasedBagsPCAAuctionFragment.setText(post_PurchasedBags.toString())
                    }else {
                        binding.tvRemainingBagsPCAAuctionFragment.setText(post_RemainingBags.toString())
                        binding.tvPurchasedBagsPCAAuctionFragment.setText(PURCHASED_BAG)
                    }
                    calculateExpense(pcaBoriList)
                }
            }
        })

    binding.edtBagsPCAAuctionFragment.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
        if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
            val text = binding.edtBagsPCAAuctionFragment.text.toString()
            if (text.contains(".")) {
                val text = binding.edtBagsPCAAuctionFragment.text.toString()
                val decimalIndex = text.indexOf(".")
                if (decimalIndex != -1) {
                    val newText = StringBuilder(text)
                    newText.delete(decimalIndex, newText.length)
                    binding.edtBagsPCAAuctionFragment.setText(newText.toString())
                    binding.edtBagsPCAAuctionFragment.setSelection(newText.length)
                    return@OnKeyListener true
                }
            }
        }
        false
    })
        binding.edtCurrentPricePCAAuctionFragment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0!!.toString().isNullOrBlank()) {
                    binding.edtCurrentPricePCAAuctionFragment.setText("0")
                    val avgPrice = NumberFormat.getCurrencyInstance().format(AVG_PRICE.toDouble()).substring(1)
                    binding.tvAveragePricePCAAuctionFragment.setText(avgPrice)
                    val totalAmount =
                        NumberFormat.getCurrencyInstance().format(TOTAL_AMOUNT.toDouble()).substring(1)
                    binding.tvTotalAmountPCAAuctionFragment.setText(totalAmount)
                    binding.edtCurrentPricePCAAuctionFragment.setSelection(1)
                } else if (p0!!.toString().length >= 2 && p0!!.toString().startsWith("0")) {
                    val subStr =
                        binding.edtCurrentPricePCAAuctionFragment.text.toString().substring(1)
                    binding.edtCurrentPricePCAAuctionFragment.setText(subStr)
                    binding.edtCurrentPricePCAAuctionFragment.setSelection(1)
                } else {
                    calculateExpense(pcaBoriList)
                }
            }
        })
        return binding.root
    }

    fun calculateExpense(dataList: ArrayList<ApiPCAAuctionDetail>) {
        try {
            var newbags = binding.edtBagsPCAAuctionFragment.text.toString().trim()
            var bags = ""
            if (newbags.contains("."))
            {
                val currentBag = newbags.split(".")[0]
                bags= "$currentBag.5"
            }else
            {
                bags= binding.edtBagsPCAAuctionFragment.text.toString().trim()
            }
            var currentPrice = binding.edtCurrentPricePCAAuctionFragment.text.toString().trim()
            if (bags.isNotEmpty() && currentPrice.isNotEmpty()) {
                //Current Calculation of Bags
                var total = 0.0
                if (bags.toFloat() > 0 && currentPrice.toDouble() > 0) {
                    total =
                        ((bags.toDouble() * commodityBhartiRate.toDouble()) / 20) * (currentPrice.toDouble())
                }
                val totalCostNF = NumberFormat.getCurrencyInstance().format(total).substring(1)
                post_CurrentTotal = total
                binding.edtTotalAmountPCAAuctionFragment.setText(totalCostNF.toString())

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
//                binding.tvAveragePricePCAAuctionFragment.setText(String.format("%.2f",post_AvgPrice))
                binding.tvAveragePricePCAAuctionFragment.setText(AvgPriceNF)

                Log.d(TAG, "calculateExpense: CUMULATIVE_TOTAL : $fomattedCumilativeTotal")
                val cumulativeTotalNF =NumberFormat.getCurrencyInstance().format(fomattedCumilativeTotal.toDouble() + total).substring(1)
                post_CumulativeTotal = fomattedCumilativeTotal.toDouble() + total
//                binding.tvTotalAmountPCAAuctionFragment.setText(String.format("%.2f",post_CumulativeTotal))
                binding.tvTotalAmountPCAAuctionFragment.setText(cumulativeTotalNF)

            } else {
                binding.edtTotalAmountPCAAuctionFragment.setText("")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "calculateExpense: ${e.message}")
        }
    }

    fun updateUIFromAPIData(apiDataModel: PCAAuctionDetailModel) {
        try {
            pcaBoriList = ArrayList()
            commodityBhartiRate = apiDataModel.CommodityBhartiPrice
            binding.tvRemainingBagsPCAAuctionFragment.setText(apiDataModel.RemainingBags)
            binding.tvBuyerBagsPCAAuctionFragment.setText(apiDataModel.BuyerBori)
            val BuyerULNF =
                NumberFormat.getCurrencyInstance().format(apiDataModel.BuyerUpperPrice.toDouble()).substring(1)
            binding.tvBuyersUpperLimitPCAAuctionFragment.setText(BuyerULNF.toString())
            val BuyerLLNF =
                NumberFormat.getCurrencyInstance().format(apiDataModel.BuyerLowerPrice.toDouble()).substring(1)
            binding.tvBuyersLowerLimitPCAAuctionFragment.setText(BuyerLLNF)
            binding.tvPurchasedBagsPCAAuctionFragment.setText(apiDataModel.TotalPurchasedBags)
            val AvgPriceNF =
                NumberFormat.getCurrencyInstance().format(apiDataModel.AvgPrice.toDouble()).substring(1)
            binding.tvAveragePricePCAAuctionFragment.setText(AvgPriceNF)
            val TotalCostNf =
                NumberFormat.getCurrencyInstance().format(apiDataModel.TotalCost.toDouble()).substring(1)
            binding.tvTotalAmountPCAAuctionFragment.setText(TotalCostNf)

            val BuyerBudgerNF =
                NumberFormat.getCurrencyInstance().format(apiDataModel.BuyerPCABudget.toDouble()).substring(1)
            binding.tvBuyersBudgetPCAAuctionFragment.setText(BuyerBudgerNF)
            BUYER_UPPER_LIMIT = apiDataModel.BuyerUpperPrice
            BUYER_LOWER_LIMIT = apiDataModel.BuyerLowerPrice
            TOTAL_AMOUNT = apiDataModel.TotalCost
            AVG_PRICE = apiDataModel.AvgPrice
            BUYER_BORI = apiDataModel.BuyerBori
            REMAINING_BAG = apiDataModel.RemainingBags
            PURCHASED_BAG = apiDataModel.TotalPurchasedBags
            pcaBoriList = apiDataModel.ApiPCAAuctionDetail
            apiDataforPost = apiDataModel

            Log.d(TAG, "updateUIFromAPIData: API_DATA_SHOP_LIST : $pcaBoriList")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "updateUIFromAPIData: ${e.printStackTrace()}")
        }
    }

    private fun getShopNameFromDB(): ArrayList<String> {
        var dataList: ArrayList<String> = ArrayList()
        try {
            var query = if (PrefUtil.getSystemLanguage().toString().equals("en"))
            {
                Query.getShortShopName(PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString())
            }else
            {
                Query.getGujShortShopName(PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString())
            }
            val cursor = DatabaseManager.ExecuteRawSql(query)
            if (cursor != null && cursor.count > 0) {
                dataList.clear()
                while (cursor.moveToNext()) {
                    if (PrefUtil.getSystemLanguage().toString().equals("en")){
                        dataList.add(cursor.getString(cursor.getColumnIndexOrThrow("ShortShopName")))
                    }else
                    {
                        dataList.add(cursor.getString(cursor.getColumnIndexOrThrow("GujaratiShortShopName")))
                    }
                }
                dataList.sort()
                val shopNameAdapter = commonUIUtility.getCustomArrayAdapter(dataList)
                binding.actShopNamePCAAuctionFragment.setAdapter(shopNameAdapter)
            }
            cursor?.close()
        } catch (e: Exception) {
            dataList.clear()
            e.printStackTrace()
            Log.e(TAG, "getShopNameFromDB: ${e.message}")
        }
        Log.d(TAG, "getShopNameFromDB: SHOPLIST : $dataList")
        return dataList
    }

    fun clearData()
    {
        binding.edtBagsPCAAuctionFragment.setText("")
        binding.edtCurrentPricePCAAuctionFragment.setText("")
        binding.edtTotalAmountPCAAuctionFragment.setText("0")
    }

    private fun
            getShopNoFromDb(): ArrayList<String> {
        var dataList: ArrayList<String> = ArrayList()
        try {
            val cursor = DatabaseManager.ExecuteRawSql(
                Query.getShopNo(
                    PrefUtil.getString(
                        PrefUtil.KEY_APMC_ID,
                        ""
                    ).toString()
                )
            )
            if (cursor != null && cursor.count > 0) {
                dataList.clear()
                while (cursor.moveToNext()) {
                    dataList.add(cursor.getString(cursor.getColumnIndexOrThrow("ShopNo")))
                }
//                dataList.sortWith(Comparator { str1, str2 ->
//                    val num1 = str1.toInt()
//                    val num2 = str2.toInt()
//                    num1.compareTo(num2)
//                })
                val newDataList = commonUIUtility.sortAlphanumericList(dataList)
                val shopNoAdapter = commonUIUtility.getCustomArrayAdapter(newDataList)
                binding.actShopNoPCAAuctionFragment.setAdapter(shopNoAdapter)
            }
            cursor?.close()
        } catch (e: Exception) {
            dataList.clear()
            e.printStackTrace()
            Log.e(TAG, "getShopNoFromDb: ${e.message}")
        }
        Log.d(TAG, "getShopNoFromDb: SHOPLIST : $dataList")
        return dataList
    }

    fun noAuctionPopup(message:String) {
        try {
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setTitle("Alert")
            alertDialog.setMessage(message)
            alertDialog.setCancelable(false)
            alertDialog.setNegativeButton("OK", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    p0!!.dismiss()
                    navController.navigate(PCAAuctionFragmentDirections.actionPCAAuctionFragmentToDashboardFragment())
                }
            })
            alertDialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "noAuctionPopup: ${e.message}")
        }
    }

    fun bindShopName(shopNo: String) {
        try {
            shopId = ""
            var shopName = DatabaseManager.ExecuteScalar(Query.getShopNameByShopNo(shopNo,PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString()))!!
            Log.d(TAG, "afterTextChanged: SELECTED_SHOP_NO : $shopNo")
            Log.d(TAG, "afterTextChanged: FETCHED_SHOP_NAME : $shopName")
            if (!shopName.equals("invalid", true)) {
                shopId = DatabaseManager.ExecuteScalar(Query.getShopIdByShopName(shopName,PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString()))!!
                binding.actShopNamePCAAuctionFragment.setText(shopName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindShopName: ${e.message}")
        }
    }

    fun bindShopNo(shopName: String) {
        try {
            shopId = ""
            var shopNo = DatabaseManager.ExecuteScalar(Query.getShopNoByShopName(shopName,PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString()))!!
            Log.d(TAG, "afterTextChanged: SELECTED_SHOP_NAME : $shopName")
            Log.d(TAG, "afterTextChanged: FETCHED_SHOP_NO : $shopNo")
            if (!shopNo.equals("invalid", true)) {
                shopId = DatabaseManager.ExecuteScalar(Query.getShopIdByShopNo(shopNo,PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString()))!!
                binding.actShopNoPCAAuctionFragment.setText(shopNo)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindShopNo: ${e.message}")
        }
    }

    private fun alertForSubmitData() {
        try {
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setTitle("Alert!")
            alertDialog.setMessage("Do you want to Submit Data?")
            alertDialog.setPositiveButton("Submit", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    postPCAData()
                    p0!!.dismiss()
                }
            })
            alertDialog.setNegativeButton("No", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    p0!!.dismiss()
                }
            })

            alertDialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "alertForSubmitData: ${e.message}")
        }
    }

    private fun postPCAData() {
        try {
            var currentBags = ""
            if (binding.edtBagsPCAAuctionFragment.text.toString().contains("."))
            {
                val newBag = binding.edtBagsPCAAuctionFragment.text.toString().split(".")[0]
                currentBags = "$newBag.5"
            }else
            {
                currentBags = binding.edtBagsPCAAuctionFragment.text.toString().trim()
            }

            val model = POSTPCAAuctionData(
                apiDataforPost.PCAAuctionHeaderId,
                "",
                apiDataforPost.PCAAuctionMasterId,
                DateUtility().getyyyyMMdd(),
                PrefUtil.getString(PrefUtil.KEY_BUYER_ID, "").toString(),
                "3",
                apiDataforPost.PCARegId,
                apiDataforPost.PCAId,
                PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"").toString(),
                commodityBhartiRate,
                PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString(),
                apiDataforPost.AuctionMasterId,
                apiDataforPost.BuyerBori,
                apiDataforPost.BuyerLowerPrice,
                apiDataforPost.BuyerUpperPrice,
                "%.2f".format(post_AvgPrice),
                post_RemainingBags.toString(),
                "%.2f".format(post_CumulativeTotal),
                post_PurchasedBags.toString(),
                shopId,
                binding.actShopNoPCAAuctionFragment.text.toString().trim(),
                currentBags,
                binding.edtCurrentPricePCAAuctionFragment.text.toString().trim(),
                "%.2f".format(post_CurrentTotal),
                PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,"").toString(),
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString(),
                "",
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString(),
                DateUtility().getyyyyMMdd(),
               "Confirm"
            )

            if (ConnectionCheck.isConnected(requireContext()))
            {
                POSTPCAAuctionDetailAPI(requireContext(),requireActivity(),this,model)
            }else{
                commonUIUtility.showToast(getString(R.string.no_internet_connection))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "postPCAData: ${e.message}")
        }
    }

    fun redirectToLogin(){
        try {
            PrefUtil.setBoolean(PrefUtil.KEY_LOGGEDIN,false)
            requireActivity().startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "redirectToLogin: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getShopData():ArrayList<ShopSelectionData> {
        var shopDataList = ArrayList<ShopSelectionData>()
        try {
            val cursor = DatabaseManager.ExecuteRawSql(Query.getShopData())
            if (cursor != null && cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val model = ShopSelectionData(
                        cursor.getString(cursor.getColumnIndexOrThrow("ShopId")),
                        cursor.getString(cursor.getColumnIndexOrThrow("ShopNo")),
                        cursor.getString(cursor.getColumnIndexOrThrow("ShortShopName")),
                        cursor.getString(cursor.getColumnIndexOrThrow("GujaratiShortShopName"))
                    )

                    shopDataList.add(model)
                }
                val newShopNoList = shopDataList.map { it.ShopNo } as ArrayList<String>
                val newDataList = commonUIUtility.sortAlphanumericList(newShopNoList)
                val shopNoAdapter = commonUIUtility.getCustomArrayAdapter(newDataList)
                binding.actShopNoPCAAuctionFragment.setAdapter(shopNoAdapter)
                Log.d(TAG, "getShopData: NEW_SHOPLIST : $shopDataList")
            }
            cursor?.close()
        } catch (e: Exception) {
            shopDataList.clear()
            e.printStackTrace()
            Log.e(TAG, "getShopData: ${e.message}")
        }
        return shopDataList
    }

    data class ShopSelectionData(val ShopId:String,var ShopNo:String,var ShortShopName:String,var ShortGujShopName:String)
}
