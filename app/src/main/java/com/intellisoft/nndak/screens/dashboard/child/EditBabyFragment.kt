package com.intellisoft.nndak.screens.dashboard.child

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.viewmodels.EditPatientViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class EditBabyFragment : Fragment(R.layout.fragment_edit_baby) {
    private val viewModel: EditPatientViewModel by viewModels()
    var submitMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = requireContext().getString(R.string.edit_patient)
        }
        requireArguments()
            .putString(QUESTIONNAIRE_FILE_PATH_KEY, "update.json")

        viewModel.livePatientData.observe(viewLifecycleOwner) {
            addQuestionnaireFragment(it)
            if (!it.toList().isNullOrEmpty()) {
                submitMenuItem?.isEnabled = true
            }
        }
        viewModel.isPatientSaved.observe(viewLifecycleOwner) {
            if (!it) {
                Toast.makeText(requireContext(), R.string.message_input_missing, Toast.LENGTH_SHORT).show()
                return@observe
            }
            Toast.makeText(requireContext(), R.string.message_patient_updated, Toast.LENGTH_SHORT).show()
            NavHostFragment.findNavController(this).navigateUp()
        }
        (activity as MainActivity).setDrawerEnabled(false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_patient_fragment_menu, menu)
        submitMenuItem = menu.findItem(R.id.action_edit_patient_submit)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavHostFragment.findNavController(this).navigateUp()
                true
            }
            R.id.action_edit_patient_submit -> {
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
        viewModel.updatePatient(questionnaireFragment.getQuestionnaireResponse())
    }

    companion object {
        const val QUESTIONNAIRE_FILE_PATH_KEY = "edit-questionnaire-file-path-key"
        const val QUESTIONNAIRE_FRAGMENT_TAG = "edit-questionnaire-fragment-tag"
    }
}