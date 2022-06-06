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
    var region: String,
    var reference: String? = ""
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

data class OrdersItem(
    val id: String,
    val resourceId: String,
    val patientId: String,
    val ipNumber: String? = "",
    val motherName: String? = "",
    val babyName: String? = "",
    val babyAge: String? = "",
    val dhmType: String? = "",
    val consentGiven: String? = "",
    val dhmReason: String? = "",
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
    var dashboard: BabyDashboard,
    var mother: MotherDashboard,
    var assessment: AssessmentItem,

    ) {
    override fun toString(): String = babyName
}

data class AssessmentItem(
    val breastfeedingBaby: String? = "",
    val breastProblems: String? = "",
    val contraindicated: String? = ""
) {
    override fun toString(): String = breastfeedingBaby.toString()
}

data class BabyDashboard(
    val babyWell: String? = "",
    val dateOfBirth: String? = "",
    val dateOfAdm: String? = "",
    val dayOfLife: String? = "",
    val gestation: String? = "",
    val apgarScore: String? = "",
    val asphyxia: String? = "",
    val neonatalSepsis: String? = "",
    val jaundice: String? = "",
    val cWeight: String? = "",
    val motherMilk: String? = "",
    val prescription: PrescriptionItem

) {
    override fun toString(): String = babyWell.toString()
}

data class MotherDashboard(
    val parity: String? = "",
    val deliveryMethod: String? = "",
    val deliveryDate: String? = "",
    val motherStatus: String? = "",
    val pmtctStatus: String? = "",
    val multiPregnancy: String? = "",
    val motherLocation: String? = "",
    val asphyxia: String? = "",
    val neonatalSepsis: String? = "",
    val jaundice: String? = "",

    ) {
    override fun toString(): String = parity.toString()
}

data class DashboardItem(
    val count: Int,
    val positive: Boolean,
    val percent: String,
    val progress: Int
) {
    override fun toString(): String = percent
}

data class FeedingCuesTips(
    val readiness: String,
    val latch: String,
    val steady: String,
    val audible: String,
    val chocking: String,
    val softening: String? = "",
    val tenSide: String? = "",
    val threeHours: String? = "",
    val sixDiapers: String? = "",
) {
    override fun toString(): String = readiness
}

data class PrescriptionItem(
    val id: String? = "",
    val resourceId: String? = "",
    val date: String? = "",
    val time: String? = "",
    val totalVolume: String? = "",
    val route: String? = "",
    val frequency: String? = "",
    val ivFluids: String? = "",
    val breastMilk: String? = "",
    val donorMilk: String? = "",
    val consent: String? = "",
    val consentDate: String? = "",
    val dhmReason: String? = "",
    val additionalFeeds: String? = "",
    val supplements: String? = "",
    val expressions: String? = ""
) {
    override fun toString(): String = resourceId.toString()
}

data class DHMDashboardItem(
    val id: String? = "",
    val resourceId: String? = "",
    val dhmInfants: String? = "",
    val dhmVolume: String? = "",
    val dhmAverageVolume: String? = "",
    val dhmFullyInfants: String? = "",
    val dhmAverageLength: String? = "",
) {
    override fun toString(): String = resourceId.toString()
}

data class MessageItem(
    val success: Boolean,
    val message: String
) {
    override fun toString(): String = message
}