package com.intellisoft.nndak.screens.dashboard.child

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.*
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
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.google.android.fhir.FhirEngine
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.charts.*
import com.intellisoft.nndak.data.RestManager
import com.intellisoft.nndak.databinding.FragmentBabyDashboardBinding
import com.intellisoft.nndak.logic.DataSort.Companion.calculateGestationDays
import com.intellisoft.nndak.logic.DataSort.Companion.extractDaysData
import com.intellisoft.nndak.logic.DataSort.Companion.regulateProjection
import com.intellisoft.nndak.utils.extractUnits
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import com.intellisoft.nndak.viewmodels.ScreenerViewModel
import timber.log.Timber
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


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

    private val apiService = RestManager()
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

        binding.apply {
            incDetails.pbLoading.visibility = View.VISIBLE
            incDetails.lnBody.visibility = View.GONE
        }
        patientDetailsViewModel.getMumChild()
        patientDetailsViewModel.getCurrentPrescriptions()
        patientDetailsViewModel.liveMumChild.observe(viewLifecycleOwner) {

            if (it != null) {

                binding.apply {
                    incDetails.pbLoading.visibility = View.GONE
                    incDetails.lnBody.visibility = View.VISIBLE
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

                        tvMotherMilk.text = it.dashboard.motherMilk ?: ""


                        val isSepsis = it.dashboard.neonatalSepsis
                        val isAsphyxia = it.dashboard.asphyxia
                        val isJaundice = it.dashboard.jaundice

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

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        patientDetailsViewModel.feedsDistribution()
        patientDetailsViewModel.liveFeeds.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.tvTotalVolume.text = it.totalFeed
                binding.tvExpressionNumber.text = it.varianceAmount
                barGraph(it)
            }
        }
        patientDetailsViewModel.activeBabyWeights()
        patientDetailsViewModel.liveWeights.observe(viewLifecycleOwner) {
            if (it != null) {
                // populateLineChart(it)

                standardCharts(it)
            }
        }
        patientDetailsViewModel.livePrescriptionsData.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.isNotEmpty()) {
                    binding.apply {
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

                            tvFeedAverage.text = "${percentage.toInt()} %"

                            Timber.e("Feeds Given $total Taken $given Percentage ${percentage.toInt()}")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                }

            }
        }

    }

    private fun standardCharts(it: WeightsData) {
        try {
            var standard = "boy.json"
            if (it.babyGender == "female") {
                standard = "girl.json"
            }
            val jsonFileString = getJsonDataFromAsset(requireContext(), standard)
            val gson = Gson()
            val listGrowthType = object : TypeToken<List<GrowthData>>() {}.type
            val growths: List<GrowthData> = gson.fromJson(jsonFileString, listGrowthType)
            growths.forEachIndexed { idx, growth -> Timber.e("Growth Item $idx:\n$growth") }
            populateLineChart(it, growths)
        } catch (e: Exception) {
            Timber.e("Growth Exception ${e.localizedMessage}")
        }
    }

    private fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            Timber.e("Growth Exception ${ioException.localizedMessage}")
            return null
        }
        return jsonString
    }


    private fun barGraph(it: FeedsDistribution) {

        val groupCount = 8
        val groupSpace = 0.15f
        val barSpace = 0.05f
        val barWidth = 0.25f
        val iv: ArrayList<BarEntry> = ArrayList()
        val ebm: ArrayList<BarEntry> = ArrayList()
        val dhm: ArrayList<BarEntry> = ArrayList()

        val intervals = ArrayList<String>()
        for ((i, entry) in it.data.withIndex()) {
            intervals.add(entry.time)
            iv.add(BarEntry(i.toFloat(), entry.ivVolume.toFloat()))
            ebm.add(BarEntry(i.toFloat(), entry.ebmVolume.toFloat()))
            dhm.add(BarEntry(i.toFloat(), entry.dhmVolume.toFloat()))


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

        binding.apply {
            feedsChart.data = data
            val xAxis: XAxis = feedsChart.xAxis
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.labelRotationAngle = -45f
            xAxis.mAxisMinimum = 0f
//            xAxis.setCenterAxisLabels(true)

            xAxis.valueFormatter = IndexAxisValueFormatter(intervals)

            feedsChart.axisLeft.setDrawGridLines(false)
            feedsChart.legend.isEnabled = true

            //remove description label
            feedsChart.description.isEnabled = false
            feedsChart.isDragEnabled = true
            feedsChart.setScaleEnabled(true)
            feedsChart.description.text = "Age (Days)"
            //add animation
            feedsChart.animateX(1000, Easing.EaseInSine)
            feedsChart.data = data

            val leftAxis: YAxis = feedsChart.axisLeft
            leftAxis.axisMinimum = 0f
            leftAxis.setDrawGridLines(true)
            leftAxis.isGranularityEnabled = false

            val rightAxis: YAxis = feedsChart.axisRight
            rightAxis.setDrawGridLines(false)
            rightAxis.setDrawZeroLine(false)
            rightAxis.isGranularityEnabled = false
            rightAxis.isEnabled = false


            feedsChart.barData.barWidth = barWidth
            feedsChart.xAxis.axisMaximum =
                0f + feedsChart.barData.getGroupWidth(
                    groupSpace,
                    barSpace
                ) * groupCount
            feedsChart.groupBars(-1f, groupSpace, barSpace)

            //refresh
            feedsChart.invalidate()

        }

    }

    /*    private fun populateLineChart(values: WeightsData) {*/
    private fun populateLineChartOriginal(values: WeightsData, growths: List<GrowthData>) {

        val intervals = ArrayList<String>()
        val actualEntries: ArrayList<Entry> = ArrayList()
        val projectedEntries: ArrayList<Entry> = ArrayList()
        val one: ArrayList<Entry> = ArrayList()

        for ((i, entry) in growths.withIndex()) {
            intervals.add(entry.age.toString())
            val option = entry.data[i].option
            if (option == "one") {
                one.add(Entry(i.toFloat(), entry.data[i].value.toFloat()))
            }
            //  actualEntries.add(Entry(i.toFloat(), entry.data[i]..toFloat()))
            //  projectedEntries.add(Entry(i.toFloat(), entry.projected.toFloat()))
        }
        /*   val actual = LineDataSet(actualEntries, "Actual Weight")*/
        val actual = LineDataSet(one, "Actual Weight")
        actual.setColors(Color.parseColor("#4472C4"))
        actual.setDrawCircleHole(false)
        actual.setDrawValues(false)
        actual.setDrawCircles(true)
        actual.circleRadius = 5f
        actual.mode = LineDataSet.Mode.CUBIC_BEZIER
        actual.lineWidth = 5f

        val projected = LineDataSet(projectedEntries, "Projected Weight")
        projected.setColors(Color.parseColor("#ED7D31"))
        projected.setDrawCircleHole(false)
        projected.setDrawValues(false)
        projected.setDrawCircles(false)
        projected.mode = LineDataSet.Mode.CUBIC_BEZIER
        projected.lineWidth = 5f

        val data = LineData(actual, projected)
        binding.growthChart.axisLeft.setDrawGridLines(false)

        val xAxis: XAxis = binding.growthChart.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.mAxisMinimum = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(intervals)
        xAxis.setLabelCount(values.data.size, true)

        binding.growthChart.legend.isEnabled = true

        //remove description label
        binding.growthChart.description.isEnabled = true
        binding.growthChart.isDragEnabled = false
        binding.growthChart.setScaleEnabled(false)
        binding.growthChart.description.text = "Age (Days)"
        binding.growthChart.description.setPosition(0f, 10f)

        //add animation
        binding.growthChart.animateX(1000, Easing.EaseInSine)
        binding.growthChart.data = data
        val leftAxis: YAxis = binding.growthChart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.mAxisMaximum = 12f
        leftAxis.labelCount = 6
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

    private fun populateLineChart(values: WeightsData, growths: List<GrowthData>) {
        binding.apply {
            tvCurrentWeight.text = "${values.currentWeight} gm"
        }
        val totalDays = calculateGestationDays(values)
        val intervals = ArrayList<String>()
        val babyWeight: ArrayList<Entry> = ArrayList()
        val one: ArrayList<Entry> = ArrayList()
        val three: ArrayList<Entry> = ArrayList()
        val four: ArrayList<Entry> = ArrayList()
        val five: ArrayList<Entry> = ArrayList()
        val seven: ArrayList<Entry> = ArrayList()

        val chartData = extractDaysData(totalDays, values, growths)
        val projectWeight = regulateProjection(chartData.projectedWeight)


        /**
         * Refined Weights
         */
        /*  for ((i, entry) in chartData.actualWeight.data.withIndex()) {
              intervals.add(entry.day)

              val equivalent = projectWeight[i].value
              babyWeight.add(Entry(i.toFloat(), entry.actual.toFloat()))
              one.add(Entry(i.toFloat(), equivalent.toFloat()))
              *//*  three.add(Entry(i.toFloat(), entry.data[2].value.toFloat()))
            four.add(Entry(i.toFloat(), entry.data[3].value.toFloat()))
            five.add(Entry(i.toFloat(), entry.data[4].value.toFloat()))
            seven.add(Entry(i.toFloat(), entry.data[6].value.toFloat()))*//*


        }  */

        for ((i, entry) in growths.withIndex()) {
            intervals.add(entry.age.toString())
/*
            val equivalent = projectWeight[i].value
            Timber.e("Equivalent Weight $equivalent")*/
            babyWeight.add(Entry(i.toFloat(), entry.data[0].value.toFloat()))
            one.add(Entry(i.toFloat(), entry.data[1].value.toFloat()))
            three.add(Entry(i.toFloat(), entry.data[2].value.toFloat()))
            four.add(Entry(i.toFloat(), entry.data[3].value.toFloat()))
            five.add(Entry(i.toFloat(), entry.data[4].value.toFloat()))
            seven.add(Entry(i.toFloat(), entry.data[6].value.toFloat()))

        }

        val baby = generateSource(babyWeight, "Actual Weight", "#4472c4")
        val dataOne = generateSource(one, "Projected Weight", "#eb975e")
        val dataThree = generateSource(three, "", "#e3a23c")
        val dataFour = generateSource(four, "", "#006600")
        val dataFive = generateSource(five, "", "#ffa20e")
        val dataSeven = generateSource(seven, "", "#0f0f0f")

        val data = LineData(
            baby, dataOne, dataThree, dataFour,
            dataFive, dataSeven
        )
        binding.growthChart.axisLeft.setDrawGridLines(false)

        val xAxis: XAxis = binding.growthChart.xAxis
        xAxis.setDrawGridLines(true)
        xAxis.setDrawAxisLine(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.mAxisMinimum = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(intervals)
        xAxis.setLabelCount(4, true)
        xAxis.setLabelCount(values.data.size, true)

        binding.growthChart.legend.isEnabled = true

        //remove description label
        binding.growthChart.description.isEnabled = true
        binding.growthChart.isDragEnabled = false
        binding.growthChart.setScaleEnabled(false)
        binding.growthChart.description.text = "Age (Days)"
        binding.growthChart.description.setPosition(0f, 10f)

        //add animation
        binding.growthChart.animateX(1000, Easing.EaseInSine)
        binding.growthChart.data = data
        val leftAxis: YAxis = binding.growthChart.axisLeft
        leftAxis.axisMinimum = 0f
        /* leftAxis.mAxisMaximum = 1200f
         leftAxis.setLabelCount(6, true)
      */
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

    private fun generateSource(one: ArrayList<Entry>, label: String, color: String): LineDataSet {
        val actual = LineDataSet(one, label)
        actual.setColors(Color.parseColor(color))
        actual.setDrawCircleHole(false)
        actual.setDrawValues(false)
        actual.setDrawCircles(false)
        actual.circleRadius = 0f
        actual.mode = LineDataSet.Mode.CUBIC_BEZIER
        actual.lineWidth = 3f

        return actual
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