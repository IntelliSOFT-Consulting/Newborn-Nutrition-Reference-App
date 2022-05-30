package com.intellisoft.nndak.screens.dashboard.child

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
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.databinding.FragmentBreastFeedingBinding
import com.intellisoft.nndak.databinding.FragmentRegistrationBinding
import com.intellisoft.nndak.dialogs.ConfirmationDialog
import com.intellisoft.nndak.dialogs.FeedingCuesDialog
import com.intellisoft.nndak.dialogs.SuccessDialog
import com.intellisoft.nndak.screens.ScreenerFragment
import com.intellisoft.nndak.screens.dashboard.RegistrationFragment
import com.intellisoft.nndak.screens.dashboard.RegistrationFragmentDirections
import com.intellisoft.nndak.utils.generateUuid
import com.intellisoft.nndak.viewmodels.ScreenerViewModel
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
    private lateinit var successDialog: SuccessDialog
    private var _binding: FragmentBreastFeedingBinding? = null
    private val viewModel: ScreenerViewModel by viewModels()
    private val args: BreastFeedingFragmentArgs by navArgs()
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
        (activity as MainActivity).showBottom(false)
        binding.apply {

            actionAssess.setOnClickListener {
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
        feedingCues.newInstance("breast-feeding.json")
        feedingCues.show(childFragmentManager, "bundle")
    }

    private fun feedingCuesClick() {
        feedingCues.dismiss()
    }

    private fun okClick() {
        confirmationDialog.dismiss()
        val questionnaireFragment =
            childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment

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

    private fun observeResourcesSaveAction() {
        viewModel.isResourcesSaved.observe(viewLifecycleOwner) {
            if (!it) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.inputs_missing),
                    Toast.LENGTH_SHORT
                )
                    .show()
                return@observe
            }
            successDialog.show(childFragmentManager, "Success Details")
        }

    }

    private fun updateArguments() {
        requireArguments().putString(QUESTIONNAIRE_FILE_PATH_KEY, "breast-feeding.json")
    }

    private fun addQuestionnaireFragment() {
        try {
      /*      val fragment = QuestionnaireFragment()
            fragment.arguments =
                bundleOf(QuestionnaireFragment.EXTRA_QUESTIONNAIRE_JSON_STRING to viewModel.questionnaire)
            childFragmentManager.commit {
                add(
                    R.id.breast_feeding_container, fragment,
                    QUESTIONNAIRE_FRAGMENT_TAG
                )
            }*/
        } catch (e: Exception) {
            Timber.e("Exception ${e.localizedMessage}")
        }
    }

    private fun onSubmitAction() {

        confirmationDialog.show(childFragmentManager, "Confirm Details")

    }

    private fun showCancelScreenerQuestionnaireAlertDialog() {
        val alertDialog: AlertDialog? =
            activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setMessage(getString(R.string.cancel_questionnaire_message))
                    setPositiveButton(getString(android.R.string.yes)) { _, _ ->
                        NavHostFragment.findNavController(this@BreastFeedingFragment).navigateUp()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard_menu, menu)
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