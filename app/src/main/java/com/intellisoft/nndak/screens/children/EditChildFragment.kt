package com.intellisoft.nndak.screens.children

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.google.android.material.button.MaterialButton
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.viewmodels.EditPatientViewModel

class EditChildFragment : Fragment(R.layout.add_patient_fragment) {
    private val viewModel: EditPatientViewModel by viewModels()

    private lateinit var cancel: MaterialButton
    private lateinit var submit: MaterialButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = requireContext().getString(R.string.edit_child)
        }
        requireArguments()
            .putString(QUESTIONNAIRE_FILE_PATH_KEY, "child.json")

        viewModel.liveChildData.observe(viewLifecycleOwner) { addQuestionnaireFragment(it) }
        viewModel.isPatientSaved.observe(viewLifecycleOwner) {
            if (!it) {
                Toast.makeText(requireContext(), R.string.message_input_missing, Toast.LENGTH_SHORT)
                    .show()
                return@observe
            }
            Toast.makeText(requireContext(), R.string.message_patient_updated, Toast.LENGTH_SHORT)
                .show()
            NavHostFragment.findNavController(this).navigateUp()
        }
        (activity as MainActivity).setDrawerEnabled(false)
        initActions(view)
    }

    private fun initActions(view: View) {
        cancel = view.findViewById(R.id.btn_cancel)
        submit = view.findViewById(R.id.btn_submit)


        /***
         * Actions
         ***/
        cancel.setOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }
        submit.setOnClickListener {
            onSubmitAction()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.hidden_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavHostFragment.findNavController(this).navigateUp()
                true
            }
            R.id.action_add_patient_submit -> {
                onSubmitAction()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addQuestionnaireFragment(pair: Pair<String, String>) {
        val fragment = QuestionnaireFragment()
        fragment.arguments =
            bundleOf(
                QuestionnaireFragment.EXTRA_QUESTIONNAIRE_JSON_STRING to pair.first,
                QuestionnaireFragment.EXTRA_QUESTIONNAIRE_RESPONSE_JSON_STRING to pair.second
            )
        childFragmentManager.commit {
            add(R.id.add_patient_container, fragment, QUESTIONNAIRE_FRAGMENT_TAG)
        }
    }

    private fun onSubmitAction() {
        val questionnaireFragment =
            childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment
        viewModel.updateChild(questionnaireFragment.getQuestionnaireResponse())
    }

    companion object {
        const val QUESTIONNAIRE_FILE_PATH_KEY = "edit-questionnaire-file-path-key"
        const val QUESTIONNAIRE_FRAGMENT_TAG = "edit-questionnaire-fragment-tag"
    }
}