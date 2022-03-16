package com.intellisoft.nndak.api

import ca.uhn.fhir.parser.IParser
import java.lang.reflect.Type
import okhttp3.ResponseBody
import org.hl7.fhir.r4.model.Resource
import retrofit2.Converter
import retrofit2.Retrofit

class FhirConverterFactory(private val parser: IParser) : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        return FhirConverter(parser)
    }
}

/** Retrofit converter that allows us to parse FHIR resources */
private class FhirConverter(private val parser: IParser) : Converter<ResponseBody, Resource> {
    override fun convert(value: ResponseBody): Resource {
        return parser.parseResource(value.string()) as Resource
    }
}
