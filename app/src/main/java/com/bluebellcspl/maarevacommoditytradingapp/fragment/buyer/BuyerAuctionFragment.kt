package com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer

import ConnectionCheck
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.icu.text.DecimalFormat
import android.icu.text.NumberFormat
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.LoginActivity
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.BuyerAuctionListAdapter
import com.bluebellcspl.maarevacommoditytradingapp.adapter.BuyerAuctionPopupAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.BuyerAuctionPopupAdapterBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.BuyerExpenseDialogLayoutBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentBuyerAuctionBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchBuyerAuctionDetailAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.POSTBuyerAuctionDataAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.AuctionDetailsModel
import com.bluebellcspl.maarevacommoditytradingapp.model.BuyerAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionPCAListModel
import com.bluebellcspl.maarevacommoditytradingapp.model.POSTBuyerAuctionData
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.RecyclerViewHelper
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


class BuyerAuctionFragment : Fragment(), RecyclerViewHelper {
    var _binding: FragmentBuyerAuctionBinding?=null
    private val binding get() = _binding!!
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val TAG = "BuyerAuctionFragment"
    private val navController by lazy { findNavController() }
    lateinit var datePicker: MaterialDatePicker<Long>
    lateinit var alertDialog: AlertDialog
    lateinit var adapter: BuyerAuctionListAdapter
    var auctionDetailList: ArrayList<AuctionDetailsModel> = ArrayList()
    var auctionDetailList2: ArrayList<AuctionDetailsModel> = ArrayList()
//    lateinit var pcaDetailList: ArrayList<newPCAModel>
    lateinit var ALLOCATED_BAGS: String
    lateinit var TOTAL_AMOUNT: String
    lateinit var TOTAL_PCA_BASIC: String
    lateinit var TOTAL_PCA_COMMISSION: String
    lateinit var TOTAL_GCA_COMMISSION: String
    lateinit var TOTAL_MARKETCESS: String
    lateinit var TOTAL_TRANSPORTATION_CHARGE: String
    lateinit var TOTAL_LABOUR_CHARGE: String
    lateinit var AUCTION_MASTER_ID: String
    lateinit var postAuction: BuyerAuctionMasterModel
    lateinit var commodityBharti:String
    var CURRENT_PCA_COUNT = 0
    var isAuctionForUpdate = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_buyer_auction, container, false)
//        commodityBharti = DatabaseManager.ExecuteScalar(Query.getCommodityBhartiByCommodityId(PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"").toString()))!!
        if (ConnectionCheck.isConnected(requireContext()))
        {
            FetchBuyerAuctionDetailAPI(requireContext(), requireActivity(), this)
        }else{
            commonUIUtility.showToast(getString(R.string.no_internet_connection))
        }

        binding.cvTotalAmountBuyerAuctionFragment.setOnClickListener {
            showExpenseAuctionDialog(auctionDetailList)
        }

        binding.btnSubmitBuyerAuctionFragment.setOnClickListener {
            if (binding.edtTotalBagsBuyerAuctionFragment.text.toString().isEmpty())
            {
                commonUIUtility.showToast("Please Enter Number of Bags!")
            }else {
                if (isAuctionForUpdate)
                {
                    showAlertDialog()
                }else
                {
                    postAuctionData()
                }
            }
        }
        //new design changes
