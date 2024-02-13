package com.bluebellcspl.maarevacommoditytradingapp.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.NotificationAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentNotificationBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.NotificationRTRMasterModelItem


class NotificationFragment : Fragment() {
    lateinit var binding: FragmentNotificationBinding
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val navController by lazy { findNavController() }
    val TAG = "NotificationFragment"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_notification, container, false)
        DatabaseManager.ExecuteQuery(Query.updateNotificationSeenStatus())
        val notificationList = getNotificationFromDB()
        bindNotificationList(notificationList)
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
                            cursor.getString(cursor.getColumnIndexOrThrow("ISSeen")),
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

    fun bindNotificationList(notificationList:ArrayList<NotificationRTRMasterModelItem>){
        try {
            if (notificationList.size>0)
            {
                val adapter = NotificationAdapter(requireContext(),notificationList)
                binding.rcViewNotificationFragment.adapter = adapter
                binding.rcViewNotificationFragment.invalidate()
            }else
            {
                commonUIUtility.showToast("No Notifications")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindNotificationList: ${e.message}", )
        }
    }
}