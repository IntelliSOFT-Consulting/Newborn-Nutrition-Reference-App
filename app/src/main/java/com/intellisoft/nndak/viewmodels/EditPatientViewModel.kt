package com.intellisoft.nndak.viewmodels

import android.app.Application
import androidx.lifecycle.*
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.get
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.helper_class.FormatHelper
import com.intellisoft.nndak.screens.dashboard.child.EditBabyFragment
import com.intellisoft.nndak.utils.Constants.SYNC_STATE
import com.intellisoft.nndak.utils.Constants.SYNC_VALUE
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import timber.log.Timber

class EditPatientViewModel(application: Application, private val state: SavedStateHandle) :
    AndroidViewModel(application) {
    private val fhirEngine: FhirEngine = FhirApplication.fhirEngine(application.applicationContext)

    private val patientId: String = requireNotNull(state["patient_id"])
    val livePatientData = liveData { emit(prepareEditPatient()) }

    private suspend fun prepareEditPatient(): Pair<String, String> {
        val patient = fhirEngine.get<Patient>(patientId)
        val question = readFileFromAssets("update.json").trimIndent()
        val parser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
        val questionnaire =
            parser.parseResource(org.hl7.fhir.r4.model.Questionnaire::class.java, question) as
                    Questionnaire

        val questionnaireResponse: QuestionnaireResponse =
            ResourceMapper.populate(questionnaire, patient)
        val questionnaireResponseJson = parser.encodeResourceToString(questionnaireResponse)
        return question to questionnaireResponseJson
    }

    private val questionnaire: String
        get() = getQuestionnaireJson()
    val isPatientSaved = MutableLiveData<Boolean>()

    private val questionnaireResource: Questionnaire
        get() =
            FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
                .parseResource(questionnaire) as
                    Questionnaire

    private var questionnaireJson: String? = null

    /**
     * Update patient registration questionnaire response into the application database.
     *
     * @param questionnaireResponse patient registration questionnaire response
     */
    fun updatePatient(questionnaireResponse: QuestionnaireResponse) {
        viewModelScope.launch {
            val entry =
                ResourceMapper.extract(questionnaireResource, questionnaireResponse).entryFirstRep
            if (entry.resource !is Patient) return@launch
            val patient = entry.resource as Patient
            if (patient.hasName() &&
                patient.name[0].hasGiven() &&
                patient.name[0].hasFamily() &&
                patient.hasBirthDate()
                && patient.hasActive()

            ) {

                val birthDate = patient.birthDate.toString()
                val refineDate = FormatHelper().getBirthdayZone(birthDate)
                val todayDate = FormatHelper().getTodayDate()
                val isValid = FormatHelper().checkDateTime(refineDate, todayDate)

                if (isValid) {
                    if (!patient.hasDeceased()) {
                        patient.addressFirstRep.postalCode = SYNC_VALUE
                        patient.addressFirstRep.state = SYNC_VALUE
                        patient.active = true
                    } else {
                        patient.addressFirstRep.postalCode = SYNC_VALUE
                        patient.addressFirstRep.state = SYNC_STATE
                        patient.active = false
                    }
                    patient.id = patientId
                    fhirEngine.update(patient)
                    isPatientSaved.value = true
                    return@launch

                }
            }

            isPatientSaved.value = false
        }
    }

    private fun getQuestionnaireJson(): String {
        questionnaireJson?.let {
            return it
        }
        questionnaireJson =
            readFileFromAssets(state[EditBabyFragment.QUESTIONNAIRE_FILE_PATH_KEY]!!)
        return questionnaireJson!!
    }

    private fun readFileFromAssets(filename: String): String {
        return getApplication<Application>().assets.open(filename).bufferedReader().use {
            it.readText()
        }
    }
}