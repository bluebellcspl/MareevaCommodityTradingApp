package com.bluebellcspl.maarevacommoditytradingapp.retrofitApi

import com.bluebellcspl.maarevacommoditytradingapp.model.APMCMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.BuyerAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.BuyerMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.BuyerPrevAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.CityMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.CommodityMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.DistrictMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.NotificationRTRMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAAuctionDetailModel
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAListModel
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAPrevAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.RoleMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.ShopMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.StateMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.TransportationMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.UserMasterModel
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


interface OurRetrofit {

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/RoleMasterGet")
    suspend fun getRoleMaster(@Body body: JsonObject): Response<RoleMasterModel>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/StateMasterGet")
    suspend fun getStateMaster(@Body body: JsonObject): Response<StateMasterModel>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/DistrictMasterGet")
    suspend fun getDistrictMaster(@Body body: JsonObject): Response<DistrictMasterModel>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/APMCMasterGet")
    suspend fun getAPMCMaster(@Body body: JsonObject): Response<APMCMasterModel>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/CommodityMasterGet")
    suspend fun getCommodityMaster(@Body body: JsonObject): Response<CommodityMasterModel>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/LoginOTPInsert")
    suspend fun getOTPForLogin(@Body body: JsonObject): Response<JsonObject>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/LoginCheck")
    suspend fun getLoginCheck(@Body body: JsonObject): Response<JsonObject>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/RegisterOTPInsert")
    suspend fun getOTPForRegister(@Body body: JsonObject): Response<JsonObject>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/Register")
    suspend fun registerBuyer(@Body body: JsonObject): Response<String>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/ShopMasterGet")
    suspend fun getShopMaster(@Body body: JsonObject): Response<ShopMasterModel>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/PCAMasterInsert")
    suspend fun postPCAInsertData(@Body body: JsonObject): Response<String>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/PCAMasterApproveGet")
    suspend fun getApprovedPCAList(@Body body: JsonObject): Response<PCAListModel>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/PCAMasterNonApproveGet")
    suspend fun getUnapprovedPCAList(@Body body: JsonObject): Response<PCAListModel>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/PCAMasterGet")
    suspend fun getPCAMaster(@Body body: JsonObject): Response<PCAListModel>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/CityMasterGet")
    suspend fun getCityMaster(@Body body: JsonObject): Response<CityMasterModel>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/TransportationMasterGet")
    suspend fun getTransportationMaster(@Body body: JsonObject): Response<TransportationMasterModel>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/PCAMasterUpdate")
    suspend fun postPCAUpdateData(@Body body: JsonObject): Response<String>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/BuyersAuctionDetailsGet")
    suspend fun getBuyerAuctionDetail(@Body body: JsonObject): Response<BuyerAuctionMasterModel>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/BuyersAuctionInsUpd")
    suspend fun POSTBuyerAuctionDetail(@Body body: JsonObject): Response<String>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/UserMasterGet")
    suspend fun getUserMaster(@Body body: JsonObject): Response<UserMasterModel>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/PCAAuctionDetailsGet")
    suspend fun getPCAAuctionDetail(@Body body: JsonObject): Response<PCAAuctionDetailModel>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/PCAAuctionDetailsGet")
    suspend fun checkPCAIsAuctionStop(@Body body: JsonObject): Response<JsonObject>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/PCAAuctionInsUpd")
    suspend fun postPCAAuctionDataInsUpd(@Body body: JsonObject): Response<JsonObject>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/PCAAuctionDelete")
    suspend fun postPCAAuctionDataDelete(@Body body: JsonObject): Response<JsonObject>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/BuyersLiveAuctionInsUpd")
    suspend fun postAuctionStartStop(@Body body: JsonObject): Response<String>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/BuyerMasterGet")
    suspend fun getBuyerMaster(@Body body: JsonObject): Response<BuyerMasterModel>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/BuyerDashPreAuctionGet")
    suspend fun getBuyerPreviousAuction(@Body body: JsonObject): Response<BuyerPrevAuctionMasterModel>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/PCADashPreAuctionGet")
    suspend fun getPCAPreviousAuction(@Body body: JsonObject): Response<PCAPrevAuctionMasterModel>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/NotificationListGet")
    suspend fun getNotification(@Body body: JsonObject): Response<NotificationRTRMasterModel>

}