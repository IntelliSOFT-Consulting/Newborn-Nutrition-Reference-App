package com.intellisoft.nndak.screens.patients

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.intellisoft.nndak.viewmodels.AddPatientViewModel
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.helper_class.DbMotherKey
import com.intellisoft.nndak.helper_class.FormatHelper
import com.intellisoft.nndak.roomdb.HealthViewModel
import org.hl7.fhir.r4.model.QuestionnaireResponse


class AddPatientFragment : Fragment(R.layout.add_patient_fragment) {

    private val viewModel: AddPatientViewModel by viewModels()

    private val TAG = "AddPatientFragment"

    private lateinit var healthViewModel: HealthViewModel

    private val formatHelper = FormatHelper()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpActionBar()
        setHasOptionsMenu(true)
        updateArguments()
        if (savedInstanceState == null) {
            addQuestionnaireFragment()
        }
        observePatientSaveAction()

        healthViewModel = HealthViewModel(requireActivity().application)
        (activity as MainActivity).setDrawerEnabled(false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.add_patient_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_patient_submit -> {
                onSubmitAction()
                true
            }
            android.R.id.home -> {
                NavHostFragment.findNavController(this).navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setUpActionBar() {
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = requireContext().getString(R.string.add_patient)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onStart() {
        super.onStart()
        showDialog()
    }

    private fun showDialog(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        builder.setTitle("National ID Number")
        builder.setMessage("Provide the patient's National/Passport No.")
        val input = EditText(requireContext())

        input.hint = "ID Number (33745...)"
        input.inputType = InputType.TYPE_CLASS_NUMBER

        builder.setView(input)
        builder.setPositiveButton("OK") { _, _ ->
            val nationalId  = input.text.toString()
            if (!TextUtils.isEmpty(nationalId)){

                val natID = DbMotherKey.NATIONALID.name
                formatHelper.saveSharedPreference(requireContext(), natID, nationalId)

                Toast.makeText(requireContext(), "ID Number has been captured. Please proceed with the other fields.", Toast.LENGTH_SHORT).show()


            }else{
                input.error = "Field cannot be empty."
                Toast.makeText(requireContext(), "You cannot proceed without the ID Number.", Toast.LENGTH_SHORT).show()
            }

        }
        builder.setNegativeButton("Cancel") { dialog, _ ->

            Toast.makeText(requireContext(), "You cannot proceed without the ID Number.", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
//            activity?.finish()

//            dialog.cancel()

        }

        builder.show()
    }

    private fun updateArguments() {
        requireArguments()
            .putString(QUESTIONNAIRE_FILE_PATH_KEY, "new-patient-registration.json")
    }

    private fun addQuestionnaireFragment() {
        val fragment = QuestionnaireFragment()
        fragment.arguments =
            bundleOf(QuestionnaireFragment.EXTRA_QUESTIONNAIRE_JSON_STRING to viewModel.questionnaire)
        childFragmentManager.commit {
            add(R.id.add_patient_container, fragment, QUESTIONNAIRE_FRAGMENT_TAG)
        }
    }

    private fun onSubmitAction() {
        val questionnaireFragment =
            childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment

      //  val context=FhirContext.forR4()
        //Log.d(TAG,context.newJsonParser().encodeResourceToString(questionnaireFragment.getQuestionnaireResponse()))
        savePatient(questionnaireFragment.getQuestionnaireResponse())
    }

    private fun savePatient(questionnaireResponse: QuestionnaireResponse) {
        viewModel.savePatient(questionnaireResponse)
    }

    private fun observePatientSaveAction() {
        viewModel.isPatientSaved.observe(viewLifecycleOwner) {
            if (!it) {
                Toast.makeText(requireContext(), "Inputs are missing.", Toast.LENGTH_SHORT).show()
                return@observe
            }
            Toast.makeText(requireContext(), "Patient is saved.", Toast.LENGTH_SHORT).show()
            NavHostFragment.findNavController(this).navigateUp()
        }
    }

    companion object {
        const val QUESTIONNAIRE_FILE_PATH_KEY = "questionnaire-file-path-key"
        const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire-fragment-tag"
    }
}