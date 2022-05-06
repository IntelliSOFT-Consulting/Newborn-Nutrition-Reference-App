package com.intellisoft.nndak.screens.children

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
import com.intellisoft.nndak.adapters.ChildDetails
import com.intellisoft.nndak.databinding.FragmentChildBinding
import com.intellisoft.nndak.utils.Constants.APGAR_SCORE
import com.intellisoft.nndak.utils.Constants.ASSESS_CHILD
import com.intellisoft.nndak.utils.Constants.FEEDING_NEEDS
import com.intellisoft.nndak.utils.Constants.NEWBORN_ADMISSION
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory

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
        val adapter = ChildDetails(false)
        binding.recycler.adapter = adapter
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = args.title
            setDisplayHomeAsUpEnabled(true)
        }
        patientDetailsViewModel.livePatientData.observe(viewLifecycleOwner) { adapter.submitList(it) }
        patientDetailsViewModel.getPatientDetailData(false)
        (activity as MainActivity).setDrawerEnabled(false)
        updateTitles()

        binding.actionScore.setOnClickListener {
            firstButtonClick()
        }
        binding.actionAssess.setOnClickListener {
            lastButtonClick()

        }
    }

    private fun lastButtonClick() {
        when (this.args.code) {
            "0" -> {
                activity?.let {
                    FhirApplication.setCurrent(
                        it,
                        ASSESS_CHILD
                    )
                }
                findNavController().navigate(
                    ChildFragmentDirections.navigateToScreening(
                        args.patientId, "nn-a5.json", "Maternity Unit"
                    )
                )
            }
            "1" -> {
                activity?.let {
                    FhirApplication.setCurrent(
                        it,
                        NEWBORN_ADMISSION
                    )
                }
                findNavController().navigate(
                    ChildFragmentDirections.navigateToScreening(
                        args.patientId, "nn-d4.json", "New Born Unit"
                    )
                )
            }
            else -> {

            }
        }
    }

    private fun firstButtonClick() {

        when (this.args.code) {
            "0" -> {
                activity?.let {
                    FhirApplication.setCurrent(
                        it,
                        APGAR_SCORE
                    )
                }
                findNavController().navigate(
                    ChildFragmentDirections.navigateToScreening(
                        args.patientId, "apgar-score.json", "Apgar Score"
                    )
                )
            }
            "1" -> {
                activity?.let {
                    FhirApplication.setCurrent(
                        it,
                        FEEDING_NEEDS
                    )
                }
                findNavController().navigate(
                    ChildFragmentDirections.navigateToScreening(
                        args.patientId, "nn-e2.json", "Feeding Needs"
                    )
                )
            }
            else -> {

            }
        }
    }

    private fun updateTitles() {

        when (this.args.code) {
            "0" -> {
                binding.actionScore.text = getString(R.string.action_score)
                binding.actionAssess.text = getString(R.string.action_additional)
            }
            "1" -> {
                binding.actionScore.text = getString(R.string.action_feeding_needs)
                binding.actionAssess.text = getString(R.string.action_new_admission)
            }
            else -> {

                binding.actionScore.text = getString(R.string.action_score)
                binding.actionAssess.text = getString(R.string.action_additional)
            }
        }
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