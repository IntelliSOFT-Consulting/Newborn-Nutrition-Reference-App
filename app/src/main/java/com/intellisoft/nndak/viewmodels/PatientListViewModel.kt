package com.intellisoft.nndak.viewmodels

import android.app.Application
import android.os.Build
import androidx.lifecycle.*
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.logicalId
import com.google.android.fhir.search.*
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.intellisoft.nndak.charts.ActualData
import com.intellisoft.nndak.charts.GrowthData
import com.intellisoft.nndak.charts.WHOData
import com.intellisoft.nndak.charts.WeightsData
import com.intellisoft.nndak.helper_class.FormatHelper
import com.intellisoft.nndak.logic.DataSort.Companion.convertToKg
import com.intellisoft.nndak.logic.DataSort.Companion.extractDailyMeasure
import com.intellisoft.nndak.logic.DataSort.Companion.extractValueIndex
import com.intellisoft.nndak.logic.DataSort.Companion.extractWeeklyMeasure
import com.intellisoft.nndak.logic.DataSort.Companion.getFormattedIntAge
import com.intellisoft.nndak.logic.DataSort.Companion.getWeekFromDays
import com.intellisoft.nndak.logic.DataSort.Companion.sortCollected
import com.intellisoft.nndak.logic.Logics.Companion.ADMISSION_WEIGHT
import com.intellisoft.nndak.logic.Logics.Companion.BABY_ASSESSMENT
import com.intellisoft.nndak.logic.Logics.Companion.BIRTH_WEIGHT
import com.intellisoft.nndak.logic.Logics.Companion.CURRENT_WEIGHT
import com.intellisoft.nndak.logic.Logics.Companion.DHM_CONSENT
import com.intellisoft.nndak.logic.Logics.Companion.DHM_REASON
import com.intellisoft.nndak.logic.Logics.Companion.DHM_TYPE
import com.intellisoft.nndak.logic.Logics.Companion.FED_AFTER
import com.intellisoft.nndak.logic.Logics.Companion.GESTATION
import com.intellisoft.nndak.logic.Translations.Companion.feedTypes
import com.intellisoft.nndak.logic.Translations.Companion.feedingTimes
import com.intellisoft.nndak.models.*
import com.intellisoft.nndak.utils.*
import com.intellisoft.nndak.utils.Constants.MAX_RESOURCE_COUNT
import com.intellisoft.nndak.utils.Constants.MIN_RESOURCE_COUNT
import com.intellisoft.nndak.utils.Constants.SYNC_STATE
import com.intellisoft.nndak.utils.Constants.SYNC_VALUE
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel.Companion.createEncounterItem
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel.Companion.createNutritionItem
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel.Companion.createObservationItem
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.*
import timber.log.Timber
import java.io.IOException
import java.time.LocalDate
import java.time.Period
import java.util.*

/**
 * The ViewModel helper class for PatientItemRecyclerViewAdapter, that is responsible for preparing
 * data for UI.
 */
