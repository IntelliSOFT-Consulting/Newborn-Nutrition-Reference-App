package com.intellisoft.nndak.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.logicalId
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.search
import com.google.gson.Gson
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.charts.ItemOrder
import com.intellisoft.nndak.data.DispenseData
import com.intellisoft.nndak.data.RestManager
import com.intellisoft.nndak.data.User
import com.intellisoft.nndak.helper_class.*
import com.intellisoft.nndak.logic.Logics
import com.intellisoft.nndak.logic.Logics.Companion.ADDITIONAL_FEEDS
import com.intellisoft.nndak.logic.Logics.Companion.ADJUST_PRESCRIPTION
import com.intellisoft.nndak.logic.Logics.Companion.ADMISSION_DATE
import com.intellisoft.nndak.logic.Logics.Companion.ADMISSION_WEIGHT
import com.intellisoft.nndak.logic.Logics.Companion.APGAR_SCORE
import com.intellisoft.nndak.logic.Logics.Companion.ASPHYXIA
import com.intellisoft.nndak.logic.Logics.Companion.ASSESSMENT_DATE
import com.intellisoft.nndak.logic.Logics.Companion.BABY_ASSESSMENT
import com.intellisoft.nndak.logic.Logics.Companion.BABY_WELL
import com.intellisoft.nndak.logic.Logics.Companion.BBA
import com.intellisoft.nndak.logic.Logics.Companion.BIRTH_WEIGHT
import com.intellisoft.nndak.logic.Logics.Companion.BREAST_MILK
import com.intellisoft.nndak.logic.Logics.Companion.BREAST_PROBLEM
import com.intellisoft.nndak.logic.Logics.Companion.COMPLETED_BY
import com.intellisoft.nndak.logic.Logics.Companion.CONSENT_DATE
import com.intellisoft.nndak.logic.Logics.Companion.CS_REASON
import com.intellisoft.nndak.logic.Logics.Companion.CURRENT_WEIGHT
import com.intellisoft.nndak.logic.Logics.Companion.DELIVERY_DATE
import com.intellisoft.nndak.logic.Logics.Companion.DELIVERY_METHOD
import com.intellisoft.nndak.logic.Logics.Companion.DHM_CONSENT
import com.intellisoft.nndak.logic.Logics.Companion.DHM_DISPENSING
import com.intellisoft.nndak.logic.Logics.Companion.DHM_REASON
import com.intellisoft.nndak.logic.Logics.Companion.DHM_ROUTE
import com.intellisoft.nndak.logic.Logics.Companion.DHM_STOCK
import com.intellisoft.nndak.logic.Logics.Companion.DHM_TYPE
import com.intellisoft.nndak.logic.Logics.Companion.DHM_VOLUME
import com.intellisoft.nndak.logic.Logics.Companion.DIAPER_CHANGED
import com.intellisoft.nndak.logic.Logics.Companion.EBM_ROUTE
import com.intellisoft.nndak.logic.Logics.Companion.EBM_VOLUME
import com.intellisoft.nndak.logic.Logics.Companion.EFFECTIVE_EXPRESSION
import com.intellisoft.nndak.logic.Logics.Companion.EXPRESSED_MILK
import com.intellisoft.nndak.logic.Logics.Companion.EXPRESSIONS
import com.intellisoft.nndak.logic.Logics.Companion.EXPRESSION_TIME
import com.intellisoft.nndak.logic.Logics.Companion.FED_AFTER
import com.intellisoft.nndak.logic.Logics.Companion.FEEDING_FREQUENCY
import com.intellisoft.nndak.logic.Logics.Companion.FEEDING_MONITORING
import com.intellisoft.nndak.logic.Logics.Companion.FEEDING_SUPPLEMENTS
import com.intellisoft.nndak.logic.Logics.Companion.FEEDS_DEFICIT
import com.intellisoft.nndak.logic.Logics.Companion.FEEDS_TAKEN
import com.intellisoft.nndak.logic.Logics.Companion.FEED_TYPE
import com.intellisoft.nndak.logic.Logics.Companion.FORMULA_ROUTE
import com.intellisoft.nndak.logic.Logics.Companion.FORMULA_TYPE
import com.intellisoft.nndak.logic.Logics.Companion.FORMULA_VOLUME
import com.intellisoft.nndak.logic.Logics.Companion.GESTATION
import com.intellisoft.nndak.logic.Logics.Companion.HEAD_CIRCUMFERENCE
import com.intellisoft.nndak.logic.Logics.Companion.INTERVENTIONS
import com.intellisoft.nndak.logic.Logics.Companion.IV_ROUTE
import com.intellisoft.nndak.logic.Logics.Companion.IV_VOLUME
import com.intellisoft.nndak.logic.Logics.Companion.JAUNDICE
import com.intellisoft.nndak.logic.Logics.Companion.MULTIPLE_BIRTH_TYPE
import com.intellisoft.nndak.logic.Logics.Companion.MULTIPLE_PREGNANCY
import com.intellisoft.nndak.logic.Logics.Companion.MUM_LOCATION
import com.intellisoft.nndak.logic.Logics.Companion.MUM_WELL
import com.intellisoft.nndak.logic.Logics.Companion.OTHER_CONDITIONS
import com.intellisoft.nndak.logic.Logics.Companion.PARITY
import com.intellisoft.nndak.logic.Logics.Companion.PMTCT
import com.intellisoft.nndak.logic.Logics.Companion.PRESCRIPTION
import com.intellisoft.nndak.logic.Logics.Companion.PRESCRIPTION_DATE
import com.intellisoft.nndak.logic.Logics.Companion.REMARKS
import com.intellisoft.nndak.logic.Logics.Companion.SEPSIS
import com.intellisoft.nndak.logic.Logics.Companion.STOOL
import com.intellisoft.nndak.logic.Logics.Companion.SUFFICIENT_EXPRESSION
import com.intellisoft.nndak.logic.Logics.Companion.TOTAL_FEEDS
import com.intellisoft.nndak.logic.Logics.Companion.VDRL
import com.intellisoft.nndak.logic.Logics.Companion.VOLUME_DISPENSED
import com.intellisoft.nndak.logic.Logics.Companion.VOMIT
import com.intellisoft.nndak.logic.Logics.Companion.WITHIN_ONE
import com.intellisoft.nndak.models.*
import com.intellisoft.nndak.screens.dashboard.RegistrationFragment.Companion.QUESTIONNAIRE_FILE_PATH_KEY
import com.intellisoft.nndak.utils.Constants.MAX_RESOURCE_COUNT
import com.intellisoft.nndak.utils.Constants.SYNC_VALUE
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel.Companion.createCarePlanItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.*
import org.hl7.fhir.r4.model.codesystems.RiskProbability
import org.json.JSONObject
import timber.log.Timber
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

const val TAG = "ScreenerViewModel"

