package com.bluebellcspl.maarevacommoditytradingapp

import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.master.LoginForAdminAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.LoginWithOTPAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.LoginForAdminModel
import com.bluebellcspl.maarevacommoditytradingapp.model.LoginWithOTPModel
import com.example.maarevacommoditytradingapp.R
import com.example.maarevacommoditytradingapp.databinding.ActivityRegisterBinding
import java.util.Locale

class RegisterActivity : AppCompatActivity() {
    lateinit var binding: ActivityRegisterBinding
    private val commonUIUtility by lazy { CommonUIUtility(this) }
    var isInitial = true
    val TAG = "RegisterActivity"
    lateinit var commodityList: ArrayList<String>
    lateinit var apmcList: ArrayList<String>
    lateinit var APMCId: String
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
        binding = DataBindingUtil.setContentView(this@RegisterActivity,R.layout.activity_register)
        DatabaseManager.initializeInstance(this)
        setLanguage()

        apmcList = bindAPMCDropDown()


        //TextWatchers
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
                    commodityList = bindCommodityDropDown(apmcId)
                    binding.actCommodityRegister.setText("")
                    binding.actStateRegister.setText("")
                    binding.actDistrictRegister.setText("")
                } else {
                    APMCId = ""
                    binding.actStateRegister.setText("")
                    binding.actDistrictRegister.setText("")
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
                            binding.actCommodityRegister.text.toString().trim(),
                            APMCId
                        )
                    )!!

                    val stateName = DatabaseManager.ExecuteScalar(Query.getStateNameByCommodityId(commodityId))!!
                    val districtName = DatabaseManager.ExecuteScalar(Query.getDistrictNameByCommodityId(commodityId))!!

                    binding.actStateRegister.setText(stateName)
                    binding.actDistrictRegister.setText(districtName)
                }else
                {
                    binding.actStateRegister.setText("")
                    binding.actDistrictRegister.setText("")
                }
            }
        }

        binding.actAPMCRegister.addTextChangedListener(apmcTextWatcher)
        binding.actCommodityRegister.addTextChangedListener(commodityTextWatcher)
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
                binding.actAPMCRegister.setAdapter(apmcAdapter)
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
                binding.actCommodityRegister.setAdapter(commodityAdapter)
                cursor.close()
            } else {
                dataList.clear()
                val commodityAdapter = commonUIUtility.getCustomArrayAdapter(dataList)
                binding.actCommodityRegister.setAdapter(commodityAdapter)
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

}