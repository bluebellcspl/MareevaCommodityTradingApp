package com.bluebellcspl.maarevacommoditytradingapp

import ConnectionCheck
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.Html
import android.transition.Explode
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.constants.Constants
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.databinding.ActivityLoginBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.TermsAndConditionDialogBinding
import com.bluebellcspl.maarevacommoditytradingapp.fragment.DashboardFragment
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchAPMCMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchCommodityMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchShopMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchTransportationMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.LoginCheckAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.LoginWithOTPAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.LogoutAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.POSTChangeAgreementStatus
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
    var isAgreed = false
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
        if (ConnectionCheck.isConnected(this))
        {
            FetchAPMCMasterAPI(this,this@LoginActivity)
            FetchTransportationMasterAPI(this,this@LoginActivity)
            FetchCommodityMasterAPI(this,this@LoginActivity)
            FetchShopMasterAPI(this,this@LoginActivity)

            val isLoggedIn = PrefUtil.getBoolean(PrefUtil.KEY_LOGGEDIN,false)
            val hasLoggedInPreviously = PrefUtil.getBoolean(PrefUtil.KEY_HAS_LOGGEDIN_PREVIOUSLY,false)
            if (!isLoggedIn)
            {
                LogoutAPI(this, this@LoginActivity,DashboardFragment())
            }
        }
        getToken()
        DatabaseManager.initializeInstance(this)
        setLanguage()
        binding.tvVersionLogin.setText(Constants.version)
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

    fun showPCATermsAndConditionDialog() {
        try {
            val alertDailogBuilder = AlertDialog.Builder(this)
            val dialogBinding = TermsAndConditionDialogBinding.inflate(layoutInflater)
            val dialogView = dialogBinding.root
            alertDailogBuilder.setView(dialogView)
            val alertDialog = alertDailogBuilder.create()
            alertDialog.setCanceledOnTouchOutside(true)
            alertDialog.setCancelable(false)
            alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()

            dialogBinding.tvTermsAndConditionsTNCDialog.setText(
                Html.fromHtml(" <h5>1.\tIntroduction</h5>\n" +
                    "    <p>Welcome to MaaReva, an online platform designed to facilitate agricultural produce trading and connect buyers with Pakka Commission Agents (PCAs) and Agricultural Produce Market Committees (APMCs). By accessing and using the MaaReva Platform, you agree to comply with the following terms and conditions. Please read them carefully before using our services.</p>\n" +
                    "\n" +
                    "    <h5>2.\tRegistration</h5>\n" +
                    "    <p>\n" +
                    "        1.\tTo use our platform, you must complete the registration process. You agree to provide accurate, current, and complete information during registration.\n" +
                    "    </p>\n" +
                    "    <p>\n" +
                    "        2.\tYou are responsible for maintaining the confidentiality of your account information, including your login credentials. If you suspect or  become aware of any unauthorized access to your account or any other breach of security, it is your responsibility to immediately notify us\n" +
                    "    </p>\n" +
                    "    <h5>3.\tBuyer's Responsibilities</h5>\n" +
                    "    <p>\n" +
                    "        1.\tAs a buyer, you agree to use the MaaReva Platform for lawful purposes and in compliance with all applicable laws and regulations.\n" +
                    "    </p><p>\n" +
                    "        2.\tYou are solely responsible for your purchase decisions, including the selection of PCAs and APMCs, price negotiations, and the quantity of agricultural produce you wish to buy. MaaReva is not responsible for any decisions you make regarding purchase transactions. MaaReva is solely providing the trading platform for your use.\n" +
                    "    </p><p>\n" +
                    "        3.\tYou agree to adhere to the terms and conditions set by the PCAs and APMCs with whom you engage in transactions through the platform.\n" +
                    "    </p>\n" +
                    "\n" +
                    "    <h5>4.\tPurchase Requirements</h5>\n" +
                    "    <p>\n" +
                    "        1.\tWhen posting purchase requirements on  the  platform,  you  must accurately specify the quantity and price of agricultural produce you wish to buy.\n" +
                    "    </p><p>\n" +
                    "        2.\tYou may distribute your purchase requirements among associated PCAs as desired.\n" +
                    "    </p><p>\n" +
                    "        3.\tThe platform provides information on last traded prices at APMCs and NCDEX to assist you in making informed decisions about price ranges.\n" +
                    "    </p><p>\n" +
                    "        4.\tYou can adjust price ranges and quantities as needed to align with your requirements.\n" +
                    "    </p>\n" +
                    "    <h5>5.\tPCA Association</h5>\n" +
                    "    <p>\n" +
                    "        1.\tYou may associate with PCAs registered on the MaaReva Platform to facilitate your transactions.\n" +
                    "    </p>\n" +
                    "    <p>\n" +
                    "        2.\tYou have the flexibility to add or remove PCAs from your network as subject to approval of MaaReva.\n" +
                    "    </p>\n" +
                    "    <p>\n" +
                    "        3.\tYou are responsible for verifying the credentials and reliability of the PCAs you associate with.\n" +
                    "    </p>\n" +
                    "    <h5>6.\tPayment and Transactions</h5>\n" +
                    "    <p>\n" +
                    "        1.\tAll payments and financial transactions are not been conducted on the platform. Financial transaction should be made as per compliance with the applicable laws and regulations as your terms.\n" +
                    "    </p><p>\n" +
                    "        2.\tYou agree to make payments to PCAs promptly and in accordance with the agreed terms.\n" +
                    "    </p><p>\n" +
                    "        3.\tThere are no legal transactions taking place on the MaaReva Platform. MaaReva only provides a service that facilitates the buying and selling process, making it faster and ensuring transparency.\n" +
                    "    </p><p>\n" +
                    "        4.\tAny issues related to the use of the MaaReva Platform for buying and selling will be resolved through our legal department only, which will provide necessary advice and guidance.\n" +
                    "    </p>\n" +
                    "    <h5>7.\tReports and Analytics</h5>\n" +
                    "    <p>\n" +
                    "        1.\tThe platform provides various report generation options to help you analyze your trading activities.\n" +
                    "    </p><p>\n" +
                    "        2.\tYou may use these reports to gain insights into your transactions, PCA performance, and trading history only.\n" +
                    "    </p>\n" +
                    "    <h5> 8.\tPrivacy and Data Security</h5>\n" +
                    "    <p>\n" +
                    "        1.\tWe are committed to safeguarding your data and privacy. Please review our Privacy Policy to understand how we collect, use, and protect your information.\n" +
                    "    </p><p>\n" +
                    "        2.\tIf any GST department or other government department requests transaction-related information from MaaReva, MaaReva will be obligated to provide them with the requested details.\n" +
                    "    </p>\n" +
                    "\n" +
                    "    <h5>9.\tTermination</h5>\n" +
                    "    <p>\n" +
                    "        1.\tWe reserve the right to terminate your account and access to the platform at our discretion, with or without cause.\n" +
                    "    </p><p>\n" +
                    "        2.\tYou may terminate your account at any time by notifying us in writing or through the platform's designated process.\n" +
                    "    </p>\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "    <h5>10.\tAmendments to Terms and Conditions</h5>\n" +
                    "    <p>  1.\tWe may update and modify these terms and conditions at any time. You will be notified of any changes, and continued use of the platform constitutes your acceptance of the revised terms.</p>\n" +
                    "\n" +
                    "\n" +
                    "    <h5>\n" +
                    "        11.\tContact Information\n" +
                    "    </h5>\n" +
                    "    <p>1.\tFor any questions, concerns, or inquiries, please contact us at [Contact Us Page].</p>\n" +
                    "\n" +
                    "\n" +
                    "    <h5>\n" +
                    "        12.\tGoverning Law\n" +
                    "    </h5>\n" +
                    "    <p>\n" +
                    "        1.\tThese terms and conditions are governed by and construed in accordance with the laws of Agriculture produces trade law.\n" +
                    "    </p><p>\n" +
                    "        2.\tEach buyer is responsible for adhering to all relevant state government regulations and laws that apply to their specific transactions.\n" +
                    "    </p><p>\n" +
                    "        By using the MaaReva Platform, you acknowledge that you have read, understood, and agreed to these terms and conditions. Failure to comply with these terms may result in the termination of your access to the platform.\n" +
                    "    </p>"))

            dialogBinding.btnAgreeTNCDialog.setOnClickListener {
                isAgreed = true
                POSTChangeAgreementStatus(this,this@LoginActivity,isAgreed.toString())
                alertDialog.dismiss()
            }
        } catch (e: Exception) {
            Log.e(TAG, "showTaskAllocationDialog: ${e.message}")
            e.printStackTrace()
        }
    }
}