package com.intellisoft.nndak.screens.dashboard.child

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView.BufferType
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.google.android.fhir.FhirEngine
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.databinding.FragmentBabyDashboardBinding
import com.intellisoft.nndak.utils.extractUnits
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
 * Use the [BabyDashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BabyDashboardFragment : Fragment() {
    private
    var _binding: FragmentBabyDashboardBinding? = null
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val args: BabyAssessmentFragmentArgs by navArgs()
    private val viewModel: ScreenerViewModel by viewModels()
    private var bWeight: Int = 0
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBabyDashboardBinding.inflate(inflater, container, false)
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

        binding.apply {
            breadcrumb.page.text =
                Html.fromHtml("Babies > Baby Profile > <font color=\"#37379B\">Baby Dashboard</font>")
            breadcrumb.page.setOnClickListener {
                findNavController().navigateUp()
            }
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
        patientDetailsViewModel.getMumChild()
        patientDetailsViewModel.getCurrentPrescriptions()
        patientDetailsViewModel.liveMumChild.observe(viewLifecycleOwner) {

            if (it != null) {

                binding.apply {
                    try {
                        val gest = it.dashboard.gestation ?: ""
                        val weight = it.birthWeight
                        val code = weight?.split("\\.".toRegex())?.toTypedArray()
                        bWeight = code?.get(0)?.toInt()!!

                        val status = it.status
                        incDetails.tvBabyName.text = it.babyName
                        incDetails.tvMumName.text = it.motherName
                        incDetails.appBirthWeight.text = it.birthWeight
                        incDetails.appGestation.text = "$gest-$status"
                        incDetails.appApgarScore.text = it.dashboard.apgarScore ?: ""
                        incDetails.appMumIp.text = it.motherIp
                        incDetails.appBabyWell.text = it.dashboard.babyWell ?: ""
                        incDetails.appAsphyxia.text = it.dashboard.asphyxia ?: ""
                        incDetails.appNeonatalSepsis.text = it.dashboard.neonatalSepsis ?: ""
                        incDetails.appJaundice.text = it.dashboard.jaundice ?: ""
                        incDetails.appBirthDate.text = it.dashboard.dateOfBirth ?: ""
                        incDetails.appLifeDay.text = it.dashboard.dayOfLife ?: ""
                        incDetails.appAdmDate.text = it.dashboard.dateOfAdm ?: ""

                        tvCurrentWeight.text = it.dashboard.cWeight ?: ""
                        tvMotherMilk.text = it.dashboard.motherMilk ?: ""


                        val isSepsis = it.dashboard.neonatalSepsis
                        val isAsphyxia = it.dashboard.asphyxia
                        val isJaundice = it.dashboard.jaundice


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


                        lineChart(it.assessment.weights)
                        barGraph(it.assessment.weights)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        patientDetailsViewModel.livePrescriptionsData.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.isNotEmpty()) {
                    binding.apply {
                        tvTotalVolume.text = it.first().feedsGiven
                        tvExpressionNumber.text = it.first().expressions

                        /**
                         * Calculate Rate
                         */
                        var total = it.first().totalVolume
                        var given = it.first().feedsGiven
                        try {
                            total = extractUnits(total.toString())
                            given = extractUnits(given.toString())

                            val percentage = (given.toDouble() / total.toDouble()) * 100

                            tvFeedAverage.text="${percentage.toInt()} %"

                            Timber.e("Feeds Given $total Taken $given Percentage ${percentage.toInt()}" )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }


                    }
                }

            }
        }

    }

    private fun lineChart(values: MutableList<Int>?) {
        if (values != null) {
            if (values.isNotEmpty()) {
                val actualEntries: ArrayList<Entry> = ArrayList()
                val projectedEntries: ArrayList<Entry> = ArrayList()

                for ((i, entry) in values.withIndex()) {
                    val value = values[i].toFloat()
                    bWeight += 100
                    actualEntries.add(Entry(i.toFloat(), value))
                    projectedEntries.add(Entry(i.toFloat(), bWeight.toFloat()))
                }
                val actual = LineDataSet(actualEntries, "Actual Weight")
                actual.setColors(Color.parseColor("#4472C4"))
                actual.setDrawCircleHole(false)
                actual.setDrawValues(false)
                actual.setDrawCircles(false)
                actual.mode = LineDataSet.Mode.CUBIC_BEZIER
                actual.lineWidth = 5f

                val projected = LineDataSet(projectedEntries, "Projected Weight")
                projected.setColors(Color.parseColor("#ED7D31"))
                projected.setDrawCircleHole(false)
                projected.setDrawValues(false)
                projected.setDrawCircles(false)
                projected.mode = LineDataSet.Mode.LINEAR
                projected.lineWidth = 5f

                val data = LineData(actual, projected)
                binding.growthChart.axisLeft.setDrawGridLines(false)

                val xAxis: XAxis = binding.growthChart.xAxis
                xAxis.setDrawGridLines(false)
                xAxis.setDrawAxisLine(false)
                xAxis.position = XAxis.XAxisPosition.BOTTOM


                binding.growthChart.legend.isEnabled = true

                //remove description label
                binding.growthChart.description.isEnabled = false
                binding.growthChart.isDragEnabled = true
                binding.growthChart.setScaleEnabled(true)
                binding.growthChart.description.text = "Age (Days)"

                //add animation
                binding.growthChart.animateX(1000, Easing.EaseInSine)
                binding.growthChart.data = data
                val leftAxis: YAxis = binding.growthChart.axisLeft
                leftAxis.axisMinimum = 0f
                leftAxis.setDrawGridLines(true)
                leftAxis.isGranularityEnabled = true


                val rightAxis: YAxis = binding.growthChart.axisRight
                rightAxis.setDrawGridLines(false)
                rightAxis.setDrawZeroLine(false)
                rightAxis.isGranularityEnabled = false
                rightAxis.isEnabled = false
                //refresh
                binding.growthChart.invalidate()
            }
        }

    }

    private fun barGraph(values: MutableList<Int>?) {
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

                binding.feedsChart.data = data
                //setting the x-axis
                val xAxis: XAxis = binding.feedsChart.xAxis
                //calling methods to hide x-axis gridlines
                binding.feedsChart.axisLeft.setDrawGridLines(false)
                xAxis.setDrawGridLines(false)
                xAxis.setDrawAxisLine(false)
                xAxis.position = XAxis.XAxisPosition.BOTTOM


                binding.feedsChart.legend.isEnabled = true
                binding.feedsChart.description.isEnabled = false
                binding.feedsChart.animateY(3000, Easing.EaseInSine)

                binding.feedsChart.barData.barWidth = barWidth
                binding.feedsChart.xAxis.axisMinimum = startYear.toFloat()
                binding.feedsChart.xAxis.axisMaximum =
                    startYear + binding.feedsChart.barData.getGroupWidth(
                        groupSpace,
                        barSpace
                    ) * groupCount
                binding.feedsChart.groupBars(startYear.toFloat(), groupSpace, barSpace)


                val rightAxis: YAxis = binding.feedsChart.axisRight
                rightAxis.setDrawGridLines(false)
                rightAxis.setDrawZeroLine(false)
                rightAxis.isGranularityEnabled = false
                rightAxis.isEnabled = false

                //refresh the chart
                binding.feedsChart.invalidate()
            }
        }
    }

    private fun barGraphAlt(values: MutableList<Int>?) {
        if (values != null) {
            if (values.isNotEmpty()) {
                val iv: ArrayList<BarEntry> = ArrayList()
                val ebm: ArrayList<BarEntry> = ArrayList()
                val dhm: ArrayList<BarEntry> = ArrayList()

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
                binding.feedsChart.axisLeft.setDrawGridLines(false)

                val xAxis: XAxis = binding.feedsChart.xAxis
                xAxis.setDrawGridLines(false)
                xAxis.setDrawAxisLine(false)
                xAxis.position = XAxis.XAxisPosition.BOTTOM

                val rightAxis: YAxis = binding.feedsChart.axisRight
                rightAxis.setLabelCount(5, false)
                rightAxis.spaceTop = 15f
                rightAxis.isEnabled = false

                binding.feedsChart.legend.isEnabled = true

                //remove description label
                binding.feedsChart.description.isEnabled = false
                binding.feedsChart.isDragEnabled = true
                binding.feedsChart.setScaleEnabled(true)
                //add animation
                binding.feedsChart.animateX(1000, Easing.EaseInSine)
                binding.feedsChart.data = data
                val leftAxis: YAxis = binding.feedsChart.axisLeft
                leftAxis.axisMinimum = 0f
                leftAxis.setDrawGridLines(true)
                leftAxis.isGranularityEnabled = true


                //refresh
                binding.feedsChart.invalidate()
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