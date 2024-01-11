package com.bluebellcspl.maarevacommoditytradingapp

import ConnectionCheck
import android.app.ActivityOptions
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.transition.Explode
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.constants.Constants
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.databinding.ActivityLoginBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.LoginCheckAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.LoginWithOTPAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.LoginForAdminModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import java.util.Locale

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private val commonUIUtility by lazy { CommonUIUtility(this) }
    var isInitial = true
    val TAG = "LoginActivity"
    lateinit var commodityList: ArrayList<String>
    lateinit var apmcList: ArrayList<String>
    lateinit var APMCId: String
    var TOKEN_ID=""
    override fun onCreate(savedInstanceState: Bundle?) {
        PrefUtil.getInstance(this)
        val languageCode = PrefUtil.getString(PrefUtil.KEY_LANGUAGE, "en")
        val activityConf = Configuration()
        val newLocale = Locale(languageCode)
        activityConf.setLocale(newLocale)
        baseContext.resources.updateConfiguration(
            activityConf,
            baseContext.resources.displayMetrics
        )
        super.onCreate(savedInstanceState)
        with(window) {
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            enterTransition = Explode()
            exitTransition = Explode()
            enterTransition.duration = 1000
            exitTransition.duration = 1000
        }
        binding = DataBindingUtil.setContentView(this@LoginActivity, R.layout.activity_login)
        getToken()
        DatabaseManager.initializeInstance(this)
        setLanguage()
        binding.tvVersionLogin.setText(Constants.version)
        binding.mchbAdminLoginLogin.isChecked = false

        binding.mchbAdminLoginLogin.addOnCheckedStateChangedListener { checkBox, state ->
            if (checkBox.isChecked)
            {
                binding.llAdmin.visibility = View.VISIBLE
                binding.llBuyerOrPCA.visibility = View.GONE
                binding.btnRegisterLogin.visibility = View.GONE
            }else
            {
                binding.llAdmin.visibility = View.GONE
                binding.llBuyerOrPCA.visibility = View.VISIBLE
                binding.btnRegisterLogin.visibility = View.VISIBLE
            }
        }
        setOnClickListeners()

    }

    private fun setOnClickListeners() {
        try {
            binding.btnGetOTPLogin.setOnClickListener {
                if (binding.edtPhoneNoLogin.text.toString().isEmpty()) {
                    commonUIUtility.showToast(getString(R.string.please_enter_phone_no))
                } else if (binding.edtPhoneNoLogin.text.toString().length < 10) {
                    commonUIUtility.showToast("Enter Valid Phone No.!")
                } else {
                    LoginWithOTPAPI(this,this@LoginActivity,binding.edtPhoneNoLogin.text.toString().trim())
                }
            }

            binding.btnVerifyOTPLogin.setOnClickListener {
                if (ConnectionCheck.isConnected(this@LoginActivity))
                {
                    if (binding.edtOTPLogin.text.toString().isEmpty()) {
                        commonUIUtility.showToast("Please Enter OTP!")
                    } else {
                        val model = LoginForAdminModel(
                            "MAT189",
                            binding.edtPhoneNoLogin.text.toString().trim(),
                            binding.edtOTPLogin.text.toString().trim(),
                            "",
                            "",
                            TOKEN_ID
                        )
                        LoginCheckAPI(this, this@LoginActivity, model)
                    }
                }else
                {
                    commonUIUtility.showToast("No Internet Connection!")
                }
            }

            binding.btnRegisterLogin.setOnClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "setOnClickListeners: ${e.message}")
        }
    }

    private fun setLanguage() {
        var language: String = PrefUtil.getString(PrefUtil.KEY_LANGUAGE, "").toString()
//    var language:String = prefUtil.getString(prefUtil.Key, "en")
        val users = arrayOf("ENGLISH", "ગુજરાતી")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, users)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnLanguage.setAdapter(adapter)
        binding.spnLanguage.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                l: Long
            ) {
                if (users[position].equals("ENGLISH", ignoreCase = true)) {
                    language = "en"
                } else if (users[position].equals("ગુજરાતી", ignoreCase = true)) {
                    language = "gu"
                }
                setLocale(language)
                PrefUtil.setString(PrefUtil.KEY_LANGUAGE, language)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        })
        when (language) {
            "en" -> binding.spnLanguage.setSelection(0)
            "gu" -> binding.spnLanguage.setSelection(1)
        }
    }

    fun setLocale(languageCode: String?) {
        val activityConf = Configuration()
        val newLocale = Locale(languageCode)
        activityConf.setLocale(newLocale)
        getBaseContext().getResources()
            .updateConfiguration(activityConf, getBaseContext().getResources().getDisplayMetrics())
        if (isInitial) {
            isInitial = false
        } else {
            finish()
            startActivity(getIntent())
        }
    }

    fun showLoginComponentRoleWise(roleName: String) {
        try {
            if (roleName.equals("Buyer", true) || roleName.equals("PCA", true)) {
                binding.llBuyerOrPCA.visibility = View.VISIBLE
                binding.llAdmin.visibility = View.GONE
            } else {
                binding.llBuyerOrPCA.visibility = View.GONE
                binding.llAdmin.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "showLoginComponent: ${e.message}")
        }
    }

    fun redirectToHome() {
        startActivity(
            Intent(this@LoginActivity, HomeActivity::class.java),
            ActivityOptions.makeSceneTransitionAnimation(this@LoginActivity)
                .toBundle()
        )
        finish()
    }

    fun clearAllData() {
        binding.edtOTPLogin.setText("")
        binding.edtPhoneNoLogin.setText("")
        binding.edtPasswordLogin.setText("")
        binding.edtUsernameLogin.setText("")
        binding.mchbRememberLogin.isChecked = false
    }

    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener<String> { task ->
            if (!task.isSuccessful) {
                Log.w("???", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            TOKEN_ID = task.result
            Log.d("???", "DEVICE TOKEN ID : $TOKEN_ID")
        })
    }
}