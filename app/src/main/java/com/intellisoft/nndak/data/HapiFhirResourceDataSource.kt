
package com.intellisoft.nndak.data

import com.google.android.fhir.sync.DataSource
import com.intellisoft.nndak.api.HapiFhirService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.OperationOutcome
import org.hl7.fhir.r4.model.Resource

/** Implementation of the [FhirDataSource] that communicates with hapi fhir. */
class HapiFhirResourceDataSource(private val service: HapiFhirService) : DataSource {

  override suspend fun loadData(path: String): Bundle {
    return service.getResource(path)
  }

  override suspend fun insert(resourceType: String, resourceId: String, payload: String): Resource {
    return service.insertResource(
      resourceType,
      resourceId,
      payload.toRequestBody("application/fhir+json".toMediaType())
    )
  }

  override suspend fun update(
    resourceType: String,
    resourceId: String,
    payload: String
  ): OperationOutcome {
    return service.updateResource(
      resourceType,
      resourceId,
      payload.toRequestBody("application/json-patch+json".toMediaType())
    )
  }

  override suspend fun delete(resourceType: String, resourceId: String): OperationOutcome {
    return service.deleteResource(resourceType, resourceId)
  }
}
