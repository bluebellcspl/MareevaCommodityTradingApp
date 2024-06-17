package com.bluebellcspl.maarevacommoditytradingapp.fragment.buyer

import ConnectionCheck
import android.app.AlertDialog
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
import com.bluebellcspl.maarevacommoditytradingapp.databinding.PcaProfileDialogPopupBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchApprovedPCAListAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.AuctionDetailsModel
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionPCAListModel
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAListModelItem
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.RecyclerViewHelper
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.bumptech.glide.Glide


class PCAListFragment : Fragment(),RecyclerViewHelper {
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val TAG = "PCAListFragment"
    private val navController by lazy { findNavController() }
    var _binding: FragmentPCAListBinding? = null
    val binding get() = _binding!!
    lateinit var approvedListAdapter : ApprovedPCAListAdapter
    lateinit var unapprovedListAdapter : UnapprovePCAListAdapter
    lateinit var approvedPCAList : ArrayList<PCAListModelItem>
    lateinit var unapprovedPCAList : ArrayList<PCAListModelItem>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_p_c_a_list, container, false)
        if (ConnectionCheck.isConnected(requireContext()))
        {
            FetchApprovedPCAListAPI(requireContext(),requireActivity(),this)
        }else{
            commonUIUtility.showToast(getString(R.string.no_internet_connection))
        }
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
                approvedListAdapter = ApprovedPCAListAdapter(requireContext(), approvedList,this)
                binding.rcViewApprovedPCAListFragment.adapter = approvedListAdapter
//                val layoutAnimationController =
//                    AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_fall_down)
                commonUIUtility.dismissProgress()
//                binding.rcViewApprovedPCAListFragment.setLayoutAnimation(layoutAnimationController)
//                binding.rcViewApprovedPCAListFragment.scheduleLayoutAnimation()
                binding.rcViewApprovedPCAListFragment.invalidate()
            }
            binding.tvApprovedPCACountPCAListFragment.setText(approvedList.size.toString())
            approvedPCAList = approvedList
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
                unapprovedListAdapter = UnapprovePCAListAdapter(requireContext(), approvedList,this)
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
            unapprovedPCAList = approvedList
        } catch (e: Exception) {
            binding.tvUnapprovedPCACountPCAListFragment.visibility = View.GONE
            commonUIUtility.dismissProgress()
            e.printStackTrace()
            Log.e(TAG, "bindUnapprovedPCAListRecyclerView: ${e.message}")
        }
    }

    override fun onItemClick(postion: Int, onclickType: String) {
        try {
            if (onclickType.equals("ApprovedList"))
            {
                var model = approvedPCAList[postion]
                showPCAProfileDialogPopup(model,"ApprovedList")

            }else
            {
                var model = unapprovedPCAList[postion]
                showPCAProfileDialogPopup(model,"UnapprovedList")

            }
        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "onItemClick: ${e.message}")
        }
    }

    override fun onBuyerAuctionPCAItemClick(postion: Int, model: AuctionDetailsModel) {

    }

    override fun getBuyerAuctionDataList(dataList: ArrayList<AuctionDetailsModel>) {

    }

    override fun getLiveAuctionPCAData(postion: Int, model: LiveAuctionPCAListModel) {
        TODO("Not yet implemented")
    }

    fun showPCAProfileDialogPopup(model:PCAListModelItem,onclickType: String){
        try {
            val alertDailogBuilder = AlertDialog.Builder(requireContext())
            val dialogBinding = PcaProfileDialogPopupBinding.inflate(layoutInflater)
            val dialogView = dialogBinding.root
            alertDailogBuilder.setView(dialogView)
            val alertDialog = alertDailogBuilder.create()
            alertDialog.setCanceledOnTouchOutside(true)
            alertDialog.setCancelable(true)
            alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()

            var IMG_URL = RetrofitHelper.IMG_BASE_URL + model.ProfilePic
            Log.d(TAG, "bindPCAData: IMG_URL : $IMG_URL")
            Glide.with(requireContext())
                .load(IMG_URL)
                .error(requireContext().getDrawable(R.drawable.baseline_person_24))
                .placeholder(requireContext().getDrawable(R.drawable.baseline_person_24))
                .into(dialogBinding.PCAProfileImgProfilePCADialog)

            dialogBinding.tvPCANameProfilePCADialog.setText(model.PCAName)
            dialogBinding.tvPCAEmailProfilePCADialog.setText(model.EmailId)
            dialogBinding.tvPCAAPMCProfilePCADialog.setText(model.APMCName)
            dialogBinding.tvPCAPhoneNoProfilePCADialog.setText(model.PCAPhoneNumber)
            dialogBinding.tvPCAAadharCardProfilePCADialog.setText(model.AdharNo)
            dialogBinding.tvPCAPANCardProfilePCADialog.setText(model.PanCardNo)
            dialogBinding.tvPCAGSTNoProfilePCADialog.setText(model.GSTNo)
            dialogBinding.tvPCAOfficeAddressProfilePCADialog.setText(model.Address)
            dialogBinding.tvPCACityProfilePCADialog.setText(model.CityName)

            dialogBinding.fabEditProfilePCADialog.setOnClickListener {
                if (onclickType.equals("ApprovedList"))
                {
                    alertDialog.dismiss()
//                    model.GujaratiShortPCAName=""
                    Log.d(TAG, "showPCAProfileDialogPopup: PCA_MODEL_FOR_EDIT : $model")
                    navController.navigate(PCAListFragmentDirections.actionPCAListFragmentToEditPCAFragment(model))
                }else
                {
                    alertDialog.dismiss()
//                    model.GujaratiShortPCAName=""
                    Log.d(TAG, "showPCAProfileDialogPopup: PCA_MODEL_FOR_EDIT : $model")
                    navController.navigate(PCAListFragmentDirections.actionPCAListFragmentToEditPCAFragment(model))
                }
            }

        }catch (e:Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "showPCAProfileDialogPopup: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}