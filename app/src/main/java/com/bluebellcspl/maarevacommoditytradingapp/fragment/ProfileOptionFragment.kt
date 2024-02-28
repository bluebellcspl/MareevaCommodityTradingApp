package com.bluebellcspl.maarevacommoditytradingapp.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.getIntent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.LoginActivity
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.constants.Constants
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentProfileOptionBinding
import java.util.Locale

class ProfileOptionFragment : Fragment() {
    lateinit var binding:FragmentProfileOptionBinding
    private val navController by lazy { findNavController() }
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    var isInitial = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val languageCode = PrefUtil.getString(PrefUtil.KEY_LANGUAGE, "en")
        val activityConf = Configuration()
        val newLocale = Locale(languageCode)
        activityConf.setLocale(newLocale)
        requireContext().resources.updateConfiguration(
            activityConf,
            requireContext().resources.displayMetrics
        )
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_profile_option, container, false)
        setLanguage()
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
                PrefUtil.setBoolean(PrefUtil.KEY_LOGGEDIN,false)
                requireActivity().startActivity(Intent(activity, LoginActivity::class.java))
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

    private fun setLanguage() {
        var language: String = PrefUtil.getString(PrefUtil.KEY_LANGUAGE, "").toString()
//    var language:String = prefUtil.getString(prefUtil.Key, "en")
        val users = arrayListOf<String>("English", "ગુજરાતી")
        if (language.equals("en"))
        {
            binding.actLanguageProfileOptionFragment.setText(users[0])
        }else
        {
                binding.actLanguageProfileOptionFragment.setText(users[1])
        }
        binding.actLanguageProfileOptionFragment.threshold = 101
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, users)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.actLanguageProfileOptionFragment.setAdapter(commonUIUtility.getCustomArrayAdapter(users))
        binding.actLanguageProfileOptionFragment.setOnItemClickListener { adapterView, view, i, l ->
                var selectedLanguage = ""
            if (users[i].equals("English", ignoreCase = true)) {
                    selectedLanguage = "en"
                } else if (users[i].equals("ગુજરાતી", ignoreCase = true)) {
                    selectedLanguage = "gu"
                }
                isInitial = language.equals(selectedLanguage)
                setLocale(selectedLanguage)
                PrefUtil.setString(PrefUtil.KEY_LANGUAGE, selectedLanguage)
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

}