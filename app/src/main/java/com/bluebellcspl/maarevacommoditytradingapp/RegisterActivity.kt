package com.bluebellcspl.maarevacommoditytradingapp

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
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.master.RegisterBuyerAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.RegisterBuyerModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.OurRetrofit
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.bluebellcspl.maarevacommoditytradingapp.databinding.ActivityRegisterBinding
import com.bluebellcspl.maarevacommoditytradingapp.databinding.TermsAndConditionDialogBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.RegisterIndividualPCAAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.IndividualPCARegisterModel
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class RegisterActivity : AppCompatActivity() {
    var _binding: ActivityRegisterBinding?=null
    val binding get() = _binding!!
    private val commonUIUtility by lazy { CommonUIUtility(this) }
    var isInitial = true
    val TAG = "RegisterActivity"
    lateinit var alertDialog: AlertDialog
    var isAgreed:Boolean = false
    var selected_APMC_ID = ""
    lateinit var _APMCList : ArrayList<APMCDetail>
    override fun onCreate(savedInstanceState: Bundle?) {
        val languageCode = PrefUtil.getSystemLanguage()
        val activityConf = Configuration()
        val newLocale = Locale(languageCode)
        activityConf.setLocale(newLocale)
        baseContext.resources.updateConfiguration(
            activityConf,
            baseContext.resources.displayMetrics
        )
        alertDialog = AlertDialog.Builder(this).create()
        super.onCreate(savedInstanceState)
        with(window) {
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            enterTransition = Explode()
            exitTransition = Explode()
            enterTransition.duration = 1000
            exitTransition.duration = 1000
        }
        _binding = DataBindingUtil.setContentView(this@RegisterActivity, R.layout.activity_register)
        DatabaseManager.initializeInstance(this)
        setLanguage()
        binding.mchbTermsConditionsRegisiter.addOnCheckedStateChangedListener { checkBox, state ->
            if (checkBox.isChecked)
            {
                showPCATermsAndConditionDialog()
            }else
            {
                isAgreed = false
            }
        }
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        try {

//            binding.btnGetOTPRegister.setOnClickListener {
//                if (binding.edtPhoneNoRegister.text.toString().isEmpty()) {
//                    commonUIUtility.showToast(getString(R.string.please_enter_phone_no))
//                } else {
//                    requestForOTP()
//                }
//            }
            _APMCList = getAPMCfromDB()
            binding.rdGrpSelectRoleRegister.setOnCheckedChangeListener { radioGroup, checkedId ->
                when(checkedId)
                {
                    R.id.mRdb_Buyer_Register -> {
                        binding.actAPMCRegisterContainer.visibility = View.GONE
                        binding.edtLocationRegisterContainer.visibility = View.VISIBLE
                        selected_APMC_ID = ""
                    }

                    R.id.mRdb_IndividualPCA_Register->{
                        binding.actAPMCRegisterContainer.visibility = View.VISIBLE
                        binding.edtLocationRegisterContainer.visibility = View.GONE
                        binding.edtLocationRegister.setText("")
                    }
                }
            }
            
            binding.actAPMCRegister.setOnItemClickListener { adapterView, view, position, long ->
                val apmcModel = _APMCList.find {it.APMCName.equals(adapterView.getItemAtPosition(position).toString())}!!
                selected_APMC_ID = apmcModel.APMCId
                Log.d(TAG, "setOnClickListeners: SELECTED_APMC_NAME : ${adapterView.getItemAtPosition(position).toString()}")
                Log.d(TAG, "setOnClickListeners: SELECTED_APMC_ID : $selected_APMC_ID")
            }

            binding.btnRegisterRegister.setOnClickListener {
                if (binding.edtPhoneNoRegister.text.toString().isEmpty()) {
                    commonUIUtility.showToast(getString(R.string.please_enter_phone_no))
                } else if (binding.edtFullNameRegister.text.toString().isEmpty()) {
                    commonUIUtility.showToast(getString(R.string.please_enter_full_name))
                }
                else if (binding.edtLocationRegisterContainer.isVisible && binding.edtLocationRegister.text.toString().isEmpty()) {
                    commonUIUtility.showToast(getString(R.string.please_enter_location_alert_msg))
                } else if (binding.actAPMCRegisterContainer.isVisible && binding.actAPMCRegister.text.toString().isEmpty()) {
                    commonUIUtility.showToast(getString(R.string.please_select_apmc_alert_msg))
                }
                else if(binding.rdGrpSelectRoleRegister.checkedRadioButtonId == -1){
                    commonUIUtility.showToast(getString(R.string.please_select_role_alert_msg))
                }
                else if (!isAgreed) {
                    commonUIUtility.showToast(getString(R.string.please_accept_terms_and_conditions))
                } else {
//                    val commodityId = DatabaseManager.ExecuteScalar(
//                        Query.getCommodityIdByCommodityNameANDAPMCId(
//                            binding.actCommodityRegister.text.toString().trim(),
//                            APMCId
//                        )
//                    )!!
//                    val stateId =
//                        DatabaseManager.ExecuteScalar(Query.getStateIdByCommodityId(commodityId))!!
//                    val districtId =
//                        DatabaseManager.ExecuteScalar(Query.getDistrictIdByCommodityId(commodityId))!!
                    if(binding.mRdbBuyerRegister.isChecked){
                        val model = RegisterBuyerModel(
                            "",
                            "",
                            "",
                            DateUtility().getyyyyMMdd(),
                            binding.edtPhoneNoRegister.text.toString().trim(),
                            "",
                            "",
                            binding.edtLocationRegister.text.toString().trim(),
                            binding.edtPhoneNoRegister.text.toString().trim(),
                            binding.edtFullNameRegister.text.toString().trim(),
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                        )

                        RegisterBuyerAPI(this,this@RegisterActivity,model)
                    }
                    else if(binding.mRdbIndividualPCARegister.isChecked){
                        val PCAModel = IndividualPCARegisterModel(
                            "MAT189",
                            DateUtility().getyyyyMMddDateTime(),
                            selected_APMC_ID,
                            binding.edtPhoneNoRegister.text.toString().trim(),
                            binding.edtFullNameRegister.text.toString().trim(),
                            "1",
                            "1",
                            "1",
                            "0",
                            "4",
                        )
                        
                        RegisterIndividualPCAAPI(this,this@RegisterActivity, PCAModel)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "setOnClickListeners: ${e.message}")
        }
    }

    private fun requestForOTP() {
        try {
            commonUIUtility.showProgress()
            val JO = JsonObject()
            JO.addProperty("MobileNo", binding.edtPhoneNoRegister.text.toString().trim())
            JO.addProperty("UserType", "2")

            Log.d(TAG, "requestForOTP: JSON : ${JO.toString()}")
            val APICall = RetrofitHelper.getInstance().create(OurRetrofit::class.java)
            lifecycleScope.launch(Dispatchers.IO) {
                val result = APICall.getOTPForRegister(JO)
                if (result.isSuccessful) {
                    if (result.body()!!.get("Success").asString.equals("true")) {
                        withContext(Dispatchers.Main) {
                            commonUIUtility.dismissProgress()
                            commonUIUtility.showToast(getString(R.string.otp_sent_successfully_alert_msg))
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            commonUIUtility.dismissProgress()
                            commonUIUtility.showToast(getString(R.string.invalid_data_of_user_alert_msg))
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        commonUIUtility.dismissProgress()
                        commonUIUtility.showToast(getString(R.string.error_sending_otp))
                    }
                    Log.e(TAG, "requestForOTP: ${result.errorBody().toString()}")
                }
            }
        } catch (e: Exception) {
            commonUIUtility.dismissProgress()
            commonUIUtility.showToast(getString(R.string.please_try_again_later_alert_msg))
            e.printStackTrace()
            Log.e(TAG, "requestForOTP: ${e.message}")
        }
    }

//    fun bindAPMCDropDown(): ArrayList<String> {
//        val dataList = ArrayList<String>()
//        try {
//            val cursor = DatabaseManager.ExecuteRawSql(Query.getAPMCName())
//            if (cursor != null && cursor.count > 0) {
//                dataList.clear()
//                while (cursor.moveToNext()) {
//                    dataList.add(cursor.getString(cursor.getColumnIndexOrThrow("APMCName")))
//                }
//
//                val apmcAdapter = commonUIUtility.getCustomArrayAdapter(dataList)
//                binding.actAPMCRegister.setAdapter(apmcAdapter)
//                cursor.close()
//            }
//
//        } catch (e: Exception) {
//            dataList.clear()
//            e.printStackTrace()
//            Log.e(TAG, "bindAPMCDropDown: ${e.message}")
//        }
//        return dataList
//    }

//    fun bindCommodityDropDown(apmdId: String): ArrayList<String> {
//        val dataList = ArrayList<String>()
//        try {
//            val cursor = DatabaseManager.ExecuteRawSql(Query.getCommodityNameByAPMCId(apmdId))
//            if (cursor != null && cursor.count > 0) {
//                dataList.clear()
//                while (cursor.moveToNext()) {
//                    dataList.add(cursor.getString(cursor.getColumnIndexOrThrow("CommodityName")))
//                }
//
//                val commodityAdapter = commonUIUtility.getCustomArrayAdapter(dataList)
//                binding.actCommodityRegister.setAdapter(commodityAdapter)
//                cursor.close()
//            } else {
//                dataList.clear()
//                val commodityAdapter = commonUIUtility.getCustomArrayAdapter(dataList)
//                binding.actCommodityRegister.setAdapter(commodityAdapter)
//            }
//
//        } catch (e: Exception) {
//            dataList.clear()
//            e.printStackTrace()
//            Log.e(TAG, "bindCommodityDropDown: ${e.message}")
//        }
//        return dataList
//    }

    private fun setLanguage() {
        var language: String = PrefUtil.getSystemLanguage().toString()
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

    fun redirectToLogin(){
        startActivity(
            Intent(this@RegisterActivity,LoginActivity::class.java),
            ActivityOptions.makeSceneTransitionAnimation(this@RegisterActivity)
                .toBundle()
        )
        finish()
    }

    fun showPCATermsAndConditionDialog() {
        try {
            val alertDailogBuilder = AlertDialog.Builder(this)
            val dialogBinding = TermsAndConditionDialogBinding.inflate(layoutInflater)
            val dialogView = dialogBinding.root
            alertDailogBuilder.setView(dialogView)
            alertDialog = alertDailogBuilder.create()
            alertDialog.setCanceledOnTouchOutside(true)
            alertDialog.setCancelable(false)
            alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()

            dialogBinding.tvTermsAndConditionsTNCDialog.setText(Html.fromHtml(" <h5>1.\tIntroduction</h5>\n" +
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
                    alertDialog.dismiss()
                }
        } catch (e: Exception) {
            Log.e(TAG, "showTaskAllocationDialog: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun getAPMCfromDB():ArrayList<APMCDetail>{
        val localArrayList = ArrayList<APMCDetail>()
        val cursor = DatabaseManager.ExecuteRawSql(Query.getAPMCDetail())
        try {
            if (cursor!=null && cursor.count>0)
            {
                while (cursor.moveToNext())
                {

                    val model = APMCDetail(
                        cursor.getString(cursor.getColumnIndexOrThrow("APMCId")),
                        cursor.getString(cursor.getColumnIndexOrThrow("APMCName"))
                    )
                    localArrayList.add(model)
                }
            }
            val apmcAdapter = commonUIUtility.getCustomArrayAdapter(ArrayList(localArrayList.map { it.APMCName }))
            binding.actAPMCRegister.setAdapter(apmcAdapter)
            localArrayList.forEach {
                Log.d(TAG, "getAPMCfromDB: ID - APMC : ${it.APMCId} - ${it.APMCName}")
            }
            Log.d(TAG, "getAPMCfromDB: AMPCList : $localArrayList")
            cursor?.close()
        }catch (e:Exception)
        {
            cursor?.close()
            localArrayList.clear()
            e.printStackTrace()
            Log.e(TAG, "getAPMCfromDB: ${e.message}")
        }
        return localArrayList
    }

    data class APMCDetail(var APMCId:String,var APMCName:String)

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(
            Intent(this@RegisterActivity,LoginActivity::class.java),
            ActivityOptions.makeSceneTransitionAnimation(this@RegisterActivity)
                .toBundle()
        )
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        alertDialog.dismiss()
    }


}