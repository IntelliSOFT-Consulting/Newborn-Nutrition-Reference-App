
package com.intellisoft.nndak.data

import android.content.Context
import androidx.work.WorkerParameters
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.sync.FhirSyncWorker
import com.intellisoft.nndak.CURRENT_ORGANIZATION
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.SYNC_PARAM
import com.intellisoft.nndak.USER_ADDRESS
import com.intellisoft.nndak.api.HapiFhirService
import org.hl7.fhir.r4.model.ResourceType

class FhirPeriodicSyncWorker(appContext: Context, workerParams: WorkerParameters) :
    FhirSyncWorker(appContext, workerParams) {

    override fun getSyncData() = mapOf(ResourceType.Patient to mapOf(SYNC_PARAM to CURRENT_ORGANIZATION))

    override fun getDataSource() =
        HapiFhirResourceDataSource(HapiFhirService.create(FhirContext.forR4().newJsonParser()))

    override fun getFhirEngine() = FhirApplication.fhirEngine(applicationContext)
}