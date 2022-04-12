package com.intellisoft.nndak

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.android.fhir.*
import com.google.android.fhir.sync.Sync
import com.intellisoft.nndak.data.FhirPeriodicSyncWorker
import com.intellisoft.nndak.utils.Constants.LOGIN
import com.intellisoft.nndak.utils.Constants.WELCOME

class FhirApplication : Application() {
    // Only initiate the FhirEngine when used for the first time, not when the app is created.
    private val fhirEngine: FhirEngine by lazy { constructFhirEngine() }

    private lateinit var instance: Context
    private val sharedPrefFile = "Neonatal"
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate() {
        super.onCreate()
        instance = this.applicationContext
          FhirEngineProvider.init(
            FhirEngineConfiguration(enableEncryptionIfSupported = true,
                DatabaseErrorStrategy.RECREATE_AT_OPEN,
                ServerConfiguration("https://hapi.fhir.org/baseR4/")
            )
          )
        Sync.oneTimeSync<FhirPeriodicSyncWorker>(this)

        sharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

    fun getInstance(): Context {
        return instance
    }

    private fun constructFhirEngine(): FhirEngine {
        return FhirEngineProvider.getInstance(this)
    }

    companion object {
        fun fhirEngine(context: Context) =
            (context.applicationContext as FhirApplication).fhirEngine

        fun getSharedPreferences(context: Context): SharedPreferences {
            return (context.applicationContext as FhirApplication).sharedPreferences
        }

        fun isWelcomed(context: Context): Boolean {
            return (context.applicationContext as FhirApplication).sharedPreferences.getBoolean(
                WELCOME,
                false
            )
        }

        fun isLoggedIn(context: Context): Boolean {
            return (context.applicationContext as FhirApplication).sharedPreferences.getBoolean(
                LOGIN,
                false
            )
        }

        fun setWelcomed(context: Context, b: Boolean) {
            (context.applicationContext as FhirApplication).editor.putBoolean(WELCOME, b).commit()
        }

        fun setLoggedIn(context: Context, b: Boolean) {
            (context.applicationContext as FhirApplication).editor.putBoolean(LOGIN, b).commit()
        }


    }
}
