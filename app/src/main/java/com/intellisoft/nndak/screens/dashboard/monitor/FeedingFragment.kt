package com.intellisoft.nndak.screens.dashboard.monitor

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.android.material.textfield.TextInputEditText
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.adapters.FeedAdapter
import com.intellisoft.nndak.adapters.MonitoringAdapter
import com.intellisoft.nndak.databinding.FragmentFeedingBinding
import com.intellisoft.nndak.dialogs.ConfirmationDialog
import com.intellisoft.nndak.dialogs.MoreExpression
import com.intellisoft.nndak.helper_class.FormatHelper
import com.intellisoft.nndak.logic.DataSort.Companion.getNumericFrequency
import com.intellisoft.nndak.models.FeedItem
import com.intellisoft.nndak.models.PrescriptionItem
import com.intellisoft.nndak.utils.boldText
import com.intellisoft.nndak.utils.disableEditing
import com.intellisoft.nndak.utils.getFutureHoursOnIntervalOfWithStart
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import com.intellisoft.nndak.viewmodels.ScreenerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.RoundingMode
import java.text.DecimalFormat

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FeedingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FeedingFragment : Fragment() {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val viewModel: ScreenerViewModel by viewModels()
    private var _binding: FragmentFeedingBinding? = null
    private lateinit var patientId: String
    private lateinit var adapterList: FeedAdapter
    private lateinit var monitoringAdapter: MonitoringAdapter
    private var scheduleTimes = ArrayList<FeedItem>()

    private lateinit var confirmationDialog: ConfirmationDialog
    private var totalV: Float = 0.0f
    private lateinit var careID: String
    private lateinit var deficit: String
    private lateinit var availableFeed: String
    private var ivPresent: Boolean = true
    private var dhmPresent: Boolean = true
    private var ebmPresent: Boolean = true
    private var formulaPresent: Boolean = true
    private var prescriptionExists: Boolean = false
    private var exitSection: Boolean = true
    private val feedsList: MutableList<FeedItem> = mutableListOf()
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedingBinding.inflate(inflater, container, false)
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
                ).get(PatientDetailsViewModel::class.java)
            confirmationDialog = ConfirmationDialog(
                this::onSubmitAction,
                resources.getString(R.string.app_okay_message)
            )
            updatePrescriptionVisibility(false)
            binding.apply {
                incHistory.lnParent.visibility = View.GONE

            }

            loadActivePrescription()
            loadFeedingHistory()

            updateArguments()
            onBackPressed()
            if (savedInstanceState == null) {
                addQuestionnaireFragment()
            }
            updateUI()

            loadActivePrescription()
            loadFeedingHistory()

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

    private fun updateUI() {

        listenToChange(binding.control.edDhm)
        listenToChange(binding.control.edEbm)
        listenToChange(binding.control.edIv)
        listenToChange(binding.control.edFormula)
        binding.apply {
            lnCollection.visibility = View.GONE
            lnHistory.visibility = View.VISIBLE
            disableEditing(control.edDeficit)
            btnSubmit.setOnClickListener {

                val ivVolume = control.edIv.text.toString()
                val dhmVolume = control.edDhm.text.toString()
                val ebmVolume = control.edEbm.text.toString()
                val formulaVolume = control.edFormula.text.toString()
                val total = incPrescribe.appTodayTotal.text.toString()
                totalV = total.toFloat()
                deficit = control.edDeficit.text.toString()

                if (ivPresent) {
                    if (ivVolume.isNotEmpty()) {
                        feedsList.add(
                            FeedItem(
                                type = "IV",
                                volume = ivVolume.toDouble().toString()
                            )
                        )
                    } else {

                        Toast.makeText(
                            requireContext(),
                            getString(R.string.inputs_missing),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                }
                if (dhmPresent) {
                    if (dhmVolume.isNotEmpty()) {
                        feedsList.add(
                            FeedItem(
                                type = "DHM",
                                volume = dhmVolume.toDouble().toString()
                            )
                        )
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.inputs_missing),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        return@setOnClickListener

                    }
                }
                if (ebmPresent) {
                    if (ebmVolume.isNotEmpty()) {
                        feedsList.add(
                            FeedItem(
                                type = "EBM",
                                volume = ebmVolume.toDouble().toString()
                            )
                        )
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.inputs_missing),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                }
                if (formulaPresent) {
                    if (formulaVolume.isNotEmpty()) {
                        feedsList.add(
                            FeedItem(
                                type = "Formula",
                                volume = formulaVolume.toDouble().toString()
                            )
                        )
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.inputs_missing),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                }
                confirmationDialog.show(childFragmentManager, "Confirm Action")
            }

            btnCancel.setOnClickListener {
                showCancelScreenerQuestionnaireAlertDialog()
            }
        }
    }

    private fun regulateViews(pr: PrescriptionItem) {
        binding.apply {
            if (pr.ivFluids == "N/A") {
                control.tilIv.visibility = View.GONE
                control.edIv.setText("0")
                ivPresent = false
            }
            if (pr.ebm == "N/A") {
                control.tilEbm.visibility = View.GONE
                control.edEbm.setText("0")
                ebmPresent = false
            }
            if (pr.formula == "N/A") {
                control.tilFormula.visibility = View.GONE
                control.edFormula.setText("0")
                formulaPresent = false
            }
            if (pr.donorMilk == "N/A") {
                control.tilDhm.visibility = View.GONE
                control.edDhm.setText("0")
                dhmPresent = false
            }
        }
    }

    private fun generateFeedsBreakDown(pr: PrescriptionItem): String {
        val sb = StringBuilder()
        if (pr.ivFluids != "N/A") {
            sb.append("IV- ${calculateFeeds(pr.ivFluids.toString(), pr.frequency.toString())}\n")
        }
        if (pr.ebm != "N/A") {
            sb.append("EBM- ${calculateFeeds(pr.ebm.toString(), pr.frequency.toString())}\n")
        }
        if (pr.donorMilk != "N/A") {
            sb.append("DHM- ${calculateFeeds(pr.donorMilk.toString(), pr.frequency.toString())}\n")
        }
        if (pr.formula != "N/A") {
            sb.append(
                "Formula- ${
                    calculateFeeds(
                        pr.formula.toString(),
                        pr.frequency.toString()
                    )
                }\n"
            )
        }
        return sb.toString()
    }

    private fun calculateFeeds(totalVolume: String, frequency: String): String {

        var total = 0.0
        try {
            val code = totalVolume.split("\\.".toRegex()).toTypedArray()
            val rate = getNumericFrequency(frequency)

            val times = 24 / rate.toFloat()
            val j: Float = times
            val k: Float = code[0].toFloat()

            val totalPerSession = (k / j).toDouble()

            val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.DOWN
            total = df.format(totalPerSession).toDouble()
        } catch (e: Exception) {
            e.printStackTrace()
            total = 0.0
        }
        return total.toString()
    }

    private fun loadActivePrescription() {
        patientDetailsViewModel.getCurrentPrescriptions()
        patientDetailsViewModel.livePrescriptionsData.observe(viewLifecycleOwner) { data ->
            if (data.isNotEmpty()) {
                val it = data.first()
                careID = it.resourceId.toString()
                prescriptionExists = true
                updatePrescriptionVisibility(true)
                binding.apply {

                    incPrescribe.appTodayTotal.text = it.totalVolume
                    incPrescribe.appRoute.text = it.route ?: ""
                    val threeHourly = calculateFeeds(it.totalVolume ?: "0", it.frequency.toString())
                    availableFeed = threeHourly

                    incPrescribe.appThreeHourly.text = threeHourly
                    val breakDown = generateFeedsBreakDown(it)
                    incPrescribe.appThreeHourlyBreak.text = breakDown
                    incPrescribe.tvFrequency.text = "${it.frequency} Feed Volume"
                    incPrescribe.tvBreakdown.text = "${it.frequency} Feed Breakdown"
                    regulateViews(it)
                    setupFeedingTimes(it.feedingTime, it.frequency ?: "3 Hourly")

                }

            } else {
                prescriptionExists = false

            }
        }
    }

    private fun updatePrescriptionVisibility(isShow: Boolean) {
        binding.apply {
            if (isShow) {
                tvCurrent.visibility = View.VISIBLE
                incPrescribe.lnParent.visibility = View.VISIBLE
            } else {

                tvCurrent.visibility = View.GONE
                incPrescribe.lnParent.visibility = View.GONE
            }
        }
    }

    private fun loadFeedingHistory() {
        patientDetailsViewModel.getFeedingHistory()
        patientDetailsViewModel.liveFeedingHistory.observe(viewLifecycleOwner) { data ->
            if (data.isNotEmpty()) {

                monitoringAdapter = MonitoringAdapter()
                binding.orderList.apply {
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
                    tvFeeding.visibility = View.VISIBLE
                    incHistory.lnParent.visibility = View.VISIBLE
                    incHistory.tvhDate.text = "Date"
                    incHistory.tvhTime.text = "Time"
                    incHistory.tvhEbm.text = "EBM"
                    incHistory.tvhDhm.text = "DHM"
                    incHistory.tvhIv.text = "IV"
                    incHistory.tvhDeficit.text = "Deficit"
                    incHistory.tvhVomit.text = "Vomit"
                    incHistory.tvhDiaper.text = "Diapers Changed"
                    incHistory.tvhStool.text = "Stool"
                    boldText(incHistory.tvhDate)
                    boldText(incHistory.tvhTime)
                    boldText(incHistory.tvhEbm)
                    boldText(incHistory.tvhDhm)
                    boldText(incHistory.tvhIv)
                    boldText(incHistory.tvhDeficit)
                    boldText(incHistory.tvhVomit)
                    boldText(incHistory.tvhDiaper)
                    boldText(incHistory.tvhStool)

                }
            }
        }
    }

    private fun setupFeedingTimes(start: String, frequency: String) {
        binding.apply {
            tvHist.visibility = View.VISIBLE
        }
        val freq = getNumericFrequency(frequency)
        scheduleTimes = createTimingList(start, freq)
        adapterList = FeedAdapter(this::click)

        binding.incSchedule.patientList.apply {
            layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL, false
                )
            adapter = adapterList

        }

        adapterList.notifyDataSetChanged()
        adapterList.submitList(scheduleTimes)

    }

    private fun listenToChange(input: TextInputEditText) {

        CoroutineScope(Dispatchers.Default).launch {
            input.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(editable: Editable) {
                    try {
                        if (editable.toString().isNotEmpty()) {
                            val newValue = editable.toString()
                            input.removeTextChangedListener(this)
                            val position: Int = input.selectionEnd
                            input.setText(newValue)
                            if (position > (input.text?.length ?: 0)) {
                                input.text?.let { input.setSelection(it.length) }
                            } else {
                                input.setSelection(position);
                            }
                            input.addTextChangedListener(this)

                            getTotals()
                        } else {
                            input.setText("0")
                            getTotals()
                        }
                    } catch (e: Exception) {

                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int,
                    count: Int, after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int,
                    before: Int, count: Int
                ) {

                }
            })
        }
    }

    private fun getTotals() {
        try {
            binding.apply {
                val dhm = control.edDhm.text.toString()
                val ebm = control.edEbm.text.toString()
                val iv = control.edIv.text.toString()
                val formula = control.edFormula.text.toString()
                if (dhm.isNotEmpty() || ebm.isNotEmpty() || iv.isNotEmpty() || formula.isNotEmpty()) {
                    val total = dhm.toFloat() + ebm.toFloat() + iv.toFloat() + formula.toFloat()
                    val def = availableFeed.toDouble() - total
                    val df = DecimalFormat("#.##")
                    df.roundingMode = RoundingMode.DOWN
                    val rounded = df.format(def)
                    control.edDeficit.setText(rounded.toString())
                } else {
                    control.edDeficit.setText("0")
                }

            }

        } catch (e: Exception) {
            Timber.e("Exception::: ${e.localizedMessage}")
        }

    }

    private fun click(item: FeedItem) {
        if (prescriptionExists) {
            updateArguments()
            clearFields()
            addQuestionnaireFragment()
            resetDisplay(true)
            exitSection = false
        } else {
            val dialog =
                SweetAlertDialog(requireContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                    .setTitleText("Error")
                    .setContentText(resources.getString(R.string.no_active))
                    .setCustomImage(R.drawable.crying)
                    .setConfirmClickListener { sDialog ->
                        run {
                            sDialog.dismiss()
                            //resetDisplay()
                        }
                    }
            dialog.setCancelable(false)
            dialog.show()
        }
    }

    private fun clearFields() {
        binding.apply {
            control.edDhm.setText("")
            control.edEbm.setText("")
            control.edIv.setText("")
            control.edFormula.setText("")
            control.edDeficit.setText("")
        }
    }

    private fun onSubmitAction() {
        try {
            confirmationDialog.dismiss()
            (activity as MainActivity).displayDialog()
            observeResourcesSaveAction()
            CoroutineScope(Dispatchers.IO).launch {
                val questionnaireFragment =
                    childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment
                if (feedsList.isNotEmpty()) {

                    viewModel.babyMonitoring(
                        questionnaireFragment.getQuestionnaireResponse(),
                        patientId,
                        careID,
                        feedsList,
                        totalV,
                        deficit
                    )

                } else {

                    Toast.makeText(
                        requireContext(),
                        getString(R.string.inputs_missing),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        } catch (e: Exception) {

        }
    }

    private fun onBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            showCancelScreenerQuestionnaireAlertDialog()
        }
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
                    Timber.e("Cancel Screener Questionnaire FeedingFragment")
                    resetDisplay(false)
                }
                .setCancelText("No")
                .show()
        }
    }

    private fun updateArguments() {
        requireArguments().putString(QUESTIONNAIRE_FILE_PATH_KEY, "date-time.json")
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
            Timber.e("QuestionnaireFragment Exception ${e.localizedMessage}")
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
                        resetDisplay(false)
                    }
                }
                .show()
        }
    }

    private fun createTimingList(start: String, freq: String): ArrayList<FeedItem> {
        val data = ArrayList<FeedItem>()
        try {
            val times = 24 / freq.toFloat()
            val intervals = getFutureHoursOnIntervalOfWithStart(start,times.toInt(), freq.toInt())
            for ((i, entry) in intervals.withIndex()) {
                val maxRange = FormatHelper().extractTimeOnlyAM(entry)
                data.add(FeedItem(type = maxRange, id = i.toString()))
            }
        } catch (e: Exception) {

        }
        return data
    }

    private fun resetDisplay(isCollection: Boolean) {
        updateArguments()
        addQuestionnaireFragment()
        loadFeedingHistory()
        loadActivePrescription()
        binding.apply {
            if (isCollection) {
                lnHistory.visibility = View.GONE
                lnCollection.visibility = View.VISIBLE
            } else {
                lnHistory.visibility = View.VISIBLE
                lnCollection.visibility = View.GONE
            }
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