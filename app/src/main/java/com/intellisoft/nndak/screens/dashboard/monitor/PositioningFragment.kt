package com.intellisoft.nndak.screens.dashboard.monitor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.adapters.MonitoringAdapter
import com.intellisoft.nndak.adapters.PositioningAdapter
import com.intellisoft.nndak.databinding.FragmentPositioningBinding
import com.intellisoft.nndak.databinding.PositioningItemBinding
import com.intellisoft.nndak.dialogs.MoreExpression
import com.intellisoft.nndak.dialogs.ViewPositioning
import com.intellisoft.nndak.models.CodingObservation
import com.intellisoft.nndak.models.PositioningHistory
import com.intellisoft.nndak.utils.boldText
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import com.intellisoft.nndak.viewmodels.ScreenerViewModel
import kotlinx.android.synthetic.main.positioning_item.view.*
import kotlinx.android.synthetic.main.success_dialog.*
import timber.log.Timber

class PositioningFragment : Fragment() {
    private lateinit var moreExpression: ViewPositioning
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val viewModel: ScreenerViewModel by viewModels()
    private var _binding: FragmentPositioningBinding? = null
    private lateinit var patientId: String
    private val dataCodes = ArrayList<CodingObservation>()
    private var hands: String = "No"
    private var mum: String = "No"
    private var baby: String = "No"
    private var attach: String = "No"
    private var suck: String = "No"
    private lateinit var encounterId: String
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPositioningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = resources.getString(R.string.app_lactation_support)
            setDisplayHomeAsUpEnabled(true)
        }
        try {
            updateArguments()
            onBackPressed()
            if (savedInstanceState == null) {
                addQuestionnaireFragment()
            }

            encounterId = FhirApplication.getUpdatedCurrent(requireContext())
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

            updateDataFields()
            loadPositionAssessments()

            binding.apply {
                lnCollection.visibility = View.GONE
                lnHistory.visibility = View.VISIBLE
                actionNewExpression.setOnClickListener {
                    actionNewExpression.visibility = View.GONE
                    lnCollection.visibility = View.VISIBLE
                    lnHistory.visibility = View.GONE
                }
                btnSubmit.setOnClickListener {
                    updateDataFields()
                    onSubmitAction()
                }
                btnCancel.setOnClickListener {
                    showCancelScreenerQuestionnaireAlertDialog()
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

    private fun loadPositionAssessments() {
        patientDetailsViewModel.getPositioningHistory()
        patientDetailsViewModel.livePositioningHistory.observe(viewLifecycleOwner) { data ->

            if (data.isNotEmpty()) {

                val monitoringAdapter = PositioningAdapter(this::clickItem)
                binding.patientList.apply {
                    layoutManager =
                        LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.VERTICAL, false
                        )
                    adapter = monitoringAdapter

                }
                monitoringAdapter.submitList(data)
                monitoringAdapter.notifyDataSetChanged()


                binding.apply {
                    incTitle.lnParent.visibility = View.VISIBLE
                    incTitle.tvhDate.text = "Date"
                    incTitle.tvhFrequency.text = "Cleaned Hands"
                    incTitle.tvhTiming.text = "Mother Position"
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
            }else{
                binding.apply {

                }
            }
        }

    }

    private fun clickItem(data: PositioningHistory) {
        moreExpression = ViewPositioning(data)
        moreExpression.show(childFragmentManager, "Confirm Details")
    }

    private fun updateArguments() {
        requireArguments().putString(QUESTIONNAIRE_FILE_PATH_KEY, "breast-feeding.json")
    }

    private fun onBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            showCancelScreenerQuestionnaireAlertDialog()
        }
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

    private fun onSubmitAction() {
        val hands = CodingObservation("Hand-Wash", "Hands Washed", hands)
        val mum = CodingObservation("Mother-Position", "Mother Position", mum)
        val baby = CodingObservation("Baby-Position", "Baby Position", baby)
        val attach = CodingObservation("Good-Attachment", "Good Attachment", attach)
        val suck = CodingObservation("Effective-Suckling", "Effective Suckling", suck)
        dataCodes.addAll(
            listOf(
                hands, mum, baby, attach, suck
            )
        )
        (activity as MainActivity).displayDialog()
        observeResourcesSaveAction()

        val questionnaireFragment =
            childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment
        viewModel.feedingCues(
            questionnaireFragment.getQuestionnaireResponse(),
            dataCodes,encounterId,
            patientId, "Positioning-Assessment"
        )
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
                        resetDisplay()
                        loadPositionAssessments()
                    }
                }
            dialog.setCancelable(false)
            dialog.show()
        }

    }

    private fun updateDataFields() {

        /**
         * Title,Description & Icon
         */
        binding.apply {
            /**
             * Radio Clicks
             */


            updateTitleIconDescription(
                incPositioning.dataHands,
                "Cleaned Hands",
                getString(R.string.app_cleaned_hands), 0
            )
            updateTitleIconDescription(
                incPositioning.dataMother,
                "Mother Position",
                "Mother relaxed, comfortable and pain controlled", 1
            )
            updateTitleIconDescription(
                incPositioning.dataBaby,
                "Baby Position",
                "Baby's nose at the level of the breast\nBaby's tummy and mother's tummy touching\nBaby close skin with the mother\nHead and the trunk in straight line\n Baby's whole body supported",
                2
            )
            updateTitleIconDescription(
                incPositioning.dataAttach,
                "Good attachment (Rename Good attachment)",
                "Hold Breast using the C-Grip\nStimulate baby to open mouth wide\nMore areola above the nipple\nChin touching breast\nMouth Open (more than 120 Degrees)\nLower Lip turned out",
                3
            )
            updateTitleIconDescription(
                incPositioning.dataSuck,
                "Effectively Suckling",
                "Baby takes slow deep suckles sometimes pausing and you may be able to see or hear baby swallowing\nNo dimpling\nSuckling is comfortable and pain free for mom",
                4
            )
        }
    }

    private fun updateTitleIconDescription(
        dataHands: PositioningItemBinding,
        title: String,
        description: String,
        index: Int
    ) {
        dataHands.tvTitle.text = title
        dataHands.tvDescription.text = description
        selection(dataHands, index)

    }

    private fun selection(dataHands: PositioningItemBinding, index: Int) {
        when (index) {
            0 -> {
                hands = if (dataHands.rbYes.isChecked) {
                    "Yes"
                } else {
                    "No"
                }
            }
            1 -> {
                mum = if (dataHands.rbYes.isChecked) {
                    "Yes"
                } else {
                    "No"
                }
            }
            2 -> {
                baby = if (dataHands.rbYes.isChecked) {
                    "Yes"
                } else {
                    "No"
                }
            }
            3 -> {
                attach = if (dataHands.rbYes.isChecked) {
                    "Yes"
                } else {
                    "No"
                }
            }
            4 -> {
                suck = if (dataHands.rbYes.isChecked) {
                    "Yes"
                } else {
                    "No"
                }
            }

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

    private fun resetDisplay() {

        binding.apply {
            actionNewExpression.visibility = View.VISIBLE
            lnHistory.visibility = View.VISIBLE
            lnCollection.visibility = View.GONE
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