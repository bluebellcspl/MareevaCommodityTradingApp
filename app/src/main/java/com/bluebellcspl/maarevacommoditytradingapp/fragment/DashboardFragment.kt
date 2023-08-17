package com.bluebellcspl.maarevacommoditytradingapp.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.LoginActivity
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.dashboardHelper.BuyerDashboard
import com.bluebellcspl.maarevacommoditytradingapp.dashboardHelper.PCADashboard
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentDashboardBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchApprovedPCAListAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAListModelItem

class DashboardFragment : Fragment() {
    lateinit var binding:FragmentDashboardBinding
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val TAG = "DashboardFragment"
    lateinit var navController: NavController
    lateinit var menuHost: MenuHost
    lateinit var CURRENT_USER:String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_dashboard, container, false)
        Log.d(TAG, "onCreateView: CURRENT_USER_ROLE : ${PrefUtil.getString(PrefUtil.KEY_ROLE_NAME,"")}")
        CURRENT_USER = PrefUtil.getString(PrefUtil.KEY_ROLE_NAME,"").toString()
        navController = findNavController()
        FetchApprovedPCAListAPI(requireContext(),requireActivity(),this)
        if (CURRENT_USER.equals("Buyer",true))
        {
            binding.buyerDashboard.root.visibility = View.VISIBLE
            binding.pcaDashboard.root.visibility = View.GONE
            BuyerDashboard(requireContext(),requireActivity(),this)
        }else if (CURRENT_USER.equals("PCA",true))
        {
            binding.pcaDashboard.root.visibility = View.VISIBLE
            binding.buyerDashboard.root.visibility = View.GONE
            PCADashboard(requireContext(),requireActivity(),this)
        }
        else if (CURRENT_USER.equals("Admin",true))
        {
            binding.buyerDashboard.root.visibility = View.GONE
            binding.pcaDashboard.root.visibility = View.GONE
            BuyerDashboard(requireContext(),requireActivity(),this)
        }

        menuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.ds_menu,menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId){
                    R.id.btn_Logout->logoutDialog()
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)

        return binding.root
    }

    fun logoutDialog() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setCancelable(false)
        alertDialog.setTitle("Logout")
        alertDialog.setMessage("Do you want to Logout?")
        alertDialog.setPositiveButton("Yes", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                p0!!.dismiss()
                PrefUtil.deletePreference()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finish()
            }
        })
        alertDialog.setNegativeButton("No", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                p0!!.dismiss()
            }
        })
        alertDialog.show()
    }

    fun bindingApprovedPCACount(dataList:ArrayList<PCAListModelItem>){
        try {
                binding.buyerDashboard.tvPCACountBuyer.setText(dataList.size.toString())
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "bindingApprovedPCACount: ${e.message}", )
        }
    }

}