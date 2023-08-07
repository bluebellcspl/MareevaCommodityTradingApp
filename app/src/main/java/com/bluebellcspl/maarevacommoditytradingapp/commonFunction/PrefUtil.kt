package com.bluebellcspl.maarevacommoditytradingapp.commonFunction

import android.content.Context
import android.content.SharedPreferences

class PrefUtil {
    companion object {
        public val ACTION_INSERT = "insert"
        public val ACTION_UPDATE = "update"
        public val ACTION_RETRIEVE = "All"
        public val ACTION_APPROVE = "approve"
        public val ACTION_REJECT = "Reject"
        public val ACTION_DELETE = "delete"
        public val ACTION_FINAL_APPROVE = "Finalapprove"
        public val KEY_PREF = "rememberMe"
        public val KEY_LOGGEDIN = "loggedIn"
        public val KEY_LANGUAGE = "language"
        public val KEY_USERNAME = "username"
        public val KEY_EMPLOYEEID = "EmployeeId"
        public val KEY_ANDROIDID = "AndroidId"
        public val KEY_BRAND = "Brand"
        public val KEY_MODEL = "Model"
        public val KEY_COMPANYCODE = "CompanyCode"
        public val KEY_FINANCIAL_YEAR = "FinancialYear"
        public val KEY_USER_ROLE = "UserRole"
        public val KEY_PASSWORD = "password"
        public val KEY_ISACTIVE = "Isactive"
        public val KEY_EMP_CODE = "EmployeeCode"
        public val KEY_EMP_NAME = "EmployeeName"
        public val KEY_EMP_DEPT = "DepartmentName"
        public val KEY_EMP_DEPT_ID = "DepartmentId"
        public val KEY_EMP_DESIGNATION = "DesignationName"
        public val KEY_EMP_DESIGNATION_CODE = "DesignationCode"
        public val KEY_EMP_DESIGNATION_TYPE = "DesignationType"
        public val KEY_EMP_DEPT_CODE = "DepartmentCode"
        public val KEY_EMP_DESIGNATION_ID = "DesignationId"
        public var KEY_LOGGEDOUT = "loggedOut"
        public var KEY_EMP_TYPE_ID = "EmployeeTypeId"
        public var KEY_EMP_TYPENAME = "EmployeeTypeName"
        public var KEY_TOKEN_ID = "tokenId"
        public var KEY_BOTTOMSHEET_STATE = "BottomSheetState"
        public var KEY_FETCH_UPDATE_DATE = "fetchUpdateDate"


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