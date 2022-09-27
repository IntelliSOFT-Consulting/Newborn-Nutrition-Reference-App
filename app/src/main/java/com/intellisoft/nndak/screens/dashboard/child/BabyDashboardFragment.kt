package com.intellisoft.nndak.screens.dashboard.child

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.intellisoft.nndak.MainActivity.Companion.updateBabyMum
import com.intellisoft.nndak.R
import com.intellisoft.nndak.charts.*
import com.intellisoft.nndak.data.RestManager
import com.intellisoft.nndak.databinding.FragmentBabyDashboardBinding
import com.intellisoft.nndak.logic.DataSort.Companion.convertToKg
import com.intellisoft.nndak.logic.DataSort.Companion.extractValueIndex
import com.intellisoft.nndak.logic.DataSort.Companion.extractValueIndexBirth
import com.intellisoft.nndak.logic.DataSort.Companion.extractValueIndexMonthly
import com.intellisoft.nndak.models.DataDisplay
import com.intellisoft.nndak.utils.establishCommonIndex
import com.intellisoft.nndak.utils.generateDoubleSource
import com.intellisoft.nndak.utils.generateSource
import com.intellisoft.nndak.utils.getJsonDataFromAsset
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import com.intellisoft.nndak.viewmodels.ScreenerViewModel
import timber.log.Timber
import java.math.RoundingMode
import java.text.DecimalFormat
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
    private lateinit var data: DataDisplay
    private var isHistory = false
    private var isAction = false
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

            babyGrowthCard.setOnClickListener {
                findNavController().navigate(
                    BabyDashboardFragmentDirections.navigateToTable(
                        args.patientId
                    )
                )
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

        }
        patientDetailsViewModel.getMumChild()
        patientDetailsViewModel.getCurrentPrescriptions()
        patientDetailsViewModel.liveMumChild.observe(viewLifecycleOwner) { mum ->
            if (mum != null) {
                binding.apply {
                    response.appIpNumber.text = mum.assessment.breastfeedingBaby
                    response.appMotherName.text = mum.assessment.breastProblems
                    response.appBabyName.text = mum.assessment.contraindicated
                }
                val gest = mum.dashboard.gestation ?: ""
                val status = mum.status

                data = DataDisplay(
                    babyName = mum.babyName,
                    status = "$gest-$status",
                    motherName = mum.motherName,
                    birthWeight = mum.birthWeight ?: "",
                    apgarScore = mum.dashboard.apgarScore ?: "",
                    motherIp = mum.motherIp,
                    babyWell = mum.dashboard.babyWell ?: "",
                    dateOfBirth = mum.dashboard.dateOfBirth ?: "",
                    dayOfLife = mum.dashboard.dayOfLife ?: "",
                    dateOfAdm = mum.dashboard.dateOfAdm ?: "",
                    neonatalSepsis = mum.dashboard.neonatalSepsis ?: "",
                    asphyxia = mum.dashboard.asphyxia ?: "",
                    jaundice = mum.dashboard.jaundice ?: ""
                )
                updateBabyMum(binding.incDetails, mum)
            }
        }

        patientDetailsViewModel.feedsDistribution()
        patientDetailsViewModel.liveFeeds.observe(viewLifecycleOwner) {
            if (it != null) {
                barGraph(it)
                binding.apply {

                    tvTotalVolume.text = it.totalFeed
                    tvMotherMilk.text = it.varianceAmount
                }
            }
        }
        loadActiveWeights()
        patientDetailsViewModel.getExpressions()
        patientDetailsViewModel.liveExpressions.observe(viewLifecycleOwner) { expression ->
            if (expression != null) {
                populateBarChart(expression)
                binding.apply {
                    tvTotalExpressed.text = expression.totalFeed
                }
            }
        }
    }

    private fun loadActiveWeights() {
        patientDetailsViewModel.activeWeeklyBabyWeights()
        patientDetailsViewModel.liveWeights.observe(viewLifecycleOwner) {
            if (it != null) {
                standardWeeklyCharts(it)
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
            statusChart.setScaleEnabled(false)
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


    private fun standardWeeklyCharts(weightsData: WeightsDetailedData) {
        try {

            val isBoy = weightsData.babyGender == "female"

            var graduateBaby = false
            val isPreterm: Boolean = try {
                weightsData.status.toDouble() < 37
            } catch (e: Exception) {
                true
            }
            if (isPreterm) {
                val weeksInLife = weightsData.weeksLife
                val gestationAge = weightsData.gestationAge
                val totalWeeks = weeksInLife + gestationAge.toInt()
                if (totalWeeks > 64) {
                    graduateBaby = true
                    if (isHistory) {
                        activeHistoricalData(isBoy, weightsData)

                    } else {
                        var who = "who-boy-monthly.json"
                        if (!isBoy) {
                            who = "who-girl-monthly.json"

                        }
                        activateHistoryButton(true)

                        calculateBabyWeight(weightsData, who, false)
                    }

                } else {
                    activeHistoricalData(isBoy, weightsData)
                }
                binding.apply {
                    val unit = if (graduateBaby) "Months" else "Weeks"
                    val name = if (graduateBaby) "Postnatal Age" else "Postmenstrual Age"
                    if (isHistory) {
                        growthLabel.text = "Postmenstrual Age (Weeks)"
                    } else {
                        growthLabel.text = "$name ($unit)"
                    }
                }

            } else {

                //check if the baby has exceeded week 13
                if (weightsData.weeksLife > 13) {
                    graduateBaby = true

                    if (isHistory) {
                        var who = "who-boy-weekly.json"
                        if (!isBoy) {
                            who = "who-girl-weekly.json"

                        }
                        calculateBabyWeight(weightsData, who, true)
                    } else {
                        var who = "who-boy-monthly.json"
                        if (!isBoy) {
                            who = "who-girl-monthly.json"

                        }
                        activateHistoryButton(false)
                        calculateBabyWeight(weightsData, who, false)
                    }


                } else {
                    var who = "who-boy-weekly.json"
                    if (!isBoy) {
                        who = "who-girl-weekly.json"

                    }
                    calculateBabyWeight(weightsData, who, true)

                }
                binding.apply {
                    val unit = if (graduateBaby) "Months" else "Weeks"
                    growthLabel.text = "Postnatal Age ($unit)"
                }
            }

            binding.apply {
                val label = if (graduateBaby) {
                    if (isPreterm) {
                        if (isHistory) "INTERGROWTH-21 Chart Preterm" else "WHO Term Chart "
                    } else {
                        "WHO Term Chart "
                    }
                } else {
                    if (isPreterm) "INTERGROWTH-21 Chart Preterm" else "WHO Term Chart "
                }
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
                tvDeviation.text = weightsData.deviation.value
                if (weightsData.deviation.positive) {
                    // get color from resources
                    val color = ContextCompat.getColor(requireContext(), R.color.dim_green)
                    tvDeviation.setTextColor(color)
                    val icon = ContextCompat.getDrawable(requireContext(), R.drawable.green)
                    imgDeviation.setImageDrawable(icon)

                    // rotate the image to negative 135 degrees
                    imgDeviation.rotation = -135f

//
                } else {
                    val color = ContextCompat.getColor(requireContext(), R.color.dim_red)
                    tvDeviation.setTextColor(color)
                    //set deviation image to red
                    val icon = ContextCompat.getDrawable(requireContext(), R.drawable.red)
                    imgDeviation.setImageDrawable(icon)


                }
                if (isAction) {
                    activateTabs(!isHistory)
                } else {
                    activateTabs(graduateBaby)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun activeHistoricalData(isBoy: Boolean, weightsData: WeightsDetailedData) {

        var standard = "boy-z-score.json"
        if (!isBoy) {
            standard = "girl-z-score.json"

        }

        val jsonFileString = getJsonDataFromAsset(requireContext(), standard)
        val gson = Gson()
        val listGrowthType = object : TypeToken<List<GrowthData>>() {}.type
        val growths: List<GrowthData> = gson.fromJson(jsonFileString, listGrowthType)
        growths.forEachIndexed { idx, growth -> }
        populateZScoreLineChart(weightsData, growths, weightsData.gestationAge)
    }

    private fun activateTabs(graduateBaby: Boolean) {
        // set background to a drawable in textview
        binding.apply {
            if (graduateBaby) {
                btnCurrent.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.active)
                btnPrevious.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.underline)
            } else {
                btnPrevious.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.active)
                btnCurrent.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.underline)
            }
        }
    }

    private fun activateHistoryButton(isPreterm: Boolean) {
        binding.apply {
            val text = if (isPreterm) "Intergrowth Chart" else " WHO Chart (0-13 weeks)"
            val textAlt = if (isPreterm) "WHO Growth Chart" else " WHO Chart (4-24 months)"
            btnPrevious.text = text
            btnCurrent.text = textAlt

            btnPrevious.visibility = View.VISIBLE
            btnCurrent.visibility = View.VISIBLE
            btnPrevious.setOnClickListener {
                isHistory = true
                isAction = true
                reloadPrevious(isHistory)
            }
            btnCurrent.setOnClickListener {
                isAction = true
                isHistory = false
                reloadPrevious(isHistory)
            }

        }
    }

    private fun reloadPrevious(history: Boolean) {
        activateTabs(!history)
        loadActiveWeights()

    }

    private fun calculateBabyWeight(
        weightsData: WeightsDetailedData,
        who: String,
        weekly: Boolean
    ) {
        val jsonFileString = getJsonDataFromAsset(requireContext(), who)
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
        values: WeightsDetailedData,
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
            Timber.e("Gestation Age::: $start $ges")
            if (start != 0) {
                if (ges > 13) {
                    val equivalent = extractValueIndexMonthly(start, values)
                    if (start == ges || start < ges) {
                        if (equivalent != "0") {
                            babyWeight.add(Entry(i.toFloat() + 1, equivalent.toFloat()))
                        }
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
        values: WeightsDetailedData,
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
            val birthWeightKg = convertToKg(birthWeight)
            val ges = values.weeksLife
            if (start == 0) {
                babyWeight.add(Entry(i.toFloat(), birthWeightKg.toFloat()))
            } else {
                val equivalent = extractValueIndexBirth(start, values)
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


    private fun barGraph(it: FeedsDistribution) {

        val groupCount = it.data.size
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
            try {
                feedsChart.data = data
                val xAxis: XAxis = feedsChart.xAxis
                xAxis.setDrawGridLines(false)
                xAxis.setDrawAxisLine(false)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.labelRotationAngle = -90f
                xAxis.mAxisMinimum = 0f

                xAxis.valueFormatter = IndexAxisValueFormatter(intervals)

                feedsChart.axisLeft.setDrawGridLines(false)
                feedsChart.legend.isEnabled = true

                //remove description label
                feedsChart.description.isEnabled = false
                feedsChart.isDragEnabled = true
                feedsChart.setScaleEnabled(false)
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun populateZScoreLineChart(
        values: WeightsDetailedData,
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
        val birthWeightKg = convertToKg(birthWeight)
        val commonIndex = establishCommonIndex(gestationAge, growths, birthWeightKg)

        for ((i, entry) in growths.subList(0, max + 1)
            .withIndex()) {

            intervals.add(entry.age.toString())
            val start = entry.age
            val ges = values.gestationAge.toInt() - 1

            if (start == ges || start > ges) {
                val equivalent = extractValueIndex(start, values)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard_menu, menu)
    }

    override fun onResume() {

        (requireActivity() as MainActivity).showBottomNavigationView(View.GONE)
        super.onResume()
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