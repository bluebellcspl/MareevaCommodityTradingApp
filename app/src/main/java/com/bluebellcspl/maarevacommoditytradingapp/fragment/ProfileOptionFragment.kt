package com.bluebellcspl.maarevacommoditytradingapp.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.LoginActivity
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.constants.Constants
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentProfileOptionBinding

class ProfileOptionFragment : Fragment() {
    lateinit var binding:FragmentProfileOptionBinding
    private val navController by lazy { findNavController() }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_profile_option, container, false)
        binding.tvVersionProfileOptionFragment.setText("v${Constants.version}")
        binding.cvMyProfileProfileOptionFragment.setOnClickListener {
            navController.navigate(ProfileOptionFragmentDirections.actionProfileOptionFragmentToProfileFragment(PrefUtil.getString(PrefUtil.KEY_ROLE_NAME,"").toString()))
        }
        binding.cvBuyerAgreementProfileOptionFragment.setOnClickListener {
            navController.navigate(ProfileOptionFragmentDirections.actionProfileOptionFragmentToAgreementFragment())
        }

        binding.cvBuyerTermProfileOptionFragment.setOnClickListener {
            navController.navigate(ProfileOptionFragmentDirections.actionProfileOptionFragmentToTermsNConditionFragment())
        }

        binding.cvBuyerLogoutProfileOptionFragment.setOnClickListener {
            logoutDialog()
        }
        return binding.root
    }

    fun logoutDialog() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setCancelable(false)
        alertDialog.setTitle("Logout")
        alertDialog.setMessage("Do you want to Logout?")
        alertDialog.setPositiveButton("Yes", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                p0!!.dismiss()
                PrefUtil.deletePreference()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finish()
            }
        })
        alertDialog.setNegativeButton("No", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                p0!!.dismiss()
            }
        })
        alertDialog.show()
    }
}