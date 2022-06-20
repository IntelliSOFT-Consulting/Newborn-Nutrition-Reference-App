package com.intellisoft.nndak.viewmodels

import android.app.Application
import android.content.res.Resources
import android.os.Build
import androidx.lifecycle.*
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.logicalId
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.search
import com.intellisoft.nndak.R
import com.intellisoft.nndak.helper_class.FormatHelper
import com.intellisoft.nndak.logic.Logics.Companion.ADMISSION_WEIGHT
import com.intellisoft.nndak.logic.Logics.Companion.CURRENT_WEIGHT
import com.intellisoft.nndak.logic.Logics.Companion.EBM
import com.intellisoft.nndak.logic.Logics.Companion.FEEDS_TAKEN
import com.intellisoft.nndak.models.*
import com.intellisoft.nndak.utils.Constants.MAX_RESOURCE_COUNT
import com.intellisoft.nndak.utils.Constants.MIN_RESOURCE_COUNT
import com.intellisoft.nndak.utils.getFormattedAge
import com.intellisoft.nndak.utils.getPastHoursOnIntervalOf
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.util.*

/**
 * The ViewModel helper class for PatientItemRecyclerViewAdapter, that is responsible for preparing
 * data for UI.
 */
class PatientDetailsViewModel(
    application: Application,
    private val fhirEngine: FhirEngine,
    private val patientId: String
) : AndroidViewModel(application) {
    val liveMumChild = MutableLiveData<MotherBabyItem>()
    val liveOrder = MutableLiveData<OrdersItem>()
    val liveFeeds = MutableLiveData<DistributionItem>()
    val livePrescriptionsData = MutableLiveData<List<PrescriptionItem>>()
    val context: Application = application


    fun feedsDistribution() {
        viewModelScope.launch { liveFeeds.value = getFeedsDataModel() }
    }

    private fun getFeedsDataModel(): DistributionItem {
        val intervals = getPastHoursOnIntervalOf(8, 3)
        val times: MutableList<String> = mutableListOf()
        val feeds: MutableList<FeedItem> = mutableListOf()
        intervals.forEach {
            val time = FormatHelper().getHour(it.toString())
            times.add(time)
            feeds.add(loadFeed(time))
        }

        return DistributionItem(time = times, feed = feeds)
    }

    private fun loadFeed(time: String): FeedItem {

        return FeedItem(volume = "200")
    }


    fun getMumChild() {

        viewModelScope.launch { liveMumChild.value = getMumChildDataModel(context) }
    }

    private suspend fun getPatient(): PatientItem {
        val patient = fhirEngine.load(Patient::class.java, patientId)
        return patient.toPatientItem(0)
    }

    private suspend fun getPatientEncounters(): List<EncounterItem> {
        val encounters: MutableList<EncounterItem> = mutableListOf()
        fhirEngine
            .search<Encounter> {
                filter(Encounter.SUBJECT, { value = "Patient/$patientId" })
                sort(Encounter.DATE, Order.DESCENDING)
            }
            .map { createEncounterItem(it, getApplication<Application>().resources) }

            .let { encounters.addAll(it) }

        return encounters.reversed()
    }


    private suspend fun pullWeights(): List<ObservationItem> {
        val observations: MutableList<ObservationItem> = mutableListOf()
        fhirEngine
            .search<Observation> {
                filter(Observation.SUBJECT, { value = "Patient/$patientId" })
                sort(Observation.DATE, Order.ASCENDING)
            }
            .map { createObservationItem(it, getApplication<Application>().resources) }
            .filter { it.code == ADMISSION_WEIGHT || it.code == CURRENT_WEIGHT }
            .let { observations.addAll(it) }
        return observations
    }

    private suspend fun getObservations(): List<ObservationItem> {
        val observations: MutableList<ObservationItem> = mutableListOf()
        fhirEngine
            .search<Observation> {
                filter(Observation.SUBJECT, { value = "Patient/$patientId" })
                sort(Observation.DATE, Order.ASCENDING)
            }
            .map { createObservationItem(it, getApplication<Application>().resources) }
            .let { observations.addAll(it) }
        return observations
    }


    private fun <T> concatenate(vararg lists: List<T>): List<T> {
        val result: MutableList<T> = ArrayList()
        lists.forEach { list: List<T> -> result.addAll(list) }
        return result
    }

    private suspend fun getMumChildDataModel(
        context: Application,
    ): MotherBabyItem {
        val feeds: MutableList<FeedItem> = mutableListOf()
        val patient = getPatient()
        val mum = getMother(patientId)
        val mumName = mum.first.toString()
        val mumIp = mum.second.toString()
        val babyWell = retrieveCode("71195-2")
        val asphyxia = retrieveCode("45735-8")
        val jaundice = retrieveCode("45736-6")
        val sepsis = retrieveCode("45755-8")
        val breastProblems = retrieveCode("Breast-Problem")
        val mumLocation = retrieveCode("Mother-Location")
        val contra = retrieveCode("Mother-Contraindicated")
        val breastfeeding = retrieveCode("Baby-BreastFeeding")
        val mumWell = retrieveCode("Mother-Well")

        var birthWeight = ""
        var status = ""
        var gestation = ""
        var apgar = ""
        val gainRate = "Normal"
        var admDate = ""
        var cWeight = ""
        var dMethod = ""
        var parity = ""
        var pmtct = ""
        var mPreg = ""
        var dDate = ""
        var motherMilk = "0 ml"
        val exp = getPatientEncounters()
        var i: Int = 0
        if (exp.isNotEmpty()) {
            for (element in exp) {
                if (element.code == "Milk Expression") {
                    i++
                }
            }
        }

        val obs = getObservations()

        val sample = pullWeights()
        val weights: MutableList<Int> = mutableListOf()

        val refined = sortCollected(sample)

        if (refined.isNotEmpty()) {
            cWeight = refined.last().value
            for (element in refined) {
                val code = element.value.split("\\.".toRegex()).toTypedArray()
                weights.add(code[0].toInt())
            }
        }

        if (obs.isNotEmpty()) {

            for (element in obs) {

                if (element.code == "Feeding-Frequency") {
                    feeds.add(FeedItem("test", "test", "test", "test", "test", "test"))
                }
                if (element.code == "93857-1") {
                    Timber.e("Delivery Date $dDate")
                    dDate = element.value.substring(0, 10)
                }
                if (element.code == "55277-8") {
                    pmtct = element.value
                }
                if (element.code == "64708-1") {
                    mPreg = element.value
                }
                if (element.code == "72149-8") {
                    dMethod = element.value
                }
                if (element.code == "45394-4") {
                    parity = "P${element.value}"
                }
                if (element.code == "8339-4") {
                    birthWeight = element.value
                    val code = element.value.split("\\.".toRegex()).toTypedArray()
                    birthWeight = if (code[0].toInt() < 2500) {
                        "$birthWeight (gm)-Low"
                    } else {
                        "$birthWeight (gm)-Normal"
                    }
                }
                if (element.code == "52455-3") {
                    admDate = element.value.substring(0, 10)
                }

                if (element.code == "9273-4") {
                    apgar = element.value
                }
                if (element.code == "Breast-Milk") {
                    motherMilk = element.value
                }

                if (element.code == "11885-1") {
                    val code = element.value.split("\\.".toRegex()).toTypedArray()
                    status = try {
                        if (code[0].toInt() < 37) {
                            "Preterm"
                        } else {
                            "Term"
                        }
                    } catch (e: Exception) {
                        "Preterm"
                    }
                    gestation = element.value
                }
            }
        }
        val total = calculateTotalExpressedMilk()

        var name = ""
        var dateOfBirth = ""
        var dayOfLife = ""


        patient.let {
            name = it.name
            dateOfBirth = it.dob
            dayOfLife = getFormattedAge(it.dob)

        }
        return MotherBabyItem(
            patientId,
            patientId,
            name,
            mumName,
            mumIp,
            patientId,
            birthWeight,
            status,
            gainRate,
            dashboard = BabyDashboard(
                gestation = gestation,
                apgarScore = apgar,
                babyWell = babyWell,
                neonatalSepsis = sepsis,
                asphyxia = asphyxia,
                jaundice = jaundice,
                dateOfBirth = dateOfBirth,
                dayOfLife = dayOfLife,
                dateOfAdm = admDate,
                cWeight = cWeight,
                motherMilk = motherMilk
            ),
            mother = MotherDashboard(
                parity = parity,
                deliveryMethod = dMethod,
                pmtctStatus = pmtct,
                multiPregnancy = mPreg,
                deliveryDate = dDate,
                motherLocation = mumLocation,
                motherStatus = mumWell
            ),
            assessment = AssessmentItem(
                breastProblems = breastProblems,
                breastfeedingBaby = breastfeeding,
                weights = weights,
                totalExpressed = total,
                contraindicated = contra
            )
        )
    }

    private suspend fun retrieveCode(code: String): String {
        var data = ""
        val obs = observationsPerCode(code)
        if (obs.isNotEmpty()) {
            val sort = sortCollected(obs)
            data = sort.last().value.trim()
            Timber.e("Retrieved  Code ${sort.last().code} Data $data")
        }
        return data
    }

    private suspend fun pullFeeds(): String {
        var total = "0 mls"
        val obs = observationsPerCode(FEEDS_TAKEN)
        if (obs.isNotEmpty()) {
            val sorted = sortCollected(obs)
            total = sorted.last().value
        }
        return total
    }

    private suspend fun calculateTotalExpressedMilk(): String {
        var volume = "0 mls"

        val single: MutableList<Int> = mutableListOf()
        val obs = observationsPerCode(EBM)
        if (obs.isNotEmpty()) {
            obs.forEach {
                val code = it.value.split("\\.".toRegex()).toTypedArray()
                single.add(code[0].toInt())
            }

            volume = single.sum().toString()
        }
        return "$volume mls"
    }

    private suspend fun observationsPerCode(key: String): List<ObservationItem> {

        val obs: MutableList<ObservationItem> = mutableListOf()
        fhirEngine
            .search<Observation> {
                filter(
                    Observation.CODE,
                    {
                        value = of(Coding().apply {
                            system = "http://snomed.info/sct"
                            code = key
                        })
                    })
                filter(Observation.SUBJECT, { value = "Patient/$patientId" })
                sort(Observation.DATE, Order.DESCENDING)
            }
            .take(MIN_RESOURCE_COUNT)
            .map {
                createObservationItem(
                    it,
                    getApplication<Application>().resources
                )
            }
            .let { obs.addAll(it) }
        return obs
    }

    private fun sortCollected(data: List<ObservationItem>): List<ObservationItem> {

        val sortedList = data.sortedWith(compareBy { it.effective })

        sortedList.forEach {
            Timber.e("Refined Data::::: ${it.value} Time:::: ${it.effective} ${it.id}")
        }
        return sortedList
    }

    private fun getFormattedAge(
        dob: String
    ): String {
        if (dob.isEmpty()) return ""
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Period.between(LocalDate.parse(dob), LocalDate.now()).let {
                when {
                    it.years > 0 -> it.years.toString()
                    it.months > 0 -> it.months.toString()
                    else -> it.days.toString()
                }
            }
        } else {
            ""
        }
    }

    private suspend fun getMother(patientId: String): Triple<String?, String?, String?> {
        val mother: MutableList<PatientItem> = mutableListOf()
        fhirEngine
            .search<Patient> {
                filter(
                    Patient.LINK, { value = "Patient/${patientId}" }

                )
            }
            .take(MAX_RESOURCE_COUNT)
            .mapIndexed { index, fhirPatient ->
                fhirPatient.toPatientItem(
                    index + 1
                )
            }
            .let { mother.addAll(it) }
        if (mother.isNotEmpty()) {
            return Triple(mother[0].name, mother[0].resourceId, mother[0].id)

        }
        return Triple(null, null, null)
    }

    fun getCurrentPrescriptions() {
        viewModelScope.launch {
            livePrescriptionsData.value = getCurrentPrescriptionsDataModel(context)
        }
    }

    private suspend fun getCurrentPrescriptionsDataModel(context: Application): List<PrescriptionItem> {
        val prescriptions: MutableList<PrescriptionItem> = mutableListOf()
        val pres = fetchCarePlans()
        Timber.e("Care Provide ${pres.size}")
        pres.forEach { item ->
            prescriptions.add(prescription(item))
        }
//        val encounters = getPatientEncounters()
//        if (encounters.isNotEmpty()) {
//
//            var item = encounters.firstOrNull {
//                it.code == "Feeds Prescription"
//            }
//            if (item != null) {
//                prescriptions.add(prescription(item))
//            }
//            for (element in encounters) {
//                if (element.code == "Feeds Prescription") {
//                    prescriptions.add(prescription(element))
//                }
//            }
//        }
        return prescriptions

    }

    private suspend fun fetchCarePlans(): List<CareItem> {
        val cares: MutableList<CareItem> = mutableListOf()
        fhirEngine
            .search<CarePlan> {
                filter(
                    CarePlan.SUBJECT,
                    { value = "Patient/$patientId" })
                sort(CarePlan.DATE, Order.DESCENDING)
            }
            .take(MAX_RESOURCE_COUNT)
            .map {
                createCarePlanItem(
                    it,
                    getApplication<Application>().resources
                )
            }
            .filter { it.status == CarePlan.CarePlanStatus.ACTIVE.toString() }
            .let { cares.addAll(it) }
        return cares
    }

    // private suspend fun prescription(encounterItem: EncounterItem): PrescriptionItem {
    private suspend fun prescription(care: CareItem): PrescriptionItem {
        val observations = getReferencedObservations(care.encounterId)

        val feeds: MutableList<FeedItem> = mutableListOf()
        var date = ""
        var time = ""
        var total = ""
        var frequency = ""
        var route = ""
        var iv = "N/A"
        var bm = "N/A"
        var dhm = "N/A"
        var ebm = "N/A"
        var consent = "N/A"
        var reason = "N/A"
        var supplements = ""
        var additional = ""
        var consentDate = "N/A"
        var expressions = 0
        val givenFeeds = pullFeeds()

        /**
         * Expressions Count
         */
        val exp = getPatientEncounters()
        if (exp.isNotEmpty()) {
            for (ex in exp) {
                if (ex.code == "Milk Expression") {
                    expressions++
                }
            }
        }
        if (observations.isNotEmpty()) {
            for (element in observations) {
                Timber.e("Codes Found::: ${element.code}")
                if (element.code == "Prescription-Date") {
                    date = element.value.substring(0, 10)
                    time = element.value.substring(12, 19)
                }
                if (element.code == "Total-Feeds") {
                    total = element.value
                }
                if (element.code == "Breast-Feed-Frequency" || element.code == "DHM-Frequency" ||
                    element.code == "Formula-Frequency" || element.code == "EBM-Feeding-Frequency"
                ) {
                    frequency = element.value
                }
                if (element.code == "EBM-Feeding-Route" || element.code == "Formula-Route" || element.code == "DHM-Route") {
                    route = element.value
                }
                if (element.code == "IV-Fluid-Volume") {
                    iv = element.value
                }
                if (element.code == "Breast-Milk") {
                    bm = element.value
                }
                if (element.code == "DHM-Volume") {
                    dhm = element.value
                }
                if (element.code == "EBM-Volume") {
                    ebm = element.value
                }
                if (element.code == "Consent-Given") {
                    consent = element.value
                }
                if (element.code == "DHM-Reason") {
                    reason = element.value
                }
                if (element.code == "Supplements-Feeding") {
                    supplements = element.value
                }
                if (element.code == "Additional-Feeds") {
                    additional = element.value
                }
                if (element.code == "Consent-Date") {
                    consentDate = element.value
                }
            }
        }

        feeds.add(FeedItem())

        return PrescriptionItem(
            id = care.resourceId,
            resourceId = care.resourceId,
            date = date,
            time = time,
            totalVolume = total,
            frequency = frequency,
            route = route,
            ivFluids = iv,
            breastMilk = ebm,
            donorMilk = dhm,
            consent = consent,
            dhmReason = reason,
            supplements = supplements,
            additionalFeeds = additional,
            consentDate = consentDate,
            feed = feeds,
            feedsGiven = givenFeeds,
            expressions = expressions.toString()
        )
    }

    private suspend fun getObservationsPerEncounter(
        code: String,
    ): List<ObservationItem> {
        val conditions: MutableList<ObservationItem> = mutableListOf()
        fhirEngine
            .search<Observation> {
                filter(
                    Observation.ENCOUNTER,
                    { value = "Encounter/$code" })
                sort(Observation.VALUE_DATE, Order.DESCENDING)
            }
            .take(MAX_RESOURCE_COUNT)
            .map {
                createObservationItem(
                    it,
                    getApplication<Application>().resources
                )
            }
            .let { conditions.addAll(it) }
        return conditions
    }

    private suspend fun getReferencedObservations(
        code: String,
    ): List<ObservationItem> {
        val conditions: MutableList<ObservationItem> = mutableListOf()
        fhirEngine
            .search<Observation> {
                filter(
                    Observation.ENCOUNTER,
                    { value = code })
                sort(Observation.VALUE_DATE, Order.DESCENDING)
            }
            .take(MAX_RESOURCE_COUNT)
            .map {
                createObservationItem(
                    it,
                    getApplication<Application>().resources
                )
            }
            .let { conditions.addAll(it) }
        return conditions
    }

    fun getOrder(orderId: String) {

        viewModelScope.launch { liveOrder.value = getOrderDataModel(context, orderId) }
    }

    private suspend fun getMothersDetails(patientId: String): Triple<String?, String?, String?> {
        val mother: MutableList<PatientItem> = mutableListOf()
        fhirEngine
            .search<Patient> {
                filter(
                    Patient.LINK, { value = "Patient/$patientId" }

                )
            }
            .take(MAX_RESOURCE_COUNT)
            .mapIndexed { index, fhirPatient ->
                fhirPatient.toPatientItem(
                    index + 1
                )

            }
            .let { mother.addAll(it) }
        if (mother.isNotEmpty()) {
            return Triple(mother[0].name, mother[0].resourceId, mother[0].id)

        }
        return Triple(null, null, null)
    }

    private suspend fun getOrderDataModel(context: Application, orderId: String): OrdersItem {

        val baby = getPatient()
        val mother = getMothersDetails(patientId)
        val motherName = mother.first.toString()
        val motherIp = mother.second.toString()

        /**
         * Collect Observations from Encounter
         */
        var consent = ""
        var dhm = ""
        var reason = ""
        var dhmVolume = "N/A"
        val observations = getObservationsPerEncounter(orderId)
        if (observations.isNotEmpty()) {
            for (element in observations) {

                Timber.e("Option:::: ${element.code} ${element.value}")

                if (element.code == "Consent-Given") {
                    consent = element.value
                }
                if (element.code == "DHM-Type") {
                    dhm = element.value
                }
                if (element.code == "DHM-Volume") {
                    dhmVolume = element.value
                }
                if (element.code == "DHM-Reason") {
                    reason = element.value
                }
            }
        }

        return OrdersItem(
            id = orderId,
            resourceId = orderId,
            patientId = patientId,
            encounterId = orderId,
            ipNumber = motherIp,
            motherName = motherName,
            babyName = baby.name,
            babyAge = getFormattedAge(baby.dob),
            consentGiven = consent,
            dhmType = dhm,
            dhmReason = reason,
            description = dhmVolume,
            status = "active"
        )
    }


    companion object {
        /**
         * Creates RelatedPersonItem objects with displayable values from the Fhir RelatedPerson objects.
         */
        fun createRelatedPersonItem(
            relation: Patient,
            resources: Resources
        ): RelatedPersonItem {
            val gender = relation.gender ?: "Unknown"
            var dob = relation.birthDate ?: ""
            var name: String? = null
            var yea: String? = null

            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            if (dob.toString().isNotEmpty()) {
                yea = formatter.format(dob)
                dob = getFormattedAge(yea.toString(), resources)
            }
            name =
                if (relation.hasName()) relation.name[0].nameAsSingleString else relation.logicalId.substring(
                    0,
                    8
                )

            return RelatedPersonItem(
                relation.logicalId,
                name.toString(),
                gender.toString(),
                dob.toString()
            )
        }

        /**
         * Creates ObservationItem objects with displayable values from the Fhir Observation objects.
         */
        fun createObservationItem(
            observation: Observation,
            resources: Resources
        ): ObservationItem {
            val observationCode = observation.code.codingFirstRep.code ?: ""


            // Show nothing if no values available for datetime and value quantity.
            val dateTimeString =
                if (observation.hasIssued()) {
                    refineDateTime(
                        observation.issued.toString()
                    )
                } else {
                    resources.getText(R.string.message_no_datetime).toString()
                }
            val value =
                when {
                    observation.hasValueQuantity() -> {
                        observation.valueQuantity.value.toString()
                    }
                    observation.hasValueCodeableConcept() -> {
                        observation.valueCodeableConcept.coding.firstOrNull()?.display ?: ""
                    }
                    observation.hasNote() -> {
                        observation.note.firstOrNull()?.author
                    }
                    else -> {
                        observation.code.text ?: observation.code.codingFirstRep.display
                    }
                }
            val valueUnit =
                if (observation.hasValueQuantity()) {
                    observation.valueQuantity.unit ?: observation.valueQuantity.code
                } else {
                    ""
                }

            val valueString = "$value $valueUnit"

            return ObservationItem(
                observation.logicalId,
                observationCode,
                dateTimeString,
                valueString,
            )
        }

        private fun refineDateTime(toString: String): String {
            val good = try {
                FormatHelper().getRefinedDate(toString)
            } catch (e: Exception) {
                toString
            }

            return good
        }

        /**
         * Creates EncounterItem objects with displayable values from the Fhir Observation objects.
         */
        fun createCarePlanItem(
            care: CarePlan,
            resources: Resources
        ): CareItem {
            val status = care.status
            Timber.e("Care Status:::: $status")

            return CareItem(
                care.logicalId,
                care.subject.reference,
                care.encounter.reference, care.status.toString()
            )
        }

        fun createEncounterItem(
            encounter: Encounter,
            resources: Resources
        ): EncounterItem {

            val encounterCode = encounter.reasonCodeFirstRep.text ?: ""
            val value = if (encounter.meta.hasLastUpdated()) {
                encounter.meta.lastUpdatedElement.value.toString()
            } else {
                ""
            }
            return EncounterItem(
                encounter.logicalId,
                encounterCode,
                encounter.logicalId,
                value

            )
        }

        /**
         * Creates NutritionItem objects with displayable values from the Fhir Observation objects.
         */
        fun createNutritionItem(
            order: NutritionOrder,
            resources: Resources
        ): NutritionItem {
            val status = order.status ?: ""
            return NutritionItem(
                order.logicalId,
                order.patient.reference,
                order.encounter.reference,
                status.toString()

            )
        }

        /** Creates ConditionItem objects with displayable values from the Fhir Condition objects. */
        fun createConditionItem(
            condition: Condition,
            resources: Resources
        ): ConditionItem {
            val observationCode =
                condition.code.text ?: condition.code.codingFirstRep.display ?: ""

            // Show nothing if no values available for datetime and value quantity.
            val dateTimeString =
                if (condition.hasOnsetDateTimeType()) {
                    condition.onsetDateTimeType.asStringValue()
                } else {
                    resources.getText(R.string.message_no_datetime).toString()
                }
            val value =
                if (condition.hasVerificationStatus()) {
                    condition.verificationStatus.codingFirstRep.code
                } else {
                    ""
                }

            return ConditionItem(
                condition.logicalId,
                observationCode,
                dateTimeString,
                value
            )
        }


    }
}

interface PatientDetailData {
    val firstInGroup: Boolean
    val lastInGroup: Boolean
}

data class PatientDetailObservation(
    val observation: ObservationItem,
    override val firstInGroup: Boolean = false,
    override val lastInGroup: Boolean = false
) : PatientDetailData


data class PatientDetailCondition(
    val condition: ConditionItem,
    override val firstInGroup: Boolean = false,
    override val lastInGroup: Boolean = false
) : PatientDetailData


class PatientDetailsViewModelFactory(
    private val application: Application,
    private val fhirEngine: FhirEngine,
    private val patientId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(PatientDetailsViewModel::class.java)) {
            "Unknown ViewModel class"
        }
        return PatientDetailsViewModel(application, fhirEngine, patientId) as T
    }


}


data class RiskAssessmentItem(
    var riskStatusColor: Int,
    var riskStatus: String,
    var lastContacted: String,
    var patientCardColor: Int
)
