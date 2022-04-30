package com.intellisoft.nndak.screens.children

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
import com.intellisoft.nndak.databinding.FragmentChildBinding
import com.intellisoft.nndak.databinding.FragmentMaternityBinding
import com.intellisoft.nndak.models.RelatedPersonItem
import com.intellisoft.nndak.screens.maternity.MaternityFragmentArgs
import com.intellisoft.nndak.screens.maternity.MaternityFragmentDirections
import com.intellisoft.nndak.screens.patients.PatientDetailsFragmentDirections
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import timber.log.Timber

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChildFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChildFragment : Fragment() {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val args: ChildFragmentArgs by navArgs()
    private var _binding: FragmentChildBinding? = null
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
        _binding = FragmentChildBinding.inflate(inflater, container, false)
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
        val adapter = MaternityDetails(this::onAddScreenerClick)
        binding.recycler.adapter = adapter
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = "Maternity Unit"
            setDisplayHomeAsUpEnabled(true)
        }
        patientDetailsViewModel.livePatientData.observe(viewLifecycleOwner) { adapter.submitList(it) }
        patientDetailsViewModel.getPatientDetailData()
        (activity as MainActivity).setDrawerEnabled(false)
        activity?.let {
            FhirApplication.setCurrent(it, newBorn = false, apgar = false, maternity = false)
        }
        binding.actionScore.setOnClickListener {
            activity?.let {
                FhirApplication.setCurrent(
                    it,
                    newBorn = false,
                    apgar = true,
                    maternity = false
                )
            }
            findNavController().navigate(
                ChildFragmentDirections.navigateToScreening(
                    args.patientId, "apgar-score.json", "Apgar Score"
                )
            )
        }
        binding.actionAssess.setOnClickListener {
            activity?.let {
                FhirApplication.setCurrent(
                    it,
                    newBorn = false,
                    apgar = false,
                    maternity = false
                )
            }
            findNavController().navigate(
                ChildFragmentDirections.navigateToScreening(
                    args.patientId, "nn-a5.json", "NewBorn Registration"
                )
            )
        }
    }

    private fun onAddScreenerClick(related: RelatedPersonItem) {
        // Toast.makeText(context, related.id, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.details_options_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavHostFragment.findNavController(this).navigateUp()
                true
            }
            R.id.menu_patient_edit -> {

                findNavController()
                    .navigate(
                        ChildFragmentDirections.navigateToEditPatient(
                            args.patientId
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