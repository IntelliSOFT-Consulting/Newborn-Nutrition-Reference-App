package com.intellisoft.nndak.helper_class

import com.intellisoft.nndak.models.DbObservations
import java.lang.Double.parseDouble
import java.text.SimpleDateFormat
import java.util.*

class FormatHelper {

    fun checkDate(birthDate: String, d2: String): Boolean {

        val sdf1 = SimpleDateFormat("E MMM dd HH:mm:ss z yyyy")
        val currentdate = sdf1.parse(birthDate)

        val sdf2 = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val newCurrentDate = sdf2.format(currentdate)

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val date1 = sdf.parse(newCurrentDate)
        val date2 = sdf.parse(d2)

        // after() will return true if and only if date1 is after date 2
        if (date1.after(date2)) {
            return false
        }
        return true

    }

    fun getTodayDate(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val date = Date()
        return formatter.format(date)
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

        } else if (value == "Other" ) {
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


}