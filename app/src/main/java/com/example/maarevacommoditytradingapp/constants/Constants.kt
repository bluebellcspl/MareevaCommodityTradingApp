package com.example.maarevacommoditytradingapp.constants

import com.karumi.dexter.BuildConfig


class Constants {
    companion object {
        val version:String = BuildConfig.VERSION_NAME
        val index: Int = 0
        val OneDayInMillies = 86400000
        val EmployeeMaster_TBL = "EmployeeMaster"
        val UserMaster_TBL = "UserMaster"
        val LeaveMaster_TBL = "LeaveMaster"
    }
}