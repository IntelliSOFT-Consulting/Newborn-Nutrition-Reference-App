package com.intellisoft.nndak.screens

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
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
import com.intellisoft.nndak.adapters.ObservationsAdapter
import com.intellisoft.nndak.databinding.FragmentMaternityBinding
import com.intellisoft.nndak.databinding.FragmentObservationsBinding
import com.intellisoft.nndak.models.EncounterItem
import com.intellisoft.nndak.models.RelatedPersonItem
import com.intellisoft.nndak.models.Steps
import com.intellisoft.nndak.screens.maternity.MaternityFragmentArgs
import com.intellisoft.nndak.screens.maternity.MaternityFragmentDirections
import com.intellisoft.nndak.utils.Constants
import com.intellisoft.nndak.viewmodels.EncounterDetailsViewModel
import com.intellisoft.nndak.viewmodels.EncounterDetailsViewModelFactory
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ObservationsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ObservationsFragment : Fragment() {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var encounterDetailsViewModel: EncounterDetailsViewModel
    private val args: ObservationsFragmentArgs by navArgs()
    private lateinit var unit: String
    private var _binding: FragmentObservationsBinding? = null
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
        _binding = FragmentObservationsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fhirEngine = FhirApplication.fhirEngine(requireContext())
        encounterDetailsViewModel =
            ViewModelProvider(
                this,
                EncounterDetailsViewModelFactory(
                    requireActivity().application,
                    fhirEngine,
                    args.encounterId
                )
            )
                .get(EncounterDetailsViewModel::class.java)

        unit = args.title
        val adapter = ObservationsAdapter()
        binding.recycler.adapter = adapter
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = unit
            setDisplayHomeAsUpEnabled(true)
        }
        encounterDetailsViewModel.liveEncounterData.observe(viewLifecycleOwner) { adapter.submitList(it) }
        encounterDetailsViewModel.loadObservations(args.encounterId)
        (activity as MainActivity).setDrawerEnabled(false)

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

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}