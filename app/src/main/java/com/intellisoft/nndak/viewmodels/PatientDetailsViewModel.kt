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
import com.intellisoft.nndak.charts.ActualData
import com.intellisoft.nndak.charts.ExpressionData
import com.intellisoft.nndak.charts.MilkExpression
import com.intellisoft.nndak.charts.WeightsData
import com.intellisoft.nndak.helper_class.FormatHelper
import com.intellisoft.nndak.logic.Logics.Companion.ADJUST_PRESCRIPTION
import com.intellisoft.nndak.logic.Logics.Companion.ADMISSION_WEIGHT
import com.intellisoft.nndak.logic.Logics.Companion.CURRENT_WEIGHT
import com.intellisoft.nndak.logic.Logics.Companion.DHM_VOLUME
import com.intellisoft.nndak.logic.Logics.Companion.DIAPER_CHANGED
import com.intellisoft.nndak.logic.Logics.Companion.EBM
import com.intellisoft.nndak.logic.Logics.Companion.EBM_VOLUME
import com.intellisoft.nndak.logic.Logics.Companion.EXPRESSED_MILK
import com.intellisoft.nndak.logic.Logics.Companion.EXPRESSION_TIME
import com.intellisoft.nndak.logic.Logics.Companion.FEEDS_DEFICIT
import com.intellisoft.nndak.logic.Logics.Companion.FEEDS_TAKEN
import com.intellisoft.nndak.logic.Logics.Companion.IV_VOLUME
import com.intellisoft.nndak.logic.Logics.Companion.REMARKS
import com.intellisoft.nndak.logic.Logics.Companion.STOOL
import com.intellisoft.nndak.logic.Logics.Companion.VOMIT
import com.intellisoft.nndak.models.*
import com.intellisoft.nndak.utils.Constants.MAX_RESOURCE_COUNT
import com.intellisoft.nndak.utils.Constants.MIN_RESOURCE_COUNT
import com.intellisoft.nndak.utils.getPastDaysOnIntervalOf
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
    val liveFeedingData = MutableLiveData<List<PrescriptionItem>>()
    val liveWeights = MutableLiveData<WeightsData>()
    val liveExpressions = MutableLiveData<MilkExpression>()
    val context: Application = application


    fun feedsDistribution() {
        viewModelScope.launch { liveFeeds.value = getFeedsDataModel() }
    }

    fun activeBabyWeights() {
        viewModelScope.launch { liveWeights.value = getWeightsDataModel() }
    }

    fun getExpressions() {
        viewModelScope.launch { liveExpressions.value = getExpressionDataModel() }
    }

    private suspend fun getExpressionDataModel(): MilkExpression {

        val expressions: MutableList<ExpressionData> = mutableListOf()
        val intervals = getPastHoursOnIntervalOf(8, 3)
        var totalFeed = 0f
        intervals.forEach {
            val milk = getHourlyExpressions(it.toString())
            totalFeed += milk.amount.toFloat()
            expressions.add(milk)
        }
        return MilkExpression(totalFeed = "$totalFeed", varianceAmount = "5", data = expressions)
    }

    private suspend fun getHourlyExpressions(time: String): ExpressionData {
        var quantity = 0f
        val expressions = observationsPerCode(EXPRESSION_TIME)
        val sorted = sortCollected(expressions)
        sorted.forEach {
            try {
                val actualTime = FormatHelper().getDateHourZone(it.value.trim())

                val currentTime = FormatHelper().getDateHour(time)
                val maxThree = FormatHelper().getHourRange(currentTime)

                val isWithinRange = FormatHelper().isWithinRange(actualTime, currentTime, maxThree)
                if (isWithinRange) {
                    val amounts = observationsPerCodeEncounter(EXPRESSED_MILK, it.encounterId)
                    amounts.forEach { data ->
                        val qty = data.quantity.toFloat()
                        quantity += qty
                    }
                }
            } catch (e: Exception) {

            }
        }
        val refinedTime = FormatHelper().getHour(time)

        return ExpressionData(time = refinedTime, amount = quantity.toString())

    }

    private suspend fun getWeightsDataModel(): WeightsData {
        val patient = getPatient()
        val weight = pullWeights()
        val data = arrayListOf<ActualData>()
        var dayOfLife = ""
        patient.let {
            dayOfLife = getFormattedAge(it.dob)

        }

        if (weight.isNotEmpty()) {

            val daysString = getPastDaysOnIntervalOf(dayOfLife.toInt(), 1)
            for ((i, entry) in daysString.withIndex()) {
                val sorted = sortCollected(weight)
                val value = extractDailyMeasure(entry, sorted)
                // val day =FormatHelper().getSimpleDate(entry.toString())
                Timber.e("DayOfLife Date $entry Values $value ")
            }
        }

        return WeightsData("200 kg", data = data)

    }

    private fun extractDailyMeasure(entry: LocalDate, sorted: List<ObservationItem>): String {
//        var value = "0 gm"

        sorted.forEach {
            val day = FormatHelper().getSimpleDate(it.effective)
            Timber.e("Day $day  ${it.value}")
            /* if (day == entry.toString()) {
                 value = it.value
                 Timber.e("Simple $day Value ${it.value}")
             } else {
                 value = "0 gm"
             }*/
        }
        return sorted.findLast { FormatHelper().getSimpleDate(it.effective) == entry.toString() }?.value
            ?: sorted.find { FormatHelper().getSimpleDate(it.effective) == entry.toString() }?.value
            ?: "0 gm"
    }


    private fun getFeedsDataModel(): DistributionItem {
        val intervals = getPastHoursOnIntervalOf(8, 3)
        val times: MutableList<String> = mutableListOf()
        val feeds: MutableList<FeedItem> = mutableListOf()
        intervals.forEach {
            val time = FormatHelper().getHour(it.toString())
            val dayTime = FormatHelper().getDateHour(it.toString())
            times.add(time)
            feeds.add(loadFeed(dayTime))
        }

        return DistributionItem(time = times, feed = feeds)
    }

    private fun loadFeed(time: String): FeedItem {
        Timber.e("Feeding Time $time")
        val iv = (13 until 50).random().toString()
        val ebm = (13 until 50).random().toString()
        val dhm = (13 until 50).random().toString()

        return FeedItem(volume = iv, route = ebm, frequency = dhm)
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

    private suspend fun observationsPerCodeEncounter(
        key: String,
        encounterId: String
    ): List<ObservationItem> {
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
                filter(Observation.ENCOUNTER, { value = encounterId })
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

    fun getFeedingInstances(careId: String) {
        viewModelScope.launch {
            liveFeedingData.value = getFeedingInstancesDataModel(context, careId)
        }
    }

    private suspend fun getFeedingInstancesDataModel(
        context: Application,
        careId: String
    ): List<PrescriptionItem> {
        val data: MutableList<PrescriptionItem> = mutableListOf()
        val pres = fetchCarePlans(careId)
        Timber.e("Care Provide ${pres.size}")
        pres.forEach { item ->
            data.add(feedsTaken(item))
        }
        return data
    }

    private suspend fun getCurrentPrescriptionsDataModel(context: Application): List<PrescriptionItem> {
        val prescriptions: MutableList<PrescriptionItem> = mutableListOf()
        val pres = fetchActiveCarePlans()
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

    private suspend fun fetchActiveCarePlans(): List<CareItem> {
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

    private suspend fun fetchCarePlans(careId: String): List<CareItem> {
        val cares: MutableList<CareItem> = mutableListOf()
        fhirEngine
            .search<CarePlan> {
                filter(
                    CarePlan.SUBJECT,
                    { value = "Patient/$patientId" })
                filter(CarePlan.PART_OF, { value = "CarePlan/$careId" })
                sort(CarePlan.DATE, Order.DESCENDING)
            }
            .take(MAX_RESOURCE_COUNT)
            .map {
                createCarePlanItem(
                    it,
                    getApplication<Application>().resources
                )
            }
            .let { cares.addAll(it) }
        return cares
    }

    private suspend fun feedsTaken(care: CareItem): PrescriptionItem {
        val date = try {
            FormatHelper().extractDateString(care.created)
        } catch (e: Exception) {
            care.created
        }
        val time = try {
            FormatHelper().extractTimeString(care.created)
        } catch (e: Exception) {
            care.created
        }
        val observations = getReferencedObservations(care.encounterId)
        val taken = observations.firstOrNull()
        return PrescriptionItem(
            id = care.resourceId,
            resourceId = care.resourceId,
            date = date,
            time = time,
            totalVolume = extractValue(observations, FEEDS_TAKEN),
            route = extractValue(observations, DIAPER_CHANGED),
            ivFluids = extractValue(observations, IV_VOLUME),
            breastMilk = extractValue(observations, EBM_VOLUME),
            donorMilk = extractValue(observations, DHM_VOLUME),
            consent = extractValue(observations, FEEDS_DEFICIT),
            dhmReason = extractValue(observations, ADJUST_PRESCRIPTION),
            supplements = extractValue(observations, VOMIT),
            consentDate = extractValue(observations, STOOL),
            additionalFeeds = extractValue(observations, REMARKS),
//            consentDate = consentDate,
//            feed = feeds,
//            feedsGiven = givenFeeds,
//            expressions = expressions.toString()

        )
    }

    private fun extractValue(observations: List<ObservationItem>, code: String): String {
        val value = observations.find { it.code == code }?.value
        return value.toString()
    }

    // private suspend fun prescription(encounterItem: EncounterItem): PrescriptionItem {
    private suspend fun prescription(care: CareItem): PrescriptionItem {
        Timber.e("Encounter Item ${care.encounterId}")
        val observations = getReferencedObservations(care.encounterId)
        Timber.e("Encounter Item $observations")

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
            id = care.encounterId,
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
                "$value",
                valueString,
                observation.encounter.reference
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
            var date = if (care.hasCreated()) {
                care.created.toString()
            } else {
                resources.getString(R.string.message_no_datetime)
            }

            return CareItem(
                care.logicalId,
                care.subject.reference,
                care.encounter.reference,
                care.status.toString(), date
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
