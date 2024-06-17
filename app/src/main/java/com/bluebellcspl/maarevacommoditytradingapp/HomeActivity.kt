package com.bluebellcspl.maarevacommoditytradingapp

import android.content.res.Configuration
import android.os.Bundle
import android.transition.Explode
import android.util.Log
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.databinding.ActivityHomeBinding
import java.util.Locale

class HomeActivity : AppCompatActivity() {
    var _binding: ActivityHomeBinding? = null
    val binding get() = _binding!!
    private val commonUIUtility by lazy { CommonUIUtility(this) }
    lateinit var navController: NavController
    val TAG = "HomeActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        PrefUtil.getInstance(this)
        val languageCode = PrefUtil.getSystemLanguage().toString()
        val activityConf = Configuration()
        val newLocale = Locale(languageCode)
        activityConf.setLocale(newLocale)
        baseContext.resources.updateConfiguration(
            activityConf,
            baseContext.resources.displayMetrics
        )
        PrefUtil.setBoolean(PrefUtil.KEY_HAS_LOGGEDIN_PREVIOUSLY, true)
        super.onCreate(savedInstanceState)
        with(window) {
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            enterTransition = Explode()
            exitTransition = Explode()
            enterTransition.duration = 700
            exitTransition.duration = 1000
        }
        _binding = DataBindingUtil.setContentView(this@HomeActivity, R.layout.activity_home)
        DatabaseManager.initializeInstance(applicationContext)
        setSupportActionBar(binding.toolbarHome.toolbar)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController
        var CURRENT_USER = PrefUtil.getString(PrefUtil.KEY_ROLE_NAME, "").toString()
        setHomeDestination(CURRENT_USER)
        setupActionBarWithNavController(navController)
        binding.toolbarHome.toolbar.setupWithNavController(navController)
        Log.d(TAG, "onCreate: Current_DESTINATION : ${navController.currentDestination}")
    }

    override fun onBackPressed() {
        if (navController.currentDestination!!.displayName.equals("com.bluebellcspl.maarevacommoditytradingapp:id/dashboardFragment") || navController.currentDestination!!.displayName.equals(
                "com.bluebellcspl.maarevacommoditytradingapp:id/buyerDashboardFragment"
            ) || navController.currentDestination!!.displayName.equals("com.bluebellcspl.maarevacommoditytradingapp:id/PCADashboardFragment")
        ) {
            finishAffinity()
        } else {
            navController.navigateUp()
        }
    }

    fun setHomeDestination(usertype: String) {
        try {
            when (usertype) {
                "Buyer" -> {
                    navController.setGraph(R.navigation.my_nav)
                    Log.d(TAG, "setHomeDestination: BUYER_NAV_GRAPH")
                }

                "PCA" -> {
                    navController.setGraph(R.navigation.pca_nav)
                    Log.d(TAG, "setHomeDestination: PCA_NAV_GRAPH")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "setHomeDestination: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}