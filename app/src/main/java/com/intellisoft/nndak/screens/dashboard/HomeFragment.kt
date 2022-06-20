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
import cn.pedant.SweetAlert.SweetAlertDialog
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.fhir.FhirEngine
import com.google.gson.Gson
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.charts.DHMData
import com.intellisoft.nndak.charts.DHMModel
import com.intellisoft.nndak.data.RestManager
import com.intellisoft.nndak.databinding.FragmentHomeBinding
import com.intellisoft.nndak.helper_class.FormatHelper
import com.intellisoft.nndak.logic.Logics.Companion.ADMIN
import com.intellisoft.nndak.logic.Logics.Companion.DOCTOR
import com.intellisoft.nndak.logic.Logics.Companion.HMB
import com.intellisoft.nndak.utils.getPastDaysOnIntervalOf
import com.intellisoft.nndak.utils.isNetworkAvailable
import com.intellisoft.nndak.viewmodels.PatientListViewModel
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber
import java.time.LocalDate


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
    private val apiService = RestManager()
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
            setHomeAsUpIndicator(R.drawable.dash)
            setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)
        (activity as MainActivity).setDrawerEnabled(true)

        syncLocalData()


        binding.apply {
            actionEnterStock.setOnClickListener {

                val allowed = validatePermission()
                if (allowed) {
                    findNavController().navigate(HomeFragmentDirections.navigateToStock())
                } else {
                    accessDenied()
                }
            }
            actionViewDhm.setOnClickListener {
                findNavController().navigate(HomeFragmentDirections.navigateToOrders())
            }
        }
        if (isNetworkAvailable(requireContext())) {

            loadLiveData()
        } else {
            syncLocalData()
        }
    }

    private fun validatePermission(): Boolean {

        val role = (requireActivity() as MainActivity).retrieveUser(true)
        if (role.isNotEmpty()) {
            return role == ADMIN || role == DOCTOR || role == HMB
        }
        return false
    }

    private fun accessDenied() {
        SweetAlertDialog(requireContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
            .setTitleText("Access Denied!!")
            .setContentText("You are not Authorized")
            .setCustomImage(R.drawable.smile)

            .show()
    }

    private fun loadLiveData() {
        apiService.loadDonorMilk(requireContext()) {

            if (it != null) {
                val gson = Gson()
                val json = gson.toJson(it)
                Timber.e("Local Sync Dara $json")
                FhirApplication.updateDHM(requireContext(), json)
                updateUI(it)
            } else {
                Timber.e("Failed to Load Data")
                syncLocalData()
            }
        }
    }

    private fun updateUI(it: DHMModel) {
        binding.apply {
            tvDhmInfants.text = it.dhmInfants
            tvVolumeAvailable.text = it.dhmVolume
            tvAverageVolume.text = it.dhmAverage
            tvFullyInfants.text = it.fullyReceiving
            tvAverageLength.text = it.dhmLength
        }

        populateData(it.data)

    }

    private fun syncLocalData() {

        val data = FhirApplication.getDHM(requireContext())
        if (data != null) {
            val gson = Gson()
            Timber.e("Local Sync Dara $data")
            try {
                val it: DHMModel = gson.fromJson(data, DHMModel::class.java)
                updateUI(it)

            } catch (e: Exception) {
                Timber.e("Local Sync Error ${e.localizedMessage}")
            }
        }
    }


    private fun populateData(data: List<DHMData>) {

        val values = getPastDaysOnIntervalOf(7, 1)

        if (values.isNotEmpty()) {
            val dayNames = formatDays(values)
            Timber.e("Days $dayNames")
            Timber.e("Values Count ${values.size}")

            val preterm: ArrayList<Entry> = ArrayList()
            val term: ArrayList<Entry> = ArrayList()
            val total: ArrayList<Entry> = ArrayList()

            for ((i, entry) in data.withIndex()) {
                preterm.add(Entry(i.toFloat(), entry.preterm.toFloat()))
                term.add(Entry(i.toFloat(), entry.term.toFloat()))
                total.add(Entry(i.toFloat(), entry.total.toFloat()))
            }

            val lessThanFive = LineDataSet(preterm, "Preterm DHM")
            lessThanFive.setColors(Color.parseColor("#F65050"))
            lessThanFive.setDrawCircleHole(false)
            lessThanFive.setDrawValues(false)
            lessThanFive.setDrawCircles(false)
            lessThanFive.mode = LineDataSet.Mode.CUBIC_BEZIER

            val lessThanSeven = LineDataSet(term, "Term DHM")
            lessThanSeven.setColors(Color.parseColor("#1EAF5F"))
            lessThanSeven.setDrawCircleHole(false)
            lessThanSeven.setDrawValues(false)
            lessThanSeven.setDrawCircles(false)
            lessThanSeven.mode = LineDataSet.Mode.CUBIC_BEZIER

            val moreThanSeven = LineDataSet(total, "Total DHM")
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
            xAxis.labelRotationAngle = -60f
            xAxis.valueFormatter = IndexAxisValueFormatter(dayNames)


            binding.totalTermChart.legend.isEnabled = true

            //remove description label
            binding.totalTermChart.description.isEnabled = false
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

    private fun formatDays(values: List<LocalDate>): ArrayList<String> {
        val days = ArrayList<String>()
        values.forEach {
            val format = FormatHelper().getDayName(it.toString())
            days.add(format)
        }
        return days
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.hidden_menu, menu)
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