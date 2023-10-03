package com.bluebellcspl.maarevacommoditytradingapp.recyclerViewHelper

import com.bluebellcspl.maarevacommoditytradingapp.model.Detail

interface RecyclerViewHelper {
    fun onItemClick(postion:Int,onclickType:String)
    fun onBuyerAuctionPCAItemClick(postion:Int,model:Detail)
}