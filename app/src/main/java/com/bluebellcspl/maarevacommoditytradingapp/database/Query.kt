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

        fun getAPMCIdByAPMCName(apmcName:String):String{
            qry = "SELECT APMCId FROM APMCMaster WHERE APMCName='$apmcName'"
            Log.d(TAG, "getAPMCIdByAPMCName: $qry")
            return qry
        }
        fun getCommodityNameByAPMCId(apmcId:String):String{
            qry = "SELECT CommodityName FROM CommodityMaster WHERE APMCId='$apmcId'"
            Log.d(TAG, "getCommodityNameByAPMCId: $qry")
            return qry
        }

        fun getCommodityIdByCommodityNameANDAPMCId(commodityName:String,apmcId: String):String{
            qry = "SELECT CommodityId FROM CommodityMaster WHERE CommodityName='$commodityName' AND APMCId='$apmcId'"
            Log.d(TAG, "getCommodityIdByCommodityNameANDAPMCId: $qry")
            return qry
        }

        fun getStateIdByCommodityId(commodityId:String):String{
            qry = "SELECT StateId FROM CommodityMaster WHERE CommodityId='$commodityId'"
            Log.d(TAG, "getStateIdByCommodityId: $qry")
            return qry
        }
        fun getStateNameByCommodityId(commodityId:String):String{
            qry = "SELECT StateName FROM CommodityMaster WHERE CommodityId='$commodityId'"
            Log.d(TAG, "getStateNameByCommodityId: $qry")
            return qry
        }
        fun getDistrictIdByCommodityId(commodityId:String):String{
            qry = "SELECT DistrictId FROM CommodityMaster WHERE CommodityId='$commodityId'"
            Log.d(TAG, "getDistrictIdByCommodityId: $qry")
            return qry
        }
        fun getDistrictNameByCommodityId(commodityId:String):String{
            qry = "SELECT DistrictName FROM CommodityMaster WHERE CommodityId='$commodityId'"
            Log.d(TAG, "getDistrictNameByCommodityId: $qry")
            return qry
        }

        fun getShopNameAndShopNo():String{
            qry = "SELECT ShopName,ShopNo FROM ShopMaster"
            Log.d(TAG, "getShopNameAndShopNo: $qry")
            return qry
        }
        fun getShopIdByShopName(shopName:String):String{
            qry = "SELECT ShopId FROM ShopMaster WHERE ShopName='$shopName'"
            Log.d(TAG, "getShopIdByShopName: $qry")
            return qry
        }

        fun getShopNoByShopName(shopName:String):String{
            qry = "SELECT ShopNo FROM ShopMaster WHERE ShopName='$shopName'"
            Log.d(TAG, "getShopNoByShopName: $qry")
            return qry
        }


    }

}