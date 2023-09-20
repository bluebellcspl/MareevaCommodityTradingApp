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
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.databinding.ActivityRegisterBinding
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class RegisterActivity : AppCompatActivity() {
    lateinit var binding: ActivityRegisterBinding
    private val commonUIUtility by lazy { CommonUIUtility(this) }
    var isInitial = true
    val TAG = "RegisterActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
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
        binding = DataBindingUtil.setContentView(this@RegisterActivity, R.layout.activity_register)
        DatabaseManager.initializeInstance(this)
        setLanguage()


        //TextWatchers
//        val commodityTextWatcher: TextWatcher = object : TextWatcher {
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//
//            }
//
//            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//
//            }
//
//            override fun afterTextChanged(p0: Editable?) {
//                if (p0.toString().isNotEmpty()) {
//                    val commodityId = DatabaseManager.ExecuteScalar(
//                        Query.getCommodityIdByCommodityNameANDAPMCId(
//                            binding.actCommodityRegister.text.toString().trim(),
//                            APMCId
//                        )
//                    )!!
//
//                    val stateName =
//                        DatabaseManager.ExecuteScalar(Query.getStateNameByCommodityId(commodityId))!!
//                    val districtName =
//                        DatabaseManager.ExecuteScalar(Query.getDistrictNameByCommodityId(commodityId))!!
//
//                    binding.actStateRegister.setText(stateName)
//                    binding.actDistrictRegister.setText(districtName)
//                } else {
//                    binding.actStateRegister.setText("")
//                    binding.actDistrictRegister.setText("")
//                }
//            }
//        }
//
//        binding.actAPMCRegister.addTextChangedListener(apmcTextWatcher)
//        binding.actCommodityRegister.addTextChangedListener(commodityTextWatcher)
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

            binding.btnRegisterRegister.setOnClickListener {
                if (binding.edtPhoneNoRegister.text.toString().isEmpty()) {
                    commonUIUtility.showToast(getString(R.string.please_enter_phone_no))
                } else if (binding.edtFullNameRegister.text.toString().isEmpty()) {
                    commonUIUtility.showToast(getString(R.string.please_enter_full_name))
                } else if (binding.edtLocationRegister.text.toString().isEmpty()) {
                    commonUIUtility.showToast(getString(R.string.please_enter_location_alert_msg))
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

    fun redirectToLogin(){
        startActivity(
            Intent(this@RegisterActivity,LoginActivity::class.java),
            ActivityOptions.makeSceneTransitionAnimation(this@RegisterActivity)
                .toBundle()
        )
        finish()
    }

}