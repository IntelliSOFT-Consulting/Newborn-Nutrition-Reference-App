package com.intellisoft.nndak.screens.dashboard.child

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.databinding.FragmentBabyAssessmentBinding
import com.intellisoft.nndak.dialogs.ConfirmationDialog
import com.intellisoft.nndak.dialogs.SuccessDialog
import com.intellisoft.nndak.screens.dashboard.BaseFragment
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import com.intellisoft.nndak.viewmodels.ScreenerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BabyAssessmentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BabyAssessmentFragment : Fragment() {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val args: BabyAssessmentFragmentArgs by navArgs()
    private val viewModel: ScreenerViewModel by viewModels()
    private lateinit var confirmationDialog: ConfirmationDialog
    private lateinit var successDialog: SuccessDialog
    private var _binding: FragmentBabyAssessmentBinding? = null
    private val binding
        get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBabyAssessmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = resources.getString(R.string.app_dashboard)
            setHomeAsUpIndicator(R.drawable.dash)
            setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)
        (activity as MainActivity).setDrawerEnabled(true)

        updateArguments()
        onBackPressed()
        observeResourcesSaveAction()
        if (savedInstanceState == null) {
            addQuestionnaireFragment()
        }

        fhirEngine = FhirApplication.fhirEngine(requireContext())
        patientDetailsViewModel =
            ViewModelProvider(
                this,
                PatientDetailsViewModelFactory(
                    requireActivity().application,
                    fhirEngine,
                    args.patientId
                )
            )
                .get(PatientDetailsViewModel::class.java)

        confirmationDialog = ConfirmationDialog(
            this::okClick,
            resources.getString(R.string.app_okay_message)
        )
        successDialog = SuccessDialog(
            this::proceedClick,
            resources.getString(R.string.app_okay_saved),false
        )
        patientDetailsViewModel.getMumChild()
        patientDetailsViewModel.liveMumChild.observe(viewLifecycleOwner) { data ->

            if (data != null) {

                binding.apply {
                    val gest = data.dashboard.gestation ?: ""
                    val status = data.status
                    incDetails.tvBabyName.text = data.babyName
                    incDetails.tvMumName.text = data.motherName
                    incDetails.appBirthWeight.text = data.birthWeight
                    incDetails.appGestation.text = "$gest-$status"
                    incDetails.appApgarScore.text = data.dashboard.apgarScore ?: ""
                    incDetails.appMumIp.text = data.motherIp
                    incDetails.appBabyWell.text = data.dashboard.babyWell ?: ""

                    incDetails.appBirthDate.text = data.dashboard.dateOfBirth ?: ""
                    incDetails.appLifeDay.text = data.dashboard.dayOfLife ?: ""
                    incDetails.appAdmDate.text = data.dashboard.dateOfAdm ?: ""

                    incDetails.appNeonatalSepsis.text = data.dashboard.neonatalSepsis ?: ""
                    incDetails.appJaundice.text = data.dashboard.jaundice ?: ""
                    incDetails.appAsphyxia.text = data.dashboard.asphyxia ?: ""


                    val isSepsis = data.dashboard.neonatalSepsis
                    if (isSepsis != "Yes") {
                        incDetails.appNeonatalSepsis.visibility = View.GONE
                        incDetails.tvNeonatalSepsis.visibility = View.GONE
                    }

                    val isAsphyxia = data.dashboard.asphyxia
                    if (isAsphyxia != "Yes") {
                        incDetails.appAsphyxia.visibility = View.GONE
                        incDetails.tvAsphyxia.visibility = View.GONE
                    }

                    val isJaundice = data.dashboard.jaundice
                    if (isJaundice != "Yes") {
                        incDetails.tvJaundice.visibility = View.GONE
                        incDetails.appJaundice.visibility = View.GONE
                    }


                }
            }
        }

        binding.apply {
            btnCancel.setOnClickListener {
                findNavController().navigateUp()
            }
            btnSubmit.setOnClickListener {
                onSubmitAction()
            }
        }

    }

    private fun okClick() {
        confirmationDialog.dismiss()
        (activity as MainActivity).displayDialog()

        CoroutineScope(Dispatchers.IO).launch {
            val questionnaireFragment =
                childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment

            val context = FhirContext.forR4()

            val questionnaire =
                context.newJsonParser()
                    .encodeResourceToString(questionnaireFragment.getQuestionnaireResponse())
            Timber.e("Questionnaire  $questionnaire")
            viewModel.completeAssessment(
                questionnaireFragment.getQuestionnaireResponse(), args.patientId
            )
        }
    }

    private fun proceedClick() {
        successDialog.dismiss()
        FhirApplication.setDashboardActive(requireContext(), true)
        findNavController().navigateUp()
    }

    private fun addQuestionnaireFragment() {
        try {
            val fragment = QuestionnaireFragment()
            fragment.arguments =
                bundleOf(QuestionnaireFragment.EXTRA_QUESTIONNAIRE_JSON_STRING to viewModel.questionnaire)
            childFragmentManager.commit {
                add(
                    R.id.add_patient_container, fragment,
                    QUESTIONNAIRE_FRAGMENT_TAG
                )
            }
        } catch (e: Exception) {
            Timber.e("Exception ${e.localizedMessage}")
        }
    }

    private fun onSubmitAction() {

        confirmationDialog.show(childFragmentManager, "Confirm Details")

    }

    private fun observeResourcesSaveAction() {
        viewModel.isResourcesSaved.observe(viewLifecycleOwner) {
            if (!it) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.inputs_missing),
                    Toast.LENGTH_SHORT
                )
                    .show()
                (activity as MainActivity).hideDialog()
                return@observe
            }
            (activity as MainActivity).hideDialog()
            successDialog.show(childFragmentManager, "Success Details")
        }


    }

    private fun showCancelScreenerQuestionnaireAlertDialog() {
        val alertDialog: AlertDialog? =
            activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setMessage(getString(R.string.cancel_questionnaire_message))
                    setPositiveButton(getString(android.R.string.yes)) { _, _ ->
                        NavHostFragment.findNavController(this@BabyAssessmentFragment)
                            .navigateUp()
                    }
                    setNegativeButton(getString(android.R.string.no)) { _, _ -> }
                }
                builder.create()
            }
        alertDialog?.show()
    }

    private fun onBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            showCancelScreenerQuestionnaireAlertDialog()
        }
    }

    private fun updateArguments() {
        requireArguments().putString(
            QUESTIONNAIRE_FILE_PATH_KEY,
            "baby-assessment.json"
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                (requireActivity() as MainActivity).openNavigationDrawer()
                true
            }
            else -> false
        }
    }

    companion object {
        const val QUESTIONNAIRE_FILE_PATH_KEY = "questionnaire-file-path-key"
        const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire-fragment-tag"
    }
}