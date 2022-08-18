package com.intellisoft.nndak.screens.dashboard.discharge

import android.os.Build
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
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
import com.google.gson.Gson
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.MainActivity.Companion.updateBabyMum
import com.intellisoft.nndak.R
import com.intellisoft.nndak.data.SessionData
import com.intellisoft.nndak.databinding.FragmentDischargeBinding
import com.intellisoft.nndak.dialogs.ConfirmationDialog
import com.intellisoft.nndak.logic.Logics
import com.intellisoft.nndak.logic.Logics.Companion.CURRENT_WEIGHT
import com.intellisoft.nndak.logic.Logics.Companion.DISCHARGE_DATE
import com.intellisoft.nndak.logic.Logics.Companion.DISCHARGE_NOTES
import com.intellisoft.nndak.logic.Logics.Companion.DISCHARGE_OUTCOME
import com.intellisoft.nndak.logic.Logics.Companion.DISCHARGE_REASON
import com.intellisoft.nndak.models.CodingObservation
import com.intellisoft.nndak.models.QuantityObservation
import com.intellisoft.nndak.utils.*
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import com.intellisoft.nndak.viewmodels.ScreenerViewModel
import kotlinx.android.synthetic.main.fragment_landing.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DischargeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DischargeFragment : Fragment() {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val args: DischargeFragmentArgs by navArgs()
    private val viewModel: ScreenerViewModel by viewModels()
    private lateinit var confirmationDialog: ConfirmationDialog
    private var _binding: FragmentDischargeBinding? = null
    private var isAlive = false
    private lateinit var resourceId: String
    private val dataCodes = ArrayList<CodingObservation>()
    private val dataQuantity = ArrayList<QuantityObservation>()
    private val binding
        get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDischargeBinding.inflate(inflater, container, false)
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

        initViews()

        binding.apply {
            breadcrumb.page.text =
                Html.fromHtml("Babies > Baby Profile > <font color=\"#37379B\"> Discharge</font>")
            breadcrumb.page.setOnClickListener {
                findNavController().navigateUp()
            }
            incDetails.pbLoading.visibility = View.VISIBLE
            incDetails.lnBody.visibility = View.GONE
        }

        confirmationDialog = ConfirmationDialog(
            this::okClick,
            resources.getString(R.string.app_okay_message)
        )
        (requireActivity() as MainActivity).showBottomNavigationView(View.GONE)
        patientDetailsViewModel.getMumChild()
        patientDetailsViewModel.liveMumChild.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                updateBabyMum(binding.incDetails, data)

            }
        }
        patientDetailsViewModel.getDischargeDetails()
        patientDetailsViewModel.liveDischargeDetails.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                if (data.isNotEmpty()) {
                    binding.apply {
                        lnHistory.visibility = View.VISIBLE
                        lnCollection.visibility = View.GONE

                        incHistory.date.text = data[0].date
                        incHistory.outcome.text = data[0].outcome
                        incHistory.alive.text = data[0].reason
                        incHistory.weight.text = data[0].weight
                        incHistory.notes.text = data[0].notes
                        resourceId = data[0].resourceId
                    }
                } else {
                    binding.apply {
                        lnHistory.visibility = View.GONE
                        lnCollection.visibility = View.VISIBLE
                    }
                    resourceId = ""
                }

            }
        }

        binding.apply {
            btnCancel.setOnClickListener {
                showCancelScreenerQuestionnaireAlertDialog()
            }
            btnSubmit.setOnClickListener {
                onSubmitAction()
            }
            incHistory.actionReadmit.setOnClickListener {
                if (resourceId.isNotEmpty()) {
                    observeAdmitSaveAction()

                    CoroutineScope(Dispatchers.IO).launch {
                        patientDetailsViewModel.readmitBaby(resourceId)
                    }

                }
            }

        }
    }

    private fun initViews() {
        binding.apply {
            showPicker(requireContext(), appDischargeDate)
            showOptionsSelection(appOutcome, R.menu.status)
            showOptions(requireContext(), appDischargeReason, R.menu.outcome)
            listenChanges(appDischargeDate, tilDischargeDate, "select date")
            listenChanges(appOutcome, tilOutcome, "select outcome")
            listenChanges(appDischargeReason, tilDischargeReason, "select reason")
            listenMaxChanges(appDischargeWeight, tilDischargeWeight, "enter weight", 400, 5000)
            listenChanges(appNotes, tilDischargeNotes, "enter notes")
        }
    }

    private fun showOptionsSelection(textInputEditText: TextInputEditText, menuItem: Int) {
        textInputEditText.setOnClickListener {
            PopupMenu(requireContext(), textInputEditText).apply {
                menuInflater.inflate(menuItem, menu)
                setOnMenuItemClickListener { item ->
                    textInputEditText.setText(item.title)
                    if (item.title == "Alive") {
                        isAlive = true
                        binding.tilDischargeReason.visibility = View.VISIBLE
                    } else {
                        isAlive = false
                        binding.tilDischargeReason.visibility = View.GONE
                    }
                    true
                }
                show()
            }
        }
    }

    private fun okClick() {
        confirmationDialog.dismiss()
        (activity as MainActivity).displayDialog()
        val date = binding.appDischargeDate.text.toString()
        val outcome = binding.appOutcome.text.toString()
        val reason = binding.appDischargeReason.text.toString()
        val weight = binding.appDischargeWeight.text.toString()
        val notes = binding.appNotes.text.toString()


        val cDate = CodingObservation(DISCHARGE_DATE, "Discharge Date", date)
        val cOutcome = CodingObservation(DISCHARGE_OUTCOME, "Discharge Outcome", outcome)
        if (isAlive) {
            val cReason = CodingObservation(DISCHARGE_REASON, "Discharge Reason", reason)
            dataCodes.add(cReason)
        }
        val cWeight = QuantityObservation(CURRENT_WEIGHT, "Discharge Weight", weight, "gm")
        val cNotes = CodingObservation(DISCHARGE_NOTES, "Discharge Notes", notes)

        dataCodes.addAll(listOf(cDate, cOutcome, cNotes))
        dataQuantity.add(cWeight)

        CoroutineScope(Dispatchers.IO).launch {
            val questionnaireFragment =
                childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment
            viewModel.handleDischarge(
                questionnaireFragment.getQuestionnaireResponse(),
                dataCodes,
                dataQuantity,
                args.patientId
            )
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
            e.printStackTrace()
        }
    }

    private fun onSubmitAction() {
        if (validateErrors()) {
            return
        }
        confirmationDialog.show(childFragmentManager, "Confirm Details")

    }

    private fun validateErrors(): Boolean {
        binding.apply {
            if (appDischargeDate.text.isNullOrEmpty()) {
                tilDischargeDate.error = resources.getString(R.string.app_discharge_date_error)
                return true
            }
            if (appOutcome.text.isNullOrEmpty()) {
                tilOutcome.error = resources.getString(R.string.app_outcome_error)
                return true
            }
            if (isAlive) {
                if (appDischargeReason.text.isNullOrEmpty()) {
                    tilDischargeReason.error =
                        resources.getString(R.string.app_discharge_reason_error)
                    return true
                }
            }
            if (appDischargeWeight.text.isNullOrEmpty()) {
                tilDischargeWeight.error =
                    resources.getString(R.string.app_discharge_weight_error)
                appDischargeWeight.requestFocus()
                return true
            }
            if (appNotes.text.isNullOrEmpty()) {
                tilDischargeNotes.error =
                    resources.getString(R.string.app_discharge_height_error)
                appNotes.requestFocus()
                return true
            }

        }
        return false
    }

    private fun observeAdmitSaveAction() {
        patientDetailsViewModel.customMessage.observe(viewLifecycleOwner) {
            if (!it.success) {
                Toast.makeText(
                    requireContext(), it.message,
                    Toast.LENGTH_SHORT
                )
                    .show()
                (activity as MainActivity).hideDialog()
                return@observe
            }

               val dialog = SweetAlertDialog(requireContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                   .setTitleText("Success")
                   .setContentText(it.message)
                   .setCustomImage(R.drawable.smile)
                   .setConfirmClickListener { sDialog ->
                       run {
                           sDialog.dismiss()
                           try {  findNavController().navigateUp()}catch (e: Exception){
                               e.printStackTrace()
                           }
                       }
                   }
               dialog.setCancelable(false)
               dialog.show()

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
            val dialog = SweetAlertDialog(requireContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText("Success")
                .setContentText(resources.getString(R.string.app_okay_saved))
                .setCustomImage(R.drawable.smile)
                .setConfirmClickListener { sDialog ->
                    run {
                        sDialog.dismiss()
                        val session = SessionData(
                            patientId = args.patientId,
                            status = true
                        )
                        val gson = Gson()
                        val json = gson.toJson(session)
                        FhirApplication.setDashboardActive(
                            requireContext(),
                            json
                        )
                        findNavController().navigateUp()
                    }
                }
            dialog.setCancelable(false)
            dialog.show()
        }


    }

    private fun showCancelScreenerQuestionnaireAlertDialog() {

        SweetAlertDialog(activity, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Are you sure?")
            .setContentText(getString(R.string.cancel_questionnaire_message))
            .setConfirmText("Yes")
            .setConfirmClickListener { d ->
                d.dismiss()
                NavHostFragment.findNavController(this@DischargeFragment).navigateUp()
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
            "discharge.json"
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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