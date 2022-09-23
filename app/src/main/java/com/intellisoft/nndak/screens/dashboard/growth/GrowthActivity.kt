package com.intellisoft.nndak.screens.dashboard.growth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
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
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.MainActivity.Companion.updateBabyMum
import com.intellisoft.nndak.R
import com.intellisoft.nndak.charts.GrowthData
import com.intellisoft.nndak.charts.WHOData
import com.intellisoft.nndak.charts.WeightsData
import com.intellisoft.nndak.databinding.ActivityGrowthBinding
import com.intellisoft.nndak.logic.DataSort
import com.intellisoft.nndak.logic.DataSort.Companion.extractValueIndex
import com.intellisoft.nndak.models.DataDisplay
import com.intellisoft.nndak.utils.establishCommonIndex
import com.intellisoft.nndak.utils.generateDoubleSource
import com.intellisoft.nndak.utils.generateSource
import com.intellisoft.nndak.utils.getJsonDataFromAsset
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import timber.log.Timber
import java.math.RoundingMode
import java.text.DecimalFormat

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
        binding.apply {
            breadcrumb.page.text =
                Html.fromHtml("Babies > Baby Profile > <font color=\"#37379B\">Baby Dashboard</font>")
            breadcrumb.page.setOnClickListener {
                finish()
            }
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
        displayPosted(args.data)
        val milk = FhirApplication.getMilk(this)
        binding.apply {
            tvMotherMilk.text = milk
        }
        loadWeights()
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadWeights()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.menu_profile -> {
                finish()
//                (this@GrowthActivity as MainActivity).navigate(R.id.profileFragment)
                return true
            }
            R.id.menu_notification -> {
                finish()
//                (this@GrowthActivity as MainActivity).navigate(R.id.notificationFragment)
                return true
            }
            else -> false
        }
    }

    private fun displayPosted(data: DataDisplay) {
        try {
            binding.apply {
                incDetails.pbLoading.visibility = View.GONE
                incDetails.lnBody.visibility = View.VISIBLE
                incDetails.tvBabyName.text = data.babyName
                incDetails.tvMumName.text = data.motherName
                incDetails.appBirthWeight.text = data.birthWeight
                incDetails.appGestation.text = data.status
                incDetails.appApgarScore.text = data.apgarScore
                incDetails.appMumIp.text = data.motherIp
                incDetails.appBabyWell.text = data.babyWell

                incDetails.appBirthDate.text = data.dateOfBirth
                incDetails.appLifeDay.text = data.dayOfLife
                incDetails.appAdmDate.text = data.dateOfAdm

                incDetails.appNeonatalSepsis.text = data.neonatalSepsis
                incDetails.appJaundice.text = data.jaundice
                incDetails.appAsphyxia.text = data.asphyxia


                val isSepsis = data.neonatalSepsis
                val isAsphyxia = data.asphyxia
                val isJaundice = data.jaundice
                if (isSepsis == "Yes" || isAsphyxia == "Yes" || isJaundice == "Yes") {
                    incDetails.lnConditions.visibility = View.VISIBLE
                }
                if (isSepsis != "Yes") {
                    incDetails.appNeonatalSepsis.visibility = View.INVISIBLE
                    incDetails.tvNeonatalSepsis.visibility = View.INVISIBLE
                }

                if (isAsphyxia != "Yes") {
                    incDetails.appAsphyxia.visibility = View.INVISIBLE
                    incDetails.tvAsphyxia.visibility = View.INVISIBLE
                }

                if (isJaundice != "Yes") {
                    incDetails.tvJaundice.visibility = View.INVISIBLE
                    incDetails.appJaundice.visibility = View.INVISIBLE
                }

            }
        } catch (e: Exception) {
            Timber.e(e)
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

            var isBoy = true
            var isPreterm = true
            val status = try {
                if (weightsData.status.toDouble() < 37) {
                    isPreterm = true
                    "Preterm"
                } else {
                    isPreterm = false
                    "Term"
                }
            } catch (e: Exception) {
                isPreterm = true
                "Preterm"
            }
            if (isPreterm) {


                var standard = "boy-z-score.json"
                if (weightsData.babyGender == "female") {
                    standard = "girl-z-score.json"
                    isBoy = false
                }

                val jsonFileString = getJsonDataFromAsset(this@GrowthActivity, standard)
                val gson = Gson()
                val listGrowthType = object : TypeToken<List<GrowthData>>() {}.type
                val growths: List<GrowthData> = gson.fromJson(jsonFileString, listGrowthType)
                growths.forEachIndexed { idx, growth -> }
                populateZScoreLineChart(weightsData, growths, weightsData.gestationAge)

                binding.apply {
                    growthLabel.text = "Postmenstrual Age (Weeks)"
                }

            } else {

                var who = "who-boy-weekly.json"
                if (weightsData.babyGender == "female") {
                    who = "who-girl-weekly.json"
                    isBoy = false
                }
                calculateBabyWeight(weightsData, who, true)


                binding.apply {
                    growthLabel.text = "Postnatal Age (Weeks)"
                }
            }

            binding.apply {
                val label = if (isPreterm) "INTERGROWTH-21 Chart Preterm" else "WHO Chart "
                if (isBoy) {
                    tvBabyGender.text =
                        "${resources.getString(R.string.app_baby_curve)} $label (Boy)"
                } else {
                    tvBabyGender.text =
                        "${resources.getString(R.string.app_baby_curve)} $label (Girl)"
                }
                val data =
                    if (isPreterm) {
                        weightsData.currentWeight.toDouble()
                    } else {
                        weightsData.currentDaily.toDouble()
                    }
                val df = DecimalFormat("#.##")
                df.roundingMode = RoundingMode.CEILING
                val weight = df.format(data)

                tvCurrentWeight.text = "$weight gm"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun populateZScoreLineChart(
        values: WeightsData,
        growths: List<GrowthData>,
        gestationAge: String
    ) {

        val intervals = ArrayList<String>()
        val babyWeight: ArrayList<Entry> = ArrayList()
        val one: ArrayList<Entry> = ArrayList()
        val two: ArrayList<Entry> = ArrayList()
        val three: ArrayList<Entry> = ArrayList()
        val four: ArrayList<Entry> = ArrayList()
        val five: ArrayList<Entry> = ArrayList()
        val six: ArrayList<Entry> = ArrayList()
        val seven: ArrayList<Entry> = ArrayList()
        val max = 37

        /**
         * Get baby's birth weight to determine the starting point
         */
        val birthWeight = values.birthWeight
        //convert to kilograms
        val birthWeightKg = DataSort.convertToKg(birthWeight)
        val commonIndex = establishCommonIndex(gestationAge, growths, birthWeightKg)

        for ((i, entry) in growths.subList(0, max + 1)
            .withIndex()) {

            intervals.add(entry.age.toString())
            val start = entry.age
            val ges = values.gestationAge.toInt() - 1

            if (start == ges || start > ges) {
                val equivalent = extractValueIndex(start, values)
                Timber.e("Test Account $start $ges $equivalent ${values.data}")
                if (equivalent == "0") {
                    // babyWeight.add(Entry(i.toFloat(), entry.data[3].value.toFloat()))
                } else {
                    babyWeight.add(Entry(i.toFloat(), equivalent.toFloat()))
                }
            }

            one.add(Entry(i.toFloat(), entry.data[0].value.toFloat()))
            two.add(Entry(i.toFloat(), entry.data[1].value.toFloat()))
            three.add(Entry(i.toFloat(), entry.data[2].value.toFloat()))
            four.add(Entry(i.toFloat(), entry.data[3].value.toFloat()))
            five.add(Entry(i.toFloat(), entry.data[4].value.toFloat()))
            six.add(Entry(i.toFloat(), entry.data[5].value.toFloat()))
            seven.add(Entry(i.toFloat(), entry.data[6].value.toFloat()))

        }

        val baby = generateDoubleSource(babyWeight, "Actual Weight", "#4472c4")
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
        populateDisplayChart(data, max, intervals, 12)

    }

    private fun calculateBabyWeight(weightsData: WeightsData, who: String, weekly: Boolean) {
        val jsonFileString = getJsonDataFromAsset(this@GrowthActivity, who)
        val gson = Gson()
        val listWhoType = object : TypeToken<List<WHOData>>() {}.type
        val growths: List<WHOData> = gson.fromJson(jsonFileString, listWhoType)
        growths.forEachIndexed { idx, growth -> }
        if (weekly) {
            populateWHOZScoreLineChart(weightsData, growths, weightsData.gestationAge)
        } else {

            populateWHOMonthlyLineChart(weightsData, growths, weightsData.gestationAge)
        }
    }

    private fun populateWHOMonthlyLineChart(
        values: WeightsData,
        growths: List<WHOData>,
        gestationAge: String
    ) {
        val intervals = ArrayList<String>()
        val babyWeight: ArrayList<Entry> = ArrayList()
        val one: ArrayList<Entry> = ArrayList()
        val two: ArrayList<Entry> = ArrayList()
        val three: ArrayList<Entry> = ArrayList()
        val four: ArrayList<Entry> = ArrayList()
        val five: ArrayList<Entry> = ArrayList()
        val six: ArrayList<Entry> = ArrayList()
        val seven: ArrayList<Entry> = ArrayList()
        val max = 25
        for ((i, entry) in growths.withIndex()) {
            intervals.add(entry.day.toString())
            val start = entry.day
            val ges = values.weeksLife
            if (start != 0) {
                val equivalent = DataSort.extractValueIndexMonthly(start, values)
                if (start == ges || start < ges) {
                    if (equivalent != "0") {
                        babyWeight.add(Entry(i.toFloat(), equivalent.toFloat()))
                    }
                }
            }
            one.add(Entry(i.toFloat(), entry.neg3.toFloat()))
            two.add(Entry(i.toFloat(), entry.neg2.toFloat()))
            three.add(Entry(i.toFloat(), entry.neg1.toFloat()))
            four.add(Entry(i.toFloat(), entry.neutral.toFloat()))
            five.add(Entry(i.toFloat(), entry.pos1.toFloat()))
            six.add(Entry(i.toFloat(), entry.pos2.toFloat()))
            seven.add(Entry(i.toFloat(), entry.pos3.toFloat()))

        }

        val baby = generateDoubleSource(babyWeight, "Actual Weight", "#4472c4")
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
        populateDisplayChart(data, max, intervals, 18)

    }


    private fun populateWHOZScoreLineChart(
        values: WeightsData,
        growths: List<WHOData>,
        gestationAge: String
    ) {
        val intervals = ArrayList<String>()
        val babyWeight: ArrayList<Entry> = ArrayList()
        val one: ArrayList<Entry> = ArrayList()
        val two: ArrayList<Entry> = ArrayList()
        val three: ArrayList<Entry> = ArrayList()
        val four: ArrayList<Entry> = ArrayList()
        val five: ArrayList<Entry> = ArrayList()
        val six: ArrayList<Entry> = ArrayList()
        val seven: ArrayList<Entry> = ArrayList()
        val max = 13


        /**
         * Get baby's birth weight to determine the starting point
         */

        for ((i, entry) in growths.subList(0, max + 1)
            .withIndex()) {
            intervals.add(entry.day.toString())
            val start = entry.day
            val birthWeight = values.birthWeight
            val birthWeightKg = DataSort.convertToKg(birthWeight)
            val ges = values.weeksLife
            if (start == 0) {
                babyWeight.add(Entry(i.toFloat(), birthWeightKg.toFloat()))
            } else {
                val equivalent = DataSort.extractValueIndexBirth(start, values)
                if (start == ges || start < ges) {
                    if (equivalent != "0") {
                        babyWeight.add(Entry(i.toFloat(), equivalent.toFloat()))
                    }
                }
            }

            one.add(Entry(i.toFloat(), entry.neg3.toFloat()))
            two.add(Entry(i.toFloat(), entry.neg2.toFloat()))
            three.add(Entry(i.toFloat(), entry.neg1.toFloat()))
            four.add(Entry(i.toFloat(), entry.neutral.toFloat()))
            five.add(Entry(i.toFloat(), entry.pos1.toFloat()))
            six.add(Entry(i.toFloat(), entry.pos2.toFloat()))
            seven.add(Entry(i.toFloat(), entry.pos3.toFloat()))

        }

        val baby = generateDoubleSource(babyWeight, "Actual Weight", "#4472c4")
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
        populateDisplayChart(data, max, intervals, 12)

    }

    private fun populateDisplayChart(
        data: LineData, max: Int,
        intervals: ArrayList<String>, yMax: Int,
    ) {

        binding.apply {

            growthChart.axisLeft.setDrawGridLines(false)

            val xAxis: XAxis = growthChart.xAxis
            xAxis.setDrawGridLines(true)
            xAxis.setDrawAxisLine(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.mAxisMinimum = 0f
            xAxis.setLabelCount(max, false)
            xAxis.valueFormatter = IndexAxisValueFormatter(intervals)
            xAxis.labelRotationAngle = -45f
            growthChart.legend.isEnabled = true

            //remove description label
            growthChart.description.isEnabled = true
            growthChart.isDragEnabled = false
            growthChart.setScaleEnabled(false)
            growthChart.description.text = "Age (weeks)"
            growthChart.description.setPosition(0f, 10f)

            //add animation
            growthChart.animateX(1000, Easing.EaseInSine)
            growthChart.data = data


            val leftAxis: YAxis = growthChart.axisLeft
            leftAxis.axisMinimum = 0f
            leftAxis.mAxisMaximum = yMax.toFloat()
            leftAxis.labelCount = (yMax.toFloat() * 2f).toInt()
            leftAxis.setDrawGridLines(true)
            leftAxis.isGranularityEnabled = true
            leftAxis.isEnabled = true

            val rightAxis: YAxis = growthChart.axisRight
            rightAxis.setDrawGridLines(true)
            rightAxis.setDrawZeroLine(true)
            rightAxis.isGranularityEnabled = true
            rightAxis.axisMinimum = 0f
            rightAxis.mAxisMaximum = yMax.toFloat()
            rightAxis.labelCount = (yMax.toFloat() * 2f).toInt()
            rightAxis.isEnabled = true
            //refresh
            growthChart.invalidate()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}