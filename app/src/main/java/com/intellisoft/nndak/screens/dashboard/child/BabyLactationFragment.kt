package com.intellisoft.nndak.screens.dashboard.child

import android.os.Build
import android.os.Bundle
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
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.fhir.FhirEngine
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.databinding.FragmentBabyLactationBinding
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
class BabyLactationFragment : Fragment(), OnChartValueSelectedListener {
    private var _binding: FragmentBabyLactationBinding? = null
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val args: BabyAssessmentFragmentArgs by navArgs()

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


            /**
             * Lactation Status Chart
             */
            chart = binding.statusChart
            chart!!.setOnChartValueSelectedListener(this@BabyLactationFragment)


         //   populateBarChart()

            actionProvideSupport.setOnClickListener {
                findNavController().navigate(BabyLactationFragmentDirections.navigateToFeeding(args.patientId))
            }
        }
        patientDetailsViewModel.getMumChild()
        patientDetailsViewModel.liveMumChild.observe(viewLifecycleOwner) { motherBabyItem ->

            if (motherBabyItem != null) {
                binding.apply {
                    val gest = motherBabyItem.dashboard.gestation ?: ""
                    val sta = motherBabyItem.status
                    incDetails.tvBabyName.text = motherBabyItem.babyName
                    incDetails.tvMumName.text = motherBabyItem.motherName
                    incDetails.appBirthWeight.text = motherBabyItem.birthWeight
                    incDetails.appGestation.text = "$gest-$sta"
                    incDetails.appApgarScore.text = motherBabyItem.dashboard.apgarScore ?: ""
                    incDetails.appMumIp.text = motherBabyItem.motherIp
                    incDetails.appBabyWell.text = motherBabyItem.dashboard.babyWell ?: ""
                    incDetails.appAsphyxia.text = motherBabyItem.dashboard.asphyxia ?: ""
                    incDetails.appNeonatalSepsis.text =
                        motherBabyItem.dashboard.neonatalSepsis ?: ""
                    incDetails.appJaundice.text = motherBabyItem.dashboard.jaundice ?: ""
                    incDetails.appBirthDate.text = motherBabyItem.dashboard.dateOfBirth ?: ""
                    incDetails.appLifeDay.text = motherBabyItem.dashboard.dayOfLife ?: ""
                    incDetails.appAdmDate.text = motherBabyItem.dashboard.dateOfAdm ?: ""

                    /**
                     * Mum Details
                     */
                    incMum.tvMumName.text = motherBabyItem.motherName
                    incMum.appIpNumber.text = motherBabyItem.motherIp
                    incMum.appDeliveryMethod.text = motherBabyItem.mother.deliveryMethod
                    incMum.appParity.text = motherBabyItem.mother.parity
                    incMum.appPmctcStatus.text = motherBabyItem.mother.pmtctStatus
                    incMum.appDeliveryDate.text = motherBabyItem.mother.deliveryDate
                    incMum.appMultiplePregnancy.text = motherBabyItem.mother.multiPregnancy

                    response.appIpNumber.text = motherBabyItem.assessment.breastfeedingBaby
                    response.appMotherName.text = motherBabyItem.assessment.breastProblems
                    response.appBabyName.text = motherBabyItem.assessment.contraindicated

                }
            }
        }


    }

    private fun populateBarChart() {

        val values = arrayListOf<Int>(4, 7, 12, 2, 14, 30)
        //adding values
        val ourBarEntries: ArrayList<BarEntry> = ArrayList()

        for ((i, entry) in values.withIndex()) {
            val value = values[i].toFloat()
            ourBarEntries.add(BarEntry(i.toFloat(), value))
        }


        val barDataSet = BarDataSet(ourBarEntries, "Volume of Milk Expressed in 24 hours")
        //set a template coloring
        barDataSet.setColors(*ColorTemplate.COLORFUL_COLORS)
        val data = BarData(barDataSet)
        binding.statusChart.data = data
        //setting the x-axis
        val xAxis: XAxis = binding.statusChart.xAxis
        //calling methods to hide x-axis gridlines
        binding.statusChart.axisLeft.setDrawGridLines(false)
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)

        //remove legend
        binding.statusChart.legend.isEnabled = true

        //remove description label
        binding.statusChart.description.isEnabled = true

        //add animation
        binding.statusChart.animateY(3000)
        //refresh the chart
        binding.statusChart.invalidate()
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

    override fun onValueSelected(e: Entry?, h: Highlight?) {
      Timber.e("onValueSelected ")
    }

    override fun onNothingSelected() {
        Timber.e("onNothingSelected ")
    }
}