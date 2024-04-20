package com.bluebellcspl.maarevacommoditytradingapp.retrofitApi

import com.bluebellcspl.maarevacommoditytradingapp.model.APMCMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.BuyerAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.BuyerMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.BuyerPrevAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.ChatHistoryModel
import com.bluebellcspl.maarevacommoditytradingapp.model.ChatRecipientModel
import com.bluebellcspl.maarevacommoditytradingapp.model.CityMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.CommodityMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.DistrictMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.InvoiceDataModel
import com.bluebellcspl.maarevacommoditytradingapp.model.NotificationRTRMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAAuctionDetailModel
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAListModel
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAPrevAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.RoleMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.ShopMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.StateMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.TransportationMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.UserMasterModel
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query


interface OurRetrofit {

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/RoleMasterGet")
//    @POST("/MaarevaApi/MaarevaApi/RoleMasterGet")
    suspend fun getRoleMaster(@Body body: JsonObject): Response<RoleMasterModel>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/StateMasterGet")
//    @POST("/MaarevaApi/MaarevaApi/StateMasterGet")
    suspend fun getStateMaster(@Body body: JsonObject): Response<StateMasterModel>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/DistrictMasterGet")
//    @POST("/MaarevaApi/MaarevaApi/DistrictMasterGet")
    suspend fun getDistrictMaster(@Body body: JsonObject): Response<DistrictMasterModel>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/APMCMasterGet")
//    @POST("/MaarevaApi/MaarevaApi/APMCMasterGet")
    suspend fun getAPMCMaster(@Body body: JsonObject): Response<APMCMasterModel>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/CommodityMasterGet")
//    @POST("/MaarevaApi/MaarevaApi/CommodityMasterGet")
    suspend fun getCommodityMaster(@Body body: JsonObject): Response<CommodityMasterModel>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/LoginOTPInsert")
//    @POST("/MaarevaApi/MaarevaApi/LoginOTPInsert")
    suspend fun getOTPForLogin(@Body body: JsonObject): Response<JsonObject>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/LoginCheck")
//    @POST("/MaarevaApi/MaarevaApi/LoginCheck")
    suspend fun getLoginCheck(@Body body: JsonObject): Response<JsonObject>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/RegisterOTPInsert")
//    @POST("/MaarevaApi/MaarevaApi/RegisterOTPInsert")
    suspend fun getOTPForRegister(@Body body: JsonObject): Response<JsonObject>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/Register")
//    @POST("/MaarevaApi/MaarevaApi/Register")
    suspend fun registerBuyer(@Body body: JsonObject): Response<JsonObject>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/ShopMasterGet")
//    @POST("/MaarevaApi/MaarevaApi/ShopMasterGet")
    suspend fun getShopMaster(@Body body: JsonObject): Response<ShopMasterModel>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/PCAMasterInsert")
//    @POST("/MaarevaApi/MaarevaApi/PCAMasterInsert")
    suspend fun postPCAInsertData(@Body body: JsonObject): Response<JsonObject>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/PCAMasterApproveGet")
//    @POST("/MaarevaApi/MaarevaApi/PCAMasterApproveGet")
    suspend fun getApprovedPCAList(@Body body: JsonObject): Response<PCAListModel>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/PCAMasterNonApproveGet")
//    @POST("/MaarevaApi/MaarevaApi/PCAMasterNonApproveGet")
    suspend fun getUnapprovedPCAList(@Body body: JsonObject): Response<PCAListModel>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/PCAMasterGet")
//    @POST("/MaarevaApi/MaarevaApi/PCAMasterGet")
    suspend fun getPCAMaster(@Body body: JsonObject): Response<PCAListModel>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/CityMasterGet")
//    @POST("/MaarevaApi/MaarevaApi/CityMasterGet")
    suspend fun getCityMaster(@Body body: JsonObject): Response<CityMasterModel>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/TransportationMasterGet")
//    @POST("/MaarevaApi/MaarevaApi/TransportationMasterGet")
    suspend fun getTransportationMaster(@Body body: JsonObject): Response<TransportationMasterModel>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/PCAMasterUpdate")
//    @POST("/MaarevaApi/MaarevaApi/PCAMasterUpdate")
    suspend fun postPCAUpdateData(@Body body: JsonObject): Response<JsonObject>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/BuyersAuctionDetailsGet")
//    @POST("/MaarevaApi/MaarevaApi/BuyersAuctionDetailsGet")
    suspend fun getBuyerAuctionDetail(@Body body: JsonObject): Response<BuyerAuctionMasterModel>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/BuyersAuctionInsUpd")
//    @POST("/MaarevaApi/MaarevaApi/BuyersAuctionInsUpd")
    suspend fun POSTBuyerAuctionDetail(@Body body: JsonObject): Response<JsonObject>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/UserMasterGet")
//    @POST("/MaarevaApi/MaarevaApi/UserMasterGet")
    suspend fun getUserMaster(@Body body: JsonObject): Response<UserMasterModel>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/PCAAuctionDetailsGet")
//    @POST("/MaarevaApi/MaarevaApi/PCAAuctionDetailsGet")
    suspend fun getPCAAuctionDetail(@Body body: JsonObject): Response<PCAAuctionDetailModel>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/PCAAuctionDetailsGet")
//    @POST("/MaarevaApi/MaarevaApi/PCAAuctionDetailsGet")
    suspend fun checkPCAIsAuctionStop(@Body body: JsonObject): Response<JsonObject>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/PCAAuctionInsUpd")
//    @POST("/MaarevaApi/MaarevaApi/PCAAuctionInsUpd")
    suspend fun postPCAAuctionDataInsUpd(@Body body: JsonObject): Response<JsonObject>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/PCAAuctionDelete")
//    @POST("/MaarevaApi/MaarevaApi/PCAAuctionDelete")
    suspend fun postPCAAuctionDataDelete(@Body body: JsonObject): Response<JsonObject>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/BuyersLiveAuctionInsUpd")
//    @POST("/MaarevaApi/MaarevaApi/BuyersLiveAuctionInsUpd")
    suspend fun postAuctionStartStop(@Body body: JsonObject): Response<String>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/BuyerMasterGet")
//    @POST("/MaarevaApi/MaarevaApi/BuyerMasterGet")
    suspend fun getBuyerMaster(@Body body: JsonObject): Response<BuyerMasterModel>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/BuyerDashPreAuctionGet")
//    @POST("/MaarevaApi/MaarevaApi/BuyerDashPreAuctionGet")
    suspend fun getBuyerPreviousAuction(@Body body: JsonObject): Response<BuyerPrevAuctionMasterModel>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/PCADashPreAuctionGet")
//    @POST("/MaarevaApi/MaarevaApi/PCADashPreAuctionGet")
    suspend fun getPCAPreviousAuction(@Body body: JsonObject): Response<PCAPrevAuctionMasterModel>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/NotificationListGet")
//    @POST("/MaarevaApi/MaarevaApi/NotificationListGet")
    suspend fun getNotification(@Body body: JsonObject): Response<NotificationRTRMasterModel>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/ChangeAgreementStatus")
//    @POST("/MaarevaApi/MaarevaApi/ChangeAgreementStatus")
    suspend fun POSTChangeAgreementStatus(@Body body: JsonObject): Response<String>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/Logout")
//    @POST("/MaarevaApi/MaarevaApi/Logout")
    suspend fun Logout(@Body body: JsonObject): Response<String>

