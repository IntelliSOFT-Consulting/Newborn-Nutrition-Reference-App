package com.intellisoft.nndak.widgets

import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.google.android.fhir.datacapture.contrib.views.barcode.QuestionnaireItemBarCodeReaderViewHolderFactory

class CustomQuestionnaireFragment : QuestionnaireFragment() {
    override fun getCustomQuestionnaireItemViewHolderFactoryMatchers():
            List<QuestionnaireItemViewHolderFactoryMatcher> {
        return listOf(
            QuestionnaireItemViewHolderFactoryMatcher(ThreeColumnWidget) { questionnaireItem ->
                questionnaireItem.getExtensionByUrl(ThreeColumnWidget.WIDGET_EXTENSION).let {
                    if (it == null) false else it.value.toString() == ThreeColumnWidget.WIDGET_TYPE
                }
            },
            QuestionnaireItemViewHolderFactoryMatcher(
                QuestionnaireItemBarCodeReaderViewHolderFactory
            ) {
                    questionnaireItem ->
                questionnaireItem.getExtensionByUrl(
                    QuestionnaireItemBarCodeReaderViewHolderFactory.WIDGET_EXTENSION
                )
                    .let {
                        if (it == null) false
                        else it.value.toString() == QuestionnaireItemBarCodeReaderViewHolderFactory.WIDGET_TYPE
                    }
            }
        )
    }
}