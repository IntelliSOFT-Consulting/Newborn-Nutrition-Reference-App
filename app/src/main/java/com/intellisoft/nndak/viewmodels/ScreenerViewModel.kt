package com.intellisoft.nndak.viewmodels

import android.app.Application
import androidx.lifecycle.*
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
import com.intellisoft.nndak.models.FeedingCuesTips
import com.intellisoft.nndak.models.PatientItem
import com.intellisoft.nndak.screens.dashboard.RegistrationFragment.Companion.QUESTIONNAIRE_FILE_PATH_KEY
import com.intellisoft.nndak.utils.Constants.SYNC_VALUE
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


    private suspend fun getEDD(itemsList1: MutableList<QuestionnaireResponse.QuestionnaireResponseItemComponent>): BasicThree {

        val formatHelper = FormatHelper()

        var basicThree = BasicThree("", "", "")

        for (mainItem in itemsList1) {

            val mainLinkId = mainItem.linkId
            val subItemList = mainItem.item
            if (mainLinkId == "Pregnancy Details") {

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

    fun completeAssessment(questionnaireResponse: QuestionnaireResponse, patientId: String) {
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

                /**
                 * Extract Observations, Patient Data
                 */
                val qh = QuestionnaireHelper()
                var feed = ""
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
                            Timber.e("Child $child")
                            if (childChild == "Assessment-Date") {
                                val parity = extractResponse(inner, "valueDateTime")
                                if (parity.isNotEmpty()) {

                                    bundle.addEntry().setResource(
                                        qh.codingQuestionnaire(
                                            "50786-3",
                                            "Assessment Date",
                                            parity
                                        )
                                    )
                                        .request.url = "Observation"
                                }
                            }


                            if (childChild == "Current-Weight") {
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
                            if (childChild == "Baby-Well") {
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
                            }
                            if (childChild == "Asphyxia") {
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
                            if (childChild == "Jaundice") {
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
                            if (childChild == "Neonatal-Sepsis") {
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
                            if (childChild == "Other-Conditions") {
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
                            if (childChild == "Baby-Fed") {
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
                                }
                            }
                            if (childChild == "Feed-Type") {
                                if (feed == "Yes") {
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
                            }
                        }
                    }
                }

                val encounterId = generateUuid()
                val subjectReference = Reference("Patient/$patientId")
                title = "Assessment"
                saveResources(bundle, subjectReference, encounterId, title)
                generateRiskAssessmentResource(bundle, subjectReference, encounterId)
                isResourcesSaved.value = true

            } catch (e: Exception) {
                Timber.d("Exception:::: ${e.printStackTrace()}")
                isResourcesSaved.value = false
                return@launch

            }
        }
    }

    fun clientRegistration(questionnaireResponse: QuestionnaireResponse, patientId: String) {
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
                    val mother = Patient()
                    val subjectReference = Reference("Patient/$patientId")
                    mother.active = true
                    mother.linkFirstRep.other = subjectReference
                    mother.gender = Enumerations.AdministrativeGender.FEMALE

                    val qh = QuestionnaireHelper()
                    var dType = ""
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
                                Timber.e("Child $child")
                                when (childChild) {
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
                                            isResourcesSaved.value = false
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
                                        val mPreg = extractResponseCode(inner, "valueCoding")
                                        if (mPreg.isNotEmpty()) {
                                            bundle.addEntry().setResource(
                                                qh.codingQuestionnaire(
                                                    "64708-1",
                                                    "Multiple Pregnancy",
                                                    mPreg
                                                )
                                            )
                                                .request.url = "Observation"
                                        }
                                    }
                                    "Multiple-Birth-Type" -> {
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
                                    "Time-Of-Delivery" -> {
                                        val bType = extractResponse(inner, "valueDateTime")
                                        bundle.addEntry().setResource(
                                            qh.codingQuestionnaire(
                                                "93857-1",
                                                "Time of Delivery",
                                                bType
                                            )
                                        )
                                            .request.url = "Observation"
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
                                        val dob = extractResponse(inner, "valueDate")
                                        baby.birthDate = FormatHelper().dateOfBirth(dob)
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
                                                    "g"

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
                                                        "g"

                                                    )
                                                )
                                                    .request.url = "Observation"
                                            }
                                        }
                                    }
                                    "Admission-Date" -> {
                                        val bType = extractResponse(inner, "valueDateTime")
                                        if (bType.isNotEmpty()) {
                                            bundle.addEntry().setResource(
                                                qh.codingQuestionnaire(
                                                    "52455-3",
                                                    "Admission Date",
                                                    bType
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
                                                    "60733-3",
                                                    "Doctor's Notes",
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
                    }
                    fhirEngine.create(baby)
                    fhirEngine.create(mother)

                    val encounterId = generateUuid()
                    title = "Client Registration"
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

    fun feedPrescription(questionnaireResponse: QuestionnaireResponse, patientId: String) {
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

                    val qh = QuestionnaireHelper()
                    var dType = ""
                    var date = FormatHelper().getTodayDate()
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
                                Timber.e("Child $child")
                                when (childChild) {
                                    "Current-Weight" -> {
                                        val value = extractResponseQuantity(inner, "valueQuantity")
                                        if (value.isNotEmpty()) {
                                            bundle.addEntry().setResource(
                                                qh.quantityQuestionnaire(
                                                    "Current-Weight",
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
                                        val value = extractResponseQuantity(inner, "valueQuantity")
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
                                    "Feeding-Frequency" -> {
                                        val value = extractResponseCode(inner, "valueCoding")
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
                                    "Feeding-Route" -> {
                                        val value = extractResponseCode(inner, "valueCoding")
                                        if (value.isNotEmpty()) {
                                            bundle.addEntry().setResource(
                                                qh.codingQuestionnaire(
                                                    "Feeding-Route",
                                                    "Feeding Route",
                                                    value
                                                )
                                            )
                                                .request.url = "Observation"
                                        }
                                    }
                                    "IV-Fluids" -> {
                                        val value = extractResponseQuantity(inner, "valueQuantity")
                                        if (value.isNotEmpty()) {
                                            bundle.addEntry().setResource(
                                                qh.quantityQuestionnaire(
                                                    "IV-Fluids",
                                                    "IV Fluids",
                                                    "IV Fluids",
                                                    value, "mls"
                                                )
                                            )
                                                .request.url = "Observation"
                                        }
                                    }
                                    "Breast-Milk" -> {
                                        val value = extractResponseQuantity(inner, "valueQuantity")
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
                                    "DHM-Required" -> {
                                        dhm = extractResponseCode(inner, "valueCoding")
                                        if (dhm.isNotEmpty()) {
                                            bundle.addEntry().setResource(
                                                qh.codingQuestionnaire(
                                                    "DHM-Required",
                                                    "DHM Required",
                                                    dhm,
                                                )
                                            )
                                                .request.url = "Observation"
                                        }
                                    }
                                    "Feeding-Supplements" -> {
                                        supplements = extractResponseCode(inner, "valueCoding")
                                        if (supplements.isNotEmpty()) {
                                            bundle.addEntry().setResource(
                                                qh.codingQuestionnaire(
                                                    "Feeding-Supplements",
                                                    "Feeding Supplements",
                                                    supplements,
                                                )
                                            )
                                                .request.url = "Observation"
                                        }
                                    }
                                    "Additional-Feeds" -> {
                                        if (supplements == "Yes") {

                                            val value = extractResponseCode(inner, "valueCoding")
                                            if (value.isNotEmpty()) {
                                                bundle.addEntry().setResource(
                                                    qh.codingQuestionnaire(
                                                        "Additional-Feeds",
                                                        "Additional Feeds",
                                                        value,
                                                    )
                                                )
                                                    .request.url = "Observation"
                                            }
                                        }
                                    }
                                    "Supplements-Feeding" -> {
                                        if (supplements == "Yes") {

                                            val value = extractResponseCode(inner, "valueCoding")
                                            if (value.isNotEmpty()) {
                                                bundle.addEntry().setResource(
                                                    qh.codingQuestionnaire(
                                                        "Supplements-Feeding",
                                                        "Feeding Supplements",
                                                        value,
                                                    )
                                                )
                                                    .request.url = "Observation"
                                            }
                                        }
                                    }
                                    "Donated-Breast-Milk" -> {
                                        if (dhm == "Yes") {

                                            val value =
                                                extractResponseQuantity(inner, "valueQuantity")
                                            if (value.isNotEmpty()) {
                                                bundle.addEntry().setResource(
                                                    qh.quantityQuestionnaire(
                                                        "Donated-Breast-Milk",
                                                        "Donated Breast Milk",
                                                        "Donated Breast Milk",
                                                        value, "mls"
                                                    )
                                                )
                                                    .request.url = "Observation"
                                            }
                                        }
                                    }
                                    "DHM-Type" -> {
                                        if (dhm == "Yes") {

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
                                    }

                                    "Consent-Given" -> {
                                        if (dhm == "Yes") {

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

                                    "DHM-Reasons" -> {
                                        if (dhm == "Yes") {

                                            val value = extractResponse(inner, "valueString")
                                            if (value.isNotEmpty()) {
                                                bundle.addEntry().setResource(
                                                    qh.codingQuestionnaire(
                                                        "DHM-Reasons",
                                                        "DHM-Reasons",
                                                        value,
                                                    )
                                                )
                                                    .request.url = "Observation"
                                            }
                                        }
                                    }

                                    else -> {
                                        println("Items skipped...")
                                    }
                                }
                            }
                        }
                    }

                    val encounterId = generateUuid()
                    title = "Feeds Prescription"
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

    fun saveMaternity(
        questionnaireResponse: QuestionnaireResponse,
        patientId: String,
        reason: String
    ) {
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

                val qh = QuestionnaireHelper()

                /**
                 * Establish if transferring to PNU
                 */
                val well = extractStatus(questionnaire, "Status-Well", true)


                val value = extractStatus(questionnaire, "Mothers-Status", false)

                if (value.isNotEmpty()) {
                    bundle.addEntry()
                        .setResource(
                            qh.codingQuestionnaire(
                                "Mother's Health",
                                value,
                                value
                            )
                        )
                        .request.url = "Observation"
                }
                val itemsList1 = questionnaireResponse.item
                val basicThree = getEDD(itemsList1)


                if (basicThree.edd.isNotEmpty() && basicThree.lmp.isNotEmpty() && basicThree.gestation.isNotEmpty()) {

                    val isDateValid = FormatHelper().dateLessThanToday(basicThree.lmp)
                    if (!isDateValid) {
                        isResourcesSaved.value = false
                        return@launch
                    }
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

                    bundle.addEntry()
                        .setResource(
                            qh.codingQuestionnaire(
                                "Gestation",
                                "Gestation",
                                basicThree.gestation
                            )
                        )
                        .request.url = "Observation"
                }


                val subjectReference = Reference("Patient/$patientId")
                val encounterId = generateUuid()
                saveResources(bundle, subjectReference, encounterId, reason)
                generateAssessmentResource(bundle, subjectReference, encounterId, risk)
                isResourcesSaved.value = true


            } catch (e: Exception) {
                Timber.d("Exception:::: ${e.printStackTrace()}")
                isResourcesSaved.value = false
                return@launch
            }


        }
    }


    private fun extractStatus(questionnaire: String, string: String, coding: Boolean): String {
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

                    if (childChild == string) {

                        value = if (coding) {
                            extractCoding(inner, 0)
                        } else {
                            extractValueString(inner, 0)
                        }

                    }
                }
            }
        }
        return value
    }


    fun saveAssessment(
        questionnaireResponse: QuestionnaireResponse,
        patientId: String,
        reason: String
    ) {

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

                title = common.getJSONObject(0).getString("linkId")

                for (i in 0 until common.length()) {

                    val item = common.getJSONObject(i)

                    val parent = item.getJSONArray("item")
                    for (j in 0 until parent.length()) {

                        val itemChild = parent.getJSONObject(j)
                        val child = itemChild.getJSONArray("item")
                        for (k in 0 until child.length()) {
                            val inner = child.getJSONObject(k)

                            when (inner.getString("linkId")) {
                                "Feeding-Frequency" -> {

                                    val value = extractValueOption(inner)
                                    frequency = Integer.parseInt(value.substring(0, 1))
                                }
                                "Volume-Required" -> {
                                    volume = extractValueDecimal(inner)

                                }
                                "Time-Seen" -> {

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
                                "Shift-Time" -> {

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
                                "Born-Where" -> {

                                    val childAnswer = inner.getJSONArray("answer")
                                    val value =
                                        childAnswer.getJSONObject(0).getString("valueString")

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
                                "Admission-Reason" -> {

                                    val childAnswer = inner.getJSONArray("answer")
                                    val value =
                                        childAnswer.getJSONObject(0).getString("valueString")

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
                                "Completed-By" -> {

                                    /**
                                     * Retrieve Logged User
                                     */
                                    /**
                                     * Retrieve Logged User
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
                                "Assessment-Date" -> {

                                    val childAnswer = inner.getJSONArray("answer")
                                    val value = childAnswer.getJSONObject(0).getString("valueDate")

                                    bundle.addEntry()
                                        .setResource(
                                            qh.codingQuestionnaire(
                                                "Date of Assessment",
                                                value,
                                                value
                                            )
                                        )
                                        .request.url = "Observation"
                                }
                                "Today-Date" -> {

                                    val childAnswer = inner.getJSONArray("answer")
                                    val value = childAnswer.getJSONObject(0).getString("valueDate")

                                    bundle.addEntry()
                                        .setResource(
                                            qh.codingQuestionnaire(
                                                "Today's Date",
                                                value,
                                                value
                                            )
                                        )
                                        .request.url = "Observation"
                                }
                                "Date-Of-Discharge" -> {

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
                                "Diagnosis-At-Discharge" -> {

                                    val childAnswer = inner.getJSONArray("answer")
                                    val value =
                                        childAnswer.getJSONObject(0).getString("valueString")

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
                                "Assessment" -> {

                                    val childAnswer = inner.getJSONArray("answer")
                                    val value =
                                        childAnswer.getJSONObject(0).getString("valueString")

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
                                "Feeding-Status" -> {

                                    val value = extractValueString(inner, 0)

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
                                "Outcome-Status" -> {

                                    val value = extractValueString(inner, 0)
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
                                "Diagnosis-Status" -> {

                                    val value = extractValueString(inner, 0)
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
                                "Intervention-Status" -> {

                                    val value = extractValueString(inner, 0)
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
                                "Recipient-Location" -> {

                                    val value = extractValueString(inner, 0)
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
                                "Done-Status" -> {

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
                                "Name-Of-Prescriber" -> {

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
                                "Nursing-Staff-Name" -> {

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
                                "Clinician-Name" -> {

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
                                "Clinician-Designation" -> {

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
                                "Treatment-Duration" -> {

                                    val value = extractValueString(inner, 0)
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
                                "Prescribing-Instructions" -> {

                                    val value = extractValueString(inner, 0)
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
                                "Reason-For-Receiving" -> {

                                    val value = extractValueString(inner, 0)
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
                                "Any-Remarks" -> {

                                    val value = extractValueString(inner, 0)
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
                                "Nursing-Plan" -> {


                                    val value = extractValueString(inner, 0)
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
                                "Prescription-Time" -> {

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
                                "Time-Of-Feeding" -> {

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
                                "DHM-Time" -> {

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
                                "Time-Lactation-Support" -> {

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
                                "Time-of-Storage" -> {

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
                                "Time-of-Expression" -> {

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
                                "Assessment-Date-Time" -> {

                                    val value = extractValueDateTime(inner)
                                    bundle.addEntry()
                                        .setResource(
                                            qh.codingQuestionnaire(
                                                "Date of Assessment",
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

                                "Batch-Number" -> {

                                    val value = extractValueString(inner, 0)
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
                                "Donor-ID" -> {

                                    val value = extractValueString(inner, 0)
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
                                "Shift-Notes" -> {

                                    val value = extractValueString(inner, 0)
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
                                "Additional-Comments" -> {


                                    val value = extractValueString(inner, 0)
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
                                "Feeding-Considered" -> {


                                    val value = extractValueString(inner, 0)
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
                                "Legal-Guardian-Signature" -> {


                                    val value = extractValueString(inner, 0)
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
                                "Dispensing-Staff-Name" -> {


                                    val value = extractValueString(inner, 0)
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
                                "Receiving-Staff-Name" -> {


                                    val value = extractValueString(inner, 0)
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
                                "Consent-Date" -> {


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
                                "Expiry-Date" -> {


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
                                else -> {
                                    println("Skip Items...")
                                }
                            }
                        }
                    }
                }

                if (isRequiredFieldMissing(bundle)) {
                    isResourcesSaved.value = false
                    return@launch
                }


                if (frequency > 0 && volume.isNotEmpty()) {
                    val value = calculate24HourFeed(frequency, volume)
                    bundle.addEntry()
                        .setResource(
                            qh.quantityQuestionnaire(
                                "Total 24hr volume required",
                                "Total 24hr volume required",
                                "Total 24hr volume required",
                                value,
                                "ml(s)"
                            )
                        )
                        .request.url = "Observation"
                }
                val subjectReference = Reference("Patient/$patientId")
                val encounterId = generateUuid()
                saveResources(bundle, subjectReference, encounterId, title)
                generateRiskAssessmentResource(bundle, subjectReference, encounterId)
                isResourcesSaved.value = true

            } catch (e: Exception) {
                Timber.d("Exception:::: ${e.printStackTrace()}")
                isResourcesSaved.value = false
                return@launch
            }
        }
    }

    private fun calculate24HourFeed(frequency: Int, volume: String): String {
        var total = 0.0
        val times = 24 / frequency
        val j: Float = times.toFloat()
        val k: Float = volume.toFloat()
        total = (j * k).toDouble()

        return total.toString()
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


    private fun extractValueString(inner: JSONObject, index: Int): String {

        val childAnswer = inner.getJSONArray("item")
        val ans = childAnswer.getJSONObject(index).getJSONArray("answer")

        return ans.getJSONObject(index).getString("valueString")
    }

    private fun extractCoding(inner: JSONObject, index: Int): String {

        val childAnswer = inner.getJSONArray("item")
        val ans = childAnswer.getJSONObject(index).getJSONArray("answer")

        return ans.getJSONObject(index).getJSONObject("valueCoding").getString("display")
    }

    private fun extractValueDecimal(inner: JSONObject): String {

        val childAnswer = inner.getJSONArray("item")
        val ans = childAnswer.getJSONObject(0).getJSONArray("answer")

        return ans.getJSONObject(0).getString("valueDecimal")
    }

    private fun extractValueDateTime(inner: JSONObject): String {

        val childAnswer = inner.getJSONArray("item")
        val ans = childAnswer.getJSONObject(0).getJSONArray("answer")

        return ans.getJSONObject(0).getString("valueDateTime")

    }

    private fun extractValueOption(inner: JSONObject): String {

        val childAnswer = inner.getJSONArray("answer")
        val ans = childAnswer.getJSONObject(0).getJSONObject("valueCoding")
        return ans.getString("display")

    }

    fun saveApgar(questionnaireResponse: QuestionnaireResponse, patientId: String, reason: String) {
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
                 * Extract time then Skip the 1st item
                 * **/

                val title = common.getJSONObject(0)
                val childTitle = title.getJSONArray("item")
                val minute = extractValueCodingTitle(childTitle, 0)

                for (i in 1 until common.length()) {

                    val child = common.getJSONObject(i)
                    val childItem = child.getJSONArray("item")

                    for (j in 0 until childItem.length()) {
                        val display = extractValueCoding(childItem, j)
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
                        qh.quantityQuestionnaire(
                            minute,
                            minute,
                            minute,
                            total.toString(),
                            "pts"

                        )
                    )
                    .request.url = "Observation"

                saveResources(bundle, subjectReference, encounterId, minute)
                generateApgarAssessmentResource(bundle, subjectReference, encounterId, total)
                apgarScore.value = ApGar("$total", "Apgar Score Successfully Recorded", isSafe)
            } catch (e: Exception) {
                Timber.d("Exception:::: ${e.printStackTrace()}")
                apgarScore.value = ApGar("", "Check All Inputs", false)
                return@launch
            }
        }
    }

    private fun extractValueCodingTitle(childTitle: JSONArray, i: Int): String {
        var time = ""
        val display = extractValueCoding(childTitle, i)
        if (display == "1 min") {
            time = "Apgar at 1 minute"
        }
        if (display == "5 mins") {

            time = "Apgar at 5 minutes"
        }
        if (display == "10 mins") {

            time = "Apgar at 10 minutes"
        }

        return time
    }

    private fun extractValueCoding(childItem: JSONArray, j: Int): String {

        val answer = childItem.getJSONObject(j)
        val childAnswer = answer.getJSONArray("answer")
        val valueCoding = childAnswer.getJSONObject(0).getString("valueCoding")
        val finalAnswer = JSONObject(valueCoding)
        return finalAnswer.getString("display")
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
        encounterId: String,
        reason: String
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
                    resource.reasonCodeFirstRep.text = reason
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

    fun breastFeeding(
        questionnaireResponse: QuestionnaireResponse, breastFeeding: String,
        efficientFeeding: String, patientId: String
    ) {
        viewModelScope.launch {
            val bundle =
                ResourceMapper.extract(
                    getApplication(),
                    questionnaireResource,
                    questionnaireResponse
                )
            val qh = QuestionnaireHelper()
            val context = FhirContext.forR4()
            val questionnaire =
                context.newJsonParser().encodeResourceToString(questionnaireResponse)
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
                            Timber.e("Child $inner")
                            if (childChild == "Breast-Feeding") {
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "Baby-BreastFeeding",
                                            breastFeeding,
                                            breastFeeding
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                            if (childChild == "Efficient-Feeding") {
                                bundle.addEntry()
                                    .setResource(
                                        qh.codingQuestionnaire(
                                            "BreastFeeding-Efficient",
                                            efficientFeeding,
                                            efficientFeeding
                                        )
                                    )
                                    .request.url = "Observation"
                            }
                        }

                    }
                }
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
                    getApplication(),
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
                        for (j in 0 until parent.length()) {
                            val itemChild = parent.getJSONObject(j)
                            val child = itemChild.getJSONArray("item")
                            for (k in 0 until child.length()) {
                                val inner = child.getJSONObject(k)
                                val childChild = inner.getString("linkId")
                                Timber.e("Child $inner")
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
                                                cues.tenSide.toString(), cues.tenSide.toString(),

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

                                }

                            }

                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val subjectReference = Reference("Patient/$patientId")

                val encounterId = generateUuid()
                title = "Feeding Cues"
                saveResources(bundle, subjectReference, encounterId, title)
                isResourcesSaved.postValue(true)
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
                    getApplication(),
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
                        for (j in 0 until parent.length()) {
                            val itemChild = parent.getJSONObject(j)
                            val child = itemChild.getJSONArray("item")
                            for (k in 0 until child.length()) {
                                val inner = child.getJSONObject(k)
                                val childChild = inner.getString("linkId")
                                Timber.e("Child $child")
                                if (childChild == "Milk-Expressed") {
                                    val volume = extractResponseQuantity(inner, "valueQuantity")
                                    if (volume.isNotEmpty()) {
                                        Timber.e("Time: $volume")
                                        bundle.addEntry().setResource(
                                            qh.codingQuestionnaire(
                                                "Milk-Expressed",
                                                "Milk Expressed",
                                                volume
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
                    }

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
                    getApplication(),
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
                        for (j in 0 until parent.length()) {
                            val itemChild = parent.getJSONObject(j)
                            val child = itemChild.getJSONArray("item")
                            for (k in 0 until child.length()) {
                                val inner = child.getJSONObject(k)
                                val childChild = inner.getString("linkId")
                                Timber.e("Child $child")
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
                    }

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
        patientId: String
    ) {
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
                        for (j in 0 until parent.length()) {
                            val itemChild = parent.getJSONObject(j)
                            val child = itemChild.getJSONArray("item")
                            for (k in 0 until child.length()) {
                                val inner = child.getJSONObject(k)
                                val childChild = inner.getString("linkId")
                                Timber.e("Child $child")
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
                                        val value = extractResponseQuantity(inner, "valueQuantity")
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
                                        val value = extractResponseQuantity(inner, "valueQuantity")
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
                                        val value = extractResponseQuantity(inner, "valueQuantity")
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
                                        val value = extractResponseQuantity(inner, "valueQuantity")
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
                    }

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

    fun addDhmRecipient(questionnaireResponse: QuestionnaireResponse) {
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

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (isRequiredFieldMissing(bundle)) {
                        isResourcesSaved.postValue(false)
                        return@launch
                    }

                    /**
                     * Retrieve Mother Using the Provided ID
                     */

                    //val subjectReference = Reference("Patient/$patientId")

                    val qh = QuestionnaireHelper()

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
                                Timber.e("Child $child")
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
                                        val value = extractResponseQuantity(inner, "valueQuantity")
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
                    }

                    /**
                     * Search Mother using the provided Ip Number
                     * If mother's records found, proceed to get the child
                     */
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


        val nutritionOrder = NutritionOrder()
        nutritionOrder.id = generateUuid()
        nutritionOrder.patient = subjectReference
        nutritionOrder.encounter = encounterReference
        nutritionOrder.status = NutritionOrder.NutritionOrderStatus.ACTIVE
        nutritionOrder.dateTime = Date()
        nutritionOrder.intent = NutritionOrder.NutritiionOrderIntent.ORDER

        fhirEngine.create(nutritionOrder)

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
                    getApplication(),
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
                        for (j in 1 until parent.length()) {
                            val itemChild = parent.getJSONObject(j)
                            val child = itemChild.getJSONArray("item")
                            for (k in 0 until child.length()) {
                                val inner = child.getJSONObject(k)
                                val childChild = inner.getString("linkId")
                                Timber.e("Child $child")
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
                    }

                    val total = Integer.parseInt(unpasteurized) + Integer.parseInt(pasteurized)
                    bundle.addEntry().setResource(
                        qh.quantityQuestionnaire(
                            "Total-Stock",
                            "Total Stock",
                            "Total Stock",
                            total.toString(), "mls"
                        )
                    )
                        .request.url = "Observation"

                    title = "DHM Stock"
                    val encounterId = generateUuid()


                    isResourcesSaved.postValue(true)

                } catch (e: Exception) {
                    Timber.d("Exception:::: ${e.printStackTrace()}")
                    isResourcesSaved.postValue(false)
                    return@launch

                }

            }
        }
    }

    fun dispensingDetails(questionnaireResponse: QuestionnaireResponse, patientId: String) {
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

            CoroutineScope(Dispatchers.IO).launch {
                try {

                    val qh = QuestionnaireHelper()

                    val json = JSONObject(questionnaire)
                    val common = json.getJSONArray("item")
                    for (i in 0 until common.length()) {
                        val item = common.getJSONObject(i)
                        val parent = item.getJSONArray("item")
                        for (j in 1 until parent.length()) {
                            val itemChild = parent.getJSONObject(j)
                            val child = itemChild.getJSONArray("item")
                            for (k in 0 until child.length()) {
                                val inner = child.getJSONObject(k)
                                val childChild = inner.getString("linkId")
                                Timber.e("Child $child")
                                when (childChild) {

                                    "Volume" -> {
                                        val volume = extractResponseQuantity(inner, "valueQuantity")
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
                    }

                    val subjectReference = Reference("Patient/$patientId}")
                    title = "DHM Dispensing"
                    val encounterId = generateUuid()
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


}





