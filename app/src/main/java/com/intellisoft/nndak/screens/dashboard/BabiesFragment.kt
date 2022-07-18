package com.intellisoft.nndak.screens.dashboard

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.sync.State
import com.google.gson.Gson
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.adapters.BabyItemAdapter
import com.intellisoft.nndak.data.SessionData
import com.intellisoft.nndak.databinding.FragmentBabiesBinding
import com.intellisoft.nndak.helper_class.FormatHelper
import com.intellisoft.nndak.models.*
import com.intellisoft.nndak.viewmodels.MainActivityViewModel
import com.intellisoft.nndak.viewmodels.PatientListViewModel
import kotlinx.android.synthetic.main.patient_list_item_view.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BabiesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BabiesFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientListViewModel: PatientListViewModel
    private lateinit var searchView: SearchView
    private var _binding: FragmentBabiesBinding? = null
    lateinit var adapterList: BabyItemAdapter
    private var mumBabyList = ArrayList<MotherBabyItem>()
    private val binding
        get() = _binding!!
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    private lateinit var mySpinner: Spinner

    private var formatter = FormatHelper()
    private var status: String = "Filter By:"


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBabiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = resources.getString(R.string.home_babies)
            setDisplayHomeAsUpEnabled(true)
        }
        try {
            fhirEngine = FhirApplication.fhirEngine(requireContext())
            patientListViewModel =
                ViewModelProvider(
                    this,
                    PatientListViewModel.PatientListViewModelFactory(
                        requireActivity().application,
                        fhirEngine, "0"
                    )
                ).get(PatientListViewModel::class.java)
            val recyclerView: RecyclerView = binding.patientListContainer.patientList

            adapterList = BabyItemAdapter(mumBabyList, this::onPatientItemClicked)
            recyclerView.adapter = adapterList
            adapterList.submitList(mumBabyList)
            binding.pbLoading.visibility = View.VISIBLE
            patientListViewModel.liveMotherBaby.observe(viewLifecycleOwner) {
                Timber.d("Submitting " + it.count() + " patient records")
                if (it.isEmpty()) {
                    binding.apply {
                        imgEmpty.visibility = View.VISIBLE
                        binding.pbLoading.visibility = View.GONE
                    }
                }
                if (it.isNotEmpty()) {
                    mumBabyList.clear()
                    when (status) {
                        "Filter By:" -> {
                            displayComplete(it)
                        }
                        "Preterm" -> {
                            filterByStatus(it, "Preterm")
                        }
                        "Term" -> {
                            filterByStatus(it, "Term")
                        }
                        "Low Weight Gain" -> {

                            filterByGain(it, true)
                        }
                        "High/Medium Weight Gain" -> {

                            filterByGain(it, false)
                        }
                        else -> {
                            displayComplete(it)
                        }
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }


        mySpinner = binding.mySpinner

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.birds,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mySpinner.adapter = adapter
        mySpinner.onItemSelectedListener = this

        patientListViewModel.patientCount.observe(
            viewLifecycleOwner
        ) { binding.patientListContainer.patientCount.text = "$it Patient(s)" }
        searchView = binding.search
        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {
                    adapterList.filter.filter(newText)
                    return false
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    adapterList.filter.filter(query)
                    return false
                }
            }
        )
        searchView.setOnQueryTextFocusChangeListener { view, focused ->
            if (!focused) {
                // hide soft keyboard
                (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (searchView.query.isNotEmpty()) {
                            searchView.setQuery("", true)
                        } else {
                            isEnabled = false
                            activity?.onBackPressed()
                        }
                    }
                }
            )

        binding.apply {
            actionAddBaby.setOnClickListener { onAddPatientClick() }

        }
        setHasOptionsMenu(true)
        (activity as MainActivity).setDrawerEnabled(true)

        lifecycleScope.launch {
            mainActivityViewModel.pollState.collect {
                Timber.d("onViewCreated: pollState Got status $it")
                // After the sync is successful, update the patients list on the page.
                if (it is State.Finished) {
                    patientListViewModel.searchPatientsByName(searchView.query.toString().trim())
                }
            }
        }
    }

    private fun displayComplete(it: List<MotherBabyItem>) {
        mumBabyList.addAll(it)
        binding.pbLoading.visibility = View.GONE
        binding.apply {
            if (mumBabyList.isEmpty()) {
                imgEmpty.visibility = View.VISIBLE
            } else {
                imgEmpty.visibility = View.GONE
            }
        }
    }

    private fun filterByGain(it: List<MotherBabyItem>, isLow: Boolean) {
        it.forEach { d ->
            val status = d.gainRate
            if (isLow) {
                if (status == "Low") {
                    mumBabyList.add(d)
                }
            } else {
                if (status == "Normal" || status == "High") {
                    mumBabyList.add(d)
                }
            }
        }
        binding.apply {
            if (mumBabyList.isEmpty()) {
                imgEmpty.visibility = View.VISIBLE
            } else {

                imgEmpty.visibility = View.GONE
            }
        }
    }

    private fun filterByStatus(babies: List<MotherBabyItem>, s: String) {
        for ((i, baby) in babies.withIndex()) {
            if (baby.status == s) {
                mumBabyList.add(baby)
            }
        }
        /*   baby.forEach {
               val status = it.status
               if (status.equals(s)) {
                   mumBabyList.add(it)

               }
               Timber.e("Selected Status $s  Current $status ${it.babyName} ${it.status}\n" +
                       "Added $mumBabyList")

           }*/
        binding.apply {
            if (mumBabyList.isEmpty()) {
                imgEmpty.visibility = View.VISIBLE
            } else {

                imgEmpty.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onResume() {

        (requireActivity() as MainActivity).showBottomNavigationView(View.GONE)
        super.onResume()
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

    private fun onPatientItemClicked(patientItem: MotherBabyItem) {
        if (patientItem.motherIp.isNotEmpty()) {
            val session = SessionData(
                patientId = patientItem.resourceId,
                status = patientItem.dashboard.assessed
            )
            val gson = Gson()
            val json = gson.toJson(session)
            FhirApplication.setDashboardActive(
                requireContext(),
                json
            )

            findNavController().navigate(
                BabiesFragmentDirections.navigateToChildDashboard(
                    patientItem.resourceId
                )
            )

        } else {
            Toast.makeText(requireContext(), "Patient Loading..., please wait", Toast.LENGTH_SHORT)
                .show()
        }
    }


    private fun onAddPatientClick() {
        findNavController().navigate(BabiesFragmentDirections.navigateToRegisterClient())
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val text: String = p0?.getItemAtPosition(p2).toString()
        patientListViewModel.searchPatientsByName("")
        status = text

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }
}