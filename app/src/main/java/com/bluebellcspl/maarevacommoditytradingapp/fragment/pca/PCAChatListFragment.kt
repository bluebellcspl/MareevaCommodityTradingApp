package com.bluebellcspl.maarevacommoditytradingapp.fragment.pca

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
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.database.DatabaseManager
import com.bluebellcspl.maarevacommoditytradingapp.database.Query
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentPCAChatListBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchChatRecipientAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.ChatRecipientModel
import com.bluebellcspl.maarevacommoditytradingapp.model.ChatRecipientModelItem
import com.bluebellcspl.maarevacommoditytradingapp.model.UserChatInfoModel
import com.google.android.material.badge.ExperimentalBadgeUtils


@ExperimentalBadgeUtils class PCAChatListFragment : Fragment() {
    val TAG = "PCAChatListFragment"
    lateinit var binding: FragmentPCAChatListBinding
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val navController by lazy { findNavController() }
    private lateinit var buyerData: ChatRecipientModelItem
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            com.bluebellcspl.maarevacommoditytradingapp.R.layout.fragment_p_c_a_chat_list,
            container,
            false
        )
        if (ConnectionCheck.isConnected(requireContext())) {
            FetchChatRecipientAPI(
                requireContext(),
                this@PCAChatListFragment
            )
        }
//        setBadge()
        clearNotification()
        setOnClickListeners()
        return binding.root
    }

    private fun setOnClickListeners() {
        try {
            binding.cvBuyerPCAChatListFragement.setOnClickListener {
                val userChatInfoModel = UserChatInfoModel(
                    PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString(),
                    buyerData.RegisterId,
                    PrefUtil.getString(PrefUtil.KEY_ROLE_ID, "").toString(),
                    buyerData.RoleId,
                    "",
                    buyerData.Name,
                    "",
                    "",
                    "",
                    ""
                )

                navController.navigate(
                    PCAChatListFragmentDirections.actionPCAChatListFragmentToChatBoxFragment(
                        userChatInfoModel
                    )
                )
            }

            binding.cvAdminPCAChatListFragement.setOnClickListener {
                val userChatInfoModel = UserChatInfoModel(
                    PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString(),
                    "1",
                    PrefUtil.getString(PrefUtil.KEY_ROLE_ID, "").toString(),
                    "1",
                    "",
                    "Admin",
                    "",
                    "",
                    "",
                    ""
                )

                navController.navigate(
                    PCAChatListFragmentDirections.actionPCAChatListFragmentToChatBoxFragment(
                        userChatInfoModel
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "setOnClickListeners: ${e.message}")
        }
    }

    fun bindBuyerData(data: ChatRecipientModel) {
        try {
            buyerData = data[0]
            if (data.isNotEmpty()) {
                binding.cvBuyerPCAChatListFragement.visibility = View.VISIBLE
                binding.tvBuyerNamePCAChatListFragement.setText(data[0].Name)
                binding.iconBuyerText.setText(getInitialLetter(data[0].Name).toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindBuyerData: ${e.message}")
        }
    }

    fun getInitialLetter(inputString: String): Char? {
        val words = inputString.trim().split("\\s+".toRegex())

        if (words.isNotEmpty()) {
            val firstWord = words[0]

            if (firstWord.isNotEmpty()) {
                return firstWord[0].toUpperCase()
            }
        }
        return null
    }

    //Clear Notification from Notification Shade
    private fun clearNotification() {
        try {
            val notificationManager =
                requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "clearNotification: ${e.message}")
        }
    }

    override fun onStop() {
        super.onStop()
        DatabaseManager.ExecuteQuery(Query.updateTMPChatNotificationStatus())
    }
}