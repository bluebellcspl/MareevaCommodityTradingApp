package com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca

import ConnectionCheck
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.IndPCAAuctionListAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.EditableDecimalInputFilter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.constants.Constants
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentIndPCAAuctionListBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.IndPcaAuctionUpdateDailogBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCAAuctionFragment.ShopNewDetails
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchIndBuyerName
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchIndPCAAuctionAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.POSTIndPCAAuctionAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.ApiIndividualPCAAuctionDetail
import com.bluebellcspl.maarevacommoditytradingapp.model.AuctionDetailsModel
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAAuctionFetchModel
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAAuctionInsertModel
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCABuyerModel
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionPCAListModel
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.RecyclerViewHelper
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.SwipeToDeleteCallback
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText

class IndPCAAuctionListFragment : Fragment(),RecyclerViewHelper {
    var _binding: FragmentIndPCAAuctionListBinding? = null
    val binding get() = _binding!!
    private val commonUIUtility: CommonUIUtility by lazy { CommonUIUtility(requireContext()) }
    val TAG = "PCAAuctionListFragment"
    private lateinit var buyerList:ArrayList<String>
    lateinit var adapter:IndPCAAuctionListAdapter
    lateinit var auctionModel:IndPCAAuctionFetchModel
    private lateinit var _ShopList:ArrayList<ShopNewDetails>
    private var commodityBhartiRate:String = ""
    var post_CurrentTotal = 0.0
    var post_CurrentPrice = 0.0
    var post_AvgPrice = 0.0
    var post_CumulativeTotal = 0.0
    var shopId = ""
    var CURRENT_IND_PCA_ID = ""
    var shopNo = ""
    val args by navArgs<IndPCAAuctionListFragmentArgs>()
    var _BuyerList:ArrayList<IndPCABuyerModel> = ArrayList()
    var SELECTED_BUYER_ID= ""
    var SELECTED_BUYER_NAME= ""
    var PREVIOUS_BUYER_ID = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_ind_p_c_a_auction_list, container, false)

        binding.rcViewIndPCAAuctionListFragment.layoutManager = LinearLayoutManager(requireContext())

        if (ConnectionCheck.isConnected(requireContext()))
        {
            FetchIndBuyerName(requireContext(),this@IndPCAAuctionListFragment)
            FetchIndPCAAuctionAPI(requireContext(),this@IndPCAAuctionListFragment,args.buyerModel.BuyerId)
        }else
        {
            commonUIUtility.showToast(requireContext().getString(R.string.no_internet_connection))
        }

        val swipeToDelete = object: SwipeToDeleteCallback(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
//                pcaAuctionList.removeAt(position)
//                binding.rcViewPCAAuctionListFrament.adapter?.notifyItemRemoved(position)
                val deleteItemModel = auctionModel.ApiIndividualPCAAuctionDetail[position]

                var postDeleteModel = IndPCAAuctionInsertModel(
                "Delete",
                    "",
                "",
                "",
                "",
                    "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                ""+deleteItemModel.IndividualPCAAuctionDetailId,
                "",
                ""+deleteItemModel.IndividualPCAAuctionMasterId,
                "",
                "",
                "",
                "",
                    "",
                "",
                "",
                "",
                "",
                "",
                ""
                )
                val alertDialog = AlertDialog.Builder(requireContext())
                alertDialog.setTitle("Alert!")
                alertDialog.setMessage(requireContext().getString(R.string.do_you_want_to_delete_pca_data_alert_msg))
                alertDialog.setPositiveButton(requireContext().getString(R.string.delete),object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        if (ConnectionCheck.isConnected(requireContext()))
                        {
                            POSTIndPCAAuctionAPI(requireContext(),this@IndPCAAuctionListFragment,postDeleteModel,position)
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
        itemTouchHelper.attachToRecyclerView(binding.rcViewIndPCAAuctionListFragment)
        return binding.root
    }

    fun bindAuctionList(model:IndPCAAuctionFetchModel){
        try {
            auctionModel = model
            commodityBhartiRate = auctionModel.CommodityBhartiPrice
            CURRENT_IND_PCA_ID = auctionModel.IndividualPCAId
            if (auctionModel.ApiIndividualPCAAuctionDetail.isEmpty()){
                commonUIUtility.showToast(requireContext().getString(R.string.no_data_found))
            }else
            {
                adapter = IndPCAAuctionListAdapter(requireContext(),auctionModel.ApiIndividualPCAAuctionDetail,this)
                binding.rcViewIndPCAAuctionListFragment.adapter = adapter
                binding.rcViewIndPCAAuctionListFragment.invalidate()
            }
        }catch (e:Exception)
        {
            commonUIUtility.showToast(requireContext().getString(R.string.please_try_again_later_alert_msg))
            e.printStackTrace()
            Log.e(TAG, "bindAuctionList: ${e.message}")
        }
    }

    fun showPCAAddAuctionDialog(model: ApiIndividualPCAAuctionDetail) {
        try {
            val alertDailogBuilder = AlertDialog.Builder(requireContext())
            val dialogBinding = IndPcaAuctionUpdateDailogBinding.inflate(layoutInflater)
            val dialogView = dialogBinding.root
            alertDailogBuilder.setView(dialogView)
            val alertDialog = alertDailogBuilder.create()
            alertDialog.setCanceledOnTouchOutside(true)
            alertDialog.setCancelable(true)
            alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()
            PREVIOUS_BUYER_ID = model.BuyerId
            SELECTED_BUYER_ID = model.BuyerId
            SELECTED_BUYER_NAME = model.BuyerName
            shopId = model.ShopId
            shopNo = model.ShopNo
            post_CurrentTotal = model.Amount.toDouble()
            post_CurrentPrice = model.CurrentPrice.toDouble()

            _ShopList = getShopDetails(dialogBinding.actShopNameIndPCAAuctionFragment)

            val buyerAdapter = commonUIUtility.getCustomArrayAdapter(_BuyerList.map { it.BuyerShortName } as ArrayList<String>)
            dialogBinding.actBuyerIndPCAAuctionFragment.setAdapter(buyerAdapter)
//            buyerList = getBuyerList(dialogBinding.actBuyerIndPCAAuctionFragment)

            if (_ShopList.isEmpty())
            {
                dialogBinding.actShopNameIndPCAAuctionFragment.isEnabled=false
                commonUIUtility.showToast("Shop List is Blank!")
            }
            val shopModel = _ShopList.find { it.ShopId.equals(model.ShopId) }!!
            dialogBinding.actShopNameIndPCAAuctionFragment.setText(shopModel.ShopNoName)
            dialogBinding.actBuyerIndPCAAuctionFragment.setText(model.BuyerName)
            dialogBinding.edtBagsPCAAuctionDialog.setText(model.Bags)
            dialogBinding.edtCurrentPricePCAAuctionDialog.setText(model.CurrentPrice)
            val amountNF = NumberFormat.getCurrencyInstance().format(model.Amount.toDouble())
            dialogBinding.edtTotalAmountPCAAuctionDialog.setText(amountNF)

//            val shopNameAdapter = commonUIUtility.getCustomArrayAdapter(getShopNameFromDB())
//            if (PrefUtil.getSystemLanguage().equals("en"))
//            {
//                dialogBinding.actShopNameIndPCAAuctionFragment.setText(model.ShortShopName)
//                _ShopDataList = ArrayList(getShopData().sortedBy { it.ShortShopName })
//                val shopNameAdapter = commonUIUtility.getCustomArrayAdapter(ArrayList(_ShopDataList.map { it.ShortShopName }))
//                dialogBinding.actShopNameIndPCAAuctionFragment.setAdapter(shopNameAdapter)
//            }else
//            {
//                dialogBinding.actShopNameIndPCAAuctionFragment.setText(model.GujaratiShortShopName)
//                _ShopDataList = ArrayList(getShopData().sortedBy { it.ShortGujShopName })
//                val shopNameAdapter = commonUIUtility.getCustomArrayAdapter(ArrayList(_ShopDataList.map { it.ShortGujShopName }))
//                dialogBinding.actShopNameIndPCAAuctionFragment.setAdapter(shopNameAdapter)
//            }


            dialogBinding.edtBagsPCAAuctionDialog.filters =arrayOf<InputFilter>(
                EditableDecimalInputFilter(5, 2)
            )
            dialogBinding.edtCurrentPricePCAAuctionDialog.filters =arrayOf<InputFilter>(
                EditableDecimalInputFilter(7, 2)
            )

            dialogBinding.actShopNameIndPCAAuctionFragment.setOnItemClickListener { adapterView, view, position,long ->
                var shopModel:ShopNewDetails
                if (PrefUtil.getSystemLanguage().equals("gu")){
                    shopModel = _ShopList.find { it-> it.GujaratiShopNoName.equals(adapterView.getItemAtPosition(position).toString())}!!
                }else{
                    shopModel = _ShopList.find { it-> it.ShopNoName.equals(adapterView.getItemAtPosition(position).toString())}!!
                }

                Log.d(TAG, "showPCAAddAuctionDialog: SELECTED_SHOP_NAME : ${shopModel.ShopNoName}")
                Log.d(TAG, "showPCAAddAuctionDialog: SELECTED_SHOP_ID : ${shopModel.ShopId}")
                shopId = shopModel.ShopId
                shopNo = shopModel.ShopNo
            }

            dialogBinding.actBuyerIndPCAAuctionFragment.setOnItemClickListener { adapterView, view, position, long ->
                val buyerModel = _BuyerList.find { it-> it.BuyerShortName.equals(adapterView.getItemAtPosition(position).toString()) }!!
                Log.d(TAG, "showPCAAddAuctionDialog: SELECTED_BUYER_ID : ${buyerModel.InBuyerId}")
                Log.d(TAG, "showPCAAddAuctionDialog: SELECTED_BUYER_SHORT_NAME : ${buyerModel.BuyerShortName}")
                SELECTED_BUYER_ID = buyerModel.InBuyerId
                SELECTED_BUYER_NAME = buyerModel.BuyerShortName
                Log.d(TAG, "showPCAAddAuctionDialog: SELECTED_BUYER_ID : $SELECTED_BUYER_ID")
            }

            dialogBinding.actBuyerIndPCAAuctionFragment.addTextChangedListener(object : TextWatcher {
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
                    }
                }
            })

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
                            auctionModel.ApiIndividualPCAAuctionDetail,
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
                            auctionModel.ApiIndividualPCAAuctionDetail,
                            dialogBinding.edtTotalAmountPCAAuctionDialog
                        )
                    }
                }
            })

            dialogBinding.btnAddPCAAuctionDialog.setOnClickListener {

                if (dialogBinding.edtCurrentPricePCAAuctionDialog.text.toString().toDouble() <1) {
                    commonUIUtility.showAlertWithOkButton("Please Enter Current Price!")
                } else if (dialogBinding.actShopNameIndPCAAuctionFragment.text.toString().isEmpty()) {
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
                    model.Amount =DecimalFormat("0.00").format(post_CurrentTotal)
                    model.Bags = DecimalFormat("0.00").format(bags.toFloat())
                    model.ShopNo = shopNo
                    model.ShopId = shopId
                    model.BuyerName = dialogBinding.actBuyerIndPCAAuctionFragment.text.toString()
                    model.CurrentPrice = java.text.DecimalFormat("0.00").format(post_CurrentPrice)
                    alertDialog.dismiss()
                    updatePCAData(model)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "showTaskAllocationDialog: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun updatePCAData(model: ApiIndividualPCAAuctionDetail) {
        var updateModel = IndPCAAuctionInsertModel(
            ""+"Update",
        ""+model.Amount,
        "",
        ""+model.Bags,
            SELECTED_BUYER_ID,
        ""+model.BuyerName,
        ""+model.CommodityBhartiPrice,
        ""+model.CommodityId,
        PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,"").toString(),
        "",
        ""+model.CreateUser,
        ""+model.CurrentPrice,
        ""+DateUtility().getyyyyMMdd(),
        ""+PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString(),
        ""+model.IndividualPCAAuctionDetailId,
        "",
        ""+model.IndividualPCAAuctionMasterId,
        ""+CURRENT_IND_PCA_ID,
        ""+PrefUtil.getString(PrefUtil.KEY_MOBILE_NO,"").toString(),
        ""+PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString(),
            ""+PREVIOUS_BUYER_ID,
        ""+PrefUtil.getString(PrefUtil.KEY_ROLE_ID,"").toString(),
        ""+shopId,
        ""+shopNo,
        ""+model.TotalCostHeader,
        ""+model.TotalPurchasedBagsHeader,
        ""+DateUtility().getyyyyMMdd(),
        ""+model.CreateUser
        )

        if (ConnectionCheck.isConnected(requireContext()))
        {
            POSTIndPCAAuctionAPI(requireContext(),this@IndPCAAuctionListFragment,updateModel,0)
        }else{
            commonUIUtility.showToast(requireContext().getString(R.string.no_internet_connection))
        }
    }

    fun calculateExpense(
        bags: String,
        currentPrice: String,
        dataList: ArrayList<ApiIndividualPCAAuctionDetail>,
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

    fun getBuyerFromAPI(dataList:ArrayList<IndPCABuyerModel>){
        _BuyerList.clear()
        _BuyerList = dataList
    }
    private fun getBuyerList(dropDown:MaterialAutoCompleteTextView):ArrayList<String>{
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
            dropDown.setAdapter(buyerAdapter)
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

    private fun insertBuyer(BuyerName:String,dropDown:MaterialAutoCompleteTextView){
        try {
            if (dropDown.text.toString().isNotEmpty() && !buyerList.contains(BuyerName)){
                val contentValue = ContentValues()
                contentValue.put("BuyerName",BuyerName)
                contentValue.put("CreateDate", DateUtility().getyyyyMMddDateTime())
                DatabaseManager.commonInsert(contentValue, Constants.TBL_BuyerData)
            }
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "insertBuyer: ${e.message}")
        }
    }

    private fun getShopDetails(dropDown:MaterialAutoCompleteTextView):ArrayList<ShopNewDetails>{
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
            dropDown.setAdapter(shopAdapter)
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

    fun deleteShopItem(position:Int){
        adapter.notifyItemRemoved(position)
        auctionModel.ApiIndividualPCAAuctionDetail.removeAt(position)
        binding.rcViewIndPCAAuctionListFragment.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(postion: Int, onclickType: String) {
        showPCAAddAuctionDialog(auctionModel.ApiIndividualPCAAuctionDetail[postion])
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