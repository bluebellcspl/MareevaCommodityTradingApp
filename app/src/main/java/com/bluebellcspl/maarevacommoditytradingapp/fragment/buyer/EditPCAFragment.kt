package com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentEditPCABinding
import com.bluebellcspl.maarevacommoditytradingapp.master.POSTPCAEditAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAListModelItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class EditPCAFragment : Fragment() {
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val TAG = "EditPCAFragment"
    private val navController by lazy { findNavController() }
    lateinit var binding: FragmentEditPCABinding
    private val args by navArgs<EditPCAFragmentArgs>()
    lateinit var apmcList: ArrayList<String>
    var marketCess = ""
    var apmcId = ""
    var stateId = ""
    var districtId = ""
    var stateName = ""
    var districtName = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_p_c_a, container, false)
        val model = args.pcaListModel
        binding.actAPMCEditPCAFragment.setText(model.APMCName)
        binding.edtStateEditPCAFragment.setText(model.StateName)
        binding.edtDistrictEditPCAFragment.setText(model.DistrictName)
        binding.edtCommodityEditPCAFragment.setText(model.CommodityName)
        binding.edtPCANameEditPCAFragment.setText(model.PCAName)
        binding.edtPhoneNoEditPCAFragment.setText(model.PCAPhoneNumber)
        binding.edtAddressEditPCAFragment.setText(model.Address)
        binding.edtPCACommissionEditPCAFragment.setText(model.PCACommission)
        binding.edtGCACommissionEditPCAFragment.setText(model.GCACommission)
        marketCess = DatabaseManager.ExecuteScalar(Query.getMarketCessByAPMCId(model.APMCId))!!
        binding.edtMarketCessEditPCAFragment.setText(model.MarketCess)

        apmcList = bindAPMCDropDown()
        val apmcTextWatch: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                apmcId = ""
                stateId = ""
                districtId = ""
                stateName = ""
                districtName = ""
                binding.edtStateEditPCAFragment.setText("")
                binding.edtDistrictEditPCAFragment.setText("")

                if (p0!!.isNotEmpty()) {
                    apmcId = DatabaseManager.ExecuteScalar(
                        Query.getAPMCIdByAPMCName(
                            p0.toString().trim()
                        )
                    ).toString()
                    stateId =
                        DatabaseManager.ExecuteScalar(Query.getStateIdByAPMCId(apmcId)).toString()
                    districtId = DatabaseManager.ExecuteScalar(Query.getDistrictIdByAPMCId(apmcId))
                        .toString()

                    stateName =
                        DatabaseManager.ExecuteScalar(Query.getStateNameByAPMCId(apmcId))!!
                    districtName =
                        DatabaseManager.ExecuteScalar(Query.getDistrictNameByAPMCId(apmcId))!!

                     marketCess = DatabaseManager.ExecuteScalar(Query.getMarketCessByAPMCId(apmcId))!!

                    if (stateName.isEmpty() || stateName.equals("invalid") || districtName.isEmpty() || districtName.equals("invalid")) {
                        binding.edtStateEditPCAFragment.setText("")
                        binding.edtDistrictEditPCAFragment.setText("")
                        binding.edtMarketCessEditPCAFragment.setText("")
                    } else {
                        binding.edtStateEditPCAFragment.setText("")
                        binding.edtDistrictEditPCAFragment.setText("")
                        binding.edtMarketCessEditPCAFragment.setText("")
                        binding.edtStateEditPCAFragment.setText(stateName)
                        binding.edtDistrictEditPCAFragment.setText(districtName)
                        binding.edtMarketCessEditPCAFragment.setText(marketCess)

                        model.APMCId = apmcId
//                        model.MarketCess = marketCess
                        model.StateId = stateId
                        model.StateName = binding.edtStateEditPCAFragment.text.toString().trim()
                        model.DistrictId = districtId
                        model.DistrictName = binding.edtDistrictEditPCAFragment.text.toString().trim()
                    }
                }
            }
        }

        binding.actAPMCEditPCAFragment.addTextChangedListener(apmcTextWatch)
        binding.actAPMCEditPCAFragment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0!!.isNotEmpty()) {
                    apmcId = DatabaseManager.ExecuteScalar(
                        Query.getAPMCIdByAPMCName(
                            p0.toString().trim()
                        )
                    ).toString()

                    marketCess = DatabaseManager.ExecuteScalar(Query.getMarketCessByAPMCId(apmcId))!!
                    binding.edtMarketCessEditPCAFragment.setText(marketCess)
                    model.MarketCess = marketCess
                    model.APMCId = apmcId
                    Log.d(TAG, "afterTextChanged: APMC_ID_EDIT : $apmcId")
                    Log.d(TAG, "afterTextChanged: MARKET_CESS_EDIT : $marketCess")
                    model.APMCId = apmcId
                }
            }
        })
        if (model.IsActive.equals("true")) {
            binding.actIsActiveEditPCAFragment.setText("Enable")
        } else {
            binding.actIsActiveEditPCAFragment.setText("Disable")
        }
        val isActiveAdapter =
            commonUIUtility.setCustomArrayAdapter(requireActivity().resources.getStringArray(R.array.isActive_Array))
        binding.actIsActiveEditPCAFragment.setAdapter(isActiveAdapter)

        binding.btnUpdateEditPCAFragment.setOnClickListener {
            if (binding.edtPCANameEditPCAFragment.text.toString().isEmpty()) {
                commonUIUtility.showToast(resources.getString(R.string.please_enter_pca_name_alert_msg))
            }else if (binding.actAPMCEditPCAFragment.text.toString().isEmpty())
            {
                commonUIUtility.showToast(getString(R.string.please_select_apmc_alert_msg))
            }else if (binding.edtStateEditPCAFragment.text.toString().isEmpty())
            {
                commonUIUtility.showToast(getString(R.string.please_select_valid_apmc_alert_msg))
            }
            else if (binding.edtAddressEditPCAFragment.text.toString().isEmpty()) {
                commonUIUtility.showToast(resources.getString(R.string.please_enter_address_alert_msg))
            }
//            else if (binding.edtGCACommissionEditPCAFragment.text.toString().isEmpty()) {
//                commonUIUtility.showToast(resources.getString(R.string.please_enter_gca_commission_alert_msg))
//            }
//            else if (binding.edtPCACommissionEditPCAFragment.text.toString().isEmpty()){
//                commonUIUtility.showToast(resources.getString(R.string.please_enter_pca_commission_alert_msg))
//            }
//            else if (binding.edtMarketCessEditPCAFragment.text.toString().isEmpty()){
//                commonUIUtility.showToast(resources.getString(R.string.please_enter_market_cess_alert_msg))
//            }
            else
            {
                displayAlertDialogForPCA()
            }
        }
        return binding.root
    }

    fun displayAlertDialogForPCA() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setCancelable(false)
        alertDialog.setTitle("Alert")
        alertDialog.setMessage("Do you want to Save PCA?")
        alertDialog.setPositiveButton("Yes", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                p0!!.dismiss()
                updatePCAData()
            }
        })
        alertDialog.setNegativeButton("No", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                p0!!.dismiss()
            }
        })
        alertDialog.show()
    }

    private fun updatePCAData() {
        try {
            val model = PCAListModelItem(
                args.pcaListModel.APMCId,
                binding.actAPMCEditPCAFragment.text.toString().trim(),
                binding.edtAddressEditPCAFragment.text.toString().trim(),
                args.pcaListModel.AdharNo,
                args.pcaListModel.AdharPhoto,
                args.pcaListModel.ApprStatus,
                args.pcaListModel.BuyerId,
                args.pcaListModel.CityId,
                args.pcaListModel.CityName,
                args.pcaListModel.CommodityId,
                args.pcaListModel.CommodityName,
                args.pcaListModel.CompanyCode,
                args.pcaListModel.CreateDate,
                args.pcaListModel.CreateUser,
                args.pcaListModel.DistrictId,
                args.pcaListModel.DistrictName,
                binding.edtEmailEditPCAFragment.text.toString().trim(),
                args.pcaListModel.GCACommission,
                args.pcaListModel.GSTCertiPhoto,
                args.pcaListModel.GSTNo,
                getIsActiveStatus(),
                args.pcaListModel.LabourCharges,
                args.pcaListModel.LicenseCopyPhoto,
                args.pcaListModel.MarketCess,
                args.pcaListModel.Mobile2,
                args.pcaListModel.PCACommission,
                args.pcaListModel.PCAId,
                binding.edtPCANameEditPCAFragment.text.toString().trim(),
                args.pcaListModel.GujaratiPCAName,
                args.pcaListModel.PCAShortName,
                args.pcaListModel.GujaratiShortPCAName,
                binding.edtPhoneNoEditPCAFragment.text.toString().trim(),
                args.pcaListModel.PCARegId,
                args.pcaListModel.PanCardNo,
                args.pcaListModel.PanCardPhoto,
                args.pcaListModel.ProfilePic,
                args.pcaListModel.RoleId,
                args.pcaListModel.RoleName,
                args.pcaListModel.StateId,
                args.pcaListModel.StateName,
                DateUtility().getyyyyMMdd(),
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString()
            )
            if (ConnectionCheck.isConnected(requireContext()))
            {
                POSTPCAEditAPI(requireContext(),requireActivity(),this,model,marketCess)
            }else{
                commonUIUtility.showToast(getString(R.string.no_internet_connection))
            }
        }catch (e:Exception)
        {
            Log.e(TAG, "updatePCAData: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun  getIsActiveStatus():String{
        var status = ""
        if (binding.actIsActiveEditPCAFragment.text.toString().equals("Enable")){
            status = "true"
        }else{
            status = "false"
        }
        return status
    }

    fun successRedirect(){
        navController.navigate(EditPCAFragmentDirections.actionEditPCAFragmentToPCAListFragment())
    }

    fun bindAPMCDropDown(): ArrayList<String> {
        val dataList = ArrayList<String>()
        try {
            lifecycleScope.launch(Dispatchers.IO)
            {
                val cursor = DatabaseManager.ExecuteRawSql(Query.getAPMCName())
                if (cursor != null && cursor.count > 0) {
                    dataList.clear()
                    while (cursor.moveToNext()) {
                        dataList.add(cursor.getString(cursor.getColumnIndexOrThrow("APMCName")))
                    }
                    dataList.sort()
                    withContext(Dispatchers.Main){
                        val apmcAdapter = commonUIUtility.getCustomArrayAdapter(dataList)
                        binding.actAPMCEditPCAFragment.setAdapter(apmcAdapter)
                    }
                    cursor.close()
                }
            }
        } catch (e: Exception) {
            dataList.clear()
            e.printStackTrace()
            Log.e(TAG, "bindAPMCDropDown: ${e.message}")
        }
        return dataList
    }

}