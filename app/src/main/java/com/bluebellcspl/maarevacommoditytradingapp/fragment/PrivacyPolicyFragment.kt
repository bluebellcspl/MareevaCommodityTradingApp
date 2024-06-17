package com.bluebellcspl.maarevacommoditytradingapp.fragment

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bluebellcspl.maarevacommoditytradingapp.R
import com.bluebellcspl.maarevacommoditytradingapp.databinding.FragmentPrivacyPolicyBinding

class PrivacyPolicyFragment : Fragment() {
    var _binding: FragmentPrivacyPolicyBinding?=null
    val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_privacy_policy, container, false)
        binding.tvPrivacyPolicyContentPrivacyPolicyFragment.setText(
            Html.fromHtml("    <p><strong>Privacy Policy Effective Date: 01-11-2023</strong></p>\n" +
                    "    \n" +
                    "    <p>MaaReva Agri Solution is committed to protecting the privacy of the users (\"you\" or \"your\") of our web application (the \"Application\"). This Privacy Policy outlines how we collect, use, disclose, and protect your personal information when you access and use the Application.</p>\n" +
                    "    \n" +
                    "    <h2>1. Information We Collect</h2>\n" +
                    "    \n" +
                    "    <p>a. <strong>Personal Information:</strong> We may collect personal information that you voluntarily provide to us when using the Application. This may include:\n" +
                    "    <ul>\n" +
                    "        <li>Name: We collect your name to personalize your experience and facilitate communication with you.</li>\n" +
                    "        <li>Email Address: We collect your email address to communicate with you, send notifications, and respond to your inquiries or requests.</li>\n" +
                    "        <li>Contact Information: We may collect your contact information, such as phone number or address, to enable efficient communication between users of the application.</li>\n" +
                    "        <li>Company Name: If you are representing a company, we may collect your company name for identification and verification purposes.</li>\n" +
                    "        <li>Adharcard and Pancard Details: We may collect your Adharcard and Pancard details for authentication and legal compliance purposes.</li>\n" +
                    "    </ul>\n" +
                    "    </p>\n" +
                    "    \n" +
                    "    <p>b. <strong>Usage Information:</strong> We may automatically collect non-personal information about your interactions with the Application. This may include:\n" +
                    "    <ul>\n" +
                    "        <li>Device Information: We collect information about your device, such as IP address, browser type, operating system, and device identifiers. This helps us ensure compatibility and troubleshoot technical issues.</li>\n" +
                    "        <li>Log Data: We may collect log data, including the pages you visit, features you use, and the time spent on the Application. This information helps us analyze user behavior and improve the Application.</li>\n" +
                    "        <li>Cookies and Similar Technologies: We may use cookies and similar technologies to enhance your browsing experience and collect information about your usage patterns. You have the option to disable cookies through your browser settings, but please note that certain features of the Application may not function properly without cookies.</li>\n" +
                    "    </ul>\n" +
                    "    </p>\n" +
                    "    \n" +
                    "    <h2>2. Use of Information</h2>\n" +
                    "    \n" +
                    "    <p>1. We use the collected information for the following purposes:\n" +
                    "    <ul>\n" +
                    "        <li>Providing and Maintaining the Application: We use your personal information to operate and maintain the functionality of our application.</li>\n" +
                    "        <li>Communication: We use your contact information, such as email address and phone number, to communicate with you, respond to your inquiries, and provide relevant updates.</li>\n" +
                    "        <li>Personalization: Your personal information allows us to personalize your experience within the Application and tailor our services to your specific needs.</li>\n" +
                    "        <li>Data Analysis: We may analyze the usage patterns and trends of our users to improve the Application, enhance user experience, and make informed business decisions.</li>\n" +
                    "        <li>Compliance and Legal Obligations: We may use your personal information to comply with applicable laws, regulations, or legal processes. We may also use it to protect our rights, property, or safety, or the rights, property, or safety of others.</li>\n" +
                    "    </ul>\n" +
                    "    </p>\n" +
                    "    \n" +
                    "    <h2>3. Disclosure of Information</h2>\n" +
                    "    \n" +
                    "    <p>a. We may share your personal information with third parties in the following cases:\n" +
                    "    <ul>\n" +
                    "        <li>With Your Consent: We may share your personal information with third parties if you provide explicit consent for such disclosure.</li>\n" +
                    "        <li>Service Providers: We may engage third-party service providers to assist us in operating and maintaining the Application. These service providers may have access to your personal information to perform specific tasks on our behalf, such as hosting, data analysis, or customer support. We ensure that these service providers adhere to strict confidentiality and data protection standards.</li>\n" +
                    "        <li>Legal Requirements: We may disclose your personal information if required by law, regulation, legal process, or governmental request. We may also disclose it to protect our rights, property, or safety, or the rights, property, or safety of others.</li>\n" +
                    "    </ul>\n" +
                    "    </p>\n" +
                    "    \n" +
                    "    <h2>4. Data Security</h2>\n" +
                    "    \n" +
                    "    <p>a. We take appropriate technical and organizational measures to safeguard your personal information from unauthorized access, disclosure, alteration, or destruction. We employ industry-standard security practices to protect your data. However, please note that no method of transmission over the internet or electronic storage is completely secure. Therefore, we cannot guarantee absolute security of your personal information.</p>\n" +
                    "    \n" +
                    "    <h2>5. Your Choices</h2>\n" +
                    "    \n" +
                    "    <p>a. Providing certain personal information may be optional, but it may limit your ability to access certain features or functionalities of our application.</p>\n" +
                    "    \n" +
                    "    <p>b. You have the option to opt-out of receiving promotional communications from us by following the instructions provided in such communications.</p>\n" +
                    "    \n" +
                    "    <h2>6. Third-Party Links and Services</h2>\n" +
                    "    \n" +
                    "    <p>a. The Agri Trade Application may contain links to third-party websites or services that are not owned or controlled by us. This Privacy Policy applies only to our application. We encourage you to review the privacy policies of those third-party platforms before providing any personal information.</p>\n" +
                    "    \n" +
                    "    <h2>7. Children's Privacy</h2>\n" +
                    "    \n" +
                    "    <p>a. The Agri Trade Application is not intended for use by individuals under the age of 18 years. We do not knowingly collect personal information from children. If we become aware that we have collected personal information from a child without parental consent, we will take steps to promptly remove the information.</p>\n" +
                    "    \n" +
                    "    <h2>8. Updates to the Privacy Policy</h2>\n" +
                    "    \n" +
                    "    <p>a. We may update this Privacy Policy from time to time to reflect changes in our privacy practices or applicable laws. We will notify you of any material changes by posting the updated Privacy Policy on our Application or through other means.</p>\n" +
                    "    \n" +
                    "    <h2>9. Contact Us</h2>\n" +
                    "    \n" +
                    "    <p>a. If you have any questions, concerns, or requests regarding this Privacy Policy or our privacy practices, please contact us.</p>\n" +
                    "\n" +
                    "    <p>By accessing and using our application, you acknowledge that you have read, understood, and agree to be bound by this Privacy Policy.</p>\n" +
                    "\n" +
                    "    <p><strong>Last Updated: 01-12-2023</strong></p>\n")
        )
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}