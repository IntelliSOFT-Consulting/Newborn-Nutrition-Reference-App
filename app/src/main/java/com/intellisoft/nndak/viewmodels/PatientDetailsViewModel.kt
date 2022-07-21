package com.intellisoft.nndak.viewmodels

import android.app.Application
import android.content.res.Resources
import android.os.Build
import androidx.lifecycle.*
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.logicalId
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.search
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.intellisoft.nndak.R
import com.intellisoft.nndak.charts.*
import com.intellisoft.nndak.helper_class.FormatHelper
import com.intellisoft.nndak.logic.DataSort.Companion.extractValueIndex
import com.intellisoft.nndak.logic.DataSort.Companion.extractWeeklyMeasure
import com.intellisoft.nndak.logic.DataSort.Companion.getWeekFromDays
import com.intellisoft.nndak.logic.DataSort.Companion.sortCollected
import com.intellisoft.nndak.logic.DataSort.Companion.sortHistory
import com.intellisoft.nndak.logic.DataSort.Companion.sortPrescriptions
import com.intellisoft.nndak.logic.Logics.Companion.ADDITIONAL_FEEDS
import com.intellisoft.nndak.logic.Logics.Companion.ADJUST_PRESCRIPTION
import com.intellisoft.nndak.logic.Logics.Companion.ADMISSION_DATE
import com.intellisoft.nndak.logic.Logics.Companion.ADMISSION_WEIGHT
import com.intellisoft.nndak.logic.Logics.Companion.APGAR_SCORE
import com.intellisoft.nndak.logic.Logics.Companion.ASPHYXIA
import com.intellisoft.nndak.logic.Logics.Companion.ASSESSMENT_DATE
import com.intellisoft.nndak.logic.Logics.Companion.BABY_ASSESSMENT
import com.intellisoft.nndak.logic.Logics.Companion.BABY_BREASTFEEDING
import com.intellisoft.nndak.logic.Logics.Companion.BABY_WELL
import com.intellisoft.nndak.logic.Logics.Companion.BIRTH_WEIGHT
import com.intellisoft.nndak.logic.Logics.Companion.BREASTS_FEEDING
import com.intellisoft.nndak.logic.Logics.Companion.BREAST_MILK
import com.intellisoft.nndak.logic.Logics.Companion.BREAST_PROBLEM
import com.intellisoft.nndak.logic.Logics.Companion.CONSENT_DATE
import com.intellisoft.nndak.logic.Logics.Companion.CURRENT_WEIGHT
import com.intellisoft.nndak.logic.Logics.Companion.DELIVERY_DATE
import com.intellisoft.nndak.logic.Logics.Companion.DELIVERY_METHOD
import com.intellisoft.nndak.logic.Logics.Companion.DHM_CONSENT
import com.intellisoft.nndak.logic.Logics.Companion.DHM_REASON
import com.intellisoft.nndak.logic.Logics.Companion.DHM_ROUTE
import com.intellisoft.nndak.logic.Logics.Companion.DHM_TYPE
import com.intellisoft.nndak.logic.Logics.Companion.DHM_VOLUME
import com.intellisoft.nndak.logic.Logics.Companion.DIAPER_CHANGED
import com.intellisoft.nndak.logic.Logics.Companion.EBM
import com.intellisoft.nndak.logic.Logics.Companion.EBM_ROUTE
import com.intellisoft.nndak.logic.Logics.Companion.EBM_VOLUME
import com.intellisoft.nndak.logic.Logics.Companion.EXPRESSED_MILK
import com.intellisoft.nndak.logic.Logics.Companion.EXPRESSIONS
import com.intellisoft.nndak.logic.Logics.Companion.EXPRESSION_TIME
import com.intellisoft.nndak.logic.Logics.Companion.FEEDING_FREQUENCY
import com.intellisoft.nndak.logic.Logics.Companion.FEEDING_MONITORING
import com.intellisoft.nndak.logic.Logics.Companion.FEEDING_SUPPLEMENTS
import com.intellisoft.nndak.logic.Logics.Companion.FEEDS_DEFICIT
import com.intellisoft.nndak.logic.Logics.Companion.FEEDS_TAKEN
import com.intellisoft.nndak.logic.Logics.Companion.FORMULA_ROUTE
import com.intellisoft.nndak.logic.Logics.Companion.FORMULA_TYPE
import com.intellisoft.nndak.logic.Logics.Companion.FORMULA_VOLUME
import com.intellisoft.nndak.logic.Logics.Companion.GESTATION
import com.intellisoft.nndak.logic.Logics.Companion.IV_ROUTE
import com.intellisoft.nndak.logic.Logics.Companion.IV_VOLUME
import com.intellisoft.nndak.logic.Logics.Companion.JAUNDICE
import com.intellisoft.nndak.logic.Logics.Companion.MULTIPLE_PREGNANCY
import com.intellisoft.nndak.logic.Logics.Companion.MUM_CONTRA
import com.intellisoft.nndak.logic.Logics.Companion.MUM_LOCATION
import com.intellisoft.nndak.logic.Logics.Companion.MUM_WELL
import com.intellisoft.nndak.logic.Logics.Companion.PARITY
import com.intellisoft.nndak.logic.Logics.Companion.PMTCT
import com.intellisoft.nndak.logic.Logics.Companion.PRESCRIPTION
import com.intellisoft.nndak.logic.Logics.Companion.PRESCRIPTION_DATE
import com.intellisoft.nndak.logic.Logics.Companion.REMARKS
import com.intellisoft.nndak.logic.Logics.Companion.SEPSIS
import com.intellisoft.nndak.logic.Logics.Companion.STOOL
import com.intellisoft.nndak.logic.Logics.Companion.TOTAL_FEEDS
import com.intellisoft.nndak.logic.Logics.Companion.VOMIT
import com.intellisoft.nndak.models.*
import com.intellisoft.nndak.utils.Constants.MAX_RESOURCE_COUNT
import com.intellisoft.nndak.utils.Constants.MIN_RESOURCE_COUNT
import com.intellisoft.nndak.utils.getPastHoursOnIntervalOf
import com.intellisoft.nndak.utils.getWeeksSoFarIntervalOf
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.*
import timber.log.Timber
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
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
    val liveAssessmentExpressions = MutableLiveData<List<ExpressionHistory>>()
    val liveOrder = MutableLiveData<OrdersItem>()
    val liveFeeds = MutableLiveData<FeedsDistribution>()
    val livePrescriptionsData = MutableLiveData<List<PrescriptionItem>>()
    val liveFeedingData = MutableLiveData<List<PrescriptionItem>>()
    val liveWeights = MutableLiveData<WeightsData>()
    val liveExpressions = MutableLiveData<MilkExpression>()
    val context: Application = application
    val liveFeedingHistory = MutableLiveData<List<FeedingHistory>>()
    val livePositioningHistory = MutableLiveData<List<PositioningHistory>>()
    val liveBreastHistory = MutableLiveData<List<BreastsHistory>>()


    fun feedsDistribution() {
        viewModelScope.launch { liveFeeds.value = getFeedsDataModel() }
    }

    fun activeBabyWeights() {
        viewModelScope.launch { liveWeights.value = getWeightsDataModel() }
    }

    fun activeWeeklyBabyWeights() {
        viewModelScope.launch { liveWeights.value = getWeightsDataModel() }
    }

    fun getExpressions() {
        viewModelScope.launch { liveExpressions.value = getExpressionDataModel() }
    }

    private suspend fun getExpressionDataModel(): MilkExpression {

        val expressions: MutableList<ExpressionData> = mutableListOf()
        val intervals = getPastHoursOnIntervalOf(8, 3)
        var totalFeed = 0f
        var ti = 0f
        intervals.forEach {
            val milk = getHourlyExpressions(it.toString())
            ti += milk.number.toFloat()
            totalFeed += milk.amount.toFloat()
            expressions.add(milk)
        }
        return MilkExpression(
            totalFeed = "$totalFeed",
            varianceAmount = ti.toString(),
            data = expressions
        )
    }

    private suspend fun getHourlyExpressions(time: String): ExpressionData {
        var quantity = 0f
        var times = 0f
        val expressions = observationsPerCode(EXPRESSED_MILK)
        val sorted = sortCollected(expressions)
        sorted.forEach {
            try {

                val feedTime = FormatHelper().getSystemHour(it.effective)
                val maxRange = FormatHelper().getDateHour(time)
                val minRange = FormatHelper().getHourRange(maxRange)

                val isWithinRange = FormatHelper().startCurrentEnd(minRange, feedTime, maxRange)
                if (isWithinRange) {
                    val qty = it.quantity.toFloat()
                    quantity += qty
                    times++

                    /*val amounts = observationsPerCodeEncounter(EXPRESSED_MILK, it.encounterId)
                    amounts.forEach { data ->
                        val qty = data.quantity.toFloat()
                        quantity += qty
                        times++
                    }*/
                }
            } catch (e: Exception) {
                Timber.e("Exception ${e.localizedMessage}")
            }
        }
        val refinedTime = FormatHelper().getRoundedHour(time)

        return ExpressionData(
            time = refinedTime,
            amount = quantity.toString(),
            number = times.toString()
        )

    }

    private suspend fun getHourlyManualExpressions(time: String): ExpressionData {
        var quantity = 0f
        val expressions = observationsPerCode(EXPRESSION_TIME)
        val sorted = sortCollected(expressions)
        sorted.forEach {
            try {
                val feedTime = FormatHelper().getDateHourZone(it.value.trim())
                val maxRange = FormatHelper().getRoundedDateHour(time)
                val minRange = FormatHelper().getHourRange(maxRange)

                val isWithinRange = FormatHelper().startCurrentEnd(minRange, feedTime, maxRange)
                if (isWithinRange) {
                    val amounts = observationsPerCodeEncounter(EXPRESSED_MILK, it.encounterId)
                    amounts.forEach { data ->
                        val qty = data.quantity.toFloat()
                        quantity += qty
                    }
                }
            } catch (e: Exception) {
                Timber.e("Exception ${e.localizedMessage}")
            }
        }
        val refinedTime = FormatHelper().getRoundedHour(time)

        return ExpressionData(time = refinedTime, amount = quantity.toString(), number = "0")

    }

    private suspend fun getWeightsDataModel(): WeightsData {
        val patient = getPatient()
        val weight = pullWeights()
        val data = arrayListOf<ActualData>()
        var dayOfLife = 0
        var weeksLife = 0
        var babyGender = ""
        val gestationAge = retrieveQuantity(GESTATION)
        var lastKnownWeight = retrieveQuantity(CURRENT_WEIGHT)
        patient.let {
            dayOfLife = getFormattedAge(it.dob)
            babyGender = it.gender
            weeksLife = getWeekFromDays(dayOfLife)

        }

        if (weight.isNotEmpty()) {
            val daysString = getWeeksSoFarIntervalOf(patient.dob, weeksLife + 1, 1)
            val sorted = sortCollected(weight)
            lastKnownWeight = sorted.last().quantity
            for ((i, entry) in daysString.withIndex()) {
                val added = gestationAge.toFloat() + i.toFloat()

                var value = extractWeeklyMeasure(entry, 6, sorted)

                if (value == "0.0") {
                    value = lastKnownWeight
                }
                data.add(ActualData(day = added.toInt(), actual = value, projected = value))
            }
        }
        Timber.e("lastKnownWeight $lastKnownWeight")
        return WeightsData(
            babyGender = babyGender,
            currentWeight = lastKnownWeight,
            gestationAge = gestationAge,
            dayOfLife = dayOfLife,
            data = data,
            weeksLife = weeksLife + 1
        )
    }


    private suspend fun getFeedsDataModel(): FeedsDistribution {
        val intervals = getPastHoursOnIntervalOf(8, 3)
        val feeds: MutableList<FeedsData> = mutableListOf()
        var totalFeed = 0f

        intervals.forEach {
            val data = loadFeed(it)

            val total =
                data.dhmVolume.toFloat() + data.ivVolume.toFloat() + data.ebmVolume.toFloat() + data.formula.toFloat()

            totalFeed += total
            feeds.add(data)
        }
        return FeedsDistribution(
            totalFeed = "$totalFeed mls",
            varianceAmount = "0",
            data = feeds
        )
    }

    private suspend fun loadFeed(it: LocalDateTime): FeedsData {

        var iv = 0f
        var ebm = 0f
        var dhm = 0f
        var bm = 0f
        var fm = 0f
        val hour = FormatHelper().getRoundedHour(it.toString())
        val carePlans = getCompletedCarePlans()

        if (carePlans.isNotEmpty()) {
            carePlans.forEach { item ->
                Timber.e("Care Plan ${item.purpose}")
                /**
                 * Consider Feeding CarePlans
                 */
                if (item.purpose == FEEDING_MONITORING) {

                    try {

                        val actualTime = FormatHelper().getRefinedDatePmAm(item.created)
                        val currentTime = FormatHelper().getRoundedDateHour(it.toString())
                        val maxThree = FormatHelper().getHourRange(currentTime)
                        val isWithinRange =
                            FormatHelper().startCurrentEnd(maxThree, actualTime, currentTime)
                        if (isWithinRange) {

                            val iVs = observationsPerCodeEncounter(
                                IV_VOLUME,
                                item.encounterId
                            )
                            iVs.forEach {
                                iv += it.quantity.toFloat()
                            }

                            val eBms = observationsPerCodeEncounter(
                                EBM_VOLUME,
                                item.encounterId
                            )

                            eBms.forEach {
                                ebm += it.quantity.toFloat()
                            }

                            val dhmS = observationsPerCodeEncounter(
                                DHM_VOLUME,
                                item.encounterId
                            )
                            dhmS.forEach {
                                dhm += it.quantity.toFloat()
                            }

                            val bmS = observationsPerCodeEncounter(
                                BREAST_MILK,
                                item.encounterId
                            )
                            bmS.forEach {
                                bm += it.quantity.toFloat()
                            }
                            val fmS = observationsPerCodeEncounter(
                                FORMULA_VOLUME,
                                item.encounterId
                            )
                            fmS.forEach {
                                fm += it.quantity.toFloat()
                            }

                        }
                    } catch (e: Exception) {

                    }

                }
            }
        } else {
            iv = 0f
            ebm = 0f
            dhm = 0f
            bm = 0f
            fm = 0f

        }

        return FeedsData(
            time = hour,
            breastVolume = bm.toString(),
            ivVolume = iv.toString(),
            ebmVolume = ebm.toString(),
            dhmVolume = dhm.toString(),
            formula = fm.toString()
        )
    }

    private suspend fun loadFeedCare(it: LocalDateTime): FeedItem {

        var iv = 0f
        var ebm = 0f
        var dhm = 0f
        val hour = FormatHelper().getRoundedHour(it.toString())
        val carePlans = getCompletedCarePlans()
        if (carePlans.isNotEmpty()) {
            carePlans.forEach { item ->
                val actualTime = FormatHelper().getRefinedDatePmAm(item.created)
                val currentTime = FormatHelper().getRoundedDateHour(it.toString())
                try {
                    val maxThree = FormatHelper().getHourRange(currentTime)
                    val isWithinRange =
                        FormatHelper().isWithinRange(actualTime, currentTime, maxThree)
                    if (isWithinRange) {

                        val iVs = observationsPerCodeEncounter(
                            IV_VOLUME,
                            item.encounterId
                        )
                        iVs.forEach {
                            iv += it.quantity.toFloat()
                        }

                        val eBms = observationsPerCodeEncounter(
                            EBM_VOLUME,
                            item.encounterId
                        )

                        eBms.forEach {
                            ebm += it.quantity.toFloat()
                        }
                        val dhmS = observationsPerCodeEncounter(
                            DHM_VOLUME,
                            item.encounterId
                        )
                        dhmS.forEach {
                            dhm += it.quantity.toFloat()
                        }

                    }
                    Timber.e("Cheza $isWithinRange")
                } catch (e: Exception) {
                    Timber.e("Cheza Exception ${e.localizedMessage}")
                }

            }
        } else {
            iv = 0f
            ebm = 0f
            dhm = 0f

        }

        return FeedItem(
            resourceId = hour,
            volume = iv.toString(),
            route = ebm.toString(),
            frequency = dhm.toString()
        )
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
        val patient = getPatient()
        val mum = getMother(patientId)
        val mumName = mum.first.toString()
        val mumIp = mum.second.toString()
        val babyWell = retrieveCode(BABY_WELL)
        val asphyxia = retrieveCode(ASPHYXIA)
        val jaundice = retrieveCode(JAUNDICE)
        val sepsis = retrieveCode(SEPSIS)
        val breastProblems = retrieveCode(BREAST_PROBLEM)
        val mumLocation = retrieveCode(MUM_LOCATION)
        val contra = retrieveCode(MUM_CONTRA)
        val breastfeeding = retrieveCode(BABY_BREASTFEEDING)
        val mumWell = retrieveCode(MUM_WELL)
        var dDate = retrieveCode(DELIVERY_DATE)
        var birthWeight = ""
        var status = ""
        var gestation = ""
        var apgar = ""
        var admDate = ""
        var dMethod = ""
        var parity = ""
        var pmtct = ""
        var mPreg = ""
        var motherMilk = "0 ml"
        var assessed = false
        val exp = getPatientEncounters()
        val encounterId = getSingleEncounters("Client Registration")?.id.toString()
        var i: Int = 0
        if (exp.isNotEmpty()) {
            for (element in exp) {
                when (element.code) {
                    EXPRESSIONS -> {
                        i++
                    }
                    BABY_ASSESSMENT -> {
                        assessed = true
                    }
                }
            }
        }

        val obs = getObservations()

        if (obs.isNotEmpty()) {
            for (element in obs) {
                when (element.code) {
                    PMTCT -> {
                        pmtct = element.value
                    }
                    MULTIPLE_PREGNANCY -> {
                        mPreg = element.value
                    }
                    DELIVERY_METHOD -> {
                        dMethod = element.value
                    }
                    PARITY -> {
                        parity = "P${element.value}"
                    }
                    BIRTH_WEIGHT -> {
                        birthWeight = element.value
                        try {
                            val code = element.value.split("\\.".toRegex()).toTypedArray()
                            birthWeight = if (code[0].toInt() < 2500) {
                                "$birthWeight -Low"
                            } else {
                                "$birthWeight -Normal"
                            }
                        } catch (e: Exception) {
                            "$birthWeight -Normal"
                        }
                    }
                    ADMISSION_DATE -> {
                        admDate =
                            try {
                                FormatHelper().extractDateString(element.value)
                            } catch (e: Exception) {
                                element.value
                            }
                    }
                    APGAR_SCORE -> {
                        apgar = element.value
                    }
                    BREAST_MILK -> {
                        motherMilk = element.value
                    }
                    GESTATION -> {
                        val code = element.quantity//.split("\\.".toRegex()).toTypedArray()

                        status = try {
                            if (code.toDouble() < 37) {
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
        }
        dDate = try {
            FormatHelper().extractDateString(dDate)
        } catch (e: Exception) {
            dDate
        }
        val total = calculateTotalExpressedMilk()

        var name = ""
        var dateOfBirth = ""
        var dayOfLife: Int

        patient.let {
            name = it.name
            dateOfBirth = it.dob
            dayOfLife = getFormattedAge(it.dob)

        }

        val gainRate = calculateWeightGainRate(patientId,patient)
        return MotherBabyItem(
            encounterId,
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
                dayOfLife = dayOfLife.toString(),
                dateOfAdm = admDate,
                motherMilk = motherMilk,
                assessed = assessed
            ),
            mother = MotherDashboard(
                parity = parity,
                deliveryMethod = dMethod,
                pmtctStatus = pmtct,
                multiPregnancy = mPreg,
                deliveryDate = dDate,
                motherLocation = mumLocation,
                motherStatus = mumWell,
            ),
            assessment = AssessmentItem(
                breastProblems = breastProblems,
                breastfeedingBaby = breastfeeding,
                totalExpressed = total,
                contraindicated = contra
            )
        )
    }

    private suspend fun calculateWeightGainRate(resourceId: String, patient: PatientItem): String {
        var standard = "boy-z-score.json"
        if (patient.gender == "female") {
            standard = "girl-z-score.json"
        }
        var gainRate = "Normal"
        try {
            val it = getWeightsDataModel()
            val jsonFileString = getJsonDataFromAsset(standard)

            val gson = Gson()
            val listGrowthType = object : TypeToken<List<GrowthData>>() {}.type
            val growths: List<GrowthData> = gson.fromJson(jsonFileString, listGrowthType)
            for ((i, entry) in growths.withIndex()) {
                val start = entry.age
                val ges = it.gestationAge.toInt()
                if (start == ges || start > ges) {
                    val equivalent = extractValueIndex(start, it)
                    val zeroDeviation = entry.data[3].value.toFloat()
                    val deviation = equivalent.toFloat() - zeroDeviation
                    if (equivalent == "0") {
                        //  gainRate = "Normal"
                    } else {

                        if (deviation < 0) {
                            gainRate = "Low"
                        } else if (deviation > 0) {
                            gainRate = "High"
                        }
                    }

                    Timber.d("deviation $zeroDeviation deviation $deviation equivalent $equivalent Rate $gainRate")
                }
            }
        } catch (e: Exception) {

        }
        return gainRate
    }

    private fun getJsonDataFromAsset(fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }

    private suspend fun retrieveFirstCode(code: String): String {
        var data = ""
        val obs = observationsPerCode(code)
        if (obs.isNotEmpty()) {
            val sort = sortCollected(obs)
            data = sort.first().value.trim()
        }
        return data
    }

    private suspend fun retrieveCode(code: String): String {
        var data = ""
        val obs = observationsPerCode(code)
        if (obs.isNotEmpty()) {
            val sort = sortCollected(obs)
            data = sort.last().value.trim()
        }
        return data
    }


    private suspend fun retrieveQuantity(code: String): String {
        var data = ""
        val obs = observationsPerCode(code)
        if (obs.isNotEmpty()) {
            val sort = sortCollected(obs)
            sort.forEach {
                Timber.e("Ordered Weight Data${it.quantity}")
            }
            data = sort.last().quantity.trim()
        }
        return data
    }

    private suspend fun retrieveQuantityFloat(code: String): Float {
        var data = ""
        val obs = observationsPerCode(code)
        if (obs.isNotEmpty()) {
            val sort = sortCollected(obs)
            data = sort.last().quantity.trim()
        }
        return data.toFloat()
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

    private suspend fun observationsCodePerEncounter(
        key: String,
        encounter: String
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
                filter(Observation.ENCOUNTER, { value = "Encounter/$encounter" })
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

    private suspend fun observationsCodePerEncounterCare(
        key: String,
        encounter: String
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
                filter(Observation.ENCOUNTER, { value = encounter })
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


    private fun getFormattedAge(
        dob: String
    ): Int {
        if (dob.isEmpty()) return 0
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Period.between(LocalDate.parse(dob), LocalDate.now()).let {
                when {
                    it.years > 0 -> it.years
                    it.months > 0 -> it.months
                    else -> it.days
                }
            }
        } else {
            0
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
            livePrescriptionsData.value = getActivePrescriptionsDataModel(context)
        }
    }

    fun getFeedingInstances(careId: String) {
        viewModelScope.launch {
            liveFeedingData.value = getFeedingInstancesDataModel(context, careId)
        }
    }

    private suspend fun getActivePrescriptionsDataModel(context: Application): List<PrescriptionItem> {
        val prescriptions: MutableList<PrescriptionItem> = mutableListOf()
        val pres = fetchActiveCarePlans()
        pres.forEach { item ->
            prescriptions.add(prescription(item))
        }
        return sortPrescriptions(prescriptions)

    }

    private suspend fun getFeedingInstancesDataModel(
        context: Application,
        careId: String
    ): List<PrescriptionItem> {


        val data: MutableList<PrescriptionItem> = mutableListOf()
        val pres = fetchCarePlansEncounter(careId)
        pres.forEach { item ->
            val prop = feedsTakenEncounter(item)
            if (prop.hour.toString() != "--") {
                data.add(feedsTakenEncounter(item))
            }
        }

        return sortPrescriptions(data)
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

    private suspend fun getCompletedCarePlans(): List<CareItem> {
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
            .filter { it.status == CarePlan.CarePlanStatus.COMPLETED.toString() }
            .let { cares.addAll(it) }
        return cares
    }

    private suspend fun getCompletedCarePlansEncounter(): List<EncounterItem> {
        val cares: MutableList<EncounterItem> = mutableListOf()
        fhirEngine
            .search<Encounter> {
                filter(
                    Encounter.SUBJECT,
                    { value = "Patient/$patientId" })
                sort(Encounter.DATE, Order.DESCENDING)
            }
            .asSequence()
            .take(MAX_RESOURCE_COUNT)
            .map {
                createEncounterItem(
                    it,
                    getApplication<Application>().resources
                )
            }
            .filter { it.status == Encounter.EncounterStatus.FINISHED.toString() }
            .filter { it.code == FEEDING_MONITORING }
            .filter { it.partOf != "" }
            .toList()
            .let { cares.addAll(it) }
        return cares
    }


    private suspend fun fetchActiveCareEncounters(): List<EncounterItem> {
        val cares: MutableList<EncounterItem> = mutableListOf()

        fhirEngine
            .search<Encounter> {
                filter(
                    Encounter.SUBJECT,
                    { value = "Patient/$patientId" })
                sort(Encounter.DATE, Order.DESCENDING)
            }
            .take(MAX_RESOURCE_COUNT)
            .map {
                createEncounterItem(
                    it,
                    getApplication<Application>().resources
                )
            }
            .filter { it.status == Encounter.EncounterStatus.INPROGRESS.toString() }
            .filter { it.code == PRESCRIPTION }
            .let { cares.addAll(it) }
        return cares
    }


    private suspend fun fetchCarePlansEncounter(careId: String): List<EncounterItem> {
        val cares: MutableList<EncounterItem> = mutableListOf()
        fhirEngine
            .search<Encounter> {
                filter(
                    Encounter.SUBJECT,
                    { value = "Patient/$patientId" })
                filter(Encounter.PART_OF, { value = careId })
                sort(Encounter.DATE, Order.DESCENDING)
            }
            .take(MAX_RESOURCE_COUNT)
            .map {
                createEncounterItem(
                    it,
                    getApplication<Application>().resources
                )

            }
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
            FormatHelper().extractCareDateString(care.created)
        } catch (e: Exception) {
            care.created
        }
        val time = try {
            FormatHelper().extractCareTimeString(care.created)
        } catch (e: Exception) {
            care.created
        }
        val observations = getReferencedObservations(care.encounterId)
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
        )
    }

    private suspend fun feedsTakenEncounter(care: EncounterItem): PrescriptionItem {
        val observations = getReferencedObservationsEncounter(care.id)
        val hour = extractValue(observations, ASSESSMENT_DATE)
        val date = try {
            FormatHelper().extractDateString(hour)
        } catch (e: Exception) {
            hour
        }
        val time = try {
            FormatHelper().extractTimeString(hour)
        } catch (e: Exception) {
            hour
        }

        return PrescriptionItem(
            id = care.id,
            resourceId = care.id,
            hour = hour,
            date = date,
            time = time,
            deficit = extractQuantity(observations, FEEDS_DEFICIT),
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
        )
    }

    private fun extractValue(observations: List<ObservationItem>, code: String): String {
        return observations.find { it.code == code }?.value ?: "--"
    }

    private fun extractValueDate(observations: List<ObservationItem>, code: String): String {
        return observations.find { it.code == code }?.effective ?: "--"
    }

    private fun extractQuantity(observations: List<ObservationItem>, code: String): String {
        return observations.find { it.code == code }?.quantity ?: "0"
    }

    //    private suspend fun prescription(care: EncounterItem): PrescriptionItem {
    private suspend fun prescription(care: CareItem): PrescriptionItem {
        val observations = getReferencedObservations(care.encounterId)
        val feeds: MutableList<FeedItem> = mutableListOf()
        val relatedFeeds = fetchCarePlansEncounter(care.encounterId)

        var date = "N/A"
        var time = "N/A"
        val total = extractQuantity(observations, TOTAL_FEEDS)
        var iv = "N/A"
        var bm = "N/A"
        var dhm = "N/A"
        var ebm = "N/A"
        var consent = "N/A"
        var formula = "N/A"
        var reason = "N/A"
        var supplements = "N/A"
        var additional = "N/A"
        var consentDate = "N/A"
        var expressions = 0

        val givenFeeds = pullFeeds()


        val def = if (relatedFeeds.isNotEmpty()) {

            val parentId = getFeedingInstancesDataModel(context, care.encounterId)
            try {
                parentId.first().deficit
            } catch (e: Exception) {
                total
            }

        } else {
            total
        }

        val exp = getPatientEncounters()
        if (exp.isNotEmpty()) {
            for (ex in exp) {
                if (ex.code == "Milk Expression") {
                    expressions++
                }
            }
        }
        val weight = pullWeights()
        val sort = sortCollected(weight)
        val cWeight = sort.last().quantity
        val routes = StringBuilder()
        var frequency = "2 Hourly"
        if (observations.isNotEmpty()) {

            val breastMilk = observationsCodePerEncounterCare(
                BREAST_MILK,
                care.encounterId
            ).firstOrNull()?.value
            if (breastMilk != null) {
                val bmVolume = extractQuantity(observations, BREAST_MILK)

                feeds.add(
                    FeedItem(
                        resourceId = BREAST_MILK,
                        volume = bmVolume,
                    )
                )
            }
            val form = observationsCodePerEncounterCare(
                FORMULA_VOLUME,
                care.encounterId
            ).firstOrNull()?.value
            if (form != null) {
                val fVolume = extractQuantity(observations, FORMULA_VOLUME)
                val fRoute = extractValue(observations, FORMULA_ROUTE)
                val fType = extractValue(observations, FORMULA_TYPE)

                feeds.add(
                    FeedItem(
                        resourceId = FORMULA_VOLUME,
                        volume = fVolume,
                        route = fRoute,
                        type = fType
                    )
                )
            }
            val expressed = observationsCodePerEncounterCare(
                EBM_VOLUME,
                care.encounterId
            ).firstOrNull()?.value
            if (expressed != null) {
                val fVolume = extractQuantity(observations, EBM_VOLUME)
                val fRoute = extractValue(observations, EBM_ROUTE)

                feeds.add(
                    FeedItem(
                        resourceId = EBM_VOLUME,
                        volume = fVolume,
                        route = fRoute,
                    )
                )
            }
            val donor = observationsCodePerEncounterCare(
                DHM_VOLUME,
                care.encounterId
            ).firstOrNull()?.value


            if (donor != null) {
                val fVolume = extractQuantity(observations, DHM_VOLUME)
                val fRoute = extractValue(observations, DHM_ROUTE)
                val dType = extractValue(observations, DHM_TYPE)
                consent = extractValue(observations, DHM_CONSENT)
                reason = extractValue(observations, DHM_REASON)

                feeds.add(
                    FeedItem(
                        resourceId = DHM_VOLUME,
                        volume = fVolume,
                        route = fRoute,
                        type = dType
                    )
                )
            }
            val fl = observationsCodePerEncounterCare(
                IV_VOLUME,
                care.encounterId
            ).firstOrNull()?.value


            if (fl != null) {
                val fVolume = extractQuantity(observations, IV_VOLUME)
                val fRoute = extractValue(observations, IV_ROUTE)

                feeds.add(
                    FeedItem(
                        resourceId = IV_VOLUME,
                        volume = fVolume,
                        route = fRoute,
                    )
                )
            }
            for (element in observations) {
                when (element.code) {
                    PRESCRIPTION_DATE -> {
                        date = FormatHelper().extractDateOnly(element.value)
                        time = FormatHelper().extractTimeOnly(element.value)
                    }
                    FEEDING_FREQUENCY -> {
                        frequency = element.value
                    }
                    EBM_ROUTE -> {
                        routes.append("EBM - ${element.value}\n")
                    }
                    IV_ROUTE -> {
                        routes.append("IV - ${element.value}\n")
                    }
                    DHM_ROUTE -> {

                        routes.append("DHM- ${element.value}\n")
                    }
                    FORMULA_ROUTE -> {
                        routes.append("Formula- ${element.value}\n")
                    }
                    IV_VOLUME -> {
                        iv = element.value
                    }
                    BREAST_MILK -> {
                        bm = element.value
                    }
                    DHM_VOLUME -> {
                        dhm = element.value
                    }
                    EBM_VOLUME -> {
                        ebm = element.value
                    }
                    FORMULA_VOLUME -> {
                        formula = element.value
                    }
                    FEEDING_SUPPLEMENTS -> {
                        supplements = element.value
                    }
                    ADDITIONAL_FEEDS -> {
                        additional = element.value
                    }
                    CONSENT_DATE -> {
                        consentDate = element.value
                    }
                }
            }
        }
        return PrescriptionItem(
            id = care.resourceId,
            resourceId = care.encounterId,
            date = date,
            time = time,
            totalVolume = total,
            frequency = frequency.trim(),
            route = routes.toString(),
            ivFluids = iv,
            breastMilk = bm,
            ebm = ebm,
            donorMilk = dhm,
            consent = consent.trim(),
            dhmReason = reason,
            supplements = supplements,
            additionalFeeds = additional,
            consentDate = consentDate,
            feed = feeds,
            feedsGiven = givenFeeds,
            expressions = expressions.toString(),
            cWeight = cWeight,
            formula = formula,
            deficit = def,
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

    private suspend fun getReferencedObservationsEncounter(
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

                if (element.code == DHM_CONSENT) {
                    consent = element.value
                }
                if (element.code == DHM_TYPE) {
                    dhm = element.value
                }
                if (element.code == DHM_VOLUME) {
                    dhmVolume = element.value
                }
                if (element.code == DHM_REASON) {
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
            babyAge = getFormattedAge(baby.dob).toString(),
            consentGiven = consent,
            dhmType = dhm,
            dhmReason = reason,
            description = dhmVolume,
            status = "active"
        )
    }

    fun getAssessmentExpressions() {
        viewModelScope.launch {
            liveAssessmentExpressions.value = getExpressionAssessmentDataModel()
        }
    }

    private suspend fun getExpressionAssessmentDataModel(): List<ExpressionHistory> {
        val expressions: MutableList<ExpressionHistory> = mutableListOf()
        val history = getAllEncounters("Expression-Assessment")
        if (history.isNotEmpty()) {
            history.forEach {

                expressions.add(retrieveExpressions(it))

            }
        }

        return expressions.reversed()
    }

    private suspend fun retrieveExpressions(it: EncounterItem): ExpressionHistory {
        val observations = getObservationsPerEncounter(it.id)
        var frequency = "N/A"
        var timing = "N/A"
        var date = "N/A"
        var massage = "N/A"
        var anxious = "N/A"
        var skinContact = "N/A"
        var handExpression = "N/A"
        var breastCondition = "N/A"
        var milkVolume = "N/A"
        if (observations.isNotEmpty()) {
            frequency = extractValue(observations, "Expression-Frequency")
            timing = extractValue(observations, "Timing-Expression")
            massage = extractValue(observations, "Breast-Massage")
            anxious = extractValue(observations, "Anxious")
            skinContact = extractValue(observations, "Skin-Contact")
            handExpression = extractValue(observations, "Hand-Expression")
            milkVolume = extractValue(observations, "Milk-Volume")
            breastCondition = extractValue(observations, "Breast-Condition")
            date = extractValueDate(observations, "Timing-Expression")
            if (date.isNotEmpty()) date = try {
                FormatHelper().getSimpleReverseDate(date)
            } catch (e: Exception) {
                date
            }
        }
        return ExpressionHistory(
            id = it.id,
            resourceId = it.id,
            date = date,
            frequency = "$frequency in 24 hours",
            timing = timing,
            massage = massage,
            anxious = anxious,
            skinContact = skinContact,
            handExpression = handExpression,
            breastCondition = breastCondition,
            milkVolume = milkVolume,
        )
    }

    private suspend fun getAllEncounters(key: String): List<EncounterItem> {
        val encounters: MutableList<EncounterItem> = mutableListOf()
        fhirEngine
            .search<Encounter> {
                filter(Encounter.SUBJECT, { value = "Patient/$patientId" })
                sort(Encounter.DATE, Order.DESCENDING)

            }
            .map { createEncounterItem(it, getApplication<Application>().resources) }
            .filter { it.code == key }
            .let { encounters.addAll(it) }

        return encounters.reversed()
    }

    private suspend fun getSingleEncounters(key: String): EncounterItem? {
        Timber.e("Searching for encounter......")
        return fhirEngine
            .search<Encounter> {
                filter(Encounter.SUBJECT, { value = "Patient/$patientId" })
                sort(Encounter.DATE, Order.DESCENDING)
            }
            .map { createEncounterItem(it, getApplication<Application>().resources) }
            .firstOrNull { it.code == key }
    }

    fun getFeedingHistory() {
        viewModelScope.launch {
            liveFeedingHistory.value = getFeedingHistoryDataModel(context)
        }
    }

    fun getPositioningHistory() {
        viewModelScope.launch {
            livePositioningHistory.value = getPositioningHistoryDataModel(context)
        }
    }

    fun getBreastAssessmentHistory() {
        viewModelScope.launch {
            liveBreastHistory.value = getBreastHistoryDataModel(context)
        }
    }

    private suspend fun getBreastHistoryDataModel(context: Application): List<BreastsHistory> {
        val history: MutableList<BreastsHistory> = mutableListOf()
        val feeding = getAllEncounters(BREASTS_FEEDING)
        if (feeding.isNotEmpty()) {
            feeding.forEach {
                val data = retrieveBreast(it)
                if (data.date != "--") {
                    history.add(data)
                }
            }
        }
        return history
    }

    private suspend fun getFeedingHistoryDataModel(context: Application): List<FeedingHistory> {
        val history: MutableList<FeedingHistory> = mutableListOf()
        val feeding = getAllEncounters(FEEDING_MONITORING)
        if (feeding.isNotEmpty()) {
            feeding.forEach {
                val data = retrieveFeeding(it)
                if (data.date != "--") {
                    history.add(data)
                }
            }
        }
        return sortHistory(history)
    }

    private suspend fun getPositioningHistoryDataModel(context: Application): List<PositioningHistory> {
        val history: MutableList<PositioningHistory> = mutableListOf()
        val feeding = getAllEncounters("Positioning-Assessment")
        if (feeding.isNotEmpty()) {
            feeding.forEach {
                val data = retrievePositioning(it)
                if (data.date != "--") {
                    history.add(data)
                }
            }
        }
        return history
    }

    private suspend fun retrieveBreast(it: EncounterItem): BreastsHistory {
        val observations = getObservationsPerEncounter(it.id)

        val hour = extractValue(observations, ASSESSMENT_DATE)
        val date = try {
            FormatHelper().getRefinedDateOnly(hour)
        } catch (e: Exception) {
            hour
        }

        return BreastsHistory(
            date = date,
            interest = extractValue(observations, "Baby-Interest"),
            cues = extractValue(observations, "Feeding-Cues"),
            sleep = extractValue(observations, "Baby-Sleep"),
            bursts = extractValue(observations, "Bursts"),
            shortFeed = extractValue(observations, "Short-Feed"),
            longSwallow = extractValue(observations, "Long-Swallow"),
            skin = extractValue(observations, "Skin-Tone"),
            nipples = extractValue(observations, "Nipples"),
            shape = extractValue(observations, "Shape"),

            )
    }

    private suspend fun retrievePositioning(it: EncounterItem): PositioningHistory {
        val observations = getObservationsPerEncounter(it.id)

        val hour = extractValue(observations, ASSESSMENT_DATE)
        val date = try {
            FormatHelper().getRefinedDateOnly(hour)
        } catch (e: Exception) {
            hour
        }

        return PositioningHistory(
            date = date,
            hands = extractValue(observations, "Hand-Wash"),
            mum = extractValue(observations, "Mother-Position"),
            baby = extractValue(observations, "Baby-Position"),
            attach = extractValue(observations, "Good-Attachment"),
            suckle = extractValue(observations, "Effective-Suckling"),
        )
    }

    private suspend fun retrieveFeeding(it: EncounterItem): FeedingHistory {
        val observations = getObservationsPerEncounter(it.id)
        val hour = extractValue(observations, ASSESSMENT_DATE)

        val date = try {
            FormatHelper().extractDateString(hour.trim())
        } catch (e: Exception) {
            hour
        }

        val time = try {
            FormatHelper().extractTimeString(hour.trim())
        } catch (e: Exception) {
            Timber.e("Date Feeding Exception ${e.localizedMessage}")
            date
        }
        return FeedingHistory(
            hour = hour,
            date = date,
            time = time,
            ebm = extractValue(observations, EBM_VOLUME),
            dhm = extractValue(observations, DHM_VOLUME),
            iv = extractValue(observations, IV_VOLUME),
            deficit = extractValue(observations, FEEDS_DEFICIT),
            vomit = extractValue(observations, VOMIT),
            diaper = extractValue(observations, DIAPER_CHANGED),
            stool = extractValue(observations, STOOL)
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

            val date = if (care.hasCreated()) {
                care.created.toString()
            } else {
                resources.getString(R.string.message_no_datetime)
            }

            return CareItem(
                care.logicalId,
                care.subject.reference,
                care.encounter.reference,
                care.status.toString(), date, care.title
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
            val status = encounter.status ?: Encounter.EncounterStatus.INPROGRESS
            val part = encounter.partOf.reference ?: ""
            return EncounterItem(
                encounter.logicalId,
                encounterCode,
                value,
                value,
                status.toString(),
                part

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
