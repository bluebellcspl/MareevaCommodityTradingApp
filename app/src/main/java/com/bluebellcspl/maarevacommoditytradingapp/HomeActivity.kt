package com.bluebellcspl.maarevacommoditytradingapp

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.transition.Explode
import android.util.Log
import android.util.TypedValue
import android.view.ContextThemeWrapper
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
import com.google.android.material.appbar.MaterialToolbar
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
//        applyToolbarTheme(binding.toolbarHome.toolbar,PrefUtil.getString(PrefUtil.KEY_ROLE_NAME,"").toString())
//        setAppTheme(PrefUtil.getString(PrefUtil.KEY_ROLE_NAME,"").toString())
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
            ) || navController.currentDestination!!.displayName.equals("com.bluebellcspl.maarevacommoditytradingapp:id/PCADashboardFragment")||navController.currentDestination!!.displayName.equals("com.bluebellcspl.maarevacommoditytradingapp:id/indPCADashboardFragment")
        ) {
            finishAffinity()
        } else {
            navController.navigateUp()
        }
    }

    fun setHomeDestination(usertype: String) {
        try {
            when (usertype) {
                PrefUtil.KEY_ROLE_BUYER -> {

                    navController.setGraph(R.navigation.my_nav)
                    Log.d(TAG, "setHomeDestination: BUYER_NAV_GRAPH")
                }

                PrefUtil.KEY_ROLE_PCA -> {
                    navController.setGraph(R.navigation.pca_nav)
                    Log.d(TAG, "setHomeDestination: PCA_NAV_GRAPH")
                }
                PrefUtil.KEY_ROLE_IND_PCA -> {
                    navController.setGraph(R.navigation.ind_pca_nav)
                    Log.d(TAG, "setHomeDestination: IND_PCA_NAV_GRAPH")
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

    private fun applyToolbarTheme(toolbar: MaterialToolbar, userType: String) {
        val toolbarTheme = when (userType) {
            "Buyer" -> {
                R.style.ToolbarTheme_Buyer
            }
            "PCA" -> R.style.ToolbarTheme_PCA
            "IndividualPCA" -> R.style.ToolbarTheme_IndPCA
            else -> {
                R.style.ToolbarTheme_PCA
            }
        }

        // Wrap the toolbar with the new theme.
        val themedContext: Context = ContextThemeWrapper(this, toolbarTheme)

        // Get the colorPrimary from the current theme.
        val colorPrimary = getThemeColor(themedContext, androidx.appcompat.R.attr.colorPrimary)

        // Apply the color to the toolbar background.
        toolbar.setBackgroundColor(colorPrimary)

        // Set the status bar color to match the toolbar.
        window.statusBarColor = colorPrimary

        toolbar.apply {
            setBackgroundColor(getThemeColor(themedContext, androidx.appcompat.R.attr.colorPrimary))
            popupTheme = toolbarTheme
        }
    }

    private fun setAppTheme(userType: String) {
        val theme = when (userType) {
            "Buyer" -> R.style.MaarevaTheme_Buyer
            "PCA" -> R.style.MaarevaTheme_PCA
            "IndividualPCA" -> R.style.MaarevaTheme_IndPCA
            else -> {R.style.MaarevaTheme_PCA}
        }
        Log.d(TAG, "setAppTheme: CURRENT_THEME : $theme")
        setTheme(theme)
    }

    private fun getThemeColor(context: Context, attrRes: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue.data
    }
}