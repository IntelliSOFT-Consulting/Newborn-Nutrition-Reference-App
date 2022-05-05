package com.intellisoft.nndak.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.gson.Gson
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.helper_class.*
import com.intellisoft.nndak.logic.Logics
import com.intellisoft.nndak.screens.ScreenerFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.math.BigDecimal
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.*
import org.hl7.fhir.r4.model.codesystems.RiskProbability
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.util.*

const val TAG = "ScreenerViewModel"

/** ViewModel for screener questionnaire screen {@link ScreenerEncounterFragment}. */
class ScreenerViewModel(application: Application, private val state: SavedStateHandle) :
    AndroidViewModel(application) {
    val questionnaire: String
        get() = getQuestionnaireJson()
    val isResourcesSaved = MutableLiveData<Boolean>()

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

            val itemsList1 = questionnaireResponse.item
            val edd = getEDD(itemsList1)

            val context = FhirContext.forR4()

            Log.e("------ ", "-------")
            println(edd)

            val subjectReference = Reference("Patient/$patientId")
            val encounterId = generateUuid()

            if (isRequiredFieldMissing(bundle)) {
                isResourcesSaved.value = false
                return@launch
            }

            val formatHelper = FormatHelper()
            val date = Date()

//            val observation = Observation()
//            observation
//                .code
//                .addCoding()
//                .setSystem("http://loinc.org")
//                .setCode("edd").display ="Expected Date of Delivery"
//            observation.value = Quantity().dateTimeValue()
//
//            bundle.addEntry()
//                .setResource(observation)
//                .request.url = "Observation"


            Timber.d(
                "Questionnaire Response:::: " + context.newJsonParser()
                    .encodeResourceToString(questionnaireResponse)
            )

            saveResources(bundle, subjectReference, encounterId)
            generateRiskAssessmentResource(bundle, subjectReference, encounterId)
            isResourcesSaved.value = true
        }
    }

    private suspend fun getEDD
                (itemsList1: MutableList<QuestionnaireResponse.QuestionnaireResponseItemComponent>):Date?{

        val formatHelper = FormatHelper()

        var edd: Date? = null

        for(mainItem in itemsList1){

            val mainLinkId = mainItem.linkId
            val subItemList = mainItem.item
            if (mainLinkId == "2.0.0"){

                for (subItem in subItemList){

                    val subItemLinkId = subItem.linkId
                    val subSubItemList = subItem.item

                    val job = Job()

                    CoroutineScope(Dispatchers.IO + job).launch {
                        if (subItemLinkId == "2.1.2"){

                            for (subSubItem in subSubItemList){

                                val pregnancyDetailsList = subSubItem.item

                                for (pregnancyDetails in pregnancyDetailsList){

                                    val menstrualAnswerList = pregnancyDetails.answer
                                    val linkId = pregnancyDetails.linkId

                                    for (pregnancyDetailsItem in menstrualAnswerList){
                                        val lmp = pregnancyDetailsItem.value.dateTimeValue().value
                                        val eddStr = formatHelper.getCalculations(lmp.toString())
                                        edd = formatHelper.convertDate(eddStr)
                                    }

                                }


                            }

                        }
                    }.join()


                }

            }

        }

        return edd




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

                /* is Condition -> {
                     if (resource.hasCode() && !resource.hasPrimitiveValue()) {
                         return true
                     }
                 }*/
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
            riskProbability?.let { riskProbability ->
                val riskAssessment =
                    RiskAssessment().apply {
                        id = generateUuid()
                        subject = subjectReference
                        encounter = Reference("Encounter/$encounterId")
                        addPrediction().apply {
                            qualitativeRisk =
                                CodeableConcept().apply {
                                    addCoding().updateRiskProbability(
                                        riskProbability
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
    private fun customMessage(text: String, context: Context){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()

    }


}
