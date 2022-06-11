package com.intellisoft.nndak.screens.custom

import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.google.android.fhir.datacapture.contrib.views.barcode.QuestionnaireItemBarCodeReaderViewHolderFactory

class CustomQuestionnaireFragment : QuestionnaireFragment() {
    override fun getCustomQuestionnaireItemViewHolderFactoryMatchers():
            List<QuestionnaireItemViewHolderFactoryMatcher> {
        return listOf(
            QuestionnaireItemViewHolderFactoryMatcher(CustomInputFieldFactory) { questionnaireItem ->
                questionnaireItem.getExtensionByUrl(CustomInputFieldFactory.WIDGET_EXTENSION).let {
                    if (it == null) false else it.value.toString() == CustomInputFieldFactory.WIDGET_TYPE
                }
            },
            QuestionnaireItemViewHolderFactoryMatcher(
                QuestionnaireItemBarCodeReaderViewHolderFactory
            ) { questionnaireItem ->
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