package com.bluebellcspl.maarevacommoditytradingapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.maarevacommoditytradingapp.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }, 2000)
    }
}