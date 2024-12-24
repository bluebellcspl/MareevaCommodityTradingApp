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
            qry = "SELECT APMCName FROM APMCMaster WHERE IsActive='true'"
            Log.d(TAG, "getAPMCName: $qry")
            return qry
        }

        fun getAPMCIdByAPMCName(apmcName:String):String{
            qry = "SELECT APMCId FROM APMCMaster WHERE APMCName='$apmcName' AND IsActive='true'"
            Log.d(TAG, "getAPMCIdByAPMCName: $qry")
            return qry
        }
        fun getCommodityNameByAPMCId(apmcId:String):String{
            qry = "SELECT CommodityName FROM CommodityMaster WHERE APMCId='$apmcId' AND IsActive='1'"
            Log.d(TAG, "getCommodityNameByAPMCId: $qry")
            return qry
        }
        fun getStateIdByAPMCId(apmcId:String):String{
            qry = "SELECT StateId FROM APMCMaster WHERE APMCId='$apmcId' AND IsActive='true'"
            Log.d(TAG, "getStateIdByAPMCId: $qry")
            return qry
        }
        fun getDistrictIdByAPMCId(apmcId:String):String{
            qry = "SELECT DistrictId FROM APMCMaster WHERE APMCId='$apmcId' AND IsActive='true'"
            Log.d(TAG, "getDistrictIdByAPMCId: $qry")
            return qry
        }
        fun getDistrictNameByAPMCId(apmcId:String):String{
            qry = "SELECT DistrictName FROM APMCMaster WHERE APMCId='$apmcId' AND IsActive='true'"
            Log.d(TAG, "getDistrictNameByAPMCId: $qry")
            return qry
        }

        fun getMarketCessByAPMCId(apmcId:String):String{
            qry = "SELECT MarketCess FROM APMCMaster WHERE APMCId='$apmcId' AND IsActive='true'"
            Log.d(TAG, "getMarketCessByAPMCId: $qry")
            return qry
        }

        fun getStateNameByAPMCId(apmcId:String):String{
            qry = "SELECT StateName FROM APMCMaster WHERE APMCId='$apmcId' AND IsActive='true'"
            Log.d(TAG, "getStateNameByAPMCId: $qry")
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

        fun getShopName(apmcId:String):String{
            qry = "SELECT ShopName FROM ShopMaster WHERE APMCId='$apmcId' AND IsActive='true'"
            Log.d(TAG, "getShopName: $qry")
            return qry
        }
        fun getShopNo(apmcId:String):String{
            qry = "SELECT ShopNo FROM ShopMaster WHERE APMCId='$apmcId' AND IsActive='true'"
            Log.d(TAG, "getShopNo: $qry")
            return qry
        }
        fun getShopIdByShopName(shopName:String,apmcId: String):String{
            qry = "SELECT ShopId FROM ShopMaster WHERE ShopName='$shopName' AND APMCId='$apmcId'"
            Log.d(TAG, "getShopIdByShopName: $qry")
            return qry
        }
        fun getShopIdByShopNo(shopNo:String,apmcId: String):String{
            qry = "SELECT ShopId FROM ShopMaster WHERE ShopNo='$shopNo' AND APMCId='$apmcId'"
            Log.d(TAG, "getShopIdByShopNo: $qry")
            return qry
        }

        fun getShopNoByShopName(shopName:String,apmcId: String):String{
            qry = "SELECT ShopNo FROM ShopMaster WHERE ShopName='$shopName' AND APMCId='$apmcId'"
            Log.d(TAG, "getShopNoByShopName: $qry")
            return qry
        }

        fun getShopNoByGujShopName(shopName:String,apmcId: String):String{
            qry = "SELECT ShopNo FROM ShopMaster WHERE ShopName='$shopName' AND APMCId='$apmcId'"
            Log.d(TAG, "getShopNoByShopName: $qry")
            return qry
        }
        fun getShopNameByShopNo(shopNo:String,apmcId: String):String{
            qry = "SELECT ShopName FROM ShopMaster WHERE ShopNo='$shopNo' AND APMCId='$apmcId'"
            Log.d(TAG, "getShopNameByShopNo: $qry")
            return qry
        }

        fun getApprovedPCAName():String{
            qry = "SELECT PCAName FROM PCAMaster WHERE IsActive='true'"
            Log.d(TAG, "getApprovedPCAName: $qry")
            return qry
        }

        fun getPCADetail():String{
            qry = "SELECT PCAId,PCAName,PCARegId,GCACommission,PCACommission,MarketCess,LabourCharges FROM PCAMaster WHERE IsActive='true' AND ApprStatus='true'"
            Log.d(TAG, "getPCADetail: $qry")
            return qry
        }

        fun getCommodityBhartiByCommodityId(commodityId: String):String{
            qry = "SELECT Bharti FROM CommodityMaster WHERE CommodityId='$commodityId' AND IsActive='1'"
            Log.d(TAG, "getCommodityBhartiByCommodityId: $qry")
            return qry
        }

        fun getUnseenNotification():String{
            qry = "SELECT NotificationId FROM NotificationMaster WHERE ISRead='false'"
            Log.d(TAG, "getUnseenNotification: $qry")
            return qry
        }

        fun getTMPTUnseenNotification():String{
            qry = "SELECT COUNT(ISRead) FROM TempNotificationMaster WHERE ISRead='false'"
            Log.d(TAG, "getTMPTUnseenNotification: $qry")
            return qry
        }

        fun getTMPTUnseenChatNotification():String{
            qry = "SELECT COUNT(IsRead) FROM TempChatNotification WHERE IsRead='false'"
            Log.d(TAG, "getTMPTUnseenChatNotification: $qry")
            return qry
        }

        fun updateNotificationSeenStatus():String{
            qry = "UPDATE NotificationMaster SET ISRead='true'"
            Log.d(TAG, "updateNotificationSeenStatus: $qry")
            return qry
        }
        fun updateTMPNotificationSeenStatus():String{
            qry = "UPDATE TempNotificationMaster SET ISRead='true'"
            Log.d(TAG, "updateNotificationSeenStatus: $qry")
            return qry
        }
        fun updateTMPChatNotificationStatus():String{
            qry = "UPDATE TempChatNotification SET IsRead='true'"
            Log.d(TAG, "updateTMPChatNotificationStatus: $qry")
            return qry
        }

        fun getAllNotification():String{
            qry = "SELECT * FROM NotificationMaster ORDER By NotificationId desc"
            Log.d(TAG, "getAllNotification: $qry")
            return qry
        }

        fun getGujaratiCommodityNameByCommodityId(commodityId: String):String{
            qry = "SELECT GujaratiCommodityName FROM CommodityMaster WHERE CommodityId='$commodityId'"
            Log.d(TAG, "getGujaratiCommodityNameByCommodityId: $qry")
            return qry
        }

        fun getGujaratiPCANameByPCAId(PCAId: String):String{
            qry = "SELECT GujaratiPCAName FROM PCAMaster WHERE PCAId='$PCAId'"
            Log.d(TAG, "getGujaratiPCANameByPCAId: $qry")
            return qry
        }

        fun getShortShopName(apmcId:String):String{
            qry = "SELECT ShortShopName FROM ShopMaster WHERE APMCId='$apmcId' AND IsActive='true'"
            Log.d(TAG, "getShortShopName: $qry")
            return qry
        }

        fun deleteAllShop():String{
            qry = "DELETE FROM ShopMaster"
            Log.d(TAG, "deleteAllShop: $qry")
            return qry
        }

        fun getGujShortShopName(apmcId:String):String{
            qry = "SELECT GujaratiShortShopName FROM ShopMaster WHERE APMCId='$apmcId' AND IsActive='true'"
            Log.d(TAG, "getGujShortShopName: $qry")
            return qry
        }

        fun getShortShopNameByShopNo(shopNo:String,apmcId: String):String{
            qry = "SELECT ShortShopName FROM ShopMaster WHERE ShopNo='$shopNo' AND APMCId='$apmcId'"
            Log.d(TAG, "getShortShopNameByShopNo: $qry")
            return qry
        }
        fun getGujShortShopNameByShopNo(shopNo:String,apmcId: String):String{
            qry = "SELECT GujaratiShortShopName FROM ShopMaster WHERE ShopNo='$shopNo' AND APMCId='$apmcId'"
            Log.d(TAG, "getGujShortShopNameByShopNo: $qry")
            return qry
        }

        fun getShopNoByShortShopName(shopName:String,apmcId: String):String{
            qry = "SELECT ShopNo FROM ShopMaster WHERE ShortShopName='$shopName' AND APMCId='$apmcId'"
            Log.d(TAG, "getShopNoByShortShopName: $qry")
            return qry
        }

        fun getShopNoByGujShortShopName(shopName:String,apmcId: String):String{
            qry = "SELECT ShopNo FROM ShopMaster WHERE GujaratiShortShopName='$shopName' AND APMCId='$apmcId'"
            Log.d(TAG, "getShopNoByGujShortShopName: $qry")
            return qry
        }

        fun getShopData():String{
            qry = "SELECT ShopId,ShopNo,ShortShopName,GujaratiShortShopName FROM ShopMaster"
            Log.d(TAG, "getShopData: $qry")
            return qry
        }

        fun getApprovedPCAs():String{
            qry = "SELECT * FROM ShopMaster"
            Log.d(TAG, "getShopNoByGujShortShopName: $qry")
            return qry
        }

        fun getCityName():String{
            qry = "SELECT CityName FROM APMCMaster"
            Log.d(TAG, "getCityName: $qry")
            return qry
        }

        fun getCityIdByCityName(cityName:String):String{
            qry = "SELECT CityId FROM APMCMaster WHERE CityName='$cityName'"
            Log.d(TAG, "getCityIdByCityName: $qry")
            return qry
        }

        fun getAPMCNameByCityId(cityId:String):String{
            qry = "SELECT APMCName FROM APMCMaster WHERE IsActive='true' AND CityId='$cityId'"
            Log.d(TAG, "getAPMCNameByCityId: $qry")
            return qry
        }

        fun getCityNameByAPMCId(apmcId:String):String{
            qry = "SELECT CityName FROM APMCMaster WHERE IsActive='true' AND APMCId='$apmcId'"
            Log.d(TAG, "getAPMCNameByCityId: $qry")
            return qry
        }

        fun getGujaratiCommodityName(commodityId:String):String{
            qry = "SELECT GujaratiCommodityName FROM CommodityMaster WHERE CommodityId='$commodityId'"
            Log.d(TAG, "getGujaratiCommodityName: $qry")
            return qry
        }

        fun getAPMCDetail():String{
            qry = "SELECT APMCId,APMCName FROM APMCMaster WHERE IsActive='true'"
            Log.d(TAG, "getAPMCDetail: $qry")
            return qry
        }

        fun getCommodityDetail():String{
            qry = "SELECT CommodityId,CommodityName,GujaratiCommodityName FROM CommodityMaster WHERE IsActive='1'"
            Log.d(TAG, "getCommodityDetail: $qry")
            return qry
        }

        fun getShopNoName():String{
            qry = "SELECT ShopId,ShopNo,ShopNoName,GujaratiShopNoName FROM ShopMaster WHERE IsActive = 'true'"
            Log.d(TAG, "getShopNoName: $qry")
            return qry
        }

        fun getBuyerList():String{
            qry = "SELECT BuyerName FROM BuyerData"
            Log.d(TAG, "getBuyerList: $qry")
            return qry
        }

        fun getCityData():String{
            qry = "SELECT CityName,CityId FROM CityMaster WHERE IsActive='true'"
            Log.d(TAG, "getCityData: $qry")
            return qry
        }


    }

}