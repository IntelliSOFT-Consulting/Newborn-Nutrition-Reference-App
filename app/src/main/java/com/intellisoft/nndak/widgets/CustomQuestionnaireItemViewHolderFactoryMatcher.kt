package com.intellisoft.nndak.widgets

import org.hl7.fhir.r4.model.Questionnaire

data class CustomQuestionnaireItemViewHolderFactoryMatcher(
    val factory: ThreeColumnWidget,
    val matches: (Questionnaire.QuestionnaireItemComponent) -> Boolean
)