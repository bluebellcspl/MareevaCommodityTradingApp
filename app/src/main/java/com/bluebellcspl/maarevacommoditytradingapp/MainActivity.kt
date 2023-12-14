package com.bluebellcspl.maarevacommoditytradingapp

import android.Manifest
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.PermissionRequestErrorListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        PrefUtil.getInstance(this)
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            checkUserPermissions()
        }, 2000)
    }

    fun checkLoggedIn() {
        Log.d(
            TAG,
            "checkLoggedIn: IS_LOGGED_IN : ${
                PrefUtil.getBoolean(PrefUtil.KEY_LOGGEDIN, false).toString()
            }"
        )
        if (PrefUtil.getBoolean(PrefUtil.KEY_LOGGEDIN, false)) {

            val intent = Intent(this@MainActivity, HomeActivity::class.java)
            startActivity(
                Intent(intent),
                ActivityOptions.makeSceneTransitionAnimation(this@MainActivity).toBundle()
            )
            finish()
        } else {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(
                Intent(intent),
                ActivityOptions.makeSceneTransitionAnimation(this@MainActivity).toBundle()
            )
            finish()
        }
    }

    fun checkUserPermissions() {
        var permissionsList = ArrayList<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsList.add(Manifest.permission.POST_NOTIFICATIONS)
            permissionsList.add(Manifest.permission.WAKE_LOCK)
        } else {
            permissionsList.add(Manifest.permission.WAKE_LOCK)
        }

        Dexter.withContext(this@MainActivity).withPermissions(permissionsList).withListener(
            object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    if (p0!!.isAnyPermissionPermanentlyDenied) {
                        Toast.makeText(
                            this@MainActivity,
                            "Please Grant Permission!",
                            Toast.LENGTH_SHORT
                        ).show()
                        for (permission in p0!!.deniedPermissionResponses) {
                            Log.d(
                                TAG,
                                "onPermissionsChecked: DENIED_PERMISSION : ${permission.permissionName}"
                            )
                        }
                    }
                    if (p0!!.areAllPermissionsGranted()) {
                        checkLoggedIn()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    p1!!.continuePermissionRequest()
                }
            }
        ).withErrorListener(object : PermissionRequestErrorListener {
            override fun onError(p0: DexterError?) {
                Log.d(TAG, "onError: ${p0!!.name}")
            }
        }).onSameThread()
            .check()
    }
}