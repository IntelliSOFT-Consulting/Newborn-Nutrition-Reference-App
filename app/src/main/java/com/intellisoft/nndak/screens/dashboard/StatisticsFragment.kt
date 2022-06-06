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
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.databinding.FragmentStatisticsBinding


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
            title = resources.getString(R.string.app_dashboard)
            setHomeAsUpIndicator(R.drawable.dash)
            setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)
        (activity as MainActivity).setDrawerEnabled(true)

        binding.apply {
//            firstFeedsChart()
//            percentageFeedsChart()
//            mortalityRateChart()
//            expressingTimesChart()
        }

    }

    private fun firstFeedsChart() {

        val values = arrayListOf<Int>(4, 7, 12, 2)
        val labels = arrayListOf<String>(
            "Fed Within 1 hour ",
            "Fed After 1 hour ",
            "Fed After 2 hours",
            "Fed After 3 hours"
        )

        //an array to store the pie slices entry
        val ourPieEntry = ArrayList<PieEntry>()

        for ((i, entry) in values.withIndex()) {
            //converting to float
            val value = values[i].toFloat()
            val label = labels[i]
            //adding each value to the pieentry array
            ourPieEntry.add(PieEntry(value, label))
        }

        //assigning color to each slices
        val pieShades: ArrayList<Int> = ArrayList()
        pieShades.add(Color.parseColor("#0E2DEC"))
        pieShades.add(Color.parseColor("#B7520E"))
        pieShades.add(Color.parseColor("#5E6D4E"))
        pieShades.add(Color.parseColor("#DA1F12"))

        //add values to the pie dataset and passing them to the constructor
        val ourSet = PieDataSet(ourPieEntry, "")
        val data = PieData(ourSet)

        //setting the slices divider width
        ourSet.sliceSpace = 1f

        //populating the colors and data
        ourSet.colors = pieShades
        binding.totalTermChart.data = data
        //setting color and size of text
        data.setValueTextColor(Color.WHITE)
        data.setValueTextSize(10f)

        //add an animation when rendering the pie chart
        binding.totalTermChart.animateY(1400, Easing.EaseInOutQuad)
        //disabling center hole
        binding.totalTermChart.isDrawHoleEnabled = true
        //do not show description text
        binding.totalTermChart.description.isEnabled = false
        //legend enabled and its various appearance settings
        binding.totalTermChart.legend.isEnabled = true
        binding.totalTermChart.legend.orientation = Legend.LegendOrientation.VERTICAL
        binding.totalTermChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        binding.totalTermChart.legend.isWordWrapEnabled = true


        binding.totalTermChart.centerText = generateCenterText()
        binding.totalTermChart.setCenterTextSize(10f)

        // radius of the center hole in percent of maximum radius

        // radius of the center hole in percent of maximum radius
        binding.totalTermChart.holeRadius = 45f
        binding.totalTermChart.transparentCircleRadius = 50f

        //dont show the text values on slices e.g Antelope, impala etc
        binding.totalTermChart.setDrawEntryLabels(false)
        //refreshing the chart
        binding.totalTermChart.invalidate()
    }

    private fun percentageFeedsChart() {

        val values = arrayListOf<Int>(4, 7, 12, 2, 9)
        val labels =
            arrayListOf<String>(
                "Donated Human Milk",
                "Breastfeeding",
                "Oral Feeds",
                "Expressed Breast Milk",
                "Formula"
            )

        //an array to store the pie slices entry
        val ourPieEntry = ArrayList<PieEntry>()

        for ((i, entry) in values.withIndex()) {
            //converting to float
            val value = values[i].toFloat()
            val label = labels[i]
            //adding each value to the pieentry array
            ourPieEntry.add(PieEntry(value, label))
        }

        //assigning color to each slices
        val pieShades: ArrayList<Int> = ArrayList()
        pieShades.add(Color.parseColor("#0E2DEC"))
        pieShades.add(Color.parseColor("#B7520E"))
        pieShades.add(Color.parseColor("#5E6D4E"))
        pieShades.add(Color.parseColor("#DA1F12"))
        pieShades.add(Color.parseColor("#B7520E"))

        //add values to the pie dataset and passing them to the constructor
        val ourSet = PieDataSet(ourPieEntry, "")
        val data = PieData(ourSet)

        //setting the slices divider width
        ourSet.sliceSpace = 1f

        //populating the colors and data
        ourSet.colors = pieShades
        binding.percentageChart.data = data
        //setting color and size of text
        data.setValueTextColor(Color.WHITE)
        data.setValueTextSize(10f)

        //add an animation when rendering the pie chart
        binding.percentageChart.animateY(1400, Easing.EaseInOutQuad)
        //disabling center hole
        binding.percentageChart.isDrawHoleEnabled = false
        //do not show description text
        binding.percentageChart.description.isEnabled = false
        //legend enabled and its various appearance settings
        binding.percentageChart.legend.isEnabled = true
        binding.percentageChart.legend.orientation = Legend.LegendOrientation.VERTICAL
        binding.percentageChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        binding.percentageChart.legend.isWordWrapEnabled = true

        //dont show the text values on slices e.g Antelope, impala etc
        binding.percentageChart.setDrawEntryLabels(false)
        //refreshing the chart
        binding.percentageChart.invalidate()
    }


    private fun mortalityRateChart() {

        val values = arrayListOf<Int>(4, 7, 12, 2, 4, 2, 5, 3, 6, 4, 3)
        val ourLineChartEntries: ArrayList<Entry> = ArrayList()

        for ((i, entry) in values.withIndex()) {
            val value = values[i].toFloat()
            ourLineChartEntries.add(Entry(i.toFloat(), value))
        }
        val lineDataSet = LineDataSet(ourLineChartEntries, "")
        lineDataSet.setColors(*ColorTemplate.PASTEL_COLORS)
        val data = LineData(lineDataSet)
        binding.mortalityChart.axisLeft.setDrawGridLines(false)
        val xAxis: XAxis = binding.mortalityChart.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        binding.mortalityChart.legend.isEnabled = false

        //remove description label
        binding.mortalityChart.description.isEnabled = false
        binding.mortalityChart.isDragEnabled = true
        binding.mortalityChart.setScaleEnabled(true)
        //add animation
        binding.mortalityChart.animateX(1000, Easing.EaseInSine)
        binding.mortalityChart.data = data
        //refresh
        binding.mortalityChart.invalidate()
    }

    private fun expressingTimesChart() {

        val values = arrayListOf<Int>(4, 7, 12, 2, 3, 2, 1, 8, 6, 4, 2, 7)
        val ourLineChartEntries: ArrayList<Entry> = ArrayList()

        for ((i, entry) in values.withIndex()) {
            val value = values[i].toFloat()
            ourLineChartEntries.add(Entry(i.toFloat(), value))
        }
        val lineDataSet = LineDataSet(ourLineChartEntries, "")
        lineDataSet.setColors(*ColorTemplate.PASTEL_COLORS)
        val data = LineData(lineDataSet)
        binding.expressingChart.axisLeft.setDrawGridLines(false)
        val xAxis: XAxis = binding.expressingChart.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        binding.expressingChart.legend.isEnabled = false

        //remove description label
        binding.expressingChart.description.isEnabled = false

        //add animation
        binding.expressingChart.animateX(1000, Easing.EaseInSine)
        binding.expressingChart.data = data
        //refresh
        binding.expressingChart.invalidate()
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