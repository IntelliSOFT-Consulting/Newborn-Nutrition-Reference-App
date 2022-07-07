package com.intellisoft.nndak.viewmodels

import android.app.Application
import androidx.lifecycle.*
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.get
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.screens.dashboard.prescription.EditPrescriptionFragment
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.*
import timber.log.Timber

class EditEncounterViewModel(application: Application, private val state: SavedStateHandle) :
    AndroidViewModel(application) {
    private val fhirEngine: FhirEngine = FhirApplication.fhirEngine(application.applicationContext)

    private val patientId: String = requireNotNull(state["encounter_id"])
    val liveEncounterData = liveData { emit(prepareEditPatient()) }

    private suspend fun prepareEditPatient(): Pair<String, String> {
        val encounter = fhirEngine.get<Encounter>(patientId)
        encounter.let {
            Timber.e("Encounter Patient ${it.subject.reference}")
        }
        val question = readFileFromAssets("feed-prescription.json").trimIndent()
        val parser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
        val questionnaire =
            parser.parseResource(org.hl7.fhir.r4.model.Questionnaire::class.java, question) as
                    Questionnaire

        val questionnaireResponse: QuestionnaireResponse =
            ResourceMapper.populate(questionnaire, encounter)
        val questionnaireResponseJson = parser.encodeResourceToString(questionnaireResponse)
        return question to questionnaireResponseJson
    }

    private val questionnaire: String
        get() = getQuestionnaireJson()
    val isResourcesSaved = MutableLiveData<Boolean>()

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
    /*   fun updatePatient(questionnaireResponse: QuestionnaireResponse) {
           viewModelScope.launch {
               val entry =
                   ResourceMapper.extract(questionnaireResource, questionnaireResponse).entryFirstRep
               if (entry.resource !is Patient) return@launch
               val patient = entry.resource as Patient
               if (patient.hasName() &&
                   patient.name[0].hasGiven() &&
                   patient.name[0].hasFamily() &&
                   patient.hasBirthDate()

               ) {
                   patient.id = patientId
                   fhirEngine.update(patient)
                   isPatientSaved.value = true
                   return@launch
               }

               isPatientSaved.value = false
           }
       }
   */
    private fun getQuestionnaireJson(): String {
        questionnaireJson?.let {
            return it
        }
        questionnaireJson =
            readFileFromAssets(state[EditPrescriptionFragment.QUESTIONNAIRE_FILE_PATH_KEY]!!)
        return questionnaireJson!!
    }

    private fun readFileFromAssets(filename: String): String {
        return getApplication<Application>().assets.open(filename).bufferedReader().use {
            it.readText()
        }
    }

    fun updatePrescription(questionnaireResponse: QuestionnaireResponse, patientId: String) {
        viewModelScope.launch {
            val entry =
                ResourceMapper.extract(questionnaireResource, questionnaireResponse).entryFirstRep
            if (entry.resource !is Encounter) return@launch
            isResourcesSaved.value = true
        }
    }
}