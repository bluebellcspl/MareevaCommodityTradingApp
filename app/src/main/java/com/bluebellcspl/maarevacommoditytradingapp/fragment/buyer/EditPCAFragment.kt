package com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentEditPCABinding
import com.bluebellcspl.maarevacommoditytradingapp.master.POSTPCAEditAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAListModelItem


class EditPCAFragment : Fragment() {
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val TAG = "EditPCAFragment"
    private val navController by lazy { findNavController() }
    lateinit var binding: FragmentEditPCABinding
    private val args by navArgs<EditPCAFragmentArgs>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_p_c_a, container, false)
        val model = args.pcaListModel
        binding.edtAPMCEditPCAFragment.setText(model.APMCName)
        binding.edtStateEditPCAFragment.setText(model.StateName)
        binding.edtDistrictEditPCAFragment.setText(model.DistrictName)
        binding.edtCommodityEditPCAFragment.setText(model.CommodityName)
        binding.edtPCANameEditPCAFragment.setText(model.PCAName)
        binding.edtPhoneNoEditPCAFragment.setText(model.PCAPhoneNumber)
        binding.edtAddressEditPCAFragment.setText(model.Address)
        binding.edtPCACommissionEditPCAFragment.setText(model.PCACommission)
        binding.edtGCACommissionEditPCAFragment.setText(model.GCACommission)
        binding.edtMarketCessEditPCAFragment.setText(model.MarketCess)

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
            } else if (binding.edtAddressEditPCAFragment.text.toString().isEmpty()) {
                commonUIUtility.showToast(resources.getString(R.string.please_enter_address_alert_msg))
            } else if (binding.edtGCACommissionEditPCAFragment.text.toString().isEmpty()) {
                commonUIUtility.showToast(resources.getString(R.string.please_enter_gca_commission_alert_msg))
            }else if (binding.edtPCACommissionEditPCAFragment.text.toString().isEmpty()){
                commonUIUtility.showToast(resources.getString(R.string.please_enter_pca_commission_alert_msg))
            }else if (binding.edtMarketCessEditPCAFragment.text.toString().isEmpty()){
                commonUIUtility.showToast(resources.getString(R.string.please_enter_market_cess_alert_msg))
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
                args.pcaListModel.APMCName,
                binding.edtAddressEditPCAFragment.text.toString().trim(),
                args.pcaListModel.ApprStatus,
                args.pcaListModel.BuyerId,
                args.pcaListModel.CommodityId,
                args.pcaListModel.CommodityName,
                args.pcaListModel.CompanyCode,
                args.pcaListModel.CreateDate,
                args.pcaListModel.CreateUser,
                args.pcaListModel.DistrictId,
                args.pcaListModel.DistrictName,
                binding.edtEmailEditPCAFragment.text.toString().trim(),
                binding.edtGCACommissionEditPCAFragment.text.toString().trim(),
                getIsActiveStatus(),
                binding.edtMarketCessEditPCAFragment.text.toString().trim(),
                binding.edtPCACommissionEditPCAFragment.text.toString().trim(),
                args.pcaListModel.PCAId,
                binding.edtPCANameEditPCAFragment.text.toString().trim(),
                args.pcaListModel.PCAPhoneNumber,
                args.pcaListModel.RoleId,
                args.pcaListModel.RoleName,
                args.pcaListModel.StateId,
                args.pcaListModel.StateName,
                DateUtility().getyyyyMMdd(),
                "",
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString(),
                PrefUtil.getString(PrefUtil.KEY_TYPE_OF_USER,"").toString()
            )

            POSTPCAEditAPI(requireContext(),requireActivity(),this,model)
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

}