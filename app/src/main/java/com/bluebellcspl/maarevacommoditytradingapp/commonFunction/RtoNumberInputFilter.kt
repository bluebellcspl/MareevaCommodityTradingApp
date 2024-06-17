package com.bluebellcspl.maarevacommoditytradingapp.commonFunction

import android.text.InputFilter
import android.text.Spanned

class RtoNumberInputFilter : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        // Build the resultant string
        val builder = StringBuilder(dest).replace(dstart, dend, source?.subSequence(start, end)?.toString() ?: "")

        // Create the final string
        val result = builder.toString()

        // Validate the length and format
        if (result.length > 13) {
            return ""
        }

        // Regular expression for RTO number validation
        val regex = Regex("^[A-Z]{0,2}(\\s[A-Z]{0,2})?(\\s\\d{0,2})?(\\s[A-Z]{0,2})?(\\s\\d{0,4})?\$")
        return if (regex.matches(result)) {
            null
        } else {
            ""
        }
    }
}