package com.bluebellcspl.maarevacommoditytradingapp.fragment.pca

import ConnectionCheck
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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.PCAAuctionListAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentPCAAuctionListBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.PcaAuctionDetailDailogLayoutBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.POSTPCAAuctionDeleteAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.POSTPCAAuctionDetailAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.ApiPCAAuctionDetail
import com.bluebellcspl.maarevacommoditytradingapp.model.AuctionDetailsModel
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionPCAListModel
import com.bluebellcspl.maarevacommoditytradingapp.model.POSTPCAAuctionData
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.RecyclerViewHelper
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.SwipeToDeleteCallback
import com.google.android.material.textfield.TextInputEditText

class PCAAuctionListFragment : Fragment(), RecyclerViewHelper {
    lateinit var binding: FragmentPCAAuctionListBinding
    private val commonUIUtility: CommonUIUtility by lazy { CommonUIUtility(requireContext()) }
    val TAG = "PCAAuctionListFragment"
    private val navController by lazy { findNavController() }
    val args by navArgs<PCAAuctionListFragmentArgs>()
    lateinit var adapter: PCAAuctionListAdapter
    lateinit var pcaAuctionList: ArrayList<ApiPCAAuctionDetail>
    lateinit var commodityBhartiRate: String
    var shopId = "0.0"
    var post_CurrentTotal = 0.0
    var post_CurrentPrice = 0.0
    var post_AvgPrice = 0.0
    var post_CumulativeTotal = 0.0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_p_c_a_auction_list,
            container,
            false
        )
        commodityBhartiRate = DatabaseManager.ExecuteScalar(
            Query.getCommodityBhartiByCommodityId(
                PrefUtil.getString(
                    PrefUtil.KEY_COMMODITY_ID,
                    ""
                ).toString()
            )
        )!!
        binding.rcViewPCAAuctionListFrament.layoutManager = LinearLayoutManager(requireContext())
        val swipeToDelete = object: SwipeToDeleteCallback(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
//                pcaAuctionList.removeAt(position)
//                binding.rcViewPCAAuctionListFrament.adapter?.notifyItemRemoved(position)

                val alertDialog = AlertDialog.Builder(requireContext())
                alertDialog.setTitle("Alert!")
                alertDialog.setMessage("Do you want to Delete PCA Data?")
                alertDialog.setPositiveButton("Delete",object : DialogInterface.OnClickListener {
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
                alertDialog.setNegativeButton("No",object : DialogInterface.OnClickListener {
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
        bindAuctionList(pcaAuctionList)
        return binding.root
    }

     fun bindAuctionList(dataList: ArrayList<ApiPCAAuctionDetail>) {
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
            dialogBinding.actShopNamePCAAuctionDialog.setText(model.ShopName)
            dialogBinding.actShopNoPCAAuctionDialog.setText(model.ShopNo)
            dialogBinding.edtBagsPCAAuctionDialog.setText(model.Bags)
            dialogBinding.edtCurrentPricePCAAuctionDialog.setText(model.CurrentPrice)
            val amountNF = NumberFormat.getCurrencyInstance().format(model.Amount.toDouble())
            dialogBinding.edtTotalAmountPCAAuctionDialog.setText(amountNF)

            val shopNameAdapter = commonUIUtility.getCustomArrayAdapter(getShopNameFromDB())
            val shopNoAdapter = commonUIUtility.getCustomArrayAdapter(getShopNoFromDb())

            dialogBinding.actShopNoPCAAuctionDialog.setAdapter(shopNoAdapter)
            dialogBinding.actShopNamePCAAuctionDialog.setAdapter(shopNameAdapter)

            dialogBinding.actShopNoPCAAuctionDialog.setOnItemClickListener { adapterView, view, i, l ->
                var shopName = DatabaseManager.ExecuteScalar(Query.getShopNameByShopNo(dialogBinding.actShopNoPCAAuctionDialog.text.toString().trim(),PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString()))!!
                shopId = DatabaseManager.ExecuteScalar(Query.getShopIdByShopNo(dialogBinding.actShopNoPCAAuctionDialog.text.toString().trim(),PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString()))!!
                dialogBinding.actShopNamePCAAuctionDialog.setText(shopName)
            }

            dialogBinding.actShopNamePCAAuctionDialog.setOnItemClickListener { adapterView, view, i, l ->
                var shopNo = DatabaseManager.ExecuteScalar(Query.getShopNoByShopName(dialogBinding.actShopNamePCAAuctionDialog.text.toString().trim(),PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString()))!!
                shopId = DatabaseManager.ExecuteScalar(Query.getShopIdByShopName(dialogBinding.actShopNamePCAAuctionDialog.toString().trim(),PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString()))!!
                dialogBinding.actShopNoPCAAuctionDialog.setText(shopNo)
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
                    } else {
//                    var bags = p0!!.toString().trim()
//                    if (bags.toInt() > 0) {
//                        post_PurchasedBags = PURCHASED_BAG.toInt() + bags.toInt()
//                        post_RemainingBags = BUYER_BORI.toInt() - post_PurchasedBags
//                        binding.tvRemainingBagsPCAAuctionFragment.setText(post_RemainingBags.toString())
//                        binding.tvPurchasedBagsPCAAuctionFragment.setText(post_PurchasedBags.toString())
//                    }
                        calculateExpense(
                            dialogBinding.edtBagsPCAAuctionDialog.text.toString().trim(),
                            dialogBinding.edtCurrentPricePCAAuctionDialog.text.toString().trim(),
                            pcaAuctionList,
                            dialogBinding.edtTotalAmountPCAAuctionDialog
                        )
                    }
                }
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
                } else if (dialogBinding.actShopNamePCAAuctionDialog.text.toString()
                        .isEmpty() || dialogBinding.actShopNoPCAAuctionDialog.text.toString()
                        .isEmpty()
                ) {
                    commonUIUtility.showAlertWithOkButton("Please Enter Shop Name or Shop No!")
                } else if (dialogBinding.edtBagsPCAAuctionDialog.text.toString().toInt() < 1) {
                    commonUIUtility.showToast("Please Enter Bags!")
                } else {
                    model.Amount = "%.2f".format(post_CurrentTotal)
                    model.Bags = dialogBinding.edtBagsPCAAuctionDialog.text.toString().trim()
                    model.ShopNo = dialogBinding.actShopNoPCAAuctionDialog.text.toString().trim()
                    model.CurrentPrice = "%.2f".format(post_CurrentPrice)
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
                "",
                "",
                "",
                "",
                "",
                "",
                commodityBhartiRate,
                "",
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
                if (bags.toInt() > 0 && currentPrice.toDouble() > 0) {
                    total =
                        ((bags.toDouble() * commodityBhartiRate.toDouble()) / 20) * (currentPrice.toDouble())
                }
                val totalCostNF = NumberFormat.getCurrencyInstance().format(total)
                post_CurrentPrice = currentPrice.toDouble()
                post_CurrentTotal = total
                view.setText(totalCostNF.toString())

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
    private fun getShopNameFromDB(): ArrayList<String> {
        var dataList: ArrayList<String> = ArrayList()
        try {
            val cursor = DatabaseManager.ExecuteRawSql(
                Query.getShopName(PrefUtil.getString(PrefUtil.KEY_APMC_ID, "").toString())
            )
            if (cursor != null && cursor.count > 0) {
                dataList.clear()
                while (cursor.moveToNext()) {
                    dataList.add(cursor.getString(cursor.getColumnIndexOrThrow("ShopName")))
                }
                dataList.sort()
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
}