package com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca

import ConnectionCheck
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.LoginActivity
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentIndPCAProfileBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchIndPCAProfileAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAProfileModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.bumptech.glide.Glide

class IndPCAProfileFragment : Fragment() {
    var _binding: FragmentIndPCAProfileBinding? = null
    val binding get() = _binding!!
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val TAG = "IndPCAProfileFragment"
    private val navController: NavController by lazy { findNavController() }
    private lateinit var _ProfileData: IndPCAProfileModel
    lateinit var menuHost: MenuHost
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_ind_p_c_a_profile, container, false)
        if (ConnectionCheck.isConnected(requireContext())){
            FetchIndPCAProfileAPI(requireContext(),this@IndPCAProfileFragment)
        }else
        {
            commonUIUtility.showToast(requireContext().getString(R.string.no_internet_connection))
        }
        menuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.ind_pca_profile_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.btn_Logout_Ind_PCA_PROFILE -> {
                        logoutDialog()
                    }

                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)
        return binding.root
    }

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

    fun getProfileData(profileModel:IndPCAProfileModel){
        _ProfileData = profileModel
        try {
            var IMG_URL = RetrofitHelper.IMG_BASE_URL + profileModel.ProfilePic
            Log.d(TAG, "bindPCAData: IMG_URL : $IMG_URL")
            Glide.with(requireContext())
                .load(IMG_URL)
                .error(requireContext().getDrawable(R.drawable.baseline_person_24))
                .placeholder(requireContext().getDrawable(R.drawable.baseline_person_24))
                .into(binding.PCAProfileImgIndPCAProfileFragment)

            binding.tvPCANameIndPCAProfileFragment.setText(profileModel.PCAName)
            binding.tvPCAEmailIndPCAProfileFragment.setText(profileModel.EmailId)
            binding.tvPCAPhoneNoIndPCAProfileFragment.setText(profileModel.PCAPhoneNumber)
            binding.tvPCAAPMCIndPCAProfileFragment.setText(profileModel.APMCName)
            binding.tvPCAAadharCardIndPCAProfileFragment.setText(profileModel.AdharNo)
            binding.tvPCAPANCardIndPCAProfileFragment.setText(profileModel.PanCardNo)
            binding.tvPCAGSTNoIndPCAProfileFragment.setText(profileModel.GSTNo)
            binding.tvPCAOfficeAddressIndPCAProfileFragment.setText(profileModel.Address)
            binding.tvPCACityIndPCAProfileFragment.setText(profileModel.CityName)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindBuyerData: ${e.message}")
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.e(TAG, "onDestroyView: ON_DESTROYED_VIEW")
    }
}