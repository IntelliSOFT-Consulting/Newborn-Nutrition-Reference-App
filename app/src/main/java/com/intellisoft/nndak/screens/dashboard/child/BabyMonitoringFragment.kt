package com.intellisoft.nndak.screens.dashboard.child

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.google.android.material.textfield.TextInputEditText
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.databinding.FragmentBabyMonitoringBinding
import com.intellisoft.nndak.dialogs.ConfirmationDialog
import com.intellisoft.nndak.dialogs.TipsDialog
import com.intellisoft.nndak.models.FeedItem
import com.intellisoft.nndak.models.FeedingCuesTips
import com.intellisoft.nndak.models.PrescriptionItem
import com.intellisoft.nndak.utils.disableEditing
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import com.intellisoft.nndak.viewmodels.ScreenerViewModel
import kotlinx.android.synthetic.main.prescribe_item.*
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
    private var totalV: Float = 0.0f
    private lateinit var careID: String
    private lateinit var deficit: String
    private lateinit var availableFeed: String
    private var ivPresent: Boolean = true
    private var dhmPresent: Boolean = true
    private var ebmPresent: Boolean = true
    private var formulaPresent: Boolean = true
    private val feedsList: MutableList<FeedItem> = mutableListOf()


    private val binding
        get() = _binding!!

    private var exit: Boolean = true

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

        //  promptQues()
        fhirEngine = FhirApplication.fhirEngine(requireContext())
        patientDetailsViewModel =
            ViewModelProvider(
                this,
                PatientDetailsViewModelFactory(
                    requireActivity().application,
                    fhirEngine,
                    args.patientId
                )
            ).get(PatientDetailsViewModel::class.java)

        binding.apply {
            lnCurrent.visibility = View.GONE
            incDetails.lnBody.visibility = View.GONE
            incDetails.pbLoading.visibility = View.VISIBLE
        }
        confirmationDialog = ConfirmationDialog(
            this::okClick,
            resources.getString(R.string.app_okay_message)
        )

        binding.apply {
            breadcrumb.page.text =
                Html.fromHtml("Babies > Baby Panel > <font color=\"#37379B\">Feeding</font>")
            breadcrumb.page.setOnClickListener {
                findNavController().navigateUp()
            }

        }
        patientDetailsViewModel.getMumChild()
        patientDetailsViewModel.getCurrentPrescriptions()
        patientDetailsViewModel.liveMumChild.observe(viewLifecycleOwner) { data ->

            if (data != null) {
                binding.apply {

                    incDetails.lnBody.visibility = View.VISIBLE
                    incDetails.pbLoading.visibility = View.GONE

                    val gest = data.dashboard.gestation ?: ""
                    val status = data.status
                    incDetails.tvBabyName.text = data.babyName
                    incDetails.tvMumName.text = data.motherName
                    incDetails.appBirthWeight.text = data.birthWeight
                    incDetails.appGestation.text = "$gest-$status"
                    incDetails.appApgarScore.text = data.dashboard.apgarScore ?: ""
                    incDetails.appMumIp.text = data.motherIp
                    incDetails.appBabyWell.text = data.dashboard.babyWell ?: ""
                    incDetails.appAsphyxia.text = data.dashboard.asphyxia ?: ""
                    incDetails.appNeonatalSepsis.text =
                        data.dashboard.neonatalSepsis ?: ""
                    incDetails.appJaundice.text = data.dashboard.jaundice ?: ""
                    incDetails.appBirthDate.text = data.dashboard.dateOfBirth ?: ""
                    incDetails.appLifeDay.text = data.dashboard.dayOfLife ?: ""
                    incDetails.appAdmDate.text = data.dashboard.dateOfAdm ?: ""


                    val isSepsis = data.dashboard.neonatalSepsis
                    val isAsphyxia = data.dashboard.asphyxia
                    val isJaundice = data.dashboard.jaundice
                    if (isSepsis == "Yes" || isAsphyxia == "Yes" || isJaundice == "Yes") {
                        incDetails.lnConditions.visibility = View.VISIBLE
                    }
                    if (isSepsis != "Yes") {
                        incDetails.appNeonatalSepsis.visibility = View.GONE
                        incDetails.tvNeonatalSepsis.visibility = View.GONE
                    }

                    if (isAsphyxia != "Yes") {
                        incDetails.appAsphyxia.visibility = View.GONE
                        incDetails.tvAsphyxia.visibility = View.GONE
                    }

                    if (isJaundice != "Yes") {
                        incDetails.tvJaundice.visibility = View.GONE
                        incDetails.appJaundice.visibility = View.GONE
                    }


                }
            }
        }

        patientDetailsViewModel.livePrescriptionsData.observe(viewLifecycleOwner) { data ->
            if (data.isNotEmpty()) {
                val it = data.first()
                careID = it.resourceId.toString()
                Timber.e("Encounter Feeding ${it.frequency}")
                binding.apply {
                    lnCurrent.visibility = View.VISIBLE
                    availableFeed = it.deficit.toString()
                    incPrescribe.appTodayTotal.text =
                        it.deficit
                    incPrescribe.appRoute.text = it.route ?: ""
                    val threeHourly = calculateFeeds(it.totalVolume ?: "0", it.frequency.toString())
                    incPrescribe.appThreeHourly.text = threeHourly
                    val breakDown = generateFeedsBreakDown(it)
                    incPrescribe.appThreeHourlyBreak.text = breakDown
                    incPrescribe.tvFrequency.text = "${it.frequency} Feed Volume"
                    regulateViews(it)

                }

            } else {
                exit = true
                val dialog =
                    SweetAlertDialog(requireContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                        .setTitleText("Error")
                        .setContentText(resources.getString(R.string.no_active))
                        .setCustomImage(R.drawable.crying)
                        .setConfirmClickListener { sDialog ->
                            run {
                                sDialog.dismiss()
                                exitBack()
                            }
                        }
                dialog.setCancelable(false)
                dialog.show()
            }
        }
        listenToChange(binding.control.edDhm)
        listenToChange(binding.control.edEbm)
        listenToChange(binding.control.edIv)
        listenToChange(binding.control.edFormula)
        binding.apply {

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

                exit = true
                Timber.e(
                    "Data\n" +
                            "IV $ivVolume\n" +
                            "DHM $dhmVolume\n" +
                            "EBM $ebmVolume\n" +
                            "Formula $formulaVolume\n" +
                            "Deficit $deficit\n" +
                            "Total $totalV"
                )
                confirmationDialog.show(childFragmentManager, "Confirm Action")
            }
            btnCancel.setOnClickListener {
                findNavController().navigateUp()
            }
            actionClickTips.setOnClickListener {
                handleShowCues()
            }
        }


    }

    private fun promptQues() {
        exit = false
        handleShowCues()
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

                    control.edDeficit.setText(def.toString())
                } else {
                    control.edDeficit.setText("0")
                }

            }

        } catch (e: Exception) {
            Timber.e("Exception::: ${e.localizedMessage}")
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
        Timber.e("Feed Calculation $totalVolume")
        var total = 0.0
        try {
            val code = totalVolume.split("\\.".toRegex()).toTypedArray()
            val rate = getNumericFrequency(frequency)
            Timber.e("Rate $rate")
            val times = 24 / rate.toFloat()
            val j: Float = times
            val k: Float = code[0].toFloat()

            val totalPerSession = (k / j).toDouble()

            val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.DOWN
            total = df.format(totalPerSession).toDouble()
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e("Feeding Exception ${e.localizedMessage} ")
        }
        return "$total mls"
    }

    private fun getNumericFrequency(frequency: String): String {
        val index = 0
        val ch = frequency[index]
        Timber.e("Value $ch")
        return ch.toString()
    }

    private fun handleShowCues() {
        exit = false
        feedingCues = TipsDialog(this::feedingCuesClick)
        feedingCues.show(childFragmentManager, "bundle")
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
            val dialog = SweetAlertDialog(requireContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText("Success")
                .setContentText(resources.getString(R.string.app_okay_saved))
                .setCustomImage(R.drawable.smile)
                .setConfirmClickListener { sDialog ->
                    run {
                        sDialog.dismiss()
                        exitBack()
                    }
                }
            dialog.setCancelable(false)
            dialog.show()
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

        SweetAlertDialog(activity, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Are you sure?")
            .setContentText(getString(R.string.cancel_questionnaire_message))
            .setConfirmText("Yes")
            .setConfirmClickListener { d ->
                d.dismiss()
                NavHostFragment.findNavController(this@BabyMonitoringFragment).navigateUp()
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
        requireArguments().putString(
            QUESTIONNAIRE_FILE_PATH_KEY,
            "date-time.json"
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

            if (feedsList.isNotEmpty()) {

                viewModel.babyMonitoring(
                    questionnaireFragment.getQuestionnaireResponse(),
                    args.patientId,
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
    }

    private fun exitBack() {
        try {
            findNavController().navigateUp()
        } catch (e: Exception) {
        }

    }

    private fun proceedClick() {
        if (exit) {
            findNavController().navigateUp()
        }
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