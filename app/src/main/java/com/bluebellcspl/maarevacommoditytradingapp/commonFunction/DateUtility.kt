package com.bluebellcspl.maarevacommoditytradingapp.commonFunction

import java.text.SimpleDateFormat
import java.util.*

class DateUtility {
    private val dateFormat : String = "dd-MM-yyyy HH:mm:ss"
    private val codeGenrateFormate : String = "ddMMyyyyHHmmss"
    private val compDateFormat = "dd-MM-yyyy"
    private val newDateFormat = "yyyy-MM-dd"
    private val compTimeFormat = "HH:mm:ss"
    private val timeFormat12Hr = "hh:mm a"

    fun getDate() : String{
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat(dateFormat, Locale.US)
        val current = sdf.format(cal.time)
        val date : String = current.toString()
        return date
    }

    fun getCompletionDate() : String{
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat(compDateFormat, Locale.US)
        val current = sdf.format(cal.time)
        val date : String = current.toString()
        return date
    }

    fun getyyyyMMdd() : String{
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat(newDateFormat, Locale.US)
        val current = sdf.format(cal.time)
        val date : String = current.toString()
        return date
    }

    fun getCompletionTime() : String{
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat(compTimeFormat, Locale.US)
        val current = sdf.format(cal.time)
        val date : String = current.toString()
        return date
    }

    fun formatToddMMyyyy(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val date = inputFormat.parse(inputDate)
        return outputFormat.format(date)
    }

}