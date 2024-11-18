package com.bluebellcspl.maarevacommoditytradingapp.constants

import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.PrefUtil
import com.bluebellcspl.maarevacommoditytradingapp.retrofitApi.RetrofitHelper

class URLHelper {
    companion object{
        val BUYER_AUCTION_DETAIL_REPORT = RetrofitHelper.BASE_URL+ "/Report/BuyerDashPCADetailsExcelReport?selectedDate=<DATE>&CommodityId=<COMMODITY_ID>&CompanyCode=<COMPANY_CODE>&BuyerRegId=<BUYER_REG_ID>"

        val BUYER_AUCTION_REPORT = RetrofitHelper.BASE_URL+ "/Report/BuyerDashPCAHeaderExcelReport?selectedDate=<DATE>&CommodityId=<COMMODITY_ID>&CompanyCode=<COMPANY_CODE>&BuyerRegId=<BUYER_REG_ID>"

        val LIVE_AUCTION_SOCKET_URL = RetrofitHelper.WEBSOCKET_BASE_URL+"/API/MaarevaApi/MaarevaApi/BuyersLiveAuctionRtr?CommodityId=<COMMODITY_ID>&Date=<DATE>&CompanyCode=<COMPANY_CODE>&BuyerRegId=<BUYER_REG_ID>"
//        val LIVE_AUCTION_SOCKET_URL = RetrofitHelper.WEBSOCKET_BASE_URL+"/API/MaarevaApi/MaarevaApi/BuyersLiveAuctionRtr?CommodityId=<COMMODITY_ID>&Date=<DATE>&CompanyCode=<COMPANY_CODE>&BuyerRegId=<BUYER_REG_ID>" //Testing Server URL

        val PCA_AUCTION_REPORT = RetrofitHelper.BASE_URL+ "/Report/PCADashPCAHeaderExcelReport?selectedDate=<DATE>&CommodityId=<COMMODITY_ID>&CompanyCode=<COMPANY_CODE>&PCARegId=<PCA_REG_ID>"

        val PCA_AUCTION_DETAIL_REPORT = RetrofitHelper.BASE_URL+ "/Report/PCADashPCADetailsExcelReport?selectedDate=<DATE>&CommodityId=<COMMODITY_ID>&CompanyCode=<COMPANY_CODE>&PCARegId=<PCA_REG_ID>"

        val IND_PCA_AUCTION_DETAIL_REPORT = RetrofitHelper.BASE_URL+
                "/Report/IndividualPCADashPCADetailsExcelReport?selectedDate=<DATE>&CommodityId=<COMMODITY_ID>&CompanyCode=<COMPANY_CODE>&IndividualPCARegId=<PCA_REG_ID>&Language=en"

        val INVOICE_DOC = RetrofitHelper.BASE_URL+ "/RDLCReport/InVoiceGenerating.aspx?InvoiceNo=<INVOICE_NO>&PCARegId=<PCA_REG_ID>"

        val INVOICE_PDF = RetrofitHelper.BASE_URL+ "/RDLCReport/InVoiceGenerating.aspx?InvoiceNo=<INVOICE_NO>&PCARegId=<PCA_REG_ID>&Action=PDF"

//        val LIVE_CHAT_SOCKET = "wss://maareva.bbcspldev.in/API/MaarevaApi/MaarevaApi/ConnectWebSocket" //Testing Server URL
        val LIVE_CHAT_SOCKET = "wss://maareva.com/API/MaarevaApi/MaarevaApi/ConnectWebSocket"
    }
}