package com.example.maarevacommoditytradingapp.database

import android.util.Log

class Query {

    companion object {
        lateinit var qry:String
        val TAG = "Query"

        fun getEmpGender():String{
            qry = "SELECT Gender FROM UserMaster"
            Log.d(TAG, "getEmpGender: $qry")
            return qry
        }

    }

}