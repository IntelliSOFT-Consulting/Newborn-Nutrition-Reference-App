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
import com.google.android.fhir.datacapture.validation.QuestionnaireResponseValidator
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.helper_class.FormatHelper
import com.intellisoft.nndak.screens.patients.AddPatientFragment
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.*
import java.util.*

/** ViewModel for patient registration screen {@link AddPatientFragment}. */
class AddPatientViewModel(application: Application, private val state: SavedStateHandle) :
    AndroidViewModel(application) {

    val questionnaire: String
        get() = getQuestionnaireJson()
    val isPatientSaved = MutableLiveData<Boolean>()

    private val questionnaireResource: Questionnaire
        get() = FhirContext.forR4().newJsonParser().parseResource(questionnaire) as Questionnaire
    private var fhirEngine: FhirEngine = FhirApplication.fhirEngine(application.applicationContext)
    private var questionnaireJson: String? = null

    /**
     * Saves patient registration questionnaire response into the application database.
     *
     * @param questionnaireResponse patient registration questionnaire response
     */
    fun savePatient(questionnaireResponse: QuestionnaireResponse) {
        viewModelScope.launch {

            val entry =
                ResourceMapper.extract(
                    getApplication(),
                    questionnaireResource,
                    questionnaireResponse
                )
                    .entryFirstRep
            if (entry.resource !is Patient) return@launch
            val patient = entry.resource as Patient

            if (patient.hasName() &&
                patient.name[0].hasGiven() &&
                patient.name[0].hasFamily() &&
                patient.hasBirthDate() &&
                patient.hasTelecom() &&
                patient.telecom[0].value != null &&
                patient.hasBirthDate()
            ) {
                val isPhoneNo = FormatHelper().checkPhoneNo(patient.telecom[0].value)

                val birthDate = patient.birthDate.toString()
                val todayDate = FormatHelper().getTodayDate()
                val isDateValid = FormatHelper().checkDate(birthDate, todayDate)


                if (isDateValid && isPhoneNo){

                    patient.active = true
                    patient.id = generateUuid()
                    fhirEngine.create(patient)
                    isPatientSaved.value = true
                    return@launch

                }
                Log.e("++++++ ", isPhoneNo.toString())

                isPatientSaved.value = false
            }

            isPatientSaved.value = false

        }
    }



    private fun getQuestionnaireJson(): String {
        questionnaireJson?.let {
            return it
        }
        questionnaireJson =
            readFileFromAssets(state[AddPatientFragment.QUESTIONNAIRE_FILE_PATH_KEY]!!)
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
}