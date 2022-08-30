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
import com.intellisoft.nndak.utils.showOptions
import com.intellisoft.nndak.utils.showPicker
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
            listenChanges(appParity, tilParity, "_")
            listenChanges(appIpNumber, tilIpNumber, "Enter IP Number")
            listenChanges(appPmtct, tilPmtct, "Select Status")
            listenChanges(appMulti, tilMulti, "Select Birth Type")
            listenChanges(appBirthType, tilBirthType, "Specify birth type")
            listenChanges(appDelivery, tilDelivery, "Select delivery method")
            listenChanges(appCs, tilCs, "Select reason")
            listenChanges(appVdrl, tilVdrl, "Select VDRL status")
            listenChanges(appSex, tilSex, "Select Baby's sex")
            listenMaxChanges(appBirthWeight, tilBirthWeight, "Enter birth weight", 400, 5000)
            listenMaxChanges(appGestation, tilGestation, "Enter gestation age", 22, 42)
            listenMaxChanges(appFiveScore, tilFiveScore, "enter score", 0, 10)
            listenMaxChanges(appTenScore, tilTenScore, "enter score", 1, 10)
            listenChanges(appBba, tilBba, "Select BBA")
            listenMaxChanges(appHead, tilHead, "Enter head circumference", 0, 50)
            listenChanges(appInter, tilInter, "Enter interventions")
            listenMaxChanges(appAdmWeight, tilAdmWeight, "Enter Admission weight", 400, 5000)
            listenChanges(appNotes, tilNotes, "Enter remarks")
            listenChanges(appDelDate, tilDelDate, "Select delivery date")
            listenChanges(appDob, tilDob, "Select date of birth")
            listenChanges(appAdmDate, tilAdmDate, "Select admission date")
            listenChanges(appDelTime, tilDelTime, "Select delivery time")
            listenChanges(appAdmTime, tilAdmTime, "Select admission time")

            /**
             * Dropdowns
             */
            showOptions(requireContext(), appPmtct, R.menu.pmtct)
            showMultiOptions(appMulti, R.menu.yesno)
            showDeliveryOptions(appDelivery, R.menu.delivery)
            showOptions(requireContext(), appBirthType, R.menu.birthtypes)
            showOptions(requireContext(), appCs, R.menu.reasons)
            showOptions(requireContext(), appVdrl, R.menu.pmtct)
            showOptions(requireContext(), appSex, R.menu.sex)
            showOptions(requireContext(), appBba, R.menu.yesno)

            /**
             * Date pickers
             */
            showPicker(requireContext(), appDelDate)
            showPicker(requireContext(), appDob)
            showPicker(requireContext(), appAdmDate)
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
            val mumIp = appIpNumber.text.toString()
            val pmtct = appPmtct.text.toString()
            val multi = appMulti.text.toString()
            val births = appBirthType.text.toString()

            val delDate = appDelDate.text.toString()
            val delTime = appDelTime.text.toString()
            val delMethod = appDelivery.text.toString()
            val cs = appCs.text.toString()
            val vdrl = appVdrl.text.toString()
            val dob = appDob.text.toString()
            val sex = appSex.text.toString()

            /**
             * Validate all the fields at once
             */
            if (mumName.isEmpty() && pari1.isEmpty() && mumIp.isEmpty() &&
                pmtct.isEmpty() && multi.isEmpty() && delDate.isEmpty() &&
                delTime.isEmpty() && delMethod.isEmpty() && cs.isEmpty() &&
                vdrl.isEmpty() && dob.isEmpty()&&sex.isEmpty()

                    ) {
                tilMumName.error = "required field"
                tilParity.error = "required field"
                tilIpNumber.error = "required field"
                tilPmtct.error = "required field"
                tilMulti.error = "required field"
                tilDelDate.error = "required field"
                tilDelTime.error = "required field"
                tilDelivery.error = "required field"
                tilCs.error = "required field"
                tilVdrl.error = "required field"
                tilDob.error = "required field"
                tilSex.error = "required field"



                return
            }



            if (mumName.isEmpty()) {
                tilMumName.error = "Enter mother's name"
                appMumName.requestFocus()
                return

            }
            if (pari1.isEmpty()) {
                tilParity.error = "Enter parity"
                appParity.requestFocus()
                return

            }

            if (mumIp.isEmpty()) {
                tilIpNumber.error = "Enter Ip Number"
                appIpNumber.requestFocus()
                return

            }
            if (pmtct.isEmpty()) {
                tilPmtct.error = "Select PMTCT"
                appPmtct.requestFocus()
                return

            }
            if (multi.isEmpty()) {
                tilMulti.error = "Select Pregnancies"
                appMulti.requestFocus()
                return

            }
            if (isMultiple) {

                if (births.isEmpty()) {
                    tilBirthType.error = "Select birth type"
                    appBirthType.requestFocus()
                    return

                }
            }

            if (delDate.isEmpty()) {
                tilDelDate.error = "Select delivery date"
                appDelDate.requestFocus()
                return

            }
            if (delTime.isEmpty()) {
                tilDelTime.error = "Select delivery time"
                appDelTime.requestFocus()
                return

            }
            if (delMethod.isEmpty()) {
                tilDelivery.error = "Select delivery method"
                appDelivery.requestFocus()
                return

            }
            if (isCs) {
                if (cs.isEmpty()) {
                    tilCs.error = "Select CS reason"
                    appCs.requestFocus()
                    return

                }
            }
            if (vdrl.isEmpty()) {
                tilVdrl.error = "Select VDRL"
                appVdrl.requestFocus()
                return

            }
            if (dob.isEmpty()) {
                tilDob.error = "Select date of birth"
                appDob.requestFocus()
                return

            }
            if (sex.isEmpty()) {
                tilSex.error = "Select gender"
                appSex.requestFocus()
                return

            }
            val bWeight = appBirthWeight.text.toString()
            val gestation = appGestation.text.toString()
            val apgarfive = appFiveScore.text.toString()
            val apgarten = appTenScore.text.toString()
            val bba = appBba.text.toString()

            if (bWeight.isEmpty()) {
                tilBirthWeight.error = "Enter birth weight"
                appBirthWeight.requestFocus()
                return

            }
            if (gestation.isEmpty()) {
                tilGestation.error = "Enter gestation"
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
                tilBba.error = "Select BBA"
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
                tilHead.error = "Enter head circumference"
                appHead.requestFocus()
                return

            }
            if (inter.isEmpty()) {
                tilInter.error = "Enter interventions"
                appInter.requestFocus()
                return

            }
            if (adWeight.isEmpty()) {
                tilAdmWeight.error = "Enter adm weight"
                appAdmWeight.requestFocus()
                return
            }
            if (admDate.isEmpty()) {
                tilAdmDate.error = "Select adm date"
                appAdmDate.requestFocus()
                return
            }
            if (admTime.isEmpty()) {
                tilAdmTime.error = "Select adm time"
                appAdmTime.requestFocus()
                return
            }
            if (remarks.isEmpty()) {
                tilNotes.error = "Enter remarks"
                appNotes.requestFocus()
                return
            }

            val isValid = FormatHelper().isSimilarDay(delDate, dob)
            if (!isValid) {
                tilDob.error = "Enter valid Dob"
                appDob.requestFocus()
                return
            }

            val isAllowed = FormatHelper().isLaterDay(admDate, dob)
            if (!isAllowed) {
                tilAdmDate.error = "Please enter valid adm date"
                appAdmDate.requestFocus()
                return
            }


            if (!withinRange(appBirthWeight, tilBirthWeight, "Enter weight", 400, 5000)) {
                tilBirthWeight.requestFocus()
                return
            }
            if (!withinRange(appGestation, tilGestation, "Enter gestation", 22, 42)) {
                appGestation.requestFocus()
                return
            }
            if (!withinRange(appFiveScore, tilFiveScore, "Enter 5 min score", 0, 10)) {
                appFiveScore.requestFocus()
                return
            }
            if (!withinRange(appTenScore, tilTenScore, "Enter 10 min score", 1, 10)) {
                appTenScore.requestFocus()
                return
            }
            if (!withinRange(appHead, tilHead, "Enter circumference", 0, 50)) {
                appHead.requestFocus()
                return
            }
            if (!withinRange(appAdmWeight, tilAdmWeight, "Enter weight", 400, 5000)) {
                appAdmWeight.requestFocus()
                return
            }

            val pari12 = CodingObservation(PARITY, "Parity", pari1)
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
                val mum = mumName.trim()

                val words = mum.split("\\s".toRegex()).toTypedArray()
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

    private fun withinRange(
        input: TextInputEditText,
        inputLayout: TextInputLayout,
        error: String,
        min: Int,
        max: Int
    ): Boolean {
        try {
            val value = input.text.toString()
            if (value.isNotEmpty()) {
                val parsed = value.toDouble()
                val minimum = min.toDouble()
                val maximum = max.toDouble()
                if (parsed < minimum) {
                    inputLayout.error = "Minimum allowed is $minimum"
                    return false
                } else if (parsed > maximum) {
                    inputLayout.error = "Maximum allowed is $maximum"
                    return false
                } else {
                    inputLayout.error = null
                    return true
                }
            } else {
                inputLayout.error = error
                return false
            }
        } catch (e: Exception) {
            return false
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
            tilMumName.error = ""
            appParity.setText("")
            tilParity.error = ""
            appIpNumber.setText("")
            tilIpNumber.error = ""
            appPmtct.setText("")
            tilPmtct.error = ""
            appMulti.setText("")
            tilMulti.error = ""
            appBirthType.setText("")
            tilBirthType.error = ""
            appDelivery.setText("")
            tilDelivery.error = ""
            appCs.setText("")
            tilCs.error = ""
            appVdrl.setText("")
            tilVdrl.error = ""
            appSex.setText("")
            tilSex.error = ""
            appBirthWeight.setText("")
            tilBirthWeight.error = ""
            appGestation.setText("")
            tilGestation.error = ""
            appFiveScore.setText("")
            tilFiveScore.error = ""
            appTenScore.setText("")
            tilTenScore.error = ""
            appBba.setText("")
            tilBba.error = ""
            appHead.setText("")
            tilHead.error = ""
            appInter.setText("")
            tilInter.error = ""
            appAdmWeight.setText("")
            tilAdmWeight.error = ""
            appNotes.setText("")
            tilNotes.error = ""
            appDelDate.setText("")
            tilDelDate.error = ""
            appDob.setText("")
            tilDob.error = ""
            appAdmDate.setText("")
            tilAdmDate.error = ""
            appDelTime.setText("")
            tilDelTime.error = ""
            appAdmTime.setText("")
            tilAdmTime.error = ""
        }
    }

    private fun generateUuid(): String {
        return UUID.randomUUID().toString()
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

    private fun listenMaxChanges(
        input: TextInputEditText,
        inputLayout: TextInputLayout,
        error: String,
        min: Int,
        max: Int
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
                            if (input.text.toString().isNotEmpty()) {
                                val parsed = newValue.toDouble()
                                val minimum = min.toDouble()
                                val maximum = max.toDouble()
                                if (parsed < minimum) {
                                    inputLayout.error = "Minimum allowed is $minimum"
                                } else if (parsed > maximum) {
                                    inputLayout.error = "Maximum allowed is $maximum"
                                } else {
                                    inputLayout.error = null
                                }
                            } else {
                                inputLayout.error = null
                            }
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