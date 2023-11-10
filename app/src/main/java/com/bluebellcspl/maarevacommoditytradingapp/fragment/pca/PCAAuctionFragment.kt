package com.bluebellcspl.maarevacommoditytradingapp.fragment.pca

import android.app.AlertDialog
import android.content.DialogInterface
import android.icu.text.NumberFormat
import android.os.Bundle
import android.text.Editable
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
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentPCAAuctionBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchPCAAuctionDetailAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.POSTPCAAuctionDetailAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.ApiPCAAuctionDetail
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAAuctionDetailModel
import com.bluebellcspl.maarevacommoditytradingapp.model.POSTPCAAuctionData


class PCAAuctionFragment : Fragment() {
    lateinit var binding: FragmentPCAAuctionBinding
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
    var post_RemainingBags = 0
    var post_PurchasedBags = 0
    var post_CurrentTotal = 0.0
    lateinit var apiDataforPost: PCAAuctionDetailModel
    lateinit var commodityBhartiRate: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_p_c_a_auction, container, false)
        binding.tvHeaderCommodityNDate.setText(
            "${
                PrefUtil.getString(
                    PrefUtil.KEY_COMMODITY_NAME,
                    ""
                )
            } - ${DateUtility().getyyyyMMdd()}"
        )
        FetchPCAAuctionDetailAPI(requireContext(), requireActivity(), this)
        commodityBhartiRate = DatabaseManager.ExecuteScalar(
            Query.getCommodityBhartiByCommodityId(
                PrefUtil.getString(
                    PrefUtil.KEY_COMMODITY_ID,
                    ""
                ).toString()
            )
        )!!
        shopNameList = getShopNameFromDB()
        shopNoList = getShopNoFromDb()

        //OnFocusChangeListener
        binding.edtBagsPCAAuctionFragment.onFocusChangeListener =
            View.OnFocusChangeListener { view: View?, b: Boolean ->
                if (b) {
                    if (binding.actShopNoPCAAuctionFragment.text.toString().isNotEmpty()) {
                        bindShopName(binding.actShopNoPCAAuctionFragment.text.toString().trim())
                    } else {
                        bindShopNo(binding.actShopNamePCAAuctionFragment.text.toString().trim())
                    }
                }
            }

        binding.btnListPCAAuctionFragment.setOnClickListener {
            navController.navigate(PCAAuctionFragmentDirections.actionPCAAuctionFragmentToPCAAuctionListFragment(apiDataforPost))
        }

        binding.btnAddPCAAuctionFragment.setOnClickListener {
            if (binding.edtCurrentPricePCAAuctionFragment.text.toString()
                    .toDouble() < BUYER_LOWER_LIMIT.toDouble()
            ) {
                commonUIUtility.showAlertWithOkButton("Current Price Must be Greater Than Lower Limit Price")
            } else if (binding.edtCurrentPricePCAAuctionFragment.text.toString()
                    .toDouble() > BUYER_UPPER_LIMIT.toDouble()
            ) {
                commonUIUtility.showAlertWithOkButton("Current Price Must be Lesser Than Upper Limit Price")
            } else if (binding.actShopNamePCAAuctionFragment.text.toString()
                    .isEmpty() || binding.actShopNoPCAAuctionFragment.text.toString().isEmpty()
            ) {
                commonUIUtility.showAlertWithOkButton("Please Enter Shop Name or Shop No!")
            } else if (binding.edtBagsPCAAuctionFragment.text.toString().toInt() < 1) {
                commonUIUtility.showToast("Please Enter Bags!")
            } else {
                alertForSubmitData()
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
                    var bags = p0!!.toString().trim()
                    if (bags.toInt() > 0) {
                        post_PurchasedBags = PURCHASED_BAG.toInt() + bags.toInt()
                        post_RemainingBags = BUYER_BORI.toInt() - post_PurchasedBags
                        binding.tvRemainingBagsPCAAuctionFragment.setText(post_RemainingBags.toString())
                        binding.tvPurchasedBagsPCAAuctionFragment.setText(post_PurchasedBags.toString())
                    } else {
                        binding.tvRemainingBagsPCAAuctionFragment.setText(post_RemainingBags.toString())
                        binding.tvPurchasedBagsPCAAuctionFragment.setText(PURCHASED_BAG)
                    }
                    calculateExpense(pcaBoriList)
                }
            }
        })
        binding.edtCurrentPricePCAAuctionFragment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0!!.toString().isNullOrBlank()) {
                    binding.edtCurrentPricePCAAuctionFragment.setText("0")
                    val avgPrice = NumberFormat.getCurrencyInstance().format(AVG_PRICE.toDouble())
                    binding.tvAveragePricePCAAuctionFragment.setText(avgPrice)
                    val totalAmount =
                        NumberFormat.getCurrencyInstance().format(TOTAL_AMOUNT.toDouble())
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
            var bags = binding.edtBagsPCAAuctionFragment.text.toString().trim()
            var currentPrice = binding.edtCurrentPricePCAAuctionFragment.text.toString().trim()
            if (bags.isNotEmpty() && currentPrice.isNotEmpty()) {
                //Current Calculation of Bags
                var total = 0.0
                if (bags.toInt() > 0 && currentPrice.toDouble() > 0) {
                    total =
                        ((bags.toDouble() * commodityBhartiRate.toDouble()) / 20) * (currentPrice.toDouble())
                }
                val totalCostNF = NumberFormat.getCurrencyInstance().format(total)
                post_CurrentTotal = total
                binding.edtTotalAmountPCAAuctionFragment.setText(totalCostNF.toString())

                //Header Calculation for Total Cost from ArrayList
                var cumulativeTotal = 0.0
                var totalPurchasedBags = 0
                for (i in 0 until dataList.size) {
                    cumulativeTotal += dataList[i].Amount.toDouble()
                    totalPurchasedBags += dataList[i].Bags.toInt()
                }
                post_AvgPrice =
                    (cumulativeTotal + total) / (((totalPurchasedBags + bags.toInt()) * commodityBhartiRate.toDouble()) / 20)
                Log.d(TAG, "calculateExpense: AVG_PRICE : $post_AvgPrice")
                val AvgPriceNF = NumberFormat.getCurrencyInstance().format(post_AvgPrice)
                binding.tvAveragePricePCAAuctionFragment.setText(AvgPriceNF)

                Log.d(TAG, "calculateExpense: CUMULATIVE_TOTAL : $cumulativeTotal")
                val cumulativeTotalNF =
                    NumberFormat.getCurrencyInstance().format(cumulativeTotal + total)
                post_CumulativeTotal = cumulativeTotal + total
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
            binding.tvRemainingBagsPCAAuctionFragment.setText(apiDataModel.RemainingBags)
            binding.tvBuyerBagsPCAAuctionFragment.setText(apiDataModel.BuyerBori)
            val BuyerULNF =
                NumberFormat.getCurrencyInstance().format(apiDataModel.BuyerUpperPrice.toDouble())
            binding.tvBuyersUpperLimitPCAAuctionFragment.setText(BuyerULNF.toString())
            val BuyerLLNF =
                NumberFormat.getCurrencyInstance().format(apiDataModel.BuyerLowerPrice.toDouble())
            binding.tvBuyersLowerLimitPCAAuctionFragment.setText(BuyerLLNF)
            binding.tvPurchasedBagsPCAAuctionFragment.setText(apiDataModel.TotalPurchasedBags)
            val AvgPriceNF =
                NumberFormat.getCurrencyInstance().format(apiDataModel.AvgPrice.toDouble())
            binding.tvAveragePricePCAAuctionFragment.setText(AvgPriceNF)
            val TotalCostNf =
                NumberFormat.getCurrencyInstance().format(apiDataModel.TotalCost.toDouble())
            binding.tvTotalAmountPCAAuctionFragment.setText(TotalCostNf)
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
            val cursor = DatabaseManager.ExecuteRawSql(
                Query.getShopName(PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString())
            )
            if (cursor != null && cursor.count > 0) {
                dataList.clear()
                while (cursor.moveToNext()) {
                    dataList.add(cursor.getString(cursor.getColumnIndexOrThrow("ShopName")))
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

    private fun getShopNoFromDb(): ArrayList<String> {
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
                dataList.sortWith(Comparator { str1, str2 ->
                    val num1 = str1.toInt()
                    val num2 = str2.toInt()
                    num1.compareTo(num2)
                })
                val shopNoAdapter = commonUIUtility.getCustomArrayAdapter(dataList)
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

    fun noAuctionPopup() {
        try {
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setTitle("Alert")
            alertDialog.setMessage("No Auction Today,Buyer has not allocated any Auction Today!")
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
                shopId = DatabaseManager.ExecuteScalar(Query.getShopIdByShopNo(shopName,PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString()))!!
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
                apiDataforPost.BuyerBori,
                apiDataforPost.BuyerLowerPrice,
                apiDataforPost.BuyerUpperPrice,
                "%.2f".format(post_AvgPrice),
                post_RemainingBags.toString(),
                "%.2f".format(post_CumulativeTotal),
                post_PurchasedBags.toString(),
                shopId,
                binding.actShopNoPCAAuctionFragment.text.toString().trim(),
                binding.edtBagsPCAAuctionFragment.text.toString().trim(),
                binding.edtCurrentPricePCAAuctionFragment.text.toString().trim(),
                "%.2f".format(post_CurrentTotal),
                PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,"").toString(),
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString(),
                DateUtility().getyyyyMMdd(),
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString(),
                DateUtility().getyyyyMMdd(),
               "Confirm"
            )

            POSTPCAAuctionDetailAPI(requireContext(),requireActivity(),this,model)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "postPCAData: ${e.message}")
        }
    }
}