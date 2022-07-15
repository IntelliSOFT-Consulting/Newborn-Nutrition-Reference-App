package com.intellisoft.nndak.screens.dashboard.monitor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.adapters.ExpressionAdapter
import com.intellisoft.nndak.databinding.FragmentExpressionBinding
import com.intellisoft.nndak.dialogs.MoreExpression
import com.intellisoft.nndak.models.ExpressionHistory
import com.intellisoft.nndak.utils.boldText
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import com.intellisoft.nndak.viewmodels.ScreenerViewModel
import kotlinx.android.synthetic.main.fragment_baby_feeds.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


class ExpressionFragment : Fragment() {

    private lateinit var moreExpression: MoreExpression
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val viewModel: ScreenerViewModel by viewModels()
    private var _binding: FragmentExpressionBinding? = null
    lateinit var adapterList: ExpressionAdapter
    private lateinit var patientId: String
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpressionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = resources.getString(R.string.app_lactation_support)
            setDisplayHomeAsUpEnabled(true)
        }
        try {
            patientId = FhirApplication.getCurrentPatient(requireContext())
            fhirEngine = FhirApplication.fhirEngine(requireContext())
            patientDetailsViewModel =
                ViewModelProvider(
                    this,
                    PatientDetailsViewModelFactory(
                        requireActivity().application,
                        fhirEngine,
                        patientId
                    )
                )
                    .get(PatientDetailsViewModel::class.java)

            val recyclerView: RecyclerView = binding.patientList

            adapterList = ExpressionAdapter(this::handleClick)
            recyclerView.adapter = adapterList


            updateArguments()
            onBackPressed()
            observeResourcesSaveAction()
            if (savedInstanceState == null) {
                addQuestionnaireFragment()
            }

            binding.apply {
                lnCollection.visibility = View.GONE
                lnHistory.visibility = View.VISIBLE
                actionNewExpression.setOnClickListener {
                    actionNewExpression.visibility = View.GONE
                    lnCollection.visibility = View.VISIBLE
                    lnHistory.visibility = View.GONE
                }
                btnSubmit.setOnClickListener {
                    onSubmitAction()
                }
                btnCancel.setOnClickListener {
                    showCancelScreenerQuestionnaireAlertDialog()
                }
            }
            patientDetailsViewModel.getAssessmentExpressions()
            patientDetailsViewModel.liveAssessmentExpressions.observe(viewLifecycleOwner) { data ->

                if (data != null) {
                    adapterList.submitList(data)
                    binding.apply {
                        incTitle.tvhDate.text = getString(R.string._date)
                        incTitle.tvhFrequency.text = getString(R.string.exp_freq)
                        incTitle.tvhTiming.text = getString(R.string.timings)
                        incTitle.tvhView.visibility = View.INVISIBLE

                        val seven: ViewGroup.LayoutParams =
                            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.7f)
                        val three: ViewGroup.LayoutParams =
                            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.3f)
                        incTitle.lnParent.weightSum = 4f
                        incTitle.tvhDate.layoutParams = seven
                        incTitle.tvhFrequency.layoutParams = three
                        incTitle.tvhTiming.layoutParams = three
                        incTitle.tvhView.layoutParams = seven

                        boldText(incTitle.tvhDate)
                        boldText(incTitle.tvhFrequency)
                        boldText(incTitle.tvhTiming)
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        isEnabled = false
                        activity?.onBackPressed()

                    }
                }
            )

        binding.apply {


        }
        setHasOptionsMenu(true)
        (activity as MainActivity).setDrawerEnabled(true)

    }

    private fun handleClick(data: ExpressionHistory) {
        Timber.e("Clicked Item ${data.date}")
        moreExpression = MoreExpression(data)
        moreExpression.show(childFragmentManager, "Confirm Details")
    }


    private fun onSubmitAction() {

        (activity as MainActivity).displayDialog()
        CoroutineScope(Dispatchers.IO).launch {
            val questionnaireFragment =
                childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment

            viewModel.updateExpression(
                questionnaireFragment.getQuestionnaireResponse(), patientId
            )
        }
    }


    private fun showCancelScreenerQuestionnaireAlertDialog() {
        SweetAlertDialog(activity, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Are you sure?")
            .setContentText(getString(R.string.cancel_questionnaire_message))
            .setConfirmText("Yes")
            .setConfirmClickListener { d ->
                d.dismiss()
                resetDisplay()
            }
            .setCancelText("No")
            .show()
    }

    private fun onBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            showCancelScreenerQuestionnaireAlertDialog()
        }
    }

    private fun updateArguments() {
        arguments?.putString(QUESTIONNAIRE_FILE_PATH_KEY, "expression.json")
    }

    private fun addQuestionnaireFragment() {
        try {
            val fragment = QuestionnaireFragment()
            fragment.arguments =
                bundleOf(QuestionnaireFragment.EXTRA_QUESTIONNAIRE_JSON_STRING to viewModel.questionnaire)
            childFragmentManager.commit {
                replace(
                    R.id.add_patient_container, fragment,
                    QUESTIONNAIRE_FRAGMENT_TAG
                )
            }
        } catch (e: Exception) {
            Timber.e("Exception ${e.localizedMessage}")
        }
    }

    private fun observeResourcesSaveAction() {
        viewModel.customMessage.observe(viewLifecycleOwner) {
            if (!it.success) {
                Toast.makeText(
                    requireContext(), it.message,
                    Toast.LENGTH_SHORT
                )
                    .show()
                (activity as MainActivity).hideDialog()
                return@observe
            }
            (activity as MainActivity).hideDialog()
            SweetAlertDialog(requireContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText("Success")
                .setContentText(resources.getString(R.string.app_okay_saved))
                .setCustomImage(R.drawable.smile)
                .setConfirmClickListener { sDialog ->
                    run {
                        sDialog.dismiss()
                        resetDisplay()
                    }
                }
                .show()
        }
    }

    private fun resetDisplay() {
        updateArguments()
        binding.apply {
            actionNewExpression.visibility = View.VISIBLE
            lnHistory.visibility = View.VISIBLE
            lnCollection.visibility = View.GONE
            patientDetailsViewModel.getAssessmentExpressions()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val QUESTIONNAIRE_FILE_PATH_KEY = "questionnaire-file-path-key"
        const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire-fragment-tag"
    }

}


