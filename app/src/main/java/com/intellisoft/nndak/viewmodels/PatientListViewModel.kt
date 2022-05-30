package com.intellisoft.nndak.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.Search
import com.google.android.fhir.search.StringFilterModifier
import com.google.android.fhir.search.count
import com.google.android.fhir.search.search
import com.intellisoft.nndak.models.*
import com.intellisoft.nndak.utils.Constants
import com.intellisoft.nndak.utils.Constants.SYNC_VALUE
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.RelatedPerson
import org.hl7.fhir.r4.model.RiskAssessment

/**
 * The ViewModel helper class for PatientItemRecyclerViewAdapter, that is responsible for preparing
 * data for UI.
 */
class PatientListViewModel(
    application: Application,
    private val fhirEngine: FhirEngine,
    private val location: String
) :
    AndroidViewModel(application) {

    val liveSearchedPatients = MutableLiveData<List<PatientItem>>()
    val liveMotherBaby = MutableLiveData<List<MotherBabyItem>>()
    val patientCount = MutableLiveData<Long>()

    init {
        updatePatientListAndPatientCount(
            { getSearchResults("", location) },
            { count("", location) })

        updateMumAndBabyCount(
            { getMumSearchResults("", location) },
            { count("", location) })
    }

    fun searchPatientsByName(nameQuery: String) {
        updatePatientListAndPatientCount(
            { getSearchResults(nameQuery, location) },
            { count(nameQuery, location) })

        updateMumAndBabyCount(
            { getMumSearchResults(nameQuery, location) },
            { count("", location) })
    }

    /**
     * [updatePatientListAndPatientCount] calls the search and count lambda and updates the live data
     * values accordingly. It is initially called when this [ViewModel] is created. Later its called
     * by the client every time search query changes or data-sync is completed.
     */
    private fun updatePatientListAndPatientCount(
        search: suspend () -> List<PatientItem>,
        count: suspend () -> Long
    ) {
        viewModelScope.launch {
            liveSearchedPatients.value = search()
            patientCount.value = count()
        }
    }

    private fun updateMumAndBabyCount(
        search: suspend () -> List<MotherBabyItem>,
        count: suspend () -> Long
    ) {
        viewModelScope.launch {
            liveMotherBaby.value = search()
            patientCount.value = count()
        }
    }

    /**
     * Returns count of all the [Patient] who match the filter criteria unlike [getSearchResults]
     * which only returns a fixed range.
     */
    private suspend fun count(nameQuery: String = "", location: String): Long {
        return fhirEngine.count<Patient> {
            if (nameQuery.isNotEmpty()) {
                filter(
                    Patient.NAME,
                    {
                        modifier = StringFilterModifier.CONTAINS
                        value = nameQuery
                    }
                )
            }
            filterCity(this, location)
        }
    }

    private suspend fun getMumSearchResults(
        nameQuery: String = "",
        location: String
    ): List<MotherBabyItem> {
        val mumBaby: MutableList<MotherBabyItem> = mutableListOf()
        fhirEngine
            .search<Patient> {
                if (nameQuery.isNotEmpty()) {
                    filter(
                        Patient.NAME,
                        {
                            modifier = StringFilterModifier.CONTAINS
                            value = nameQuery
                        }
                    )


                }
                filterCity(this, location)
                sort(Patient.GIVEN, Order.ASCENDING)
                count = 100
                from = 0
            }

            .mapIndexed { index, fhirPatient ->
                mum(
                    fhirPatient.toPatientItem(
                        index + 1
                    ), index + 1
                )

            }
            .let {

                mumBaby.addAll(it)
            }

        return mumBaby
    }

    private suspend fun mum(baby: PatientItem, position: Int): MotherBabyItem {
        val mother: MutableList<PatientItem> = mutableListOf()
        fhirEngine
            .search<Patient> {
                filter(
                    Patient.LINK, { value = "Patient/${baby.resourceId}" }

                )

            }
            //.take(Constants.MAX_RESOURCE_COUNT)
            .mapIndexed { index, fhirPatient ->
                fhirPatient.toPatientItem(
                    index + 1
                )

            }
            .let { mother.addAll(it) }
        val obs = getObservations(baby.resourceId)
        var birthWeight = ""
        var status = ""
        if (obs.isNotEmpty()) {
            for (element in obs) {
                if (element.code == "8339-4") {
                    birthWeight = element.value
                }
                if (element.code == "11885-1") {
                    val code = element.value.split("\\.".toRegex()).toTypedArray()
                    status = if (code[0].toInt() < 37) {
                        "Preterm"
                    } else {
                        "Term"
                    }

                }
            }
        }
        var mumName = ""
        var motherIp = ""
        if (mother.isNotEmpty()) {
            mumName = mother[0].name
            motherIp = mother[0].resourceId
        }

        return MotherBabyItem(
            id = position.toString(),
            resourceId = baby.resourceId,
            babyName = baby.name,
            motherName = mumName,
            motherIp = motherIp,
            babyIp = baby.resourceId,
            birthWeight = birthWeight,
            status = status,
            gainRate = "Normal",
            dashboard = BabyDashboard(),
            mother = MotherDashboard()
        )

    }

    private suspend fun getObservations(resourceId: String): List<ObservationItem> {
        val observations: MutableList<ObservationItem> = mutableListOf()
        fhirEngine
            .search<Observation> {
                filter(Observation.SUBJECT, { value = "Patient/$resourceId" })
                sort(Observation.DATE, Order.DESCENDING)
            }
            .map {
                PatientDetailsViewModel.createObservationItem(
                    it,
                    getApplication<Application>().resources
                )
            }
            .let { observations.addAll(it) }
        return observations
    }


    private suspend fun getSearchResults(
        nameQuery: String = "",
        location: String
    ): List<PatientItem> {
        val patients: MutableList<PatientItem> = mutableListOf()
        fhirEngine
            .search<Patient> {
                if (nameQuery.isNotEmpty()) {
                    filter(
                        Patient.NAME,
                        {
                            modifier = StringFilterModifier.CONTAINS
                            value = nameQuery
                        }
                    )

                }
                filterCity(this, location)
                sort(Patient.GIVEN, Order.ASCENDING)
                count = 100
                from = 0
            }
            .mapIndexed { index, fhirPatient -> fhirPatient.toPatientItem(index + 1) }
            .let { patients.addAll(it) }

        return patients
    }

    private fun filterCity(search: Search, location: String) {
        search.filter(Patient.ADDRESS_POSTALCODE, { value = SYNC_VALUE })
    }


    private suspend fun getRiskAssessments(): Map<String, RiskAssessment?> {
        return fhirEngine.search<RiskAssessment> {}.groupBy { it.subject.reference }
            .mapValues { entry
                ->
                entry
                    .value
                    .filter { it.hasOccurrence() }
                    .sortedByDescending { it.occurrenceDateTimeType.value }
                    .firstOrNull()
            }
    }


    class PatientListViewModelFactory(
        private val application: Application,
        private val fhirEngine: FhirEngine,
        private val location: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PatientListViewModel::class.java)) {
                return PatientListViewModel(application, fhirEngine, location) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}

