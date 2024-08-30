package com.bluebellcspl.maarevacommoditytradingapp.commonFunction

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class PrefUtil {
    companion object {
        private val TAG = "PrefUtil"
        public val ACTION_INSERT = "insert"
        public val ACTION_UPDATE = "update"
        public val ACTION_RETRIEVE = "All"
        public val KEY_PREF = "rememberMe"
        public val KEY_LOGGEDIN = "loggedIn"
        public val KEY_LANGUAGE = "language"
        public val KEY_USERNAME = "username"
        public val KEY_EMPLOYEE_ID = "EmployeeId"
        public val KEY_REGISTER_ID = "RegisterId"
        public val KEY_MOBILE_NO = "MobileNo"
        public val KEY_ROLE_ID = "RoleId"
        public val KEY_ROLE_NAME = "RoleName"
        public val KEY_STATE_ID = "StateId"
        public val KEY_STATE_NAME = "StateName"
        public val KEY_DISTRICT_ID = "DistrictId"
        public val KEY_DISTRICT_NAME = "DistrictName"
        public val KEY_APMC_ID = "APMCId"
        public val KEY_APMC_NAME = "APMCName"
        public val KEY_COMMODITY_ID = "CommodityId"
        public val KEY_COMMODITY_NAME = "CommodityName"
        public val KEY_COMMODITY_NAME_GUJ = "GujaratiCommodityName"
        public val KEY_COMMODITY_BHARTI = "CommodityBharti"
        public val KEY_COMPANY_CODE = "CompanyCode"
        public val KEY_EMP_NAME = "Name"
        public val KEY_CREATE_USER = "CreateUser"
        public val KEY_BUYER_ID = "BuyerId"
        public val KEY_BUYER_CITY_ID = "CityId"
        public val KEY_TYPE_OF_USER = "Typeofuser"
        public val KEY_NAME = "Name"
        public val KEY_LOCATION = "Location"
        public val KEY_COMMODITY_Id = "CommodityId"
        public val KEY_USER_NAME = "UserName"
        public val KEY_USER_PASSWORD = "UserPassword"
        public val KEY_IsActive = "IsActive"
        public val KEY_IsUser = "IsUser"
        public val KEY_CREATE_DATE = "CreateDate"
        public val KEY_HAS_LOGGEDIN_PREVIOUSLY = "hasLoggedInPreviously"



        lateinit var editor: SharedPreferences.Editor
        lateinit var preferences: SharedPreferences
        lateinit var systemLanguageEditor: SharedPreferences.Editor
        lateinit var systemLanguagePreferences: SharedPreferences

        fun getInstance(context: Context) {
            preferences = context.getSharedPreferences(KEY_PREF, 0)
            editor = preferences.edit()
        }

        fun getLanguageInstance(context: Context) {
            systemLanguagePreferences = context.getSharedPreferences("Language", 0)
            systemLanguageEditor = systemLanguagePreferences.edit()
        }

        public fun setString(PrefName: String, Value: String) {
            editor.putString(PrefName, Value)
            editor.commit()
        }

        public fun setSystemLanguage(Value: String) {
            systemLanguageEditor.putString(KEY_LANGUAGE, Value)
            systemLanguageEditor.commit()
        }
        public fun getSystemLanguage():String? {
            return systemLanguagePreferences.getString(KEY_LANGUAGE, "")
        }

        public fun getString(PrefName: String, Value: String): String? {
            return preferences.getString(PrefName, Value)
        }

        public fun setBoolean(PrefName: String, Value: Boolean) {
            editor.putBoolean(PrefName, Value)
            editor.commit()
        }

        public fun getBoolean(PrefName: String, Value: Boolean): Boolean {
            return preferences.getBoolean(PrefName, Value)
        }

        public fun deletePreference() {
            editor.clear()
            editor.commit()
            Log.d(TAG, "deletePreference: SHARED_PREF_DELETED")
        }
    }

}