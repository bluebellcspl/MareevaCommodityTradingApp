package com.bluebellcspl.maarevacommoditytradingapp.commonFunction

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import android.util.Log

class GPSProvider(context: Context, activity: Activity) {
    val TAG = "GPSProvider"
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    var gpsEnable: Boolean = false
    var networkEnable: Boolean = false
    lateinit var mContext: Context
    lateinit var mActivity: Activity
    lateinit var alertDialog: AlertDialog
    init {
        mContext = context
        mActivity = activity
        getLocationProvider()
    }

    private fun getLocationProvider() {
        try {
            gpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            networkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!gpsEnable && !networkEnable) {
                alertDialog = AlertDialog.Builder(mContext)
                    .setMessage("GPS Location or Network Not Enabled")
                    .setCancelable(false)
                    .setPositiveButton("Setting", object : DialogInterface.OnClickListener {
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            mContext.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            mActivity.finish()
                        }
                    })
                    .setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            mActivity.finish()
                        }
                    })
                    .show()
            }
            else
            {
                alertDialog.dismiss()
            }
        } catch (e: Exception) {
            Log.e(TAG, "getLocationProvider: " + e.message)
        }
    }
}