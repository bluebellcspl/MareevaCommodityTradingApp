package com.bluebellcspl.maarevacommoditytradingapp.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.dashboardHelper.BuyerDashboard
import com.bluebellcspl.maarevacommoditytradingapp.dashboardHelper.PCADashboard
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {
    lateinit var binding:FragmentDashboardBinding
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val TAG = "DashboardFragment"
    lateinit var CURRENT_USER:String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_dashboard, container, false)
        Log.d(TAG, "onCreateView: CURRENT_USER_ROLE : ${PrefUtil.getString(PrefUtil.KEY_ROLE_NAME,"")}")
        CURRENT_USER = PrefUtil.getString(PrefUtil.KEY_ROLE_NAME,"").toString()

        if (CURRENT_USER.equals("Buyer",true))
        {
            binding.buyerDashboard.root.visibility = View.VISIBLE
            binding.pcaDashboard.root.visibility = View.GONE
            BuyerDashboard(requireContext(),requireActivity(),binding)
        }else if (CURRENT_USER.equals("PCA",true))
        {
            binding.pcaDashboard.root.visibility = View.VISIBLE
            binding.buyerDashboard.root.visibility = View.GONE
            PCADashboard(requireContext(),requireActivity(),binding)
        }
        else if (CURRENT_USER.equals("Admin",true))
        {
            binding.buyerDashboard.root.visibility = View.GONE
            binding.pcaDashboard.root.visibility = View.GONE
            PCADashboard(requireContext(),requireActivity(),binding)
        }

        return binding.root
    }

}