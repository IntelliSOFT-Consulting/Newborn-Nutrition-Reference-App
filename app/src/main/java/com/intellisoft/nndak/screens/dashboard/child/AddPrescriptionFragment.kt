package com.intellisoft.nndak.screens.dashboard.child

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.screens.ScreenerFragmentArgs
import com.intellisoft.nndak.utils.Constants
import com.intellisoft.nndak.viewmodels.ScreenerViewModel
import timber.log.Timber

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddPrescriptionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddPrescriptionFragment : Fragment(R.layout.fragment_add_prescription) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpActionBar()
        setHasOptionsMenu(true)
        onBackPressed()

        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = "Add Prescription"

            setDisplayHomeAsUpEnabled(true)
        }
        (activity as MainActivity).setDrawerEnabled(false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.hidden_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
//            R.id.action_add_patient_submit -> {
//                onSubmitAction()
//                true
//            }
//            android.R.id.home -> {
//                showCancelScreenerQuestionnaireAlertDialog()
//                true
//            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setUpActionBar() {
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun onBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }
    }


    companion object {
        const val QUESTIONNAIRE_FILE_PATH_KEY = "questionnaire-file-path-key"
        const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire-fragment-tag"
    }
}