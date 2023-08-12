package com.bluebellcspl.maarevacommoditytradingapp

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.example.maarevacommoditytradingapp.R

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        PrefUtil.getInstance(this)
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            checkLoggedIn()
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
}