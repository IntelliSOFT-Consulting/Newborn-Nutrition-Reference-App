package com.intellisoft.nndak.screens.dashboard

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.*
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.data.SessionData
import com.intellisoft.nndak.databinding.FragmentCustomRegistrationBinding
import com.intellisoft.nndak.helper_class.FormatHelper
import com.intellisoft.nndak.logic.Logics.Companion.ADMISSION_DATE
import com.intellisoft.nndak.logic.Logics.Companion.ADMISSION_WEIGHT
import com.intellisoft.nndak.logic.Logics.Companion.APGAR_SCORE
import com.intellisoft.nndak.logic.Logics.Companion.BBA
import com.intellisoft.nndak.logic.Logics.Companion.BIRTH_WEIGHT
import com.intellisoft.nndak.logic.Logics.Companion.CS_REASON
import com.intellisoft.nndak.logic.Logics.Companion.DELIVERY_DATE
import com.intellisoft.nndak.logic.Logics.Companion.DELIVERY_METHOD
import com.intellisoft.nndak.logic.Logics.Companion.GESTATION
import com.intellisoft.nndak.logic.Logics.Companion.HEAD_CIRCUMFERENCE
import com.intellisoft.nndak.logic.Logics.Companion.INTERVENTIONS
import com.intellisoft.nndak.logic.Logics.Companion.MULTIPLE_BIRTH_TYPE
import com.intellisoft.nndak.logic.Logics.Companion.MULTIPLE_PREGNANCY
import com.intellisoft.nndak.logic.Logics.Companion.PARITY
import com.intellisoft.nndak.logic.Logics.Companion.PMTCT
import com.intellisoft.nndak.logic.Logics.Companion.REMARKS
import com.intellisoft.nndak.logic.Logics.Companion.VDRL
import com.intellisoft.nndak.models.CodingObservation
import com.intellisoft.nndak.models.QuantityObservation
import com.intellisoft.nndak.utils.Constants.SYNC_VALUE
import com.intellisoft.nndak.viewmodels.ScreenerViewModel
import kotlinx.android.synthetic.main.fragment_custom_registration.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Reference
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class CustomRegistrationFragment : Fragment() {
    private var _binding: FragmentCustomRegistrationBinding? = null
    private val binding
        get() = _binding!!
    private var c: Calendar = Calendar.getInstance()
    private val viewModel: ScreenerViewModel by viewModels()
    private val dataCodes = ArrayList<CodingObservation>()
    private val dataQuantity = ArrayList<QuantityObservation>()
    private var isMultiple = false
    private var isCs = false
    private lateinit var mCalendar: Calendar
    private lateinit var mSdf: SimpleDateFormat
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = resources.getString(R.string.home_client)
            setHomeAsUpIndicator(R.drawable.dash)
            setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)
        (activity as MainActivity).setDrawerEnabled(true)
        onBackPressed()
        updateArguments()
        onBackPressed()
        if (savedInstanceState == null) {
            addQuestionnaireFragment()
        }
        initViews()

    }

    private fun updateArguments() {
        requireArguments().putString(QUESTIONNAIRE_FILE_PATH_KEY, "client-registration.json")
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

    private fun showPicker(input: TextInputEditText) {
        input.setOnClickListener {
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { view, myear, mmonth, mdayOfMonth ->
                    val mon = mmonth + 1
                    val msg = "$mdayOfMonth/$mon/$myear"
                    input.setText(msg)
                },
                year,
                month,
                day
            )
            datePickerDialog.datePicker.maxDate = Date().time
            datePickerDialog.show()
        }
    }

    private fun showTimePicker(input: TextInputEditText) {
        input.setOnClickListener {
            val mTimePicker: TimePickerDialog
            val mcurrentTime = Calendar.getInstance()
            val hour = mcurrentTime.get(Calendar.HOUR_OF_DAY)
            val minute = mcurrentTime.get(Calendar.MINUTE)

            mTimePicker = TimePickerDialog(
                requireContext(),
                { view, hourOfDay, minute ->
                    input.setText(
                        String.format("%02d:%02d", hourOfDay, minute)
                    )
                }, hour, minute, false
            )
            mTimePicker.show()
        }
    }

    private fun initViews() {
        binding.apply {

            breadcrumb.page.text =
                Html.fromHtml("Babies > <font color=\"#37379B\">Patient Registration</font>")
            breadcrumb.page.setOnClickListener {
                findNavController().navigate(CustomRegistrationFragmentDirections.navigateToLanding())
            }
            btnCancel.setOnClickListener {
                showCancelScreenerQuestionnaireAlertDialog()
            }
            btnSubmit.setOnClickListener {
                validateData()
            }

            listenChanges(appMumName, tilMumName, "Enter full name")
            listenChanges(appParity, tilParity, "Enter 5th min score")
            listenChanges(appTwoParity, tilTwoParity, "Enter 10th min score")
            listenChanges(appIpNumber, tilIpNumber, "Enter IP Number")
            listenChanges(appPmtct, tilPmtct, "Select Status")
            listenChanges(appMulti, tilMulti, "Select Birth Type")
            listenChanges(appBirthType, tilBirthType, "Specify birth type")
            listenChanges(appDelivery, tilDelivery, "Select delivery method")
            listenChanges(appCs, tilCs, "Select reason")
            listenChanges(appVdrl, tilVdrl, "Select VDRL status")
            listenChanges(appSex, tilSex, "Select Baby's sex")
            listenChanges(appBirthWeight, tilBirthWeight, "Enter birth weight")
            listenChanges(appGestation, tilGestation, "Enter gestation age")
            listenChanges(appFiveScore, tilFiveScore, "Enter 5th min score")
            listenChanges(appTenScore, tilTenScore, "Enter 10th min score")
            listenChanges(appBba, tilBba, "Select BBA")
            listenChanges(appHead, tilHead, "Enter head circumference")
            listenChanges(appInter, tilInter, "Enter interventions")
            listenChanges(appAdmWeight, tilAdmWeight, "Enter Admission weight")
            listenChanges(appNotes, tilNotes, "Enter remarks")
            listenChanges(appDelDate, tilDelDate, "Select delivery date")
            listenChanges(appDob, tilDob, "Select date of birth")
            listenChanges(appAdmDate, tilAdmDate, "Select admission date")
            listenChanges(appDelTime, tilDelTime, "Select delivery time")
            listenChanges(appAdmTime, tilAdmTime, "Select admission time")

            /**
             * Dropdowns
             */
            showOptions(appPmtct, R.menu.pmtct)
            showMultiOptions(appMulti, R.menu.yesno)
            showDeliveryOptions(appDelivery, R.menu.delivery)
            showOptions(appBirthType, R.menu.birthtypes)
            showOptions(appCs, R.menu.reasons)
            showOptions(appVdrl, R.menu.pmtct)
            showOptions(appSex, R.menu.sex)
            showOptions(appBba, R.menu.yesno)

            /**
             * Date pickers
             */
            showPicker(appDelDate)
            showPicker(appDob)
            showPicker(appAdmDate)
            /**
             * Time Pickers
             */
            showTimePicker(appDelTime)
            showTimePicker(appAdmTime)

        }
    }

    private fun showDeliveryOptions(textInputEditText: TextInputEditText, menuItem: Int) {
        textInputEditText.setOnClickListener {
            PopupMenu(requireContext(), textInputEditText).apply {
                menuInflater.inflate(menuItem, menu)
                setOnMenuItemClickListener { item ->
                    textInputEditText.setText(item.title)
                    if (item.title == "CS") {
                        isCs = true
                        binding.tilCs.visibility = View.VISIBLE
                    } else {
                        isCs = false
                        binding.tilCs.visibility = View.GONE
                    }
                    true
                }
                show()
            }
        }
    }

    private fun showMultiOptions(textInputEditText: TextInputEditText, menuItem: Int) {
        textInputEditText.setOnClickListener {
            PopupMenu(requireContext(), textInputEditText).apply {
                menuInflater.inflate(menuItem, menu)
                setOnMenuItemClickListener { item ->
                    textInputEditText.setText(item.title)
                    if (item.title == "Yes") {
                        isMultiple = true
                        binding.tilBirthType.visibility = View.VISIBLE
                    } else {
                        isMultiple = false
                        binding.tilBirthType.visibility = View.INVISIBLE
                    }
                    true
                }
                show()
            }
        }
    }

    private fun validateData() {
        binding.apply {
            val mumName = appMumName.text.toString()
            val pari1 = appParity.text.toString()
            val pari2 = appTwoParity.text.toString()
            val mumIp = appIpNumber.text.toString()
            val pmtct = appPmtct.text.toString()
            val multi = appMulti.text.toString()
            val births = appBirthType.text.toString()


            if (mumName.isEmpty()) {
                tilMumName.error = "Please enter mother's name"
                appMumName.requestFocus()
                return

            }
            if (pari1.isEmpty()) {
                tilParity.error = ""
                appParity.requestFocus()
                return

            }
            if (pari2.isEmpty()) {
                tilTwoParity.error = ""
                appTwoParity.requestFocus()
                return

            }
            if (mumIp.isEmpty()) {
                tilIpNumber.error = "Please enter Ip Number"
                appIpNumber.requestFocus()
                return

            }
            if (pmtct.isEmpty()) {
                tilPmtct.error = "Please select PMTCT"
                appPmtct.requestFocus()
                return

            }
            if (multi.isEmpty()) {
                tilMulti.error = "Please select Pregnancies"
                appMulti.requestFocus()
                return

            }
            if (isMultiple) {

                if (births.isEmpty()) {
                    tilBirthType.error = "Please select birth type"
                    appBirthType.requestFocus()
                    return

                }
            }
            val delDate = appDelDate.text.toString()
            val delTime = appDelTime.text.toString()
            val delMethod = appDelivery.text.toString()
            val cs = appCs.text.toString()
            val vdrl = appVdrl.text.toString()
            val dob = appDob.text.toString()
            val sex = appSex.text.toString()

            if (delDate.isEmpty()) {
                tilDelDate.error = "Please select delivery date"
                appDelDate.requestFocus()
                return

            }
            if (delTime.isEmpty()) {
                tilDelTime.error = "Please select delivery time"
                appDelTime.requestFocus()
                return

            }
            if (delMethod.isEmpty()) {
                tilDelivery.error = "Please select delivery method"
                appDelivery.requestFocus()
                return

            }
            if (isCs) {
                if (cs.isEmpty()) {
                    tilCs.error = "Please select CS reason"
                    appCs.requestFocus()
                    return

                }
            }
            if (vdrl.isEmpty()) {
                tilVdrl.error = "Please select VDRL"
                appVdrl.requestFocus()
                return

            }
            if (dob.isEmpty()) {
                tilDob.error = "Please select date of birth"
                appDob.requestFocus()
                return

            }
            if (sex.isEmpty()) {
                tilSex.error = "Please select gender"
                appSex.requestFocus()
                return

            }
            val bWeight = appBirthWeight.text.toString()
            val gestation = appGestation.text.toString()
            val apgarfive = appFiveScore.text.toString()
            val apgarten = appTenScore.text.toString()
            val bba = appBba.text.toString()

            if (bWeight.isEmpty()) {
                tilBirthWeight.error = "Please enter birth weight"
                appBirthWeight.requestFocus()
                return

            }
            if (gestation.isEmpty()) {
                tilGestation.error = "Please enter gestation"
                appGestation.requestFocus()
                return

            }
            if (apgarfive.isEmpty()) {
                tilFiveScore.error = "Enter score"
                appFiveScore.requestFocus()
                return

            }
            if (apgarten.isEmpty()) {
                tilTenScore.error = "Enter score"
                appTenScore.requestFocus()
                return

            }
            if (bba.isEmpty()) {
                tilBba.error = "Please select BBA"
                appBba.requestFocus()
                return

            }
            val head = appHead.text.toString()
            val inter = appInter.text.toString()
            val adWeight = appAdmWeight.text.toString()
            val admDate = appAdmDate.text.toString()
            val admTime = appAdmTime.text.toString()
            val remarks = appNotes.text.toString()


            if (head.isEmpty()) {
                tilHead.error = "enter head circumference"
                appHead.requestFocus()
                return

            }
            if (inter.isEmpty()) {
                tilInter.error = "enter interventions"
                appInter.requestFocus()
                return

            }
            if (adWeight.isEmpty()) {
                tilAdmWeight.error = "enter adm weight"
                appAdmWeight.requestFocus()
                return
            }
            if (admDate.isEmpty()) {
                tilAdmDate.error = "select adm date"
                appAdmDate.requestFocus()
                return
            }
            if (admTime.isEmpty()) {
                tilAdmTime.error = "Please select adm time"
                appAdmTime.requestFocus()
                return
            }
            if (remarks.isEmpty()) {
                tilNotes.error = "Please enter remarks"
                appNotes.requestFocus()
                return
            }

            val isValid = FormatHelper().isSimilarDay(delDate, dob)
            if (!isValid) {
                tilDob.error = "Please enter valid Dob"
                appDob.requestFocus()
                return
            }

            val isAllowed = FormatHelper().isLaterDay(admDate, dob)
            if (!isAllowed) {
                tilAdmDate.error = "Please enter valid adm date"
                appAdmDate.requestFocus()
                return
            }

            val pari12 = CodingObservation(PARITY, "Parity", pari1)
            val pari22 = CodingObservation("Parity-Two", "Second Parity", pari2)
            val pm = CodingObservation(PMTCT, "Pmtct", pmtct)
            val multiPreg = CodingObservation(MULTIPLE_PREGNANCY, "Multiple Pregnancy", multi)
            val mBirths = CodingObservation(MULTIPLE_BIRTH_TYPE, "Multiple Birth Types", births)
            val deDate = CodingObservation(DELIVERY_DATE, "Delivery Date", delDate)
            val deTime = CodingObservation("Delivery-Time", "Delivery Time", delTime)
            val deMethod = CodingObservation(DELIVERY_METHOD, "Delivery Method", delMethod)
            val csRe = CodingObservation(CS_REASON, "Cs Reason", cs)
            val vd = CodingObservation(VDRL, "VDRL", vdrl)
            val birthW = QuantityObservation(BIRTH_WEIGHT, "Birth Weight", bWeight, "gm")
            val ges = QuantityObservation(GESTATION, "Gestation", gestation, "wks")
            val five = QuantityObservation(APGAR_SCORE, "5 mins Score", apgarfive, "score")
            val ten = QuantityObservation("10-Score", "10 mins Score", apgarten, "score")
            val bornB = CodingObservation(BBA, "Born before Arrival", bba)
            val headC = QuantityObservation(HEAD_CIRCUMFERENCE, "Head Circumference", head, "cm")
            val ventions = CodingObservation(INTERVENTIONS, "Interventions", inter)
            val aWeight = QuantityObservation(ADMISSION_WEIGHT, "Admission Weight", adWeight, "gm")
            val aDate = CodingObservation(ADMISSION_DATE, "Admission Date", admDate)
            val aTime = CodingObservation("Admission-Time", "Admission Time", admTime)
            val aRemarks = CodingObservation(REMARKS, "Additional Notes", remarks)
            if (isMultiple) {
                dataCodes.add(mBirths)
            }
            if (isCs) {
                dataCodes.add(csRe)
            }
            dataQuantity.addAll(
                listOf(
                    birthW, ges, five, ten, headC, aWeight
                )
            )
            dataCodes.addAll(
                listOf(
                    pari12,
                    pari22,
                    pm,
                    multiPreg,
                    deDate,
                    deTime,
                    deMethod,
                    vd,
                    bornB,
                    ventions,
                    aDate,
                    aTime,
                    aRemarks
                )
            )

            val baby = Patient()
            val mother = Patient()
            val babyIp = generateUuid()
            try {

                val words = mumName.split("\\s".toRegex()).toTypedArray()
                val subjectReference = Reference("Patient/$babyIp")
                mother.nameFirstRep.family = words[2]
                mother.nameFirstRep.addGiven(words[0])
                mother.nameFirstRep.addGiven(words[1])
                mother.id = mumIp
                mother.active = true
                mother.linkFirstRep.other = subjectReference
                mother.gender = Enumerations.AdministrativeGender.FEMALE

                baby.id = babyIp
                baby.nameFirstRep.family = words[0]
                baby.nameFirstRep.addGiven("Baby")
                baby.active = true
                baby.birthDate = FormatHelper().dateOfBirthCustom(dob)
                baby.addressFirstRep.postalCode = SYNC_VALUE
                baby.addressFirstRep.state = SYNC_VALUE
                baby.gender = if (sex == "Male") {
                    Enumerations.AdministrativeGender.MALE
                } else {
                    Enumerations.AdministrativeGender.FEMALE
                }

                (activity as MainActivity).displayDialog()
                observeResourcesSaveAction(babyIp)
                CoroutineScope(Dispatchers.IO).launch {
                    val questionnaireFragment =
                        childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment
                    viewModel.customRegistration(
                        questionnaireFragment.getQuestionnaireResponse(),
                        baby,
                        mother,
                        babyIp,
                        dataCodes, dataQuantity
                    )
                }
            } catch (e: Exception) {
                Timber.e("Registration Exception ${e.localizedMessage}")
                tilMumName.error = "Please enter 3 names"
                appMumName.requestFocus()
                return
            }
        }
    }

    private fun observeResourcesSaveAction(patientId: String) {
        viewModel.customMessage.observe(viewLifecycleOwner) {

            if (it != null) {
                if (it.success) {
                    (activity as MainActivity).hideDialog()
                    val dialog =
                        SweetAlertDialog(requireContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                            .setTitleText("Success")
                            .setContentText(resources.getString(R.string.app_client_registered))
                            .setCustomImage(R.drawable.smile)
                            .setConfirmClickListener { sDialog ->
                                run {
                                    sDialog.dismiss()
                                    resetFields()
                                    val session = SessionData(
                                        patientId = patientId,
                                        status = false
                                    )
                                    val gson = Gson()
                                    val json = gson.toJson(session)
                                    FhirApplication.setDashboardActive(
                                        requireContext(),
                                        json
                                    )
                                    findNavController().navigate(
                                        CustomRegistrationFragmentDirections.navigateToBabyDashboard(
                                            patientId
                                        )
                                    )
                                }
                            }


                    dialog.setCancelable(false)
                    dialog.show()
                } else {

                    (activity as MainActivity).hideDialog()
                    return@observe
                }
            }
            (activity as MainActivity).hideDialog()

        }
    }

    private fun resetFields() {
        binding.apply {
            appMumName.setText("")
            appParity.setText("")
            appTwoParity.setText("")
            appIpNumber.setText("")
            appPmtct.setText("")
            appMulti.setText("")
            appBirthType.setText("")
            appDelivery.setText("")
            appCs.setText("")
            appVdrl.setText("")
            appSex.setText("")
            appBirthWeight.setText("")
            appGestation.setText("")
            appFiveScore.setText("")
            appTenScore.setText("")
            appBba.setText("")
            appHead.setText("")
            appInter.setText("")
            appAdmWeight.setText("")
            appNotes.setText("")
            appDelDate.setText("")
            appDob.setText("")
            appAdmDate.setText("")
            appDelTime.setText("")
            appAdmTime.setText("")
        }
    }

    private fun generateUuid(): String {
        return UUID.randomUUID().toString()
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

    private fun listenChanges(
        input: TextInputEditText,
        inputLayout: TextInputLayout,
        error: String
    ) {

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
                            inputLayout.error = null
                        } else {
                            inputLayout.error = error
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

    override fun onResume() {
        hideBottom()
        super.onResume()
    }

    private fun hideBottom() {

        (requireActivity() as MainActivity).showBottomNavigationView(View.GONE)
    }

    private fun onBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            showCancelScreenerQuestionnaireAlertDialog()
        }
    }

    private fun showCancelScreenerQuestionnaireAlertDialog() {
        SweetAlertDialog(activity, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Are you sure?")
            .setContentText(getString(R.string.cancel_questionnaire_message))
            .setConfirmText("Yes")
            .setConfirmClickListener { d ->
                d.dismiss()
                NavHostFragment.findNavController(this@CustomRegistrationFragment).navigateUp()
            }
            .setCancelText("No")
            .show()
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