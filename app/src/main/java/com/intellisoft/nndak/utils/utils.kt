package com.intellisoft.nndak.utils

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.intellisoft.nndak.R
import com.intellisoft.nndak.helper_class.FormatHelper
import com.intellisoft.nndak.utils.Constants.CORNER_RADIUS
import com.intellisoft.nndak.utils.Constants.FILL_COLOR
import com.intellisoft.nndak.utils.Constants.STROKE_COLOR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Address
import org.hl7.fhir.r4.model.ContactPoint
import org.hl7.fhir.r4.model.HumanName
import timber.log.Timber
import java.io.File
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.util.*
import java.util.regex.Pattern

fun showPicker(context: Context, input: TextInputEditText) {
    input.setOnClickListener {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(
            context,
            { view, myear, mmonth, mdayOfMonth ->
                val mon = mmonth + 1
                val msg = "$mdayOfMonth/$mon/$myear"
                input.setText(msg)
            },
            year,
            month,
            day
        )
        datePickerDialog.datePicker.maxDate = Date().time
        datePickerDialog.show()
    }
}


fun listenPlainChanges(
    input: TextInputEditText,
    inputLayout: TextInputLayout,
    error: String
) {

    CoroutineScope(Dispatchers.Default).launch {
        input.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(editable: Editable) {
                try {
                    if (editable.toString().isNotEmpty()) {
                        val newValue = editable.toString()
                        input.removeTextChangedListener(this)
                        val position: Int = input.selectionEnd
                        input.setText(newValue)
                        if (position > (input.text?.length ?: 0)) {
                            input.text?.let { input.setSelection(it.length) }
                        } else {
                            input.setSelection(position);
                        }
                        input.addTextChangedListener(this)
                        inputLayout.error = null
                    } else {
                        inputLayout.error = error
                    }
                } catch (e: Exception) {

                }
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {

            }
        })
    }
}

fun deleteCache(context: Context) {
    try {
        val dir: File = context.cacheDir
        deleteDir(dir)
    } catch (e: Exception) {
    }
}

fun deleteDir(dir: File?): Boolean {
    return if (dir != null && dir.isDirectory) {
        val children: Array<String> = dir.list()
        for (i in children.indices) {
            val success = deleteDir(File(dir, children[i]))
            if (!success) {
                return false
            }
        }
        dir.delete()
    } else if (dir != null && dir.isFile) {
        dir.delete()
    } else {
        false
    }
}

fun boldText(textView: TextView) {
    textView.setTypeface(null, Typeface.BOLD)
}

fun isTablet(ctx: Context): Boolean {
    return ctx.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
}

fun formatTime(values: List<LocalDateTime>): ArrayList<String> {
    val days = ArrayList<String>()
    values.forEach {
        val time = FormatHelper().getHourNoExtension(it.toString())
        days.add(time)
    }
    return days

}

fun formatDate(values: List<LocalDate>): ArrayList<String> {
    val days = ArrayList<String>()
    values.forEach {
        val format = FormatHelper().getRefinedDate(it.toString())
        days.add(format)
    }
    return days
}


fun formatMonths(values: List<LocalDate>): ArrayList<String> {
    val days = ArrayList<String>()
    values.forEach {
        val format = FormatHelper().getMonthName(it.toString())
        days.add(format)
    }
    return days
}

fun loadTime(values: List<LocalDateTime>): ArrayList<String> {
    val days = ArrayList<String>()
    values.forEach {
        val time = FormatHelper().getHour(it.toString())
        days.add(time)
    }
    return days

}

fun extractUnits(value: String): String {
    val code = value.split("\\.".toRegex()).toTypedArray()
    return code[0]
}

fun getPastHoursOnIntervalOf(times: Int, interval: Int): List<LocalDateTime> {
    val list: MutableList<LocalDateTime> = ArrayList()
    var date = LocalDateTime.now()
    for (i in 1..times) {
        list.add(date)
        date = date.minusHours(interval.toLong())
    }
    return list.reversed()
}


fun getPastHoursOnIntervalOfWithStart(
    start: String,
    times: Int,
    interval: Int
): List<String> {
    val list: MutableList<String> = ArrayList()
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH)
    val date = sdf.parse(start)
    val calendar: Calendar = GregorianCalendar()
    calendar.time = date
    var feed: String
    for (i in 1..times) {
        calendar.add(Calendar.HOUR, -interval)
        feed = sdf.format(calendar.time)
        list.add(feed)
    }
    return list
}

fun getFutureHoursOnIntervalOfWithStart(
    start: String,
    times: Int,
    interval: Int
): List<String> {
    val list: MutableList<String> = ArrayList()
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH)
    val date = sdf.parse(start)
    val calendar: Calendar = GregorianCalendar()
    calendar.time = date
    var feed: String
    val max = times - 1
    for (i in 0..max) {
        feed = sdf.format(calendar.time)
        list.add(feed)
        calendar.add(Calendar.HOUR, interval)
    }
    return list
}

fun formatFeedingTime(values: List<LocalDateTime>): ArrayList<String> {
    val days = ArrayList<String>()
    values.forEach {
        val time = FormatHelper().getHour(it.toString())
        days.add(time)
    }
    return days

}

fun getPastDaysOnIntervalOf(times: Int, interval: Int): List<LocalDate> {
    val list: MutableList<LocalDate> = ArrayList()
    var date = LocalDate.now()
    for (i in 1..times) {
        list.add(date)
        date = date.minusDays(interval.toLong())
    }
    return list.reversed()
}


fun getWeeksSoFarIntervalOf(start: String, times: Int, interval: Int): List<LocalDate> {
    val list: MutableList<LocalDate> = ArrayList()
    var date = LocalDate.parse(start)
    for (i in 1..times) {
        list.add(date)
        date = date.plusWeeks(interval.toLong())
    }
    return list
}


fun getPastMonthsOnIntervalOf(times: Int, interval: Int): List<LocalDate> {
    val list: MutableList<LocalDate> = ArrayList()
    var date = LocalDate.now()
    for (i in 1..times) {
        list.add(date)
        date = date.minusMonths(interval.toLong())
    }
    return list.reversed()
}

fun dimOption(imageView: ImageView, color: String) {
    ImageViewCompat.setImageTintMode(imageView, PorterDuff.Mode.SRC_ATOP)
    ImageViewCompat.setImageTintList(
        imageView,
        ColorStateList.valueOf(Color.parseColor(color))
    )

}

fun setSystemBarColor(act: Activity, @ColorRes color: Int) {
    val window: Window = act.window
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    window.statusBarColor = act.resources.getColor(color)
}

fun isNetworkAvailable(context: Context?): Boolean {
    if (context == null) return false
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
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
