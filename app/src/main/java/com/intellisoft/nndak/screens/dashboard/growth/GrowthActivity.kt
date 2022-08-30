package com.intellisoft.nndak.screens.dashboard.growth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.navArgs
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.fhir.FhirEngine
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.R
import com.intellisoft.nndak.charts.GrowthData
import com.intellisoft.nndak.charts.WeightsData
import com.intellisoft.nndak.databinding.ActivityGrowthBinding
import com.intellisoft.nndak.logic.DataSort
import com.intellisoft.nndak.logic.DataSort.Companion.extractValueIndex
import com.intellisoft.nndak.screens.dashboard.child.BabyDashboardFragmentDirections
import com.intellisoft.nndak.utils.generateSource
import com.intellisoft.nndak.utils.getJsonDataFromAsset
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import timber.log.Timber

class GrowthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGrowthBinding
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val args: GrowthActivityArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrowthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Baby's Growth"
            setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
            setDisplayHomeAsUpEnabled(true)
        }

        fhirEngine = FhirApplication.fhirEngine(this)
        patientDetailsViewModel =
            ViewModelProvider(
                this,
                PatientDetailsViewModelFactory(
                    this.application,
                    fhirEngine,
                    args.patientId
                )
            ).get(PatientDetailsViewModel::class.java)
        loadWeights()
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadWeights()
        }
    }

    private fun loadWeights() {
        patientDetailsViewModel.activeWeeklyBabyWeights()
        binding.swipeRefreshLayout.isRefreshing = true
        patientDetailsViewModel.liveWeights.observe(this) {
            if (it != null) {
                binding.swipeRefreshLayout.isRefreshing = false
                binding.cardView.visibility = View.VISIBLE
                standardWeeklyCharts(it)
            }
        }
    }

    private fun standardWeeklyCharts(weightsData: WeightsData) {
        try {
            var standard = "boy-z-score.json"
            if (weightsData.babyGender == "female") {
                standard = "girl-z-score.json"
            }
            val jsonFileString = getJsonDataFromAsset(this, standard)
            val gson = Gson()
            val listGrowthType = object : TypeToken<List<GrowthData>>() {}.type
            val growths: List<GrowthData> = gson.fromJson(jsonFileString, listGrowthType)
            growths.forEachIndexed { idx, growth -> }
            populateZScoreLineChart(weightsData, growths)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun populateZScoreLineChart(values: WeightsData, growths: List<GrowthData>) {

        val intervals = ArrayList<String>()
        val babyWeight: ArrayList<Entry> = ArrayList()
        val one: ArrayList<Entry> = ArrayList()
        val two: ArrayList<Entry> = ArrayList()
        val three: ArrayList<Entry> = ArrayList()
        val four: ArrayList<Entry> = ArrayList()
        val five: ArrayList<Entry> = ArrayList()
        val six: ArrayList<Entry> = ArrayList()
        val seven: ArrayList<Entry> = ArrayList()

        for ((i, entry) in growths.subList(0,25).withIndex()) {

            intervals.add(entry.age.toString())
            val start = entry.age
            val ges = values.gestationAge.toInt()

            if (start == ges || start > ges) {
                val equivalent = extractValueIndex(start, values)
                if (equivalent == "0") {
                    babyWeight.add(Entry(i.toFloat(), entry.data[3].value.toFloat()))
                } else {
                    babyWeight.add(Entry(i.toFloat(), equivalent.toFloat()))
                }
            } else {
                babyWeight.add(Entry(i.toFloat(), entry.data[3].value.toFloat()))
            }
            one.add(Entry(i.toFloat(), entry.data[0].value.toFloat()))
            two.add(Entry(i.toFloat(), entry.data[1].value.toFloat()))
            three.add(Entry(i.toFloat(), entry.data[2].value.toFloat()))
            four.add(Entry(i.toFloat(), entry.data[3].value.toFloat()))
            five.add(Entry(i.toFloat(), entry.data[4].value.toFloat()))
            six.add(Entry(i.toFloat(), entry.data[5].value.toFloat()))
            seven.add(Entry(i.toFloat(), entry.data[6].value.toFloat()))
        }

        val baby = generateSource(babyWeight, "Actual Weight", "#4472c4")
        val dataOne = generateSource(one, "-3", "#000000")
        val dataTwo = generateSource(two, "-2", "#880d0d")
        val dataThree = generateSource(three, "-1", "#ffa20e")
        val dataFour = generateSource(four, "0", "#006600")
        val dataFive = generateSource(five, "+1", "#ffa20e")
        val dataSix = generateSource(six, "+2", "#880d0d")
        val dataSeven = generateSource(seven, "+3", "#000000")

        val data = LineData(
            baby, dataOne, dataTwo, dataThree, dataFour,
            dataFive, dataSix, dataSeven
        )
        binding.growthChart.axisLeft.setDrawGridLines(false)

        val xAxis: XAxis = binding.growthChart.xAxis
        xAxis.setDrawGridLines(true)
        xAxis.setDrawAxisLine(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.mAxisMinimum = 0f
        xAxis.setLabelCount(24, false)
        xAxis.valueFormatter = IndexAxisValueFormatter(intervals)
        xAxis.labelRotationAngle = -45f

        binding.growthChart.legend.isEnabled = true

        //remove description label
        binding.growthChart.description.isEnabled = true
        binding.growthChart.isDragEnabled = false
        binding.growthChart.setScaleEnabled(false)
        binding.growthChart.description.setPosition(0f, 10f)

        //add animation
        binding.growthChart.animateX(1000, Easing.EaseInSine)
        binding.growthChart.data = data
        val leftAxis: YAxis = binding.growthChart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.mAxisMaximum = 12f
        leftAxis.setLabelCount(24, true)

        leftAxis.setDrawGridLines(true)
        leftAxis.isGranularityEnabled = true


        val rightAxis: YAxis = binding.growthChart.axisRight
        rightAxis.setDrawGridLines(true)
        rightAxis.setDrawZeroLine(false)
        rightAxis.isGranularityEnabled = false
        rightAxis.isEnabled = false
        //refresh
        binding.growthChart.invalidate()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}