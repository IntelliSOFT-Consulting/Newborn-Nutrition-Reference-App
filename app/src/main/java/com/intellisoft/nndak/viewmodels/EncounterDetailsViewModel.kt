package com.intellisoft.nndak.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.search.search
import com.intellisoft.nndak.MAX_RESOURCE_COUNT
import com.intellisoft.nndak.R
import com.intellisoft.nndak.models.*
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel.Companion.createConditionItem
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel.Companion.createObservationItem
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.*


class EncounterDetailsViewModel(
    application: Application,
    private val fhirEngine: FhirEngine,
    private val encounterId: String
) : AndroidViewModel(application) {

    val liveEncounterData = MutableLiveData<List<PatientDetailData>>()
    val context: Application = application

    fun loadObservations(code: String) {

        viewModelScope.launch { liveEncounterData.value = getEncounterDataModel(context, code) }
    }

    private suspend fun getEncounterDataModel(
        context: Application,
        code: String
    ): List<PatientDetailData> {
        val data = mutableListOf<PatientDetailData>()
        val observations = getObservationsPerEncounter(context, code)
        val conditions = getConditionsPerEncounter(context, code)

        if (observations.isNotEmpty()) {

           // data.add(PatientDetailHeader(getString(R.string.header_observation)))


            val observationDataModel =
                observations.mapIndexed { index, observationItem ->

                    PatientDetailObservation(
                        observationItem,
                        firstInGroup = index == 0,
                        lastInGroup = index == observations.size - 1
                    )

                }

            data.addAll(observationDataModel)

        }

        if (conditions.isNotEmpty()) {
            //data.add(PatientDetailHeader(getString(R.string.header_conditions)))
            val conditionDataModel =
                conditions.mapIndexed { index, conditionItem ->
                    PatientDetailCondition(
                        conditionItem,
                        firstInGroup = index == 0,
                        lastInGroup = index == conditions.size - 1
                    )
                }
            data.addAll(conditionDataModel)
        }
        return data
    }


    private suspend fun getConditionsPerEncounter(
        context: Application,
        code: String
    ): List<ConditionItem> {
        val conditions: MutableList<ConditionItem> = mutableListOf()
        fhirEngine
            .search<Condition> { filter(Condition.ENCOUNTER, { value = "Encounter/$encounterId" }) }
            .take(MAX_RESOURCE_COUNT)
            .map {
                createConditionItem(
                    it,
                    getApplication<Application>().resources
                )
            }
            .let { conditions.addAll(it) }
        return conditions
    }


    private suspend fun getObservationsPerEncounter(
        context: Application,
        code: String
    ): List<ObservationItem> {
        val conditions: MutableList<ObservationItem> = mutableListOf()
        fhirEngine
            .search<Observation> {
                filter(
                    Observation.ENCOUNTER,
                    { value = "Encounter/$encounterId" })
            }
            .take(MAX_RESOURCE_COUNT)
            .map {
                createObservationItem(
                    it,
                    getApplication<Application>().resources
                )
            }
            .let { conditions.addAll(it) }
        return conditions
    }


}


class EncounterDetailsViewModelFactory(
    private val application: Application,
    private val fhirEngine: FhirEngine,
    private val encounterId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(EncounterDetailsViewModel::class.java)) {
            "Unknown ViewModel class"
        }
        return EncounterDetailsViewModel(application, fhirEngine, encounterId) as T
    }


}