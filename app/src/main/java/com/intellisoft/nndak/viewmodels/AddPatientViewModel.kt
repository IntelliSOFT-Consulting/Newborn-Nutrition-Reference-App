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
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.datacapture.validation.QuestionnaireResponseValidator
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.helper_class.DbMotherInfo
import com.intellisoft.nndak.helper_class.DbMotherKey
import com.intellisoft.nndak.helper_class.FormatHelper
import com.intellisoft.nndak.roomdb.HealthViewModel
import com.intellisoft.nndak.roomdb.MotherInfo
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

            val context = getApplication<Application?>().applicationContext

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

                    val fhirId = generateUuid()
                    val phoneNumber = patient.telecom[0].value

                    var familyName = ""
                    var firstName = ""

                    val nameList = patient.name
                    for (name in nameList){
                        familyName = name.family
                        firstName = name.given[0].toString()
                    }

                    val motherDOB = patient.birthDate.toString()
                    val natID = DbMotherKey.NATIONALID.name

                    val nationalId = FormatHelper().retrieveSharedPreference(context, natID).toString()

                    val motherInfo = DbMotherInfo(nationalId, motherDOB, firstName, familyName, phoneNumber, fhirId)

                    val healthViewModel = HealthViewModel(getApplication())
                    healthViewModel.updateMotherInfo(context, motherInfo)

                    patient.active = true
                    patient.id = fhirId
                    fhirEngine.create(patient)
                    isPatientSaved.value = true
                    return@launch

                }else{

                    if (!isDateValid)
                        customMessage("The provided date is incorrect.", context)

                    if (!isPhoneNo)
                        customMessage("The provided phone number is incorrect.", context)

                    isPatientSaved.value = false

                }

            }else{

                customMessage("There some missing fields. Please check on them before proceeding.", context)
                isPatientSaved.value = false

            }

        }
    }

    private fun customMessage(text: String, context: Context){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()

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