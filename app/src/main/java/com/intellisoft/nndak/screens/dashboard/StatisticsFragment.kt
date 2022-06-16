package com.intellisoft.nndak.screens.dashboard

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.fhir.FhirEngine
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.charts.ChartFormatter
import com.intellisoft.nndak.databinding.FragmentStatisticsBinding
import com.intellisoft.nndak.helper_class.FormatHelper
import com.intellisoft.nndak.models.PieItem
import com.intellisoft.nndak.utils.getPastMonthsOnIntervalOf
import com.intellisoft.nndak.viewmodels.PatientListViewModel
import timber.log.Timber
import java.time.LocalDate
import java.util.*
import kotlin.math.roundToInt


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StatisticsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StatisticsFragment : Fragment() {
    private var _binding: FragmentStatisticsBinding? = null
    private val binding
        get() = _binding!!
    private var c: Calendar = Calendar.getInstance()
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientListViewModel: PatientListViewModel
    private var totalTerm: Int = 0
    private var totalPreTerm: Int = 0
    private var totalBabies: Int = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = resources.getString(R.string.app_statistics)
            setHomeAsUpIndicator(R.drawable.dash)
            setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)
        (activity as MainActivity).setDrawerEnabled(true)

        fhirEngine = FhirApplication.fhirEngine(requireContext())
        patientListViewModel =
            ViewModelProvider(
                this,
                PatientListViewModel.PatientListViewModelFactory(
                    requireActivity().application,
                    fhirEngine, "0"
                )
            ).get(PatientListViewModel::class.java)

        patientListViewModel.liveMotherBaby.observe(viewLifecycleOwner) {

            if (it != null) {
                totalBabies = it.count()
                for (count in it) {
                    if (count.status == "Preterm") {
                        totalPreTerm++
                    } else {
                        totalTerm++
                    }
                }


                val prePercentage = (totalPreTerm.toDouble() / totalBabies) * 100
                val termPercentage = (totalTerm.toDouble() / totalBabies) * 100

                Timber.d("Babies Total $prePercentage")

                binding.apply {
                    tvTotal.text = totalBabies.toString()
                    tvPreterm.text = totalPreTerm.toString()
                    tvTerm.text = totalTerm.toString()
                    try {
                        tvPreAverage.text = "${prePercentage.roundToInt()} %"
                        tvTermAverage.text = "${termPercentage.roundToInt()} %"

                        pbTerm.progress = termPercentage.roundToInt()
                        pbPreTerm.progress = prePercentage.roundToInt()
                    } catch (e: Exception) {

                    }

                }
            }

        }

        patientListViewModel.loadFeedingTime()
        patientListViewModel.liveFeedsTime.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.apply {
                    firstFeedsChart(it.times)
                    percentageFeedsChart(it.feeds)
                }

            }
        }
        binding.apply {

            mortalityRateChart()
            expressingTimesChart()
        }

    }

    private fun firstFeedsChart(list: List<PieItem>) {
        val pieShades: ArrayList<Int> = ArrayList()
        val entries = ArrayList<PieEntry>()
        for (pie in list) {
            entries.add(PieEntry(pie.value.toFloat(), pie.label))
            pieShades.add(Color.parseColor(pie.color))
        }

        val ourSet = PieDataSet(entries, "")
        val data = PieData(ourSet)
        ourSet.sliceSpace = 1f
        ourSet.colors = pieShades
        data.setValueTextColor(Color.WHITE)
        data.setValueTextSize(10f)
        data.setValueFormatter(ChartFormatter())
        binding.apply {
            totalTermChart.setExtraOffsets(20F, 8F, 75F, 8F)
            totalTermChart.data = data
            totalTermChart.animateY(1400, Easing.EaseInOutQuad)
            totalTermChart.isDrawHoleEnabled = true
            totalTermChart.description.isEnabled = false
            totalTermChart.setUsePercentValues(true)

            totalTermChart.legend.setDrawInside(false)
            totalTermChart.legend.isEnabled = true
            totalTermChart.legend.orientation = Legend.LegendOrientation.VERTICAL
            totalTermChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            totalTermChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
            totalTermChart.legend.isWordWrapEnabled = true
            totalTermChart.legend.xEntrySpace = 10f
            totalTermChart.legend.yEntrySpace = 10f
            totalTermChart.legend.yOffset = 10f
            totalTermChart.legend.xOffset = 10f
            totalTermChart.extraTopOffset = 15f
            totalTermChart.extraBottomOffset = 15f
            totalTermChart.extraLeftOffset = 0f
            totalTermChart.extraRightOffset = 50f

            totalTermChart.centerText = generateCenterText()
            totalTermChart.setCenterTextSize(10f)
            totalTermChart.holeRadius = 45f
            totalTermChart.transparentCircleRadius = 50f
            totalTermChart.setDrawEntryLabels(false)
            //refreshing the chart
            totalTermChart.invalidate()
        }

    }

    private fun percentageFeedsChart(list: List<PieItem>) {
        val pieShades: ArrayList<Int> = ArrayList()
        val entries = ArrayList<PieEntry>()
        for (pie in list) {
            entries.add(PieEntry(pie.value.toFloat(), pie.label))
            pieShades.add(Color.parseColor(pie.color))
        }


        val ourSet = PieDataSet(entries, "")
        val data = PieData(ourSet)

        ourSet.sliceSpace = 1f
        ourSet.colors = pieShades
        data.setValueTextColor(Color.WHITE)
        data.setValueTextSize(10f)

        binding.apply {

            percentageChart.data = data
            percentageChart.legend.setDrawInside(false)
            percentageChart.legend.isEnabled = true
            percentageChart.legend.orientation = Legend.LegendOrientation.VERTICAL
            percentageChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            percentageChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
            percentageChart.legend.isWordWrapEnabled = true
            percentageChart.legend.xEntrySpace = 10f
            percentageChart.legend.yEntrySpace = 10f
            percentageChart.legend.yOffset = 10f
            percentageChart.legend.xOffset = 10f
            percentageChart.extraTopOffset = 15f
            percentageChart.extraBottomOffset = 15f
            percentageChart.extraLeftOffset = 0f
            percentageChart.extraRightOffset = 50f
            percentageChart.animateY(1400, Easing.EaseInOutQuad)
            percentageChart.isDrawHoleEnabled = false
            percentageChart.description.isEnabled = false
            percentageChart.setDrawEntryLabels(false)
            percentageChart.invalidate()
        }

    }


    private fun mortalityRateChart() {
        val month: Int = c.get(Calendar.MONTH) + 1
        Timber.e("Which Month $month")
        val values = getPastMonthsOnIntervalOf(month, 1)

        if (values.isNotEmpty()) {
            val input = arrayListOf<Int>(4, 7, 9, 4, 7, 5)
            val dayNames = formatMonths(values)
            Timber.e("Days $dayNames")
            Timber.e("Values Count ${values.size}")

            val lessFive: ArrayList<Entry> = ArrayList()

            for ((i, entry) in input.withIndex()) {
                val value = input[i].toFloat()
                lessFive.add(Entry(i.toFloat(), value))
            }

            val lessThanFive = LineDataSet(lessFive, "Mortality Rate")
            lessThanFive.setColors(Color.parseColor("#F65050"))
            lessThanFive.setDrawCircleHole(false)
            lessThanFive.setDrawValues(false)
            lessThanFive.setDrawCircles(false)
            lessThanFive.mode = LineDataSet.Mode.CUBIC_BEZIER


            val data = LineData(lessThanFive)
            binding.mortalityChart.axisLeft.setDrawGridLines(false)

            val xAxis: XAxis = binding.mortalityChart.xAxis
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.labelRotationAngle = -60f
            xAxis.valueFormatter = IndexAxisValueFormatter(dayNames)


            binding.mortalityChart.legend.isEnabled = true

            //remove description label
            binding.mortalityChart.description.isEnabled = false
            binding.mortalityChart.isDragEnabled = true
            binding.mortalityChart.setScaleEnabled(true)
            binding.mortalityChart.description.text = "Age (Days)"
            //add animation
            binding.mortalityChart.animateX(1000, Easing.EaseInSine)
            binding.mortalityChart.data = data

            val leftAxis: YAxis = binding.mortalityChart.axisLeft
            leftAxis.axisMinimum = 0f
            leftAxis.setDrawGridLines(true)
            leftAxis.isGranularityEnabled = false


            val rightAxis: YAxis = binding.mortalityChart.axisRight
            rightAxis.setDrawGridLines(false)
            rightAxis.setDrawZeroLine(false)
            rightAxis.isGranularityEnabled = false
            rightAxis.isEnabled = false
            //refresh
            binding.mortalityChart.invalidate()
        }

    }


    private fun expressingTimesChart() {
        val month: Int = c.get(Calendar.MONTH) + 1
        Timber.e("Which Month $month")
        val values = getPastMonthsOnIntervalOf(month, 1)
        if (values.isNotEmpty()) {
            val input = arrayListOf<Int>(4, 7, 9, 3, 4, 7, 5)
            val dayNames = formatMonths(values)
            Timber.e("Days $dayNames")
            Timber.e("Values Count ${values.size}")

            val lessFive: ArrayList<Entry> = ArrayList()
            val lessSeven: ArrayList<Entry> = ArrayList()
            val moreSeven: ArrayList<Entry> = ArrayList()

            for ((i, entry) in input.withIndex()) {
                val value = input[i].toFloat()
                lessFive.add(Entry(i.toFloat(), value))
                lessSeven.add(Entry(i.toFloat(), value + 1))
                moreSeven.add(Entry(i.toFloat(), value - 2))
            }

            val lessThanFive = LineDataSet(lessFive, "5 or Less")
            lessThanFive.setColors(Color.parseColor("#F65050"))
            lessThanFive.setDrawCircleHole(false)
            lessThanFive.setDrawValues(false)
            lessThanFive.setDrawCircles(false)
            lessThanFive.mode = LineDataSet.Mode.CUBIC_BEZIER

            val lessThanSeven = LineDataSet(lessSeven, "6-7 times")
            lessThanSeven.setColors(Color.parseColor("#1EAF5F"))
            lessThanSeven.setDrawCircleHole(false)
            lessThanSeven.setDrawValues(false)
            lessThanSeven.setDrawCircles(false)
            lessThanSeven.mode = LineDataSet.Mode.CUBIC_BEZIER

            val moreThanSeven = LineDataSet(moreSeven, "7 or More")
            moreThanSeven.setColors(Color.parseColor("#77A9FF"))
            moreThanSeven.setDrawCircleHole(false)
            moreThanSeven.setDrawValues(false)
            moreThanSeven.setDrawCircles(false)
            moreThanSeven.mode = LineDataSet.Mode.CUBIC_BEZIER

            val data = LineData(lessThanFive, lessThanSeven, moreThanSeven)
            binding.expressingChart.axisLeft.setDrawGridLines(false)

            val xAxis: XAxis = binding.expressingChart.xAxis
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.labelRotationAngle = -60f
            xAxis.valueFormatter = IndexAxisValueFormatter(dayNames)


            binding.expressingChart.legend.isEnabled = true

            //remove description label
            binding.expressingChart.description.isEnabled = false
            binding.expressingChart.isDragEnabled = true
            binding.expressingChart.setScaleEnabled(true)
            binding.expressingChart.description.text = "Age (Days)"
            //add animation
            binding.expressingChart.animateX(1000, Easing.EaseInSine)
            binding.expressingChart.data = data

            val leftAxis: YAxis = binding.expressingChart.axisLeft
            leftAxis.axisMinimum = 0f
            leftAxis.setDrawGridLines(true)
            leftAxis.isGranularityEnabled = false


            val rightAxis: YAxis = binding.expressingChart.axisRight
            rightAxis.setDrawGridLines(false)
            rightAxis.setDrawZeroLine(false)
            rightAxis.isGranularityEnabled = false
            rightAxis.isEnabled = false
            //refresh
            binding.expressingChart.invalidate()
        }

    }


    private fun formatMonths(values: List<LocalDate>): ArrayList<String> {
        val days = ArrayList<String>()
        values.forEach {
            val format = FormatHelper().getMonthName(it.toString())
            days.add(format)
        }
        return days
    }

    private fun generateCenterText(): CharSequence {
        val s = SpannableString("Feeds")
        s.setSpan(RelativeSizeSpan(2f), 0, 5, 0)
        s.setSpan(ForegroundColorSpan(Color.GRAY), 5, s.length, 0)
        return s
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