
package com.intellisoft.nndak.data

import android.content.Context
import androidx.work.WorkerParameters
import com.google.android.fhir.sync.DownloadWorkManager
import com.google.android.fhir.sync.FhirSyncWorker
import com.intellisoft.nndak.FhirApplication

class FhirPeriodicSyncWorker(appContext: Context, workerParams: WorkerParameters) :
    FhirSyncWorker(appContext, workerParams) {

    override fun getDownloadWorkManager(): DownloadWorkManager {
        return DownloadManagerImpl()
    }

    override fun getFhirEngine() = FhirApplication.fhirEngine(applicationContext)
}