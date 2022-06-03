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
import com.intellisoft.nndak.databinding.FragmentBabyMonitoringBinding
import com.intellisoft.nndak.dialogs.ConfirmationDialog
import com.intellisoft.nndak.dialogs.SuccessDialog
import com.intellisoft.nndak.dialogs.TipsDialog
import com.intellisoft.nndak.models.FeedingCuesTips
import com.intellisoft.nndak.screens.dashboard.RegistrationFragmentDirections
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
 * Use the [BabyMonitoringFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BabyMonitoringFragment : Fragment() {
    private var _binding: FragmentBabyMonitoringBinding? = null
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val args: BabyMonitoringFragmentArgs by navArgs()
    private val viewModel: ScreenerViewModel by viewModels()
    private lateinit var feedingCues: TipsDialog
    private lateinit var confirmationDialog: ConfirmationDialog
    private lateinit var successDialog: SuccessDialog
    private val binding
        get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBabyMonitoringBinding.inflate(inflater, container, false)
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
        patientDetailsViewModel.getMumChild()
        patientDetailsViewModel.liveMumChild.observe(viewLifecycleOwner) { motherBabyItem ->

            if (motherBabyItem != null) {
                binding.apply {
                    val gest = motherBabyItem.dashboard.gestation ?: ""
                    val status = motherBabyItem.status
                    incDetails.tvBabyName.text = motherBabyItem.babyName
                    incDetails.tvMumName.text = motherBabyItem.motherName
                    incDetails.appBirthWeight.text = motherBabyItem.birthWeight
                    incDetails.appGestation.text = "$gest-$status"
                    incDetails.appApgarScore.text = motherBabyItem.dashboard.apgarScore ?: ""
                    incDetails.appMumIp.text = motherBabyItem.motherIp
                    incDetails.appBabyWell.text = motherBabyItem.dashboard.babyWell ?: ""
                    incDetails.appAsphyxia.text = motherBabyItem.dashboard.asphyxia ?: ""
                    incDetails.appNeonatalSepsis.text = motherBabyItem.dashboard.neonatalSepsis ?: ""
                    incDetails.appJaundice.text = motherBabyItem.dashboard.jaundice ?: ""
                    incDetails.appBirthDate.text = motherBabyItem.dashboard.dateOfBirth ?: ""
                    incDetails.appLifeDay.text = motherBabyItem.dashboard.dayOfLife ?: ""
                    incDetails.appAdmDate.text = motherBabyItem.dashboard.dateOfAdm ?: ""

                    /**
                     * Prescriptions
                     */
                    incPrescribe.appTodayTotal.text = motherBabyItem.dashboard.prescription.totalVolume ?: ""
                    incPrescribe.appRoute.text = motherBabyItem.dashboard.prescription.route ?: ""

                }
            }
        }
        binding.apply {
            screen.btnSubmit.setOnClickListener {
                confirmationDialog.show(childFragmentManager, "Confirm Action")
            }
            screen.btnCancel.setOnClickListener {
                findNavController().navigateUp()
            }
            actionClickTips.setOnClickListener {
                handleShowCues()
            }
        }

        confirmationDialog = ConfirmationDialog(
            this::okClick,
            resources.getString(R.string.app_okay_message)
        )
        successDialog = SuccessDialog(
            this::proceedClick, resources.getString(R.string.app_okay_saved)
        )

    }

    private fun handleShowCues() {
        feedingCues = TipsDialog(this::feedingCuesClick)
        feedingCues.show(childFragmentManager, "bundle")
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

    private fun showCancelScreenerQuestionnaireAlertDialog() {
        val alertDialog: AlertDialog? =
            activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setMessage(getString(R.string.cancel_questionnaire_message))
                    setPositiveButton(getString(android.R.string.yes)) { _, _ ->
                        NavHostFragment.findNavController(this@BabyMonitoringFragment).navigateUp()
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
            "baby-monitoring.json"
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun feedingCuesClick(cues: FeedingCuesTips) {
        feedingCues.dismiss()

        (activity as MainActivity).displayDialog()

        CoroutineScope(Dispatchers.IO).launch {
            val questionnaireFragment =
                childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment

            val context = FhirContext.forR4()

            val questionnaire =
                context.newJsonParser()
                    .encodeResourceToString(questionnaireFragment.getQuestionnaireResponse())
            Timber.e("Questionnaire  $questionnaire")
            viewModel.babyMonitoringCues(
                questionnaireFragment.getQuestionnaireResponse(), cues, args.patientId
            )
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

            viewModel.babyMonitoring(
                questionnaireFragment.getQuestionnaireResponse(), args.patientId
            )
        }
    }

    private fun proceedClick() {
        successDialog.dismiss()
        findNavController().navigateUp()

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