package com.bluebellcspl.maarevacommoditytradingapp.retrofitApi

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitHelper {
//    val BASE_URL = "https://maareva.com/"
    val BASE_URL = "https://maareva.bbcspldev.in/" //Testing Server URL
//    val IMG_BASE_URL = "https://maareva.com/UploadImages/"
    val IMG_BASE_URL = "https://maareva.bbcspldev.in/UploadImages/" //Testing Server URL
//    val WEBSOCKET_BASE_URL = "wss://maareva.com"
    val WEBSOCKET_BASE_URL = "wss://maareva.bbcspldev.in"  //Testing Sever URL

    fun getInstance(): Retrofit {
        val gson = GsonBuilder().setLenient().serializeNulls().create()

        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()
    }
}