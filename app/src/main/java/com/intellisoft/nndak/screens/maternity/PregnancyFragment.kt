package com.intellisoft.nndak.screens.maternity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager.widget.ViewPager
import com.google.android.fhir.FhirEngine
import com.google.android.material.tabs.TabLayout
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.adapters.CustomAdapter
import com.intellisoft.nndak.adapters.MaternityDetails
import com.intellisoft.nndak.databinding.FragmentMaternityBinding
import com.intellisoft.nndak.databinding.FragmentPregnancyBinding
import com.intellisoft.nndak.models.EncounterItem
import com.intellisoft.nndak.models.RelatedPersonItem
import com.intellisoft.nndak.models.Steps
import com.intellisoft.nndak.utils.Constants
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory


class PregnancyFragment : Fragment() {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var unit: String
    private lateinit var patientId: String
    private var _binding: FragmentPregnancyBinding? = null
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
        _binding = FragmentPregnancyBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fhirEngine = FhirApplication.fhirEngine(requireContext())
        patientId = FhirApplication.getPatient(requireContext())
        patientDetailsViewModel =
            ViewModelProvider(
                this,
                PatientDetailsViewModelFactory(
                    requireActivity().application,
                    fhirEngine,
                    patientId
                )
            )
                .get(PatientDetailsViewModel::class.java)

        val steps = Steps(fistIn = "Record Maternity", lastIn = "New Born", secondButton = true)
        unit = "Maternity Unit"
        val adapter =
            MaternityDetails(
                this::onAddScreenerClick,
                this::newBorn,
                this::maternityClick,
                this::encounterClick,
                steps,
                true
            )
        binding.recycler.adapter = adapter
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = unit
            setDisplayHomeAsUpEnabled(true)
        }
        patientDetailsViewModel.livePatientData.observe(viewLifecycleOwner) { adapter.submitList(it) }
        patientDetailsViewModel.getEncountersData()
        (activity as MainActivity).setDrawerEnabled(false)
    }
    private fun encounterClick(encounter: EncounterItem) {
//        findNavController().navigate(
//            MaternityFragmentDirections.navigateToObservations(
//                args.patientId,
//                encounter.id
//            )
//        )
    }

    private fun onAddScreenerClick(related: RelatedPersonItem) {
//        findNavController().navigate(
//            MaternityFragmentDirections.navigateToChild(related.id, args.code, unit)
//        )
    }

    private fun newBorn() {
        activity?.let {
            FhirApplication.setCurrent(
                it,
                Constants.NEWBORN
            )
        }
//        findNavController().navigate(
//            MaternityFragmentDirections.navigateToScreening(
//                args.patientId, "maternal-child-registration.json", "Maternity Unit"
//            )
        //   )
    }

    private fun maternityClick() {
        // requestStage()
        activity?.let {
            FhirApplication.setCurrent(
                it,
                Constants.MATERNITY
            )
        }

    }
}