package com.bluebellcspl.maarevacommoditytradingapp.fragment.IndividualPca

import ConnectionCheck
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.adapter.IndPCAAuctionBuyerWiseListAdapter
import com.bluebellcspl.maarevacommoditytradingapp.adapter.IndPCAAuctionListAdapter
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentIndPCAAuctionBuyerListBinding
import com.bluebellcspl.maarevacommoditytradingapp.master.FetchIndPCAAuctionAPI
import com.bluebellcspl.maarevacommoditytradingapp.model.ApiIndividualPCAAuctionDetail
import com.bluebellcspl.maarevacommoditytradingapp.model.AuctionDetailsModel
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCAAuctionFetchModel
import com.bluebellcspl.maarevacommoditytradingapp.model.IndPCABuyerWiseAuctionModel
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionPCAListModel
import com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper.RecyclerViewHelper


class IndPCAAuctionBuyerListFragment : Fragment(), RecyclerViewHelper {
    var _binding: FragmentIndPCAAuctionBuyerListBinding? = null
    val binding get() = _binding!!
    private val commonUIUtility: CommonUIUtility by lazy { CommonUIUtility(requireContext()) }
    private val navController by lazy { findNavController() }
    val TAG = "IndPCAAuctionBuyerListFragment"
    lateinit var adapter: IndPCAAuctionBuyerWiseListAdapter
    lateinit var auctionModel: IndPCAAuctionFetchModel
    var buyerWiseAuctionList = ArrayList<IndPCABuyerWiseAuctionModel>()
    var SELECTED_BUYER_ID = ""
    var SELECTED_BUYER_NAME = ""
    var COMMODITY_BHARTI_PRICE = "0"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
         _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_ind_p_c_a_auction_buyer_list, container, false)
        binding.rcViewBuyerAuctionListIndPCAAuctionBuyerListFragment.layoutManager = LinearLayoutManager(requireContext())

        if (ConnectionCheck.isConnected(requireContext()))
        {
            FetchIndPCAAuctionAPI(requireContext(),this@IndPCAAuctionBuyerListFragment)
        }else
        {
            commonUIUtility.showToast(requireContext().getString(R.string.no_internet_connection))
        }

        Log.d("??", "onCreateView: ON_CREATE")
        return binding.root
    }
    fun bindAuctionList(model:IndPCAAuctionFetchModel){
        try {
            auctionModel = model
            COMMODITY_BHARTI_PRICE = model.CommodityBhartiPrice
            if (auctionModel.ApiIndividualPCAAuctionDetail.isEmpty()){
                commonUIUtility.showToast(requireContext().getString(R.string.no_data_found))
            }else
            {
                buyerWiseAuctionList = bindBuyerAuctionList(auctionModel.ApiIndividualPCAAuctionDetail)
                adapter = IndPCAAuctionBuyerWiseListAdapter(requireContext(),buyerWiseAuctionList,this)
                binding.rcViewBuyerAuctionListIndPCAAuctionBuyerListFragment.adapter = adapter
                binding.rcViewBuyerAuctionListIndPCAAuctionBuyerListFragment.invalidate()
            }
        }catch (e:Exception)
        {
            commonUIUtility.showToast(requireContext().getString(R.string.please_try_again_later_alert_msg))
            e.printStackTrace()
            Log.e(TAG, "bindAuctionList: ${e.message}")
        }

    }

    private fun bindBuyerAuctionList(datalist:ArrayList<ApiIndividualPCAAuctionDetail>):ArrayList<IndPCABuyerWiseAuctionModel>{
        var buyerList = ArrayList<IndPCABuyerWiseAuctionModel>()
        try {

            datalist.forEach { model ->
                val buyerModel = IndPCABuyerWiseAuctionModel(
                    model.BuyerId,
                    model.BuyerName,
                    model.CurrentPrice,
                    model.Bags,
                    model.Amount
                )
                buyerList.add(buyerModel)
            }

            buyerList = mergeBuyers(buyerList)
            return buyerList
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "bindBuyerAuctionList: ${e.message}")
            buyerList.clear()
            return buyerList
        }
    }

    fun mergeBuyers(buyers: ArrayList<IndPCABuyerWiseAuctionModel>): ArrayList<IndPCABuyerWiseAuctionModel> {
        val mergedMap = mutableMapOf<String, IndPCABuyerWiseAuctionModel>()

        for (buyer in buyers) {
            // Check if the buyer already exists in the map

                //Formula for Avg Rate
//            post_AvgPrice = (cumulativeTotal + total) / (((totalPurchasedBags + bags.toFloat()) * commodityBhartiRate.toDouble()) / 20)

            if (mergedMap.containsKey(buyer.BuyerName)) {
                // If exists, sum up the values
                val existingBuyer = mergedMap[buyer.BuyerName]!!
                existingBuyer.Bags = (existingBuyer.Bags.toDouble() + buyer.Bags.toDouble()).toString()
                existingBuyer.Total = (existingBuyer.Total.toDouble() + buyer.Total.toDouble()).toString()
                existingBuyer.Rate = (existingBuyer.Total.toDouble() / ((existingBuyer.Bags.toDouble() * COMMODITY_BHARTI_PRICE.toDouble())/20)).toString()
            } else {
                // If not exists, add to the map
                mergedMap[buyer.BuyerName] = IndPCABuyerWiseAuctionModel(
                    BuyerId = buyer.BuyerId, // Assuming BuyerId is unique for each entry
                    BuyerName = buyer.BuyerName,
                    Rate = (buyer.Total.toDouble() / ((buyer.Bags.toDouble() * COMMODITY_BHARTI_PRICE.toDouble())/20)).toString(),
                    Bags = buyer.Bags,
                    Total = buyer.Total
                )
            }
        }
        // Return the merged list as an ArrayList
        return ArrayList(mergedMap.values)
    }


    override fun onItemClick(postion: Int, onclickType: String) {
        Log.d(TAG, "onItemClick: CLICKED_POSITION : $postion")
        val model = buyerWiseAuctionList[postion]
        Log.d(TAG, "onItemClick: CLICKED_BUYER : ${model.BuyerName}")
        Log.d(TAG, "onItemClick: CLICKED_BUYER_ID : ${model.BuyerId}")
        Log.d(TAG, "onItemClick: CLICKED_BUYER_BAGS : ${model.Bags}")
        Log.d(TAG, "onItemClick: CLICKED_BUYER_RATE : ${model.Rate}")
        Log.d(TAG, "onItemClick: CLICKED_BUYER_TOTAL : ${model.Total}")

        navController.navigate(IndPCAAuctionBuyerListFragmentDirections.actionIndPCAAuctionBuyerListFragmentToIndPCAAuctionListFragment(model))
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

    override fun onResume() {
        super.onResume()
        Log.d("??", "onResume: ON_RESUME")
    }

    override fun onStop() {
        super.onStop()
        Log.d("??", "onStop: ON_STOP")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("??", "onDestroy: ON_DESTROY")
//        _binding = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("??", "onDestroyView: ON_DESTROY_VIEW")
        _binding = null
    }
}