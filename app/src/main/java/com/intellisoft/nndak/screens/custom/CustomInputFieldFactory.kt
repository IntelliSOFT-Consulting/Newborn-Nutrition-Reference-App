package com.intellisoft.nndak.screens.custom

import android.view.View
import com.google.android.fhir.datacapture.validation.ValidationResult
import com.google.android.fhir.datacapture.views.QuestionnaireItemViewHolderDelegate
import com.google.android.fhir.datacapture.views.QuestionnaireItemViewHolderFactory
import com.google.android.fhir.datacapture.views.QuestionnaireItemViewItem
import com.google.android.material.textfield.TextInputEditText
import com.intellisoft.nndak.R

object CustomInputFieldFactory :
    QuestionnaireItemViewHolderFactory(R.layout.custom_edittext_layout) {
    override fun getQuestionnaireItemViewHolderDelegate(): QuestionnaireItemViewHolderDelegate =
        object : QuestionnaireItemViewHolderDelegate {
            private lateinit var numberPicker: TextInputEditText
            override lateinit var questionnaireItemViewItem: QuestionnaireItemViewItem

            override fun init(itemView: View) {
                /**
                 * Call the [QuestionnaireItemViewHolderDelegate.onAnswerChanged] function when the widget
                 * is interacted with and answer is changed by user input
                 */
                numberPicker = itemView.findViewById(R.id.ed_deficit)
            }

            override fun bind(questionnaireItemViewItem: QuestionnaireItemViewItem) {
//                numberPicker.minValue = 1
//                numberPicker.maxValue = 100
            }

            override fun displayValidationResult(validationResult: ValidationResult) {
                // Custom validation message
            }

            override fun setReadOnly(isReadOnly: Boolean) {
                numberPicker.isEnabled = !isReadOnly
            }
        }

    const val WIDGET_EXTENSION = "http://dummy-widget-type-extension"
    const val WIDGET_TYPE = "number-picker"
}