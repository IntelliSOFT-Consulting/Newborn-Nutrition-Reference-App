package com.intellisoft.nndak.utils

import android.graphics.Color
import androidx.annotation.ColorInt

object Constants {

    const val DEMO_API_SERVER = "https://devnndak.intellisoftkenya.com/api/"
//    const val DEMO_SERVER = "https://devnndak.intellisoftkenya.com/fhir/"
    const val DEMO_SERVER = "https://hapi.fhir.org/baseR4/"
    const val MAX_RESOURCE_COUNT = 100
    const val MIN_RESOURCE_COUNT = 10
    const val SYNC_VALUE = "Pumwani"
    const val SYNC_STATE = "PMH"
    const val SYNC_PARAM = "address-postalcode"
    const val ACTIVE = "active"
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val SERVER_URL_DEMO = "server_url_demo"
    const val SERVER_URL = "server_url"
    const val SERVER_SET = "server_set"
    const val ACCESS_TOKEN = "access_token"
    const val USER_ACCOUNT = "user_account"
    const val MILK_EXPRESSION = "milk_expression"
    const val FEEDINGS = "feedings"
    const val WEIGHTS = "weights"
    const val STATISTICS = "statistics"
    const val DHM = "donor"
    const val STROKE_WIDTH = 2f
    const val CORNER_RADIUS = 10f

    @ColorInt
    const val FILL_COLOR = Color.TRANSPARENT

    @ColorInt
    const val STROKE_COLOR = Color.GRAY

}