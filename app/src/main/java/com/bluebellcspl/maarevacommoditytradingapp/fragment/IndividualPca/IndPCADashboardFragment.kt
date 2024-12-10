package com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca

import ConnectionCheck
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.icu.text.NumberFormat
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.HomeActivity
import com.bluebellcspl.maarevacommoditytradingapp.LoginActivity
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.IndPCAAuctionReportAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentIndPCADashboardBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.pca.PCADashboardFragmentDirections
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchBuyerPreviousAuctionAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchIndPCAAuctionAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchIndPCAAuctionReport
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchShopMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAAuctionFetchModel
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAAuctionReportModel
import com.bluebellcspl.maarevacommoditytradingapp.model.ShopMasterAPICallModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.materialswitch.MaterialSwitch
import java.util.Locale

class IndPCADashboardFragment : Fragment() {
    var _binding:FragmentIndPCADashboardBinding? = null
    val binding get() = _binding!!
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val navController by lazy { findNavController() }
    val TAG = "IndPCADashboardFragment"
    lateinit var menuHost: MenuHost
    lateinit var _CommodityList : ArrayList<CommodityDetail>
    lateinit var _CommodityNameList : ArrayList<String>
    private var PREV_AUCTION_SELECTED_DATE = ""
    private var languageSwitch: MaterialSwitch? = null
    private var languageLayout:LinearLayout? = null
    var isInitial = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_ind_p_c_a_dashboard, container, false)
        Log.d("??", "onCreateView: ON_CREATEVIEW_IND_PCA_DASHBOARD_FRAGMENT")
        if (PrefUtil.getSystemLanguage()!!.isEmpty()){
            PrefUtil.setSystemLanguage("en")
        }
        Log.d(TAG, "onCreateView: CURRENT_SYS_LANG : ${PrefUtil.getSystemLanguage()}")
        if (PrefUtil.getSystemLanguage()!!.equals("gu")){
            var gujCommodityName = DatabaseManager.ExecuteScalar(Query.getGujaratiCommodityName(PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"")!!))
            if (gujCommodityName.equals("invalid")){
                gujCommodityName = ""
            }
            binding.actCommodityIndPCADashboardFragment.setText(gujCommodityName)
        }else{

            binding.actCommodityIndPCADashboardFragment.setText(PrefUtil.getString(PrefUtil.KEY_COMMODITY_NAME,""))
        }
        binding.tvDateNewIndPCADashboardFragment.setText(DateUtility().getCompletionDate())
        _CommodityList = getCommodityfromDB()

        languageSwitch = (activity as HomeActivity).binding.toolbarHome.languageSwitch
        languageLayout = (activity as HomeActivity).binding.toolbarHome.languageLayout

        languageLayout!!.visibility = View.VISIBLE

        if (PrefUtil.getSystemLanguage().equals("gu")){
            languageSwitch!!.isChecked = true
        }else{
            languageSwitch!!.isChecked = false
        }

        if (ConnectionCheck.isConnected(requireContext()))
        {
            callAPI()
        }else{
            commonUIUtility.showToast(requireContext().getString(R.string.no_internet_connection))
        }
        binding.actCommodityIndPCADashboardFragment.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.actCommodityIndPCADashboardFragment.showDropDown()
        }
        binding.swipeToRefreshIndPCADashboardFragment.setOnRefreshListener {
            binding.swipeToRefreshIndPCADashboardFragment.isRefreshing = false
            callAPI()
        }

        menuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.ind_pca_ds_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.btn_Logout_Ind_PCA -> {
                        logoutDialog()
                    }
                    R.id.btn_Invoice_Ind_PCA -> {
                        if (!_CommodityNameList.contains(binding.actCommodityIndPCADashboardFragment.text.toString())){
                            commonUIUtility.showToast(requireContext().getString(R.string.please_select_commodity_alert_msg))
                        }else
                        {
                            navController.navigate(IndPCADashboardFragmentDirections.actionIndPCADashboardFragmentToIndPCAInvoiceFragment())
                        }
                    }R.id.btn_Invoice_Stock_Ind_PCA -> {
                        if (!_CommodityNameList.contains(binding.actCommodityIndPCADashboardFragment.text.toString())){
                            commonUIUtility.showToast(requireContext().getString(R.string.please_select_commodity_alert_msg))
                        }else
                        {
                            navController.navigate(IndPCADashboardFragmentDirections.actionIndPCADashboardFragmentToIndPCAInvoiceStockFragment())
                        }
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)

        binding.actCommodityIndPCADashboardFragment.setOnItemClickListener { adapterView, view, position, long ->
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

            if (ConnectionCheck.isConnected(requireContext()))
            {
                FetchIndPCAAuctionAPI(requireContext(),this@IndPCADashboardFragment)
                FetchIndPCAAuctionReport(requireContext(),this@IndPCADashboardFragment,"")
            }else{
                commonUIUtility.showToast(requireContext().getString(R.string.no_internet_connection))
            }
        }

        languageSwitch!!.setOnCheckedChangeListener { _, isChecked ->
            var selectedLanguage = if (isChecked) "gu" else "en"
            PrefUtil.setSystemLanguage(selectedLanguage)
            setLocale(selectedLanguage)
        }

        setOnClickListeners()
        return binding.root
    }

    private fun callAPI() {
        if (ConnectionCheck.isConnected(requireContext()))
        {
            FetchShopMasterAPI(requireContext(),requireActivity(),
                ShopMasterAPICallModel(PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString(), "GetAPMCwise",PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,"").toString())
            )

            if (PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"")!!.isNotEmpty()){
                FetchIndPCAAuctionAPI(requireContext(),this@IndPCADashboardFragment)
                FetchIndPCAAuctionReport(requireContext(),this@IndPCADashboardFragment,PREV_AUCTION_SELECTED_DATE)
            }else
            {
                commonUIUtility.showToast(requireContext().getString(R.string.please_select_commodity_alert_msg))
            }
        }else{
            commonUIUtility.showToast(requireContext().getString(R.string.no_internet_connection))
        }
    }

    fun updateAuctionUI(auctionModel:IndPCAAuctionFetchModel){
        val auctionDataStringBuilder = StringBuilder()
        auctionDataStringBuilder.append(requireContext().getString(R.string.bags_lbl)+ " ${auctionModel.TotalBags}")
        binding.tvPurchasedBagsNewIndPCADashboardFragment.setText(auctionDataStringBuilder.toString())
        auctionDataStringBuilder.clear()

        val rateNF = NumberFormat.getCurrencyInstance().format(auctionModel.TotalAveragePrice.toDouble()).substring(1)
        auctionDataStringBuilder.append(requireContext().getString(R.string.rate_lbl)+ " $rateNF")
        binding.tvPurchasedAvgRateNewIndPCADashboardFragment.setText(auctionDataStringBuilder.toString())
        auctionDataStringBuilder.clear()

        val costNF = NumberFormat.getCurrencyInstance().format(auctionModel.TotalAmount.toDouble()).substring(1)
        auctionDataStringBuilder.append(requireContext().getString(R.string.cost_lbl)+ " $costNF")
        binding.tvPurchasedTotalCostNewIndPCADashboardFragment.setText(auctionDataStringBuilder.toString())
        auctionDataStringBuilder.clear()

    }

    private fun setOnClickListeners() {
        try {
            binding.cvPCAAuctionNewIndPCADashboardFragment.setOnClickListener {
                if (!_CommodityNameList.contains(binding.actCommodityIndPCADashboardFragment.text.toString())){
                    commonUIUtility.showToast(requireContext().getString(R.string.please_select_commodity_alert_msg))
                }else
                {
                    navController.navigate(IndPCADashboardFragmentDirections.actionIndPCADashboardFragmentToIndPCAAuctionFragment())
                }
            }

            binding.btnDatePickerNewIndPCADashboardFragment.setOnClickListener {
                showDatePickerDialog()
            }

            binding.cvPreviousAuctionNewIndPCADashboardFragment.setOnClickListener {
                if (!_CommodityNameList.contains(binding.actCommodityIndPCADashboardFragment.text.toString())){
                    commonUIUtility.showToast(requireContext().getString(R.string.please_select_commodity_alert_msg))
                }else
                {
                    navController.navigate(IndPCADashboardFragmentDirections.actionIndPCADashboardFragmentToIndPCAAuctionReportFragment(PREV_AUCTION_SELECTED_DATE))
                }
            }
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "setOnClickListeners: ${e.message}")
        }
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
                        cursor.getString(cursor.getColumnIndexOrThrow("GujaratiCommodityName"))
                    )
                    localArrayList.add(model)
                }
            }
            var commodityAdapter :ArrayAdapter<String>
            if(PrefUtil.getSystemLanguage().equals("gu")){
                _CommodityNameList = ArrayList(localArrayList.map { it.CommodityGujName })
                commodityAdapter = commonUIUtility.getCustomArrayAdapter(_CommodityNameList)
            }else
            {
                _CommodityNameList = ArrayList(localArrayList.map { it.CommodityName })
                commodityAdapter = commonUIUtility.getCustomArrayAdapter(_CommodityNameList)
            }

            binding.actCommodityIndPCADashboardFragment.setAdapter(commodityAdapter)
            localArrayList.forEach {
                Log.d(TAG, "getCommodityfromDB: ID - APMC : ${it.CommodityId} - ${it.CommodityName}")
            }
            Log.d(TAG, "getCommodityfromDB: AMPCList : $localArrayList")
            cursor?.close()
        }catch (e:Exception)
        {
            cursor?.close()
            _CommodityNameList = ArrayList()
            localArrayList.clear()
            e.printStackTrace()
            Log.e(TAG, "getCommodityfromDB: ${e.message}")
        }
        return localArrayList
    }

    data class CommodityDetail(var CommodityId:String,var CommodityName:String, var CommodityGujName:String)

    fun logoutDialog() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setCancelable(false)
        alertDialog.setTitle(requireContext().getString(R.string.logout))
        alertDialog.setMessage(requireContext().getString(R.string.do_you_want_to_logout_alert_msg))
        alertDialog.setPositiveButton(requireContext().getString(R.string.yes), object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                p0!!.dismiss()
                PrefUtil.setBoolean(PrefUtil.KEY_LOGGEDIN,false)
                DatabaseManager.ExecuteScalar(Query.deleteAllShop())
                requireActivity().startActivity(Intent(activity, LoginActivity::class.java))
                requireActivity().finish()
            }
        })
        alertDialog.setNegativeButton(requireContext().getString(R.string.no), object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                p0!!.dismiss()
            }
        })
        alertDialog.show()
    }

    private fun showDatePickerDialog() {
        val calendarConstraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.before(System.currentTimeMillis()))
            .build()
        val builder =
            MaterialDatePicker.Builder.datePicker().setCalendarConstraints(calendarConstraints)

        val datePicker = builder.build()
        datePicker.addOnPositiveButtonClickListener {
            // Handle the selected date
            val selectedDateInMillis = it
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            val date = dateFormat.format(selectedDateInMillis)
            PREV_AUCTION_SELECTED_DATE = date
            FetchIndPCAAuctionReport(
                requireContext(),
                this@IndPCADashboardFragment,
                PREV_AUCTION_SELECTED_DATE
            )
        }
        if (!datePicker.isAdded) {
            datePicker.show(childFragmentManager, datePicker.toString())
        }
    }

    fun bindReportData(model: IndPCAAuctionReportModel){
        try {
            val stringBuilder = StringBuilder()
            stringBuilder.append(requireContext().getString(R.string.date_lbl) +" ${model.Date}")

            PREV_AUCTION_SELECTED_DATE = model.Date
            binding.tvPreviousAuctionDateNewIndPCADashboardFragment.setText(stringBuilder.toString())
            stringBuilder.clear()

            val totalAvgRateNF = NumberFormat.getCurrencyInstance().format(model.TotalAveragePrice.toDouble()).substring(1)
            stringBuilder.append(requireContext().getString(R.string.avg_rate_lbl) +" $totalAvgRateNF")
            binding.tvPreviousAuctionAvgRateNewIndPCADashboardFragment.setText(stringBuilder.toString())
            stringBuilder.clear()

            val totalAmountNF = NumberFormat.getCurrencyInstance().format(model.TotalAmount.toDouble()).substring(1)
            stringBuilder.append(requireContext().getString(R.string.cost_lbl) +" $totalAmountNF")
            binding.tvPreviousAuctionTotalCostNewIndPCADashboardFragment.setText(stringBuilder.toString())
            stringBuilder.clear()

            stringBuilder.append(requireContext().getString(R.string.bags_lbl) +" ${model.TotalBags}")
            binding.tvPreviousAuctionPurchasedBagsNewIndPCADashboardFragment.setText(stringBuilder.toString())
            stringBuilder.clear()

        }catch (e:Exception)
        {
            Log.e(TAG, "bindReportData: ${e.message}")
            e.printStackTrace()
        }
    }

    fun setLocale(languageCode: String?) {
        val activityConf = Configuration()
        val newLocale = Locale(languageCode)
        activityConf.setLocale(newLocale)
        requireContext().getResources()
            .updateConfiguration(activityConf, requireContext().getResources().getDisplayMetrics())
        if (isInitial) {
            isInitial = false
        } else {
            requireActivity().finish()
            startActivity(requireActivity().getIntent())
        }
    }

    override fun onResume() {
        super.onResume()
        languageSwitch = (activity as HomeActivity).binding.toolbarHome.languageSwitch
        languageLayout = (activity as HomeActivity).binding.toolbarHome.languageLayout

        languageLayout!!.visibility = View.VISIBLE

        if (PrefUtil.getSystemLanguage().equals("gu")){
            languageSwitch!!.isChecked = true
        }else{
            languageSwitch!!.isChecked = false
        }

        if (PrefUtil.getSystemLanguage()!!.equals("gu")){
            var gujCommodityName = DatabaseManager.ExecuteScalar(Query.getGujaratiCommodityName(PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"")!!))
            if (gujCommodityName.equals("invalid")){
                gujCommodityName = ""
            }
            binding.actCommodityIndPCADashboardFragment.setText(gujCommodityName)
        }else{

            binding.actCommodityIndPCADashboardFragment.setText(PrefUtil.getString(PrefUtil.KEY_COMMODITY_NAME,""))
        }
        binding.tvDateNewIndPCADashboardFragment.setText(DateUtility().getCompletionDate())
        _CommodityList = getCommodityfromDB()

        if (ConnectionCheck.isConnected(requireContext()))
        {
            callAPI()
        }else{
            commonUIUtility.showToast(requireContext().getString(R.string.no_internet_connection))
        }
        Log.d("??", "onResume: RESUME_IND_PCA_DASHBOARD_FRAGMENT")
    }

    override fun onStop() {
        super.onStop()
        Log.d("??", "onStop: STOP_IND_PCA_DASHBOARD_FRAGMENT")
//        languageLayout!!.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("??", "onDestroy: DESTROY_VIEW_IND_PCA_DASHBOARD_FRAGMENT")
        _binding = null
        languageLayout!!.visibility = View.GONE
    }
}