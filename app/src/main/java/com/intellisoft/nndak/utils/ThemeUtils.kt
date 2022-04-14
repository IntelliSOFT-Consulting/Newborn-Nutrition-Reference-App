package com.intellisoft.nndak.utils

import android.app.Application
import com.intellisoft.nndak.FhirApplication

object ThemeUtils {

    private lateinit var application: Application

    fun init(application: Application){
        this.application=application
    }
}