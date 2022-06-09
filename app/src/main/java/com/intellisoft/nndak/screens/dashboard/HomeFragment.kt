package com.intellisoft.nndak.screens.dashboard

import android.graphics.Color
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
import com.github.mikephil.charting.components.YAxis
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

                    populateData()

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


    private fun populateData() {

        val values = arrayListOf<Int>(4, 7, 12, 2, 3, 2, 1, 8, 6, 4, 2, 7)
        if (values.isNotEmpty()) {
            val lessFive: ArrayList<Entry> = ArrayList()
            val lessSeven: ArrayList<Entry> = ArrayList()
            val moreSeven: ArrayList<Entry> = ArrayList()

            for ((i, entry) in values.withIndex()) {
                val value = values[i].toFloat()
                lessFive.add(Entry(i.toFloat(), value))
                lessSeven.add(Entry(i.toFloat() + 1, value))
                moreSeven.add(Entry(i.toFloat() - 1, value))
            }
            val lessThanFive = LineDataSet(lessFive, "Preterm DHM")
            lessThanFive.setColors(Color.parseColor("#F65050"))
            lessThanFive.setDrawCircleHole(false)
            lessThanFive.setDrawValues(false)
            lessThanFive.setDrawCircles(false)
            lessThanFive.mode = LineDataSet.Mode.CUBIC_BEZIER

            val lessThanSeven = LineDataSet(lessSeven, "Term DHM")
            lessThanSeven.setColors(Color.parseColor("#1EAF5F"))
            lessThanSeven.setDrawCircleHole(false)
            lessThanSeven.setDrawValues(false)
            lessThanSeven.setDrawCircles(false)
            lessThanSeven.mode = LineDataSet.Mode.CUBIC_BEZIER

            val moreThanSeven = LineDataSet(moreSeven, "Total DHM")
            moreThanSeven.setColors(Color.parseColor("#77A9FF"))
            moreThanSeven.setDrawCircleHole(false)
            moreThanSeven.setDrawValues(false)
            moreThanSeven.setDrawCircles(false)
            moreThanSeven.mode = LineDataSet.Mode.CUBIC_BEZIER

            val data = LineData(lessThanFive, lessThanSeven, moreThanSeven)
            binding.totalTermChart.axisLeft.setDrawGridLines(false)

            val xAxis: XAxis = binding.totalTermChart.xAxis
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM

            binding.totalTermChart.legend.isEnabled = true

            //remove description label
            binding.totalTermChart.description.isEnabled = true
            binding.totalTermChart.isDragEnabled = true
            binding.totalTermChart.setScaleEnabled(true)
            binding.totalTermChart.description.text = "Age (Days)"
            //add animation
            binding.totalTermChart.animateX(1000, Easing.EaseInSine)
            binding.totalTermChart.data = data

            val leftAxis: YAxis = binding.totalTermChart.axisLeft
            leftAxis.axisMinimum = 0f
            leftAxis.setDrawGridLines(true)
            leftAxis.isGranularityEnabled = false


            val rightAxis: YAxis = binding.totalTermChart.axisRight
            rightAxis.setDrawGridLines(false)
            rightAxis.setDrawZeroLine(false)
            rightAxis.isGranularityEnabled = false
            rightAxis.isEnabled = false
            //refresh
            binding.totalTermChart.invalidate()
        }
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