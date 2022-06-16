package com.intellisoft.nndak.helper_class

import android.content.Context
import android.os.Build
import com.intellisoft.nndak.R
import com.intellisoft.nndak.models.DbObservations
import timber.log.Timber
import java.lang.Double.parseDouble
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit


class FormatHelper {

    fun isSameDay(dateOne: String, dateTwo: String): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

        Timber.e("Current $dateOne")
        Timber.e("Value $dateTwo")

        val date1 = sdf.parse(dateOne)
        val date2 = sdf.parse(dateTwo)

        if (date1 != null) {
            if (date1.equals(date2)) {
                return true
            }
        }
        return false
    }

    fun dateLessThanToday(valueDate: String): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val today = getTodayDateNoTime()

        Timber.e("Current $today")
        Timber.e("Value $valueDate")

        val date1 = sdf.parse(today)
        val date2 = sdf.parse(valueDate)

        if (date1 != null) {
            if (date1.equals(date2) || date1.after(date2)) {
                return true
            }
        }
        return false
    }

    fun checkDate(birthDate: String, d2: String): Boolean {

        val sdf1 = SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
        val currentdate = sdf1.parse(birthDate)

        val sdf2 = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH)
        val newCurrentDate = sdf2.format(currentdate)

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH)
        val date1 = sdf.parse(newCurrentDate)
        val date2 = sdf.parse(d2)

        // after() will return true if and only if date1 is after date 2
        if (date1.after(date2)) {
            return false
        }
        return true

    }


    fun getTodayDate(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH)
        val date = Date()
        return formatter.format(date)
    }

    private fun getTodayDateNoTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val date = Date()
        return sdf.format(date)
    }

    fun dateOfBirth(date: String): Date? {
        val destFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        return destFormat.parse(date)

    }

     fun getHour(date: String): String {
        val sourceFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH)
        val destFormat = SimpleDateFormat("hh a", Locale.ENGLISH)

        val convertedDate = sourceFormat.parse(date)
        return convertedDate?.let { destFormat.format(it) }.toString()

    }

     fun getDayName(date: String): String {

        val sourceFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val destFormat = SimpleDateFormat("EEEE", Locale.ENGLISH)

        val convertedDate = sourceFormat.parse(date)
        return convertedDate?.let { destFormat.format(it) }.toString()

    }

     fun getMonthName(date: String): String {

        val sourceFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val destFormat = SimpleDateFormat("MMM", Locale.ENGLISH)

        val convertedDate = sourceFormat.parse(date)
        return convertedDate?.let { destFormat.format(it) }.toString()

    }
    private fun getRefinedDate(date: String): String {

        val sourceFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val destFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

        val convertedDate = sourceFormat.parse(date)
        return convertedDate?.let { destFormat.format(it) }.toString()

    }

    fun extractDateString(date: String): String {

        val sourceFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val destFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)

        val convertedDate = sourceFormat.parse(date)
        return convertedDate?.let { destFormat.format(it) }.toString()

    }

    fun checkPhoneNo(string: String): Boolean {
        var isNo = true

        try {
            val num = parseDouble(string)
        } catch (e: NumberFormatException) {
            isNo = false
        }
        return isNo
    }

    fun saveSharedPreference(
        context: Context,
        sharedKey: String,
        sharedValue: String
    ) {

        val appName = context.getString(R.string.app_name)
        val sharedPreferences = context.getSharedPreferences(appName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(sharedKey, sharedValue)
        editor.apply()
    }

    fun retrieveSharedPreference(
        context: Context,
        sharedKey: String
    ): String? {

        val appName = context.getString(R.string.app_name)

        val sharedPreferences = context.getSharedPreferences(appName, Context.MODE_PRIVATE)
        return sharedPreferences.getString(sharedKey, null)

    }

    fun getSearchQuery(spinnerValue: String, context: Context): String {

        val birds = context.resources.getStringArray(R.array.birds)

        val queryParam = when (spinnerValue) {
            birds[0].toString() -> {
                DbMotherKey.PATIENT_NAME.name
            }
            birds[1].toString() -> {
                DbMotherKey.PATIENT_NAME.name
            }
            birds[2].toString() -> {
                DbMotherKey.NATIONALID.name
            }
            birds[3].toString() -> {
                DbMotherKey.PHONE_NUMBER.name
            }
            birds[4].toString() -> {
                DbMotherKey.MOTHER_DOB.name
            }
            else -> DbMotherKey.PATIENT_NAME.name
        }
        return queryParam

    }

    fun formatDate(toString: String): String {

        val sdf1 = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
        return sdf1.format(toString)

    }


}


