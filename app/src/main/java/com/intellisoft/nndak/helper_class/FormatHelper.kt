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

    fun getCalculations(dateStr: String): String {

        val tripleData = getDateDetails(dateStr)
        val month = tripleData.second.toString().toInt()
        var year = tripleData.third.toString().toInt()

        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
        val date = sdf.parse(dateStr)
        cal.time = date

        if (month > 3) {
            cal.add(Calendar.YEAR, 1)
        }

        cal.add(Calendar.MONTH, -3)
        cal.add(Calendar.DATE, 7)

        if (month < 4) {
            cal.add(Calendar.YEAR, 1)
        }

        val sdf1 = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

        val newDate = cal.time

        return sdf1.format(newDate)


    }

    fun refineLMP(dateStr: String): String {

        val tripleData = getDateDetails(dateStr)

        val day = tripleData.first.toString().toInt()
        val month = tripleData.second.toString().toInt()
        val year = tripleData.third.toString().toInt()

        val caddy = if (day < 10) {
            "0$day"
        } else {
            day
        }
        val cool = if (month < 10) {
            "0$month"
        } else {
            month
        }

        return "$caddy/$cool/$year"
    }

    fun calculateGestation(lmpDate: String): String {

        val days = try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val today = getTodayDateNoTime()
            val formatted = getRefinedDate(lmpDate)
            val date1 = sdf.parse(today)
            val date2 = sdf.parse(formatted)

            val diff: Long = date1.time - date2.time

            val totalDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
            val daysOfWeek = 7
            val weeks = totalDays / daysOfWeek
            val days = totalDays % daysOfWeek

            Timber.e("$totalDays total $weeks weeks $days days")
            "$weeks week(s) $days days"

        } catch (e: Exception) {
            e.printStackTrace()
            "0"
        }
        return days

    }

    private fun getDateDetails(dateStr: String): Triple<Int?, Int?, Int?> {

        val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
        } else {
            return Triple(null, null, null)
        }
        val date = LocalDate.parse(dateStr, formatter)

        val day = date.dayOfMonth
        val month = date.monthValue
        val year = date.year

        return Triple(day, month, year)

    }

    fun convertDate(valueDate: String): Date {

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val currentDate = sdf.parse(valueDate)
        val sdf1 = SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
        val newDateStr = sdf1.format(currentDate)
        return sdf1.parse(newDateStr)
    }

    fun dateLessThanToday(valueDate: String): Boolean {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val today = getTodayDateNoTime()

        Timber.e("Current $today")
        Timber.e("Value $valueDate")

        val date1 = sdf.parse(today)
        val date2 = sdf.parse(valueDate)

        if (date1 != null) {
            if (date1.after(date2)) {
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
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val date = Date()
        return formatter.format(date)
    }
    fun dateOfBirth(date: String): Date? {
      //  val cool =getRefinedDate(date)
        val destFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        return  destFormat.parse(date)

    }
    private fun getRefinedDate(date: String): String {

        val sourceFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val destFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

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

    fun fhirObservations(value: String, value1: String): DbObservations {

        val title: String
        var dbValue = value

        if (value == "Negative" || value == "Positive" || value == "Unknown") {
            //HIV
            title = "HIV Status"
        } else if (value == "BF" || value == "NGT") {
            //Method of feeding
            title = "Method of feeding"
        } else if (value == "SVD" || value == "Breech" || value == "Vacuum" || value == "Cs") {
            //Delivery Method
            title = "Delivery Method"
        } else if (value == "Alive" || value == "Dead") {
            //Baby Life State at birth
            title = "Baby Life at Birth"
        } else if (value == "Male" || value == "Female") {
            //Baby Biological sex
            title = "Baby Biological Sex"

        } else if (value == "YES" || value == "NO") {
            //Was baby born b4 arrival to facility
            title = "Was baby born before arrival to facility"
        } else if (value == "Yes" || value == "No") {
            title = "Birth of one baby on a single pregnancy"

        } else if (value == "Elective" || value == "Emergency") {
            title = "Reason for CS"

        } else if (value == "Other") {
            title = "Multiple Pregnancy"

        } else if (value == "Twins" || value == "Triplets" || value == "Quadruplets"
            || value == "Quintuplets" || value == "Sextuplets" || value == "Septuplets"
        ) {
            title = "How many babies on a single pregnancy"
        } else if (value == "Early Labor" || value == "Active Labor" || value == "Transition") {
            title = "Labour Stage"

        } else if (value == "None" || value == "Engourged Breasts" || value == "Sore Nipples" || value == "Cracked Nipples" || value == "Bleeding Nipples" || value == "Inverted Nipples" || value == "Flat Nipples") {
            title = "Breast Problem"

        } else {
            //Any other
            title = value
            dbValue = value1
        }

        return DbObservations(dbValue, title)

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


