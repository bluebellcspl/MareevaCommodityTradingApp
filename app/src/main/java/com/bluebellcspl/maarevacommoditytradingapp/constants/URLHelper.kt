package com.bluebellcspl.maarevacommoditytradingapp.constants

import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.DateUtility

class URLHelper {
    companion object{
        val BASE_URL_PROJECT = "https://maareva.bbcspldev.in/"
        val BASE_URL_API = "https://maarevaapi.bbcspldev.in/"

        val BUYER_AUCTION_DETAIL_REPORT = "https://maareva.bbcspldev.in/Report/BuyerDashPCADetailsExcelReport?selectedDate=<DATE>&CommodityId=<COMMODITY_ID>&CompanyCode=<COMPANY_CODE>&BuyerRegId=<BUYER_REG_ID>"

        val BUYER_AUCTION_REPORT = "https://maareva.bbcspldev.in/Report/BuyerDashPCAHeaderExcelReport?selectedDate=<DATE>&CommodityId=<COMMODITY_ID>&CompanyCode=<COMPANY_CODE>&BuyerRegId=<BUYER_REG_ID>"

        val LIVE_AUCTION_SOCKET_URL = "ws://maarevaapi.bbcspldev.in/MaarevaApi/MaarevaApi/BuyersLiveAuctionRtr?CommodityId=<COMMODITY_ID>&Date=<DATE>&CompanyCode=<COMPANY_CODE>&BuyerRegId=<BUYER_REG_ID>"
    }
}