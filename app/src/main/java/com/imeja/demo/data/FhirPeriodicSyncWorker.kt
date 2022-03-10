
package com.imeja.demo.data

import android.content.Context
import androidx.work.WorkerParameters
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.sync.FhirSyncWorker
import com.imeja.demo.FhirApplication
import com.imeja.demo.USER_ADDRESS
import com.imeja.demo.api.HapiFhirService
import org.hl7.fhir.r4.model.ResourceType

class FhirPeriodicSyncWorker(appContext: Context, workerParams: WorkerParameters) :
    FhirSyncWorker(appContext, workerParams) {

    override fun getSyncData() = mapOf(ResourceType.Patient to mapOf("address-city" to USER_ADDRESS))

    override fun getDataSource() =
        HapiFhirResourceDataSource(HapiFhirService.create(FhirContext.forR4().newJsonParser()))

    override fun getFhirEngine() = FhirApplication.fhirEngine(applicationContext)
}