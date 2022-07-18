package com.intellisoft.nndak.screens.dashboard.child

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cn.pedant.SweetAlert.SweetAlertDialog
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.fhir.FhirEngine
import com.google.gson.Gson
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.charts.MilkExpression
import com.intellisoft.nndak.data.RestManager
import com.intellisoft.nndak.databinding.FragmentBabyLactationBinding
import com.intellisoft.nndak.logic.Logics.Companion.ADMINISTRATOR
import com.intellisoft.nndak.logic.Logics.Companion.HMB_ASSISTANT
import com.intellisoft.nndak.logic.Logics.Companion.NEONATOLOGIST
import com.intellisoft.nndak.logic.Logics.Companion.NURSE
import com.intellisoft.nndak.logic.Logics.Companion.NUTRITION_OFFICER
import com.intellisoft.nndak.logic.Logics.Companion.PEDIATRICIAN
import com.intellisoft.nndak.utils.formatTime
import com.intellisoft.nndak.utils.getPastHoursOnIntervalOf
import com.intellisoft.nndak.utils.isNetworkAvailable
import com.intellisoft.nndak.utils.loadTime
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import timber.log.Timber


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BabyLactationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BabyLactationFragment : Fragment() {
    private var _binding: FragmentBabyLactationBinding? = null
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val args: BabyLactationFragmentArgs by navArgs()
    private var bWeight: Int = 0
    private var contra: String = "No"
    private val binding
        get() = _binding!!

    private val apiService = RestManager()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBabyLactationBinding.inflate(inflater, container, false)
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
//        syncLocal()

        binding.apply {
            lnParent.visibility = View.GONE
            lnStatus.visibility = View.GONE
            loading.visibility = View.VISIBLE
        }



        binding.apply {
            val allowed = validatePermission()
            breadcrumb.page.text =
                Html.fromHtml("Baby Panel > <font color=\"#37379B\">Lactation Support</font>")
            breadcrumb.page.setOnClickListener {
                findNavController().navigateUp()
            }


            /**
             * Three Columns
             */
            status.lnParent.weightSum = 3F
            response.lnParent.weightSum = 3F

            /**
             * Update Titles
             */
            status.appIpNumber.text = getString(R.string.mother_breastfeeding_baby)
            status.appMotherName.text = getString(R.string.breast_problems)
            status.appBabyName.text = getString(R.string.mother_contraindicated)

            status.appBabyAge.visibility = View.GONE
            status.appDhmType.visibility = View.GONE
            status.appConsent.visibility = View.GONE
            status.appAction.visibility = View.GONE
            /**
             * Update Responses
             */

            response.appIpNumber.text = null
            response.appMotherName.text = null
            response.appBabyName.text = null

            response.appBabyAge.visibility = View.GONE
            response.appDhmType.visibility = View.GONE
            response.appConsent.visibility = View.GONE
            response.appAction.visibility = View.GONE


            actionProvideSupport.setOnClickListener {
                if (allowed) {
                    findNavController().navigate(
                        BabyLactationFragmentDirections.navigateToFeeding(
                            args.patientId, contra
                        )
                    )
                } else {
                    accessDenied()
                }
            }
        }
        patientDetailsViewModel.getMumChild()
        patientDetailsViewModel.liveMumChild.observe(viewLifecycleOwner) { data ->

            if (data != null) {
                binding.apply {

                    lnParent.visibility = View.VISIBLE
                    lnStatus.visibility = View.VISIBLE
                    loading.visibility = View.GONE


                    val gest = data.dashboard.gestation ?: ""
                    val sta = data.status
                    val weight = data.birthWeight
                    try {
                        val code = weight?.split("\\.".toRegex())?.toTypedArray()
                        bWeight = code?.get(0)?.toInt()!!
                    } catch (e: Exception) {
                    }

                    incDetails.tvBabyName.text = data.babyName
                    incDetails.tvMumName.text = data.motherName
                    incDetails.appBirthWeight.text = data.birthWeight
                    incDetails.appGestation.text = "$gest-$sta"
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

                    /**
                     * Mum Details
                     */
                    incMum.tvMumName.text = data.motherName
                    incMum.appIpNumber.text = data.motherIp
                    incMum.appDeliveryMethod.text = data.mother.deliveryMethod
                    incMum.appParity.text = data.mother.parity
                    incMum.appPmctcStatus.text = data.mother.pmtctStatus
                    incMum.appDeliveryDate.text = data.mother.deliveryDate
                    incMum.appMultiplePregnancy.text = data.mother.multiPregnancy
                    val mumWell = if (data.mother.motherStatus == "Yes") {
                        "Well"
                    } else {
                        "Unwell"
                    }
                    incMum.appMumLocation.text = data.mother.motherLocation
                    incMum.appMotherStatus.text = mumWell

                    response.appIpNumber.text = data.assessment.breastfeedingBaby
                    response.appMotherName.text = data.assessment.breastProblems
                    response.appBabyName.text = data.assessment.contraindicated
                    contra = data.assessment.contraindicated.toString()

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
        patientDetailsViewModel.getExpressions()
        patientDetailsViewModel.liveExpressions.observe(viewLifecycleOwner) { expression ->
            if (expression != null) {
                populateBarChart(expression)
                binding.apply {

                    tvTotalExpressed.text = "${expression.totalFeed} mls"
                }
            }
        }
        /* if (isNetworkAvailable(requireContext())) {
             loadData()
         } else {
             syncLocal()
         }*/

    }


    private fun validatePermission(): Boolean {

        val role = (requireActivity() as MainActivity).retrieveUser(true)
        if (role.isNotEmpty()) {
            return role == ADMINISTRATOR
                    || role == NUTRITION_OFFICER
                    || role == PEDIATRICIAN
                    || role == NEONATOLOGIST
                    || role == NURSE
        }
        return false
    }

    private fun accessDenied() {
        SweetAlertDialog(requireContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
            .setTitleText("Access Denied!!")
            .setContentText("You are not Authorized")
            .setCustomImage(R.drawable.smile)
            .show()
    }

    private fun loadData() {
        apiService.loadExpressedMilk(requireContext(), args.patientId) {
            if (it != null) {
                val gson = Gson()
                val json = gson.toJson(it)
                try {
                    FhirApplication.updateLocalFeeding(requireContext(), json)
                    populateBarChart(it)
                } catch (e: Exception) {

                }
            } else {
                syncLocal()
            }
        }
    }

    private fun syncLocal() {
        val data = FhirApplication.getExpressions(requireContext())
        if (data != null) {
            val gson = Gson()

            Timber.e("Local Sync Dara $data")
            try {
                val it: MilkExpression = gson.fromJson(data, MilkExpression::class.java)
                populateBarChart(it)
            } catch (e: Exception) {
                Timber.e("Local Sync Error ${e.localizedMessage}")
            }
        }
    }


    private fun populateBarChart(it: MilkExpression) {

        val good: ArrayList<BarEntry> = ArrayList()
        val better: ArrayList<BarEntry> = ArrayList()
        val best: ArrayList<BarEntry> = ArrayList()

        val intervals = ArrayList<String>()
        for ((i, entry) in it.data.withIndex()) {
            intervals.add(entry.time)
            if (entry.amount.toFloat() < 30) {
                good.add(BarEntry(i.toFloat(), entry.amount.toFloat()))
            } else if (entry.amount.toFloat() < 99) {
                better.add(BarEntry(i.toFloat(), entry.amount.toFloat()))
            } else {
                best.add(BarEntry(i.toFloat(), entry.amount.toFloat()))
            }
        }

        val fluids = BarDataSet(good, "")
        fluids.setColors(Color.parseColor("#da1e27"))
        fluids.setDrawValues(false)

        val fluids1 = BarDataSet(better, "")
        fluids1.setColors(Color.parseColor("#0043cd"))
        fluids1.setDrawValues(false)

        val fluids2 = BarDataSet(best, "")
        fluids2.setColors(Color.parseColor("#24a047"))
        fluids2.setDrawValues(false)

        val data = BarData(fluids, fluids1, fluids2)
        data.setValueFormatter(LargeValueFormatter())

        binding.apply {

            val xAxis: XAxis = statusChart.xAxis
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.labelRotationAngle = -45f
            xAxis.mAxisMinimum = 1f
            xAxis.valueFormatter = IndexAxisValueFormatter(intervals)

            statusChart.axisLeft.setDrawGridLines(false)
            statusChart.legend.isEnabled = true

            //remove description label
            statusChart.description.isEnabled = false
            statusChart.isDragEnabled = true
            statusChart.setScaleEnabled(true)
            statusChart.description.text = "Age (Days)"
            //add animation
            statusChart.animateX(1000, Easing.EaseInSine)
            statusChart.data = data

            val leftAxis: YAxis = statusChart.axisLeft
            leftAxis.axisMinimum = 0f
            leftAxis.setDrawGridLines(true)
            leftAxis.isGranularityEnabled = false

            val rightAxis: YAxis = statusChart.axisRight
            rightAxis.setDrawGridLines(false)
            rightAxis.setDrawZeroLine(false)
            rightAxis.isGranularityEnabled = false
            rightAxis.isEnabled = false

            //refresh
            statusChart.invalidate()

        }

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
            R.id.menu_profile -> {
                (requireActivity() as MainActivity).navigate(R.id.profileFragment)
                return true
            }
            R.id.menu_notification -> {
                (requireActivity() as MainActivity).navigate(R.id.notificationFragment)
                return true
            }
            else -> false
        }
    }

}