/** ViewModel for screener questionnaire screen {@link ScreenerEncounterFragment}. */
class ScreenerViewModel(application: Application, private val state: SavedStateHandle) :
    AndroidViewModel(application) {
    val questionnaire: String
        get() = getQuestionnaireJson()
    val customMessage = MutableLiveData<MessageItem>()


    lateinit var title: String
    lateinit var dhm: String
    lateinit var given: String
    lateinit var patientIp: String
    lateinit var unpasteurized: String
    lateinit var pasteurized: String

    private val questionnaireResource: Questionnaire
        get() = FhirContext.forR4().newJsonParser().parseResource(questionnaire) as Questionnaire
    private var questionnaireJson: String? = null
    private var fhirEngine: FhirEngine = FhirApplication.fhirEngine(application.applicationContext)

    private val user = FhirApplication.getProfile(application.applicationContext)
    private val gson = Gson()
    private var frequency: Int = 0
    private var risk: Int = 0
    private var volume: String = "0"
    private var feeds: MutableList<String> = mutableListOf()


    fun completeAssessment(questionnaireResponse: QuestionnaireResponse, patientId: String) {
        viewModelScope.launch {
            val bundle =
                ResourceMapper.extract(
                    questionnaireResource,
                    questionnaireResponse
                )
            val context = FhirContext.forR4()
            val questionnaire =
                context.newJsonParser().encodeResourceToString(questionnaireResponse)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (isRequiredFieldMissing(bundle)) {
                        customMessage.postValue(
                            MessageItem(
                                success = false,
                                message = "Check required fields"
                            )
                        )
                        return@launch
                    }

                    /**
                     * Extract Observations, Patient Data
                     */
                    val qh = QuestionnaireHelper()
                    var feed = ""
                    var assessDate = ""
                    val json = JSONObject(questionnaire)
                    val common = json.getJSONArray("item")
                    for (i in 0 until common.length()) {
                        val inner = common.getJSONObject(i)

                        when (inner.getString("linkId")) {
                            "Assessment-Date" -> {
                                assessDate = extractResponse(inner, "valueDateTime")
                                if (assessDate.isNotEmpty()) {

                                    bundle.addEntry().setResource(
                                        qh.codingTimeQuestionnaire(
                                            ASSESSMENT_DATE,
                                            "Assessment Date",
                                            assessDate
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Current-Weight" -> {
                                val code = extractResponse(inner, "valueDecimal")
                                if (code.isNotEmpty()) {
                                    bundle.addEntry().setResource(
                                        qh.quantityQuestionnaire(
                                            CURRENT_WEIGHT,
                                            "Current Weight",
                                            "Current Weight",
                                            code,
                                            "gm"

                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Baby-Well" -> {
                                val code = extractResponseCode(inner, "valueCoding")
                                if (code.isNotEmpty()) {

                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            BABY_WELL,
                                            "Baby is Well",
                                            code
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                                val location = if (code == "Yes") {
                                    "PNU"
                                } else {
                                    "NBU"
                                }
                                bundle.addEntry().setResource(
                                    qh.codingQuestionnaire(
                                        MUM_LOCATION,
                                        "Location of Mother",
                                        location
                                    )
                                )
                                    .request.url = "Observation"

                            }
                            "Asphyxia" -> {
                                val code = extractResponseCode(inner, "valueCoding")
                                if (code.isNotEmpty()) {

                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            ASPHYXIA,
                                            "Asphyxia",
                                            code
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Jaundice" -> {
                                val code = extractResponseCode(inner, "valueCoding")
                                if (code.isNotEmpty()) {

                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            JAUNDICE,
                                            "Jaundice",
                                            code
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Neonatal-Sepsis" -> {
                                val code = extractResponseCode(inner, "valueCoding")
                                if (code.isNotEmpty()) {

                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            SEPSIS,
                                            "Neonatal Sepsis",
                                            code
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Mother-Well" -> {
                                val code = extractResponseCode(inner, "valueCoding")
                                if (code.isNotEmpty()) {

                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            MUM_WELL,
                                            "Mother is Well",
                                            code
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Breast-Problem" -> {
                                val code = extractResponseCode(inner, "valueCoding")
                                if (code.isNotEmpty()) {

                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            BREAST_PROBLEM,
                                            "Breast Problems",
                                            code
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Other-Conditions" -> {
                                val code = extractResponse(inner, "valueString")
                                if (code.isNotEmpty()) {

                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            OTHER_CONDITIONS,
                                            "Other Conditions",
                                            code
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Baby-Fed" -> {
                                feed = extractResponseCode(inner, "valueCoding")
                                if (feed.isNotEmpty()) {

                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            WITHIN_ONE,
                                            "Baby Fed within 1 Hour",
                                            feed
                                        )
                                    )
                                        .request.url = "Observation"
                                    if (feed == "Yes") {

                                        bundle.addEntry().setResource(
                                            qh.codingQuestionnaire(
                                                FED_AFTER,
                                                "Fed After",
                                                "Within 1 Hour"
                                            )
                                        )
                                            .request.url = "Observation"
                                    }
                                }
                            }
                            "Feed-Type" -> {
                                val code = extractResponseCode(inner, "valueCoding")
                                if (code.isNotEmpty()) {

                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            FEED_TYPE,
                                            "Type of Feed",
                                            code
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Fed-After" -> {
                                val code = extractResponseCode(inner, "valueCoding")
                                if (code.isNotEmpty()) {

                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            FED_AFTER,
                                            "Fed After",
                                            code
                                        )
                                    )
                                        .request.url = "Observation"
                                }

                            }
                            "Additional-Notes" -> {
                                val type = extractResponse(inner, "valueString")
                                if (type.isNotEmpty()) {
                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            REMARKS,
                                            "Additional Notes and Remarks",
                                            type,
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                        }


                    }

                    /**
                     * Handle date validation assessDate
                     */
                    val validDate = FormatHelper().dateTimeLessThanNow(assessDate)
                    if (validDate) {
                        val encounterId = generateUuid()
                        val subjectReference = Reference("Patient/$patientId")
                        title = BABY_ASSESSMENT
                        saveResources(bundle, subjectReference, encounterId, title)
                        customMessage.postValue(
                            MessageItem(
                                success = true,
                                message = "Assessment Successful"
                            )
                        )
                    } else {
                        customMessage.postValue(
                            MessageItem(
                                success = false,
                                message = "Enter valid Date"
                            )
                        )
                        return@launch
                    }
                } catch (e: Exception) {
                    Timber.d("Exception:::: ${e.printStackTrace()}")
                    customMessage.postValue(
                        MessageItem(
                            success = false,
                            message = "Please check required fields"
                        )
                    )
                    return@launch
                }
            }
        }
    }

    fun clientRegistration(questionnaireResponse: QuestionnaireResponse, patientId: String) {
        viewModelScope.launch {
            val bundle = ResourceMapper.extract(questionnaireResource, questionnaireResponse)
            val context = FhirContext.forR4()
            val questionnaire =
                context.newJsonParser().encodeResourceToString(questionnaireResponse)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (isRequiredFieldMissing(bundle)) {
                        customMessage.postValue(
                            MessageItem(
                                success = false,
                                message = "Please fill in all required fields"
                            )
                        )
                        return@launch
                    }

                    /**
                     * Extract Observations, Patient Data
                     */
                    val baby = Patient()
                    baby.active = true
                    baby.id = patientId
                    baby.addressFirstRep.postalCode = SYNC_VALUE
                    baby.addressFirstRep.state = SYNC_VALUE

                    val subjectReference = Reference("Patient/$patientId")

                    val mother = Patient()
                    mother.active = true
                    mother.linkFirstRep.other = subjectReference
                    mother.gender = Enumerations.AdministrativeGender.FEMALE

                    val qh = QuestionnaireHelper()
                    var dType = ""
                    var mPeg = ""
                    var deliveryDate = ""
                    var birthDate = ""
                    var admissionDate = ""

                    val json = JSONObject(questionnaire)
                    val common = json.getJSONArray("item")
                    for (k in 0 until common.length()) {
                        val inner = common.getJSONObject(k)
                        when (inner.getString("linkId")) {
                            "Mother-Name" -> {
                                val mumsName = extractResponse(inner, "valueString")
                                if (mumsName.isNotEmpty()) {
                                    val words =
                                        mumsName.split("\\s".toRegex()).toTypedArray()
                                    try {
                                        mother.nameFirstRep.family = words[1]
                                        mother.nameFirstRep.addGiven(words[0])
                                    } catch (e: Exception) {
                                        customMessage.postValue(
                                            MessageItem(
                                                success = false,
                                                message = "Enter mother's first and last name"
                                            )
                                        )
                                        return@launch
                                    }
                                }
                            }
                            "Parity" -> {
                                val parity = extractResponse(inner, "valueDecimal")
                                if (parity.isNotEmpty()) {

                                    bundle.addEntry().setResource(
                                        qh.quantityQuestionnaire(
                                            PARITY,
                                            "Parity",
                                            "Parity",
                                            parity.toDouble().toString(), "times"
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Ip-Number" -> {
                                val ipNo = extractResponse(inner, "valueString")
                                if (ipNo.isNotEmpty()) {
                                    mother.id = ipNo

                                } else {
                                    customMessage.postValue(
                                        MessageItem(
                                            success = false,
                                            message = "Please enter IP Number"
                                        )
                                    )
                                    return@launch
                                }
                            }
                            "PMTCT" -> {
                                val pmtct = extractResponseCode(inner, "valueCoding")
                                if (pmtct.isNotEmpty()) {
                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            PMTCT,
                                            "PMTCT",
                                            pmtct
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Multiple-Pregnancy" -> {
                                mPeg = extractResponseCode(inner, "valueCoding")
                                if (mPeg.isNotEmpty()) {
                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            MULTIPLE_PREGNANCY,
                                            "Multiple Pregnancy",
                                            mPeg
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Multiple-Birth-Type" -> {
                                if (mPeg == "Yes") {
                                    val bType = extractResponseCode(inner, "valueCoding")
                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            MULTIPLE_BIRTH_TYPE,
                                            "Multiple Birth Type",
                                            bType
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Time-Of-Delivery" -> {
                                deliveryDate = extractResponse(inner, "valueDateTime")
                                if (deliveryDate.isNotEmpty()) {
                                    bundle.addEntry().setResource(
                                        qh.codingTimeQuestionnaire(
                                            DELIVERY_DATE,
                                            "Time of Delivery",
                                            deliveryDate
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Delivery-Method" -> {
                                dType = extractResponseCode(inner, "valueCoding")
                                bundle.addEntry().setResource(
                                    qh.codingQuestionnaire(
                                        DELIVERY_METHOD,
                                        "Delivery Method",
                                        dType
                                    )
                                )
                                    .request.url = "Observation"
                            }
                            "CS-Reason" -> {
                                if (dType == "CS") {
                                    dType = extractResponseCode(inner, "valueCoding")
                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            CS_REASON,
                                            "CS Reason",
                                            dType
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "VDRL" -> {
                                val bType = extractResponseCode(inner, "valueCoding")
                                bundle.addEntry().setResource(
                                    qh.codingQuestionnaire(
                                        VDRL,
                                        "VDRL",
                                        bType
                                    )
                                )
                                    .request.url = "Observation"
                            }
                            "Date-Of-Birth" -> {
                                birthDate = extractResponse(inner, "valueDate")
                                if (birthDate.isNotEmpty()) {

                                    baby.birthDate = FormatHelper().dateOfBirth(birthDate)
                                }

                            }

                            "Baby-Name" -> {
                                val mumsName = extractResponse(inner, "valueString")
                                val words = mumsName.split("\\s".toRegex()).toTypedArray()
                                baby.nameFirstRep.family = words[1]
                                baby.nameFirstRep.addGiven(words[0])
                            }
                            "Baby-Sex" -> {
                                when (extractResponseCode(inner, "valueCoding")) {
                                    "Female" -> {
                                        baby.gender =
                                            Enumerations.AdministrativeGender.FEMALE
                                    }
                                    else -> {
                                        baby.gender =
                                            Enumerations.AdministrativeGender.FEMALE
                                    }
                                }
                            }
                            "Birth-Weight" -> {
                                val bType = extractResponse(inner, "valueDecimal")
                                if (bType.isNotEmpty()) {
                                    bundle.addEntry().setResource(
                                        qh.quantityQuestionnaire(
                                            BIRTH_WEIGHT,
                                            "Birth Weight",
                                            "Birth Weight",
                                            bType,
                                            "gm"

                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Gestation" -> {
                                val bType = extractResponse(inner, "valueDecimal")
                                if (bType.isNotEmpty()) {
                                    bundle.addEntry().setResource(
                                        qh.quantityQuestionnaire(
                                            GESTATION,
                                            "Gestation",
                                            "Gestation",
                                            bType, "wks"
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Apgar-Score" -> {
                                val bType = extractResponse(inner, "valueDecimal")
                                if (bType.isNotEmpty()) {
                                    bundle.addEntry().setResource(
                                        qh.quantityQuestionnaire(
                                            APGAR_SCORE,
                                            "Apgar Score",
                                            "Apgar Score",
                                            bType, "score"
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "BBA" -> {
                                val bType = extractResponseCode(inner, "valueCoding")
                                if (bType.isNotEmpty()) {
                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            BBA,
                                            "BBA",
                                            bType
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Head-Circumference" -> {
                                val bType = extractResponse(inner, "valueDecimal")
                                if (bType.isNotEmpty()) {
                                    bundle.addEntry().setResource(
                                        qh.quantityQuestionnaire(
                                            HEAD_CIRCUMFERENCE,
                                            "Head Circumference",
                                            "Head Circumference",
                                            bType, "cm"
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Interventions" -> {
                                val bType = extractResponse(inner, "valueString")
                                if (bType.isNotEmpty()) {
                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            INTERVENTIONS,
                                            "Interventions",
                                            bType
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Admission-Weight" -> {
                                val bType = extractResponse(inner, "valueDecimal")
                                if (bType.isNotEmpty()) {
                                    if (bType.isNotEmpty()) {
                                        bundle.addEntry().setResource(
                                            qh.quantityQuestionnaire(
                                                ADMISSION_WEIGHT,
                                                "Admission Weight",
                                                "Admission Weight",
                                                bType,
                                                "gm"

                                            )
                                        )
                                            .request.url = "Observation"
                                    }
                                }
                            }
                            "Admission-Date" -> {
                                admissionDate = extractResponse(inner, "valueDateTime")
                                if (admissionDate.isNotEmpty()) {
                                    bundle.addEntry().setResource(
                                        qh.codingTimeQuestionnaire(
                                            ADMISSION_DATE,
                                            "Admission Date",
                                            admissionDate
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Doctor-Notes" -> {
                                val bType = extractResponse(inner, "valueString")
                                if (bType.isNotEmpty()) {
                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            REMARKS,
                                            "Additional Notes",
                                            bType
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            else -> {
                                println("Items skipped...")
                            }
                        }
                    }

                    /**
                     * Check for Valid Date of birth:: should be less than today
                     */
                    val validDateOfBirth = FormatHelper().dateLessThanToday(birthDate)
                    if (validDateOfBirth) {
                        val valid =
                            FormatHelper().isSameDay(birthDate, deliveryDate.substring(0, 10))

                        val validAdmission =
                            FormatHelper().dateLessThanToday(admissionDate.substring(0, 10))
                        if (valid && validAdmission) {


                            val value = retrieveUser(false)

                            bundle.addEntry()
                                .setResource(
                                    qh.codingQuestionnaire(
                                        COMPLETED_BY,
                                        value,
                                        value
                                    )
                                )
                                .request.url = "Observation"

                            val encounterId = generateUuid()
                            title = "Client Registration"
                            saveResourceToDatabase(resource = baby)
                            saveResourceToDatabase(resource = mother)
                            saveResources(bundle, subjectReference, encounterId, title)
                            customMessage.postValue(MessageItem(true, "Success"))
                        } else {
                            if (!valid) {
                                customMessage.postValue(
                                    MessageItem(
                                        false,
                                        "Please Enter Valid Delivery Date"
                                    )
                                )
                            } else {
                                customMessage.postValue(
                                    MessageItem(
                                        false,
                                        "Please Enter Valid Admission Date"
                                    )
                                )
                            }
                        }
                    } else {
                        customMessage.postValue(
                            MessageItem(
                                false,
                                "Please Enter Valid Date of Birth"
                            )
                        )
                        return@launch
                    }

                } catch (e: Exception) {
                    Timber.d("Exception:::: ${e.printStackTrace()}")
                    customMessage.postValue(
                        MessageItem(
                            false,
                            "Please fill in all required fields"
                        )
                    )
                    return@launch

                }
            }
        }
    }

    fun feedPrescription(questionnaireResponse: QuestionnaireResponse, patientId: String) {
        viewModelScope.launch {
            val bundle =
                ResourceMapper.extract(
                    questionnaireResource,
                    questionnaireResponse
                )
            val context = FhirContext.forR4()
            val questionnaire =
                context.newJsonParser().encodeResourceToString(questionnaireResponse)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (isRequiredFieldMissing(bundle)) {
                        customMessage.postValue(
                            MessageItem(
                                success = false,
                                message = "Please fill in all required fields"
                            )
                        )
                        return@launch
                    }
                    val subjectReference = Reference("Patient/$patientId")
                    updatePreviousPrescriptions(patientId)
                    val qh = QuestionnaireHelper()
                    var totalVolume = 0f
                    var breastVolume = 0f
                    var ebmVolume = 0f
                    var formulaVolume = 0f
                    var ivVolume = 0f
                    var dhmVolume = 0f
                    var presDate = ""
                    var valueConsent = ""
                    val date = FormatHelper().getTodayDate()
                    val json = JSONObject(questionnaire)
                    val common = json.getJSONArray("item")
                    for (i in 0 until common.length()) {
                        val inner = common.getJSONObject(i)
                        when (inner.getString("linkId")) {

                            "Current-Weight" -> {
                                val value =
                                    extractResponse(inner, "valueDecimal")
                                if (value.isNotEmpty()) {
                                    bundle.addEntry().setResource(
                                        qh.quantityQuestionnaire(
                                            CURRENT_WEIGHT,
                                            "Current Weight",
                                            "Current Weight",
                                            value, "gm"
                                        )
                                    )
                                        .request.url = "Observation"

                                }
                            }
                            "Total-Feeds" -> {
                                val total =
                                    extractResponse(inner, "valueDecimal")
                                if (total.isNotEmpty()) {
                                    totalVolume = total.toFloat()
                                    bundle.addEntry().setResource(
                                        qh.quantityQuestionnaire(
                                            TOTAL_FEEDS,
                                            "Total Feeds",
                                            "Total Feeds",
                                            total, "mls"
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Feed-Frequency" -> {
                                val value =
                                    extractResponseCode(inner, "valueCoding")

                                if (value.isNotEmpty()) {
                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            FEEDING_FREQUENCY,
                                            "Feed Frequency", value,
                                        )
                                    )
                                        .request.url = "Observation"
                                }


                            }
                            "Feed-Type-Selection" -> {
                                val value = extractResponseCodeArray(inner, "valueCoding")

                                if (value.isNotEmpty()) {
                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            "Feeding-Frequency",
                                            "Feeding Frequency",
                                            value
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            /** Breast Feeding */
                            "Breast-Milk" -> {
                                if (feeds.isNotEmpty()) {
                                    if (feeds.contains("Breast Feed")) {
                                        val value =
                                            extractResponse(inner, "valueDecimal")

                                        if (value.isNotEmpty()) {
                                            breastVolume = value.toFloat()
                                            bundle.addEntry().setResource(
                                                qh.quantityQuestionnaire(
                                                    BREAST_MILK,
                                                    "Breast Milk", "Breast Milk",
                                                    value, "mls"
                                                )
                                            )
                                                .request.url = "Observation"
                                        }
                                    }
                                }
                            }

                            /**Expressed Breast Milk**/
                            "EBM-Feeding-Route" -> {
                                if (feeds.isNotEmpty()) {
                                    if (feeds.contains("EBM")) {

                                        val value =
                                            extractResponseCode(inner, "valueCoding")

                                        if (value.isNotEmpty()) {
                                            bundle.addEntry().setResource(
                                                qh.codingQuestionnaire(
                                                    EBM_ROUTE,
                                                    "EBM Feeding Route", value,
                                                )
                                            )
                                                .request.url = "Observation"
                                        }
                                    }
                                }
                            }
                            "EBM-Volume" -> {
                                if (feeds.isNotEmpty()) {
                                    if (feeds.contains("EBM")) {

                                        val value =
                                            extractResponse(inner, "valueDecimal")

                                        if (value.isNotEmpty()) {
                                            ebmVolume = value.toFloat()
                                            bundle.addEntry().setResource(
                                                qh.quantityQuestionnaire(
                                                    EBM_VOLUME,
                                                    "EBM Volume", "EBM Volume", value, "mls"
                                                )
                                            )
                                                .request.url = "Observation"
                                        }
                                    }
                                }
                            }

                            /**Formula**/
                            "Formula-Type" -> {
                                if (feeds.isNotEmpty()) {
                                    if (feeds.contains("Formula")) {
                                        val value =
                                            extractResponseCode(inner, "valueCoding")

                                        if (value.isNotEmpty()) {
                                            bundle.addEntry().setResource(
                                                qh.codingQuestionnaire(
                                                    FORMULA_TYPE,
                                                    "Formula Type", value
                                                )
                                            )
                                                .request.url = "Observation"
                                        }
                                    }
                                }
                            }
                            "Formula-Route" -> {
                                if (feeds.isNotEmpty()) {
                                    if (feeds.contains("Formula")) {
                                        val value =
                                            extractResponseCode(inner, "valueCoding")

                                        if (value.isNotEmpty()) {
                                            bundle.addEntry().setResource(
                                                qh.codingQuestionnaire(
                                                    FORMULA_ROUTE,
                                                    "Formula Route", value
                                                )
                                            )
                                                .request.url = "Observation"
                                        }
                                    }
                                }
                            }
                            "Formula-Volume" -> {
                                if (feeds.isNotEmpty()) {
                                    if (feeds.contains("Formula")) {
                                        val value =
                                            extractResponse(inner, "valueDecimal")

                                        if (value.isNotEmpty()) {
                                            formulaVolume = value.toFloat()
                                            bundle.addEntry().setResource(
                                                qh.quantityQuestionnaire(
                                                    FORMULA_VOLUME,
                                                    "Formula Volume",
                                                    "Formula Volume",
                                                    value,
                                                    "mls"
                                                )
                                            )
                                                .request.url = "Observation"
                                        }
                                    }
                                }
                            }


                            /**DHM**/

                            "DHM-Type" -> {
                                if (feeds.isNotEmpty()) {
                                    if (feeds.contains("DHM")) {
                                        val value =
                                            extractResponseCode(inner, "valueCoding")

                                        if (value.isNotEmpty()) {
                                            bundle.addEntry().setResource(
                                                qh.codingQuestionnaire(
                                                    DHM_TYPE,
                                                    "DHM Type", value
                                                )
                                            )
                                                .request.url = "Observation"
                                        }
                                    }
                                }
                            }
                            "DHM-Route" -> {
                                if (feeds.isNotEmpty()) {
                                    if (feeds.contains("DHM")) {
                                        val value =
                                            extractResponseCode(inner, "valueCoding")

                                        if (value.isNotEmpty()) {
                                            bundle.addEntry().setResource(
                                                qh.codingQuestionnaire(
                                                    DHM_ROUTE,
                                                    "DHM Route", value
                                                )
                                            )
                                                .request.url = "Observation"
                                        }
                                    }
                                }
                            }
                            "DHM-Volume" -> {
                                if (feeds.isNotEmpty()) {
                                    if (feeds.contains("DHM")) {
                                        val value =
                                            extractResponse(inner, "valueDecimal")

                                        if (value.isNotEmpty()) {
                                            dhmVolume = value.toFloat()
                                            bundle.addEntry().setResource(
                                                qh.quantityQuestionnaire(
                                                    DHM_VOLUME,
                                                    "DHM Volume", "DHM Volume", value, "mls"
                                                )
                                            )
                                                .request.url = "Observation"
                                        }
                                    }
                                }
                            }

                            "Consent-Given" -> {
                                if (feeds.isNotEmpty()) {
                                    if (feeds.contains("DHM")) {
                                        valueConsent =
                                            extractResponseCode(inner, "valueCoding")

                                        if (valueConsent.isNotEmpty()) {
                                            val consent = if (valueConsent == "Yes") {
                                                "Signed"
                                            } else {
                                                "Not Signed"
                                            }
                                            bundle.addEntry().setResource(
                                                qh.codingQuestionnaire(
                                                    DHM_CONSENT,
                                                    "Consent Given", consent,
                                                )
                                            )
                                                .request.url = "Observation"
                                        }
                                    }
                                }
                            }
                            "Consent-Date" -> {
                                if (feeds.isNotEmpty()) {
                                    if (feeds.contains("DHM")) {
                                        presDate =
                                            extractResponse(inner, "valueDate")

                                        if (presDate.isNotEmpty()) {
                                            bundle.addEntry().setResource(
                                                qh.codingQuestionnaire(
                                                    CONSENT_DATE,
                                                    "Consent Date", presDate,
                                                )
                                            )
                                                .request.url = "Observation"
                                        }
                                    }
                                }
                            }
                            "DHM-Reasons" -> {
                                if (feeds.isNotEmpty()) {
                                    if (feeds.contains("DHM")) {
                                        val value =
                                            extractResponse(inner, "valueString")

                                        if (value.isNotEmpty()) {
                                            bundle.addEntry().setResource(
                                                qh.codingQuestionnaire(
                                                    DHM_REASON,
                                                    "DHM Reasons", value,
                                                )
                                            )
                                                .request.url = "Observation"
                                        }
                                    }
                                }
                            }

                            /** IV Fluid and Additives */
                            "IV-Fluid-Route" -> {
                                if (feeds.isNotEmpty()) {
                                    if (feeds.contains("IV Fluid and Additives")) {
                                        val value =
                                            extractResponseCode(inner, "valueCoding")

                                        if (value.isNotEmpty()) {
                                            bundle.addEntry().setResource(
                                                qh.codingQuestionnaire(
                                                    IV_ROUTE,
                                                    "IV Fluid Route", value,
                                                )
                                            )
                                                .request.url = "Observation"
                                        }
                                    }
                                }
                            }
                            "IV-Fluid-Volume" -> {
                                if (feeds.isNotEmpty()) {
                                    if (feeds.contains("IV Fluid and Additives")) {
                                        val value =
                                            extractResponse(inner, "valueDecimal")

                                        if (value.isNotEmpty()) {
                                            ivVolume = value.toFloat()
                                            bundle.addEntry().setResource(
                                                qh.quantityQuestionnaire(
                                                    IV_VOLUME,
                                                    "IV Fluid Volume",
                                                    "IV-Fluid-Volume",
                                                    value,
                                                    "mls"
                                                )
                                            )
                                                .request.url = "Observation"
                                        }
                                    }
                                }
                            }

                            "Feeding-Supplements" -> {
                                val value =
                                    extractResponseCode(inner, "valueCoding")

                                if (value.isNotEmpty()) {
                                    dhm = value
                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            ADDITIONAL_FEEDS,
                                            "Feeding Supplements", value
                                        )
                                    )
                                        .request.url = "Observation"
                                }

                            }
                            "Supplements-Considered" -> {
                                if (dhm == "Yes") {
                                    val value =
                                        extractResponseCode(inner, "valueCoding")

                                    if (value.isNotEmpty()) {

                                        bundle.addEntry().setResource(
                                            qh.codingQuestionnaire(
                                                FEEDING_SUPPLEMENTS,
                                                "Supplements Considered", value
                                            )
                                        )
                                            .request.url = "Observation"
                                    }
                                }
                            }
                            "Additional-Notes" -> {
                                val value =
                                    extractResponse(inner, "valueString")

                                if (value.isNotEmpty()) {

                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            REMARKS,
                                            "Additional Notes and Remarks", value
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }

                            else -> {
                                println("Items skipped...")
                            }
                        }

                    }

                    val currentPrescribed =
                        breastVolume + ebmVolume + formulaVolume + ivVolume + dhmVolume

                    if (currentPrescribed == totalVolume) {


                        val value = retrieveUser(false)
                        bundle.addEntry()
                            .setResource(
                                qh.codingQuestionnaire(
                                    COMPLETED_BY,
                                    value,
                                    value
                                )
                            )
                            .request.url = "Observation"

                        bundle.addEntry().setResource(
                            qh.codingQuestionnaire(
                                PRESCRIPTION_DATE,
                                "Prescription Date",
                                date
                            )
                        )
                            .request.url = "Observation"
                        val encounterId = generateUuid()
                        title = PRESCRIPTION
                        val encounterReference = Reference("Encounter/$encounterId")
                        if (feeds.isNotEmpty()) {
                            if (feeds.contains("DHM")) {

                                if (valueConsent.isEmpty()) {
                                    customMessage.postValue(
                                        MessageItem(
                                            success = false,
                                            message = "Please Select if Consent was Given"
                                        )
                                    )
                                    return@launch
                                }
                                if (valueConsent == "Yes") {
                                    val isValid = FormatHelper().dateLessThanToday(presDate)
                                    if (!isValid) {
                                        customMessage.postValue(
                                            MessageItem(
                                                success = false,
                                                message = "Please Select a valid Date"
                                            )
                                        )
                                        return@launch
                                    }
                                }
                                val no = NutritionOrder()
                                no.id = generateUuid()
                                no.patient = subjectReference
                                no.encounter = encounterReference
                                no.status = NutritionOrder.NutritionOrderStatus.ACTIVE
                                no.dateTime = Date()
                                no.intent = NutritionOrder.NutritiionOrderIntent.ORDER
                                saveResourceToDatabase(no)
                            }
                        }
                        val care = CarePlan()
                        care.encounter = encounterReference
                        care.subject = subjectReference
                        care.status = CarePlan.CarePlanStatus.ACTIVE
                        care.title = title
                        care.intent = CarePlan.CarePlanIntent.ORDER
                        care.created = Date()
                        saveResourceToDatabase(care)

                        saveResources(bundle, subjectReference, encounterId, title)
                        customMessage.postValue(
                            MessageItem(
                                success = true,
                                message = "Prescription Successfully saved"
                            )
                        )
                    } else {
                        if (currentPrescribed < totalVolume) {
                            customMessage.postValue(
                                MessageItem(
                                    success = false,
                                    message = "Please check Total Volumes"
                                )
                            )
                        } else {
                            customMessage.postValue(
                                MessageItem(
                                    success = false,
                                    message = "Please check Feed Breakdown Volumes"
                                )
                            )
                        }
                    }

                } catch (e: Exception) {
                    Timber.d("Exception:::: ${e.localizedMessage}")

                    customMessage.postValue(
                        MessageItem(
                            success = false,
                            message = "Experienced Problems Processing Data, Try again"
                        )
                    )
                    return@launch

                }
            }
        }
    }

    private suspend fun updateFeedingPrescriptions(patientId: String) {

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
                PatientDetailsViewModel.createEncounterItem(
                    it,
                    getApplication<Application>().resources
                )
            }
            .filter { it.status == Encounter.EncounterStatus.INPROGRESS.toString() }
            .filter { it.code == PRESCRIPTION }
            .let { cares.addAll(it) }
        if (cares.isNotEmpty()) {
            cares.forEach { cp ->

                val subjectReference = Reference("Patient/$patientId")
                val e = Encounter()
                e.subject = subjectReference
                e.id = cp.id
                e.reasonCodeFirstRep.text = cp.code
                e.status = Encounter.EncounterStatus.FINISHED
                saveResourceToDatabase(e)
            }
        }
    }

    private suspend fun updatePreviousPrescriptions(patientId: String) {

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

        if (cares.isNotEmpty()) {
            cares.forEach { cp ->

                val encounterReference = Reference(cp.encounterId)
                val subjectReference = Reference(cp.patientId)
                val carePlan = CarePlan()
                carePlan.id = cp.resourceId
                carePlan.encounter = encounterReference
                carePlan.subject = subjectReference
                carePlan.status = CarePlan.CarePlanStatus.COMPLETED
                carePlan.title = PRESCRIPTION
                carePlan.created = Date()
                carePlan.intent = CarePlan.CarePlanIntent.ORDER
                saveResourceToDatabase(carePlan)
            }
        }
    }

    private fun extractResponse(child: JSONObject, value: String): String {

        val childAnswer = child.getJSONArray("item")
        val ans = childAnswer.getJSONObject(0).getJSONArray("answer")
        return ans.getJSONObject(0).getString(value)
    }

    private fun extractResponseQuantity(child: JSONObject, value: String): String {

        val childAnswer = child.getJSONArray("item")
        val ans = childAnswer.getJSONObject(0).getJSONArray("answer")
        return ans.getJSONObject(0).getJSONObject(value).getString("value")
    }

    private fun extractResponseCode(child: JSONObject, value: String): String {

        val childAnswer = child.getJSONArray("item")
        val ans = childAnswer.getJSONObject(0).getJSONArray("answer")

        return ans.getJSONObject(0).getJSONObject(value).getString("display")
    }

    private fun extractResponseCodeArray(child: JSONObject, value: String): String {

        val childAnswer = child.getJSONArray("item")
        val ans = childAnswer.getJSONObject(0).getJSONArray("answer")
        for (i in 0 until ans.length()) {
            val each = ans.getJSONObject(i).getJSONObject(value).getString("display")
            feeds.add(each)
        }
        return feeds.toString()
    }


    private fun calculate24HourFeed(frequency: Int, volume: String): String {
        var total = 0.0
        val times = 24 / frequency
        val j: Float = times.toFloat()
        val k: Float = volume.toFloat()
        total = (j * k).toDouble()

        return total.toString()
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

    private suspend fun saveResources(
        bundle: Bundle,
        subjectReference: Reference,
        encounterId: String,
        reason: String,
    ) {

        val encounterReference = Reference("Encounter/$encounterId")
        bundle.entry.forEach {
            when (val resource = it.resource) {
                is Observation -> {
                    if (resource.hasCode()) {
                        resource.id = generateUuid()
                        resource.subject = subjectReference
                        resource.encounter = encounterReference
                        resource.issued = Date()
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
                    resource.reasonCodeFirstRep.text = reason
                    resource.reasonCodeFirstRep.codingFirstRep.code = reason
                    resource.status = Encounter.EncounterStatus.INPROGRESS
                    saveResourceToDatabase(resource)
                }

            }
        }
    }

    private suspend fun saveFeedingResources(
        bundle: Bundle,
        subjectReference: Reference,
        basedOnReference: Reference,
        encounterId: String,
        reason: String,
    ) {

        val encounterReference = Reference("Encounter/$encounterId")
        bundle.entry.forEach {
            when (val resource = it.resource) {
                is Observation -> {
                    if (resource.hasCode()) {
                        resource.id = generateUuid()
                        resource.subject = subjectReference
                        resource.encounter = encounterReference
                        resource.issued = Date()
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
                    resource.reasonCodeFirstRep.text = reason
                    resource.reasonCodeFirstRep.codingFirstRep.code = reason
                    resource.status = Encounter.EncounterStatus.INPROGRESS
                    resource.partOf = basedOnReference
                    saveResourceToDatabase(resource)
                }

            }
        }
    }

    private suspend fun emptyObservations(
        bundle: Bundle, encounterId: String,
        reason: String,
    ) {

        val encounterReference = Reference("Encounter/$encounterId")
        bundle.entry.forEach {
            when (val resource = it.resource) {
                is Observation -> {
                    if (resource.hasCode()) {
                        resource.id = generateUuid()
                        resource.issued = Date()
                        resource.encounter = encounterReference
                        saveResourceToDatabase(resource)
                    }
                }
                is Encounter -> {
                    resource.id = encounterId
                    resource.reasonCodeFirstRep.text = reason
                    saveResourceToDatabase(resource)
                }
            }
        }
    }

    private fun isRequiredFieldMissing(bundle: Bundle): Boolean {
        bundle.entry.forEach {
            when (val resource = it.resource) {
                is Observation -> {
                    if (resource.hasValueQuantity() && !resource.valueQuantity.hasValueElement()) {
                        return true
                    }
                    if (resource.hasCode() && !resource.code.hasText()) {
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
            readFileFromAssets(state[QUESTIONNAIRE_FILE_PATH_KEY]!!)
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

    /**
     * APGAR Score
     */
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

    /**
     * Mother's Health
     */


    private suspend fun generateRiskAssessmentResource(
        bundle: Bundle,
        subjectReference: Reference,
        encounterId: String
    ) {
        val spO2 = getSpO2(bundle)
        spO2?.let {
            val isSymptomPresent = isSymptomPresent(bundle)
            val isComorbidityPresent = isComorbidityPresent(bundle)
            val riskProbability =
                getRiskProbability(isSymptomPresent, isComorbidityPresent, it)
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

    fun breastFeeding(
        questionnaireResponse: QuestionnaireResponse, breastFeeding: String,
        efficientFeeding: String, patientId: String
    ) {
        viewModelScope.launch {
            val bundle =
                ResourceMapper.extract(
                    questionnaireResource,
                    questionnaireResponse
                )
            val qh = QuestionnaireHelper()
            val context = FhirContext.forR4()
            val questionnaire =
                context.newJsonParser().encodeResourceToString(questionnaireResponse)
            try {
                bundle.addEntry()
                    .setResource(
                        qh.codingQuestionnaire(
                            "Baby-BreastFeeding",
                            breastFeeding,
                            breastFeeding
                        )
                    )
                    .request.url = "Observation"
                bundle.addEntry()
                    .setResource(
                        qh.codingQuestionnaire(
                            "BreastFeeding-Efficient",
                            efficientFeeding,
                            efficientFeeding
                        )
                    )
                    .request.url = "Observation"

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

            } catch (e: Exception) {
                customMessage.postValue(
                    MessageItem(
                        success = false,
                        message = "Experienced problems, please try again"
                    )
                )
                return@launch
            }

            val subjectReference = Reference("Patient/$patientId")

            val encounterId = generateUuid()
            title = "Breast Feeding"
            saveResources(bundle, subjectReference, encounterId, title)
            customMessage.postValue(
                MessageItem(
                    success = true,
                    message = "Update successful"
                )
            )
        }
    }

    fun feedingCues(
        questionnaireResponse: QuestionnaireResponse,
        cues: ArrayList<CodingObservation>,
        patientId: String
    ) {
        viewModelScope.launch {
            val bundle =
                ResourceMapper.extract(
                    questionnaireResource,
                    questionnaireResponse
                )
            val qh = QuestionnaireHelper()
            val context = FhirContext.forR4()
            val questionnaire =
                context.newJsonParser().encodeResourceToString(questionnaireResponse)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    cues.forEach {
                        bundle.addEntry()
                            .setResource(
                                qh.codingQuestionnaire(
                                    it.code,
                                    it.display,
                                    it.value

                                )
                            )
                            .request.url = "Observation"

                    }
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

                    val subjectReference = Reference("Patient/$patientId")

                    val encounterId = generateUuid()
                    title = "Feeding Cues"
                    saveResources(bundle, subjectReference, encounterId, title)
                    customMessage.postValue(
                        MessageItem(
                            success = true,
                            message = "Record updated successfully"
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    customMessage.postValue(
                        MessageItem(
                            success = false,
                            message = "Experienced problems, please try again"
                        )
                    )
                    return@launch
                }

            }
        }
    }

    fun milkExpressionAssessment(
        questionnaireResponse: QuestionnaireResponse, effectiveExpression: String,
        expressedSufficient: String, patientId: String
    ) {
        viewModelScope.launch {
            val bundle =
                ResourceMapper.extract(
                    questionnaireResource,
                    questionnaireResponse
                )
            val context = FhirContext.forR4()
            val questionnaire =
                context.newJsonParser().encodeResourceToString(questionnaireResponse)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (isRequiredFieldMissing(bundle)) {
                        customMessage.postValue(
                            MessageItem(
                                success = false,
                                message = "Please check required fields"
                            )
                        )
                        return@launch
                    }

                    val qh = QuestionnaireHelper()
                    var dateTime = ""
                    val json = JSONObject(questionnaire)
                    val common = json.getJSONArray("item")
                    for (i in 0 until common.length()) {
                        val inner = common.getJSONObject(i)
                        val childChild = inner.getString("linkId")

                        if (childChild == "Milk-Expressed") {
                            val volume = extractResponse(inner, "valueDecimal")
                            if (volume.isNotEmpty()) {

                                bundle.addEntry().setResource(
                                    qh.quantityQuestionnaire(
                                        EXPRESSED_MILK,
                                        "Milk Expressed",
                                        "Milk Expressed",
                                        volume, "mls"
                                    )
                                )
                                    .request.url = "Observation"
                                bundle.addEntry().setResource(
                                    qh.codingQuestionnaire(
                                        EFFECTIVE_EXPRESSION,
                                        "Effective Expression",
                                        effectiveExpression
                                    )
                                )
                                    .request.url = "Observation"
                                bundle.addEntry().setResource(
                                    qh.codingQuestionnaire(
                                        SUFFICIENT_EXPRESSION,
                                        "Sufficient Expression",
                                        expressedSufficient
                                    )
                                )
                                    .request.url = "Observation"
                            }
                        }
                        if (childChild == "Time-Expressed") {
                            dateTime = extractResponse(inner, "valueDateTime")

                            if (dateTime.isNotEmpty()) {

                                bundle.addEntry().setResource(
                                    qh.codingTimeQuestionnaire(
                                        EXPRESSION_TIME,
                                        "Time Expressed",
                                        dateTime
                                    )
                                )
                                    .request.url = "Observation"
                            }
                        }

                    }
                    if (dateTime.isNotEmpty()) {

                        val refineTime =
                            FormatHelper().dateTimeLessThanNow(dateTime)

                        if (refineTime) {
                            val value = retrieveUser(false)

                            bundle.addEntry()
                                .setResource(
                                    qh.codingQuestionnaire(
                                        COMPLETED_BY,
                                        value,
                                        value
                                    )
                                )
                                .request.url = "Observation"

                            val encounterId = generateUuid()
                            val subjectReference = Reference("Patient/$patientId")
                            title = EXPRESSIONS
                            saveResources(bundle, subjectReference, encounterId, title)
                            generateRiskAssessmentResource(bundle, subjectReference, encounterId)
                            customMessage.postValue(
                                MessageItem(
                                    success = true,
                                    message = "Milk Expression successful"
                                )
                            )
                        } else {
                            customMessage.postValue(
                                MessageItem(
                                    success = false,
                                    message = "Please Enter valid date"
                                )
                            )
                        }
                    } else {
                        Timber.e("Time of Expression Empty ")
                        customMessage.postValue(
                            MessageItem(
                                success = false,
                                message = "Please check required fields"
                            )
                        )
                    }

                } catch (e: Exception) {
                    Timber.d("Exception:::: ${e.printStackTrace()}")
                    customMessage.postValue(
                        MessageItem(
                            success = false,
                            message = "Experienced problems, please try again"
                        )
                    )
                    return@launch
                }
            }
        }

    }

    fun babyMonitoringCues(
        questionnaireResponse: QuestionnaireResponse,
        cues: FeedingCuesTips,
        patientId: String
    ) {
        viewModelScope.launch {
            val bundle =
                ResourceMapper.extract(
                    questionnaireResource,
                    questionnaireResponse
                )
            val context = FhirContext.forR4()
            val questionnaire =
                context.newJsonParser().encodeResourceToString(questionnaireResponse)

            CoroutineScope(Dispatchers.IO).launch {
                try {


                    /**
                     * Extract Observations, Patient Data
                     */
                    val qh = QuestionnaireHelper()

                    bundle.addEntry().setResource(
                        qh.codingQuestionnaire(
                            "Proper-Positioning",
                            "Proper Positioning",
                            cues.latch
                        )
                    )
                        .request.url = "Observation"
                    bundle.addEntry().setResource(
                        qh.codingQuestionnaire(
                            "Stimulated-Nipples",
                            "Stimulated Nipples",
                            cues.readiness
                        )
                    )
                        .request.url = "Observation"
                    bundle.addEntry().setResource(
                        qh.codingQuestionnaire(
                            "Early-Latching",
                            "Early Latching",
                            cues.steady
                        )
                    )
                        .request.url = "Observation"
                    bundle.addEntry().setResource(
                        qh.codingQuestionnaire(
                            "Manual-Drops",
                            "Manual Drops",
                            cues.audible
                        )
                    )
                        .request.url = "Observation"
                    bundle.addEntry().setResource(
                        qh.codingQuestionnaire(
                            "Baby-Awakens",
                            "Baby Awakens",
                            cues.chocking
                        )
                    )
                        .request.url = "Observation"


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

                    val encounterId = generateUuid()
                    val subjectReference = Reference("Patient/$patientId")
                    title = "Baby Assessment"
                    saveResources(bundle, subjectReference, encounterId, title)
                    generateRiskAssessmentResource(bundle, subjectReference, encounterId)
                    customMessage.postValue(
                        MessageItem(
                            success = true,
                            message = "Update successful"
                        )
                    )

                } catch (e: Exception) {
                    Timber.d("Exception:::: ${e.printStackTrace()}")
                    customMessage.postValue(
                        MessageItem(
                            success = false,
                            message = "Experienced problems, please try again"
                        )
                    )
                    return@launch
                }
            }
        }
    }

    fun babyMonitoring(
        questionnaireResponse: QuestionnaireResponse,
        patientId: String,
        careID: String,
        feedsList: MutableList<FeedItem>,
        totalV: Float,
        deficit: String
    ) {
        viewModelScope.launch {
            val bundle =
                ResourceMapper.extract(
                    questionnaireResource,
                    questionnaireResponse
                )
            val context = FhirContext.forR4()
            var assessDate = ""
            val questionnaire =
                context.newJsonParser().encodeResourceToString(questionnaireResponse)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val qh = QuestionnaireHelper()
                    val taken = totalV - deficit.toFloat()
                    bundle.addEntry().setResource(
                        qh.quantityQuestionnaire(
                            FEEDS_TAKEN,
                            "Total Feeds Taken",
                            "Total Feeds Taken",
                            taken.toString(), "mls"
                        )
                    )
                        .request.url = "Observation"

                    bundle.addEntry().setResource(
                        qh.quantityQuestionnaire(
                            FEEDS_DEFICIT,
                            "Feeds Deficit",
                            "Feeds Deficit",
                            deficit, "mls"
                        )
                    )
                        .request.url = "Observation"


                    feedsList.forEach {
                        when (it.type) {
                            "IV" -> {

                                bundle.addEntry().setResource(
                                    qh.quantityQuestionnaire(
                                        IV_VOLUME,
                                        "IV Volume",
                                        "IV Volume",
                                        it.volume.toString(),
                                        "mls"
                                    )
                                )
                                    .request.url = "Observation"
                            }
                            "DHM" -> {

                                bundle.addEntry().setResource(
                                    qh.quantityQuestionnaire(
                                        DHM_VOLUME,
                                        "DHM Volume",
                                        "DHM Volume",
                                        it.volume.toString(), "mls"
                                    )
                                )
                                    .request.url = "Observation"
                            }
                            "EBM" -> {

                                bundle.addEntry().setResource(
                                    qh.quantityQuestionnaire(
                                        EBM_VOLUME,
                                        "EBM Volume",
                                        "EBM Volume",
                                        it.volume.toString(), "mls"
                                    )
                                )
                                    .request.url = "Observation"
                            }
                            else -> {
                                println("Skipped Feeds Items")
                            }
                        }
                    }

                    val json = JSONObject(questionnaire)
                    val common = json.getJSONArray("item")
                    for (i in 0 until common.length()) {
                        val inner = common.getJSONObject(i)
                        when (inner.getString("linkId")) {
                            "Current-Weight" -> {
                                val code = extractResponse(inner, "valueDecimal")
                                if (code.isNotEmpty()) {
                                    bundle.addEntry().setResource(
                                        qh.quantityQuestionnaire(
                                            CURRENT_WEIGHT,
                                            "Current Weight",
                                            "Current Weight",
                                            code,
                                            "gm"

                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Assessment-Date" -> {
                                assessDate = extractResponse(inner, "valueDateTime")
                                if (assessDate.isNotEmpty()) {

                                    bundle.addEntry().setResource(
                                        qh.codingTimeQuestionnaire(
                                            ASSESSMENT_DATE,
                                            "Assessment Date",
                                            assessDate
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Vomit" -> {
                                val value = extractResponseCode(inner, "valueCoding")
                                if (value.isNotEmpty()) {

                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            VOMIT,
                                            "Vomit",
                                            value
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Stool" -> {
                                val value = extractResponseCode(inner, "valueCoding")
                                if (value.isNotEmpty()) {

                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            STOOL,
                                            "Stool",
                                            value
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Diaper-Changed" -> {
                                val value =
                                    extractResponse(inner, "valueDecimal")
                                if (value.isNotEmpty()) {

                                    bundle.addEntry().setResource(
                                        qh.quantityQuestionnaire(
                                            DIAPER_CHANGED,
                                            "Diaper Changed",
                                            "Diaper Changed",
                                            value, "pcs"
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Adjust-Prescription" -> {
                                val value = extractResponseCode(inner, "valueCoding")
                                if (value.isNotEmpty()) {

                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            ADJUST_PRESCRIPTION,
                                            "Adjust-Prescription",
                                            value
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Additional-Notes" -> {
                                val value =
                                    extractResponse(inner, "valueString")

                                if (value.isNotEmpty()) {

                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            REMARKS,
                                            "Additional Notes and Remarks", value
                                        )
                                    )
                                        .request.url = "Observation"
                                }

                            }
                            else -> {
                                println("Skip...")
                            }
                        }

                    }
                    val value = retrieveUser(false)

                    bundle.addEntry()
                        .setResource(
                            qh.codingQuestionnaire(
                                COMPLETED_BY,
                                value,
                                value
                            )
                        )
                        .request.url = "Observation"

                    val encounterId = generateUuid()
                    val subjectReference = Reference("Patient/$patientId")
                    /*  val encounterReference = Reference("Encounter/$encounterId")*/
                    val basedOnReference = Reference("$careID")
                    title = FEEDING_MONITORING
                    if (assessDate.isNotEmpty()) {
                        val lessThanNow = FormatHelper().dateTimeLessThanNow(assessDate)
                        if (lessThanNow) {


                            saveFeedingResources(
                                bundle,
                                subjectReference,
                                basedOnReference,
                                encounterId,
                                title
                            )
                            generateRiskAssessmentResource(bundle, subjectReference, encounterId)
                            customMessage.postValue(
                                MessageItem(
                                    success = true,
                                    message = "Feeding Successfully saved"
                                )
                            )
                        } else {
                            customMessage.postValue(
                                MessageItem(
                                    success = false,
                                    message = "Please check Feeding Time"
                                )
                            )
                        }
                    }

                } catch (e: Exception) {
                    Timber.d("Exception:::: ${e.printStackTrace()}")
                    customMessage.postValue(
                        MessageItem(
                            success = false,
                            message = "Experienced Problems, please try again"
                        )
                    )
                    return@launch
                }
            }
        }
    }


    fun addDhmStock(questionnaireResponse: QuestionnaireResponse) {
        viewModelScope.launch {
            val bundle =
                ResourceMapper.extract(
                    questionnaireResource,
                    questionnaireResponse
                )
            val context = FhirContext.forR4()
            val questionnaire =
                context.newJsonParser().encodeResourceToString(questionnaireResponse)

            CoroutineScope(Dispatchers.IO).launch {
                try {

                    val qh = QuestionnaireHelper()

                    val json = JSONObject(questionnaire)
                    val common = json.getJSONArray("item")
                    for (i in 0 until common.length()) {
                        val item = common.getJSONObject(i)
                        val parent = item.getJSONArray("item")
                        for (k in 0 until parent.length()) {
                            val inner = parent.getJSONObject(k)
                            val childChild = inner.getString("linkId")
                            Timber.e("Child $parent")
                            when (childChild) {

                                "Unpasteurized" -> {
                                    unpasteurized = extractResponse(inner, "valueInteger")
                                    if (unpasteurized.isNotEmpty()) {
                                        bundle.addEntry().setResource(
                                            qh.quantityQuestionnaire(
                                                "Unpasteurized",
                                                "Unpasteurized",
                                                "Unpasteurized",
                                                unpasteurized, "mls"
                                            )
                                        )
                                            .request.url = "Observation"
                                    }
                                }
                                "Pasteurized" -> {
                                    pasteurized = extractResponse(inner, "valueInteger")
                                    if (pasteurized.isNotEmpty()) {
                                        bundle.addEntry().setResource(
                                            qh.quantityQuestionnaire(
                                                "Pasteurized",
                                                "Pasteurized",
                                                "Pasteurized",
                                                pasteurized, "mls"
                                            )
                                        )
                                            .request.url = "Observation"
                                    }
                                }
                                else -> {
                                    println("Items skipped...")
                                }
                            }

                        }
                    }

                    val total =
                        Integer.parseInt(unpasteurized) + Integer.parseInt(pasteurized)
                    bundle.addEntry().setResource(
                        qh.quantityQuestionnaire(
                            "Total-Stock",
                            "Total Stock",
                            "Total Stock",
                            total.toString(), "mls"
                        )
                    )
                        .request.url = "Observation"


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

                    title = "DHM Stock"
                    val encounterId = generateUuid()
                    val encounterReference = Reference("Encounter/$encounterId")
//                    saveResources(bundle,)

                } catch (e: Exception) {
                    Timber.d("Exception:::: ${e.printStackTrace()}")

                    return@launch

                }

            }
        }
    }

    fun dispensingDetails(
        questionnaireResponse: QuestionnaireResponse,
        patientId: String,
        orderId: String,
        encounterId: String,
        dhmType: String
    ) {
        viewModelScope.launch {
            val bundle =
                ResourceMapper.extract(
                    questionnaireResource,
                    questionnaireResponse
                )
            val context = FhirContext.forR4()
            val questionnaire =
                context.newJsonParser().encodeResourceToString(questionnaireResponse)
            var type = ""
            var dispensed = "0"
            CoroutineScope(Dispatchers.IO).launch {
                try {

                    val qh = QuestionnaireHelper()

                    val json = JSONObject(questionnaire)
                    val common = json.getJSONArray("item")
                    for (i in 0 until common.length()) {
                        val inner = common.getJSONObject(i)
                        when (inner.getString("linkId")) {

                            "Volume" -> {
                                dispensed =
                                    extractResponse(inner, "valueDecimal")
                                if (dispensed.isNotEmpty()) {
                                    bundle.addEntry().setResource(
                                        qh.quantityQuestionnaire(
                                            VOLUME_DISPENSED,
                                            "Volume Dispensed",
                                            "Volume Dispensed",
                                            volume, "mls"
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "DHM-Type" -> {
                                type = extractResponseCode(inner, "valueCoding")

                                if (type.isNotEmpty()) {

                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            DHM_TYPE,
                                            "DHM Type",
                                            type
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            "Additional-Notes" -> {
                                val notes = extractResponse(inner, "valueString")
                                if (notes.isNotEmpty()) {
                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            REMARKS,
                                            "Additional Notes and Remarks",
                                            notes,
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            else -> {
                                println("Items skipped...")
                            }
                        }

                    }

                    if (type.isNotEmpty()) {
                        if (dhmType.trim() == type.trim()) {
                            val value = retrieveUser(false)

                            bundle.addEntry()
                                .setResource(
                                    qh.codingQuestionnaire(
                                        COMPLETED_BY,
                                        value,
                                        value
                                    )
                                )
                                .request.url = "Observation"
                            val subjectReference = Reference("Patient/$patientId")
                            val encounterReference = Reference("Encounter/$encounterId")
                            title = DHM_DISPENSING
                            updateNutrition(orderId)
                            updateDHMStock(dhmType, dispensed)

                            saveResources(bundle, subjectReference, encounterId, title)
                            customMessage.postValue(MessageItem(true, "Success"))
                        } else {
                            customMessage.postValue(MessageItem(false, "Please check the DHM Type"))
                        }
                    } else {
                        customMessage.postValue(MessageItem(false, "Please Select DHM Type"))
                    }

                } catch (e: Exception) {
                    Timber.d("Exception:::: ${e.printStackTrace()}")
                    customMessage.postValue(MessageItem(false, "Experienced problems saving data"))
                    return@launch

                }

            }
        }
    }

    private fun updateDHMStock(dhmType: String, dispensed: String) {
        /***
         * Function to Calculate the Stock Volumes
         */
    }

    private suspend fun updateNutrition(orderId: String) {
        try {
            val order = fhirEngine.load(NutritionOrder::class.java, orderId)
            val no = NutritionOrder()
            no.patient = order.patient
            no.status = NutritionOrder.NutritionOrderStatus.COMPLETED
            no.encounter = order.encounter
            no.id = order.logicalId
            saveResourceToDatabase(no)
        } catch (e: Exception) {
            Timber.e("Order Exception ${e.localizedMessage}")
        }


        /*    val order = NutritionOrder()
            order.id = orderId
            order.status = NutritionOrder.NutritionOrderStatus.COMPLETED
            order.patient = subjectReference
            order.encounter = encounterReference
            saveResourceToDatabase(order)*/
    }

    fun makeComplete() {
        customMessage.postValue(null)
    }

    fun updateStock(
        questionnaireResponse: QuestionnaireResponse,
        stockList: ArrayList<CodingObservation>,

        ) {
        viewModelScope.launch {
            val bundle =
                ResourceMapper.extract(
                    questionnaireResource,
                    questionnaireResponse
                )

            CoroutineScope(Dispatchers.IO).launch {
                try {


                    val qh = QuestionnaireHelper()

                    stockList.forEach {
                        bundle.addEntry()
                            .setResource(
                                qh.codingQuestionnaire(
                                    it.code,
                                    it.display,
                                    it.value

                                )
                            )
                            .request.url = "Observation"

                    }

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

                    emptyObservations(bundle, generateUuid(), DHM_STOCK)
                    customMessage.postValue(
                        MessageItem(
                            success = true,
                            message = "Update successful"
                        )
                    )

                } catch (e: Exception) {
                    customMessage.postValue(
                        MessageItem(
                            success = false,
                            message = "Experienced problems, please try again"
                        )
                    )

                }
            }

        }

    }


    fun updatePrescription(
        questionnaireResponse: QuestionnaireResponse,
        patientId: String,
        data: Prescription
    ) {
        viewModelScope.launch {
            val bundle =
                ResourceMapper.extract(
                    questionnaireResource,
                    questionnaireResponse
                )

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val subjectReference = Reference("Patient/$patientId")
                    updatePreviousPrescriptions(patientId)
                    val qh = QuestionnaireHelper()
                    val date = FormatHelper().getTodayDate()
                    if (data.data.isNotEmpty()) {
                        data.data.forEach {
                            if (it.coding) {
                                bundle.addEntry().setResource(
                                    qh.codingQuestionnaire(
                                        it.code,
                                        it.title,
                                        it.value,
                                    )
                                )
                                    .request.url = "Observation"
                            } else {
                                bundle.addEntry().setResource(
                                    qh.quantityQuestionnaire(
                                        it.code,
                                        it.title,
                                        it.title,
                                        it.value, "mls"
                                    )
                                )
                                    .request.url = "Observation"
                            }
                            /*    if (it.volume != null) {
                                    bundle.addEntry().setResource(
                                        qh.quantityQuestionnaire(
                                            it.resourceId.toString(),
                                            it.resourceId.toString(),
                                            it.resourceId.toString(),
                                            it.volume.toString(), "mls"
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                                if (it.frequency != null) {
                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            it.id.toString(),
                                            it.id.toString(),
                                            it.frequency.toString(),
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                                if (it.route != null) {
                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            it.type.toString(),
                                            it.type.toString(),
                                            it.route.toString(),
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                                if (it.specific != null) {
                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            it.logicalId.toString(),
                                            it.logicalId.toString(),
                                            it.specific.toString(),
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                                if (it.frequencyAlt != null) {
                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            it.idAlt.toString(),
                                            it.idAlt.toString(),
                                            it.frequencyAlt.toString(),
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                                if (it.routeAlt != null) {
                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            it.typeAlt.toString(),
                                            it.typeAlt.toString(),
                                            it.routeAlt.toString(),
                                        )
                                    )
                                        .request.url = "Observation"
                                }*/

                        }
                    }
                    bundle.addEntry().setResource(
                        qh.quantityQuestionnaire(
                            CURRENT_WEIGHT,
                            "Current Weight",
                            "Current Weight",
                            data.currentWeight, "gm"
                        )
                    )
                        .request.url = "Observation"
                    bundle.addEntry().setResource(
                        qh.quantityQuestionnaire(
                            TOTAL_FEEDS,
                            "Total Feeds",
                            "Total Feeds",
                            data.totalFeeds, "mls"
                        )
                    )
                        .request.url = "Observation"

                    val value = retrieveUser(false)

                    bundle.addEntry()
                        .setResource(
                            qh.codingQuestionnaire(
                                COMPLETED_BY,
                                value,
                                value
                            )
                        )
                        .request.url = "Observation"

                    bundle.addEntry().setResource(
                        qh.codingQuestionnaire(
                            PRESCRIPTION_DATE,
                            "Prescription Date",
                            date
                        )
                    )
                        .request.url = "Observation"
                    bundle.addEntry().setResource(
                        qh.codingQuestionnaire(
                            FEEDING_SUPPLEMENTS,
                            "Supplements Considered", data.supplements
                        )
                    )
                        .request.url = "Observation"
                    bundle.addEntry().setResource(
                        qh.codingQuestionnaire(
                            FEEDING_FREQUENCY,
                            "Feed Frequency", data.feedFrequency
                        )
                    )
                        .request.url = "Observation"
                    bundle.addEntry().setResource(
                        qh.codingQuestionnaire(
                            ADDITIONAL_FEEDS,
                            "Feeding Supplements", data.additional
                        )
                    )
                        .request.url = "Observation"
                    val encounterId = generateUuid()

                    val encounterReference = Reference("Encounter/$encounterId")
                    title = PRESCRIPTION


                    if (data.data.isNotEmpty()) {
                        val dhm = data.data.find { it.code == DHM_VOLUME }?.value

                        if (dhm != null) {
                            val no = NutritionOrder()
                            no.id = generateUuid()
                            no.patient = subjectReference
                            no.encounter = encounterReference
                            no.status = NutritionOrder.NutritionOrderStatus.ACTIVE
                            no.dateTime = Date()
                            no.intent = NutritionOrder.NutritiionOrderIntent.ORDER
                            saveResourceToDatabase(no)
                        }
                    }
                    val care = CarePlan()
                    care.encounter = encounterReference
                    care.subject = subjectReference
                    care.status = CarePlan.CarePlanStatus.ACTIVE
                    care.title = title
                    care.intent = CarePlan.CarePlanIntent.ORDER
                    care.created = Date()
                    saveResourceToDatabase(care)
                    saveResources(bundle, subjectReference, encounterId, title)
                    customMessage.postValue(
                        MessageItem(
                            success = true, message = "Update Successful"
                        )
                    )

                } catch (e: Exception) {
                    Timber.e("Ex Data ${e.localizedMessage}")
                    customMessage.postValue(
                        MessageItem(
                            success = false, message = "Experienced problems, please try again "
                        )
                    )

                }
            }

        }
    }

    fun updateExpression(questionnaireResponse: QuestionnaireResponse, patientId: String) {
        viewModelScope.launch {
            val bundle =
                ResourceMapper.extract(
                    questionnaireResource,
                    questionnaireResponse
                )

            try {
                if (hasMissingRequired(bundle)) {
                    customMessage.postValue(
                        MessageItem(
                            success = false,
                            message = "Check required fields"
                        )
                    )
                    return@launch
                }

                val subjectReference = Reference("Patient/$patientId")
                val encounterId = generateUuid()
                title = "Expression-Assessment"
                autoSaveResources(bundle, subjectReference, encounterId, title)
                customMessage.postValue(
                    MessageItem(
                        success = true, message = "Update Successful"
                    )
                )
            } catch (e: Exception) {
                customMessage.postValue(
                    MessageItem(
                        success = false, message = "Experienced problems saving data"
                    )
                )
            }
        }
    }

    private fun hasMissingRequired(bundle: Bundle): Boolean {
        bundle.entry.forEach {
            when (val resource = it.resource) {
                is Observation -> {
 
                    if (resource.hasValue() && !resource.valueStringType.hasValue()) {

                        return true
                    }
                }
            }
        }
        return false
    }

    private suspend fun autoSaveResources(
        bundle: Bundle,
        subjectReference: Reference,
        encounterId: String,
        reason: String,
    ) {

        val encounterReference = Reference("Encounter/$encounterId")
        bundle.entry.forEach {
            when (val resource = it.resource) {
                is Observation -> {
                    if (resource.hasCode()) {
                        resource.id = generateUuid()
                        resource.subject = subjectReference
                        resource.encounter = encounterReference
                        resource.valueStringType.value = resource.code.text
                        resource.issued = Date()
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
                    resource.reasonCodeFirstRep.text = reason
                    resource.reasonCodeFirstRep.codingFirstRep.code = reason
                    resource.status = Encounter.EncounterStatus.INPROGRESS
                    resource.period.start = Date()
                    saveResourceToDatabase(resource)
                }

            }
        }
    }

    fun liveDispensing(
        questionnaireResponse: QuestionnaireResponse,
        context: Context,
        apiService: RestManager,
        order: ItemOrder,
        dhmType: String
    ) {
        viewModelScope.launch {
            val bundle =
                ResourceMapper.extract(
                    questionnaireResource,
                    questionnaireResponse
                )
            val fh = FhirContext.forR4()
            val questionnaire =
                fh.newJsonParser().encodeResourceToString(questionnaireResponse)
            var type = ""
            var dispensed = "0"
            var remarks = ""
            var typeDHM = ""
            var volumeD = ""
            CoroutineScope(Dispatchers.IO).launch {
                try {

                    val qh = QuestionnaireHelper()

                    val json = JSONObject(questionnaire)
                    val common = json.getJSONArray("item")
                    for (i in 0 until common.length()) {
                        val inner = common.getJSONObject(i)
                        when (inner.getString("linkId")) {

                            "Volume" -> {
                                dispensed =
                                    extractResponse(inner, "valueDecimal")
                                if (dispensed.isNotEmpty()) {
                                    volumeD = dispensed
                                }
                            }
                            "DHM-Type" -> {
                                type = extractResponseCode(inner, "valueCoding")

                                if (type.isNotEmpty()) {
                                    typeDHM = type
                                }
                            }
                            "Additional-Notes" -> {
                                val notes = extractResponse(inner, "valueString")
                                if (notes.isNotEmpty()) {
                                    remarks = notes
                                }
                            }
                            else -> {
                                println("Items skipped...")
                            }
                        }

                    }
                    if (type.isNotEmpty()) {
                        if (dhmType.trim() == type.trim()) {
                            val stock = DispenseData(
                                dhmType = typeDHM,
                                dhmVolume = volumeD,
                                remarks = remarks,
                                orderId = order.orderId,
                                userId = retrieveUser(false)
                            )
                            val k = Gson()
                            Timber.e("Payload ${k.toJson(stock)}")
                            apiService.dispenseStock(context, stock) {
                                if (it != null) {

                                    customMessage.postValue(MessageItem(true, "Success"))
                                } else {

                                    customMessage.postValue(
                                        MessageItem(
                                            false,
                                            "Experienced problems saving data"
                                        )
                                    )
                                }
                            }
                        } else {
                            customMessage.postValue(MessageItem(false, "Please check the DHM Type"))
                        }
                    } else {
                        customMessage.postValue(MessageItem(false, "Please Select DHM Type"))
                    }

                } catch (e: Exception) {
                    Timber.d("Exception:::: ${e.printStackTrace()}")
                    customMessage.postValue(MessageItem(false, "Experienced problems saving data"))
                    return@launch

                }

            }
        }
    }


}





