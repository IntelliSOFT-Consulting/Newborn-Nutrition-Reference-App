package com.intellisoft.nndak.utils

import android.graphics.Color
import androidx.annotation.ColorInt

object Constants {

    const val DEMO_API_SERVER="https://devnndak.intellisoftkenya.com/api/"
    const val DEMO_SERVER="https://devnndak.intellisoftkenya.com/fhir/"
//    const val DEMO_SERVER="https://hapi.fhir.org/baseR4/"

    const val MAX_RESOURCE_COUNT = 40
    const val SYNC_VALUE = "PMH"
    const val SYNC_PARAM = "address-postalcode"
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val SERVER_URL_DEMO = "server_url_demo"
    const val SERVER_URL = "server_url"
    const val SERVER_SET = "server_set"
    const val ACCESS_TOKEN = "access_token"
    const val USER_ACCOUNT = "user_account"
    const val STROKE_WIDTH = 2f
    const val CORNER_RADIUS = 10f

    @ColorInt
    const val FILL_COLOR = Color.TRANSPARENT

    @ColorInt
    const val STROKE_COLOR = Color.GRAY

    /**
     * Key Indexes to navigate through the app
     */
    const val MATERNITY = "0"
    const val NEWBORN = "1"
    const val APGAR_SCORE = "2"
    const val ASSESS_CHILD = "3"
    const val FEEDING_NEEDS = "4"
    const val NEWBORN_ADMISSION = "5"
    const val MOTHER_ASSESSMENT = "6"
    const val CHILD_ASSESSMENT = "7"
    const val POST_MOTHER_ASSESSMENT = "8"
    const val POST_LACTATION_ASSESSMENT = "9"
    const val CHILD_FEEDING_EFFECTIVENESS = "10"
    const val MILK_CONSENT_FORM = "11"
    const val MILK_PRESCRIPTION = "12"
    const val MILK_RECEIVABLE = "10"
    const val HEALTH_ASSESSMENT = "11"
    const val DISCHARGE = "12"



}