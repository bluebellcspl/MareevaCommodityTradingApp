package com.bluebellcspl.maarevacommoditytradingapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchAPMCMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchCommodityMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchDistrictMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchRoleMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchStateMasterAPI
import com.example.maarevacommoditytradingapp.R
import com.example.maarevacommoditytradingapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    lateinit var binding:ActivityLoginBinding
    private val commonUIUtility by lazy { CommonUIUtility(this) }
    val TAG = "LoginActivity"
    lateinit var commodityList :ArrayList<String>
    lateinit var apmcList :ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@LoginActivity,R.layout.activity_login)
        DatabaseManager.initializeInstance(this)
        FetchRoleMasterAPI(this,this@LoginActivity)
        FetchStateMasterAPI(this,this@LoginActivity)
        FetchDistrictMasterAPI(this,this@LoginActivity)
        FetchCommodityMasterAPI(this,this@LoginActivity)
        FetchAPMCMasterAPI(this,this@LoginActivity)

        apmcList = bindAPMCDropDown()
        commodityList = bindCommodityDropDown()

    }

     fun bindRoleDropDown():ArrayList<String> {
         val dataList = ArrayList<String>()
        try {

            val cursor = DatabaseManager.ExecuteRawSql(Query.getRoleName())
            if (cursor!=null && cursor.count>0){
                dataList.clear()
                while (cursor.moveToNext())
                {
                    dataList.add(cursor.getString(cursor.getColumnIndexOrThrow("RoleName")))
                }

                val roleAdapter = commonUIUtility.getCustomArrayAdapter(dataList)
                binding.actRoleLogin.setAdapter(roleAdapter)
                cursor.close()
            }

        }catch (e:Exception)
        {
            dataList.clear()
            e.printStackTrace()
            Log.e(TAG, "bindRoleDropDown: ${e.message}")
        }

         return dataList
    }

     fun bindAPMCDropDown():ArrayList<String> {
         val dataList = ArrayList<String>()
         try {
            val cursor = DatabaseManager.ExecuteRawSql(Query.getAPMCName())
            if (cursor!=null && cursor.count>0){
                dataList.clear()
                while (cursor.moveToNext())
                {
                    dataList.add(cursor.getString(cursor.getColumnIndexOrThrow("APMCName")))
                }

                val apmcAdapter = commonUIUtility.getCustomArrayAdapter(dataList)
                binding.actAPMCLogin.setAdapter(apmcAdapter)
                cursor.close()
            }

        }catch (e:Exception)
        {
            dataList.clear()
            e.printStackTrace()
            Log.e(TAG, "bindAPMCDropDown: ${e.message}")
        }
         return dataList
    }

    fun bindCommodityDropDown():ArrayList<String> {
        val dataList = ArrayList<String>()
        try {
            val cursor = DatabaseManager.ExecuteRawSql(Query.getCommodityName())
            if (cursor!=null && cursor.count>0){
                dataList.clear()
                while (cursor.moveToNext())
                {
                    dataList.add(cursor.getString(cursor.getColumnIndexOrThrow("CommodityName")))
                }

                val commodityAdapter = commonUIUtility.getCustomArrayAdapter(dataList)
                binding.actCommodityLogin.setAdapter(commodityAdapter)
                cursor.close()
            }

        }catch (e:Exception)
        {
            dataList.clear()
            e.printStackTrace()
            Log.e(TAG, "bindCommodityDropDown: ${e.message}")
        }
        return dataList
    }
}