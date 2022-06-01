package com.intellisoft.nndak.helper_class

import org.hl7.fhir.r4.model.NutritionOrder
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Quantity
import org.hl7.fhir.r4.model.Questionnaire

class QuestionnaireHelper {

    fun nutritionResource(code:String){
        val nutrition= NutritionOrder()
        nutrition.text
    }

    fun codingQuestionnaire(code: String,
                            display: String,
                            text: String):
            Observation {
        val observation = Observation()
        observation
            .code
            .addCoding()
            .setSystem("http://snomed.info/sct")
            .setCode(code).display = display
        observation.code.text = text
        return observation
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