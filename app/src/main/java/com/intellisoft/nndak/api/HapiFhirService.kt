package com.intellisoft.nndak.api

import ca.uhn.fhir.parser.IParser
import com.intellisoft.nndak.data.User
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.OperationOutcome
import org.hl7.fhir.r4.model.Resource
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import retrofit2.Call

/** hapi.fhir.org API communication via Retrofit */
interface HapiFhirService {


  @Headers("Content-Type: application/json")
  @POST("users")
  fun loginUser(@Body userData: User): Call<User>

  @GET suspend fun getResource(@Url url: String): Bundle
  @PUT("{type}/{id}")
  suspend fun insertResource(
    @Path("type") type: String,
    @Path("id") id: String,
    @Body body: RequestBody
  ): Resource
  @PATCH("{type}/{id}")
  suspend fun updateResource(
    @Path("type") type: String,
    @Path("id") id: String,
    @Body body: RequestBody
  ): OperationOutcome
  @DELETE("{type}/{id}")
  suspend fun deleteResource(@Path("type") type: String, @Path("id") id: String): OperationOutcome

  @POST(".") suspend fun postData(@Body body: RequestBody): Resource

  companion object {
    const val BASE_URL = "https://hapi.fhir.org/baseR4/"

    fun create(parser: IParser): HapiFhirService {
      val logger = HttpLoggingInterceptor()
      logger.level = HttpLoggingInterceptor.Level.BODY

      val client = OkHttpClient.Builder().addInterceptor(logger).build()
      return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(FhirConverterFactory(parser))
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(HapiFhirService::class.java)
    }
  }
}