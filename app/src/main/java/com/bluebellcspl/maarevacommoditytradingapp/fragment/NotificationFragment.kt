package com.bluebellcspl.maarevacommoditytradingapp.fragment

import ConnectionCheck
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.NotificationAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentNotificationBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchNotificationAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchNotificationPageWiseAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.POSTSeenNotificationAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.NotificationRTRMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.NotificationRTRMasterModelItem
import com.bluebellcspl.maarevacommoditytradingapp.model.POSTSeenNotificationModel


class NotificationFragment : Fragment() {
    lateinit var binding: FragmentNotificationBinding
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val navController by lazy { findNavController() }
    val TAG = "NotificationFragment"
    lateinit var filter: IntentFilter
    var PAGE=1
    var ITEM_COUNT = 10
    private var isLoading = false
    private var isLastPage = false
    lateinit var adapter: NotificationAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_notification, container, false)
        filter = IntentFilter("ACTION_NOTIFICATION_RECEIVED")
        clearNotification()
        adapter = NotificationAdapter(requireContext())
        binding.nestedScrollViewNotificationFragment.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            if (!isLoading && !isLastPage) {
                if (scrollY == binding.nestedScrollViewNotificationFragment.getChildAt(0).measuredHeight - binding.nestedScrollViewNotificationFragment.measuredHeight) {
//                    binding.progressNotification.visibility = View.VISIBLE
                    PAGE++
                    // Load more data
                    if (ConnectionCheck.isConnected(requireContext()))
                    {
                        FetchNotificationPageWiseAPI(requireContext(),this@NotificationFragment,PAGE,ITEM_COUNT)
                    }
                }
            }
        })
        if (ConnectionCheck.isConnected(requireContext()))
        {
//            FetchNotificationAPI(requireContext(),this@NotificationFragment)
            FetchNotificationPageWiseAPI(requireContext(),this@NotificationFragment,PAGE,ITEM_COUNT)
        }
//        else
//        {
//            bindNotificationList()
//        }


        // Load initial data

        return binding.root
    }

    fun getNotificationFromDB(): ArrayList<NotificationRTRMasterModelItem> {
        var dataList = ArrayList<NotificationRTRMasterModelItem>()
        try {
            val cursor = DatabaseManager.ExecuteRawSql(Query.getAllNotification())
            if (cursor != null && cursor.count > 0) {
                dataList.clear()
                while (cursor.moveToNext()) {
                    dataList.add(
                        NotificationRTRMasterModelItem(
                            cursor.getString(cursor.getColumnIndexOrThrow("Cdate")),
                            cursor.getString(cursor.getColumnIndexOrThrow("CreateUser")),
//                            cursor.getString(cursor.getColumnIndexOrThrow("FromRoleId")),
                            "",
                            cursor.getString(cursor.getColumnIndexOrThrow("FullMsg")),
                            cursor.getString(cursor.getColumnIndexOrThrow("ISRead")),
//                            cursor.getString(cursor.getColumnIndexOrThrow("ISSeen")),
                            "",
                            cursor.getString(cursor.getColumnIndexOrThrow("Link")),
                            cursor.getString(cursor.getColumnIndexOrThrow("Name")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("NotificationId")),
                            cursor.getString(cursor.getColumnIndexOrThrow("RoleName")),
                            cursor.getString(cursor.getColumnIndexOrThrow("ShortMsg")),
                            cursor.getString(cursor.getColumnIndexOrThrow("ToRoleId")),
                            cursor.getString(cursor.getColumnIndexOrThrow("ToUserId"))
                        )
                    )
                }
                Log.d(TAG, "getNotificationFromDB: NOTIFICATION_LIST_SIZE : ${dataList.toString()}")
            }
        } catch (e: Exception) {
            dataList.clear()
            Log.e(TAG, "getNotificationFromDB: ${e.message}")
            e.printStackTrace()
        }
        return dataList
    }

    fun bindNotificationList(){
        try {
            val notificationList = getNotificationFromDB()
            if (notificationList.size>0)
            {
//                val adapter = NotificationAdapter(requireContext(),notificationList)
                binding.rcViewNotificationFragment.adapter = adapter
                adapter.submitList(notificationList)
            }else
            {
                commonUIUtility.showToast("No Notifications")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindNotificationList: ${e.message}", )
        }
    }

    private fun clearNotification(){
        try {
            val notificationManager = requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "clearNotification: ${e.message}", )
        }
    }

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            FetchNotificationAPI(requireContext(),this@NotificationFragment)
        }
    }

    override fun onStart() {
        super.onStart()
        requireContext().registerReceiver(notificationReceiver,filter)
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(notificationReceiver)
        clearNotification()
        val unSeenNotificationList = getUnseenNotification()
        if (ConnectionCheck.isConnected(requireContext()))
        {
            POSTSeenNotificationAPI(requireContext(),this@NotificationFragment,unSeenNotificationList)
        }
        DatabaseManager.ExecuteQuery(Query.updateNotificationSeenStatus())
        DatabaseManager.ExecuteQuery(Query.updateTMPNotificationSeenStatus())

    }

    private fun getUnseenNotification():ArrayList<POSTSeenNotificationModel>{
        val dataList = ArrayList<POSTSeenNotificationModel>()
        try {
            val cursor = DatabaseManager.ExecuteRawSql(Query.getUnseenNotification())
            if (cursor!= null && cursor.count>0)
            {
                while (cursor.moveToNext())
                {
                    val model = POSTSeenNotificationModel(cursor.getString(cursor.getColumnIndexOrThrow("NotificationId")))
                    dataList.add(model)
                }
                Log.d(TAG, "getUnseenNotification: UNSEEN_NOTIFICATION_LIST_COUNT : ${dataList.size}")
            }
        } catch (e: Exception) {
            dataList.clear()
            e.printStackTrace()
            Log.e(TAG, "getUnseenNotification: ${e.message}")
        }
        return dataList
    }

    fun newBindNotificationList(notificationModel: NotificationRTRMasterModel) {
        try {
            binding.rcViewNotificationFragment.adapter = adapter
            adapter.addNewData(notificationModel)
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "newBindNotificationList: ${e.message}")
        }
    }

}