    @Headers("Content-Type:application/json")
  @POST("/API/MaarevaApi/MaarevaApi/NotificationListInsUpd")
//    @POST("/MaarevaApi/MaarevaApi/NotificationListInsUpd")
    suspend fun postUnseenNotificationSync(@Body body: JsonArray): Response<JsonArray>

    @Multipart
  @POST("/API/MaarevaApi/MaarevaApi/SendFileOrAudioByApi")
//    @POST("/MaarevaApi/MaarevaApi/SendFileOrAudioByApi")
    suspend fun uploadFile(
        @Part("Date") Date: RequestBody,
        @Part("FromUser") FromUser: RequestBody,
        @Part("ToUser") ToUser: RequestBody,
        @Part("messageId") messageId: RequestBody,
        @Part("MessageType") MessageType: RequestBody,
        @Part("FileExt") FileExt: RequestBody,
        @Part FileMedia: MultipartBody.Part
    ): Response<String>

  @POST("/API/MaarevaApi/MaarevaApi/NotificationListGet")
//    @POST("/MaarevaApi/MaarevaApi/NotificationListGet")
    suspend fun getNotificationListPageWise(
        @Body body: JsonObject,
        @Query("Page") page: Int,
        @Query("ItemsPerPage") itemsPerPage: Int
    ):Response<NotificationRTRMasterModel>

    @POST("/API/MaarevaApi/MaarevaApi/PersonwiseChatHistoryGet")
//    @POST("/MaarevaApi/MaarevaApi/PersonwiseChatHistoryGet")
    suspend fun getPreviousChat(
        @Body body: JsonObject,
        @Query("Page") page: Int,
        @Query("ItemsPerPage") itemsPerPage: Int
    ):Response<ChatHistoryModel>

    @POST("/API/MaarevaApi/MaarevaApi/UserChatListGet")
//    @POST("/MaarevaApi/MaarevaApi/UserChatListGet")
    suspend fun getChatRecipient(
        @Body body: JsonObject,
    ):Response<ChatRecipientModel>

    @POST("/API/MaarevaApi/MaarevaApi/ShopListGet")
//    @POST("/MaarevaApi/MaarevaApi/UserChatListGet")
    suspend fun getInvoiceData(
        @Body body: JsonObject,
    ):Response<JsonObject>

}