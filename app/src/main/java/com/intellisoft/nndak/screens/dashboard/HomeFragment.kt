package com.intellisoft.nndak.screens.dashboard

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.fhir.FhirEngine
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.databinding.FragmentHomeBinding
import com.intellisoft.nndak.viewmodels.PatientListViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientListViewModel: PatientListViewModel
    private val binding
        get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = resources.getString(R.string.home_menu_dashboard)
            setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)

        fhirEngine = FhirApplication.fhirEngine(requireContext())
        patientListViewModel =
            ViewModelProvider(
                this,
                PatientListViewModel.PatientListViewModelFactory(
                    requireActivity().application,
                    fhirEngine, "0"
                )
            )
                .get(PatientListViewModel::class.java)
        patientListViewModel.liveDHMDashboard.observe(viewLifecycleOwner) {

            if (it != null) {
                binding.apply {
                    tvDhmInfants.text = it.dhmInfants
                    tvVolumeAvailable.text = it.dhmVolume
                    tvAverageVolume.text = it.dhmAverageVolume
                    tvFullyInfants.text = it.dhmFullyInfants
                    tvAverageLength.text = it.dhmAverageLength

                    populateGraphData()

                }
            }
        }

        binding.apply {
            actionEnterStock.setOnClickListener {
                findNavController().navigate(HomeFragmentDirections.navigateToStock())
            }
            actionViewDhm.setOnClickListener {
                findNavController().navigate(HomeFragmentDirections.navigateToOrders())
            }
        }

    }


    private fun populateGraphData() {

        val values = arrayListOf<Int>(2, 4, 6, 8, 6, 4)
        val ourLineChartEntries: ArrayList<Entry> = ArrayList()

        for ((i, entry) in values.withIndex()) {
            val value = values[i].toFloat()
            ourLineChartEntries.add(Entry(i.toFloat(), value))
        }
        val values2 = arrayListOf<Int>(1, 3, 5, 7, 5, 3)
        val ourLineChartEntries2: ArrayList<Entry> = ArrayList()

        for ((i, entry) in values2.withIndex()) {
            val value = values2[i].toFloat()
            ourLineChartEntries2.add(Entry(i.toFloat(), value))
        }
        val values3 = arrayListOf<Int>(0, 2, 4, 6, 4, 2, 0)
        val ourLineChartEntries3: ArrayList<Entry> = ArrayList()

        for ((i, entry) in values3.withIndex()) {
            val value = values3[i].toFloat()
            ourLineChartEntries3.add(Entry(i.toFloat(), value))
        }

        /**First Line Chart Data*/
        val lineDataSet = LineDataSet(ourLineChartEntries, "")
        lineDataSet.setColors(*ColorTemplate.PASTEL_COLORS)

        val lineDataSet2 = LineDataSet(ourLineChartEntries2, "")
        lineDataSet2.setColors(*ColorTemplate.PASTEL_COLORS)

        val lineDataSet3 = LineDataSet(ourLineChartEntries3, "")
        lineDataSet3.setColors(*ColorTemplate.PASTEL_COLORS)


        val data = LineData(lineDataSet, lineDataSet2, lineDataSet3)


        binding.totalTermChart.axisLeft.setDrawGridLines(false)
        val xAxis: XAxis = binding.totalTermChart.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)

        binding.totalTermChart.legend.isEnabled = false

        //remove description label
        binding.totalTermChart.description.isEnabled = false
        binding.totalTermChart.isDragEnabled = true
        binding.totalTermChart.setScaleEnabled(true)
        //add animation
        binding.totalTermChart.animateX(1000, Easing.EaseInSine)
        binding.totalTermChart.data = data
        //refresh
        binding.totalTermChart.invalidate()
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
            else -> false
        }
    }
}