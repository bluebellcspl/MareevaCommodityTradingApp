package com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer

import ConnectionCheck
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.BuyerChatListAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentBuyerChatListBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchChatRecipientAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.AuctionDetailsModel
import com.bluebellcspl.maarevacommoditytradingapp.model.ChatRecipientModel
import com.bluebellcspl.maarevacommoditytradingapp.model.ChatRecipientModelItem
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionPCAListModel
import com.bluebellcspl.maarevacommoditytradingapp.model.UserChatInfoModel
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.RecyclerViewHelper

class BuyerChatListFragment : Fragment(),RecyclerViewHelper {
    lateinit var binding:FragmentBuyerChatListBinding
    private val commonUIUtility by lazy { CommonUIUtility(requireContext())}
    private val navController by lazy { findNavController() }
    val TAG="BuyerChatListFragment"
    lateinit var adapter:BuyerChatListAdapter
    lateinit var pcaList:ArrayList<ChatRecipientModelItem>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_buyer_chat_list, container, false)
        clearNotification()
        if (ConnectionCheck.isConnected(requireContext()))
        {
            FetchChatRecipientAPI(
                requireContext(),
                this@BuyerChatListFragment
            )
        }

        binding.cvAdminBuyerChatList.setOnClickListener {
            val userChatInfoModel = UserChatInfoModel(
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString(),
                "1",
                PrefUtil.getString(PrefUtil.KEY_ROLE_ID,"").toString(),
                "1",
                "",
                "Admin",
                "",
                "",
                "",
                ""
            )

            navController.navigate(BuyerChatListFragmentDirections.actionBuyerChatListFragmentToChatBoxFragment(userChatInfoModel))
        }
        return binding.root
    }
    fun bindChatListView(dataList:ChatRecipientModel){
        try {
            pcaList = dataList
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
            model.RegisterId,
            PrefUtil.getString(PrefUtil.KEY_ROLE_ID,"").toString(),
            model.RoleId,
            model.Name,
            model.ShortName.toString(),
            model.GujName.toString(),
            model.GujShortName.toString(),
            "",
            ""
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

    //Clear Notifications from Notification Shade
    private fun clearNotification(){
        try {
            val notificationManager = requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "clearNotification: ${e.message}", )
        }
    }

    //Chat Notification Badge from Dashboard

    override fun onStop() {
        super.onStop()
        DatabaseManager.ExecuteQuery(Query.updateTMPChatNotificationStatus())

    }
}