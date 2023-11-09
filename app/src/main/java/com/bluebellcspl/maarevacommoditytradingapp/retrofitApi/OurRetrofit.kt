package com.bluebellcspl.maarevacommoditytradingapp.retrofitApi

import com.bluebellcspl.maarevacommoditytradingapp.model.APMCMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.BuyerAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.CityMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.CommodityMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.DistrictMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAAuctionDetailModel
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAListModel
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
    @POST("/MaarevaApi/MaarevaApi/MaarevaApi/RoleMasterGet")
    suspend fun getRoleMaster(@Body body: JsonObject): Response<RoleMasterModel>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/MaarevaApi/StateMasterGet")
    suspend fun getStateMaster(@Body body: JsonObject): Response<StateMasterModel>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/MaarevaApi/DistrictMasterGet")
    suspend fun getDistrictMaster(@Body body: JsonObject): Response<DistrictMasterModel>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/MaarevaApi/APMCMasterGet")
    suspend fun getAPMCMaster(@Body body: JsonObject): Response<APMCMasterModel>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/MaarevaApi/CommodityMasterGet")
    suspend fun getCommodityMaster(@Body body: JsonObject): Response<CommodityMasterModel>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/MaarevaApi/LoginOTPInsert")
    suspend fun getOTPForLogin(@Body body: JsonObject): Response<JsonObject>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/MaarevaApi/LoginCheck")
    suspend fun getLoginCheck(@Body body: JsonObject): Response<JsonObject>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/MaarevaApi/RegisterOTPInsert")
    suspend fun getOTPForRegister(@Body body: JsonObject): Response<JsonObject>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/MaarevaApi/Register")
    suspend fun registerBuyer(@Body body: JsonObject): Response<String>
    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/MaarevaApi/ShopMasterGet")
    suspend fun getShopMaster(@Body body: JsonObject): Response<ShopMasterModel>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/MaarevaApi/PCAMasterInsert")
    suspend fun postPCAInsertData(@Body body: JsonObject): Response<String>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/MaarevaApi/PCAMasterApproveGet")
    suspend fun getApprovedPCAList(@Body body: JsonObject): Response<PCAListModel>
    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/MaarevaApi/PCAMasterNonApproveGet")
    suspend fun getUnapprovedPCAList(@Body body: JsonObject): Response<PCAListModel>
    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/MaarevaApi/PCAMasterGet")
    suspend fun getPCAMaster(@Body body: JsonObject): Response<PCAListModel>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/MaarevaApi/CityMasterGet")
    suspend fun getCityMaster(@Body body: JsonObject): Response<CityMasterModel>
    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/MaarevaApi/TransportationMasterGet")
    suspend fun getTransportationMaster(@Body body: JsonObject): Response<TransportationMasterModel>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/MaarevaApi/PCAMasterUpdate")
    suspend fun postPCAUpdateData(@Body body: JsonObject): Response<String>

    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/MaarevaApi/BuyersAuctionDetailsGet")
    suspend fun getBuyerAuctionDetail(@Body body: JsonObject): Response<BuyerAuctionMasterModel>
    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/MaarevaApi/BuyersAuctionInsUpd")
    suspend fun POSTBuyerAuctionDetail(@Body body: JsonObject): Response<String>
    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/MaarevaApi/UserMasterGet")
    suspend fun getUserMaster(@Body body: JsonObject): Response<UserMasterModel>
    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/MaarevaApi/PCAAuctionDetailsGet")
    suspend fun getPCAAuctionDetail(@Body body: JsonObject): Response<PCAAuctionDetailModel>
    @Headers("Content-Type:application/json")
    @POST("/MaarevaApi/MaarevaApi/MaarevaApi/PCAAuctionInsUpd")
    suspend fun postPCAAuctionDataInsUpd(@Body body: JsonObject): Response<JsonObject>
}