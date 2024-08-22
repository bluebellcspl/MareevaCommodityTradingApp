package com.bluebellcspl.maarevacommoditytradingapp.commonFunction

import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class DateUtility {
    private val dateFormat : String = "dd-MM-yyyy HH:mm:ss"
    private val newDateTimeFormat: String = "yyyy-MM-dd HH:mm:ss"
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

    fun getyyyyMMddDateTime() : String{
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat(newDateTimeFormat, Locale.US)
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
    fun formatToyyyyMMdd(inputDate: String): String {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(inputDate)
        return outputFormat.format(date)
    }

    fun isStartDateBeforeEndDate(startDate: String, endDate: String, format: String): Boolean {
        val formatter = DateTimeFormatter.ofPattern(format)
        val start = LocalDate.parse(startDate, formatter)
        val end = LocalDate.parse(endDate, formatter)
        return start.isBefore(end) || start.isEqual(end)
    }


    fun getDateFromEditTextAsLong(editText: TextInputEditText, format: String): Long? {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        return try {
            val dateString = editText.text.toString().trim()
            val date = dateFormat.parse(dateString)
            date?.time
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}