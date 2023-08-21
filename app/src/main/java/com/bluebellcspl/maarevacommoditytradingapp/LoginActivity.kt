package com.bluebellcspl.maarevacommoditytradingapp

import android.app.ActivityOptions
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchAPMCMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchCommodityMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchDistrictMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchRoleMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchStateMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.LoginCheckAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.LoginWithOTPAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.LoginForAdminModel
import com.bluebellcspl.maarevacommoditytradingapp.model.LoginWithOTPModel
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.databinding.ActivityLoginBinding
import java.util.Locale

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private val commonUIUtility by lazy { CommonUIUtility(this) }
    var isInitial = true
    val TAG = "LoginActivity"
    lateinit var commodityList: ArrayList<String>
    lateinit var apmcList: ArrayList<String>
    lateinit var APMCId: String
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
        DatabaseManager.initializeInstance(this)
        setLanguage()
        FetchRoleMasterAPI(this, this@LoginActivity)
        FetchStateMasterAPI(this, this@LoginActivity)
        FetchDistrictMasterAPI(this, this@LoginActivity)
        FetchCommodityMasterAPI(this, this@LoginActivity)
        FetchAPMCMasterAPI(this, this@LoginActivity)

        apmcList = bindAPMCDropDown()


        //TextWatchers
        val roleTextWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString().isNotEmpty()) {
                    clearAllData()
                    showLoginComponentRoleWise(p0.toString())
                }
            }
        }
        val apmcTextWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString().isNotEmpty()) {
                    APMCId = ""
                    val apmcId = DatabaseManager.ExecuteScalar(
                        Query.getAPMCIdByAPMCName(
                            p0.toString().trim()
                        )
                    )!!
                    APMCId = apmcId
                    Log.d(TAG, "afterTextChanged: APMC_ID : $apmcId")
                    commodityList = bindCommodityDropDown(apmcId)
                    binding.actCommodityLogin.setText("")
                    binding.actStateLogin.setText("")
                    binding.actDistrictLogin.setText("")
                } else {
                    APMCId = ""
                    binding.actStateLogin.setText("")
                    binding.actDistrictLogin.setText("")
                }
            }
        }
        val commodityTextWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString().isNotEmpty()) {
                    val commodityId = DatabaseManager.ExecuteScalar(
                        Query.getCommodityIdByCommodityNameANDAPMCId(
                            binding.actCommodityLogin.text.toString().trim(),
                            APMCId
                        )
                    )!!

                    val stateName = DatabaseManager.ExecuteScalar(Query.getStateNameByCommodityId(commodityId))!!
                    val districtName = DatabaseManager.ExecuteScalar(Query.getDistrictNameByCommodityId(commodityId))!!

                    binding.actStateLogin.setText(stateName)
                    binding.actDistrictLogin.setText(districtName)
                }else
                {
                    binding.actStateLogin.setText("")
                    binding.actDistrictLogin.setText("")
                }
            }
        }

        binding.actRoleLogin.addTextChangedListener(roleTextWatcher)
        binding.actAPMCLogin.addTextChangedListener(apmcTextWatcher)
        binding.actCommodityLogin.addTextChangedListener(commodityTextWatcher)
        setOnClickListeners()

    }

    private fun setOnClickListeners() {
        try {
            binding.btnGetOTPLogin.setOnClickListener {
                if (binding.actRoleLogin.text.toString().isEmpty()) {
                    commonUIUtility.showToast(getString(R.string.please_select_role_alert_msg))
                } else if (binding.actAPMCLogin.text.toString().isEmpty()) {
                    commonUIUtility.showToast(getString(R.string.please_select_apmc_alert_msg))
                } else if (binding.actCommodityLogin.text.toString().isEmpty()) {
                    commonUIUtility.showToast(getString(R.string.please_select_commodity_alert_msg))
                } else if (binding.edtPhoneNoLogin.text.toString().isEmpty()) {
                    commonUIUtility.showToast(getString(R.string.please_enter_phone_no))
                } else if (!apmcList.contains(binding.actAPMCLogin.text.toString().trim())) {
                    Log.d(TAG, "setOnClickListeners: APMC_NAME : $binding.actAPMCLogin.text.toString().trim()")
                    commonUIUtility.showToast(getString(R.string.please_select_valid_apmc_alert_msg))
                } else {
                    val commodityId = DatabaseManager.ExecuteScalar(
                        Query.getCommodityIdByCommodityNameANDAPMCId(
                            binding.actCommodityLogin.text.toString().trim(),
                            APMCId
                        )
                    )!!
                    val stateId =
                        DatabaseManager.ExecuteScalar(Query.getStateIdByCommodityId(commodityId))!!
                    val districtId =
                        DatabaseManager.ExecuteScalar(Query.getDistrictIdByCommodityId(commodityId))!!



                    val model = LoginWithOTPModel(
                        APMCId,
                        commodityId,
                        districtId,
                        binding.edtPhoneNoLogin.text.toString().trim(),
                        stateId,
                        getUserType()
                    )

                    LoginWithOTPAPI(this, this@LoginActivity, model)

                }
            }

            binding.btnVerifyOTPLogin.setOnClickListener {
                if (binding.llAdmin.isVisible) {
                    if (binding.edtUsernameLogin.text.toString().isEmpty()) {
                        commonUIUtility.showToast(getString(R.string.please_enter_username_alert_msg))
                    } else if (binding.edtPasswordLogin.text.toString().isEmpty()) {
                        commonUIUtility.showToast(getString(R.string.please_enter_password_alert_msg))
                    } else {
                        val model = LoginForAdminModel(
                            "",
                            "Admin",
                            "",
                            "MAT189",
                            "",
                            "",
                            "",
                            "Admin",
                            "",
                            "1",
                            binding.edtUsernameLogin.text.toString().trim(),
                            binding.edtPasswordLogin.text.toString().trim()
                        )

                        LoginCheckAPI(this, this@LoginActivity, model)

                    }
                } else {
                    if(binding.edtOTPLogin.text.toString().isEmpty())
                    {
                        commonUIUtility.showToast("Please Enter OTP!")
                    }else
                    {
                        val commodityId = DatabaseManager.ExecuteScalar(
                            Query.getCommodityIdByCommodityNameANDAPMCId(
                                binding.actCommodityLogin.text.toString().trim(),
                                APMCId
                            )
                        )!!
                        val stateId =
                            DatabaseManager.ExecuteScalar(Query.getStateIdByCommodityId(commodityId))!!
                        val districtId =
                            DatabaseManager.ExecuteScalar(Query.getDistrictIdByCommodityId(commodityId))!!


                        val model = LoginForAdminModel(
                            APMCId,
                            binding.actRoleLogin.text.toString().trim(),
                            commodityId,
                            "MAT189",
                            districtId,
                            binding.edtPhoneNoLogin.text.toString().trim(),
                            binding.edtOTPLogin.text.toString().trim(),
                            "",
                            stateId,
                            getUserType(),
                            "",
                            ""
                        )

                        LoginCheckAPI(this, this@LoginActivity, model)
                    }
                }
            }

            binding.btnRegisterLogin.setOnClickListener {
                startActivity(Intent(this@LoginActivity,RegisterActivity::class.java))
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "setOnClickListeners: ${e.message}")
        }
    }

    fun bindRoleDropDown(): ArrayList<String> {
        val dataList = ArrayList<String>()
        try {

            val cursor = DatabaseManager.ExecuteRawSql(Query.getRoleName())
            if (cursor != null && cursor.count > 0) {
                dataList.clear()
                while (cursor.moveToNext()) {
                    dataList.add(cursor.getString(cursor.getColumnIndexOrThrow("RoleName")))
                }

                val roleAdapter = commonUIUtility.getCustomArrayAdapter(dataList)
                binding.actRoleLogin.setAdapter(roleAdapter)
                cursor.close()
            }

        } catch (e: Exception) {
            dataList.clear()
            e.printStackTrace()
            Log.e(TAG, "bindRoleDropDown: ${e.message}")
        }

        return dataList
    }

    fun bindAPMCDropDown(): ArrayList<String> {
        val dataList = ArrayList<String>()
        try {
            val cursor = DatabaseManager.ExecuteRawSql(Query.getAPMCName())
            if (cursor != null && cursor.count > 0) {
                dataList.clear()
                while (cursor.moveToNext()) {
                    dataList.add(cursor.getString(cursor.getColumnIndexOrThrow("APMCName")))
                }

                val apmcAdapter = commonUIUtility.getCustomArrayAdapter(dataList)
                binding.actAPMCLogin.setAdapter(apmcAdapter)
                cursor.close()
            }

        } catch (e: Exception) {
            dataList.clear()
            e.printStackTrace()
            Log.e(TAG, "bindAPMCDropDown: ${e.message}")
        }
        return dataList
    }

    fun bindCommodityDropDown(apmdId: String): ArrayList<String> {
        val dataList = ArrayList<String>()
        try {
            val cursor = DatabaseManager.ExecuteRawSql(Query.getCommodityNameByAPMCId(apmdId))
            if (cursor != null && cursor.count > 0) {
                dataList.clear()
                while (cursor.moveToNext()) {
                    dataList.add(cursor.getString(cursor.getColumnIndexOrThrow("CommodityName")))
                }

                val commodityAdapter = commonUIUtility.getCustomArrayAdapter(dataList)
                binding.actCommodityLogin.setAdapter(commodityAdapter)
                cursor.close()
            } else {
                dataList.clear()
                val commodityAdapter = commonUIUtility.getCustomArrayAdapter(dataList)
                binding.actCommodityLogin.setAdapter(commodityAdapter)
            }

        } catch (e: Exception) {
            dataList.clear()
            e.printStackTrace()
            Log.e(TAG, "bindCommodityDropDown: ${e.message}")
        }
        return dataList
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

    fun getUserType():String{
        var userType = ""
        if (binding.actRoleLogin.text.toString().equals("Buyer", true)) {
            userType = "2"
        } else if (binding.actRoleLogin.text.toString().equals("PCA", true)) {
            userType = "3"
        }
        else if (binding.actRoleLogin.text.toString().equals("Admin", true)) {
            userType = "1"
        }
        PrefUtil.setString(PrefUtil.KEY_TYPE_OF_USER,userType)
        return userType
    }

    fun redirectToHome(){
        startActivity(
            Intent(this@LoginActivity,HomeActivity::class.java),
            ActivityOptions.makeSceneTransitionAnimation(this@LoginActivity)
                .toBundle()
        )
        finish()
    }

    fun clearAllData(){
        binding.actStateLogin.setText("")
        binding.actDistrictLogin.setText("")
        binding.actCommodityLogin.setText("")
        binding.actAPMCLogin.setText("")
        binding.edtOTPLogin.setText("")
        binding.edtPhoneNoLogin.setText("")
        binding.edtPasswordLogin.setText("")
        binding.edtUsernameLogin.setText("")
        binding.mchbRememberLogin.isChecked = false
    }
}