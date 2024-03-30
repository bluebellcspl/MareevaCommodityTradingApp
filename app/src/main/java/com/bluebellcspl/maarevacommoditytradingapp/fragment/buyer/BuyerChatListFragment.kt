package com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer

import ConnectionCheck
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.BuyerChatListAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentBuyerChatListBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchApprovedPCAListAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.AuctionDetailsModel
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionPCAListModel
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAListModel
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAListModelItem
import com.bluebellcspl.maarevacommoditytradingapp.model.UserChatInfoModel
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.RecyclerViewHelper

class BuyerChatListFragment : Fragment(),RecyclerViewHelper {
    lateinit var binding:FragmentBuyerChatListBinding
    private val commonUIUtility by lazy { CommonUIUtility(requireContext())}
    private val navController by lazy { findNavController() }
    val TAG="BuyerChatListFragment"
    lateinit var adapter:BuyerChatListAdapter
    lateinit var pcaList:PCAListModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_buyer_chat_list, container, false)
        if (ConnectionCheck.isConnected(requireContext()))
        {
            FetchApprovedPCAListAPI(
                requireContext(),
                requireActivity(),
                this@BuyerChatListFragment
            )
        }
        return binding.root
    }
    fun bindChatListView(dataList:ArrayList<PCAListModelItem>){
        try {
            pcaList = dataList as PCAListModel
            if (dataList.isNotEmpty())
            {
                adapter = BuyerChatListAdapter(requireContext(),dataList,this)
                binding.rcViewBuyerChatList.adapter = adapter
                binding.rcViewBuyerChatList.invalidate()
            }else
            {
                commonUIUtility.showToast("No Chat")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindChatListView: ${e.message}", )
        }
    }

    override fun onItemClick(postion: Int, onclickType: String) {
        val model = pcaList[postion]
        val userChatInfoModel = UserChatInfoModel(
            PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString(),
            model.PCARegId,
            PrefUtil.getString(PrefUtil.KEY_ROLE_ID,"").toString(),
            model.RoleId,
            model.PCAName,
            model.PCAShortName.toString(),
            model.GujaratiPCAName.toString(),
            model.GujaratiShortPCAName.toString(),
            model.ApprStatus,
            model.IsActive
        )
        Log.d(TAG, "onItemClick: CHAT_USER_MODEL : $userChatInfoModel")
        navController.navigate(BuyerChatListFragmentDirections.actionBuyerChatListFragmentToChatBoxFragment(userChatInfoModel))
    }

    override fun onBuyerAuctionPCAItemClick(postion: Int, model: AuctionDetailsModel) {
        TODO("Not yet implemented")
    }

    override fun getBuyerAuctionDataList(dataList: ArrayList<AuctionDetailsModel>) {
        TODO("Not yet implemented")
    }

    override fun getLiveAuctionPCAData(postion: Int, model: LiveAuctionPCAListModel) {
        TODO("Not yet implemented")
    }

}