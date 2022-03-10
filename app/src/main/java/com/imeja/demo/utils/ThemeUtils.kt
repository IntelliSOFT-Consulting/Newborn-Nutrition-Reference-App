package com.imeja.demo.utils

import android.app.Application
import com.imeja.demo.FhirApplication

object ThemeUtils {

    private lateinit var application: Application

    fun init(application: Application){
        this.application=application
    }

    @JvmStatic
    fun isDarkModeActivated(): Boolean {
        val defaultSharedPref = FhirApplication.getSharedPreferences(application)
        return defaultSharedPref.getBoolean(Constants.DARK_MODE, false)
    }

    @JvmStatic
    fun setDarkMode(darkMode: Boolean) {
        val editor = FhirApplication.getSharedPreferences(application).edit()
        editor.putBoolean(Constants.DARK_MODE, darkMode)
        editor.apply()
    }
}