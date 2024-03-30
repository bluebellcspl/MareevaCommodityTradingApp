package com.bluebellcspl.maarevacommoditytradingapp.fragment.pca

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentPCAChatListBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchBuyerMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.BuyerMasterModelItem
import com.bluebellcspl.maarevacommoditytradingapp.model.UserChatInfoModel

class PCAChatListFragment : Fragment() {
    val TAG = "PCAChatListFragment"
    lateinit var binding:FragmentPCAChatListBinding
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val navController by lazy { findNavController() }
    private lateinit var buyerData:BuyerMasterModelItem
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_p_c_a_chat_list, container, false)
        FetchBuyerMasterAPI(requireContext(),requireActivity(),this@PCAChatListFragment)
        setOnClickListeners()
        return binding.root
    }

    private fun setOnClickListeners() {
        try {
        binding.cvBuyerPCAChatListFragement.setOnClickListener {
            val userChatInfoModel = UserChatInfoModel(
                PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString(),
                buyerData.BuyerRegId,
                PrefUtil.getString(PrefUtil.KEY_ROLE_ID,"").toString(),
                buyerData.RoleId,
                "",
                buyerData.Name,
                "",
                "",
                "",
                buyerData.IsActive
            )

            navController.navigate(PCAChatListFragmentDirections.actionPCAChatListFragmentToChatBoxFragment(userChatInfoModel))
        }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "setOnClickListeners: ${e.message}", )
        }
    }

    fun bindBuyerData(data: BuyerMasterModelItem?) {
        try {
        buyerData = data!!
            binding.tvBuyerNamePCAChatListFragement.setText(data.Name)
            binding.iconBuyerText.setText(getInitialLetter(data.Name).toString())
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
}