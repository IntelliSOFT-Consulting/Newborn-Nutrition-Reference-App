package com.intellisoft.nndak.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.fhir.sync.Sync
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.data.FhirPeriodicSyncWorker
import com.intellisoft.nndak.helper_class.QuestionnaireHelper
import com.intellisoft.nndak.logic.FeedValidator
import com.intellisoft.nndak.logic.Logics.Companion.FEEDS_TAKEN
import com.intellisoft.nndak.models.MessageItem
import com.intellisoft.nndak.utils.generateUuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Reference
import timber.log.Timber
import java.util.Date

/** Records a single feed for a baby and pushes it to the server immediately. */
class FeedEntryViewModel(application: Application) : AndroidViewModel(application) {

    private val fhirEngine = FhirApplication.fhirEngine(application.applicationContext)
    private val qh = QuestionnaireHelper()

    private val _status = MutableLiveData<MessageItem>()
    val status: LiveData<MessageItem>
        get() = _status

    private var isSyncing = false

    fun saveFeed(
        patientId: String,
        encounterId: String,
        feedType: String,
        volume: String,
        babyWeightGrams: Int?
    ) {
        val result = FeedValidator.validate(volume, feedType, babyWeightGrams)
        if (!result.isValid) {
            _status.value = MessageItem(success = false, message = result.message!!)
            return
        }

        val observation = qh.quantityQuestionnaire(
            FEEDS_TAKEN, feedType, "Feeds Taken", volume, "mls"
        )
        observation.id = generateUuid()
        observation.subject = Reference("Patient/$patientId")
        observation.encounter = Reference("Encounter/$encounterId")
        observation.issued = Date()

        viewModelScope.launch(Dispatchers.IO) {
            fhirEngine.create(observation)
        }

        syncNow()
        _status.value = MessageItem(success = true, message = "Feed saved and synced")
    }

    /** Uploads straight away instead of waiting for the 15 minute periodic sync. */
    private fun syncNow() {
        if (isSyncing) return
        isSyncing = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Sync.oneTimeSync<FhirPeriodicSyncWorker>(getApplication())
                Timber.d("Quick feed sync requested")
            } finally {
                isSyncing = false
            }
        }
    }
}