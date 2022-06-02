package com.intellisoft.nndak.screens.dashboard.child

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.R
import com.intellisoft.nndak.adapters.BabyItemAdapter
import com.intellisoft.nndak.adapters.PrescriptionAdapter
import com.intellisoft.nndak.databinding.FragmentBabyFeedsBinding
import com.intellisoft.nndak.databinding.FragmentChildDashboardBinding
import com.intellisoft.nndak.models.PrescriptionItem
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModel
import com.intellisoft.nndak.viewmodels.PatientDetailsViewModelFactory
import timber.log.Timber

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BabyFeedsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BabyFeedsFragment : Fragment() {
    private var _binding: FragmentBabyFeedsBinding? = null
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private val args: BabyAssessmentFragmentArgs by navArgs()
    private val binding
        get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBabyFeedsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = resources.getString(R.string.app_dashboard)

            setHomeAsUpIndicator(R.drawable.dash)
            setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)
        (activity as MainActivity).setDrawerEnabled(true)



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
        patientDetailsViewModel.getMumChild()
        patientDetailsViewModel.getCurrentPrescriptions()
        patientDetailsViewModel.liveMumChild.observe(viewLifecycleOwner) { motherBabyItem ->

            if (motherBabyItem != null) {
                binding.apply {
                    val gest = motherBabyItem.dashboard.gestation ?: ""
                    val status = motherBabyItem.status
                    incDetails.tvBabyName.text = motherBabyItem.babyName
                    incDetails.tvMumName.text = motherBabyItem.motherName
                    incDetails.appBirthWeight.text = motherBabyItem.birthWeight
                    incDetails.appGestation.text = "$gest-$status"
                    incDetails.appApgarScore.text = motherBabyItem.dashboard.apgarScore ?: ""
                    incDetails.appMumIp.text = motherBabyItem.motherIp
                    incDetails.appBabyWell.text = motherBabyItem.dashboard.babyWell ?: ""
                    incDetails.appAsphyxia.text = motherBabyItem.dashboard.asphyxia ?: ""
                    incDetails.appNeonatalSepsis.text = motherBabyItem.dashboard.neonatalSepsis ?: ""
                    incDetails.appJaundice.text = motherBabyItem.dashboard.jaundice ?: ""
                    incDetails.appBirthDate.text = motherBabyItem.dashboard.dateOfBirth ?: ""
                    incDetails.appLifeDay.text = motherBabyItem.dashboard.dayOfLife ?: ""
                    incDetails.appAdmDate.text = motherBabyItem.dashboard.dateOfAdm ?: ""

                }
            }
        }

        /**
         * List of Prescriptions
         */
        val recyclerView: RecyclerView = binding.prescriptionList
        val adapter = PrescriptionAdapter(this::onPrescriptionItemClick)
        recyclerView.adapter = adapter

        patientDetailsViewModel.livePrescriptionsData.observe(viewLifecycleOwner) {
            Timber.d("Prescriptions has " + it.count() + " records")
            if (it.isNotEmpty()) {
                binding.actionUpdatePrescription.visibility = View.VISIBLE
            }
            binding.pbLoading.visibility = View.GONE
            adapter.submitList(it)
        }

        binding.apply {
            actionNewPrescription.setOnClickListener {
                findNavController().navigate(
                    BabyFeedsFragmentDirections.navigateToAddPrescription(
                        args.patientId
                    )
                )
            }
            actionUpdatePrescription.setOnClickListener {
                findNavController().navigate(BabyFeedsFragmentDirections.navigateToEditPrescription())
            }
        }

    }

    private fun onPrescriptionItemClick(prescriptionItem: PrescriptionItem) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                (requireActivity() as MainActivity).openNavigationDrawer()
                true
            }
            else -> false
        }
    }
}

