package com.intellisoft.nndak

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.android.fhir.*
import com.google.android.fhir.sync.Sync
import com.intellisoft.nndak.charts.MilkExpression
import com.intellisoft.nndak.data.AuthResponse
import com.intellisoft.nndak.data.FhirPeriodicSyncWorker
import com.intellisoft.nndak.utils.Constants.ACCESS_TOKEN
import com.intellisoft.nndak.utils.Constants.ACTIVE
import com.intellisoft.nndak.utils.Constants.CURRENT_BABY
import com.intellisoft.nndak.utils.Constants.DEMO_API_SERVER
import com.intellisoft.nndak.utils.Constants.DEMO_SERVER
import com.intellisoft.nndak.utils.Constants.DHM
import com.intellisoft.nndak.utils.Constants.FEEDINGS
import com.intellisoft.nndak.utils.Constants.LOGIN
import com.intellisoft.nndak.utils.Constants.MILK_EXPRESSION
import com.intellisoft.nndak.utils.Constants.ORDER
import com.intellisoft.nndak.utils.Constants.RELATED
import com.intellisoft.nndak.utils.Constants.SERVER_SET
import com.intellisoft.nndak.utils.Constants.SERVER_URL
import com.intellisoft.nndak.utils.Constants.SERVER_URL_DEMO
import com.intellisoft.nndak.utils.Constants.STATISTICS
import com.intellisoft.nndak.utils.Constants.USER_ACCOUNT
import com.intellisoft.nndak.utils.Constants.WEIGHTS
import com.intellisoft.nndak.utils.Constants.WELCOME
import timber.log.Timber


class FhirApplication : Application() {
    // Only initiate the FhirEngine when used for the first time, not when the app is created.
    private val fhirEngine: FhirEngine by lazy { constructFhirEngine() }

    private lateinit var instance: Context
    private val sharedPrefFile = "Neonatal"
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        instance = this.applicationContext

        sharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        FhirEngineProvider.init(
            FhirEngineConfiguration(
                enableEncryptionIfSupported = true,
                DatabaseErrorStrategy.RECREATE_AT_OPEN,
                ServerConfiguration(DEMO_SERVER)
            )
        )
        Sync.oneTimeSync<FhirPeriodicSyncWorker>(this)


    }


    private fun constructFhirEngine(): FhirEngine {
        return FhirEngineProvider.getInstance(this)
    }

    companion object {

        fun fhirEngine(context: Context) =
            (context.applicationContext as FhirApplication).fhirEngine


        fun getServerURL(context: Context): String? {
            return (context.applicationContext as FhirApplication).sharedPreferences.getString(
                SERVER_URL, DEMO_API_SERVER
            )
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

        fun isServerSet(context: Context): Boolean {
            return (context.applicationContext as FhirApplication).sharedPreferences.getBoolean(
                SERVER_SET,
                false
            )
        }


        fun setWelcomed(context: Context, b: Boolean) {
            (context.applicationContext as FhirApplication).editor.putBoolean(WELCOME, b).commit()
        }

        fun setLoggedIn(context: Context, b: Boolean) {
            (context.applicationContext as FhirApplication).editor.putBoolean(LOGIN, b).commit()
        }

        fun updateCurrent(context: Context, b: String) {
            (context.applicationContext as FhirApplication).editor.putString(RELATED, b).commit()
        }

        fun setDashboardActive(context: Context, b: String) {
            (context.applicationContext as FhirApplication).editor.putString(ACTIVE, b).commit()
        }

        fun getDashboardActive(context: Context): String {
            return (context.applicationContext as FhirApplication).sharedPreferences.getString(
                ACTIVE,
                ""
            ).toString()
        }
        fun getUpdatedCurrent(context: Context): String {
            return (context.applicationContext as FhirApplication).sharedPreferences.getString(
                RELATED,
                ""
            ).toString()
        }

        fun updateDetails(context: Context, it: AuthResponse) {
            (context.applicationContext as FhirApplication).editor.putString(ACCESS_TOKEN, it.token)
                .commit()
        }

        fun updateProfile(context: Context, it: String) {
            (context.applicationContext as FhirApplication).editor.putString(USER_ACCOUNT, it)
                .commit()
        }

        fun fetchAuthToken(context: Context): String? {
            return (context.applicationContext as FhirApplication).sharedPreferences.getString(
                ACCESS_TOKEN,
                ""
            )
        }

        fun getProfile(context: Context): String? {
            return (context.applicationContext as FhirApplication).sharedPreferences.getString(
                USER_ACCOUNT,
                ""
            )
        }


        fun updateLocalFeeding(context: Context, it: String) {
            (context.applicationContext as FhirApplication).editor.putString(MILK_EXPRESSION, it)
                .commit()
        }


        fun mumContra(context: Context, it: String) {
            (context.applicationContext as FhirApplication).editor.putString("Contra", it)
                .commit()
        }


        fun updateFeedings(context: Context, it: String) {
            (context.applicationContext as FhirApplication).editor.putString(FEEDINGS, it)
                .commit()
        }


        fun updateSyncTime(context: Context, it: String) {
            (context.applicationContext as FhirApplication).editor.putString("Sync", it)
                .commit()
        }

        fun getSyncTime(context: Context): String? {
            return (context.applicationContext as FhirApplication).sharedPreferences.getString(
                "Sync",
                ""
            )
        }

        fun updateWeights(context: Context, it: String) {
            (context.applicationContext as FhirApplication).editor.putString(WEIGHTS, it)
                .commit()
        }

        fun updateStatistics(context: Context, it: String) {
            (context.applicationContext as FhirApplication).editor.putString(STATISTICS, it)
                .commit()
        }


        fun updateCurrentOrder(context: Context, it: String) {
            (context.applicationContext as FhirApplication).editor.putString(ORDER, it)
                .commit()
        }


        fun updateDHM(context: Context, it: String) {
            (context.applicationContext as FhirApplication).editor.putString(DHM, it)
                .commit()
        }


        fun updateCurrentPatient(context: Context, it: String) {
            (context.applicationContext as FhirApplication).editor.putString(CURRENT_BABY, it)
                .commit()
        }

        fun getExpressions(context: Context): String? {
            return (context.applicationContext as FhirApplication).sharedPreferences.getString(
                MILK_EXPRESSION,
                ""
            )
        }

        fun getOrder(context: Context): String? {
            return (context.applicationContext as FhirApplication).sharedPreferences.getString(
                ORDER,
                ""
            )
        }

        fun getCurrentPatient(context: Context): String {
            return (context.applicationContext as FhirApplication).sharedPreferences.getString(
                CURRENT_BABY,
                ""
            ).toString()
        }

        fun getFeedings(context: Context): String? {
            return (context.applicationContext as FhirApplication).sharedPreferences.getString(
                FEEDINGS,
                ""
            )
        }

        fun getStatistics(context: Context): String? {
            return (context.applicationContext as FhirApplication).sharedPreferences.getString(
                STATISTICS,
                ""
            )
        }

        fun getDHM(context: Context): String? {
            return (context.applicationContext as FhirApplication).sharedPreferences.getString(
                DHM,
                ""
            )
        }

        fun getWeights(context: Context): String? {
            return (context.applicationContext as FhirApplication).sharedPreferences.getString(
                WEIGHTS,
                ""
            )
        }


    }
}
