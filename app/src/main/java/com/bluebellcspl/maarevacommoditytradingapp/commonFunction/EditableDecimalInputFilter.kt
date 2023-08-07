package com.bluebellcspl.maarevacommoditytradingapp.commonFunction

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Pattern

class EditableDecimalInputFilter(startPlaces:Int,endPlaces:Int) : InputFilter {

    var mPattern: Pattern? = null

    init {
        mPattern =
            Pattern.compile("^-?(?:\\d{0,"+ (startPlaces-1) + "}+(?:\\.\\d{0,"+(endPlaces-1)+"})?)?\$")
    }

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val matcher = mPattern!!.matcher(dest)
        return if (!matcher.matches()) "" else null
    }

}
