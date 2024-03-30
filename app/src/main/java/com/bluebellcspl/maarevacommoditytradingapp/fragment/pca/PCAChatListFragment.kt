package com.bluebellcspl.maarevacommoditytradingapp.fragment.pca

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluebellcspl.maarevacommoditytradingapp.R
class PCAChatListFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_p_c_a_chat_list, container, false)
    }
}