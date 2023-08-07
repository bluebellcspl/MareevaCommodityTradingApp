package com.example.maarevacommoditytradingapp.commonFunction

import android.content.Context
import android.net.ConnectivityManager

class NetworkUtility(var context: Context) {
    fun isInternetOnline(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return if (activeNetwork != null && activeNetwork.isConnected) {
            true
        } else false
    }

    fun isOnline () : Boolean{
        return if (isInternetOnline()) {
            true
        } else {
            false
        }
    }


}