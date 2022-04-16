package com.intellisoft.nndak.utils

import android.content.res.ColorStateList
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.textfield.TextInputEditText
import com.intellisoft.nndak.utils.Constants.CORNER_RADIUS
import com.intellisoft.nndak.utils.Constants.FILL_COLOR
import com.intellisoft.nndak.utils.Constants.STROKE_COLOR
import org.hl7.fhir.r4.model.Address
import org.hl7.fhir.r4.model.ContactPoint
import org.hl7.fhir.r4.model.HumanName
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object Common {

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
        ).parse(dob)

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
}