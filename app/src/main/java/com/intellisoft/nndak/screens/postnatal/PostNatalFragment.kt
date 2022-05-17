package com.intellisoft.nndak.screens.postnatal

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.fhir.FhirEngine
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.adapters.MaternityDetails
import com.intellisoft.nndak.databinding.FragmentPostNatalBinding
import com.intellisoft.nndak.models.EncounterItem
import com.intellisoft.nndak.models.RelatedPersonItem
import com.intellisoft.nndak.models.Steps
import com.intellisoft.nndak.utils.Constants.POST_LACTATION_ASSESSMENT
import com.intellisoft.nndak.utils.Constants.POST_MOTHER_ASSESSMENT
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import timber.log.Timber

class PostNatalFragment : Fragment() {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val args: PostNatalFragmentArgs by navArgs()
    private lateinit var unit: String
    private var _binding: FragmentPostNatalBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostNatalBinding.inflate(inflater, container, false)
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
        val steps = Steps(fistIn = "Assessment", lastIn = "Lactation Support", secondButton = true)
        unit = "Post Natal Unit"
        val adapter =
            MaternityDetails(
                this::onAddScreenerClick,
                this::lactationClick,
                this::assessmentClick,
                this::encounterClick,
                steps,
                true
            )
        binding.recycler.adapter = adapter
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = "Post Natal Unit"
            setDisplayHomeAsUpEnabled(true)
        }
        patientDetailsViewModel.livePatientData.observe(viewLifecycleOwner) { adapter.submitList(it) }
        patientDetailsViewModel.getMaternityDetailData(args.code)
        (activity as MainActivity).setDrawerEnabled(false)
        bottomSheetBehavior = BottomSheetBehavior.from(binding.post.bottomSheet)

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                val text = when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> "Close Persistent Bottom Sheet"
                    BottomSheetBehavior.STATE_COLLAPSED -> "Open Persistent Bottom Sheet"
                    else -> "Persistent Bottom Sheet"
                }
            }
        })
        bottomSheetButtons()
    }
    private fun encounterClick(encounter: EncounterItem) {
        Toast.makeText(activity, encounter.code, Toast.LENGTH_SHORT).show()
    }
    private fun bottomSheetButtons() {
        binding.post.imgExit.setOnClickListener {
            toggleSheet()

        }
        binding.post.imgNeeds.setOnClickListener {
            toggleSheet()
            activity?.let {
                FhirApplication.setCurrent(
                    it,
                    POST_LACTATION_ASSESSMENT
                )
            }
            findNavController().navigate(
                PostNatalFragmentDirections.navigateToScreening(
                    args.patientId,
                    "post-natal-lactation-assessment.json",
                    "Lactation Support Assessment"
                )
            )
        }
        binding.post.imgFeeds.setOnClickListener {
            toggleSheet()
            activity?.let {
                FhirApplication.setCurrent(
                    it,
                    POST_LACTATION_ASSESSMENT
                )
            }
            findNavController().navigate(
                PostNatalFragmentDirections.navigateToScreening(
                    args.patientId, "post-natal-milk-expression.json", "Milk Expression and Storage"
                )
            )
        }

    }

    private fun toggleSheet() {

        val state = if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
            BottomSheetBehavior.STATE_COLLAPSED
        else
            BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.state = state
    }

    private fun onAddScreenerClick(related: RelatedPersonItem) {
        findNavController().navigate(
            PostNatalFragmentDirections.navigateToChild(related.id, args.code, unit)
        )
    }

    private fun assessmentClick() {
        activity?.let {
            FhirApplication.setCurrent(
                it,
                POST_MOTHER_ASSESSMENT
            )
        }
        findNavController().navigate(
            PostNatalFragmentDirections.navigateToScreening(
                args.patientId, "post-natal-mother-assessment.json", "Mother’s Health Assessment"
            )
        )

    }

    private fun lactationClick() {
        toggleSheet()
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