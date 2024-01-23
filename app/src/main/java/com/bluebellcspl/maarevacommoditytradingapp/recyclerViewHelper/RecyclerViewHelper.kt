package com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper

import com.bluebellcspl.maarevacommoditytradingapp.model.AuctionDetailsModel
import com.bluebellcspl.maarevacommoditytradingapp.model.Detail
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionMasterModel
import com.bluebellcspl.maarevacommoditytradingapp.model.LiveAuctionPCAListModel

interface RecyclerViewHelper {
    fun onItemClick(postion:Int,onclickType:String)
    fun onBuyerAuctionPCAItemClick(postion:Int,model:AuctionDetailsModel)
    fun getBuyerAuctionDataList(dataList:ArrayList<AuctionDetailsModel>)

    fun getLiveAuctionPCAData(postion:Int,model: LiveAuctionPCAListModel)

}