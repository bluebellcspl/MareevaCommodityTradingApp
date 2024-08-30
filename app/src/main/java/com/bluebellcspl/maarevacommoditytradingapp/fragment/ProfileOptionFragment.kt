package com.bluebellcspl.maarevacommoditytradingapp.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.getIntent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
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
    var _binding:FragmentProfileOptionBinding? = null
    val binding get() = _binding!!
    private val navController by lazy { findNavController() }
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    var isInitial = true
    val TAG = "ProfileOptionFragment"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        val languageCode = PrefUtil.getString(PrefUtil.KEY_LANGUAGE, "en")
//        val activityConf = Configuration()
//        val newLocale = Locale(languageCode)
//        activityConf.setLocale(newLocale)
//        requireContext().resources.updateConfiguration(
//            activityConf,
//            requireContext().resources.displayMetrics
//        )
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_profile_option, container, false)
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

        binding.btnSendProfileOptionFragment.setOnClickListener {
            if (binding.edtQueryProfileOptionFragment.text.toString().isNotEmpty()){
                sendEmailForQuery()
            }else
            {
                commonUIUtility.showToast(getString(R.string.please_enter_your_query_alert_msg))
            }
        }
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

    private fun setLanguage() {
        var language: String = PrefUtil.getSystemLanguage().toString()
//    var language:String = prefUtil.getString(prefUtil.Key, "en")
        val users = arrayListOf<String>("English", "ગુજરાતી")
        if (language.equals("en"))
        {
            binding.actLanguageProfileOptionFragment.setText(users[0])
        }else if (language.equals("gu"))
        {
                binding.actLanguageProfileOptionFragment.setText(users[1])
        }else
        {
            binding.actLanguageProfileOptionFragment.setText("English")
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
            PrefUtil.setSystemLanguage(selectedLanguage)
            Log.d(TAG, "setLanguage: SYSTEM_LANGUAGE : ${PrefUtil.getSystemLanguage()}")
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
    
    private fun sendEmailForQuery(){
        try {
            var subjectStringBuilder = StringBuilder()
            var roleName = PrefUtil.getString(PrefUtil.KEY_ROLE_NAME,"")!!
            var mobileNo = PrefUtil.getString(PrefUtil.KEY_MOBILE_NO,"")!!
            var name = PrefUtil.getString(PrefUtil.KEY_NAME,"")!!
            subjectStringBuilder.append("Inquiry Request From ")
            subjectStringBuilder.append("$roleName - $name - $mobileNo")
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822" // Use this MIME type to indicate email
                putExtra(Intent.EXTRA_EMAIL, arrayOf("hello@maareva.com")) // recipient(s)
                putExtra(Intent.EXTRA_SUBJECT, subjectStringBuilder.toString()) // subject
                putExtra(Intent.EXTRA_TEXT, binding.edtQueryProfileOptionFragment.text.toString().trim()) // body
            }
            intent.setPackage("com.google.android.gm") // Explicitly set Gmail package
            requireActivity().startActivity(intent)
//            if (intent.resolveActivity(requireContext().packageManager) != null) {
//            } else {
//                println("Gmail is not installed.")
//            }
        }catch (e:Exception)
        {
            Log.e(TAG, "sendEmailForQuery: ${e.message}", )
            e.printStackTrace()
        }
    } 

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}