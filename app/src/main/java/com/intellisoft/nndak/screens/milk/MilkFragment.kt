package com.intellisoft.nndak.screens.milk

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
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
import com.intellisoft.nndak.databinding.FragmentMilkBinding
import com.intellisoft.nndak.models.RelatedPersonItem
import com.intellisoft.nndak.models.Steps
import com.intellisoft.nndak.screens.postnatal.PostNatalFragmentDirections
import com.intellisoft.nndak.utils.Constants
import com.intellisoft.nndak.utils.Constants.MILK_CONSENT_FORM
import com.intellisoft.nndak.utils.Constants.MILK_PRESCRIPTION
import com.intellisoft.nndak.utils.Constants.MILK_RECEIVABLE
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import timber.log.Timber

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MilkFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MilkFragment : Fragment() {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val args: MilkFragmentArgs by navArgs()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var unit: String
    private var _binding: FragmentMilkBinding? = null
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
        _binding = FragmentMilkBinding.inflate(inflater, container, false)
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
        val steps =
            Steps(fistIn = "Consent Form", lastIn = "Order Confirmation", secondButton = true)
        unit = "Human Milk Bank"
        val adapter =
            MaternityDetails(
                this::onAddScreenerClick,
                this::prescriptionClick,
                this::consentFormClick,
                steps,
                true
            )
        binding.recycler.adapter = adapter
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = unit
            setDisplayHomeAsUpEnabled(true)
        }
        patientDetailsViewModel.livePatientData.observe(viewLifecycleOwner) { adapter.submitList(it) }
        patientDetailsViewModel.getPatientDetailData(false, args.code)
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


    private fun bottomSheetButtons() {
        updateTitleIcons()
        binding.post.imgExit.setOnClickListener {
            toggleSheet()

        }
        binding.post.imgNeeds.setOnClickListener {
            toggleSheet()
            activity?.let {
                FhirApplication.setCurrent(
                    it,
                    MILK_PRESCRIPTION
                )
            }
            findNavController().navigate(
                MilkFragmentDirections.navigateToScreening(
                    args.patientId, "human-milk-order-confirmation.json", "Order Confirmation"
                )
            )
        }
        binding.post.imgFeeds.setOnClickListener {
            toggleSheet()
            activity?.let {
                FhirApplication.setCurrent(
                    it,
                    MILK_RECEIVABLE
                )
            }
            findNavController().navigate(
                PostNatalFragmentDirections.navigateToScreening(
                    args.patientId, "human-milk-receivable.json", "Receive DHM"
                )
            )
        }

    }

    private fun updateTitleIcons() {

        binding.post.tvFirstTitle.text = getString(R.string.action_update_details)
        binding.post.tvLastTitle.text = getString(R.string.action_dhm_receival)

        binding.post.imgNeeds.setImageDrawable(
            activity?.let {
                ContextCompat.getDrawable(
                    it.applicationContext,
                    R.drawable.update
                )
            }
        )
        binding.post.imgFeeds.setImageDrawable(
            activity?.let {
                ContextCompat.getDrawable(
                    it.applicationContext,
                    R.drawable.received
                )
            }
        )

    }

    private fun toggleSheet() {

        val state = if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
            BottomSheetBehavior.STATE_COLLAPSED
        else
            BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.state = state
    }

    private fun onAddScreenerClick(related: RelatedPersonItem) {

    }

    private fun consentFormClick() {
        activity?.let {
            FhirApplication.setCurrent(
                it,
                MILK_CONSENT_FORM
            )
        }
        findNavController().navigate(
            MilkFragmentDirections.navigateToScreening(
                args.patientId, "human-milk-consent.json", "Consent Request"
            )
        )
    }

    private fun prescriptionClick() {
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