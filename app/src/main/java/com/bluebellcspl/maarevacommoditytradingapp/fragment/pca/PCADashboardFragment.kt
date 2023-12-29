package com.bluebellcspl.maarevacommoditytradingapp.fragment.pca

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentPCADashboardBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchCityMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchCommodityMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchShopMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchTransportationMasterAPI


class PCADashboardFragment : Fragment() {
    lateinit var binding:FragmentPCADashboardBinding
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val navController by lazy { findNavController() }
    val TAG = "PCADashboardFragment"
    lateinit var menuHost: MenuHost
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_p_c_a_dashboard, container, false)
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        FetchCityMasterAPI(requireContext(),requireActivity())
        FetchTransportationMasterAPI(requireContext(),requireActivity())
        FetchCommodityMasterAPI(requireContext(), requireActivity())
        FetchShopMasterAPI(requireContext(), requireActivity())
        menuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.ds_menu,menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId){
                    R.id.btn_Profile->{
                        navController.navigate(PCADashboardFragmentDirections.actionPCADashboardFragmentToProfileOptionFragment())
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)
        setOnClickListeners()
        return binding.root
    }

    private fun setOnClickListeners() {
        try {
            binding.cvAuctionPCADashboardFragment.setOnClickListener {
                navController.navigate(PCADashboardFragmentDirections.actionPCADashboardFragmentToPCAAuctionFragment("Hello"))
            }
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "setOnClickListeners: ${e.message}", )
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(false)
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }
}