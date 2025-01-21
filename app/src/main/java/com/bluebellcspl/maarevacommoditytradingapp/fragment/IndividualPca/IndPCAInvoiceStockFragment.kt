package com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca

import ConnectionCheck
import android.icu.text.NumberFormat
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.IndPCAInvoiceStockAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentIndPCAInvoiceStockBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca.IndPCADashboardFragment.CommodityDetail
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchCommodityForStockAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchIndPCAStockBuyerWiseAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.CommodityListModel
import com.bluebellcspl.maarevacommoditytradingapp.model.CommodityListModelItem
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoiceStockModel
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAInvoiceStockModelItem
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAStockBuyerWiseModel
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAStockBuyerWiseModelItem
import java.text.DecimalFormat

class IndPCAInvoiceStockFragment : Fragment(),IndPCAInvoiceStockAdapterListener {
    var _binding: FragmentIndPCAInvoiceStockBinding? = null
    val binding get() = _binding!!
    val TAG = "IndPCAInvoiceStockFragment"
    private val commonUIUtility: CommonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val navController: NavController by lazy { findNavController() }
    val _InvoiceStockList = arrayListOf<IndPCAStockBuyerWiseModelItem>()
    lateinit var _CommodityList : ArrayList<CommodityListModelItem>
    lateinit var _CommodityNameList : ArrayList<String>
    lateinit var adapter:IndPCAInvoiceStockAdapter
    var SELECTED_COMMODITY_NAME = ""
    var SELECTED_COMMODITY_ID = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_ind_p_c_a_invoice_stock, container, false)

