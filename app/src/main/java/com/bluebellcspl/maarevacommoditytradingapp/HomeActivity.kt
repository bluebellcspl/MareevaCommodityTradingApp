package com.bluebellcspl.maarevacommoditytradingapp

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Explode
import android.util.Log
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.databinding.ActivityHomeBinding
import java.util.Locale

class HomeActivity : AppCompatActivity() {
    lateinit var binding: ActivityHomeBinding
    private val commonUIUtility by lazy { CommonUIUtility(this) }
    lateinit var navController: NavController
    val TAG = "HomeActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        PrefUtil.getInstance(this)
        val languageCode = PrefUtil.getString(PrefUtil.KEY_LANGUAGE, "en")
        val activityConf = Configuration()
        val newLocale = Locale(languageCode)
        activityConf.setLocale(newLocale)
        baseContext.resources.updateConfiguration(
            activityConf,
            baseContext.resources.displayMetrics
        )
        super.onCreate(savedInstanceState)
        with(window) {
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            enterTransition = Explode()
            exitTransition = Explode()
            enterTransition.duration = 700
            exitTransition.duration = 1000
        }
        binding = DataBindingUtil.setContentView(this@HomeActivity,R.layout.activity_home)
        setSupportActionBar(binding.toolbarHome.toolbar)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController
        setupActionBarWithNavController(navController)
        binding.toolbarHome.toolbar.setupWithNavController(navController)
        Log.d(TAG, "onCreate: Current_DESTINATION : ${navController.currentDestination}")
    }

    override fun onBackPressed() {
        if (navController.currentDestination!!.displayName.equals("com.bluebellcspl.maarevacommoditytradingapp:id/dashboardFragment")) {
            finishAffinity()
        } else {
            navController.navigateUp()
        }
    }
}