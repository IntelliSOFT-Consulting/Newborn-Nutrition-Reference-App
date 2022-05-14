package com.intellisoft.nndak.screens.assessments

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
import com.intellisoft.nndak.adapters.MaternityDetails
import com.intellisoft.nndak.adapters.PatientDetailsRecyclerViewAdapter
import com.intellisoft.nndak.databinding.FragmentAssessmentBinding
import com.intellisoft.nndak.models.RelatedPersonItem
import com.intellisoft.nndak.models.Steps
import com.intellisoft.nndak.screens.maternity.MaternityFragmentDirections
import com.intellisoft.nndak.utils.Constants
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import timber.log.Timber

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AssessmentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AssessmentFragment : Fragment() {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val args: AssessmentFragmentArgs by navArgs()
    private lateinit var unit: String
    private var _binding: FragmentAssessmentBinding? = null
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
        _binding = FragmentAssessmentBinding.inflate(inflater, container, false)
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
        val steps = Steps(fistIn = "Assessment", lastIn = "Child Assessment", secondButton = true)
        unit = "Assessment"
        val adapter =
            MaternityDetails(
                this::onAddScreenerClick,
                this::childAssessmentClick,
                this::assessmentClick,
                steps,
                false
            )
        binding.recycler.adapter = adapter
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = "Assessment"
            setDisplayHomeAsUpEnabled(true)
        }
        patientDetailsViewModel.livePatientData.observe(viewLifecycleOwner) { adapter.submitList(it) }
        patientDetailsViewModel.getMaternityDetailData(args.code)
        (activity as MainActivity).setDrawerEnabled(false)

    }

    private fun onAddScreenerClick(related: RelatedPersonItem) {
        findNavController().navigate(
            AssessmentFragmentDirections.navigateToChild(related.id, args.code, unit)
        )
    }

    private fun childAssessmentClick() {

    }

    private fun assessmentClick() {


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