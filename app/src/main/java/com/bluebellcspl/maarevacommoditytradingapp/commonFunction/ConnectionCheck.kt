package com.bluebellcspl.maarevacommoditytradingapp.commonFunction

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import android.widget.Toast

class ConnectionCheck : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (isConnected(context)) {
            Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Not Connected!", Toast.LENGTH_SHORT).show()
        }
    }

    fun isConnected(context: Context?): Boolean {
        try {
            val connectivityManager =
                context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val networkInfo = connectivityManager.activeNetworkInfo
                return networkInfo != null && networkInfo.isConnected
            }
        } catch (e: Exception) {
            e.stackTrace
            Log.e("ConnectionCheckClass", "isConnected: " + e.message)
        }
        return false
    }
}