class PatientListViewModel(
    application: Application,
    private val fhirEngine: FhirEngine,
    private val location: String
) :
    AndroidViewModel(application) {

    private val liveSearchedPatients = MutableLiveData<List<PatientItem>>()
    val liveMotherBaby = MutableLiveData<List<MotherBabyItem>>()
    val patientCount = MutableLiveData<Long>()
    val context: Application = application
    val discharged = false

    init {
        updatePatientListAndPatientCount(
            { getSearchResults("", location, discharged) },
            { count("", location, discharged) })

        updateMumAndBabyCount(
            { getMumSearchResults("", location, discharged) },
            { count("", location, discharged) })
    }


    fun searchPatientsByName(nameQuery: String, discharged: Boolean) {
        updatePatientListAndPatientCount(
            { getSearchResults(nameQuery, location, discharged) },
            { count(nameQuery, location, discharged) })

        updateMumAndBabyCount(
            { getMumSearchResults(nameQuery, location, discharged) },
            { count("", location, discharged) })

    }

    /**
     * [updatePatientListAndPatientCount] calls the search and count lambda and updates the live data
     * values accordingly. It is initially called when this [ViewModel] is created. Later its called
     * by the client every time search query changes or data-sync is completed.
     */
    private fun updatePatientListAndPatientCount(
        search: suspend () -> List<PatientItem>,
        count: suspend () -> Long
    ) {
        viewModelScope.launch {
            liveSearchedPatients.value = search()
            patientCount.value = count()
        }
    }

    private fun updateMumAndBabyCount(
        search: suspend () -> List<MotherBabyItem>,
        count: suspend () -> Long
    ) {
        viewModelScope.launch {
            liveMotherBaby.value = search()
            patientCount.value = count()
        }
    }

    /**
     * Returns count of all the [Patient] who match the filter criteria unlike [getSearchResults]
     * which only returns a fixed range.
     */
    private suspend fun count(nameQuery: String = "", location: String, discharged: Boolean): Long {
        return fhirEngine.count<Patient> {
            if (nameQuery.isNotEmpty()) {
                filter(
                    Patient.NAME,
                    {
                        modifier = StringFilterModifier.CONTAINS
                        value = nameQuery
                    }
                )
            }
            filterCity(this, location, discharged)
        }
    }


    private suspend fun getDhmDashboardSearchResults(
        nameQuery: String = "",
        location: String
    ): DHMDashboardItem {
        val orders = getOrdersSearchResults(nameQuery, location)
        val data: MutableList<PieItem> = mutableListOf()
        val total = 0
        val days = getPastDaysOnIntervalOf(7, 1)

        days.forEach {
            val format = FormatHelper().getDayName(it.toString())
            data.add(PieItem(value = format, "10", "#F65050"))
        }
        return DHMDashboardItem(
            dhmInfants = orders.size.toString(),
            dhmFullyInfants = orders.size.toString(),
            dhmVolume = total.toString(),
            dhmAverageVolume = orders.size.toString(),
            dhmAverageLength = orders.size.toString(),

            )
    }

    private suspend fun getOrdersSearchResults(
        nameQuery: String = "",
        location: String
    ): List<OrdersItem> {
        val orders: MutableList<OrdersItem> = mutableListOf()
        try {
            fhirEngine
                .search<NutritionOrder> {

                    sort(NutritionOrder.DATETIME, Order.ASCENDING)
                    from = 0

                }
                .map {
                    loadOrders(
                        it,
                        createNutritionItem(it, getApplication<Application>().resources)
                    )

                }
                .filter { it.status.trim() == NutritionOrder.NutritionOrderStatus.ACTIVE.toString() }
                .let {

                    orders.addAll(it)
                }
        } catch (e: Exception) {
            Timber.e("Error::: ${e.localizedMessage}")
        }

        return orders
    }

    private suspend fun getOrdersSearchResultsOld(
        nameQuery: String = "",
        location: String
    ): List<OrdersItem> {
        val orders: MutableList<OrdersItem> = mutableListOf()
        fhirEngine
            .search<Encounter> {
                sort(Encounter.DATE, Order.ASCENDING)
                from = 0
            }
            .map {
                loadOrdersOld(
                    it,
                    createEncounterItem(it, getApplication<Application>().resources)
                )

            }
            .filter { it.description == "DHM Recipient" }
            .let {

                orders.addAll(it)
            }

        return orders
    }

    private suspend fun loadOrders(it: NutritionOrder, item: NutritionItem): OrdersItem {

        val patientId = it.patient.reference.drop(8)
        val baby = getPatient(patientId)

        val mother = getMothersDetails(it.patient.reference)
        val motherName = mother.first.toString()
        val motherIp = mother.second.toString()

        var dhmType = ""
        var consentGiven = ""
        var dhmReason = ""

        val observations = getObservationsPerEncounter(it.encounter.reference)
        if (observations.isNotEmpty()) {
            for (element in observations) {
                when (element.code) {
                    DHM_CONSENT -> {
                        consentGiven = element.value
                    }
                    DHM_TYPE -> {
                        dhmType = element.value
                    }
                    DHM_REASON -> {
                        dhmReason = element.value
                    }
                }
            }
        }

        return OrdersItem(
            id = it.logicalId,
            resourceId = it.logicalId,
            patientId = it.patient.reference.drop(8),
            encounterId = it.encounter.reference.drop(10),
            motherName = motherName,
            babyAge = getFormattedAge(baby.dob),
            dhmType = dhmType,
            babyName = baby.name,
            ipNumber = motherIp,
            consentGiven = consentGiven,
            dhmReason = dhmReason,
            description = dhmReason,
            status = item.status.toString()
        )
    }

    private suspend fun loadOrdersOld(it: Encounter, item: EncounterItem): OrdersItem {
        val patientId = it.subject.reference.drop(8)
        val baby = getPatient(patientId)

        val mother = getMothersDetails(it.subject.reference)
        val motherName = mother.first.toString()
        val motherIp = mother.second.toString()

        var dhmType = ""
        var consentGiven = ""
        var dhmReason = ""

        val observations = getObservationsPerEncounter("Encounter/${it.logicalId}")
        if (observations.isNotEmpty()) {
            for (element in observations) {
                when (element.code) {
                    DHM_CONSENT -> {
                        consentGiven = element.value
                    }
                    DHM_TYPE -> {
                        dhmType = element.value
                    }
                    DHM_REASON -> {
                        dhmReason = element.value
                    }
                }
            }
        }

        return OrdersItem(
            id = it.logicalId,
            resourceId = it.logicalId,
            patientId = it.subject.reference.drop(8),
            encounterId = it.logicalId,
            motherName = motherName,
            babyAge = getFormattedAge(baby.dob),
            dhmType = dhmType,
            babyName = baby.name,
            ipNumber = motherIp,
            consentGiven = consentGiven,
            dhmReason = dhmReason,
            description = it.reasonCodeFirstRep.text, status = "active"
        )
    }


    private suspend fun getPatient(patientId: String): PatientItem {
        return PatientDetailsViewModel(
            getApplication(),
            fhirEngine,
            patientId
        ).getPatient(patientId)
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

    private suspend fun getMothersDetails(patientId: String): Triple<String?, String?, String?> {
        val mother: MutableList<PatientItem> = mutableListOf()
        fhirEngine
            .search<Patient> {
                filter(
                    Patient.LINK, { value = patientId }
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

    private suspend fun getObservationsPerEncounter(
        encounterId: String
    ): List<ObservationItem> {
        val conditions: MutableList<ObservationItem> = mutableListOf()
        fhirEngine
            .search<Observation> {
                filter(
                    Observation.ENCOUNTER,
                    { value = encounterId })
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


    private suspend fun getMumSearchResults(
        nameQuery: String = "",
        location: String,
        discharged: Boolean = false
    ): List<MotherBabyItem> {
        val mumBaby: MutableList<MotherBabyItem> = mutableListOf()
        fhirEngine
            .search<Patient> {
                if (nameQuery.isNotEmpty()) {
                    filter(
                        Patient.NAME,
                        {
                            modifier = StringFilterModifier.CONTAINS
                            value = nameQuery
                        }
                    )
                }
                filterCity(this, location, discharged)
                sort(Patient.GIVEN, Order.ASCENDING)
                from = 0
            }

            .mapIndexed { index, fhirPatient ->
                mum(
                    fhirPatient.toPatientItem(
                        index + 1
                    ), index + 1
                )

            }
            .let {

                mumBaby.addAll(it)
            }

        return mumBaby
    }

    private suspend fun mum(baby: PatientItem, position: Int): MotherBabyItem {
        val mother: MutableList<PatientItem> = mutableListOf()
        fhirEngine
            .search<Patient> {
                filter(
                    Patient.LINK, { value = "Patient/${baby.resourceId}" }

                )
            }
            .mapIndexed { index, fhirPatient ->
                fhirPatient.toPatientItem(
                    index + 1
                )

            }
            .let { mother.addAll(it) }

        val obs = getObservations(baby.resourceId)
        var birthWeight = "0"
        var status = ""
        var gestation = ""
        var assessed = false
        val encounters = getEncounters(baby.resourceId)
        if (encounters.isNotEmpty()) {
            for (element in encounters) {
                when (element.code) {
                    BABY_ASSESSMENT -> {
                        assessed = true
                    }
                }
            }
        }
        if (obs.isNotEmpty()) {
            for (element in obs) {

                if (element.code == BIRTH_WEIGHT) {
                    birthWeight = element.quantity
                }
                if (element.code == GESTATION) {
                    val code = element.quantity

                    status = try {
                        if (code.toDouble() < 37) {
                            "Preterm"
                        } else {
                            "Term"
                        }
                    } catch (e: Exception) {

                        "Preterm"
                    }
                    gestation = code

                }
            }
        }
        var mumName = ""
        var motherIp = ""
        if (mother.isNotEmpty()) {
            mumName = mother[0].name
            motherIp = mother[0].resourceId
        }
        val gainRate =
            calculateWeightGainRate(
                baby.resourceId,
                baby.gender,
                status,
                birthWeight,
                gestation,
                baby.dob
            )

        return MotherBabyItem(
            id = position.toString(),
            resourceId = baby.resourceId,
            babyName = baby.name,
            motherName = mumName,
            motherIp = motherIp,
            babyIp = baby.resourceId,
            birthWeight = "$birthWeight gm",
            status = status,
            gainRate = gainRate,
            dashboard = BabyDashboard(
                assessed = assessed
            ),
            mother = MotherDashboard(),
            assessment = AssessmentItem()

        )

    }

    private suspend fun calculateWeightGainRate(
        resourceId: String,
        gender: String,
        status: String,
        birthWeight: String,
        gestationAge: String,
        dob: String
    ): String {

        var gainRate = "Normal"
        //determine if term or preterm
        val term = status == "Term"
        if (term) {
            try {
                var who = "who-boys.json"
                if (gender == "female") {
                    who = "who-girls.json"
                }
                val babiesWeight = getWeightsDataModel(resourceId)
                val jsonFileString = getJsonDataFromAsset(who)
                val gson = Gson()
                val listWhoType = object : TypeToken<List<WHOData>>() {}.type
                val growths: List<WHOData> = gson.fromJson(jsonFileString, listWhoType)
                val age = getFormattedIntAge(dob)
                //determine where the baby falls from birth weight

                val commonIndex = determineCommonIndex(growths, birthWeight)
                val nextIndex =
                    if (commonIndex == 6) {
                        commonIndex
                    } else {
                        commonIndex + 1
                    }

                // if age is 0 the use 1
                val ageInDays = if (age == 0) {
                    1
                } else {
                    age
                }
                val whoWeight = growths.find { it.day == ageInDays }
                val weight = babiesWeight.dailyData.find { it.day == ageInDays }
                if (weight != null) {
                    val actual = weight.actual
                    val childWeight = convertToKg(actual)
                    val birthWeightKg = convertToKg(birthWeight)
                    if (whoWeight != null) {
                        val zero = whoWeight.neg3
                        val one = whoWeight.neg2
                        val two = whoWeight.neg1
                        val three = whoWeight.neutral
                        val four = whoWeight.pos1
                        val five = whoWeight.pos2
                        val six = whoWeight.pos3
                        val list = listOf(zero, one, two, three, four, five, six)
                        //return value from list with index of common index
                        val normalRange = list[commonIndex]
                        val nextRange = list[nextIndex]

                        if (childWeight.toFloat() < normalRange) {
                            gainRate = if (childWeight < birthWeightKg) {
                                "Low"
                            } else {
                                "Normal"
                            }
                        } else if (childWeight.toFloat() > nextRange) {
                            gainRate = "High"
                        }

                    }
                }
            } catch (e: Exception) {
                gainRate = "Normal"
            }


        } else {

            var standard = "boy-z-score.json"
            if (gender == "female") {
                standard = "girl-z-score.json"
            }

            try {
                val babiesWeight = getWeightsDataModel(resourceId)
                val jsonFileString = getJsonDataFromAsset(standard)

                val gson = Gson()
                val listGrowthType = object : TypeToken<List<GrowthData>>() {}.type
                val growths: List<GrowthData> = gson.fromJson(jsonFileString, listGrowthType)

                val birthWeightKg = convertToKg(birthWeight)
                val commonIndex = establishCommonIndex(gestationAge, growths, birthWeightKg)
                //add +1 to common
                val nextIndex = commonIndex + 1
                val currentAgeWeight = growths.find { it.age == gestationAge.toInt() }

                //get current weight at index common
                if (currentAgeWeight != null) {
                    val normalRange = currentAgeWeight.data[commonIndex].value
                    val nextRange = currentAgeWeight.data[nextIndex].value
                    val childWeight = extractValueIndex(currentAgeWeight.age, babiesWeight)

                    Timber.e("Normal Range $normalRange Next Range $nextRange Child Weight $childWeight")
                    if (childWeight < normalRange) {
                        gainRate = if (childWeight < birthWeightKg) {
                            "Low"
                        } else {
                            "Normal"
                        }
                    } else if (childWeight > nextRange) {
                        gainRate = "High"
                    }

                }

            } catch (e: Exception) {
                Timber.e("Exception $e")

            }
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

    private suspend fun getWeightsDataModel(resourceId: String): WeightsData {
        val patient = getPatient(resourceId)
        val weight = pullWeights(resourceId)
        val data = arrayListOf<ActualData>()
        val dailyData = arrayListOf<ActualData>()
        val dataBirth = arrayListOf<ActualData>()
        val dataMonthly = arrayListOf<ActualData>()
        var dayOfLife = 0
        var weeksLife = 0
        var babyGender = ""
        val gestationAge = retrieveQuantity(resourceId, GESTATION)
        val birthWeight = retrieveQuantity(resourceId, BIRTH_WEIGHT)
        var lastKnownWeight = retrieveQuantity(resourceId, CURRENT_WEIGHT)
        //get if baby is term or preterm
        val status = retrieveQuantity(resourceId, GESTATION)
        patient.let {
            dayOfLife = getFormattedIntAge(it.dob)
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
                data.add(
                    ActualData(
                        day = added.toInt(),
                        actual = value,
                        projected = value,
                        date = entry.toString()
                    )
                )
            }
            val days = getDaysSoFarIntervalOf(patient.dob, dayOfLife + 1, 1)
            val sortedDays = sortCollected(weight)
            for ((i, entry) in days.withIndex()) {
                /**
                 * Extract daily weight
                 */
                var value = extractDailyMeasure(entry, sortedDays)
                if (value == "0.0") {
                    value = lastKnownWeight
                }
                dailyData.add(
                    ActualData(
                        day = i,
                        actual = value,
                        projected = value,
                        date = entry.toString()
                    )
                )

            }

            /**
             * Calculate the weight for the baby in weeks since birth
             */


            val weeklyString = getWeeksSoFarIntervalOf(patient.dob, weeksLife + 1, 1)
            val sortedWeekly = sortCollected(weight)
            for ((i, entry) in weeklyString.withIndex()) {
                val added = i.toFloat() + 1f
                var value = extractWeeklyMeasure(entry, 6, sortedWeekly)
                if (value == "0.0") {
                    value = lastKnownWeight
                }
                dataBirth.add(
                    ActualData(
                        day = added.toInt(),
                        actual = value,
                        projected = value,
                        date = entry.toString()
                    )
                )
            }


            val months = getFormattedAgeMonths(patient.dob)

            val monthlyString = getMonthSoFarIntervalOf(patient.dob, months + 1, 1)
            val sortedMonthly = sortCollected(weight)
            for ((i, entry) in monthlyString.withIndex()) {
                val added = i.toFloat()
                var value = extractWeeklyMeasure(entry, 28, sortedMonthly)

                if (value == "0.0") {
                    value = lastKnownWeight
                }
                dataMonthly.add(
                    ActualData(
                        day = added.toInt(),
                        actual = value,
                        projected = value,
                        date = entry.toString()
                    )
                )
            }

        }
        return WeightsData(
            status = status,
            babyGender = babyGender,
            currentWeight = data.last().actual,
            birthWeight = retrieveQuantity(resourceId, BIRTH_WEIGHT),
            currentDaily = dailyData.last().actual,
            gestationAge = gestationAge,
            dayOfLife = dayOfLife,
            data = data,
            dailyData = dailyData,
        )
    }

    private fun getFormattedAgeMonths(
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

    private suspend fun retrieveQuantity(patientId: String, code: String): String {
        var data = ""
        val obs = observationsPerCode(patientId, code)
        if (obs.isNotEmpty()) {
            val sort = sortCollected(obs)
            data = sort.last().quantity.trim()
        }
        return data
    }

    private suspend fun observationsPerCode(patientId: String, key: String): List<ObservationItem> {
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

    private suspend fun pullWeights(patientId: String): List<ObservationItem> {
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


    private suspend fun getEncounters(patientId: String): List<EncounterItem> {
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

    private suspend fun getObservations(resourceId: String): List<ObservationItem> {
        val observations: MutableList<ObservationItem> = mutableListOf()
        fhirEngine
            .search<Observation> {
                filter(Observation.SUBJECT, { value = "Patient/$resourceId" })
                sort(Observation.DATE, Order.DESCENDING)
            }
            .map {
                createObservationItem(
                    it,
                    getApplication<Application>().resources
                )
            }
            .let { observations.addAll(it) }
        return observations
    }


    private suspend fun getSearchResults(
        nameQuery: String = "",
        location: String,
        discharged: Boolean
    ): List<PatientItem> {
        val patients: MutableList<PatientItem> = mutableListOf()
        fhirEngine
            .search<Patient> {
                if (nameQuery.isNotEmpty()) {
                    filter(
                        Patient.NAME,
                        {
                            modifier = StringFilterModifier.CONTAINS
                            value = nameQuery
                        }
                    )

                }
                filterCity(this, location, discharged)
                sort(Patient.GIVEN, Order.ASCENDING)
                from = 0
            }
            .mapIndexed { index, fhirPatient -> fhirPatient.toPatientItem(index + 1) }
            .let { patients.addAll(it) }

        return patients
    }

    private fun filterCity(search: Search, location: String, discharged: Boolean) {
        search.filter(Patient.ADDRESS_POSTALCODE, { value = SYNC_VALUE })
        search.filter(Patient.ADDRESS_STATE, {
            value = if (discharged) {
                SYNC_STATE
            } else {
                SYNC_VALUE
            }
        })
    }


    private suspend fun retrieveFeedingTimes(): StaticCharts {
        val feeds: MutableList<PieItem> = mutableListOf()
        val times: MutableList<PieItem> = mutableListOf()
        val random = Random()

        for (i in feedingTimes) {
            times.add(PieItem(extractPerHour(i.key), i.key, i.value))
        }

        for (i in feedTypes) {
            feeds.add(PieItem(random.nextInt(24).toString(), i.key, i.value))
        }
        return StaticCharts(feeds = feeds, times = times)
    }

    private suspend fun extractPerHour(key: String): String {
        var i = 0
        val feedingTimes = countFeedingIntervals(FED_AFTER)
        feedingTimes.forEach {
            if (key == it.value.trim()) {
                i++
            }
            Timber.e("Extraction Value ${it.value} Searched $key Results $i")
        }
        return i.toString()

    }

    private suspend fun countFeedingIntervals(key: String): List<ObservationItem> {

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
                sort(Observation.VALUE_DATE, Order.DESCENDING)
            }
            .map {
                createObservationItem(
                    it,
                    getApplication<Application>().resources
                )
            }
            .let { obs.addAll(it) }
        return obs
    }


    class PatientListViewModelFactory(
        private val application: Application,
        private val fhirEngine: FhirEngine,
        private val location: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PatientListViewModel::class.java)) {
                return PatientListViewModel(application, fhirEngine, location) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}

internal fun Patient.toPatientItem(position: Int): PatientItem {
    // Show nothing if no values available for gender and date of birth.
    val patientId = if (hasIdElement()) idElement.idPart else ""
    val name = if (hasName()) name[0].nameAsSingleString else ""
    val gender = if (hasGenderElement()) genderElement.valueAsString else ""
    val dob = if (hasBirthDateElement()) birthDateElement.valueAsString else ""
    val phone = if (hasTelecom()) telecom[0].value else ""
    val city = if (hasAddress()) address[0].city else ""
    val country = if (hasAddress()) address[0].country else ""
    val state = if (hasAddress()) address[0].state else ""
    val district = if (hasAddress()) address[0].district else ""
    val region = if (hasAddress()) address[0].text else ""
    val isActive = active
    val html: String = if (hasText()) text.div.valueAsString else ""
    val reference: String = if (hasLink()) link[0].other.reference else ""


    return PatientItem(
        id = position.toString(),
        resourceId = patientId,
        name = name,
        gender = gender ?: "",
        dob = dob ?: "",
        phone = phone ?: "",
        city = city ?: "",
        country = country ?: "",
        isActive = isActive,
        html = html,
        state = state ?: "",
        district = district ?: "",
        region = region ?: "",
        reference = reference ?: ""
    )
}


/***
 * Child details
 * ***/
internal fun RelatedPerson.toRelatedPersonItem(position: Int): RelatedPersonItem {
    // Show nothing if no values available for gender and date of birth.
    val patientId = if (hasIdElement()) idElement.idPart else ""
    val name = if (hasName()) name[0].nameAsSingleString else ""
    val gender = if (hasGenderElement()) genderElement.valueAsString else ""
    val dob = if (hasBirthDateElement()) birthDateElement.valueAsString else ""

    return RelatedPersonItem(
        id = patientId,
        name = name,
        gender = gender ?: "",
        dob = dob ?: "",
    )
}
