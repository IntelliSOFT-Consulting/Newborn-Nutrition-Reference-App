package com.intellisoft.nndak.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.intellisoft.nndak.R
import com.intellisoft.nndak.databinding.FeedingCuesDialogBinding
import com.intellisoft.nndak.viewmodels.ScreenerViewModel
import timber.log.Timber

class FeedingCuesDialog(
    private val proceed: () -> Unit,
) : DialogFragment() {
    private var _binding: FeedingCuesDialogBinding? = null
    private val viewModel: ScreenerViewModel by viewModels()
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FeedingCuesDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.setCancelable(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeResourcesSaveAction()
      //  if (savedInstanceState == null) {
            //addQuestionnaireFragment()
        //}
        binding.apply {
            btnSubmit.setOnClickListener { proceed() }
        }

    }
    fun newInstance(questionnaire: String) = FeedingCuesDialog.apply {
        arguments = Bundle().apply {
            putString(QUESTIONNAIRE_FILE_PATH_KEY, questionnaire)
        }
    }

    private fun addQuestionnaireFragment() {
        try {
            val fragment = QuestionnaireFragment()
            fragment.arguments =
                bundleOf(QuestionnaireFragment.EXTRA_QUESTIONNAIRE_JSON_STRING to viewModel.questionnaire)
            childFragmentManager.commit {
                add(
                    R.id.breast_feeding_container, fragment,
                    QUESTIONNAIRE_FRAGMENT_TAG
                )
            }
        } catch (e: Exception) {
            Timber.e("Exception ${e.localizedMessage}")
        }
    }

    private fun observeResourcesSaveAction() {

    }

    companion object {
        const val QUESTIONNAIRE_FILE_PATH_KEY = "questionnaire-file-path-key"
        const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire-fragment-tag"
    }
}