package com.bluebellcspl.maarevacommoditytradingapp.commonFunction

import java.math.BigDecimal
import java.math.RoundingMode

object AmountNumberToWords {
    private val ones = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
        "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    )

    private val tens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )

    private fun convertToWords(n: Int, s: String): String {
        var num = n
        var str = ""
        if (num > 19) {
            str += tens[num / 10] + " " + ones[num % 10]
        } else {
            str += ones[num]
        }
        if (num != 0) {
            str += " $s"
        }
        return str
    }

    fun convert(number: BigDecimal): String {
        if (number == BigDecimal.ZERO) return "Zero Rupees"

        val stringBuilder = StringBuilder(number.toPlainString())
        val decimalIndex = stringBuilder.indexOf(".")
        val decimalNumber = if (decimalIndex != -1) {
            stringBuilder.substring(decimalIndex + 1).toInt()
        } else {
            0
        }
        val integerPart = number.setScale(0, RoundingMode.FLOOR).toLong()
        val decimalPart = decimalNumber

        var num = integerPart
        val crore = num / 10000000
        num %= 10000000
        val lakh = num / 100000
        num %= 100000
        val thousand = num / 1000
        num %= 1000
        val hundred = num / 100
        num %= 100
        val ten = num

        var result = ""
        if (crore > 0) result += convertToWords(crore.toInt(), "Crore ")
        if (lakh > 0) result += convertToWords(lakh.toInt(), "Lakh ")
        if (thousand > 0) result += convertToWords(thousand.toInt(), "Thousand ")
        if (hundred > 0) result += convertToWords(hundred.toInt(), "Hundred ")
        if (ten > 0) result += if (result.isNotEmpty()) "and " + convertToWords(ten.toInt(), "") else convertToWords(ten.toInt(), "")

        result += " Rupees"

        if (decimalPart > 0) {
            result += " and " + convertToWords(decimalPart, "Paise")
        }

        return result.trim()
    }
}
