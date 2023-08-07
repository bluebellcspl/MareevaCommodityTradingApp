package com.bluebellcspl.maarevacommoditytradingapp.database

import android.util.Log

class Query {

    companion object {
        lateinit var qry:String
        val TAG = "Query"


        fun getRoleName():String{
            qry = "SELECT RoleName FROM RoleMaster"
            Log.d(TAG, "getRoleName: $qry")
            return qry
        }

        fun getAPMCName():String{
            qry = "SELECT APMCName FROM APMCMaster"
            Log.d(TAG, "getAPMCName: $qry")
            return qry
        }
        fun getCommodityName():String{
            qry = "SELECT CommodityName FROM CommodityMaster"
            Log.d(TAG, "getCommodityName: $qry")
            return qry
        }


    }

}