internal fun Patient.toPatientItem(position: Int): PatientItem {
    // Show nothing if no values available for gender and date of birth.
    val patientId = if (hasIdElement()) idElement.idPart else ""
    val name = if (hasName()) name[0].nameAsSingleString else ""
    val gender = if (hasGenderElement()) genderElement.valueAsString else ""
    val dob = if (hasBirthDateElement()) birthDateElement.valueAsString else ""
    val phone = if (hasTelecom()) telecom[0].value else ""
    val city = if (hasAddress()) address[0].city else ""
    val country = if (hasAddress()) address[0].country else ""
    val state = if (hasAddress()) address[0].state else ""
    val district = if (hasAddress()) address[0].district else ""
    val region = if (hasAddress()) address[0].text else ""
    val isActive = active
    val html: String = if (hasText()) text.div.valueAsString else ""


    return PatientItem(
        id = position.toString(),
        resourceId = patientId,
        name = name,
        gender = gender ?: "",
        dob = dob ?: "",
        phone = phone ?: "",
        city = city ?: "",
        country = country ?: "",
        isActive = isActive,
        html = html,
        state = state ?: "",
        district = district ?: "",
        region = region ?: ""
    )
}


/***
 * Child details
 * ***/
internal fun RelatedPerson.toRelatedPersonItem(position: Int): RelatedPersonItem {
    // Show nothing if no values available for gender and date of birth.
    val patientId = if (hasIdElement()) idElement.idPart else ""
    val name = if (hasName()) name[0].nameAsSingleString else ""
    val gender = if (hasGenderElement()) genderElement.valueAsString else ""
    val dob = if (hasBirthDateElement()) birthDateElement.valueAsString else ""

    return RelatedPersonItem(
        id = patientId,
        name = name,
        gender = gender ?: "",
        dob = dob ?: "",
    )
}
