package com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.LoginActivity
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.EditableDecimalInputFilter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentAddPCABinding
import com.bluebellcspl.maarevacommoditytradingapp.master.POSTPCAInsertAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.POSTPCAInsertModel

class AddPCAFragment : Fragment() {
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val TAG = "AddPCAFragment"
    private val navController by lazy { findNavController() }
    lateinit var binding: FragmentAddPCABinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_p_c_a, container, false)
        binding.edtAPMCAddPCAFragment.setText(
            PrefUtil.getString(PrefUtil.KEY_APMC_NAME, "").toString()
        )
        binding.edtStateAddPCAFragment.setText(
            PrefUtil.getString(PrefUtil.KEY_STATE_NAME, "").toString()
        )
        binding.edtDistrictAddPCAFragment.setText(
            PrefUtil.getString(PrefUtil.KEY_DISTRICT_NAME, "").toString()
        )
        binding.edtCommodityAddPCAFragment.setText(
            PrefUtil.getString(
                PrefUtil.KEY_COMMODITY_NAME,
                ""
            ).toString()
        )

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
            } else if (binding.edtAddressAddPCAFragment.text.toString().isEmpty()) {
                commonUIUtility.showToast(getString(R.string.please_enter_address_alert_msg))
            } else if (binding.edtEmailAddPCAFragment.text.toString().isEmpty()) {
                commonUIUtility.showToast(getString(R.string.please_enter_email_id_alert_msg))
            } else if (!Patterns.EMAIL_ADDRESS.matcher(
                    binding.edtEmailAddPCAFragment.text.toString().trim()
                ).matches()
            ) {
                commonUIUtility.showToast(getString(R.string.please_enter_valid_email_id_alert_msg))
            } else if (binding.edtGCACommissionAddPCAFragment.text.toString().isEmpty()) {
                commonUIUtility.showToast(getString(R.string.please_enter_gca_commission_alert_msg))
            } else if (binding.edtPCACommissionAddPCAFragment.text.toString().isEmpty()) {
                commonUIUtility.showToast(getString(R.string.please_enter_pca_commission_alert_msg))
            } else if (binding.edtMarketCessAddPCAFragment.text.toString().isEmpty()) {
                commonUIUtility.showToast(getString(R.string.please_enter_market_cess_alert_msg))
            } else if (binding.edtPhoneNoAddPCAFragment.text.toString().length < 10) {
                commonUIUtility.showToast(getString(R.string.please_enter_valid_phone_no_alert_msg))
            }else
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
                PrefUtil.getString(PrefUtil.KEY_APMC_ID,"").toString(),
                PrefUtil.getString(PrefUtil.KEY_APMC_NAME,"").toString(),
                "",
                binding.edtAddressAddPCAFragment.text.toString().trim(),
                "0",
                PrefUtil.getString(PrefUtil.KEY_BUYER_ID,"").toString(),
                PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"").toString(),
                PrefUtil.getString(PrefUtil.KEY_COMMODITY_NAME,"").toString(),
                PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,"").toString(),
                DateUtility().getyyyyMMdd(),
                PrefUtil.getString(PrefUtil.KEY_BUYER_ID,"").toString(),
                PrefUtil.getString(PrefUtil.KEY_DISTRICT_ID,"").toString(),
                PrefUtil.getString(PrefUtil.KEY_DISTRICT_NAME,"").toString(),
                binding.edtEmailAddPCAFragment.text.toString().trim(),
                binding.edtGCACommissionAddPCAFragment.text.toString().trim(),
                "1",
                binding.edtMarketCessAddPCAFragment.text.toString().trim(),
                binding.edtPCACommissionAddPCAFragment.text.toString().trim(),
                "",
                binding.edtPCANameAddPCAFragment.text.toString().trim(),
                binding.edtPhoneNoAddPCAFragment.text.toString().trim(),
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString(),
                PrefUtil.getString(PrefUtil.KEY_ROLE_ID,"").toString(),
                PrefUtil.getString(PrefUtil.KEY_STATE_ID,"").toString(),
                PrefUtil.getString(PrefUtil.KEY_STATE_NAME,"").toString(),
                commonUIUtility.getUserType(),
                "",
                ""
            )

            POSTPCAInsertAPI(requireContext(),requireActivity(),this,model)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "sendPCAData: ${e.message}")
        }
    }

    fun clearData(){
        binding.edtPCANameAddPCAFragment.setText("")
        binding.edtPhoneNoAddPCAFragment.setText("")
        binding.edtAddressAddPCAFragment.setText("")
        binding.edtEmailAddPCAFragment.setText("")
        binding.edtGCACommissionAddPCAFragment.setText("")
        binding.edtPCACommissionAddPCAFragment.setText("")
        binding.edtMarketCessAddPCAFragment.setText("")
    }
}