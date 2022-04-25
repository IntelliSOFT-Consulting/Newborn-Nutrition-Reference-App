package com.intellisoft.nndak.models

import android.content.res.Resources
import com.intellisoft.nndak.viewmodels.RiskAssessmentItem

data class PatientItem(
    val id: String,
    val resourceId: String,
    val name: String,
    val gender: String,
    val dob: String,
    val phone: String,
    val city: String,
    val country: String,
    val isActive: Boolean,
    val html: String,
    var risk: String? = "",
    var riskItem: RiskAssessmentItem? = null,
    var state: String
) {
    override fun toString(): String = name


}


data class RelatedPersonItem(
    val id: String,
    val name: String,
    val gender: String,
    val dob: String
) {
    override fun toString(): String = name
}


data class ObservationItem(
    val id: String,
    val code: String,
    val effective: String,
    val value: String
) {
    override fun toString(): String = code
}

data class ConditionItem(
    val id: String,
    val code: String,
    val effective: String,
    val value: String
) {
    override fun toString(): String = code
}
data class DbObservations(
    val value: String,
    val title:String
)