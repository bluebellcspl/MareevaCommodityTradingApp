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
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.PCAAuctionListAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.EditableDecimalInputFilter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentPCAAuctionListBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.PcaAuctionDetailDailogLayoutBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.PCAAuctionFragment.ShopSelectionData
import com.bluebellcspl.maarevacommoditytradingapp.master.POSTPCAAuctionDeleteAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.POSTPCAAuctionDetailAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.ApiPCAAuctionDetail
import com.bluebellcspl.maarevacommoditytradingapp.model.AuctionDetailsModel
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionPCAListModel
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAAuctionDetailModel
import com.bluebellcspl.maarevacommoditytradingapp.model.POSTPCAAuctionData
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.RecyclerViewHelper
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.SwipeToDeleteCallback
import com.google.android.material.textfield.TextInputEditText
import java.text.DecimalFormat

class PCAAuctionListFragment : Fragment(), RecyclerViewHelper {
    var _binding: FragmentPCAAuctionListBinding? = null
    val binding get() = _binding!!
    private val commonUIUtility: CommonUIUtility by lazy { CommonUIUtility(requireContext()) }
    val TAG = "PCAAuctionListFragment"
    private val navController by lazy { findNavController() }
    val args by navArgs<PCAAuctionListFragmentArgs>()
    lateinit var adapter: PCAAuctionListAdapter
    lateinit var pcaAuctionList: ArrayList<ApiPCAAuctionDetail>
    private val commodityBhartiRate:String by lazy { args.pcaAuctionDetailModel.CommodityBhartiPrice }
    private  val  pcaAuctionDataModel by lazy { args.pcaAuctionDetailModel }
    var post_CurrentTotal = 0.0
    var post_CurrentPrice = 0.0
    var post_AvgPrice = 0.0
    var post_CumulativeTotal = 0.0
    private lateinit var _ShopDataList : ArrayList<ShopSelectionData>
    private lateinit var newShopNoAdapter: ArrayAdapter<String>
    var shopId = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_p_c_a_auction_list,
            container,
            false
        )
