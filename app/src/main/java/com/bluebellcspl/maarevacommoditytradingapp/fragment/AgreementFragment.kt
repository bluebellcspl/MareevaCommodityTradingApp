package com.bluebellcspl.maarevacommoditytradingapp.fragment

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentAgreementBinding

class AgreementFragment : Fragment() {
    var _binding:FragmentAgreementBinding?=null
    val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_agreement, container, false)
        binding.tvAgreementContent.setText(
            Html.fromHtml(" <h5>1.\tIntroduction</h5>\n" +
                "    <p>Welcome to MaaReva, an online platform designed to facilitate agricultural produce trading and connect buyers with Pakka Commission Agents (PCAs) and Agricultural Produce Market Committees (APMCs). By accessing and using the MaaReva Platform, you agree to comply with the following terms and conditions. Please read them carefully before using our services.</p>\n" +
                "\n" +
                "    <h5>2.\tRegistration</h5>\n" +
                "    <p>\n" +
                "        1.\tTo use our platform, you must complete the registration process. You agree to provide accurate, current, and complete information during registration.\n" +
                "    </p>\n" +
                "    <p>\n" +
                "        2.\tYou are responsible for maintaining the confidentiality of your account information, including your login credentials. If you suspect or  become aware of any unauthorized access to your account or any other breach of security, it is your responsibility to immediately notify us\n" +
                "    </p>\n" +
                "    <h5>3.\tBuyer's Responsibilities</h5>\n" +
                "    <p>\n" +
                "        1.\tAs a buyer, you agree to use the MaaReva Platform for lawful purposes and in compliance with all applicable laws and regulations.\n" +
                "    </p><p>\n" +
                "        2.\tYou are solely responsible for your purchase decisions, including the selection of PCAs and APMCs, price negotiations, and the quantity of agricultural produce you wish to buy. MaaReva is not responsible for any decisions you make regarding purchase transactions. MaaReva is solely providing the trading platform for your use.\n" +
                "    </p><p>\n" +
                "        3.\tYou agree to adhere to the terms and conditions set by the PCAs and APMCs with whom you engage in transactions through the platform.\n" +
                "    </p>\n" +
                "\n" +
                "    <h5>4.\tPurchase Requirements</h5>\n" +
                "    <p>\n" +
                "        1.\tWhen posting purchase requirements on  the  platform,  you  must accurately specify the quantity and price of agricultural produce you wish to buy.\n" +
                "    </p><p>\n" +
                "        2.\tYou may distribute your purchase requirements among associated PCAs as desired.\n" +
                "    </p><p>\n" +
                "        3.\tThe platform provides information on last traded prices at APMCs and NCDEX to assist you in making informed decisions about price ranges.\n" +
                "    </p><p>\n" +
                "        4.\tYou can adjust price ranges and quantities as needed to align with your requirements.\n" +
                "    </p>\n" +
                "    <h5>5.\tPCA Association</h5>\n" +
                "    <p>\n" +
                "        1.\tYou may associate with PCAs registered on the MaaReva Platform to facilitate your transactions.\n" +
                "    </p>\n" +
                "    <p>\n" +
                "        2.\tYou have the flexibility to add or remove PCAs from your network as subject to approval of MaaReva.\n" +
                "    </p>\n" +
                "    <p>\n" +
                "        3.\tYou are responsible for verifying the credentials and reliability of the PCAs you associate with.\n" +
                "    </p>\n" +
                "    <h5>6.\tPayment and Transactions</h5>\n" +
                "    <p>\n" +
                "        1.\tAll payments and financial transactions are not been conducted on the platform. Financial transaction should be made as per compliance with the applicable laws and regulations as your terms.\n" +
                "    </p><p>\n" +
                "        2.\tYou agree to make payments to PCAs promptly and in accordance with the agreed terms.\n" +
                "    </p><p>\n" +
                "        3.\tThere are no legal transactions taking place on the MaaReva Platform. MaaReva only provides a service that facilitates the buying and selling process, making it faster and ensuring transparency.\n" +
                "    </p><p>\n" +
                "        4.\tAny issues related to the use of the MaaReva Platform for buying and selling will be resolved through our legal department only, which will provide necessary advice and guidance.\n" +
                "    </p>\n" +
                "    <h5>7.\tReports and Analytics</h5>\n" +
                "    <p>\n" +
                "        1.\tThe platform provides various report generation options to help you analyze your trading activities.\n" +
                "    </p><p>\n" +
                "        2.\tYou may use these reports to gain insights into your transactions, PCA performance, and trading history only.\n" +
                "    </p>\n" +
                "    <h5> 8.\tPrivacy and Data Security</h5>\n" +
                "    <p>\n" +
                "        1.\tWe are committed to safeguarding your data and privacy. Please review our Privacy Policy to understand how we collect, use, and protect your information.\n" +
                "    </p><p>\n" +
                "        2.\tIf any GST department or other government department requests transaction-related information from MaaReva, MaaReva will be obligated to provide them with the requested details.\n" +
                "    </p>\n" +
                "\n" +
                "    <h5>9.\tTermination</h5>\n" +
                "    <p>\n" +
                "        1.\tWe reserve the right to terminate your account and access to the platform at our discretion, with or without cause.\n" +
                "    </p><p>\n" +
                "        2.\tYou may terminate your account at any time by notifying us in writing or through the platform's designated process.\n" +
                "    </p>\n" +
                "\n" +
                "\n" +
                "\n" +
                "    <h5>10.\tAmendments to Terms and Conditions</h5>\n" +
                "    <p>  1.\tWe may update and modify these terms and conditions at any time. You will be notified of any changes, and continued use of the platform constitutes your acceptance of the revised terms.</p>\n" +
                "\n" +
                "\n" +
                "    <h5>\n" +
                "        11.\tContact Information\n" +
                "    </h5>\n" +
                "    <p>1.\tFor any questions, concerns, or inquiries, please contact us at [Contact Us Page].</p>\n" +
                "\n" +
                "\n" +
                "    <h5>\n" +
                "        12.\tGoverning Law\n" +
                "    </h5>\n" +
                "    <p>\n" +
                "        1.\tThese terms and conditions are governed by and construed in accordance with the laws of Agriculture produces trade law.\n" +
                "    </p><p>\n" +
                "        2.\tEach buyer is responsible for adhering to all relevant state government regulations and laws that apply to their specific transactions.\n" +
                "    </p><p>\n" +
                "        By using the MaaReva Platform, you acknowledge that you have read, understood, and agreed to these terms and conditions. Failure to comply with these terms may result in the termination of your access to the platform.\n" +
                "    </p>"))
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}