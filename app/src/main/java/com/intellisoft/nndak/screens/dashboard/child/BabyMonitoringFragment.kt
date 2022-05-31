package com.intellisoft.nndak.screens.dashboard.child

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.databinding.FragmentBabyMonitoringBinding
import com.intellisoft.nndak.databinding.FragmentChildDashboardBinding
import com.intellisoft.nndak.dialogs.ConfirmationDialog
import com.intellisoft.nndak.dialogs.FeedingCuesDialog
import com.intellisoft.nndak.dialogs.SuccessDialog
import com.intellisoft.nndak.models.FeedingCues
import com.intellisoft.nndak.screens.dashboard.RegistrationFragmentDirections
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
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
    private val args: BabyAssessmentFragmentArgs by navArgs()
    private lateinit var feedingCues: FeedingCuesDialog
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
        patientDetailsViewModel.liveMumChild.observe(viewLifecycleOwner) {
            Timber.e("Mother Baby ${it.gainRate}")
            if (it != null) {
                binding.apply {
                    val gest = it.dashboard?.gestation ?: ""
                    val status = it.status
                    incDetails.tvBabyName.text = it.babyName
                    incDetails.tvMumName.text = it.motherName
                    incDetails.appBirthWeight.text = it.birthWeight
                    incDetails.appGestation.text = "$gest-$status"
                    incDetails.appApgarScore.text = it.dashboard?.apgarScore ?: ""
                    incDetails.appMumIp.text = it.motherIp
                    incDetails.appBabyWell.text = it.dashboard?.babyWell ?: ""
                    incDetails.appAsphyxia.text = it.dashboard?.asphyxia ?: ""
                    incDetails.appNeonatalSepsis.text = it.dashboard?.neonatalSepsis ?: ""
                    incDetails.appJaundice.text = it.dashboard?.jaundice ?: ""
                    incDetails.appBirthDate.text = it.dashboard?.dateOfBirth ?: ""
                    incDetails.appLifeDay.text = it.dashboard?.dayOfLife ?: ""
                    incDetails.appAdmDate.text = it.dashboard?.dateOfAdm ?: ""

                }
            }
        }
        binding.apply {
            actionClickTips.setOnClickListener {
                handleShowCues()
            }
        }

        confirmationDialog = ConfirmationDialog(
            this::okClick,
            resources.getString(R.string.app_confirm_message)
        )
        successDialog = SuccessDialog(
            this::proceedClick, resources.getString(R.string.app_client_registered)
        )

    }

    private fun handleShowCues() {
        feedingCues = FeedingCuesDialog(this::feedingCuesClick)
        feedingCues.show(childFragmentManager, "bundle")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun feedingCuesClick(cues: FeedingCues) {
        feedingCues.dismiss()
    }

    private fun okClick() {
        confirmationDialog.dismiss()
        val questionnaireFragment =
            childFragmentManager.findFragmentByTag(BreastFeedingFragment.QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment

        val context = FhirContext.forR4()

        val questionnaire =
            context.newJsonParser()
                .encodeResourceToString(questionnaireFragment.getQuestionnaireResponse())
        Timber.e("Questionnaire  $questionnaire")

        /*  viewModel.clientRegistration(
              questionnaireFragment.getQuestionnaireResponse(), patientId
          )*/
    }

    private fun proceedClick() {
        successDialog.dismiss()
        findNavController().navigate(
            RegistrationFragmentDirections.navigateToBabyDashboard(
                args.patientId, false
            )
        )
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
}