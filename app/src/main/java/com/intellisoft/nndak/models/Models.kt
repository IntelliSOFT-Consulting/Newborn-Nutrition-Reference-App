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
    var state: String,
    var district: String,
    var region: String
) {
    override fun toString(): String = name


}

data class BasicThree(
    val lmp: String,
    val edd: String,
    val gestation: String,
)

data class RelatedPersonItem(
    val id: String,
    val name: String,
    val gender: String,
    val dob: String,
    var riskItem: RiskAssessmentItem? = null,
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

data class EncounterItem(
    val id: String,
    val code: String,
    val effective: String,
    val value: String
) {
    override fun toString(): String = code
}


data class DbObservations(
    val value: String,
    val title: String
)

data class ApGar(
    val score: String,
    val message: String,
    val isSafe: Boolean
)

data class Steps(
    val fistIn: String?,
    val lastIn: String?,
    val secondButton: Boolean?
)


/**
 * Mother-Baby
 */

data class MotherBabyItem(
    val id: String,
    val resourceId: String,
    val babyName: String,
    val motherName: String,
    val motherIp: String,
    val babyIp: String,
    val birthWeight: String? = "",
    val status: String? = "",
    val gainRate: String? = "",
    var dashboard: BabyDashboard?

) {
    override fun toString(): String = babyName
}

data class BabyDashboard(
    val babyWell: String?="",
    val dateOfBirth: String?="",
    val dateOfAdm: String?="",
    val dayOfLife: String? = "",
    val gestation: String? = "",
    val apgarScore: String?="",
    val asphyxia: String?="",
    val neonatalSepsis: String?="",
    val jaundice: String?="",

    ) {
    override fun toString(): String = babyWell.toString()
}

data class MotherDashboard(
    val parity: String,
    val deliveryMethod: String,
    val deliveryDate: String,
    val motherStatus: String? = "",
    val pmtctStatus: String? = "",
    val multiPregnancy: String? = "",
    val motherLocation: String,
    val asphyxia: String,
    val neonatalSepsis: String,
    val jaundice: String,
    val shared: MotherBabyItem

) {
    override fun toString(): String = shared.babyName
}

data class DashboardItem(
    val count: Int,
    val positive: Boolean,
    val percent: String,
    val progress: Int
) {
    override fun toString(): String = percent
}