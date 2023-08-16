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

    fun getUserCode() : String{
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat(codeGenrateFormate, Locale.US)
        val current = sdf.format(cal.time)
        val date : String = "USER"+current.toString()
        return date
    }

    fun getSurveyId() : String{
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat(codeGenrateFormate, Locale.US)
        val current = sdf.format(cal.time)
        val date : String = "SUR"+current.toString()
        return date
    }

    fun getMenberCode() : String{
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat(codeGenrateFormate, Locale.US)
        val current = sdf.format(cal.time)
        val date : String = "MEM"+current.toString()
        return date
    }

    fun getAnimalCode() : String{
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat(codeGenrateFormate, Locale.US)
        val current = sdf.format(cal.time)
        val date : String = "ANIMAL"+current.toString()
        return date
    }

    fun get12HourTime() : String{
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat(timeFormat12Hr, Locale.US)
        val current = sdf.format(cal.time)
        val date : String = current.toString()
        return date
    }

}