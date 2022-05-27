package com.intellisoft.nndak.utils

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.util.Patterns
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.textfield.TextInputEditText
import com.intellisoft.nndak.R
import com.intellisoft.nndak.models.PatientItem
import com.intellisoft.nndak.models.RelatedPersonItem
import com.intellisoft.nndak.utils.Constants.CORNER_RADIUS
import com.intellisoft.nndak.utils.Constants.FILL_COLOR
import com.intellisoft.nndak.utils.Constants.STROKE_COLOR
import org.hl7.fhir.r4.model.Address
import org.hl7.fhir.r4.model.ContactPoint
import org.hl7.fhir.r4.model.HumanName
import timber.log.Timber
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.util.*
import java.util.regex.Pattern

fun isNetworkAvailable(context: Context?): Boolean {
    if (context == null) return false
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    return true
                }
            }
        }
    } else {
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
            return true
        }
    }
    return false
}

fun disableEditing(editText: TextInputEditText?) {
    editText?.isFocusable = false
    editText?.isCursorVisible = false
    editText?.keyListener = null

}

fun getNames(
    firstname: String,
    other_name: String,
    title: String
): List<HumanName> {
    return listOf(
        HumanName().addGiven(firstname)
            .addPrefix(title).setFamily(other_name).setUse(HumanName.NameUse.OFFICIAL)
    )
}

fun getTelephone(telephone: String): List<ContactPoint> {
    return listOf(
        ContactPoint().setSystem(ContactPoint.ContactPointSystem.PHONE)
            .setValue(telephone)
    )

}

fun getAddress(city: String, country: String): List<Address> {
    return listOf(Address().setCity(city.uppercase()).setCountry(country.uppercase()))
}

fun generateUuid(): String {
    return UUID.randomUUID().toString()
}

fun getDate(dob: String): Date {
    return SimpleDateFormat(
        "yyyy-mm-dd",
        Locale.ENGLISH
    ).parse(dob)!!

}

fun validInput(input: String): Boolean {
    if (input.isEmpty()) {
        return false
    }
    return true
}

fun validEmail(email: String): Boolean {
    val emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
            "[a-zA-Z0-9_+&*-]+)*@" +
            "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
            "A-Z]{2,7}$"

    val pat: Pattern = Pattern.compile(emailRegex)
    return pat.matcher(email).matches()
}

fun isValidPassword(string: String): Boolean {
    if (string.length > 5) {
        return true
    }
    return false
}

fun allCornersRounded(): MaterialShapeDrawable {
    return MaterialShapeDrawable(
        ShapeAppearanceModel.builder()
            .setAllCornerSizes(CORNER_RADIUS)
            .setAllCorners(RoundedCornerTreatment())
            .build()
    )
        .applyStrokeColor()
}

fun topCornersRounded(): MaterialShapeDrawable {
    return MaterialShapeDrawable(
        ShapeAppearanceModel.builder()
            .setTopLeftCornerSize(CORNER_RADIUS)
            .setTopRightCornerSize(CORNER_RADIUS)
            .setTopLeftCorner(RoundedCornerTreatment())
            .setTopRightCorner(RoundedCornerTreatment())
            .build()
    )
        .applyStrokeColor()
}

fun bottomCornersRounded(): MaterialShapeDrawable {
    return MaterialShapeDrawable(
        ShapeAppearanceModel.builder()
            .setBottomLeftCornerSize(CORNER_RADIUS)
            .setBottomRightCornerSize(CORNER_RADIUS)
            .setBottomLeftCorner(RoundedCornerTreatment())
            .setBottomRightCorner(RoundedCornerTreatment())
            .build()
    )
        .applyStrokeColor()
}

fun noCornersRounded(): MaterialShapeDrawable {
    return MaterialShapeDrawable(ShapeAppearanceModel.builder().build()).applyStrokeColor()
}

fun MaterialShapeDrawable.applyStrokeColor(): MaterialShapeDrawable {
    strokeWidth = Constants.STROKE_WIDTH
    fillColor = ColorStateList.valueOf(FILL_COLOR)
    strokeColor = ColorStateList.valueOf(STROKE_COLOR)
    return this
}

fun isValidURL(text: String): Boolean {

    return try {
        URL(text).toURI()
        Patterns.WEB_URL.matcher(text).matches()
    } catch (e: MalformedURLException) {
        e.printStackTrace()
        false
    } catch (e: URISyntaxException) {
        e.printStackTrace()
        false
    }
}

fun hideProgress(progressBar: ProgressBar) {
    progressBar.isVisible = false
}

fun showProgress(progressBar: ProgressBar) {
    progressBar.isVisible = true
}

fun getFormattedAge(
    dob: String,
    resources: Resources
): String {
    if (dob.isEmpty()) return ""

    return Period.between(LocalDate.parse(dob), LocalDate.now()).let {
        when {
            it.years > 0 -> resources.getQuantityString(R.plurals.ageYear, it.years, it.years)
            it.months > 0 -> resources.getQuantityString(R.plurals.ageMonth, it.months, it.months)
            else -> resources.getQuantityString(R.plurals.ageDay, it.days, it.days)
        }
    }
}

enum class ViewTypes {
    HEADER,
    PATIENT,
    PATIENT_PROPERTY,
    OBSERVATION,
    ENCOUNTER,
    CONDITION;

    companion object {
        fun from(ordinal: Int): ViewTypes {
            return values()[ordinal]
        }
    }

}

/**
 * Observations vs Conditions
 */

enum class ObservationViewTypes {
    OBSERVATION,
    CONDITION;

    companion object {
        fun from(ordinal: Int): ObservationViewTypes {
            return values()[ordinal]
        }
    }

}


/***
 *Only display patient header*/
enum class ViewType {
    HEADER,
    PATIENT,
    CHILD,
    PATIENT_PROPERTY,
    RELATION,
    OBSERVATION,
    ENCOUNTER,
    CONDITION;

    companion object {
        fun from(ordinal: Int): ViewType {
            return values()[ordinal]
        }
    }

}