package com.intellisoft.nndak.widgets

import android.view.View
import com.google.android.fhir.datacapture.validation.ValidationResult
import com.google.android.fhir.datacapture.views.QuestionnaireItemViewHolderDelegate
import com.google.android.fhir.datacapture.views.QuestionnaireItemViewHolderFactory
import com.google.android.fhir.datacapture.views.QuestionnaireItemViewItem
import com.google.android.material.textfield.TextInputEditText
import com.intellisoft.nndak.R

object ThreeColumnWidget :
    QuestionnaireItemViewHolderFactory(R.layout.horizontal_item_three) {
    override fun getQuestionnaireItemViewHolderDelegate(): QuestionnaireItemViewHolderDelegate =
        object : QuestionnaireItemViewHolderDelegate {
           // private lateinit var holder: QuestionnaireItemHeaderView
            private lateinit var inputOne: TextInputEditText
            private lateinit var inputTwo: TextInputEditText
            private lateinit var inputThree: TextInputEditText
            override lateinit var questionnaireItemViewItem: QuestionnaireItemViewItem

            override fun init(itemView: View) {
                /**
                 * Call the [QuestionnaireItemViewHolderDelegate.onAnswerChanged] function when the widget
                 * is interacted with and answer is changed by user input
                 */
                inputOne = itemView.findViewById(R.id.text_input_edit_text)
                inputTwo = itemView.findViewById(R.id.text_input_edit_text_two)
                inputThree = itemView.findViewById(R.id.text_input_edit_text_three)

            }

            override fun bind(questionnaireItemViewItem: QuestionnaireItemViewItem) {
                val item = questionnaireItemViewItem.questionnaireItem

            }

            override fun displayValidationResult(validationResult: ValidationResult) {
                // Custom validation message
            }

            override fun setReadOnly(isReadOnly: Boolean) {
                inputOne.isEnabled = !isReadOnly
                inputTwo.isEnabled = !isReadOnly
                inputThree.isEnabled = !isReadOnly
            }
        }

    const val WIDGET_EXTENSION = "http://dummy-widget-type-extension"
    const val WIDGET_TYPE = "string"
}