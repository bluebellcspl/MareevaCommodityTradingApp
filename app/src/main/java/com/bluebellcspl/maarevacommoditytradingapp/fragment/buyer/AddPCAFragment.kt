package com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.LoginActivity
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.EditableDecimalInputFilter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentAddPCABinding
import com.bluebellcspl.maarevacommoditytradingapp.master.POSTPCAInsertAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.POSTPCAInsertModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddPCAFragment : Fragment() {
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val TAG = "AddPCAFragment"
    private val navController by lazy { findNavController() }
    lateinit var binding: FragmentAddPCABinding
    lateinit var apmcList: ArrayList<String>
    lateinit var apmcId: String
    lateinit var stateId: String
    lateinit var districtId: String
    lateinit var stateName: String
    lateinit var districtName: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_p_c_a, container, false)

        binding.edtCommodityAddPCAFragment.setText(PrefUtil.getString(PrefUtil.KEY_COMMODITY_NAME,"").toString())
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
                binding.edtStateAddPCAFragment.setText("")
                binding.edtDistrictAddPCAFragment.setText("")

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

                    val marketCess = DatabaseManager.ExecuteScalar(Query.getMarketCessByAPMCId(apmcId))!!

                    if (stateName.isEmpty() || stateName.equals("invalid") || districtName.isEmpty() || districtName.equals("invalid")) {
                        binding.edtStateAddPCAFragment.setText("")
                        binding.edtDistrictAddPCAFragment.setText("")
                        binding.edtMarketCessAddPCAFragment.setText("")
                    } else {
                        binding.edtStateAddPCAFragment.setText("")
                        binding.edtDistrictAddPCAFragment.setText("")
                        binding.edtMarketCessAddPCAFragment.setText("")
                        binding.edtStateAddPCAFragment.setText(stateName)
                        binding.edtDistrictAddPCAFragment.setText(districtName)
                        binding.edtMarketCessAddPCAFragment.setText(marketCess)
                    }
                }
            }
        }

        binding.actAPMCAddPCAFragment.addTextChangedListener(apmcTextWatch)
        binding.edtGCACommissionAddPCAFragment.filters =
            arrayOf<InputFilter>(EditableDecimalInputFilter(6, 3))
        binding.edtPCACommissionAddPCAFragment.filters =
            arrayOf<InputFilter>(EditableDecimalInputFilter(6, 3))
        binding.edtMarketCessAddPCAFragment.filters =
            arrayOf<InputFilter>(EditableDecimalInputFilter(6, 3))

        binding.btnSaveAddPCAFragment.setOnClickListener {
            if (binding.edtPCANameAddPCAFragment.text.toString().isEmpty()) {
                commonUIUtility.showToast(getString(R.string.please_enter_pca_name_alert_msg))
            } else if (binding.edtPhoneNoAddPCAFragment.text.toString().isEmpty()) {
                commonUIUtility.showToast(getString(R.string.please_enter_phone_no))
            }
            else if (binding.edtPhoneNoAddPCAFragment.text.toString().length<10) {
                commonUIUtility.showToast(getString(R.string.please_enter_valid_phone_no_alert_msg))
            }
            else if (binding.actAPMCAddPCAFragment.text.toString().isEmpty()) {
                commonUIUtility.showToast(getString(R.string.please_select_apmc_alert_msg))
            }else if (binding.edtStateAddPCAFragment.text.toString().isEmpty())
            {
                commonUIUtility.showToast(getString(R.string.please_select_valid_apmc_alert_msg))
            }
//            else if (binding.edtAddressAddPCAFragment.text.toString().isEmpty()) {
//                commonUIUtility.showToast(getString(R.string.please_enter_address_alert_msg))
//            } else if (binding.edtEmailAddPCAFragment.text.toString().isEmpty()) {
//                commonUIUtility.showToast(getString(R.string.please_enter_email_id_alert_msg))
//            } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.edtEmailAddPCAFragment.text.toString().trim()).matches()) {
//                commonUIUtility.showToast(getString(R.string.please_enter_valid_email_id_alert_msg))
//            }
//            else if (binding.edtGCACommissionAddPCAFragment.text.toString().isEmpty())
//            {
//                commonUIUtility.showToast(getString(R.string.please_enter_gca_commission_alert_msg))
//            } else if (binding.edtPCACommissionAddPCAFragment.text.toString().isEmpty()) {
//                commonUIUtility.showToast(getString(R.string.please_enter_pca_commission_alert_msg))
//            } else if (binding.edtPhoneNoAddPCAFragment.text.toString().length < 10) {
//                commonUIUtility.showToast(getString(R.string.please_enter_valid_phone_no_alert_msg))
//            }
        else {
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
                sendPCAData()
            }
        })
        alertDialog.setNegativeButton("No", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                p0!!.dismiss()
            }
        })
        alertDialog.show()
    }

    private fun sendPCAData() {
        try {
            val model = POSTPCAInsertModel(
                apmcId,
                binding.actAPMCAddPCAFragment.text.toString().trim(),
                "insert",
                binding.edtAddressAddPCAFragment.text.toString().trim(),
                "0",
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString(),
                PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID, "").toString(),
                PrefUtil.getString(PrefUtil.KEY_COMMODITY_NAME, "").toString(),
                PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE, "").toString(),
                DateUtility().getyyyyMMdd(),
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString(),
                districtId,
                districtName,
                binding.edtEmailAddPCAFragment.text.toString().trim(),
                binding.edtGCACommissionAddPCAFragment.text.toString().trim(),
                "1",
                binding.edtMarketCessAddPCAFragment.text.toString().trim(),
                binding.edtPCACommissionAddPCAFragment.text.toString().trim(),
                "",
                binding.edtPCANameAddPCAFragment.text.toString().trim(),
                binding.edtPhoneNoAddPCAFragment.text.toString().trim(),
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString(),
                PrefUtil.getString(PrefUtil.KEY_ROLE_ID, "").toString(),
                stateId,
                stateName,
                commonUIUtility.getUserType(),
                "",
                ""
            )
            Log.d(TAG, "sendPCAData: ADD_PCA_MODEL : $model")
            POSTPCAInsertAPI(requireContext(), requireActivity(), this, model)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "sendPCAData: ${e.message}")
        }
    }

    fun clearData() {
        binding.edtPCANameAddPCAFragment.setText("")
        binding.edtPhoneNoAddPCAFragment.setText("")
        binding.edtAddressAddPCAFragment.setText("")
        binding.edtEmailAddPCAFragment.setText("")
        binding.actAPMCAddPCAFragment.setText("")
        binding.edtGCACommissionAddPCAFragment.setText("")
        binding.edtPCACommissionAddPCAFragment.setText("")
        binding.edtMarketCessAddPCAFragment.setText("")
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
                        binding.actAPMCAddPCAFragment.setAdapter(apmcAdapter)
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