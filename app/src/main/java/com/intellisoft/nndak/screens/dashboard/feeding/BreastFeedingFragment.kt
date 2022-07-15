package com.intellisoft.nndak.screens.dashboard.feeding

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ca.uhn.fhir.context.FhirContext
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.databinding.FragmentBreastFeedingBinding
import com.intellisoft.nndak.dialogs.ConfirmationDialog
import com.intellisoft.nndak.dialogs.FeedingCuesDialog
import com.intellisoft.nndak.models.CodingObservation
import com.intellisoft.nndak.models.FeedingCuesTips
import com.intellisoft.nndak.viewmodels.ScreenerViewModel
import kotlinx.android.synthetic.main.activity_login.*
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
 * Use the [BreastFeedingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BreastFeedingFragment : Fragment() {
    private lateinit var feedingCues: FeedingCuesDialog
    private lateinit var confirmationDialog: ConfirmationDialog
    private lateinit var breastFeeding: String
    private lateinit var efficientFeeding: String
    private var exit: Boolean = true
    private lateinit var effectiveExpression: String
    private lateinit var expressedSufficient: String
    private var _binding: FragmentBreastFeedingBinding? = null
    private val viewModel: ScreenerViewModel by viewModels()
    private val args: BreastFeedingFragmentArgs by navArgs()
    private val feedingCuesList = ArrayList<CodingObservation>()
    private val binding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = arguments

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBreastFeedingBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = resources.getString(R.string.action_provide_support)
            setHomeAsUpIndicator(R.drawable.dash)
            setDisplayHomeAsUpEnabled(true)
        }
        updateArguments()
        onBackPressed()
        observeResourcesSaveAction()
        if (savedInstanceState == null) {
            addQuestionnaireFragment()
        }
        setHasOptionsMenu(true)

        checkMothersStatus(args.contra)

        promptQues()

        binding.apply {

            actionMilkExpression.setOnClickListener {
                exit = true
                effectiveExpression = if (rbMotherYes.isChecked) {
                    "Yes"
                } else {
                    "No"
                }

                expressedSufficient = if (rbVolumeYes.isChecked) {
                    "Yes"
                } else {
                    "No"
                }
                completeMilkExpression(effectiveExpression, expressedSufficient)
            }
            actionAssess.setOnClickListener {
                exit = false
                handleShowCues()
            }
            actionBreastFeeding.setOnClickListener {
                exit = true
                breastFeeding = if (rbYes.isChecked) {
                    "Yes"
                } else {
                    "No"
                }
                efficientFeeding = if (rbEfiYes.isChecked) {
                    "Yes"
                } else {
                    "No"
                }
                completeBreastfeedingAssessment(breastFeeding, efficientFeeding)

            }

        }

        confirmationDialog = ConfirmationDialog(
            this::okClick,
            resources.getString(R.string.app_okay_message)
        )


    }

    private fun checkMothersStatus(status: String) {
        binding.apply {
            if (status == "Yes") {
                lnContra.visibility = View.VISIBLE
                actionMilkExpression.isEnabled = false
            } else {
                lnContra.visibility = View.GONE
                actionMilkExpression.isEnabled = true
            }
            FhirApplication.mumContra(requireContext(), status)
        }
    }

    private fun promptQues() {
        exit = false
        handleShowCues()
    }

    private fun completeMilkExpression(effectiveExpression: String, expressedSufficient: String) {
        (activity as MainActivity).displayDialog()

        CoroutineScope(Dispatchers.IO).launch {
            val questionnaireFragment =
                childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment

            val context = FhirContext.forR4()

            val questionnaire =
                context.newJsonParser()
                    .encodeResourceToString(questionnaireFragment.getQuestionnaireResponse())
            Timber.e("Questionnaire  $questionnaire")
            viewModel.milkExpressionAssessment(
                questionnaireFragment.getQuestionnaireResponse(),
                effectiveExpression,
                expressedSufficient,
                args.patientId
            )
        }
    }

    private fun completeBreastfeedingAssessment(breastFeeding: String, efficientFeeding: String) {
        (activity as MainActivity).displayDialog()

        CoroutineScope(Dispatchers.IO).launch {
            val questionnaireFragment =
                childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment

            viewModel.breastFeeding(
                questionnaireFragment.getQuestionnaireResponse(),
                breastFeeding,
                efficientFeeding,
                args.patientId
            )
        }
    }

    private fun handleShowCues() {
        feedingCues = FeedingCuesDialog(this::feedingCuesClick)
        feedingCues.show(childFragmentManager, "Feeding Cues")
    }

    private fun feedingCuesClick(cues: FeedingCuesTips) {
        feedingCues.dismiss()
        (activity as MainActivity).displayDialog()

        val questionnaireFragment =
            childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment

        val readiness =
            CodingObservation("Feeding-Readiness", "Feeding Readiness", cues.readiness)
        val latch = CodingObservation("Latch", "Latch", cues.latch)
        val steady = CodingObservation("Steady-Suck", "Steady Suck", cues.steady)
        val audible = CodingObservation("Audible-Swallow", "Audible Swallow", cues.audible)
        val chocking = CodingObservation("Chocking", "Chocking", cues.chocking)
        val softening =
            CodingObservation("Breast-Softening", "Breast Softening", cues.softening.toString())
        val tenSide =
            CodingObservation("10-Minutes-Side", "10 Minutes per Side", cues.tenSide.toString())
        val threeHours = CodingObservation("2-3-Hours", "2-3 Hours", cues.threeHours.toString())
        val sixDiapers =
            CodingObservation("6-8-Wet-Diapers", "6-8 Wet Diapers", cues.sixDiapers.toString())
        val contra = CodingObservation(
            "Mother-Contraindicated",
            "MotherContraindicated",
            cues.contra.toString()
        )
        updateArgs(contra)
        feedingCuesList.addAll(
            listOf(
                readiness,
                latch,
                steady,
                audible,
                chocking,
                softening,
                tenSide,
                threeHours,
                sixDiapers,
                contra
            )
        )

        viewModel.feedingCues(
            questionnaireFragment.getQuestionnaireResponse(),
            feedingCuesList,
            args.patientId,"Feeding Cues"
        )

    }


    private fun okClick() {
        confirmationDialog.dismiss()

    }


    private fun observeResourcesSaveAction() {
        viewModel.customMessage.observe(viewLifecycleOwner) {
            if (!it.success) {
                Toast.makeText(
                    requireContext(), it.message,
                    Toast.LENGTH_SHORT
                ).show()
                (activity as MainActivity).hideDialog()
                return@observe
            }
            (activity as MainActivity).hideDialog()
            val dialog = SweetAlertDialog(requireContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText("Success")
                .setContentText(resources.getString(R.string.app_okay_saved))
                .setCustomImage(R.drawable.smile)
                .setConfirmClickListener { sDialog ->
                    run {
                        sDialog.dismiss()
                        if (exit) {
                            try {
                                findNavController().navigateUp()
                            } catch (e: Exception) {
                            }
                        }
                    }
                }
            dialog.setCancelable(false)
            dialog.show()
        }

    }

    private fun updateArguments() {
        requireArguments().putString(QUESTIONNAIRE_FILE_PATH_KEY, "breast-feeding.json")
    }

    private fun updateArgs(contra: CodingObservation) {
        if (contra.value == "Yes") {
            exit = true
        } else {
            checkMothersStatus(contra.value)
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

    private fun onSubmitAction() {

        confirmationDialog.show(childFragmentManager, "Confirm Details")

    }

    private fun showCancelScreenerQuestionnaireAlertDialog() {

        SweetAlertDialog(activity, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Are you sure?")
            .setContentText(getString(R.string.cancel_questionnaire_message))
            .setConfirmText("Yes")
            .setConfirmClickListener { d ->
                d.dismiss()
                NavHostFragment.findNavController(this@BreastFeedingFragment).navigateUp()
            }
            .setCancelText("No")
            .show()
    }

    private fun onBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            showCancelScreenerQuestionnaireAlertDialog()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.hidden_menu, menu)
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