package com.intellisoft.nndak.screens.dashboard.child

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
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
import com.google.gson.Gson
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.MainActivity.Companion.updateBabyMum
import com.intellisoft.nndak.R
import com.intellisoft.nndak.data.SessionData
import com.intellisoft.nndak.databinding.FragmentBabyAssessmentBinding
import com.intellisoft.nndak.databinding.FragmentCustomAssessmentBinding
import com.intellisoft.nndak.dialogs.ConfirmationDialog
import com.intellisoft.nndak.logic.Logics
import com.intellisoft.nndak.logic.Logics.Companion.ASPHYXIA
import com.intellisoft.nndak.logic.Logics.Companion.ASSESSMENT_DATE
import com.intellisoft.nndak.logic.Logics.Companion.BABY_WELL
import com.intellisoft.nndak.logic.Logics.Companion.BREAST_PROBLEM
import com.intellisoft.nndak.logic.Logics.Companion.CURRENT_WEIGHT
import com.intellisoft.nndak.logic.Logics.Companion.FED_AFTER
import com.intellisoft.nndak.logic.Logics.Companion.FEED_TYPE
import com.intellisoft.nndak.logic.Logics.Companion.INTERVENTIONS
import com.intellisoft.nndak.logic.Logics.Companion.JAUNDICE
import com.intellisoft.nndak.logic.Logics.Companion.MUM_WELL
import com.intellisoft.nndak.logic.Logics.Companion.REMARKS
import com.intellisoft.nndak.logic.Logics.Companion.SEPSIS
import com.intellisoft.nndak.logic.Logics.Companion.WITHIN_ONE
import com.intellisoft.nndak.models.CodingObservation
import com.intellisoft.nndak.models.QuantityObservation
import com.intellisoft.nndak.utils.*
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import com.intellisoft.nndak.viewmodels.ScreenerViewModel
import kotlinx.android.synthetic.main.fragment_custom_assessment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Observation
import timber.log.Timber
import java.util.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BabyAssessmentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BabyAssessmentFragment : Fragment() {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val args: BabyAssessmentFragmentArgs by navArgs()
    private val viewModel: ScreenerViewModel by viewModels()
    private var afterOne = false
    private lateinit var confirmationDialog: ConfirmationDialog
    private var _binding: FragmentCustomAssessmentBinding? = null
    private val binding
        get() = _binding!!

    private val dataCodes = ArrayList<CodingObservation>()
    private val dataQuantity = ArrayList<QuantityObservation>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomAssessmentBinding.inflate(inflater, container, false)
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

        binding.apply {
            breadcrumb.page.text =
                Html.fromHtml("Babies > Baby Profile > <font color=\"#37379B\"> Assess</font>")
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
        initViews()

        binding.apply {
            btnCancel.setOnClickListener {
                findNavController().navigateUp()
            }
            btnSubmit.setOnClickListener {
                onSubmitAction()
            }
        }
    }

    private fun initViews() {
        binding.apply {
            showPicker(requireContext(), dateTime.appDate)
            showTimePicker(requireContext(), dateTime.appTime)
            showOptions(requireContext(), weightWell.appSelect, R.menu.yesno)

            jaundiceAsphyxia.tilValue.hint = "Asphyxia"
            jaundiceAsphyxia.tilSelect.hint = "Jaundice"

            showOptions(requireContext(), jaundiceAsphyxia.appValue, R.menu.yesno)
            showOptions(requireContext(), jaundiceAsphyxia.appSelect, R.menu.yesno)
            disableEditingAddDropdown(jaundiceAsphyxia.appValue)

            sepsisBreast.tilValue.hint = "Neonatal Sepsis"
            sepsisBreast.tilSelect.hint = "Breast Problems"

            showOptions(requireContext(), sepsisBreast.appValue, R.menu.yesno)
            showOptions(requireContext(), sepsisBreast.appSelect, R.menu.breast_problems)
            disableEditingAddDropdown(sepsisBreast.appValue)

            mumWellOther.tilValue.hint = "Other Conditions"
            mumWellOther.tilSelect.hint = "Mother is Well"

            mumWellOther.appValue.inputType = InputType.TYPE_CLASS_TEXT
            mumWellOther.appValue.setTextIsSelectable(true)
            showOptions(requireContext(), mumWellOther.appSelect, R.menu.yesno)

            babyFedAfter.tilValue.hint = "Feed within one hour"
            babyFedAfter.tilSelect.hint = "When was Baby Fed?"
            babyFedAfter.tilSelect.visibility = View.GONE
            showMultiOptions(babyFedAfter.appValue, R.menu.yesno)
            disableEditingAddDropdown(babyFedAfter.appValue)
            showOptions(requireContext(), babyFedAfter.appSelect, R.menu.feed_timings)
            feedType.tilValue.hint = "Feed Type"
            showOptions(requireContext(), feedType.appValue, R.menu.feed_type)
            disableEditingAddDropdown(feedType.appValue)
            feedType.tilSelect.visibility = View.GONE

            //listen to changes to all field
            listenMaxChanges(weightWell.appValue, weightWell.tilValue, "required", 400, 5000)
            listenChanges(dateTime.appDate, dateTime.tilDate, "required")
            listenChanges(dateTime.appTime, dateTime.tilTime, "required")
            listenChanges(weightWell.appValue, weightWell.tilValue, "required")
            listenChanges(weightWell.appSelect, weightWell.tilSelect, "required")
            listenChanges(jaundiceAsphyxia.appValue, jaundiceAsphyxia.tilValue, "required")
            listenChanges(jaundiceAsphyxia.appSelect, jaundiceAsphyxia.tilSelect, "required")
            listenChanges(sepsisBreast.appValue, sepsisBreast.tilValue, "required")
            listenChanges(sepsisBreast.appSelect, sepsisBreast.tilSelect, "required")
            listenChanges(mumWellOther.appValue, mumWellOther.tilValue, "required")
            listenChanges(mumWellOther.appSelect, mumWellOther.tilSelect, "required")
            listenChanges(babyFedAfter.appValue, babyFedAfter.tilValue, "required")
            listenChanges(babyFedAfter.appSelect, babyFedAfter.tilSelect, "required")
            listenChanges(feedType.appValue, feedType.tilValue, "required")
            listenChanges(feedType.appSelect, feedType.tilSelect, "required")

            listenChanges(babyFedAfter.appValue, babyFedAfter.tilValue, "required")
            if (afterOne) {
                listenChanges(babyFedAfter.appSelect, babyFedAfter.tilSelect, "required")
            }
            listenChanges(appNotes, tilNotes, "required")


        }
    }

    private fun showMultiOptions(textInputEditText: TextInputEditText, menuItem: Int) {
        textInputEditText.setOnClickListener {
            PopupMenu(requireContext(), textInputEditText).apply {
                menuInflater.inflate(menuItem, menu)
                setOnMenuItemClickListener { item ->
                    textInputEditText.setText(item.title)
                    if (item.title == "Yes") {
                        afterOne = false
                        binding.babyFedAfter.tilSelect.visibility = View.GONE
                    } else {
                        afterOne = true
                        binding.babyFedAfter.tilSelect.visibility = View.VISIBLE
                    }
                    true
                }
                show()
            }
        }
    }

    private fun disableEditingAddDropdown(appValue: TextInputEditText) {
        appValue.isClickable = false
        appValue.isFocusable = false
//        add drawableEnd
        val drawable =
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_arrow_drop_down_24)
        appValue.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)


    }

    private fun okClick() {
        confirmationDialog.dismiss()

        (activity as MainActivity).displayDialog()

        CoroutineScope(Dispatchers.IO).launch {
            val questionnaireFragment =
                childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment

            viewModel.handleAssessment(
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
            Timber.e("Exception ${e.localizedMessage}")
        }
    }

    private fun onSubmitAction() {


        //get all user inputs from views

        binding.apply {
            //get all user inputs from views date to notes
            val date = dateTime.appDate.text.toString()
            val time = dateTime.appTime.text.toString()
            val weight = weightWell.appValue.text.toString()
            val babyWell = weightWell.appSelect.text.toString()
            val jaundice = jaundiceAsphyxia.appValue.text.toString()
            val asphyxia = jaundiceAsphyxia.appSelect.text.toString()
            val sepsis = sepsisBreast.appValue.text.toString()
            val breast = sepsisBreast.appSelect.text.toString()
            val mumWell = mumWellOther.appValue.text.toString()
            val others = mumWellOther.appSelect.text.toString()
            val babyFed = babyFedAfter.appValue.text.toString()
            val fedTime = babyFedAfter.appSelect.text.toString()
            val type = feedType.appValue.text.toString()
            val notes = appNotes.text.toString()

            //check if all fields are filled

            if (date.isEmpty() && time.isEmpty() && weight.isEmpty() && babyWell.isEmpty() && jaundice.isEmpty()
                && asphyxia.isEmpty() && sepsis.isEmpty() && breast.isEmpty() && mumWell.isEmpty() && others.isEmpty()
                && babyFed.isEmpty() && type.isEmpty() && notes.isEmpty()
            ) {
                dateTime.tilDate.error = "required field"
                dateTime.tilTime.error = "required field"
                weightWell.tilValue.error = "required field"
                weightWell.tilSelect.error = "required field"
                jaundiceAsphyxia.tilValue.error = "required field"
                jaundiceAsphyxia.tilSelect.error = "required field"
                sepsisBreast.tilValue.error = "required field"
                sepsisBreast.tilSelect.error = "required field"
                mumWellOther.tilValue.error = "required field"
                mumWellOther.tilSelect.error = "required field"
                babyFedAfter.tilValue.error = "required field"
                babyFedAfter.tilSelect.error = "required field"
                feedType.tilValue.error = "required field"
                appNotes.error = "required field"

                return
            }
            if (afterOne) {
                if (fedTime.isEmpty()) {
                    babyFedAfter.tilSelect.error = "required field"
                    return
                }
            }

            //check if date is valid for individual
            if (date.isEmpty()) {
                dateTime.tilDate.error = "required field"
                return
            }
            if (time.isEmpty()) {
                dateTime.tilTime.error = "required field"
                return
            }
            if (weight.isEmpty()) {
                weightWell.tilValue.error = "required field"
                weightWell.appValue.requestFocus()
                return
            }
            if (babyWell.isEmpty()) {
                weightWell.tilSelect.error = "required field"
                return
            }
            if (jaundice.isEmpty()) {
                jaundiceAsphyxia.tilValue.error = "required field"
                return
            }
            if (asphyxia.isEmpty()) {
                jaundiceAsphyxia.tilSelect.error = "required field"
                return
            }
            if (sepsis.isEmpty()) {
                sepsisBreast.tilValue.error = "required field"
                return
            }
            if (breast.isEmpty()) {
                sepsisBreast.tilSelect.error = "required field"
                return
            }
            if (mumWell.isEmpty()) {
                mumWellOther.tilValue.error = "required field"
                return
            }
            if (others.isEmpty()) {
                mumWellOther.tilSelect.error = "required field"
                mumWellOther.appSelect.requestFocus()
                return
            }
            if (babyFed.isEmpty()) {
                babyFedAfter.tilValue.error = "required field"
                return
            }
            if (afterOne) {
                if (fedTime.isEmpty()) {
                    babyFedAfter.tilSelect.error = "required field"
                    return
                }
            }
            if (type.isEmpty()) {
                feedType.tilValue.error = "required field"
                return
            }
            if (notes.isEmpty()) {
                appNotes.error = "required field"
                return
            }
            val dateValue = "$date $time"

            val dateCode = CodingObservation(ASSESSMENT_DATE, "Assessment Date", dateValue)
            val weightCode = QuantityObservation(CURRENT_WEIGHT, "Birth Weight", weight, "gm")
            val babyWellCode = CodingObservation(BABY_WELL, "Baby Well", babyWell)
            val jaundiceCode = CodingObservation(JAUNDICE, "Jaundice", jaundice)
            val asphyxiaCode = CodingObservation(ASPHYXIA, "Asphyxia", asphyxia)
            val sepsisCode = CodingObservation(SEPSIS, "Sepsis", sepsis)
            val breastCode = CodingObservation(BREAST_PROBLEM, "Breast Feeding", breast)
            val mumWellCode = CodingObservation(MUM_WELL, "Mum Well", mumWell)
            val othersCode = CodingObservation(INTERVENTIONS, "Other Problem", others)
            val babyFedCode = CodingObservation(WITHIN_ONE, "Baby Fed", babyFed)
            if (afterOne) {
                val fedTimeCode = CodingObservation(FED_AFTER, "Feed Time", fedTime)
                dataCodes.add(fedTimeCode)
            }
            val typeCode = CodingObservation(FEED_TYPE, "Feed Type", type)
            val notesCode = CodingObservation(REMARKS, "Notes", notes)

            // add all code
            dataCodes.addAll(
                listOf(
                    dateCode,
                    babyWellCode,
                    jaundiceCode,
                    asphyxiaCode,
                    sepsisCode,
                    breastCode,
                    mumWellCode,
                    othersCode,
                    babyFedCode,
                    typeCode,
                    notesCode
                )

            )
            dataQuantity.addAll(
                listOf(
                    weightCode
                )
            )
        }
        // create observations codes


        confirmationDialog.show(childFragmentManager, "Confirm Details")

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
                NavHostFragment.findNavController(this@BabyAssessmentFragment).navigateUp()
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
            "baby-assessment.json"
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