package com.intellisoft.nndak.screens.newborn

import android.os.Bundle
import android.view.*
import android.widget.Toast
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
import com.intellisoft.nndak.adapters.MaternityDetails
import com.intellisoft.nndak.adapters.PatientDetailsRecyclerViewAdapter
import com.intellisoft.nndak.databinding.FragmentNewBornBinding
import com.intellisoft.nndak.models.RelatedPersonItem
import com.intellisoft.nndak.models.Steps
import com.intellisoft.nndak.screens.maternity.MaternityFragmentDirections
import com.intellisoft.nndak.screens.patients.PatientListFragmentDirections
import com.intellisoft.nndak.utils.Constants
import com.intellisoft.nndak.utils.Constants.MOTHER_ASSESSMENT
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import timber.log.Timber

class NewBornFragment : Fragment() {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val args: NewBornFragmentArgs by navArgs()
    private lateinit var unit: String
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
        val steps = Steps(fistIn = "Assessment", lastIn = "Record Feeding", secondButton = false)
        unit = "New Born Unit"
        val adapter = MaternityDetails(
            this::onAddScreenerClick,
            this::recordFeeding,
            this::assessmentClick,
            steps,
            true
        )
        binding.recycler.adapter = adapter
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = unit
            setDisplayHomeAsUpEnabled(true)
        }
        patientDetailsViewModel.livePatientData.observe(viewLifecycleOwner) { adapter.submitList(it) }
        patientDetailsViewModel.getMaternityDetailData()
        (activity as MainActivity).setDrawerEnabled(false)

    }


    private fun onAddScreenerClick(related: RelatedPersonItem) {
        findNavController().navigate(
            NewBornFragmentDirections.navigateToChild(related.id, args.code, unit)
        )
    }

    private fun recordFeeding() {
      /*  findNavController().navigate(
            NewBornFragmentDirections.navigateToScreening(
                args.patientId, "record-feeding-data.json", "Rapid Assessment"
            )
        )*/
    }

    private fun assessmentClick() {
        activity?.let {
            FhirApplication.setCurrent(
                it,
                MOTHER_ASSESSMENT
            )
        }
        findNavController().navigate(
            NewBornFragmentDirections.navigateToScreening(
                args.patientId, "mothers-medical.json", "Rapid Assessment"
            )
        )
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.hidden_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavHostFragment.findNavController(this).navigateUp()
                true
            }

            R.id.menu_prescribe -> {

                findNavController().navigate(
                    NewBornFragmentDirections.navigateToScreening(
                        args.patientId, "nn-e4.json", "Prescribe Feeds"
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