package com.intellisoft.nndak.screens.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.intellisoft.nndak.R
import org.hl7.fhir.r4.model.Questionnaire

/** View for the prefix, question, and hint of a questionnaire item. */
internal class QuestionnaireItemHeaderView(context: Context, attrs: AttributeSet?) :
    LinearLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.questionnaire_item_header, this, true)
    }

    private var prefix: TextView = findViewById(R.id.prefix)
    private var question: TextView = findViewById(R.id.question)
    private var hint: TextView = findViewById(R.id.hint)

    fun bind(questionnaireItem: Questionnaire.QuestionnaireItemComponent) {
        val localizedPrefixSpanned = questionnaireItem.prefix
        prefix.visibility =
            if (localizedPrefixSpanned.isNullOrEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }
        prefix.text = localizedPrefixSpanned

        val localizedTextSpanned =questionnaireItem.text
        question.visibility =
            if (localizedTextSpanned.isNullOrEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }
        question.text = localizedTextSpanned

        val localizedHintSpanned = questionnaireItem.text
        hint.visibility =
            if (localizedHintSpanned.isNullOrEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }
        hint.text = localizedHintSpanned
        //   Make the entire view GONE if there is nothing to show. This is to avoid an empty row in the
        // questionnaire.
        visibility =
            if (question.visibility == VISIBLE ||
                prefix.visibility == VISIBLE ||
                hint.visibility == VISIBLE
            ) {
                VISIBLE
            } else {
                GONE
            }
    }
}