//        if (PrefUtil.getSystemLanguage()!!.isEmpty()){
//            PrefUtil.setSystemLanguage("en")
//        }
//        Log.d(TAG, "onCreateView: CURRENT_SYS_LANG : ${PrefUtil.getSystemLanguage()}")
//        if (PrefUtil.getSystemLanguage()!!.equals("gu")){
//            var gujCommodityName = DatabaseManager.ExecuteScalar(Query.getGujaratiCommodityName(PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"")!!))
//            if (gujCommodityName.equals("invalid")){
//                gujCommodityName = ""
//            }
//            binding.actCommodityIndPCAInvoiceStockFragment.setText(gujCommodityName)
//        }else{
//
//            binding.actCommodityIndPCAInvoiceStockFragment.setText(PrefUtil.getString(PrefUtil.KEY_COMMODITY_NAME,""))
//        }
//
//        SELECTED_COMMODITY_ID = PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"").toString()
        callAPI()
        binding.actCommodityIndPCAInvoiceStockFragment.setOnItemClickListener { adapterView, view, position, long ->
            var commodityModel: CommodityListModelItem
            if (PrefUtil.getSystemLanguage().equals("gu")){
                commodityModel = _CommodityList.find {it.CommodityName.equals(adapterView.getItemAtPosition(position).toString())}!!
            }
            else{
                commodityModel = _CommodityList.find {it.CommodityName.equals(adapterView.getItemAtPosition(position).toString())}!!
            }

            Log.d(TAG, "onCreateView: SELECTED_COMMODITY_NAME : ${commodityModel.CommodityName}")
            Log.d(TAG, "onCreateView: SELECTED_COMMODITY_ID : ${commodityModel.CommodityId}")

            SELECTED_COMMODITY_NAME = commodityModel.CommodityName
            SELECTED_COMMODITY_ID = commodityModel.CommodityId
            PrefUtil.setString(PrefUtil.KEY_COMMODITY_NAME,SELECTED_COMMODITY_NAME)
            PrefUtil.setString(PrefUtil.KEY_COMMODITY_ID,SELECTED_COMMODITY_ID)

            callStockAPI()
        }
        return binding.root
    }

    private fun callAPI() {
        if (ConnectionCheck.isConnected(requireContext())){
            val commodityListmodel = IndPCAInvoiceStockFetchAPIModel(
                "AllCommodityList",
                "",
                "",
                ""+PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,""),
                "",
                ""+PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,""),
                ""+PrefUtil.getSystemLanguage()
            )
            FetchCommodityForStockAPI(requireContext(),this@IndPCAInvoiceStockFragment,commodityListmodel)

        }else
        {
            commonUIUtility.showToast(requireContext().getString(R.string.no_internet_connection))
        }
    }

    private fun callStockAPI() {
        if (ConnectionCheck.isConnected(requireContext())){
            val model = IndPCAInvoiceStockFetchAPIModel(
                "AllBuyer",
                "",
                ""+SELECTED_COMMODITY_ID,
                ""+PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,""),
                "",
                ""+PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,""),
                ""
            )
            FetchIndPCAStockBuyerWiseAPI(requireContext(),this@IndPCAInvoiceStockFragment,model)
        }else
        {
            commonUIUtility.showToast(requireContext().getString(R.string.no_internet_connection))
        }
    }

    fun bindStockCommodityList(dataList : ArrayList<CommodityListModelItem>){
        if (dataList.isNotEmpty()){
            var commodityAdapter : ArrayAdapter<String>
            if(PrefUtil.getSystemLanguage().equals("gu")){
                _CommodityNameList = ArrayList(dataList.map { it.CommodityName })
                commodityAdapter = commonUIUtility.getCustomArrayAdapter(_CommodityNameList)
            }else
            {
                _CommodityNameList = ArrayList(dataList.map { it.CommodityName })
                commodityAdapter = commonUIUtility.getCustomArrayAdapter(_CommodityNameList)
            }

            binding.actCommodityIndPCAInvoiceStockFragment.setAdapter(commodityAdapter)

            _CommodityList = dataList
        }
    }
    fun bindAPIStockData(dataList:IndPCAStockBuyerWiseModel){
        _InvoiceStockList.clear()
        _InvoiceStockList.addAll(dataList)
        bindStockListRCView(_InvoiceStockList)
        calculateHeaderTotal(_InvoiceStockList)
    }

    fun onNoDataFound()
    {
        _InvoiceStockList.clear()
        binding.tvAmountIndPCAInvoiceStockFragment.setText("")
        binding.tvBagsIndPCAInvoiceStockFragment.setText("")
        binding.rcViewIndPCAInvoiceStockFragment.adapter = null
    }

    private fun bindStockListRCView(dataList:ArrayList<IndPCAStockBuyerWiseModelItem>){
        try {
            adapter = IndPCAInvoiceStockAdapter(requireContext(),dataList,this@IndPCAInvoiceStockFragment)
            binding.rcViewIndPCAInvoiceStockFragment.adapter = adapter
            binding.rcViewIndPCAInvoiceStockFragment.invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindStockListRCView: ${e.message}")
        }
    }

    private fun calculateHeaderTotal(dataList:ArrayList<IndPCAStockBuyerWiseModelItem>){
        try {
            var total = 0.0
            var bags =0.0
            for (model in dataList){
                total += model.TotalAvailableAmount.toDouble()
                bags += model.TotalAvailableBags.toDouble()
            }

            binding.tvAmountIndPCAInvoiceStockFragment.text = numberFormat(total)
            binding.tvBagsIndPCAInvoiceStockFragment.text = formatDecimal(bags)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "calculateHeaderTotal: ${e.message}")
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
                        cursor.getString(cursor.getColumnIndexOrThrow("CommodityName"))
                    )
                    localArrayList.add(model)
                }
            }
            var commodityAdapter : ArrayAdapter<String>
            if(PrefUtil.getSystemLanguage().equals("gu")){
                _CommodityNameList = ArrayList(localArrayList.map { it.CommodityGujName })
                commodityAdapter = commonUIUtility.getCustomArrayAdapter(_CommodityNameList)
            }else
            {
                _CommodityNameList = ArrayList(localArrayList.map { it.CommodityName })
                commodityAdapter = commonUIUtility.getCustomArrayAdapter(_CommodityNameList)
            }

            binding.actCommodityIndPCAInvoiceStockFragment.setAdapter(commodityAdapter)
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

    override fun onResume() {
        super.onResume()
        _InvoiceStockList.clear()
        if (PrefUtil.getSystemLanguage()!!.isEmpty()){
            PrefUtil.setSystemLanguage("en")
        }
        Log.d(TAG, "onCreateView: CURRENT_SYS_LANG : ${PrefUtil.getSystemLanguage()}")
        if (PrefUtil.getSystemLanguage()!!.equals("gu")){
            var gujCommodityName = DatabaseManager.ExecuteScalar(Query.getGujaratiCommodityName(PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"")!!))
            if (gujCommodityName.equals("invalid")){
                gujCommodityName = ""
            }
            binding.actCommodityIndPCAInvoiceStockFragment.setText(gujCommodityName)
        }else{
            if (PrefUtil.getString(PrefUtil.KEY_COMMODITY_NAME,"").equals("invalid")){
                binding.actCommodityIndPCAInvoiceStockFragment.setText("")
            }else
            binding.actCommodityIndPCAInvoiceStockFragment.setText(PrefUtil.getString(PrefUtil.KEY_COMMODITY_NAME,""))
        }
        callAPI()
        SELECTED_COMMODITY_ID = PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"").toString()
        callStockAPI()
//        _CommodityList = getCommodityfromDB()4

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class IndPCAInvoiceStockFetchAPIModel(
        var Action: String,
        var BuyerId: String,
        var CommodityId: String,
        var CompanyCode: String,
        var CurrentDate: String,
        var IndividualPCARegId: String,
        var Language: String
    )

    override fun onInvoiceStockItemClick(model: Any, position: Int) {
        Log.d(TAG, "onInvoiceStockItemClick: BUYER_SELECTED_POSITION : $position")
        navController.navigate(IndPCAInvoiceStockFragmentDirections.actionIndPCAInvoiceStockFragmentToIndPCAInvoiceStockDetailFragment(model as IndPCAStockBuyerWiseModelItem))
    }

    private fun formatDecimal(value: Double): String {
        return DecimalFormat("0.00").format(value)
    }

    private fun numberFormat(value: Double): String {
        return NumberFormat.getCurrencyInstance().format(value).substring(1)
    }
}

interface IndPCAInvoiceStockAdapterListener{
    fun onInvoiceStockItemClick(model:Any,position:Int)
}