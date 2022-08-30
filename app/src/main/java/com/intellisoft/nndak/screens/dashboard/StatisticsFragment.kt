package com.intellisoft.nndak.screens.dashboard

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.*
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
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.charts.*
import com.intellisoft.nndak.data.RestManager
import com.intellisoft.nndak.databinding.FragmentStatisticsBinding
import com.intellisoft.nndak.helper_class.FormatHelper
import com.intellisoft.nndak.models.PieItem
import com.intellisoft.nndak.utils.*
import com.intellisoft.nndak.viewmodels.PatientListViewModel
import kotlinx.android.synthetic.main.fragment_landing.view.*
import timber.log.Timber
import java.text.DecimalFormat
import java.text.SimpleDateFormat
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
    private val apiService = RestManager()
    private var totalTerm: Int = 0
    private var totalPreTerm: Int = 0
    private var totalBabies: Int = 0
    private lateinit var mCalendar: Calendar
    private lateinit var mSdf: SimpleDateFormat
    private lateinit var dateSetListener: OnDateSetListener
    private lateinit var startDate: String
    private lateinit var endDate: String

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
        syncLocalData()
        checkCurrentDevice()
        updateDateSelection()

        if (isNetworkAvailable(requireContext())) {
            loadLiveData()
        } else {
            syncLocalData()
        }
    }

    private fun updateDateSelection() {
        val current = SimpleDateFormat("dd/MM/yyyy").format(System.currentTimeMillis())
        startDate = current
        endDate = current

        binding.apply {
            btnStart.text = "From"
            btnEnd.text = "To"
            btnStart.setOnClickListener {
                showPopUp(btnStart, "From ")

            }
            btnEnd.setOnClickListener {
                showPopUp(btnEnd, "To ")
            }
        }

    }

    private fun showPopUp(btnEnd: MaterialButton, label: String) {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { view, myear, mmonth, mdayOfMonth ->
                btnEnd.text = label
            },
            year,
            month,
            day
        )
        datePickerDialog.datePicker.maxDate = Date().time
        datePickerDialog.show()
    }


    private fun checkCurrentDevice() {
        if (isTablet(requireContext())) {
            binding.textView.visibility = View.VISIBLE
            binding.textView1.visibility = View.VISIBLE
        }
    }

    private fun syncLocalData() {
        val data = FhirApplication.getStatistics(requireContext())
        if (data != null) {
            val gson = Gson()
            try {
                val it: Statistics = gson.fromJson(data, Statistics::class.java)
                updateUI(it)

            } catch (e: Exception) {
            }
        }
    }

    private fun checkIfFragmentAttached(operation: Context.() -> Unit) {
        if (isAdded && context != null) {
            operation(requireContext())
        }
    }

    override fun onResume() {

        (requireActivity() as MainActivity).showBottomNavigationView(View.VISIBLE)
        super.onResume()
    }

    private fun updateUI(it: Statistics) {
        checkIfFragmentAttached {
            binding.apply {
                incData.tvTotal.text = it.totalBabies
                incData.tvPreterm.text = it.preterm
                incData.tvTerm.text = it.term
                incData.tvAverage.text = it.averageDays
                tvRate.text = getString(R.string.app_mortality).replace("0", it.mortalityRate.rate)
                populateFeedingTime(it.firstFeeding)
                populateFeedsPercentage(it.percentageFeeds)
                populateMortality(it.mortalityRate.data)
                populateExpressingTimes(it.expressingTime)

                val prePercentage =
                    ((it.preterm.toDouble() / it.totalBabies.toDouble()) * 100).roundToInt()
                val termPercentage =
                    ((it.term.toDouble() / it.totalBabies.toDouble()) * 100).roundToInt()
                incData.tvPreAverage.text = prePercentage.toString()
                incData.tvTermAverage.text = termPercentage.toString()

                incData.pbTerm.progress = termPercentage.toInt()
                incData.pbPreTerm.progress = prePercentage.toInt()
            }

        }
    }

    private fun loadLiveData() {
        showLoading(true)
        apiService.loadStatistics(requireContext()) {
            try {
                showLoading(false)
                if (it != null) {
                    val gson = Gson()
                    val json = gson.toJson(it)
                    try {
                        FhirApplication.updateStatistics(requireContext(), json)
                        updateUI(it)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {

                    syncLocalData()
                }
            } catch (e: Exception) {
                Timber.e("Error loading statistics ${e.message}")
            }
        }

    }

    private fun showLoading(b: Boolean) {
        binding.loadingProgress.visibility = if (b) View.VISIBLE else View.GONE
    }

    private fun populateExpressingTimes(expressingTime: List<ExpressingTime>) {
        try {

            val intervals = ArrayList<String>()
            val lessFive: ArrayList<Entry> = ArrayList()
            val lessSeven: ArrayList<Entry> = ArrayList()
            val moreSeven: ArrayList<Entry> = ArrayList()

            for ((i, entry) in expressingTime.withIndex()) {
                intervals.add(entry.month)
                lessFive.add(Entry(i.toFloat(), entry.underFive.toFloat()))
                lessSeven.add(Entry(i.toFloat(), entry.underSeven.toFloat()))
                moreSeven.add(Entry(i.toFloat(), entry.aboveSeven.toFloat()))
            }

            val lessThanFive = LineDataSet(lessFive, "5 or Less")
            lessThanFive.setColors(Color.parseColor("#F65050"))
            lessThanFive.setDrawCircleHole(false)
            lessThanFive.setDrawValues(false)
            lessThanFive.setDrawCircles(false)
            lessThanFive.mode = LineDataSet.Mode.CUBIC_BEZIER

            val lessThanSeven = LineDataSet(lessSeven, "6-7 times")
            lessThanSeven.setColors(Color.parseColor("#1EAF5F"))
            lessThanSeven.setDrawCircleHole(true)
            lessThanSeven.setDrawValues(false)
            lessThanSeven.setDrawCircles(true)
            lessThanSeven.mode = LineDataSet.Mode.CUBIC_BEZIER

            val moreThanSeven = LineDataSet(moreSeven, "7 or More")
            moreThanSeven.setColors(Color.parseColor("#77A9FF"))
            moreThanSeven.setDrawCircleHole(false)
            moreThanSeven.setDrawValues(false)
            moreThanSeven.setDrawCircles(false)
            moreThanSeven.mode = LineDataSet.Mode.CUBIC_BEZIER

            //val data = LineData(lessThanFive)
            val data = LineData(lessThanFive, lessThanSeven, moreThanSeven)
            binding.expressingChart.axisLeft.setDrawGridLines(false)

            val xAxis: XAxis = binding.expressingChart.xAxis
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.mAxisMinimum = 1f
            xAxis.labelRotationAngle = -60f
            xAxis.valueFormatter = IndexAxisValueFormatter(intervals)
            xAxis.setLabelCount(expressingTime.size, true)

            binding.expressingChart.legend.isEnabled = true

            //remove description label
            binding.expressingChart.description.isEnabled = false
            binding.expressingChart.isDragEnabled = true
            binding.expressingChart.setScaleEnabled(false)
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

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadTimes(data: List<ExpressingTime>): ArrayList<String> {
        val days = ArrayList<String>()
        val sortedList = data.sortedWith(compareBy { it.month })
        sortedList.forEach {
            days.add(it.month)
        }
        return days
    }


    private fun populateMortality(rates: List<Data>) {

        val values = getPastDaysOnIntervalOf(rates.size, 1)

        if (values.isNotEmpty()) {

            val intervals = ArrayList<String>()
            val mortality: ArrayList<Entry> = ArrayList()


            for ((i, entry) in rates.withIndex()) {
                intervals.add(entry.month)
                val value = entry.value.replace("[^\\d.]".toRegex(), "")
                mortality.add(Entry(i.toFloat(), value.toFloat()))
            }

            val mRate = LineDataSet(mortality, "Rates")
            mRate.setColors(Color.parseColor("#F65050"))
            mRate.fillColor = Color.parseColor("#F65050")
            //  mRate.fillAlpha = 10
            mRate.setDrawCircleHole(false)
            mRate.setDrawValues(false)
            mRate.setDrawCircles(false)
            mRate.mode = LineDataSet.Mode.CUBIC_BEZIER


            val data = LineData(mRate)
            binding.mortalityChart.axisLeft.setDrawGridLines(false)

            val xAxis: XAxis = binding.mortalityChart.xAxis
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.labelRotationAngle = -60f
            xAxis.valueFormatter = IndexAxisValueFormatter(intervals)
            xAxis.setLabelCount(rates.size, true)

            binding.mortalityChart.legend.isEnabled = false

            //remove description label
            binding.mortalityChart.description.isEnabled = false
            binding.mortalityChart.isDragEnabled = true
            binding.mortalityChart.setScaleEnabled(false)
            binding.mortalityChart.description.text = "Age (Days)"
            //add animation
            binding.mortalityChart.animateX(1000, Easing.EaseInSine)
            binding.mortalityChart.data = data

            val leftAxis: YAxis = binding.mortalityChart.axisLeft
            leftAxis.setDrawGridLines(true)
            leftAxis.isGranularityEnabled = false
            leftAxis.setLabelCount(5, false)
            leftAxis.axisMinimum = 0f
            //leftAxis.axisMaximum = 60f

            val rightAxis: YAxis = binding.mortalityChart.axisRight
            rightAxis.setDrawGridLines(false)
            rightAxis.setDrawZeroLine(false)
            rightAxis.isGranularityEnabled = false
            rightAxis.isEnabled = false
            //refresh
            binding.mortalityChart.invalidate()
        }

    }

    private fun getMaximum(rates: List<Data>): Float {
        var min = Int.MAX_VALUE
        rates.forEach {
            min = min.coerceAtMost(it.value.toInt())

        }
        return min.toFloat()
    }

    private fun populateFeedsPercentage(percentageFeeds: PercentageFeeds) {
        val pie: MutableList<PieItem> = mutableListOf()
        pie.add(
            PieItem(
                percentageFeeds.dhm,
                "Donated Human Milk " + calculatePercentage(percentageFeeds, percentageFeeds.dhm),
                "#1EAF5F"
            )
        )
        pie.add(
            PieItem(
                percentageFeeds.iv,
                "Iv Fluids " + calculatePercentage(percentageFeeds, percentageFeeds.ebm),
                "#F65050"
            )
        )
//        pie.add(PieItem(percentageFeeds.oral, "Oral Feeds", "#FFC600"))
        pie.add(
            PieItem(
                percentageFeeds.ebm,
                "Expressed Breast Milk " + calculatePercentage(
                    percentageFeeds,
                    percentageFeeds.ebm
                ),
                "#6C63FF"
            )
        )
        pie.add(
            PieItem(
                percentageFeeds.formula,
                "Formula " + calculatePercentage(percentageFeeds, percentageFeeds.formula),
                "#BA1B22"
            )
        )

        val pieShades: ArrayList<Int> = ArrayList()
        val entries = ArrayList<PieEntry>()
        for ((i, entry) in pie.withIndex()) {
            entries.add(PieEntry(entry.value.toFloat(), entry.label))
            pieShades.add(Color.parseColor(entry.color))
        }

        val ourSet = PieDataSet(entries, "")
        val data = PieData(ourSet)

        ourSet.sliceSpace = 1f
        ourSet.colors = pieShades
        data.setValueTextColor(Color.WHITE)
        data.setValueTextSize(10f)
        data.setValueFormatter(ChartFormatter())
        binding.apply {

            percentageChart.data = data
            percentageChart.legend.setDrawInside(false)
            percentageChart.legend.isEnabled = true
            if (isTablet(requireContext())) {
                percentageChart.legend.orientation = Legend.LegendOrientation.VERTICAL
                percentageChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
                percentageChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            } else {
                percentageChart.legend.orientation = Legend.LegendOrientation.HORIZONTAL
                percentageChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                percentageChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT

            }
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


    private fun populateFeedingTime(firstFeeding: FirstFeeding) {
        val pie: MutableList<PieItem> = mutableListOf()
        pie.add(
            PieItem(
                firstFeeding.withinOne,
                "Within 1 Hour " + getPercentage(firstFeeding, firstFeeding.withinOne),
                "#428d9b"
            )
        )
        pie.add(
            PieItem(
                firstFeeding.afterOne,
                "After 1 Hour " + getPercentage(firstFeeding, firstFeeding.afterOne),
                "#f8c632"
            )
        )
        pie.add(
            PieItem(
                firstFeeding.afterTwo,
                "After 3 Hours " + getPercentage(firstFeeding, firstFeeding.afterTwo),
                "#f1b8b8"
            )
        )
        pie.add(
            PieItem(
                firstFeeding.afterThree,
                "Within 1 Day " + getPercentage(firstFeeding, firstFeeding.afterThree),
                "#706dfd"
            )
        )
        pie.add(
            PieItem(
                firstFeeding.afterThree,
                "After 2 Days " + getPercentage(firstFeeding, firstFeeding.afterThree),
                "#3c6f26"
            )
        )

        val pieShades: ArrayList<Int> = ArrayList()
        val entries = ArrayList<PieEntry>()
        for ((i, entry) in pie.withIndex()) {
            entries.add(PieEntry(entry.value.toFloat(), entry.label))
            pieShades.add(Color.parseColor(entry.color))
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

            if (isTablet(requireContext())) {
                totalTermChart.legend.orientation = Legend.LegendOrientation.VERTICAL
                totalTermChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
                totalTermChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            } else {
                totalTermChart.legend.orientation = Legend.LegendOrientation.HORIZONTAL
                totalTermChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                totalTermChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT

            }
            totalTermChart.legend.isWordWrapEnabled = false
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

    private fun getPercentage(firstFeeding: FirstFeeding, withinOne: String): String {
        val percentage = try {
            val total =
                firstFeeding.withinOne.toFloat() + firstFeeding.afterOne.toFloat() + firstFeeding.afterTwo.toFloat() + firstFeeding.afterThree.toFloat()
            val average = (withinOne.toFloat() / total) * 100
            // two decimal places
            val dat=DecimalFormat("#.##").format(average)
            dat.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            "0.0"
        }
        return "$percentage %"
    }

    private fun calculatePercentage(percentageFeeds: PercentageFeeds, ebm: String): String {
        val data = try {
            val total = percentageFeeds.dhm.toFloat() + percentageFeeds.iv.toFloat() +
                    percentageFeeds.ebm.toFloat() + percentageFeeds.formula.toFloat()
            val av = (ebm.toFloat() / total) * 100
            av.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            "0.0"
        }
        return "$data %"
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