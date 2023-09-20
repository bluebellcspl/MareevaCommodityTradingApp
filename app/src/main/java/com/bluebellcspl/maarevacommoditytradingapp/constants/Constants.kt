package com.bluebellcspl.maarevacommoditytradingapp.constants

import com.karumi.dexter.BuildConfig


class Constants {
    companion object {
        val version:String = BuildConfig.VERSION_NAME
        val index: Int = 0
        val OneDayInMillies = 86400000
        val TBL_RoleMaster = "RoleMaster"
        val TBL_StateMaster = "StateMaster"
        val TBL_DistrictMaster = "DistrictMaster"
        val TBL_APMCMaster = "APMCMaster"
        val TBL_CommodityMaster = "CommodityMaster"
        val TBL_ShopMaster = "ShopMaster"
        val TBL_PCAMaster = "PCAMaster"
    }
}