//        commodityBhartiRate = DatabaseManager.ExecuteScalar(Query.getCommodityBhartiByCommodityId(PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"").toString()))!!
        binding.rcViewPCAAuctionListFrament.layoutManager = LinearLayoutManager(requireContext())
        val swipeToDelete = object: SwipeToDeleteCallback(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
//                pcaAuctionList.removeAt(position)
//                binding.rcViewPCAAuctionListFrament.adapter?.notifyItemRemoved(position)

                val alertDialog = AlertDialog.Builder(requireContext())
                alertDialog.setTitle("Alert!")
                alertDialog.setMessage(requireContext().getString(R.string.do_you_want_to_delete_pca_data_alert_msg))
                alertDialog.setPositiveButton(requireContext().getString(R.string.delete),object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        if (ConnectionCheck.isConnected(requireContext()))
                        {
                            POSTPCAAuctionDeleteAPI(requireContext(),requireActivity(),this@PCAAuctionListFragment,pcaAuctionList,position,adapter)
                        }else{
                            commonUIUtility.showToast(getString(R.string.no_internet_connection))
                        }
                        p0!!.dismiss()
                    }
                })
                alertDialog.setNegativeButton(requireContext().getString(R.string.no),object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {

                        adapter.notifyDataSetChanged()
                        p0!!.dismiss()
                    }
                })
                alertDialog.show()
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDelete)
        itemTouchHelper.attachToRecyclerView(binding.rcViewPCAAuctionListFrament)
        pcaAuctionList = args.pcaAuctionDetailModel.ApiPCAAuctionDetail
        getShopData()
        bindAuctionList(args.pcaAuctionDetailModel)
        return binding.root
    }

     fun bindAuctionList(model: PCAAuctionDetailModel) {
         pcaAuctionList = model.ApiPCAAuctionDetail
         val dataList = model.ApiPCAAuctionDetail
        if (dataList.isNotEmpty()) {
            adapter = PCAAuctionListAdapter(requireContext(), dataList, this)
            binding.rcViewPCAAuctionListFrament.adapter = adapter
            binding.rcViewPCAAuctionListFrament.invalidate()
        } else {
            commonUIUtility.showToast("No Auction List!")
        }
    }

    fun showPCAAddAuctionDialog(model: ApiPCAAuctionDetail) {
        try {
            val alertDailogBuilder = AlertDialog.Builder(requireContext())
            val dialogBinding = PcaAuctionDetailDailogLayoutBinding.inflate(layoutInflater)
            val dialogView = dialogBinding.root
            alertDailogBuilder.setView(dialogView)
            val alertDialog = alertDailogBuilder.create()
            alertDialog.setCanceledOnTouchOutside(true)
            alertDialog.setCancelable(true)
            alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()
            shopId = model.ShopId
            post_CurrentTotal = model.Amount.toDouble()
            post_CurrentPrice = model.CurrentPrice.toDouble()

            dialogBinding.actShopNoPCAAuctionDialog.setText(model.ShopNo)
            dialogBinding.edtBagsPCAAuctionDialog.setText(model.Bags)
            dialogBinding.edtCurrentPricePCAAuctionDialog.setText(model.CurrentPrice)
            val amountNF = NumberFormat.getCurrencyInstance().format(model.Amount.toDouble())
            dialogBinding.edtTotalAmountPCAAuctionDialog.setText(amountNF)

//            val shopNameAdapter = commonUIUtility.getCustomArrayAdapter(getShopNameFromDB())
            if (PrefUtil.getSystemLanguage().equals("en"))
            {
                dialogBinding.actShopNamePCAAuctionDialog.setText(model.ShortShopName)
                _ShopDataList = ArrayList(getShopData().sortedBy { it.ShortShopName })
                val shopNameAdapter = commonUIUtility.getCustomArrayAdapter(ArrayList(_ShopDataList.map { it.ShortShopName }))
                dialogBinding.actShopNamePCAAuctionDialog.setAdapter(shopNameAdapter)
            }else
            {
                dialogBinding.actShopNamePCAAuctionDialog.setText(model.GujaratiShortShopName)
                _ShopDataList = ArrayList(getShopData().sortedBy { it.ShortGujShopName })
                val shopNameAdapter = commonUIUtility.getCustomArrayAdapter(ArrayList(_ShopDataList.map { it.ShortGujShopName }))
                dialogBinding.actShopNamePCAAuctionDialog.setAdapter(shopNameAdapter)
            }

            dialogBinding.actShopNoPCAAuctionDialog.setAdapter(newShopNoAdapter)

            dialogBinding.edtBagsPCAAuctionDialog.filters =arrayOf<InputFilter>(
                EditableDecimalInputFilter(5, 2)
            )
            dialogBinding.edtCurrentPricePCAAuctionDialog.filters =arrayOf<InputFilter>(
                EditableDecimalInputFilter(7, 2)
            )
            dialogBinding.actShopNoPCAAuctionDialog.setOnItemClickListener { adapterView, view, i, l ->
                var shopName = DatabaseManager.ExecuteScalar(Query.getShopNameByShopNo(dialogBinding.actShopNoPCAAuctionDialog.text.toString().trim(),PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString()))!!
                shopId = DatabaseManager.ExecuteScalar(Query.getShopIdByShopNo(dialogBinding.actShopNoPCAAuctionDialog.text.toString().trim(),PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString()))!!
                dialogBinding.actShopNamePCAAuctionDialog.setText(shopName)
            }

            dialogBinding.actShopNamePCAAuctionDialog.setOnItemClickListener { parent, view, position, id ->
                val selectedShop = _ShopDataList[position]
                Log.d(TAG, "onCreateView: SHOP_ID : ${selectedShop.ShopId}")
                Log.d(TAG, "onCreateView: SHOP_NO : ${selectedShop.ShopNo}")
                Log.d(TAG, "onCreateView: SHOP_NAME : ${selectedShop.ShortShopName}")
                Log.d(TAG, "onCreateView: SHOP_NAME_GUJ : ${selectedShop.ShortGujShopName}")
                shopId = selectedShop.ShopId
                dialogBinding.actShopNoPCAAuctionDialog.setText(selectedShop.ShopNo)
            }
            dialogBinding.edtBagsPCAAuctionDialog.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun afterTextChanged(p0: Editable?) {
                    if (p0!!.toString().isEmpty()) {
                        dialogBinding.edtBagsPCAAuctionDialog.setText("0")
                        dialogBinding.edtBagsPCAAuctionDialog.setSelection(1)
                    } else if (p0!!.toString().length >= 2 && p0!!.toString().startsWith("0")) {
                        val subStr =
                            dialogBinding.edtBagsPCAAuctionDialog.text.toString().substring(1)
                        dialogBinding.edtBagsPCAAuctionDialog.setText(subStr)
                        dialogBinding.edtBagsPCAAuctionDialog.setSelection(1)
                    }else {
                        if (p0!!.endsWith(".")) {
                            val stringBuilder =
                                StringBuilder(dialogBinding.edtBagsPCAAuctionDialog.text.toString())
                            stringBuilder.append("50")
                            dialogBinding.edtBagsPCAAuctionDialog.setText(stringBuilder.toString())
                            dialogBinding.edtBagsPCAAuctionDialog.setSelection(stringBuilder.length)
                        }
                        calculateExpense(
                            dialogBinding.edtBagsPCAAuctionDialog.text.toString().trim(),
                            dialogBinding.edtCurrentPricePCAAuctionDialog.text.toString().trim(),
                            pcaAuctionList,
                            dialogBinding.edtTotalAmountPCAAuctionDialog
                        )
                    }
                }
            })

            dialogBinding.edtBagsPCAAuctionDialog.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    val text = dialogBinding.edtBagsPCAAuctionDialog.text.toString()
                    if (text.contains(".")) {
                        val text = dialogBinding.edtBagsPCAAuctionDialog.text.toString()
                        val decimalIndex = text.indexOf(".")
                        if (decimalIndex != -1) {
                            val newText = StringBuilder(text)
                            newText.delete(decimalIndex, newText.length)
                            dialogBinding.edtBagsPCAAuctionDialog.setText(newText.toString())
                            dialogBinding.edtBagsPCAAuctionDialog.setSelection(newText.length)
                            return@OnKeyListener true
                        }
                    }
                }
                false
            })
            dialogBinding.edtCurrentPricePCAAuctionDialog.addTextChangedListener(object :
                TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun afterTextChanged(p0: Editable?) {
                    if (p0!!.toString().isNullOrBlank()) {
                        dialogBinding.edtCurrentPricePCAAuctionDialog.setText("0")
                        dialogBinding.edtCurrentPricePCAAuctionDialog.setSelection(1)
                    } else if (p0!!.toString().length >= 2 && p0!!.toString().startsWith("0")) {
                        val subStr = dialogBinding.edtCurrentPricePCAAuctionDialog.text.toString()
                            .substring(1)
                        dialogBinding.edtCurrentPricePCAAuctionDialog.setText(subStr)
                        dialogBinding.edtCurrentPricePCAAuctionDialog.setSelection(1)
                    } else {
                        calculateExpense(
                            dialogBinding.edtBagsPCAAuctionDialog.text.toString().trim(),
                            dialogBinding.edtCurrentPricePCAAuctionDialog.text.toString().trim(),
                            pcaAuctionList,
                            dialogBinding.edtTotalAmountPCAAuctionDialog
                        )
                    }
                }
            })

            dialogBinding.btnAddPCAAuctionDialog.setOnClickListener {

                if (dialogBinding.edtCurrentPricePCAAuctionDialog.text.toString().toDouble() <1) {
                    commonUIUtility.showAlertWithOkButton("Please Enter Current Price!")
                } else if (dialogBinding.actShopNamePCAAuctionDialog.text.toString().isEmpty() || dialogBinding.actShopNoPCAAuctionDialog.text.toString().isEmpty()) {
                    commonUIUtility.showAlertWithOkButton("Please Enter Shop Name or Shop No!")
                } else if (dialogBinding.edtBagsPCAAuctionDialog.text.toString().toFloat() < 1) {
                    commonUIUtility.showToast("Please Enter Bags!")
                }else if (dialogBinding.edtBagsPCAAuctionDialog.text.toString().endsWith(".") || dialogBinding.edtBagsPCAAuctionDialog.text.toString().startsWith(".")){
                    commonUIUtility.showToast(getString(R.string.please_enter_valid_bags_alert_msg))
                }else if (dialogBinding.edtCurrentPricePCAAuctionDialog.text.toString().endsWith(".") || dialogBinding.edtCurrentPricePCAAuctionDialog.text.toString().startsWith(".")){
                    commonUIUtility.showToast(getString(R.string.please_enter_valid_input_alert_msg))
                }
                else {
                    var bags = dialogBinding.edtBagsPCAAuctionDialog.text.toString().trim()
                    model.Amount = DecimalFormat("0.00").format(post_CurrentTotal)
                    model.Bags = DecimalFormat("0.00").format(bags.toFloat())
                    model.ShopNo = dialogBinding.actShopNoPCAAuctionDialog.text.toString().trim()
                    model.CurrentPrice = DecimalFormat("0.00").format(post_CurrentPrice)
                    alertDialog.dismiss()
                    updatePCAData(model)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "showTaskAllocationDialog: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun updatePCAData(model: ApiPCAAuctionDetail) {
        try {
            val postDataModel = POSTPCAAuctionData(
                "",
                model.PCAAuctionDetailId,
                model.PCAAuctionMasterId,
                DateUtility().getyyyyMMdd(),
                "",
                "",
                pcaAuctionDataModel.PCARegId,
                "",
                pcaAuctionDataModel.CommodityId,
                pcaAuctionDataModel.CommodityBhartiPrice,
                pcaAuctionDataModel.APMCId,
                pcaAuctionDataModel.AuctionMasterId,
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                shopId,
                model.ShopNo,
                model.Bags,
                model.CurrentPrice,
                model.Amount,
                PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE, "").toString(),
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString(),
                DateUtility().getyyyyMMdd(),
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString(),
                DateUtility().getyyyyMMdd(),
                "update"
            )
            if (ConnectionCheck.isConnected(requireContext()))
            {
                POSTPCAAuctionDetailAPI(requireContext(),requireActivity(),this,postDataModel)
            }else{
                commonUIUtility.showToast(getString(R.string.no_internet_connection))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "updatePCAData: ${e.message}")
        }
    }

    fun calculateExpense(
        bags: String,
        currentPrice: String,
        dataList: ArrayList<ApiPCAAuctionDetail>,
        view: TextInputEditText
    ) {
        try {
            if (bags.isNotEmpty() && currentPrice.isNotEmpty()) {
                //Current Calculation of Bags
                var total = 0.0
                var latestBags = ""
                if (bags.contains("."))
                {
                    val currentBag = bags.split(".")[0]
                    latestBags = "$currentBag.5"
                }else
                {
                    latestBags = bags
                }
                if (latestBags.toFloat() > 0 && currentPrice.toDouble() > 0) {
                    total =
                        ((latestBags.toFloat() * commodityBhartiRate.toDouble()) / 20) * (currentPrice.toDouble())
                }
                val totalCostNF = NumberFormat.getCurrencyInstance().format(total)
                post_CurrentPrice = currentPrice.toDouble()
                post_CurrentTotal = total
                view.setText(totalCostNF.toString())

                //Header Calculation for Total Cost from ArrayList
                var cumulativeTotal = 0.0
                var totalPurchasedBags = 0f
                for (i in 0 until dataList.size) {
                    cumulativeTotal += dataList[i].Amount.toDouble()
                    totalPurchasedBags += dataList[i].Bags.toFloat()
                }
                post_AvgPrice =
                    (cumulativeTotal + total) / (((totalPurchasedBags + latestBags.toFloat()) * commodityBhartiRate.toDouble()) / 20)
                Log.d(TAG, "calculateExpense: AVG_PRICE : $post_AvgPrice")
//                val AvgPriceNF = NumberFormat.getCurrencyInstance().format(post_AvgPrice)
//                binding.tvAveragePricePCAAuctionFragment.setText(AvgPriceNF)

                Log.d(TAG, "calculateExpense: CUMULATIVE_TOTAL : $cumulativeTotal")
                val cumulativeTotalNF =
                    NumberFormat.getCurrencyInstance().format(cumulativeTotal + total)
                post_CumulativeTotal = cumulativeTotal + total
//                binding.tvTotalAmountPCAAuctionFragment.setText(cumulativeTotalNF)

            } else {
                view.setText("")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "calculateExpense: ${e.message}")
        }
    }
