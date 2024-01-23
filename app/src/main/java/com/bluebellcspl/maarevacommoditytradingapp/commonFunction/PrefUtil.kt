package com.bluebellcspl.maarevacommoditytradingapp.commonFunction

import android.content.Context
import android.content.SharedPreferences

class PrefUtil {
    companion object {
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



        lateinit var editor: SharedPreferences.Editor
        lateinit var preferences: SharedPreferences

        fun getInstance(context: Context) {
            preferences = context.getSharedPreferences(KEY_PREF, 0)
            editor = preferences.edit()
        }

        public fun setString(PrefName: String, Value: String) {
            editor.putString(PrefName, Value)
            editor.commit()
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
        }
    }

}