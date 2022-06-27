package com.intellisoft.nndak.helper_class

import org.hl7.fhir.r4.model.*
import java.util.*

class QuestionnaireHelper {

    fun nutritionResource(code: String) {
        val nutrition = NutritionOrder()
        nutrition.text
    }

    fun codingQuestionnaire(
        code: String,
        display: String,
        text: String
    ):
            Observation {
        val observation = Observation()
        observation
            .code
            .addCoding()
            .setSystem("http://snomed.info/sct")
            .setCode(code).display = display
        observation.code.text = text
        observation.valueStringType.value = text
        return observation
    }

    fun codingTimeQuestionnaire(
        code: String,
        display: String,
        text: String
    ):
            Observation {
        val observation = Observation()
        observation
            .code
            .addCoding()
            .setSystem("http://snomed.info/sct")
            .setCode(code).display = display
        observation.code.text = text
      //  observation.valueDateTimeType.value = text
        return observation
    }

    fun order(
        generateUuid: String,
        encounterReference: Reference,
        subjectReference: Reference
    ): NutritionOrder {
        val no = NutritionOrder()
        no.id = generateUuid
        no.patient = subjectReference
        no.encounter = encounterReference
        no.status = NutritionOrder.NutritionOrderStatus.ACTIVE
        no.dateTime = Date()
        no.intent = NutritionOrder.NutritiionOrderIntent.ORDER

        return no
    }

    fun quantityQuestionnaire(
        code: String,
        display: String,
        text: String,
        quantity: String,
        units: String
    ): Observation {
        val observation = Observation()
        observation
            .code
            .addCoding()
            .setSystem("http://snomed.info/sct")
            .setCode(code).display = display
        observation.code.text = text
        observation.value = Quantity()
            .setValue(quantity.toBigDecimal())
            .setUnit(units)
            .setSystem("http://unitsofmeasure.org")
        return observation
    }


}