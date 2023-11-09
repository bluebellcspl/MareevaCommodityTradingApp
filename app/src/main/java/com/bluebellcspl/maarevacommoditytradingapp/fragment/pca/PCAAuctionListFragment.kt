package com.bluebellcspl.maarevacommoditytradingapp.fragment.pca

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.PCAAuctionListAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentPCAAuctionListBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.ApiPCAAuctionDetail
import com.bluebellcspl.maarevacommoditytradingapp.model.AuctionDetailsModel
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.RecyclerViewHelper

class PCAAuctionListFragment : Fragment(),RecyclerViewHelper {
    lateinit var binding:FragmentPCAAuctionListBinding
    private val commonUIUtility:CommonUIUtility by lazy { CommonUIUtility(requireContext())}
    val TAG = "PCAAuctionListFragment"
    private val navController by lazy { findNavController() }
    val args by navArgs<PCAAuctionListFragmentArgs>()
    lateinit var adapter:PCAAuctionListAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_p_c_a_auction_list, container, false)
        binding.rcViewPCAAuctionListFrament.layoutManager = LinearLayoutManager(requireContext())
        bindAuctionList(args.pcaAuctionDetailModel.ApiPCAAuctionDetail)
        return binding.root
    }
    private fun bindAuctionList(dataList: ArrayList<ApiPCAAuctionDetail>){
        if (dataList.isNotEmpty())
        {
            adapter = PCAAuctionListAdapter(requireContext(),dataList,this)
        }else
        {
            commonUIUtility.showToast("No Auction List!")
        }
    }

    override fun onItemClick(postion: Int, onclickType: String) {

    }

    override fun onBuyerAuctionPCAItemClick(postion: Int, model: AuctionDetailsModel) {

    }

    override fun getBuyerAuctionDataList(dataList: ArrayList<AuctionDetailsModel>) {

    }
}