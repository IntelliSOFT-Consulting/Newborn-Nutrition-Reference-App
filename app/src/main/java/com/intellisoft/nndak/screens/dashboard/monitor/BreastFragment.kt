package com.intellisoft.nndak.screens.dashboard.monitor

import android.os.Bundle
import android.view.Gravity
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
import ca.uhn.fhir.context.FhirContext
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.adapters.BreastAdapter
import com.intellisoft.nndak.databinding.FragmentBreastBinding
import com.intellisoft.nndak.databinding.FragmentPositioningBinding
import com.intellisoft.nndak.databinding.PositioningItemBinding
import com.intellisoft.nndak.dialogs.MoreExpression
import com.intellisoft.nndak.dialogs.ViewBreastFeeding
import com.intellisoft.nndak.logic.Logics.Companion.BREASTS_FEEDING
import com.intellisoft.nndak.models.BreastsHistory
import com.intellisoft.nndak.models.CodingObservation
import com.intellisoft.nndak.utils.boldText
import com.intellisoft.nndak.utils.controlRadio
import com.intellisoft.nndak.utils.showErrorView
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import com.intellisoft.nndak.viewmodels.ScreenerViewModel
import timber.log.Timber

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BreastFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BreastFragment : Fragment() {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val viewModel: ScreenerViewModel by viewModels()
    private val dataCodes = ArrayList<CodingObservation>()
    private var _binding: FragmentBreastBinding? = null
    private lateinit var careID: String
    private var exitSection: Boolean = true
    private lateinit var patientId: String
    private lateinit var interest: String
    private lateinit var cues: String
    private lateinit var sleep: String
    private lateinit var bursts: String
    private lateinit var shortFeed: String
    private lateinit var longSwallow: String
    private lateinit var skin: String
    private lateinit var nipples: String
    private lateinit var shape: String

    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBreastBinding.inflate(inflater, container, false)
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
                ).get(PatientDetailsViewModel::class.java)
            resetDisplay()
            loadActivePrescription()
            handleClicks()
            binding.apply {
                lnCollection.visibility = View.GONE
                lnHistory.visibility = View.VISIBLE
                actionNewExpression.setOnClickListener {
                    updateArguments()
                    addQuestionnaireFragment()
                    exitSection = false
                    actionNewExpression.visibility = View.GONE
                    lnCollection.visibility = View.VISIBLE
                    lnHistory.visibility = View.GONE
                    resetViews()
                }
                btnSubmit.setOnClickListener {
                    dataMapping()
                    onSubmitAction()
                }
                btnCancel.setOnClickListener {
                    showCancelScreenerQuestionnaireAlertDialog()
                }
            }

            dataMapping()
            loadBreastsAssessments()
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

        setHasOptionsMenu(true)
        (activity as MainActivity).setDrawerEnabled(true)

    }

    private fun resetViews() {
        binding.apply {
            incPositioning.dataInterest.rbGroup.clearCheck()
            incPositioning.dataCues.rbGroup.clearCheck()
            incPositioning.dataSleep.rbGroup.clearCheck()
            incPositioning.dataBursts.rbGroup.clearCheck()
            incPositioning.dataShortFeed.rbGroup.clearCheck()
            incPositioning.dataLongSwallow.rbGroup.clearCheck()
            incPositioning.dataNormalSkin.rbGroup.clearCheck()
            incPositioning.dataNipples.rbGroup.clearCheck()
            incPositioning.dataShape.rbGroup.clearCheck()
        }
    }

    private fun handleClicks() {
        binding.apply {
            controlRadio(incPositioning.dataInterest)
            controlRadio(incPositioning.dataCues)
            controlRadio(incPositioning.dataSleep)
            controlRadio(incPositioning.dataBursts)
            controlRadio(incPositioning.dataShortFeed)
            controlRadio(incPositioning.dataLongSwallow)
            controlRadio(incPositioning.dataNormalSkin)
            controlRadio(incPositioning.dataNipples)
            controlRadio(incPositioning.dataShape)
        }


    }

    private fun updateArguments() {
        requireArguments().putString(QUESTIONNAIRE_FILE_PATH_KEY, "breast-feeding.json")
    }

    private fun onBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            showCancelScreenerQuestionnaireAlertDialog()
        }
    }

    private fun loadActivePrescription() {
        patientDetailsViewModel.getCurrentPrescriptions()
        patientDetailsViewModel.livePrescriptionsData.observe(viewLifecycleOwner) { data ->
            if (data.isNotEmpty()) {
                val it = data.first()
                careID = it.resourceId.toString()
            }
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
        if (validFields()) {
            val interest = CodingObservation("Baby-Interest", "Baby Interest", interest)
            val cues = CodingObservation("Feeding-Cues", "Feeding Cues", cues)
            val sleep = CodingObservation("Baby-Sleep", "Baby Sleep", sleep)
            val bursts = CodingObservation("Bursts", "Bursts", bursts)
            val shortFeed = CodingObservation("Short-Feed", "Short Feed", shortFeed)
            val longSwallow = CodingObservation("Long-Swallow", "Long Swallow", longSwallow)
            val skin = CodingObservation("Skin-Tone", "Skin Tone", skin)
            val nipples = CodingObservation("Nipples", "Nipples", nipples)
            val shape = CodingObservation("Shape", "Shape", shape)
            dataCodes.addAll(
                listOf(
                    interest, cues, sleep, bursts, shortFeed, longSwallow, skin, nipples, shape
                )
            )
            (activity as MainActivity).displayDialog()
            observeResourcesSaveAction()

            val questionnaireFragment =
                childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment


            viewModel.customAssessment(
                questionnaireFragment.getQuestionnaireResponse(),
                dataCodes,
                patientId, BREASTS_FEEDING
            )
        }
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
                        loadBreastsAssessments()
                    }
                }
            dialog.setCancelable(false)
            dialog.show()
        }

    }

    private fun loadBreastsAssessments() {
        patientDetailsViewModel.getBreastAssessmentHistory()
        patientDetailsViewModel.liveBreastHistory.observe(viewLifecycleOwner) { data ->
            if (data.isNotEmpty()) {

                val monitoringAdapter = BreastAdapter(this::clickItem)
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
                    incTitle.tvhDate.text = getString(R.string._date)
                    incTitle.tvhFrequency.text = getString(R.string._interest)
                    incTitle.tvhTiming.text = getString(R.string._cues)
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

    }

    private fun clickItem(data: BreastsHistory) {
        binding.apply {
            actionNewExpression.visibility = View.GONE
            lnCollection.visibility = View.VISIBLE
            lnHistory.visibility = View.GONE
            btnCancel.visibility = View.GONE
            btnSubmit.visibility = View.GONE
            exitSection = false

            if (data.interest.trim() == "Yes") {
                incPositioning.dataInterest.rbYes.isChecked = true
            } else {
                incPositioning.dataInterest.rbNo.isChecked = true
            }
            if (data.cues.trim() == "Yes") {
                incPositioning.dataCues.rbYes.isChecked = true
            } else {
                incPositioning.dataCues.rbNo.isChecked = true
            }
            if (data.sleep.trim() == "Yes") {
                incPositioning.dataSleep.rbYes.isChecked = true
            } else {
                incPositioning.dataSleep.rbNo.isChecked = true
            }
            if (data.bursts.trim() == "Yes") {
                incPositioning.dataBursts.rbYes.isChecked = true
            } else {
                incPositioning.dataBursts.rbNo.isChecked = true
            }
            if (data.shortFeed.trim() == "Yes") {
                incPositioning.dataShortFeed.rbYes.isChecked = true
            } else {
                incPositioning.dataShortFeed.rbNo.isChecked = true
            }
            if (data.longSwallow.trim() == "Yes") {
                incPositioning.dataLongSwallow.rbYes.isChecked = true
            } else {
                incPositioning.dataLongSwallow.rbNo.isChecked = true
            }
            if (data.skin.trim() == "Yes") {
                incPositioning.dataNormalSkin.rbYes.isChecked = true
            } else {
                incPositioning.dataNormalSkin.rbNo.isChecked = true
            }
            if (data.nipples.trim() == "Yes") {
                incPositioning.dataNipples.rbYes.isChecked = true
            } else {
                incPositioning.dataNipples.rbNo.isChecked = true
            }
            if (data.shape.trim() == "Yes") {
                incPositioning.dataShape.rbYes.isChecked = true
            } else {
                incPositioning.dataShape.rbNo.isChecked = true
            }

        }
/*
        moreExpression = ViewBreastFeeding(data)
        moreExpression.show(childFragmentManager, "Confirm Details")*/
    }

    private fun showCancelScreenerQuestionnaireAlertDialog() {
        if (exitSection) {

            (activity as MainActivity).openDashboard(patientId)
        } else {
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
    }

    private fun dataMapping() {
        binding.apply {
            updateTitleIconDescription(
                incPositioning.dataInterest,
                "Your baby is not interested when offered Breast, Sleepy", 0
            )
            updateTitleIconDescription(
                incPositioning.dataCues,
                "Is showing feeding cues but not attaching", 1
            )
            updateTitleIconDescription(
                incPositioning.dataSleep,
                "Attaches to the breast but quickly falls asleep", 2
            )
            updateTitleIconDescription(
                incPositioning.dataBursts,
                "Attaches for short bursts and long pauses", 3
            )
            updateTitleIconDescription(
                incPositioning.dataShortFeed,
                "Attaches well for a sustained period with long rhythmical sucking and swallowing for a short feed (Requiring stimulation)",
                4
            )
            updateTitleIconDescription(
                incPositioning.dataLongSwallow,
                "Attaches well for a sustained period with long rhythmical sucking and swallowing",
                5
            )
            updateTitleIconDescription(
                incPositioning.dataNormalSkin,
                "Normal Skin color and tone",
                6
            )
            updateTitleIconDescription(
                incPositioning.dataNipples,
                "Breast and nipples are comfortable",
                7
            )
            updateTitleIconDescription(
                incPositioning.dataShape,
                "Nipples are the same shape at the end of the feed as at the start",
                8
            )
        }
    }

    private fun updateTitleIconDescription(
        dataHands: PositioningItemBinding,
        description: String,
        index: Int
    ) {
        dataHands.tvTitle.visibility = View.GONE
        dataHands.civImage.visibility = View.GONE
        dataHands.lnHolder.gravity = Gravity.CENTER
        dataHands.lnHolder.setHorizontalGravity(Gravity.CENTER)
        dataHands.tvDescription.text = description
        selection(dataHands, index)

    }

    private fun validFields(): Boolean {
        binding.apply {
            if (interest.isEmpty() || interest == "") {
                showErrorView(incPositioning.dataInterest.tvError, "Selected if interested")
                return false
            }
            if (cues.isEmpty() || cues == "") {
                showErrorView(incPositioning.dataCues.tvError, "Selected if cues")
                return false
            }
            if (sleep.isEmpty() || sleep == "") {
                showErrorView(incPositioning.dataSleep.tvError, "Selected if sleep")
                return false
            }
            if (bursts.isEmpty() || bursts == "") {
                showErrorView(incPositioning.dataBursts.tvError, "Selected if bursts")
                return false
            }
            if (shortFeed.isEmpty() || shortFeed == "") {
                showErrorView(incPositioning.dataShortFeed.tvError, "Selected if short feed")
                return false
            }
            if (longSwallow.isEmpty() || longSwallow == "") {
                showErrorView(incPositioning.dataLongSwallow.tvError, "Selected if long swallow")
                return false
            }
            if (skin.isEmpty() || skin == "") {
                showErrorView(incPositioning.dataNormalSkin.tvError, "Selected if normal skin")
                return false
            }
            if (nipples.isEmpty() || nipples == "") {
                showErrorView(incPositioning.dataNipples.tvError, "Selected if nipples")
                return false
            }
            if (shape.isEmpty() || shape == "") {
                showErrorView(incPositioning.dataShape.tvError, "Selected if shape")
                return false
            }
        }
        return true
    }

    private fun selection(dataHands: PositioningItemBinding, index: Int) {
        when (index) {
            0 -> {
                interest = if (dataHands.rbYes.isChecked) {
                    "Yes"
                } else if (dataHands.rbNo.isChecked) {
                    "No"
                } else {
                    ""
                }
            }
            1 -> {
                cues = if (dataHands.rbYes.isChecked) {
                    "Yes"
                } else if (dataHands.rbNo.isChecked) {
                    "No"
                } else {
                    ""
                }

            }
            2 -> {
                sleep = if (dataHands.rbYes.isChecked) {
                    "Yes"
                } else if (dataHands.rbNo.isChecked) {
                    "No"
                } else {
                    ""
                }

            }
            3 -> {
                bursts = if (dataHands.rbYes.isChecked) {
                    "Yes"
                } else if (dataHands.rbNo.isChecked) {
                    "No"
                } else {
                    ""
                }

            }
            4 -> {
                shortFeed = if (dataHands.rbYes.isChecked) {
                    "Yes"
                } else if (dataHands.rbNo.isChecked) {
                    "No"
                } else {
                    ""
                }

            }
            5 -> {
                longSwallow = if (dataHands.rbYes.isChecked) {
                    "Yes"
                } else if (dataHands.rbNo.isChecked) {
                    "No"
                } else {
                    ""
                }

            }
            6 -> {
                skin = if (dataHands.rbYes.isChecked) {
                    "Yes"
                } else if (dataHands.rbNo.isChecked) {
                    "No"
                } else {
                    ""
                }

            }
            7 -> {
                nipples = if (dataHands.rbYes.isChecked) {
                    "Yes"
                } else if (dataHands.rbNo.isChecked) {
                    "No"
                } else {
                    ""
                }

            }
            8 -> {
                shape = if (dataHands.rbYes.isChecked) {
                    "Yes"
                } else if (dataHands.rbNo.isChecked) {
                    "No"
                } else {
                    ""
                }

            }

        }
    }

    private fun resetDisplay() {
        exitSection = true
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

    override fun onResume() {
        resetDisplay()
        super.onResume()
    }

    companion object {
        const val QUESTIONNAIRE_FILE_PATH_KEY = "questionnaire-file-path-key"
        const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire-fragment-tag"
    }
}