//    private fun getShopNameFromDB(): ArrayList<String> {
//        var dataList: ArrayList<String> = ArrayList()
//        try {
//            val cursor = DatabaseManager.ExecuteRawSql(
//                Query.getShopName(PrefUtil.getString(PrefUtil.KEY_APMC_ID, "").toString())
//            )
//            if (cursor != null && cursor.count > 0) {
//                dataList.clear()
//                while (cursor.moveToNext()) {
//                    dataList.add(cursor.getString(cursor.getColumnIndexOrThrow("ShopName")))
//                }
//                dataList.sort()
//            }
//            cursor?.close()
//        } catch (e: Exception) {
//            dataList.clear()
//            e.printStackTrace()
//            Log.e(TAG, "getShopNameFromDB: ${e.message}")
//        }
//        Log.d(TAG, "getShopNameFromDB: SHOPLIST : $dataList")
//        return dataList
//    }
//
//    private fun getShopNoFromDb(): ArrayList<String> {
//        var dataList: ArrayList<String> = ArrayList()
//        try {
//            val cursor = DatabaseManager.ExecuteRawSql(
//                Query.getShopNo(
//                    PrefUtil.getString(
//                        PrefUtil.KEY_APMC_ID,
//                        ""
//                    ).toString()
//                )
//            )
//            if (cursor != null && cursor.count > 0) {
//                dataList.clear()
//                while (cursor.moveToNext()) {
//                    dataList.add(cursor.getString(cursor.getColumnIndexOrThrow("ShopNo")))
//                }
//                dataList.sortWith(Comparator { str1, str2 ->
//                    val num1 = str1.toInt()
//                    val num2 = str2.toInt()
//                    num1.compareTo(num2)
//                })
//            }
//            cursor?.close()
//        } catch (e: Exception) {
//            dataList.clear()
//            e.printStackTrace()
//            Log.e(TAG, "getShopNoFromDb: ${e.message}")
//        }
//        Log.d(TAG, "getShopNoFromDb: SHOPLIST : $dataList")
//        return dataList
//    }

    override fun onItemClick(postion: Int, onclickType: String) {
        showPCAAddAuctionDialog(pcaAuctionList[postion])
    }

    override fun onBuyerAuctionPCAItemClick(postion: Int, model: AuctionDetailsModel) {

    }

    override fun getBuyerAuctionDataList(dataList: ArrayList<AuctionDetailsModel>) {

    }

    override fun getLiveAuctionPCAData(postion: Int, model: LiveAuctionPCAListModel) {
        TODO("Not yet implemented")
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
                 newShopNoAdapter = commonUIUtility.getCustomArrayAdapter(newDataList)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}