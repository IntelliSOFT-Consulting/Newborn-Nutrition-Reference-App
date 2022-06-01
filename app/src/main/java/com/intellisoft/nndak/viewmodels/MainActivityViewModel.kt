
package com.intellisoft.nndak.viewmodels

import android.app.Application
import android.os.Build
import android.text.format.DateFormat
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import com.google.android.fhir.sync.PeriodicSyncConfiguration
import com.google.android.fhir.sync.RepeatInterval
import com.google.android.fhir.sync.State
import com.google.android.fhir.sync.Sync
import com.intellisoft.nndak.data.FhirPeriodicSyncWorker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/** View model for [MainActivity]. */
class MainActivityViewModel(application: Application, private val state: SavedStateHandle) :
  AndroidViewModel(application) {
  private val _lastSyncTimestampLiveData = MutableLiveData<String>()
  val lastSyncTimestampLiveData: LiveData<String>
    get() = _lastSyncTimestampLiveData

  private val job = Sync.basicSyncJob(application.applicationContext)
  private val _pollState = MutableSharedFlow<State>()
  val pollState: Flow<State>
    get() = _pollState

  init {
    poll()
  }

  /** Requests periodic sync. */
  @OptIn(ExperimentalCoroutinesApi::class)
  fun poll() {
    viewModelScope.launch {
      job.poll(
          PeriodicSyncConfiguration(
            syncConstraints = Constraints.Builder().build(),
            repeat = RepeatInterval(interval = 15, timeUnit = TimeUnit.MINUTES)
          ),
          FhirPeriodicSyncWorker::class.java
        )
        .collect { _pollState.emit(it) }
    }
  }

  /** Emits last sync time. */
  @RequiresApi(Build.VERSION_CODES.O)
  fun updateLastSyncTimestamp() {
    val formatter =
      DateTimeFormatter.ofPattern(
        if (DateFormat.is24HourFormat(getApplication())) formatString24 else formatString12
      )
    _lastSyncTimestampLiveData.value =
      job.lastSyncTimestamp()?.toLocalDateTime()?.format(formatter) ?: ""
  }

  companion object {
    private const val formatString24 = "yyyy-MM-dd HH:mm:ss"
    private const val formatString12 = "yyyy-MM-dd hh:mm:ss a"
  }
}
