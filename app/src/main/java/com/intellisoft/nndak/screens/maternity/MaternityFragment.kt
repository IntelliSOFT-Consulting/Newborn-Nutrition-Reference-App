package com.intellisoft.nndak.screens.maternity

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.adapters.MaternityDetails
import com.intellisoft.nndak.adapters.PatientDetailsRecyclerViewAdapter
import com.intellisoft.nndak.databinding.FragmentMaternityBinding
import com.intellisoft.nndak.databinding.FragmentNewBornBinding
import com.intellisoft.nndak.models.RelatedPersonItem
import com.intellisoft.nndak.screens.ScreenerFragmentArgs
import com.intellisoft.nndak.screens.newborn.NewBornFragmentArgs
import com.intellisoft.nndak.screens.newborn.NewBornFragmentDirections
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import com.intellisoft.nndak.viewmodels.ScreenerViewModel
import timber.log.Timber

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
        val adapter = MaternityDetails(this::onAddScreenerClick)
        binding.recycler.adapter = adapter
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = "Maternity Unit"
            setDisplayHomeAsUpEnabled(true)
        }
        patientDetailsViewModel.livePatientData.observe(viewLifecycleOwner) { adapter.submitList(it) }
        patientDetailsViewModel.getMaternityDetailData()
        (activity as MainActivity).setDrawerEnabled(false)
        activity?.let {
            FhirApplication.setCurrent(it, newBorn = false, apgar = false, maternity = false)
        }
    }

    private fun onAddScreenerClick(related: RelatedPersonItem) {
        findNavController().navigate(
            MaternityFragmentDirections.navigateToChild(related.id)
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.maternity, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavHostFragment.findNavController(this).navigateUp()
                true
            }
            R.id.menu_maternity -> {

                activity?.let {
                    FhirApplication.setCurrent(
                        it,
                        newBorn = false,
                        apgar = false,
                        maternity = true
                    )
                }
                findNavController().navigate(
                    MaternityFragmentDirections.navigateToScreening(
                        args.patientId, "maternity-registration.json", "Maternity Unit"
                    )
                )
                true
            }
            R.id.menu_apgar_score -> {
                activity?.let {
                    FhirApplication.setCurrent(
                        it,
                        newBorn = false,
                        apgar = true,
                        maternity = false
                    )
                }
                findNavController().navigate(
                    MaternityFragmentDirections.navigateToScreening(
                        args.patientId, "apgar-score.json", "Apgar Score"
                    )
                )
                true
            }
            R.id.menu_new_born -> {
                activity?.let {
                    FhirApplication.setCurrent(
                        it,
                        newBorn = true,
                        apgar = false,
                        maternity = false,
                    )
                }
                findNavController().navigate(
                    MaternityFragmentDirections.navigateToScreening(
                        args.patientId, "child.json", "Maternity Unit"
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