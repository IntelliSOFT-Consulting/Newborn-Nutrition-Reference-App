package com.intellisoft.nndak.screens.dashboard.child

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.fhir.FhirEngine
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.databinding.FragmentBabyLactationBinding
import com.intellisoft.nndak.screens.dashboard.BaseFragment
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
    private var chart: BarChart? = null
    private val binding
        get() = _binding!!


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


        binding.apply {
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
                findNavController().navigate(BabyLactationFragmentDirections.navigateToFeeding(args.patientId))
            }
        }
        patientDetailsViewModel.getMumChild()
        patientDetailsViewModel.liveMumChild.observe(viewLifecycleOwner) { data ->

            if (data != null) {
                binding.apply {
                    val gest = data.dashboard.gestation ?: ""
                    val sta = data.status
                    val weight = data.birthWeight
                    val code = weight?.split("\\.".toRegex())?.toTypedArray()
                    bWeight = code?.get(0)?.toInt()!!

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

                    response.appIpNumber.text = data.assessment.breastfeedingBaby
                    response.appMotherName.text = data.assessment.breastProblems
                    response.appBabyName.text = data.assessment.contraindicated
                    tvTotalExpressed.text = data.assessment.totalExpressed


                    val isSepsis = data.dashboard.neonatalSepsis
                    if (isSepsis != "Yes") {
                        incDetails.appNeonatalSepsis.visibility = View.GONE
                        incDetails.tvNeonatalSepsis.visibility = View.GONE
                    }

                    val isAsphyxia = data.dashboard.asphyxia
                    if (isAsphyxia != "Yes") {
                        incDetails.appAsphyxia.visibility = View.GONE
                        incDetails.tvAsphyxia.visibility = View.GONE
                    }

                    val isJaundice = data.dashboard.jaundice
                    if (isJaundice != "Yes") {
                        incDetails.tvJaundice.visibility = View.GONE
                        incDetails.appJaundice.visibility = View.GONE
                    }

                    populateBarChart(data.assessment.weights)

                }
            }
        }


    }

    private fun populateBarChart(values: MutableList<Int>?) {
        val groupCount = 6
        val groupSpace = 0.08f
        val barSpace = 0.03f
        val barWidth = 0.2f

        val startYear = 2022
        val endYear: Int = +groupCount
        val iv: ArrayList<BarEntry> = ArrayList()
        val ebm: ArrayList<BarEntry> = ArrayList()
        val dhm: ArrayList<BarEntry> = ArrayList()
        if (values != null) {
            if (values.isNotEmpty()) {
                for ((i, entry) in values.withIndex()) {
                    val value = values[i].toFloat()
                    bWeight += 100
                    iv.add(BarEntry(i.toFloat(), value))
                    ebm.add(BarEntry(i.toFloat(), bWeight.toFloat()))
                    dhm.add(BarEntry(i.toFloat(), bWeight.toFloat()))
                }

                val fluids = BarDataSet(iv, "IV")
                fluids.setColors(Color.parseColor("#4472C4"))
                fluids.setDrawValues(false)

                val expressed = BarDataSet(ebm, "EBM")
                expressed.setColors(Color.parseColor("#ED7D31"))
                expressed.setDrawValues(false)

                val donor = BarDataSet(dhm, "DHM")
                donor.setColors(Color.parseColor("#A5A5A5"))
                donor.setDrawValues(false)

                val data = BarData(fluids, expressed, donor)
                data.setValueFormatter(LargeValueFormatter())

                binding.statusChart.data = data
                //setting the x-axis
                val xAxis: XAxis = binding.statusChart.xAxis
                //calling methods to hide x-axis gridlines
                binding.statusChart.axisLeft.setDrawGridLines(false)
                xAxis.setDrawGridLines(false)
                xAxis.setDrawAxisLine(false)
                xAxis.position = XAxis.XAxisPosition.BOTTOM


                binding.statusChart.legend.isEnabled = true
                binding.statusChart.description.isEnabled = false
                binding.statusChart.animateY(3000, Easing.EaseInSine)

                binding.statusChart.barData.barWidth = barWidth
                binding.statusChart.xAxis.axisMinimum = startYear.toFloat()
                binding.statusChart.xAxis.axisMaximum =
                    startYear + binding.statusChart.barData.getGroupWidth(
                        groupSpace,
                        barSpace
                    ) * groupCount
                binding.statusChart.groupBars(startYear.toFloat(), groupSpace, barSpace)


                val rightAxis: YAxis = binding.statusChart.axisRight
                rightAxis.setDrawGridLines(false)
                rightAxis.setDrawZeroLine(false)
                rightAxis.isGranularityEnabled = false
                rightAxis.isEnabled = false

                //refresh the chart
                binding.statusChart.invalidate()
            }
        }
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


}