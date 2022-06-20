package com.intellisoft.nndak.viewmodels

import android.app.Application
import androidx.lifecycle.*
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.logicalId
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.search
import com.google.gson.Gson
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.data.User
import com.intellisoft.nndak.helper_class.*
import com.intellisoft.nndak.logic.Logics
import com.intellisoft.nndak.logic.Logics.Companion.BABY_ASSESSMENT
import com.intellisoft.nndak.logic.Logics.Companion.DHM_STOCK
import com.intellisoft.nndak.logic.Logics.Companion.FEEDING_MONITORING
import com.intellisoft.nndak.logic.Logics.Companion.PRESCRIPTION
import com.intellisoft.nndak.models.*
import com.intellisoft.nndak.screens.dashboard.RegistrationFragment.Companion.QUESTIONNAIRE_FILE_PATH_KEY
import com.intellisoft.nndak.utils.Constants
import com.intellisoft.nndak.utils.Constants.SYNC_VALUE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.lang3.time.DateUtils.isSameDay
import org.hl7.fhir.r4.model.*
import org.hl7.fhir.r4.model.codesystems.EncounterStatus
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
    val customMessage = MutableLiveData<MessageItem>()
    val apgarScore = MutableLiveData<ApGar>()
    var isSafe = false
    lateinit var title: String
    lateinit var supplements: String
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
                        isResourcesSaved.postValue(false)
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
                        val item = common.getJSONObject(i)
                        val parent = item.getJSONArray("item")
                        for (k in 0 until parent.length()) {
                            val inner = parent.getJSONObject(k)
                            val childChild = inner.getString("linkId")
                            Timber.e("Child $inner")
                            when (childChild) {
                                "Assessment-Date" -> {
                                    assessDate = extractResponse(inner, "valueDateTime")
                                    if (assessDate.isNotEmpty()) {

                                        bundle.addEntry().setResource(
                                            qh.codingQuestionnaire(
                                                "50786-3",
                                                "Assessment Date",
                                                assessDate
                                            )
                                        )
                                            .request.url = "Observation"
                                    }
                                }
                                "Current-Weight" -> {
                                    val code = extractResponseQuantity(inner, "valueQuantity")
                                    if (code.isNotEmpty()) {
                                        bundle.addEntry().setResource(
                                            qh.quantityQuestionnaire(
                                                "3141-9",
                                                "Current Weight",
                                                "Current Weight",
                                                code,
                                                "g"

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
                                                "71195-2",
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
                                            "Mother-Location",
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
                                                "45735-8",
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
                                                "45736-6",
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
                                                "45755-8",
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
                                                "Mother-Well",
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
                                                "Breast-Problem",
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
                                                "457736-6",
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
                                                "46556-7",
                                                "Baby Fed within 1 Hour",
                                                feed
                                            )
                                        )
                                            .request.url = "Observation"
                                        if (feed == "Yes") {

                                            bundle.addEntry().setResource(
                                                qh.codingQuestionnaire(
                                                    "Fed-After",
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
                                                "46557-5",
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
                                                "Fed-After",
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
                                                "Additional-Notes",
                                                "Additional Notes and Remarks",
                                                type,
                                            )
                                        )
                                            .request.url = "Observation"
                                    }
                                }
                            }
                        }

                    }

                    /**
                     * Handle date validation assessDate
                     */
                    Timber.e("Assessment Date $assessDate")
                    val validDate = FormatHelper().dateLessThanToday(assessDate.substring(0, 10))
                    if (validDate) {
                        val encounterId = generateUuid()
                        val subjectReference = Reference("Patient/$patientId")
                        title = BABY_ASSESSMENT
                        saveResources(bundle, subjectReference, encounterId, title)
                        isResourcesSaved.postValue(true)
                    } else {
                        isResourcesSaved.postValue(false)
                        return@launch
                    }
                } catch (e: Exception) {
                    Timber.d("Exception:::: ${e.printStackTrace()}")
                    isResourcesSaved.postValue(false)
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
                        isResourcesSaved.postValue(false)
                        return@launch
                    }

                    /**
                     * Extract Observations, Patient Data
                     */
                    val baby = Patient()
                    baby.active = true
                    baby.id = patientId
                    baby.addressFirstRep.postalCode = SYNC_VALUE

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
                    for (i in 0 until common.length()) {
                        val item = common.getJSONObject(i)
                        val parent = item.getJSONArray("item")
                        for (k in 0 until parent.length()) {
                            val inner = parent.getJSONObject(k)
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
                                            isResourcesSaved.postValue(false)
                                            return@launch
                                        }
                                    }
                                }
                                "Parity" -> {
                                    val parity = extractResponse(inner, "valueInteger")
                                    if (parity.isNotEmpty()) {

                                        bundle.addEntry().setResource(
                                            qh.codingQuestionnaire(
                                                "45394-4",
                                                "Parity",
                                                parity
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
                                        isResourcesSaved.postValue(false)
                                        return@launch
                                    }
                                }
                                "PMTCT" -> {
                                    val pmtct = extractResponseCode(inner, "valueCoding")
                                    if (pmtct.isNotEmpty()) {
                                        bundle.addEntry().setResource(
                                            qh.codingQuestionnaire(
                                                "55277-8",
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
                                                "64708-1",
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
                                                "55277-8",
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
                                            qh.codingQuestionnaire(
                                                "93857-1",
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
                                            "72149-8",
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
                                                "73762-7",
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
                                            "14904-7",
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
                                "Baby-State" -> {
                                    val state = extractResponseCode(inner, "valueCoding")
                                    if (state.isNotEmpty()) {
                                        bundle.addEntry().setResource(
                                            qh.codingQuestionnaire(
                                                "Baby-State",
                                                "Baby's State",
                                                state
                                            )
                                        )
                                            .request.url = "Observation"
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
                                    val bType = extractResponseQuantity(inner, "valueQuantity")
                                    if (bType.isNotEmpty()) {
                                        bundle.addEntry().setResource(
                                            qh.quantityQuestionnaire(
                                                "8339-4",
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
                                            qh.codingQuestionnaire(
                                                "11885-1",
                                                "Gestation",
                                                bType
                                            )
                                        )
                                            .request.url = "Observation"
                                    }
                                }
                                "Apgar-Score" -> {
                                    val bType = extractResponse(inner, "valueDecimal")
                                    if (bType.isNotEmpty()) {
                                        bundle.addEntry().setResource(
                                            qh.codingQuestionnaire(
                                                "9273-4",
                                                "Apgar Score",
                                                bType
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
                                                "16491-3",
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
                                            qh.codingQuestionnaire(
                                                "33172-8",
                                                "Head Circumference",
                                                bType
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
                                                "52508-9",
                                                "Interventions",
                                                bType
                                            )
                                        )
                                            .request.url = "Observation"
                                    }
                                }
                                "Admission-Weight" -> {
                                    val bType = extractResponseQuantity(inner, "valueQuantity")
                                    if (bType.isNotEmpty()) {
                                        if (bType.isNotEmpty()) {
                                            bundle.addEntry().setResource(
                                                qh.quantityQuestionnaire(
                                                    "29463-7",
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
                                            qh.codingQuestionnaire(
                                                "52455-3",
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
                                                "Additional-Notes",
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
                    }

                    //

                    /**
                     * Check for Valid Date of birth:: should be less than today
                     */
                    val validDateOfBirth = FormatHelper().dateLessThanToday(birthDate)
                    if (validDateOfBirth) {
                        Timber.e("Valid Date of Birth $validDateOfBirth")
                        val valid =
                            FormatHelper().isSameDay(birthDate, deliveryDate.substring(0, 10))

                        Timber.e("Both Date of Birth and Delivery Same $valid")

                        val validAdmission =
                            FormatHelper().dateLessThanToday(admissionDate.substring(0, 10))
                        if (valid && validAdmission) {


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
                            title = "Client Registration"
                            saveResourceToDatabase(resource = baby)
                            saveResourceToDatabase(resource = mother)
                            saveResources(bundle, subjectReference, encounterId, title)
                            customMessage.postValue(MessageItem(true, "Success"))
                        } else {
                            customMessage.postValue(
                                MessageItem(
                                    false,
                                    "Please Enter Valid Delivery Date"
                                )
                            )
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
                    customMessage.postValue(MessageItem(false, "Please Check All Inputs"))
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
                        isResourcesSaved.value = false
                        return@launch
                    }


                    /**
                     * Extract Observations, Patient Data
                     */

                    val subjectReference = Reference("Patient/$patientId")
                    updatePreviousPrescriptions(patientId)
                    val qh = QuestionnaireHelper()

                    val date = FormatHelper().getTodayDate()
                    val json = JSONObject(questionnaire)
                    val common = json.getJSONArray("item")
                    for (i in 0 until common.length()) {
                        val item = common.getJSONObject(i)
                        val parent = item.getJSONArray("item")

                        for (k in 0 until parent.length()) {
                            val inner = parent.getJSONObject(k)
                            val childChild = inner.getString("linkId")
                            Timber.e("Child $inner")
                            when (childChild) {
                                /*  "Day-Of-Life" -> {
                                      val value =
                                          extractResponse(inner, "valueInteger")
                                      if (value.isNotEmpty()) {
                                          bundle.addEntry().setResource(
                                              qh.codingQuestionnaire(
                                                  "Day-Of-Life",
                                                  "Day Of Life",
                                                  value,
                                              )
                                          )
                                              .request.url = "Observation"

                                      }
                                  }*/
                                "Current-Weight" -> {
                                    val value =
                                        extractResponseQuantity(inner, "valueQuantity")
                                    if (value.isNotEmpty()) {
                                        bundle.addEntry().setResource(
                                            qh.quantityQuestionnaire(
                                                "3141-9",
                                                "Current Weight",
                                                "Current Weight",
                                                value, "gm"
                                            )
                                        )
                                            .request.url = "Observation"

                                        bundle.addEntry().setResource(
                                            qh.codingQuestionnaire(
                                                "Prescription-Date",
                                                "Prescription Date",
                                                date
                                            )
                                        )
                                            .request.url = "Observation"
                                    }
                                }
                                "Total-Feeds" -> {
                                    val value =
                                        extractResponseQuantity(inner, "valueQuantity")
                                    if (value.isNotEmpty()) {

                                        bundle.addEntry().setResource(
                                            qh.quantityQuestionnaire(
                                                "Total-Feeds",
                                                "Total Feeds",
                                                "Total Feeds",
                                                value, "mls"
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
                                                extractResponseQuantity(inner, "valueQuantity")

                                            if (value.isNotEmpty()) {
                                                bundle.addEntry().setResource(
                                                    qh.quantityQuestionnaire(
                                                        "Breast-Milk",
                                                        "Breast Milk", "Breast Milk",
                                                        value, "mls"
                                                    )
                                                )
                                                    .request.url = "Observation"
                                            }
                                        }
                                    }
                                }
                                "Breast-Feed-Frequency" -> {
                                    if (feeds.isNotEmpty()) {
                                        if (feeds.contains("Breast Feed")) {
                                            val value =
                                                extractResponseCode(inner, "valueCoding")

                                            if (value.isNotEmpty()) {
                                                bundle.addEntry().setResource(
                                                    qh.codingQuestionnaire(
                                                        "Breast-Feed-Frequency",
                                                        "Breast Feed Frequency", value,
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
                                                        "EBM-Feeding-Route",
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
                                                extractResponseQuantity(inner, "valueQuantity")

                                            if (value.isNotEmpty()) {
                                                bundle.addEntry().setResource(
                                                    qh.quantityQuestionnaire(
                                                        "EBM-Volume",
                                                        "EBM Volume", "EBM Volume", value, "mls"
                                                    )
                                                )
                                                    .request.url = "Observation"
                                            }
                                        }
                                    }
                                }
                                "EBM-Feeding-Frequency" -> {
                                    if (feeds.isNotEmpty()) {
                                        if (feeds.contains("EBM")) {

                                            val value =
                                                extractResponseCode(inner, "valueCoding")

                                            if (value.isNotEmpty()) {
                                                bundle.addEntry().setResource(
                                                    qh.codingQuestionnaire(
                                                        "EBM-Feeding-Frequency",
                                                        "EBM Feeding Frequency", value
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
                                                        "Formula-Type",
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
                                                        "Formula-Route",
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
                                                extractResponseQuantity(inner, "valueQuantity")

                                            if (value.isNotEmpty()) {
                                                bundle.addEntry().setResource(
                                                    qh.quantityQuestionnaire(
                                                        "Formula-Volume",
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
                                "Formula-Frequency" -> {
                                    if (feeds.isNotEmpty()) {
                                        if (feeds.contains("Formula")) {
                                            val value =
                                                extractResponseCode(inner, "valueCoding")

                                            if (value.isNotEmpty()) {
                                                bundle.addEntry().setResource(
                                                    qh.codingQuestionnaire(
                                                        "Formula-Frequency",
                                                        "Formula Frequency", value
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
                                                        "DHM-Type",
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
                                                        "DHM-Route",
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
                                                extractResponseQuantity(inner, "valueQuantity")

                                            if (value.isNotEmpty()) {
                                                bundle.addEntry().setResource(
                                                    qh.quantityQuestionnaire(
                                                        "DHM-Volume",
                                                        "DHM Volume", "DHM Volume", value, "mls"
                                                    )
                                                )
                                                    .request.url = "Observation"
                                            }
                                        }
                                    }
                                }
                                "DHM-Frequency" -> {
                                    if (feeds.isNotEmpty()) {
                                        if (feeds.contains("DHM")) {
                                            val value =
                                                extractResponseCode(inner, "valueCoding")

                                            if (value.isNotEmpty()) {
                                                bundle.addEntry().setResource(
                                                    qh.codingQuestionnaire(
                                                        "DHM-Frequency",
                                                        "DHM Frequency", value,
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
                                            val value =
                                                extractResponseCode(inner, "valueCoding")

                                            if (value.isNotEmpty()) {
                                                val consent = if (value == "Yes") {
                                                    "Signed"
                                                } else {
                                                    "Not Signed"
                                                }
                                                bundle.addEntry().setResource(
                                                    qh.codingQuestionnaire(
                                                        "Consent-Given",
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
                                            val value =
                                                extractResponse(inner, "valueDate")

                                            if (value.isNotEmpty()) {
                                                bundle.addEntry().setResource(
                                                    qh.codingQuestionnaire(
                                                        "Consent-Date",
                                                        "Consent Date", value,
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
                                                        "DHM-Reason",
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
                                                        "IV-Fluid-Route",
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
                                                extractResponseQuantity(inner, "valueQuantity")

                                            if (value.isNotEmpty()) {
                                                bundle.addEntry().setResource(
                                                    qh.quantityQuestionnaire(
                                                        "IV-Fluid-Volume",
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
                                "IV-Fluid-Frequency" -> {
                                    if (feeds.isNotEmpty()) {
                                        if (feeds.contains("IV Fluid and Additives")) {
                                            val value =
                                                extractResponseCode(inner, "valueCoding")

                                            if (value.isNotEmpty()) {
                                                bundle.addEntry().setResource(
                                                    qh.codingQuestionnaire(
                                                        "IV-Fluid-Frequency",
                                                        "IV-Fluid-Frequency", value
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
                                                "Additional-Feeds",
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
                                                    "Supplements-Feeding",
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
                                                "Additional-Notes",
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
                    val encounterId = generateUuid()
                    title = PRESCRIPTION

                    val encounterReference = Reference("Encounter/$encounterId")
                    if (feeds.isNotEmpty()) {
                        if (feeds.contains("DHM")) {
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
                    saveResourceToDatabase(care)

                    saveResources(bundle, subjectReference, encounterId, title)
                    isResourcesSaved.postValue(true)

                } catch (e: Exception) {
                    Timber.d("Exception:::: ${e.printStackTrace()}")
                    isResourcesSaved.postValue(false)
                    return@launch

                }
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
            .take(Constants.MAX_RESOURCE_COUNT)
            .map {
                PatientDetailsViewModel.createCarePlanItem(
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
                    if (resource.hasValueStringType() && !resource.valueStringType.hasPrimitiveValue()) {
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
    private suspend fun generateAssessmentResource(
        bundle: Bundle,
        subjectReference: Reference,
        encounterId: String,
        total: Int
    ) {
        val riskProbability = dangerScore(total)
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

    private fun dangerScore(
        total: Int
    ): RiskProbability? {
        if (total < 0) return RiskProbability.HIGH else if (total < 1) return RiskProbability.MODERATE else if (total > 0) return RiskProbability.LOW
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
                e.printStackTrace()
            }

            val subjectReference = Reference("Patient/$patientId")

            val encounterId = generateUuid()
            title = "Breast Feeding"
            saveResources(bundle, subjectReference, encounterId, title)
            isResourcesSaved.value = true
        }
    }

    fun feedingCues(
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
            val qh = QuestionnaireHelper()
            val context = FhirContext.forR4()
            val questionnaire =
                context.newJsonParser().encodeResourceToString(questionnaireResponse)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val json = JSONObject(questionnaire)
                    val common = json.getJSONArray("item")
                    for (i in 0 until common.length()) {
                        val item = common.getJSONObject(i)
                        val parent = item.getJSONArray("item")
                        for (k in 0 until parent.length()) {
                            val inner = parent.getJSONObject(k)
                            val childChild = inner.getString("linkId")
                            Timber.e("Child $parent")
                            if (childChild == "Time-Expressed") {
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Feeding-Readiness",
                                            cues.readiness, cues.readiness,

                                            )
                                    )
                                    .request.url = "Observation"

                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Latch",
                                            cues.latch, cues.latch,

                                            )
                                    )
                                    .request.url = "Observation"
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Steady Suck",
                                            cues.steady, cues.steady,

                                            )
                                    )
                                    .request.url = "Observation"
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Audible-Swallow",
                                            cues.audible, cues.audible,

                                            )
                                    )
                                    .request.url = "Observation"

                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Chocking",
                                            cues.chocking, cues.chocking,

                                            )
                                    )
                                    .request.url = "Observation"

                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Breast-Softening",
                                            cues.softening.toString(),
                                            cues.softening.toString(),

                                            )
                                    )
                                    .request.url = "Observation"

                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "10-Minutes-Side",
                                            cues.tenSide.toString(),
                                            cues.tenSide.toString(),

                                            )
                                    )
                                    .request.url = "Observation"

                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "2-3-Hours",
                                            cues.threeHours.toString(),
                                            cues.threeHours.toString(),

                                            )
                                    )
                                    .request.url = "Observation"

                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "6-8-Wet-Diapers",
                                            cues.sixDiapers.toString(),
                                            cues.sixDiapers.toString(),

                                            )
                                    )
                                    .request.url = "Observation"

                                bundle.addEntry().setResource(
                                    qh.codingQuestionnaire(
                                        "Mother-Contraindicated",
                                        "Mother Contraindicated",
                                        cues.contra.toString()
                                    )
                                )
                                    .request.url = "Observation"

                            }
                        }
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
                    isResourcesSaved.postValue(true)
                } catch (e: Exception) {
                    e.printStackTrace()
                    isResourcesSaved.postValue(false)
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
                        isResourcesSaved.postValue(false)
                        return@launch
                    }

                    /**
                     * Extract Observations, Patient Data
                     */
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
                            if (childChild == "Milk-Expressed") {
                                val volume = extractResponseQuantity(inner, "valueQuantity")
                                if (volume.isNotEmpty()) {
                                    Timber.e("Time: $volume")
                                    bundle.addEntry().setResource(
                                        qh.quantityQuestionnaire(
                                            "226790003",
                                            "Milk Expressed",
                                            "Milk Expressed",
                                            volume, "mls"
                                        )
                                    )
                                        .request.url = "Observation"
                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            "Effective-Expression",
                                            "Effective Expression",
                                            effectiveExpression
                                        )
                                    )
                                        .request.url = "Observation"
                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            "Sufficient-Expression",
                                            "Sufficient Expression",
                                            expressedSufficient
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                            if (childChild == "Time-Expressed") {
                                val volume = extractResponse(inner, "valueDateTime")
                                Timber.e("Time: $volume")
                                if (volume.isNotEmpty()) {

                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            "Time-Expressed",
                                            "Time Expressed",
                                            volume
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }
                        }

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

                    val encounterId = generateUuid()
                    val subjectReference = Reference("Patient/$patientId")
                    title = "Milk Expression"
                    saveResources(bundle, subjectReference, encounterId, title)
                    generateRiskAssessmentResource(bundle, subjectReference, encounterId)
                    isResourcesSaved.postValue(true)

                } catch (e: Exception) {
                    Timber.d("Exception:::: ${e.printStackTrace()}")
                    isResourcesSaved.postValue(false)
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

                    val json = JSONObject(questionnaire)
                    val common = json.getJSONArray("item")
                    for (i in 0 until common.length()) {
                        val item = common.getJSONObject(i)
                        val parent = item.getJSONArray("item")
                        for (k in 0 until parent.length()) {
                            val inner = parent.getJSONObject(k)
                            val childChild = inner.getString("linkId")
                            Timber.e("Child $parent")
                            if (childChild == "Assessment-Date") {
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


                            }
                        }

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

                    val encounterId = generateUuid()
                    val subjectReference = Reference("Patient/$patientId")
                    title = "Baby Assessment"
                    saveResources(bundle, subjectReference, encounterId, title)
                    generateRiskAssessmentResource(bundle, subjectReference, encounterId)
                    isResourcesSaved.postValue(true)

                } catch (e: Exception) {
                    Timber.d("Exception:::: ${e.printStackTrace()}")
                    isResourcesSaved.postValue(false)
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
            val questionnaire =
                context.newJsonParser().encodeResourceToString(questionnaireResponse)

            CoroutineScope(Dispatchers.IO).launch {
                try {

                    /**
                     * Extract Observations, Patient Data
                     */
                    val qh = QuestionnaireHelper()

                    val taken = totalV - deficit.toFloat()

                    bundle.addEntry().setResource(
                        qh.quantityQuestionnaire(
                            "Total-Taken",
                            "Total Feeds Taken",
                            "Total Feeds Taken",
                            taken.toString(), "mls"
                        )
                    )
                        .request.url = "Observation"

                    bundle.addEntry().setResource(
                        qh.quantityQuestionnaire(
                            "Feeds-Deficit",
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
                                        "IV-Volume",
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
                                        "DHM-Volume",
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
                                        "EBM-Volume",
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
                        val item = common.getJSONObject(i)
                        val parent = item.getJSONArray("item")
                        for (k in 0 until parent.length()) {
                            val inner = parent.getJSONObject(k)
                            val childChild = inner.getString("linkId")
                            Timber.e("Child $parent")
                            when (childChild) {
                                "Assessment-Date" -> {
                                    val value = extractResponse(inner, "valueDateTime")
                                    if (value.isNotEmpty()) {

                                        bundle.addEntry().setResource(
                                            qh.codingQuestionnaire(
                                                "50786-3",
                                                "Assessment Date",
                                                value
                                            )
                                        )
                                            .request.url = "Observation"
                                    }
                                }
                                "Feeding-IV" -> {
                                    val value =
                                        extractResponseQuantity(inner, "valueQuantity")
                                    if (value.isNotEmpty()) {

                                        bundle.addEntry().setResource(
                                            qh.quantityQuestionnaire(
                                                "IV-Volume",
                                                "IV Volume",
                                                "IV Volume",
                                                value,
                                                "ml"
                                            )
                                        )
                                            .request.url = "Observation"
                                    }
                                }
                                "DHM-Volume" -> {
                                    val value =
                                        extractResponseQuantity(inner, "valueQuantity")
                                    if (value.isNotEmpty()) {

                                        bundle.addEntry().setResource(
                                            qh.quantityQuestionnaire(
                                                "DHM-Volume",
                                                "DHM Volume",
                                                "DHM Volume",
                                                value, "ml"
                                            )
                                        )
                                            .request.url = "Observation"
                                    }
                                }
                                "EBM-Volume" -> {
                                    val value =
                                        extractResponseQuantity(inner, "valueQuantity")
                                    if (value.isNotEmpty()) {

                                        bundle.addEntry().setResource(
                                            qh.quantityQuestionnaire(
                                                "EBM-Volume",
                                                "EBM Volume",
                                                "EBM Volume",
                                                value, "ml"
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
                                                "Vomit",
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
                                                "Stool",
                                                "Stool",
                                                value
                                            )
                                        )
                                            .request.url = "Observation"
                                    }
                                }
                                "Diaper-Changed" -> {
                                    val value =
                                        extractResponseQuantity(inner, "valueQuantity")
                                    if (value.isNotEmpty()) {

                                        bundle.addEntry().setResource(
                                            qh.quantityQuestionnaire(
                                                "Diaper-Changed",
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
                                                "Adjust-Prescription",
                                                "Adjust-Prescription",
                                                value
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

                    val encounterId = generateUuid()
                    val subjectReference = Reference("Patient/$patientId")
                    val encounterReference = Reference("Encounter/$encounterId")
                    val basedOnReference = Reference("CarePlan/$careID")
                    title = FEEDING_MONITORING

                    val care = CarePlan()
                    care.encounter = encounterReference
                    care.subject = subjectReference
                    care.status = CarePlan.CarePlanStatus.COMPLETED
                    care.title = title
                    care.intent = CarePlan.CarePlanIntent.ORDER
                    care.addPartOf(basedOnReference)
                    saveResourceToDatabase(care)

                    saveResources(bundle, subjectReference, encounterId, title)
                    generateRiskAssessmentResource(bundle, subjectReference, encounterId)
                    isResourcesSaved.postValue(true)

                } catch (e: Exception) {
                    Timber.d("Exception:::: ${e.printStackTrace()}")
                    isResourcesSaved.postValue(false)
                    return@launch
                }
            }
        }
    }

    fun addDhmRecipient(questionnaireResponse: QuestionnaireResponse) {
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
                        isResourcesSaved.postValue(false)
                        return@launch
                    }

                    /**
                     * Retrieve Mother Using the Provided ID
                     */


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
                                "IP-Number" -> {
                                    patientIp = extractResponse(inner, "valueString")
                                    if (patientIp.isNotEmpty()) {

                                        bundle.addEntry().setResource(
                                            qh.codingQuestionnaire(
                                                "IP-Number",
                                                "IP Number",
                                                patientIp,
                                            )
                                        )
                                            .request.url = "Observation"
                                    }
                                }
                                "Mother-Name" -> {
                                    val value = extractResponse(inner, "valueString")
                                    if (value.isNotEmpty()) {
                                        bundle.addEntry().setResource(
                                            qh.codingQuestionnaire(
                                                "Mother-Name",
                                                "Mother's Name",
                                                value
                                            )
                                        )
                                            .request.url = "Observation"
                                    }
                                }
                                "Baby-Name" -> {
                                    val value = extractResponse(inner, "valueString")
                                    if (value.isNotEmpty()) {
                                        bundle.addEntry().setResource(
                                            qh.codingQuestionnaire(
                                                "Baby-Name",
                                                "Baby Name",
                                                value
                                            )
                                        )
                                            .request.url = "Observation"
                                    }
                                }
                                "DHM-Type" -> {
                                    val value = extractResponseCode(inner, "valueCoding")
                                    if (value.isNotEmpty()) {
                                        bundle.addEntry().setResource(
                                            qh.codingQuestionnaire(
                                                "DHM-Type",
                                                "DHM Type",
                                                value,
                                            )
                                        )
                                            .request.url = "Observation"
                                    }
                                }
                                "Breast-Milk" -> {
                                    val value =
                                        extractResponseQuantity(inner, "valueQuantity")
                                    if (value.isNotEmpty()) {
                                        bundle.addEntry().setResource(
                                            qh.quantityQuestionnaire(
                                                "Breast-Milk",
                                                "Breast Milk",
                                                "Breast-Milk",
                                                value, "mls"
                                            )
                                        )
                                            .request.url = "Observation"
                                    }
                                }

                                "Consent-Given" -> {

                                    given = extractResponseCode(inner, "valueCoding")
                                    if (given.isNotEmpty()) {
                                        bundle.addEntry().setResource(
                                            qh.codingQuestionnaire(
                                                "Consent-Given",
                                                "Consent Given",
                                                given,
                                            )
                                        )
                                            .request.url = "Observation"
                                    }

                                }
                                "Consent-Date" -> {
                                    if (given == "Yes") {

                                        val value = extractResponse(inner, "valueDate")
                                        if (value.isNotEmpty()) {
                                            bundle.addEntry().setResource(
                                                qh.codingQuestionnaire(
                                                    "Consent-Date",
                                                    "Consent Date",
                                                    value,
                                                )
                                            )
                                                .request.url = "Observation"
                                        }
                                    }
                                }

                                "DHM-Reason" -> {

                                    val value = extractResponse(inner, "valueString")
                                    if (value.isNotEmpty()) {
                                        bundle.addEntry().setResource(
                                            qh.codingQuestionnaire(
                                                "DHM-Reason",
                                                "DHM Reason",
                                                value,
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

                    /**
                     * Search Mother using the provided Ip Number
                     * If mother's records found, proceed to get the child
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

                    collectRelatedData(patientIp, bundle)
                    isResourcesSaved.postValue(true)

                } catch (e: Exception) {
                    Timber.d("Exception:::: ${e.printStackTrace()}")
                    isResourcesSaved.postValue(false)
                    return@launch

                }
            }
        }
    }

    private suspend fun collectRelatedData(patientIp: String, bundle: Bundle) {
        val mother = getPatient(patientIp)
        /**
         * Using mother's details, search the baby & Extract the id section only
         */
        Timber.e("Mother::: ${mother.reference}")
        val babyIp = mother.reference.toString().drop(8)
        Timber.e("Baby::: $babyIp")
        val baby = getPatient(babyIp)

        val encounterId = generateUuid()
        title = "DHM Recipient"

        val subjectReference = Reference("Patient/$babyIp")
        subjectReference.display = baby.name

        val encounterReference = Reference("Encounter/$encounterId")
        encounterReference.display = title

        /*  val no = NutritionOrder()
          no.id = generateUuid()
          no.patient = subjectReference
          no.encounter = encounterReference
          no.status = NutritionOrder.NutritionOrderStatus.ACTIVE
          no.dateTime = Date()
          no.intent = NutritionOrder.NutritiionOrderIntent.ORDER

          fhirEngine.create(no)*/

        saveResources(bundle, subjectReference, encounterId, title)

    }

    private suspend fun getPatient(patientId: String): PatientItem {
        val patient = fhirEngine.load(Patient::class.java, patientId)
        return patient.toPatientItem(0)
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

                    isResourcesSaved.postValue(true)

                } catch (e: Exception) {
                    Timber.d("Exception:::: ${e.printStackTrace()}")
                    isResourcesSaved.postValue(false)
                    return@launch

                }

            }
        }
    }

    fun dispensingDetails(
        questionnaireResponse: QuestionnaireResponse,
        patientId: String,
        orderId: String,
        encounterId: String
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

                                "Volume" -> {
                                    val volume =
                                        extractResponseQuantity(inner, "valueQuantity")
                                    if (volume.isNotEmpty()) {
                                        bundle.addEntry().setResource(
                                            qh.quantityQuestionnaire(
                                                "Volume-Dispensed",
                                                "Volume Dispensed",
                                                "Volume Dispensed",
                                                volume, "mls"
                                            )
                                        )
                                            .request.url = "Observation"
                                    }
                                }
                                "DHM-Type" -> {
                                    val type = extractResponseCode(inner, "valueCoding")
                                    if (type.isNotEmpty()) {
                                        bundle.addEntry().setResource(
                                            qh.codingQuestionnaire(
                                                "DHM-Type",
                                                "DHM Type",
                                                type
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
                                                "Additional-Notes",
                                                "Additional Notes and Remarks",
                                                type,
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
                    val encounterReference = Reference("Encounter/$encounterId")
                    title = "DHM Dispensing"

                    val order = NutritionOrder()
                    order.id = orderId
                    order.status = NutritionOrder.NutritionOrderStatus.COMPLETED
                    order.patient = subjectReference
                    order.encounter = encounterReference
                    saveResourceToDatabase(order)
                    saveResources(bundle, subjectReference, encounterId, title)
                    isResourcesSaved.postValue(true)

                } catch (e: Exception) {
                    Timber.d("Exception:::: ${e.printStackTrace()}")
                    isResourcesSaved.postValue(false)
                    return@launch

                }

            }
        }
    }

    fun makeComplete() {
        customMessage.postValue(null)
    }

    fun updateStock(
        questionnaireResponse: QuestionnaireResponse,
        pa: String,
        upa: String,
        totalDhm: String
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

                    bundle.addEntry()
                        .setResource(
                            qh.quantityQuestionnaire(
                                "Pasteurized",
                                "Pasteurized",
                                "Pasteurized",
                                pa,
                                "mls"
                            )
                        )
                        .request.url = "Observation"

                    bundle.addEntry()
                        .setResource(
                            qh.quantityQuestionnaire(
                                "Un-Pasteurized",
                                "Un Pasteurized",
                                "Un Pasteurized",
                                upa,
                                "mls"
                            )
                        )
                        .request.url = "Observation"

                    bundle.addEntry()
                        .setResource(
                            qh.quantityQuestionnaire(
                                "Total-Stock",
                                "Total Stock",
                                "Total Stock",
                                totalDhm,
                                "mls"
                            )
                        )
                        .request.url = "Observation"
                    emptyObservations(bundle, generateUuid(), DHM_STOCK)
                    isResourcesSaved.postValue(true)

                } catch (e: Exception) {
                    isResourcesSaved.postValue(false)

                }
            }

        }

    }

    fun testSubmit(questionnaireResponse: QuestionnaireResponse) {
        viewModelScope.launch {
            val bundle = ResourceMapper.extract(questionnaireResource, questionnaireResponse)
            val subjectReference = Reference("Patient/c6fc7a5f-a74a-494c-907c-85ada7d527ff")
            val encounterId = generateUuid()
            if (isRequiredFieldMissing(bundle)) {
                isResourcesSaved.value = false
                return@launch
            }
            saveResources(bundle, subjectReference, encounterId,"Test Case")
            generateRiskAssessmentResource(bundle, subjectReference, encounterId)
            isResourcesSaved.value = true
        }

    }


}





