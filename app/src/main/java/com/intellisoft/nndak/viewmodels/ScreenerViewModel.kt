package com.intellisoft.nndak.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.gson.Gson
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.helper_class.*
import com.intellisoft.nndak.logic.Logics
import com.intellisoft.nndak.models.ApGar
import com.intellisoft.nndak.screens.ScreenerFragment
import java.math.BigDecimal
import java.util.UUID
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.*
import org.hl7.fhir.r4.model.codesystems.RiskProbability
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber

const val TAG = "ScreenerViewModel"

/** ViewModel for screener questionnaire screen {@link ScreenerEncounterFragment}. */
class ScreenerViewModel(application: Application, private val state: SavedStateHandle) :
    AndroidViewModel(application) {
    val questionnaire: String
        get() = getQuestionnaireJson()
    val isResourcesSaved = MutableLiveData<Boolean>()
    val apgarScore = MutableLiveData<ApGar>()
    var isSafe = false

    private val questionnaireResource: Questionnaire
        get() = FhirContext.forR4().newJsonParser().parseResource(questionnaire) as Questionnaire
    private var questionnaireJson: String? = null
    private var fhirEngine: FhirEngine = FhirApplication.fhirEngine(application.applicationContext)

    fun saveScreenerEncounter(questionnaireResponse: QuestionnaireResponse, patientId: String) {
        viewModelScope.launch {
            val bundle =
                ResourceMapper.extract(
                    getApplication(),
                    questionnaireResource,
                    questionnaireResponse
                )
            val context = FhirContext.forR4()


            val questionnaire =
                context.newJsonParser().encodeResourceToString(questionnaireResponse)


            val json = JSONObject(questionnaire)
            val itemJsonArray = json.getJSONArray("item")
            for (i in 0 until itemJsonArray.length()) {

                val item = itemJsonArray.getJSONObject(i)
                var jsonObject = JSONObject()

                val jsonArrayItem = item.getJSONArray("item")
                for (j in 0 until jsonArrayItem.length()) {

                    val jsonObjectItem = jsonArrayItem.getJSONObject(j)

                    val linkIdPeriods = jsonObjectItem.getString("linkId")

                    if (linkIdPeriods == "2.1.2") {

                        val jsonArrayItem2 = jsonObjectItem.getJSONArray("item")

                        for (k in 0 until jsonArrayItem2.length()) {

                            val jsonObjectItem3 = jsonArrayItem2.getJSONObject(k)
                            val jsonArrayItem3 = jsonObjectItem3.getJSONArray("item")

                            for (l in 0 until jsonArrayItem3.length()) {

                                val jsonObjectItem4 = jsonArrayItem3.getJSONObject(l)
                                val linkIdPeriods1 = jsonObjectItem4.getString("linkId")

                                if (linkIdPeriods1 == "menstrualPeriod") {

                                    val jsonArrayPeriods = jsonObjectItem4.getJSONArray("answer")

                                    for (m in 0 until jsonArrayPeriods.length()) {

                                        val jsonObjectItemPeriods =
                                            jsonArrayPeriods.getJSONObject(m)
                                        val valueDate = jsonObjectItemPeriods.getString("valueDate")

                                        //Check if the date is valid
                                        val todayDate = FormatHelper().getTodayDate()
                                        val formattedDate = FormatHelper().convertDate(valueDate)

                                        val isDateValid =
                                            FormatHelper().checkDate(formattedDate, todayDate)
                                        if (isDateValid) {

                                            //Get the calculations

                                            val edd = FormatHelper().getCalculations(formattedDate)

                                            val dbAnswer = DbAnswer(edd)
                                            val dbAnswerList = ArrayList<DbAnswer>()
                                            dbAnswerList.add(dbAnswer)

                                            val dbValueDate = DbValueDate("2.1.3", dbAnswerList)
                                            val dbValueDateList = ArrayList<DbValueDate>()
                                            dbValueDateList.add(dbValueDate)

                                            val dbItem = DbItem("1.1.0", dbValueDateList)
                                            val dbItemList = ArrayList<DbItem>()
                                            dbItemList.add(dbItem)

                                            val eddJson = DbQuestionnaireData(
                                                "expectedDateDelivery",
                                                dbItemList
                                            )
                                            val outputJson = Gson().toJson(eddJson)
                                            jsonObject = JSONObject(outputJson)
                                            jsonArrayItem.put(jsonObject)

                                        } else {
                                            //Send error message that date is beyond today

                                        }


                                    }

                                }


                            }


                        }

                    }


                }


            }
            //Add Edd to Questionnaire

            Log.e("-------- ", json.toString())


            Timber.d(
                "Questionnaire Response:::: " + context.newJsonParser()
                    .encodeResourceToString(questionnaireResponse)
            )


            val subjectReference = Reference("Patient/$patientId")
            val encounterId = generateUuid()
            if (isRequiredFieldMissing(bundle)) {
                isResourcesSaved.value = false
                return@launch
            }
            saveResources(bundle, subjectReference, encounterId)
            generateRiskAssessmentResource(bundle, subjectReference, encounterId)
            isResourcesSaved.value = true
        }
    }

    fun saveChild(questionnaireResponse: QuestionnaireResponse, patientId: String) {
        viewModelScope.launch {
            val entry =
                ResourceMapper.extract(
                    getApplication(),
                    questionnaireResource,
                    questionnaireResponse
                )
                    .entryFirstRep
            if (entry.resource !is Patient) return@launch
            val child = entry.resource as Patient

            if (child.hasBirthDate() && child.hasGender()) {

                val birthDate = child.birthDate.toString()
                val todayDate = FormatHelper().getTodayDate()
                val isDateValid = FormatHelper().checkDate(birthDate, todayDate)

                if (isDateValid) {
                    val subjectReference = Reference("Patient/$patientId")
                    child.active = true
                    child.id = generateUuid()

                    fhirEngine.create(child)
                    isResourcesSaved.value = true
                    return@launch

                }

                isResourcesSaved.value = false
            }

            isResourcesSaved.value = false
        }
    }

    fun saveRelatedPerson(questionnaireResponse: QuestionnaireResponse, patientId: String) {
        viewModelScope.launch {
            val bundle =
                ResourceMapper.extract(
                    getApplication(),
                    questionnaireResource,
                    questionnaireResponse
                ).entryFirstRep
            if (bundle.resource !is Patient) return@launch
            val relatedPerson = bundle.resource as Patient

            if (relatedPerson.hasBirthDate() && relatedPerson.hasGender()
            ) {
                Timber.d("Name ${relatedPerson.name[0].family}")
                val subjectReference = Reference("Patient/$patientId")
                relatedPerson.active = true
                relatedPerson.id = generateUuid()
                relatedPerson.linkFirstRep.other = subjectReference
                fhirEngine.create(relatedPerson)
                isResourcesSaved.value = true
                return@launch

            }
            isResourcesSaved.value = false
        }
    }

    fun saveMaternity(questionnaireResponse: QuestionnaireResponse, patientId: String) {
        viewModelScope.launch {
            val bundle =
                ResourceMapper.extract(
                    getApplication(),
                    questionnaireResource,
                    questionnaireResponse
                )
            val context = FhirContext.forR4()
            Timber.d(
                "Questionnaire Response:::: " + context.newJsonParser()
                    .encodeResourceToString(questionnaireResponse)
            )


            val subjectReference = Reference("Patient/$patientId")
            val encounterId = generateUuid()
            if (isRequiredFieldMissing(bundle)) {
                isResourcesSaved.value = false
                return@launch
            }
            saveResources(bundle, subjectReference, encounterId)
            generateRiskAssessmentResource(bundle, subjectReference, encounterId)
            isResourcesSaved.value = true
        }
    }

    fun saveApgar(questionnaireResponse: QuestionnaireResponse, patientId: String) {
        val score = mutableListOf<Int>()
        viewModelScope.launch {
            val bundle =
                ResourceMapper.extract(
                    getApplication(),
                    questionnaireResource,
                    questionnaireResponse
                )
            val context = FhirContext.forR4()


            val questionnaire =
                context.newJsonParser().encodeResourceToString(questionnaireResponse)
            try {
                val json = JSONObject(questionnaire)
                val item = json.getJSONArray("item")
                val parent = item.getJSONObject(0).getString("item")

                val common = JSONArray(parent)
                /**
                 * Skip the 1st item
                 * **/
                for (i in 1 until common.length()) {

                    val child = common.getJSONObject(i)
                    val childItem = child.getJSONArray("item")

                    for (j in 0 until childItem.length()) {

                        val answer = childItem.getJSONObject(j)
                        val childAnswer = answer.getJSONArray("answer")
                        val valueCoding = childAnswer.getJSONObject(0).getString("valueCoding")
                        val finalAnswer = JSONObject(valueCoding)
                        val display = finalAnswer.getString("display")
                        score.add(display.toInt())
                    }
                }

                val total = score.sum()


                val subjectReference = Reference("Patient/$patientId")
                val encounterId = generateUuid()
                if (isRequiredFieldMissing(bundle)) {
                    apgarScore.value = ApGar("", "Check All Inputs", false)
                    return@launch
                }
                if (total <= 3) {
                    isSafe = false
                }
                if (total in 4..6) {
                    isSafe = true
                }
                if (total > 6) {
                    isSafe = true
                }

                saveResources(bundle, subjectReference, encounterId)
                generateApgarAssessmentResource(bundle, subjectReference, encounterId, total)
                apgarScore.value = ApGar("$total", "Apgar Score Successfully Recorded", isSafe)
            } catch (e: Exception) {
                Timber.d("Exception:::: ${e.printStackTrace()}")
                apgarScore.value = ApGar("", "Check All Inputs", false)
                return@launch
            }
        }
    }

    fun saveCarePlan(questionnaireResponse: QuestionnaireResponse, patientId: String) {
        viewModelScope.launch {
            val bundle =
                ResourceMapper.extract(
                    getApplication(),
                    questionnaireResource,
                    questionnaireResponse
                ).entryFirstRep
            if (bundle.resource !is CarePlan) return@launch
            val carePlan = bundle.resource as CarePlan

            if (carePlan.hasDescription()
            ) {
                val subjectReference = Reference("Patient/$patientId")

                carePlan.id = generateUuid()
                carePlan.subject = subjectReference
                fhirEngine.create(carePlan)
                isResourcesSaved.value = true
                return@launch

            }
            isResourcesSaved.value = false
        }
    }

    private suspend fun saveResources(
        bundle: Bundle,
        subjectReference: Reference,
        encounterId: String
    ) {
        val encounterReference = Reference("Encounter/$encounterId")
        bundle.entry.forEach {
            when (val resource = it.resource) {
                is Observation -> {
                    if (resource.hasCode()) {
                        resource.id = generateUuid()
                        resource.subject = subjectReference
                        resource.encounter = encounterReference
                        saveResourceToDatabase(resource)
                    }
                }
                is Condition -> {
                    if (resource.hasCode()) {
                        resource.id = generateUuid()
                        resource.subject = subjectReference
                        resource.encounter = encounterReference
                        saveResourceToDatabase(resource)
                    }
                }
                is Encounter -> {
                    resource.subject = subjectReference
                    resource.id = encounterId
                    saveResourceToDatabase(resource)
                }
            }
        }
    }

    private fun isRequiredFieldMissing(bundle: Bundle): Boolean {
        bundle.entry.forEach {
            val resource = it.resource
            when (resource) {
                is Observation -> {
                    if (resource.hasValueQuantity() && !resource.valueQuantity.hasValueElement()) {
                        return true
                    }
                }


                // TODO check other resources inputs
            }
        }
        return false
    }

    private suspend fun saveResourceToDatabase(resource: Resource) {
        fhirEngine.create(resource)
    }

    private fun getQuestionnaireJson(): String {
        questionnaireJson?.let {
            return it
        }
        questionnaireJson =
            readFileFromAssets(state[ScreenerFragment.QUESTIONNAIRE_FILE_PATH_KEY]!!)
        return questionnaireJson!!
    }

    private fun readFileFromAssets(filename: String): String {
        return getApplication<Application>().assets.open(filename).bufferedReader().use {
            it.readText()
        }
    }

    private fun generateUuid(): String {
        return UUID.randomUUID().toString()
    }

    private suspend fun generateApgarAssessmentResource(
        bundle: Bundle,
        subjectReference: Reference,
        encounterId: String,
        total: Int
    ) {
        val riskProbability = getProbability(total)
        riskProbability?.let { rProbability ->
            val riskAssessment =
                RiskAssessment().apply {
                    id = generateUuid()
                    subject = subjectReference
                    encounter = Reference("Encounter/$encounterId")
                    addPrediction().apply {
                        qualitativeRisk =
                            CodeableConcept().apply {
                                addCoding().updateRiskProbability(
                                    rProbability
                                )
                            }
                    }
                    occurrence = DateTimeType.now()
                }
            saveResourceToDatabase(riskAssessment)
        }

    }

    private fun getProbability(
        total: Int
    ): RiskProbability? {
        if (total <= 3) return RiskProbability.HIGH else if (total in 4..6) return RiskProbability.MODERATE else if (total > 6) return RiskProbability.LOW
        return null
    }

    private suspend fun generateRiskAssessmentResource(
        bundle: Bundle,
        subjectReference: Reference,
        encounterId: String
    ) {
        val spO2 = getSpO2(bundle)
        spO2?.let {
            val isSymptomPresent = isSymptomPresent(bundle)
            val isComorbidityPresent = isComorbidityPresent(bundle)
            val riskProbability = getRiskProbability(isSymptomPresent, isComorbidityPresent, it)
            riskProbability?.let { rProbability ->
                val riskAssessment =
                    RiskAssessment().apply {
                        id = generateUuid()
                        subject = subjectReference
                        encounter = Reference("Encounter/$encounterId")
                        addPrediction().apply {
                            qualitativeRisk =
                                CodeableConcept().apply {
                                    addCoding().updateRiskProbability(
                                        rProbability
                                    )
                                }
                        }
                        occurrence = DateTimeType.now()
                    }
                saveResourceToDatabase(riskAssessment)
            }
        }
    }

    private fun getRiskProbability(
        isSymptomPresent: Boolean,
        isComorbidityPresent: Boolean,
        spO2: BigDecimal
    ): RiskProbability? {
        if (spO2 < BigDecimal(90)) {
            return RiskProbability.HIGH
        } else if (spO2 >= BigDecimal(90) && spO2 < BigDecimal(94)) {
            return RiskProbability.MODERATE
        } else if (isSymptomPresent) {
            return RiskProbability.MODERATE
        } else if (spO2 >= BigDecimal(94) && isComorbidityPresent) {
            return RiskProbability.MODERATE
        } else if (spO2 >= BigDecimal(94) && !isComorbidityPresent) {
            return RiskProbability.LOW
        }
        return null
    }

    private fun Coding.updateRiskProbability(riskProbability: RiskProbability) {
        code = riskProbability.toCode()
        display = riskProbability.display
    }

    private fun getSpO2(bundle: Bundle): BigDecimal? {
        return bundle
            .entry
            .asSequence()
            .filter { it.resource is Observation }
            .map { it.resource as Observation }
            .filter {
                it.hasCode() && it.code.hasCoding() && it.code.coding.first().code.equals(
                    Logics.SPO2
                )
            }
            .map { it.valueQuantity.value }
            .firstOrNull()
    }

    private fun isSymptomPresent(bundle: Bundle): Boolean {
        val count =
            bundle
                .entry
                .filter { it.resource is Observation }
                .map { it.resource as Observation }
                .filter { it.hasCode() && it.code.hasCoding() }
                .flatMap { it.code.coding }
                .map { it.code }
                .filter { isSymptomPresent(it) }
                .count()
        return count > 0
    }

    private fun isSymptomPresent(symptom: String): Boolean {
        return Logics.symptoms.contains(symptom)
    }

    private fun isComorbidityPresent(bundle: Bundle): Boolean {
        val count =
            bundle
                .entry
                .filter { it.resource is Condition }
                .map { it.resource as Condition }
                .filter { it.hasCode() && it.code.hasCoding() }
                .flatMap { it.code.coding }
                .map { it.code }
                .filter { isComorbidityPresent(it) }
                .count()
        return count > 0
    }

    private fun isComorbidityPresent(comorbidity: String): Boolean {
        return Logics.comorbidities.contains(comorbidity)
    }

    /***
     * apgar score
     * ***/


}

