package com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.ApprovedPCAListAdapter
import com.bluebellcspl.maarevacommoditytradingapp.adapter.UnapprovePCAListAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentPCAListBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchApprovedPCAListAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchUnapprovedPCAListAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAListModelItem


class PCAListFragment : Fragment() {
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val TAG = "PCAListFragment"
    private val navController by lazy { findNavController() }
    lateinit var binding: FragmentPCAListBinding
    lateinit var approvedListAdapter : ApprovedPCAListAdapter
    lateinit var unapprovedListAdapter : UnapprovePCAListAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_p_c_a_list, container, false)
        FetchApprovedPCAListAPI(requireContext(),requireActivity(),this)
        FetchUnapprovedPCAListAPI(requireContext(),requireActivity(),this)
        return binding.root
    }

    fun bindApprovedPCAListRecyclerView(approvedList: ArrayList<PCAListModelItem>) {
        commonUIUtility.showProgress()
        try {
            if (approvedList.isEmpty()) {
                commonUIUtility.dismissProgress()
                commonUIUtility.showToast("No Data Found Approved PCA")
                binding.rcViewApprovedPCAListFragment.visibility = View.GONE
            } else {
                approvedListAdapter = ApprovedPCAListAdapter(requireContext(), approvedList)
                binding.rcViewApprovedPCAListFragment.adapter = approvedListAdapter
//                val layoutAnimationController =
//                    AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_fall_down)
                commonUIUtility.dismissProgress()
//                binding.rcViewApprovedPCAListFragment.setLayoutAnimation(layoutAnimationController)
//                binding.rcViewApprovedPCAListFragment.scheduleLayoutAnimation()
                binding.rcViewApprovedPCAListFragment.invalidate()
            }
            binding.tvApprovedPCACountPCAListFragment.setText(approvedList.size.toString())
        } catch (e: Exception) {
            binding.tvApprovedPCACountPCAListFragment.visibility = View.GONE
            commonUIUtility.dismissProgress()
            e.printStackTrace()
            Log.e(TAG, "bindApprovedPCAListRecyclerView: ${e.message}")
        }
    }
    fun bindUnapprovedPCAListRecyclerView(approvedList: ArrayList<PCAListModelItem>) {
        commonUIUtility.showProgress()
        try {
            if (approvedList.isEmpty()) {
                commonUIUtility.dismissProgress()
                commonUIUtility.showToast("No Data Found Unapproved PCA")
                binding.rcViewUnapprovedPCAListFragment.visibility = View.GONE
            } else {
                unapprovedListAdapter = UnapprovePCAListAdapter(requireContext(), approvedList)
                binding.rcViewUnapprovedPCAListFragment.adapter = unapprovedListAdapter
                commonUIUtility.dismissProgress()
//                val layoutAnimationController =
//                    AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_fall_down)
//                commonUIUtility.dismissProgress()
//                binding.rcViewApprovedPCAListFragment.setLayoutAnimation(layoutAnimationController)
//                binding.rcViewApprovedPCAListFragment.scheduleLayoutAnimation()
                binding.rcViewUnapprovedPCAListFragment.invalidate()
            }
            binding.tvUnapprovedPCACountPCAListFragment.setText(approvedList.size.toString())
        } catch (e: Exception) {
            binding.tvUnapprovedPCACountPCAListFragment.visibility = View.GONE
            commonUIUtility.dismissProgress()
            e.printStackTrace()
            Log.e(TAG, "bindUnapprovedPCAListRecyclerView: ${e.message}")
        }
    }
}