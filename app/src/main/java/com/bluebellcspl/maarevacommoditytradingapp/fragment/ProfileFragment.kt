package com.bluebellcspl.maarevacommoditytradingapp.fragment

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bluebellcspl.maarevacommoditytradingapp.LoginActivity
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.FileDownloader
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentProfileBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchApprovedPCAListAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchBuyerMasterAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchPCAAuctionDetailAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.POSTDeleteBuyerAccountAPI
import com.bluebellcspl.maarevacommoditytradingapp.master.POSTDeletePCAAccountAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.BuyerMasterModelItem
import com.bluebellcspl.maarevacommoditytradingapp.model.PCAListModelItem
import com.bluebellcspl.maarevacommoditytradingapp.model.PostDeleteBuyerProfileModel
import com.bluebellcspl.maarevacommoditytradingapp.model.PostDeletePCAProfileModel
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper
import com.bumptech.glide.Glide

class ProfileFragment : Fragment() {
    var _binding: FragmentProfileBinding? = null
    val binding get() = _binding!!
    private val args by navArgs<ProfileFragmentArgs>()
    val TAG = "ProfileFragment"
    private val commonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val fileDownloader by lazy { FileDownloader.getInstance(requireContext()) }
    var isPCAAuctionLive: Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        if (args.userType.equals("pca", true)) {
            FetchApprovedPCAListAPI(requireContext(), requireActivity(), this@ProfileFragment)
            FetchPCAAuctionDetailAPI(requireContext(), requireActivity(), this)
            binding.llBuyerViewProfileFragment.visibility = View.GONE
            binding.llPCAViewProfileFragment.visibility = View.VISIBLE

            binding.btnPCADeleteAccountProfileFragment.setOnClickListener {
                if (isPCAAuctionLive) {
                    commonUIUtility.showAlertWithOkButton(getString(R.string.auction_in_progress_an_auction_has_been_started_so_you_cannot_delete_or_back_up_your_data_for_today))
                } else {
                    showDeleteAccDialog()
                }
            }
        } else {
            FetchBuyerMasterAPI(requireContext(), requireActivity(), this@ProfileFragment)
            binding.llBuyerViewProfileFragment.visibility = View.VISIBLE
            binding.llPCAViewProfileFragment.visibility = View.GONE

            binding.btnBuyerDeleteAccountProfileFragment.setOnClickListener {
                showDeleteAccDialog()
            }
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

    fun showDeleteAccDialog() {
        try {
            val alertDialogBuilder = AlertDialog.Builder(requireContext())
            alertDialogBuilder.setCancelable(true)
            alertDialogBuilder.setTitle(getString(R.string.alert_account_delete))
            alertDialogBuilder.setMessage(getString(R.string.account_delete_dialog_text))
            alertDialogBuilder.setNegativeButton(
                getString(R.string.cancel),
                DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface!!.dismiss()
                })
            alertDialogBuilder.setPositiveButton(requireContext().getString(R.string.delete),
                DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface!!.dismiss()
                    showBackUpDailog()
                })
            alertDialogBuilder.show()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "showDeleteAccDialog: ${e.message}")
        }
    }

    fun showBackUpDailog() {
        try {
            val alertDialogBuilder = AlertDialog.Builder(requireContext())
            alertDialogBuilder.setCancelable(true)
            alertDialogBuilder.setTitle(getString(R.string.backup_your_data))
            alertDialogBuilder.setMessage(getString(R.string.backup_dialog_text))
            alertDialogBuilder.setPositiveButton(getString(R.string.backup_n_delete),
                DialogInterface.OnClickListener { dialogInterface, i ->
//                    commonUIUtility.showBackupProgress()
//                    Handler(Looper.getMainLooper()).postDelayed(Runnable {
//                        commonUIUtility.dismissProgress()
//                    },10000)

                    if (PrefUtil.getString(PrefUtil.KEY_ROLE_NAME, "").toString().equals("PCA")) {
                        val postDeletePCAProfileModel = PostDeletePCAProfileModel(
                            "DeletePCA",
                            "" + PrefUtil.getString(PrefUtil.KEY_BUYER_ID, "").toString(),
                            "" + PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID, "").toString(),
                            "" + PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE, "").toString(),
                            "" + PrefUtil.getSystemLanguage(),
                            "" + PrefUtil.getString(PrefUtil.KEY_MOBILE_NO, "").toString(),
                            "" + PrefUtil.getString(PrefUtil.KEY_NAME, "").toString(),
                            "" + PrefUtil.getString(PrefUtil.KEY_REGISTER_ID, "").toString()
                        )

                        POSTDeletePCAAccountAPI(
                            requireContext(),
                            this@ProfileFragment,
                            postDeletePCAProfileModel
                        )
                    } else {
                        val postDeleteBuyerProfileModel = PostDeleteBuyerProfileModel(
                            ""+"DeleteBuyer",
                            ""+PrefUtil.getString(PrefUtil.KEY_MOBILE_NO,"").toString(),
                            ""+PrefUtil.getString(PrefUtil.KEY_NAME,"").toString(),
                            ""+PrefUtil.getString(PrefUtil.KEY_REGISTER_ID,"").toString(),
                            ""+PrefUtil.getString(PrefUtil.KEY_COMMODITY_ID,"").toString(),
                            ""+PrefUtil.getString(PrefUtil.KEY_COMPANY_CODE,"").toString(),
                            ""+PrefUtil.getSystemLanguage()
                        )

                        POSTDeleteBuyerAccountAPI(requireContext(),this@ProfileFragment,postDeleteBuyerProfileModel)
                    }
                    dialogInterface!!.dismiss()
                })
            alertDialogBuilder.show()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "showBackUpDailog: ${e.message}")
        }
    }

    fun downloadBackup(fileUrl: String) {
        try {
            // commonUIUtility.showBackupProgress()
            val fileName =
                "Maareva_Backup_${PrefUtil.getString(PrefUtil.KEY_NAME, "").toString()}.zip"
            val desc = "Downloading Backup"
            fileDownloader.downloadZipFile(fileUrl, fileName, desc, this@ProfileFragment)
        } catch (e: Exception) {
            commonUIUtility.dismissProgress()
            e.printStackTrace()
            Log.e(TAG, "downloadPCABackup: ${e.message}")
        }
    }

    fun logout() {
//        commonUIUtility.dismissProgress()
        PrefUtil.setBoolean(PrefUtil.KEY_LOGGEDIN, false)
        requireActivity().startActivity(Intent(activity, LoginActivity::class.java))
        requireActivity().finish()
    }

    fun pcaAuctionLiveCheck(isAuctionLive: Boolean) {
        isPCAAuctionLive = isAuctionLive
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}