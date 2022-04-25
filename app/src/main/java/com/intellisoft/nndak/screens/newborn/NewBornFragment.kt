package com.intellisoft.nndak.screens.newborn

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.fhir.FhirEngine
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.adapters.PatientDetailsRecyclerViewAdapter
import com.intellisoft.nndak.databinding.FragmentNewBornBinding
import com.intellisoft.nndak.screens.patients.PatientListFragmentDirections
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import timber.log.Timber

class NewBornFragment : Fragment() {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val args: NewBornFragmentArgs by navArgs()
    private var _binding: FragmentNewBornBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewBornBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        val adapter = PatientDetailsRecyclerViewAdapter(::onAddScreenerClick, ::onMaternityClick)
        binding.recycler.adapter = adapter
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = "New Born Unit"
            setDisplayHomeAsUpEnabled(true)
        }
        patientDetailsViewModel.livePatientData.observe(viewLifecycleOwner) { adapter.submitList(it) }
        patientDetailsViewModel.getPatientDetailData()
        (activity as MainActivity).setDrawerEnabled(false)
    }

    private fun onAddScreenerClick() {

    }

    private fun onMaternityClick() {

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.options_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavHostFragment.findNavController(this).navigateUp()
                true
            }
            R.id.menu_new_born -> {
                Timber.e("Resource ID::: " + args.patientId)
                findNavController().navigate(
                    NewBornFragmentDirections.navigateToScreening(
                        args.patientId, "nn-d2.json", "Rapid Assessment"
                    )
                )
                true
            }
            R.id.menu_assessment -> {
                Timber.e("Resource ID::: " + args.patientId)
                findNavController().navigate(
                    NewBornFragmentDirections.navigateToScreening(
                        args.patientId, "nn-e7.json", "Rapid Assessment"
                    )
                )
                true
            }
            R.id.menu_prescribe -> {
                Timber.e("Resource ID::: " + args.patientId)
                findNavController().navigate(
                    NewBornFragmentDirections.navigateToScreening(
                        args.patientId, "nn-e4.json", "Prescribe Feeds"
                    )
                )
                true
            }
            R.id.menu_relation -> {
                Timber.e("Resource ID::: " + args.patientId)
                activity?.let { FhirApplication.setCurrent(it, true) }
                findNavController().navigate(
                    NewBornFragmentDirections.navigateToScreening(
                        args.patientId, "child.json", "Related Person"
                    )
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}