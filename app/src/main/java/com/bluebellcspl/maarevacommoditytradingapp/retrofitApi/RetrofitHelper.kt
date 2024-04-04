package com.bluebellcspl.maarevacommoditytradingapp.retrofitApi

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitHelper {
    val BASE_URL = "https://maareva.com/"
//    val BASE_URL = "https://maareva.bbcspldev.in/"
    val IMG_BASE_URL = "https://maareva.com/UploadImages/"
//    val IMG_BASE_URL = "https://maareva.bbcspldev.in/UploadImages/"

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