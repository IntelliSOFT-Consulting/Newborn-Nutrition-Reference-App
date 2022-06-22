package com.intellisoft.nndak.screens.custom

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.FOCUS_DOWN
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.NumberPicker
import androidx.core.widget.doAfterTextChanged
import com.google.android.fhir.datacapture.validation.ValidationResult
import com.google.android.fhir.datacapture.validation.getSingleStringValidationMessage
import com.google.android.fhir.datacapture.views.QuestionnaireItemViewHolderDelegate
import com.google.android.fhir.datacapture.views.QuestionnaireItemViewHolderFactory
import com.google.android.fhir.datacapture.views.QuestionnaireItemViewItem
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.intellisoft.nndak.R
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.StringType

object CustomNumberPickerFactory : QuestionnaireItemViewHolderFactory(R.layout.custom_layout) {
    override fun getQuestionnaireItemViewHolderDelegate(): QuestionnaireItemViewHolderDelegate =
        object : QuestionnaireItemViewHolderDelegate {
            private lateinit var numberPicker: NumberPicker
            override lateinit var questionnaireItemViewItem: QuestionnaireItemViewItem

            override fun init(itemView: View) {
                /**
                 * Call the [QuestionnaireItemViewHolderDelegate.onAnswerChanged] function when the widget
                 * is interacted with and answer is changed by user input
                 */
                numberPicker = itemView.findViewById(R.id.number_picker)
            }

            override fun bind(questionnaireItemViewItem: QuestionnaireItemViewItem) {
                numberPicker.minValue = 1
                numberPicker.maxValue = 100
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
/*

object CustomInputFieldFactory :
    QuestionnaireItemViewHolderFactory(R.layout.custom_edittext_layout) {
    override fun getQuestionnaireItemViewHolderDelegate(): QuestionnaireItemViewHolderDelegate =
        object : QuestionnaireItemViewHolderDelegate {
            private lateinit var header: QuestionnaireItemHeaderView
            private lateinit var textInputLayout: TextInputLayout
            private lateinit var textInputEditText: TextInputEditText
            override lateinit var questionnaireItemViewItem: QuestionnaireItemViewItem
            private var textWatcher: TextWatcher? = null

            override fun init(itemView: View) {
                header = itemView.findViewById(R.id.header)
                textInputLayout = itemView.findViewById(R.id.text_input_layout)
                textInputEditText = itemView.findViewById(R.id.text_input_edit_text)
            }

            override fun bind(questionnaireItemViewItem: QuestionnaireItemViewItem) {
                header.bind(questionnaireItemViewItem.questionnaireItem)
                textInputLayout.hint = questionnaireItemViewItem.questionnaireItem.prefix
                textInputEditText.removeTextChangedListener(textWatcher)
                textInputEditText.setText(getText(questionnaireItemViewItem.singleAnswerOrNull))
                textInputEditText.setOnFocusChangeListener { view, focused ->
                    if (!focused) {
                        (view.context.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as
                                InputMethodManager)
                            .hideSoftInputFromWindow(view.windowToken, 0)
                    }
                }
                textInputEditText.setOnEditorActionListener { view, actionId, _ ->
                    if (actionId != EditorInfo.IME_ACTION_NEXT) {
                        false
                    }
                    view.focusSearch(FOCUS_DOWN)?.requestFocus(FOCUS_DOWN) ?: false
                }
                textWatcher =
                    textInputEditText.doAfterTextChanged { editable: Editable? ->
                        questionnaireItemViewItem.singleAnswerOrNull = getValue(editable.toString())
                        onAnswerChanged(textInputEditText.context)
                    }
            }

            override fun displayValidationResult(validationResult: ValidationResult) {
                // Custom validation message
                textInputLayout.error =
                    if (validationResult.getSingleStringValidationMessage() == "") null
                    else validationResult.getSingleStringValidationMessage()
            }

            override fun setReadOnly(isReadOnly: Boolean) {
                textInputEditText.isEnabled = !isReadOnly
            }

            fun getValue(
                text: String
            ): QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent? {
                return text.let {
                    if (it.isEmpty()) {
                        null
                    } else {
                        QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent().setValue(
                            StringType(it)
                        )
                    }
                }
            }

            fun getText(
                answer: QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent?
            ): String {
                return answer?.valueStringType?.value ?: ""
            }
        }


    const val WIDGET_EXTENSION = "http://dummy-widget-type-extension"
    const val WIDGET_TYPE = "number-picker"
}*/
