package com.intellisoft.nndak.helper_class

import java.text.SimpleDateFormat
import java.util.*

class FormatHelper {

    fun checkDate(birthDate: String, d2: String):Boolean{

        val sdf1 = SimpleDateFormat("E MMM dd HH:mm:ss z yyyy")
        val currentdate = sdf1.parse(birthDate)

        val sdf2 = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val newCurrentDate = sdf2.format(currentdate)

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val date1 = sdf.parse(newCurrentDate)
        val date2 = sdf.parse(d2)

        // after() will return true if and only if date1 is after date 2
        if(date1.after(date2)){
            return false
        }
        return true

    }

    fun getTodayDate(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val date = Date()
        return formatter.format(date)
    }

}