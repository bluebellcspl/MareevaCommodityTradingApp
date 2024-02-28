package com.bluebellcspl.maarevacommoditytradingapp.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentProfileBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchApprovedPCAListAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchBuyerMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.BuyerMasterModelItem
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAListModelItem
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.bumptech.glide.Glide

class ProfileFragment : Fragment() {
    lateinit var binding: FragmentProfileBinding
    private val args by navArgs<ProfileFragmentArgs>()
    val TAG = "ProfileFragment"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        if (args.userType.equals("pca", true)) {
            FetchApprovedPCAListAPI(requireContext(),requireActivity(),this@ProfileFragment)
            binding.llBuyerViewProfileFragment.visibility = View.GONE
            binding.llPCAViewProfileFragment.visibility = View.VISIBLE
        } else {
            FetchBuyerMasterAPI(requireContext(), requireActivity(), this@ProfileFragment)
            binding.llBuyerViewProfileFragment.visibility = View.VISIBLE
            binding.llPCAViewProfileFragment.visibility = View.GONE
        }
        return binding.root
    }

    fun bindBuyerData(data: BuyerMasterModelItem?) {
        try {
            var IMG_URL = RetrofitHelper.IMG_BASE_URL + data?.ProfileImage
            Log.d(TAG, "bindBuyerData: IMG_URL : $IMG_URL")
            Glide.with(requireContext())
                .load(IMG_URL)
                .error(requireContext().getDrawable(R.drawable.baseline_person_24))
                .placeholder(requireContext().getDrawable(R.drawable.baseline_person_24))
                .into(binding.BuyerProfileImgProfileFragment)

            binding.tvBuyerNameProfileFragment.setText(data?.Name)
            binding.tvBuyerEmailProfileFragment.setText(data?.Email)
            binding.tvBuyerPhoneNoProfileFragment.setText(data?.MobileNo)
            binding.tvBuyerCommodityProfileFragment.setText(data?.CommodityName)
            binding.tvBuyerAadharCardProfileFragment.setText(data?.AdharCardNo)
            binding.tvBuyerPANCardProfileFragment.setText(data?.PanCardNo)
            binding.tvBuyerGSTNoProfileFragment.setText(data?.GSTNo)
            binding.tvBuyerOfficeAddressProfileFragment.setText(data?.OfficeAddress)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindBuyerData: ${e.message}")
        }
    }

    fun bindPCAData(data: PCAListModelItem?) {
        try {
            var IMG_URL = RetrofitHelper.IMG_BASE_URL + data?.ProfilePic
            Log.d(TAG, "bindPCAData: IMG_URL : $IMG_URL")
            Glide.with(requireContext())
                .load(IMG_URL)
                .error(requireContext().getDrawable(R.drawable.baseline_person_24))
                .placeholder(requireContext().getDrawable(R.drawable.baseline_person_24))
                .into(binding.PCAProfileImgProfileFragment)

            binding.tvPCANameProfileFragment.setText(data?.PCAName)
            binding.tvPCAEmailProfileFragment.setText(data?.EmailId)
            binding.tvPCAPhoneNoProfileFragment.setText(data?.PCAPhoneNumber)
            binding.tvPCAAPMCProfileFragment.setText(data?.APMCName)
            binding.tvPCAAadharCardProfileFragment.setText(data?.AdharNo)
            binding.tvPCAPANCardProfileFragment.setText(data?.PanCardNo)
            binding.tvPCAGSTNoProfileFragment.setText(data?.GSTNo)
            binding.tvPCAOfficeAddressProfileFragment.setText(data?.Address)
            binding.tvPCACityProfileFragment.setText(data?.CityName)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindBuyerData: ${e.message}")
        }
    }
}