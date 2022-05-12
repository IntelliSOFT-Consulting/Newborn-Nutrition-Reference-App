package com.intellisoft.nndak.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.gson.Gson
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.data.User
import com.intellisoft.nndak.helper_class.*
import com.intellisoft.nndak.logic.Logics
import com.intellisoft.nndak.models.ApGar
import com.intellisoft.nndak.models.BasicThree
import com.intellisoft.nndak.screens.ScreenerFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.*
import org.hl7.fhir.r4.model.codesystems.RiskProbability
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.math.BigDecimal
import java.util.*


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

    private val user = FhirApplication.getProfile(application.applicationContext)
    private val gson = Gson()


    fun saveScreenerEncounter(questionnaireResponse: QuestionnaireResponse, patientId: String) {
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


    private suspend fun getEDD(itemsList1: MutableList<QuestionnaireResponse.QuestionnaireResponseItemComponent>): BasicThree {

        val formatHelper = FormatHelper()

        var basicThree = BasicThree("", "", "")

        for (mainItem in itemsList1) {

            val mainLinkId = mainItem.linkId
            val subItemList = mainItem.item
            if (mainLinkId == "2.0.0") {

                for (subItem in subItemList) {

                    val subItemLinkId = subItem.linkId
                    val subSubItemList = subItem.item

                    val job = Job()

                    CoroutineScope(Dispatchers.IO + job).launch {
                        if (subItemLinkId == "2.1.2") {

                            for (subSubItem in subSubItemList) {

                                val pregnancyDetailsList = subSubItem.item

                                for (pregnancyDetails in pregnancyDetailsList) {

                                    val menstrualAnswerList = pregnancyDetails.answer

                                    for (pregnancyDetailsItem in menstrualAnswerList) {
                                        val lmp = pregnancyDetailsItem.value.dateTimeValue().value
                                        val eddStr = formatHelper.getCalculations(lmp.toString())
                                        val ges = formatHelper.calculateGestation(lmp.toString())
                                        basicThree = BasicThree(
                                            formatHelper.refineLMP(lmp.toString()),
                                            eddStr,
                                            ges
                                        )
                                    }

                                }


                            }

                        }
                    }.join()


                }

            }

        }

        return basicThree
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
                val birthDate = relatedPerson.birthDate.toString()
                val todayDate = FormatHelper().getTodayDate()
                val isDateValid = FormatHelper().checkDate(birthDate, todayDate)

                if (isDateValid) {
                    val subjectReference = Reference("Patient/$patientId")
                    relatedPerson.active = true
                    relatedPerson.id = generateUuid()
                    relatedPerson.linkFirstRep.other = subjectReference
                    fhirEngine.create(relatedPerson)
                    isResourcesSaved.value = true
                    return@launch
                } else {
                    isResourcesSaved.value = false
                }

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


            val questionnaire =
                context.newJsonParser().encodeResourceToString(questionnaireResponse)
            try {
                if (isRequiredFieldMissing(bundle)) {
                    isResourcesSaved.value = false
                    return@launch
                }

                val value = extractStatus(questionnaire)


                val qh = QuestionnaireHelper()
                bundle.addEntry()
                    .setResource(
                        qh.codingQuestionnaire(
                            "Mother's Health",
                            value,
                            value
                        )
                    )
                    .request.url = "Observation"
                val itemsList1 = questionnaireResponse.item
                val basicThree = getEDD(itemsList1)

                if (basicThree.edd.isNotEmpty() && basicThree.lmp.isNotEmpty()) {
                    bundle.addEntry()
                        .setResource(
                            qh.codingQuestionnaire(
                                "Expected Date of Delivery",
                                "Expected Date of Delivery",
                                basicThree.edd
                            )
                        )
                        .request.url = "Observation"
                    bundle.addEntry()
                        .setResource(
                            qh.codingQuestionnaire(
                                "Last Menstrual Period",
                                "Last Menstrual Period",
                                basicThree.lmp
                            )
                        )
                        .request.url = "Observation"
                }


                /*     bundle.addEntry()
                         .setResource(
                             qh.codingQuestionnaire(
                                 "Gestation",
                                 "Gestation",
                                 basicThree.gestation
                             )
                         )
                         .request.url = "Observation"
     */

                val subjectReference = Reference("Patient/$patientId")
                val encounterId = generateUuid()
                saveResources(bundle, subjectReference, encounterId)
                generateRiskAssessmentResource(bundle, subjectReference, encounterId)
                isResourcesSaved.value = true
                // return@launch


            } catch (e: Exception) {
                Timber.d("Exception:::: ${e.printStackTrace()}")
                isResourcesSaved.value = false
                return@launch
            }


        }
    }

    private fun extractStatus(questionnaire: String): String {
        val json = JSONObject(questionnaire)
        val common = json.getJSONArray("item")
        var value = ""
        for (i in 0 until common.length()) {

            val item = common.getJSONObject(i)
            val parent = item.getJSONArray("item")
            for (j in 0 until parent.length()) {

                val itemChild = parent.getJSONObject(j)
                val child = itemChild.getJSONArray("item")
                for (k in 0 until child.length()) {
                    val inner = child.getJSONObject(k)
                    val childChild = inner.getString("linkId")

                    if (childChild == "Mothers-Status") {

                        value = extractValueString(inner)

                    }
                }
            }
        }
        return value
    }

    fun saveAssessment(questionnaireResponse: QuestionnaireResponse, patientId: String) {

        viewModelScope.launch {
            val bundle =
                ResourceMapper.extract(
                    getApplication(),
                    questionnaireResource,
                    questionnaireResponse
                )
            val context = FhirContext.forR4()
            val qh = QuestionnaireHelper()

            val questionnaire =
                context.newJsonParser().encodeResourceToString(questionnaireResponse)
            Timber.e("Questionnaire  $questionnaire")
            try {
                val json = JSONObject(questionnaire)
                val common = json.getJSONArray("item")
                for (i in 0 until common.length()) {

                    val item = common.getJSONObject(i)
                    val parent = item.getJSONArray("item")
                    for (j in 0 until parent.length()) {

                        val itemChild = parent.getJSONObject(j)
                        val child = itemChild.getJSONArray("item")
                        for (k in 0 until child.length()) {
                            val inner = child.getJSONObject(k)
                            val childChild = inner.getString("linkId")

                            if (childChild == "Time-Seen") {

                                val value = extractValueDateTime(inner)

                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Time Baby Seen",
                                            "${value.substring(0, 10)} - ${
                                                value.substring(
                                                    11,
                                                    16
                                                )
                                            }",
                                            "${value.substring(0, 10)} - ${
                                                value.substring(
                                                    11,
                                                    16
                                                )
                                            }",
                                        )
                                    )
                                    .request.url = "Observation"

                            }
                            if (childChild == "Shift-Time") {

                                val value = extractValueDateTime(inner)

                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Shift Time",
                                            "${value.substring(0, 10)} - ${
                                                value.substring(
                                                    11,
                                                    16
                                                )
                                            }",
                                            "${value.substring(0, 10)} - ${
                                                value.substring(
                                                    11,
                                                    16
                                                )
                                            }",
                                        )
                                    )
                                    .request.url = "Observation"

                            }
                            if (childChild == "Born-Where") {

                                val childAnswer = inner.getJSONArray("answer")
                                val value = childAnswer.getJSONObject(0).getString("valueString")

                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Born Where",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Admission-Reason") {

                                val childAnswer = inner.getJSONArray("answer")
                                val value = childAnswer.getJSONObject(0).getString("valueString")

                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Admission Reason",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Completed-By") {

                                /*   val childAnswer = inner.getJSONArray("answer")
                                   val value = childAnswer.getJSONObject(0).getString("valueString")
   */
                                /**
                                 * Retrieve Logged User
                                 */
                                val value = retrieveUser(false)

                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Completed By",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Assessment-Date") {

                                val childAnswer = inner.getJSONArray("answer")
                                val value = childAnswer.getJSONObject(0).getString("valueDate")

                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Assessment Date",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Date-Of-Discharge") {

                                val childAnswer = inner.getJSONArray("answer")
                                val value = childAnswer.getJSONObject(0).getString("valueDate")

                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Date Of Discharge",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Diagnosis-At-Discharge") {

                                val childAnswer = inner.getJSONArray("answer")
                                val value = childAnswer.getJSONObject(0).getString("valueString")

                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Diagnosis At Discharge",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Assessment") {

                                val childAnswer = inner.getJSONArray("answer")
                                val value = childAnswer.getJSONObject(0).getString("valueString")

                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Mother's Medical Condition",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Feeding-Status") {

                                val value = extractValueString(inner)

                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Feeding Status on Discharge",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Outcome-Status") {

                                val value = extractValueString(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Outcome Status on Discharge",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Diagnosis-Status") {

                                val value = extractValueString(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Diagnosis",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Intervention-Status") {

                                val value = extractValueString(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Intervention",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Recipient-Location") {

                                val value = extractValueString(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Recipient Location",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Done-Status") {

                                val value = retrieveUser(false)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Completed By",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Name-Of-Prescriber") {

                                val value = retrieveUser(false)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Name of Prescriber",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Nursing-Staff-Name") {

                                val value = retrieveUser(false)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Nursing Staff Name",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Clinician-Name") {

                                val value = retrieveUser(false)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Clinician Name",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Clinician-Designation") {

                                val value = retrieveUser(true)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Clinician Designation",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Treatment-Duration") {

                                val value = extractValueString(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Treatment Duration",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Prescribing-Instructions") {

                                val value = extractValueString(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Prescribing Instructions",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Reason-For-Receiving") {

                                val value = extractValueString(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Reason for Receiving DHM",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Any-Remarks") {

                                val value = extractValueString(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Any Feeding Remarks",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Nursing-Plan") {


                                val value = extractValueString(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Nursing Plan",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Prescription-Time") {

                                val value = extractValueDateTime(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Prescription Time",
                                            "${value.substring(0, 10)} - ${
                                                value.substring(
                                                    11,
                                                    16
                                                )
                                            }",
                                            "${value.substring(0, 10)} - ${
                                                value.substring(
                                                    11,
                                                    16
                                                )
                                            }",
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Time-Of-Feeding") {

                                val value = extractValueDateTime(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Time of Feeding",
                                            "${value.substring(0, 10)} - ${
                                                value.substring(
                                                    11,
                                                    16
                                                )
                                            }",
                                            "${value.substring(0, 10)} - ${
                                                value.substring(
                                                    11,
                                                    16
                                                )
                                            }",
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "DHM-Time") {

                                val value = extractValueDateTime(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Time of DHM Order",
                                            "${value.substring(0, 10)} - ${
                                                value.substring(
                                                    11,
                                                    16
                                                )
                                            }",
                                            "${value.substring(0, 10)} - ${
                                                value.substring(
                                                    11,
                                                    16
                                                )
                                            }",
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Time-Lactation-Support") {

                                val value = extractValueDateTime(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Time of Lactation Support",
                                            "${value.substring(0, 10)} - ${
                                                value.substring(
                                                    11,
                                                    16
                                                )
                                            }",
                                            "${value.substring(0, 10)} - ${
                                                value.substring(
                                                    11,
                                                    16
                                                )
                                            }",
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Time-of-Storage") {

                                val value = extractValueDateTime(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Time of Storage",
                                            "${value.substring(0, 10)} - ${
                                                value.substring(
                                                    11,
                                                    16
                                                )
                                            }",
                                            "${value.substring(0, 10)} - ${
                                                value.substring(
                                                    11,
                                                    16
                                                )
                                            }",
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Time-of-Expression") {

                                val value = extractValueDateTime(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Time of Expression",
                                            "${value.substring(0, 10)} - ${
                                                value.substring(
                                                    11,
                                                    16
                                                )
                                            }",
                                            "${value.substring(0, 10)} - ${
                                                value.substring(
                                                    11,
                                                    16
                                                )
                                            }",
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Nursing-Plan") {

                                val value = extractValueString(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Nursing Plan",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Batch-Number") {

                                val value = extractValueString(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Batch Number",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Donor-ID") {

                                val value = extractValueString(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Donor ID",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Shift-Notes") {

                                val value = extractValueString(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Shift Notes",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Additional-Comments") {


                                val value = extractValueString(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Additional Comments",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Feeding-Considered") {


                                val value = extractValueString(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Feeding Considered",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Legal-Guardian-Signature") {


                                val value = extractValueString(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Legal Guardian Signature",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Dispensing-Staff-Name") {


                                val value = extractValueString(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Dispensing Staff Name",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Receiving-Staff-Name") {


                                val value = extractValueString(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Dispensing Staff Name",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Consent-Date") {


                                val value = extractValueDate(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Consent Date",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Expiry-Date") {


                                val value = extractValueDate(inner)
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Expiry Date",
                                            value,
                                            value
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                        }
                    }
                }

                if (isRequiredFieldMissing(bundle)) {
                    isResourcesSaved.value = false
                    return@launch
                }

                val subjectReference = Reference("Patient/$patientId")
                val encounterId = generateUuid()
                saveResources(bundle, subjectReference, encounterId)
                generateRiskAssessmentResource(bundle, subjectReference, encounterId)
                isResourcesSaved.value = true

            } catch (e: Exception) {
                Timber.d("Exception:::: ${e.printStackTrace()}")
                isResourcesSaved.value = false
                return@launch
            }
        }
    }

    private fun extractValueDate(inner: JSONObject): String {

        val childAnswer = inner.getJSONArray("item")
        val ans = childAnswer.getJSONObject(0).getJSONArray("answer")

        return ans.getJSONObject(0).getString("valueDate")
    }

    private fun retrieveUser(isRole: Boolean): String {
        val name = try {
            val it: User = gson.fromJson(user, User::class.java)
            if (isRole) {
                it.role
            } else {
                it.names
            }
        } catch (e: Exception) {
            ""
        }
        return name
    }


    private fun extractValueString(inner: JSONObject): String {

        val childAnswer = inner.getJSONArray("item")
        val ans = childAnswer.getJSONObject(0).getJSONArray("answer")

        return ans.getJSONObject(0).getString("valueString")
    }

    private fun extractValueDateTime(inner: JSONObject): String {

        val childAnswer = inner.getJSONArray("item")
        val ans = childAnswer.getJSONObject(0).getJSONArray("answer")

        return ans.getJSONObject(0).getString("valueDateTime")

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


                val qh = QuestionnaireHelper()
                bundle.addEntry()
                    .setResource(
                        qh.codingQuestionnaire(
                            "Apgar Score",
                            total.toString(),
                            total.toString()
                        )
                    )
                    .request.url = "Observation"

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
        Timber.e("Bundle $bundle")
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

private operator fun String.div(divisor: Int): Int {
    return divisor

}

private operator fun String.rem(divisor: Int): Int {
    return divisor

}