//        pcaDetailList = getPCADetails()
        Log.d(TAG, "onCreateView: PCA_COUNT_IN_AUCTION_API : $CURRENT_PCA_COUNT")

        binding.edtTotalBagsBuyerAuctionFragment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString().isNotEmpty()) {
                    lifecycleScope.launch(Dispatchers.Default){
                        allocateBagsAutomatically(p0.toString().trim())
                    }
                } else {
//                    val detailModel = Detail("","","","","","","","","","","","","")
//                    auctionDetailList.add(detailModel)
                    bindRecyclerView(auctionDetailList)
                    binding.rcViewBuyerAuctionFragment.invalidate()
                    binding.rcViewBuyerAuctionFragment.adapter = null
                    binding.tvTotalAmountBuyerAuctionFragment.setText("")
                    binding.tvLeftBagsBuyerAuctionFragment.setText("")
                }
            }
        })
        return binding.root
    }

    private fun getPCADetails(): ArrayList<newPCAModel> {
        var dataList: ArrayList<newPCAModel> = ArrayList()
        try {
            val cursor = DatabaseManager.ExecuteRawSql(Query.getPCADetail())
            if (cursor != null && cursor.count > 0) {
                dataList.clear()
                while (cursor.moveToNext()) {
                    val model = newPCAModel(
                        cursor.getString(cursor.getColumnIndexOrThrow("PCAId")),
                        cursor.getString(cursor.getColumnIndexOrThrow("PCAName")),
                        cursor.getString(cursor.getColumnIndexOrThrow("PCARegId")),
                        cursor.getString(cursor.getColumnIndexOrThrow("GCACommission")),
                        cursor.getString(cursor.getColumnIndexOrThrow("PCACommission")),
                        cursor.getString(cursor.getColumnIndexOrThrow("MarketCess")),
                        cursor.getString(cursor.getColumnIndexOrThrow("LabourCharges")),
//                        cursor.getString(cursor.getColumnIndexOrThrow("TransportationCharges"))
                        ""
                    )
                    dataList.add(model)
                }
            }
            cursor?.close()
        } catch (e: Exception) {
            dataList.clear()
            e.printStackTrace()
            Log.e(TAG, "getPCAName: ${e.message}")
        }
        Log.d(TAG, "getPCAName: APPROVED_PCA_LIST : $dataList")
        return dataList
    }

    fun updateUIFromAPIData(dataFromAPI: BuyerAuctionMasterModel) {
//        auctionDetailList = ArrayList()
        try {
            commodityBharti = dataFromAPI.CommodityBhartiPrice
            Log.d(TAG, "onCreateView: COMMODITY_BHARTI : $commodityBharti")
            if (dataFromAPI.BudgetAmount.isEmpty()) {
                dataFromAPI.BudgetAmount = "0.0"
            }
            if (dataFromAPI.TotalBags.contains("."))
            {
                val newTotalBag = dataFromAPI.TotalBags.split(".")[0]
                dataFromAPI.TotalBags = newTotalBag
                binding.edtTotalBagsBuyerAuctionFragment.setText(dataFromAPI.TotalBags)
            }else
            {
                binding.edtTotalBagsBuyerAuctionFragment.setText(dataFromAPI.TotalBags)
            }
            binding.tvTotalAmountBuyerAuctionFragment.setText(dataFromAPI.BudgetAmount)
//            val expense = dataFromAPI.TotalBasic.toDouble() + dataFromAPI.TotalGCAComm.toDouble()+dataFromAPI.TotalPCAComm.toDouble()+dataFromAPI.TotalMarketCess.toDouble()+dataFromAPI.TotalTransportationCharge.toDouble()+dataFromAPI.TotalLabourCharge.toDouble()
//            binding.edtOtherCommissionBuyerAuctionFragment.setText("%.f".format(expense))
            binding.tvBasicAmountBuyerAuctionFragment.setText(dataFromAPI.TotalBasic)
            if (dataFromAPI.LeftBags.contains("."))
            {
                val newLeftBag = dataFromAPI.LeftBags.split(".")[0]
                dataFromAPI.LeftBags = newLeftBag
                binding.tvLeftBagsBuyerAuctionFragment.setText(dataFromAPI.LeftBags)
            }else
            {
                binding.tvLeftBagsBuyerAuctionFragment.setText(dataFromAPI.LeftBags)
            }
            auctionDetailList = dataFromAPI.AuctionDetailsModel
            postAuction = dataFromAPI
            CURRENT_PCA_COUNT = dataFromAPI.AuctionDetailsModel.size
            bindRecyclerView(auctionDetailList)
            if (dataFromAPI.BudgetAmount.isNotEmpty() && dataFromAPI.TotalBags.isNotEmpty())
            {
                currentAuctionEditPopup(dataFromAPI)
            }
            Log.d(TAG, "updateUIFromAPIData: FIRST_LIST : $auctionDetailList")
        } catch (e: Exception) {
            Log.e(TAG, "updateUIFromAPIData: ${e.message}")
            e.printStackTrace()
        }
    }

    fun bindRecyclerView(dataList: ArrayList<AuctionDetailsModel>) {
        try {
            if (dataList.isNotEmpty()) {
                adapter = BuyerAuctionListAdapter(requireContext(), dataList, this,commodityBharti)
                binding.rcViewBuyerAuctionFragment.adapter = adapter
                binding.rcViewBuyerAuctionFragment.setItemViewCacheSize(dataList.size)
                binding.rcViewBuyerAuctionFragment.setHasFixedSize(true)
                binding.rcViewBuyerAuctionFragment.invalidate()
            } else {
                commonUIUtility.showToast("No Details Found!")
            }
        } catch (e: Exception) {
            Log.e(TAG, "bindRecyclerView: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun allocateBagsAutomatically(bags: String) {
        try {
            auctionDetailList2 = ArrayList()
//            val pcaCount = pcaDetailList.size
            val bagsPerPerson = distributeBagsEvenly(bags.toDouble(), CURRENT_PCA_COUNT)
            auctionDetailList2.addAll(auctionDetailList)
            for (i in 0 until CURRENT_PCA_COUNT) {
                Log.d(TAG, "allocateBagsAutomatically: BAG_POSITION : $i")
                Log.d(TAG, "allocateBagsAutomatically: PER_PERSON_BAG : ${bagsPerPerson[i]}")
                auctionDetailList2[i].Bags = bagsPerPerson[i].toString()
                auctionDetailList2[i].Basic = "0.0"
                Log.d(TAG, "allocateBagsAutomatically: AFTER_BAG_ALLOCATED_TO_PCA : ${auctionDetailList2[i].PCAShortName} - ${auctionDetailList2[i].Bags}")
            }

//            Log.d(TAG, "allocateBagsAutomatically: SECOND_LIST : $auctionDetailList2")
            auctionDetailList2.forEach { it->
                Log.d(TAG, "allocateBagsAutomatically: ALLOCATED_BAG PCA_NAME : ${it.Bags} - ${it.PCAShortName}")
            }
            withContext(Dispatchers.Main){
                bindRecyclerView(auctionDetailList2)
                calculateOtherExpenses(auctionDetailList2)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "allocateBagsAutomatically: ${e.message}")
        }
    }

   suspend fun distributeBagsEvenly(totalBags: Double, numberOfSellers: Int): List<Int> {
        val totalBagsInt = totalBags.toInt()
        val bagsPerSeller = totalBagsInt / numberOfSellers
        val remainingBags = totalBagsInt % numberOfSellers

        val distributedBags = MutableList(numberOfSellers) { bagsPerSeller }

        // Distribute remaining bags evenly among sellers
        for (i in 0 until remainingBags) {
            distributedBags[i]++
        }

        Log.d(TAG, "distributeBagsEvenly: BAGS_DISTRIBUTION : $distributedBags")
        return distributedBags
    }

    override fun onItemClick(postion: Int, onclickType: String) {
        showPCAExpenseDialog(auctionDetailList,postion)
    }

    override fun onBuyerAuctionPCAItemClick(postion: Int, model: AuctionDetailsModel) {
//        showPCAAddAuctionDialog(model,postion)
    }

    override fun getBuyerAuctionDataList(dataList: ArrayList<AuctionDetailsModel>) {
//        dataList.forEach { model ->
//            Log.d(TAG, "getBuyerAuctionDataList: ELEMENT : $model")
//        }
        auctionDetailList = dataList
        calculateOtherExpenses(auctionDetailList)
    }

    fun calculateOtherExpenses(dataList: ArrayList<AuctionDetailsModel>) {
        if (dataList.isEmpty()) {
            commonUIUtility.showToast("No Bags are Allocated!")
        } else {
            var leftBags = "0"
            var ab: Int = 0
            var basic: Double = 0.0
            var total: Double = 0.0
            var pcaCommission = 0.0
            var gcaCommission = 0.0
            var marketCess = 0.0
            var transportCharge = 0.0
            var labourCharge = 0.0
            for (model in dataList) {
                ab += model.Bags.toInt()
                basic += model.Basic.toDouble()
                total += model.Amount.toDouble()
                pcaCommission += model.PCACommCharge.toDouble()
                gcaCommission += model.GCACommCharge.toDouble()
                marketCess += model.MarketCessCharge.toDouble()
                transportCharge += model.TransportationCharge.toDouble()
                leftBags = (binding.edtTotalBagsBuyerAuctionFragment.text.toString().trim().split(".")[0]
                    .toInt() - ab).toString()
                if (model.LabourCharge == null)
                {
                    model.LabourCharge = "0"
                }
                labourCharge += model.LabourCharge!!.toDouble()
                val formattedBasic = DecimalFormat("0.00").format(basic)
                val formattedTotal = DecimalFormat("0.00").format(total)
                val formattedPCAComm = DecimalFormat("0.00").format(pcaCommission)
                val formattedGCAComm = DecimalFormat("0.00").format(gcaCommission)
                val formattedMarketCess = DecimalFormat("0.00").format(marketCess)
                val formattedTransport = DecimalFormat("0.00").format(transportCharge)
                val formattedLabour = DecimalFormat("0.00").format(labourCharge)
                Log.d(TAG, "calculateOtherExpenses: PCA_NAME : ${model.PCAName}")
                Log.d(TAG, "calculateOtherExpenses: ALLOCATED_BAGS : $ab")
                Log.d(TAG, "calculateOtherExpenses: TOTAL_AMOUNT : $formattedTotal")
                Log.d(TAG, "calculateOtherExpenses: TOTAL_PCA_BASIC : $formattedBasic")
                Log.d(TAG, "calculateOtherExpenses: TOTAL_PCACOMMISSION : $formattedPCAComm")
                Log.d(TAG, "calculateOtherExpenses: TOTAL_GCACOMMISSION : $formattedGCAComm")
                Log.d(TAG, "calculateOtherExpenses: TOTAL_MARKETCESS : $formattedMarketCess")
                Log.d(TAG, "calculateOtherExpenses: TOTAL_TRANSPORTATION_CHARGE : $formattedTransport")
                Log.d(TAG, "calculateOtherExpenses: TOTAL_LABOUR_CHARGE : $formattedLabour")
                Log.d(TAG, "calculateOtherExpenses: ================================================================================")
            }
            binding.tvLeftBagsBuyerAuctionFragment.setText(leftBags)
            val basicnf = NumberFormat.getCurrencyInstance().format(basic).substring(1)
            binding.tvBasicAmountBuyerAuctionFragment.setText(basicnf)
            val totalnf = NumberFormat.getCurrencyInstance().format(total).substring(1)
            binding.tvTotalAmountBuyerAuctionFragment.setText(totalnf)
//            binding.tvTotalAmountBuyerAuctionFragment.setText("%.2f".format(total))

//            ALLOCATED_BAGS = ab.toString()
//            TOTAL_PCA_BASIC = "%.2f".format(basic)
//            TOTAL_AMOUNT = "%.2f".format(total)
//            TOTAL_PCA_COMMISSION = "%.2f".format(pcaCommission)
//            TOTAL_GCA_COMMISSION = "%.2f".format(gcaCommission)
//            TOTAL_MARKETCESS = "%.2f".format(marketCess)
//            TOTAL_TRANSPORTATION_CHARGE = "%.2f".format(transportCharge)
//            TOTAL_LABOUR_CHARGE = "%.2f".format(labourCharge)

            ALLOCATED_BAGS = ab.toString()
            TOTAL_PCA_BASIC = DecimalFormat("0.00").format(basic)
            TOTAL_AMOUNT = DecimalFormat("0.00").format(total)
            TOTAL_PCA_COMMISSION = DecimalFormat("0.00").format(pcaCommission)
            TOTAL_GCA_COMMISSION = DecimalFormat("0.00").format(gcaCommission)
            TOTAL_MARKETCESS = DecimalFormat("0.00").format(marketCess)
            TOTAL_TRANSPORTATION_CHARGE = DecimalFormat("0.00").format(transportCharge)
            TOTAL_LABOUR_CHARGE = DecimalFormat("0.00").format(labourCharge)
        }
    }

    fun showExpenseAuctionDialog(dataList: ArrayList<AuctionDetailsModel>) {
        try {
            val alertDailogBuilder = AlertDialog.Builder(requireContext())
            val dialogBinding = BuyerExpenseDialogLayoutBinding.inflate(layoutInflater)
            val dialogView = dialogBinding.root
            alertDailogBuilder.setView(dialogView)
            alertDialog = alertDailogBuilder.create()
            alertDialog.setCanceledOnTouchOutside(true)
            alertDialog.setCancelable(true)
            alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()

            dialogBinding.llTotalBagsBuyerExpenseDialog.visibility = View.VISIBLE
            dialogBinding.llExpensesBuyerExpenseDialog.setOnClickListener {
                if (dialogBinding.llExpandableExpensesBuyerExpenseDialog.isVisible)
                {
                    dialogBinding.llExpandableExpensesBuyerExpenseDialog.visibility = View.GONE
                }else
                {
                    dialogBinding.llExpandableExpensesBuyerExpenseDialog.visibility = View.VISIBLE
                }
            }
            var basic: Double = 0.0
            var total: Double = 0.0
            var pcaCommission = 0.0
            var gcaCommission = 0.0
            var marketCess = 0.0
            var transportCharge = 0.0
            var labourCharge = 0.0
            var allocatedBags=0
            for (model in dataList) {
                if (model.LowerLimit.toDouble()>0 && model.UpperLimit.toDouble()>0)
                {
                    allocatedBags+=model.Bags.toInt()
                    transportCharge += model.TransportationCharge.toDouble()
                    labourCharge += model.LabourCharge!!.toDouble()
                }
                basic += model.Basic.toDouble()
                total += model.Amount.toDouble()
                pcaCommission += model.PCACommCharge.toDouble()
                gcaCommission += model.GCACommCharge.toDouble()
                marketCess += model.MarketCessCharge.toDouble()
                Log.d(TAG, "showExpenseAuctionDialog: TC : ${model.TransportationCharge}")
                Log.d(TAG, "showExpenseAuctionDialog: TOTAL_AMOUNT : $total")
                Log.d(TAG, "showExpenseAuctionDialog: TOTAL_PCACOMMISSION : $pcaCommission")
                Log.d(TAG, "showExpenseAuctionDialog: TOTAL_GCACOMMISSION : $gcaCommission")
                Log.d(TAG, "showExpenseAuctionDialog: TOTAL_MARKETCESS : $marketCess")
                Log.d(TAG,"showExpenseAuctionDialog: TOTAL_TRANSPORTATION_CHARGE : $transportCharge")
                Log.d(TAG, "showExpenseAuctionDialog: TOTAL_LABOUR_CHARGE : $labourCharge")
            }
            if (binding.tvTotalAmountBuyerAuctionFragment.text.toString()
                    .equals("") || binding.tvTotalAmountBuyerAuctionFragment.text.toString()
                    .isEmpty()
            ) {
                dialogBinding.tvTotalBasicAmountBuyerExpenseDialog.setText("0.0")
                dialogBinding.tvTotalExpenseBuyerExpenseDialog.setText("0.0")
                dialogBinding.tvTotalPCACommissionBuyerExpenseDialog.setText("0.0")
                dialogBinding.tvTotalGCACommissionBuyerExpenseDialog.setText("0.0")
                dialogBinding.tvTotalMarketCessBuyerExpenseDialog.setText("0.0")
                dialogBinding.tvTotalTransportChargeBuyerExpenseDialog.setText("0.0")
                dialogBinding.tvTotalLabourChargeBuyerExpenseDialog.setText("0.0")
                dialogBinding.tvTotalBagsBuyerExpenseDialog.setText("0.0")
            } else {

//                dialogBinding.tvTotalBasicAmountBuyerExpenseDialog.setText("%.2f".format(basic))
//                dialogBinding.tvTotalPCACommissionBuyerExpenseDialog.setText("%.2f".format(pcaCommission))
//                dialogBinding.tvTotalGCACommissionBuyerExpenseDialog.setText("%.2f".format(gcaCommission))
//                dialogBinding.tvTotalMarketCessBuyerExpenseDialog.setText("%.2f".format(marketCess))
//                dialogBinding.tvTotalTransportChargeBuyerExpenseDialog.setText("%.2f".format(transportCharge))
//                dialogBinding.tvTotalLabourChargeBuyerExpenseDialog.setText("%.2f".format(labourCharge))
                val basicNF = NumberFormat.getCurrencyInstance().format(basic).substring(1)
                dialogBinding.tvTotalBasicAmountBuyerExpenseDialog.setText(basicNF.toString())
                val totalExpense = pcaCommission+gcaCommission+marketCess+transportCharge+labourCharge
                val ExpensesNF = NumberFormat.getCurrencyInstance().format(totalExpense).substring(1)
                dialogBinding.tvTotalExpenseBuyerExpenseDialog.setText(ExpensesNF.toString())
                val pcaCommNF = NumberFormat.getCurrencyInstance().format(pcaCommission).substring(1)
                dialogBinding.tvTotalPCACommissionBuyerExpenseDialog.setText(pcaCommNF.toString())
                val gcaCommNF = NumberFormat.getCurrencyInstance().format(gcaCommission).substring(1)
                dialogBinding.tvTotalGCACommissionBuyerExpenseDialog.setText(gcaCommNF.toString())
                val marketCessNF = NumberFormat.getCurrencyInstance().format(marketCess).substring(1)
                dialogBinding.tvTotalMarketCessBuyerExpenseDialog.setText(marketCessNF.toString())
                val transportChargeNF = NumberFormat.getCurrencyInstance().format(transportCharge).substring(1)
                dialogBinding.tvTotalTransportChargeBuyerExpenseDialog.setText(transportChargeNF.toString())
                val labourChargeNF = NumberFormat.getCurrencyInstance().format(labourCharge).substring(1)
                dialogBinding.tvTotalLabourChargeBuyerExpenseDialog.setText(labourChargeNF.toString())
                dialogBinding.tvTotalBagsBuyerExpenseDialog.setText(allocatedBags.toString())
            }

        } catch (e: Exception) {
            Log.e(TAG, "showExpenseAuctionDialog: ${e.message}")
            e.printStackTrace()
        }
    }
    fun showPCAExpenseDialog(dataList: ArrayList<AuctionDetailsModel>,postion: Int) {
        try {
            val alertDailogBuilder = AlertDialog.Builder(requireContext())
            val dialogBinding = BuyerExpenseDialogLayoutBinding.inflate(layoutInflater)
            val dialogView = dialogBinding.root
            alertDailogBuilder.setView(dialogView)
            alertDialog = alertDailogBuilder.create()
            alertDialog.setCanceledOnTouchOutside(true)
            alertDialog.setCancelable(true)
            alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()
            var model = dataList[postion]
            dialogBinding.llPCANameBuyerExpenseDialog.visibility = View.VISIBLE
            dialogBinding.llBagsBuyerExpenseDialog.visibility = View.VISIBLE
            dialogBinding.llExpensesBuyerExpenseDialog.setOnClickListener {
                if (dialogBinding.llExpandableExpensesBuyerExpenseDialog.isVisible)
                {
                    dialogBinding.llExpandableExpensesBuyerExpenseDialog.visibility = View.GONE
                }else
                {
                    dialogBinding.llExpandableExpensesBuyerExpenseDialog.visibility = View.VISIBLE
                }
            }
            var basic: Double = 0.0
            var total: Double = 0.0
            var pcaCommission = 0.0
            var gcaCommission = 0.0
            var marketCess = 0.0
            var transportCharge = 0.0
            var labourCharge = 0.0
//            for (model in dataList) {
                basic = model.Basic.toDouble()
                total = model.Amount.toDouble()
                pcaCommission = model.PCACommCharge.toDouble()
                gcaCommission = model.GCACommCharge.toDouble()
                marketCess = model.MarketCessCharge.toDouble()
                transportCharge = model.TransportationCharge.toDouble()
                Log.d(TAG, "showExpenseAuctionDialog: TC : ${model.TransportationCharge}")
            if (model.LabourCharge == null)
            {
                model.LabourCharge = "0"
            }
                labourCharge = model.LabourCharge!!.toDouble()
                Log.d(TAG, "showExpenseAuctionDialog: TOTAL_AMOUNT : $total")
                Log.d(TAG, "showExpenseAuctionDialog: TOTAL_PCACOMMISSION : $pcaCommission")
                Log.d(TAG, "showExpenseAuctionDialog: TOTAL_GCACOMMISSION : $gcaCommission")
                Log.d(TAG, "showExpenseAuctionDialog: TOTAL_MARKETCESS : $marketCess")
                Log.d(
                    TAG,
                    "showExpenseAuctionDialog: TOTAL_TRANSPORTATION_CHARGE : $transportCharge"
                )
                Log.d(TAG, "showExpenseAuctionDialog: TOTAL_LABOUR_CHARGE : $labourCharge")
//            }
            if (binding.tvTotalAmountBuyerAuctionFragment.text.toString()
                    .equals("") || binding.tvTotalAmountBuyerAuctionFragment.text.toString()
                    .isEmpty()
            ) {
                dialogBinding.tvTotalBasicAmountBuyerExpenseDialog.setText("0.0")
                dialogBinding.tvTotalExpenseBuyerExpenseDialog.setText("0.0")
                dialogBinding.tvTotalPCACommissionBuyerExpenseDialog.setText("0.0")
                dialogBinding.tvTotalGCACommissionBuyerExpenseDialog.setText("0.0")
                dialogBinding.tvTotalMarketCessBuyerExpenseDialog.setText("0.0")
                dialogBinding.tvTotalTransportChargeBuyerExpenseDialog.setText("0.0")
                dialogBinding.tvTotalLabourChargeBuyerExpenseDialog.setText("0.0")
            } else {

                val basicNF = NumberFormat.getCurrencyInstance().format(basic).substring(1)
                dialogBinding.tvTotalBasicAmountBuyerExpenseDialog.setText(basicNF.toString())
                val totalExpense = pcaCommission+gcaCommission+marketCess+transportCharge+labourCharge
                val ExpensesNF = NumberFormat.getCurrencyInstance().format(totalExpense).substring(1)
                dialogBinding.tvTotalExpenseBuyerExpenseDialog.setText(ExpensesNF)
                val pcaCommNF = NumberFormat.getCurrencyInstance().format(pcaCommission).substring(1)
                dialogBinding.tvTotalPCACommissionBuyerExpenseDialog.setText(pcaCommNF.toString())
                val gcaCommNF = NumberFormat.getCurrencyInstance().format(gcaCommission).substring(1)
                dialogBinding.tvTotalGCACommissionBuyerExpenseDialog.setText(gcaCommNF.toString())
                val marketCessNF = NumberFormat.getCurrencyInstance().format(marketCess).substring(1)
                dialogBinding.tvTotalMarketCessBuyerExpenseDialog.setText(marketCessNF.toString())
                val transportChargeNF = NumberFormat.getCurrencyInstance().format(transportCharge).substring(1)
                dialogBinding.tvTotalTransportChargeBuyerExpenseDialog.setText(transportChargeNF.toString())
                val labourChargeNF = NumberFormat.getCurrencyInstance().format(labourCharge).substring(1)
                dialogBinding.tvTotalLabourChargeBuyerExpenseDialog.setText(labourChargeNF.toString())
                dialogBinding.tvBagsBuyerExpenseDialog.setText(model.Bags)
                if (PrefUtil.getSystemLanguage().equals("gu")) {
                    dialogBinding.tvPCANameBuyerExpenseDialog.setText(DatabaseManager.ExecuteScalar(Query.getGujaratiPCANameByPCAId(model.PCAId)))
                } else {
                    dialogBinding.tvPCANameBuyerExpenseDialog.setText(model.PCAName)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "showExpenseAuctionDialog: ${e.message}")
            e.printStackTrace()
        }
    }

    fun showAlertDialog() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle("Alert")
        alertDialog.setMessage(requireContext().getString(R.string.do_you_want_to_update_auction_alert_msg))
        alertDialog.setPositiveButton(requireContext().getString(R.string.yes), object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                postAuctionData()
            }
        })
        alertDialog.setNegativeButton(requireContext().getString(R.string.no), object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                p0!!.dismiss()
            }
        })
        alertDialog.show()
    }

    private fun postAuctionData() {
        try {
            val postAuctionDataModel = POSTBuyerAuctionData(
                "insert",
                ALLOCATED_BAGS,
                auctionDetailList,
                postAuction.AuctionMasterId,
                TOTAL_AMOUNT,
                auctionDetailList[0].BuyerCityId,
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString(),
                PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"").toString(),
                commodityBharti,
                PrefUtil.getString(PrefUtil.KEY_COMMODITY_NAME,"").toString(),
                PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,"").toString(),
                "",
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString(),
                DateUtility().getyyyyMMdd(),
                binding.tvLeftBagsBuyerAuctionFragment.text.toString(),
                postAuction.RoleId,
                binding.edtTotalBagsBuyerAuctionFragment.text.toString().trim(),
                TOTAL_PCA_BASIC,
                TOTAL_AMOUNT,
                TOTAL_GCA_COMMISSION,
                TOTAL_LABOUR_CHARGE,
                TOTAL_MARKETCESS,
                TOTAL_PCA_COMMISSION,
                CURRENT_PCA_COUNT.toString(),
                TOTAL_TRANSPORTATION_CHARGE,
                DateUtility().getyyyyMMdd(),
                PrefUtil.getString(PrefUtil.KEY_BUYER_ID, "").toString()
            )
            if (ConnectionCheck.isConnected(requireContext()))
            {
                POSTBuyerAuctionDataAPI(requireContext(),requireActivity(),this,postAuctionDataModel)
            }else{
                commonUIUtility.showToast(getString(R.string.no_internet_connection))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "postAuctionData: ${e.message}")
        }
    }

    fun redirectToBuyerDashboard()
    {
        navController.navigate(BuyerAuctionFragmentDirections.actionBuyerAuctionFragmentToBuyerDashboardFragment())
    }
    data class newPCAModel(
        var PCAId: String,
        var PCAName: String,
        var PCARegId: String,
        var GCACommission: String,
        var PCACommission: String,
        var MarketCess: String,
        var LabourCharges: String,
        var TransportationCharges: String
    )

    public fun onAuctionInsertSuccessful()
    {
        FetchBuyerAuctionDetailAPI(requireContext(), requireActivity(), this)
    }

    override fun getLiveAuctionPCAData(postion: Int, model: LiveAuctionPCAListModel) {
        TODO("Not yet implemented")
    }

    fun currentAuctionEditPopup(dataFromAPI: BuyerAuctionMasterModel){
        try {
            val alertDailogBuilder = AlertDialog.Builder(requireContext())
            val dialogBinding = BuyerAuctionPopupAdapterBinding.inflate(layoutInflater)
            val dialogView = dialogBinding.root
            alertDailogBuilder.setView(dialogView)
            alertDialog = alertDailogBuilder.create()
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.setCancelable(false)
            alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()

            dataFromAPI.CommodityBhartiPrice = commodityBharti
            val amountNF = NumberFormat.getCurrencyInstance().format(dataFromAPI.BudgetAmount.toDouble()).substring(1)
            dialogBinding.tvTotalBagsBuyerAuctionPopup.setText(dataFromAPI.TotalBags)
            dialogBinding.tvTotalAmountBuyerAuctionPopup.setText(amountNF)
            dialogBinding.tvLeftBagsBuyerAuctionPopup.setText(dataFromAPI.LeftBags)

            val popupAdapter = BuyerAuctionPopupAdapter(requireContext(),dataFromAPI.AuctionDetailsModel,dataFromAPI.CommodityBhartiPrice)
            dialogBinding.rcViewBuyerAuctionPopup.adapter = popupAdapter
            dialogBinding.rcViewBuyerAuctionPopup.invalidate()

            dialogBinding.btnEditAuctionBuyerAuctionPopup.setOnClickListener {
                isAuctionForUpdate = true
                alertDialog.dismiss()
            }
            dialogBinding.btnClosePopupBuyerAuctionPopup.setOnClickListener {
                alertDialog.dismiss()
                navController.navigateUp()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "currentAuctionEditPopup: ${e.message}", )
        }
    }

    fun redirectToLogin(){
        try {
            PrefUtil.setBoolean(PrefUtil.KEY_LOGGEDIN,false)
            requireActivity().startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "redirectToLogin: ${e.message}", )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}