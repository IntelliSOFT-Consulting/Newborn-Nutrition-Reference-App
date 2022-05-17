package com.intellisoft.nndak.screens.maternity

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
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
import com.intellisoft.nndak.auth.LoginActivity
import com.intellisoft.nndak.databinding.FragmentMaternityBinding
import com.intellisoft.nndak.models.EncounterItem
import com.intellisoft.nndak.models.RelatedPersonItem
import com.intellisoft.nndak.models.Steps
import com.intellisoft.nndak.utils.Constants.MATERNITY
import com.intellisoft.nndak.utils.Constants.NEWBORN
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MaternityFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MaternityFragment : Fragment() {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val args: MaternityFragmentArgs by navArgs()
    private lateinit var unit: String
    private var _binding: FragmentMaternityBinding? = null
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
        _binding = FragmentMaternityBinding.inflate(inflater, container, false)
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
        patientDetailsViewModel.getMaternityDetailData(args.code)
        (activity as MainActivity).setDrawerEnabled(false)

    }

    private fun encounterClick(encounter: EncounterItem) {
        findNavController().navigate(
            MaternityFragmentDirections.navigateToObservations(
                args.patientId,
                encounter.id
            )
        )
    }

    private fun onAddScreenerClick(related: RelatedPersonItem) {
        findNavController().navigate(
            MaternityFragmentDirections.navigateToChild(related.id, args.code, unit)
        )
    }

    private fun newBorn() {
        activity?.let {
            FhirApplication.setCurrent(
                it,
                NEWBORN
            )
        }
        findNavController().navigate(
            MaternityFragmentDirections.navigateToScreening(
                args.patientId, "maternal-child-registration.json", "Maternity Unit"
            )
        )
    }

    private fun maternityClick() {
        requestStage()
        activity?.let {
            FhirApplication.setCurrent(
                it,
                MATERNITY
            )
        }

    }

    private fun requestStage() {
        val builder = activity?.let { AlertDialog.Builder(it) }
        builder?.setTitle("Select Option")
        builder?.setMessage("Please select an option below")

        builder?.setPositiveButton(getString(R.string.starting)) { dialog, which ->
            dialog.dismiss()
            findNavController().navigate(
                MaternityFragmentDirections.navigateToScreening(
                    args.patientId, "maternal-first-time-registration.json", "Maternity Unit"
                )
            )
        }

        builder?.setNegativeButton(getString(R.string.existing)) { dialog, which ->
            dialog.dismiss()
            findNavController().navigate(
                MaternityFragmentDirections.navigateToScreening(
                    args.patientId, "maternal-maternity-registration.json", "Maternity Unit"
                )
            )
        }
        builder?.show()
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