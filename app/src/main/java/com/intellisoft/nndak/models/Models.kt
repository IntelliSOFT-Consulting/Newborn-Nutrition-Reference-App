package com.intellisoft.nndak.models

import android.os.Parcel
import android.os.Parcelable
import com.intellisoft.nndak.viewmodels.RiskAssessmentItem

class SimpleNotification(
    val title: String,
    val content: String,
)

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

data class CodingObservation(
    val code: String,
    val display: String,
    val value: String,
)

data class QuantityObservation(
    val code: String,
    val display: String,
    val value: String,
    val unit: String,
)

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
    val quantity: String,
    val value: String,
    val encounterId: String
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
    val value: String,
    val status: String,
    val partOf: String
) {
    override fun toString(): String = code
}


data class NutritionItem(
    val id: String,
    val patient: String,
    val encounter: String,
    val status: String?,
) {
    override fun toString(): String = id
}


data class OrdersItem(
    val id: String,
    val resourceId: String,
    val patientId: String,
    val encounterId: String,
    val description: String,
    val status: String,
    val ipNumber: String? = "",
    val motherName: String? = "",
    val babyName: String? = "",
    val babyAge: String? = "",
    val dhmType: String? = "",
    val consentGiven: String? = "",
    val dhmReason: String? = "",
    val code: String? = "",
)

/**
 * Expressions History
 */
data class ExpressionHistory(
    val id: String,
    val resourceId: String,
    val date: String,
    val frequency: String,
    val timing: String,
    val massage: String,
    val anxious: String,
    val skinContact: String,
    val handExpression: String,
    val breastCondition: String,
    val milkVolume: String,
)

/**
 * Mother-Baby
 */

//make this a data class parcelable

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
    val contraindicated: String? = "",
    val totalExpressed: String? = "",
    val weights: MutableList<Int>? = null
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
    val assessed: Boolean = false

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
    val contra: String? = "",
) {
    override fun toString(): String = readiness
}

data class PrescriptionItem(
    val id: String? = "",
    val resourceId: String? = "",
    val hour: String? = "",
    val date: String? = "",
    val time: String? = "",
    val totalVolume: String? = "",
    val route: String? = "",
    val frequency: String? = "",
    val ivFluids: String? = "",
    val breastMilk: String? = "",
    val ebm: String? = "",
    val donorMilk: String? = "",
    val consent: String? = "",
    val consentDate: String? = "",
    val dhmReason: String? = "",
    val additionalFeeds: String? = "",
    val supplements: String? = "",
    val expressions: String? = "",
    val feedsGiven: String? = "",
    val cWeight: String? = "",
    val formula: String? = "",
    val deficit: String? = "",
    val feedingTime: String,
    val feed: List<FeedItem>? = null
) {
    override fun toString(): String = resourceId.toString()
}


data class DistributionItem(
    val feed: List<FeedItem>
)

data class DHMDashboardItem(
    val id: String? = "",
    val resourceId: String? = "",
    val dhmInfants: String? = "",
    val dhmVolume: String? = "",
    val dhmAverageVolume: String? = "",
    val dhmFullyInfants: String? = "",
    val dhmAverageLength: String? = "",
    val data: List<PieItem>? = null
) {
    override fun toString(): String = resourceId.toString()
}

data class MessageItem(
    val success: Boolean,
    val message: String
) {
    override fun toString(): String = message
}

data class Prescription(
    val currentWeight: String,
    val totalFeeds: String,
    val feedFrequency: String,
    val supplements: String,
    val additional: String,
    val data: List<FeedDataItem>
)

data class FeedingHistory(
    val hour: String,
    val date: String,
    val time: String,
    val ebm: String,
    val dhm: String,
    val iv: String,
    val deficit: String,
    val vomit: String,
    val diaper: String,
    val stool: String,
)

data class PositioningHistory(
    val date: String,
    val hands: String,
    val mum: String,
    val baby: String,
    val attach: String,
    val suckle: String
)

data class BreastsHistory(
    val date: String,
    val interest: String,
    val cues: String,
    val sleep: String,
    val bursts: String,
    val shortFeed: String,
    val longSwallow: String,
    val skin: String,
    val nipples: String,
    val shape: String
)

data class FeedItem(
    val id: String? = "",
    val idAlt: String? = "",
    val resourceId: String? = "",
    val type: String? = "",
    val route: String? = "",
    val typeAlt: String? = "",
    val routeAlt: String? = "",
    val volume: String? = "",
    val frequency: String? = "",
    val frequencyAlt: String? = "",
    val logicalId: String? = "",
    val specific: String? = ""
) {
    override fun toString(): String = type.toString()
}

data class PieItem(
    val value: String,
    val label: String,
    val color: String
)

data class StaticCharts(
    val feeds: List<PieItem>,
    val times: List<PieItem>,
)

data class CareItem(
    val resourceId: String,
    val patientId: String,
    val encounterId: String,
    val status: String,
    val created: String,
    val purpose: String,
) {
    override fun toString(): String = resourceId
}

data class FeedDataItem(
    val code: String,
    val title: String,
    val value: String,
    val coding: Boolean
)

data class ScheduleTime(
    val hour: String,
)

data class DischargeItem(
    val resourceId: String,
    val date: String,
    val outcome: String,
    val reason: String,
    val weight: String,
    val notes: String
)
//create a parcelable data class

data class DataDisplay(
    val babyName: String,
    val status:String,
    val motherName:String,
    val birthWeight:String,
    val apgarScore:String,
    val motherIp:String,
    val babyWell:String,
    val dateOfBirth:String,
    val dayOfLife:String,
    val dateOfAdm:String,
    val neonatalSepsis:String,
    val jaundice:String,
    val asphyxia:String,

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",

    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(babyName)
        parcel.writeString(status)
        parcel.writeString(motherName)
        parcel.writeString(birthWeight)
        parcel.writeString(apgarScore)
        parcel.writeString(motherIp)
        parcel.writeString(babyWell)
        parcel.writeString(dateOfBirth)
        parcel.writeString(dayOfLife)
        parcel.writeString(dateOfAdm)
        parcel.writeString(neonatalSepsis)
        parcel.writeString(jaundice)
        parcel.writeString(asphyxia)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DataDisplay> {
        override fun createFromParcel(parcel: Parcel): DataDisplay {
            return DataDisplay(parcel)
        }

        override fun newArray(size: Int): Array<DataDisplay?> {
            return arrayOfNulls(size)
        }
    }


}