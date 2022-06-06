package com.intellisoft.nndak.screens.dashboard

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
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
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.adapters.BabyItemAdapter
import com.intellisoft.nndak.databinding.FragmentBabiesBinding
import com.intellisoft.nndak.helper_class.DbMotherKey
import com.intellisoft.nndak.helper_class.FormatHelper
import com.intellisoft.nndak.models.*
import com.intellisoft.nndak.roomdb.HealthViewModel
import com.intellisoft.nndak.viewmodels.MainActivityViewModel
import com.intellisoft.nndak.viewmodels.PatientListViewModel
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
    private val binding
        get() = _binding!!
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    private lateinit var mySpinner: Spinner

    private var formatter = FormatHelper()

    private var filterData: String? = null

    private lateinit var healthViewModel: HealthViewModel

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
            val adapter = BabyItemAdapter(this::onPatientItemClicked)
            recyclerView.adapter = adapter

            patientListViewModel.liveMotherBaby.observe(viewLifecycleOwner) {
                Timber.d("Submitting " + it.count() + " patient records")
                if (it.isEmpty()) {
                    binding.apply {
                        imgEmpty.visibility = View.VISIBLE
                    }
                }
                binding.pbLoading.visibility = View.GONE
                adapter.submitList(it)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        healthViewModel = HealthViewModel(requireActivity().application)

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

                    if (filterData == DbMotherKey.PATIENT_NAME.name) {
                        patientListViewModel.searchPatientsByName(newText)
                    } else {

                        formatter.saveSharedPreference(
                            requireContext(),
                            "queryValue", newText.toString()
                        )

                        val motherInfo =
                            filterData?.let { healthViewModel.getMotherInfo(it, requireContext()) }

                        if (motherInfo != null) {

                            val patientName = motherInfo.familyName
                            patientListViewModel.searchPatientsByName(patientName)

                        } else {

                            patientListViewModel.searchPatientsByName(newText)

//                            Toast.makeText(
//                                requireContext(),
//                                "We could not find the patient.",
//                                Toast.LENGTH_SHORT
//                            ).show()
                        }


                    }


                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    patientListViewModel.searchPatientsByName(query)
                    return true
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
                // hide the soft keyboard when the navigation drawer is shown on the screen.
                searchView.clearFocus()
                (requireActivity() as MainActivity).openNavigationDrawer()
                true
            }
            else -> false
        }
    }

    private fun onPatientItemClicked(patientItem: MotherBabyItem) {
        findNavController().navigate(
            BabiesFragmentDirections.navigateToChildDashboard(
                patientItem.resourceId,
                true
            )
        )
    }


    private fun onAddPatientClick() {
        findNavController().navigate(BabiesFragmentDirections.navigateToRegisterClient())
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val text: String = p0?.getItemAtPosition(p2).toString()
        filterData = formatter.getSearchQuery(text, requireContext())

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }
}