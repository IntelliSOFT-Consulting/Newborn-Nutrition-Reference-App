package com.intellisoft.nndak.screens.dashboard.prescription

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ca.uhn.fhir.context.FhirContext
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.google.android.material.textfield.TextInputEditText
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.databinding.UpdatePrescriptionBinding
import com.intellisoft.nndak.dialogs.ConfirmationDialog
import com.intellisoft.nndak.logic.Logics.Companion.ADDITIONAL_FEEDS
import com.intellisoft.nndak.logic.Logics.Companion.BREAST_FREQUENCY
import com.intellisoft.nndak.logic.Logics.Companion.BREAST_MILK
import com.intellisoft.nndak.logic.Logics.Companion.DHM_CONSENT
import com.intellisoft.nndak.logic.Logics.Companion.DHM_FREQUENCY
import com.intellisoft.nndak.logic.Logics.Companion.DHM_REASON
import com.intellisoft.nndak.logic.Logics.Companion.DHM_ROUTE
import com.intellisoft.nndak.logic.Logics.Companion.DHM_TYPE
import com.intellisoft.nndak.logic.Logics.Companion.DHM_VOLUME
import com.intellisoft.nndak.logic.Logics.Companion.EBM_FREQUENCY
import com.intellisoft.nndak.logic.Logics.Companion.EBM_ROUTE
import com.intellisoft.nndak.logic.Logics.Companion.EBM_VOLUME
import com.intellisoft.nndak.logic.Logics.Companion.FEEDING_SUPPLEMENTS
import com.intellisoft.nndak.logic.Logics.Companion.FORMULA_FREQUENCY
import com.intellisoft.nndak.logic.Logics.Companion.FORMULA_ROUTE
import com.intellisoft.nndak.logic.Logics.Companion.FORMULA_TYPE
import com.intellisoft.nndak.logic.Logics.Companion.FORMULA_VOLUME
import com.intellisoft.nndak.logic.Logics.Companion.IV_FREQUENCY
import com.intellisoft.nndak.logic.Logics.Companion.IV_ROUTE
import com.intellisoft.nndak.logic.Logics.Companion.IV_VOLUME
import com.intellisoft.nndak.models.FeedItem
import com.intellisoft.nndak.models.Prescription
import com.intellisoft.nndak.models.PrescriptionItem
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import com.intellisoft.nndak.viewmodels.ScreenerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class EditPrescriptionFragment : Fragment() {
    private lateinit var confirmationDialog: ConfirmationDialog
    private var _binding: UpdatePrescriptionBinding? = null
    private lateinit var fhirEngine: FhirEngine
    private val viewModel: ScreenerViewModel by viewModels()
    private val args: EditPrescriptionFragmentArgs by navArgs()
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var currentWeight: String
    private lateinit var totalFeeds: String
    private lateinit var supp: String
    private lateinit var other: String
    private var aggregateTotal: Float = 0f

    private val feedsList: MutableList<FeedItem> = mutableListOf()
    private val binding
        get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = UpdatePrescriptionBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = "Edit Prescription"
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
        /**
         * Custom Update
         */
        createUI()

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

        patientDetailsViewModel.getCurrentPrescriptions()
        patientDetailsViewModel.livePrescriptionsData.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                updateUI(it.first())
            }
        }

        binding.apply {
            btnSubmit.setOnClickListener {
                onSubmitAction()
            }
            btnCancel.setOnClickListener {
                showCancelScreenerQuestionnaireAlertDialog()
            }
        }
        confirmationDialog = ConfirmationDialog(
            this::okClick,
            resources.getString(R.string.app_okay_message)
        )


    }

    private fun updateUI(it: PrescriptionItem) {
        Timber.e("Update Pres ${it.cWeight}")
        binding.apply {

            eWeight.setText(it.cWeight.toString())
            eTotal.setText(it.totalVolume.toString())
            otherSup.appFrequency.setText(it.additionalFeeds)
            otherValue.appFrequency.setText(it.supplements)

            if (it.breastMilk != "N/A") {
                cbBreast.isChecked = true
                tvBreast.visibility = View.VISIBLE
                lnBrestMilk.visibility = View.VISIBLE
                updateVolumeFrequency(bfVolume.volume, bfFreq.appFrequency, it.feed, BREAST_MILK)

            }
            if (it.formula != "N/A") {
                cbFormula.isChecked = true
                tvFormula.visibility = View.VISIBLE
                lnFormula.visibility = View.VISIBLE
                lnFormulaAlt.visibility = View.VISIBLE
                updateVolumeFrequencyRouteType(
                    formulaVolume.volume,
                    formulaFreq.appFrequency,
                    formulaRoute.appType,
                    formulaType.appFrequency,
                    it.feed,
                    FORMULA_VOLUME
                )
            }
            if (it.ebm != "N/A") {
                cbEbm.isChecked = true
                tvExpressed.visibility = View.VISIBLE
                lnEbmMilk.visibility = View.VISIBLE

                updateVolumeFrequencyRoute(
                    ebmVolume.volume,
                    ebmFreq.appFrequency,
                    ebmRoute.appType,
                    it.feed,
                    EBM_VOLUME
                )
            }
            if (it.donorMilk != "N/A") {
                cbDhm.isChecked = true
                tvDhm.visibility = View.VISIBLE
                lnDhmMilk.visibility = View.VISIBLE
                lnDhmMilkOther.visibility = View.VISIBLE
                val con = if (it.consent == "Signed") {
                    "Yes"
                } else {
                    "No"
                }
                dhmConsent.appFrequency.setText(con)
                dhmReason.volume.setText(it.dhmReason)
                updateVolumeFrequencyRouteType(
                    dhmVolume.volume, dhmFreq.appFrequency,
                    dhmRoute.appType, dhmType.appFrequency, it.feed, DHM_VOLUME
                )
            }
            if (it.ivFluids != "N/A") {
                cbFluid.isChecked = true
                tvIvFluids.visibility = View.VISIBLE
                lnIvFluids.visibility = View.VISIBLE
                updateVolumeFrequencyRoute(
                    ivVolume.volume,
                    ivFreq.appFrequency,
                    ivRoute.appType,
                    it.feed,
                    IV_VOLUME
                )
            }
            if (it.additionalFeeds != "N/A") {
                if (it.additionalFeeds == "Yes") {
                    otherValue.tilFre.visibility = View.VISIBLE
                }
            }

        }

        regulateViews()
    }

    private fun regulateViews() {
        binding.apply {

            cbBreast.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    tvBreast.visibility = View.VISIBLE
                    lnBrestMilk.visibility = View.VISIBLE
                } else {
                    tvBreast.visibility = View.GONE
                    lnBrestMilk.visibility = View.GONE
                }
                feedsList.clear()
            }

            cbFormula.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    tvFormula.visibility = View.VISIBLE
                    lnFormula.visibility = View.VISIBLE
                    lnFormulaAlt.visibility = View.VISIBLE
                } else {
                    tvFormula.visibility = View.GONE
                    lnFormula.visibility = View.GONE
                    lnFormulaAlt.visibility = View.GONE
                }
                feedsList.clear()
            }

            cbEbm.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    tvExpressed.visibility = View.VISIBLE
                    lnEbmMilk.visibility = View.VISIBLE
                } else {
                    tvExpressed.visibility = View.GONE
                    lnEbmMilk.visibility = View.GONE
                }
                feedsList.clear()
            }

            cbDhm.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    tvDhm.visibility = View.VISIBLE
                    lnDhmMilk.visibility = View.VISIBLE
                    lnDhmMilkOther.visibility = View.VISIBLE
                    lnDhmAlt.visibility = View.VISIBLE
                } else {
                    tvDhm.visibility = View.GONE
                    lnDhmMilk.visibility = View.GONE
                    lnDhmMilkOther.visibility = View.GONE
                    lnDhmAlt.visibility = View.GONE
                }
                feedsList.clear()
            }

            cbFluid.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    tvIvFluids.visibility = View.VISIBLE
                    lnIvFluids.visibility = View.VISIBLE
                } else {
                    tvIvFluids.visibility = View.GONE
                    lnIvFluids.visibility = View.GONE
                }
                feedsList.clear()
            }
        }
    }

    private fun updateVolumeFrequencyRouteType(
        volume: TextInputEditText,
        frequency: TextInputEditText,
        route: TextInputEditText,
        type: TextInputEditText,
        feed: List<FeedItem>?,
        code: String
    ) {
        val vol = feed?.find { it.resourceId == code }?.volume
        val freq = feed?.find { it.resourceId == code }?.frequency
        val rou = feed?.find { it.resourceId == code }?.route
        val typ = feed?.find { it.resourceId == code }?.type

        volume.setText(vol)
        frequency.setText(freq)
        route.setText(rou)
        type.setText(typ)
    }


    private fun updateVolumeFrequency(
        volume: TextInputEditText,
        appFrequency: TextInputEditText,
        feed: List<FeedItem>?,
        code: String
    ) {
        val vol = feed?.find { it.resourceId == code }?.volume
        val freq = feed?.find { it.resourceId == code }?.frequency

        volume.setText(vol)
        appFrequency.setText(freq)
    }

    private fun updateVolumeFrequencyRoute(
        volume: TextInputEditText,
        frequency: TextInputEditText,
        route: TextInputEditText,
        feed: List<FeedItem>?,
        code: String
    ) {
        val vol = feed?.find { it.resourceId == code }?.volume
        val freq = feed?.find { it.resourceId == code }?.frequency
        val rou = feed?.find { it.resourceId == code }?.route

        volume.setText(vol)
        frequency.setText(freq)
        route.setText(rou)
    }


    private fun createUI() {

        binding.apply {

            dhmType.tilFre.hint = "DHM Type"
            dhmConsent.tilFre.hint = "Consent Given?"
            otherSup.tilFre.hint = getString(R.string.app_add)
            otherValue.tilFre.hint = getString(R.string.supp)
            formulaType.tilFre.hint = getString(R.string.type)
            /**
             * Freq
             */
            showOptions(bfFreq.appFrequency, R.menu.menu_frequency)
            showOptions(ebmFreq.appFrequency, R.menu.menu_frequency)
            showOptions(dhmFreq.appFrequency, R.menu.menu_frequency)
            showOptions(dhmType.appFrequency, R.menu.menu_in_transaction)
            showOptions(dhmConsent.appFrequency, R.menu.menu_consent)
            showOptions(ivFreq.appFrequency, R.menu.menu_frequency)
            showOptions(formulaFreq.appFrequency, R.menu.menu_frequency)
            showOptions(otherSup.appFrequency, R.menu.menu_consent)
            showOptions(otherValue.appFrequency, R.menu.menu_supp)
            showOptions(formulaType.appFrequency, R.menu.menu_formula)

            /**
             * Route
             */
            showOptions(ebmRoute.appType, R.menu.route)
            showOptions(dhmRoute.appType, R.menu.route)
            showOptions(ivRoute.appType, R.menu.route)
            showOptions(formulaRoute.appType, R.menu.route)

        }
    }

    private fun showOptions(textInputEditText: TextInputEditText, menuItem: Int) {
        textInputEditText.setOnClickListener {
            PopupMenu(requireContext(), textInputEditText).apply {
                menuInflater.inflate(menuItem, menu)
                setOnMenuItemClickListener { item ->
                    textInputEditText.setText(item.title)
                    true
                }
                show()
            }
        }
    }

    private fun okClick() {
        confirmationDialog.dismiss()
        (activity as MainActivity).displayDialog()

        val questionnaireFragment =
            childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment
        try {

            val data = Prescription(
                currentWeight = currentWeight.toDouble().toString(),
                totalFeeds = totalFeeds.toDouble().toString(),
                supplements = other,
                additional = supp,
                data = feedsList
            )
            viewModel.updatePrescription(
                questionnaireFragment.getQuestionnaireResponse(), args.patientId, data
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                requireContext(),
                getString(R.string.inputs_missing),
                Toast.LENGTH_SHORT
            ).show()

        }
    }

    private fun updateArguments() {
        requireArguments().putString(QUESTIONNAIRE_FILE_PATH_KEY, "feed-prescription.json")
    }

    private fun observeResourcesSaveAction() {
        viewModel.isResourcesSaved.observe(viewLifecycleOwner) {
            if (!it) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.inputs_missing),
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
                        findNavController().navigateUp()
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

    private fun addQuestionnaireFragment(pair: Pair<String, String>) {
        Timber.e("First ${pair.first}")
        val fragment = QuestionnaireFragment()
        fragment.arguments =
            bundleOf(
                QuestionnaireFragment.EXTRA_QUESTIONNAIRE_JSON_STRING to pair.first,
                QuestionnaireFragment.EXTRA_QUESTIONNAIRE_RESPONSE_JSON_STRING to pair.second
            )
        childFragmentManager.commit {
            add(R.id.add_patient_container, fragment, QUESTIONNAIRE_FRAGMENT_TAG)
        }
    }


    private fun onSubmitAction() {

        binding.apply {
            currentWeight = eWeight.text.toString()
            totalFeeds = eTotal.text.toString()
            supp = otherSup.appFrequency.text.toString()
            other = otherValue.appFrequency.text.toString()
            totalFeeds = eTotal.text.toString()

            if (TextUtils.isEmpty(currentWeight)) {
                tilWeight.error = "Please Enter valid value"
                return
            }

            if (TextUtils.isEmpty(totalFeeds)) {
                tliTotal.error = "PLease enter valid volume"
                return
            }
            aggregateTotal = totalFeeds.toFloat()

        }

        if (binding.cbBreast.isChecked || binding.cbEbm.isChecked || binding.cbFormula.isChecked || binding.cbDhm.isChecked || binding.cbFluid.isChecked) {
            binding.apply {
                feedsList.clear()
                if (cbBreast.isChecked) {
                    val vol = bfVolume.volume.text.toString()
                    val freq = bfFreq.appFrequency.text.toString()
                    if (checkEmptyData(vol) || checkEmptyData(freq)) {
                        return
                    }
                    feedsList.add(
                        FeedItem(
                            resourceId = BREAST_MILK,
                            id = BREAST_FREQUENCY,
                            volume = vol.toDouble().toString(),
                            frequency = freq
                        )
                    )
                }
                if (cbEbm.isChecked) {
                    val vol = ebmVolume.volume.text.toString()
                    val freq = ebmFreq.appFrequency.text.toString()
                    val rou = ebmRoute.appType.text.toString()
                    if (checkEmptyData(vol) || checkEmptyData(freq) || checkEmptyData(rou)) {
                        return
                    }
                    feedsList.add(
                        FeedItem(
                            resourceId = EBM_VOLUME,
                            id = EBM_FREQUENCY,
                            type = EBM_ROUTE,
                            volume = vol.toDouble().toString(),
                            frequency = freq,
                            route = rou
                        )
                    )
                }
                if (cbFluid.isChecked) {
                    val vol = ivVolume.volume.text.toString()
                    val freq = ivFreq.appFrequency.text.toString()
                    val rou = ivRoute.appType.text.toString()
                    if (checkEmptyData(vol) || checkEmptyData(freq) || checkEmptyData(rou)) {
                        return
                    }
                    feedsList.add(
                        FeedItem(
                            resourceId = IV_VOLUME,
                            id = IV_FREQUENCY,
                            type = IV_ROUTE,
                            volume = vol.toDouble().toString(),
                            frequency = freq,
                            route = rou
                        )
                    )
                }
                if (cbFormula.isChecked) {
                    val vol = formulaVolume.volume.text.toString()
                    val freq = formulaFreq.appFrequency.text.toString()
                    val rou = formulaRoute.appType.text.toString()
                    val typ = formulaType.appFrequency.text.toString()

                    if (checkEmptyData(vol) || checkEmptyData(freq) || checkEmptyData(rou) || checkEmptyData(
                            typ
                        )
                    ) {
                        return@apply
                    }
                    feedsList.add(
                        FeedItem(
                            resourceId = FORMULA_VOLUME,
                            id = FORMULA_FREQUENCY,
                            type = FORMULA_ROUTE,
                            logicalId = FORMULA_TYPE,
                            volume = vol.toDouble().toString(),
                            frequency = freq,
                            route = rou,
                            specific = typ
                        )
                    )
                }
                if (cbDhm.isChecked) {
                    val vol = dhmVolume.volume.text.toString()
                    val freq = dhmFreq.appFrequency.text.toString()
                    val rou = dhmRoute.appType.text.toString()

                    val typ = dhmType.appFrequency.text.toString()
                    val con = dhmConsent.appFrequency.text.toString()
                    val reason = dhmReason.volume.text.toString()

                    if (checkEmptyData(vol) || checkEmptyData(freq) || checkEmptyData(rou) || checkEmptyData(
                            typ
                        ) || checkEmptyData(con) || checkEmptyData(reason)
                    ) {
                        return
                    }
                    val sig = if (con.trim() == "Yes") {
                        "Signed"
                    } else {
                        "Not Signed"
                    }

                    feedsList.add(
                        FeedItem(
                            logicalId = DHM_TYPE,
                            resourceId = DHM_VOLUME,
                            id = DHM_FREQUENCY,
                            type = DHM_ROUTE,
                            idAlt = DHM_CONSENT,
                            typeAlt = DHM_REASON,
                            volume = vol.toDouble().toString(),
                            frequency = freq,
                            route = rou,
                            specific = typ,
                            frequencyAlt = sig,
                            routeAlt = reason,
                        )
                    )
                }

                var totalFeedsVolume = 0f
                feedsList.forEach {
                    val quantity = it.volume
                    totalFeedsVolume += quantity?.toFloat() ?: 0f
                }
                if (totalFeedsVolume == aggregateTotal) {
                    confirmationDialog.show(childFragmentManager, "Confirm Details")
                } else {
                    if (totalFeedsVolume < aggregateTotal) {
                        Toast.makeText(
                            requireContext(),
                            "Please check Total Feeds",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Please check Feed Breakdown Volumes",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.inputs_missing),
                Toast.LENGTH_SHORT
            ).show()

        }

    }

    private fun checkEmptyData(vol: String): Boolean {
        if (vol.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.inputs_missing),
                Toast.LENGTH_SHORT
            ).show()
            return true
        }
        return false

    }

    private fun showCancelScreenerQuestionnaireAlertDialog() {
        val alertDialog: AlertDialog? =
            activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setMessage(getString(R.string.cancel_questionnaire_message))
                    setPositiveButton(getString(R.string.yes)) { _, _ ->
                        NavHostFragment.findNavController(this@EditPrescriptionFragment)
                            .navigateUp()
                    }
                    setNegativeButton(getString(R.string.no)) { _, _ -> }
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