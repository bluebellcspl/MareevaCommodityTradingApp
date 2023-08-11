package com.bluebellcspl.maarevacommoditytradingapp.retrofitApi

import com.bluebellcspl.maarevacommoditytradingapp.model.APMCMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.CommodityMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.DistrictMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.RoleMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.StateMasterModel
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
}