package com.intellisoft.nndak.screens.patients

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.sync.State
import com.intellisoft.nndak.*
import com.intellisoft.nndak.adapters.PatientItemRecyclerViewAdapter
import com.intellisoft.nndak.databinding.FragmentPatientListBinding
import com.intellisoft.nndak.models.PatientItem
import com.intellisoft.nndak.viewmodels.MainActivityViewModel
import com.intellisoft.nndak.viewmodels.PatientListViewModel
import com.intellisoft.nndak.viewmodels.TAG
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class PatientListFragment : Fragment() {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientListViewModel: PatientListViewModel
    private lateinit var searchView: SearchView
    private var _binding: FragmentPatientListBinding? = null
    private val binding
        get() = _binding!!
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()
    private val args: PatientListFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientListBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = resources.getString(R.string.title_patient_list)
            setDisplayHomeAsUpEnabled(true)
        }


        fhirEngine = FhirApplication.fhirEngine(requireContext())
        patientListViewModel =
            ViewModelProvider(
                this,
                PatientListViewModel.PatientListViewModelFactory(
                    requireActivity().application,
                    fhirEngine
                )
            )
                .get(PatientListViewModel::class.java)
        val recyclerView: RecyclerView = binding.patientListContainer.patientList
        val adapter = PatientItemRecyclerViewAdapter(this::onPatientItemClicked)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(

            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL).apply {
                setDrawable(ColorDrawable(Color.LTGRAY))
            }
        )

        patientListViewModel.liveSearchedPatients.observe(
            viewLifecycleOwner
        ) {
            Log.d("PatientListActivity", "Submitting ${it.count()} patient records")
            adapter.submitList(it)
        }

        patientListViewModel.patientCount.observe(
            viewLifecycleOwner
        ) { binding.patientListContainer.patientCount.text = "$it Patient(s)" }
        searchView = binding.search
        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {
                    patientListViewModel.searchPatientsByName(newText)
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
            addPatient.setOnClickListener { onAddPatientClick() }
            addPatient.setColorFilter(Color.WHITE)
        }
        setHasOptionsMenu(true)
        (activity as MainActivity).setDrawerEnabled(true)

        lifecycleScope.launch {
            mainActivityViewModel.pollState.collect {
                Log.d(TAG, "onViewCreated: pollState Got status $it")
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

    private fun onPatientItemClicked(patientItem: PatientItem) {
        if (args.step.isNotEmpty()) {
            if (args.step == "0") {

//                screenerScreen(patientItem, "maternity-registration.json", "Maternity Registration")
                findNavController().navigate(
                    PatientListFragmentDirections.actionPatientListToMaternityFragment(
                        patientItem.resourceId, "maternity-registration.json", "Maternity Registration"
                    )
                )
            }
            if (args.step == "1") {

                screenerScreen(patientItem, "mother-child-assessment.json", "Mother & Child Assessment")
            }
            if (args.step == "2") {

                screenerScreen(patientItem, "new-born.json", "New Born Unit")

            }
            if (args.step == "3") {
                screenerScreen(patientItem, "post-natal.json", "Post Natal Unit")
            }
            if (args.step == "4") {

                Timber.e("Resource ID::: " + patientItem.resourceId)
                findNavController().navigate(
                    PatientListFragmentDirections.navigateToProductDetail(
                        patientItem.resourceId
                    )
                )
            }
            if (args.step == "5") {

                screenerScreen(patientItem, "human-milk.json", "Human Milk Bank")
            }
            if (args.step == "6") {
                screenerScreen(patientItem, "assessment.json", "Monitoring & Assessment")
            }

        }
    }

    private fun screenerScreen(patientItem: PatientItem, asset: String, title: String) {

        Timber.e("Resource ID::: " + patientItem.resourceId)
        findNavController().navigate(
            PatientListFragmentDirections.actionPatientListToScreenerEncounterFragment(
                patientItem.resourceId, asset, title
            )
        )
    }

    private fun onAddPatientClick() {
        findNavController().navigate(PatientListFragmentDirections.actionPatientListToAddPatientFragment())
    }
}
