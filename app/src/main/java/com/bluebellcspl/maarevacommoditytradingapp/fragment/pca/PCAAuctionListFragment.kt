package com.bluebellcspl.maarevacommoditytradingapp.fragment.pca

import android.app.AlertDialog
import android.icu.text.NumberFormat
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.PCAAuctionListAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentPCAAuctionListBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.PcaAuctionDetailDailogLayoutBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.ApiPCAAuctionDetail
import com.bluebellcspl.maarevacommoditytradingapp.model.AuctionDetailsModel
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.RecyclerViewHelper
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class PCAAuctionListFragment : Fragment(),RecyclerViewHelper {
    lateinit var binding:FragmentPCAAuctionListBinding
    private val commonUIUtility:CommonUIUtility by lazy { CommonUIUtility(requireContext())}
    val TAG = "PCAAuctionListFragment"
    private val navController by lazy { findNavController() }
    val args by navArgs<PCAAuctionListFragmentArgs>()
    lateinit var adapter:PCAAuctionListAdapter
    lateinit var pcaAuctionList:ArrayList<ApiPCAAuctionDetail>
    var shopId = "0.0"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_p_c_a_auction_list, container, false)
        binding.rcViewPCAAuctionListFrament.layoutManager = LinearLayoutManager(requireContext())
        pcaAuctionList = args.pcaAuctionDetailModel.ApiPCAAuctionDetail
        bindAuctionList(pcaAuctionList)
        return binding.root
    }
    private fun bindAuctionList(dataList: ArrayList<ApiPCAAuctionDetail>){
        if (dataList.isNotEmpty())
        {
            adapter = PCAAuctionListAdapter(requireContext(),dataList,this)
            binding.rcViewPCAAuctionListFrament.adapter = adapter
            binding.rcViewPCAAuctionListFrament.invalidate()
        }else
        {
            commonUIUtility.showToast("No Auction List!")
        }
    }

    fun showPCAAddAuctionDialog(model:ApiPCAAuctionDetail) {
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

        dialogBinding.actShopNamePCAAuctionDialog.setText(model.ShopName)
        dialogBinding.actShopNoPCAAuctionDialog.setText(model.ShopNo)
        dialogBinding.edtBagsPCAAuctionDialog.setText(model.Bags)
        val amountNF = NumberFormat.getCurrencyInstance().format(model.Amount.toDouble())
        dialogBinding.edtCurrentPricePCAAuctionDialog.setText(amountNF)

        val shopNameAdapter = commonUIUtility.getCustomArrayAdapter(getShopNameFromDB())
        val shopNoAdapter = commonUIUtility.getCustomArrayAdapter(getShopNoFromDb())

        //OnFocusChangeListener
        dialogBinding.edtBagsPCAAuctionDialog.onFocusChangeListener =
            View.OnFocusChangeListener { view: View?, b: Boolean ->
                if (b) {
                    if (dialogBinding.actShopNoPCAAuctionDialog.text.toString().isNotEmpty()) {
                        bindShopName(dialogBinding.actShopNoPCAAuctionDialog.text.toString().trim(),dialogBinding.actShopNamePCAAuctionDialog)
                    } else {
                        bindShopNo(dialogBinding.actShopNamePCAAuctionDialog.text.toString().trim(),dialogBinding.actShopNoPCAAuctionDialog)
                    }
                }
            }

        dialogBinding.actShopNoPCAAuctionDialog.setAdapter(shopNoAdapter)
        dialogBinding.actShopNamePCAAuctionDialog.setAdapter(shopNameAdapter)



    } catch (e: Exception) {
        Log.e(TAG, "showTaskAllocationDialog: ${e.message}")
        e.printStackTrace()
    }
}

    fun bindShopName(shopNo: String,view: MaterialAutoCompleteTextView) {
        try {
            shopId = ""
            var shopName = DatabaseManager.ExecuteScalar(Query.getShopNameByShopNo(shopNo,PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString()))!!
            Log.d(TAG, "afterTextChanged: SELECTED_SHOP_NO : $shopNo")
            Log.d(TAG, "afterTextChanged: FETCHED_SHOP_NAME : $shopName")
            if (!shopName.equals("invalid", true)) {
                shopId = DatabaseManager.ExecuteScalar(Query.getShopIdByShopName(shopName,PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString()))!!
                view.setText(shopName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindShopName: ${e.message}")
        }
    }

    fun bindShopNo(shopName: String,view: MaterialAutoCompleteTextView) {
        try {
            shopId = ""
            var shopNo = DatabaseManager.ExecuteScalar(Query.getShopNoByShopName(shopName,PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString()))!!
            Log.d(TAG, "afterTextChanged: SELECTED_SHOP_NAME : $shopName")
            Log.d(TAG, "afterTextChanged: FETCHED_SHOP_NO : $shopNo")
            if (!shopNo.equals("invalid", true)) {
                shopId = DatabaseManager.ExecuteScalar(Query.getShopIdByShopNo(shopName,PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString()))!!
                view.setText(shopNo)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindShopNo: ${e.message}")
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

    }

    override fun onBuyerAuctionPCAItemClick(postion: Int, model: AuctionDetailsModel) {

    }

    override fun getBuyerAuctionDataList(dataList: ArrayList<AuctionDetailsModel>) {

    }
}